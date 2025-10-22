package com.youtopin.vaadin.component;

import com.youtopin.vaadin.form.FormValidationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.ButtonVariant;
import java.io.ByteArrayInputStream;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A dynamic form component that reads a JSON specification and builds a
 * responsive form at runtime. The specification defines sections,
 * field types, labels in multiple languages, validation rules, and
 * conditional visibility. Field values are stored in an internal map
 * and can be submitted to a backend service for validation via a
 * supplied {@link FormValidationService}. This component is also
 * locale‑aware: it selects labels based on the current UI locale and
 * updates them on locale change events.
 */
public class GeneratedForm extends VerticalLayout implements LocaleChangeObserver {

    // DOM 'file-remove' helper (no DomListenerRegistration dependency)
    private static void onFileRemove(Upload upload, java.util.function.Consumer<String> handler) {
        upload.getElement().addEventListener("file-remove", e -> {
            String removed = e.getEventData().getString("event.detail.file.name");
            handler.accept(removed);
        }).addEventData("event.detail.file.name");
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }


    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonNode spec;
    private final String formId;
    private final FormValidationService validationService;
    private final JsonNode submitConfig;
    private final Map<String, Component> fieldComponents = new HashMap<>();
    private final Map<String, Object> fieldValues = new HashMap<>();
    private final Map<String, List<Runnable>> visibilityWatchers = new HashMap<>();
    private final List<Runnable> localeUpdaters = new ArrayList<>();
    private final Map<String, ActionDefinition> actionsById = new LinkedHashMap<>();
    private final List<SubmissionListener> submissionListeners = new ArrayList<>();
    private H3 formTitle;
    private Button submitButton;

    /**
     * Create a form from a JSON specification file located on the classpath
     * under {@code /forms/}. The JSON must include an {@code id},
     * {@code title}, and a {@code layout} array describing sections and
     * fields. See the accompanying sample in src/main/resources/forms for
     * details.
     *
     * @param jsonResource the filename of the form JSON specification
     * @param validationService the service used for backend validation
     * @throws RuntimeException if the specification cannot be read or parsed
     */
    public GeneratedForm(String jsonResource, FormValidationService validationService) {
        this.validationService = validationService;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("forms/" + jsonResource)) {
            if (in == null) {
                throw new IllegalArgumentException("Form specification not found: " + jsonResource);
            }
            this.spec = mapper.readTree(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read form specification " + jsonResource, e);
        }
        this.submitConfig = spec.get("submit");
        this.formId = spec.has("id") ? spec.get("id").asText() : jsonResource;
        buildForm();
    }

    /**
     * Build the form UI from the JSON specification. Sections are rendered
     * as titled blocks and fields are laid out using {@link FormLayout}
     * with the configured number of columns. Field values and validity
     * state are stored internally; external consumers can trigger submissions
     * via {@link #submit(String)} or observe completions through
     * {@link #addSubmissionListener(SubmissionListener)}.
     */
    private void buildForm() {
        removeAll();
        localeUpdaters.clear();
        fieldComponents.clear();
        fieldValues.clear();
        visibilityWatchers.clear();
        actionsById.clear();
        setSpacing(false);
        setPadding(false);
        setWidthFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        addClassNames("generated-form", "app-card", "stack-lg");

        String title = getTranslationFromNode(spec.get("title"));
        if (hasText(title)) {
            formTitle = new H3(title);
            formTitle.addClassName("form-title");
            add(formTitle);
            localeUpdaters.add(() -> {
                String t = getTranslationFromNode(spec.get("title"));
                if (hasText(t) && formTitle != null) {
                    formTitle.setText(t);
                }
            });
        } else {
            formTitle = null;
        }
        // Layout sections
        ArrayNode layout = (ArrayNode) spec.get("layout");
        if (layout != null) {
            layout.forEach(sectionNode -> buildSection(sectionNode));
        }
        buildActions();
    }

    /**
     * Create a section of the form from its JSON definition. Each section
     * may specify a title, number of columns, and an array of fields.
     *
     * @param section the JSON node describing a section
     */
    private void buildSection(JsonNode section) {
        String type = section.has("type") ? section.get("type").asText() : "";
        if (!"section".equals(type)) {
            return;
        }
        VerticalLayout sectionLayout = new VerticalLayout();
        sectionLayout.setPadding(false);
        sectionLayout.setSpacing(false);
        sectionLayout.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        sectionLayout.addClassNames("form-section", "stack-md");

        String title = getTranslationFromNode(section.get("title"));
        if (hasText(title)) {
            H4 h4 = new H4(title);
            sectionLayout.addComponentAsFirst(h4);
            localeUpdaters.add(() -> {
                String t = getTranslationFromNode(section.get("title"));
                if (t != null) {
                    h4.setText(t);
                }
            });
        }

        int columns = section.has("columns") ? section.get("columns").asInt(1) : 1;
        JsonNode layoutConfig = section.get("layout");
        String layoutType = layoutConfig != null && layoutConfig.has("type")
                ? layoutConfig.get("type").asText()
                : "form";

        HasComponents fieldContainer;
        switch (layoutType.toLowerCase(Locale.ROOT)) {
            case "horizontal" -> {
                HorizontalLayout row = new HorizontalLayout();
                row.setPadding(false);
                row.setSpacing(false);
                row.setWidthFull();
                row.getStyle().set("gap", resolveSpacing(layoutConfig, "var(--lumo-space-m)"));
                if (layoutConfig != null && layoutConfig.has("align")) {
                    Alignment alignment = parseAlignment(layoutConfig.get("align").asText());
                    if (alignment != null) {
                        row.setAlignItems(alignment);
                    }
                }
                if (layoutConfig != null && layoutConfig.has("justify")) {
                    JustifyContentMode justify = parseJustify(layoutConfig.get("justify").asText());
                    if (justify != null) {
                        row.setJustifyContentMode(justify);
                    }
                }
                boolean wrap = layoutConfig != null && layoutConfig.has("wrap") && layoutConfig.get("wrap").asBoolean(false);
                row.getStyle().set("flex-wrap", wrap ? "wrap" : "nowrap");
                fieldContainer = row;
            }
            case "vertical" -> {
                VerticalLayout column = new VerticalLayout();
                column.setPadding(false);
                column.setSpacing(false);
                column.setWidthFull();
                column.getStyle().set("gap", resolveSpacing(layoutConfig, "var(--lumo-space-m)"));
                if (layoutConfig != null && layoutConfig.has("align")) {
                    Alignment alignment = parseAlignment(layoutConfig.get("align").asText());
                    if (alignment != null) {
                        column.setAlignItems(alignment);
                    }
                }
                if (layoutConfig != null && layoutConfig.has("justify")) {
                    JustifyContentMode justify = parseJustify(layoutConfig.get("justify").asText());
                    if (justify != null) {
                        column.setJustifyContentMode(justify);
                    }
                }
                fieldContainer = column;
            }
            default -> {
                FormLayout formLayout = createFormLayout(columns, layoutConfig);
                fieldContainer = formLayout;
            }
        }

        applyLayoutProperties((Component) fieldContainer, layoutConfig);

        ArrayNode fields = (ArrayNode) section.get("fields");
        if (fields != null) {
            fields.forEach(field -> {
                Component comp = createField(field);
                if (comp != null) {
                    addComponentToContainer(fieldContainer, comp, field);
                }
            });
        }
        sectionLayout.add((Component) fieldContainer);
        add(sectionLayout);
    }


