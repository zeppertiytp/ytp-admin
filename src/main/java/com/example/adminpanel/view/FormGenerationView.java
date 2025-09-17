package com.example.adminpanel.view;

import com.example.adminpanel.component.GeneratedForm;
import com.example.adminpanel.service.FormValidationService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
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
@PageTitle("Forms")
public class FormGenerationView extends VerticalLayout {

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
    }
}