package com.youtopin.vaadin.samples.ui.view;

import com.youtopin.vaadin.component.HorizontalWizard;
import com.youtopin.vaadin.component.HorizontalWizard.WizardStep;
import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.options.OptionItem;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.formengine.wizard.WizardFormFlowCoordinator;
import com.youtopin.vaadin.i18n.TranslationProvider;
import com.youtopin.vaadin.samples.application.tour.OutboundTourWizardService;
import com.youtopin.vaadin.samples.application.tour.OutboundTourWizardState;
import com.youtopin.vaadin.samples.application.tour.TourReferenceDataService;
import com.youtopin.vaadin.samples.ui.formengine.definition.tour.OutboundTourAccommodationFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.tour.OutboundTourBasicsFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.tour.OutboundTourPlaceholderFormDefinition;
import com.youtopin.vaadin.samples.application.tour.model.OutboundTourAccommodationFormData;
import com.youtopin.vaadin.samples.application.tour.model.OutboundTourBasicsFormData;
import com.youtopin.vaadin.samples.application.tour.model.OutboundTourPlaceholderFormData;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Sample wizard demonstrating a multi-step product creation flow using the form engine.
 */
@PageTitle("ایجاد تور خارجی")
@Route(value = "forms/outbound-tour", layout = MainLayout.class)
public class OutboundTourWizardView extends AppPageLayout implements LocaleChangeObserver {

    private static final List<String> STEP_ORDER = List.of(
            OutboundTourWizardState.STEP_BASICS,
            OutboundTourWizardState.STEP_ACCOMMODATIONS,
            OutboundTourWizardState.STEP_TRANSPORT,
            OutboundTourWizardState.STEP_SERVICES,
            OutboundTourWizardState.STEP_DATES,
            OutboundTourWizardState.STEP_PRICING,
            OutboundTourWizardState.STEP_SUMMARY
    );

    private final FormEngine formEngine;
    private final TranslationProvider translationProvider;
    private final TourReferenceDataService referenceDataService;
    private final OutboundTourWizardService wizardService;

    private OutboundTourWizardState wizardState;

    private final HorizontalWizard wizard = new HorizontalWizard();
    private final WizardFormFlowCoordinator<OutboundTourWizardState> coordinator;

    private final Map<String, String> stepLabelKeys = Map.of(
            OutboundTourWizardState.STEP_BASICS, "tourwizard.step.basics",
            OutboundTourWizardState.STEP_ACCOMMODATIONS, "tourwizard.step.accommodation",
            OutboundTourWizardState.STEP_TRANSPORT, "tourwizard.step.transport",
            OutboundTourWizardState.STEP_SERVICES, "tourwizard.step.services",
            OutboundTourWizardState.STEP_DATES, "tourwizard.step.dates",
            OutboundTourWizardState.STEP_PRICING, "tourwizard.step.pricing",
            OutboundTourWizardState.STEP_SUMMARY, "tourwizard.step.summary"
    );

    private final H1 pageTitle = new H1();
    private final Paragraph productBadge = new Paragraph();
    private final Button resetButton = new Button();
    private final Div wizardContainer = new Div();
    private final Div formHost = new Div();

    private RenderedForm<OutboundTourBasicsFormData> basicsForm;
    private RenderedForm<OutboundTourAccommodationFormData> accommodationForm;
    private RenderedForm<OutboundTourPlaceholderFormData> transportForm;
    private RenderedForm<OutboundTourPlaceholderFormData> servicesForm;
    private RenderedForm<OutboundTourPlaceholderFormData> datesForm;
    private RenderedForm<OutboundTourPlaceholderFormData> pricingForm;
    private RenderedForm<OutboundTourPlaceholderFormData> summaryForm;

