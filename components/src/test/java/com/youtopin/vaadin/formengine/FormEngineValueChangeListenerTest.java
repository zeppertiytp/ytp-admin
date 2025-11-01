package com.youtopin.vaadin.formengine;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.I18NProvider;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm.FieldValueChangeEvent;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm.FieldValueChangeListener;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.formengine.binder.DynamicPropertyBag;
import com.youtopin.vaadin.formengine.binder.ValidationContext;

class FormEngineValueChangeListenerTest {

    private FormEngine engine;

    @BeforeEach
    void setUp() {
        engine = new FormEngine(new OptionCatalogRegistry());
    }

    @Test
    void valueChangeEventIncludesValidationContext() {
        RenderedForm<DirectoryBean> rendered = renderForm();
        DirectoryBean bean = new DirectoryBean();
        rendered.initializeWithBean(bean);

        Map<FieldDefinition, FieldInstance> fields = rendered.getFields();
        FieldInstance codeField = findField(fields, "code");
        FieldInstance labelField = findField(fields, "label");

        ((TextField) codeField.getValueComponent()).setValue("C-001");

        List<FieldValueChangeEvent<DirectoryBean>> events = new ArrayList<>();
        FieldValueChangeListener<DirectoryBean> listener = events::add;
        rendered.addValueChangeListener(listener);

        ((TextField) labelField.getValueComponent()).setValue("Main branch");

        assertThat(events).hasSize(1);
        FieldValueChangeEvent<DirectoryBean> event = events.get(0);
        assertThat(event.getForm()).isSameAs(rendered);
        assertThat(event.getFieldDefinition().getPath()).isEqualTo("label");
        assertThat(event.getValue()).isEqualTo("Main branch");
        assertThat(event.getRawValue()).isEqualTo("Main branch");
        ValidationContext<DirectoryBean> context = event.getValidationContext();
        assertThat(context).isNotNull();
        assertThat(context.getBean()).isSameAs(bean);
        assertThatValue(context.read("code"), "C-001");
        assertThat(event.readScopedValue("code")).isEqualTo("C-001");
    }

    @Test
    void valueChangeEventScopesRepeatableDynamicValues() {
        RenderedForm<DirectoryBean> rendered = renderForm();
        DirectoryBean bean = new DirectoryBean();
        bean.ensureMinimumEntries(2);
        rendered.initializeWithBean(bean);

        Map<FieldDefinition, FieldInstance> fields = rendered.getFields();
        FieldInstance codeField = findField(fields, "code");
        ((TextField) codeField.getValueComponent()).setValue("DYN-ROOT");

        Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatables = rendered.getRepeatableGroups();
        List<Map<FieldDefinition, FieldInstance>> entries = repeatables.get("directory-entry");
        Map<FieldDefinition, FieldInstance> firstEntry = entries.get(0);
        Map<FieldDefinition, FieldInstance> secondEntry = entries.get(1);

        FieldInstance firstRole = findField(firstEntry, "entries.profile.role");
        FieldInstance secondRole = findField(secondEntry, "entries.profile.role");
        FieldInstance secondSegment = findField(secondEntry, "entries.profile.segment");

        ((TextField) firstRole.getValueComponent()).setValue("Manager");
        ((TextField) secondRole.getValueComponent()).setValue("Analyst");

        List<FieldValueChangeEvent<DirectoryBean>> events = new ArrayList<>();
        rendered.addValueChangeListener(events::add);

        ((TextField) secondSegment.getValueComponent()).setValue("Enterprise");

        assertThat(events).hasSize(1);
        FieldValueChangeEvent<DirectoryBean> event = events.get(0);
        assertThat(event.getFieldInstance()).isSameAs(secondSegment);
        assertThat(event.getFieldDefinition().getPath()).isEqualTo("entries.profile.segment");
        ValidationContext<DirectoryBean> context = event.getValidationContext();
        assertThat(context.read(event.getFieldInstance(), "profile.role")).isEqualTo("Analyst");
        assertThat(event.readScopedValue("profile.role")).isEqualTo("Analyst");
        assertThat(context.read("entries.profile.role")).isInstanceOfAny(String.class, List.class);
        assertThatValue(context.read("code"), "DYN-ROOT");
    }

