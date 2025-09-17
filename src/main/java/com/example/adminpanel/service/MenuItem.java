package com.example.adminpanel.service;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Simple data holder representing a single menu entry.  Each menu item
 * belongs to a group (for example, "general" or "other"), has a
 * translation key for its label, an optional Vaadin icon, and a view
 * class to navigate to.  When the {@code view} is {@code null}, the
 * item will not trigger navigation and can instead show a placeholder
 * action such as a notification.
 */
public class MenuItem {
    private final String group;
    private final String labelKey;
    private final VaadinIcon icon;
    private final Class<? extends Component> view;
    private final java.util.List<MenuItem> children;

    /**
     * Constructs a MenuItem with no children.  This is the typical top‑level
     * constructor used for leaf menu items.  All parameters are required and
     * children are assumed to be empty.
     *
     * @param group    the group key used for top‑level grouping
     * @param labelKey the translation key for the item label (under the prefix {@code menu.})
     * @param icon     optional Vaadin icon to display; may be {@code null}
     * @param view     the view class to navigate to when clicked; may be {@code null} for non‑navigating items
     */
    public MenuItem(String group, String labelKey, VaadinIcon icon, Class<? extends Component> view) {
        this(group, labelKey, icon, view, java.util.Collections.emptyList());
    }

    /**
     * Constructs a MenuItem that may have child items.  Use this
     * constructor to define items with a submenu.  When {@code children}
     * contains one or more items, clicking this item will not navigate
     * directly but instead expand to show the children.
     *
     * @param group    the group key used for top‑level grouping
     * @param labelKey the translation key for the item label (under the prefix {@code menu.})
     * @param icon     optional Vaadin icon to display; may be {@code null}
     * @param view     the view class to navigate to when clicked; ignored when children are present
     * @param children the list of child menu items; may be {@code null} or empty for no children
     */
    public MenuItem(String group, String labelKey, VaadinIcon icon, Class<? extends Component> view,
                    java.util.List<MenuItem> children) {
        this.group = group;
        this.labelKey = labelKey;
        this.icon = icon;
        this.view = view;
        // Normalize children list to empty to avoid null checks
        this.children = children != null ? java.util.List.copyOf(children) : java.util.Collections.emptyList();
    }

    public String getGroup() {
        return group;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public VaadinIcon getIcon() {
        return icon;
    }

    public Class<? extends Component> getView() {
        return view;
    }

    /**
     * Returns the child items of this menu item.  The list may be empty but
     * will never be {@code null}.
     *
     * @return immutable list of child menu items
     */
    public java.util.List<MenuItem> getChildren() {
        return children;
    }

    /**
     * Indicates whether this menu item has child items.  When {@code true},
     * clicking the item should expand or collapse the submenu instead of
     * immediately navigating.
     *
     * @return {@code true} if children are present
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }
}