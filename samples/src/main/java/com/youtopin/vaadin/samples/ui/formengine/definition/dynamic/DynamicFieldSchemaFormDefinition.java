package com.youtopin.vaadin.samples.ui.formengine.definition.dynamic;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.annotation.UiValidation;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicFieldSchemaFormData;

/**
 * First wizard step: lets users decide which attributes appear in the repeatable group.
 */
@UiForm(
        id = "dynamic-schema-form",
        titleKey = "dynamicwizard.schema.title",
        descriptionKey = "dynamicwizard.schema.description",
        bean = DynamicFieldSchemaFormData.class,
        sections = {
                DynamicFieldSchemaFormDefinition.MetadataSection.class,
                DynamicFieldSchemaFormDefinition.AttributesSection.class,
                DynamicFieldSchemaFormDefinition.EntrySection.class
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

    @UiSection(id = "dynamic-schema-attributes", titleKey = "dynamicwizard.schema.section.attributes",
            groups = AttributeGroup.class, order = 10)
    public static class AttributesSection {
    }

    @UiSection(id = "dynamic-schema-entries", titleKey = "dynamicwizard.schema.section.entries",
            groups = EntryGroup.class, order = 20)
    public static class EntrySection {
    }

    @UiGroup(id = "dynamic-schema-metadata-group", columns = 2)
    public static class MetadataGroup {

        @UiField(path = "collectionTitle", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.schema.field.collectionTitle",
                helperKey = "dynamicwizard.schema.field.collectionTitle.helper",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void collectionTitle() {
        }
    }

    @UiGroup(id = "dynamic-schema-attribute-group", columns = 2)
    public static class AttributeGroup {

        @UiField(path = "requireOwner", component = UiField.ComponentType.SWITCH,
                labelKey = "dynamicwizard.schema.field.requireOwner",
                helperKey = "dynamicwizard.schema.field.requireOwner.helper")
        public void requireOwner() {
        }

        @UiField(path = "includeEmail", component = UiField.ComponentType.SWITCH,
                labelKey = "dynamicwizard.schema.field.includeEmail",
                helperKey = "dynamicwizard.schema.field.includeEmail.helper")
        public void includeEmail() {
        }

        @UiField(path = "includePhone", component = UiField.ComponentType.SWITCH,
                labelKey = "dynamicwizard.schema.field.includePhone",
                helperKey = "dynamicwizard.schema.field.includePhone.helper")
        public void includePhone() {
        }

        @UiField(path = "includeNotes", component = UiField.ComponentType.SWITCH,
                labelKey = "dynamicwizard.schema.field.includeNotes",
                helperKey = "dynamicwizard.schema.field.includeNotes.helper")
        public void includeNotes() {
        }
    }

    @UiGroup(id = "dynamic-schema-entry-group", columns = 2)
    public static class EntryGroup {

        @UiField(path = "entryCount", component = UiField.ComponentType.INTEGER,
                labelKey = "dynamicwizard.schema.field.entryCount",
                helperKey = "dynamicwizard.schema.field.entryCount.helper",
                validations = {
                        @UiValidation(messageKey = "dynamicwizard.validation.entries.min",
                                expression = "value != null && value >= 1"),
                        @UiValidation(messageKey = "dynamicwizard.validation.entries.max",
                                expression = "value != null && value <= 10")
                })
        public void entryCount() {
        }
    }
}
