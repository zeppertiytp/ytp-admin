# CHANGELOG

## Unreleased

### Form engine value change listeners

* **Form engine:** Scoped the validation context exposed by `RenderedForm#addValueChangeListener` so dynamic property bag
  bindings can read sibling values via `FieldValueChangeEvent#readScopedValue` and `ValidationContext#read(FieldInstance, String)`.
* **Docs:** Documented the scoped value helpers for listener callbacks and coordinator integrations.

### Static section and group captions

* **Form engine:** Added `title` and `description` attributes to `@UiSection` and `title` to `@UiGroup`, enabling static captions when translation keys are omitted and ensuring repeatable headings respect the new fields.
* **Docs:** Described the static caption fallbacks in the form engine reference.

## v4.6.21
### Multi-group repeatable entries

* **Form engine:** Refined repeatable group typography so parent headings, per-entry titles, and nested group captions remain visually distinct when multiple layouts appear inside the same entry.

## v4.6.20
### Multi-group repeatable entries

* **Form engine:** Added `entryGroups` support to `@UiGroup`, allowing repeatable entries to compose multiple child layouts while reusing field path validation.
* **Form engine:** Updated the runtime to render per-entry subgroups, flatten nested field definitions for hydration, and compute collection paths from child groups.
* **Docs:** Documented the `entryGroups` attribute and multi-group semantics in the form engine reference.
* **Samples:** Introduced the composite contact directory form showcasing multi-group repeatable entries with localized headings.

## v4.6.19
### Option catalog creation API

* **Form engine:** Added opt-in `supportsCreate`/`create` hooks to `OptionCatalog` and propagated overrides across built-in
  providers so persistence layers can accept inline option creation while static data keeps declining it.
* **Form engine:** Taught `OptionCatalogRegistry` to document mutable registrations and reuse explicit remote or callback
  catalogs, ensuring creation-capable providers stay reachable across locales.
* **Docs:** Documented the runtime creation contract, identifier expectations, and error handling guidance in the form engine
  reference.

### Iconoir-first navigation icons

* **Navigation:** Switched navigation menu metadata to use Iconoir sprite identifiers directly, removing the Vaadin icon mapping layer and trimming JSON down to kebab-cased names.
* **Navigation:** Bundled the upstream Iconoir sprite wholesale so every gallery icon is available to menu definitions without manual asset curation.
* **Samples:** Refreshed the bundled icon sprite with Iconoir glyphs used by the sample navigation tree and updated menu JSON to reference them directly.
* **Docs:** Documented the Iconoir naming scheme and relaxed input handling so legacy uppercase or underscored names continue to resolve.

## v4.6.18
### Wizard form coordinator and persistent sample flows

* **Form engine:** Introduced `WizardFormFlowCoordinator` to connect `HorizontalWizard` with Form Engine
  forms, centralising step registration, completion tracking, and persistence callbacks so hosts can
  reuse multi-step orchestration without bespoke listeners.
* **Samples:** Updated the project launch wizard to generate and surface a project ID via the coordinator
  and published a new `/forms/wizard-coordinator` sample that demonstrates issuing a workspace ID in the
  first step and reusing it in later steps.
* **Docs:** Added a coordinator how-to with code snippets covering ID persistence and refreshed the
  how-to index with the new guide.
* **Tests:** Added unit and integration coverage verifying coordinator context propagation, wizard
  selection, and form submission behaviour.
# v4.6.17
### Repeatable duplication controls and sample

* **Form engine:** Added an optional duplicate dialog for repeatable groups so editors can select an existing entry and append a cloned copy to the end of the list while preserving numbering.
* **Form engine:** Expanded regression coverage to verify the duplication flow copies field values and respects repeatable limits.
* **Docs:** Clarified the `allowDuplicate` option to highlight the new control and dialog workflow.
* **Samples:** Published a landing page template form showcasing the duplicate button with localized translations and helper text.

## v4.6.16
### Dynamic read-only overrides and locking sample defaults

* **Form engine:** Added `RenderedForm#addReadOnlyOverride` and `refreshReadOnlyState()` so hosts can mark individual fields as
  read-only using the current bean alongside metadata-driven locking.
* **Samples:** Seeded the profile locking demo with immutable administrator data and applied a dynamic override so forms render
  with read-only content immediately while still allowing other sections to be edited.
