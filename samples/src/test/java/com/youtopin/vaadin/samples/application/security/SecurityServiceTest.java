package com.youtopin.vaadin.samples.application.security;

import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

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
        SecurityContextHolder.clearContext();
        UI.setCurrent(null);
    }

    @Test
    void isAuthenticatedReturnsFalseWhenContextEmpty() {
        SecurityContextHolder.clearContext();

        assertFalse(securityService.isAuthenticated());
    }

    @Test
    void isAuthenticatedIgnoresAnonymousAuthentication() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        assertFalse(securityService.isAuthenticated());
    }

    @Test
    void isAuthenticatedReturnsTrueForAuthenticatedUser() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "credentials");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(securityService.isAuthenticated());
    }

    @Test
    void logoutRedirectsBrowserToSpringSecurityLogoutEndpoint() {
        UI ui = Mockito.mock(UI.class, Mockito.RETURNS_DEEP_STUBS);
        UI.setCurrent(ui);

        securityService.logout();

        Mockito.verify(ui.getPage()).setLocation("/logout");
    }
}
