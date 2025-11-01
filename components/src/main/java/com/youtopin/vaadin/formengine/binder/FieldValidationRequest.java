package com.youtopin.vaadin.formengine.binder;

import java.util.Objects;

import com.youtopin.vaadin.formengine.definition.FieldDefinition;

/**
 * Payload supplied to validator beans for context-aware checks.
 *
 * @param <T>
 *            bean type managed by the orchestrator
 */
public final class FieldValidationRequest<T> {

    private final FieldDefinition fieldDefinition;
    private final Object value;
    private final ValidationContext<T> context;

    public FieldValidationRequest(FieldDefinition fieldDefinition, Object value, ValidationContext<T> context) {
        this.fieldDefinition = Objects.requireNonNull(fieldDefinition, "fieldDefinition");
        this.value = value;
        this.context = context;
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public Object getValue() {
        return value;
    }

    public ValidationContext<T> getContext() {
        return context;
    }
}
