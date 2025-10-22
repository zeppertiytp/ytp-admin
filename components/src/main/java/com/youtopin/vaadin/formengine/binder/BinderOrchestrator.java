package com.youtopin.vaadin.formengine.binder;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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

    public BinderOrchestrator(Class<T> beanType, Object ignored) {
        this.beanType = beanType;
    }

    public void bindField(FieldInstance instance, FieldDefinition definition) {
        boundFields.put(definition, instance);
    }

    public void writeBean(T bean) throws ValidationException {
        for (Map.Entry<FieldDefinition, FieldInstance> entry : boundFields.entrySet()) {
            FieldDefinition definition = entry.getKey();
            FieldInstance fieldInstance = entry.getValue();
            Object rawValue = ((HasValue<?, ?>) fieldInstance.getValueComponent()).getValue();
            validate(definition, rawValue);
            Object converted = convertToBean(definition, rawValue);
            writeProperty(definition.getPath(), bean, converted);
        }
        asyncValidations.forEach(CompletableFuture::join);
        asyncValidations.clear();
    }

    private void validate(FieldDefinition definition, Object value) throws ValidationException {
        for (ValidationDefinition validation : definition.getValidations()) {
            if (!validation.getExpression().isBlank()) {
                SerializablePredicate<Object> predicate = val -> evaluateExpression(validation.getExpression(), val);
                if (!predicate.test(value)) {
                    throw new ValidationException(List.of(), List.of(ValidationResult.error(validation.getMessageKey())));
                }
            }
            if (!validation.getAsyncValidatorBean().isBlank()) {
                asyncValidations.add(CompletableFuture.supplyAsync(() -> Boolean.TRUE));
            }
        }
        if (!definition.getRequiredWhen().isBlank() && (value == null || String.valueOf(value).isBlank())) {
            throw new ValidationException(List.of(), List.of(ValidationResult.error(definition.getRequiredMessageKey())));
        }
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
        if (value == null) {
            return false;
        }
        if (value instanceof String str) {
            return !str.isBlank();
        }
        return true;
    }

    private String capitalize(String property) {
        if (property.isEmpty()) {
            return property;
        }
        return Character.toUpperCase(property.charAt(0)) + property.substring(1);
    }
}
