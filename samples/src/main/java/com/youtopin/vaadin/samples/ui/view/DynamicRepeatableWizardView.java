package com.youtopin.vaadin.samples.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.youtopin.vaadin.component.HorizontalWizard;
import com.youtopin.vaadin.component.HorizontalWizard.WizardStep;
import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.FormDefinition;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.formengine.wizard.WizardFormFlowCoordinator;
import com.youtopin.vaadin.i18n.TranslationProvider;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.DynamicRepeatableWizardService;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.DynamicRepeatableWizardState;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicEntryCollectionFormData;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicEntryRow;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicFieldBlueprint;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicFieldSchema;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicFieldSchemaFormData;
import com.youtopin.vaadin.samples.ui.formengine.definition.dynamic.DynamicFieldSchemaFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.dynamic.DynamicEntryFormFactory;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Sample wizard showing how the first step can reshape a repeatable editor rendered in the second step.
 */
@Route(value = "forms/dynamic-repeatable", layout = MainLayout.class)
@PageTitle("Dynamic Repeatable Wizard")
public class DynamicRepeatableWizardView extends AppPageLayout implements LocaleChangeObserver {

    private final FormEngine formEngine;
    private final TranslationProvider translationProvider;
    private final DynamicRepeatableWizardService wizardService;
    private final DynamicRepeatableWizardState wizardState;

    private final HorizontalWizard wizard = new HorizontalWizard();
    private final WizardFormFlowCoordinator<DynamicRepeatableWizardState> coordinator;

    private final List<String> stepOrder = List.of(
            DynamicRepeatableWizardState.STEP_SCHEMA,
            DynamicRepeatableWizardState.STEP_ENTRIES
    );
    private final Map<String, String> stepLabelKeys = Map.of(
            DynamicRepeatableWizardState.STEP_SCHEMA, "dynamicwizard.step.schema.label",
            DynamicRepeatableWizardState.STEP_ENTRIES, "dynamicwizard.step.entries.label"
    );
    private final Map<String, String> stepTitleKeys = Map.of(
            DynamicRepeatableWizardState.STEP_SCHEMA, "dynamicwizard.step.schema.title",
            DynamicRepeatableWizardState.STEP_ENTRIES, "dynamicwizard.step.entries.title"
    );
    private final Map<String, String> stepDescriptionKeys = Map.of(
            DynamicRepeatableWizardState.STEP_SCHEMA, "dynamicwizard.step.schema.description",
            DynamicRepeatableWizardState.STEP_ENTRIES, "dynamicwizard.step.entries.description"
    );

    private final H1 pageTitle;
    private final Paragraph pageSubtitle = new Paragraph();
    private final H2 stepHeading = new H2();
    private final Paragraph stepDescription = new Paragraph();
    private final VerticalLayout formCard;
    private Component activeFormLayout;

    private RenderedForm<DynamicFieldSchemaFormData> schemaForm;
    private RenderedForm<DynamicEntryCollectionFormData> entryForm;

