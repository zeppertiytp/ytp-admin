package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.ui.formengine.model.AgendaBuilderFormData;

/**
 * Definition for the agenda builder sample showcasing add/remove driven sections.
 */
@UiForm(
        id = "agenda-builder",
        titleKey = "forms.agenda.title",
        descriptionKey = "forms.agenda.description",
        bean = AgendaBuilderFormData.class,
        sections = {
                AgendaBuilderFormDefinition.OverviewSection.class,
                AgendaBuilderFormDefinition.SegmentsSection.class
        },
        actions = {
                @UiAction(id = "agenda-add-section", labelKey = "forms.agenda.action.addSection",
                        placement = UiAction.Placement.HEADER, type = UiAction.ActionType.SECONDARY, order = 0),
                @UiAction(id = "agenda-remove-section", labelKey = "forms.agenda.action.removeSection",
                        placement = UiAction.Placement.HEADER, type = UiAction.ActionType.SECONDARY, order = 1),
                @UiAction(id = "agenda-submit", labelKey = "forms.agenda.action.submit",
                        placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SUBMIT, order = 1)
        }
)
public final class AgendaBuilderFormDefinition {

    private AgendaBuilderFormDefinition() {
    }

    @UiSection(id = "agenda-overview", titleKey = "forms.agenda.section.overview",
            groups = OverviewGroup.class, order = 0)
    public static class OverviewSection {
    }

    @UiSection(id = "agenda-segments", titleKey = "forms.agenda.section.segments",
            groups = SegmentGroup.class, order = 1)
    public static class SegmentsSection {
    }

    @UiGroup(id = "agenda-overview-group", columns = 2)
    public static class OverviewGroup {

        @UiField(path = "agenda.sessionName", component = UiField.ComponentType.TEXT,
                labelKey = "forms.agenda.field.sessionName", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required")
        public void sessionName() {
        }

        @UiField(path = "agenda.facilitator", component = UiField.ComponentType.TEXT,
                labelKey = "forms.agenda.field.facilitator")
        public void facilitator() {
        }

        @UiField(path = "agenda.location", component = UiField.ComponentType.TEXT,
                labelKey = "forms.agenda.field.location", helperKey = "forms.agenda.field.location.helper")
        public void location() {
        }

        @UiField(path = "agenda.summary", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.agenda.field.summary", helperKey = "forms.agenda.field.summary.helper", colSpan = 2)
        public void summary() {
        }
    }

    @UiGroup(id = "agenda-segment-group", columns = 2,
            repeatable = @UiRepeatable(enabled = true, min = 1, max = 6,
                    mode = UiRepeatable.RepeatableMode.CARD_DIALOG, uniqueBy = "title", allowDuplicate = false))
    public static class SegmentGroup {

        @UiField(path = "agenda.segments.title", component = UiField.ComponentType.TEXT,
                labelKey = "forms.agenda.field.segmentTitle", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required")
        public void title() {
        }

        @UiField(path = "agenda.segments.description", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.agenda.field.segmentDescription",
                helperKey = "forms.agenda.field.segmentDescription.helper", colSpan = 2)
        public void description() {
        }
    }
}
