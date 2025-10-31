package com.youtopin.vaadin.formengine.registry;

import java.util.Currency;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.youtopin.vaadin.component.JalaliDateTimePicker;
import com.youtopin.vaadin.component.LocationPicker;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.options.OptionCatalog;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;
import com.youtopin.vaadin.formengine.options.OptionItem;
import com.youtopin.vaadin.formengine.options.OptionPage;
import com.youtopin.vaadin.formengine.options.SearchQuery;
import com.youtopin.vaadin.util.LocaleUtil;

/**
 * Registry mapping component types to configurable factories.
 */
public final class FieldRegistry {

    private final Map<UiField.ComponentType, FieldFactory> factories = new EnumMap<>(UiField.ComponentType.class);
    private final OptionCatalogRegistry optionCatalogRegistry;

    public FieldRegistry(OptionCatalogRegistry optionCatalogRegistry) {
        this.optionCatalogRegistry = Objects.requireNonNull(optionCatalogRegistry, "optionCatalogRegistry");
        registerDefaults();
    }

    public void register(UiField.ComponentType type, FieldFactory factory) {
        factories.put(type, factory);
    }

    public FieldInstance create(FieldDefinition definition, FieldFactoryContext context) {
        FieldFactory factory = factories.get(definition.getComponentType());
        if (factory == null) {
            throw new IllegalStateException("No field factory registered for " + definition.getComponentType());
        }
        FieldInstance instance = factory.create(definition, context);
        applyCommonAttributes(definition, context, instance.getComponent());
        return instance;
    }

    private void registerDefaults() {
        register(UiField.ComponentType.TEXT, (definition, context) -> {
            TextField field = new TextField();
            field.setValueChangeMode(ValueChangeMode.EAGER);
            applyPlaceholder(field, definition, context);
            return wrap(field);
        });
        register(UiField.ComponentType.TEXT_AREA, (definition, context) -> {
            TextArea area = new TextArea();
            area.setValueChangeMode(ValueChangeMode.EAGER);
            area.setMaxHeight("20rem");
            applyPlaceholder(area, definition, context);
            return wrap(area);
        });
        register(UiField.ComponentType.NUMBER, (definition, context) -> {
            String direction = LocaleUtil.isRtl(context.getLocale()) ? "rtl" : "ltr";
            NumberField field = new NumberField();
            field.setValueChangeMode(ValueChangeMode.EAGER);
            field.getElement().setAttribute("inputmode", "decimal");
            field.getElement().setProperty("dir", direction);
            field.getElement().setAttribute("dir", direction);
            return wrap(field);
        });
        register(UiField.ComponentType.INTEGER, (definition, context) -> {
            String direction = LocaleUtil.isRtl(context.getLocale()) ? "rtl" : "ltr";
            IntegerField field = new IntegerField();
            field.setValueChangeMode(ValueChangeMode.EAGER);
            field.getElement().setAttribute("inputmode", "numeric");
            field.getElement().setProperty("dir", direction);
            field.getElement().setAttribute("dir", direction);
            return wrap(field);
        });
        register(UiField.ComponentType.MONEY, (definition, context) -> {
            BigDecimalField field = new BigDecimalField();
            Currency currency = resolveCurrency(context);
            if (currency != null) {
                field.setSuffixComponent(new Span(currency.getSymbol(context.getLocale())));
            }
            field.setValueChangeMode(ValueChangeMode.EAGER);
            field.getElement().setProperty("dir", "ltr");
            field.getElement().setAttribute("dir", "ltr");
            return wrap(field);
        });
        register(UiField.ComponentType.DATE, (definition, context) -> wrap(new DatePicker()));
        register(UiField.ComponentType.DATETIME, (definition, context) -> wrap(new DateTimePicker()));
        register(UiField.ComponentType.TIME, (definition, context) -> {
            String direction = LocaleUtil.isRtl(context.getLocale()) ? "rtl" : "ltr";
            TimePicker picker = new TimePicker();
            picker.setStep(java.time.Duration.ofMinutes(5));
            picker.getElement().setProperty("dir", "ltr");
            picker.getElement().setAttribute("dir", direction);
            return wrap(picker);
        });
        register(UiField.ComponentType.JALALI_DATE, (definition, context) -> {
            JalaliDateTimePicker picker = new JalaliDateTimePicker();
            picker.setPickerVariant(JalaliDateTimePicker.PickerVariant.DATE);
            String placeholder = context.translate(definition.getPlaceholderKey());
            if (!placeholder.isBlank()) {
                picker.setOpenButtonLabel(placeholder);
            }
            return wrap(picker);
        });
        register(UiField.ComponentType.JALALI_DATE_TIME, (definition, context) -> {
            JalaliDateTimePicker picker = new JalaliDateTimePicker();
            picker.setPickerVariant(JalaliDateTimePicker.PickerVariant.DATE_TIME);
            String placeholder = context.translate(definition.getPlaceholderKey());
            if (!placeholder.isBlank()) {
                picker.setOpenButtonLabel(placeholder);
            }
            return wrap(picker);
        });
        register(UiField.ComponentType.CHECKBOX, (definition, context) -> wrap(new Checkbox()));
        register(UiField.ComponentType.SWITCH, (definition, context) -> {
            Checkbox checkbox = new Checkbox();
            checkbox.getElement().setAttribute("theme", "switch");
            return wrap(checkbox);
        });
        register(UiField.ComponentType.RADIO, this::createRadioGroup);
        register(UiField.ComponentType.SELECT, this::createComboBox);
        register(UiField.ComponentType.MULTI_SELECT, this::createMultiSelectComboBox);
        register(UiField.ComponentType.TAGS, this::createTagsField);
        register(UiField.ComponentType.PASSWORD, (definition, context) -> {
            PasswordField field = new PasswordField();
            field.setRevealButtonVisible(true);
            return wrap(field);
        });
        register(UiField.ComponentType.EMAIL, (definition, context) -> wrap(new EmailField()));
        register(UiField.ComponentType.PHONE, (definition, context) -> {
            String direction = LocaleUtil.isRtl(context.getLocale()) ? "rtl" : "ltr";
            TextField field = new TextField();
            field.setPattern("[0-9()+\\- ]*");
            field.setAllowedCharPattern("[0-9()+\\- ]");
            field.getElement().setProperty("dir", "ltr");
            field.getElement().setAttribute("dir", direction);
            return wrap(field);
        });
        register(UiField.ComponentType.FILE, this::createFileUpload);
        register(UiField.ComponentType.ENUM, this::createComboBox);
        register(UiField.ComponentType.AUTOCOMPLETE, this::createComboBox);
        register(UiField.ComponentType.SUBFORM, (definition, context) -> wrap(new SubformLauncher(definition.getLabelKey(), context)));
        register(UiField.ComponentType.MAP, (definition, context) -> {
            String triggerLabel = context.translate(definition.getPlaceholderKey());
            MapLocationField mapField = new MapLocationField(context,
                    triggerLabel.isBlank() ? context.translate("form.pickLocation") : triggerLabel);
            mapField.setTriggerAriaLabel(context.translate(definition.getLabelKey()));
            return new FieldInstance(mapField, mapField, List.of());
        });
        register(UiField.ComponentType.REPEATABLE, (definition, context) -> wrap(new RepeatableField(context)));
    }

