package com.example.adminpanel.ui.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Branded notification component used across the application. It enlarges the
 * default Vaadin notification, applies consistent iconography and allows
 * callers to select the screen corner that should host the toast.
 */
public class AppNotification extends Notification implements LocaleChangeObserver {

    private static final String BASE_THEME = "app-notification";
    private static final String VARIANT_PREFIX = BASE_THEME + "--";
    private static final Set<String> RTL_LANGS = Set.of("ar", "fa", "he", "ur");

    public enum Variant {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    public enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    private static final String AUTO_CLOSE_EVENT = "app-notification-auto-close";

    private final Div wrapper;
    private final Span title;
    private final Span description;
    private final Div iconContainer;
    private final Button closeButton;
    private Variant variant;
    private Corner corner = Corner.TOP_RIGHT;
    private Message titleMessage;
    private Message descriptionMessage;
    private long autoCloseDurationMillis;

    public AppNotification(String titleKey, String descriptionKey, Variant initialVariant) {
        this(Message.translationKey(titleKey), Message.translationKey(descriptionKey), initialVariant);
    }

    public AppNotification(Message titleMessage, Message descriptionMessage, Variant initialVariant) {
        super();
        getElement().getThemeList().add(BASE_THEME);
        setPosition(Position.TOP_END);
        setDuration(0);
        getElement().setAttribute("role", "alert");
        getElement().setAttribute("aria-live", "polite");

        wrapper = new Div();
        wrapper.addClassName("app-notification__wrapper");

        iconContainer = new Div();
        iconContainer.addClassName("app-notification__icon");
        iconContainer.getElement().setAttribute("aria-hidden", "true");
        wrapper.add(iconContainer);

        Div body = new Div();
        body.addClassName("app-notification__body");

        title = new Span();
        title.addClassName("app-notification__title");
        body.add(title);

        description = new Span();
        description.addClassName("app-notification__description");
        body.add(description);

        wrapper.add(body);

        closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), event -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ICON);
        closeButton.addClassName("app-notification__close");
        setCloseButtonAriaLabel("Close notification");
        wrapper.add(closeButton);

        registerAutoCloseBridge();
        add(wrapper);

