# Samples module

The `samples` module hosts a Spring Boot Vaadin application that demonstrates
how to integrate the reusable components shipped in `vaadin-components`. Use it
as living documentation when building product features.

## Running the showcase
```bash
mvn -pl samples spring-boot:run
```
Visit `http://localhost:8080`. The application immediately redirects to Keycloak for authentication; configure a realm and
client as described below before launching the sample.

## Routes & scenarios
| Route | View class | Highlights |
| --- | --- | --- |
| `/` | `com.youtopin.vaadin.samples.ui.view.DashboardView` | Landing page tiles showing how to compose app cards and notifications. |
| `/design-system` | `com.youtopin.vaadin.samples.ui.view.DesignSystemView` | Interactive controls for notifications, typography tokens, and design primitives. |
| `/forms` | `com.youtopin.vaadin.samples.ui.view.FormGenerationView` | Renders two `GeneratedForm` instances sourced from JSON specs, including Jalali pickers and the map location field. |
| `/forms/wizard-flow` | `com.youtopin.vaadin.samples.ui.view.WizardFormFlowView` | Session-backed project launch wizard powered by `WizardFormFlowCoordinator`. |
| `/forms/wizard-coordinator` | `com.youtopin.vaadin.samples.ui.view.WizardCoordinatorSampleView` | Workspace provisioning flow that generates an ID in the first step and summarises stored context. |
| `/wizard` | `com.youtopin.vaadin.samples.ui.view.WizardView` | Demonstrates `HorizontalWizard` interactions, clickable steps, and custom colouring. |
| `/persons` | `com.youtopin.vaadin.samples.ui.view.PersonTableView` | Full CRUD-style table built on `FilterablePaginatedGrid` with saved views and exports. |
| `/full-grid` | `com.youtopin.vaadin.samples.ui.view.FullGridView` | Full-height grid configuration verifying expand-to-fill layouts. |
| `/compact-grid` | `com.youtopin.vaadin.samples.ui.view.CompactGridView` | Grid constrained by min/max heights, illustrating internal scrolling. |
| `/multi-grids` | `com.youtopin.vaadin.samples.ui.view.MultipleGridsView` | Compares feature combinations and selection handling across grid instances. |
| `/components/card` | `com.youtopin.vaadin.samples.ui.view.components.CardSampleView` | App card compositions, responsive layouts, and action bars. |
| `/components/accordion` | `com.youtopin.vaadin.samples.ui.view.components.AccordionSampleView` | Accessible accordion layouts for settings panels. |
| `/components/app-layout` | `com.youtopin.vaadin.samples.ui.view.components.AppLayoutSampleView` | Header/drawer wiring with locale-aware navigation. |
| `/components/master-detail-layout` | `com.youtopin.vaadin.samples.ui.view.components.MasterDetailLayoutSampleView` | Split-view master/detail using Vaadin `AppLayout` patterns. |
| `/components/confirm-dialog` | `com.youtopin.vaadin.samples.ui.view.components.ConfirmDialogSampleView` | Confirm dialog flows with asynchronous operations. |
| `/components/progress-bar` | `com.youtopin.vaadin.samples.ui.view.components.ProgressBarSampleView` | Progress bar styling, indeterminate state, and success/warning colours. |

## Keycloak & PKCE setup

The showcase relies exclusively on Keycloak's OpenID Connect support with PKCE. Create a **public** client (no client secret)
for the sample UI and enable the following redirect URL:

```
http://localhost:8080/login/oauth2/code/keycloak
```

Recommended environment variables:

```bash
export KEYCLOAK_CLIENT_ID=ytp-admin-ui
export KEYCLOAK_AUTH_URI="http://localhost:18080/realms/ytp-admin/protocol/openid-connect/auth"
export KEYCLOAK_TOKEN_URI="http://localhost:18080/realms/ytp-admin/protocol/openid-connect/token"
export KEYCLOAK_JWK_SET_URI="http://localhost:18080/realms/ytp-admin/protocol/openid-connect/certs"
export KEYCLOAK_USER_INFO_URI="http://localhost:18080/realms/ytp-admin/protocol/openid-connect/userinfo"
```

Once Keycloak is running and the client is configured, start the Vaadin sample. All routes remain protected until Keycloak
completes the login flow. User scopes from the ID token, `scope` claim, and Keycloak role assignments are cached in the Vaadin
session so the navigation menu can react to Keycloak permissions without extra round trips.

## Directory structure
```
samples/
├── frontend/                 # Theme, routes, and shared TS modules
├── src/main/java/com/youtopin/vaadin/samples/
│   ├── application/          # Service layer stubs (security, form engine)
│   ├── config/               # Spring configuration
│   ├── domain/               # In-memory data models
│   ├── infrastructure/       # Mock repositories and translations
│   ├── ui/layout/            # `MainLayout` and base page templates
│   └── ui/view/              # Route views (see table above)
└── src/main/resources/
    ├── forms/                # JSON specs consumed by `GeneratedForm`
    ├── messages*.properties  # Localisation bundles (fa & en)
    └── menu/navigation-menu.json
```

## Extending the samples
- Add new component views under `ui/view/components` and register routes with
  `@Route(value = "components/...", layout = MainLayout.class)`.
- Update `menu/navigation-menu.json` and translation bundles to expose new
  pages in the navigation drawer.
- Document additional scenarios in `docs/components/` or dedicated how-to
  guides.

