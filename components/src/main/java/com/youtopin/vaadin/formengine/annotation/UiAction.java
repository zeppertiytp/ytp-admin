package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a form action rendered as a button in the generated form.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiAction {

    /**
     * @return Stable identifier for the action. Used when binding click listeners.
     */
    String id();

    /**
     * @return I18N message key for the button caption.
     */
    String labelKey();

    /**
     * @return Optional I18N key providing assistive description text.
     */
    String descriptionKey() default "";

    /**
     * @return Optional expression controlling visibility. Empty string keeps the action visible.
     */
    String visibleWhen() default "";

    /**
     * @return Optional expression controlling enablement. Empty string keeps the action enabled.
     */
    String enabledWhen() default "";

    /**
     * @return Placement of the button in the rendered layout.
     */
    Placement placement() default Placement.FOOTER;

    /**
     * @return Target section identifier used when {@link #placement()} is {@link Placement#SECTION_FOOTER}.
     */
    String sectionId() default "";

    /**
     * @return Execution mode for the action.
     */
    ActionType type() default ActionType.SUBMIT;

    /**
     * @return Sort order of the action relative to other buttons in the same placement.
     */
    int order() default 0;

    /**
     * @return Optional security guard definition applied to this action.
     */
    UiSecurity security() default @UiSecurity;

    /**
     * Supported placements for rendering action buttons.
     */
    enum Placement {
        HEADER,
        FOOTER,
        SECTION_FOOTER
    }

    /**
     * Supported execution modes for actions.
     */
    enum ActionType {
        SUBMIT,
        SECONDARY
    }
}
