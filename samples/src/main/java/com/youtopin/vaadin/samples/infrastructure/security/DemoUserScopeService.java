package com.youtopin.vaadin.samples.infrastructure.security;

import com.youtopin.vaadin.samples.application.security.UserScopeService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/**
 * Demo implementation that returns an empty scope set.  The application currently uses an
 * in-memory login without OAuth/OIDC integration, so there are no scopes to resolve.  Having a
 * bean in place makes it straightforward to plug in a real implementation once authentication is
 * backed by an identity provider.
 */
@Service
public class DemoUserScopeService implements UserScopeService {

    @Override
    public Set<String> getCurrentUserScopes() {
        return Collections.emptySet();
    }
}
