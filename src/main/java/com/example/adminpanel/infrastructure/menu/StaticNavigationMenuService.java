package com.example.adminpanel.infrastructure.menu;

import com.example.adminpanel.application.menu.NavigationMenuService;
import com.example.adminpanel.domain.menu.MenuItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Static implementation of {@link NavigationMenuService} used for the demo
 * application.  The menu structure is defined in code and exposes route names
 * instead of Vaadin view classes so the UI can remain decoupled from concrete
 * navigation targets.
 */
@Service
public class StaticNavigationMenuService implements NavigationMenuService {

    private static final Logger log = LoggerFactory.getLogger(StaticNavigationMenuService.class);

    @Override
    public List<MenuItem> getMenuItemsForCurrentUser() {
        log.debug("Building static navigation menu for current user");
        MenuItem personsMenu = new MenuItem(
                "general",
                "menu.persons",
                VaadinIcon.USERS,
                null,
                List.of(
                        new MenuItem("general", "menu.persons.list", VaadinIcon.LIST, "persons"),
                        new MenuItem("general", "menu.persons.add", VaadinIcon.PLUS, null)
                )
        );

        MenuItem examplesMenu = new MenuItem(
                "examples",
                "menu.examples",
                VaadinIcon.TABLE,
                null,
                List.of(
                        new MenuItem("examples", "menu.fullgrid", VaadinIcon.LIST, "full-grid"),
                        new MenuItem("examples", "menu.compactgrid", VaadinIcon.LIST, "compact-grid"),
                        new MenuItem("examples", "menu.multigrids", VaadinIcon.LIST, "multi-grids")
                )
        );

        MenuItem formsMenu = new MenuItem(
                "forms",
                "menu.forms",
                VaadinIcon.CLIPBOARD_TEXT,
                null,
                List.of(
                        new MenuItem("forms", "menu.formgeneration", VaadinIcon.CLIPBOARD_TEXT, "forms")
                )
        );

        List<MenuItem> menu = List.of(
                new MenuItem("general", "menu.dashboard", VaadinIcon.DASHBOARD, ""),
                personsMenu,
                examplesMenu,
                formsMenu,
                new MenuItem("other", "menu.placeholder", VaadinIcon.CLIPBOARD_TEXT, null)
        );
        log.debug("Navigation menu built with {} root item(s)", menu.size());
        return menu;
    }
}
