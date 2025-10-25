package com.youtopin.vaadin.formengine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.youtopin.vaadin.formengine.RepeatableTitleGenerator;

/**
 * Configures repeatable groups rendering lists of entries.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UiRepeatable {

    /**
     * @return Enables repeatable behaviour when set to true.
     */
    boolean enabled() default false;

    /**
     * @return Rendering mode used by the repeatable editor.
     */
    RepeatableMode mode() default RepeatableMode.GRID_EDITOR;

    /**
     * @return Minimum number of items required.
     */
    int min() default 0;

    /**
     * @return Maximum number of items allowed. {@link Integer#MAX_VALUE} disables the limit.
     */
    int max() default Integer.MAX_VALUE;

    /**
     * @return Dot notation expression used to enforce uniqueness among items (e.g. {@code code}). Empty disables uniqueness.
     */
    String uniqueBy() default "";

    /**
     * @return Template string used for item summary badges.
     */
    String summaryTemplate() default "";

    /**
     * @return Message key formatted for individual item titles. Supports {@link java.text.MessageFormat} placeholders where
     * {0} resolves to the index after applying {@link #itemTitleOffset()} and {1} resolves to the zero-based index.
     */
    String itemTitleKey() default "";

    /**
     * @return Offset added to the zero-based index before substituting the value into {@link #itemTitleKey()} templates.
     */
    int itemTitleOffset() default 1;

    /**
     * @return Strategy used to generate item titles. Implementations must provide a public no-argument constructor and can
     * leverage the provided index to build custom numbering.
     */
    Class<? extends RepeatableTitleGenerator> titleGenerator() default RepeatableTitleGenerator.Default.class;

    /**
     * @return Enables drag and drop reordering.
     */
    boolean allowReorder() default true;

    /**
     * @return Enables duplication of an existing entry.
     */
    boolean allowDuplicate() default true;

    /**
     * Rendering modes supported for repeatable groups.
     */
    enum RepeatableMode {
        NONE,
        GRID_EDITOR,
        CARD_DIALOG,
        INLINE_PANEL
    }
}
