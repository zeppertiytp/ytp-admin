package com.youtopin.vaadin.samples.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper methods that extract OAuth2/OIDC scopes and Keycloak role claims from Spring Security
 * authentication objects.
 */
final class OidcScopeUtils {

    private static final String SCOPE_PREFIX = "SCOPE_";

    private OidcScopeUtils() {
    }

    static Set<String> extractScopes(Authentication authentication) {
        if (authentication == null) {
            return Set.of();
        }

        LinkedHashSet<String> scopes = new LinkedHashSet<>();
        authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && authority.startsWith(SCOPE_PREFIX))
                .map(authority -> authority.substring(SCOPE_PREFIX.length()))
                .forEach(scopes::add);

        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            scopes.addAll(extractFromScopeAttribute(oidcUser.getClaims().get("scope")));
            scopes.addAll(extractFromScopeAttribute(oidcUser.getUserInfo() != null
                    ? oidcUser.getUserInfo().getClaim("scope") : null));
            scopes.addAll(extractRealmAndResourceRoles(oidcUser));
        } else if (principal instanceof OAuth2AuthenticatedPrincipal oauthPrincipal) {
            scopes.addAll(extractFromScopeAttribute(oauthPrincipal.getAttribute("scope")));
        }

        return scopes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(scope -> !scope.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> extractFromScopeAttribute(Object attribute) {
        if (attribute == null) {
            return Set.of();
        }
        if (attribute instanceof String scopeString) {
            return splitScopes(scopeString);
        }
        if (attribute instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .flatMap(value -> splitScopes(value).stream())
                    .collect(Collectors.toSet());
        }
        return Set.of(attribute.toString());
    }

    private static Set<String> splitScopes(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        String[] tokens = value.trim().split("\\s+");
        LinkedHashSet<String> scopes = new LinkedHashSet<>(tokens.length);
        for (String token : tokens) {
            String scope = token.trim();
            if (!scope.isEmpty()) {
                scopes.add(scope);
            }
        }
        return scopes;
    }

    private static Set<String> extractRealmAndResourceRoles(OidcUser oidcUser) {
        LinkedHashSet<String> scopes = new LinkedHashSet<>();

        Optional.ofNullable(oidcUser.getClaimAsMap("realm_access"))
                .map(map -> map.get("roles"))
                .ifPresent(roles -> scopes.addAll(normaliseRoleCollection("realm", roles)));

        Optional.ofNullable(oidcUser.getClaimAsMap("resource_access"))
                .ifPresent(resourceAccess -> resourceAccess.forEach((client, value) -> {
                    if (value instanceof Map<?, ?> clientMap) {
                        Object roles = clientMap.get("roles");
                        scopes.addAll(normaliseRoleCollection(client.toString(), roles));
                    }
                }));

        return scopes;
    }

    private static Collection<String> normaliseRoleCollection(String prefix, Object rolesAttribute) {
        if (rolesAttribute instanceof Collection<?> rolesCollection) {
            return rolesCollection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(role -> prefix + ":" + role)
                    .collect(Collectors.toSet());
        }
        if (rolesAttribute != null) {
            return Set.of(prefix + ":" + rolesAttribute);
        }
        return Set.of();
    }
}
