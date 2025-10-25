# Annotation Form Engine Reference

The `form-engine` module delivers a schema-first, annotation-driven form generator for Vaadin Flow applications. This document
covers the main concepts, how the metadata classes are composed, and practical examples for every annotation exposed by the
engine. The goal is to help you model complex forms (including nested and repeatable structures) with full i18n, RTL, and
action orchestration support.

## High-level architecture

1. **Annotation model** – Java annotations in `com.youtopin.vaadin.formengine.annotation` describe forms, sections, groups,
   fields, option providers, repeatables, validations, cross-field rules, security, subforms, and actions.
2. **Annotation scanner** – `AnnotationFormScanner` reflects over the annotated classes, validates their structure, and
   produces a `FormDefinition` tree composed of sections, groups, fields, and actions. It fails fast when references (paths,
   form identifiers, option providers) are invalid.
3. **Field registry** – `FieldRegistry` turns each `FieldDefinition` into a Vaadin component instance. Factories apply i18n
   keys, aria labels, RTL fixes, theme tokens, and special component behaviour (e.g. Jalali calendars, map pickers, repeatable
   editors).
4. **Binder orchestrator** – `BinderOrchestrator` wraps Vaadin `Binder<T>` to support nested property paths (`a.b.c`), value
   converters (numbers, money, enums), list bindings (tags), asynchronous validation, and error mapping.
   The rendered form exposes `initializeWithBean` to pre-populate components from an existing bean instance. The helper walks the
   configured `@UiField` paths, resolves repeatable collections, and applies option items so prefilled forms remain consistent
   with their data providers.
5. **Option catalog** – Providers such as `StaticOptions`, `EnumOptions`, `CallbackOptions`, `RemoteOptions`, and
   `CascadingOptions` standardise fetch and search capabilities for select-like components, including debounced search,
   pagination, empty states, and `allowCreate` subform hand-offs.
6. **State engine** – Expression evaluation watches binder value changes and toggles visibility, enablement, requirements,
   and section/action states. Dependency graphs prevent cyclic evaluations.

## Quick-start example

```java
@UiForm(
    id = "employee-onboarding",
    titleKey = "form.employee.title",
    descriptionKey = "form.employee.description",
    bean = EmployeeFormModel.class,
    sections = {PersonalSection.class, ContractSection.class},
    actions = {
        @UiAction(id = "primarySubmit", labelKey = "form.save", placement = UiAction.Placement.FOOTER),
        @UiAction(id = "saveDraft", labelKey = "form.saveDraft", placement = UiAction.Placement.HEADER,
                  type = UiAction.ActionType.SECONDARY, order = -10)
    }
)
class EmployeeOnboardingForm {}

@UiSection(id = "personal", titleKey = "form.employee.personal", groups = {IdentityGroup.class})
class PersonalSection {}

@UiGroup(id = "identity", columns = 2)
class IdentityGroup {

    @UiField(path = "person.firstName", labelKey = "form.person.firstName", colSpan = 1)
    String firstName;

    @UiField(path = "person.lastName", labelKey = "form.person.lastName", colSpan = 1)
    String lastName;

    @UiField(path = "person.birthDate", labelKey = "form.person.birthDate",
            component = UiField.ComponentType.JALALI_DATE,
            helperKey = "form.person.birthDate.helper")
    LocalDate birthDate;
}
```

The form engine renders headers and action bars according to the annotations, wires nested binder paths, and enforces
validation rules specified in the metadata. The backing `EmployeeFormModel` exposes nested getters such as
`getPerson().getFirstName()` and `getPerson().getBirthDate()`, ensuring the property paths resolve correctly.

## Annotation catalogue

Each annotation is documented below with its attributes and usage scenarios. Examples highlight how the attributes change the
rendered form behaviour.

### `@UiForm`

Declares a root form definition. Apply it to a top-level class that references sections, lifecycle hooks, and action buttons.

| Attribute | Description |
| --- | --- |
| `id` | Stable identifier used for persistence, caching, and security guards. Must be unique. |
| `titleKey` | I18N key for the form header title. Falls back to `id` when left blank. |
| `descriptionKey` | I18N key for helper text shown below the title. Useful for instructions. |
| `bean` | Java class managed by `Binder<T>`. Enables nested property resolution and Bean Validation groups. |
| `sections` | Array of classes annotated with `@UiSection`. Defines the layout of the form. |
| `actions` | Array of `@UiAction` describing buttons rendered in header/footers. Defaults to a single submit. |
| `lifecycleHooks` | Hook identifiers executed by the engine (beforeLoad, afterLoad, beforeSubmit, afterSubmit). |

