package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.ui.formengine.model.RepeatableDuplicationFormData;

/**
 * Demonstrates duplication controls for repeatable groups.
 */
@UiForm(
        id = "content-duplication",
        titleKey = "forms.duplicateSample.title",
        descriptionKey = "forms.duplicateSample.description",
        bean = RepeatableDuplicationFormData.class,
        sections = RepeatableDuplicationFormDefinition.ContentSection.class,
        actions = @UiAction(id = "content-save", labelKey = "forms.duplicateSample.action.save",
                placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SUBMIT, order = 0)
)
public final class RepeatableDuplicationFormDefinition {

    private RepeatableDuplicationFormDefinition() {
    }

    @UiSection(id = "content-section", titleKey = "forms.duplicateSample.section.blocks",
            groups = ContentGroup.class, order = 0)
    public static class ContentSection {
    }

    @UiGroup(id = "content-blocks", columns = 2,
            repeatable = @UiRepeatable(enabled = true, min = 1, max = 6,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "forms.duplicateSample.block.title",
                    allowDuplicate = true))
    public static class ContentGroup {

        @UiField(path = "layout.blocks.title", component = UiField.ComponentType.TEXT,
                labelKey = "forms.duplicateSample.field.blockTitle",
                helperKey = "forms.duplicateSample.field.blockTitle.helper",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void blockTitle() {
        }

        @UiField(path = "layout.blocks.summary", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.duplicateSample.field.blockSummary",
                helperKey = "forms.duplicateSample.field.blockSummary.helper", colSpan = 2)
        public void blockSummary() {
        }
    }
}
