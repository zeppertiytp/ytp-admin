package com.youtopin.vaadin.samples.application.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityServiceTest {

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService();
    }

    @AfterEach
    void tearDown() {
        VaadinSession.setCurrent(null);
        UI.setCurrent(null);
    }

    @Test
    void authenticateStoresFlagInCurrentSession() {
        VaadinSession session = Mockito.mock(VaadinSession.class);
        VaadinSession.setCurrent(session);

        boolean authenticated = securityService.authenticate("admin", "admin");

        assertTrue(authenticated);
        Mockito.verify(session).setAttribute("authenticated", true);
    }

    @Test
    void authenticateFallsBackToUiSessionWhenCurrentMissing() {
        UI ui = Mockito.mock(UI.class);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(ui.getSession()).thenReturn(session);
        UI.setCurrent(ui);

        boolean authenticated = securityService.authenticate("admin", "admin");

        assertTrue(authenticated);
        Mockito.verify(session).setAttribute("authenticated", true);
    }

    @Test
    void isAuthenticatedReadsFlagFromSession() {
        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.getAttribute("authenticated")).thenReturn(Boolean.TRUE);
        VaadinSession.setCurrent(session);

        assertTrue(securityService.isAuthenticated());
    }

    @Test
    void isAuthenticatedReturnsFalseWhenNoSession() {
        VaadinSession.setCurrent(null);
        UI.setCurrent(null);

        assertFalse(securityService.isAuthenticated());
    }

    @Test
    void logoutInvalidatesSessionAndNavigates() {
        VaadinSession session = Mockito.mock(VaadinSession.class);
        WrappedSession wrappedSession = Mockito.mock(WrappedSession.class);
        Mockito.when(session.getSession()).thenReturn(wrappedSession);
        VaadinSession.setCurrent(session);

        UI ui = Mockito.mock(UI.class);
        UI.setCurrent(ui);

        securityService.logout();

        Mockito.verify(wrappedSession).invalidate();
        Mockito.verify(session).close();
        Mockito.verify(ui).navigate("login");
    }
}
