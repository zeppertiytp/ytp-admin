package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.application.wizard.model.ProjectLaunchTeamFormData;

/**
 * Form definition for the wizard's team configuration step.
 */
@UiForm(
        id = "project-launch-team",
        titleKey = "",
        descriptionKey = "",
        bean = ProjectLaunchTeamFormData.class,
        sections = {
                ProjectLaunchTeamFormDefinition.TeamCoreSection.class,
                ProjectLaunchTeamFormDefinition.CollaborationSection.class
        },
        actions = {
                @UiAction(id = "project-team-back", labelKey = "wizardform.action.back",
                        placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SECONDARY,
                        order = 0),
                @UiAction(id = "project-team-next", labelKey = "wizardform.action.next",
                        placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SUBMIT,
                        order = 1)
        }
)
public final class ProjectLaunchTeamFormDefinition {

    private ProjectLaunchTeamFormDefinition() {
    }

    @UiSection(id = "project-team-core", titleKey = "wizardform.team.section.core",
            groups = TeamCoreGroup.class, order = 0)
    public static class TeamCoreSection {
    }

    @UiSection(id = "project-team-collaboration", titleKey = "wizardform.team.section.collaboration",
            groups = TeamCollaborationGroup.class, order = 1)
    public static class CollaborationSection {
    }

    @UiGroup(id = "team-core", columns = 2)
    public static class TeamCoreGroup {

        @UiField(path = "leadName", component = UiField.ComponentType.TEXT,
                labelKey = "wizardform.team.leadName",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void leadName() {
        }

        @UiField(path = "leadEmail", component = UiField.ComponentType.EMAIL,
                labelKey = "wizardform.team.leadEmail",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void leadEmail() {
        }

        @UiField(path = "teamSize", component = UiField.ComponentType.INTEGER,
                labelKey = "wizardform.team.teamSize")
        public void teamSize() {
        }
    }

    @UiGroup(id = "team-collaboration", columns = 2)
    public static class TeamCollaborationGroup {

        @UiField(path = "workModel", component = UiField.ComponentType.SELECT,
                labelKey = "wizardform.team.workModel",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.work-model"))
        public void workModel() {
        }

        @UiField(path = "communicationChannel", component = UiField.ComponentType.TEXT,
                labelKey = "wizardform.team.channel", helperKey = "wizardform.team.channel.helper", colSpan = 2)
        public void communicationChannel() {
        }
    }
}
