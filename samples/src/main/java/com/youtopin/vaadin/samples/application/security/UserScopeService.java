package com.youtopin.vaadin.samples.application.security;

import java.util.Set;

/**
 * Provides the OAuth/OIDC scopes associated with the currently authenticated user.
 * Implementations can fetch the scopes from Spring Security's authentication context,
 * a Vaadin session attribute, or a remote user info endpoint.  The navigation menu can
 * then be filtered based on the scopes returned by this service.
 */
public interface UserScopeService {

    /**
     * Returns the set of scopes granted to the current user.  The returned collection must never be
     * {@code null} and should contain unique scope identifiers (for example {@code "admin:read"}).
     *
     * @return scopes granted to the current user, or an empty set when the user is anonymous or the
     *         application does not expose scope information
     */
    Set<String> getCurrentUserScopes();
}