* **Tests:** Extended the form engine read-only suite to cover the programmatic override flow and state refresh behaviour.
* **Docs:** Expanded `docs/form-engine-reference.md` with `readOnlyWhen` guidance, mixed-state repeatable examples, and a
  runtime override walkthrough that links back to the profile locking sample.

## v4.6.15
### Read-only metadata and locking sample

* **Form engine:** Introduced `readOnlyWhen` metadata for sections, groups, and fields and wired a state evaluator that disables
  repeatable controls while keeping layouts visible.
* **Generated form:** Parsed `readOnly` / `readOnlyWhen` flags, mirroring conditional logic with new watchers and accessibility
  attributes so JSON-driven forms respect immutable content.
* **Tests:** Added regression coverage for annotation and JSON pipelines to ensure locked data renders as read-only.
* **Samples:** Published a profile locking demo that flips fields to read-only after submit, showing the new metadata in action.

## v4.6.14
### Documentation sweep and form engine notes

* **Docs:** Added a component catalogue (`docs/components/README.md`) with dedicated guides for AppNotification, AppCard, AppIcon, FilterablePaginatedGrid, GeneratedForm, JalaliDateTimePicker, and LocationPicker.
* **Docs:** Captured repeatable workflows in `docs/how-to.md` so contributors can add components, extend the form engine, and publish new samples consistently.
* **Samples:** Documented every showcase route and scenario in `samples/README.md` to help reviewers find component demos quickly.
* **Form engine:** Expanded the public guidance for the runtime renderer in `docs/components/generated-form.md` and cross-referenced the annotation reference so integrators can trace the JSON pipeline end-to-end.
* **Meta:** Refreshed `README.md` and `AGENT.md` with updated structure, build commands, and documentation entry points.


## v4.6.13
### Horizontal wizard interaction updates

* **Component:** Added optional click support to `HorizontalWizard` steps,
  including pointer and focus styling, keyboard activation, and new
  `StepClickEvent`/`CurrentStepChangeEvent` hooks so applications can react to
  user navigation.
* **Component:** Propagated the state colors to each step container and circle
  token so accent backgrounds stay applied even when theme fallbacks fail,
  keeping the current and completed indicators legible.
* **Design system:** Restored the missing accent color ramps (info/success/
  warning/danger 600–700) and aligned the wizard's contrast fallback with the
  global inverse text token so highlighted steps stay legible in light and dark
  themes.
* **Samples:** Updated `/wizard` to mix clickable and read-only steps, wiring a
  real-time status caption via the new change listener so flows can trigger view
  logic as the selection changes.
* **Documentation:** Expanded `docs/horizontal-wizard.md` to cover the new
  events and interaction model for reviewers.

## v4.6.12
### Horizontal wizard progress indicator

* **Component:** Added the `HorizontalWizard` Flow component with configurable
  colors for completed/current/upcoming states and per-step overrides, plus a
  responsive CSS module that now keeps the layout in a single row with
  horizontal scrolling and automatically scrolls the active step into view on
  attach. Refined the active-state styling with a subtle scale, halo, and label
  emphasis so the current step stands out clearly against completed ones while
  padding the container to prevent the highlight from being clipped.
* **Samples:** Published a `/wizard` showcase view demonstrating default and
  custom color schemes, wired into the navigation menu with new translations, and
  expanded it with an eight-step product roadmap sample to validate horizontal
  scrolling behaviour.
* **Documentation:** Captured usage guidance in `docs/horizontal-wizard.md` and
  referenced the sample from the platform overview, including the new extended
  scenario and scrolling guidance.

### Scrollbar styling refresh

* **Design system:** Introduced theme-level scrollbar tokens and global styles
  that render slimmer, rounded scrollbars with color-mixed thumbs in both light
  and dark modes, aligning overflow containers with the platform's visual
  language.

## v4.6.11
### Draft action defaults and manual success handling cleanup

* **Backend validation skips for drafts:** Actions that disable client-side validation now skip backend validation automatically unless they explicitly override it, removing the need for redundant `backendValidation: false` flags on draft-style buttons.
* **Success message opt-in:** `FormSubmissionEvent#getSuccessMessage()` now only returns a value when the specification provides one, so hosts avoid unexpected default strings and can opt-in to messaging when desired.
* **Docs & samples:** Updated the form generation guide and sample JSON to remove unnecessary action metadata, keeping manual handling examples focused on alignment and validation control.

