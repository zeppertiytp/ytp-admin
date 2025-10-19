package com.youtopin.vaadin.samples.ui.view;

import com.youtopin.vaadin.component.HorizontalWizard;
import com.youtopin.vaadin.component.HorizontalWizard.WizardStep;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;

import java.util.List;

/**
 * Showcase view for the {@link HorizontalWizard} component. Demonstrates
 * default styling and per-step color overrides.
 */
@Route(value = "wizard", layout = MainLayout.class)
public class WizardView extends AppPageLayout implements LocaleChangeObserver {

    private final H1 pageTitle;
    private final H2 basicSampleTitle = new H2();
    private final Span basicSampleDescription = new Span();
    private final H2 colorSampleTitle = new H2();
    private final Span colorSampleDescription = new Span();
    private final Span colorSampleStatus = new Span();
    private final H2 longSampleTitle = new H2();
    private final Span longSampleDescription = new Span();
    private final HorizontalWizard onboardingWizard = new HorizontalWizard();
    private final HorizontalWizard releaseWizard = new HorizontalWizard();
    private final HorizontalWizard projectWizard = new HorizontalWizard();

    public WizardView() {
        pageTitle = createPageTitle(getTranslation("wizard.title"));

        basicSampleDescription.addClassName("page-subtitle");
        colorSampleDescription.addClassName("page-subtitle");
        colorSampleStatus.addClassName("page-subtitle");
        longSampleDescription.addClassName("page-subtitle");

        onboardingWizard.setWidthFull();
        releaseWizard.setWidthFull();
        projectWizard.setWidthFull();

        releaseWizard.setCompletedColor("var(--color-success-600)");
        releaseWizard.setCurrentColor("var(--color-info-700)");
        releaseWizard.setUpcomingColor("var(--lumo-contrast-40pct)");

        VerticalLayout basicCard = createCard();
        basicCard.addClassName("stack-lg");
        basicCard.add(basicSampleTitle, basicSampleDescription, onboardingWizard);

        VerticalLayout colorCard = createCard();
        colorCard.addClassName("stack-lg");
        colorCard.add(colorSampleTitle, colorSampleDescription, releaseWizard, colorSampleStatus);

        VerticalLayout longCard = createCard();
        longCard.addClassName("stack-lg");
        longCard.add(longSampleTitle, longSampleDescription, projectWizard);

        add(basicCard, colorCard, longCard);

        releaseWizard.addCurrentStepChangeListener(event -> updateReleaseStatus());

        updateContent();
        updatePageTitle();
    }

    private void updateContent() {
        basicSampleTitle.setText(getTranslation("wizard.sample.basic"));
        basicSampleDescription.setText(getTranslation("wizard.sample.basicDescription"));
        colorSampleTitle.setText(getTranslation("wizard.sample.customColors"));
        colorSampleDescription.setText(getTranslation("wizard.sample.customColorsDescription"));
        longSampleTitle.setText(getTranslation("wizard.sample.longFlow"));
        longSampleDescription.setText(getTranslation("wizard.sample.longFlowDescription"));

        onboardingWizard.setSteps(createOnboardingSteps());
        onboardingWizard.setCurrentStepId("profile");

        releaseWizard.setSteps(createReleaseSteps());
        releaseWizard.setCurrentStepId("configure");

        projectWizard.setSteps(createProjectSteps());
        projectWizard.setCurrentStepId("qa");

        updateReleaseStatus();
    }

    private void updatePageTitle() {
        UI ui = UI.getCurrent();
        String title = getTranslation("wizard.title");
        pageTitle.setText(title);
        if (ui != null) {
            ui.getPage().setTitle(title);
        }
    }

    private List<WizardStep> createOnboardingSteps() {
        return List.of(
                WizardStep.of("welcome", getTranslation("wizard.steps.welcome")),
                WizardStep.of("profile", getTranslation("wizard.steps.profile")),
                WizardStep.of("security", getTranslation("wizard.steps.security")),
                WizardStep.of("done", getTranslation("wizard.steps.complete"))
        );
    }

    private List<WizardStep> createReleaseSteps() {
        return List.of(
                WizardStep.of("plan", getTranslation("wizard.steps.plan"))
                        .withCompletedColor("var(--color-secondary-600)")
                        .clickable(),
                WizardStep.of("prepare", getTranslation("wizard.steps.prepare"))
                        .withCompletedColor("var(--color-info-500)")
                        .clickable(),
                WizardStep.of("configure", getTranslation("wizard.steps.configure"))
                        .withCompletedColor("var(--color-warning-500)")
                        .withClickable(false),
                WizardStep.of("launch", getTranslation("wizard.steps.launch"))
                        .withCompletedColor("var(--color-success-600)")
                        .withClickable(false)
        );
    }

    private List<WizardStep> createProjectSteps() {
        return List.of(
                WizardStep.of("brief", getTranslation("wizard.steps.brief")),
                WizardStep.of("research", getTranslation("wizard.steps.research")),
                WizardStep.of("design", getTranslation("wizard.steps.design")),
                WizardStep.of("development", getTranslation("wizard.steps.development")),
                WizardStep.of("qa", getTranslation("wizard.steps.qa")),
                WizardStep.of("localization", getTranslation("wizard.steps.localization")),
                WizardStep.of("launch", getTranslation("wizard.steps.launch"))
                        .clickable(),
                WizardStep.of("measure", getTranslation("wizard.steps.measure"))
        );
    }

    private void updateReleaseStatus() {
        colorSampleStatus.setText(createReleaseStatusMessage());
    }

    private String createReleaseStatusMessage() {
        return getTranslation("wizard.sample.currentPhase", releaseWizard.getCurrentStepId()
                .map(this::translatePhaseLabel)
                .orElse(""));
    }

    private String translatePhaseLabel(String stepId) {
        return switch (stepId) {
            case "plan" -> getTranslation("wizard.steps.plan");
            case "prepare" -> getTranslation("wizard.steps.prepare");
            case "configure" -> getTranslation("wizard.steps.configure");
            case "launch" -> getTranslation("wizard.steps.launch");
            default -> stepId;
        };
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateContent();
        updatePageTitle();
    }
}
