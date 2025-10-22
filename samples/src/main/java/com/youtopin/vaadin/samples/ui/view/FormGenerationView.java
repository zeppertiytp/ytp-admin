package com.youtopin.vaadin.samples.ui.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtopin.vaadin.component.GeneratedForm;
import com.youtopin.vaadin.form.FormValidationService;
import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.i18n.TranslationProvider;
import com.youtopin.vaadin.samples.ui.formengine.definition.AccessPolicyFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.EmployeeOnboardingFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.definition.ProductCatalogFormDefinition;
import com.youtopin.vaadin.samples.ui.formengine.model.AccessPolicyFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.EmployeeOnboardingFormData;
import com.youtopin.vaadin.samples.ui.formengine.model.ProductCatalogFormData;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Demonstrates the annotation-driven form engine through three sample forms.
 */
@Route(value = "forms", layout = MainLayout.class)
public class FormGenerationView extends AppPageLayout implements LocaleChangeObserver {

    private static final Logger log = LoggerFactory.getLogger(FormGenerationView.class);

    private final FormEngine formEngine;
    private final TranslationProvider translationProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        sampleContainer.add(
                buildSampleCard(
                        "forms.sample.onboarding.heading",
                        "forms.sample.onboarding.description",
                        EmployeeOnboardingFormDefinition.class,
                        EmployeeOnboardingFormData::new
                ),
                buildSampleCard(
                        "forms.sample.catalog.heading",
                        "forms.sample.catalog.description",
                        ProductCatalogFormDefinition.class,
                        ProductCatalogFormData::new
                ),
                buildSampleCard(
                        "forms.sample.policy.heading",
                        "forms.sample.policy.description",
                        AccessPolicyFormDefinition.class,
                        AccessPolicyFormData::new
                )
        );
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

    private <T> Component buildSampleCard(String headingKey,
                                          String descriptionKey,
                                          Class<?> definitionClass,
                                          Supplier<T> beanSupplier) {
        H2 heading = new H2(getTranslation(headingKey));
        heading.addClassNames("text-primary", "mt-0");
        Paragraph description = new Paragraph(getTranslation(descriptionKey));
        description.addClassNames("text-secondary", "mb-s");

        RenderedForm<T> rendered = renderForm(definitionClass, beanSupplier);
        rendered.getLayout().setWidthFull();

        VerticalLayout content = new VerticalLayout(heading, description, rendered.getLayout());
        content.setSpacing(true);
        content.setPadding(false);
        content.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        return createCard(content);
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
        return rendered;
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
