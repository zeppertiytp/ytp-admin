package com.example.adminpanel.ui.view;

import com.example.adminpanel.ui.component.AppNotification;
import com.example.adminpanel.ui.layout.AppPageLayout;
import com.example.adminpanel.ui.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Route(value = "design-system", layout = MainLayout.class)
public class DesignSystemView extends AppPageLayout implements LocaleChangeObserver {

    private final H1 pageTitle;
    private final H2 paletteTitle;
    private final H2 buttonsTitle;
    private final H2 badgesTitle;
    private final Span paletteDescription;
    private final Span buttonsDescription;
    private final Span badgesDescription;
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
    private final Div paletteGrid;
    private final Map<Span, String> swatchLabels = new HashMap<>();
    private final H2 notificationsTitle;
    private final Span notificationsDescription;
    private final Select<AppNotification.Corner> notificationCornerSelect;
    private final Button infoNotificationButton;
    private final Button successNotificationButton;
    private final Button warningNotificationButton;
    private final Button errorNotificationButton;

    public DesignSystemView() {
        pageTitle = createPageTitle(getTranslation("designSystem.title"));

        paletteTitle = new H2();
        paletteDescription = new Span();
        paletteDescription.addClassName("page-subtitle");
        paletteGrid = responsiveGrid();
        paletteGrid.add(
                swatch("designSystem.primary", "--color-primary-600"),
                swatch("designSystem.secondary", "--color-secondary-600"),
                swatch("designSystem.success", "--color-success-500"),
                swatch("designSystem.info", "--color-info-500"),
                swatch("designSystem.warning", "--color-warning-500"),
                swatch("designSystem.danger", "--color-danger-500")
        );
        VerticalLayout paletteCard = createCard();
        paletteCard.addClassName("stack-lg");
        paletteCard.add(paletteTitle, paletteDescription, paletteGrid);
        add(paletteCard);

        Div buttons = responsiveGrid();
        primaryButton = btn("primary");
        secondaryButton = btn("secondary");
        infoButton = btn("info");
        successButton = btn("success");
        warningButton = btn("warning");
        dangerButton = btn("danger");
        buttons.add(primaryButton, secondaryButton, infoButton, successButton, warningButton, dangerButton);
        buttonsTitle = new H2();
        buttonsDescription = new Span();
        buttonsDescription.addClassName("page-subtitle");
        VerticalLayout buttonsCard = createCard();
        buttonsCard.addClassName("stack-lg");
        buttonsCard.add(buttonsTitle, buttonsDescription, buttons);
        add(buttonsCard);

        Div badges = responsiveGrid();
        primaryBadge = badge("primary");
        secondaryBadge = badge("secondary");
        infoBadge = badge("info");
        successBadge = badge("success");
        warningBadge = badge("warning");
        dangerBadge = badge("danger");
        badges.add(primaryBadge, secondaryBadge, infoBadge, successBadge, warningBadge, dangerBadge);
        badgesTitle = new H2();
        badgesDescription = new Span();
        badgesDescription.addClassName("page-subtitle");
        VerticalLayout badgesCard = createCard();
        badgesCard.addClassName("stack-lg");
        badgesCard.add(badgesTitle, badgesDescription, badges);
        add(badgesCard);

        notificationsTitle = new H2();
        notificationsDescription = new Span();
        notificationsDescription.addClassName("page-subtitle");

        notificationCornerSelect = new Select<>();
        notificationCornerSelect.setItems(AppNotification.Corner.values());
        notificationCornerSelect.setValue(AppNotification.Corner.TOP_RIGHT);
        notificationCornerSelect.setItemLabelGenerator(this::cornerLabel);
        notificationCornerSelect.setWidthFull();
        notificationCornerSelect.getStyle().set("maxWidth", "240px");

        Div notificationButtons = responsiveGrid();
        infoNotificationButton = btn("info");
        infoNotificationButton.addClickListener(event -> showNotification(AppNotification.Variant.INFO));
        successNotificationButton = btn("success");
        successNotificationButton.addClickListener(event -> showNotification(AppNotification.Variant.SUCCESS));
        warningNotificationButton = btn("warning");
        warningNotificationButton.addClickListener(event -> showNotification(AppNotification.Variant.WARNING));
        errorNotificationButton = btn("danger");
        errorNotificationButton.addClickListener(event -> showNotification(AppNotification.Variant.ERROR));
        notificationButtons.add(infoNotificationButton, successNotificationButton, warningNotificationButton, errorNotificationButton);

        VerticalLayout notificationsCard = createCard();
        notificationsCard.addClassName("stack-lg");
        notificationsCard.add(notificationsTitle, notificationsDescription, notificationCornerSelect, notificationButtons);
        add(notificationsCard);

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
        String appliedTheme = "primary".equals(themeName) ? "primary" : "primary " + themeName;
        b.getElement().setAttribute("theme", appliedTheme);
        b.setWidthFull();
        return b;
    }

