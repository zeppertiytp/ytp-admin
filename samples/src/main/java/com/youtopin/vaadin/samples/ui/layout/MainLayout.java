package com.youtopin.vaadin.samples.ui.layout;

import com.youtopin.vaadin.samples.application.menu.NavigationMenuService;
import com.youtopin.vaadin.samples.application.security.SecurityService;
import com.youtopin.vaadin.navigation.MenuItem;
import com.youtopin.vaadin.i18n.LocaleDirectionService;
import com.youtopin.vaadin.component.AppIcon;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main shell layout (header + drawer).
 * NOTE: Do NOT put @Theme here—it's on AppShell (as required by Vaadin).
 */
@CssImport("./styles/vazir-font.css")
@CssImport(value = "./styles/rtl-fix.css", themeFor = "vaadin-app-layout")
public class MainLayout extends AppLayout implements LocaleChangeObserver {

    private final SecurityService securityService;
    private final NavigationMenuService navigationMenuService;
    private final LocaleDirectionService localeDirectionService;

    // Header
    private DrawerToggle toggle;
    private MenuBar userMenu;
    private Button languageButton;
    private Component themeItem;
    private Span userName;

    // Containers
    private VerticalLayout menuLayout;
    private HorizontalLayout header;
    private HorizontalLayout headerActions;
    private Span headerSpacer;

    // RTL helper: follow the UI element's dir
    private boolean isRtl() {
        String dir = UI.getCurrent().getElement().getAttribute("dir");
        return "rtl".equalsIgnoreCase(dir);
    }

    @Autowired
    public MainLayout(SecurityService securityService, NavigationMenuService navigationMenuService,
                      LocaleDirectionService localeDirectionService) {
        this.securityService = securityService;
        this.navigationMenuService = navigationMenuService;
        this.localeDirectionService = localeDirectionService;

        // Drawer is the primary section
        setPrimarySection(Section.DRAWER);

        createHeader();
        createDrawer();

        // Make sure header side matches current direction
        localeDirectionService.applyDirection(UI.getCurrent());
        updateHeaderOrder();
    }

    /* ------------------- Header ------------------- */

    private void createHeader() {
        toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", getTranslation("header.toggleMenu"));

        // Avatar + name (menu trigger)
        Avatar avatar = new Avatar("A");
        userName = new Span(getTranslation("profile.name"));
        HorizontalLayout userInfo = new HorizontalLayout(avatar, userName);
        userInfo.setSpacing(true);
        userInfo.setAlignItems(FlexComponent.Alignment.CENTER);

        // User menu
        userMenu = new MenuBar();
        userMenu.setOpenOnHover(true);
        ensureMenuOverlayMinWidth(userMenu);
        var userMenuRoot = userMenu.addItem(userInfo);
        var subMenu = userMenuRoot.getSubMenu();
        subMenu.addItem(getTranslation("header.profile"));
        subMenu.addItem(getTranslation("header.preferences"));
        themeItem = subMenu.addItem(getThemeLabel(), e -> toggleTheme());
        subMenu.addItem(getTranslation("header.logout"), e -> securityService.logout());

        // Language button shows the OTHER language (switch target)
        languageButton = new Button(getLanguageLabel());
        languageButton.addClickListener(click -> {
            UI ui = UI.getCurrent();
            Locale current = ui.getLocale();
            Locale next = "fa".equals(current.getLanguage()) ? Locale.ENGLISH : new Locale("fa");
            localeDirectionService.applyLocale(ui, next);
            ui.getSession().setAttribute("preferred-locale", next);
            updateHeaderOrder();
        });

        header = new HorizontalLayout();
        header.setPadding(true);
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        headerSpacer = new Span();
        headerSpacer.getStyle().set("flex", "1");

        headerActions = new HorizontalLayout(languageButton, userMenu);
        headerActions.setSpacing(true);
        headerActions.setAlignItems(FlexComponent.Alignment.CENTER);

        updateHeaderOrder();
        addToNavbar(header);
    }

    /** Rebuild header order so toggle sits above the drawer side. */
    private void updateHeaderOrder() {
        if (header == null || headerActions == null || headerSpacer == null || toggle == null) return;
        header.removeAll();
        // Always add in this order; container direction (dir=ltr/rtl) places them on the correct sides.
        header.add(toggle, headerSpacer, headerActions);
    }

    /* ------------------- Drawer ------------------- */

    private void createDrawer() {
        menuLayout = new VerticalLayout();
        menuLayout.setPadding(false);
        menuLayout.setSpacing(false);
        menuLayout.setSizeFull();
        menuLayout.addClassName("app-drawer");
        buildMenu();
        addToDrawer(menuLayout);
    }

