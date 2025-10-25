package com.youtopin.vaadin.samples.ui.view.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;

/**
 * Sample view that demonstrates how to assemble the Vaadin App Layout in
 * different configurations. Each example showcases a typical navigation
 * pattern aligned with the design system so developers can quickly apply the
 * snippets to real dashboards.
 */
@Route(value = "components/app-layout", layout = MainLayout.class)
public class AppLayoutSampleView extends AppPageLayout implements LocaleChangeObserver {

    private final H1 pageTitle;

    private final H2 drawerSectionTitle;
    private final Span drawerSectionDescription;
    private final AppLayout drawerAppLayout;
    private final H3 drawerBrandTitle;
    private final Span drawerBrandSubtitle;
    private final Span drawerOverviewLabel;
    private final Span drawerSalesLabel;
    private final Span drawerIntegrationsLabel;
    private final Span drawerFooterLabel;
    private final H3 drawerContentHeading;
    private final Paragraph drawerContentBody;

    private final H2 navbarSectionTitle;
    private final Span navbarSectionDescription;
    private final AppLayout navbarAppLayout;
    private final H3 navbarBrandTitle;
    private final Span navbarBrandSubtitle;
    private final Tab navbarOverviewTab;
    private final Span navbarOverviewLabel;
    private final Tab navbarReleasesTab;
    private final Span navbarReleasesLabel;
    private final Tab navbarTeamTab;
    private final Span navbarTeamLabel;
    private final Button navbarInviteButton;
    private final Button navbarHelpButton;
    private final H3 navbarContentHeading;
    private final Paragraph navbarContentBody;

    private final H2 responsiveSectionTitle;
    private final Span responsiveSectionDescription;
    private final AppLayout responsiveAppLayout;
    private final H3 responsiveBrandTitle;
    private final Span responsiveBrandSubtitle;
    private final H3 responsiveDrawerHeading;
    private final Paragraph responsiveDrawerDescription;
    private final H3 responsiveContentHeading;
    private final Paragraph responsiveContentDescription;
    private final Paragraph responsiveHint;
    private final Button responsiveOpenButton;
    private final Button responsiveCloseButton;

    public AppLayoutSampleView() {
        pageTitle = createPageTitle(getTranslation("components.appLayout.title"));

        drawerSectionTitle = new H2();
        drawerSectionDescription = createSubtitle();
        drawerBrandTitle = new H3();
        drawerBrandSubtitle = new Span();
        drawerOverviewLabel = new Span();
        drawerSalesLabel = new Span();
        drawerIntegrationsLabel = new Span();
        drawerFooterLabel = new Span();
        drawerContentHeading = new H3();
        drawerContentBody = new Paragraph();
        drawerAppLayout = buildDrawerExample();
        VerticalLayout drawerCard = createCard(drawerSectionTitle, drawerSectionDescription, drawerAppLayout);
        drawerCard.addClassName("stack-lg");
        add(drawerCard);

        navbarSectionTitle = new H2();
        navbarSectionDescription = createSubtitle();
        navbarBrandTitle = new H3();
        navbarBrandSubtitle = new Span();
        navbarOverviewLabel = new Span();
        navbarReleasesLabel = new Span();
        navbarTeamLabel = new Span();
        navbarInviteButton = new Button();
        navbarHelpButton = new Button();
        navbarContentHeading = new H3();
        navbarContentBody = new Paragraph();
        navbarOverviewTab = new Tab(navbarOverviewLabel);
        navbarReleasesTab = new Tab(navbarReleasesLabel);
        navbarTeamTab = new Tab(navbarTeamLabel);
        navbarAppLayout = buildNavbarExample();
        VerticalLayout navbarCard = createCard(navbarSectionTitle, navbarSectionDescription, navbarAppLayout);
        navbarCard.addClassName("stack-lg");
        add(navbarCard);

        responsiveSectionTitle = new H2();
        responsiveSectionDescription = createSubtitle();
        responsiveBrandTitle = new H3();
        responsiveBrandSubtitle = new Span();
        responsiveDrawerHeading = new H3();
        responsiveDrawerDescription = new Paragraph();
        responsiveContentHeading = new H3();
        responsiveContentDescription = new Paragraph();
        responsiveHint = new Paragraph();
        responsiveOpenButton = new Button();
        responsiveCloseButton = new Button();
        responsiveAppLayout = buildResponsiveExample();
        VerticalLayout responsiveCard = createCard(responsiveSectionTitle, responsiveSectionDescription, responsiveAppLayout);
        responsiveCard.addClassName("stack-lg");
        add(responsiveCard);

        updateTexts();
    }

