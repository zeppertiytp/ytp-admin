package com.youtopin.vaadin.samples.ui.view;

import com.youtopin.vaadin.component.HorizontalWizard;
import com.youtopin.vaadin.component.HorizontalWizard.WizardStep;
import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.formengine.wizard.WizardFormFlowCoordinator;
import com.youtopin.vaadin.i18n.TranslationProvider;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Minimal sample showcasing {@link WizardFormFlowCoordinator} with a workspace provisioning flow.
 */
@Route(value = "forms/wizard-coordinator", layout = MainLayout.class)
public class WizardCoordinatorSampleView extends AppPageLayout implements LocaleChangeObserver {

    private static final String STEP_DETAILS = "workspace-details";
    private static final String STEP_ROLLOUT = "workspace-rollout";

    private final FormEngine formEngine;
    private final TranslationProvider translationProvider;

    private final WorkspaceContext context = new WorkspaceContext();
    private final HorizontalWizard wizard = new HorizontalWizard();
    private final WizardFormFlowCoordinator<WorkspaceContext> coordinator;

    private final Map<String, String> stepLabelKeys = Map.of(
            STEP_DETAILS, "wizardcoord.step.details.label",
            STEP_ROLLOUT, "wizardcoord.step.rollout.label"
    );
    private final Map<String, String> stepTitleKeys = Map.of(
            STEP_DETAILS, "wizardcoord.step.details.title",
            STEP_ROLLOUT, "wizardcoord.step.rollout.title"
    );
    private final Map<String, String> stepDescriptionKeys = Map.of(
            STEP_DETAILS, "wizardcoord.step.details.description",
            STEP_ROLLOUT, "wizardcoord.step.rollout.description"
    );

    private final H1 pageTitle;
    private final Paragraph pageSubtitle = new Paragraph();
    private final H2 stepHeading = new H2();
    private final Paragraph stepDescription = new Paragraph();
    private final Paragraph workspaceIdBadge = new Paragraph();
    private final H2 summaryHeading = new H2();
    private final Paragraph rolloutSummary = new Paragraph();
    private final VerticalLayout formCard;
    private Component activeFormLayout;

    @Autowired
    public WizardCoordinatorSampleView(FormEngine formEngine,
                                       TranslationProvider translationProvider) {
        this.formEngine = formEngine;
        this.translationProvider = translationProvider;
        this.coordinator = new WizardFormFlowCoordinator<>(wizard,
                List.of(STEP_DETAILS, STEP_ROLLOUT), context);

        wizard.setWidthFull();

        pageTitle = createPageTitle("");
        pageSubtitle.addClassName("page-subtitle");
        VerticalLayout wizardCard = createCard(pageSubtitle, wizard);
        wizardCard.addClassName("stack-lg");
        add(wizardCard);

        stepHeading.addClassNames("text-primary", "mt-0");
        stepDescription.addClassName("text-secondary");
        workspaceIdBadge.addClassNames("text-secondary", "font-monospace");

        formCard = createCard(stepHeading, stepDescription, workspaceIdBadge);
        formCard.addClassName("stack-lg");
        add(formCard);

        rolloutSummary.addClassNames("text-secondary");
        VerticalLayout summaryCard = createCard(summaryHeading, rolloutSummary);
        summaryCard.getStyle().set("background-color", "var(--lumo-base-color)");
        summaryCard.addClassName("stack-md");
        add(summaryCard);

        rebuildForms();
        configureCallbacks();
        updateTexts();
        coordinator.configureWizard(this::buildWizardStep);
        showStep(STEP_DETAILS);
        updateSummary();
    }

    private void configureCallbacks() {
        coordinator.onStepSelection((stepId, state) -> {
            showStep(stepId);
            updateSummary();
        });
        coordinator.onStepCompletion((stepId, state) -> updateSummary());
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

        RenderedForm<WorkspaceDetails> detailsForm = renderForm(WorkspaceDetailsForm.class, context::getDetails);
        coordinator.registerStepForm(STEP_DETAILS, detailsForm, WorkspaceContext::getDetails);
        configureDetailsForm(detailsForm);

        RenderedForm<WorkspaceRollout> rolloutForm = renderForm(WorkspaceRolloutForm.class, context::getRollout);
        coordinator.registerStepForm(STEP_ROLLOUT, rolloutForm, WorkspaceContext::getRollout);
        configureRolloutForm(rolloutForm);
    }

    private <T> RenderedForm<T> renderForm(Class<?> definitionClass, java.util.function.Supplier<T> beanSupplier) {
        Locale locale = getLocale();
        RenderedForm<T> rendered = formEngine.render(definitionClass, translationProvider, locale, isRtl(locale));
        rendered.getFields().forEach((FieldDefinition definition, FieldInstance instance) ->
                rendered.getOrchestrator().bindField(instance, definition));
        T bean = beanSupplier.get();
        rendered.initializeWithBean(bean);
        return rendered;
    }

    private void configureDetailsForm(RenderedForm<WorkspaceDetails> form) {
        form.addActionHandler("workspace-details-next", context -> {
            ensureWorkspaceId();
            coordinator.markStepCompleted(STEP_DETAILS);
            coordinator.nextStep(STEP_DETAILS).ifPresent(coordinator::setCurrentStepId);
        });
    }