    private RenderedForm<DirectoryBean> renderForm() {
        RenderedForm<DirectoryBean> rendered = engine.render(TestDirectoryForm.class, new StubI18NProvider(), Locale.ENGLISH, false);
        rendered.getFields().forEach((definition, instance) -> {
            HasValue<?, ?> component = instance.getValueComponent();
            if (component instanceof TextField textField) {
                textField.setValue("");
            }
        });
        return rendered;
    }

    private FieldInstance findField(Map<FieldDefinition, FieldInstance> fields, String path) {
        return fields.entrySet().stream()
                .filter(entry -> path.equals(entry.getKey().getPath()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Field not found: " + path));
    }

    private void assertThatValue(Object actual, Object expected) {
        if (actual instanceof List<?> list) {
            assertThat(list).anySatisfy(value -> assertThat(value).isEqualTo(expected));
        } else {
            assertThat(actual).isEqualTo(expected);
        }
    }

    @UiForm(id = "directory-form",
            bean = DirectoryBean.class,
            titleKey = "forms.test.directory.title",
            sections = DirectorySection.class)
    public static class TestDirectoryForm {
    }

    @UiSection(id = "directory-section",
            titleKey = "forms.test.directory.section",
            groups = {DirectoryMetadataGroup.class, DirectoryEntryGroup.class})
    public static class DirectorySection {
    }

    @UiGroup(id = "directory-metadata",
            columns = 2)
    public static class DirectoryMetadataGroup {

        @UiField(path = "code",
                component = UiField.ComponentType.TEXT,
                labelKey = "forms.test.directory.code",
                order = 0)
        public void code() {
        }

        @UiField(path = "label",
                component = UiField.ComponentType.TEXT,
                labelKey = "forms.test.directory.label",
                order = 10)
        public void label() {
        }
    }

    @UiGroup(id = "directory-entry",
            repeatable = @UiRepeatable(enabled = true,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    min = 1,
                    max = 5),
            columns = 2)
    public static class DirectoryEntryGroup {

        @UiField(path = "entries.profile.segment",
                component = UiField.ComponentType.TEXT,
                labelKey = "forms.test.directory.segment",
                order = 0)
        public void segment() {
        }

        @UiField(path = "entries.profile.role",
                component = UiField.ComponentType.TEXT,
                labelKey = "forms.test.directory.role",
                order = 10)
        public void role() {
        }
    }

    public static final class DirectoryBean implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String code = "";
        private String label = "";
        private final List<DirectoryEntry> entries = new ArrayList<>();

        public DirectoryBean() {
            ensureMinimumEntries(1);
        }

        public List<DirectoryEntry> getEntries() {
            return entries;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void ensureMinimumEntries(int minimum) {
            int target = Math.max(1, minimum);
            while (entries.size() < target) {
                entries.add(new DirectoryEntry());
            }
        }
    }

    public static final class DirectoryEntry implements DynamicPropertyBag, Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private final Map<String, Object> groups = new LinkedHashMap<>();

        DirectoryEntry() {
            ensureGroup("profile");
        }

        private DynamicGroup ensureGroup(String name) {
            return (DynamicGroup) groups.computeIfAbsent(name, key -> new DynamicGroup());
        }

        @Override
        public Object readDynamicProperty(String name) {
            if (name == null || name.isBlank()) {
                return null;
            }
            return groups.computeIfAbsent(name, key -> new DynamicGroup());
        }

        @Override
        public boolean writeDynamicProperty(String name, Object value) {
            if (name == null || name.isBlank()) {
                return false;
            }
            groups.put(name, value);
            return true;
        }
    }

    public static final class DynamicGroup implements DynamicPropertyBag, Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private final Map<String, Object> values = new LinkedHashMap<>();

        @Override
        public Object readDynamicProperty(String name) {
            if (name == null || name.isBlank()) {
                return null;
            }
            return values.get(name);
        }

        @Override
        public boolean writeDynamicProperty(String name, Object value) {
            if (name == null || name.isBlank()) {
                return false;
            }
            values.put(name, value);
            return true;
        }
    }

    private static final class StubI18NProvider implements I18NProvider {

        private static final long serialVersionUID = 1L;

        @Override
        public List<Locale> getProvidedLocales() {
            return List.of(Locale.ENGLISH);
        }

        @Override
        public String getTranslation(String key, Locale locale, Object... params) {
            return key == null ? "" : key;
        }
    }
}