    private void buildActions() {
        submitButton = null;
        List<ActionDefinition> actions = resolveActions();
        if (actions.isEmpty()) {
            return;
        }
        HorizontalLayout actionsBar = new HorizontalLayout();
        actionsBar.setPadding(false);
        actionsBar.setSpacing(false);
        actionsBar.setWidthFull();
        actionsBar.addClassName("form-actions");
        actionsBar.setJustifyContentMode(JustifyContentMode.END);

        actions.forEach(action -> {
            Button button = new Button(resolveActionLabel(action));
            action.themeVariants().forEach(button::addThemeVariants);
            ActionDefinition enriched = action.withButton(button);
            button.addClickListener(e -> submit(enriched));
            actionsBar.add(button);
            if (submitButton == null) {
                submitButton = button;
            }
            localeUpdaters.add(() -> button.setText(resolveActionLabel(enriched)));
            actionsById.put(enriched.id(), enriched);
        });

        add(actionsBar);
    }


    private List<ActionDefinition> resolveActions() {
        List<ActionDefinition> actions = new ArrayList<>();
        if (submitConfig != null && submitConfig.has("actions") && submitConfig.get("actions").isArray()) {
            int index = 0;
            for (JsonNode node : submitConfig.get("actions")) {
                ActionDefinition def = parseActionDefinition(node, index++);
                if (def != null) {
                    actions.add(def);
                }
            }
        }
        if (actions.isEmpty()) {
            actions.add(defaultActionDefinition());
        }
        return actions;
    }

    private ActionDefinition parseActionDefinition(JsonNode actionNode, int index) {
        if (actionNode == null || actionNode.isNull()) {
            return null;
        }
        String id = actionNode.has("id") ? actionNode.get("id").asText() : null;
        if (!hasText(id)) {
            id = index == 0 ? "submit" : "action-" + (index + 1);
        }
        JsonNode labelNode = actionNode.get("label");
        String labelKey = actionNode.has("labelKey") ? actionNode.get("labelKey").asText(null) : null;
        List<ButtonVariant> variants = parseButtonVariants(actionNode, index == 0);
        boolean clientValidation = actionNode.has("validate")
                ? actionNode.get("validate").asBoolean(true)
                : actionNode.has("clientValidation")
                        ? actionNode.get("clientValidation").asBoolean(true)
                        : true;
        Boolean backendOverride = actionNode.has("backendValidation")
                ? Boolean.valueOf(actionNode.get("backendValidation").asBoolean())
                : null;
        JsonNode successNode = actionNode.get("successMessage");
        String successKey = actionNode.has("successMessageKey")
                ? actionNode.get("successMessageKey").asText(null)
                : null;
        return new ActionDefinition(id, labelNode, labelKey, variants, clientValidation, backendOverride, successNode,
                successKey, actionNode, null);
    }

    private List<ButtonVariant> parseButtonVariants(JsonNode actionNode, boolean isFirst) {
        LinkedHashSet<ButtonVariant> variants = new LinkedHashSet<>();
        if (actionNode != null) {
            if (actionNode.has("theme")) {
                JsonNode themeNode = actionNode.get("theme");
                if (themeNode.isArray()) {
                    themeNode.forEach(node -> addButtonVariant(node.asText(), variants));
                } else if (themeNode.isTextual()) {
                    addButtonVariant(themeNode.asText(), variants);
                }
            }
            if (actionNode.has("themeVariants")) {
                JsonNode themeNode = actionNode.get("themeVariants");
                if (themeNode.isArray()) {
                    themeNode.forEach(node -> addButtonVariant(node.asText(), variants));
                } else if (themeNode.isTextual()) {
                    addButtonVariant(themeNode.asText(), variants);
                }
            }
            if (actionNode.path("primary").asBoolean(false)) {
                variants.add(ButtonVariant.LUMO_PRIMARY);
            }
        }
        if (variants.isEmpty() && isFirst) {
            variants.add(ButtonVariant.LUMO_PRIMARY);
        }
        return List.copyOf(variants);
    }