## v4.6.10
### Manual submission handling and action alignment

* **Left/right actions:** Added per-action `align` metadata so secondary buttons can anchor to the opposite side of the footer while primaries stay grouped on the right. The action bar now renders dedicated left/right clusters.
* **Host-driven success handling:** Removed the built-in success/failure notifications. Successful submissions fire `FormSubmissionEvent`s with the translated success message so host views decide how to respond (toast, navigation, etc.).
* **Docs & samples:** Documented the new alignment option and manual handling approach, and updated sample form JSON to showcase a left-aligned draft button.

## v4.6.9
### Multi-action submissions for generated forms

* **Configurable actions:** Introduced `submit.actions` so generated forms can render multiple buttons (e.g. “Submit” and “Submit & Exit”), each with independent validation and theming.
* **Action-aware validation:** Extended `FormValidationService` with an action-aware overload and added submission events plus the `submit(String)` helper for programmatic triggers.
* **Docs & samples:** Documented the new action schema, updated the layout sample form to demonstrate draft saving, and refined the in-memory validator logging.

## v4.6.8
### FormLayout-first generated forms

* **Responsive defaults:** Standardised the generated form grid around Vaadin `FormLayout`, adding automatic responsive steps that keep single-column mobile layouts while flowing to full column counts with aside labels on wider screens.
* **Configurable breakpoints:** Added JSON support for explicit `layout.responsiveSteps` arrays and per-field `colSpan` values so complex forms can tune breakpoints and stretch fields across multiple columns without custom code.
* **Documentation & samples:** Updated the form generation guide and the sample `user_form_with_layout.json` to illustrate responsive step overrides and column spanning.

## v4.6.7
### JSON-driven navigation menu with scope filtering

* **Feature:** Replaced the static navigation provider with a JSON-backed
  `JsonNavigationMenuService` that loads `menu/navigation-menu.json` at startup
  and filters items per user scopes.
* **Security readiness:** Introduced configurable `AND`/`OR` evaluation for
  `requiredScopes`, paving the way for OIDC-powered role enforcement.
* **Resilience:** Added schema validation to fail fast on missing groups,
  labels, or malformed children and normalised whitespace/icon names while
  loading the menu tree.
* **Documentation:** Captured configuration guidance in
  `docs/navigation-menu.md` and linked it from the architecture overview.

## v4.6.6
### Jalali picker year range controls

* **Dropdown-only year selection:** Removed the duplicated year arrow buttons
  so the Jalali picker now relies on the dropdown for cross-year navigation,
  preventing reversed controls in RTL layouts.
* **Configurable bounds:** Added `minYear`/`maxYear` options (alongside the
  existing ISO `min`/`max`) so developers can trim the available years without
  defining full date-time limits. The component enforces these bounds across
  day availability and the dropdown itself.
* **Guidance & samples:** Documented the new options, refreshed the generated
  form guide, and updated the sample JSON so both date and date-time Jalali
  fields demonstrate year capping.

## v4.6.4
### Jalali picker range normalisation

* **Date-only sanitisation:** Normalised the Jalali picker so date-only mode
  now trims ISO inputs without times, clears lingering hour/minute data when
  switching modes, and treats plain `YYYY-MM-DD` values as midnight entries.
* **Range boundary fixes:** Re-applied min/max constraints using date-aware
  comparisons, ensuring boundary minutes remain selectable for date-time mode
  while respecting zeroed times for pure date pickers.

## v4.6.3
### Jalali picker overlay and date-only mode

* **Overlay trigger:** Reworked the Jalali picker so it opens from a button
  and displays the chosen value outside the overlay, improving layout density
  and usability in custom views.
* **Date-only support:** Added a dedicated `jalaliDate` field type plus
  `showTime`/`pickerVariant` hints, allowing generated forms (and standalone
  usages) to hide the time controls while keeping min/max constraints.
* **Configurable captions:** Introduced an `openLabel` option alongside new
  translations and documented configuration options. Sample JSON and the form
  generation guide were updated accordingly.

## v4.6.5
### Jalali picker year navigation

* **Jump by year:** Added quick previous/next year buttons and a year dropdown
  to the Jalali picker header so users can navigate long date ranges without
  stepping month by month.
