package com.youtopin.vaadin.samples.infrastructure.security;

/**
 * Shared constants for attributes stored in the HTTP/Vaadin session.
 */
final class SecuritySessionAttributes {

    /** Session attribute containing the current user's scopes. */
    static final String USER_SCOPES = SecuritySessionAttributes.class.getName() + ".USER_SCOPES";

    private SecuritySessionAttributes() {
    }
}
