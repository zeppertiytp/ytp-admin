package com.youtopin.vaadin.samples.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Captures the Keycloak-issued scopes granted to the current user and stores them inside the HTTP session
 * so that Vaadin components can query them later through {@link com.youtopin.vaadin.samples.application.security.UserScopeService}.
 */
@Component
class SessionScopeStoringAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(SessionScopeStoringAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        Set<String> scopes = new LinkedHashSet<>(OidcScopeUtils.extractScopes(authentication));
        HttpSession session = request.getSession(true);
        session.setAttribute(SecuritySessionAttributes.USER_SCOPES, Set.copyOf(scopes));
        log.info("Stored {} scope(s) for authenticated principal '{}'", scopes.size(), authentication.getName());

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
