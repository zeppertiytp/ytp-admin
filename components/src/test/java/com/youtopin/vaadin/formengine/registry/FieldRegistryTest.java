package com.youtopin.vaadin.formengine.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.definition.CrossFieldValidationDefinition;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.FormDefinition;
import com.youtopin.vaadin.formengine.definition.OptionsDefinition;
import com.youtopin.vaadin.formengine.definition.SecurityDefinition;
import com.youtopin.vaadin.formengine.definition.ValidationDefinition;
import com.youtopin.vaadin.formengine.options.OptionCatalog;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;
import com.youtopin.vaadin.formengine.options.OptionItem;
import com.youtopin.vaadin.formengine.options.OptionPage;
import com.youtopin.vaadin.formengine.options.SearchQuery;

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

    @Test
    void selectFieldCreatesOptionWhenCatalogSupportsIt() {
        RecordingCatalog catalog = RecordingCatalog.supported(new OptionItem("alpha", "Alpha"));
        OptionCatalogRegistry registry = new OptionCatalogRegistry();
        registry.register("callback", catalog);
        FieldRegistry fieldRegistry = new FieldRegistry(registry);
        FieldDefinition definition = selectableFieldDefinition(UiField.ComponentType.SELECT, true);
        FieldFactoryContext context = context(Locale.ENGLISH);

        FieldInstance instance = fieldRegistry.create(definition, context);
        ComboBox<OptionItem> combo = (ComboBox<OptionItem>) instance.getValueComponent();

        assertTrue(combo.isAllowCustomValue(), "Custom values should be allowed when allowCreate is enabled");

        ComponentUtil.fireEvent(combo, new ComboBox.CustomValueSetEvent<>(combo, false, "Alpha"));

        OptionItem value = combo.getValue();
        assertNotNull(value, "Created option should be selected");
        assertEquals("alpha", value.getId());
        assertEquals("Alpha", value.getLabel());
        assertEquals("Alpha", catalog.getLastValue());
        assertEquals(Locale.ENGLISH, catalog.getLastLocale());
        assertEquals(Map.of(
                "formId", "test",
                "fieldPath", definition.getPath(),
                "componentType", definition.getComponentType().name(),
                "allowCreateFormId", definition.getOptionsDefinition().getAllowCreateFormId()), catalog.getLastContext());
    }

    @Test
    void selectFieldShowsErrorWhenCatalogRejectsCreation() {
        RecordingCatalog catalog = RecordingCatalog.unsupported();
        OptionCatalogRegistry registry = new OptionCatalogRegistry();
        registry.register("callback", catalog);
        FieldRegistry fieldRegistry = new FieldRegistry(registry);
        FieldDefinition definition = selectableFieldDefinition(UiField.ComponentType.SELECT, true);
        FieldFactoryContext context = context(Locale.ENGLISH);

        FieldInstance instance = fieldRegistry.create(definition, context);
        ComboBox<OptionItem> combo = (ComboBox<OptionItem>) instance.getValueComponent();

        ComponentUtil.fireEvent(combo, new ComboBox.CustomValueSetEvent<>(combo, false, "Alpha"));

        assertTrue(combo.isInvalid(), "ComboBox should be marked invalid when creation is rejected");
        assertEquals("Option creation is not supported", combo.getErrorMessage());
        assertEquals(0, catalog.getCreateInvocations(), "Catalog#create should not be called when creation is unsupported");
    }

    @Test
    void multiSelectFieldMergesNewOptionOnCreation() {
        RecordingCatalog catalog = RecordingCatalog.supported(new OptionItem("beta", "Beta"));
        OptionCatalogRegistry registry = new OptionCatalogRegistry();
        registry.register("callback", catalog);
        FieldRegistry fieldRegistry = new FieldRegistry(registry);
        FieldDefinition definition = selectableFieldDefinition(UiField.ComponentType.MULTI_SELECT, true);
        FieldFactoryContext context = context(Locale.ENGLISH);

        FieldInstance instance = fieldRegistry.create(definition, context);
        MultiSelectComboBox<OptionItem> field = (MultiSelectComboBox<OptionItem>) instance.getValueComponent();
        field.setValue(Set.of(new OptionItem("alpha", "Alpha")));

        ComponentUtil.fireEvent(field, new MultiSelectComboBox.CustomValueSetEvent<>(field, false, "Beta"));

        Set<OptionItem> selectedItems = field.getValue();
        assertEquals(2, selectedItems.size(), "Newly created option should be merged into selection");
        assertTrue(selectedItems.stream().anyMatch(item -> "beta".equals(item.getId())));
        assertEquals(Map.of(
                "formId", "test",
                "fieldPath", definition.getPath(),
                "componentType", definition.getComponentType().name(),
                "allowCreateFormId", definition.getOptionsDefinition().getAllowCreateFormId()), catalog.getLastContext());
    }

    @Test
    void multiSelectFieldSurfacesCreationFailure() {
        RecordingCatalog catalog = RecordingCatalog.throwing(new RuntimeException("Boom"));
        OptionCatalogRegistry registry = new OptionCatalogRegistry();
        registry.register("callback", catalog);
        FieldRegistry fieldRegistry = new FieldRegistry(registry);
        FieldDefinition definition = selectableFieldDefinition(UiField.ComponentType.MULTI_SELECT, true);
        FieldFactoryContext context = context(Locale.ENGLISH);

        FieldInstance instance = fieldRegistry.create(definition, context);
        MultiSelectComboBox<OptionItem> field = (MultiSelectComboBox<OptionItem>) instance.getValueComponent();
        field.setValue(Set.of(new OptionItem("alpha", "Alpha")));

        ComponentUtil.fireEvent(field, new MultiSelectComboBox.CustomValueSetEvent<>(field, false, "Gamma"));

        assertTrue(field.isInvalid(), "Component should be marked invalid when creation fails");
        assertEquals("Boom", field.getErrorMessage());
        assertEquals(1, catalog.getCreateInvocations(), "Catalog#create should be invoked when supported");
        assertEquals(1, field.getValue().size(), "Selection should remain unchanged on failure");
    }

    private static FieldFactoryContext context(Locale locale) {
        FormDefinition formDefinition = new FormDefinition("test", "", "", Object.class, List.of(), List.of(), Map.of());
        return new FieldFactoryContext(formDefinition, locale, true, (key, loc) -> "", component -> {});
    }

    private static FieldDefinition moneyFieldDefinition() {
        OptionsDefinition options = new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", false);
        SecurityDefinition security = new SecurityDefinition("", "", List.of(), true);
        return new FieldDefinition(
                "pricing.price",
                UiField.ComponentType.MONEY,
                "label",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                options,
                List.<ValidationDefinition>of(),
                List.<CrossFieldValidationDefinition>of(),
                security,
                0,
                1,
                1);
    }

    private static FieldDefinition selectableFieldDefinition(UiField.ComponentType componentType, boolean allowCreate) {
        OptionsDefinition options = new OptionsDefinition(true, UiOptions.ProviderType.CALLBACK, List.of(), "",
                "callback", "", "", allowCreate, "create-form", false);
        SecurityDefinition security = new SecurityDefinition("", "", List.of(), true);
        return new FieldDefinition(
                "options." + componentType.name().toLowerCase(Locale.ROOT),
                componentType,
                "label",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                options,
                List.<ValidationDefinition>of(),
                List.<CrossFieldValidationDefinition>of(),
                security,
                0,
                1,
                1);
    }

    private static final class RecordingCatalog implements OptionCatalog {

        private final boolean supportsCreate;
        private final OptionItem item;
        private final RuntimeException failure;
        private int createInvocations;
        private String lastValue;
        private Locale lastLocale;
        private Map<String, Object> lastContext = Map.of();

        private RecordingCatalog(boolean supportsCreate, OptionItem item, RuntimeException failure) {
            this.supportsCreate = supportsCreate;
            this.item = item;
            this.failure = failure;
        }

        static RecordingCatalog supported(OptionItem item) {
            return new RecordingCatalog(true, item, null);
        }

        static RecordingCatalog unsupported() {
            return new RecordingCatalog(false, null, null);
        }

        static RecordingCatalog throwing(RuntimeException failure) {
            return new RecordingCatalog(true, null, failure);
        }

        @Override
        public OptionPage fetch(SearchQuery query) {
            return OptionPage.empty();
        }

        @Override
        public List<OptionItem> byIds(java.util.Collection<String> ids) {
            return List.of();
        }

        @Override
        public boolean supportsCreate() {
            return supportsCreate;
        }

        @Override
        public OptionItem create(String value, Locale locale, Map<String, Object> context) {
            createInvocations++;
            lastValue = value;
            lastLocale = locale;
            lastContext = context;
            if (failure != null) {
                throw failure;
            }
            return item;
        }

        int getCreateInvocations() {
            return createInvocations;
        }

        String getLastValue() {
            return lastValue;
        }

        Locale getLastLocale() {
            return lastLocale;
        }

        Map<String, Object> getLastContext() {
            return lastContext;
        }
    }
}