    private void buildMenu() {
        if (menuLayout == null) return;

        menuLayout.removeAll();

        Map<String, List<MenuItem>> grouped =
                navigationMenuService.getMenuItemsForCurrentUser().stream()
                        .collect(Collectors.groupingBy(MenuItem::getGroup, LinkedHashMap::new, Collectors.toList()));

        grouped.forEach((group, items) -> {
            H6 groupHeader = new H6(getTranslation("menu." + group));
            groupHeader.addClassName("nav-section-title");

            VerticalLayout groupLayout = new VerticalLayout();
            groupLayout.setPadding(false);
            groupLayout.setSpacing(false);
            groupLayout.setWidthFull();
            groupLayout.addClassName("nav-group");

            Div section = new Div();
            section.setWidthFull();
            section.addClassName("nav-section");
            section.add(groupHeader, groupLayout);

            for (MenuItem item : items) {
                if (item.hasChildren()) {
                    // Parent item with children (use Details)
                    Component icon = item.getIconName()
                            .map(name -> new AppIcon(name, "20"))
                            .orElse(null);
                    Span label = new Span(getTranslation(item.getLabelKey()));

                    HorizontalLayout summary = createNavRow(icon, label);
                    summary.addClassName("nav-row--summary");
                    summary.getStyle().set("cursor", "pointer");

                    Icon caret = VaadinIcon.CHEVRON_DOWN.create();
                    caret.addClassName("nav-row__caret");
                    caret.addClassName("nav-row__caret--closed");
                    caret.getElement().setAttribute("aria-hidden", "true");
                    summary.add(caret);

                    VerticalLayout childrenLayout = new VerticalLayout();
                    childrenLayout.setPadding(false);
                    childrenLayout.setSpacing(false);
                    childrenLayout.setWidthFull();
                    childrenLayout.addClassName("nav-children");

                    for (MenuItem child : item.getChildren()) {
                        Component childIcon = child.getIconName()
                                .map(name -> new AppIcon(name, "18"))
                                .orElse(null);
                        Span childLabel = new Span(getTranslation(child.getLabelKey()));

                        HorizontalLayout childRow = createNavRow(childIcon, childLabel);
                        childRow.addClassName("nav-row--child");
                        childRow.getStyle().set("cursor", "pointer");

                        childRow.addClickListener(e -> handleNavigation(child));
                        childrenLayout.add(childRow);
                    }

                    Details details = new Details(summary, childrenLayout);
                    details.setWidthFull();
                    details.setOpened(false);
                    details.addClassName("nav-accordion");
                    details.addThemeName("menu"); // <— important: used to scope CSS
                    details.addOpenedChangeListener(event -> {
                        if (event.isOpened()) {
                            caret.removeClassName("nav-row__caret--closed");
                        } else {
                            caret.addClassName("nav-row__caret--closed");
                        }
                    });
                    groupLayout.add(details);
                } else {
                    // Leaf item row
                    Component icon = item.getIconName()
                            .map(name -> new AppIcon(name, "20"))
                            .orElse(null);
                    Span label = new Span(getTranslation(item.getLabelKey()));

                    HorizontalLayout row = createNavRow(icon, label);
                    row.addClassName("nav-row--leaf");
                    row.getStyle().set("cursor", "pointer");

                    row.addClickListener(e -> handleNavigation(item));
                    groupLayout.add(row);
                }
            }

            menuLayout.add(section);
        });
    }

    /* ------------------- Theme & Locale ------------------- */

    private void toggleTheme() {
        boolean dark = isDarkTheme();
        if (dark) {
            UI.getCurrent().getElement().getThemeList().remove(Lumo.DARK);
            UI.getCurrent().getElement().getThemeList().add(Lumo.LIGHT);
        } else {
            UI.getCurrent().getElement().getThemeList().remove(Lumo.LIGHT);
            UI.getCurrent().getElement().getThemeList().add(Lumo.DARK);
        }
        VaadinSession.getCurrent().setAttribute("darkTheme", !dark);
        themeItem.getElement().setText(getThemeLabel());

        // keep client-side in sync for reloads/deep links
        UI.getCurrent().getPage().executeJs(
                "const t = $0 ? 'dark' : 'light'; document.documentElement.setAttribute('theme', t); localStorage.setItem('theme', t);",
                !dark
        );
    }


    private boolean isDarkTheme() {
        Object attr = VaadinSession.getCurrent().getAttribute("darkTheme");
        if (attr instanceof Boolean bool) return bool;
        return UI.getCurrent().getElement().getThemeList().contains(Lumo.DARK);
    }

