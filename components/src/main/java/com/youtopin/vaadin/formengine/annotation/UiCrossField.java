package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a cross field rule evaluated after individual field validation.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiCrossField {

    /**
     * @return Expression evaluated by the state engine; returning true produces an error.
     */
    String expression();

    /**
     * @return Message key displayed when the expression evaluates to true.
     */
    String messageKey();

    /**
     * @return Bean Validation groups targeted by this rule.
     */
    Class<?>[] groups() default {};

    /**
     * @return Optional list of field paths whose errors should show the message.
     */
    String[] targetPaths() default {};
}
