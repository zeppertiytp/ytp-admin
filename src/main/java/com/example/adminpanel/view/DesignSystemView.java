package com.example.adminpanel.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;

import java.util.LinkedHashMap;
import java.util.Map;

@Route(value = "design-system", layout = MainLayout.class)
public class DesignSystemView extends ViewFrame implements LocaleChangeObserver {

    private final H2 pageHeading;
    private final Paragraph description;
    private final H3 foundationsTitle;
    private final Paragraph spacingPrinciple;
    private final Paragraph typographyPrinciple;
    private final Paragraph surfacesPrinciple;
    private final H3 spacingTitle;
    private final H3 typographyTitle;

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
    private final H3 buttonsTitle;
    private final H3 badgesTitle;

    private final Map<String, Span> spacingLabels = new LinkedHashMap<>();
    private final Map<String, Span> typeLabels = new LinkedHashMap<>();

    public DesignSystemView() {
        VerticalLayout content = createContentSection();
        content.addClassName("design-system-view");
        content.getStyle().set("gap", "var(--space-xl)");

        pageHeading = new H2();
        pageHeading.addClassNames("view-title");
        description = new Paragraph();
        description.addClassName("text-muted");
        content.add(pageHeading, description);

        VerticalLayout foundationsCard = createCard();
        foundationsTitle = new H3();
        foundationsTitle.addClassName("section-title");
        spacingPrinciple = new Paragraph();
        typographyPrinciple = new Paragraph();
        surfacesPrinciple = new Paragraph();
        foundationsCard.add(foundationsTitle, spacingPrinciple, typographyPrinciple, surfacesPrinciple);
        content.add(foundationsCard);

        VerticalLayout spacingCard = createCard();
        spacingTitle = new H3();
        spacingTitle.addClassName("section-title");
        Div spacingTokens = buildSpacingTokens();
        spacingCard.add(spacingTitle, spacingTokens);
        content.add(spacingCard);

        VerticalLayout typographyCard = createCard();
        typographyTitle = new H3();
        typographyTitle.addClassName("section-title");
        Div typographyTokens = buildTypeTokens();
        typographyCard.add(typographyTitle, typographyTokens);
        content.add(typographyCard);

        Div buttonsGrid = responsiveGrid();
        primaryButton = themedButton("primary");
        secondaryButton = themedButton("secondary");
        infoButton = themedButton("info");
        successButton = themedButton("success");
        warningButton = themedButton("warning");
        dangerButton = themedButton("danger");
        buttonsGrid.add(primaryButton, secondaryButton, infoButton, successButton, warningButton, dangerButton);
        buttonsTitle = new H3();
        buttonsTitle.addClassName("section-title");
        VerticalLayout buttonsCard = createCard(buttonsTitle, buttonsGrid);
        content.add(buttonsCard);

        Div badgesGrid = responsiveGrid();
        primaryBadge = badge("primary");
        secondaryBadge = badge("secondary");
        infoBadge = badge("info");
        successBadge = badge("success");
        warningBadge = badge("warning");
        dangerBadge = badge("danger");
        badgesGrid.add(primaryBadge, secondaryBadge, infoBadge, successBadge, warningBadge, dangerBadge);
        badgesTitle = new H3();
        badgesTitle.addClassName("section-title");
        VerticalLayout badgesCard = createCard(badgesTitle, badgesGrid);
        content.add(badgesCard);

        updateTexts();
        updatePageTitle();
    }

    private VerticalLayout createCard() {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("surface-card", "view-section");
        card.setPadding(false);
        card.setSpacing(false);
        card.setWidthFull();
        card.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        card.getStyle().set("gap", "var(--space-m)");
        return card;
    }

    private VerticalLayout createCard(H3 title, Component body) {
        VerticalLayout card = createCard();
        title.addClassName("section-title");
        card.add(title, body);
        return card;
    }

    private Div responsiveGrid() {
        Div grid = new Div();
        grid.addClassNames("token-grid");
        grid.getStyle().set("grid-template-columns", "repeat(auto-fit, minmax(180px, 1fr))");
        return grid;
    }

    private Div buildSpacingTokens() {
        Div grid = responsiveGrid();
        grid.addClassName("spacing-token-grid");
        grid.add(spacingSwatch("--space-2xs", "designSystem.space.2xs"));
        grid.add(spacingSwatch("--space-xs", "designSystem.space.xs"));
        grid.add(spacingSwatch("--space-s", "designSystem.space.s"));
        grid.add(spacingSwatch("--space-m", "designSystem.space.m"));
        grid.add(spacingSwatch("--space-l", "designSystem.space.l"));
        grid.add(spacingSwatch("--space-xl", "designSystem.space.xl"));
        grid.add(spacingSwatch("--space-2xl", "designSystem.space.2xl"));
        return grid;
    }

    private Component spacingSwatch(String token, String labelKey) {
        Div wrapper = new Div();
        wrapper.addClassName("token-swatch");
        Div sample = new Div();
        sample.addClassName("token-swatch__sample");
        sample.getStyle().set("height", "var(" + token + ")");
        wrapper.add(sample);
        Span label = new Span();
        label.addClassName("token-swatch__label");
        spacingLabels.put(labelKey, label);
        wrapper.add(label);
        return wrapper;
    }

    private Div buildTypeTokens() {
        Div grid = responsiveGrid();
        grid.addClassName("type-token-grid");
        grid.add(typeSwatch("--font-size-sm", "designSystem.type.sm"));
        grid.add(typeSwatch("--font-size-base", "designSystem.type.base"));
        grid.add(typeSwatch("--font-size-lg", "designSystem.type.lg"));
        grid.add(typeSwatch("--font-size-xl", "designSystem.type.xl"));
        return grid;
    }

    private Component typeSwatch(String token, String labelKey) {
        Div wrapper = new Div();
        wrapper.addClassName("token-swatch");
        Span sample = new Span("Aa");
        sample.addClassName("token-swatch__type");
        sample.getStyle().set("font-size", "var(" + token + ")");
        wrapper.add(sample);
        Span label = new Span();
        label.addClassName("token-swatch__label");
        typeLabels.put(labelKey, label);
        wrapper.add(label);
        return wrapper;
    }

    private Button themedButton(String themeName) {
        Button button = new Button();
        button.getElement().setAttribute("theme", themeName);
        button.setWidthFull();
        return button;
    }

    private Span badge(String theme) {
        Span badge = new Span();
        badge.getElement().setAttribute("theme", "badge");
        badge.getElement().getClassList().add(theme);
        badge.addClassName("badge-token");
        return badge;
    }

    private void updateTexts() {
        pageHeading.setText(getTranslation("designSystem.title"));
        description.setText(getTranslation("designSystem.description"));
        foundationsTitle.setText(getTranslation("designSystem.foundationsCard"));
        spacingPrinciple.setText(getTranslation("designSystem.foundation.spacing"));
        typographyPrinciple.setText(getTranslation("designSystem.foundation.typography"));
        surfacesPrinciple.setText(getTranslation("designSystem.foundation.surfaces"));
        spacingTitle.setText(getTranslation("designSystem.spacingCard"));
        typographyTitle.setText(getTranslation("designSystem.typographyCard"));
        buttonsTitle.setText(getTranslation("designSystem.buttonsCard"));
        badgesTitle.setText(getTranslation("designSystem.badgesCard"));

        spacingLabels.forEach((key, label) -> label.setText(getTranslation(key)));
        typeLabels.forEach((key, label) -> label.setText(getTranslation(key)));

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
