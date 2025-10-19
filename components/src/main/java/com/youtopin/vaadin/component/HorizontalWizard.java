package com.youtopin.vaadin.component;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.shared.Registration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Horizontal wizard component that renders the progress across multiple steps.
 * Circles representing each step are connected with thin separators, the
 * current step is highlighted and completed steps can optionally apply custom
 * colors.
 */
@CssImport("./components/horizontal-wizard.css")
public class HorizontalWizard extends Composite<Div> implements HasSize {

    private final List<WizardStep> steps = new ArrayList<>();
    private final List<StepElement> stepElements = new ArrayList<>();
    private final List<ConnectorElement> connectors = new ArrayList<>();
    private final Map<String, Integer> indexById = new LinkedHashMap<>();
    private Registration pendingAttachScroll;

    private String completedColor = "var(--color-primary-600)";
    private String currentColor = "var(--color-primary-700)";
    private String upcomingColor = "var(--lumo-contrast-30pct)";

    private int currentIndex = -1;

    public HorizontalWizard() {
        Div container = getContent();
        container.addClassName("horizontal-wizard");
        container.setWidthFull();
        container.getElement().setAttribute("role", "list");
    }

    /**
     * Replaces the wizard steps with the provided list, keeping the current
     * step if the identifier still exists. When no current step is available the
     * first item becomes active.
     *
     * @param steps ordered collection of steps
     */
    public void setSteps(Collection<WizardStep> steps) {
        Objects.requireNonNull(steps, "steps must not be null");

        String previousId = getCurrentStepId().orElse(null);
        List<WizardStep> sanitized = new ArrayList<>(steps.size());
        LinkedHashSet<String> ids = new LinkedHashSet<>();

        for (WizardStep step : steps) {
            Objects.requireNonNull(step, "steps must not contain null");
            if (!ids.add(step.getId())) {
                throw new IllegalArgumentException("Duplicate step id '" + step.getId() + "'");
            }
            sanitized.add(step.copy());
        }

        this.steps.clear();
        this.steps.addAll(sanitized);
        rebuildElements();

        if (previousId != null && indexById.containsKey(previousId)) {
            setCurrentStepIndex(indexById.get(previousId));
        } else if (!this.steps.isEmpty()) {
            setCurrentStepIndex(0);
        } else {
            currentIndex = -1;
            refreshStates();
        }
    }

    /**
     * Convenience var-args setter for steps.
     */
    public void setSteps(WizardStep... steps) {
        setSteps(Arrays.asList(steps));
    }

    /**
     * Returns an immutable view of the configured steps.
     */
    public List<WizardStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Sets the active step by zero-based index.
     *
     * @param index index of the current step
     */
    public void setCurrentStepIndex(int index) {
        if (index < 0 || index >= steps.size()) {
            throw new IllegalArgumentException("Step index out of bounds: " + index);
        }
        if (index == currentIndex) {
            if (index >= 0 && index < stepElements.size()) {
                scrollStepIntoView(stepElements.get(index));
            }
            return;
        }

        int previousIndex = currentIndex;
        currentIndex = index;
        refreshStates();
        fireEvent(new CurrentStepChangeEvent(this,
                previousIndex >= 0 ? previousIndex : null,
                previousIndex >= 0 ? steps.get(previousIndex) : null,
                currentIndex,
                steps.get(currentIndex)));
    }

    /**
     * @return the currently selected step index if one has been configured
     */
    public OptionalInt getCurrentStepIndex() {
        return currentIndex >= 0 ? OptionalInt.of(currentIndex) : OptionalInt.empty();
    }

    /**
     * Sets the current step by identifier.
     *
     * @param stepId identifier of the step
     */
    public void setCurrentStepId(String stepId) {
        Objects.requireNonNull(stepId, "stepId must not be null");
        Integer index = indexById.get(stepId);
        if (index == null) {
            throw new IllegalArgumentException("Unknown step id '" + stepId + "'");
        }
        setCurrentStepIndex(index);
    }

    /**
     * @return identifier of the active step, if any
     */
    public Optional<String> getCurrentStepId() {
        if (currentIndex < 0 || currentIndex >= steps.size()) {
            return Optional.empty();
        }
        return Optional.of(steps.get(currentIndex).getId());
    }

    /**
     * Moves the current step forward if possible.
     */
    public void advance() {
        if (currentIndex >= 0 && currentIndex < steps.size() - 1) {
            setCurrentStepIndex(currentIndex + 1);
        }
    }

    /**
     * Moves the current step backwards if possible.
     */
    public void retreat() {
        if (currentIndex > 0) {
            setCurrentStepIndex(currentIndex - 1);
        }
    }

    /**
     * Registers a listener that is notified whenever the current step changes.
     */
    public Registration addCurrentStepChangeListener(
            ComponentEventListener<CurrentStepChangeEvent> listener) {
        return addListener(CurrentStepChangeEvent.class, listener);
    }

    /**
     * Registers a listener that is notified when a clickable step is triggered.
     */
    public Registration addStepClickListener(
            ComponentEventListener<StepClickEvent> listener) {
        return addListener(StepClickEvent.class, listener);
    }

