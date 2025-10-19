# Vaadin Admin Panel — v4.0.0

**Date:** 2025-08-24

## Summary
Stable foundation with RTL-first theme, light/dark persistence, language switch (FA/EN), polished navigation, and i18n-ready grid.

## Highlights
- Tokenized theme (`components/src/main/resources/META-INF/resources/frontend/themes/app`) with **light/dark** and **RTL**.
- Shell & drawer: header reorders by `dir`; drawer moves left/right with FA/EN.
- Theme persistence via **session + localStorage** and `document.documentElement[theme]`.
- Icon layer: **Iconoir-style** sprite + `AppIcon` Flow wrapper.
- Grid i18n (Filter/Apply/Clear/Rows per page) + locale change updates.
- Horizontal wizard component with configurable colors and responsive layout.
- Design System page to preview semantic colors.

## Structure
- `components/src/main/resources/META-INF/resources/frontend/themes/app/` → `styles.css` (entry), `tokens.css`, `light.css`, `dark.css`, `rtl.css`, `typography.css`
- `components/src/main/resources/META-INF/resources/frontend/components/app-icon.js`
- `components/src/main/resources/META-INF/resources/frontend/icons/iconoir-sprite.svg`
- `samples/src/main/java/com/youtopin/vaadin/samples/AppShell.java` (`@Theme("app")` + digit normalizer + theme init)
- `components/src/main/java/com/youtopin/vaadin/i18n/UiLocaleInitializer.java` (default locale + `dir/lang`)
- `samples/src/main/java/com/youtopin/vaadin/samples/ui/layout/MainLayout.java` (header/drawer, toggles, reordering)
- `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/DesignSystemView.java` (responsive DS samples)
- `components/src/main/java/com/youtopin/vaadin/component/FilterablePaginatedGrid.java` (i18n & icon)
- `components/src/main/java/com/youtopin/vaadin/component/HorizontalWizard.java` + `/wizard` sample view.

## Build
The repository is a Maven multi-module project.  Typical flows:

1. **Validate everything** (recommended):
   ```bash
   mvn clean verify
   ```
   This compiles the reusable `vaadin-components` library, executes the sample application build, and runs unit tests.
2. **Publish the component library to your local Maven cache** so it can be consumed by other projects:
   ```bash
   mvn -pl components -am clean install
   ```
   The `-am` flag ensures transitive modules are built when necessary.

## Run
### Sample showcase (Vaadin dev mode)
Launch the demo UI from the `vaadin-samples` module:
```bash
mvn -pl samples -am spring-boot:run
```
The application becomes available at http://localhost:8080 with routes such as `/` and `/design-system`.

> [!TIP]
> The repository ships with `.mvn/maven.config` that already enables `-am` by default,
> so running `mvn -pl samples spring-boot:run` from the root project will also build
> the component library before launching the demo.

### Production build
To produce an optimised bundle (e.g. before deploying the samples) run:
```bash
mvn -pl samples -am clean package -Pproduction
```
The resulting Spring Boot fat JAR will be placed under `samples/target/`.

## Known items
- Details caret styling can be tuned further.
- Server-side digit converters (optional next).
- Optional npm Iconoir migration.

## Next steps
1) Data Grid v1 (hide/show, resize, reorder, multi-sort, selection + bulk actions, saved views, export).
2) Spring Converters for Persian digits.
3) Command palette; dashboards; form generation; notifications; A11y audit.

