# ADR-20250920: Adopt layered architecture with enforceable boundaries

## Status
Accepted

## Context
The code base grew organically with UI views, reusable components and service
implementations interleaved under `service`, `component` and `view` packages.
UI classes depended directly on concrete services (`MockPersonService`,
`MenuService`) and domain models lived alongside Vaadin-specific code. There was
no executable documentation describing package boundaries, so regressions were
likely when adding features.

## Decision
* Introduce explicit packages per architectural layer (`domain`, `application`,
  `infrastructure`, `ui`).
* Extract interfaces for cross-layer contracts (`PersonDirectory`,
  `NavigationMenuService`, `FormValidationService`).
* Move concrete implementations to the `infrastructure` package and retain
  Vaadin UI in `ui` packages only.
* Document the layering rules in `docs/architecture.md` and add ArchUnit tests
  (`ArchitectureTest`) that fail the build when dependencies violate the rules.

## Consequences
* UI components now depend on abstractions, enabling future replacement of the
  in-memory data sources without touching Vaadin views.
* Developers must follow the documented package layout; violating it will break
  the build via ArchUnit tests, providing fast feedback.
* The new structure increases clarity and supports SOLID principles, but teams
  must adjust imports when creating new classes.
