package com.youtopin.vaadin.samples.ui.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.youtopin.vaadin.component.GeneratedForm;
import com.youtopin.vaadin.form.FormValidationService;
import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.SectionDefinition;
import com.youtopin.vaadin.formengine.definition.GroupDefinition;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.i18n.TranslationProvider;
import com.youtopin.vaadin.samples.ui.formengine.definition.AccessPolicyFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.AgendaBuilderFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.DailyPlanFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.DailyPlanListFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.EmployeeOnboardingFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.ProductCatalogFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.InventoryManagementFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.ProfileLockFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.model.AccessPolicyFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.AgendaBuilderFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.DailyPlanFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.DailyPlanListFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.EmployeeOnboardingFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.ProductCatalogFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.ProfileLockFormData;
import com.youtopin.vaadin.samples.application.formengine.model.InventoryManagementFormData;
import com.youtopin.vaadin.samples.application.formengine.InventoryPlanService;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.SerializationFeature;

import com.vaadin.flow.component.textfield.IntegerField;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Demonstrates the annotation-driven form engine through four sample forms.
 */
@Route(value = "forms", layout = MainLayout.class)
public class FormGenerationView extends AppPageLayout implements LocaleChangeObserver {

    private static final Logger log = LoggerFactory.getLogger(FormGenerationView.class);

    private final FormEngine formEngine;
    private final TranslationProvider translationProvider;
    private final ObjectMapper objectMapper;
    private final InventoryPlanService inventoryPlanService;

    private final H2 generatedDefaultHeading;
    private final H2 generatedLayoutHeading;
    private final GeneratedForm defaultGeneratedForm;
    private final GeneratedForm layoutGeneratedForm;
    private final VerticalLayout sampleContainer;
    private H1 pageTitle;

    @Autowired
    public FormGenerationView(FormEngine formEngine,
                              TranslationProvider translationProvider,
                              FormValidationService validationService,
                              InventoryPlanService inventoryPlanService) {
        this.formEngine = Objects.requireNonNull(formEngine, "formEngine");
        this.translationProvider = Objects.requireNonNull(translationProvider, "translationProvider");
        this.inventoryPlanService = Objects.requireNonNull(inventoryPlanService, "inventoryPlanService");
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        pageTitle = createPageTitle("");

        generatedDefaultHeading = new H2();
        generatedDefaultHeading.addClassNames("text-primary", "mt-0");
        generatedLayoutHeading = new H2();
        generatedLayoutHeading.addClassNames("text-primary");
        defaultGeneratedForm = new GeneratedForm("user_form.json", validationService);
        layoutGeneratedForm = new GeneratedForm("user_form_with_layout.json", validationService);

        VerticalLayout generatedFormsCard = createCard(
                generatedDefaultHeading,
                defaultGeneratedForm,
                generatedLayoutHeading,
                layoutGeneratedForm
        );

        sampleContainer = new VerticalLayout();
        sampleContainer.setSpacing(true);
        sampleContainer.setPadding(false);
        sampleContainer.addClassNames("stack-xl");

        add(generatedFormsCard, sampleContainer);
        renderSamples();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        renderSamples();
    }