    public OutboundTourWizardView(FormEngine formEngine,
                                  TranslationProvider translationProvider,
                                  TourReferenceDataService referenceDataService,
                                  OutboundTourWizardService wizardService) {
        this.formEngine = formEngine;
        this.translationProvider = translationProvider;
        this.referenceDataService = referenceDataService;
        this.wizardService = wizardService;
        this.wizardState = wizardService.load();
        this.coordinator = new WizardFormFlowCoordinator<>(wizard, STEP_ORDER, wizardState);

        configureLayout();
        configureWizard();
        rebuildForms();
        coordinator.setCompletedSteps(wizardState.getCompletedSteps());
        coordinator.setCurrentStepId(wizardState.getCurrentStepId());
        showStep(wizardState.getCurrentStepId());
        updateTexts();
    }

    private void configureLayout() {
        setPadding(false);
        setSpacing(false);
        wizard.setWidthFull();
        wizard.getElement().getStyle().set("--horizontal-wizard-circle-size", "56px");
        wizard.getElement().getStyle().set("--horizontal-wizard-connector-color", "var(--color-primary-500)");
        wizardContainer.addClassNames("app-card", "stack-lg", "mb-lg");
        wizardContainer.setWidthFull();
        wizardContainer.add(wizard);

        pageTitle.addClassName("page-title");
        productBadge.addClassNames("text-secondary", "font-monospace");
        resetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
        resetButton.setIcon(new Icon(VaadinIcon.REFRESH));
        resetButton.addClickListener(event -> startNewDraft());

        HorizontalLayout header = new HorizontalLayout(pageTitle, productBadge, resetButton);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.addClassName("mb-md");

        formHost.setWidthFull();
        formHost.addClassName("stack-lg");
        formHost.getStyle().set("min-height", "var(--lumo-size-xl, 320px)");

        Div formCard = new Div(formHost);
        formCard.addClassNames("app-card", "stack-lg");
        formCard.setWidthFull();

        add(header, wizardContainer, formCard);
        setFlexGrow(1, formCard);
    }

    private void configureWizard() {
        coordinator.configureWizard(this::buildWizardStep);
        coordinator.onStepSelection((stepId, state) -> {
            wizardState.setCurrentStepId(stepId);
            showStep(stepId);
        });
        coordinator.onStepCompletion((stepId, state) -> wizardState.markStepCompleted(stepId));
        wizard.addCurrentStepChangeListener(event -> wizardState.setCurrentStepId(event.getCurrentStep().getId()));
    }

    private WizardStep buildWizardStep(String stepId) {
        String labelKey = stepLabelKeys.getOrDefault(stepId, stepId);
        return WizardStep.of(stepId, getTranslation(labelKey));
    }

    private void rebuildForms() {
        coordinator.clearForms();
        basicsForm = renderForm(OutboundTourBasicsFormDefinition.class, wizardState::getBasics);
        configureBasicsForm(basicsForm);
        coordinator.registerStepForm(OutboundTourWizardState.STEP_BASICS, basicsForm, OutboundTourWizardState::getBasics);

        accommodationForm = renderForm(OutboundTourAccommodationFormDefinition.class, wizardState::getAccommodations);
        configureAccommodationForm(accommodationForm);
        coordinator.registerStepForm(OutboundTourWizardState.STEP_ACCOMMODATIONS, accommodationForm,
                OutboundTourWizardState::getAccommodations);

        transportForm = renderForm(OutboundTourPlaceholderFormDefinition.class, wizardState::getTransport);
        configurePlaceholderForm(transportForm, OutboundTourWizardState.STEP_TRANSPORT);
        coordinator.registerStepForm(OutboundTourWizardState.STEP_TRANSPORT, transportForm, OutboundTourWizardState::getTransport);

        servicesForm = renderForm(OutboundTourPlaceholderFormDefinition.class, wizardState::getServices);
        configurePlaceholderForm(servicesForm, OutboundTourWizardState.STEP_SERVICES);
        coordinator.registerStepForm(OutboundTourWizardState.STEP_SERVICES, servicesForm, OutboundTourWizardState::getServices);

        datesForm = renderForm(OutboundTourPlaceholderFormDefinition.class, wizardState::getDates);
        configurePlaceholderForm(datesForm, OutboundTourWizardState.STEP_DATES);
        coordinator.registerStepForm(OutboundTourWizardState.STEP_DATES, datesForm, OutboundTourWizardState::getDates);

        pricingForm = renderForm(OutboundTourPlaceholderFormDefinition.class, wizardState::getPricing);
        configurePlaceholderForm(pricingForm, OutboundTourWizardState.STEP_PRICING);
        coordinator.registerStepForm(OutboundTourWizardState.STEP_PRICING, pricingForm, OutboundTourWizardState::getPricing);

        summaryForm = renderForm(OutboundTourPlaceholderFormDefinition.class, wizardState::getSummary);
        configurePlaceholderForm(summaryForm, OutboundTourWizardState.STEP_SUMMARY);
        coordinator.registerStepForm(OutboundTourWizardState.STEP_SUMMARY, summaryForm, OutboundTourWizardState::getSummary);
    }

