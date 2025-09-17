package com.example.adminpanel.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;

@Route(value = "design-system", layout = MainLayout.class)
public class DesignSystemView extends VerticalLayout implements LocaleChangeObserver {

    private final H2 heading;
    private final H2 buttonsTitle;
    private final H2 badgesTitle;
    private final Button primaryButton;
    private final Button secondaryButton;
    private final Button infoButton;
    private final Button successButton;
    private final Button warningButton;
    private final Button dangerButton;
    private final Span primaryBadge;
    private final Span secondaryBadge;
    private final Span infoBadge;
    private final Span successBadge;
    private final Span warningBadge;
    private final Span dangerBadge;

    public DesignSystemView() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        heading = new H2();
        add(heading);

        Div buttons = responsiveGrid();
        primaryButton = btn("primary");
        secondaryButton = btn("secondary");
        infoButton = btn("info");
        successButton = btn("success");
        warningButton = btn("warning");
        dangerButton = btn("danger");
        buttons.add(primaryButton, secondaryButton, infoButton, successButton, warningButton, dangerButton);
        buttonsTitle = new H2();
        add(card(buttonsTitle, buttons));

        Div badges = responsiveGrid();
        primaryBadge = badge("primary");
        secondaryBadge = badge("secondary");
        infoBadge = badge("info");
        successBadge = badge("success");
        warningBadge = badge("warning");
        dangerBadge = badge("danger");
        badges.add(primaryBadge, secondaryBadge, infoBadge, successBadge, warningBadge, dangerBadge);
        badgesTitle = new H2();
        add(card(badgesTitle, badges));

        updateTexts();
        updatePageTitle();
    }

    private Div responsiveGrid() {
        Div grid = new Div();
        grid.getStyle().set("display", "grid");
        grid.getStyle().set("grid-template-columns", "repeat(auto-fit, minmax(140px, 1fr))");
        grid.getStyle().set("gap", "var(--space-3)");
        grid.setWidthFull();
        return grid;
    }

    private Button btn(String themeName) {
        Button b = new Button();
        b.getElement().setAttribute("theme", themeName);
        b.setWidthFull();
        return b;
    }

    private Span badge(String theme) {
        Span s = new Span();
        s.getElement().setAttribute("theme", "badge");
        s.getElement().getClassList().add(theme);
        return s;
    }

    private VerticalLayout card(H2 title, com.vaadin.flow.component.Component content) {
        VerticalLayout card = new VerticalLayout();
        card.add(title);
        card.add(content);
        card.setPadding(true);
        card.setSpacing(true);
        card.setWidthFull();
        card.getElement().getClassList().add("app-card");
        return card;
    }

    private void updateTexts() {
        heading.setText(getTranslation("designSystem.heading"));
        buttonsTitle.setText(getTranslation("designSystem.buttonsCard"));
        badgesTitle.setText(getTranslation("designSystem.badgesCard"));
        primaryButton.setText(getTranslation("designSystem.primary"));
        secondaryButton.setText(getTranslation("designSystem.secondary"));
        infoButton.setText(getTranslation("designSystem.info"));
        successButton.setText(getTranslation("designSystem.success"));
        warningButton.setText(getTranslation("designSystem.warning"));
        dangerButton.setText(getTranslation("designSystem.danger"));
        primaryBadge.setText(getTranslation("designSystem.primary"));
        secondaryBadge.setText(getTranslation("designSystem.secondary"));
        infoBadge.setText(getTranslation("designSystem.info"));
        successBadge.setText(getTranslation("designSystem.success"));
        warningBadge.setText(getTranslation("designSystem.warning"));
        dangerBadge.setText(getTranslation("designSystem.danger"));
    }

    private void updatePageTitle() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("designSystem.title"));
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
        updatePageTitle();
    }
}
