package com.youtopin.vaadin.formengine.binder;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.function.SerializablePredicate;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.ValidationDefinition;
import com.youtopin.vaadin.formengine.options.OptionItem;
import com.youtopin.vaadin.formengine.registry.FieldInstance;

/**
 * Simplified binder orchestration that converts values and applies custom validation rules.
 */
public final class BinderOrchestrator<T> {

    private final Class<T> beanType;
    private final Map<FieldDefinition, FieldInstance> boundFields = new LinkedHashMap<>();
    private final List<CompletableFuture<?>> asyncValidations = new ArrayList<>();
    private final Function<String, String> messageResolver;

    private static final Pattern LENGTH_PATTERN = Pattern.compile("^length\\s*(<=|>=|<|>)\\s*(\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALUE_COMPARISON_PATTERN = Pattern.compile(
            "^value\\s*(==|!=|>=|<=|>|<)\\s*(-?\\d+(?:\\.\\d+)?)$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern GENERAL_COMPARISON_PATTERN = Pattern.compile(
            "^\\s*(.+?)\\s*(==|!=|>=|<=|>|<)\\s*(.+)\\s*$");

    public BinderOrchestrator(Class<T> beanType, Function<String, String> messageResolver) {
        this.beanType = beanType;
        this.messageResolver = messageResolver != null ? messageResolver : key -> key == null ? "" : key;
    }

    public void bindField(FieldInstance instance, FieldDefinition definition) {
        boundFields.put(definition, instance);
    }

    public void writeBean(T bean) throws ValidationException {
        ValidationContext<T> context = buildDefaultContext(bean);
        writeBean(bean, context);
    }

    public void writeBean(T bean, ValidationContext<T> context) throws ValidationException {
        ValidationContext<T> effectiveContext = context == null ? ValidationContext.empty(bean) : context;
        List<ValidationResult> validationErrors = new ArrayList<>();
        Map<FieldDefinition, Object> pendingWrites = new LinkedHashMap<>();
        for (Map.Entry<FieldDefinition, FieldInstance> entry : boundFields.entrySet()) {
            FieldDefinition definition = entry.getKey();
            FieldInstance fieldInstance = entry.getValue();
            clearValidationState(fieldInstance);
            Object rawValue = ((HasValue<?, ?>) fieldInstance.getValueComponent()).getValue();
            List<String> errorKeys = validate(definition, rawValue, effectiveContext);
            if (!errorKeys.isEmpty()) {
                errorKeys.stream()
                        .map(this::resolveMessage)
                        .forEach(message -> {
                            applyValidationError(fieldInstance, message);
                            validationErrors.add(ValidationResult.error(message));
                        });
                continue;
            }
            Object converted = convertToBean(definition, rawValue);
            pendingWrites.put(definition, converted);
        }
        if (!validationErrors.isEmpty()) {
            asyncValidations.clear();
            throw new ValidationException(List.of(), validationErrors);
        }
        asyncValidations.forEach(CompletableFuture::join);
        asyncValidations.clear();
        for (Map.Entry<FieldDefinition, Object> entry : pendingWrites.entrySet()) {
            writeProperty(entry.getKey().getPath(), bean, entry.getValue());
        }
    }

    public Object convertRawValue(FieldDefinition definition, Object value) {
        return convertToBean(definition, value);
    }

    public Object coerceValueForType(Object value, Class<?> targetType) {
        return coerceValue(value, targetType);
    }

    private List<String> validate(FieldDefinition definition, Object value, ValidationContext<T> context) {
        List<String> messages = new ArrayList<>();
        if (!definition.getRequiredWhen().isBlank() && isEmpty(value)) {
            String key = definition.getRequiredMessageKey().isBlank()
                    ? "forms.validation.required"
                    : definition.getRequiredMessageKey();
            messages.add(key);
        }
        boolean optionalAndEmpty = definition.getRequiredWhen().isBlank() && isEmpty(value);
        for (ValidationDefinition validation : definition.getValidations()) {
            if (optionalAndEmpty) {
                continue;
            }
            if (!validation.getExpression().isBlank()) {
                SerializablePredicate<Object> predicate = candidate ->
                        evaluateExpression(definition, validation.getExpression(), candidate, context);
                if (!predicate.test(value)) {
                    messages.add(validation.getMessageKey());
                }
            }
            if (!validation.getAsyncValidatorBean().isBlank()) {
                asyncValidations.add(CompletableFuture.supplyAsync(() -> {
                    evaluateExpression(definition, "true", value, context);
                    return Boolean.TRUE;
                }));
            }
        }
        return messages;
    }

