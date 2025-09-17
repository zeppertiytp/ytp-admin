package com.example.adminpanel.component;

import com.example.adminpanel.service.FormValidationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.example.adminpanel.component.LocationPicker;
import java.io.ByteArrayInputStream;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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



    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonNode spec;
    private final String formId;
    private final FormValidationService validationService;
    private final Map<String, Component> fieldComponents = new HashMap<>();
    private final Map<String, Object> fieldValues = new HashMap<>();
    private final Map<String, List<Runnable>> visibilityWatchers = new HashMap<>();
    private final List<Runnable> localeUpdaters = new ArrayList<>();
    private H3 titleHeading;
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
        this.formId = spec.has("id") ? spec.get("id").asText() : jsonResource;
        buildForm();
        addClassNames("surface-card", "form-shell");
        setPadding(false);
        setSpacing(false);
        setWidthFull();
    }

    /**
     * Build the form UI from the JSON specification. Sections are rendered
     * as titled blocks and fields are laid out using {@link FormLayout}
     * with the configured number of columns. Field values and validity
     * state are stored internally; external consumers should call
     * {@link #submit()} to trigger backend validation and optionally
     * process the submission.
     */
    private void buildForm() {
        removeAll();
        localeUpdaters.clear();

        String title = getTranslationFromNode(spec.get("title"));
        if (StringUtils.hasText(title)) {
            titleHeading = new H3(title);
            titleHeading.addClassName("form-shell__title");
            add(titleHeading);
            localeUpdaters.add(() -> {
                String translated = getTranslationFromNode(spec.get("title"));
                if (StringUtils.hasText(translated)) {
                    titleHeading.setText(translated);
                }
            });
        } else {
            titleHeading = null;
        }

        VerticalLayout sections = new VerticalLayout();
        sections.addClassName("form-sections");
        sections.setPadding(false);
        sections.setSpacing(false);
        sections.setWidthFull();
        sections.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        add(sections);

        ArrayNode layout = (ArrayNode) spec.get("layout");
        if (layout != null) {
            layout.forEach(sectionNode -> {
                Component built = buildSection(sectionNode);
                if (built != null) {
                    sections.add(built);
                }
            });
        }

        submitButton = new Button(getTranslation("form.submit"));
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.addClassName("form-submit");
        submitButton.addClickListener(e -> submit());
        localeUpdaters.add(() -> submitButton.setText(getTranslation("form.submit")));

        Div actions = new Div();
        actions.addClassName("form-actions");
        actions.add(submitButton);
        add(actions);
    }

    /**
     * Create a section of the form from its JSON definition. Each section
     * may specify a title, number of columns, and an array of fields.
     *
     * @param section the JSON node describing a section
     */
    private Component buildSection(JsonNode section) {
        String type = section.has("type") ? section.get("type").asText() : "";
        if (!"section".equals(type)) {
            return null;
        }
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.addClassName("form-section");
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setWidthFull();
        wrapper.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);

        String title = getTranslationFromNode(section.get("title"));
        if (StringUtils.hasText(title)) {
            H4 h4 = new H4(title);
            h4.addClassName("form-section__title");
            wrapper.add(h4);
            localeUpdaters.add(() -> {
                String t = getTranslationFromNode(section.get("title"));
                if (StringUtils.hasText(t)) {
                    h4.setText(t);
                }
            });
        }
        int columns = section.has("columns") ? section.get("columns").asInt(1) : 1;
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(List.of(new FormLayout.ResponsiveStep("0", columns)));
        formLayout.setWidthFull();
        // Add a gap between columns to improve spacing, especially in RTL languages
        formLayout.getStyle().set("column-gap", "var(--lumo-space-m)");
        formLayout.getStyle().set("row-gap", "var(--lumo-space-m)");
        // Fields
        ArrayNode fields = (ArrayNode) section.get("fields");
        if (fields != null) {
            fields.forEach(field -> {
                Component comp = createField(field);
                if (comp != null) {
                    formLayout.add(comp);
                }
            });
        }
        wrapper.add(formLayout);
        return wrapper;
    }

    /**
     * Create a Vaadin component for a single field definition. Supported
     * field types include text, email, tel, number, select and switch.
     * Required and pattern validations are applied client‑side; backend
     * validation will be triggered on submit.
     *
     * @param field the JSON node describing the field
     * @return the created component or {@code null} if type is unknown
     */
    /**
 * Create a Vaadin component for a single field definition.
 */
