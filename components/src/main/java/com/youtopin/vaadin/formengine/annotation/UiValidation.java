package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares additional validation metadata attached to a {@link UiField}.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiValidation {

    /**
     * @return Message key used when the validator reports an error.
     */
    String messageKey();

    /**
     * @return Expression evaluated by the state engine. A truthy value marks the field invalid.
     */
    String expression() default "";

    /**
     * @return Bean Validation group classes activated when this rule should run.
     */
    Class<?>[] groups() default {};

    /**
     * @return Bean name of a synchronous validator that inspects the current field value.
     */
    String validatorBean() default "";

    /**
     * @return Fully qualified name of a validator bean implementing asynchronous validation (CompletableFuture based).
     */
    String asyncValidatorBean() default "";
}
