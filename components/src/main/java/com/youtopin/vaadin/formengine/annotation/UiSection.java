package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a logical section grouping field groups together in the generated layout.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiSection {

    /**
     * @return Stable identifier for the section. Used in state expressions and visibility guards.
     */
    String id();

    /**
     * @return Message key for the section header title. If empty the identifier will be used.
     */
    String titleKey() default "";

    /**
     * @return Message key for helper text displayed under the section title.
     */
    String descriptionKey() default "";

    /**
     * @return Field group classes belonging to the section. Classes must carry {@link UiGroup} annotation.
     */
    Class<?>[] groups();

    /**
     * @return Optional boolean expression controlling the section visibility. Evaluated by the state engine.
     */
    String visibleWhen() default "";

    /**
     * @return Expression determining when the section content should be rendered read-only.
     */
    String readOnlyWhen() default "";

    /**
     * @return Optional identifier of the security guard declared via {@link UiSecurity}.
     */
    String securityGuard() default "";

    /**
     * @return Sort order for rendering. Lower numbers appear first.
     */
    int order() default 0;
}