**Example – global lifecycle hooks**

```java
@UiForm(
    id = "product-editor",
    titleKey = "form.product.title",
    bean = ProductModel.class,
    sections = {GeneralSection.class, PricingSection.class},
    lifecycleHooks = {"beforeLoad", "afterSubmit"}
)
class ProductCatalogForm {}
```

The lifecycle hook names allow the application to register listeners for loading default data or invoking services after
submission.

### `@UiSection`

Groups one or more `@UiGroup` blocks into a logical section with its own visibility, ordering, and security guard.

| Attribute | Description |
| --- | --- |
| `id` | Stable identifier for referencing in expressions or action placement. |
| `titleKey` | Message key for the section header; empty hides the title. |
| `descriptionKey` | Helper text key displayed beneath the header. Useful for contextual guidance. |
| `groups` | Ordered array of classes annotated with `@UiGroup`. |
| `visibleWhen` | Expression evaluated by the state engine to toggle section visibility. |
| `securityGuard` | Identifier of a security guard defined elsewhere. When denied, the section is hidden or disabled based on guard rules. |
| `order` | Sort order relative to other sections. Lower values render earlier. |

**Example – conditional visibility**

```java
@UiSection(
    id = "advanced", titleKey = "form.product.advanced", order = 20,
    visibleWhen = "bean.type == 'COMPLEX'",
    groups = {AdvancedSettingsGroup.class}
)
class AdvancedSection {}
```

When the bound bean’s `type` property equals `COMPLEX`, the section becomes visible; otherwise it remains hidden.

### `@UiGroup`

Defines a layout container inside a section. Groups control the column count, nested subforms, and repeatable behaviour.

| Attribute | Description |
| --- | --- |
| `id` | Stable identifier for referencing in summaries or expressions. |
| `titleKey` | Optional caption key displayed above the group. |
| `columns` | Number of columns used in the responsive grid when rendering child fields. |
| `repeatable` | `@UiRepeatable` configuration enabling list-style editing (grid, cards, inline panels). |
| `subform` | `@UiSubform` configuration applied when the group contains SUBFORM components. |

**Example – repeatable group**

```java
@UiGroup(id = "attachments", titleKey = "form.policy.attachments", columns = 1,
         repeatable = @UiRepeatable(enabled = true, mode = UiRepeatable.RepeatableMode.CARD_DIALOG,
                                     summaryTemplate = "{{item.title}}", max = 5))
class AttachmentGroup {
    @UiField(path = "attachments[].title", labelKey = "form.policy.attachment.title")
    String title;
}
```

Users can add up to five attachment entries, each edited in a dialog card and summarised by the template.

### Prefilling and repository-backed forms

The `FormEngine.RenderedForm#initializeWithBean` helper reads the values of every bound field and repeatable entry from a given
bean, applying the correct presentation values (including `OptionItem` instances) to the underlying components. This makes it
straightforward to load an existing record or expose a repository snapshot when the form opens:

```java
InventoryManagementFormData data = inventoryRepository.load();
RenderedForm<InventoryManagementFormData> rendered = formEngine.render(
        InventoryManagementFormDefinition.class, provider, locale, rtl);
rendered.getFields().forEach((definition, instance) ->
        rendered.getOrchestrator().bindField(instance, definition));
rendered.initializeWithBean(data); // fields and repeatable items now match persisted state
rendered.setActionBeanSupplier(inventoryRepository::load);
rendered.addActionHandler("inventory-save", context -> inventoryRepository.save(context.getBean()));
```

When combined with an action handler that persists submissions back into an in-memory or remote repository, this pattern
delivers a full edit experience without additional boilerplate.

### Sample coverage map

The demo application under `samples/` ships multiple annotated forms that exercise distinct engine features:

| Sample | Highlights |
| --- | --- |
| Employee onboarding | Jalali inputs, map picker, validation groups |
| Product catalog | Money converters, async validation, cascading selects |
| Access policy designer | Multi-action workflow, repeatable conditions, subforms |
| Dynamic day planner | State-driven sections, numeric triggers |
| Agenda builder | External buttons driving repeatable groups |
| Inventory manager | Prefilled data, repository persistence, repeatable collections |

Use these references to see how annotations map to runtime behaviour and to copy working patterns into your own projects.

### `@UiField`