    private Span createSubtitle() {
        Span subtitle = new Span();
        subtitle.addClassName("page-subtitle");
        return subtitle;
    }

    private AppLayout buildDrawerExample() {
        AppLayout layout = createPreviewContainer();
        layout.setPrimarySection(AppLayout.Section.DRAWER);

        DrawerToggle toggle = new DrawerToggle();
        drawerBrandTitle.getStyle().set("margin", "0");
        drawerBrandSubtitle.getStyle().set("color", "var(--text-secondary)");

        VerticalLayout brandWrapper = new VerticalLayout(drawerBrandTitle, drawerBrandSubtitle);
        brandWrapper.setSpacing(false);
        brandWrapper.setPadding(false);
        brandWrapper.addClassName("stack-xs");

        layout.addToNavbar(true, toggle, brandWrapper);

        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.addClassName("stack-md");

        Anchor overviewLink = createDrawerLink(drawerOverviewLabel, VaadinIcon.DASHBOARD);

        Anchor salesLink = createDrawerLink(drawerSalesLabel, VaadinIcon.TRENDING_UP);

        Anchor integrationsLink = createDrawerLink(drawerIntegrationsLabel, VaadinIcon.PLUG);

        drawerFooterLabel.getStyle().set("color", "var(--text-secondary)");
        drawerFooterLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");

        drawerContent.add(overviewLink, salesLink, integrationsLink, drawerFooterLabel);
        layout.addToDrawer(drawerContent);

        drawerContentHeading.getStyle().set("margin", "0");

        VerticalLayout content = new VerticalLayout(drawerContentHeading, drawerContentBody);
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassName("stack-sm");
        layout.setContent(content);

        return layout;
    }

    private AppLayout buildNavbarExample() {
        AppLayout layout = createPreviewContainer();
        layout.setPrimarySection(AppLayout.Section.NAVBAR);

        navbarBrandTitle.getStyle().set("margin", "0");
        navbarBrandSubtitle.getStyle().set("color", "var(--text-secondary)");
        navbarBrandSubtitle.getStyle().set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout brand = new VerticalLayout(navbarBrandTitle, navbarBrandSubtitle);
        brand.setSpacing(false);
        brand.setPadding(false);
        brand.addClassName("stack-xs");

        Tabs navigationTabs = new Tabs();
        navigationTabs.setWidthFull();

        navigationTabs.add(navbarOverviewTab, navbarReleasesTab, navbarTeamTab);

        navbarInviteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        navbarHelpButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(navbarInviteButton, navbarHelpButton);
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        layout.addToNavbar(brand);
        layout.addToNavbar(navigationTabs);
        layout.addToNavbar(actions);

        navbarContentHeading.getStyle().set("margin", "0");

        VerticalLayout content = new VerticalLayout(navbarContentHeading, navbarContentBody);
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassName("stack-sm");
        layout.setContent(content);

        return layout;
    }

    private AppLayout buildResponsiveExample() {
        AppLayout layout = createPreviewContainer();
        layout.setPrimarySection(AppLayout.Section.DRAWER);
        layout.setDrawerOpened(false);

        DrawerToggle toggle = new DrawerToggle();
        responsiveBrandTitle.getStyle().set("margin", "0");
        responsiveBrandSubtitle.getStyle().set("color", "var(--text-secondary)");
        responsiveBrandSubtitle.getStyle().set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout brand = new VerticalLayout(responsiveBrandTitle, responsiveBrandSubtitle);
        brand.setSpacing(false);
        brand.setPadding(false);
        brand.addClassName("stack-xs");

        layout.addToNavbar(true, toggle, brand);

        responsiveDrawerHeading.getStyle().set("margin", "0");
        responsiveDrawerDescription.getStyle().set("font-size", "var(--lumo-font-size-s)");
        responsiveDrawerDescription.getStyle().set("color", "var(--text-secondary)");

        VerticalLayout drawer = new VerticalLayout(responsiveDrawerHeading, responsiveDrawerDescription);
        drawer.setPadding(false);
        drawer.setSpacing(false);
        drawer.addClassName("stack-sm");
        layout.addToDrawer(drawer);

        responsiveContentHeading.getStyle().set("margin", "0");
        responsiveHint.getStyle().set("font-size", "var(--lumo-font-size-s)");
        responsiveHint.getStyle().set("color", "var(--text-secondary)");

        responsiveOpenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        responsiveOpenButton.addClickListener(event -> layout.setDrawerOpened(true));

        responsiveCloseButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        responsiveCloseButton.addClickListener(event -> layout.setDrawerOpened(false));

        HorizontalLayout controls = new HorizontalLayout(responsiveOpenButton, responsiveCloseButton);
        controls.setSpacing(true);
        controls.setPadding(false);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout content = new VerticalLayout(responsiveContentHeading, responsiveContentDescription, controls, responsiveHint);
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassName("stack-sm");
        layout.setContent(content);

        return layout;
    }

