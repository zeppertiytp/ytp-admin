package com.youtopin.vaadin.formengine.registry;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.FormDefinition;
import com.youtopin.vaadin.formengine.definition.OptionsDefinition;
import com.youtopin.vaadin.formengine.definition.SecurityDefinition;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;

/**
 * Tests for {@link FieldRegistry} money field creation.
 */
class FieldRegistryTest {

    @Test
    void moneyFieldOmitsSuffixWhenLocaleHasNoCountry() {
        FieldRegistry registry = new FieldRegistry(new OptionCatalogRegistry());
        FieldDefinition definition = moneyFieldDefinition();
        FieldFactoryContext context = context(new Locale("fa"));

        FieldInstance instance = registry.create(definition, context);
        BigDecimalField field = (BigDecimalField) instance.getValueComponent();

        assertNull(field.getSuffixComponent(), "Suffix should be absent when currency cannot be resolved");
    }

    @Test
    void moneyFieldUsesCurrencyWhenLocaleProvidesCountry() {
        FieldRegistry registry = new FieldRegistry(new OptionCatalogRegistry());
        FieldDefinition definition = moneyFieldDefinition();
        FieldFactoryContext context = context(Locale.forLanguageTag("fa-IR"));

        FieldInstance instance = registry.create(definition, context);
        BigDecimalField field = (BigDecimalField) instance.getValueComponent();

        assertNotNull(field.getSuffixComponent(), "Suffix should be created when currency is available");
    }

    private static FieldFactoryContext context(Locale locale) {
        FormDefinition formDefinition = new FormDefinition("test", "", "", Object.class, List.of(), List.of(), Map.of());
        return new FieldFactoryContext(formDefinition, locale, true, (key, loc) -> "", component -> {});
    }

    private static FieldDefinition moneyFieldDefinition() {
        OptionsDefinition options = new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", false);
        SecurityDefinition security = new SecurityDefinition("", "", List.of(), true);
        return new FieldDefinition("pricing.price", UiField.ComponentType.MONEY, "label", "", "", "", "", "", "", "",
                options, List.of(), List.of(), security, 0, 1, 1);
    }
}