        setTitle(titleMessage);
        setDescription(descriptionMessage);
        setVariant(initialVariant != null ? initialVariant : Variant.INFO);
        setCorner(Corner.TOP_RIGHT);
    }

    public void setTitle(String text) {
        setTitle(Message.translationKey(text));
    }

    public void setDescription(String text) {
        setDescription(Message.translationKey(text));
    }

    public void setTitle(Message message) {
        titleMessage = message;
        applyLocalizedMessage(title, message);
    }

    public void setDescription(Message message) {
        descriptionMessage = message;
        applyLocalizedMessage(description, message);
    }

    public void setVariant(Variant newVariant) {
        if (newVariant == null) {
            return;
        }
        variant = newVariant;
        refreshVariantTheme();
        iconContainer.removeAll();
        Icon icon = createIcon(newVariant);
        iconContainer.add(icon);
    }

    public Variant getVariant() {
        return variant;
    }

    public void setCorner(Corner corner) {
        if (corner == null) {
            return;
        }
        this.corner = corner;
        applyCornerPosition();
    }

    /**
     * Configures automatic dismissal for the notification. Passing {@code null}
     * or a non-positive duration disables the timeout and requires manual
     * dismissal.
     *
     * @param duration how long the notification should stay open before closing
     *                 automatically; if {@code null} or not positive the
     *                 notification will stay open until closed explicitly
     */
    public void setAutoCloseDuration(Duration duration) {
        long requested = duration != null ? Math.max(0, duration.toMillis()) : 0;
        autoCloseDurationMillis = requested;
        if (autoCloseDurationMillis <= 0) {
            disableClientAutoClose();
        } else if (isOpened()) {
            syncClientAutoClose();
        }
    }

    /**
     * Returns the current automatic dismissal duration.
     *
     * @return the configured timeout or {@link Duration#ZERO} when disabled
     */
    public Duration getAutoCloseDuration() {
        if (autoCloseDurationMillis <= 0) {
            return Duration.ZERO;
        }
        return Duration.ofMillis(autoCloseDurationMillis);
    }

    public void setCloseButtonAriaLabel(String label) {
        String value = label == null ? "" : label;
        closeButton.getElement().setAttribute("aria-label", value);
        closeButton.getElement().setProperty("title", value);
    }

    public static AppNotification show(String titleKey, String descriptionKey, Variant variant, Corner corner) {
        AppNotification notification = new AppNotification(titleKey, descriptionKey, variant);
        notification.setCorner(corner);
        notification.open();
        return notification;
    }

    public static AppNotification show(Message titleMessage, Message descriptionMessage, Variant variant, Corner corner) {
        AppNotification notification = new AppNotification(titleMessage, descriptionMessage, variant);
        notification.setCorner(corner);
        notification.open();
        return notification;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        applyLocalizedMessage(title, titleMessage);
        applyLocalizedMessage(description, descriptionMessage);
        applyCornerPosition();
    }

    @Override
    public void open() {
        super.open();
        if (autoCloseDurationMillis > 0) {
            syncClientAutoClose();
        }
    }

    @Override
    public void close() {
        disableClientAutoClose();
        super.close();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        disableClientAutoClose();
        super.onDetach(detachEvent);
    }

    private void applyLocalizedMessage(Span target, Message message) {
        if (target == null) {
            return;
        }
        String resolved = resolveMessage(message);
        target.setText(resolved);
        target.setVisible(resolved != null && !resolved.isBlank());
    }

    private String resolveMessage(Message message) {
        if (message == null) {
            return "";
        }
        return message.resolve(UI.getCurrent());
    }

    private void refreshVariantTheme() {
        ThemeList themeList = getElement().getThemeList();
        List<String> toRemove = new ArrayList<>();
        for (String theme : themeList) {
            if (theme.startsWith(VARIANT_PREFIX)) {
                toRemove.add(theme);
            }
        }
        toRemove.forEach(themeList::remove);
        themeList.add(VARIANT_PREFIX + variant.name().toLowerCase(Locale.ENGLISH));
    }

    private void applyCornerPosition() {
        boolean rtl = isCurrentDirectionRtl();
        Position position = determinePosition(corner, rtl);
        setPosition(position);
        refreshDirectionAwareStyles();
    }

    private Position determinePosition(Corner corner, boolean rtl) {
        Corner safeCorner = corner != null ? corner : Corner.TOP_RIGHT;
        return switch (safeCorner) {
            case TOP_LEFT -> rtl ? Position.TOP_END : Position.TOP_START;
            case TOP_RIGHT -> rtl ? Position.TOP_START : Position.TOP_END;
            case BOTTOM_LEFT -> rtl ? Position.BOTTOM_END : Position.BOTTOM_START;
            case BOTTOM_RIGHT -> rtl ? Position.BOTTOM_START : Position.BOTTOM_END;
        };
    }

    private Icon createIcon(Variant type) {
        VaadinIcon vaadinIcon = switch (type) {
            case INFO -> VaadinIcon.INFO_CIRCLE_O;
            case SUCCESS -> VaadinIcon.CHECK_CIRCLE_O;
            case WARNING -> VaadinIcon.WARNING;
            case ERROR -> VaadinIcon.CLOSE_CIRCLE;
        };
        Icon icon = vaadinIcon.create();
        icon.addClassName("app-notification__icon-graphic");
        icon.setSize("24px");
        return icon;
    }

    private void refreshDirectionAwareStyles() {
        boolean rtl = isCurrentDirectionRtl();
        wrapper.getElement().getClassList().set("app-notification__wrapper--rtl", rtl);
        closeButton.getElement().getClassList().set("app-notification__close--rtl", rtl);
    }

    private boolean isCurrentDirectionRtl() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return false;
        }
        String dir = ui.getElement().getAttribute("dir");
        if (dir != null && !dir.isBlank()) {
            return "rtl".equalsIgnoreCase(dir);
        }
        Locale locale = ui.getLocale();
        if (locale == null) {
            return false;
        }
        String language = locale.getLanguage();
        return RTL_LANGS.contains(language);
    }

    private void registerAutoCloseBridge() {
        wrapper.getElement().addEventListener(AUTO_CLOSE_EVENT, event -> {
            if (autoCloseDurationMillis > 0 && isOpened()) {
                close();
            }
        });
    }

    private void syncClientAutoClose() {
        long delay = autoCloseDurationMillis;
        if (delay <= 0) {
            disableClientAutoClose();
            return;
        }
        wrapper.getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(this, context ->
                        wrapper.getElement().executeJs(
                                "const host=this;const duration=$0;const eventName=$1;" +
                                        "if (!duration || duration<=0){" +
                                        "  if (host.__appNotificationAutoClose){" +
                                        "    if (host.__appNotificationAutoClose.timerId){clearTimeout(host.__appNotificationAutoClose.timerId);}" +
                                        "    host.removeEventListener('mouseenter', host.__appNotificationAutoClose.onEnter);" +
                                        "    host.removeEventListener('mouseleave', host.__appNotificationAutoClose.onLeave);" +
                                        "    host.__appNotificationAutoClose=null;" +
                                        "  }" +
                                        "  return;" +
                                        "}" +
                                        "let data=host.__appNotificationAutoClose;" +
                                        "if(!data){" +
                                        "  data=host.__appNotificationAutoClose={timerId:0,startedAt:0,remaining:duration};" +
                                        "  data.invokeClose=()=>{if(!host.isConnected){return;}host.dispatchEvent(new CustomEvent(eventName,{bubbles:false,composed:false}));};" +
                                        "  data.startTimer=timeout=>{" +
                                        "    if (data.timerId){clearTimeout(data.timerId);}" +
                                        "    if (timeout<=0){data.invokeClose();return;}" +
                                        "    data.remaining=timeout;" +
                                        "    data.startedAt=Date.now();" +
                                        "    data.timerId=window.setTimeout(()=>{" +
                                        "      data.timerId=0;" +
                                        "      data.startedAt=0;" +
                                        "      data.remaining=0;" +
                                        "      data.invokeClose();" +
                                        "    }, timeout);" +
                                        "  };" +
                                        "  data.pause=()=>{" +
                                        "    if (data.timerId){clearTimeout(data.timerId);data.timerId=0;}" +
                                        "    if (data.startedAt){data.remaining=Math.max(0,data.remaining-(Date.now()-data.startedAt));data.startedAt=0;}" +
                                        "  };" +
                                        "  data.resume=()=>{" +
                                        "    if (!host.isConnected){return;}" +
                                        "    if (data.remaining<=0){data.invokeClose();return;}" +
                                        "    data.startTimer(data.remaining);" +
                                        "  };" +
                                        "  data.onEnter=()=>data.pause();" +
                                        "  data.onLeave=()=>data.resume();" +
                                        "  host.addEventListener('mouseenter', data.onEnter);" +
                                        "  host.addEventListener('mouseleave', data.onLeave);" +
                                        "}" +
                                        "const info=host.__appNotificationAutoClose;" +
                                        "info.startTimer(duration);",
                                delay,
                                AUTO_CLOSE_EVENT)));
    }

    private void disableClientAutoClose() {
        wrapper.getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(this, context ->
                        wrapper.getElement().executeJs(
                                "const host=this;const data=host.__appNotificationAutoClose;" +
                                        "if(!data){return;}" +
                                        "if(data.timerId){clearTimeout(data.timerId);}" +
                                        "host.removeEventListener('mouseenter', data.onEnter);" +
                                        "host.removeEventListener('mouseleave', data.onLeave);" +
                                        "host.__appNotificationAutoClose=null;")));
    }

    public static final class Message {

        private final String key;
        private final String en;
        private final String fa;

        private Message(String key, String en, String fa) {
            this.key = key;
            this.en = en;
            this.fa = fa;
        }

        public static Message translationKey(String key) {
            if (key == null || key.isBlank()) {
                return new Message(null, null, null);
            }
            return new Message(key, null, null);
        }

        public static Message bilingual(String english, String farsi) {
            if ((english == null || english.isBlank()) && (farsi == null || farsi.isBlank())) {
                return new Message(null, null, null);
            }
            return new Message(null, english, farsi);
        }

        public static Message literal(String text) {
            if (text == null || text.isBlank()) {
                return new Message(null, null, null);
            }
            return new Message(null, text, text);
        }

        public String resolve(UI ui) {
            if (key != null) {
                if (ui != null) {
                    String translation = ui.getTranslation(key);
                    if (translation != null && !translation.isBlank()) {
                        return translation;
                    }
                }
                return key;
            }
            Locale locale = null;
            if (ui != null) {
                locale = ui.getLocale();
            }
            boolean isFa = locale != null && "fa".equalsIgnoreCase(locale.getLanguage());
            if (isFa) {
                return firstNonBlank(fa, en);
            }
            return firstNonBlank(en, fa);
        }

        public String getKey() {
            return key;
        }

        public String getEn() {
            return en;
        }

        public String getFa() {
            return fa;
        }

        private static String firstNonBlank(String primary, String secondary) {
            if (primary != null && !primary.isBlank()) {
                return primary;
            }
            if (secondary != null && !secondary.isBlank()) {
                return secondary;
            }
            return "";
        }
    }
}
