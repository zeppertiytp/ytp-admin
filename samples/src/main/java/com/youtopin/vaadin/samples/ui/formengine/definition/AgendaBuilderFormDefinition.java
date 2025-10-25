package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
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
                AgendaBuilderFormDefinition.SegmentOneSection.class,
                AgendaBuilderFormDefinition.SegmentTwoSection.class,
                AgendaBuilderFormDefinition.SegmentThreeSection.class,
                AgendaBuilderFormDefinition.SegmentFourSection.class
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

    @UiSection(id = "agenda-segment-1", titleKey = "forms.agenda.section.segment1",
            groups = SegmentOneGroup.class, order = 1)
    public static class SegmentOneSection {
    }

    @UiSection(id = "agenda-segment-2", titleKey = "forms.agenda.section.segment2",
            groups = SegmentTwoGroup.class, order = 2)
    public static class SegmentTwoSection {
    }

    @UiSection(id = "agenda-segment-3", titleKey = "forms.agenda.section.segment3",
            groups = SegmentThreeGroup.class, order = 3)
    public static class SegmentThreeSection {
    }

    @UiSection(id = "agenda-segment-4", titleKey = "forms.agenda.section.segment4",
            groups = SegmentFourGroup.class, order = 4)
    public static class SegmentFourSection {
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

    @UiGroup(id = "agenda-segment-one-group")
    public static class SegmentOneGroup {

        @UiField(path = "agenda.segment1", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.agenda.field.segment1", helperKey = "forms.agenda.field.segment.helper")
        public void segment() {
        }
    }

    @UiGroup(id = "agenda-segment-two-group")
    public static class SegmentTwoGroup {

        @UiField(path = "agenda.segment2", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.agenda.field.segment2", helperKey = "forms.agenda.field.segment.helper")
        public void segment() {
        }
    }

    @UiGroup(id = "agenda-segment-three-group")
    public static class SegmentThreeGroup {

        @UiField(path = "agenda.segment3", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.agenda.field.segment3", helperKey = "forms.agenda.field.segment.helper")
        public void segment() {
        }
    }

    @UiGroup(id = "agenda-segment-four-group")
    public static class SegmentFourGroup {

        @UiField(path = "agenda.segment4", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.agenda.field.segment4", helperKey = "forms.agenda.field.segment.helper")
        public void segment() {
        }
    }
}
