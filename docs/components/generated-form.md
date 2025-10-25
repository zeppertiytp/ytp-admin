# GeneratedForm

`GeneratedForm` reads a JSON specification and renders a responsive Vaadin form
at runtime. It complements the annotation-driven form engine by consuming the
exported JSON emitted by the engine pipeline.

- **Source:** `components/src/main/java/com/youtopin/vaadin/component/GeneratedForm.java`
- **Related engine docs:**
  - `docs/form-engine-reference.md`
  - `docs/form-generation.md`
- **Sample JSON:** `samples/src/main/resources/forms/`

## Key capabilities
- Section and group rendering with configurable column counts using
  `FormLayout` responsive steps.
- Field factories for text, numbers, dates, Jalali pickers, uploads, radio
  groups, and combo boxes, including helper texts and placeholder handling.
- Binder-free state management: component values are stored in an internal map
  keyed by field name while validation delegates to
  `FormValidationService` implementations.
- Action bars with left/right clusters and per-action metadata (theme variant,
  validation strategy, success messages, navigation hints).
- Locale-aware labels, helper texts, and section headers updated on
  `LocaleChangeEvent`.
- Submission listeners triggered after successful validation that expose the
  action id, immutable value snapshot, and original JSON nodes.
- Programmatic submission via `submit(String actionId)` to drive flows from
  external buttons.

## Usage
```java
FormValidationService validationService = request -> ValidationResult.ok();
GeneratedForm onboarding = new GeneratedForm("user_form.json", validationService);
onboarding.addSubmissionListener(event -> {
    if ("submit".equals(event.getActionId())) {
        notificationService.success(getTranslation("form.saved"));
    }
});
```

Place JSON specifications under `src/main/resources/forms/`. Each file should
include:
- `id`, `title`, and `layout` (sections → fields)
- `submit` metadata describing button groups and action identifiers
- Field-level validation and state expressions emitted by the form engine

## Integration with the annotation form engine
1. Annotate domain classes using `@UiForm`, `@UiSection`, and `@UiField`.
2. Export JSON using the engine's serialization utilities (see
   `docs/form-generation.md`).
3. Feed the JSON into `GeneratedForm` for runtime rendering.
4. Handle `FormSubmissionEvent`s to trigger service calls and custom
   notifications.

## Accessibility & localisation
- Field components reuse Vaadin's accessible counterparts. Provide
  descriptive translation keys for labels, helper texts, and validation errors.
- RTL support is automatic when the surrounding UI direction is `rtl`; Jalali
  fields use the dedicated picker implementation.
- Upload components expose `aria-label`s derived from translation entries.

## Sample references
- Runtime demo view:
  `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/FormGenerationView.java`
- JSON specs: `samples/src/main/resources/forms/user_form.json` and
  `samples/src/main/resources/forms/user_form_with_layout.json`
- Validation stub: `samples/src/main/java/com/youtopin/vaadin/samples/infrastructure/form/InMemoryFormValidationService.java`

