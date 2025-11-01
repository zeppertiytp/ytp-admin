package com.youtopin.vaadin.formengine.binder;

import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous validator bean executed during binder submission.
 *
 * @param <T>
 *            bean type managed by the orchestrator
 */
@FunctionalInterface
public interface AsyncFieldValidator<T> {

    /**
     * Performs validation asynchronously.
     *
     * @param request
     *            validation request containing metadata and context values
     * @return future resolving to {@code true} when the value is valid
     */
    CompletableFuture<Boolean> validateAsync(FieldValidationRequest<T> request);
}