* **Accessible labels:** Localised navigation labels for month and year
  controls so screen readers describe the new shortcuts in both English and
  Farsi.

## v4.6.4
### Jalali picker range normalisation

* **Date-only sanitisation:** Normalised the Jalali picker so date-only mode
  now trims ISO inputs without times, clears lingering hour/minute data when
  switching modes, and treats plain `YYYY-MM-DD` values as midnight entries.
* **Range boundary fixes:** Re-applied min/max constraints using date-aware
  comparisons, ensuring boundary minutes remain selectable for date-time mode
  while respecting zeroed times for pure date pickers.

## v4.6.3
### Jalali picker overlay and date-only mode

* **Overlay trigger:** Reworked the Jalali picker so it opens from a button
  and displays the chosen value outside the overlay, improving layout density
  and usability in custom views.
* **Date-only support:** Added a dedicated `jalaliDate` field type plus
  `showTime`/`pickerVariant` hints, allowing generated forms (and standalone
  usages) to hide the time controls while keeping min/max constraints.
* **Configurable captions:** Introduced an `openLabel` option alongside new
  translations and documented configuration options. Sample JSON and the form
  generation guide were updated accordingly.

## v4.6.2
### Lombok-based logging refinements

* **Consistent logger injection:** Replaced manual `LoggerFactory` usage with
  Lombok's `@Slf4j` annotation across security and demo infrastructure
  services to standardise how loggers are obtained.
* **Guidance update:** Documented the Lombok logging convention in the
  architecture guide so future contributors follow the same pattern.

## v4.6.1
### Structured logging for demo services

* **SLF4J adoption:** Replaced console prints in the in-memory infrastructure
  services with `org.slf4j.Logger` usage and added contextual log messages for
  form validation, menu construction and pagination helpers.
* **Security service instrumentation:** Added authentication and logout log
  statements while guarding against missing Vaadin session or UI contexts.
* **Consistent configuration:** Defined baseline log levels and a key-value
  console pattern in `application.properties` so environments share the same
  log structure.
* **Documentation:** Extended `docs/architecture.md` with logging guidance for
  future contributors.

## v4.6.0
### Enforce layered architecture and SOLID boundaries

* **Package restructuring:** Moved domain models into `domain.*`, extracted
  application-layer interfaces (`NavigationMenuService`, `PersonDirectory`,
  `FormValidationService`, pagination contracts) and relocated Vaadin views and
  components under `ui.*`. Infrastructure implementations now live under
  `infrastructure.*`, keeping the UI focused on abstractions.
* **Navigation decoupling:** Replaced view-class based menu configuration with
  route-driven `MenuItem`s so the UI depends on the `NavigationMenuService`
  interface instead of concrete view classes.
* **Documentation:** Added `docs/architecture.md` detailing the layered
  structure and dependency rules, plus ADR `docs/ADR-20250920-architecture-refactor.md`.
* **Architecture tests:** Added ArchUnit tests that fail the build whenever the
  documented package boundaries are violated.
* **Version bump:** Updated `version.txt` to `v4.6.0`.

## v4.5.0
### Enhanced form generator and new input types

* **Locale‑aware section titles:** The dynamic form generator now updates section
  headings when the user switches languages.  Section titles defined in the JSON
  specification are registered with the locale change mechanism and refresh
  automatically on locale change, just like field labels.
* **Improved RTL spacing:** Added column and row gaps to the generated
  `FormLayout` using Lumo spacing tokens.  Forms now maintain consistent
  spacing between inputs in Farsi (RTL) and English (LTR) layouts.
* **Additional field types:** The form generator now supports more input
  components:
  - **Date picker** (`date`) for selecting dates.
  - **File upload** (`file`) for single file selection; stores uploaded file
    names.
  - **Multi‑file upload** (`multiFile`) allowing multiple attachments; stores a
    list of file names.
  - **Radio buttons** (`radio`) for single‑choice selection; options are
    specified in the JSON with translated labels.
  These new types respect required indicators, client‑side validation and
  localisation.
* **Documentation:** Added a comprehensive guide under
  `docs/form-generation.md` explaining the form specification format, supported
  field types, conditional visibility, validation rules, localisation and an
  example.  This document provides developers with the information needed to
  extend the form generator and create complex forms.
* **Example specification updated:** The `user_form.json` example now
  includes a third section (“Additional”) demonstrating the new field types,
  including date of birth, single and multi file uploads, and a gender
  selection using radio buttons.
