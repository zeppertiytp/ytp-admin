package com.youtopin.vaadin.samples.ui.view;

import com.youtopin.vaadin.component.HorizontalWizard;
import com.youtopin.vaadin.component.HorizontalWizard.WizardStep;
import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.formengine.wizard.WizardFormFlowCoordinator;
import com.youtopin.vaadin.i18n.TranslationProvider;
import com.youtopin.vaadin.samples.application.wizard.ProjectLaunchWizardService;
import com.youtopin.vaadin.samples.application.wizard.ProjectLaunchWizardState;
import com.youtopin.vaadin.samples.ui.formengine.definition.ProjectLaunchBasicsFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.ProjectLaunchChecklistFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.ProjectLaunchTeamFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.model.ProjectLaunchBasicsFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.ProjectLaunchChecklistFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.ProjectLaunchTeamFormData;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Demonstrates how the horizontal wizard component can orchestrate multiple form engine steps
 * while persisting the collected data inside the Vaadin session.
 */
@Route(value = "forms/wizard-flow", layout = MainLayout.class)
public class WizardFormFlowView extends AppPageLayout implements LocaleChangeObserver {

    private final FormEngine formEngine;
    private final TranslationProvider translationProvider;
    private final ProjectLaunchWizardService wizardService;
    private final ProjectLaunchWizardState wizardState;

    private final HorizontalWizard wizard = new HorizontalWizard();
    private final WizardFormFlowCoordinator<ProjectLaunchWizardState> coordinator;

    private final List<String> stepOrder = List.of(
            ProjectLaunchWizardState.STEP_BASICS,
            ProjectLaunchWizardState.STEP_TEAM,
            ProjectLaunchWizardState.STEP_CHECKLIST
    );
    private final Map<String, String> stepLabelKeys = Map.of(
            ProjectLaunchWizardState.STEP_BASICS, "wizardform.step.basics.label",
            ProjectLaunchWizardState.STEP_TEAM, "wizardform.step.team.label",
            ProjectLaunchWizardState.STEP_CHECKLIST, "wizardform.step.launch.label"
    );
    private final Map<String, String> stepTitleKeys = Map.of(
            ProjectLaunchWizardState.STEP_BASICS, "wizardform.step.basics.title",
            ProjectLaunchWizardState.STEP_TEAM, "wizardform.step.team.title",
            ProjectLaunchWizardState.STEP_CHECKLIST, "wizardform.step.launch.title"
    );
    private final Map<String, String> stepDescriptionKeys = Map.of(
            ProjectLaunchWizardState.STEP_BASICS, "wizardform.step.basics.description",
            ProjectLaunchWizardState.STEP_TEAM, "wizardform.step.team.description",
            ProjectLaunchWizardState.STEP_CHECKLIST, "wizardform.step.launch.description"
    );

    private final H1 pageTitle;
    private final Paragraph pageSubtitle = new Paragraph();
    private final H2 stepHeading = new H2();
    private final Paragraph stepDescription = new Paragraph();
    private final Paragraph projectIdBadge = new Paragraph();
    private final VerticalLayout formCard;
    private Component activeFormLayout;

    @Autowired
    public WizardFormFlowView(FormEngine formEngine,
                              TranslationProvider translationProvider,
                              ProjectLaunchWizardService wizardService) {
        this.formEngine = formEngine;
        this.translationProvider = translationProvider;
        this.wizardService = wizardService;
        this.wizardState = wizardService.load();

        wizard.setWidthFull();
        coordinator = new WizardFormFlowCoordinator<>(wizard, stepOrder, wizardState);

        pageTitle = createPageTitle("");
        pageSubtitle.addClassName("page-subtitle");

        VerticalLayout wizardCard = createCard(pageSubtitle, wizard);
        wizardCard.addClassName("stack-lg");
        add(wizardCard);

        stepHeading.addClassNames("text-primary", "mt-0");
        stepDescription.addClassName("text-secondary");
        projectIdBadge.addClassNames("text-secondary", "font-monospace");

        formCard = createCard(stepHeading, stepDescription, projectIdBadge);
        formCard.addClassName("stack-lg");
        add(formCard);

        rebuildForms();
        configureCallbacks();
        coordinator.setCompletedSteps(wizardState.getCompletedSteps());

        String initialStep = wizardState.getCurrentStepId();
        if (initialStep == null || !stepOrder.contains(initialStep)) {
            initialStep = coordinator.determineInitialStep();
        }
        coordinator.setCurrentStepId(initialStep);

        updateTexts();
        coordinator.configureWizard(this::buildWizardStep);
        showStep(initialStep);
    }

    private void configureCallbacks() {
        coordinator.onStepSelection((stepId, state) -> {
            state.setCurrentStepId(stepId);
            wizardService.store(state);
            showStep(stepId);
        });
        coordinator.onStepCompletion((stepId, state) -> wizardService.store(state));
    }

    private WizardStep buildWizardStep(String stepId) {
        return WizardStep.of(stepId, getTranslation(stepLabelKeys.get(stepId)));
    }