    /**
     * Overrides the default color applied to completed steps that do not define
     * a per-step color.
     */
    public void setCompletedColor(String completedColor) {
        this.completedColor = normalizeColor(completedColor, "completedColor");
        refreshStates();
    }

    public String getCompletedColor() {
        return completedColor;
    }

    /**
     * Overrides the color used for the current step indicator.
     */
    public void setCurrentColor(String currentColor) {
        this.currentColor = normalizeColor(currentColor, "currentColor");
        refreshStates();
    }

    public String getCurrentColor() {
        return currentColor;
    }

    /**
     * Overrides the color used for upcoming steps and connectors.
     */
    public void setUpcomingColor(String upcomingColor) {
        this.upcomingColor = normalizeColor(upcomingColor, "upcomingColor");
        refreshStates();
    }

    public String getUpcomingColor() {
        return upcomingColor;
    }

    private void rebuildElements() {
        Div container = getContent();
        container.removeAll();
        stepElements.clear();
        connectors.clear();
        indexById.clear();

        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                ConnectorElement connector = new ConnectorElement();
                connectors.add(connector);
                container.add(connector);
            }

            WizardStep step = steps.get(i);
            StepElement element = new StepElement(step, i);
            stepElements.add(element);
            indexById.put(step.getId(), i);
            container.add(element);
        }
    }

    private void refreshStates() {
        StepElement activeStep = null;
        for (int i = 0; i < stepElements.size(); i++) {
            StepState state = stateForIndex(i);
            StepElement element = stepElements.get(i);
            element.applyState(state);
            if (state == StepState.CURRENT) {
                activeStep = element;
            }
        }
        for (int i = 0; i < connectors.size(); i++) {
            connectors.get(i).applyState(stepElements.get(i));
        }
        if (activeStep != null) {
            scrollStepIntoView(activeStep);
        }
    }

    private StepState stateForIndex(int index) {
        if (currentIndex < 0) {
            return StepState.UPCOMING;
        }
        if (index < currentIndex) {
            return StepState.COMPLETED;
        }
        if (index == currentIndex) {
            return StepState.CURRENT;
        }
        return StepState.UPCOMING;
    }

    private static String normalizeColor(String color, String parameterName) {
        Objects.requireNonNull(color, parameterName + " must not be null");
        if (color.isBlank()) {
            throw new IllegalArgumentException(parameterName + " must not be blank");
        }
        return color;
    }

    private enum StepState {
        COMPLETED,
        CURRENT,
        UPCOMING
    }

    private void scrollStepIntoView(StepElement activeStep) {
        Runnable command = () -> activeStep.getElement().executeJs(
                "this.scrollIntoView({behavior: 'instant', block: 'nearest', inline: 'center'});");
        if (isAttached()) {
            command.run();
        } else {
            if (pendingAttachScroll != null) {
                pendingAttachScroll.remove();
            }
            pendingAttachScroll = addAttachListener(event -> {
                command.run();
                if (pendingAttachScroll != null) {
                    pendingAttachScroll.remove();
                    pendingAttachScroll = null;
                }
            });
        }
    }

    private final class StepElement extends Div {
        private final WizardStep step;
        private final int index;
        private final Div circle = new Div();
        private final Span label = new Span();
        private StepState state = StepState.UPCOMING;
        private String activeColor = upcomingColor;

        private StepElement(WizardStep step, int index) {
            this.step = step;
            this.index = index;
            addClassName("horizontal-wizard__step");
            getElement().setAttribute("role", "listitem");

            circle.addClassName("horizontal-wizard__circle");
            circle.getElement().setAttribute("role", "presentation");
            circle.getElement().setAttribute("aria-hidden", "false");

            label.addClassName("horizontal-wizard__label");

            add(circle, label);
            updateTexts();

            addClickListener(event -> handleActivation());
            getElement().addEventListener("keydown", event -> {
                if (!step.isClickable()) {
                    return;
                }
                handleActivation();
            }).setFilter("event.key === 'Enter' || event.key === ' '");
        }

        private void updateTexts() {
            label.setText(step.getTitle());
            String indicator = step.getIndicator().filter(it -> !it.isBlank())
                    .orElseGet(() -> Integer.toString(index + 1));
            circle.setText(indicator);
            circle.getElement().setProperty("title", step.getTitle());
            circle.getElement().setAttribute("aria-label", step.getTitle());
            updateInteractionState();
        }

        private void applyState(StepState state) {
            this.state = state;
            updateTexts();
            String stateName = state.name().toLowerCase(Locale.ROOT);
            getElement().setAttribute("data-state", stateName);
            if (state == StepState.CURRENT) {
                circle.getElement().setAttribute("aria-current", "step");
            } else {
                circle.getElement().removeAttribute("aria-current");
            }
            String color = colorForState(state);
            this.activeColor = color;
            circle.getStyle().set("--horizontal-wizard-step-color", color);
        }

        private StepState getState() {
            return state;
        }

        private String getActiveColor() {
            return activeColor;
        }

        private String colorForState(StepState state) {
            return switch (state) {
                case COMPLETED -> step.getCompletedColor().orElse(completedColor);
                case CURRENT -> currentColor;
                case UPCOMING -> upcomingColor;
            };
        }

        private void updateInteractionState() {
            boolean clickable = step.isClickable();
            getElement().setAttribute("tabindex", clickable ? "0" : "-1");
            getElement().setAttribute("data-clickable", clickable ? "true" : "false");
            getElement().setAttribute("aria-disabled", clickable ? "false" : "true");
        }

        private void handleActivation() {
            if (!step.isClickable()) {
                return;
            }
            HorizontalWizard.this.setCurrentStepIndex(index);
            HorizontalWizard.this.fireEvent(
                    new StepClickEvent(HorizontalWizard.this, index, steps.get(index)));
        }
    }

    private final class ConnectorElement extends Div {
        private ConnectorElement() {
            addClassName("horizontal-wizard__connector");
            getElement().setAttribute("aria-hidden", "true");
        }

        private void applyState(StepElement precedingStep) {
            StepState state = precedingStep.getState();
            String stateName = state.name().toLowerCase(Locale.ROOT);
            getElement().setAttribute("data-state", stateName);
            String color = state == StepState.UPCOMING ? upcomingColor : precedingStep.getActiveColor();
            getStyle().set("--horizontal-wizard-connector-color", color);
        }
    }

    /**
     * Declarative description of a wizard step.
     */
    public static class WizardStep implements Serializable {
        private final String id;
        private String title;
        private String indicator;
        private String completedColor;
        private boolean clickable;

        private WizardStep(String id, String title) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("step id must not be blank");
            }
            this.id = id;
            this.title = Objects.requireNonNull(title, "title must not be null");
        }

        /**
         * Factory method for creating a step.
         */
        public static WizardStep of(String id, String title) {
            return new WizardStep(id, title);
        }

        /**
         * Sets a custom indicator that is rendered inside the circle instead of
         * the default step index.
         */
        public WizardStep withIndicator(String indicator) {
            this.indicator = indicator;
            return this;
        }

        /**
         * Clears the custom indicator and falls back to the ordinal number.
         */
        public WizardStep clearIndicator() {
            this.indicator = null;
            return this;
        }

        /**
         * Sets the label shown beneath the indicator.
         */
        public WizardStep withTitle(String title) {
            this.title = Objects.requireNonNull(title, "title must not be null");
            return this;
        }

        /**
         * Applies a specific color that is used when the step is marked as
         * completed.
         */
        public WizardStep withCompletedColor(String cssColor) {
            if (cssColor == null || cssColor.isBlank()) {
                throw new IllegalArgumentException("cssColor must not be blank");
            }
            this.completedColor = cssColor;
            return this;
        }

        /**
         * Resets any custom completed color and falls back to the wizard default.
         */
        public WizardStep clearCompletedColor() {
            this.completedColor = null;
            return this;
        }

        /**
         * Marks the step as clickable. Clickable steps fire click events and update
         * the current selection when activated.
         */
        public WizardStep clickable() {
            return withClickable(true);
        }

        /**
         * Explicitly enables or disables click support for the step.
         */
        public WizardStep withClickable(boolean clickable) {
            this.clickable = clickable;
            return this;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Optional<String> getIndicator() {
            return Optional.ofNullable(indicator);
        }

        public Optional<String> getCompletedColor() {
            return Optional.ofNullable(completedColor);
        }

        public boolean isClickable() {
            return clickable;
        }

        private WizardStep copy() {
            WizardStep copy = new WizardStep(id, title);
            copy.indicator = indicator;
            copy.completedColor = completedColor;
            copy.clickable = clickable;
            return copy;
        }
    }

    /**
     * Event fired when the current wizard step changes.
     */
    public static class CurrentStepChangeEvent extends ComponentEvent<HorizontalWizard> {
        private final Integer previousIndex;
        private final WizardStep previousStep;
        private final int currentIndex;
        private final WizardStep currentStep;

        private CurrentStepChangeEvent(HorizontalWizard source,
                Integer previousIndex,
                WizardStep previousStep,
                int currentIndex,
                WizardStep currentStep) {
            super(source, false);
            this.previousIndex = previousIndex;
            this.previousStep = previousStep;
            this.currentIndex = currentIndex;
            this.currentStep = currentStep;
        }

        public OptionalInt getPreviousIndex() {
            return previousIndex == null ? OptionalInt.empty() : OptionalInt.of(previousIndex);
        }

        public Optional<WizardStep> getPreviousStep() {
            return Optional.ofNullable(previousStep);
        }

        public int getCurrentIndex() {
            return currentIndex;
        }

        public WizardStep getCurrentStep() {
            return currentStep;
        }
    }

    /**
     * Event fired when a clickable step is activated by the user.
     */
    public static class StepClickEvent extends ComponentEvent<HorizontalWizard> {
        private final int index;
        private final WizardStep step;

        private StepClickEvent(HorizontalWizard source, int index, WizardStep step) {
            super(source, false);
            this.index = index;
            this.step = step;
        }

        public int getIndex() {
            return index;
        }

        public WizardStep getStep() {
            return step;
        }
    }
}
