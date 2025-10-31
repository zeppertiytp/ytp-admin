# Dynamically shape wizard repeatable groups

Multi-step wizards can change their subsequent steps at runtime by sharing a
single context object across every form and re-registering the steps whenever a
prerequisite changes. This guide explains the pattern in detail and walks
through the updated `/forms/dynamic-repeatable` sample where the first step
designs an arbitrary schema and the next step renders a repeatable editor from
that design.

## 1. Keep mutable wizard state in a shared context

`WizardFormFlowCoordinator` keeps one context bean for all steps. Store the
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
        entries.syncWithSchema(schema);
    }
}
```

`DynamicFieldSchemaFormData` stores the repeatable metadata and a list of
`DynamicFieldBlueprint` objects describing each dynamic field (key, label, type,
helper text, placeholder, required flag). The collection form keeps a schema
snapshot plus a list of `DynamicEntryRow` instances backed by `DynamicFieldValues`,
which exposes a `Map<String,Object>` of row values. Whenever step one changes
the blueprint, `syncWithSchema` copies the schema, normalises keys, adjusts the
repeatable row count, and ensures every row exposes the new field set.【F:samples/src/main/java/com/youtopin/vaadin/samples/application/dynamicrepeatable/DynamicRepeatableWizardState.java†L13-L63】【F:samples/src/main/java/com/youtopin/vaadin/samples/application/dynamicrepeatable/model/DynamicEntryCollectionFormData.java†L13-L52】

## 2. Build form definitions dynamically

The schema step still relies on annotations, but the entry step now assembles a
`FormDefinition` at runtime. `DynamicEntryFormFactory` turns the blueprint list
into repeatable field definitions, sets the summary template, and registers the
same action ids as the static form used before. `FormEngine` exposes a new
`render(FormDefinition, …)` overload so runtime definitions go through the same
rendering pipeline as annotated classes.【F:samples/src/main/java/com/youtopin/vaadin/samples/ui/formengine/definition/dynamic/DynamicEntryFormFactory.java†L18-L109】【F:components/src/main/java/com/youtopin/vaadin/formengine/FormEngine.java†L70-L170】

The view clears and re-registers the entry form whenever the schema changes. On
submit, it sanitises field keys, syncs the entry collection, rebuilds the form,
and jumps to the next step.

```java
form.addActionHandler("dynamic-schema-next", context -> {
    wizardState.getSchema().sanitiseFieldKeys();
    wizardState.syncEntriesWithSchema();
    rebuildEntryForm();
    coordinator.markStepCompleted(DynamicRepeatableWizardState.STEP_SCHEMA);
    coordinator.nextStep(DynamicRepeatableWizardState.STEP_SCHEMA)
            .ifPresent(coordinator::setCurrentStepId);
});
```

`rebuildEntryForm` renders the runtime definition and registers it with the
coordinator so the wizard always reflects the latest schema.【F:samples/src/main/java/com/youtopin/vaadin/samples/ui/view/DynamicRepeatableWizardView.java†L125-L210】

## 3. Bind dynamic field names with `DynamicPropertyBag`

The form engine’s binder normally uses JavaBean getters and setters. To support
fields whose names are only known at runtime, the binder now honours the
`DynamicPropertyBag` interface. `DynamicFieldValues` implements this interface
and delegates reads/writes to an internal `Map<String,Object>`, letting the
repeatable entries expose any schema-defined key through the standard
data-binding mechanism.【F:components/src/main/java/com/youtopin/vaadin/formengine/binder/DynamicPropertyBag.java†L1-L33】【F:samples/src/main/java/com/youtopin/vaadin/samples/application/dynamicrepeatable/model/DynamicFieldValues.java†L1-L63】

Every `DynamicEntryRow` simply returns a `DynamicFieldValues` instance, and the
form definition uses paths like `entries.values.locationCode`. The binder falls
back to `DynamicPropertyBag` when it cannot locate a JavaBean accessor, keeping
the orchestration code unchanged.【F:samples/src/main/java/com/youtopin/vaadin/samples/application/dynamicrepeatable/model/DynamicEntryRow.java†L1-L22】【F:components/src/main/java/com/youtopin/vaadin/formengine/binder/BinderOrchestrator.java†L124-L178】

## 4. Validate repeatable rows against the runtime schema

The finish handler iterates over the active blueprints and checks each row.
Required fields surface a translated error message that points to the offending
row and field label.

```java
for (DynamicFieldBlueprint blueprint : fields) {
    if (!blueprint.isRequired()) {
        continue;
    }
    Object value = row.getValues().get(blueprint.getFieldKey());
    if (value == null || (value instanceof String str && str.isBlank())) {
        String message = getTranslation("dynamicwizard.validation.requiredField",
                indexLabel, fieldLabel, requiredMessage);
        return List.of(message);
    }
}
```

Because the blueprint carries the friendly label, the notification can point to
the exact field the user defined in step one.【F:samples/src/main/java/com/youtopin/vaadin/samples/ui/view/DynamicRepeatableWizardView.java†L200-L226】

## 5. Explore the `/forms/dynamic-repeatable` sample

Run `mvn -pl samples spring-boot:run` and open
`http://localhost:8080/forms/dynamic-repeatable` to experiment. Step one lets
you add, remove, and reorder field blueprints while choosing the input type,
helper text, placeholder, and required flag for each field. Submitting the first
step rebuilds the second step in-place so you can immediately fill the generated
rows. The notification on completion confirms the schema-aware validation
succeeded.【F:samples/src/main/java/com/youtopin/vaadin/samples/ui/view/DynamicRepeatableWizardView.java†L37-L226】

Use this pattern whenever a later wizard step depends on earlier answers: keep
shared state in the coordinator context, generate `FormDefinition` instances from
the current schema, and expose dynamic row values through `DynamicPropertyBag`
implementations so the binder can read and write fields that only exist at
runtime.