    public DynamicRepeatableWizardView(FormEngine formEngine,
                                       TranslationProvider translationProvider,
                                       DynamicRepeatableWizardService wizardService) {
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
        stepDescription.addClassNames("text-secondary");

        formCard = createCard(stepHeading, stepDescription);
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

    private void rebuildForms() {
        if (activeFormLayout != null) {
            formCard.remove(activeFormLayout);
            activeFormLayout = null;
        }
        coordinator.clearForms();

        schemaForm = renderForm(DynamicFieldSchemaFormDefinition.class, DynamicRepeatableWizardState::getSchema);
        coordinator.registerStepForm(DynamicRepeatableWizardState.STEP_SCHEMA, schemaForm,
                DynamicRepeatableWizardState::getSchema);
        configureSchemaForm(schemaForm);

        rebuildEntryForm();
    }

    private void rebuildEntryForm() {
        FormDefinition definition = DynamicEntryFormFactory.create(wizardState.getSchema());
        entryForm = renderForm(definition, DynamicRepeatableWizardState::getEntries);
        coordinator.registerStepForm(DynamicRepeatableWizardState.STEP_ENTRIES, entryForm,
                DynamicRepeatableWizardState::getEntries);
        entryForm.setRepeatableEntryCount(DynamicEntryFormFactory.GROUP_ID, wizardState.getSchema().getEntryCount());
        configureEntryForm(entryForm);
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

    private <T> RenderedForm<T> renderForm(Class<?> definitionClass,
                                           Function<DynamicRepeatableWizardState, T> beanResolver) {
        Locale locale = getLocale();
        RenderedForm<T> rendered = formEngine.render(definitionClass, translationProvider, locale, isRtl(locale));
        rendered.getFields().forEach((FieldDefinition definition, FieldInstance instance) ->
                rendered.getOrchestrator().bindField(instance, definition));
        T bean = beanResolver.apply(wizardState);
        rendered.initializeWithBean(bean);
        return rendered;
    }

    private <T> RenderedForm<T> renderForm(FormDefinition definition,
                                           Function<DynamicRepeatableWizardState, T> beanResolver) {
        Locale locale = getLocale();
        RenderedForm<T> rendered = formEngine.render(definition, translationProvider, locale, isRtl(locale));
        rendered.getFields().forEach((FieldDefinition fieldDefinition, FieldInstance instance) ->
                rendered.getOrchestrator().bindField(instance, fieldDefinition));
        T bean = beanResolver.apply(wizardState);
        rendered.initializeWithBean(bean);
        return rendered;
    }

    private void configureSchemaForm(RenderedForm<DynamicFieldSchemaFormData> form) {
        form.addActionHandler("dynamic-schema-next", context -> {
            wizardState.getSchema().sanitiseFieldKeys();
            wizardState.syncEntriesWithSchema();
            rebuildEntryForm();
            wizardState.markStepCompleted(DynamicRepeatableWizardState.STEP_SCHEMA);
            coordinator.markStepCompleted(DynamicRepeatableWizardState.STEP_SCHEMA);
            coordinator.nextStep(DynamicRepeatableWizardState.STEP_SCHEMA)
                    .ifPresent(coordinator::setCurrentStepId);
        });
    }

    private void configureEntryForm(RenderedForm<DynamicEntryCollectionFormData> form) {
        Button backButton = form.getActionButtons().get("dynamic-entries-back");
        if (backButton != null) {
            backButton.addClickListener(event -> coordinator.previousStep(DynamicRepeatableWizardState.STEP_ENTRIES)
                    .ifPresent(coordinator::setCurrentStepId));
        }
        form.addActionHandler("dynamic-entries-finish", context -> {
            DynamicEntryCollectionFormData data = wizardState.getEntries();
            List<String> errors = validateEntries(data);
            if (!errors.isEmpty()) {
                Notification notification = Notification.show(errors.get(0));
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setDuration(4000);
                return;
            }
            wizardState.markStepCompleted(DynamicRepeatableWizardState.STEP_ENTRIES);
            coordinator.markStepCompleted(DynamicRepeatableWizardState.STEP_ENTRIES);
            wizardService.store(wizardState);
            Notification notification = Notification.show(getTranslation("dynamicwizard.notification.saved"));
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3500);
        });
    }

    private List<String> validateEntries(DynamicEntryCollectionFormData data) {
        DynamicFieldSchema schema = data.getSchema();
        List<DynamicFieldBlueprint> fields = schema.getFieldSnapshot();
        if (fields.isEmpty()) {
            return List.of();
        }
        String requiredMessage = getTranslation("forms.validation.required");
        for (int i = 0; i < data.getEntries().size(); i++) {
            DynamicEntryRow row = data.getEntries().get(i);
            String indexLabel = getTranslation("dynamicwizard.validation.entryIndex", i + 1);
            for (DynamicFieldBlueprint blueprint : fields) {
                if (!blueprint.isRequired()) {
                    continue;
                }
                Object value = row.getValues().get(blueprint.getFieldKey());
                if (value == null || (value instanceof String str && str.isBlank())) {
                    String fieldLabel = blueprint.getLabel().isBlank()
                            ? blueprint.getFieldKey()
                            : blueprint.getLabel();
                    String message = getTranslation("dynamicwizard.validation.requiredField",
                            indexLabel, fieldLabel, requiredMessage);
                    return List.of(message);
                }
            }
        }
        return List.of();
    }

    private void showStep(String stepId) {
        RenderedForm<?> form = coordinator.getForms().get(stepId);
        if (form == null) {
            return;
        }
        Component layout = form.getLayout();
        if (!Objects.equals(activeFormLayout, layout)) {
            if (activeFormLayout != null) {
                formCard.remove(activeFormLayout);
            }
            activeFormLayout = layout;
            formCard.add(activeFormLayout);
        }
        stepHeading.setText(getTranslation(stepTitleKeys.get(stepId)));
        stepDescription.setText(getTranslation(stepDescriptionKeys.get(stepId)));
    }

    private boolean isRtl(Locale locale) {
        return locale != null && "fa".equalsIgnoreCase(locale.getLanguage());
    }

    private void updateTexts() {
        pageTitle.setText(getTranslation("dynamicwizard.page.title"));
        pageSubtitle.setText(getTranslation("dynamicwizard.page.subtitle"));
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        rebuildForms();
        coordinator.setCompletedSteps(wizardState.getCompletedSteps());
        updateTexts();
        coordinator.configureWizard(this::buildWizardStep);
        showStep(coordinator.getCurrentStepId());
    }
}
