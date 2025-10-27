# Keycloak OIDC with PKCE

The samples application now requires Keycloak as the sole authentication provider. The integration uses the OAuth 2.0
Authorization Code flow with Proof Key for Code Exchange (PKCE) so the Vaadin UI acts as a public client without storing a
client secret.

## Prerequisites

1. Keycloak 22+ (or any distribution that supports PKCE for public clients).
2. A realm dedicated to the admin panel showcase (examples below assume `ytp-admin`).
3. A public client configured in Keycloak:
   - **Client type:** `OpenID Connect`
   - **Client authentication:** Disabled (public client)
   - **Standard flow:** Enabled
   - **Redirect URI:** `http://localhost:8080/login/oauth2/code/keycloak`
   - **Web origins:** `http://localhost:8080`

## Environment variables

Export the following variables before launching the application to avoid editing `application.properties`:

```bash
export KEYCLOAK_CLIENT_ID=ytp-admin-ui
export KEYCLOAK_AUTH_URI="http://localhost:18080/realms/ytp-admin/protocol/openid-connect/auth"
export KEYCLOAK_TOKEN_URI="http://localhost:18080/realms/ytp-admin/protocol/openid-connect/token"
export KEYCLOAK_JWK_SET_URI="http://localhost:18080/realms/ytp-admin/protocol/openid-connect/certs"
export KEYCLOAK_USER_INFO_URI="http://localhost:18080/realms/ytp-admin/protocol/openid-connect/userinfo"
```

The URIs above assume Keycloak is reachable at `http://localhost:18080`. Adjust them when running behind HTTPS or a reverse
proxy.

## Running the stack

1. Start Keycloak and create a test user with the scopes or roles you want to surface in the navigation menu.
2. Launch the Vaadin samples application:

   ```bash
   mvn -pl samples spring-boot:run
   ```

3. Open `http://localhost:8080`. You are immediately redirected to Keycloak for login.

## Scope propagation

During the OAuth2 login handshake Spring Security captures the scopes/roles emitted by Keycloak. The
`SessionScopeStoringAuthenticationSuccessHandler` stores the normalized scope set in the HTTP session. A dedicated
`SessionUserScopeService` reads the same attribute inside Vaadin sessions, allowing the navigation menu to hide or reveal
items based on Keycloak assignments without additional HTTP round-trips.

The following sources contribute to the final scope set:

* `scope` parameter issued alongside the authorization code
* Authorities exposed by Spring Security (e.g. `SCOPE_profile`)
* `realm_access.roles` and `resource_access` entries from the Keycloak ID token

## Logout

Selecting **Logout** in the header triggers the Spring Security OIDC logout handler, which calls Keycloak's end-session
endpoint and returns the user to the application. Any cached scopes in the session are discarded automatically.
