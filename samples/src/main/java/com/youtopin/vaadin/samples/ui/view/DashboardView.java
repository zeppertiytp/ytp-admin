package com.youtopin.vaadin.samples.ui.view;

import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

/**
 * The dashboard view displayed after successful login.  It acts as the
 * landing page inside the {@link MainLayout}.  This view observes
 * locale changes to update its title accordingly.
 */
@Route(value = "", layout = MainLayout.class)
// The page title is set programmatically in the constructor and on locale changes
public class DashboardView extends AppPageLayout implements LocaleChangeObserver {

    private H1 title;
    private Span subtitle;
    private Span heroMessage;

    public DashboardView() {
        title = createPageTitle(getTranslation("dashboard.title"));

        subtitle = new Span(getTranslation("dashboard.subtitle"));
        subtitle.addClassName("page-subtitle");
        add(subtitle);

        VerticalLayout hero = createCard();
        hero.addClassNames("app-card--center", "stack-sm");
        heroMessage = new Span(getTranslation("dashboard.welcome"));
        heroMessage.addClassName("page-subtitle");
        hero.add(heroMessage);
        add(hero);
        // Set the browser tab title programmatically based on the current locale
        UI.getCurrent().getPage().setTitle(getTranslation("dashboard.title"));
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        title.setText(getTranslation("dashboard.title"));
        subtitle.setText(getTranslation("dashboard.subtitle"));
        heroMessage.setText(getTranslation("dashboard.welcome"));
        // Update the page title when the locale changes
        UI.getCurrent().getPage().setTitle(getTranslation("dashboard.title"));
    }

}