    private <T> RenderedForm<T> renderForm(Class<?> definitionClass, java.util.function.Supplier<T> beanSupplier) {
        Locale locale = getLocale();
        RenderedForm<T> rendered = formEngine.render(definitionClass, translationProvider, locale, isRtl(locale));
        rendered.getFields().forEach((FieldDefinition definition, FieldInstance instance) ->
                rendered.getOrchestrator().bindField(instance, definition));
        rendered.initializeWithBean(beanSupplier.get());
        return rendered;
    }

    private void configureBasicsForm(RenderedForm<OutboundTourBasicsFormData> form) {
        form.addActionHandler("outbound-basics-save", context -> {
            if (handleBasicsSubmission(false)) {
                Notification notification = Notification.show(getTranslation("tourwizard.notification.saved"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });
        form.addActionHandler("outbound-basics-next", context -> {
            if (handleBasicsSubmission(true)) {
                coordinator.markStepCompleted(OutboundTourWizardState.STEP_BASICS);
                wizardState.markStepCompleted(OutboundTourWizardState.STEP_BASICS);
                coordinator.nextStep(OutboundTourWizardState.STEP_BASICS)
                        .ifPresent(next -> {
                            coordinator.setCurrentStepId(next);
                            showStep(next);
                        });
            }
        });
        configureComboBox(form, "general.operatorId", referenceDataService::searchOperators);
        configureComboBox(form, "general.leaderId", referenceDataService::searchTourLeaders);
        configureComboBox(form, "general.tourClassId", referenceDataService::searchTourClasses);
        configureTagsField(form, "general.tagIds");
        configureProvinceCity(form);
        configureTransitGroup(form);
        configureDestinationGroup(form);
        configureDifficulty(form);
        configureServices(form);
        configureItineraryGroup(form);
    }

    private boolean handleBasicsSubmission(boolean advance) {
        wizardState.syncItinerary();
        wizardService.ensureProductId(wizardState);
        wizardService.store(wizardState);
        updateProductBadge();
        List<String> destinationCityIds = wizardState.getBasics().getDestinationPlan().getDestinations().stream()
                .map(OutboundTourBasicsFormData.Destination::getCityId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
        wizardService.synchronizeDestinations(wizardState, destinationCityIds, referenceDataService::resolveCityName);
        rebuildForms();
        coordinator.setCompletedSteps(wizardState.getCompletedSteps());
        if (!advance) {
            coordinator.setCurrentStepId(OutboundTourWizardState.STEP_BASICS);
            showStep(OutboundTourWizardState.STEP_BASICS);
        }
        return true;
    }

    private void configureComboBox(RenderedForm<?> form, String path, Function<String, List<OptionItem>> search) {
        findComboBox(form, path).ifPresent(combo -> {
            CallbackDataProvider<OptionItem, String> provider = optionProvider(search);
            combo.setDataProvider(provider, identityFilter());
            combo.setItemLabelGenerator(OptionItem::getLabel);
        });
    }

    private void configureTagsField(RenderedForm<?> form, String path) {
        findMultiComboBox(form, path).ifPresent(field -> {
            CallbackDataProvider<OptionItem, String> provider = optionProvider(referenceDataService::searchTags);
            field.setDataProvider(provider, identityFilter());
            field.setItemLabelGenerator(OptionItem::getLabel);
            field.setAllowCustomValue(true);
            field.addCustomValueSetListener(event -> {
                OptionItem created = referenceDataService.registerTag(event.getDetail());
                if (created != null) {
                    Set<OptionItem> selected = new LinkedHashSet<>(field.getValue());
                    selected.add(created);
                    field.getDataProvider().refreshAll();
                    field.setValue(selected);
                }
            });
        });
    }

    private void configureProvinceCity(RenderedForm<OutboundTourBasicsFormData> form) {
        Optional<ComboBox<OptionItem>> provinceBox = findComboBox(form, "origin.provinceId");
        Optional<ComboBox<OptionItem>> cityBox = findComboBox(form, "origin.cityId");
        provinceBox.ifPresent(box -> {
            CallbackDataProvider<OptionItem, String> provinceProvider = optionProvider(referenceDataService::searchProvinces);
            box.setDataProvider(provinceProvider, identityFilter());
            box.addValueChangeListener(event -> {
                ComboBox<OptionItem> cityCombo = cityBox.orElse(null);
                if (cityCombo != null) {
                    cityCombo.clear();
                    CallbackDataProvider<OptionItem, String> cityProvider = optionProvider(filter ->
                            referenceDataService.searchCitiesForProvince(
                                    event.getValue() != null ? event.getValue().getId() : "", filter));
                    cityCombo.setDataProvider(cityProvider, identityFilter());
                }
            });
            OptionItem currentProvince = box.getValue();
            cityBox.ifPresent(cities -> {
                CallbackDataProvider<OptionItem, String> cityProvider = optionProvider(filter -> referenceDataService
                        .searchCitiesForProvince(currentProvince != null ? currentProvince.getId() : "", filter));
                cities.setDataProvider(cityProvider, identityFilter());
            });
        });
    }

    private void configureTransitGroup(RenderedForm<OutboundTourBasicsFormData> form) {
        List<Map<FieldDefinition, FieldInstance>> entries = form.getRepeatableGroups()
                .getOrDefault("tour-transit-group", List.of());
        for (Map<FieldDefinition, FieldInstance> entry : entries) {
            ComboBox<OptionItem> continent = fieldCombo(entry, "transit.legs.continentId");
            ComboBox<OptionItem> country = fieldCombo(entry, "transit.legs.countryId");
            ComboBox<OptionItem> city = fieldCombo(entry, "transit.legs.cityId");
            if (continent != null) {
                setComboBoxProvider(continent, referenceDataService::searchContinents);
                continent.addValueChangeListener(event -> {
                    country.clear();
                    city.clear();
                    setComboBoxProvider(country, filter -> referenceDataService.searchCountries(
                            event.getValue() != null ? event.getValue().getId() : "", filter));
                });
            }
            if (country != null) {
                OptionItem continentValue = continent != null ? continent.getValue() : null;
                setComboBoxProvider(country, filter -> referenceDataService.searchCountries(
                        continentValue != null ? continentValue.getId() : "", filter));
                country.addValueChangeListener(event -> {
                    city.clear();
                    setComboBoxProvider(city, filter -> referenceDataService.searchCitiesForCountry(
                            event.getValue() != null ? event.getValue().getId() : "", filter));
                });
            }
            if (city != null) {
                OptionItem countryValue = country != null ? country.getValue() : null;
                setComboBoxProvider(city, filter -> referenceDataService.searchCitiesForCountry(
                        countryValue != null ? countryValue.getId() : "", filter));
            }
        }
    }

    private void configureDestinationGroup(RenderedForm<OutboundTourBasicsFormData> form) {
        List<Map<FieldDefinition, FieldInstance>> entries = form.getRepeatableGroups()
                .getOrDefault("tour-destination-group", List.of());
        for (Map<FieldDefinition, FieldInstance> entry : entries) {
            ComboBox<OptionItem> continent = fieldCombo(entry, "destinationPlan.destinations.continentId");
            ComboBox<OptionItem> country = fieldCombo(entry, "destinationPlan.destinations.countryId");
            ComboBox<OptionItem> city = fieldCombo(entry, "destinationPlan.destinations.cityId");
            if (continent != null) {
                setComboBoxProvider(continent, referenceDataService::searchContinents);
                continent.addValueChangeListener(event -> {
                    country.clear();
                    city.clear();
                    setComboBoxProvider(country, filter -> referenceDataService.searchCountries(
                            event.getValue() != null ? event.getValue().getId() : "", filter));
                });
            }
            if (country != null) {
                OptionItem continentValue = continent != null ? continent.getValue() : null;
                setComboBoxProvider(country, filter -> referenceDataService.searchCountries(
                        continentValue != null ? continentValue.getId() : "", filter));
                country.addValueChangeListener(event -> {
                    city.clear();
                    setComboBoxProvider(city, filter -> referenceDataService.searchCitiesForCountry(
                            event.getValue() != null ? event.getValue().getId() : "", filter));
                });
            }
            if (city != null) {
                OptionItem countryValue = country != null ? country.getValue() : null;
                setComboBoxProvider(city, filter -> referenceDataService.searchCitiesForCountry(
                        countryValue != null ? countryValue.getId() : "", filter));
            }
        }
    }

    private void configureDifficulty(RenderedForm<OutboundTourBasicsFormData> form) {
        configureComboBox(form, "details.difficultyLevel", referenceDataService::searchDifficultyLevels);
    }

    private void configureServices(RenderedForm<OutboundTourBasicsFormData> form) {
        findMultiComboBox(form, "details.excludedServiceIds")
                .ifPresent(field -> {
                    field.setDataProvider(optionProvider(referenceDataService::searchExcludedServices), identityFilter());
                    field.setItemLabelGenerator(OptionItem::getLabel);
                });
        findMultiComboBox(form, "details.requiredDocumentIds")
                .ifPresent(field -> {
                    field.setDataProvider(optionProvider(referenceDataService::searchRequiredDocuments), identityFilter());
                    field.setItemLabelGenerator(OptionItem::getLabel);
                });
        findMultiComboBox(form, "details.requiredEquipmentIds")
                .ifPresent(field -> {
                    field.setDataProvider(optionProvider(referenceDataService::searchRequiredEquipment), identityFilter());
                    field.setItemLabelGenerator(OptionItem::getLabel);
                });
    }

    private void configureItineraryGroup(RenderedForm<OutboundTourBasicsFormData> form) {
        Integer days = wizardState.getBasics().getDetails().getDurationDays();
        if (days != null && days > 0) {
            form.setRepeatableEntryCount("tour-itinerary-group", days);
        }
    }

    private void configureAccommodationForm(RenderedForm<OutboundTourAccommodationFormData> form) {
        form.addActionHandler("outbound-accommodation-save", context -> {
            if (handleAccommodationSubmission(false)) {
                Notification notification = Notification.show(getTranslation("tourwizard.notification.saved"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });
        form.addActionHandler("outbound-accommodation-back", context -> coordinator.previousStep(
                        OutboundTourWizardState.STEP_ACCOMMODATIONS)
                .ifPresent(previous -> {
                    coordinator.setCurrentStepId(previous);
                    showStep(previous);
                }));
        form.addActionHandler("outbound-accommodation-next", context -> {
            if (handleAccommodationSubmission(true)) {
                coordinator.markStepCompleted(OutboundTourWizardState.STEP_ACCOMMODATIONS);
                wizardState.markStepCompleted(OutboundTourWizardState.STEP_ACCOMMODATIONS);
                coordinator.nextStep(OutboundTourWizardState.STEP_ACCOMMODATIONS)
                        .ifPresent(next -> {
                            coordinator.setCurrentStepId(next);
                            showStep(next);
                        });
            }
        });
        configureAccommodationGroups(form);
        configureRoomGroup(form);
    }

    private boolean handleAccommodationSubmission(boolean advance) {
        if (!validateAccommodationQuality()) {
            Notification notification = Notification.show(getTranslation("tourwizard.validation.quality.required"));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        wizardService.store(wizardState);
        if (!advance) {
            coordinator.setCurrentStepId(OutboundTourWizardState.STEP_ACCOMMODATIONS);
            showStep(OutboundTourWizardState.STEP_ACCOMMODATIONS);
        }
        return true;
    }

    private boolean validateAccommodationQuality() {
        return wizardState.getAccommodations().getAccommodations().stream().noneMatch(entry ->
                "hotel".equals(entry.getAccommodationTypeId())
                        && (entry.getQualityId() == null || entry.getQualityId().isBlank()));
    }

    private void configureAccommodationGroups(RenderedForm<OutboundTourAccommodationFormData> form) {
        List<Map<FieldDefinition, FieldInstance>> entries = form.getRepeatableGroups()
                .getOrDefault("tour-accommodation-group", List.of());
        for (Map<FieldDefinition, FieldInstance> entry : entries) {
            ComboBox<OptionItem> type = fieldCombo(entry, "accommodations.accommodationTypeId");
            ComboBox<OptionItem> defaultHotel = fieldCombo(entry, "accommodations.defaultAccommodationId");
            ComboBox<OptionItem> quality = fieldCombo(entry, "accommodations.qualityId");
            ComboBox<OptionItem> catering = fieldCombo(entry, "accommodations.cateringServiceId");
            ComboBox<OptionItem> amenities = fieldCombo(entry, "accommodations.amenityServiceId");
            setComboBoxProvider(type, referenceDataService::searchAccommodationTypes);
            setComboBoxProvider(defaultHotel, referenceDataService::searchDefaultAccommodations);
            setComboBoxProvider(quality, referenceDataService::searchHotelQualities);
            setComboBoxProvider(catering, referenceDataService::searchCateringServices);
            setComboBoxProvider(amenities, referenceDataService::searchAmenityServices);
            FieldInstance nameField = entry.entrySet().stream()
                    .filter(e -> e.getKey().getPath().equals("accommodations.accommodationName"))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (defaultHotel != null && nameField != null) {
                defaultHotel.addValueChangeListener(event -> {
                    HasValue<?, ?> valueComponent = nameField.getValueComponent();
                    if (valueComponent != null) {
                        @SuppressWarnings("unchecked")
                        HasValue<?, String> textField = (HasValue<?, String>) valueComponent;
                        String label = event.getValue() != null ? event.getValue().getLabel() : "";
                        textField.setValue(label);
                    }
                });
            }
        }
    }

    private void configureRoomGroup(RenderedForm<OutboundTourAccommodationFormData> form) {
        findRepeatableCombos(form, "tour-rooms-group", "rooms.roomTypeId")
                .forEach(combo -> setComboBoxProvider(combo, referenceDataService::searchRoomTypes));
    }

    private void configurePlaceholderForm(RenderedForm<OutboundTourPlaceholderFormData> form, String stepId) {
        form.addActionHandler("outbound-placeholder-back", context -> coordinator.previousStep(stepId)
                .ifPresent(previous -> {
                    coordinator.setCurrentStepId(previous);
                    showStep(previous);
                }));
        form.addActionHandler("outbound-placeholder-next", context -> {
            coordinator.markStepCompleted(stepId);
            wizardState.markStepCompleted(stepId);
            coordinator.nextStep(stepId).ifPresent(next -> {
                coordinator.setCurrentStepId(next);
                showStep(next);
            });
        });
    }

    private void showStep(String stepId) {
        RenderedForm<?> form = coordinator.getForms().get(stepId);
        formHost.removeAll();
        if (form != null) {
            Component layout = form.getLayout();
            layout.getElement().getStyle().set("width", "100%");
            if (layout instanceof HasSize sized) {
                sized.setWidthFull();
            }
            formHost.add(layout);
        }
        wizard.setCurrentStepId(stepId);
        updateProductBadge();
    }

    private void startNewDraft() {
        wizardState.reset();
        wizardService.store(wizardState);
        rebuildForms();
        coordinator.setCompletedSteps(Set.of());
        coordinator.setCurrentStepId(OutboundTourWizardState.STEP_BASICS);
        showStep(OutboundTourWizardState.STEP_BASICS);
        updateProductBadge();
    }

    private void updateTexts() {
        pageTitle.setText(getTranslation("tourwizard.page.title"));
        resetButton.setText(getTranslation("tourwizard.action.reset"));
        updateProductBadge();
        coordinator.refreshWizard();
    }

    private void updateProductBadge() {
        if (wizardState.hasProductId()) {
            productBadge.setText(getTranslation("tourwizard.product.id", wizardState.getProductId()));
        } else {
            productBadge.setText(getTranslation("tourwizard.product.id.pending"));
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
        rebuildForms();
        coordinator.setCurrentStepId(wizardState.getCurrentStepId());
        showStep(wizardState.getCurrentStepId());
    }

    private boolean isRtl(Locale locale) {
        return locale != null && "fa".equalsIgnoreCase(locale.getLanguage());
    }

    private Optional<ComboBox<OptionItem>> findComboBox(RenderedForm<?> form, String path) {
        return form.getFields().entrySet().stream()
                .filter(entry -> entry.getKey().getPath().equals(path))
                .map(Map.Entry::getValue)
                .map(field -> (ComboBox<OptionItem>) field.getValueComponent())
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    private Optional<MultiSelectComboBox<OptionItem>> findMultiComboBox(RenderedForm<?> form, String path) {
        return form.getFields().entrySet().stream()
                .filter(entry -> entry.getKey().getPath().equals(path))
                .map(Map.Entry::getValue)
                .map(field -> (MultiSelectComboBox<OptionItem>) field.getValueComponent())
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    private ComboBox<OptionItem> fieldCombo(Map<FieldDefinition, FieldInstance> entry, String path) {
        return entry.entrySet().stream()
                .filter(e -> e.getKey().getPath().equals(path))
                .map(Map.Entry::getValue)
                .map(instance -> (ComboBox<OptionItem>) instance.getValueComponent())
                .findFirst()
                .orElse(null);
    }

    private List<ComboBox<OptionItem>> findRepeatableCombos(RenderedForm<?> form, String groupId, String path) {
        List<Map<FieldDefinition, FieldInstance>> entries = form.getRepeatableGroups().getOrDefault(groupId, List.of());
        return entries.stream()
                .map(entry -> fieldCombo(entry, path))
                .filter(combo -> combo != null)
                .toList();
    }

    private void setComboBoxProvider(ComboBox<OptionItem> combo, Function<String, List<OptionItem>> search) {
        if (combo == null) {
            return;
        }
        CallbackDataProvider<OptionItem, String> provider = optionProvider(search);
        combo.setDataProvider(provider, identityFilter());
        combo.setItemLabelGenerator(OptionItem::getLabel);
    }

    private SerializableFunction<String, String> identityFilter() {
        return value -> value;
    }

    private CallbackDataProvider<OptionItem, String> optionProvider(Function<String, List<OptionItem>> search) {
        return DataProvider.fromFilteringCallbacks(query -> {
                    String filter = query.getFilter().orElse("");
                    List<OptionItem> items = search.apply(filter);
                    int offset = Math.max(query.getOffset(), 0);
                    int limit = query.getLimit();
                    if (offset >= items.size()) {
                        return Stream.empty();
                    }
                    if (limit <= 0) {
                        return items.stream().skip(offset);
                    }
                    int end = Math.min(items.size(), offset + limit);
                    return items.subList(offset, end).stream();
                }, query -> search.apply(query.getFilter().orElse("")).size());
    }
}
