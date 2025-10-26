# Orchestrate multi-step forms with WizardFormFlowCoordinator

The `WizardFormFlowCoordinator` links the `HorizontalWizard` component with Form Engine
`RenderedForm` instances. It centralises navigation, completion state, and persistence so
views no longer have to duplicate listener wiring.

## 1. Instantiate the coordinator

```java
HorizontalWizard wizard = new HorizontalWizard();
WizardFormFlowCoordinator<ProjectLaunchWizardState> coordinator =
        new WizardFormFlowCoordinator<>(wizard,
                List.of(ProjectLaunchWizardState.STEP_BASICS,
                        ProjectLaunchWizardState.STEP_TEAM,
                        ProjectLaunchWizardState.STEP_CHECKLIST),
                wizardState);
```

*The ordered step identifiers define navigation order and completion rules.*

## 2. Register rendered forms per step

```java
RenderedForm<ProjectLaunchBasicsFormData> basicsForm = render(ProjectLaunchBasicsFormDefinition.class);
coordinator.registerStepForm(ProjectLaunchWizardState.STEP_BASICS,
        basicsForm, ProjectLaunchWizardState::getBasics);
```

The coordinator automatically injects the `actionBeanSupplier` so every action works with the
shared context bean. Repeat for each step before showing the wizard.

## 3. Configure callbacks and wizard metadata

```java
coordinator.onStepSelection((stepId, state) -> {
    state.setCurrentStepId(stepId);
    wizardService.store(state);
    showStep(stepId);
});
coordinator.onStepCompletion((stepId, state) -> wizardService.store(state));
coordinator.configureWizard(stepId -> WizardStep.of(stepId, getTranslation(labelKeys.get(stepId))));
```

The selection callback is invoked for programmatic navigation and user clicks, ensuring the
context stays in sync. The completion callback is optional but convenient for persisting the
updated context to a session or backend store.

## 4. Example: Generate an ID in the first step and reuse it later

```java
form.addActionHandler("project-basics-next", context -> {
    if (wizardState.getProjectId().isBlank()) {
        wizardState.setProjectId("PRJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
    }
    coordinator.markStepCompleted(ProjectLaunchWizardState.STEP_BASICS);
    coordinator.nextStep(ProjectLaunchWizardState.STEP_BASICS)
            .ifPresent(coordinator::setCurrentStepId);
});
```

Later steps can read `wizardState.getProjectId()` to display the generated identifier or pass it
in API calls. Because the coordinator runs the completion callback after `markStepCompleted`, the
new ID is stored automatically.

The refreshed `WizardFormFlowView` and the new sample route `/forms/wizard-coordinator` both use
this pattern to demonstrate reusing the generated ID across steps.

## 5. Explore the sample

Run the samples module and open `/forms/wizard-coordinator` to see the coordinator drive two
steps while persisting the workspace identifier and summarising the collected data.
