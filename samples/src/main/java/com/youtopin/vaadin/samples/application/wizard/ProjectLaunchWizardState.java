package com.youtopin.vaadin.samples.application.wizard;

import com.youtopin.vaadin.samples.ui.formengine.model.ProjectLaunchBasicsFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.ProjectLaunchChecklistFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.ProjectLaunchTeamFormData;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Session-scoped state tracking the wizard progress and collected form data.
 */
public class ProjectLaunchWizardState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String STEP_BASICS = "project-basics";
    public static final String STEP_TEAM = "project-team";
    public static final String STEP_CHECKLIST = "project-checklist";

    private final ProjectLaunchBasicsFormData basics = new ProjectLaunchBasicsFormData();
    private final ProjectLaunchTeamFormData team = new ProjectLaunchTeamFormData();
    private final ProjectLaunchChecklistFormData checklist = new ProjectLaunchChecklistFormData();
    private final LinkedHashSet<String> completedSteps = new LinkedHashSet<>();

    private String currentStepId = STEP_BASICS;

    public ProjectLaunchBasicsFormData getBasics() {
        return basics;
    }

    public ProjectLaunchTeamFormData getTeam() {
        return team;
    }

    public ProjectLaunchChecklistFormData getChecklist() {
        return checklist;
    }

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

    public String getCurrentStepId() {
        return currentStepId;
    }

    public void setCurrentStepId(String currentStepId) {
        if (currentStepId != null && !currentStepId.isBlank()) {
            this.currentStepId = currentStepId;
        }
    }

    public void reset() {
        completedSteps.clear();
        currentStepId = STEP_BASICS;
    }

    @Override
    public String toString() {
        return "ProjectLaunchWizardState{" +
                "currentStepId='" + currentStepId + '\'' +
                ", completedSteps=" + completedSteps +
                '}';
    }
}
