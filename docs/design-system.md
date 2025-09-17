# Admin UI Design System

This project now uses a token-driven design system that keeps every view
consistent across light, dark and RTL layouts. The foundations are
implemented in `frontend/themes/app/tokens.css` and applied through
utility classes and view helpers.

## Foundations

| Category   | Tokens / Guidance |
|------------|-------------------|
| **Color**  | Primary teal (`--color-primary-600`) for emphasis, indigo secondary accent, semantic support tones (`info`, `success`, `warning`, `danger`). Surfaces split into page (`--surface-base`) and elevated cards (`--surface-raised`). |
| **Typography** | Base font size 16 px with Vazirmatn stack. Utility tokens: `--font-size-sm`, `--font-size-base`, `--font-size-lg`, `--font-size-xl`. Heading classes (`view-title`, `section-title`) align with this scale. |
| **Spacing** | 4 px baseline (`--space-2xs` … `--space-2xl`). Layout padding uses `--layout-content-padding`, cards use `--layout-card-padding`. |
| **Radius & Elevation** | Rounded corners (`--radius-m` default) and layered shadows (`--shadow-xs` … `--shadow-lg`) provide depth without clutter. |

## Layout primitives

* `ViewFrame` (Java) ensures every view gets the `view-frame` styling and
  exposes helpers for default, narrow and full-width content sections.
* `.surface-card` (CSS) standardises cards with padding, border and
  shadow. `.view-section` and `.view-section-stack` organise vertical
  spacing between cards or nested groups.
* `.view-content--narrow` constrains forms and wizards to 720 px for
  comfortable reading.

## Navigation shell

* `.app-header` defines a 64 px tall top bar with brand/utility spacing.
* Drawer groups rely on `.app-drawer`, `.app-drawer__section-title` and
  `.app-nav-item` for uniform padding, hover states and icon/text
  alignment. The same classes work in RTL without extra tweaks.

## Form styling

* `GeneratedForm` now renders inside a `.form-shell` surface card. Each
  JSON section becomes a `.form-section` with consistent inner spacing.
* The submit bar uses `.form-actions`, pushing actions to the right in
  LTR and left in RTL automatically.
* Inputs inherit the global control height (`--control-height`) so text
  fields, selectors and buttons share the same vertical rhythm.

## Visual tokens in the Design System view

The `/design-system` route showcases:

* Foundations (spacing, typography, surfaces) explained alongside the
  primary palette.
* Interactive spacing and type swatches (`.token-grid` and
  `.token-swatch`) so designers can reference values quickly.
* Button and badge variants rendered inside reusable `surface-card`
  containers.

Use these primitives when adding new pages to keep the interface
balanced, airy and accessible.
