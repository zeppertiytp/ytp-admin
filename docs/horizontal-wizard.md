# Horizontal Wizard

The `HorizontalWizard` component renders progress through a linear flow using
numbered circles connected by slim separators. It lives in the component
library so application views can provide a consistent multi-step experience.

## Component overview

- **Source:** `components/src/main/java/com/youtopin/vaadin/component/HorizontalWizard.java`
- **Styles:** `components/src/main/resources/META-INF/resources/frontend/components/horizontal-wizard.css`
- **API highlights:**
  - `setSteps(Collection<WizardStep>)` accepts an ordered list of steps.
  - `setCurrentStepId(String)` / `setCurrentStepIndex(int)` update the active
    position.
  - `setCompletedColor`, `setCurrentColor`, and `setUpcomingColor` tune the
    default palette, while each `WizardStep` can override its completed color via
    `withCompletedColor(String)`.
  - `WizardStep.withIndicator(String)` optionally replaces the ordinal number in
    the circle with a custom label.
  - `WizardStep.clickable()` enables pointer and keyboard interaction for the
    step. When activated, the wizard fires `StepClickEvent`s and updates the
    current index.
  - `addCurrentStepChangeListener` publishes `CurrentStepChangeEvent`s whenever
    the selection changes, regardless of whether it was triggered by the user or
    programmatically.
  - `addStepClickListener` allows views to react to user interaction on
    clickable steps, e.g. to open supporting content or navigate elsewhere.
  - `advance()` / `retreat()` move the selection forwards or backwards.

### Usage example

```java
HorizontalWizard wizard = new HorizontalWizard();
wizard.setSteps(
        WizardStep.of("welcome", getTranslation("wizard.steps.welcome")),
        WizardStep.of("profile", getTranslation("wizard.steps.profile")),
        WizardStep.of("security", getTranslation("wizard.steps.security"))
                .withCompletedColor("var(--color-success-600)")
);
wizard.setCurrentStepId("profile");
wizard.setCurrentColor("var(--color-info-700)");
wizard.setUpcomingColor("var(--lumo-contrast-40pct)");
```

The CSS module keeps the layout horizontal across breakpoints, enabling
horizontal scrolling when the sequence exceeds the available width so the
structure never wraps into multiple rows.

When customising the palette, prefer the provided design tokens such as
`--color-info-600`, `--color-info-700`, and `--color-success-600`. These shades
are available in both light and dark themes and pair with the wizard's
inverse-text fallback so the active indicator always remains legible.

## Sample view

- **Route:** `/wizard`
- **Location:** `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/WizardView.java`
- **Menu entry:** "Wizard" under the *Other* section (`menu/navigation-menu.json`).

`WizardView` renders three cards:

1. An onboarding flow that uses default colors to illustrate the base look.
2. A release checklist that mixes clickable and read-only steps. The wizard
   still overrides the default colors, and an explanatory caption beneath the
   component updates in real time via `addCurrentStepChangeListener` so QA can
   see how views react to selection changes.
3. An extended product delivery roadmap with eight steps so reviewers can
   confirm horizontal scrolling, spacing, and the reinforced highlight of the
   active stage. Additional padding keeps the accent halo visible, and the wizard
   automatically scrolls the active step into view on load so long flows remain
   easy to interpret.

All cards update their copy and step labels on locale change. The showcase
page is linked from the documentation navigation and is described here so QA and
contributors can quickly verify the component in action.
