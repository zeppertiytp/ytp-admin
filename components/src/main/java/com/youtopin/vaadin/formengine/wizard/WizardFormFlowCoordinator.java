package com.youtopin.vaadin.formengine.wizard;

import com.youtopin.vaadin.component.HorizontalWizard;
import com.youtopin.vaadin.component.HorizontalWizard.WizardStep;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Coordinates a {@link HorizontalWizard} with {@link RenderedForm} instances so hosts can
 * collect multi-step form data while persisting a shared context object.
 *
 * @param <C> type of the shared context bean.
 */
public class WizardFormFlowCoordinator<C> {

    private final HorizontalWizard wizard;
    private final List<String> orderedStepIds;
    private final C context;
    private final Map<String, RenderedForm<?>> forms = new LinkedHashMap<>();
    private final LinkedHashSet<String> completedSteps = new LinkedHashSet<>();

    private Function<String, WizardStep> stepFactory;
    private BiConsumer<String, C> stepSelectionCallback;
    private BiConsumer<String, C> stepCompletionCallback;

    private boolean wizardInitialised;
    private boolean suppressWizardEvents;
    private String currentStepId;

    /**
     * Creates a new coordinator bound to the provided wizard and ordered steps.
     *
     * @param wizard         wizard component that renders the progress indicator
     * @param orderedStepIds ordered list of step identifiers
     * @param context        shared context that contains all step beans
     */
    public WizardFormFlowCoordinator(HorizontalWizard wizard,
                                     Collection<String> orderedStepIds,
                                     C context) {
        this.wizard = Objects.requireNonNull(wizard, "wizard must not be null");
        Objects.requireNonNull(orderedStepIds, "orderedStepIds must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.orderedStepIds = List.copyOf(orderedStepIds);
        if (this.orderedStepIds.isEmpty()) {
            throw new IllegalArgumentException("orderedStepIds must contain at least one entry");
        }
        ensureUniqueIds(this.orderedStepIds);

        wizard.addCurrentStepChangeListener(event -> {
            if (suppressWizardEvents || event.getCurrentStep() == null) {
                return;
            }
            String stepId = event.getCurrentStep().getId();
            updateCurrentStep(stepId, true);
        });
        wizard.addStepClickListener(event -> {
            String stepId = event.getStep().getId();
            if (isStepAccessible(stepId)) {
                setCurrentStepId(stepId);
            }
        });

        this.currentStepId = this.orderedStepIds.get(0);
    }

    private static void ensureUniqueIds(Collection<String> ids) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (String id : ids) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("step identifiers must not be null or blank");
            }
            if (!seen.add(id)) {
                throw new IllegalArgumentException("Duplicate step identifier '" + id + "'");
            }
        }
    }

    /**
     * Registers the form rendered for the given step. The form's action supplier is configured
     * to return the bean resolved from the shared context, ensuring action handlers receive
     * the latest state without additional boilerplate.
     *
     * @param stepId       identifier of the step
     * @param renderedForm rendered form associated with the step
     * @param beanResolver function that returns the bean representing the step inside the context
     * @param <T>          form bean type
     * @return the registered form
     */
    public <T> RenderedForm<T> registerStepForm(String stepId,
                                                RenderedForm<T> renderedForm,
                                                Function<C, T> beanResolver) {
        Objects.requireNonNull(stepId, "stepId must not be null");
        Objects.requireNonNull(renderedForm, "renderedForm must not be null");
        Objects.requireNonNull(beanResolver, "beanResolver must not be null");
        ensureKnownStep(stepId);
        renderedForm.setActionBeanSupplier(() -> Objects.requireNonNull(beanResolver.apply(context),
                "beanResolver must not return null"));
        forms.put(stepId, renderedForm);
        return renderedForm;
    }

    private void ensureKnownStep(String stepId) {
        if (!orderedStepIds.contains(stepId)) {
            throw new IllegalArgumentException("Unknown step id '" + stepId + "'");
        }
    }

    /**
     * Clears any previously registered forms. The wizard configuration remains intact.
     */
    public void clearForms() {
        forms.clear();
    }

    /**
     * Provides an immutable view of the registered forms keyed by step identifier.
     */
    public Map<String, RenderedForm<?>> getForms() {
        return Collections.unmodifiableMap(forms);
    }

    /**
     * Configures the wizard step factory. The factory is invoked every time the coordinator
     * refreshes the wizard to rebuild the clickable metadata using the latest completion state.
     *
     * @param factory step factory that must return a new {@link WizardStep} instance per invocation
     */
    public void configureWizard(Function<String, WizardStep> factory) {
        this.stepFactory = Objects.requireNonNull(factory, "factory must not be null");
        refreshWizard();
    }

    /**
     * Rebuilds the wizard using the configured step factory. Hosts normally only need to call
     * this when locale-dependent labels change.
     */
    public void refreshWizard() {
        if (stepFactory == null) {
            throw new IllegalStateException("Wizard step factory not configured");
        }
        List<WizardStep> steps = new ArrayList<>(orderedStepIds.size());
        int resumeIndex = computeResumeIndex();
        for (int index = 0; index < orderedStepIds.size(); index++) {
            String stepId = orderedStepIds.get(index);
            WizardStep step = Objects.requireNonNull(stepFactory.apply(stepId),
                    () -> "Step factory returned null for id " + stepId);
            step.withClickable(index <= resumeIndex);
            steps.add(step);
        }
        wizardInitialised = true;
        wizard.setSteps(steps);
        wizard.setCompletedSteps(completedSteps);
        ensureCurrentStep();
        applyCurrentStepToWizard();
    }

    /**
     * Marks a step as completed and persists the state through the configured callback, if any.
     */
    public void markStepCompleted(String stepId) {
        ensureKnownStep(stepId);
        if (completedSteps.add(stepId)) {
            if (stepCompletionCallback != null) {
                stepCompletionCallback.accept(stepId, context);
            }
            if (wizardInitialised) {
                refreshWizard();
            }
        }
    }

    /**
     * Replaces the completed steps with the provided collection.
     */
    public void setCompletedSteps(Collection<String> stepIds) {
        completedSteps.clear();
        if (stepIds != null) {
            stepIds.stream()
                    .filter(Objects::nonNull)
                    .filter(orderedStepIds::contains)
                    .forEach(completedSteps::add);
        }
        if (wizardInitialised) {
            refreshWizard();
        }
    }

    /**
     * Returns an immutable view of the completed step identifiers.
     */
    public Set<String> getCompletedSteps() {
        return Collections.unmodifiableSet(completedSteps);
    }

    /**
     * Sets the callback that is invoked whenever the current step changes.
     */
    public void onStepSelection(BiConsumer<String, C> callback) {
        this.stepSelectionCallback = callback;
    }

    /**
     * Sets the callback that is invoked whenever a step transitions to the completed state.
     */
    public void onStepCompletion(BiConsumer<String, C> callback) {
        this.stepCompletionCallback = callback;
    }

    /**
     * Returns the identifier of the current step.
     */
    public String getCurrentStepId() {
        return currentStepId;
    }

    /**
     * Programmatically selects a step. The wizard component is updated immediately when it has
     * already been initialised.
     */
    public void setCurrentStepId(String stepId) {
        ensureKnownStep(stepId);
        updateCurrentStep(stepId, false);
    }

    private void updateCurrentStep(String stepId, boolean fromWizard) {
        if (Objects.equals(currentStepId, stepId)) {
            return;
        }
        this.currentStepId = stepId;
        if (!fromWizard) {
            applyCurrentStepToWizard();
        }
        if (stepSelectionCallback != null) {
            stepSelectionCallback.accept(stepId, context);
        }
    }

    private void applyCurrentStepToWizard() {
        if (!wizardInitialised || currentStepId == null) {
            return;
        }
        suppressWizardEvents = true;
        try {
            wizard.setCurrentStepId(currentStepId);
        } finally {
            suppressWizardEvents = false;
        }
    }

    private void ensureCurrentStep() {
        if (currentStepId != null && orderedStepIds.contains(currentStepId)) {
            return;
        }
        int resumeIndex = computeResumeIndex();
        currentStepId = orderedStepIds.get(Math.min(resumeIndex, orderedStepIds.size() - 1));
        if (stepSelectionCallback != null) {
            stepSelectionCallback.accept(currentStepId, context);
        }
    }

    /**
     * Computes the resume index based on the first incomplete step. Steps up to this index
     * (inclusive) are considered accessible.
     */
    public int computeResumeIndex() {
        for (int index = 0; index < orderedStepIds.size(); index++) {
            if (!completedSteps.contains(orderedStepIds.get(index))) {
                return index;
            }
        }
        return orderedStepIds.size() - 1;
    }

    /**
     * Determines whether the requested step should be interactable by the user according to the
     * completion rules.
     */
    public boolean isStepAccessible(String stepId) {
        int requestedIndex = orderedStepIds.indexOf(stepId);
        if (requestedIndex < 0) {
            return false;
        }
        return requestedIndex <= computeResumeIndex();
    }

    /**
     * Returns the identifier of the next step after the supplied one.
     */
    public Optional<String> nextStep(String current) {
        int index = orderedStepIds.indexOf(current);
        if (index >= 0 && index < orderedStepIds.size() - 1) {
            return Optional.of(orderedStepIds.get(index + 1));
        }
        return Optional.empty();
    }

    /**
     * Returns the identifier of the previous step before the supplied one.
     */
    public Optional<String> previousStep(String current) {
        int index = orderedStepIds.indexOf(current);
        if (index > 0) {
            return Optional.of(orderedStepIds.get(index - 1));
        }
        return Optional.empty();
    }

    /**
     * Returns the identifier of the first step that should be presented when resuming the flow.
     */
    public String determineInitialStep() {
        return orderedStepIds.get(Math.min(computeResumeIndex(), orderedStepIds.size() - 1));
    }

    /**
     * Returns the shared context bean.
     */
    public C getContext() {
        return context;
    }

    public HorizontalWizard getWizard() {
        return wizard;
    }
}
