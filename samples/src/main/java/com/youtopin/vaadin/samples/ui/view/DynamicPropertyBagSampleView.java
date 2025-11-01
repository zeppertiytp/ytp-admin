package com.youtopin.vaadin.samples.ui.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.i18n.TranslationProvider;
import com.youtopin.vaadin.samples.application.dynamicbag.DynamicBagFormData;
import com.youtopin.vaadin.samples.ui.formengine.definition.dynamic.DynamicPropertyBagDirectoryFormDefinition;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;
import com.youtopin.vaadin.util.LocaleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Demonstrates a single form where repeatable entries rely entirely on {@link com.youtopin.vaadin.formengine.binder.DynamicPropertyBag}.
 */
@Route(value = "forms/dynamic-bag", layout = MainLayout.class)
@PageTitle("Dynamic Property Bag Repeatable")
public class DynamicPropertyBagSampleView extends AppPageLayout implements LocaleChangeObserver {

    private static final Logger log = LoggerFactory.getLogger(DynamicPropertyBagSampleView.class);

    private final FormEngine formEngine;
    private final TranslationProvider translationProvider;
    private final ObjectMapper objectMapper;

    private final Paragraph subtitle;
    private final VerticalLayout formCard;
    private final Paragraph resultHeading;
    private final Div resultContent;

    private RenderedForm<DynamicBagFormData> renderedForm;
    private DynamicBagFormData formData;
    private final H1 pageTitle;

    public DynamicPropertyBagSampleView(FormEngine formEngine,
                                        TranslationProvider translationProvider) {
        this.formEngine = formEngine;
        this.translationProvider = translationProvider;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        pageTitle = createPageTitle("");
        subtitle = new Paragraph();
        subtitle.addClassName("page-subtitle");

        resultHeading = new Paragraph();
        resultHeading.addClassName("text-primary");
        resultContent = new Div();
        resultContent.addClassNames("code-block", "bg-contrast-5", "p-m", "font-monospace");
        resultContent.getStyle().set("white-space", "pre-wrap");
        resultContent.getStyle().set("direction", UI.getCurrent().getElement().getAttribute("dir"));

        formCard = createCard(subtitle, resultHeading, resultContent);
        formCard.addClassName("stack-lg");
        add(formCard);

        rebuildForm();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        rebuildForm();
    }

    private void rebuildForm() {
        Locale locale = getLocale();
        boolean rtl = LocaleUtil.isRtl(locale);

        pageTitle.setText(getTranslation("forms.dynamicBag.title"));
        subtitle.setText(getTranslation("forms.dynamicBag.description"));
        resultHeading.setText(getTranslation("forms.dynamicBag.result.heading"));
        resultContent.setText(getTranslation("forms.dynamicBag.result.placeholder"));
        resultContent.getStyle().set("direction", rtl ? "rtl" : "ltr");

        formCard.removeAll();
        formCard.add(subtitle);

        formData = DynamicBagFormData.sample();
        renderedForm = formEngine.render(DynamicPropertyBagDirectoryFormDefinition.class,
                translationProvider,
                locale,
                rtl);
        renderedForm.getFields().forEach((FieldDefinition definition, FieldInstance instance) ->
                renderedForm.getOrchestrator().bindField(instance, definition));
        renderedForm.initializeWithBean(formData);
        renderedForm.setActionBeanSupplier(() -> formData);
        configureActions();

        formCard.add(renderedForm.getLayout(), resultHeading, resultContent);
    }

    private void configureActions() {
        renderedForm.addValidationFailureListener((action, exception) -> {
            Notification notification = Notification.show(getTranslation("form.correctErrors"));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        renderedForm.addActionHandler("dynamic-bag-submit", context -> {
            DynamicBagFormData submitted = context.getBean();
            try {
                String json = objectMapper.writeValueAsString(submitted.toSerializableEntries());
                resultContent.setText(json);
                log.info("Dynamic bag submission:\n{}", json);
                Notification notification = Notification.show(getTranslation("forms.dynamicBag.result.success"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (JsonProcessingException ex) {
                log.warn("Unable to serialise dynamic bag submission", ex);
                Notification notification = Notification.show(getTranslation("forms.dynamicBag.result.error"));
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }
}
