package com.youtopin.vaadin.samples.ui.view;

import com.youtopin.vaadin.samples.application.security.SecurityService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The login view presents a full screen login form to the user.
 *
 * <p>
 * It uses Vaadin's {@link LoginOverlay} component for a modern look and
 * feel.  Authentication is delegated to {@link SecurityService} and
 * the view implements {@link BeforeEnterObserver} to redirect
 * already authenticated users away from the login page.
 */
@Route(value = "login")
@PermitAll
public class LoginView extends VerticalLayout implements BeforeEnterObserver, LocaleChangeObserver {

    private final SecurityService securityService;
    private final LoginOverlay loginOverlay;

    @Autowired
    public LoginView(SecurityService securityService) {
        this.securityService = securityService;
        this.loginOverlay = createLoginOverlay();

        setSizeFull();
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        add(loginOverlay);

        updatePageTitle();

        loginOverlay.addLoginListener(event -> {
            boolean success = securityService.authenticate(event.getUsername(), event.getPassword());
            if (success) {
                loginOverlay.close();
                UI.getCurrent().navigate("");
            } else {
                loginOverlay.setError(true);
            }
        });
    }

    /**
     * Creates a configured login overlay with translated texts and no
     * forgot password button.
     *
     * @return a new instance of {@link LoginOverlay}
     */
    private LoginOverlay createLoginOverlay() {
        LoginOverlay overlay = new LoginOverlay();
        overlay.setForgotPasswordButtonVisible(false);
        overlay.getElement().getThemeList().add("app-login");
        // Compose custom I18n for the login form using our translation service
        LoginI18n i18n = createI18n();
        overlay.setI18n(i18n);
        overlay.setOpened(true);
        return overlay;
    }

    private void updatePageTitle() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("login.title"));
        }
    }

    /**
     * Builds the internationalisation object for the login component.
     * This method reads translation keys through the Vaadin API to
     * ensure correct locale usage.
     *
     * @return a populated {@link LoginI18n} instance
     */
    private LoginI18n createI18n() {
        // Build the i18n object manually
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle(getTranslation("login.title"));
        i18n.getHeader().setDescription(getTranslation("login.description"));
        i18n.getForm().setTitle("");
        i18n.getForm().setUsername(getTranslation("login.username"));
        i18n.getForm().setPassword(getTranslation("login.password"));
        i18n.getForm().setSubmit(getTranslation("login.button"));
        i18n.setAdditionalInformation("");
        return i18n;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // If the user is already authenticated redirect to the root (dashboard)
        if (securityService.isAuthenticated()) {
            event.forwardTo("");
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        loginOverlay.setI18n(createI18n());
        updatePageTitle();
    }
}
