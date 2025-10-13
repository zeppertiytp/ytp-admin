# Vaadin Admin Panel — v4.0.0

**Date:** 2025-08-24

## Summary
Stable foundation with RTL-first theme, light/dark persistence, language switch (FA/EN), polished navigation, and i18n-ready grid.

## Highlights
- Tokenized theme (`frontend/themes/app`) with **light/dark** and **RTL**.
- Shell & drawer: header reorders by `dir`; drawer moves left/right with FA/EN.
- Theme persistence via **session + localStorage** and `document.documentElement[theme]`.
- Icon layer: **Iconoir-style** sprite + `AppIcon` Flow wrapper.
- Grid i18n (Filter/Apply/Clear/Rows per page) + locale change updates.
- Design System page to preview semantic colors.

## Structure
- `frontend/themes/app/` → `styles.css` (entry), `tokens.css`, `light.css`, `dark.css`, `rtl.css`, `typography.css`
- `frontend/components/app-icon.js`
- `src/main/resources/static/icons/iconoir-sprite.svg`
- `samples/src/main/java/com/youtopin/vaadin/samples/AppShell.java` (`@Theme("app")` + digit normalizer + theme init)
- `samples/src/main/java/com/youtopin/vaadin/samples/config/UiLocaleInitializer.java` (default FA, set `dir/lang`)
- `samples/src/main/java/com/youtopin/vaadin/samples/ui/layout/MainLayout.java` (header/drawer, toggles, reordering)
- `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/DesignSystemView.java` (responsive DS samples)
- `components/src/main/java/com/youtopin/vaadin/component/FilterablePaginatedGrid.java` (i18n & icon)

## Run
```bash
mvn -Dvaadin.clean-frontend=true clean spring-boot:run
```
Routes: `/` and `/design-system`

## Known items
- Details caret styling can be tuned further.
- Server-side digit converters (optional next).
- Optional npm Iconoir migration.

## Next steps
1) Data Grid v1 (hide/show, resize, reorder, multi-sort, selection + bulk actions, saved views, export).
2) Spring Converters for Persian digits.
3) Command palette; dashboards; form generation; notifications; A11y audit.

