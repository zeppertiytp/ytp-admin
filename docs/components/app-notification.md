# AppNotification

`AppNotification` extends Vaadin's server-side `Notification` to provide a
branded toast component with localisation, theme variants, and direction-aware
positioning.

- **Source:** `components/src/main/java/com/youtopin/vaadin/component/AppNotification.java`
- **Frontend styles:** `components/src/main/resources/META-INF/resources/frontend/themes/app/app-notification.css`

## Key capabilities
- Four variants (`INFO`, `SUCCESS`, `WARNING`, `ERROR`) that automatically
  update the icon and CSS theme.
- Corner positioning aware of RTL locales (`fa`, `ar`, `he`, `ur`) so "top right"
  resolves to the correct edge when the UI direction flips.
- Localised titles and descriptions via `Message.translationKey`,
  `Message.bilingual`, or `Message.literal` helpers. When translation keys are
  missing, the component gracefully falls back to literal copy.
- Optional auto-dismiss timers that pause while the pointer hovers the toast and
  resume afterwards, using an inlined client-side bridge.
- Accessible close button that exposes an `aria-label` and title attribute.

## Usage
```java
AppNotification notification = new AppNotification(
        getTranslation("notification.saved.title"),
        getTranslation("notification.saved.body"),
        AppNotification.Variant.SUCCESS
);
notification.setCorner(AppNotification.Corner.BOTTOM_RIGHT);
notification.setAutoCloseDuration(Duration.ofSeconds(6));
notification.open();
```

To use message keys directly, create the component with
`AppNotification.Message.translationKey("notification.key")`. The helper
resolves translations when the locale changes.

## Configuration reference
- `setCorner(Corner)` – Positions the toast in one of the four corners. RTL
  locales automatically mirror the placement.
- `setAutoCloseDuration(Duration)` / `getAutoCloseDuration()` – Enables or
  disables the pause-on-hover timeout.
- `setVariant(Variant)` – Applies the desired color palette and icon.
- `setTitle` / `setDescription` – Accept either plain text or `Message`
  instances.
- `setCloseButtonAriaLabel(String)` – Customises the accessible label for the
  close button.
- `show(...)` – Static helpers that configure, open, and return the component.

## Accessibility & localisation
- Applies `role="alert"` and `aria-live="polite"` to respect screen readers.
- Mirrors layout classes for RTL locales so paddings and close button placement
  remain correct.
- Uses Vaadin icons sized at 24px with `aria-hidden` to avoid duplicate
  announcements.

## Sample reference
- Interactive controls under **Design System → Notifications** in
  `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/DesignSystemView.java`.

