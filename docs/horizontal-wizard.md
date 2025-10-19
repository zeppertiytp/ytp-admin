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

The CSS module keeps the layout horizontal on wide screens and stacks steps on
narrow viewports while hiding the connectors for clarity.

## Sample view

- **Route:** `/wizard`
- **Location:** `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/WizardView.java`
- **Menu entry:** "Wizard" under the *Other* section (`menu/navigation-menu.json`).

`WizardView` renders two cards:

1. An onboarding flow that uses default colors to illustrate the base look.
2. A release checklist where every completed step provides a distinct accent via
   `withCompletedColor`, while the wizard itself overrides the current and
   upcoming colors.

Both cards update their copy and step labels on locale change. The showcase
page is linked from the documentation navigation and is described here so QA and
contributors can quickly verify the component in action.