    private Span badge(String theme) {
        Span s = new Span();
        s.getElement().setAttribute("theme", "badge");
        s.getElement().getClassList().add(theme);
        return s;
    }

    private void updateTexts() {
        pageTitle.setText(getTranslation("designSystem.title"));
        paletteTitle.setText(getTranslation("designSystem.heading"));
        paletteDescription.setText(getTranslation("designSystem.paletteDescription"));
        swatchLabels.forEach((label, key) -> label.setText(getTranslation(key)));
        buttonsTitle.setText(getTranslation("designSystem.buttonsCard"));
        buttonsDescription.setText(getTranslation("designSystem.actionsDescription"));
        badgesTitle.setText(getTranslation("designSystem.badgesCard"));
        badgesDescription.setText(getTranslation("designSystem.statusDescription"));
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
        notificationsTitle.setText(getTranslation("designSystem.notificationsCard"));
        notificationsDescription.setText(getTranslation("designSystem.notificationsDescription"));
        notificationCornerSelect.setLabel(getTranslation("designSystem.notificationsCornerLabel"));
        notificationCornerSelect.getListDataView().refreshAll();
        infoNotificationButton.setText(getTranslation("designSystem.notificationTrigger.info"));
        successNotificationButton.setText(getTranslation("designSystem.notificationTrigger.success"));
        warningNotificationButton.setText(getTranslation("designSystem.notificationTrigger.warning"));
        errorNotificationButton.setText(getTranslation("designSystem.notificationTrigger.error"));
    }

    private void showNotification(AppNotification.Variant variant) {
        AppNotification.Message titleMessage;
        AppNotification.Message bodyMessage;

        if (variant == AppNotification.Variant.WARNING) {
            titleMessage = AppNotification.Message.bilingual(
                    "Custom bilingual warning",
                    "هشدار دو زبانه سفارشی"
            );
            bodyMessage = AppNotification.Message.bilingual(
                    "Use bilingual values when you need ad-hoc localized text outside the bundle.",
                    "وقتی به متن محلی خارج از بسته ترجمه نیاز دارید از مقادیر دو زبانه استفاده کنید."
            );
        } else {
            String variantKey = variantKeyFor(variant);
            titleMessage = AppNotification.Message.translationKey("designSystem.notificationTitle." + variantKey);
            bodyMessage = AppNotification.Message.translationKey("designSystem.notificationBody." + variantKey);
        }

        AppNotification notification = new AppNotification(titleMessage, bodyMessage, variant);
        notification.setCloseButtonAriaLabel(getTranslation("designSystem.notificationClose"));
        AppNotification.Corner corner = notificationCornerSelect.getValue();
        if (corner != null) {
            notification.setCorner(corner);
        }
        notification.open();
    }

    private String cornerLabel(AppNotification.Corner corner) {
        if (corner == null) {
            return "";
        }
        return getTranslation("designSystem.notificationsCorner." + corner.name().toLowerCase(Locale.ENGLISH).replace('_', '-'));
    }

    private String variantKeyFor(AppNotification.Variant variant) {
        return variant.name().toLowerCase(Locale.ENGLISH);
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

    private Div swatch(String labelKey, String colorVar) {
        Div swatch = new Div();
        swatch.addClassName("color-swatch");

        Span label = new Span();
        label.addClassName("color-swatch__label");
        swatchLabels.put(label, labelKey);
        label.setText(getTranslation(labelKey));

        Div preview = new Div();
        preview.addClassName("color-swatch__preview");
        preview.getStyle().set("background", "var(" + colorVar + ")");

        Span token = new Span(colorVar);
        token.addClassName("color-swatch__token");

        swatch.add(label, preview, token);
        return swatch;
    }
}
