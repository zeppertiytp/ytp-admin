# Samples module

The `samples` module hosts a Spring Boot Vaadin application that demonstrates
how to integrate the reusable components shipped in `vaadin-components`. Use it
as living documentation when building product features.

## Running the showcase
```bash
mvn -pl samples spring-boot:run
```
Visit `http://localhost:8080`. The default login is **admin / admin**.

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

The login view (`/login`) uses `LoginView` to showcase a minimal authentication
screen compatible with RTL.

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

