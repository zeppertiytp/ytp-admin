# How-to guides

Practical workflows for extending the Vaadin admin platform.

## Add a new reusable component
1. Implement the server-side class under
   `components/src/main/java/com/youtopin/vaadin/component/`.
2. Create supporting frontend resources under
   `components/src/main/resources/META-INF/resources/frontend/components/`.
3. Write component documentation in `docs/components/` and update the catalogue
   table.
4. Expose a sample under `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/components/`
   with an `@Route` prefixed by `components/`.
5. Update navigation metadata in `samples/src/main/resources/menu/navigation-menu.json`
   and translations in `samples/src/main/resources/messages*.properties`.

## Extend the form engine
1. Declare annotations in `components/src/main/java/com/youtopin/vaadin/formengine/annotation/`.
2. Register factories in `components/src/main/java/com/youtopin/vaadin/formengine/registry/`.
3. Document new metadata in `docs/form-engine-reference.md` and update
   `docs/components/generated-form.md` with usage notes.
4. Add regression samples via JSON specs in `samples/src/main/resources/forms/`
   and link them from `FormGenerationView`.

## Publish a new sample workflow
1. Add a view class under `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/`.
2. Annotate with `@Route` and inherit from `AppPageLayout` to get the standard
   responsive shell.
3. Register navigation entries and translations.
4. Document the scenario in `samples/README.md` and reference any relevant
   components in `docs/components/`.
5. Update `README.md` if the workflow introduces new tooling or scripts.

## Coordinate wizard-backed multi-step forms
1. Follow the dedicated guide in [`docs/how-to-wizard-form-flow-coordinator.md`](how-to-wizard-form-flow-coordinator.md)
   to connect `HorizontalWizard` with Form Engine forms.
2. Use `WizardFormFlowCoordinator` callbacks to persist shared context such as a generated
   entity ID before navigating to the next step.
3. Reference the `/forms/wizard-coordinator` sample for a complete example that issues an ID in
   the first step and reuses it later in the flow.
4. For schema-driven repeatable groups, read
   [`docs/how-to-dynamic-repeatable-wizard.md`](how-to-dynamic-repeatable-wizard.md) and explore the
   `/forms/dynamic-repeatable` sample.