    private FieldInstance wrap(HasValue<?, ?> field) {
        return new FieldInstance(field, (Component) field, List.of());
    }

    private void applyCommonAttributes(FieldDefinition definition, FieldFactoryContext context, Component component) {
        String direction = LocaleUtil.isRtl(context.getLocale()) ? "rtl" : "ltr";

        if (component instanceof HasLabel hasLabel) {
            hasLabel.setLabel(context.translate(definition.getLabelKey()));
        } else {
            component.getElement().setProperty("aria-label", context.translate(definition.getLabelKey()));
        }
        if (component instanceof HasHelper hasHelper) {
            hasHelper.setHelperText(context.translate(definition.getHelperKey()));
        }
        component.getElement().setAttribute("aria-describedby", definition.getPath());
        component.getElement().setAttribute("data-path", definition.getPath());
        component.getElement().getStyle().set("color", "var(--lumo-body-text-color)");
        component.getElement().getStyle().set("background-color", "var(--lumo-base-color)");
        if (context.isRtl()) {
            String attrDir = component.getElement().getAttribute("dir");
            String propDir = component.getElement().getProperty("dir");
            boolean hasExplicitDirection = (attrDir != null && !attrDir.isBlank())
                    || (propDir != null && !propDir.isBlank());
            if (!hasExplicitDirection) {
                component.getElement().setAttribute("dir", direction);
            }
        }
        context.applyTheme(component);
    }

    private void applyPlaceholder(TextField field, FieldDefinition definition, FieldFactoryContext context) {
        field.setPlaceholder(context.translate(definition.getPlaceholderKey()));
    }

    private void applyPlaceholder(TextArea field, FieldDefinition definition, FieldFactoryContext context) {
        field.setPlaceholder(context.translate(definition.getPlaceholderKey()));
    }