    private AppLayout createPreviewContainer() {
        AppLayout layout = new AppLayout();
        layout.getElement().getStyle().set("width", "100%");
        layout.getElement().getStyle().set("height", "360px");
        layout.getElement().getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        layout.getElement().getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        layout.getElement().getStyle().set("overflow", "hidden");
        layout.getElement().getStyle().set("background", "var(--lumo-base-color)");
        return layout;
    }

    private Anchor createDrawerLink(Span label, VaadinIcon icon) {
        Icon navIcon = icon.create();
        navIcon.setSize("var(--lumo-icon-size-s)");
        Anchor link = new Anchor();
        link.setHref("#");
        link.getElement().getStyle().set("display", "flex");
        link.getElement().getStyle().set("align-items", "center");
        link.getElement().getStyle().set("gap", "var(--space-2)");
        link.getElement().getStyle().set("text-decoration", "none");
        link.getElement().getStyle().set("color", "var(--text-secondary)");
        link.getElement().getStyle().set("padding", "var(--space-2) 0");
        label.getStyle().set("font-size", "var(--lumo-font-size-m)");
        link.add(navIcon, label);
        return link;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        pageTitle.setText(getTranslation("components.appLayout.title"));

        drawerSectionTitle.setText(getTranslation("components.appLayout.drawer.title"));
        drawerSectionDescription.setText(getTranslation("components.appLayout.drawer.description"));
        drawerBrandTitle.setText(getTranslation("components.appLayout.drawer.brand"));
        drawerBrandSubtitle.setText(getTranslation("components.appLayout.drawer.subtitle"));
        drawerOverviewLabel.setText(getTranslation("components.appLayout.drawer.nav.overview"));
        drawerSalesLabel.setText(getTranslation("components.appLayout.drawer.nav.sales"));
        drawerIntegrationsLabel.setText(getTranslation("components.appLayout.drawer.nav.integrations"));
        drawerFooterLabel.setText(getTranslation("components.appLayout.drawer.footer"));
        drawerContentHeading.setText(getTranslation("components.appLayout.drawer.content.title"));
        drawerContentBody.setText(getTranslation("components.appLayout.drawer.content.body"));

        navbarSectionTitle.setText(getTranslation("components.appLayout.navbar.title"));
        navbarSectionDescription.setText(getTranslation("components.appLayout.navbar.description"));
        navbarBrandTitle.setText(getTranslation("components.appLayout.navbar.brand"));
        navbarBrandSubtitle.setText(getTranslation("components.appLayout.navbar.subtitle"));
        navbarOverviewLabel.setText(getTranslation("components.appLayout.navbar.tab.overview"));
        navbarReleasesLabel.setText(getTranslation("components.appLayout.navbar.tab.releases"));
        navbarTeamLabel.setText(getTranslation("components.appLayout.navbar.tab.team"));
        navbarInviteButton.setText(getTranslation("components.appLayout.navbar.action.invite"));
        navbarHelpButton.setText(getTranslation("components.appLayout.navbar.action.help"));
        navbarContentHeading.setText(getTranslation("components.appLayout.navbar.content.title"));
        navbarContentBody.setText(getTranslation("components.appLayout.navbar.content.body"));

        responsiveSectionTitle.setText(getTranslation("components.appLayout.responsive.title"));
        responsiveSectionDescription.setText(getTranslation("components.appLayout.responsive.description"));
        responsiveBrandTitle.setText(getTranslation("components.appLayout.responsive.brand"));
        responsiveBrandSubtitle.setText(getTranslation("components.appLayout.responsive.subtitle"));
        responsiveDrawerHeading.setText(getTranslation("components.appLayout.responsive.drawer.title"));
        responsiveDrawerDescription.setText(getTranslation("components.appLayout.responsive.drawer.description"));
        responsiveContentHeading.setText(getTranslation("components.appLayout.responsive.content.title"));
        responsiveContentDescription.setText(getTranslation("components.appLayout.responsive.content.description"));
        responsiveOpenButton.setText(getTranslation("components.appLayout.responsive.action.open"));
        responsiveCloseButton.setText(getTranslation("components.appLayout.responsive.action.close"));
        responsiveHint.setText(getTranslation("components.appLayout.responsive.hint"));

        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("components.appLayout.title"));
        }
    }
}
