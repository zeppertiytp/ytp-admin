package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a single field in the generated form. The annotation can be applied to methods or fields inside a
 * {@link UiGroup}-annotated type.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiField {

    /**
     * @return Dot notation path resolved against the bound bean (for example {@code employee.address.city}).
     */
    String path();

    /**
     * @return Component type to instantiate for the field.
     */
    ComponentType component() default ComponentType.TEXT;

    /**
     * @return I18N key for the field label. Required for accessibility.
     */
    String labelKey();

    /**
     * @return I18N key for helper text rendered under the field.
     */
    String helperKey() default "";

    /**
     * @return I18N key for the placeholder text (if supported by the component).
     */
    String placeholderKey() default "";

    /**
     * @return Expression evaluated by the state engine. When truthy the field becomes required and Binder validation uses the generated message.
     */
    String requiredWhen() default "";

    /**
     * @return Optional message key used when {@link #requiredWhen()} evaluates to true.
     */
    String requiredMessageKey() default "";

    /**
     * @return Expression determining visibility of the field. Empty value keeps it always visible.
     */
    String visibleWhen() default "";

    /**
     * @return Expression determining if the field is enabled. Empty value keeps it always enabled.
     */
    String enabledWhen() default "";

    /**
     * @return Default value expression evaluated when creating new bean instances.
     */
    String defaultValue() default "";

    /**
     * @return Options configuration for select like components.
     */
    UiOptions options() default @UiOptions;

    /**
     * @return Additional validators evaluated by the engine and Vaadin Binder.
     */
    UiValidation[] validations() default {};

    /**
     * @return Cross field rules bound to this field. Use this for conditional uniqueness or dependencies.
     */
    UiCrossField[] crossField() default {};

    /**
     * @return Security guard associated with this field. Defaults to permissive guard.
     */
    UiSecurity security() default @UiSecurity;

    /**
     * @return Order within the group. Lower values are rendered first.
     */
    int order() default 0;

    /**
     * @return Number of columns the field should span inside its group layout.
     */
    int colSpan() default 1;

    /**
     * @return Number of rows the field should span inside its group layout.
     */
    int rowSpan() default 1;

    /**
     * Supported Vaadin component types for the form engine.
     */
    enum ComponentType {
        TEXT,
        TEXT_AREA,
        NUMBER,
        INTEGER,
        MONEY,
        DATE,
        DATETIME,
        TIME,
        JALALI_DATE,
        JALALI_DATE_TIME,
        CHECKBOX,
        SWITCH,
        RADIO,
        SELECT,
        MULTI_SELECT,
        TAGS,
        PASSWORD,
        EMAIL,
        PHONE,
        RICH_TEXT,
        FILE,
        ENUM,
        AUTOCOMPLETE,
        MAP,
        SUBFORM,
        REPEATABLE
    }
}
