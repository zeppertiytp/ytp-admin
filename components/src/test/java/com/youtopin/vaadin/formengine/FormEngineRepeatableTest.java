package com.youtopin.vaadin.formengine;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.I18NProvider;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;
import com.youtopin.vaadin.formengine.registry.FieldInstance;

class FormEngineRepeatableTest {

    @Test
    void propagatesRepeatableEntryValuesToBean() throws Exception {
        FormEngine engine = new FormEngine(new OptionCatalogRegistry());
        RenderedForm<RepeatableBean> rendered = engine.render(
                TestRepeatableForm.class,
                new StubI18NProvider(),
                Locale.ENGLISH,
                false);

        RepeatableBean bean = new RepeatableBean();
        rendered.setActionBeanSupplier(() -> bean);
        rendered.getFields().forEach((definition, instance) ->
                rendered.getOrchestrator().bindField(instance, definition));

        Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatables = repeatableGroups(rendered);
        Map<FieldDefinition, FieldInstance> entry = repeatables.get("test-repeatable-group").get(0);
        entry.entrySet().stream()
                .filter(e -> "plan.segments.title".equals(e.getKey().getPath()))
                .findFirst()
                .ifPresent(e -> ((TextField) e.getValue().getValueComponent()).setValue("Kick-off"));
        entry.entrySet().stream()
                .filter(e -> "plan.segments.notes".equals(e.getKey().getPath()))
                .findFirst()
                .ifPresent(e -> ((TextArea) e.getValue().getValueComponent()).setValue("Introductions"));

        AtomicReference<FormEngine.ActionExecutionContext<RepeatableBean>> contextRef = new AtomicReference<>();
        rendered.addActionHandler("test-submit", contextRef::set);
        Button submit = rendered.getActionButtons().get("test-submit");
        submit.click();

        assertThat(contextRef.get()).isNotNull();
        assertThat(bean.getPlan().getSegments())
                .hasSize(1)
                .first()
                .satisfies(segment -> {
                    assertThat(segment.getTitle()).isEqualTo("Kick-off");
                    assertThat(segment.getNotes()).isEqualTo("Introductions");
                });
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatableGroups(RenderedForm<?> rendered)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = rendered.getClass().getDeclaredField("repeatableGroups");
        field.setAccessible(true);
        return (Map<String, List<Map<FieldDefinition, FieldInstance>>>) field.get(rendered);
    }

    private static final class StubI18NProvider implements I18NProvider {
        @Override
        public List<Locale> getProvidedLocales() {
            return List.of(Locale.ENGLISH);
        }

        @Override
        public String getTranslation(String key, Locale locale, Object... params) {
            return key == null ? "" : key;
        }
    }

    @UiForm(
            id = "test-repeatable",
            titleKey = "",
            descriptionKey = "",
            bean = RepeatableBean.class,
            sections = TestSection.class,
            actions = @UiAction(id = "test-submit", labelKey = "submit", placement = UiAction.Placement.FOOTER,
                    type = UiAction.ActionType.SUBMIT, order = 0)
    )
    public static class TestRepeatableForm {
    }

    @UiSection(id = "test-section", titleKey = "", groups = TestRepeatableGroup.class, order = 0)
    public static class TestSection {
    }

    @UiGroup(id = "test-repeatable-group", columns = 1,
            repeatable = @UiRepeatable(enabled = true, min = 1, max = 4,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    uniqueBy = "title", allowDuplicate = false))
    public static class TestRepeatableGroup {

        @UiField(path = "plan.segments.title", component = UiField.ComponentType.TEXT, labelKey = "segmentTitle")
        public void title() {
        }

        @UiField(path = "plan.segments.notes", component = UiField.ComponentType.TEXT_AREA, labelKey = "segmentNotes")
        public void notes() {
        }
    }

    public static class RepeatableBean {
        private final Plan plan = new Plan();

        public Plan getPlan() {
            return plan;
        }
    }

    public static class Plan {
        private final List<Segment> segments = new ArrayList<>();

        public List<Segment> getSegments() {
            return segments;
        }

        public void setSegments(List<Segment> segments) {
            this.segments.clear();
            if (segments != null) {
                segments.stream()
                        .filter(Objects::nonNull)
                        .map(Segment::copyOf)
                        .forEach(this.segments::add);
            }
        }
    }

    public static class Segment {
        private String title = "";
        private String notes = "";

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = Objects.toString(title, "");
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = Objects.toString(notes, "");
        }

        private static Segment copyOf(Segment source) {
            Segment copy = new Segment();
            copy.setTitle(source == null ? "" : source.getTitle());
            copy.setNotes(source == null ? "" : source.getNotes());
            return copy;
        }
    }
}
