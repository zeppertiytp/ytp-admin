package com.youtopin.vaadin.samples.ui.formengine.definition.dynamic;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.annotation.UiValidation;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicEntryCollectionFormData;

/**
 * Second wizard step: renders the repeatable group driven by the schema configured earlier.
 */
@UiForm(
        id = "dynamic-entry-form",
        titleKey = "dynamicwizard.entries.title",
        descriptionKey = "dynamicwizard.entries.description",
        bean = DynamicEntryCollectionFormData.class,
        sections = {
                DynamicEntryCollectionFormDefinition.SchemaSummarySection.class,
                DynamicEntryCollectionFormDefinition.EntryEditorSection.class
        },
        actions = {
                @UiAction(id = "dynamic-entries-back", labelKey = "dynamicwizard.action.back",
                        placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SECONDARY,
                        order = 0),
                @UiAction(id = "dynamic-entries-finish", labelKey = "dynamicwizard.action.finish",
                        placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SUBMIT,
                        order = 1)
        }
)
public final class DynamicEntryCollectionFormDefinition {

    private DynamicEntryCollectionFormDefinition() {
    }

    @UiSection(id = "dynamic-entry-schema", titleKey = "dynamicwizard.entries.section.schema",
            groups = SchemaSummaryGroup.class, order = 0)
    public static class SchemaSummarySection {
    }

    @UiSection(id = "dynamic-entry-editor", titleKey = "dynamicwizard.entries.section.entries",
            groups = EntryEditorGroup.class, order = 10)
    public static class EntryEditorSection {
    }

    @UiGroup(id = "dynamic-entry-schema-group", columns = 2)
    public static class SchemaSummaryGroup {

        @UiField(path = "schema.collectionTitle", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.entries.field.collectionTitle",
                readOnlyWhen = "true")
        public void collectionTitle() {
        }

        @UiField(path = "schema.requireOwner", component = UiField.ComponentType.SWITCH,
                labelKey = "dynamicwizard.entries.field.requireOwner",
                readOnlyWhen = "true")
        public void requireOwner() {
        }

        @UiField(path = "schema.includeEmail", component = UiField.ComponentType.SWITCH,
                labelKey = "dynamicwizard.entries.field.includeEmail",
                readOnlyWhen = "true")
        public void includeEmail() {
        }

        @UiField(path = "schema.includePhone", component = UiField.ComponentType.SWITCH,
                labelKey = "dynamicwizard.entries.field.includePhone",
                readOnlyWhen = "true")
        public void includePhone() {
        }

        @UiField(path = "schema.includeNotes", component = UiField.ComponentType.SWITCH,
                labelKey = "dynamicwizard.entries.field.includeNotes",
                readOnlyWhen = "true")
        public void includeNotes() {
        }
    }

    @UiGroup(id = "dynamic-entry-group", columns = 2,
            repeatable = @UiRepeatable(enabled = true,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "dynamicwizard.entries.repeatable.title",
                    allowManualAdd = false,
                    allowManualRemove = false,
                    summaryTemplate = "{label}"))
    public static class EntryEditorGroup {

        @UiField(path = "entries.label", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.entries.field.label",
                helperKey = "dynamicwizard.entries.field.label.helper",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void label() {
        }

        @UiField(path = "entries.owner", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.entries.field.owner",
                visibleWhen = "{\"field\":\"schema.requireOwner\",\"value\":true}")
        public void owner() {
        }

        @UiField(path = "entries.email", component = UiField.ComponentType.EMAIL,
                labelKey = "dynamicwizard.entries.field.email",
                visibleWhen = "{\"field\":\"schema.includeEmail\",\"value\":true}")
        public void email() {
        }

        @UiField(path = "entries.phone", component = UiField.ComponentType.TEXT,
                labelKey = "dynamicwizard.entries.field.phone",
                helperKey = "dynamicwizard.entries.field.phone.helper",
                visibleWhen = "{\"field\":\"schema.includePhone\",\"value\":true}")
        public void phone() {
        }

        @UiField(path = "entries.notes", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "dynamicwizard.entries.field.notes",
                helperKey = "dynamicwizard.entries.field.notes.helper",
                visibleWhen = "{\"field\":\"schema.includeNotes\",\"value\":true}")
        public void notes() {
        }
    }
}
