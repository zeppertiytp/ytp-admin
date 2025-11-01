# Dynamic property bag repeatable form

This guide introduces the `/forms/dynamic-bag` sample that showcases how to collect repeatable
entries without defining strongly typed beans for every field. Instead, both the repeatable rows and
the nested groups inside each row delegate property access to `DynamicPropertyBag` implementations.

## Key ideas

1. **Map-backed row model** – `DynamicBagEntry` implements `DynamicPropertyBag` and exposes three
   nested maps (`profile`, `address`, `channels`). Each nested map is another `DynamicPropertyBag`
   (`DynamicBagValues`) so any field inside the repeatable hierarchy is stored dynamically.
2. **Minimal container bean** – The form binds to `DynamicBagFormData`, which only owns a
   `List<DynamicBagEntry>`. Adding or removing fields in the form definition does not require any
   bean changes as long as the path stays within these bags.
3. **Inline JSON preview** – When the submit action runs, the view serialises the repeatable values
   by calling `DynamicBagFormData.toSerializableEntries()` and prints the JSON payload below the form.

## Try it out

1. Start the sample application and open `http://localhost:8080/forms/dynamic-bag`.
2. Add or duplicate contacts, then submit the form. The payload printed below the form matches the
   `DynamicPropertyBag` maps without needing new Java properties.

For a deeper walkthrough of dynamic repeatables, combine this sample with the wizard documented in
[`docs/how-to-dynamic-repeatable-wizard.md`](./how-to-dynamic-repeatable-wizard.md).