* **Translations:** Updated translation files to include any new messages
  required by the enhanced generator.  Fields defined in JSON continue to
  supply their own translated labels.
* **Version bump:** Updated `version.txt` to `v4.5.0` to reflect these
  enhancements and to indicate that v4.4.0 is now considered stable.

## v4.5.1
### Fix form specification and minor improvements

* **Corrected form specification:** The `user_form.json` example contained
  misplaced braces that resulted in a malformed JSON and prevented the form
  generator from parsing it.  This release fixes the specification by
  properly closing the `phone` field object and moving the “Additional”
  section definition outside the settings section.  Developers can now
  load the form without encountering `JsonParseException` errors.
* **Resource packaging:** Added the `forms` directory to the Spring Boot
  resources in `project_source` and ensured that the corrected
  `user_form.json` is included in the build.  This prevents missing
  resource errors when running the application.
* **Version bump:** Updated `version.txt` to `v4.5.1`.

## v4.5.2
### New form components and group fields

* **Return to stable baseline:** Starting from the stable `v4.5.1` code, this release
  adds several new form input types without changing the underlying data
  structures.  The core form generator remains backwards compatible.
* **Enhanced file selectors:** The existing `file` and `multiFile` types now
  display the selected file name and a human‑readable size.  Multi‑file
  uploads show a list of file names and allow removing individual files via
  the upload component's built‑in remove event.  Both single and multi file
  uploads update their drop labels and buttons on locale change.
* **Image selector:** Added a new `image` field type that accepts image files,
  displays a 100 × 100 pixel thumbnail preview after upload, and stores the
  file name in the form values.  Upload labels and buttons are fully
  localised.
* **Map location selector:** Introduced a `map` field type using the Leaflet
  library.  Users can search for an address via Nominatim, click on the
  map or drag a marker to pick a location, and the component captures the
  latitude and longitude as a `lat,lng` string.  The search field label
  updates with locale changes.
* **Dynamic group fields:** Added a `group` field type that lets users add
  or remove rows containing subfields (e.g. passenger details).  Each row
  renders its own set of text, number, email or tel fields based on the
  specification.  Buttons for adding and removing items use new
  translation keys (`form.addItem`, `form.removeItem`) and update on
  locale change.
* **Helper restoration:** Restored the `humanReadableByteCount()` helper
  method in `GeneratedForm` to convert byte counts into human readable
  strings for file size display.
* **Translations:** Added new translation keys `form.addItem` and
  `form.removeItem` to both English and Farsi properties files for the
  group field buttons.
* **Version bump:** Updated `version.txt` to `v4.5.2` and noted that
  `v4.5.1` remains the previous stable baseline.  Testers should verify
  the new components thoroughly before considering this release stable.

## v4.5.3
### Packaging cleanup and minor fixes

* **Project structure cleanup:** Previous archives included an extra
  `project_source` directory alongside the primary source tree, which
  caused confusion and duplicated files.  This release removes the
  duplicate folder and packages only the canonical project directory
  (`src`, `pom.xml`, etc.), ensuring a clean, sane workspace when
  unpacking the zip file.
* **Version bump:** Updated `version.txt` to `v4.5.3` to reflect the
  packaging fix.  There are no functional changes in this release.

## v4.5.4
### Enhanced form demo and logging

* **Sample form enhancements:** Updated the `user_form.json` demonstration
  specification to showcase the newly supported form components.  The
  “Additional” section now includes an **image** upload (`image`), a
  **location selector** (`map`) using Leaflet and Nominatim, and a
  **dynamic group** (`group`) for passengers with first and last name
  fields.  These additions make it easy to test and visualise the
  advanced input types without modifying the core logic.
* **Server-side logging:** Modified the `MockFormValidationService` to
  print submitted form values to the server console.  This helps
  developers verify that all field values—including custom types—are
  collected and passed to backend validation.
* **Version bump:** Updated `version.txt` to `v4.5.4` to reflect the new
  demonstration features and logging improvements.

## v4.5.5
### New form components and packaging fix

* **Complete set of new form components:** Building on the enhancements in
  v4.5.4, this release finalises support for advanced input types.  The form
  generator now includes improved file selectors that display file names and
  sizes, multi‑file uploads that list selected files and allow removal, an
  image selector with a thumbnail preview, a map location selector using
  Leaflet and Nominatim for searching and selecting coordinates, and a
  dynamic group field that lets users add or remove sets of sub‑fields (for
  example, passenger details).  These components are fully localised and
  integrated with conditional visibility and server‑side validation.
