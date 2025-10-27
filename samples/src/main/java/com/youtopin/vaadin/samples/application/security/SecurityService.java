package com.youtopin.vaadin.samples.application.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Security facade exposing authentication helpers to the Vaadin UI layer. Authentication is delegated to Spring
 * Security's Keycloak OIDC integration so there are no manual username/password checks in the application code.
 */
@Service
public class SecurityService {
    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);

    /**
     * Returns {@code true} when the current thread is associated with an authenticated Spring Security context.
     *
     * @return {@code true} if the user is authenticated with Keycloak
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * Initiates the OIDC logout flow by redirecting the browser to the Spring Security logout endpoint. Keycloak handles
     * the session termination and redirects back to the application based on the `post_logout_redirect_uri` configured in
     * {@link com.youtopin.vaadin.samples.infrastructure.security.SecurityConfiguration}.
     */
    public void logout() {
        UI current = UI.getCurrent();
        if (current != null) {
            current.getPage().setLocation("/logout");
        } else {
            log.warn("Logout requested but no UI is bound to the current thread; ensure logout is triggered from the UI thread.");
        }
    }

    /**
     * Convenience accessor used by legacy callers to obtain the current Vaadin session.
     *
     * @return the active {@link VaadinSession} or {@code null} when no UI is bound to the thread
     */
    public VaadinSession resolveSession() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            return session;
        }
        UI current = UI.getCurrent();
        return current != null ? current.getSession() : null;
    }
}
