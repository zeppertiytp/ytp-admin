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

Use a horizontal layout for compact inline groups such as first/last-name pairs or filter toolbars, and a vertical layout when you need stacked cards but still want spacing control between field groups.

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
| `jalaliDate`     | Jalali calendar date picker (`JalaliDateTimePicker` in date mode). Opens in an overlay via a button, honours optional `min`/`max` ISO values and an `openLabel`, and returns a `java.time.LocalDateTime` at midnight. |
| `jalaliDateTime` | Jalali calendar date & time picker (`JalaliDateTimePicker`). Opens via the same overlay button, supports optional `min`/`max` ISO values, `minuteStep`, and `openLabel`, and can be switched to date-only mode with `showTime: false` or `pickerVariant: "date"`. Returns a `java.time.LocalDateTime`. |
| `file`        | Single file upload (`Upload`).  The uploaded file name is stored; extend if you need to persist content. |
| `multiFile`   | Multiple file upload (`Upload`).  Stores a list of uploaded file names. |
| `radio`       | Single selection radio group (`RadioButtonGroup`).  `options` must be provided. |

Additional types (e.g. `time`, `dateTime`, `textArea`) can be added by extending `GeneratedForm.createField()`.

The Jalali pickers render a lightweight value preview next to a button. Clicking the button opens an overlay that contains the calendar (and optionally time controls). Common configuration keys:

- `min` / `max`: ISO 8601 strings (e.g. `"2024-01-01T00:00:00"`) that constrain selectable dates.
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
   - The service returns a map of field names to error keys (translation keys).  The form displays these messages next to the corresponding fields and prevents submission.

## Internationalisation

The form generator is locale‑aware:

- Section and field labels are selected from translation objects based on `UI.getCurrent().getLocale()`.  If the current language isn’t found, English is used as a fallback.
- Labels and option captions update automatically when the user switches language at runtime.  Section titles are registered with the locale change mechanism so they refresh as well.
- Error messages and button captions (`form.required`, `form.correctErrors`, `form.success`, `form.submit`) come from the application’s `messages.properties` files.

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
  "submit": {"endpoint":"/api/users","method":"POST","backendValidation":true}
}
```

This form includes basic text/email fields, a select drop‑down, a boolean switch controlling the visibility of a dependent field, and examples of richer inputs such as the standard date picker, the Jalali date-time picker, file uploads (single and multi) and radio buttons.

For layout-specific behaviour, review `user_form_with_layout.json`.  It keeps the first section on the responsive grid, switches the second section to a wrapped horizontal layout for inline fields, and renders the final section in a stacked vertical container with custom spacing and width.

## Conclusion

The dynamic form generator allows you to design complex data entry screens declaratively in JSON.  By combining localisation, validation, conditional logic and a growing set of input types, you can rapidly build consistent forms across your application.  See the code in `GeneratedForm.java` for further extension points.
