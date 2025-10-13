# ADR-20250920: Standardised application notification component

## Status
Accepted

## Context
The design system currently relies on Vaadin's default notification component, which is small, centrally positioned, and lacks consistent styling hooks for the application's semantic colour palette. We need a reusable notification that is visually aligned with the rest of the admin panel, supports multiple severity levels, and can be presented in any screen corner to respect different layout needs and RTL locales.

## Decision
Introduce a dedicated `AppNotification` component that extends Vaadin `Notification`. The component:

- applies a custom theme (`app-notification`) to enlarge the toast, include iconography, and expose semantic colour accents.
- exposes an enum-based API for four variants (info, success, warning, error) and a corner placement abstraction that maps to Vaadin positions while respecting RTL layouts.
- ships with matching theme styles in `vaadin-notification-card.css` so that the look is consistent in both LTR and RTL modes.
- includes helper methods (e.g., `setCloseButtonAriaLabel`, `show`) to streamline usage.
- resolves notification copy either via translation bundle keys or explicit bilingual payloads so ad-hoc messages remain localized.
- supports optional auto-dismiss timers that pause while the toast is hovered to avoid closing active interactions.

The design system view now showcases the component with translated copy, placement selector, and auto-dismiss control so designers and developers can reference the supported variants and behaviours.

## Consequences
- Teams can reuse `AppNotification` for consistent toast messaging without re-implementing styling.
- RTL behaviour is centralised, reducing the risk of incorrectly positioned notifications in Persian or Arabic locales.
- Additional variants in the future can extend the enums without breaking existing call sites.
- Theme styling for notifications is now maintained inside the design system theme directory, so future palette changes must update `vaadin-notification-card.css` to keep the component aligned.