    private Object convertToBean(FieldDefinition definition, Object value) {
        if (value == null) {
            return null;
        }
        return switch (definition.getComponentType()) {
            case NUMBER -> value;
            case INTEGER -> value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
            case MONEY -> value instanceof BigDecimal ? value : new BigDecimal(String.valueOf(value));
            case SELECT, AUTOCOMPLETE, ENUM -> value instanceof OptionItem option ? option.getId() : String.valueOf(value);
            case MULTI_SELECT, TAGS -> {
                if (value instanceof Set<?> set) {
                    yield set.stream().map(item -> item instanceof OptionItem option ? option.getId() : String.valueOf(item))
                            .toList();
                }
                if (value instanceof List<?> list) {
                    yield list.stream().map(String::valueOf).toList();
                }
                yield List.of();
            }
            default -> value;
        };
    }

    private void writeProperty(String path, T bean, Object value) {
        try {
            String[] segments = path.split("\\.");
            Object current = bean;
            for (int i = 0; i < segments.length - 1; i++) {
                current = accessGetter(current, segments[i]);
                if (current == null) {
                    return;
                }
            }
            accessSetter(current, segments[segments.length - 1], value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to write property " + path, e);
        }
    }

    public Object readProperty(String path, T bean) {
        if (bean == null || path == null || path.isBlank()) {
            return null;
        }
        try {
            Object current = bean;
            String[] segments = path.split("\\.");
            for (String segment : segments) {
                if (current == null) {
                    return null;
                }
                current = accessGetter(current, segment);
            }
            return current;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read property " + path, e);
        }
    }

    private Object accessGetter(Object bean, String property) throws ReflectiveOperationException {
        if (bean instanceof DynamicPropertyBag bag) {
            return bag.readDynamicProperty(property);
        }
        try {
            Method getter = bean.getClass().getMethod("get" + capitalize(property));
            return getter.invoke(bean);
        } catch (NoSuchMethodException ex) {
            Method booleanGetter = bean.getClass().getMethod("is" + capitalize(property));
            return booleanGetter.invoke(bean);
        }
    }

    private void accessSetter(Object bean, String property, Object value) throws ReflectiveOperationException {
        if (bean instanceof DynamicPropertyBag bag && bag.writeDynamicProperty(property, value)) {
            return;
        }
        Method setter = Arrays.stream(bean.getClass().getMethods())
                .filter(method -> method.getName().equals("set" + capitalize(property)))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException("set" + capitalize(property)));
        Class<?> parameterType = setter.getParameterTypes()[0];
        if (value == null) {
            if (parameterType.isPrimitive()) {
                return;
            }
            setter.invoke(bean, value);
            return;
        }
        Object coerced = coerceValue(value, parameterType);
        setter.invoke(bean, coerced);
    }

    private Object coerceValue(Object value, Class<?> targetType) {
        Class<?> boxedType = box(targetType);
        if (boxedType.isInstance(value)) {
            return value;
        }
        if (value instanceof OptionItem option && boxedType == String.class) {
            return option.getId();
        }
        if (boxedType == BigDecimal.class) {
            return toBigDecimal(value);
        }
        if (Number.class.isAssignableFrom(boxedType)) {
            return coerceNumber(value, boxedType);
        }
        if (boxedType == Boolean.class) {
            return toBoolean(value);
        }
        if (boxedType == String.class) {
            return String.valueOf(value);
        }
        if (boxedType.isEnum()) {
            return toEnum(value, boxedType);
        }
        if (java.time.temporal.Temporal.class.isAssignableFrom(boxedType) && value instanceof String str) {
            return toTemporal(str, boxedType);
        }
        if (Collection.class.isAssignableFrom(boxedType) && value instanceof Collection<?> collection) {
            return coerceCollection(collection, boxedType);
        }
        if (Map.class.isAssignableFrom(boxedType) && value instanceof Map<?, ?> map) {
            return new LinkedHashMap<>(map);
        }
        return value;
    }