    private void renderSamples() {
        updateHeadings();
        sampleContainer.removeAll();
        ProfileLockFormData profileLockData = new ProfileLockFormData();
        List<SampleDescriptor<?>> descriptors = List.of(
                new SampleDescriptor<>(
                        "forms.sample.onboarding.heading",
                        "forms.sample.onboarding.description",
                        "forms.sample.onboarding.features",
                        EmployeeOnboardingFormDefinition.class,
                        EmployeeOnboardingFormData::new
                ),
                new SampleDescriptor<>(
                        "forms.sample.catalog.heading",
                        "forms.sample.catalog.description",
                        "forms.sample.catalog.features",
                        ProductCatalogFormDefinition.class,
                        ProductCatalogFormData::new
                ),
                new SampleDescriptor<>(
                        "forms.sample.policy.heading",
                        "forms.sample.policy.description",
                        "forms.sample.policy.features",
                        AccessPolicyFormDefinition.class,
                        AccessPolicyFormData::new
                ),
                new SampleDescriptor<>(
                        "forms.sample.plan.heading",
                        "forms.sample.plan.description",
                        "forms.sample.plan.features",
                        DailyPlanFormDefinition.class,
                        DailyPlanFormData::new
                ),
                new SampleDescriptor<>(
                        "forms.sample.planlist.heading",
                        "forms.sample.planlist.description",
                        "forms.sample.planlist.features",
                        DailyPlanListFormDefinition.class,
                        DailyPlanListFormData::new
                ),
                new SampleDescriptor<>(
                        "forms.sample.agenda.heading",
                        "forms.sample.agenda.description",
                        "forms.sample.agenda.features",
                        AgendaBuilderFormDefinition.class,
                        AgendaBuilderFormData::new
                ),
                new SampleDescriptor<>(
                        "forms.sample.locking.heading",
                        "forms.sample.locking.description",
                        "forms.sample.locking.features",
                        ProfileLockFormDefinition.class,
                        () -> profileLockData
                ),
                new SampleDescriptor<>(
                        "forms.sample.inventory.heading",
                        "forms.sample.inventory.description",
                        "forms.sample.inventory.features",
                        InventoryManagementFormDefinition.class,
                        () -> InventoryManagementFormData.copyOf(inventoryPlanService.load())
                )
        );
        descriptors.stream()
                .map(this::buildSampleCard)
                .forEach(sampleContainer::add);
    }

