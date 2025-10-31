package com.youtopin.vaadin.samples.ui.formengine.definition.dynamic;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.annotation.UiValidation;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicFieldSchemaFormData;

/**
 * Wizard step that lets users design the dynamic fields used in the next step.
 */
@UiForm(
        id = "dynamic-schema-form",
        titleKey = "dynamicwizard.schema.title",
        descriptionKey = "dynamicwizard.schema.description",
        bean = DynamicFieldSchemaFormData.class,
        sections = {
                DynamicFieldSchemaFormDefinition.MetadataSection.class,
                DynamicFieldSchemaFormDefinition.BlueprintSection.class
        },
        actions = {
                @UiAction(id = "dynamic-schema-next", labelKey = "dynamicwizard.action.next",
                        placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SUBMIT,
                        order = 0)
        }
)
public final class DynamicFieldSchemaFormDefinition {

    private DynamicFieldSchemaFormDefinition() {
    }

    @UiSection(id = "dynamic-schema-metadata", titleKey = "dynamicwizard.schema.section.metadata",
            groups = MetadataGroup.class, order = 0)
    public static class MetadataSection {
    }

    @UiSection(id = "dynamic-schema-blueprints", titleKey = "dynamicwizard.schema.section.blueprints",
            groups = BlueprintGroup.class, order = 10)
    public static class BlueprintSection {
    }

    @UiGroup(id = "dynamic-schema-metadata-group", columns = 2)
    public static class MetadataGroup {

        @UiField(path = "collectionTitle", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.schema.field.collectionTitle",
                helperKey = "dynamicwizard.schema.field.collectionTitle.helper",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void collectionTitle() {
        }

        @UiField(path = "entryCount", component = UiField.ComponentType.INTEGER,
                labelKey = "dynamicwizard.schema.field.entryCount",
                helperKey = "dynamicwizard.schema.field.entryCount.helper",
                validations = {
                        @UiValidation(messageKey = "dynamicwizard.validation.entries.min",
                                expression = "value != null && value >= 1"),
                        @UiValidation(messageKey = "dynamicwizard.validation.entries.max",
                                expression = "value != null && value <= 20")
                })
        public void entryCount() {
        }

        @UiField(path = "summaryFieldKey", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.schema.field.summary",
                helperKey = "dynamicwizard.schema.field.summary.helper")
        public void summaryFieldKey() {
        }
    }

    @UiGroup(id = "dynamic-schema-blueprint-group", columns = 2,
            repeatable = @UiRepeatable(enabled = true,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    allowManualAdd = true,
                    allowManualRemove = true,
                    allowDuplicate = true,
                    itemTitleKey = "dynamicwizard.schema.repeatable.title",
                    summaryTemplate = "{label}"))
    public static class BlueprintGroup {

        @UiField(path = "fields.fieldKey", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.schema.field.fieldKey",
                helperKey = "dynamicwizard.schema.field.fieldKey.helper",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void fieldKey() {
        }

        @UiField(path = "fields.label", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.schema.field.label",
                helperKey = "dynamicwizard.schema.field.label.helper",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void label() {
        }

        @UiField(path = "fields.fieldType", component = UiField.ComponentType.ENUM,
                labelKey = "dynamicwizard.schema.field.type",
                helperKey = "dynamicwizard.schema.field.type.helper",
                options = @UiOptions(enabled = true,
                        type = UiOptions.ProviderType.ENUM,
                        enumType = "com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicFieldBlueprint$FieldType"))
        public void fieldType() {
        }

        @UiField(path = "fields.required", component = UiField.ComponentType.SWITCH,
                labelKey = "dynamicwizard.schema.field.required",
                helperKey = "dynamicwizard.schema.field.required.helper")
        public void required() {
        }

        @UiField(path = "fields.helper", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.schema.field.helper",
                helperKey = "dynamicwizard.schema.field.helper.helper")
        public void helper() {
        }

        @UiField(path = "fields.placeholder", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.schema.field.placeholder",
                helperKey = "dynamicwizard.schema.field.placeholder.helper")
        public void placeholder() {
        }
    }
}
