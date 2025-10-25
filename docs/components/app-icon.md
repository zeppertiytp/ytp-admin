# AppIcon

`AppIcon` exposes the shared Iconoir sprite as a Flow component that renders a
custom web component (`app-icon`).

- **Source:** `components/src/main/java/com/youtopin/vaadin/component/AppIcon.java`
- **Frontend module:** `components/src/main/resources/META-INF/resources/frontend/components/app-icon.js`

## Key capabilities
- Automatically sets up an inline SVG `<use>` element referencing
  `frontend/icons/iconoir-sprite.svg`.
- `size` attribute accepts numeric values (pixels) or tokens such as `"20"`.
- Applies `role="img"` and derives an `aria-label` from the icon name unless a
  custom label is set.

## Usage
```java
AppIcon icon = new AppIcon("calendar", "24");
icon.getElement().setAttribute("aria-label", getTranslation("icon.calendar"));
```

Use alongside Vaadin components wherever an icon slot is available. The sample
navigation builds menu rows with `AppIcon` instances sized for the drawer.

## Sample reference
- `samples/src/main/java/com/youtopin/vaadin/samples/ui/layout/MainLayout.java`
  converts logical icon names from `menu/navigation-menu.json` into `AppIcon`
  instances for navigation links.