    private String getThemeLabel() {
        return getTranslation(isDarkTheme() ? "theme.light" : "theme.dark");
    }

    /** Show the OTHER language (switch target) */
    private String getLanguageLabel() {
        Locale locale = UI.getCurrent().getLocale();
        boolean isFa = locale != null && "fa".equals(locale.getLanguage());
        return getTranslation(isFa ? "language.english" : "language.farsi");
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        toggle.getElement().setAttribute("aria-label", getTranslation("header.toggleMenu"));

        localeDirectionService.applyDirection(UI.getCurrent());

        // Update submenu labels
        userMenu.getItems().forEach(item -> {
            var sub = item.getSubMenu();
            if (sub != null) {
                var items = sub.getItems();
                if (items.size() >= 4) {
                    items.get(0).getElement().setText(getTranslation("header.profile"));
                    items.get(1).getElement().setText(getTranslation("header.preferences"));
                    items.get(2).getElement().setText(getThemeLabel());
                    items.get(3).getElement().setText(getTranslation("header.logout"));
                }
            }
        });

        userName.setText(getTranslation("profile.name"));
        if (languageButton != null) languageButton.setText(getLanguageLabel());

        updateHeaderOrder();
        buildMenu();
    }

    /* ------------------- Attach hook ------------------- */

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        Boolean darkPref = (Boolean) VaadinSession.getCurrent().getAttribute("darkTheme");
        boolean dark = darkPref != null && darkPref;

        // Apply Lumo dark/light to the UI theme list
        if (dark) {
            UI.getCurrent().getElement().getThemeList().remove(Lumo.LIGHT);
            UI.getCurrent().getElement().getThemeList().add(Lumo.DARK);
        } else {
            UI.getCurrent().getElement().getThemeList().remove(Lumo.DARK);
            UI.getCurrent().getElement().getThemeList().add(Lumo.LIGHT);
        }

        // Mirror to document so token-based CSS picks it up immediately
        UI.getCurrent().getPage().executeJs(
                "document.documentElement.setAttribute('theme', $0); localStorage.setItem('theme', $0);",
                dark ? "dark" : "light"
        );

        updateHeaderOrder();
    }

    private HorizontalLayout createNavRow(Component icon, Span label) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(false);
        row.setPadding(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setWidthFull();
        row.addClassName("nav-row");

        if (icon != null) {
            icon.getElement().getClassList().add("nav-row__icon");
            row.add(icon);
        }

        label.addClassName("nav-row__label");
        row.add(label);
        row.setFlexGrow(1, label);

        return row;
    }

    private void handleNavigation(MenuItem item) {
        item.getNavigationTarget().ifPresentOrElse(
                route -> UI.getCurrent().navigate(route),
                () -> Notification.show(getTranslation(item.getLabelKey()))
        );
    }

    private void ensureMenuOverlayMinWidth(MenuBar menuBar) {
        if (menuBar == null) {
            return;
        }
        menuBar.getElement().executeJs(
                """
                        const host = this;
                        if (host.__overlayMinWidthApplied) {
                            return;
                        }
                        host.__overlayMinWidthApplied = true;
                        const MIN_WIDTH = 260;

                        const resolveOverlay = () => {
                            const sub = host._subMenu;
                            return sub && sub.$ && sub.$.overlay ? sub.$.overlay : null;
                        };

                        const applyWidth = () => {
                            const overlay = resolveOverlay();
                            if (!overlay) {
                                return;
                            }
                            const width = Math.max(host.offsetWidth, MIN_WIDTH);
                            overlay.style.minWidth = width + 'px';
                            overlay.style.width = width + 'px';
                            overlay.style.setProperty('--menu-overlay-min-width', width + 'px');
                            host.style.setProperty('--menu-overlay-min-width', width + 'px');
                        };

                        const scheduleApply = () => {
                            requestAnimationFrame(() => {
                                applyWidth();
                                const overlay = resolveOverlay();
                                if (overlay) {
                                    overlay.addEventListener('animationend', applyWidth, { once: true });
                                }
                            });
                        };

                        const openedListener = (event) => {
                            if (event.type === 'opened-changed' && !(event.detail && event.detail.value)) {
                                return;
                            }
                            scheduleApply();
                        };

                        ['vaadin-overlay-open', 'vaadin-overlay-opened', 'opened-changed']
                            .forEach(evt => host.addEventListener(evt, openedListener));

                        host.addEventListener('vaadin-overlay-close', () => requestAnimationFrame(applyWidth));

                        new ResizeObserver(() => applyWidth()).observe(host);
                """
        );
    }

}
