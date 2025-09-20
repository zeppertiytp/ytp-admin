package com.example.adminpanel.ui.view;

import com.example.adminpanel.application.form.FormValidationService;
import com.example.adminpanel.ui.component.GeneratedForm;
import com.example.adminpanel.ui.layout.AppPageLayout;
import com.example.adminpanel.ui.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * View demonstrating dynamic form generation. This page loads a JSON
 * specification from the classpath and renders a form using the
 * {@link GeneratedForm} component. The form supports translated
 * labels, required and pattern validation, conditional visibility and
 * server‑side validation via a {@link FormValidationService}.
 */
@Route(value = "forms", layout = MainLayout.class)
public class FormGenerationView extends AppPageLayout implements LocaleChangeObserver {

    @Autowired
    public FormGenerationView(FormValidationService validationService) {
        pageTitle = createPageTitle(getTranslation("forms.title"));
        GeneratedForm form = new GeneratedForm("user_form.json", validationService);
        GeneratedForm layoutForm = new GeneratedForm("user_form_with_layout.json", validationService);
        add(defaultSampleHeading, form, layoutSampleHeading, layoutForm);
        setHorizontalComponentAlignment(Alignment.START, defaultSampleHeading, form, layoutSampleHeading, layoutForm);
        updatePageTitle();
    }

    private final H1 pageTitle;
    private final H2 defaultSampleHeading = new H2();
    private final H2 layoutSampleHeading = new H2();

    private void updatePageTitle() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("forms.title"));
        }
        pageTitle.setText(getTranslation("forms.title"));
        defaultSampleHeading.setText(getTranslation("forms.sample.basic"));
        layoutSampleHeading.setText(getTranslation("forms.sample.layout"));
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updatePageTitle();
    }
}
