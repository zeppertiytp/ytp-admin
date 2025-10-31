# Dynamically shape wizard repeatable groups

Multi-step wizards can change their subsequent steps at runtime by sharing a
single context object across every form and re-registering the steps whenever a
prerequisite changes. This guide explains the pattern in detail and walks
through the new `/forms/dynamic-repeatable` sample where the first step designs
the fields that appear inside a repeatable editor rendered in the second step.

## 1. Keep mutable wizard state in a shared context

`WizardFormFlowCoordinator` keeps a single context bean for all steps. Store the
schema definition and the repeatable entries inside that bean so every form sees
the latest values.

```java
public final class DynamicRepeatableWizardState implements Serializable {

    public static final String STEP_SCHEMA = "dynamic-schema";
    public static final String STEP_ENTRIES = "dynamic-entries";

    private final DynamicFieldSchemaFormData schema = new DynamicFieldSchemaFormData();
    private final DynamicEntryCollectionFormData entries = new DynamicEntryCollectionFormData();

    public DynamicRepeatableWizardState() {
        syncEntriesWithSchema();
    }

    public void syncEntriesWithSchema() {
        entries.getSchema().copyFrom(schema);
        entries.ensureEntryCount(schema.getEntryCount());
    }
}
```

The helper method synchronises the repeatable collection with the latest schema
whenever step one changes its configuration.【F:samples/src/main/java/com/youtopin/vaadin/samples/application/dynamicrepeatable/DynamicRepeatableWizardState.java†L13-L51】

## 2. Re-render or refresh dependent forms when prerequisites change

When the schema step is submitted, copy the new configuration into the entries
bean, adjust the repeatable item count, and mark the step as completed before
navigating forward.

```java
form.addActionHandler("dynamic-schema-next", context -> {
    wizardState.syncEntriesWithSchema();
    entryForm.initializeWithBean(wizardState.getEntries());
    entryForm.setRepeatableEntryCount("dynamic-entry-group", wizardState.getSchema().getEntryCount());
    wizardState.markStepCompleted(DynamicRepeatableWizardState.STEP_SCHEMA);
    coordinator.markStepCompleted(DynamicRepeatableWizardState.STEP_SCHEMA);
    coordinator.nextStep(DynamicRepeatableWizardState.STEP_SCHEMA)
            .ifPresent(coordinator::setCurrentStepId);
});
```

`RenderedForm#initializeWithBean` rebinds the repeatable editor with the updated
entries so the UI reflects the new schema immediately.【F:samples/src/main/java/com/youtopin/vaadin/samples/ui/view/DynamicRepeatableWizardView.java†L156-L166】

## 3. Drive field visibility from schema flags

The second step exposes every possible field but hides optional ones with
`visibleWhen` rules that point at the schema snapshot stored in the same bean.

```java
@UiField(path = "entries.email", component = UiField.ComponentType.EMAIL,
        labelKey = "dynamicwizard.entries.field.email",
        visibleWhen = "{\"field\":\"schema.includeEmail\",\"value\":true}")
public void email() {
}
```

Because the schema booleans are read-only fields in the same form, the
visibility watchers update automatically whenever the schema flags change.【F:samples/src/main/java/com/youtopin/vaadin/samples/ui/formengine/definition/dynamic/DynamicEntryCollectionFormDefinition.java†L77-L120】

## 4. Validate repeatable rows against the active schema

Optional columns become mandatory only when the corresponding schema flag is
set. The sample keeps the server-side validation in the finish handler so the
rules stay in one place.

```java
List<String> errors = validateEntries(data);
if (!errors.isEmpty()) {
    Notification notification = Notification.show(errors.get(0));
    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    notification.setDuration(4000);
    return;
}
```

The helper checks each entry against the current schema and presents the first
error with a translated index label.【F:samples/src/main/java/com/youtopin/vaadin/samples/ui/view/DynamicRepeatableWizardView.java†L184-L214】

## 5. Explore the `/forms/dynamic-repeatable` sample

Run `mvn -pl samples spring-boot:run` and open
`http://localhost:8080/forms/dynamic-repeatable` to experiment. Step one lets you
choose which optional fields the repeatable editor should display and how many
rows to create. Submitting the first step rebuilds the second step in-place so
you can immediately fill the generated rows. The notification on completion
confirms the schema-aware validation succeeded.【F:samples/src/main/java/com/youtopin/vaadin/samples/ui/view/DynamicRepeatableWizardView.java†L37-L215】

Use this pattern whenever a later wizard step depends on earlier answers: keep
shared state in the coordinator context, refresh the affected forms when
upstream data changes, and express conditional visibility or validation through
that shared state.
