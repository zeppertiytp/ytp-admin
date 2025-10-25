package com.youtopin.vaadin.formengine.scanner;

/**
 * Thrown when annotation scanning fails.
 */
public class FormDefinitionException extends RuntimeException {

    public FormDefinitionException(String message) {
        super(message);
    }

    public FormDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
