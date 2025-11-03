package com.youtopin.vaadin.samples.ui.formengine.definition.dynamic;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.definition.ActionDefinition;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.FormDefinition;
import com.youtopin.vaadin.formengine.definition.GroupDefinition;
import com.youtopin.vaadin.formengine.definition.RepeatableDefinition;
import com.youtopin.vaadin.formengine.definition.SectionDefinition;
import com.youtopin.vaadin.formengine.definition.ValidationDefinition;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicEntryCollectionFormData;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicFieldBlueprint;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicFieldSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds a {@link FormDefinition} for the entry step based on a runtime schema.
 */
public final class DynamicEntryFormFactory {

    public static final String GROUP_ID = "dynamic-entry-group";

    private DynamicEntryFormFactory() {
    }

    public static FormDefinition create(DynamicFieldSchema schema) {
        List<SectionDefinition> sections = new ArrayList<>();

        sections.add(new SectionDefinition(
                "dynamic-entry-schema",
                "dynamicwizard.entries.section.schema",
                "",
                "",
                "",
                "",
                "",
                0,
                List.of(createSchemaSummaryGroup())));

        sections.add(new SectionDefinition(
                "dynamic-entry-editor",
                "dynamicwizard.entries.section.entries",
                "",
                "",
                "",
                "",
                "",
                10,
                List.of(createEntryGroup(schema))));

        List<ActionDefinition> actions = List.of(
                new ActionDefinition("dynamic-entries-back", "dynamicwizard.action.back", "", "", "",
                        UiAction.Placement.FOOTER, "", UiAction.ActionType.SECONDARY, 0, null),
                new ActionDefinition("dynamic-entries-finish", "dynamicwizard.action.finish", "", "", "",
                        UiAction.Placement.FOOTER, "", UiAction.ActionType.SUBMIT, 1, null)
        );

        return new FormDefinition(
                "dynamic-entry-form",
                "dynamicwizard.entries.title",
                "dynamicwizard.entries.description",
                DynamicEntryCollectionFormData.class,
                sections,
                actions,
                Map.of()
        );
    }

    private static GroupDefinition createSchemaSummaryGroup() {
        List<FieldDefinition> fields = List.of(
                createReadOnlyField("schema.collectionTitle", UiField.ComponentType.TEXT,
                        "dynamicwizard.entries.field.collectionTitle"),
                createReadOnlyField("schema.entryCount", UiField.ComponentType.INTEGER,
                        "dynamicwizard.entries.field.entryCount"),
                createReadOnlyField("schema.summaryFieldKey", UiField.ComponentType.TEXT,
                        "dynamicwizard.entries.field.summaryField"));
        return new GroupDefinition("dynamic-entry-schema-group", "", "", 2,
                null, null, "true", fields, List.of());
    }

    private static FieldDefinition createReadOnlyField(String path,
                                                       UiField.ComponentType type,
                                                       String labelKey) {
        return new FieldDefinition(path, type, labelKey, "", "", "", "",
                "", "", "true", "", null, List.of(), List.of(), null, 0, 1, 1);
    }

    private static GroupDefinition createEntryGroup(DynamicFieldSchema schema) {
        List<FieldDefinition> fields = new ArrayList<>();
        int order = 0;
        Map<String, DynamicFieldBlueprint> byKey = schema.indexByKey();
        for (DynamicFieldBlueprint blueprint : byKey.values()) {
            fields.add(createDynamicField(blueprint, order++));
        }

        RepeatableDefinition repeatable = new RepeatableDefinition(true,
                UiRepeatable.RepeatableMode.INLINE_PANEL,
                1,
                50,
                "",
                summaryTemplate(schema),
                "dynamicwizard.entries.repeatable.title",
                1,
                null,
                false,
                false,
                false,
                false);

        return new GroupDefinition(GROUP_ID, "", "", 2, repeatable, null, "",
                fields, List.of());
    }

    private static FieldDefinition createDynamicField(DynamicFieldBlueprint blueprint, int order) {
        UiField.ComponentType type = switch (blueprint.getFieldType()) {
            case TEXT -> UiField.ComponentType.TEXT;
            case TEXT_AREA -> UiField.ComponentType.TEXT_AREA;
            case INTEGER -> UiField.ComponentType.INTEGER;
            case NUMBER -> UiField.ComponentType.NUMBER;
            case EMAIL -> UiField.ComponentType.EMAIL;
            case DATE -> UiField.ComponentType.DATE;
        };
        ValidationDefinition requiredValidation = blueprint.isRequired()
                ? new ValidationDefinition("forms.validation.required",
                "value != null && !value.toString().isBlank()",
                List.of(), "")
                : null;
        List<ValidationDefinition> validations = requiredValidation == null ? List.of() : List.of(requiredValidation);
        return new FieldDefinition(
                "entries.values." + blueprint.getFieldKey(),
                type,
                safeLabel(blueprint.getLabel()),
                safeLabel(blueprint.getHelper()),
                safeLabel(blueprint.getPlaceholder()),
                blueprint.isRequired() ? "true" : "",
                blueprint.isRequired() ? "forms.validation.required" : "",
                "",
                "",
                "",
                "",
                null,
                validations,
                List.of(),
                null,
                order,
                type == UiField.ComponentType.TEXT_AREA ? 2 : 1,
                type == UiField.ComponentType.TEXT_AREA ? 2 : 1
        );
    }

    private static String safeLabel(String value) {
        return value == null || value.isBlank() ? "" : value;
    }

    private static String summaryTemplate(DynamicFieldSchema schema) {
        String key = schema.getSummaryFieldKey();
        if (key == null || key.isBlank()) {
            return "";
        }
        return "{values." + key + "}";
    }
}
