package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares security metadata for forms, sections, groups, fields, or actions.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiSecurity {

    /**
     * @return Guard identifier shared across annotations.
     */
    String guardId() default "";

    /**
     * @return Boolean expression executed against the security context to determine access.
     */
    String expression() default "";

    /**
     * @return Explicit authority names required for visibility.
     */
    String[] requiredAuthorities() default {};

    /**
     * @return When true the component is rendered but disabled if access fails; otherwise it is hidden.
     */
    boolean showWhenDenied() default false;
}
