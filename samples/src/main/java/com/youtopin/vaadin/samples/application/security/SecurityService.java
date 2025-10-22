package com.youtopin.vaadin.samples.application.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Simple security service that performs in‑memory authentication for
 * demonstration purposes.  In production this class should be
 * replaced or extended to delegate authentication to an external
 * provider (e.g. Keycloak) via Spring Security.
 */
@Service
public class SecurityService {
    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);

    /** Session attribute name used to store the authentication flag. */
    private static final String AUTH_SESSION_ATTR = "authenticated";

    /**
     * Attempts to authenticate the user.  For this demo the only
     * accepted credentials are {@code admin/admin}.  When
     * authenticated the flag is stored in the Vaadin session.
     *
     * @param username the username provided by the user
     * @param password the password provided by the user
     * @return {@code true} if authentication was successful; {@code false} otherwise
     */
    public boolean authenticate(String username, String password) {
        boolean authenticated = "admin".equals(username) && "admin".equals(password);
        VaadinSession session = resolveSession();
        if (session != null) {
            session.setAttribute(AUTH_SESSION_ATTR, authenticated);
        } else {
            log.warn("Authentication attempted for user '{}' without an active Vaadin session", username);
        }

        if (authenticated) {
            log.info("User '{}' authenticated successfully", username);
        } else {
            log.warn("Authentication failed for user '{}'", username);
        }

        return authenticated;
    }

    /**
     * Checks whether the current user has been authenticated.
     *
     * @return {@code true} if the session holds an authenticated flag
     */
    public boolean isAuthenticated() {
        VaadinSession session = resolveSession();
        if (session == null) {
            return false;
        }
        Object attr = session.getAttribute(AUTH_SESSION_ATTR);
        if (attr instanceof Boolean bool) {
            return bool;
        }
        return false;
    }

    /**
     * Logs out the current user by clearing the session and navigating
     * back to the login view.  This method can be called from any
     * point inside the UI.
     */
    public void logout() {
        VaadinSession session = resolveSession();
        if (session != null) {
            session.getSession().invalidate();
            session.close();
            log.info("User session invalidated and closed");
        } else {
            log.debug("Logout requested but no Vaadin session was available");
        }
        // Redirect to login page
        UI current = UI.getCurrent();
        if (current != null) {
            current.navigate("login");
        } else {
            log.debug("Unable to redirect to login view because no UI is bound to the current thread");
        }
    }

    private VaadinSession resolveSession() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            return session;
        }
        UI current = UI.getCurrent();
        return current != null ? current.getSession() : null;
    }
}
