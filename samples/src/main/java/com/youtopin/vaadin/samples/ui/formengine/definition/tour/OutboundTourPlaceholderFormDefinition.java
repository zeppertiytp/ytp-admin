package com.youtopin.vaadin.samples.ui.formengine.definition.tour;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.application.tour.model.OutboundTourPlaceholderFormData;

/**
 * Generic placeholder form used for steps that are not yet implemented.
 */
@UiForm(
        id = "outbound-tour-placeholder",
        titleKey = "tourwizard.placeholder.form.title",
        descriptionKey = "tourwizard.placeholder.form.description",
        bean = OutboundTourPlaceholderFormData.class,
        sections = OutboundTourPlaceholderFormDefinition.ContentSection.class,
        actions = {
                @UiAction(id = "outbound-placeholder-back", labelKey = "tourwizard.action.previous",
                        placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SECONDARY, order = 0),
                @UiAction(id = "outbound-placeholder-next", labelKey = "tourwizard.action.next",
                        placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SUBMIT, order = 1)
        }
)
public final class OutboundTourPlaceholderFormDefinition {

    private OutboundTourPlaceholderFormDefinition() {
    }

    @UiSection(id = "tour-placeholder-section", titleKey = "tourwizard.placeholder.section.notes",
            groups = PlaceholderGroup.class, order = 0)
    public static class ContentSection {
    }

    @UiGroup(id = "tour-placeholder-group", columns = 1)
    public static class PlaceholderGroup {

        @UiField(path = "notes", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "tourwizard.placeholder.field.notes")
        public void notes() {
        }
    }
}
