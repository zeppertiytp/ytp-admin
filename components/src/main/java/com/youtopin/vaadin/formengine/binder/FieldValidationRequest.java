package com.youtopin.vaadin.formengine.binder;

import java.util.Objects;

import com.youtopin.vaadin.formengine.definition.FieldDefinition;

import lombok.Getter;

/**
 * Payload supplied to validator beans for context-aware checks.
 *
 * @param <T>
 *            bean type managed by the orchestrator
 */
@Getter
public final class FieldValidationRequest<T> {

    private final FieldDefinition fieldDefinition;
    private final Object value;
    private final ValidationContext<T> context;

    public FieldValidationRequest(FieldDefinition fieldDefinition, Object value, ValidationContext<T> context) {
        this.fieldDefinition = Objects.requireNonNull(fieldDefinition, "fieldDefinition");
        this.value = value;
        this.context = context;
    }

}
