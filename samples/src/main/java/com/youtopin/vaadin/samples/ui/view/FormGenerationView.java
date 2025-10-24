package com.youtopin.vaadin.samples.ui.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.youtopin.vaadin.component.GeneratedForm;
import com.youtopin.vaadin.form.FormValidationService;
import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.i18n.TranslationProvider;
import com.youtopin.vaadin.samples.ui.formengine.definition.AccessPolicyFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.DailyPlanFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.EmployeeOnboardingFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.ProductCatalogFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.model.AccessPolicyFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.DailyPlanFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.EmployeeOnboardingFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.ProductCatalogFormData;
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

import java.util.LinkedHashMap;
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

    private final H2 generatedDefaultHeading;
    private final H2 generatedLayoutHeading;
    private final GeneratedForm defaultGeneratedForm;
    private final GeneratedForm layoutGeneratedForm;
    private final VerticalLayout sampleContainer;
    private H1 pageTitle;

    @Autowired
    public FormGenerationView(FormEngine formEngine,
                              TranslationProvider translationProvider,
                              FormValidationService validationService) {
        this.formEngine = Objects.requireNonNull(formEngine, "formEngine");
        this.translationProvider = Objects.requireNonNull(translationProvider, "translationProvider");
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
        rendered.setActionBeanSupplier(() -> beanSupplier.get());
        rendered.getActionButtons().keySet().forEach(actionId ->
                rendered.addActionHandler(actionId, context -> logSubmission(definitionClass.getSimpleName(), context.getActionDefinition().getId(), context.getBean()))
        );
        rendered.addValidationFailureListener((actionDefinition, exception) ->
                log.warn("Validation failed for action '{}' in form '{}'", actionDefinition.getId(), definitionClass.getSimpleName(), exception));
        if (definitionClass.equals(DailyPlanFormDefinition.class)) {
            configureDynamicPlan(rendered);
        }
        return rendered;
    }

    private <T> void configureDynamicPlan(RenderedForm<T> rendered) {
        Map<String, VerticalLayout> daySections = rendered.getSections().entrySet().stream()
                .filter(entry -> entry.getKey().getId().startsWith("day-"))
                .collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        FieldDefinition dayCountDefinition = rendered.getFields().keySet().stream()
                .filter(definition -> "schedule.dayCount".equals(definition.getPath()))
                .findFirst()
                .orElse(null);
        FieldInstance dayCountInstance = dayCountDefinition == null ? null : rendered.getFields().get(dayCountDefinition);
        if (dayCountInstance != null) {
            HasValue<?, ?> component = dayCountInstance.getValueComponent();
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

    private void updateDayVisibility(Map<String, VerticalLayout> daySections, int activeDays) {
        int index = 1;
        for (Map.Entry<String, VerticalLayout> entry : daySections.entrySet()) {
            entry.getValue().setVisible(index <= activeDays);
            index++;
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