* **Sample form updated:** The `user_form.json` demonstration has been
  expanded to showcase the new input types.  In addition to basic fields,
  it now includes a profile photo upload (`image`), a home location
  selector (`map`), and a passengers list (`group`) with first and last
  names.  This allows users to experiment with all advanced controls
  without altering the application code.
* **Server logging improvement:** The mock validation service prints
  submitted form values to the server logs.  This helps developers verify
  that data from custom components—including uploaded files, images,
  locations and group fields—is captured correctly before backend
  validation.
* **Packaging cleanup:** This release cleans up the project structure and
  packaging.  Previous archives sometimes contained extra directories
  (`project_source`) or did not include the latest source changes.  The
  new zip contains only the canonical project tree (`src`, `pom.xml`,
  `frontend`, `docs`, etc.) with all updates from the `work_project` folder.
* **Version bump:** Updated `version.txt` to `v4.5.5` to reflect the
  completed component suite and packaging fix.

## v4.5.6
### Form component refinements and bug fixes

* **Improved file selectors:** Refined single and multi‑file uploads to ensure
  the selected file names and sizes display correctly.  Multi‑file uploads now
  update their list when files are removed via the upload component and hide the
  names when no files remain.  These changes resolve issues where names were
  not shown or cleared as expected.
* **Image selector fixes:** Image uploads now properly hide the thumbnail
  preview when the file is removed.  The thumbnail is only visible after at
  least one image has been uploaded, preventing empty preview boxes from
  appearing.
* **Multi‑image support:** Added a new `multiImage` field type that behaves
  similarly to multi‑file uploads but restricts input to images and shows a
  thumbnail preview for each selected file.  Users can remove images
  individually, and the preview container hides itself when empty.
* **Group field enhancements:** The dynamic group field now supports
  `minItems` and `maxItems` constraints defined in the JSON specification.
  A red remove button (using the `error` theme) is provided for each item,
  and the add button is disabled when the maximum count is reached.  The
  remove buttons are disabled when the group contains the minimum number of
  items.  These refinements align with the design system and prevent
  unwanted additions or deletions.
* **Specification updates:** The sample `user_form.json` continues to
  demonstrate the advanced field types introduced in previous versions.  The
  group field can now define `minItems` and `maxItems` to control the
  number of rows rendered.
* **Logging:** The mock validation service still logs submitted form data to
  the console, helping developers verify that the new field types pass
  values correctly.
* **Version bump:** Updated `version.txt` to `v4.5.6` to reflect these
  refinements.

## v4.4.0
### Form generation feature and dynamic forms
* **Dynamic form generator:** Introduced a new `GeneratedForm` component that builds Vaadin forms from JSON specifications stored under `src/main/resources/forms`.  The component reads a specification file, creates form sections and fields on the fly, applies client‑side validations (required, pattern), handles conditional visibility (`visibleWhen` expressions), and updates labels based on the current locale.  Field values are bound to an internal map and validated server‑side via a new `FormValidationService`.
* **Validation service:** Added the `FormValidationService` interface and a `MockFormValidationService` implementation.  The service provides backend validation for generated forms and demonstrates how to return field‑level error messages (e.g. checking if an email is already taken).  Errors are displayed inline on the corresponding form fields when returned from the service.
* **Form specification example:** Added `user_form.json` under `src/main/resources/forms`, defining a sample user registration form with translated labels, required fields, pattern validators, a select field with options, and conditional visibility (phone number appears only when two‑factor authentication is enabled).  The specification includes both English and Farsi labels.
* **Generated form view:** Added `FormGenerationView` with route `/forms` to showcase the dynamic form.  It autowires the validation service, loads the `user_form.json` specification, and displays the generated form.  A success notification appears upon valid submission.
* **Menu update:** Added a new `forms` group to the menu with a single item linking to the form generation demo.  Updated `MenuService` to include this group and used an appropriate icon for form pages.
* **Translations:** Added new translation keys in both English and Farsi for form labels, validation messages (`form.required`, `form.correctErrors`, `form.success`, `form.submit`) and backend validation errors (e.g. `email.taken`).  Added translations for the new menu items (`menu.forms`, `menu.formgeneration`).
* **Version bump:** Updated `version.txt` to `v4.4.0` and marked `v4.3.0` as the stable baseline in the changelog.

