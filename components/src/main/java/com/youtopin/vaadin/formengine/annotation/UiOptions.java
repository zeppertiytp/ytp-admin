package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares option provider configuration for option aware components (selects, radios, autocompletes).
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiOptions {

    /**
     * @return Activates the option provider when true.
     */
    boolean enabled() default false;

    /**
     * @return Type of option provider to use.
     */
    ProviderType type() default ProviderType.STATIC;

    /**
     * @return Static entries (for {@link ProviderType#STATIC}) encoded as {@code value|label} pairs.
     */
    String[] entries() default {};

    /**
     * @return Fully qualified enum class name when {@link ProviderType#ENUM} is used.
     */
    String enumType() default "";

    /**
     * @return Spring bean name or class reference when using {@link ProviderType#CALLBACK}.
     */
    String callbackRef() default "";

    /**
     * @return Remote endpoint identifier resolved by the demo application for {@link ProviderType#REMOTE}.
     */
    String remoteRef() default "";

    /**
     * @return Parent field path used by cascading providers.
     */
    String cascadeFrom() default "";

    /**
     * @return When true the component allows creating new records through a subform dialog.
     */
    boolean allowCreate() default false;

    /**
     * @return Subform id used when {@link #allowCreate()} is enabled.
     */
    String allowCreateFormId() default "";

    /**
     * @return Enables client side filtering of fetched data.
     */
    boolean clientFilter() default true;

    /**
     * Option provider types supported by the engine.
     */
    enum ProviderType {
        STATIC,
        ENUM,
        CALLBACK,
        REMOTE,
        CASCADING
    }
}
