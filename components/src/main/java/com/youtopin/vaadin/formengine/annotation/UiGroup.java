package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a layout group controlling column layout and field composition.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiGroup {

    /**
     * @return Stable identifier for the group used in expression rules.
     */
    String id();

    /**
     * @return Message key for the group caption. Empty value hides caption.
     */
    String titleKey() default "";

    /**
     * @return Number of columns used by this group when rendered.
     */
    int columns() default 1;

    /**
     * @return Repeatable configuration for this group. Empty configuration disables repeatable behaviour.
     */
    UiRepeatable repeatable() default @UiRepeatable;

    /**
     * @return Subform configuration applied when {@link UiField#component()} is {@link UiField.ComponentType#SUBFORM}.
     */
    UiSubform subform() default @UiSubform;

    /**
     * @return Expression determining when the group should render its children as read-only.
     */
    String readOnlyWhen() default "";
}
