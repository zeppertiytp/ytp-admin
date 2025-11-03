package com.youtopin.vaadin.samples.application.wizard;

import com.youtopin.vaadin.samples.application.wizard.model.ProjectLaunchBasicsFormData;
import com.youtopin.vaadin.samples.application.wizard.model.ProjectLaunchChecklistFormData;
import com.youtopin.vaadin.samples.application.wizard.model.ProjectLaunchTeamFormData;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;

/**
 * Session-scoped state tracking the wizard progress and collected form data.
 */
public class ProjectLaunchWizardState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String STEP_BASICS = "project-basics";
    public static final String STEP_TEAM = "project-team";
    public static final String STEP_CHECKLIST = "project-checklist";

    @Getter
    private final ProjectLaunchBasicsFormData basics = new ProjectLaunchBasicsFormData();
    @Getter
    private final ProjectLaunchTeamFormData team = new ProjectLaunchTeamFormData();
    @Getter
    private final ProjectLaunchChecklistFormData checklist = new ProjectLaunchChecklistFormData();
    private final LinkedHashSet<String> completedSteps = new LinkedHashSet<>();

    @Getter
    private String currentStepId = STEP_BASICS;
    @Getter
    private String projectId = "";

    public Set<String> getCompletedSteps() {
        return Collections.unmodifiableSet(completedSteps);
    }

    public boolean isStepCompleted(String stepId) {
        return stepId != null && completedSteps.contains(stepId);
    }

    public void markStepCompleted(String stepId) {
        if (stepId != null && !stepId.isBlank()) {
            completedSteps.add(stepId);
        }
    }

    public void setCurrentStepId(String currentStepId) {
        if (currentStepId != null && !currentStepId.isBlank()) {
            this.currentStepId = currentStepId;
        }
    }
    public void setProjectId(String projectId) {
        if (projectId == null) {
            this.projectId = "";
        } else {
            this.projectId = projectId.trim();
        }
    }

    public void reset() {
        completedSteps.clear();
        currentStepId = STEP_BASICS;
        projectId = "";
    }

    @Override
    public String toString() {
        return "ProjectLaunchWizardState{" +
                "currentStepId='" + currentStepId + '\'' +
                ", completedSteps=" + completedSteps +
                '}';
    }
}
