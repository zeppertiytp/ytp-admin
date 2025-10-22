package com.youtopin.vaadin.formengine.options;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.OptionsDefinition;
import com.youtopin.vaadin.formengine.definition.SecurityDefinition;
import com.youtopin.vaadin.formengine.definition.ValidationDefinition;

class OptionCatalogRegistryTest {

    private final OptionCatalogRegistry registry = new OptionCatalogRegistry();

    @Test
    void resolvesStaticOptions() {
        FieldDefinition definition = new FieldDefinition("status", UiField.ComponentType.SELECT, "status", "", "",
                "", "", "", "", "", new OptionsDefinition(true, UiOptions.ProviderType.STATIC,
                        java.util.List.of("A|Active", "I|Inactive"), "", "", "", "", false, "", true),
                java.util.List.of(), java.util.List.of(), new SecurityDefinition("", "", java.util.List.of(), false), 0, 1, 1);
        OptionCatalog catalog = registry.resolve(definition, Locale.forLanguageTag("fa-IR"));
        OptionPage page = catalog.fetch(new SearchQuery("", 0, 10, Locale.forLanguageTag("fa-IR"), java.util.Map.of()));
        assertThat(page.getItems()).extracting(OptionItem::getId).containsExactly("A", "I");
    }
}