private Component createField(JsonNode field) {
    String name = field.get("name").asText();
    String type = field.get("type").asText();
    String label = getTranslationFromNode(field.get("label"));
    boolean required = field.has("required") && field.get("required").asBoolean(false);

    Component comp;

    switch (type) {

        case "text": {
            TextField tf = new TextField(label);
            tf.setClearButtonVisible(true);
            tf.setWidthFull();
            if (required) tf.setRequiredIndicatorVisible(true);
            applyValidators(tf, field);
            tf.addValueChangeListener(ev -> {
                fieldValues.put(name, ev.getValue());
                validateField(tf, name, required, field);
                runVisibilityWatchers(name);
            });
            comp = tf;
            break;
        }

        case "email": {
            EmailField ef = new EmailField(label);
            ef.setClearButtonVisible(true);
            ef.setWidthFull();
            if (required) ef.setRequiredIndicatorVisible(true);
            applyValidators(ef, field);
            ef.addValueChangeListener(ev -> {
                fieldValues.put(name, ev.getValue());
                validateField(ef, name, required, field);
                runVisibilityWatchers(name);
            });
            comp = ef;
            break;
        }

        case "tel": {
            TextField tf = new TextField(label);
            tf.setClearButtonVisible(true);
            tf.setWidthFull();
            if (required) tf.setRequiredIndicatorVisible(true);
            applyValidators(tf, field);
            tf.addValueChangeListener(ev -> {
                fieldValues.put(name, ev.getValue());
                validateField(tf, name, required, field);
                runVisibilityWatchers(name);
            });
            comp = tf;
            break;
        }

        case "number": {
            NumberField nf = new NumberField(label);
            nf.setClearButtonVisible(true);
            nf.setWidthFull();
            if (required) nf.setRequiredIndicatorVisible(true);
            applyValidators(nf, field);
            nf.addValueChangeListener(ev -> {
                fieldValues.put(name, ev.getValue());
                validateField(nf, name, required, field);
                runVisibilityWatchers(name);
            });
            comp = nf;
            break;
        }

        case "select": {
            ComboBox<String> cb = new ComboBox<>(label);
            cb.setWidthFull();
            java.util.List<String> values = new java.util.ArrayList<>();
            java.util.Map<String, String> display = new java.util.HashMap<>();
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
            if (required) cb.setRequiredIndicatorVisible(true);
            cb.addValueChangeListener(ev -> {
                fieldValues.put(name, ev.getValue());
                validateField(cb, name, required, field);
                runVisibilityWatchers(name);
            });
            comp = cb;
            break;
        }

        case "switch": {
            Checkbox cb = new Checkbox(label);
            cb.addValueChangeListener(ev -> {
                fieldValues.put(name, ev.getValue());
                validateField(cb, name, required, field);
                runVisibilityWatchers(name);
            });
            comp = cb;
            break;
        }

        case "date": {
            DatePicker dp = new DatePicker(label);
            dp.setWidthFull();
            if (required) dp.setRequiredIndicatorVisible(true);
            dp.addValueChangeListener(ev -> {
                fieldValues.put(name, ev.getValue());
                if (required) {
                    if (ev.getValue() == null) setError(dp, getTranslation("form.required"));
                    else clearError(dp);
                }
                runVisibilityWatchers(name);
            });
            comp = dp;
            break;
        }

        case "file": {
            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("*/*");
            upload.setMaxFiles(1);
            upload.setDropLabel(new Span(label));
            upload.setUploadButton(new Button(label));

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
            wrapper.setSpacing(true);
            comp = wrapper;
            break;
        }

        case "multiFile": {
            MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("*/*");
            upload.setDropLabel(new Span(label));
            upload.setUploadButton(new Button(label));

            java.util.List<String> files = new java.util.ArrayList<>();
            Span namesInfo = new Span();
            namesInfo.setVisible(false);
            Runnable updateNames = () -> {
                if (files.isEmpty()) { namesInfo.setText(""); namesInfo.setVisible(false); }
                else { namesInfo.setText(String.join(", ", files)); namesInfo.setVisible(true); }
            };

            upload.addSucceededListener(ev -> {
                files.add(ev.getFileName());
                fieldValues.put(name, new java.util.ArrayList<>(files));
                updateNames.run();
                runVisibilityWatchers(name);
            });

            onFileRemove(upload, removed -> {
                files.remove(removed);
                fieldValues.put(name, new java.util.ArrayList<>(files));
                updateNames.run();
                runVisibilityWatchers(name);
            });

            VerticalLayout wrapper = new VerticalLayout(upload, namesInfo);
            wrapper.setPadding(false);
            wrapper.setSpacing(true);
            comp = wrapper;
            break;
        }

        case "image": {
            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("image/*");
            upload.setMaxFiles(1);
            upload.setDropLabel(new Span(label));
            upload.setUploadButton(new Button(label));

            Image preview = new Image();
            preview.setWidth("100px");
            preview.setHeight("100px");
            preview.getStyle().set("object-fit", "cover");
            preview.setVisible(false);

            upload.addSucceededListener(ev -> {
                try {
                    byte[] data = buffer.getInputStream().readAllBytes();
                    StreamResource res = new StreamResource(ev.getFileName(), () -> new java.io.ByteArrayInputStream(data));
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

            VerticalLayout wrap = new VerticalLayout(upload, preview);
            wrap.setPadding(false);
            wrap.setSpacing(true);
            comp = wrap;
            break;
        }

        case "multiImage": {
            MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("image/*");
            upload.setDropLabel(new Span(label));
            upload.setUploadButton(new Button(label));

            java.util.List<String> images = new java.util.ArrayList<>();
            java.util.Map<String, Image> thumbs = new java.util.HashMap<>();
            HorizontalLayout previewContainer = new HorizontalLayout();
            previewContainer.setSpacing(true);
            previewContainer.setVisible(false);

            upload.addSucceededListener(ev -> {
                String fname = ev.getFileName();
                images.add(fname);
                try {
                    byte[] data = buffer.getInputStream(fname).readAllBytes();
                    StreamResource res = new StreamResource(fname, () -> new java.io.ByteArrayInputStream(data));
                    Image img = new Image(res, fname);
                    img.setWidth("100px");
                    img.setHeight("100px");
                    img.getStyle().set("object-fit", "cover");
                    thumbs.put(fname, img);
                    previewContainer.add(img);
                    previewContainer.setVisible(true);
                    fieldValues.put(name, new java.util.ArrayList<>(images));
                    runVisibilityWatchers(name);
                } catch (Exception ex) { /* ignore */ }
            });

            onFileRemove(upload, removed -> {
                images.remove(removed);
                Image img = thumbs.remove(removed);
                if (img != null) previewContainer.remove(img);
                if (images.isEmpty()) previewContainer.setVisible(false);
                fieldValues.put(name, new java.util.ArrayList<>(images));
                runVisibilityWatchers(name);
            });

            VerticalLayout wrapper = new VerticalLayout(upload, previewContainer);
            wrapper.setPadding(false);
            wrapper.setSpacing(true);
            comp = wrapper;
            break;
        }

        case "map": {
            Button open = new Button(getTranslation("form.pickLocation"));
            Span selected = new Span(getTranslation("form.noLocationSelected"));

            Dialog dialog = new Dialog();
            dialog.setWidth("820px");
            dialog.setHeight("620px");

            LocationPicker picker = new LocationPicker();
            picker.addLocationSelectedListener(ev -> {
                String textPos = ev.getLat() + ", " + ev.getLng();
                selected.setText(getTranslation("form.selectedLocation") + ": " + textPos);
                fieldValues.put(name, java.util.Map.of("lat", ev.getLat(), "lng", ev.getLng()));
                runVisibilityWatchers(name);
                dialog.close();
            });
            dialog.add(picker);

            open.addClickListener(e -> dialog.open());

            VerticalLayout wrapper = new VerticalLayout(open, selected);
            wrapper.setPadding(false);
            wrapper.setSpacing(true);
            comp = wrapper;
            break;
        }

        case "group": {
            VerticalLayout groupContainer = new VerticalLayout();
            groupContainer.setPadding(false);
            groupContainer.setSpacing(true);
            int minItems = field.has("minItems") ? field.get("minItems").asInt() : 0;
            int maxItems = field.has("maxItems") ? field.get("maxItems").asInt() : Integer.MAX_VALUE;
            java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
            Button addBtn = new Button("+");

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

            java.util.function.Consumer<Void> addRow = unused -> {
                if (items.size() >= maxItems) return;
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.setSpacing(true);
                java.util.Map<String, Object> itemValues = new java.util.HashMap<>();
                if (field.has("fields") && field.get("fields").isArray()) {
                    for (JsonNode sub : (ArrayNode) field.get("fields")) {
                        String subName = sub.get("name").asText();
                        String subType = sub.get("type").asText();
                        String subLabel = getTranslationFromNode(sub.get("label"));
                        boolean subReq = sub.has("required") && sub.get("required").asBoolean(false);
                        Component fcomp;
                        switch (subType) {
                            case "text": {
                                TextField tf = new TextField(subLabel);
                                if (subReq) tf.setRequiredIndicatorVisible(true);
                                tf.setClearButtonVisible(true);
                                tf.addValueChangeListener(ev -> itemValues.put(subName, ev.getValue()));
                                fcomp = tf;
                                break;
                            }
                            case "number": {
                                NumberField nf = new NumberField(subLabel);
                                if (subReq) nf.setRequiredIndicatorVisible(true);
                                nf.setClearButtonVisible(true);
                                nf.addValueChangeListener(ev -> itemValues.put(subName, ev.getValue()));
                                fcomp = nf;
                                break;
                            }
                            case "email": {
                                EmailField ef = new EmailField(subLabel);
                                if (subReq) ef.setRequiredIndicatorVisible(true);
                                ef.setClearButtonVisible(true);
                                ef.addValueChangeListener(ev -> itemValues.put(subName, ev.getValue()));
                                fcomp = ef;
                                break;
                            }
                            case "tel": {
                                TextField tf2 = new TextField(subLabel);
                                if (subReq) tf2.setRequiredIndicatorVisible(true);
                                tf2.setClearButtonVisible(true);
                                tf2.addValueChangeListener(ev -> itemValues.put(subName, ev.getValue()));
                                fcomp = tf2;
                                break;
                            }
                            default: {
                                TextField def = new TextField(subLabel);
                                def.addValueChangeListener(ev -> itemValues.put(subName, ev.getValue()));
                                fcomp = def;
                                break;
                            }
                        }
                        if (fcomp instanceof com.vaadin.flow.component.HasSize hs) hs.setWidth("200px");
                        row.add(fcomp);
                    }
                }
                Button rem = new Button("-");
                rem.getElement().getThemeList().add("error");
                rem.addClickListener(ev -> {
                    if (items.size() > minItems) {
                        groupContainer.remove(row);
                        items.remove(itemValues);
                        fieldValues.put(name, new java.util.ArrayList<>(items));
                        runVisibilityWatchers(name);
                        refreshGroupButtons.run();
                    }
                });
                row.add(rem);
                groupContainer.add(row);
                items.add(itemValues);
                fieldValues.put(name, new java.util.ArrayList<>(items));
                runVisibilityWatchers(name);
                refreshGroupButtons.run();
            };

            addBtn.addClickListener(ev -> addRow.accept(null));
            VerticalLayout wrapper = new VerticalLayout(groupContainer, addBtn);
            wrapper.setSpacing(true);
            wrapper.setPadding(false);
            int initial = Math.max(1, minItems);
            for (int i = 0; i < initial; i++) addRow.accept(null);
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
            break;
        }

        case "radio": {
            com.vaadin.flow.component.radiobutton.RadioButtonGroup<String> radio =
                new com.vaadin.flow.component.radiobutton.RadioButtonGroup<>();
            radio.setWidthFull();
            radio.setLabel(label);
            if (field.has("options") && field.get("options").isArray()) {
                java.util.List<String> values = new java.util.ArrayList<>();
                java.util.Map<String, String> display = new java.util.HashMap<>();
                for (JsonNode opt : field.get("options")) {
                    String val = opt.get("value").asText();
                    values.add(val);
                    String lbl = getTranslationFromNode(opt.get("label"));
                    display.put(val, lbl);
                }
                radio.setItems(values);
                radio.setItemLabelGenerator(item -> display.getOrDefault(item, item));
                localeUpdaters.add(() -> {
                    java.util.Map<String, String> disp = new java.util.HashMap<>();
                    for (JsonNode opt : field.get("options")) {
                        String val = opt.get("value").asText();
                        String lbl = getTranslationFromNode(opt.get("label"));
                        disp.put(val, lbl);
                    }
                    radio.setItemLabelGenerator(item -> disp.getOrDefault(item, item));
                    radio.setLabel(getTranslationFromNode(field.get("label")));
                });
            }
            if (required) radio.setRequiredIndicatorVisible(true);
            radio.addValueChangeListener(ev -> {
                fieldValues.put(name, ev.getValue());
                if (required) {
                    if (ev.getValue() == null) setError(radio, getTranslation("form.required"));
                    else clearError(radio);
                }
                runVisibilityWatchers(name);
            });
            comp = radio;
            break;
        }

        default: {
            return null;
        }
    }

    fieldComponents.put(name, comp);
    fieldValues.put(name, null);
    if (field.has("visibleWhen")) {
        JsonNode cond = field.get("visibleWhen");
        setupVisibilityWatcher(name, cond);
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
    private void submit() {
        // Validate required and pattern constraints manually
        boolean hasErrors = false;
        for (Map.Entry<String, Component> entry : fieldComponents.entrySet()) {
            String name = entry.getKey();
            Component comp = entry.getValue();
            JsonNode fieldSpec = findFieldSpecByName(name);
            if (fieldSpec == null) continue;
            boolean required = fieldSpec.has("required") && fieldSpec.get("required").asBoolean(false);
            validateField(comp, name, required, fieldSpec);
            if (comp instanceof TextField tf) {
                if (tf.isInvalid()) hasErrors = true;
            } else if (comp instanceof EmailField ef) {
                if (ef.isInvalid()) hasErrors = true;
            } else if (comp instanceof NumberField nf) {
                if (nf.isInvalid()) hasErrors = true;
            } else if (comp instanceof ComboBox<?> cb) {
                if (cb.isInvalid()) hasErrors = true;
            }
        }
        if (hasErrors) {
            Notification.show(getTranslation("form.correctErrors"));
            return;
        }
        // Backend validation
        Map<String, String> errors = validationService.validate(formId, new HashMap<>(fieldValues));
        if (!errors.isEmpty()) {
            errors.forEach((field, msgKey) -> {
                Component comp = fieldComponents.get(field);
                if (comp != null) {
                    setError(comp, getTranslation(msgKey));
                }
            });
            Notification.show(getTranslation("form.correctErrors"));
            return;
        }
        // Success
        Notification.show(getTranslation("form.success"));
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

    @Override
    public void localeChange(LocaleChangeEvent event) {
        // Update title
        String title = getTranslationFromNode(spec.get("title"));
        if (StringUtils.hasText(title)) {
            // The first child is expected to be the title H3
            if (!getChildren().findFirst().isEmpty()) {
                Component first = getChildren().findFirst().get();
                if (first instanceof H3 h) {
                    h.setText(title);
                }
            }
        }
        // Update section titles and field labels via localeUpdaters
        localeUpdaters.forEach(Runnable::run);
    }
}