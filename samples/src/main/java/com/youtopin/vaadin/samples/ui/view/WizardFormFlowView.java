package com.youtopin.vaadin.samples.ui.view;

import com.youtopin.vaadin.component.HorizontalWizard;
import com.youtopin.vaadin.component.HorizontalWizard.WizardStep;
import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

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
    private final Map<String, RenderedForm<?>> forms = new LinkedHashMap<>();

    private final H1 pageTitle;
    private final Paragraph pageSubtitle = new Paragraph();
    private final H2 stepHeading = new H2();
    private final Paragraph stepDescription = new Paragraph();
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

        pageTitle = createPageTitle("");
        pageSubtitle.addClassName("page-subtitle");

        wizard.setWidthFull();

        VerticalLayout wizardCard = createCard(pageSubtitle, wizard);
        wizardCard.addClassName("stack-lg");
        add(wizardCard);

        stepHeading.addClassNames("text-primary", "mt-0");
        stepDescription.addClassName("text-secondary");

        formCard = createCard(stepHeading, stepDescription);
        formCard.addClassName("stack-lg");
        add(formCard);

        wizard.addCurrentStepChangeListener(event -> {
            String stepId = event.getCurrentStep().getId();
            wizardState.setCurrentStepId(stepId);
            wizardService.store(wizardState);
            showStep(stepId);
        });
        wizard.addStepClickListener(event -> {
            String stepId = event.getStep().getId();
            if (isStepAccessible(stepId)) {
                wizard.setCurrentStepId(stepId);
            }
        });

        rebuildForms();
        updateTexts();
        wizard.setSteps(buildWizardSteps());

        String initialStep = determineInitialStep();
        wizard.setCurrentStepId(initialStep);
    }

    private void rebuildForms() {
        if (activeFormLayout != null) {
            formCard.remove(activeFormLayout);
            activeFormLayout = null;
        }
        forms.clear();

        RenderedForm<ProjectLaunchBasicsFormData> basicsForm = renderForm(
                ProjectLaunchBasicsFormDefinition.class,
                wizardState::getBasics
        );
        configureBasicsForm(basicsForm);
        forms.put(ProjectLaunchWizardState.STEP_BASICS, basicsForm);

        RenderedForm<ProjectLaunchTeamFormData> teamForm = renderForm(
                ProjectLaunchTeamFormDefinition.class,
                wizardState::getTeam
        );
        configureTeamForm(teamForm);
        forms.put(ProjectLaunchWizardState.STEP_TEAM, teamForm);

        RenderedForm<ProjectLaunchChecklistFormData> checklistForm = renderForm(
                ProjectLaunchChecklistFormDefinition.class,
                wizardState::getChecklist
        );
        configureChecklistForm(checklistForm);
        forms.put(ProjectLaunchWizardState.STEP_CHECKLIST, checklistForm);
    }

    private List<WizardStep> buildWizardSteps() {
        return stepOrder.stream()
                .map(step -> WizardStep.of(step, getTranslation(stepLabelKeys.get(step))))
                .toList();
    }

    private <T> RenderedForm<T> renderForm(Class<?> definitionClass, Supplier<T> beanSupplier) {
        Locale locale = getLocale();
        boolean rtl = isRtl(locale);
        RenderedForm<T> rendered = formEngine.render(definitionClass, translationProvider, locale, rtl);
        rendered.getFields().forEach((FieldDefinition definition, FieldInstance instance) ->
                rendered.getOrchestrator().bindField(instance, definition));
        T bean = beanSupplier.get();
        rendered.initializeWithBean(bean);
        rendered.setActionBeanSupplier(beanSupplier);
        return rendered;
    }

    private void configureBasicsForm(RenderedForm<ProjectLaunchBasicsFormData> form) {
        form.addActionHandler("project-basics-next", context -> {
            wizardState.markStepCompleted(ProjectLaunchWizardState.STEP_BASICS);
            wizardService.store(wizardState);
            navigateTo(nextStep(ProjectLaunchWizardState.STEP_BASICS)
                    .orElse(ProjectLaunchWizardState.STEP_BASICS));
        });
    }

    private void configureTeamForm(RenderedForm<ProjectLaunchTeamFormData> form) {
        Button backButton = form.getActionButtons().get("project-team-back");
        if (backButton != null) {
            backButton.addClickListener(event -> navigateTo(previousStep(ProjectLaunchWizardState.STEP_TEAM)
                    .orElse(ProjectLaunchWizardState.STEP_BASICS)));
        }
        form.addActionHandler("project-team-next", context -> {
            wizardState.markStepCompleted(ProjectLaunchWizardState.STEP_TEAM);
            wizardService.store(wizardState);
            navigateTo(nextStep(ProjectLaunchWizardState.STEP_TEAM)
                    .orElse(ProjectLaunchWizardState.STEP_TEAM));
        });
    }

    private void configureChecklistForm(RenderedForm<ProjectLaunchChecklistFormData> form) {
        Button backButton = form.getActionButtons().get("project-launch-back");
        if (backButton != null) {
            backButton.addClickListener(event -> navigateTo(previousStep(ProjectLaunchWizardState.STEP_CHECKLIST)
                    .orElse(ProjectLaunchWizardState.STEP_TEAM)));
        }
        form.addActionHandler("project-launch-finish", context -> {
            wizardState.markStepCompleted(ProjectLaunchWizardState.STEP_CHECKLIST);
            wizardService.store(wizardState);
            Notification notification = Notification.show(getTranslation("wizardform.finish.success"));
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
    }

    private void navigateTo(String stepId) {
        if (stepId != null && stepOrder.contains(stepId)) {
            wizard.setCurrentStepId(stepId);
        }
    }

    private Optional<String> nextStep(String current) {
        int index = stepOrder.indexOf(current);
        if (index >= 0 && index < stepOrder.size() - 1) {
            return Optional.of(stepOrder.get(index + 1));
        }
        return Optional.empty();
    }

    private Optional<String> previousStep(String current) {
        int index = stepOrder.indexOf(current);
        if (index > 0) {
            return Optional.of(stepOrder.get(index - 1));
        }
        return Optional.empty();
    }

    private boolean isStepAccessible(String stepId) {
        int requestedIndex = stepOrder.indexOf(stepId);
        if (requestedIndex < 0) {
            return false;
        }
        int resumeIndex = computeResumeIndex();
        return requestedIndex <= resumeIndex;
    }

    private int computeResumeIndex() {
        for (int i = 0; i < stepOrder.size(); i++) {
            String stepId = stepOrder.get(i);
            if (!wizardState.isStepCompleted(stepId)) {
                return i;
            }
        }
        return stepOrder.size() - 1;
    }

    private String determineInitialStep() {
        for (String stepId : stepOrder) {
            if (!wizardState.isStepCompleted(stepId)) {
                wizardState.setCurrentStepId(stepId);
                wizardService.store(wizardState);
                return stepId;
            }
        }
        String stored = wizardState.getCurrentStepId();
        if (stored != null && stepOrder.contains(stored)) {
            return stored;
        }
        String last = stepOrder.get(stepOrder.size() - 1);
        wizardState.setCurrentStepId(last);
        wizardService.store(wizardState);
        return last;
    }

    private void showStep(String stepId) {
        RenderedForm<?> form = forms.get(stepId);
        stepHeading.setText(getTranslation(stepTitleKeys.get(stepId)));
        stepDescription.setText(getTranslation(stepDescriptionKeys.get(stepId)));
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

    @Override
    public void localeChange(LocaleChangeEvent event) {
        rebuildForms();
        updateTexts();
        String currentStep = wizard.getCurrentStepId().orElse(determineInitialStep());
        wizard.setSteps(buildWizardSteps());
        wizard.setCurrentStepId(currentStep);
    }

    private boolean isRtl(Locale locale) {
        return locale != null && "fa".equalsIgnoreCase(locale.getLanguage());
    }
}