    private Object coerceCollection(Collection<?> collection, Class<?> targetType) {
        Collection<?> normalized = collection.stream()
                .map(item -> item instanceof OptionItem option ? option.getId() : item)
                .toList();
        if (Set.class.isAssignableFrom(targetType)) {
            return new LinkedHashSet<>(normalized);
        }
        return new ArrayList<>(normalized);
    }

    private Object coerceNumber(Object value, Class<?> targetType) {
        Number number;
        if (value instanceof Number candidate) {
            number = candidate;
        } else if (value instanceof String str && !str.isBlank()) {
            try {
                number = new BigDecimal(str);
            } catch (NumberFormatException ex) {
                return value;
            }
        } else {
            return value;
        }
        if (targetType == Integer.class) {
            return number.intValue();
        }
        if (targetType == Long.class) {
            return number.longValue();
        }
        if (targetType == Short.class) {
            return number.shortValue();
        }
        if (targetType == Byte.class) {
            return number.byteValue();
        }
        if (targetType == Double.class) {
            return number.doubleValue();
        }
        if (targetType == Float.class) {
            return number.floatValue();
        }
        return number;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return new BigDecimal(str);
            } catch (NumberFormatException ex) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String str) {
            return Boolean.parseBoolean(str);
        }
        if (value instanceof Number number) {
            return Math.abs(number.doubleValue()) > 0d;
        }
        return Boolean.FALSE;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object toEnum(Object value, Class<?> enumType) {
        if (value == null) {
            return null;
        }
        String candidate;
        if (enumType.isInstance(value)) {
            return value;
        }
        if (value instanceof Enum<?> enumValue) {
            candidate = enumValue.name();
        } else {
            candidate = String.valueOf(value);
        }
        for (Object constant : enumType.getEnumConstants()) {
            Enum<?> enumConstant = (Enum<?>) constant;
            if (Objects.equals(enumConstant.name(), candidate) || enumConstant.name().equalsIgnoreCase(candidate)) {
                return enumConstant;
            }
        }
        return null;
    }

    private Object toTemporal(String value, Class<?> targetType) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            if (targetType == LocalDateTime.class) {
                return LocalDateTime.parse(value);
            }
            if (targetType == LocalDate.class) {
                return LocalDate.parse(value);
            }
            if (targetType == LocalTime.class) {
                return LocalTime.parse(value);
            }
        } catch (DateTimeParseException ex) {
            return null;
        }
        return null;
    }

    private Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return switch (type.getName()) {
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "short" -> Short.class;
            case "byte" -> Byte.class;
            case "boolean" -> Boolean.class;
            case "char" -> Character.class;
            default -> type;
        };
    }

    private boolean evaluateExpression(FieldDefinition definition,
                                       String expression,
                                       Object value,
                                       ValidationContext<T> context) {
        if (expression == null || expression.isBlank()) {
            return true;
        }
        String trimmed = expression.trim();
        int orIndex = findLogicalOperator(trimmed, "||");
        if (orIndex >= 0) {
            String left = trimmed.substring(0, orIndex);
            String right = trimmed.substring(orIndex + 2);
            return evaluateExpression(definition, left, value, context)
                    || evaluateExpression(definition, right, value, context);
        }
        int andIndex = findLogicalOperator(trimmed, "&&");
        if (andIndex >= 0) {
            String left = trimmed.substring(0, andIndex);
            String right = trimmed.substring(andIndex + 2);
            return evaluateExpression(definition, left, value, context)
                    && evaluateExpression(definition, right, value, context);
        }
        if (trimmed.startsWith("!")) {
            return !evaluateExpression(definition, trimmed.substring(1), value, context);
        }
        if ("notBlank".equalsIgnoreCase(trimmed) || "notEmpty".equalsIgnoreCase(trimmed)) {
            return !isEmpty(value);
        }
        Matcher lengthMatcher = LENGTH_PATTERN.matcher(trimmed);
        if (lengthMatcher.matches()) {
            int threshold = Integer.parseInt(lengthMatcher.group(2));
            int length = value == null ? 0 : String.valueOf(value).length();
            return compare(length, threshold, lengthMatcher.group(1));
        }
        Matcher comparisonMatcher = VALUE_COMPARISON_PATTERN.matcher(trimmed);
        if (comparisonMatcher.matches()) {
            double comparedValue = toDouble(value);
            if (Double.isNaN(comparedValue)) {
                return false;
            }
            double threshold = Double.parseDouble(comparisonMatcher.group(2));
            return compare(comparedValue, threshold, comparisonMatcher.group(1));
        }
        Matcher generalMatcher = GENERAL_COMPARISON_PATTERN.matcher(trimmed);
        if (generalMatcher.matches()) {
            Object left = resolveOperand(definition, generalMatcher.group(1), value, context);
            Object right = resolveOperand(definition, generalMatcher.group(3), value, context);
            return compareOperands(left, right, generalMatcher.group(2));
        }
        Object candidate = resolveOperand(definition, trimmed, value, context);
        return toTruth(candidate);
    }

    private int findLogicalOperator(String expression, String operator) {
        int length = expression.length();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int index = 0; index < length - 1; index++) {
            char current = expression.charAt(index);
            if (current == '\\' && index + 1 < length) {
                index++;
                continue;
            }
            if (current == '\'') {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (current == '"') {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (inSingleQuote || inDoubleQuote) {
                continue;
            }
            if (expression.startsWith(operator, index)) {
                return index;
            }
        }
        return -1;
    }

    private Object resolveOperand(FieldDefinition definition,
                                  String token,
                                  Object value,
                                  ValidationContext<T> context) {
        if (token == null) {
            return null;
        }
        String trimmed = token.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("'") && trimmed.endsWith("'") && trimmed.length() >= 2) {
            return unescape(trimmed.substring(1, trimmed.length() - 1), '\'');
        }
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return unescape(trimmed.substring(1, trimmed.length() - 1), '"');
        }
        if ("null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        if ("true".equalsIgnoreCase(trimmed)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return Boolean.FALSE;
        }
        if ("value".equalsIgnoreCase(trimmed)) {
            return value;
        }
        if (context != null) {
            if ("bean".equalsIgnoreCase(trimmed)) {
                return context.getBean();
            }
            if (trimmed.startsWith("bean.")) {
                return context.read(trimmed);
            }
            Object scoped = context.read(definition, trimmed);
            if (scoped != null || context.containsValue(trimmed)) {
                return scoped;
            }
        }
        if (isNumeric(trimmed)) {
            try {
                return new BigDecimal(trimmed);
            } catch (NumberFormatException ex) {
                return trimmed;
            }
        }
        return trimmed;
    }

    private String unescape(String candidate, char quote) {
        String escapedQuote = "\\" + quote;
        return candidate.replace(escapedQuote, String.valueOf(quote)).replace("\\\\", "\\");
    }

    private boolean isNumeric(String token) {
        try {
            new BigDecimal(token);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean compareOperands(Object left, Object right, String operator) {
        if ("==".equals(operator)) {
            return Objects.equals(normalizeOperand(left), normalizeOperand(right));
        }
        if ("!=".equals(operator)) {
            return !compareOperands(left, right, "==");
        }
        Double leftNumber = toComparableNumber(left);
        Double rightNumber = toComparableNumber(right);
        if (leftNumber != null && rightNumber != null) {
            return compare(leftNumber, rightNumber, operator);
        }
        if (left instanceof Comparable<?> comparable && right != null
                && comparable.getClass().isAssignableFrom(right.getClass())) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            int comparison = ((Comparable) comparable).compareTo(right);
            return switch (operator) {
                case ">" -> comparison > 0;
                case "<" -> comparison < 0;
                case ">=" -> comparison >= 0;
                case "<=" -> comparison <= 0;
                default -> false;
            };
        }
        if (left instanceof String leftStr && right instanceof String rightStr) {
            int comparison = leftStr.compareTo(rightStr);
            return switch (operator) {
                case ">" -> comparison > 0;
                case "<" -> comparison < 0;
                case ">=" -> comparison >= 0;
                case "<=" -> comparison <= 0;
                default -> false;
            };
        }
        return false;
    }

    private Object normalizeOperand(Object operand) {
        if (operand instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros();
        }
        if (operand instanceof Number number) {
            return number.doubleValue();
        }
        if (operand instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return operand;
    }

    private Double toComparableNumber(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.doubleValue();
        }
        if (value instanceof String str && isNumeric(str)) {
            try {
                return new BigDecimal(str).doubleValue();
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private boolean toTruth(Object candidate) {
        if (candidate == null) {
            return false;
        }
        if (candidate instanceof Boolean bool) {
            return bool;
        }
        if (candidate instanceof Number number) {
            return Math.abs(number.doubleValue()) > 0.0000001d;
        }
        if (candidate instanceof String str) {
            return !str.isBlank() && !"false".equalsIgnoreCase(str.trim())
                    && !"0".equals(str.trim());
        }
        return true;
    }

    private ValidationContext<T> buildDefaultContext(T bean) {
        if (boundFields.isEmpty()) {
            return ValidationContext.empty(bean);
        }
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<FieldDefinition, FieldInstance> entry : boundFields.entrySet()) {
            FieldDefinition definition = entry.getKey();
            FieldInstance instance = entry.getValue();
            HasValue<?, ?> component = instance.getValueComponent();
            if (component == null) {
                continue;
            }
            Object raw = component.getValue();
            Object converted = convertRawValue(definition, raw);
            addValue(values, definition.getPath(), converted, null, -1);
        }
        return ValidationContext.of(bean, values, Map.of(), Map.of(),
                (candidate, path) -> candidate == null ? null : readProperty(path, candidate));
    }

    private void addValue(Map<String, Object> target,
                          String path,
                          Object value,
                          String relative,
                          int index) {
        if (path == null || path.isBlank()) {
            return;
        }
        mergeValue(target, path, value);
        if (relative != null && !relative.isBlank() && index >= 0) {
            mergeValue(target, relative + "[" + index + "]", value);
        }
    }

    private void mergeValue(Map<String, Object> target, String key, Object value) {
        if (key == null || key.isBlank()) {
            return;
        }
        Object existing = target.get(key);
        if (existing == null) {
            target.put(key, value);
            return;
        }
        if (existing instanceof List<?> list) {
            List<Object> merged = new ArrayList<>(list);
            merged.add(value);
            target.put(key, merged);
            return;
        }
        List<Object> merged = new ArrayList<>();
        merged.add(existing);
        merged.add(value);
        target.put(key, merged);
    }

    private String capitalize(String property) {
        if (property.isEmpty()) {
            return property;
        }
        return Character.toUpperCase(property.charAt(0)) + property.substring(1);
    }

    private void clearValidationState(FieldInstance instance) {
        boolean handled = instance.getCustomValidationHandler()
                .map(handler -> {
                    handler.clear(instance);
                    return true;
                })
                .orElse(false);
        if (!handled) {
            instance.getDefaultValidationHandler().clear(instance);
        }
    }

    private void applyValidationError(FieldInstance instance, String message) {
        boolean handled = instance.getCustomValidationHandler()
                .map(handler -> handler.apply(instance, message))
                .orElse(false);
        if (!handled) {
            instance.getDefaultValidationHandler().apply(instance, message);
        }
    }

    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String str) {
            return str.isBlank();
        }
        if (value instanceof Collection<?> collection) {
            return collection.isEmpty();
        }
        if (value instanceof Map<?, ?> map) {
            return map.isEmpty();
        }
        return false;
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ex) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }

    private boolean compare(double left, double right, String operator) {
        return switch (operator) {
            case ">" -> left > right;
            case "<" -> left < right;
            case ">=" -> left >= right;
            case "<=" -> left <= right;
            case "==" -> Double.compare(left, right) == 0;
            case "!=" -> Double.compare(left, right) != 0;
            default -> true;
        };
    }

    private String resolveMessage(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String resolved = messageResolver.apply(key);
        if (resolved == null || resolved.isBlank()) {
            return key;
        }
        return resolved;
    }
}
