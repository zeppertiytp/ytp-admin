package com.youtopin.vaadin.formengine.binder;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValidation;
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

    public BinderOrchestrator(Class<T> beanType, Function<String, String> messageResolver) {
        this.beanType = beanType;
        this.messageResolver = messageResolver != null ? messageResolver : key -> key == null ? "" : key;
    }

    public void bindField(FieldInstance instance, FieldDefinition definition) {
        boundFields.put(definition, instance);
    }

    public void writeBean(T bean) throws ValidationException {
        List<ValidationResult> validationErrors = new ArrayList<>();
        Map<FieldDefinition, Object> pendingWrites = new LinkedHashMap<>();
        for (Map.Entry<FieldDefinition, FieldInstance> entry : boundFields.entrySet()) {
            FieldDefinition definition = entry.getKey();
            FieldInstance fieldInstance = entry.getValue();
            clearValidationState(fieldInstance);
            Object rawValue = ((HasValue<?, ?>) fieldInstance.getValueComponent()).getValue();
            List<String> errorKeys = validate(definition, rawValue);
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

    private List<String> validate(FieldDefinition definition, Object value) {
        List<String> messages = new ArrayList<>();
        if (!definition.getRequiredWhen().isBlank() && isEmpty(value)) {
            String key = definition.getRequiredMessageKey().isBlank()
                    ? "forms.validation.required"
                    : definition.getRequiredMessageKey();
            messages.add(key);
        }
        for (ValidationDefinition validation : definition.getValidations()) {
            if (!validation.getExpression().isBlank()) {
                SerializablePredicate<Object> predicate = candidate -> evaluateExpression(validation.getExpression(), candidate);
                if (!predicate.test(value)) {
                    messages.add(validation.getMessageKey());
                }
            }
            if (!validation.getAsyncValidatorBean().isBlank()) {
                asyncValidations.add(CompletableFuture.supplyAsync(() -> Boolean.TRUE));
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

    private Object accessGetter(Object bean, String property) throws ReflectiveOperationException {
        try {
            Method getter = bean.getClass().getMethod("get" + capitalize(property));
            return getter.invoke(bean);
        } catch (NoSuchMethodException ex) {
            Method booleanGetter = bean.getClass().getMethod("is" + capitalize(property));
            return booleanGetter.invoke(bean);
        }
    }

    private void accessSetter(Object bean, String property, Object value) throws ReflectiveOperationException {
        Method setter = Arrays.stream(bean.getClass().getMethods())
                .filter(method -> method.getName().equals("set" + capitalize(property)))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException("set" + capitalize(property)));
        setter.invoke(bean, value);
    }

    private boolean evaluateExpression(String expression, Object value) {
        if (expression == null || expression.isBlank()) {
            return true;
        }
        String trimmed = expression.trim();
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
        if (value instanceof String str) {
            return !str.isBlank();
        }
        return value != null;
    }

    private String capitalize(String property) {
        if (property.isEmpty()) {
            return property;
        }
        return Character.toUpperCase(property.charAt(0)) + property.substring(1);
    }

    private void clearValidationState(FieldInstance instance) {
        HasValue<?, ?> valueComponent = instance.getValueComponent();
        if (valueComponent instanceof HasValidation hasValidation) {
            hasValidation.setErrorMessage("");
            hasValidation.setInvalid(false);
        }
        if (valueComponent instanceof Component component) {
            component.getElement().removeAttribute("aria-invalid");
            component.getElement().getThemeList().remove("error");
            component.getElement().setProperty("title", "");
        }
        Component component = instance.getComponent();
        if (component instanceof HasValidation hasValidation) {
            hasValidation.setErrorMessage("");
            hasValidation.setInvalid(false);
        }
        component.getElement().removeAttribute("aria-invalid");
        component.getElement().getThemeList().remove("error");
        component.getElement().setProperty("title", "");
    }

    private void applyValidationError(FieldInstance instance, String message) {
        HasValue<?, ?> valueComponent = instance.getValueComponent();
        boolean handled = false;
        if (valueComponent instanceof HasValidation hasValidation) {
            hasValidation.setErrorMessage(message);
            hasValidation.setInvalid(true);
            handled = true;
        }
        Component component = instance.getComponent();
        if (!handled && component instanceof HasValidation hasValidation) {
            hasValidation.setErrorMessage(message);
            hasValidation.setInvalid(true);
            handled = true;
        }
        if (!handled) {
            if (valueComponent instanceof Component valueAsComponent) {
                valueAsComponent.getElement().setProperty("title", message);
                valueAsComponent.getElement().setAttribute("aria-invalid", "true");
                valueAsComponent.getElement().getThemeList().add("error");
            } else {
                component.getElement().setProperty("title", message);
                component.getElement().setAttribute("aria-invalid", "true");
                component.getElement().getThemeList().add("error");
            }
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
