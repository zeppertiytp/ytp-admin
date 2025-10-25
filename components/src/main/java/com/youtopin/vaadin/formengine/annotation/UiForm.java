package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares an annotated class as the root metadata for a form definition.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiForm {

    /**
     * @return Stable identifier for the form definition. Used for persistence and security routing.
     */
    String id();

    /**
     * @return I18N message key for the human readable title shown above the form. Fallbacks to {@link #id()} if empty.
     */
    String titleKey() default "";

    /**
     * @return I18N message key for the form helper/description text. Empty string omits helper text.
     */
    String descriptionKey() default "";

    /**
     * @return Type of the bean managed by Vaadin Binder. Used for nested property resolution and Bean Validation groups.
     */
    Class<?> bean();

    /**
     * @return Sections composing this form. Section classes must be annotated with {@link UiSection}.
     */
    Class<?>[] sections();

    /**
     * @return Action metadata describing buttons rendered with the form.
     */
    UiAction[] actions() default {@UiAction(id = "submit", labelKey = "form.submit")};

    /**
     * @return Lifecycle hook identifiers executed by the engine (beforeLoad, afterLoad, beforeSubmit, afterSubmit).
     */
    String[] lifecycleHooks() default {};
}