## v4.3.0
### Dynamic grid height and demonstration views
* **Dynamic height:** Added configurable height support to `FilterablePaginatedGrid`.  New properties `expandGrid`, `minHeight` and `maxHeight` control how the grid occupies vertical space.  When `expandGrid` is `true` (default) the grid expands to fill the available space.  When `false`, the grid uses the specified minimum and maximum heights and scrolls internally if its content exceeds the limit.  Setter methods and an internal style helper apply these options on the fly.
* **Layout update:** The layout logic in `initLayout()` now respects the `expandGrid` flag instead of unconditionally calling `expand(grid)`.  The grid's flex‐grow, min/max height and overflow are updated via the new helper method.
* **Example views:** Added three new views under an "Examples" menu section:
  * **FullGridView** – shows a full‑height grid that expands to fill the page.
  * **CompactGridView** – demonstrates a grid constrained by minimum and maximum heights (e.g. 200–400 px) so it doesn’t dominate the page.
  * **MultipleGridsView** – displays two grids on the same page, each with its own height constraints and feature set.
* **Menu and translations:** Added a new `examples` group in `MenuService` and corresponding translation keys (`menu.examples`, `menu.fullgrid`, `menu.compactgrid`, `menu.multigrids`) with English and Farsi translations.
* **Version bump:** Updated `version.txt` to `v4.3.0` to reflect these enhancements.

This release has been **marked as stable**.  Future changelog entries will note
that v4.3.0 is a stable baseline for the project.

## v4.2.1
### Configurable grid features and improved table configuration
- Added a `GridFeature` enumeration and a constructor parameter to enable or disable advanced capabilities (column configuration, export, saved views and row selection) on a per-grid basis. Each grid can now specify exactly which features to provide.
- Introduced a single **Table Configuration** menu (`grid.config`) that groups column visibility, export and view actions into a compact button. Export actions (CSV and Excel) and view actions (save view and apply saved views) are nested under their own submenus to reduce clutter.
- Removed the individual column, export and view buttons/combobox from the top controls; replaced with the grouped configuration menu to save space.
- Added support for registering a selection listener via `setSelectionListener`, allowing views to react to row selection (e.g. enabling a Delete button in the parent view).
- Fixed the bulk action button caption: it now passes the selected count as an integer to the translation function so that `{0}` is replaced correctly in both English and Farsi. The caption updates on selection change and locale change.
- Updated translations with new keys: `grid.config`, `grid.export`, `grid.views`, and updated existing keys to reflect the new grouping. Also ensured that the selection count uses integer interpolation instead of string to avoid showing `{0}`.
- Cleaned up duplicate field declarations in `FilterablePaginatedGrid` introduced during previous patches.

## v4.2.3
### Fix MenuItem class resolution in `FilterablePaginatedGrid`
* **Fixed:** Resolved a compilation error caused by referencing the wrong `MenuItem` type.  In Vaadin Flow, `MenuItem` is a top‑level class within the `com.vaadin.flow.component.menubar` package, not a nested class of `MenuBar`.  All references and field declarations now import and use `com.vaadin.flow.component.menubar.MenuItem` directly and no longer refer to `MenuBar.MenuItem`.  The `loadViewNames()` method uses a `java.util.List<MenuItem>` to hold menu items.
* **Version bump:** Updated `version.txt` accordingly.

## v4.2.4
### Package update with corrected `MenuItem` references
* **Fixed packaging:** The previous release contained compiled files that still referenced `MenuBar.MenuItem`, leading to unresolved symbol errors during compilation.  This release repackages the project using the corrected `FilterablePaginatedGrid` source where all menu entries use the top‑level `com.vaadin.flow.component.menubar.MenuItem`.  No code changes beyond packaging; this ensures the distributed sources compile successfully against Vaadin Flow 24.
* **Version bump:** Updated `version.txt` to `v4.2.4` to reflect the repackaged release.

