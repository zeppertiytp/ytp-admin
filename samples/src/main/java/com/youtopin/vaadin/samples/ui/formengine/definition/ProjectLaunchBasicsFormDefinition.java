package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.application.wizard.model.ProjectLaunchBasicsFormData;

/**
 * Form definition for the wizard's first step collecting project basics.
 */
@UiForm(
        id = "project-launch-basics",
        titleKey = "",
        descriptionKey = "",
        bean = ProjectLaunchBasicsFormData.class,
        sections = {
                ProjectLaunchBasicsFormDefinition.CoreSection.class,
                ProjectLaunchBasicsFormDefinition.ScheduleSection.class
        },
        actions = {
                @UiAction(id = "project-basics-next", labelKey = "wizardform.action.next",
                        placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SUBMIT,
                        order = 1)
        }
)
public final class ProjectLaunchBasicsFormDefinition {

    private ProjectLaunchBasicsFormDefinition() {
    }

    @UiSection(id = "project-basics-core", titleKey = "wizardform.basics.section.core",
            groups = ProjectDetailsGroup.class, order = 0)
    public static class CoreSection {
    }

    @UiSection(id = "project-basics-schedule", titleKey = "wizardform.basics.section.schedule",
            groups = ProjectScheduleGroup.class, order = 1)
    public static class ScheduleSection {
    }

    @UiGroup(id = "project-details", columns = 2)
    public static class ProjectDetailsGroup {

        @UiField(path = "name", component = UiField.ComponentType.TEXT,
                labelKey = "wizardform.basics.name",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void name() {
        }

        @UiField(path = "owner", component = UiField.ComponentType.TEXT,
                labelKey = "wizardform.basics.owner",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void owner() {
        }

        @UiField(path = "summary", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "wizardform.basics.summary", colSpan = 2)
        public void summary() {
        }
    }

    @UiGroup(id = "project-schedule", columns = 2)
    public static class ProjectScheduleGroup {

        @UiField(path = "kickoffDate", component = UiField.ComponentType.DATE,
                labelKey = "wizardform.basics.kickoff")
        public void kickoffDate() {
        }

        @UiField(path = "durationWeeks", component = UiField.ComponentType.INTEGER,
                labelKey = "wizardform.basics.duration", helperKey = "wizardform.basics.duration.helper")
        public void durationWeeks() {
        }
    }
}