Attaches input metadata to a field or getter within a group. The engine reads the annotation to instantiate the right component
and configure validation, options, and security. The annotated member can be either a field or a method; only the metadata is
used by the scanner, but aligning the Java type with the underlying bean property (e.g. `String`, `LocalDateTime`, custom value
objects) improves readability and tooling support.

| Attribute | Description |
| --- | --- |
| `path` | Dot-notation property path relative to the bound bean. Supports list notation (`items[].name`). |
| `component` | Component type resolved by `FieldRegistry` (text, number, Jalali date/time, map, subform, repeatable, etc.). |
| `labelKey` | Message key for the field label (mandatory for accessibility). |
| `helperKey` | Helper text key shown under the field. |
| `placeholderKey` | Placeholder key for text inputs. |
| `requiredWhen` | Expression that toggles mandatory state. Works with `requiredMessageKey`. |
| `requiredMessageKey` | Message key used when the `requiredWhen` expression evaluates to true. |
| `visibleWhen` | Expression controlling visibility. |
| `enabledWhen` | Expression controlling enablement (e.g. disable when workflow locked). |
| `defaultValue` | Expression evaluated on new bean creation to prefill values. |
| `options` | `@UiOptions` configuration for select-style components. |
| `validations` | Array of `@UiValidation` rules for custom logic or async checks. |
| `crossField` | Array of `@UiCrossField` rules targeting related properties. |
| `security` | Guard configuration determining visibility/enablement under security constraints. |
| `order` | Sort order within the group. |
| `colSpan` / `rowSpan` | Layout spans within the group grid. Useful for wide text areas or map components. |

**Example – Jalali date and map fields**

```java
@UiField(path = "employee.contractStart", labelKey = "form.employee.contractStart",
         component = UiField.ComponentType.JALALI_DATE_TIME,
         helperKey = "form.employee.contractStart.helper")
LocalDateTime contractStart;

@UiField(path = "employee.homeLocation", labelKey = "form.employee.homeLocation",
         component = UiField.ComponentType.MAP, colSpan = 2,
         helperKey = "form.employee.homeLocation.helper")
GeoPoint homeLocation;
```

The Jalali picker honours RTL rendering while returning `LocalDateTime` values; the map component wraps the RTL-friendly
`MapLocationField` and expands across two columns for readability. In this example the bound bean stores coordinates inside a
`GeoPoint` value object.

### `@UiOptions`

Configures option providers for select-style components (select, multi-select, radio, autocomplete, tags).

| Attribute | Description |
| --- | --- |
| `enabled` | Activates the option provider. Required when using non-static data. |
| `type` | Provider type (`STATIC`, `ENUM`, `CALLBACK`, `REMOTE`, `CASCADING`). |
| `entries` | Array of `value|label` pairs for static options. |
| `enumType` | Fully qualified enum class name for enum-backed options. |
| `callbackRef` | Spring bean name or class reference for callback providers. Useful for in-memory datasets. |
| `remoteRef` | Endpoint identifier resolved by the demo application when querying remote services. |
| `cascadeFrom` | Parent field path used to cascade options (e.g. city list filtered by selected province). |
| `allowCreate` | Enables `allowCreate` flow where a subform dialog collects new items. |
| `allowCreateFormId` | Identifier of the form rendered in the creation dialog. |
| `clientFilter` | Enables client-side filtering of fetched options. Disable when the provider performs server-side search. |

**Example – cascading remote autocomplete**

```java
@UiField(path = "product.brand", labelKey = "form.product.brand",
         component = UiField.ComponentType.AUTOCOMPLETE,
         options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CASCADING,
                              remoteRef = "/api/catalog/brands", cascadeFrom = "product.category",
                              allowCreate = true, allowCreateFormId = "brand-create"))
BrandSummary brand;
```

The autocomplete fetches brands using the selected category as a filter, allows creating a new brand through the `brand-create`
subform, and automatically selects the newly created item. In this case the bean exposes a `BrandSummary` value object, but you
can also bind to identifiers when preferred.

### `@UiValidation`

Defines per-field validators executed in addition to Bean Validation constraints.

| Attribute | Description |
| --- | --- |
| `messageKey` | Localised message shown when the validation fails. |
| `expression` | State-engine expression returning `true` for invalid states. Ideal for synchronous rules. |
| `groups` | Bean Validation groups controlling when the rule is active. Useful for staged validation flows. |
| `asyncValidatorBean` | Bean name implementing asynchronous validation via `CompletableFuture`. Blocks form submission until resolved. |

**Example – async email uniqueness**

