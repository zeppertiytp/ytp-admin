package com.youtopin.vaadin.samples.infrastructure.security;

import com.youtopin.vaadin.samples.application.security.UserScopeService;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves the scopes associated with the authenticated user from the HTTP/Vaadin session. If the
 * session has not been populated yet, the service falls back to the Spring Security context and
 * caches the result for subsequent calls.
 */
@Service
public class SessionUserScopeService implements UserScopeService {

    @Override
    public Set<String> getCurrentUserScopes() {
        Set<String> sessionScopes = readScopesFromSession();
        if (!sessionScopes.isEmpty()) {
            return sessionScopes;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> extractedScopes = new LinkedHashSet<>(OidcScopeUtils.extractScopes(authentication));
        if (extractedScopes.isEmpty()) {
            return Set.of();
        }

        storeScopesInSession(extractedScopes);
        return Set.copyOf(extractedScopes);
    }

    private Set<String> readScopesFromSession() {
        Object attribute = readRawScopeAttribute();
        if (attribute instanceof Set<?> set) {
            LinkedHashSet<String> scopes = new LinkedHashSet<>();
            set.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(scope -> !scope.isBlank())
                    .forEach(scopes::add);
            return Set.copyOf(scopes);
        }
        return Set.of();
    }

    private void storeScopesInSession(Set<String> scopes) {
        Set<String> immutableScopes = Set.copyOf(scopes);

        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null) {
            vaadinSession.getSession().setAttribute(SecuritySessionAttributes.USER_SCOPES, immutableScopes);
        }

        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(request -> request.getSession(false))
                .ifPresent(session -> session.setAttribute(SecuritySessionAttributes.USER_SCOPES, immutableScopes));
    }

    private Object readRawScopeAttribute() {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null) {
            Object attribute = vaadinSession.getSession().getAttribute(SecuritySessionAttributes.USER_SCOPES);
            if (attribute != null) {
                return attribute;
            }
        }

        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(request -> request.getSession(false))
                .map(session -> session.getAttribute(SecuritySessionAttributes.USER_SCOPES))
                .orElse(null);
    }
}
