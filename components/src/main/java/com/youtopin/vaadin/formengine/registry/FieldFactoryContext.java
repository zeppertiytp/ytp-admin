package com.youtopin.vaadin.formengine.registry;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.youtopin.vaadin.formengine.definition.FormDefinition;

/**
 * Context information provided to field factories.
 */
public final class FieldFactoryContext {

    private final FormDefinition formDefinition;
    private final Locale locale;
    private final boolean rtl;
    private final BiFunction<String, Locale, String> messageResolver;
    private final Consumer<Component> themeTokenApplier;

    public FieldFactoryContext(FormDefinition formDefinition,
                               Locale locale,
                               boolean rtl,
                               BiFunction<String, Locale, String> messageResolver,
                               Consumer<Component> themeTokenApplier) {
        this.formDefinition = Objects.requireNonNull(formDefinition, "formDefinition");
        this.locale = Objects.requireNonNull(locale, "locale");
        this.rtl = rtl;
        this.messageResolver = Objects.requireNonNull(messageResolver, "messageResolver");
        this.themeTokenApplier = Objects.requireNonNull(themeTokenApplier, "themeTokenApplier");
    }

    public FormDefinition getFormDefinition() {
        return formDefinition;
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean isRtl() {
        return rtl;
    }

    public String translate(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        return messageResolver.apply(key, locale);
    }

    public void applyTheme(Component component) {
        themeTokenApplier.accept(component);
    }
}