```java
@UiField(path = "account.email", labelKey = "form.account.email",
         component = UiField.ComponentType.EMAIL,
         validations = {
             @UiValidation(messageKey = "form.account.emailExists",
                           asyncValidatorBean = "emailUniquenessValidator")
         })
String email;
```

When the user submits the form, the orchestrator waits for the `emailUniquenessValidator` bean to complete before finalising the
binder submission.

### `@UiCrossField`

Captures validation rules that involve multiple fields. The expression is evaluated after individual field validation.

| Attribute | Description |
| --- | --- |
| `expression` | State-engine expression. Returning `true` marks the rule invalid. Refer to fields by their paths. |
| `messageKey` | Error message key displayed when the rule fails. |
| `groups` | Bean Validation groups that activate this rule. |
| `targetPaths` | Field paths where the error should be displayed. Defaults to the owning field when omitted. |

**Example – matching password fields**

```java
@UiField(path = "account.password", labelKey = "form.account.password",
         component = UiField.ComponentType.PASSWORD,
         crossField = {
             @UiCrossField(expression = "bean.account.password != bean.account.confirmPassword",
                           messageKey = "form.account.passwordMismatch",
                           targetPaths = {"account.confirmPassword"})
         })
String password;
```

The engine highlights the confirm password field when the values differ.

### `@UiRepeatable`

Enables repeatable groups where users can manage collections of items with add/remove/reorder actions.

| Attribute | Description |
| --- | --- |
| `enabled` | Turns the repeatable behaviour on. |
| `mode` | Rendering mode (`GRID_EDITOR`, `CARD_DIALOG`, `INLINE_PANEL`). |
| `min` / `max` | Minimum and maximum item counts. `Integer.MAX_VALUE` disables the max constraint. |
| `uniqueBy` | Property path within the item used to enforce uniqueness. |
| `summaryTemplate` | Template for summary chips or cards, using `{{item.property}}` placeholders. |
| `itemTitleKey` | Message key applied to entry headers. Supports `MessageFormat` where `{0}` equals the index after the configured offset and `{1}` is the zero-based index. |
| `itemTitleOffset` | Offset added to the zero-based index before substitution. Defaults to `1` for human-friendly numbering. |
| `titleGenerator` | Custom `com.youtopin.vaadin.formengine.RepeatableTitleGenerator` implementation used when templates are insufficient. |
| `allowReorder` | Enables drag-and-drop ordering. |
| `allowDuplicate` | Allows duplicating an existing item. |

Use `itemTitleKey` in combination with `itemTitleOffset` for numbered captions (for example, `Segment {0}` yields `Segment 1`, `Segment 2`, ...). When titles depend on richer logic—such as locale-specific ordinals or composite metadata—provide a custom `titleGenerator`; the engine supplies the zero-based index so implementations can apply their own math.

**Example – deep nesting with card dialogs**

```java
@UiGroup(id = "policies", titleKey = "form.access.policies",
         repeatable = @UiRepeatable(enabled = true, mode = UiRepeatable.RepeatableMode.CARD_DIALOG,
                                     uniqueBy = "code", min = 1,
                                     itemTitleKey = "form.access.policy.entryTitle"))
class PolicyGroup {
    @UiField(path = "policies[].code", labelKey = "form.access.policy.code")
    String code;

    @UiField(path = "policies[].rules", labelKey = "form.access.policy.rules",
             component = UiField.ComponentType.REPEATABLE)
    List<RuleModel> rules;
}
```

Each policy entry opens in a dialog for editing and can contain nested repeatable rule groups.

### `@UiSubform`

Configures nested subforms either inline or in dialogs. Useful for `SUBFORM` fields or `allowCreate` flows.

| Attribute | Description |
| --- | --- |
| `enabled` | Enables the subform configuration. |
| `formId` | Identifier of the form definition rendered inside the subform. |
| `mode` | Presentation mode (`INLINE` or `DIALOG`). |
| `autoOpen` | Automatically open the subform when the parent renders. |

**Example – inline subform**

```java
@UiGroup(id = "address", subform = @UiSubform(enabled = true, formId = "address-form", mode = UiSubform.SubformMode.INLINE))
class AddressGroup {
    @UiField(path = "employee.address", labelKey = "form.employee.address",
             component = UiField.ComponentType.SUBFORM)
    AddressModel address;
}
```

The address subform reuses the `address-form` definition and renders inline within the parent group.

### `@UiAction`

Describes buttons rendered by the form engine. Actions can appear in the header, global footer, or section footers, and they can
be wired to distinct behaviours.

