package com.youtopin.vaadin.formengine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
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
    void propagatesRepeatableEntryValuesToBean() {
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

        Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatables = rendered.getRepeatableGroups();
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

    @Test
    void updatesRepeatableEntryTitlesUsingTemplate() {
        FormEngine engine = new FormEngine(new OptionCatalogRegistry());
        RenderedForm<RepeatableBean> rendered = engine.render(
                TestRepeatableForm.class,
                new StubI18NProvider(),
                Locale.ENGLISH,
                false);

        List<String> initialTitles = repeatableTitles(rendered);
        assertThat(initialTitles).containsExactly("Segment 1");

        Button addButton = findRepeatableAddButton(rendered, "test-repeatable-group");
        addButton.click();

        List<String> updatedTitles = repeatableTitles(rendered);
        assertThat(updatedTitles).containsExactly("Segment 1", "Segment 2");
    }

    @Test
    void initializeWithBeanPrefillsRepeatableValues() {
        FormEngine engine = new FormEngine(new OptionCatalogRegistry());
        RenderedForm<RepeatableBean> rendered = engine.render(
                TestRepeatableForm.class,
                new StubI18NProvider(),
                Locale.ENGLISH,
                false);

        rendered.getFields().forEach((definition, instance) ->
                rendered.getOrchestrator().bindField(instance, definition));

        RepeatableBean bean = new RepeatableBean();
        Segment segment = new Segment();
        segment.setTitle("Agenda");
        segment.setNotes("Discuss roadmap");
        bean.getPlan().getSegments().add(segment);

        rendered.initializeWithBean(bean);

        Map<String, List<Map<FieldDefinition, FieldInstance>>> repeatables = rendered.getRepeatableGroups();
        Map<FieldDefinition, FieldInstance> entry = repeatables.get("test-repeatable-group").get(0);
        entry.entrySet().stream()
                .filter(e -> "plan.segments.title".equals(e.getKey().getPath()))
                .findFirst()
                .ifPresent(e -> assertThat(((TextField) e.getValue().getValueComponent()).getValue()).isEqualTo("Agenda"));
        entry.entrySet().stream()
                .filter(e -> "plan.segments.notes".equals(e.getKey().getPath()))
                .findFirst()
                .ifPresent(e -> assertThat(((TextArea) e.getValue().getValueComponent()).getValue()).isEqualTo("Discuss roadmap"));
    }

    @Test
    void setRepeatableEntryCountRespectsManualControls() {
        FormEngine engine = new FormEngine(new OptionCatalogRegistry());
        RenderedForm<RepeatableBean> rendered = engine.render(
                ManualControlForm.class,
                new StubI18NProvider(),
                Locale.ENGLISH,
                false);

        Button addButton = findRepeatableAddButton(rendered, "manual-repeatable-group");
        assertThat(addButton.isEnabled()).isFalse();

        rendered.setRepeatableEntryCount("manual-repeatable-group", 3);
        assertThat(rendered.getRepeatableGroups().get("manual-repeatable-group")).hasSize(3);

        rendered.setRepeatableEntryCount("manual-repeatable-group", 1);
        assertThat(rendered.getRepeatableGroups().get("manual-repeatable-group")).hasSize(1);

        rendered.setRepeatableEntryCount("manual-repeatable-group", 10);
        assertThat(rendered.getRepeatableGroups().get("manual-repeatable-group")).hasSize(5);

        List<Button> removeButtons = flatten(rendered.getLayout())
                .filter(component -> component instanceof Button)
                .map(component -> (Button) component)
                .filter(button -> "true".equals(button.getElement().getAttribute("data-repeatable-remove")))
                .toList();

        assertThat(removeButtons).isNotEmpty();
        removeButtons.forEach(button -> assertThat(button.isEnabled()).isFalse());
    }

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

    private List<String> repeatableTitles(RenderedForm<?> rendered) {
        return flatten(rendered.getLayout())
                .filter(component -> "true".equals(component.getElement().getAttribute("data-repeatable-title")))
                .filter(component -> component instanceof HasText)
                .map(component -> ((HasText) component).getText())
                .collect(Collectors.toList());
    }

    private Button findRepeatableAddButton(RenderedForm<?> rendered, String groupId) {
        return flatten(rendered.getLayout())
                .filter(component -> component instanceof Button)
                .map(component -> (Button) component)
                .filter(button -> groupId.equals(button.getElement().getAttribute("data-repeatable-add")))
                .findFirst()
                .orElseThrow();
    }

    private Stream<Component> flatten(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren().flatMap(this::flatten));
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
                    uniqueBy = "title", itemTitleKey = "test.repeatable.itemTitle",
                    allowDuplicate = false))
    public static class TestRepeatableGroup {

        @UiField(path = "plan.segments.title", component = UiField.ComponentType.TEXT, labelKey = "segmentTitle")
        public void title() {
        }

        @UiField(path = "plan.segments.notes", component = UiField.ComponentType.TEXT_AREA, labelKey = "segmentNotes")
        public void notes() {
        }
    }

    @UiForm(
            id = "manual-repeatable",
            titleKey = "",
            descriptionKey = "",
            bean = RepeatableBean.class,
            sections = ManualControlSection.class,
            actions = @UiAction(id = "manual-submit", labelKey = "submit", placement = UiAction.Placement.FOOTER,
                    type = UiAction.ActionType.SUBMIT, order = 0)
    )
    public static class ManualControlForm {
    }

    @UiSection(id = "manual-section", titleKey = "", groups = ManualControlGroup.class, order = 0)
    public static class ManualControlSection {
    }

    @UiGroup(id = "manual-repeatable-group", columns = 1,
            repeatable = @UiRepeatable(enabled = true, min = 0, max = 5,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "test.repeatable.itemTitle", allowDuplicate = false,
                    allowManualAdd = false, allowManualRemove = false))
    public static class ManualControlGroup {

        @UiField(path = "plan.segments.title", component = UiField.ComponentType.TEXT, labelKey = "segmentTitle")
        public void title() {
        }

        @UiField(path = "plan.segments.notes", component = UiField.ComponentType.TEXT_AREA, labelKey = "segmentNotes")
        public void notes() {
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
