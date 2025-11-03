package com.youtopin.vaadin.formengine.definition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.youtopin.vaadin.formengine.RepeatableTitleGenerator;
import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSubform;

class FormDefinitionCloneTest {

    @Test
    void cloneProducesDeepCopyForNestedDefinitions() {
        FormDefinition original = createSampleForm();

        FormDefinition cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original.getId(), cloned.getId());
        assertEquals(original.getSections().size(), cloned.getSections().size());

        SectionDefinition originalSection = original.getSections().get(0);
        SectionDefinition clonedSection = cloned.getSections().get(0);
        assertNotSame(originalSection, clonedSection);
        assertEquals(originalSection.getId(), clonedSection.getId());

        GroupDefinition originalGroup = originalSection.getGroups().get(0);
        GroupDefinition clonedGroup = clonedSection.getGroups().get(0);
        assertNotSame(originalGroup, clonedGroup);
        assertEquals(originalGroup.getId(), clonedGroup.getId());

        FieldDefinition originalField = originalGroup.getFields().get(0);
        FieldDefinition clonedField = clonedGroup.getFields().get(0);
        assertNotSame(originalField, clonedField);
        assertEquals(originalField.getPath(), clonedField.getPath());

        GroupDefinition originalNestedGroup = originalGroup.getEntryGroups().get(0);
        GroupDefinition clonedNestedGroup = clonedGroup.getEntryGroups().get(0);
        assertNotSame(originalNestedGroup, clonedNestedGroup);
        assertEquals(originalNestedGroup.getId(), clonedNestedGroup.getId());

