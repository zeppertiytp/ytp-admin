package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.application.wizard.model.ProjectLaunchChecklistFormData;

/**
 * Form definition for the launch readiness checklist step.
 */
@UiForm(
        id = "project-launch-checklist",
        titleKey = "",
        descriptionKey = "",
        bean = ProjectLaunchChecklistFormData.class,
        sections = ProjectLaunchChecklistFormDefinition.ReadinessSection.class,
        actions = {
                @UiAction(id = "project-launch-back", labelKey = "wizardform.action.back",
                        placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SECONDARY,
                        order = 0),
                @UiAction(id = "project-launch-finish", labelKey = "wizardform.action.finish",
                        placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SUBMIT,
                        order = 1)
        }
)
public final class ProjectLaunchChecklistFormDefinition {

    private ProjectLaunchChecklistFormDefinition() {
    }

    @UiSection(id = "project-launch-review", titleKey = "wizardform.launch.section.review",
            groups = ReadinessGroup.class, order = 0)
    public static class ReadinessSection {
    }

    @UiGroup(id = "project-launch-readiness", columns = 2)
    public static class ReadinessGroup {

        @UiField(path = "launchDate", component = UiField.ComponentType.DATE,
                labelKey = "wizardform.launch.launchDate",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void launchDate() {
        }

        @UiField(path = "riskLevel", component = UiField.ComponentType.RADIO,
                labelKey = "wizardform.launch.riskLevel",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.launch-risk"),
                colSpan = 2)
        public void riskLevel() {
        }

        @UiField(path = "communicationReady", component = UiField.ComponentType.SWITCH,
                labelKey = "wizardform.launch.communicationReady",
                helperKey = "wizardform.launch.communicationReady.helper", colSpan = 2)
        public void communicationReady() {
        }

        @UiField(path = "supportContact", component = UiField.ComponentType.TEXT,
                labelKey = "wizardform.launch.supportContact")
        public void supportContact() {
        }

        @UiField(path = "notes", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "wizardform.launch.notes", colSpan = 2)
        public void notes() {
        }
    }
}
