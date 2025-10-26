package com.youtopin.vaadin.formengine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.textfield.TextArea;
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

class FormEngineRepeatableReadOnlyTest {

    @Test
    void appliesEntrySpecificReadOnlyExpressions() {
        FormEngine engine = new FormEngine(new OptionCatalogRegistry());
        RenderedForm<RepeatableBean> rendered = engine.render(
                ReadOnlyRepeatableForm.class,
                new StubI18NProvider(),
                Locale.ENGLISH,
                false);

        rendered.getFields().forEach((definition, instance) ->
                rendered.getOrchestrator().bindField(instance, definition));

        RepeatableBean bean = new RepeatableBean();
        Segment locked = new Segment();
        locked.setTitle("Locked");
        locked.setNotes("should lock");
        Segment editable = new Segment();
        editable.setTitle("Editable");
        editable.setNotes("should edit");
        bean.getPlan().getSegments().add(locked);
        bean.getPlan().getSegments().add(editable);

        rendered.initializeWithBean(bean);

        Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatables = rendered.getRepeatableGroups();
        TextArea firstNotes = (TextArea) findEntryField(repeatables, "read-only-repeatable-group", 0,
                "plan.segments.notes").getValueComponent();
        TextArea secondNotes = (TextArea) findEntryField(repeatables, "read-only-repeatable-group", 1,
                "plan.segments.notes").getValueComponent();

        assertThat(firstNotes.isReadOnly()).isTrue();
        assertThat(secondNotes.isReadOnly()).isFalse();

        bean.getPlan().getSegments().get(0).setTitle("Editable");
        rendered.initializeWithBean(bean);

        assertThat(firstNotes.isReadOnly()).isFalse();
        assertThat(secondNotes.isReadOnly()).isFalse();
    }

    @Test
    void readOnlyOverrideTargetsSpecificRepeatableEntry() {
        FormEngine engine = new FormEngine(new OptionCatalogRegistry());
        RenderedForm<RepeatableBean> rendered = engine.render(
                OverrideRepeatableForm.class,
                new StubI18NProvider(),
                Locale.ENGLISH,
                false);

        rendered.getFields().forEach((definition, instance) ->
                rendered.getOrchestrator().bindField(instance, definition));

        RepeatableBean bean = new RepeatableBean();
        Segment first = new Segment();
        first.setTitle("Locked");
        first.setNotes("first");
        Segment second = new Segment();
        second.setTitle("Editable");
        second.setNotes("second");
        bean.getPlan().getSegments().add(first);
        bean.getPlan().getSegments().add(second);

        rendered.initializeWithBean(bean);

        Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatables = rendered.getRepeatableGroups();
        TextArea firstNotes = (TextArea) findEntryField(repeatables, "override-repeatable-group", 0,
                "plan.segments.notes").getValueComponent();
        TextArea secondNotes = (TextArea) findEntryField(repeatables, "override-repeatable-group", 1,
                "plan.segments.notes").getValueComponent();

        assertThat(firstNotes.isReadOnly()).isFalse();
        assertThat(secondNotes.isReadOnly()).isFalse();

        RenderedForm.ReadOnlyOverride<RepeatableBean> repeatableOverride = (definition, context) ->
                "plan.segments.notes".equals(definition.getPath())
                        && context != null
                        && context.getRepeatableEntry()
                        .map(entry -> entry.getIndex() == 1
                                && "Editable".equals(entry.getValues().get("title")))
                        .orElse(false);
        rendered.addReadOnlyOverride(repeatableOverride);

        assertThat(firstNotes.isReadOnly()).isFalse();
        assertThat(secondNotes.isReadOnly()).isTrue();
    }

    private FieldInstance findEntryField(Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatables,
                                         String groupId,
                                         int index,
                                         String path) {
        return repeatables.getOrDefault(groupId, List.of())
                .get(index)
                .entrySet()
                .stream()
                .filter(entry -> path.equals(entry.getKey().getPath()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
    }

    private static final class StubI18NProvider implements I18NProvider {
        @Override
        public List<Locale> getProvidedLocales() {
            return List.of(Locale.ENGLISH);
        }

        @Override
        public String getTranslation(String key, Locale locale, Object... params) {
            if ("test.repeatable.itemTitle".equals(key)) {
                return "Segment {0}";
            }
            return key == null ? "" : key;
        }
    }

    @UiForm(
            id = "read-only-repeatable",
            titleKey = "",
            descriptionKey = "",
            bean = RepeatableBean.class,
            sections = ReadOnlySection.class,
            actions = @UiAction(id = "read-only-submit", labelKey = "submit", placement = UiAction.Placement.FOOTER,
                    type = UiAction.ActionType.SUBMIT, order = 0)
    )
    public static class ReadOnlyRepeatableForm {
    }

    @UiSection(id = "read-only-section", titleKey = "", groups = ReadOnlyRepeatableGroup.class, order = 0)
    public static class ReadOnlySection {
    }

    @UiGroup(id = "read-only-repeatable-group", columns = 1,
            repeatable = @UiRepeatable(enabled = true, min = 2, max = 4,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "test.repeatable.itemTitle", allowDuplicate = false))
    public static class ReadOnlyRepeatableGroup {

        @UiField(path = "plan.segments.title", component = UiField.ComponentType.TEXT, labelKey = "segmentTitle")
        public void title() {
        }

        @UiField(path = "plan.segments.notes", component = UiField.ComponentType.TEXT_AREA, labelKey = "segmentNotes",
                readOnlyWhen = "plan.segments.title == 'Locked'")
        public void notes() {
        }
    }

    @UiForm(
            id = "override-repeatable",
            titleKey = "",
            descriptionKey = "",
            bean = RepeatableBean.class,
            sections = OverrideSection.class,
            actions = @UiAction(id = "override-submit", labelKey = "submit", placement = UiAction.Placement.FOOTER,
                    type = UiAction.ActionType.SUBMIT, order = 0)
    )
    public static class OverrideRepeatableForm {
    }

    @UiSection(id = "override-section", titleKey = "", groups = OverrideRepeatableGroup.class, order = 0)
    public static class OverrideSection {
    }

    @UiGroup(id = "override-repeatable-group", columns = 1,
            repeatable = @UiRepeatable(enabled = true, min = 2, max = 4,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "test.repeatable.itemTitle", allowDuplicate = false))
    public static class OverrideRepeatableGroup {

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