        assertNotSame(originalGroup.getRepeatableDefinition(), clonedGroup.getRepeatableDefinition());
        assertNotSame(originalGroup.getSubformDefinition(), clonedGroup.getSubformDefinition());
    }

    @Test
    void mutatingCloneDoesNotImpactOriginalGraph() {
        FormDefinition original = createSampleForm();
        FormDefinition cloned = original.clone();

        SectionDefinition clonedSection = cloned.getSections().get(0);
        SectionDefinition originalSection = original.getSections().get(0);
        clonedSection.setTitleKey("changed.section.title");
        assertEquals("section.title", originalSection.getTitleKey());
        assertEquals("changed.section.title", clonedSection.getTitleKey());

        GroupDefinition clonedGroup = clonedSection.getGroups().get(0);
        GroupDefinition originalGroup = originalSection.getGroups().get(0);
        clonedGroup.setColumns(4);
        assertEquals(2, originalGroup.getColumns());
        assertEquals(4, clonedGroup.getColumns());

        RepeatableDefinition clonedRepeatable = clonedGroup.getRepeatableDefinition();
        RepeatableDefinition originalRepeatable = originalGroup.getRepeatableDefinition();
        clonedRepeatable.setMax(42);
        assertEquals(5, originalRepeatable.getMax());
        assertEquals(42, clonedRepeatable.getMax());

        SubformDefinition clonedSubform = clonedGroup.getSubformDefinition();
        SubformDefinition originalSubform = originalGroup.getSubformDefinition();
        clonedSubform.setFormId("alternate-subform");
        assertEquals("child-form", originalSubform.getFormId());
        assertEquals("alternate-subform", clonedSubform.getFormId());

        List<GroupDefinition> updatedEntryGroups = new ArrayList<>(clonedGroup.getEntryGroups());
        updatedEntryGroups.add(createLeafGroup("extra-entry"));
        clonedGroup.setEntryGroups(updatedEntryGroups);
        assertEquals(1, originalGroup.getEntryGroups().size());
        assertEquals(2, clonedGroup.getEntryGroups().size());

        FieldDefinition clonedField = clonedGroup.getFields().get(0);
        FieldDefinition originalField = originalGroup.getFields().get(0);
        clonedField.setLabelKey("changed.field.label");
        assertEquals("employee.code.label", originalField.getLabelKey());
        assertEquals("changed.field.label", clonedField.getLabelKey());

        List<ValidationDefinition> updatedValidations = new ArrayList<>(clonedField.getValidations());
        updatedValidations.add(new ValidationDefinition("other.message", "", List.of(), ""));
        clonedField.setValidations(updatedValidations);
        assertEquals(1, originalField.getValidations().size());
        assertEquals(2, clonedField.getValidations().size());

        List<CrossFieldValidationDefinition> updatedCrossField = new ArrayList<>(clonedField.getCrossFieldValidations());
        updatedCrossField.add(new CrossFieldValidationDefinition("true", "cross.other", List.of(), List.of("other")));
        clonedField.setCrossFieldValidations(updatedCrossField);
        assertEquals(1, originalField.getCrossFieldValidations().size());
        assertEquals(2, clonedField.getCrossFieldValidations().size());

        List<SectionDefinition> updatedSections = new ArrayList<>(cloned.getSections());
        updatedSections.add(createSection("extra-section", List.of(createLeafGroup("leaf"))));
        cloned.setSections(updatedSections);
        assertEquals(1, original.getSections().size());
        assertEquals(2, cloned.getSections().size());

        List<ActionDefinition> newActions = new ArrayList<>(cloned.getActions());
        newActions.add(createAction("approve"));
        cloned.setActions(newActions);
        newActions.clear();
        assertEquals(1, original.getActions().size());
        assertEquals(2, cloned.getActions().size());

        Map<String, LifecycleHookDefinition> hooks = new LinkedHashMap<>();
        hooks.put("afterSave", new LifecycleHookDefinition("afterSave", "hookBean", "after"));
        cloned.setLifecycleHooks(hooks);
        hooks.clear();
        assertEquals(1, original.getLifecycleHooks().size());
        assertEquals(1, cloned.getLifecycleHooks().size());

        clonedGroup.setEntryGroups(null);
        assertTrue(clonedGroup.getEntryGroups().isEmpty());
    }

    private static FormDefinition createSampleForm() {
        GroupDefinition nestedGroup = createLeafGroup("nested-group");
        GroupDefinition parentGroup = new GroupDefinition(
                "group",
                "group.title",
                2,
                createRepeatableDefinition(),
                createSubformDefinition(),
                "",
                List.of(createField("employee.code")),
                List.of(nestedGroup));

        SectionDefinition section = createSection("section", List.of(parentGroup));

        return new FormDefinition(
                "form",
                "form.title",
                "form.description",
                SampleBean.class,
                List.of(section),
                List.of(createAction("submit")),
                Map.of("beforeSave", new LifecycleHookDefinition("beforeSave", "hookBean", "before")));
    }

    private static GroupDefinition createLeafGroup(String id) {
        return new GroupDefinition(
                id,
                id + ".title",
                1,
                null,
                null,
                "",
                List.of(createField(id + ".field")),
                List.of());
    }

    private static SectionDefinition createSection(String id, List<GroupDefinition> groups) {
        return new SectionDefinition(
                id,
                "section.title",
                "section.description",
                "",
                "",
                "",
                0,
                groups);
    }

    private static RepeatableDefinition createRepeatableDefinition() {
        return new RepeatableDefinition(
                true,
                UiRepeatable.RepeatableMode.CARD_DIALOG,
                1,
                5,
                "code",
                "summary {0}",
                "item.title",
                1,
                new RepeatableTitleGenerator.Default(),
                true,
                true,
                true,
                true);
    }

    private static SubformDefinition createSubformDefinition() {
        return new SubformDefinition(true, "child-form", UiSubform.SubformMode.DIALOG, true);
    }

    private static FieldDefinition createField(String path) {
        return new FieldDefinition(
                path,
                UiField.ComponentType.TEXT,
                path + ".label",
                path + ".helper",
                path + ".placeholder",
                "",
                "",
                "",
                "",
                "",
                "",
                new OptionsDefinition(true, UiOptions.ProviderType.STATIC, List.of("A|Alpha"), "", "", "", "", false, "", false),
                List.of(new ValidationDefinition("validation.message", "", List.of(), "")),
                List.of(new CrossFieldValidationDefinition("a == b", "cross.message", List.of(), List.of("other"))),
                new SecurityDefinition("guard", "", List.of(), false),
                0,
                1,
                1);
    }

    private static ActionDefinition createAction(String id) {
        return new ActionDefinition(
                id,
                id + ".label",
                id + ".description",
                "",
                "",
                UiAction.Placement.FOOTER,
                "",
                UiAction.ActionType.SUBMIT,
                0,
                new SecurityDefinition("guard", "", List.of(), false));
    }

    private static final class SampleBean {
    }
}
