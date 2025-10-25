# Component Catalogue

This catalogue describes every reusable component exported by the
`vaadin-components` module. Each entry summarises the intent, links to the
source, highlights integration steps, and references extended guides.

| Component | Summary | Documentation |
| --- | --- | --- |
| AppNotification | Branded toast notification with localisation, icon variants, and auto-dismiss support. | [Guide](app-notification.md) |
| AppCard | Wrapper around `vaadin-card` with helper slots for media, headers, subtitles, and footers. | [Guide](app-card.md) |
| AppIcon | Lightweight custom element bridge that renders Iconoir glyphs from the shared sprite. | [Guide](app-icon.md) |
| FilterablePaginatedGrid | Data grid with dynamic filters, pagination, saved views, and export actions. | [Guide](filterable-paginated-grid.md) |
| GeneratedForm | Runtime component that renders JSON form specifications produced by the form engine. | [Guide](generated-form.md) |
| HorizontalWizard | Multi-step progress indicator with keyboard and pointer interactions. | [Existing guide](../horizontal-wizard.md) |
| JalaliDateTimePicker | Date and date-time input with Jalali calendar support and ISO conversion utilities. | [Guide](jalali-date-time-picker.md) |
| LocationPicker | Map-backed location selector with address search and coordinate output. | [Guide](location-picker.md) |

## Contributing new components

1. Place Java sources under `components/src/main/java/com/youtopin/vaadin/component/`.
2. Add frontend dependencies to `components/src/main/resources/META-INF/resources/frontend/components/` when required.
3. Document the component in this directory and update the table above.
4. Link to the new guide from the root `README.md` and any relevant sample view.

All guides should include:
- **Overview** with intent and key classes.
- **Usage** snippet demonstrating typical setup.
- **Configuration** section for theming, events, and data binding.
- **Accessibility & localisation** notes highlighting ARIA roles, keyboard support, and RTL behaviour.
- **Sample references** pointing to demo views or JSON fixtures.

