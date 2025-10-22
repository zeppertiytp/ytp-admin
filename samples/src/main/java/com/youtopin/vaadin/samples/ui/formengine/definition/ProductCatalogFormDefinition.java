package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.ui.formengine.model.ProductCatalogFormData;

/**
 * Annotation-driven definition for the product catalog editor sample.
 */
@UiForm(
        id = "product-catalog",
        titleKey = "forms.catalog.title",
        descriptionKey = "forms.catalog.description",
        bean = ProductCatalogFormData.class,
        sections = {
                ProductCatalogFormDefinition.ProductSection.class,
                ProductCatalogFormDefinition.PricingSection.class,
                ProductCatalogFormDefinition.AvailabilitySection.class
        },
        actions = {
                @UiAction(id = "catalog-preview", labelKey = "forms.catalog.action.preview", placement = UiAction.Placement.HEADER,
                        type = UiAction.ActionType.SECONDARY),
                @UiAction(id = "catalog-save", labelKey = "forms.catalog.action.save", placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SUBMIT, order = 1)
        }
)
public final class ProductCatalogFormDefinition {

    private ProductCatalogFormDefinition() {
    }

    @UiSection(id = "product", titleKey = "forms.catalog.section.product", groups = {
            ProductDetailsGroup.class
    }, order = 0)
    public static class ProductSection {
    }

    @UiSection(id = "pricing", titleKey = "forms.catalog.section.pricing", groups = {
            PricingGroup.class
    }, order = 1)
    public static class PricingSection {
    }

    @UiSection(id = "availability", titleKey = "forms.catalog.section.availability", groups = {
            AvailabilityGroup.class
    }, order = 2)
    public static class AvailabilitySection {
    }

    @UiGroup(id = "product-details", columns = 2)
    public static class ProductDetailsGroup {

        @UiField(path = "product.name", component = UiField.ComponentType.TEXT,
                labelKey = "forms.catalog.product.name", placeholderKey = "forms.catalog.product.name.placeholder",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void name() {
        }

        @UiField(path = "product.sku", component = UiField.ComponentType.TEXT,
                labelKey = "forms.catalog.product.sku", helperKey = "forms.catalog.product.sku.helper")
        public void sku() {
        }

        @UiField(path = "product.summary", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.catalog.product.summary", colSpan = 2)
        public void summary() {
        }

        @UiField(path = "product.description", component = UiField.ComponentType.RICH_TEXT,
                labelKey = "forms.catalog.product.description", colSpan = 2)
        public void description() {
        }

        @UiField(path = "product.releaseDate", component = UiField.ComponentType.JALALI_DATE,
                labelKey = "forms.catalog.product.release", placeholderKey = "forms.catalog.product.release.placeholder")
        public void releaseDate() {
        }
    }

    @UiGroup(id = "pricing-details", columns = 2)
    public static class PricingGroup {

        @UiField(path = "pricing.price", component = UiField.ComponentType.MONEY,
                labelKey = "forms.catalog.pricing.price", requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void price() {
        }

        @UiField(path = "pricing.currency", component = UiField.ComponentType.SELECT,
                labelKey = "forms.catalog.pricing.currency",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.currencies"))
        public void currency() {
        }

        @UiField(path = "pricing.taxCategory", component = UiField.ComponentType.SELECT,
                labelKey = "forms.catalog.pricing.tax",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.tax"))
        public void taxCategory() {
        }
    }

    @UiGroup(id = "availability-details", columns = 2)
    public static class AvailabilityGroup {

        @UiField(path = "availability.status", component = UiField.ComponentType.SELECT,
                labelKey = "forms.catalog.availability.status",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.stock-status"))
        public void status() {
        }

        @UiField(path = "availability.channels", component = UiField.ComponentType.MULTI_SELECT,
                labelKey = "forms.catalog.availability.channels",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.channels"))
        public void channels() {
        }

        @UiField(path = "availability.tags", component = UiField.ComponentType.TAGS,
                labelKey = "forms.catalog.availability.tags",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.product-tags"),
                colSpan = 2)
        public void tags() {
        }
    }
}