    private Currency resolveCurrency(FieldFactoryContext context) {
        Locale locale = context.getLocale();
        if (locale == null) {
            return null;
        }
        String country = locale.getCountry();
        if (country == null || country.isBlank()) {
            return null;
        }
        try {
            return Currency.getInstance(locale);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private FieldInstance createRadioGroup(FieldDefinition definition, FieldFactoryContext context) {
        RadioButtonGroup<OptionItem> group = new RadioButtonGroup<>();
        OptionCatalog catalog = optionCatalogRegistry.resolve(definition, context.getLocale());
        group.setItems(catalog.fetch(new SearchQuery("", 0, Integer.MAX_VALUE, context.getLocale(), Map.of())).getItems());
        group.setItemLabelGenerator(OptionItem::getLabel);
        return new FieldInstance(group, group, List.of());
    }

    private FieldInstance createComboBox(FieldDefinition definition, FieldFactoryContext context) {
        ComboBox<OptionItem> comboBox = new ComboBox<>();
        comboBox.setPageSize(30);
        comboBox.setAllowCustomValue(false);
        OptionCatalog catalog = optionCatalogRegistry.resolve(definition, context.getLocale());
        CallbackDataProvider<OptionItem, String> provider = DataProvider.fromFilteringCallbacks(
                query -> fetchOptions(catalog, query, context),
                query -> sizeOptions(catalog, query, context));
        comboBox.setDataProvider(provider, filter -> filter);
        comboBox.setItemLabelGenerator(OptionItem::getLabel);
        comboBox.addCustomValueSetListener(event -> {
            if (definition.getOptionsDefinition().isAllowCreate()) {
                comboBox.getElement().executeJs("this.dispatchEvent(new CustomEvent('form-engine-create', {detail: $0}))",
                        event.getDetail());
            }
        });
        return new FieldInstance(comboBox, comboBox, List.of());
    }

    private FieldInstance createMultiSelectComboBox(FieldDefinition definition, FieldFactoryContext context) {
        MultiSelectComboBox<OptionItem> field = new MultiSelectComboBox<>();
        field.setPageSize(30);
        OptionCatalog catalog = optionCatalogRegistry.resolve(definition, context.getLocale());
        CallbackDataProvider<OptionItem, String> provider = DataProvider.fromFilteringCallbacks(
                query -> fetchOptions(catalog, query, context),
                query -> sizeOptions(catalog, query, context));
        field.setDataProvider(provider, filter -> filter);
        field.setItemLabelGenerator(OptionItem::getLabel);
        return new FieldInstance(field, field, List.of());
    }

    private FieldInstance createTagsField(FieldDefinition definition, FieldFactoryContext context) {
        MultiSelectComboBox<OptionItem> field = new MultiSelectComboBox<>();
        OptionCatalog catalog = optionCatalogRegistry.resolve(definition, context.getLocale());
        CallbackDataProvider<OptionItem, String> provider = DataProvider.fromFilteringCallbacks(
                query -> fetchOptions(catalog, query, context),
                query -> sizeOptions(catalog, query, context));
        field.setDataProvider(provider, filter -> filter);
        field.setItemLabelGenerator(OptionItem::getLabel);
        return new FieldInstance(field, field, List.of());
    }

    private java.util.stream.Stream<OptionItem> fetchOptions(OptionCatalog catalog, Query<OptionItem, String> query,
                                                             FieldFactoryContext context) {
        SearchQuery search = new SearchQuery(query.getFilter().orElse(""), query.getPage(), query.getPageSize(),
                context.getLocale(), Map.of());
        OptionPage page = catalog.fetch(search);
        return page.getItems().stream();
    }

    private int sizeOptions(OptionCatalog catalog, Query<OptionItem, String> query, FieldFactoryContext context) {
        SearchQuery search = new SearchQuery(query.getFilter().orElse(""), query.getPage(), query.getPageSize(),
                context.getLocale(), Map.of());
        return catalog.fetch(search).getTotalSize();
    }

    private FieldInstance createFileUpload(FieldDefinition definition, FieldFactoryContext context) {
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setDropAllowed(true);
        upload.setMaxFiles(5);
        upload.addSucceededListener(event -> upload.getElement().executeJs("this.dispatchEvent(new CustomEvent('upload-success'))"));
        return new FieldInstance(new FileUploadField(buffer), upload, List.of());
    }

    private static final class FileUploadField extends CustomField<List<String>> {

        private final MultiFileMemoryBuffer buffer;

        private FileUploadField(MultiFileMemoryBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        protected List<String> generateModelValue() {
            return List.copyOf(buffer.getFiles());
        }

        @Override
        protected void setPresentationValue(List<String> newPresentationValue) {
            // no-op, upload is driven by user interaction
        }
    }

    private static final class SubformLauncher extends CustomField<Map<String, Object>> {
        private final Button openButton;

        private SubformLauncher(String labelKey, FieldFactoryContext context) {
            openButton = new Button(context.translate(labelKey));
            openButton.addClickListener(event -> openButton.getElement()
                    .executeJs("this.dispatchEvent(new CustomEvent('form-engine-open-subform'))"));
        }

        @Override
        protected Map<String, Object> generateModelValue() {
            return Map.of();
        }

        @Override
        protected void setPresentationValue(Map<String, Object> map) {
        }

        protected Component initContent() {
            return openButton;
        }
    }

    private static final class MapLocationField extends CustomField<Map<String, Double>> {
        private final FieldFactoryContext context;
        private final Button openButton;
        private final Span selection;
        private final Dialog dialog;
        private final LocationPicker picker;
        private Map<String, Double> currentValue = Map.of();

        private MapLocationField(FieldFactoryContext context, String triggerLabel) {
            this.context = context;
            this.openButton = new Button(triggerLabel);
            this.selection = new Span();
            this.selection.getElement().setAttribute("role", "status");
            this.selection.getElement().setAttribute("aria-live", "polite");
            updateSelectionText();
            this.dialog = new Dialog();
            dialog.setWidth("min(90vw, 820px)");
            dialog.setHeight("min(80vh, 620px)");
            dialog.getElement().setAttribute("dir", context.isRtl() ? "rtl" : "ltr");
            this.picker = new LocationPicker();
            picker.getElement().setAttribute("dir", context.isRtl() ? "rtl" : "ltr");
            picker.addLocationSelectedListener(event -> {
                Map<String, Double> coords = Map.of(
                        "lat", event.getLat(),
                        "lng", event.getLng());
                currentValue = coords;
                updateSelectionText();
                setModelValue(coords, true);
                dialog.close();
            });
            dialog.add(picker);
            dialog.addOpenedChangeListener(event -> {
                if (event.isOpened()) {
                    picker.getUI().ifPresent(ui ->
                            ui.beforeClientResponse(picker, ctx -> picker.invalidateSize()));
                }
            });
            openButton.addClickListener(event -> dialog.open());
            openButton.getElement().setAttribute("aria-haspopup", "dialog");
            openButton.getElement().getThemeList().add("primary");
            if (context.isRtl()) {
                openButton.getElement().setAttribute("dir", "rtl");
                selection.getElement().setAttribute("dir", "rtl");
            }
            selection.getElement().getStyle().set("color", "var(--lumo-secondary-text-color)");
            getElement().appendChild(dialog.getElement());
            VerticalLayout layout = new VerticalLayout(openButton, selection);
            layout.setPadding(false);
            layout.setSpacing(false);
            layout.addClassName("stack-sm");
            getElement().appendChild(layout.getElement());
        }

        private void setTriggerAriaLabel(String label) {
            if (label != null && !label.isBlank()) {
                openButton.getElement().setAttribute("aria-label", label);
                dialog.getElement().setAttribute("aria-label", label);
            }
        }

        @Override
        protected Map<String, Double> generateModelValue() {
            return currentValue;
        }

        @Override
        protected void setPresentationValue(Map<String, Double> newPresentationValue) {
            currentValue = newPresentationValue == null ? Map.of() : Map.copyOf(newPresentationValue);
            updateSelectionText();
        }

        private void updateSelectionText() {
            if (currentValue.isEmpty()) {
                selection.setText(context.translate("form.noLocationSelected"));
            } else {
                double lat = currentValue.getOrDefault("lat", Double.NaN);
                double lng = currentValue.getOrDefault("lng", Double.NaN);
                if (Double.isFinite(lat) && Double.isFinite(lng)) {
                    String coords = String.format(Locale.ROOT, "%.6f, %.6f", lat, lng);
                    selection.setText(context.translate("form.selectedLocation") + ": " + coords);
                } else {
                    selection.setText(context.translate("form.noLocationSelected"));
                }
            }
        }
    }

    private static final class RepeatableField extends CustomField<List<Map<String, Object>>> {
        private final VerticalLayout layout;
        private final Button addButton;
        private final FieldFactoryContext context;

        private RepeatableField(FieldFactoryContext context) {
            layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);
            this.context = context;
            addButton = new Button(context.translate("form.repeatable.add"));
            addButton.addClickListener(event -> addEntry(Map.of()));
            layout.add(addButton);
        }

        private void addEntry(Map<String, Object> entry) {
            HorizontalLayout row = new HorizontalLayout();
            row.add(new Span(entry.toString()));
            Button remove = new Button(context.translate("form.repeatable.remove"), event -> {
                layout.remove(row);
                updateValue();
            });
            row.add(remove);
            layout.addComponentAtIndex(layout.getComponentCount() - 1, row);
            updateValue();
        }

        protected Component initContent() {
            return layout;
        }

        @Override
        protected List<Map<String, Object>> generateModelValue() {
            return layout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .map(component -> Map.<String, Object>of())
                    .toList();
        }

        @Override
        protected void setPresentationValue(List<Map<String, Object>> newPresentationValue) {
            layout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .toList()
                    .forEach(layout::remove);
            for (Map<String, Object> entry : newPresentationValue) {
                addEntry(entry);
            }
        }
    }
}
