package com.youtopin.vaadin.formengine.binder;

/**
 * Synchronous validator bean executed during binder submission.
 *
 * @param <T>
 *            bean type managed by the orchestrator
 */
@FunctionalInterface
public interface FieldValidator<T> {

    /**
     * Performs validation for the supplied request.
     *
     * @param request
     *            validation request containing metadata and context values
     * @return {@code true} when the value is valid, {@code false} otherwise
     */
    boolean validate(FieldValidationRequest<T> request);
}
