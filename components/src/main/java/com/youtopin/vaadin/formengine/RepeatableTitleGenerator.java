package com.youtopin.vaadin.formengine;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

import com.youtopin.vaadin.formengine.definition.GroupDefinition;
import com.youtopin.vaadin.formengine.definition.RepeatableDefinition;
import com.youtopin.vaadin.formengine.registry.FieldFactoryContext;

/**
 * Generates titles for repeatable group entries.
 */
@FunctionalInterface
public interface RepeatableTitleGenerator {

    /**
     * Generates a localized title for the given entry index.
     *
     * @param index       zero-based position of the entry within the repeatable group
     * @param context     rendering context supplying translation helpers
     * @param group       group definition associated with the repeatable
     * @param repeatable  repeatable configuration for the entry collection
     * @return localized caption for the entry
     */
    String generate(int index, FieldFactoryContext context, GroupDefinition group, RepeatableDefinition repeatable);

    /**
     * Default implementation that uses {@link RepeatableDefinition#getItemTitleKey()} or the parent group title to produce a
     * caption. The template is formatted with {@link MessageFormat} so that argument {@code {0}} resolves to the index after
     * applying {@link RepeatableDefinition#getItemTitleOffset()} and argument {@code {1}} resolves to the zero-based index.
     */
    final class Default implements RepeatableTitleGenerator {

        @Override
        public String generate(int index, FieldFactoryContext context, GroupDefinition group, RepeatableDefinition repeatable) {
            Objects.requireNonNull(context, "context");
            Objects.requireNonNull(group, "group");
            Objects.requireNonNull(repeatable, "repeatable");
            int displayIndex = index + repeatable.getItemTitleOffset();
            String patternKey = repeatable.getItemTitleKey();
            String pattern;
            if (patternKey != null && !patternKey.isBlank()) {
                pattern = safeString(context.translate(patternKey));
            } else {
                pattern = resolveGroupTitle(context, group);
            }
            if (!pattern.isBlank() && pattern.contains("{") && pattern.contains("}")) {
                MessageFormat formatter = new MessageFormat(pattern, resolveLocale(context));
                return formatter.format(new Object[]{displayIndex, index});
            }
            if (pattern.isBlank()) {
                return String.valueOf(displayIndex);
            }
            return pattern.trim() + " " + displayIndex;
        }

        private String resolveGroupTitle(FieldFactoryContext context, GroupDefinition group) {
            String translated = safeString(context.translate(group.getTitleKey()));
            if (!translated.isBlank()) {
                return translated;
            }
            return safeString(group.getTitle());
        }

        private String safeString(String value) {
            return value == null ? "" : value;
        }

        private Locale resolveLocale(FieldFactoryContext context) {
            Locale locale = context.getLocale();
            return locale == null ? Locale.getDefault() : locale;
        }
    }
}
