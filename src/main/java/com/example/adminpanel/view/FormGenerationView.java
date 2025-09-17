package com.example.adminpanel.view;

import com.example.adminpanel.component.GeneratedForm;
import com.example.adminpanel.service.FormValidationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
public class FormGenerationView extends VerticalLayout implements LocaleChangeObserver {

    @Autowired
    public FormGenerationView(FormValidationService validationService) {
        setPadding(true);
        setSpacing(true);
        setSizeFull();
        // Create a generated form from the sample specification. The
        // JSON file is located under src/main/resources/forms/user_form.json.
        GeneratedForm form = new GeneratedForm("user_form.json", validationService);
        add(form);
        setHorizontalComponentAlignment(Alignment.START, form);
        updatePageTitle();
    }

    private void updatePageTitle() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("forms.title"));
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updatePageTitle();
    }
}