| Attribute | Description |
| --- | --- |
| `id` | Stable identifier for wiring click listeners in the consuming view. |
| `labelKey` | Message key for the button caption. |
| `descriptionKey` | Optional helper text for assistive technology (e.g. tooltip or aria description). |
| `visibleWhen` | Expression controlling visibility. |
| `enabledWhen` | Expression controlling enablement. |
| `placement` | Where the action renders (`HEADER`, `FOOTER`, `SECTION_FOOTER`). |
| `sectionId` | Section identifier when the placement is `SECTION_FOOTER`. |
| `type` | Execution mode (`SUBMIT` or `SECONDARY`). The engine routes submit actions through binder submission. |
| `order` | Sort order within the same placement. |
| `security` | `@UiSecurity` guard applied to the action. |

**Example – multiple submit buttons**

```java
@UiForm(
    id = "access-policy",
    bean = AccessPolicyModel.class,
    sections = {PolicySection.class},
    actions = {
        @UiAction(id = "publish", labelKey = "form.access.publish",
                  placement = UiAction.Placement.HEADER, order = -5),
        @UiAction(id = "save", labelKey = "form.access.save",
                  placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SUBMIT),
        @UiAction(id = "delete", labelKey = "form.access.delete",
                  placement = UiAction.Placement.SECTION_FOOTER, sectionId = "policies",
                  type = UiAction.ActionType.SECONDARY,
                  security = @UiSecurity(requiredAuthorities = {"POLICY_DELETE"}))
    }
)
class AccessPolicyForm {}
```

The view can attach different listeners to each action id, and the engine positions the buttons according to their placement.

### `@UiSecurity`

Provides declarative security guards for forms, sections, groups, fields, or actions.

| Attribute | Description |
| --- | --- |
| `guardId` | Shared guard identifier resolved by the security integration. |
| `expression` | Boolean expression evaluated against the security context. |
| `requiredAuthorities` | Array of authority names required to interact with the component. |
| `showWhenDenied` | When `true`, the component renders disabled; otherwise it is hidden. |

**Example – read-only guard**

```java
@UiField(path = "employee.salary", labelKey = "form.employee.salary",
         component = UiField.ComponentType.MONEY,
         security = @UiSecurity(requiredAuthorities = {"PAYROLL_VIEW"}, showWhenDenied = true))
BigDecimal salary;
```

Users lacking `PAYROLL_VIEW` see the field but cannot edit it, because the guard renders it disabled rather than hidden.

### `@UiRepeatable` + `@UiField` (REPEATABLE component)

When you need nested repeatable blocks, combine the REPEATABLE component type with a repeatable-enabled group dedicated to the
child form structure.

```java
@UiGroup(id = "contacts", titleKey = "form.employee.contacts",
         repeatable = @UiRepeatable(enabled = true, mode = UiRepeatable.RepeatableMode.GRID_EDITOR))
class ContactGroup {
    @UiField(path = "contacts[]", labelKey = "form.employee.contactEntry",
             component = UiField.ComponentType.SUBFORM,
             options = @UiOptions(),
             security = @UiSecurity())
    List<ContactModel> contactEntries;
}
```

The engine renders a grid editor listing each contact row, with add/remove/reorder buttons controlled by the repeatable settings.

### `@UiSubform` + `allowCreate`

To allow users to create related entities from inside a select input, enable `allowCreate` in `@UiOptions` and point to a
subform definition. The engine opens the subform, saves the new item, updates the provider, and selects the value.

```java
@UiField(path = "product.supplier", labelKey = "form.product.supplier",
         component = UiField.ComponentType.SELECT,
         options = @UiOptions(enabled = true, type = UiOptions.ProviderType.REMOTE,
                              remoteRef = "/api/catalog/suppliers",
                              allowCreate = true, allowCreateFormId = "supplier-create"))
SupplierSummary supplier;
```

## Additional resources

- `components/src/main/java/com/youtopin/vaadin/formengine/annotation` – Source of the annotations with JavaDoc comments.
- `components/src/main/java/com/youtopin/vaadin/formengine/scanner/AnnotationFormScanner` – Implementation of metadata scanning.
- `components/src/main/java/com/youtopin/vaadin/formengine/FormEngine` – Entry point that assembles forms from definitions.
- `samples/src/main/java/com/youtopin/vaadin/samples/formengine` – Demo views showcasing employee onboarding, product catalog,
  and access policy designer scenarios.

Use this reference alongside the demo samples to map each acceptance criterion to a working example.
