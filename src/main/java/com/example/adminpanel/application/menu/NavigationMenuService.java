package com.example.adminpanel.application.menu;

import com.example.adminpanel.domain.menu.MenuItem;

import java.util.List;

/**
 * Application service responsible for providing the navigation structure
 * for the authenticated user.  Implementations can source menu metadata
 * from configuration, databases or remote APIs.  UI components consume
 * this service via the {@link MenuItem} aggregate so that navigation
 * decisions remain decoupled from Vaadin view classes.
 */
public interface NavigationMenuService {

    /**
     * Returns the menu items that should be displayed for the current user.
     *
     * @return ordered list of menu items grouped by {@link MenuItem#getGroup()}
     */
    List<MenuItem> getMenuItemsForCurrentUser();
}
