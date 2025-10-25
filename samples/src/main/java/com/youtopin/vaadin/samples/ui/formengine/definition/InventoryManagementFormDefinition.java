package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.application.formengine.model.InventoryManagementFormData;

/**
 * Form definition showcasing repository-backed repeatable entries and prefilled data.
 */
@UiForm(
        id = "inventory-management",
        titleKey = "forms.inventory.title",
        descriptionKey = "forms.inventory.description",
        bean = InventoryManagementFormData.class,
        sections = {
                InventoryManagementFormDefinition.SummarySection.class,
                InventoryManagementFormDefinition.ItemsSection.class
        },
        actions = {
                @UiAction(id = "inventory-save",
                        labelKey = "forms.inventory.action.save",
                        placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SUBMIT,
                        order = 0)
        }
)
public final class InventoryManagementFormDefinition {

    private InventoryManagementFormDefinition() {
    }

    @UiSection(id = "inventory-summary", titleKey = "forms.inventory.section.summary",
            groups = SummaryGroup.class, order = 0)
    public static class SummarySection {
    }

    @UiSection(id = "inventory-items", titleKey = "forms.inventory.section.items",
            groups = ItemsGroup.class, order = 1)
    public static class ItemsSection {
    }

    @UiGroup(id = "inventory-summary-group", columns = 2)
    public static class SummaryGroup {

        @UiField(path = "inventory.code", component = UiField.ComponentType.TEXT,
                labelKey = "forms.inventory.field.code", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required")
        public void code() {
        }

        @UiField(path = "inventory.name", component = UiField.ComponentType.TEXT,
                labelKey = "forms.inventory.field.name", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required")
        public void name() {
        }

        @UiField(path = "inventory.owner", component = UiField.ComponentType.TEXT,
                labelKey = "forms.inventory.field.owner")
        public void owner() {
        }

        @UiField(path = "inventory.notes", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.inventory.field.notes", helperKey = "forms.inventory.field.notes.helper",
                colSpan = 2)
        public void notes() {
        }
    }

    @UiGroup(id = "inventory-items-group", columns = 2,
            repeatable = @UiRepeatable(enabled = true, min = 1, max = 6,
                    mode = UiRepeatable.RepeatableMode.CARD_DIALOG,
                    itemTitleKey = "forms.inventory.itemTitle", uniqueBy = "sku",
                    allowDuplicate = false))
    public static class ItemsGroup {

        @UiField(path = "inventory.items.sku", component = UiField.ComponentType.TEXT,
                labelKey = "forms.inventory.field.itemSku", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required")
        public void sku() {
        }

        @UiField(path = "inventory.items.description", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.inventory.field.itemDescription", colSpan = 2)
        public void description() {
        }

        @UiField(path = "inventory.items.quantity", component = UiField.ComponentType.INTEGER,
                labelKey = "forms.inventory.field.itemQuantity", helperKey = "forms.inventory.field.itemQuantity.helper",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void quantity() {
        }

        @UiField(path = "inventory.items.location", component = UiField.ComponentType.TEXT,
                labelKey = "forms.inventory.field.itemLocation")
        public void location() {
        }
    }
}