    private void configureRolloutForm(RenderedForm<WorkspaceRollout> form) {
        Button backButton = form.getActionButtons().get("workspace-rollout-back");
        if (backButton != null) {
            backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            backButton.addClickListener(event -> coordinator.previousStep(STEP_ROLLOUT)
                    .ifPresent(coordinator::setCurrentStepId));
        }
        form.addActionHandler("workspace-rollout-finish", actionContext -> {
            coordinator.markStepCompleted(STEP_ROLLOUT);
            Notification notification = Notification.show(getTranslation("wizardcoord.finish.success", context.getWorkspaceId()));
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        form.addValidationFailureListener((action, exception) ->
                Notification.show(getTranslation("wizardcoord.validation.failed"))
                        .addThemeVariants(NotificationVariant.LUMO_ERROR));
    }

    private void ensureWorkspaceId() {
        if (context.getWorkspaceId().isBlank()) {
            context.setWorkspaceId("WS-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT));
        }
        updateWorkspaceIdBadge();
    }

    private void showStep(String stepId) {
        RenderedForm<?> form = coordinator.getForms().get(stepId);
        stepHeading.setText(getTranslation(stepTitleKeys.get(stepId)));
        stepDescription.setText(getTranslation(stepDescriptionKeys.get(stepId)));
        updateWorkspaceIdBadge();
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

    private void updateSummary() {
        WorkspaceDetails details = context.getDetails();
        WorkspaceRollout rollout = context.getRollout();
        String summary = getTranslation("wizardcoord.summary.template",
                context.getWorkspaceId().isBlank() ? getTranslation("wizardcoord.summary.pendingId") : context.getWorkspaceId(),
                details.getName(),
                rollout.getChannel(),
                rollout.getSupportContact());
        rolloutSummary.setText(summary);
    }

    private void updateWorkspaceIdBadge() {
        if (context.getWorkspaceId().isBlank()) {
            workspaceIdBadge.setText(getTranslation("wizardcoord.workspaceId.pending"));
        } else {
            workspaceIdBadge.setText(getTranslation("wizardcoord.workspaceId.assigned", context.getWorkspaceId()));
        }
    }

    private void updateTexts() {
        String title = getTranslation("wizardcoord.title");
        pageTitle.setText(title);
        pageSubtitle.setText(getTranslation("wizardcoord.description"));
        summaryHeading.setText(getTranslation("wizardcoord.summary.title"));
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(title);
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        rebuildForms();
        updateTexts();
        coordinator.refreshWizard();
        showStep(coordinator.getCurrentStepId());
        updateSummary();
    }

    private boolean isRtl(Locale locale) {
        return locale != null && "fa".equalsIgnoreCase(locale.getLanguage());
    }

    public static class WorkspaceContext implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final WorkspaceDetails details = new WorkspaceDetails();
        private final WorkspaceRollout rollout = new WorkspaceRollout();
        private String workspaceId = "";

        public WorkspaceDetails getDetails() {
            return details;
        }

        public WorkspaceRollout getRollout() {
            return rollout;
        }

        public String getWorkspaceId() {
            return workspaceId;
        }

        public void setWorkspaceId(String workspaceId) {
            this.workspaceId = workspaceId == null ? "" : workspaceId.trim();
        }
    }

    public static class WorkspaceDetails implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String name = "";
        private String ownerEmail = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name == null ? "" : name.trim();
        }

        public String getOwnerEmail() {
            return ownerEmail;
        }

        public void setOwnerEmail(String ownerEmail) {
            this.ownerEmail = ownerEmail == null ? "" : ownerEmail.trim();
        }
    }

    public static class WorkspaceRollout implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String channel = "";
        private String supportContact = "";

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel == null ? "" : channel.trim();
        }

        public String getSupportContact() {
            return supportContact;
        }

        public void setSupportContact(String supportContact) {
            this.supportContact = supportContact == null ? "" : supportContact.trim();
        }
    }

    @UiForm(
            id = STEP_DETAILS,
            titleKey = "wizardcoord.step.details.title",
            descriptionKey = "",
            bean = WorkspaceDetails.class,
            sections = DetailsSection.class,
            actions = {
                    @UiAction(id = "workspace-details-next", labelKey = "wizardcoord.action.next", placement = UiAction.Placement.FOOTER)
            }
    )
    public static class WorkspaceDetailsForm {
    }

    @UiSection(id = "details", titleKey = "wizardcoord.step.details.title", groups = DetailsGroup.class)
    public static class DetailsSection {
    }

    @UiGroup(id = "details-group", columns = 1)
    public static class DetailsGroup {

        @UiField(path = "name", component = UiField.ComponentType.TEXT, labelKey = "wizardcoord.fields.name")
        public void name() {
        }

        @UiField(path = "ownerEmail", component = UiField.ComponentType.EMAIL, labelKey = "wizardcoord.fields.ownerEmail")
        public void ownerEmail() {
        }
    }

    @UiForm(
            id = STEP_ROLLOUT,
            titleKey = "wizardcoord.step.rollout.title",
            descriptionKey = "",
            bean = WorkspaceRollout.class,
            sections = RolloutSection.class,
            actions = {
                    @UiAction(id = "workspace-rollout-back", labelKey = "wizardcoord.action.back", placement = UiAction.Placement.FOOTER, order = 0),
                    @UiAction(id = "workspace-rollout-finish", labelKey = "wizardcoord.action.finish", placement = UiAction.Placement.FOOTER, order = 1)
            }
    )
    public static class WorkspaceRolloutForm {
    }

    @UiSection(id = "rollout", titleKey = "wizardcoord.step.rollout.title", groups = RolloutGroup.class)
    public static class RolloutSection {
    }

    @UiGroup(id = "rollout-group", columns = 1)
    public static class RolloutGroup {

        @UiField(path = "channel", component = UiField.ComponentType.TEXT, labelKey = "wizardcoord.fields.channel")
        public void channel() {
        }

        @UiField(path = "supportContact", component = UiField.ComponentType.TEXT, labelKey = "wizardcoord.fields.support")
        public void supportContact() {
        }
    }
}
