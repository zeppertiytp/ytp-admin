package com.youtopin.vaadin.navigation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Simple data holder representing a single menu entry.  Each menu item
 * belongs to a group (for example, "general" or "other"), has a
 * translation key for its label, an optional Iconoir icon name, and an
 * optional navigation target (a Vaadin route identifier).  When no
 * navigation target is provided the item can be used to open dialogs or
 * trigger other UI actions instead of navigating.
 */
public final class MenuItem {
    private final String group;
    private final String labelKey;
    private final String iconName;
    private final Optional<String> navigationTarget;
    private final List<MenuItem> children;

    /**
     * Constructs a MenuItem with no children.  This is the typical top‑level
     * constructor used for leaf menu items.  All parameters are required and
     * children are assumed to be empty.
     *
     * @param group    the group key used for top‑level grouping
     * @param labelKey the translation key for the item label (under the prefix {@code menu.})
     * @param iconName optional Iconoir symbol name to display; may be {@code null}
     * @param navigationTarget optional Vaadin route to navigate to; may be {@code null} for non‑navigating items
     */
    public MenuItem(String group, String labelKey, String iconName, String navigationTarget) {
        this(group, labelKey, iconName, navigationTarget, Collections.emptyList());
    }

    /**
     * Constructs a MenuItem that may have child items.  Use this
     * constructor to define items with a submenu.  When {@code children}
     * contains one or more items, clicking this item will not navigate
     * directly but instead expand to show the children.
     *
     * @param group    the group key used for top‑level grouping
     * @param labelKey the translation key for the item label (under the prefix {@code menu.})
     * @param iconName optional Iconoir symbol name to display; may be {@code null}
     * @param navigationTarget optional Vaadin route to navigate to; ignored when children are present
     * @param children the list of child menu items; may be {@code null} or empty for no children
     */
    public MenuItem(String group, String labelKey, String iconName, String navigationTarget,
                    List<MenuItem> children) {
        this.group = Objects.requireNonNull(group, "group must not be null");
        this.labelKey = Objects.requireNonNull(labelKey, "labelKey must not be null");
        this.iconName = normalize(iconName);
        this.navigationTarget = Optional.ofNullable(navigationTarget);
        this.children = children != null ? List.copyOf(children) : Collections.emptyList();
    }

    public String getGroup() {
        return group;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public Optional<String> getIconName() {
        return Optional.ofNullable(iconName);
    }

    public Optional<String> getNavigationTarget() {
        return navigationTarget;
    }

    public boolean hasNavigationTarget() {
        return navigationTarget.isPresent();
    }

    /**
     * Returns the child items of this menu item.  The list may be empty but
     * will never be {@code null}.
     *
     * @return immutable list of child menu items
     */
    public List<MenuItem> getChildren() {
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

    private static String normalize(String iconName) {
        if (iconName == null) {
            return null;
        }
        String trimmed = iconName.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
