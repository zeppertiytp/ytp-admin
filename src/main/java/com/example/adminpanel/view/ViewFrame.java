package com.example.adminpanel.view;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Base layout for application views that enforces the design system spacing
 * and width constraints. Views extending this class automatically get the
 * {@code view-frame} styling hook and helper methods for building content
 * sections with consistent paddings.
 */
public abstract class ViewFrame extends VerticalLayout {

    protected ViewFrame() {
        addClassName("view-frame");
        setSpacing(false);
        setPadding(false);
        setWidthFull();
        setHeightFull();
        setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
    }

    /**
     * Creates a default-width content section that follows the design system
     * max-width and spacing rules.
     */
    protected VerticalLayout createContentSection() {
        return createContentSection(ContentWidth.DEFAULT);
    }

    /**
     * Creates a content section with the requested width profile.
     *
     * @param width the preferred width behaviour for the section
     * @return a configured {@link VerticalLayout} added to this frame
     */
    protected VerticalLayout createContentSection(ContentWidth width) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("view-content");
        if (width == ContentWidth.NARROW) {
            section.addClassName("view-content--narrow");
        } else if (width == ContentWidth.FULL) {
            section.addClassName("view-content--full");
        }
        section.setSpacing(false);
        section.setPadding(false);
        section.setWidthFull();
        section.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        add(section);
        if (width == ContentWidth.FULL) {
            setFlexGrow(1, section);
        }
        return section;
    }

    /** Width presets for view content sections. */
    protected enum ContentWidth {
        DEFAULT,
        NARROW,
        FULL
    }
}
