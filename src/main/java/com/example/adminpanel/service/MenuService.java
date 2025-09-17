package com.example.adminpanel.service;

import com.example.adminpanel.view.DashboardView;
import com.example.adminpanel.view.FullGridView;
import com.example.adminpanel.view.CompactGridView;
import com.example.adminpanel.view.MultipleGridsView;
import com.example.adminpanel.view.FormGenerationView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service providing menu configuration based on the current user.  In a
 * real application, this service could query the database or inspect
 * user roles to determine which items should be visible.  Here we
 * return a static list of items for demonstration purposes.
 */
@Service
public class MenuService {

    /**
     * Returns the menu items available to the current user.  The list is
     * grouped by the {@link MenuItem#getGroup()} property.  Keys should
     * correspond to translation keys under the prefix {@code menu.},
     * e.g. a group named "general" will be translated using
     * {@code menu.general}.
     *
     * @return menu items for the current user
     */
    public List<MenuItem> getMenuItemsForCurrentUser() {
        // In a real application, inspect the authentication principal or
        // call repositories here to determine the appropriate menu items.
        // To demonstrate submenu capability, the "persons" entry includes two
        // children: one to list all persons and another placeholder to add a person.
        // If you do not wish to display submenus, simply leave the children list empty.
        MenuItem personsMenu = new MenuItem(
                "general",
                "menu.persons",
                VaadinIcon.USERS,
                null,
                java.util.List.of(
                        new MenuItem("general", "menu.persons.list", VaadinIcon.LIST, com.example.adminpanel.view.PersonTableView.class),
                        new MenuItem("general", "menu.persons.add", VaadinIcon.PLUS, null)
                )
        );
        // Build examples menu with custom grid demos.  The group key "examples" is used
        // for translations under menu.examples.  Each child item links to a separate
        // demonstration view to illustrate different grid height and feature configurations.
        MenuItem examplesMenu = new MenuItem(
                "examples",
                "menu.examples",
                VaadinIcon.TABLE,
                null,
                java.util.List.of(
                        new MenuItem("examples", "menu.fullgrid", VaadinIcon.LIST, FullGridView.class),
                        new MenuItem("examples", "menu.compactgrid", VaadinIcon.LIST, CompactGridView.class),
                        new MenuItem("examples", "menu.multigrids", VaadinIcon.LIST, MultipleGridsView.class)
                )
        );

        // Forms menu demonstrating dynamic form generation.  The group key
        // "forms" corresponds to translations under menu.forms.  A
        // single child item links to the form generation demonstration view.
        // Use a clipboard icon for the forms menu instead of VaadinIcon.FORM.  Some
        // versions of Vaadin do not include a dedicated FORM icon, which can
        // lead to compilation errors.  The clipboard icon conveys the notion
        // of documents or forms and is available across Vaadin versions.
        MenuItem formsMenu = new MenuItem(
                "forms",
                "menu.forms",
                VaadinIcon.CLIPBOARD_TEXT,
                null,
                java.util.List.of(
                        new MenuItem("forms", "menu.formgeneration", VaadinIcon.CLIPBOARD_TEXT, com.example.adminpanel.view.FormGenerationView.class)
                )
        );
        return List.of(
                new MenuItem("general", "menu.dashboard", VaadinIcon.DASHBOARD, DashboardView.class),
                personsMenu,
                examplesMenu,
                formsMenu,
                new MenuItem("other", "menu.placeholder", VaadinIcon.CLIPBOARD_TEXT, null)
        );
    }
}