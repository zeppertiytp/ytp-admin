# Form Generation Guide

This document describes how to configure and extend the dynamic form generator provided in this project.  The `GeneratedForm` component reads a JSON specification at runtime and builds a Vaadin Flow form with sections, fields, validation rules, conditional visibility and localisation.

## JSON Specification Structure

A form definition is a JSON object with the following top‑level fields:

- `id`: A unique identifier for the form.  Used when storing saved views or calling backend validation.
- `title`: An object whose keys are language codes (e.g. `"fa"`, `"en"`) and whose values are the translated title strings.
- `layout`: An array of section definitions.  Each element must have:
  - `type`: The literal string `"section"`.
  - `title`: An object with language keys and translated section titles.
  - `fields`: An array of field definitions.
  - `columns` (optional): Number of columns to display when the section uses the default responsive form grid.
  - `layout` (optional): An object that overrides the container type and flex properties for the section.  See [Section layout overrides](#section-layout-overrides).

### Field Definition

Each entry in the `fields` array describes a single input component.  Common properties:

| Property       | Type                            | Description |
|---------------:|---------------------------------|------------|
| `name`         | string                          | Unique identifier for the field.  Used as the key when submitting data. |
| `label`        | object or string                | The field label.  When an object, keys are locale codes and values are translations.  When a string, the same text is used for all locales. |
| `type`         | string                          | Determines the input component.  Supported types are listed below. |
| `required`     | boolean (optional)              | Whether the field is mandatory.  A required indicator is shown and client‑side validation prevents submission when empty. |
| `validators`   | array (optional)                | Additional client‑side validators.  Currently only `pattern` is supported. |
| `options`      | array (for `select`/`radio`)    | A list of choices.  Each entry has a `value` (submitted value) and a `label` (translations). |
| `visibleWhen`  | object (optional)               | Defines conditional visibility.  Use `"all"` or `"any"` with an array of conditions.  Each condition has `field`, `op`, and `value`.  Currently `op` supports `EQ` (equals). |
| `colSpan`      | number or string (optional)     | How many columns the field should span inside the responsive `FormLayout`. Values greater than one stretch the component across multiple grid columns. |

### Section layout overrides

Sections render in a responsive `FormLayout` by default and honour the numeric `columns` value.  When you need more control—such as stacking fields vertically with custom spacing or arranging them in a wrapped horizontal row—add a `layout` object alongside the section definition.

| Property | Type | Description |
| --- | --- | --- |
| `type` | string | Container choice. Supported values are `"form"` (default, respects `columns`), `"horizontal"` (uses `HorizontalLayout`), and `"vertical"` (uses `VerticalLayout`). |
| `align` | string | Maps to `setAlignItems` (`start`, `center`, `end`, `stretch`, `baseline`). |
| `justify` | string | Maps to `setJustifyContentMode` (`start`, `center`, `end`, `between`, `around`, `evenly`). |
| `spacing` | string/number | Controls the CSS gap between field components (for numbers the generator appends `px`). |
| `width` | string | Optional explicit width for the section container (`"auto"`, `"100%"`, `"320px"`, etc.). |
| `wrap` | boolean | Only for `type: "horizontal"`. When `true`, enables wrapping inside the horizontal layout. |
| `classNames` | array | Extra CSS class names applied to the section container. |
| `responsiveSteps` | array | Only for `type: "form"`. Overrides the generated Vaadin `FormLayout.ResponsiveStep` list. Each entry accepts `minWidth` (or `width`), `columns`, and optional `labelsPosition` (`top`/`aside`). |

When no custom steps are supplied, the generator creates sensible defaults that flow from a single column on narrow screens to the declared `columns` value once the viewport reaches `1200px`, at which point labels shift `aside` to match Vaadin’s best practice guidance.

Supplying custom responsive steps gives fine-grained control. For example, the sample form defines two breakpoints to move from stacked labels to an aside layout:

```json
"layout": {
  "responsiveSteps": [
    { "minWidth": "0", "columns": 1, "labelsPosition": "top" },
    { "minWidth": "640px", "columns": 2, "labelsPosition": "aside" }
  ]
}
```

> **Note:** `columns` still only applies when `layout.type` is `"form"` (or omitted). Horizontal and vertical layouts ignore the column count but pick up other flex settings.

Example (excerpt from `user_form_with_layout.json`):

```json
{
  "type": "section",
  "title": { "fa": "نمایش در یک ردیف", "en": "Inline layout" },
  "layout": {
    "type": "horizontal",
    "align": "end",
    "justify": "between",
    "spacing": "var(--lumo-space-m)",
    "wrap": true,
    "classNames": ["inline-section"]
  },
  "fields": [
    { "name": "firstName", "label": { "fa": "نام", "en": "First name" }, "type": "text", "required": true },
    { "name": "lastName", "label": { "fa": "نام خانوادگی", "en": "Last name" }, "type": "text", "required": true },
    { "name": "newsletter", "label": { "fa": "خبرنامه", "en": "Newsletter" }, "type": "switch" }
  ]
}
```

Use a horizontal layout for compact inline groups such as first/last-name pairs or filter toolbars, and a vertical layout when you need stacked cards but still want spacing control between field groups. When sticking to the default `form` layout, use `colSpan` to stretch wider inputs (e.g. biography text areas) across multiple columns while preserving responsive behaviour.

### Supported Field Types

| Type          | Component & Notes |
|---------------|------------------|
| `text`        | Simple text input (`TextField`). |
| `email`       | Email input (`EmailField`) with email pattern applied by default. |
| `tel`         | Telephone input (`TextField`).  Pattern validators can enforce formats (e.g. Iranian phone numbers). |
| `number`      | Numeric input (`NumberField`). |
| `select`      | Drop‑down selection (`ComboBox`).  `options` must be provided. |
| `switch`      | Boolean input (`Checkbox`) with on/off state. |
| `date`        | Date picker (`DatePicker`) returning a `java.time.LocalDate`. |
| `jalaliDate`     | Jalali calendar date picker (`JalaliDateTimePicker` in date mode). Opens in an overlay via a button, honours optional `min`/`max` ISO values, `minYear`/`maxYear` Jalali bounds, and an `openLabel`, and returns a `java.time.LocalDateTime` at midnight. |
| `jalaliDateTime` | Jalali calendar date & time picker (`JalaliDateTimePicker`). Opens via the same overlay button, supports optional `min`/`max` ISO values, `minYear`/`maxYear`, `minuteStep`, and `openLabel`, and can be switched to date-only mode with `showTime: false` or `pickerVariant: "date"`. Returns a `java.time.LocalDateTime`. |
| `file`        | Single file upload (`Upload`).  The uploaded file name is stored; extend if you need to persist content. |
| `multiFile`   | Multiple file upload (`Upload`).  Stores a list of uploaded file names. |
| `radio`       | Single selection radio group (`RadioButtonGroup`).  `options` must be provided. |

Additional types (e.g. `time`, `dateTime`, `textArea`) can be added by extending `GeneratedForm.createField()`.

The Jalali pickers render a lightweight value preview next to a button. Clicking the button opens an overlay that contains the calendar (and optionally time controls). Common configuration keys:

- `min` / `max`: ISO 8601 strings (e.g. `"2024-01-01T00:00:00"`) that constrain selectable dates.
- `minYear` / `maxYear`: Jalali year numbers that trim the year dropdown even when full ISO bounds are unnecessary.
- `minuteStep`: numeric value that limits the time selector granularity when the time panel is visible.
- `openLabel`: string or translation object that replaces the default button caption.
- `showTime` / `pickerVariant`: set `showTime` to `false` or `pickerVariant` to `"date"` to hide the time controls even when using the `jalaliDateTime` type.

Both variants deliver values to the backend as `java.time.LocalDateTime` instances.

### Conditional Visibility

To show or hide a field based on other field values, use the `visibleWhen` property.  It accepts either `all` or `any` arrays of conditions.  Example:

```json
{
  "name": "phone",
  "type": "tel",
  "label": {"en": "Phone", "fa": "موبایل"},
  "visibleWhen": {
    "all": [
      {"field": "twoFactor", "op": "EQ", "value": true}
    ]
  },
  "validators": [
    {"type": "pattern", "value": "^09\\d{9}$", "message": {"en": "Invalid phone", "fa": "موبایل نامعتبر"}}
  ]
}
```

In this example the phone field only appears when the `twoFactor` checkbox is checked.

## Validation

The generated form performs validation in two phases:

1. **Client‑side checks:**
   - Required fields must be filled before submission.
   - Pattern validators defined in the JSON are applied on text, email or number fields.  Each validator has a `value` (regular expression) and a `message` with locale keys.
   - When invalid, fields display the translated error message.

2. **Backend validation:**
   - After client checks pass, the form collects all field values into a map and calls the injected `FormValidationService`.
   - Override `FormValidationService#validate(String, Map, String)` to inspect which action triggered the submission.  The default implementation delegates to the historical two-parameter method for backwards compatibility.
   - The service returns a map of field names to error keys (translation keys).  The form displays these messages next to the corresponding fields and prevents submission.

## Internationalisation

The form generator is locale‑aware:

- Section and field labels are selected from translation objects based on `UI.getCurrent().getLocale()`.  If the current language isn’t found, English is used as a fallback.
- Labels and option captions update automatically when the user switches language at runtime.  Section titles are registered with the locale change mechanism so they refresh as well.
- Error messages and default button captions (`form.required`, `form.correctErrors`, `form.success`, `form.submit`) come from the application’s `messages.properties` files.  Individual actions can override these captions directly in JSON if needed.

## Submission actions & listeners

The root-level `submit` object controls which buttons appear and how they behave.  When omitted, the component renders a single primary action labelled with the translated `form.submit` caption and runs both client and backend validation.

### Action configuration

Declare an `actions` array under `submit` to render multiple buttons:

| Property | Type | Description |
| --- | --- | --- |
| `id` | string | Required. Unique identifier used to distinguish the action in listeners and backend validation. |
| `label` | object or string | Optional. Localised captions for the button. When omitted, `labelKey` or the `id` is used. |
| `labelKey` | string | Optional translation key resolved via `Component#getTranslation`. Useful for reusing existing i18n keys. |
| `theme` / `themeVariants` | string or array | Optional Vaadin Button theme variants (`primary`, `success`, `tertiary`, `tertiary-inline`, `icon`, `contrast`, `error`, `danger`). The first action defaults to `primary` if nothing is supplied. |
| `validate` / `clientValidation` | boolean | Defaults to `true`. When `false`, skips client-side validation (useful for “Save draft” buttons). |
| `backendValidation` | boolean | Overrides the root `submit.backendValidation` flag for the specific action. Defaults to the root value or `true` if unspecified. |
| `align` | string | Optional button placement. Accepts `left`/`right` (or `start`/`end`). Defaults to `right`. Left-aligned actions render on the opposite side of the action bar, useful for secondary flows like “Cancel” or “Back”. |
| `successMessage` | object or string | Optional custom success message (string or i18n map). Falls back to `form.success`. Exposed via `FormSubmissionEvent#getSuccessMessage()` so the host view can display or ignore it. |
| `successMessageKey` | string | Translation key resolved at runtime; ignored when `successMessage` is provided. |

Buttons marked with `align: "left"` are rendered on the opposite side of the footer bar so you can keep primary actions on the
right while exposing secondary flows (e.g. “Cancel”, “Back”) on the left.

### Handling submissions programmatically

Every successful submission raises a `FormSubmissionEvent`.  Register listeners from Java code to react differently per action:

```java
form.addSubmissionListener(event -> {
    if ("submit-exit".equals(event.getActionId())) {
        navigateAway();
    }
});
```

Listeners receive the immutable value map, the raw JSON node describing the action, the overall `submit` configuration and the button instance.  The form no longer shows success notifications automatically; instead, use the provided metadata to react appropriately:

```java
form.addSubmissionListener(event -> {
    event.getSuccessMessage().ifPresent(message ->
        Notification.show(message, 3000, Notification.Position.BOTTOM_CENTER));
    routeToNextStep(event.getActionId(), event.getValues());
});
```

To trigger a submission manually (for example, from a keyboard shortcut), call `form.submit("submit")` with the desired action identifier.

## Extending the Specification

You can add new types or custom properties by extending `GeneratedForm.createField()`.  For instance, to support a time picker, import `TimePicker` and add a new case:

```java
case "time" -> {
    TimePicker tp = new TimePicker(label);
    // configure tp ...
    comp = tp;
}
```

Likewise, more comparison operators (`GT`, `LT`, etc.) can be added to the `setupVisibilityWatcher()` method to support richer conditions.

## Example

The sample form `user_form.json` demonstrates many of the features:

```json
{
  "id": "user-form-v1",
  "title": {"fa":"کاربر جدید","en":"New User"},
  "layout": [
    {
      "type": "section",
      "title": {"fa":"اطلاعات اصلی","en":"Main"},
      "columns": 2,
      "fields": [
        {"name":"firstName","label":{"fa":"نام","en":"First name"},"type":"text","required":true},
        {"name":"lastName","label":{"fa":"نام خانوادگی","en":"Last name"},"type":"text","required":true},
        {"name":"email","label":{"fa":"ایمیل","en":"Email"},"type":"email","validators":[{"type":"pattern","value":"^.+@.+\\..+$","message":{"fa":"ایمیل نامعتبر","en":"Invalid email"}}],"required":true},
        {"name":"role","label":{"fa":"نقش","en":"Role"},"type":"select","options":[{"value":"ADMIN","label":{"fa":"مدیر","en":"Admin"}},{"value":"EDITOR","label":{"fa":"ویرایشگر","en":"Editor"}}],"required":true}
      ]
    },
    {
      "type":"section",
      "title":{"fa":"تنظیمات","en":"Settings"},
      "columns":2,
      "fields":[
        {"name":"twoFactor","label":{"fa":"دو مرحله‌ای","en":"Two‑factor"},"type":"switch"},
        {"name":"phone","label":{"fa":"موبایل","en":"Phone"},"type":"tel","visibleWhen":{"all":[{"field":"twoFactor","op":"EQ","value":true}]},"validators":[{"type":"pattern","value":"^09\\d{9}$","message":{"fa":"موبایل نامعتبر","en":"Invalid phone"}}]}
      ]
    },
    {
      "type":"section",
      "title":{"fa":"اطلاعات بیشتر","en":"Additional"},
      "columns":2,
      "fields":[
        {"name":"dob","label":{"fa":"تاریخ تولد","en":"Date of Birth"},"type":"date"},
        {"name":"lastLogin","label":{"fa":"آخرین ورود","en":"Last login"},"type":"jalaliDateTime","minuteStep":5,"min":"2020-01-01T00:00:00","openLabel":{"fa":"انتخاب تاریخ و زمان","en":"Pick last login"}},
        {"name":"interview","label":{"fa":"تاریخ مصاحبه","en":"Interview date"},"type":"jalaliDate","openLabel":{"fa":"انتخاب تاریخ","en":"Pick interview date"},"max":"2025-12-31T00:00:00"},
        {"name":"resume","label":{"fa":"رزومه","en":"Resume"},"type":"file"},
        {"name":"attachments","label":{"fa":"فایل‌ها","en":"Attachments"},"type":"multiFile"},
        {"name":"gender","label":{"fa":"جنسیت","en":"Gender"},"type":"radio","options":[{"value":"M","label":{"fa":"مرد","en":"Male"}},{"value":"F","label":{"fa":"زن","en":"Female"}},{"value":"O","label":{"fa":"دیگر","en":"Other"}}]}
      ]
    }
  ],
  "submit": {
    "endpoint":"/api/users",
    "method":"POST",
    "backendValidation":true,
    "actions": [
      {"id":"submit","labelKey":"form.submit"},
      {"id":"submit-exit","label":{"fa":"ثبت و خروج","en":"Submit and Exit"},"validate":false,"backendValidation":false,
       "successMessage":{"fa":"پیش‌نویس ذخیره شد","en":"Draft saved"}}
    ]
  }
}
```

This form includes basic text/email fields, a select drop‑down, a boolean switch controlling the visibility of a dependent field, and examples of richer inputs such as the standard date picker, the Jalali date-time picker, file uploads (single and multi) and radio buttons.

The layout-focused sample `user_form_with_layout.json` combines responsive grid overrides with the multi-action configuration shown above so you can offer “Submit” and “Submit & Exit” flows side by side.

For layout-specific behaviour, review `user_form_with_layout.json`.  It keeps the first section on the responsive grid, switches the second section to a wrapped horizontal layout for inline fields, and renders the final section in a stacked vertical container with custom spacing and width.

## Conclusion

The dynamic form generator allows you to design complex data entry screens declaratively in JSON.  By combining localisation, validation, conditional logic and a growing set of input types, you can rapidly build consistent forms across your application.  See the code in `GeneratedForm.java` for further extension points.
