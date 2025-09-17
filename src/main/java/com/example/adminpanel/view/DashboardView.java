package com.example.adminpanel.view;

import com.example.adminpanel.security.SecurityService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
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
public class DashboardView extends VerticalLayout implements LocaleChangeObserver, BeforeEnterObserver {

    private final SecurityService securityService;
    private H1 title;

    @Autowired
    public DashboardView(SecurityService securityService) {
        this.securityService = securityService;
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        title = new H1(getTranslation("dashboard.title"));
        add(title);
        // Set the browser tab title programmatically based on the current locale
        UI.getCurrent().getPage().setTitle(getTranslation("dashboard.title"));
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        title.setText(getTranslation("dashboard.title"));
        // Update the page title when the locale changes
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