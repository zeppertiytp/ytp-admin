package com.example.adminpanel.view;

import com.example.adminpanel.security.SecurityService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The dashboard view displayed after successful login.  It acts as the
 * landing page inside the {@link MainLayout}.  This view observes
 * locale changes to update its title accordingly.
 */
@Route(value = "", layout = MainLayout.class)
// The page title is set programmatically in the constructor and on locale changes
public class DashboardView extends ViewFrame implements LocaleChangeObserver, BeforeEnterObserver {

    private final SecurityService securityService;
    private H1 title;
    private Paragraph subtitle;

    @Autowired
    public DashboardView(SecurityService securityService) {
        this.securityService = securityService;
        VerticalLayout content = createContentSection();
        content.addClassName("view-hero");
        content.setAlignItems(Alignment.START);

        title = new H1(getTranslation("dashboard.title"));
        subtitle = new Paragraph(getTranslation("dashboard.subtitle"));
        subtitle.addClassName("text-muted");

        content.add(title, subtitle);

        UI.getCurrent().getPage().setTitle(getTranslation("dashboard.title"));
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        title.setText(getTranslation("dashboard.title"));
        subtitle.setText(getTranslation("dashboard.subtitle"));
        UI.getCurrent().getPage().setTitle(getTranslation("dashboard.title"));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Redirect unauthenticated users to the login page
        if (!securityService.isAuthenticated()) {
            event.forwardTo("login");
        }
    }
}