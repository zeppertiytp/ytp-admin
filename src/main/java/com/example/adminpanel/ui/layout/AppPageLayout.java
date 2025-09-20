package com.example.adminpanel.ui.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Base layout for all application views. Applies the shared theme classes that
 * control the maximum content width, spacing rhythm and alignment so every page
 * renders inside the same visual container.
 */
public class AppPageLayout extends VerticalLayout {

    public AppPageLayout() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        addClassName("app-page");
    }

    /**
     * Creates and adds a page title element that follows the design system
     * typography rules.
     *
     * @param text translated text for the heading
     * @return the created {@link H1}
     */
    protected H1 createPageTitle(String text) {
        H1 title = new H1(text);
        title.addClassName("page-title");
        add(title);
        return title;
    }

    /**
     * Utility helper that wraps the provided components in a themed card
     * surface. Cards stretch to the available width and stack their children
     * vertically with the default medium gap.
     *
     * @param components components to include inside the card
     * @return the configured {@link VerticalLayout}
     */
    protected VerticalLayout createCard(Component... components) {
        VerticalLayout card = new VerticalLayout(components);
        card.setPadding(false);
        card.setSpacing(false);
        card.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        card.addClassNames("app-card", "stack-md");
        card.setWidthFull();
        return card;
    }
}