    private void updateHeadings() {
        if (pageTitle == null) {
            pageTitle = createPageTitle("");
        }
        String title = getTranslation("forms.title");
        pageTitle.setText(title);
        generatedDefaultHeading.setText(getTranslation("forms.sample.basic"));
        generatedLayoutHeading.setText(getTranslation("forms.sample.layout"));
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(title);
        }
    }

    private <T> Component buildSampleCard(SampleDescriptor<T> descriptor) {
        H2 heading = new H2(getTranslation(descriptor.headingKey()));
        heading.addClassNames("text-primary", "mt-0");
        Paragraph description = new Paragraph(getTranslation(descriptor.descriptionKey()));
        description.addClassNames("text-secondary", "mb-s");

        UnorderedList featureList = buildFeatureList(descriptor.featurePrefix());
        featureList.addClassNames("text-secondary");

        RenderedForm<T> rendered = renderForm(descriptor.definitionClass(), descriptor.beanSupplier());
        rendered.getLayout().setWidthFull();

        VerticalLayout content = new VerticalLayout(heading, description, featureList, rendered.getLayout());
        content.setSpacing(true);
        content.setPadding(false);
        content.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        return createCard(content);
    }

    private UnorderedList buildFeatureList(String featurePrefix) {
        UnorderedList list = new UnorderedList();
        int index = 1;
        while (true) {
            String key = featurePrefix + "." + index;
            String translation = getTranslation(key);
            if (translation == null || translation.equals(key)) {
                break;
            }
            ListItem item = new ListItem(translation);
            list.add(item);
            index++;
        }
        list.setVisible(list.getChildren().findAny().isPresent());
        return list;
    }

    private <T> RenderedForm<T> renderForm(Class<?> definitionClass, Supplier<T> beanSupplier) {
        Locale locale = getLocale();
        boolean rtl = isRtl(locale);
        RenderedForm<T> rendered = formEngine.render(definitionClass, translationProvider, locale, rtl);
        for (Map.Entry<FieldDefinition, FieldInstance> entry : rendered.getFields().entrySet()) {
            rendered.getOrchestrator().bindField(entry.getValue(), entry.getKey());
        }
        T initialBean = beanSupplier.get();
        rendered.initializeWithBean(initialBean);
        rendered.setActionBeanSupplier(beanSupplier);
        rendered.getDefinition().getActions().stream()
                .filter(action -> action.getType() == UiAction.ActionType.SUBMIT)
                .forEach(action -> rendered.addActionHandler(action.getId(), context ->
                        logSubmission(definitionClass.getSimpleName(), context.getActionDefinition().getId(), context.getBean())));
        rendered.addValidationFailureListener((actionDefinition, exception) ->
                log.warn("Validation failed for action '{}' in form '{}'", actionDefinition.getId(), definitionClass.getSimpleName(), exception));
        if (definitionClass.equals(DailyPlanFormDefinition.class)) {
            configureDynamicPlan(rendered);
        } else if (definitionClass.equals(DailyPlanListFormDefinition.class)) {
            configureDynamicPlanList(rendered);
        } else if (definitionClass.equals(AgendaBuilderFormDefinition.class)) {
            configureAgendaSections(rendered);
        } else if (definitionClass.equals(ProfileLockFormDefinition.class)) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Supplier<ProfileLockFormData> supplier = (Supplier) beanSupplier;
            @SuppressWarnings("unchecked")
            RenderedForm<ProfileLockFormData> profileForm = (RenderedForm<ProfileLockFormData>) rendered;
            configureProfileLock(profileForm, supplier);
        } else if (definitionClass.equals(InventoryManagementFormDefinition.class)) {
            @SuppressWarnings("unchecked")
            RenderedForm<InventoryManagementFormData> inventoryForm = (RenderedForm<InventoryManagementFormData>) rendered;
            configureInventoryPlan(inventoryForm);
        }
        return rendered;
    }

    private <T> void configureDynamicPlan(RenderedForm<T> rendered) {
        List<VerticalLayout> daySections = rendered.getSections().entrySet().stream()
                .filter(entry -> entry.getKey().getId().startsWith("day-"))
                .sorted(Comparator.comparingInt(entry -> resolveNumericSuffix(entry.getKey().getId())))
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(ArrayList::new));
        FieldDefinition dayCountDefinition = rendered.getFields().keySet().stream()
                .filter(definition -> "schedule.dayCount".equals(definition.getPath()))
                .findFirst()
                .orElse(null);
        FieldInstance dayCountInstance = dayCountDefinition == null ? null : rendered.getFields().get(dayCountDefinition);
        if (dayCountInstance != null) {
            HasValue<?, ?> component = dayCountInstance.getValueComponent();
            if (component instanceof IntegerField integerField) {
                integerField.setMin(0);
                integerField.setMax(daySections.size());
            }
            component.addValueChangeListener(event -> updateDayVisibility(daySections, toInteger(event.getValue())));
            updateDayVisibility(daySections, toInteger(component.getValue()));
            Button resetButton = rendered.getActionButtons().get("plan-reset");
            if (resetButton != null) {
                resetButton.addClickListener(event -> {
                    component.setValue(null);
                    updateDayVisibility(daySections, 0);
                });
            }
        }
    }

    private <T> void configureDynamicPlanList(RenderedForm<T> rendered) {
        FieldDefinition dayCountDefinition = rendered.getFields().keySet().stream()
                .filter(definition -> "schedule.dayCount".equals(definition.getPath()))
                .findFirst()
                .orElse(null);
        FieldInstance dayCountInstance = dayCountDefinition == null ? null : rendered.getFields().get(dayCountDefinition);
        GroupDefinition repeatableGroup = rendered.getDefinition().getSections().stream()
                .filter(section -> "plan-list-days".equals(section.getId()))
                .findFirst()
                .map(section -> section.getGroups().isEmpty() ? null : section.getGroups().get(0))
                .orElse(null);
        if (dayCountInstance == null || repeatableGroup == null) {
            return;
        }

        int minEntries = repeatableGroup.getRepeatableDefinition().getMin();
        int maxEntries = repeatableGroup.getRepeatableDefinition().getMax();
        String groupId = repeatableGroup.getId();

        HasValue<?, ?> dayCountComponent = dayCountInstance.getValueComponent();
        if (dayCountComponent instanceof IntegerField integerField) {
            integerField.setMin(minEntries);
            integerField.setMax(maxEntries);
        }

        syncDailyPlanRepeatable(rendered, groupId, minEntries, maxEntries, toInteger(dayCountComponent.getValue()));
        dayCountComponent.addValueChangeListener(event ->
                syncDailyPlanRepeatable(rendered, groupId, minEntries, maxEntries, toInteger(event.getValue())));

        Button resetButton = rendered.getActionButtons().get("planlist-reset");
        if (resetButton != null) {
            resetButton.addClickListener(event -> {
                dayCountComponent.clear();
                syncDailyPlanRepeatable(rendered, groupId, minEntries, maxEntries, 0);
            });
        }
    }

    private <T> void syncDailyPlanRepeatable(RenderedForm<T> rendered,
                                             String groupId,
                                             int minEntries,
                                             int maxEntries,
                                             int requestedEntries) {
        int desired = Math.max(minEntries, Math.min(requestedEntries, maxEntries));
        rendered.setRepeatableEntryCount(groupId, desired);
    }

    private void updateDayVisibility(List<VerticalLayout> daySections, int requestedActiveDays) {
        int activeDays = clampActiveDays(requestedActiveDays, daySections.size());
        for (int index = 0; index < daySections.size(); index++) {
            daySections.get(index).setVisible(index < activeDays);
        }
    }

    private int clampActiveDays(int requestedActiveDays, int max) {
        return Math.min(Math.max(requestedActiveDays, 0), max);
    }

    private <T> void configureAgendaSections(RenderedForm<T> rendered) {
        SectionDefinition segmentsDefinition = rendered.getSections().keySet().stream()
                .filter(section -> "agenda-segments".equals(section.getId()))
                .findFirst()
                .orElse(null);
        if (segmentsDefinition == null) {
            return;
        }
        VerticalLayout sectionLayout = rendered.getSections().get(segmentsDefinition);
        if (sectionLayout == null) {
            return;
        }
        Button addButton = rendered.getActionButtons().get("agenda-add-section");
        Button removeButton = rendered.getActionButtons().get("agenda-remove-section");
        VerticalLayout repeatableLayout = sectionLayout.getChildren()
                .filter(component -> component instanceof VerticalLayout)
                .map(component -> (VerticalLayout) component)
                .filter(layout -> layout.getElement().getClassList().contains("form-engine-repeatable-group"))
                .findFirst()
                .orElse(null);
        if (repeatableLayout == null) {
            return;
        }
        String groupId = segmentsDefinition.getGroups().isEmpty() ? "" : segmentsDefinition.getGroups().get(0).getId();
        VerticalLayout entriesContainer = repeatableLayout.getChildren()
                .filter(component -> component instanceof VerticalLayout)
                .map(component -> (VerticalLayout) component)
                .filter(layout -> groupId.equals(layout.getElement().getAttribute("data-repeatable-container")))
                .findFirst()
                .orElse(null);
        Button internalAdd = repeatableLayout.getChildren()
                .filter(component -> component instanceof Button)
                .map(component -> (Button) component)
                .findFirst()
                .orElse(null);
        if (entriesContainer == null || internalAdd == null) {
            return;
        }
        if (addButton != null) {
            addButton.addClickListener(event -> {
                if (internalAdd.isEnabled()) {
                    internalAdd.click();
                    syncAgendaButtons(addButton, removeButton, internalAdd, entriesContainer);
                }
            });
        }
        if (removeButton != null) {
            removeButton.addClickListener(event -> {
                if (removeLastRepeatableEntry(entriesContainer)) {
                    syncAgendaButtons(addButton, removeButton, internalAdd, entriesContainer);
                }
            });
        }
        syncAgendaButtons(addButton, removeButton, internalAdd, entriesContainer);
    }

    private void configureProfileLock(RenderedForm<ProfileLockFormData> rendered,
                                      Supplier<ProfileLockFormData> supplier) {
        ProfileLockFormData bean = supplier.get();
        rendered.setActionBeanSupplier(() -> bean);
        rendered.addActionHandler("profile-lock-submit", context -> {
            ProfileLockFormData current = context.getBean();
            current.setLocked(true);
            current.setContactLocked(true);
            rendered.initializeWithBean(current);
        });
    }

    private void syncAgendaButtons(Button addButton, Button removeButton, Button internalAdd, VerticalLayout entriesContainer) {
        if (addButton != null && internalAdd != null) {
            addButton.setEnabled(internalAdd.isEnabled());
        }
        if (removeButton != null) {
            boolean canRemove = entriesContainer.getChildren()
                    .filter(component -> component instanceof VerticalLayout)
                    .map(component -> (VerticalLayout) component)
                    .filter(layout -> layout.getElement().hasAttribute("data-repeatable-entry"))
                    .flatMap(VerticalLayout::getChildren)
                    .filter(child -> child instanceof Button)
                    .map(child -> (Button) child)
                    .anyMatch(button -> "true".equals(button.getElement().getAttribute("data-repeatable-remove")) && button.isEnabled());
            removeButton.setEnabled(canRemove);
        }
    }

    private boolean removeLastRepeatableEntry(VerticalLayout entriesContainer) {
        java.util.List<VerticalLayout> wrappers = collectRepeatableEntryWrappers(entriesContainer);
        if (wrappers.isEmpty()) {
            return false;
        }
        VerticalLayout lastWrapper = wrappers.get(wrappers.size() - 1);
        return lastWrapper.getChildren()
                .filter(child -> child instanceof Button)
                .map(child -> (Button) child)
                .filter(button -> "true".equals(button.getElement().getAttribute("data-repeatable-remove")) && button.isEnabled())
                .findFirst()
                .map(button -> {
                    button.click();
                    return true;
                })
                .orElse(false);
    }

    private java.util.List<VerticalLayout> collectRepeatableEntryWrappers(VerticalLayout entriesContainer) {
        return entriesContainer.getChildren()
                .filter(component -> component instanceof VerticalLayout)
                .map(component -> (VerticalLayout) component)
                .filter(layout -> layout.getElement().hasAttribute("data-repeatable-entry"))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private int resolveNumericSuffix(String id) {
        if (id == null || id.isBlank()) {
            return Integer.MAX_VALUE;
        }
        int separator = id.lastIndexOf('-');
        String numericPortion = separator >= 0 && separator < id.length() - 1 ? id.substring(separator + 1) : id;
        try {
            return Integer.parseInt(numericPortion);
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    private int toInteger(Object value) {
        if (value instanceof Number number) {
            return Math.max(number.intValue(), 0);
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Math.max(Integer.parseInt(str), 0);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return 0;
    }

    private void configureInventoryPlan(RenderedForm<InventoryManagementFormData> rendered) {
        rendered.addActionHandler("inventory-save", context -> {
            inventoryPlanService.save(context.getBean());
            InventoryManagementFormData refreshed = InventoryManagementFormData.copyOf(inventoryPlanService.load());
            rendered.initializeWithBean(refreshed);
        });
    }

    private record SampleDescriptor<T>(String headingKey,
                                       String descriptionKey,
                                       String featurePrefix,
                                       Class<?> definitionClass,
                                       Supplier<T> beanSupplier) {
    }

    private boolean isRtl(Locale locale) {
        return locale != null && "fa".equalsIgnoreCase(locale.getLanguage());
    }

    private <T> void logSubmission(String formName, String actionId, T bean) {
        try {
            String payload = objectMapper.writeValueAsString(bean);
            log.info("Form '{}' action '{}' submitted with payload: {}", formName, actionId, payload);
        } catch (JsonProcessingException ex) {
            log.warn("Unable to serialize submission for form '{}' action '{}'", formName, actionId, ex);
        }
    }
}
