# ADR-20250920: Jalali picker overlay and date-only mode

## Status
Accepted

## Context
The dynamic form generator recently gained a custom `JalaliDateTimePicker`. The
initial implementation rendered the entire calendar/time surface inline inside
forms, which crowded layouts and made it hard to reuse the component outside the
`GeneratedForm`. It also lacked a way to present a date-only Jalali picker while
retaining the same JSON-driven configuration model.

## Decision
- Switch the Jalali picker UI to an overlay that is opened from a trigger
  button. The selected value remains visible next to the button for quick
  review.
- Extend the Java and TypeScript implementations with a `PickerVariant` so the
  picker can run in either `DATE_TIME` or `DATE` mode. Generated forms can choose
  between them via the field `type` (`jalaliDate` or `jalaliDateTime`) or the
  optional `showTime`/`pickerVariant` hints.
- Add an `openLabel` option so JSON specifications (and standalone usages) can
  translate the trigger button caption while still defaulting to sensible
  built-in messages.
- Expose programmatic `openPicker` / `closePicker` helpers and keep minute-step
  validation for the time-aware mode.

## Consequences
- Forms no longer stretch vertically when Jalali fields are present and the
  picker can be embedded anywhere in the UI with consistent behaviour.
- Documentation, sample JSON and translations were updated to describe the new
  options and default captions.
- The component now maintains a small amount of overlay state. We added document
  listeners to close the popup when the user clicks outside or presses Escape.