    private void addButtonVariant(String value, LinkedHashSet<ButtonVariant> variants) {
        if (!hasText(value)) {
            return;
        }
        switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "primary" -> variants.add(ButtonVariant.LUMO_PRIMARY);
            case "success" -> variants.add(ButtonVariant.LUMO_SUCCESS);
            case "error", "danger" -> variants.add(ButtonVariant.LUMO_ERROR);
            case "contrast" -> variants.add(ButtonVariant.LUMO_CONTRAST);
            case "tertiary" -> variants.add(ButtonVariant.LUMO_TERTIARY);
            case "tertiary-inline" -> variants.add(ButtonVariant.LUMO_TERTIARY_INLINE);
            case "icon" -> variants.add(ButtonVariant.LUMO_ICON);
            default -> {
                // ignore unknown variants to keep compatibility
            }
        }
    }

    private ActionDefinition defaultActionDefinition() {
        return new ActionDefinition("submit", null, "form.submit", List.of(ButtonVariant.LUMO_PRIMARY), true, null, null,
                null, null, null);
    }

    private String resolveActionLabel(ActionDefinition action) {
        String label = getTranslationFromNode(action.labelNode());
        if (!hasText(label) && hasText(action.labelKey())) {
            label = getTranslation(action.labelKey());
        }
        if (!hasText(label) && hasText(action.id())) {
            label = action.id();
        }
        return label;
    }

    private String resolveActionSuccessMessage(ActionDefinition action) {
        String message = getTranslationFromNode(action.successMessageNode());
        if (!hasText(message) && hasText(action.successMessageKey())) {
            message = getTranslation(action.successMessageKey());
        }
        if (!hasText(message)) {
            message = getTranslation("form.success");
        }
        return message;
    }


    private FormLayout createFormLayout(int columns, JsonNode layoutConfig) {
        FormLayout formLayout = new FormLayout();
        formLayout.addClassName("form-section__grid");
        formLayout.setWidthFull();
        configureResponsiveSteps(formLayout, columns, layoutConfig);
        String spacing = resolveSpacing(layoutConfig, "var(--lumo-space-m)");
        formLayout.getStyle().set("column-gap", spacing);
        formLayout.getStyle().set("row-gap", spacing);
        return formLayout;
    }

    private void configureResponsiveSteps(FormLayout formLayout, int columns, JsonNode layoutConfig) {
        List<FormLayout.ResponsiveStep> steps = new ArrayList<>();
        if (layoutConfig != null && layoutConfig.has("responsiveSteps") && layoutConfig.get("responsiveSteps").isArray()) {
            layoutConfig.get("responsiveSteps").forEach(node -> {
                if (node == null || node.isNull()) {
                    return;
                }
                String minWidth = node.has("minWidth") ? node.get("minWidth").asText("0")
                        : node.has("width") ? node.get("width").asText("0") : "0";
                int cols = node.has("columns") ? Math.max(1, node.get("columns").asInt(1)) : 1;
                FormLayout.ResponsiveStep.LabelsPosition labelsPosition = null;
                if (node.has("labelsPosition")) {
                    labelsPosition = parseLabelsPosition(node.get("labelsPosition").asText());
                }
                if (labelsPosition != null) {
                    steps.add(new FormLayout.ResponsiveStep(minWidth, cols, labelsPosition));
                } else {
                    steps.add(new FormLayout.ResponsiveStep(minWidth, cols));
                }
            });
        }
        if (steps.isEmpty()) {
            steps.add(new FormLayout.ResponsiveStep("0", 1));
            if (columns >= 2) {
                steps.add(new FormLayout.ResponsiveStep("640px", Math.min(2, columns)));
            }
            if (columns >= 3) {
                steps.add(new FormLayout.ResponsiveStep("960px", Math.min(3, columns)));
            }
            steps.add(new FormLayout.ResponsiveStep("1200px", Math.max(1, columns), FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
        }
        formLayout.setResponsiveSteps(steps);
    }

    private FormLayout.ResponsiveStep.LabelsPosition parseLabelsPosition(String value) {
        if (!hasText(value)) {
            return null;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "top" -> FormLayout.ResponsiveStep.LabelsPosition.TOP;
            case "aside", "left" -> FormLayout.ResponsiveStep.LabelsPosition.ASIDE;
            default -> null;
        };
    }

    private void addComponentToContainer(HasComponents container, Component component, JsonNode fieldSpec) {
        container.add(component);
        if (container instanceof FormLayout formLayout) {
            Integer colspan = resolveColSpan(fieldSpec);
            if (colspan != null && colspan > 1) {
                formLayout.setColspan(component, colspan);
            }
        }
    }

    private Integer resolveColSpan(JsonNode fieldSpec) {
        if (fieldSpec == null) {
            return null;
        }
        JsonNode node = fieldSpec.has("colSpan") ? fieldSpec.get("colSpan") : fieldSpec.get("colspan");
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isInt()) {
            return node.asInt();
        }
        if (node.isTextual()) {
            String text = node.asText();
            if (!hasText(text)) {
                return null;
            }
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                String fieldName = fieldSpec.has("name") ? fieldSpec.get("name").asText() : "unknown";
                throw new IllegalArgumentException("Field '" + fieldName + "' has invalid colSpan", ex);
            }
        }
        return null;
    }


    private void applyLayoutProperties(Component container, JsonNode layoutConfig) {
        if (container == null || layoutConfig == null) {
            return;
        }
        if (layoutConfig.has("width")) {
            String width = layoutConfig.get("width").asText();
            if (hasText(width) && container instanceof com.vaadin.flow.component.HasSize sized) {
                sized.setWidth(width);
            }
        }
        if (layoutConfig.has("classNames") && layoutConfig.get("classNames").isArray()) {
            layoutConfig.get("classNames").forEach(node -> {
                if (node != null && node.isTextual()) {
                    String className = node.asText();
                    if (hasText(className)) {
                        container.addClassName(className);
                    }
                }
            });
        }
    }

    private String resolveSpacing(JsonNode layoutConfig, String defaultValue) {
        if (layoutConfig == null || !layoutConfig.has("spacing")) {
            return defaultValue;
        }
        String parsed = parseSpacing(layoutConfig.get("spacing"));
        return parsed != null ? parsed : defaultValue;
    }

    private String parseSpacing(JsonNode spacingNode) {
        if (spacingNode == null) {
            return null;
        }
        if (spacingNode.isNumber()) {
            double value = spacingNode.asDouble();
            if (Double.isNaN(value)) {
                return null;
            }
            if (Math.floor(value) == value) {
                return (long) value + "px";
            }
            return value + "px";
        }
        if (spacingNode.isTextual()) {
            String text = spacingNode.asText();
            return hasText(text) ? text : null;
        }
        return null;
    }

    private Alignment parseAlignment(String value) {
        if (!hasText(value)) {
            return null;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "start" -> Alignment.START;
            case "center" -> Alignment.CENTER;
            case "end" -> Alignment.END;
            case "stretch" -> Alignment.STRETCH;
            case "baseline" -> Alignment.BASELINE;
            default -> null;
        };
    }

    private JustifyContentMode parseJustify(String value) {
        if (!hasText(value)) {
            return null;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "start" -> JustifyContentMode.START;
            case "center" -> JustifyContentMode.CENTER;
            case "end" -> JustifyContentMode.END;
            case "between" -> JustifyContentMode.BETWEEN;
            case "around" -> JustifyContentMode.AROUND;
            case "evenly" -> JustifyContentMode.EVENLY;
            default -> null;
        };
    }

    private LocalDateTime parseIsoDateTime(JsonNode fieldSpec, String property, String fieldName) {
        JsonNode node = fieldSpec.get(property);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isTextual()) {
            throw new IllegalArgumentException(
                    "Field '" + fieldName + "' property '" + property + "' must be a string in ISO-8601 format");
        }
        String text = node.asText();
        if (!hasText(text)) {
            return null;
        }
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Field '" + fieldName + "' property '" + property + "' must use ISO_LOCAL_DATE_TIME format", ex);
        }
    }

    private Integer parseIntegerProperty(JsonNode fieldSpec, String property, String fieldName) {
        JsonNode node = fieldSpec.get(property);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.intValue();
        }
        if (node.isTextual()) {
            String text = node.asText();
            if (!hasText(text)) {
                return null;
            }
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Field '" + fieldName + "' property '" + property + "' must be an integer", ex);
            }
        }
        throw new IllegalArgumentException(
                "Field '" + fieldName + "' property '" + property + "' must be numeric or string");
    }


    private JalaliDateTimePicker createJalaliPicker(JsonNode field, String name, String label,
            boolean required, JalaliDateTimePicker.PickerVariant defaultVariant) {
        JalaliDateTimePicker picker = new JalaliDateTimePicker();
        picker.setLabel(label);
        picker.setWidthFull();
        JalaliDateTimePicker.PickerVariant variant = resolveJalaliVariant(field, defaultVariant);
        picker.setPickerVariant(variant);
        if (required) {
            picker.setRequiredIndicatorVisible(true);
        }
        LocalDateTime min = parseIsoDateTime(field, "min", name);
        if (min != null) {
            picker.setMin(min);
        }
        LocalDateTime max = parseIsoDateTime(field, "max", name);
        if (max != null) {
            picker.setMax(max);
        }
        Integer minYear = parseIntegerProperty(field, "minYear", name);
        if (minYear != null) {
            try {
                picker.setMinYear(minYear);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Field '" + name + "' has invalid minYear " + minYear, ex);
            }
        }
        Integer maxYear = parseIntegerProperty(field, "maxYear", name);
        if (maxYear != null) {
            try {
                picker.setMaxYear(maxYear);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Field '" + name + "' has invalid maxYear " + maxYear, ex);
            }
        }
        if (variant == JalaliDateTimePicker.PickerVariant.DATE_TIME) {
            Integer minuteStep = parseIntegerProperty(field, "minuteStep", name);
            if (minuteStep != null) {
                try {
                    picker.setMinuteStep(minuteStep);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException(
                            "Field '" + name + "' has invalid minuteStep " + minuteStep, ex);
                }
            }
        }
        String openLabel = getTranslationFromNode(field.get("openLabel"));
        if (hasText(openLabel)) {
            picker.setOpenButtonLabel(openLabel);
            localeUpdaters.add(() -> {
                String translated = getTranslationFromNode(field.get("openLabel"));
                picker.setOpenButtonLabel(hasText(translated) ? translated : null);
            });
        }
        picker.addValueChangeListener(ev -> {
            fieldValues.put(name, ev.getValue());
            if (required) {
                if (ev.getValue() == null) {
                    setError(picker, getTranslation("form.required"));
                } else {
                    clearError(picker);
                }
            }
            runVisibilityWatchers(name);
        });
        return picker;
    }

    private JalaliDateTimePicker.PickerVariant resolveJalaliVariant(JsonNode fieldSpec,
            JalaliDateTimePicker.PickerVariant defaultVariant) {
        if (fieldSpec == null) {
            return defaultVariant;
        }
        JalaliDateTimePicker.PickerVariant variant = defaultVariant;
        if (fieldSpec.has("pickerVariant")) {
            variant = parseJalaliVariantValue(fieldSpec.get("pickerVariant").asText(), variant);
        } else if (fieldSpec.has("pickerMode")) {
            variant = parseJalaliVariantValue(fieldSpec.get("pickerMode").asText(), variant);
        } else if (fieldSpec.has("mode")) {
            variant = parseJalaliVariantValue(fieldSpec.get("mode").asText(), variant);
        }
        if (fieldSpec.has("showTime") && fieldSpec.get("showTime").isBoolean()) {
            variant = fieldSpec.get("showTime").asBoolean(true)
                    ? JalaliDateTimePicker.PickerVariant.DATE_TIME
                    : JalaliDateTimePicker.PickerVariant.DATE;
        }
        return variant;
    }

    private JalaliDateTimePicker.PickerVariant parseJalaliVariantValue(String value,
            JalaliDateTimePicker.PickerVariant fallback) {
        if (!hasText(value)) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        return switch (normalized) {
            case "date", "date-only", "dateonly" -> JalaliDateTimePicker.PickerVariant.DATE;
            case "date-time", "datetime", "dateandtime", "date-time-picker" ->
                    JalaliDateTimePicker.PickerVariant.DATE_TIME;
            default -> fallback;
        };
    }


    /**
     * Create a Vaadin component for a single field definition. Supported field types include
     * text, email, tel, number, select, switch, radio buttons and various upload inputs.
     * Required and pattern validations are applied client-side; backend validation will be
     * triggered on submit.
     *
     * @param field the JSON node describing the field
     * @return the created component or {@code null} if type is unknown
     */
    private Component createField(JsonNode field) {
        String name = field.get("name").asText();
        String type = field.has("type") ? field.get("type").asText() : "text";
        String label = getTranslationFromNode(field.get("label"));
        boolean required = field.has("required") && field.get("required").asBoolean(false);

        Component comp;

        switch (type) {
            case "text" -> {
                TextField tf = new TextField(label);
                tf.setClearButtonVisible(true);
                tf.setWidthFull();
                if (required) {
                    tf.setRequiredIndicatorVisible(true);
                }
                applyValidators(tf, field);
                tf.addValueChangeListener(ev -> {
                    fieldValues.put(name, ev.getValue());
                    validateField(tf, name, required, field);
                    runVisibilityWatchers(name);
                });
                comp = tf;
            }
            case "email" -> {
                EmailField ef = new EmailField(label);
                ef.setClearButtonVisible(true);
                ef.setWidthFull();
                if (required) {
                    ef.setRequiredIndicatorVisible(true);
                }
                applyValidators(ef, field);
                ef.addValueChangeListener(ev -> {
                    fieldValues.put(name, ev.getValue());
                    validateField(ef, name, required, field);
                    runVisibilityWatchers(name);
                });
                comp = ef;
            }
            case "tel" -> {
                TextField tf = new TextField(label);
                tf.setClearButtonVisible(true);
                tf.setWidthFull();
                if (required) {
                    tf.setRequiredIndicatorVisible(true);
                }
                applyValidators(tf, field);
                tf.addValueChangeListener(ev -> {
                    fieldValues.put(name, ev.getValue());
                    validateField(tf, name, required, field);
                    runVisibilityWatchers(name);
                });
                comp = tf;
            }
            case "number" -> {
                NumberField nf = new NumberField(label);
                nf.setClearButtonVisible(true);
                nf.setWidthFull();
                if (required) {
                    nf.setRequiredIndicatorVisible(true);
                }
                applyValidators(nf, field);
                nf.addValueChangeListener(ev -> {
                    fieldValues.put(name, ev.getValue());
                    validateField(nf, name, required, field);
                    runVisibilityWatchers(name);
                });
                comp = nf;
            }
            case "select" -> {
                ComboBox<String> cb = new ComboBox<>(label);
                cb.setWidthFull();
                List<String> values = new ArrayList<>();
                Map<String, String> display = new HashMap<>();
                if (field.has("options") && field.get("options").isArray()) {
                    for (JsonNode opt : (ArrayNode) field.get("options")) {
                        String value = opt.get("value").asText();
                        String lbl = getTranslationFromNode(opt.get("label"));
                        values.add(value);
                        display.put(value, lbl);
                    }
                }
                cb.setItems(values);
                cb.setItemLabelGenerator(item -> display.getOrDefault(item, item));
                if (required) {
                    cb.setRequiredIndicatorVisible(true);
                }
                cb.addValueChangeListener(ev -> {
                    fieldValues.put(name, ev.getValue());
                    validateField(cb, name, required, field);
                    runVisibilityWatchers(name);
                });
                comp = cb;
            }
            case "switch" -> {
                Checkbox cb = new Checkbox(label);
                cb.setWidthFull();
                cb.addValueChangeListener(ev -> {
                    fieldValues.put(name, ev.getValue());
                    validateField(cb, name, required, field);
                    runVisibilityWatchers(name);
                });
                comp = cb;
            }
            case "date" -> {
                DatePicker dp = new DatePicker(label);
                dp.setWidthFull();
                if (required) {
                    dp.setRequiredIndicatorVisible(true);
                }
                dp.addValueChangeListener(ev -> {
                    fieldValues.put(name, ev.getValue());
                    if (required) {
                        if (ev.getValue() == null) {
                            setError(dp, getTranslation("form.required"));
                        } else {
                            clearError(dp);
                        }
                    }
                    runVisibilityWatchers(name);
                });
                comp = dp;
            }
            case "jalaliDateTime" -> comp = createJalaliPicker(field, name, label, required,
                    JalaliDateTimePicker.PickerVariant.DATE_TIME);
            case "jalaliDate" -> comp = createJalaliPicker(field, name, label, required,
                    JalaliDateTimePicker.PickerVariant.DATE);
            case "file" -> {
                MemoryBuffer buffer = new MemoryBuffer();
                Upload upload = new Upload(buffer);
                upload.setAcceptedFileTypes("*/*");
                upload.setMaxFiles(1);
                Span dropLabel = new Span(label);
                Button uploadButton = new Button(label);
                upload.setDropLabel(dropLabel);
                upload.setUploadButton(uploadButton);

                Span info = new Span();
                info.setVisible(false);

                upload.addSucceededListener(ev -> {
                    try {
                        byte[] data = buffer.getInputStream().readAllBytes();
                        String human = humanReadableByteCount(data.length);
                        info.setText(ev.getFileName() + " (" + human + ")");
                    } catch (Exception ex) {
                        info.setText(ev.getFileName());
                    }
                    info.setVisible(true);
                    fieldValues.put(name, ev.getFileName());
                    runVisibilityWatchers(name);
                });

                onFileRemove(upload, removed -> {
                    info.setText("");
                    info.setVisible(false);
                    fieldValues.put(name, null);
                    runVisibilityWatchers(name);
                });

                VerticalLayout wrapper = new VerticalLayout(upload, info);
                wrapper.setPadding(false);
                wrapper.setSpacing(false);
                wrapper.addClassName("stack-sm");
                comp = wrapper;

                localeUpdaters.add(() -> {
                    String translated = getTranslationFromNode(field.get("label"));
                    dropLabel.setText(translated);
                    uploadButton.setText(translated);
                });
            }
            case "multiFile" -> {
                MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
                Upload upload = new Upload(buffer);
                upload.setAcceptedFileTypes("*/*");
                Span dropLabel = new Span(label);
                Button uploadButton = new Button(label);
                upload.setDropLabel(dropLabel);
                upload.setUploadButton(uploadButton);

                List<String> files = new ArrayList<>();
                Span namesInfo = new Span();
                namesInfo.setVisible(false);
                Runnable updateNames = () -> {
                    if (files.isEmpty()) {
                        namesInfo.setText("");
                        namesInfo.setVisible(false);
                    } else {
                        namesInfo.setText(String.join(", ", files));
                        namesInfo.setVisible(true);
                    }
                };

                upload.addSucceededListener(ev -> {
                    files.add(ev.getFileName());
                    fieldValues.put(name, new ArrayList<>(files));
                    updateNames.run();
                    runVisibilityWatchers(name);
                });

                onFileRemove(upload, removed -> {
                    files.remove(removed);
                    fieldValues.put(name, new ArrayList<>(files));
                    updateNames.run();
                    runVisibilityWatchers(name);
                });

                VerticalLayout wrapper = new VerticalLayout(upload, namesInfo);
                wrapper.setPadding(false);
                wrapper.setSpacing(false);
                wrapper.addClassName("stack-sm");
                comp = wrapper;

                localeUpdaters.add(() -> {
                    String translated = getTranslationFromNode(field.get("label"));
                    dropLabel.setText(translated);
                    uploadButton.setText(translated);
                });
            }
            case "image" -> {
                MemoryBuffer buffer = new MemoryBuffer();
                Upload upload = new Upload(buffer);
                upload.setAcceptedFileTypes("image/*");
                upload.setMaxFiles(1);
                Span dropLabel = new Span(label);
                Button uploadButton = new Button(label);
                upload.setDropLabel(dropLabel);
                upload.setUploadButton(uploadButton);

                Image preview = new Image();
                preview.setWidth("100px");
                preview.setHeight("100px");
                preview.getStyle().set("object-fit", "cover");
                preview.setVisible(false);

                upload.addSucceededListener(ev -> {
                    try {
                        byte[] data = buffer.getInputStream().readAllBytes();
                        StreamResource res = new StreamResource(ev.getFileName(), () -> new ByteArrayInputStream(data));
                        preview.setSrc(res);
                    } catch (Exception ex) {
                        preview.setSrc("");
                    }
                    preview.setVisible(true);
                    fieldValues.put(name, ev.getFileName());
                    runVisibilityWatchers(name);
                });

                onFileRemove(upload, removed -> {
                    preview.setSrc("");
                    preview.setVisible(false);
                    fieldValues.put(name, null);
                    runVisibilityWatchers(name);
                });

                VerticalLayout wrapper = new VerticalLayout(upload, preview);
                wrapper.setPadding(false);
                wrapper.setSpacing(false);
                wrapper.addClassName("stack-sm");
                comp = wrapper;

                localeUpdaters.add(() -> {
                    String translated = getTranslationFromNode(field.get("label"));
                    dropLabel.setText(translated);
                    uploadButton.setText(translated);
                });
            }
            case "multiImage" -> {
                MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
                Upload upload = new Upload(buffer);
                upload.setAcceptedFileTypes("image/*");
                Span dropLabel = new Span(label);
                Button uploadButton = new Button(label);
                upload.setDropLabel(dropLabel);
                upload.setUploadButton(uploadButton);

                List<String> images = new ArrayList<>();
                Map<String, Image> thumbs = new HashMap<>();
                HorizontalLayout previewContainer = new HorizontalLayout();
                previewContainer.setSpacing(true);
                previewContainer.setVisible(false);

                upload.addSucceededListener(ev -> {
                    String fname = ev.getFileName();
                    images.add(fname);
                    try {
                        byte[] data = buffer.getInputStream(fname).readAllBytes();
                        StreamResource res = new StreamResource(fname, () -> new ByteArrayInputStream(data));
                        Image img = new Image(res, fname);
                        img.setWidth("100px");
                        img.setHeight("100px");
                        img.getStyle().set("object-fit", "cover");
                        thumbs.put(fname, img);
                        previewContainer.add(img);
                        previewContainer.setVisible(true);
                        fieldValues.put(name, new ArrayList<>(images));
                        runVisibilityWatchers(name);
                    } catch (Exception ex) {
                        // ignore failures when reading preview data
                    }
                });

                onFileRemove(upload, removed -> {
                    images.remove(removed);
                    Image img = thumbs.remove(removed);
                    if (img != null) {
                        previewContainer.remove(img);
                    }
                    if (images.isEmpty()) {
                        previewContainer.setVisible(false);
                    }
                    fieldValues.put(name, new ArrayList<>(images));
                    runVisibilityWatchers(name);
                });

                VerticalLayout wrapper = new VerticalLayout(upload, previewContainer);
                wrapper.setPadding(false);
                wrapper.setSpacing(false);
                wrapper.addClassName("stack-sm");
                comp = wrapper;

                localeUpdaters.add(() -> {
                    String translated = getTranslationFromNode(field.get("label"));
                    dropLabel.setText(translated);
                    uploadButton.setText(translated);
                });
            }
            case "map" -> {
                Button open = new Button(getTranslation("form.pickLocation"));
                Span selected = new Span(getTranslation("form.noLocationSelected"));

                Dialog dialog = new Dialog();
                dialog.setWidth("820px");
                dialog.setHeight("620px");

                LocationPicker picker = new LocationPicker();
                picker.addLocationSelectedListener(ev -> {
                    String textPos = ev.getLat() + ", " + ev.getLng();
                    selected.setText(getTranslation("form.selectedLocation") + ": " + textPos);
                    fieldValues.put(name, Map.of("lat", ev.getLat(), "lng", ev.getLng()));
                    runVisibilityWatchers(name);
                    dialog.close();
                });
                dialog.add(picker);

                open.addClickListener(e -> dialog.open());

                VerticalLayout wrapper = new VerticalLayout(open, selected);
                wrapper.setPadding(false);
                wrapper.setSpacing(false);
                wrapper.addClassName("stack-sm");
                comp = wrapper;

                localeUpdaters.add(() -> {
                    open.setText(getTranslation("form.pickLocation"));
                    Object value = fieldValues.get(name);
                    if (value instanceof Map<?, ?> coords && coords.containsKey("lat") && coords.containsKey("lng")) {
                        selected.setText(getTranslation("form.selectedLocation") + ": " + coords.get("lat") + ", " + coords.get("lng"));
                    } else {
                        selected.setText(getTranslation("form.noLocationSelected"));
                    }
                });
            }
            case "group" -> {
                VerticalLayout groupContainer = new VerticalLayout();
                groupContainer.setPadding(false);
                groupContainer.setSpacing(false);
                groupContainer.addClassName("stack-sm");
                int minItems = field.has("minItems") ? field.get("minItems").asInt() : 0;
                int maxItems = field.has("maxItems") ? field.get("maxItems").asInt() : Integer.MAX_VALUE;
                List<Map<String, Object>> items = new ArrayList<>();
                Button addBtn = new Button(getTranslation("form.addItem"));

                Runnable refreshGroupButtons = () -> {
                    addBtn.setEnabled(items.size() < maxItems);
                    groupContainer.getChildren().forEach(child -> {
                        if (child instanceof HorizontalLayout hl) {
                            hl.getChildren().forEach(c -> {
                                if (c instanceof Button b && b.getElement().getThemeList().contains("error")) {
                                    b.setEnabled(items.size() > minItems);
                                }
                            });
                        }
                    });
                };

                Consumer<Void> addRow = unused -> {
                    if (items.size() >= maxItems) {
                        return;
                    }
                    HorizontalLayout row = new HorizontalLayout();
                    row.setWidthFull();
                    row.setSpacing(true);
                    Map<String, Object> itemValues = new HashMap<>();
                    if (field.has("fields") && field.get("fields").isArray()) {
                        for (JsonNode sub : (ArrayNode) field.get("fields")) {
                            String subName = sub.get("name").asText();
                            String subType = sub.get("type").asText();
                            String subLabel = getTranslationFromNode(sub.get("label"));
                            boolean subReq = sub.has("required") && sub.get("required").asBoolean(false);
                            Component fcomp;
                            switch (subType) {
                                case "text" -> {
                                    TextField tf = new TextField(subLabel);
                                    if (subReq) {
                                        tf.setRequiredIndicatorVisible(true);
                                    }
                                    tf.setClearButtonVisible(true);
                                    tf.addValueChangeListener(ev -> itemValues.put(subName, ev.getValue()));
                                    fcomp = tf;
                                }
                                case "number" -> {
                                    NumberField nf = new NumberField(subLabel);
                                    if (subReq) {
                                        nf.setRequiredIndicatorVisible(true);
                                    }
                                    nf.setClearButtonVisible(true);
                                    nf.addValueChangeListener(ev -> itemValues.put(subName, ev.getValue()));
                                    fcomp = nf;
                                }
                                case "email" -> {
                                    EmailField ef = new EmailField(subLabel);
                                    if (subReq) {
                                        ef.setRequiredIndicatorVisible(true);
                                    }
                                    ef.setClearButtonVisible(true);
                                    ef.addValueChangeListener(ev -> itemValues.put(subName, ev.getValue()));
                                    fcomp = ef;
                                }
                                case "tel" -> {
                                    TextField tf = new TextField(subLabel);
                                    if (subReq) {
                                        tf.setRequiredIndicatorVisible(true);
                                    }
                                    tf.setClearButtonVisible(true);
                                    tf.addValueChangeListener(ev -> itemValues.put(subName, ev.getValue()));
                                    fcomp = tf;
                                }
                                default -> {
                                    TextField def = new TextField(subLabel);
                                    def.addValueChangeListener(ev -> itemValues.put(subName, ev.getValue()));
                                    fcomp = def;
                                }
                            }
                            if (fcomp instanceof com.vaadin.flow.component.HasSize size) {
                                size.setWidth("200px");
                            }
                            row.add(fcomp);
                        }
                    }
                    Button rem = new Button(getTranslation("form.removeItem"));
                    rem.getElement().getThemeList().add("error");
                    rem.addClickListener(ev -> {
                        if (items.size() > minItems) {
                            groupContainer.remove(row);
                            items.remove(itemValues);
                            fieldValues.put(name, new ArrayList<>(items));
                            runVisibilityWatchers(name);
                            refreshGroupButtons.run();
                        }
                    });
                    row.add(rem);
                    groupContainer.add(row);
                    items.add(itemValues);
                    fieldValues.put(name, new ArrayList<>(items));
                    runVisibilityWatchers(name);
                    refreshGroupButtons.run();
                };

                addBtn.addClickListener(ev -> addRow.accept(null));
                VerticalLayout wrapper = new VerticalLayout(groupContainer, addBtn);
                wrapper.setSpacing(false);
                wrapper.setPadding(false);
                wrapper.addClassName("stack-sm");
                int initial = Math.max(1, minItems);
                for (int i = 0; i < initial; i++) {
                    addRow.accept(null);
                }
                comp = wrapper;

                localeUpdaters.add(() -> {
                    addBtn.setText(getTranslation("form.addItem"));
                    groupContainer.getChildren().forEach(child -> {
                        if (child instanceof HorizontalLayout hl) {
                            hl.getChildren().forEach(c -> {
                                if (c instanceof Button b && b.getElement().getThemeList().contains("error")) {
                                    b.setText(getTranslation("form.removeItem"));
                                }
                            });
                        }
                    });
                });
            }
            case "radio" -> {
                RadioButtonGroup<String> radio = new RadioButtonGroup<>();
                radio.setWidthFull();
                radio.setLabel(label);
                if (field.has("options") && field.get("options").isArray()) {
                    List<String> values = new ArrayList<>();
                    Map<String, String> display = new HashMap<>();
                    for (JsonNode opt : field.get("options")) {
                        String val = opt.get("value").asText();
                        values.add(val);
                        String lbl = getTranslationFromNode(opt.get("label"));
                        display.put(val, lbl);
                    }
                    radio.setItems(values);
                    radio.setItemLabelGenerator(item -> display.getOrDefault(item, item));
                    localeUpdaters.add(() -> {
                        Map<String, String> disp = new HashMap<>();
                        for (JsonNode opt : field.get("options")) {
                            disp.put(opt.get("value").asText(), getTranslationFromNode(opt.get("label")));
                        }
                        radio.setItemLabelGenerator(item -> disp.getOrDefault(item, item));
                        radio.setLabel(getTranslationFromNode(field.get("label")));
                    });
                }
                if (required) {
                    radio.setRequiredIndicatorVisible(true);
                }
                radio.addValueChangeListener(ev -> {
                    fieldValues.put(name, ev.getValue());
                    if (required) {
                        if (ev.getValue() == null) {
                            setError(radio, getTranslation("form.required"));
                        } else {
                            clearError(radio);
                        }
                    }
                    runVisibilityWatchers(name);
                });
                comp = radio;
            }
            default -> {
                return null;
            }
        }

        fieldComponents.put(name, comp);
        fieldValues.putIfAbsent(name, null);
        if (field.has("visibleWhen")) {
            setupVisibilityWatcher(name, field.get("visibleWhen"));
        }
        localeUpdaters.add(() -> updateComponentLabel(comp, field));
        return comp;
    }
    /**
     * Apply any additional validators defined in the field spec to the given
     * text component. Patterns specified under the "validators" array will
     * be transformed into client‑side checks. The component must support
     * {@code setInvalid(boolean)} and {@code setErrorMessage(String)}.
     *
     * @param comp the input component
     * @param field the field definition
     */
    private void applyValidators(Component comp, JsonNode field) {
        if (!field.has("validators")) {
            return;
        }
        JsonNode validators = field.get("validators");
        if (!validators.isArray()) {
            return;
        }
        for (JsonNode val : validators) {
            String type = val.has("type") ? val.get("type").asText() : "";
            switch (type) {
                case "pattern" -> {
                    String pattern = val.get("value").asText();
                    // message may be keyed by locale
                    String message = getTranslationFromNode(val.get("message"));
                    if (comp instanceof TextField tf) {
                        tf.addValueChangeListener(ev -> {
                            String v = ev.getValue();
                            if (v != null && !v.isEmpty() && !v.matches(pattern)) {
                                tf.setInvalid(true);
                                tf.setErrorMessage(message);
                            } else {
                                // clear error if matches
                                tf.setInvalid(false);
                                tf.setErrorMessage(null);
                            }
                        });
                    } else if (comp instanceof EmailField ef) {
                        ef.addValueChangeListener(ev -> {
                            String v = ev.getValue();
                            if (v != null && !v.isEmpty() && !v.matches(pattern)) {
                                ef.setInvalid(true);
                                ef.setErrorMessage(message);
                            } else {
                                ef.setInvalid(false);
                                ef.setErrorMessage(null);
                            }
                        });
                    } else if (comp instanceof TextField tf2) {
                        // Already handled above; included for completeness
                    }
                }
                // Additional validator types can be handled here
            }
        }
    }

    /**
     * Validate a single field for required and any pattern validators. This
     * method sets the component's invalid state and error message if the
     * value does not meet the criteria.
     *
     * @param comp the field component
     * @param name the property name
     * @param required whether the field is required
     * @param fieldSpec the original field specification
     */
    private void validateField(Component comp, String name, boolean required, JsonNode fieldSpec) {
        Object value = fieldValues.get(name);
        // Required check
        boolean empty = value == null || (value instanceof String str && str.isBlank());
        if (required && empty) {
            setError(comp, getTranslation("form.required"));
            return;
        }
        // Pattern checks are applied via listeners in applyValidators
        // Clear any existing required error if not empty
        clearError(comp);
    }

    /**
     * Set an error message on a supported field component. Currently
     * supports TextField, EmailField, NumberField, ComboBox and Checkbox.
     *
     * @param comp the component
     * @param message the error message
     */
    private void setError(Component comp, String message) {
        if (comp instanceof TextField tf) {
            tf.setInvalid(true);
            tf.setErrorMessage(message);
        } else if (comp instanceof EmailField ef) {
            ef.setInvalid(true);
            ef.setErrorMessage(message);
        } else if (comp instanceof NumberField nf) {
            nf.setInvalid(true);
            nf.setErrorMessage(message);
        } else if (comp instanceof ComboBox<?> cb) {
            cb.setInvalid(true);
            cb.setErrorMessage(message);
        } else if (comp instanceof Checkbox) {
            // Checkbox does not support invalid state natively
        } else if (comp instanceof com.vaadin.flow.component.datepicker.DatePicker dp) {
            dp.setInvalid(true);
            dp.setErrorMessage(message);
        } else if (comp instanceof JalaliDateTimePicker jalali) {
            jalali.setInvalid(true);
            jalali.setErrorMessage(message);
        } else if (comp instanceof com.vaadin.flow.component.radiobutton.RadioButtonGroup<?> rg) {
            rg.setInvalid(true);
            rg.setErrorMessage(message);
        }
    }

    /**
     * Clear any invalid flag and error message from the component.
     *
     * @param comp the component
     */
    private void clearError(Component comp) {
        if (comp instanceof TextField tf) {
            tf.setInvalid(false);
            tf.setErrorMessage(null);
        } else if (comp instanceof EmailField ef) {
            ef.setInvalid(false);
            ef.setErrorMessage(null);
        } else if (comp instanceof NumberField nf) {
            nf.setInvalid(false);
            nf.setErrorMessage(null);
        } else if (comp instanceof ComboBox<?> cb) {
            cb.setInvalid(false);
            cb.setErrorMessage(null);
        } else if (comp instanceof com.vaadin.flow.component.datepicker.DatePicker dp) {
            dp.setInvalid(false);
            dp.setErrorMessage(null);
        } else if (comp instanceof JalaliDateTimePicker jalali) {
            jalali.setInvalid(false);
            jalali.setErrorMessage(null);
        } else if (comp instanceof com.vaadin.flow.component.radiobutton.RadioButtonGroup<?> rg) {
            rg.setInvalid(false);
            rg.setErrorMessage(null);
        }
    }

    /**
     * Attach a visibility watcher to a field with conditional visibility.
     * The watcher will update the visibility of the target field when the
     * controlling fields change. Conditions are specified under
     * "visibleWhen" with either "all" or "any" arrays of conditions.
     *
     * @param fieldName the field that will be shown/hidden
     * @param condition the JSON node describing the visibility condition
     */
    private void setupVisibilityWatcher(String fieldName, JsonNode condition) {
        // Determine the condition type (all or any)
        boolean requireAll = condition.has("all");
        ArrayNode condArray = requireAll ? (ArrayNode) condition.get("all") : (ArrayNode) condition.get("any");
        if (condArray == null) {
            return;
        }
        // Build a predicate evaluating the condition
        Supplier<Boolean> predicate = () -> {
            boolean result = requireAll;
            for (JsonNode cond : condArray) {
                String depField = cond.get("field").asText();
                String op = cond.get("op").asText("EQ");
                JsonNode valNode = cond.get("value");
                Object expected = null;
                if (valNode != null && !valNode.isNull()) {
                    if (valNode.isBoolean()) expected = valNode.asBoolean();
                    else if (valNode.isNumber()) expected = valNode.numberValue();
                    else expected = valNode.asText();
                }
                Object actual = fieldValues.get(depField);
                boolean match = false;
                if ("EQ".equals(op)) {
                    match = (expected == null && actual == null) || (expected != null && expected.equals(actual));
                }
                // Extend here for more operators as needed
                if (requireAll) {
                    if (!match) {
                        result = false;
                        break;
                    }
                } else {
                    if (match) {
                        result = true;
                        break;
                    }
                }
            }
            return result;
        };
        // Define the update action for the field
        Runnable update = () -> {
            Component comp = fieldComponents.get(fieldName);
            if (comp != null) {
                comp.setVisible(predicate.get());
            }
        };
        // Register watchers for each dependent field
        for (JsonNode cond : condArray) {
            String depField = cond.get("field").asText();
            visibilityWatchers.computeIfAbsent(depField, k -> new ArrayList<>()).add(update);
        }
        // Initialise the visibility
        update.run();
    }

    /**
     * Run all registered visibility watchers for the given field. This should
     * be invoked whenever a controlling field value changes.
     *
     * @param fieldName the name of the controlling field
     */
    private void runVisibilityWatchers(String fieldName) {
        List<Runnable> watchers = visibilityWatchers.get(fieldName);
        if (watchers != null) {
            watchers.forEach(Runnable::run);
        }
    }

    /**
     * Retrieve a translated string from a node containing language keys. If
     * the current locale language is not present, fallback to English if
     * available, else return the first available translation. If the node
     * is a simple text node, return its text directly.
     *
     * @param node the JSON node containing translations
     * @return the selected translation or {@code null}
     */
    private String getTranslationFromNode(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        Locale locale = UI.getCurrent().getLocale();
        String lang = locale != null ? locale.getLanguage() : "en";
        JsonNode child = node.get(lang);
        if (child != null) {
            return child.asText();
        }
        // fallback to English
        child = node.get("en");
        if (child != null) {
            return child.asText();
        }
        // fallback to any available
        if (node.fieldNames().hasNext()) {
            return node.get(node.fieldNames().next()).asText();
        }
        return null;
    }

    /**
     * Update the label of a field component based on the current locale
     * and the original field specification. This method is invoked
     * whenever the locale changes.
     *
     * @param comp the component to update
     * @param fieldSpec the field definition
     */
    private void updateComponentLabel(Component comp, JsonNode fieldSpec) {
        String label = getTranslationFromNode(fieldSpec.get("label"));
        if (comp instanceof TextField tf) {
            tf.setLabel(label);
        } else if (comp instanceof EmailField ef) {
            ef.setLabel(label);
        } else if (comp instanceof NumberField nf) {
            nf.setLabel(label);
        } else if (comp instanceof ComboBox<?> cb) {
            cb.setLabel(label);
            // update item labels as well
            if (fieldSpec.has("options") && fieldSpec.get("options").isArray()) {
                Map<String, String> display = new HashMap<>();
                for (JsonNode opt : fieldSpec.get("options")) {
                    String value = opt.get("value").asText();
                    String lbl = getTranslationFromNode(opt.get("label"));
                    display.put(value, lbl);
                }
                ((ComboBox<String>) cb).setItemLabelGenerator(item -> display.getOrDefault(item, item));
            }
        } else if (comp instanceof Checkbox cb) {
            cb.setLabel(label);
        } else if (comp instanceof JalaliDateTimePicker jalali) {
            jalali.setLabel(label);
        }
    }

    /**
     * Translate a key using Vaadin's translation mechanism. If no
     * translation is found, the key itself is returned. This delegates
     * to UI.getCurrent().getTranslation() which uses the configured
     * translation provider and locale.
     *
     * @param key the translation key
     * @return the translated string
     */
    private String getTranslation(String key) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            return ui.getTranslation(key);
        }
        return key;
    }

    /**
     * Convert a byte count into a human readable string using decimal units.
     * For example, 1536 becomes "1.5 KB".  This helper is used to
     * display file sizes in the file upload components.  It was present
     * in earlier versions of this component but was accidentally removed
     * during refactoring.  Restoring it here ensures that file size
     * information can be rendered correctly.
     *
     * @param bytes number of bytes
     * @return human readable representation
     */
    private static String humanReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp - 1) + "";
        return String.format(java.util.Locale.ROOT, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Perform form submission. First validate all client‑side rules and
     * if none are violated, send the collected data to the validation
     * service. Backend validation errors are then displayed on the
     * corresponding fields. If no errors remain, a notification is
     * shown indicating success.
     */
    private void submit(ActionDefinition action) {
        if (action == null) {
            action = actionsById.values().stream().findFirst().orElse(defaultActionDefinition());
        }
        if (shouldRunClientValidation(action) && runClientValidation()) {
            Notification.show(getTranslation("form.correctErrors"));
            return;
        }

        Map<String, String> backendErrors = Collections.emptyMap();
        if (shouldRunBackendValidation(action)) {
            backendErrors = validationService.validate(formId, new HashMap<>(fieldValues), action.id());
        }
        if (!backendErrors.isEmpty()) {
            backendErrors.forEach((field, msgKey) -> {
                Component comp = fieldComponents.get(field);
                if (comp != null) {
                    setError(comp, getTranslation(msgKey));
                }
            });
            Notification.show(getTranslation("form.correctErrors"));
            return;
        }

        Notification.show(resolveActionSuccessMessage(action));
        fireSubmissionEvent(action);
    }

    private boolean shouldRunClientValidation(ActionDefinition action) {
        return action.clientValidation();
    }

    private boolean shouldRunBackendValidation(ActionDefinition action) {
        if (action.backendValidationOverride() != null) {
            return action.backendValidationOverride();
        }
        if (submitConfig != null && submitConfig.has("backendValidation")) {
            return submitConfig.get("backendValidation").asBoolean(true);
        }
        return true;
    }

    private boolean runClientValidation() {
        boolean hasErrors = false;
        for (Map.Entry<String, Component> entry : fieldComponents.entrySet()) {
            String name = entry.getKey();
            Component comp = entry.getValue();
            JsonNode fieldSpec = findFieldSpecByName(name);
            if (fieldSpec == null) {
                continue;
            }
            boolean required = fieldSpec.has("required") && fieldSpec.get("required").asBoolean(false);
            validateField(comp, name, required, fieldSpec);
            if (comp instanceof TextField tf) {
                if (tf.isInvalid()) {
                    hasErrors = true;
                }
            } else if (comp instanceof EmailField ef) {
                if (ef.isInvalid()) {
                    hasErrors = true;
                }
            } else if (comp instanceof NumberField nf) {
                if (nf.isInvalid()) {
                    hasErrors = true;
                }
            } else if (comp instanceof ComboBox<?> cb) {
                if (cb.isInvalid()) {
                    hasErrors = true;
                }
            } else if (comp instanceof JalaliDateTimePicker jalali) {
                if (jalali.isInvalid()) {
                    hasErrors = true;
                }
            }
        }
        return hasErrors;
    }

    private void fireSubmissionEvent(ActionDefinition action) {
        if (submissionListeners.isEmpty()) {
            return;
        }
        Map<String, Object> snapshot = Collections.unmodifiableMap(new HashMap<>(fieldValues));
        FormSubmissionEvent event = new FormSubmissionEvent(this, action.id(), snapshot, action.rawNode(), submitConfig,
                action.button());
        submissionListeners.forEach(listener -> listener.onSubmit(event));
    }

    /**
     * Find the JSON specification for a given field name. Searches
     * through all sections and fields to locate the field definition.
     *
     * @param name the field name
     * @return the field definition or {@code null} if not found
     */
    private JsonNode findFieldSpecByName(String name) {
        ArrayNode layout = (ArrayNode) spec.get("layout");
        if (layout == null) return null;
        for (JsonNode section : layout) {
            if (section.has("fields")) {
                for (JsonNode field : section.get("fields")) {
                    if (field.has("name") && name.equals(field.get("name").asText())) {
                        return field;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Register a listener that will be notified after a successful submission.
     * The listener receives the triggered action identifier, the immutable
     * snapshot of field values, and direct access to the originating JSON
     * snippets for further processing.
     *
     * @param listener the listener to register (must not be {@code null})
     * @return a {@link Registration} for removing the listener
     */
    public Registration addSubmissionListener(SubmissionListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        submissionListeners.add(listener);
        return () -> submissionListeners.remove(listener);
    }

    /**
     * Programmatically trigger submission for the given action identifier.
     * This executes the same validation pipeline as clicking the associated
     * button.
     *
     * @param actionId the identifier declared in the form specification
     */
    public void submit(String actionId) {
        ActionDefinition action = actionsById.get(actionId);
        if (action == null) {
            throw new IllegalArgumentException("Unknown form action: " + actionId);
        }
        submit(action);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        if (formTitle != null) {
            String title = getTranslationFromNode(spec.get("title"));
            if (hasText(title)) {
                formTitle.setText(title);
            }
        }
        // Update section titles and field labels via localeUpdaters
        localeUpdaters.forEach(Runnable::run);
    }

    @FunctionalInterface
    public interface SubmissionListener {
        void onSubmit(FormSubmissionEvent event);
    }

    public static final class FormSubmissionEvent {
        private final GeneratedForm source;
        private final String actionId;
        private final Map<String, Object> values;
        private final JsonNode actionSpec;
        private final JsonNode submitSpec;
        private final Button actionButton;

        private FormSubmissionEvent(GeneratedForm source, String actionId, Map<String, Object> values, JsonNode actionSpec,
                JsonNode submitSpec, Button actionButton) {
            this.source = source;
            this.actionId = actionId;
            this.values = values;
            this.actionSpec = actionSpec;
            this.submitSpec = submitSpec;
            this.actionButton = actionButton;
        }

        public GeneratedForm getSource() {
            return source;
        }

        public String getActionId() {
            return actionId;
        }

        public Map<String, Object> getValues() {
            return values;
        }

        public JsonNode getActionSpec() {
            return actionSpec;
        }

        public JsonNode getSubmitSpec() {
            return submitSpec;
        }

        public Button getActionButton() {
            return actionButton;
        }
    }

    private record ActionDefinition(String id, JsonNode labelNode, String labelKey, List<ButtonVariant> themeVariants,
            boolean clientValidation, Boolean backendValidationOverride, JsonNode successMessageNode,
            String successMessageKey, JsonNode rawNode, Button button) {

        ActionDefinition withButton(Button enrichedButton) {
            return new ActionDefinition(id, labelNode, labelKey, themeVariants, clientValidation, backendValidationOverride,
                    successMessageNode, successMessageKey, rawNode, enrichedButton);
        }
    }
}