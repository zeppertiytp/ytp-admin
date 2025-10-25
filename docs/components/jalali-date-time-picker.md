# JalaliDateTimePicker

`JalaliDateTimePicker` provides Jalali (Persian) calendar support for Vaadin
forms with optional time selection. It keeps client-side conversions in sync
with ISO-8601 values returned to the server.

- **Source:** `components/src/main/java/com/youtopin/vaadin/component/JalaliDateTimePicker.java`
- **Frontend module:** `components/src/main/resources/META-INF/resources/frontend/components/jalali-date-time-picker.ts`
- **NPM dependency:** `jalaali-js`

## Key capabilities
- Switchable modes via `setPickerVariant(PickerVariant.DATE)` or
  `PickerVariant.DATE_TIME`.
- ISO min/max bounds plus dedicated Jalali year caps through
  `setMinYear`/`setMaxYear` for dropdown-only navigation.
- Locale-aware month and weekday labels (Farsi and transliterated English).
- Optional custom open button label (`setOpenButtonLabel`) exposed to assistive
  technologies.
- Value clamping to enforce min/max ranges even when the client attempts to
  input an out-of-range Jalali date.

## Usage
```java
JalaliDateTimePicker picker = new JalaliDateTimePicker();
picker.setLabel(getTranslation("form.contract.start"));
picker.setPickerVariant(JalaliDateTimePicker.PickerVariant.DATE);
picker.setMinYear(1398);
picker.setMaxYear(1410);
picker.addValueChangeListener(event -> {
    LocalDateTime start = event.getValue();
    if (start != null) {
        service.schedule(start);
    }
});
```

## Accessibility & localisation
- Implements `LocaleChangeObserver` to refresh translated month/weekday labels
  when the UI locale changes.
- Mirrors Vaadin's form field interfaces (`HasHelper`, `HasValidation`,
  `Focusable`) so required indicators, error messages, and helper texts follow
  existing patterns.
- `aria-expanded`, `aria-haspopup`, and keyboard interaction behaviours are
  handled in the TypeScript module.

## Sample references
- Generated forms: Jalali date and date-time fields appear in
  `samples/src/main/resources/forms/user_form.json` and
  `samples/src/main/resources/forms/user_form_with_layout.json`.
- Runtime showcase: `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/FormGenerationView.java` renders both Jalali
  field types through `GeneratedForm`.

