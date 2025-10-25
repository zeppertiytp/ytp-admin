package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures nested subforms used either inline or within dialogs.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiSubform {

    /**
     * @return Enables the subform configuration when true.
     */
    boolean enabled() default false;

    /**
     * @return Identifier of the form definition used to render the subform contents.
     */
    String formId() default "";

    /**
     * @return Presentation mode for the subform.
     */
    SubformMode mode() default SubformMode.DIALOG;

    /**
     * @return When true the subform is shown immediately when the parent is rendered.
     */
    boolean autoOpen() default false;

    /**
     * Presentation modes supported for subforms.
     */
    enum SubformMode {
        INLINE,
        DIALOG
    }
}