    private void rebuildForms() {
        if (activeFormLayout != null) {
            formCard.remove(activeFormLayout);
            activeFormLayout = null;
        }
        coordinator.clearForms();

        RenderedForm<ProjectLaunchBasicsFormData> basicsForm = renderForm(
                ProjectLaunchBasicsFormDefinition.class,
                ProjectLaunchWizardState::getBasics
        );
        coordinator.registerStepForm(ProjectLaunchWizardState.STEP_BASICS,
                basicsForm, ProjectLaunchWizardState::getBasics);
        configureBasicsForm(basicsForm);

        RenderedForm<ProjectLaunchTeamFormData> teamForm = renderForm(
                ProjectLaunchTeamFormDefinition.class,
                ProjectLaunchWizardState::getTeam
        );
        coordinator.registerStepForm(ProjectLaunchWizardState.STEP_TEAM,
                teamForm, ProjectLaunchWizardState::getTeam);
        configureTeamForm(teamForm);

        RenderedForm<ProjectLaunchChecklistFormData> checklistForm = renderForm(
                ProjectLaunchChecklistFormDefinition.class,
                ProjectLaunchWizardState::getChecklist
        );
        coordinator.registerStepForm(ProjectLaunchWizardState.STEP_CHECKLIST,
                checklistForm, ProjectLaunchWizardState::getChecklist);
        configureChecklistForm(checklistForm);
    }

    private <T> RenderedForm<T> renderForm(Class<?> definitionClass,
                                           Function<ProjectLaunchWizardState, T> beanResolver) {
        Locale locale = getLocale();
        boolean rtl = isRtl(locale);
        RenderedForm<T> rendered = formEngine.render(definitionClass, translationProvider, locale, rtl);
        rendered.getFields().forEach((FieldDefinition definition, FieldInstance instance) ->
                rendered.getOrchestrator().bindField(instance, definition));
        T bean = beanResolver.apply(wizardState);
        rendered.initializeWithBean(bean);
        return rendered;
    }

    private void configureBasicsForm(RenderedForm<ProjectLaunchBasicsFormData> form) {
        form.addActionHandler("project-basics-next", context -> {
            ensureProjectId();
            coordinator.markStepCompleted(ProjectLaunchWizardState.STEP_BASICS);
            coordinator.nextStep(ProjectLaunchWizardState.STEP_BASICS)
                    .ifPresent(coordinator::setCurrentStepId);
        });
    }

    private void configureTeamForm(RenderedForm<ProjectLaunchTeamFormData> form) {
        Button backButton = form.getActionButtons().get("project-team-back");
        if (backButton != null) {
            backButton.addClickListener(event -> coordinator.previousStep(ProjectLaunchWizardState.STEP_TEAM)
                    .ifPresent(coordinator::setCurrentStepId));
        }
        form.addActionHandler("project-team-next", context -> {
            coordinator.markStepCompleted(ProjectLaunchWizardState.STEP_TEAM);
            coordinator.nextStep(ProjectLaunchWizardState.STEP_TEAM)
                    .ifPresent(coordinator::setCurrentStepId);
        });
    }

    private void configureChecklistForm(RenderedForm<ProjectLaunchChecklistFormData> form) {
        Button backButton = form.getActionButtons().get("project-launch-back");
        if (backButton != null) {
            backButton.addClickListener(event -> coordinator.previousStep(ProjectLaunchWizardState.STEP_CHECKLIST)
                    .ifPresent(coordinator::setCurrentStepId));
        }
        form.addActionHandler("project-launch-finish", context -> {
            coordinator.markStepCompleted(ProjectLaunchWizardState.STEP_CHECKLIST);
            String projectId = wizardState.getProjectId();
            String message = projectId == null || projectId.isBlank()
                    ? getTranslation("wizardform.finish.success.noid")
                    : getTranslation("wizardform.finish.success", projectId);
            Notification notification = Notification.show(message);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
    }

    private void ensureProjectId() {
        if (wizardState.getProjectId() == null || wizardState.getProjectId().isBlank()) {
            String generated = "PRJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            wizardState.setProjectId(generated);
        }
        updateProjectIdBadge();
    }

    private void showStep(String stepId) {
        RenderedForm<?> form = coordinator.getForms().get(stepId);
        stepHeading.setText(getTranslation(stepTitleKeys.get(stepId)));
        stepDescription.setText(getTranslation(stepDescriptionKeys.get(stepId)));
        updateProjectIdBadge();
        if (activeFormLayout != null) {
            formCard.remove(activeFormLayout);
            activeFormLayout = null;
        }
        if (form != null) {
            Component layout = form.getLayout();
            layout.getElement().getStyle().set("width", "100%");
            if (layout instanceof HasSize sized) {
                sized.setWidth("100%");
            }
            activeFormLayout = layout;
            formCard.add(layout);
        }
    }

    private void updateTexts() {
        String title = getTranslation("wizardform.title");
        pageTitle.setText(title);
        pageSubtitle.setText(getTranslation("wizardform.description"));
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(title);
        }
    }

    private void updateProjectIdBadge() {
        String projectId = wizardState.getProjectId();
        if (projectId == null || projectId.isBlank()) {
            projectIdBadge.setText(getTranslation("wizardform.projectId.pending"));
        } else {
            projectIdBadge.setText(getTranslation("wizardform.projectId.assigned", projectId));
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        rebuildForms();
        updateTexts();
        coordinator.refreshWizard();
        showStep(coordinator.getCurrentStepId());
    }

    private boolean isRtl(Locale locale) {
        return locale != null && "fa".equalsIgnoreCase(locale.getLanguage());
    }
}
