# Architecture Overview

## Layered structure

The code base follows a layered architecture aligned with SOLID and Vaadin best
practices. Packages are organised by responsibility:

| Layer | Package | Responsibilities |
| --- | --- | --- |
| Domain | `com.example.adminpanel.domain..` | Pure models and aggregates (`Person`, `MenuItem`). Contains no Vaadin UI dependencies beyond simple value objects. |
| Application | `com.example.adminpanel.application..` | Use-case and service interfaces (`PersonDirectory`, `NavigationMenuService`, `FormValidationService`, `SecurityService`, pagination contracts). Depends only on the domain. |
| Infrastructure | `com.example.adminpanel.infrastructure..` | Technical implementations of application services (in-memory data sources, static menu provider, form validation stub). May depend on domain and application layers but never on UI packages. |
| UI | `com.example.adminpanel.ui..` | Vaadin layouts, views and reusable components. Depends on domain types and application interfaces only. |
| Configuration & support | `com.example.adminpanel.config..`, `com.example.adminpanel.i18n..` | Bootstrapping, locale handling and translation provider. |

## Dependency rules

1. **Domain is pure.** Classes in `..domain..` must not depend on `..application..`,
   `..infrastructure..`, or `..ui..` packages.
2. **Application isolates use cases.** Classes in `..application..` may depend on
   domain types but must not depend on infrastructure or UI packages.
3. **Infrastructure implements, never dictates.** Classes in
   `..infrastructure..` implement application interfaces and must not depend on
   UI packages.
4. **UI depends on abstractions.** Classes in `..ui..` may depend on application
   interfaces and domain objects but must not depend on infrastructure packages.
5. **Configuration bridges.** `..config..` can wire any layer but should prefer
   application interfaces.

## Contract enforcement

ArchUnit tests in `src/test/java/com/example/adminpanel/architecture` enforce the
rules above. The build fails if any class violates the declared architecture.
Run `mvn test` to execute them.

## SOLID alignment

* **Single Responsibility:** Classes are moved into packages matching their
  responsibility (e.g. `PersonDirectory` interface vs. `InMemoryPersonDirectory`
  implementation).
* **Open/Closed & Liskov:** Interfaces define extension points; implementations
  honour contracts without changing callers.
* **Interface Segregation:** UI components depend on lean interfaces such as
  `PersonDirectory` and `NavigationMenuService` instead of concrete services.
* **Dependency Inversion:** UI and configuration layers depend on abstractions
  defined in the application layer; infrastructure supplies the concrete beans.

Follow these rules when adding new code to preserve a clean architecture.