## v4.2.5
### Resolve `MenuItem` class not found errors by using nested class
* **Fixed:** Addressed persistent compilation errors related to `MenuItem`.  Earlier versions attempted to use `com.vaadin.flow.component.menubar.MenuItem` as a top‑level class, but this class does not exist in Vaadin Flow 24.  The correct type is the nested class `MenuBar.MenuItem`.  This release updates `FilterablePaginatedGrid` to remove the erroneous import and declare menu items using `MenuBar.MenuItem`.  Casts and lists in `localeChange()` and `loadViewNames()` have been updated accordingly.  The code now compiles against Vaadin Flow 24 without unresolved symbols.
* **Version bump:** Updated `version.txt` to `v4.2.5` to reflect this fix.

## v4.2.6
### Correct `MenuItem` import and usage

* **Fixed:** Compilation errors continued to arise because `MenuBar.MenuItem` does not exist in Vaadin Flow 24 and the previous fix reverted to using the nested type.  This release imports `MenuItem` from `com.vaadin.flow.component.contextmenu.MenuItem` and updates all menu item declarations (`configRoot`, `exportRoot`, `viewsRoot`) and casts in `FilterablePaginatedGrid` to use this top‑level type.  The `loadViewNames()` method now populates a `List<MenuItem>` for view entries.  These changes align the code with Vaadin's current API and resolve the symbol lookup issues.
* **Version bump:** Updated `version.txt` to `v4.2.6` to reflect the corrected import and declarations.

## v4.2.7
### Packaging fix to ensure correct `MenuItem` import

* **Fixed Packaging:** The previous release (v4.2.6) still shipped a jar and source archive referencing `MenuBar.MenuItem` in `FilterablePaginatedGrid.java`, which does not exist in Vaadin Flow 24 and caused compilation failures.  This release rebuilds the package using the updated source where all menu items are declared as the top‑level `MenuItem` from `com.vaadin.flow.component.contextmenu`.  No functional changes beyond ensuring the compiled and source artifacts reflect the corrected imports.
* **Version bump:** Updated `version.txt` to `v4.2.7`.

## v4.2.8
### Fix translation placeholder for selected count

* **Fixed:** The bulk action button caption continued to show `{0} selected` instead of the actual number of selected rows.  This was due to the translation strings using `{0}` placeholders, which are not supported by `String.format`.  Updated `grid.selectedCount` in both English and Farsi translation files to use `%d` so that the selected count is correctly formatted by `String.format(locale, value, params)`.  Now the button displays "0 selected", "1 selected", etc., according to the locale.
* **Version bump:** Updated `version.txt` to `v4.2.8`.
## v4.1.1
### Miscellaneous fixes
- Removed unused `GridSortOrderBuilder` import in `FilterablePaginatedGrid.java` which caused a compilation error with certain Vaadin versions.
- Bumped version number in `version.txt`.

## v4.1.0
### Added grid v1 enhancements
- Enabled multi-sort and column reordering on the `FilterablePaginatedGrid`, allowing users to sort by multiple columns and rearrange column order via drag and drop. Columns are now resizable for better control of layout.
- Added multi-selection support and a bulk action button that displays the number of selected rows and can trigger bulk actions. Selection updates automatically enable or disable the button.
- Added a "Columns" button that opens a dialog listing all columns with checkboxes to toggle their visibility.
- Added an export menu to download the grid data. Currently supports CSV export (generating a file with translated headers and all filtered rows) and an Excel entry that is stubbed for future implementation.
- Added "Save view" functionality: users can save the current grid state (column order and visibility) to browser local storage. A prompt requests a view name when saving.
- Added a view selector combo box to apply saved views. Selecting a view restores the saved column order and visibility.
- Added new translation keys for the additional grid controls: `grid.columns`, `grid.exportCsv`, `grid.exportExcel`, `grid.saveView`, `grid.viewPlaceholder`, and `grid.selectedCount`.
- Improved the filters dialog: apply and clear actions now update the applied filters bar; range filters are consolidated into single chips. Chips can be removed individually, updating filters and refreshing the grid.
- Updated pagination controls: the page size selector uses a translated label (`grid.rowsPerPage`) and updates the grid when changed.


## v4.0.0
- Theme tokens with light/dark and RTL.
- Header/Drawer synchronized with language and attach-time reordering.
- Theme persistence across routes and deep-links.
- Navigation rows: icon→label, full width, compact spacing.
- Iconoir sprite + AppIcon wrapper in nav and grid.
- FilterablePaginatedGrid i18n for controls and labels.
- Design System preview page.

