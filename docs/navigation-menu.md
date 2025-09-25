# Navigation menu configuration

The admin panel builds its drawer menu from a JSON configuration file instead
of hard-coded Java classes. This keeps the UI layout declarative, enables
runtime overrides, and allows us to extend the schema without touching the UI
layer.

## File location

* **Classpath resource:** `src/main/resources/menu/navigation-menu.json`
* **Loader:** `JsonNavigationMenuService` (infrastructure) reads the file once
  during startup and keeps the parsed structure in memory.
* **Consumer:** `MainLayout` (UI) resolves items through the
  `NavigationMenuService` interface, so the UI only depends on the domain
  `MenuItem` model.

## JSON schema

Each entry in the `items` array describes either a leaf route or a menu branch
with optional children.

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `group` | string | ✓ | Translation key suffix (e.g. `general`) used to group items under `menu.<group>` headings. |
| `labelKey` | string | ✓ | Full translation key for the drawer label (e.g. `menu.persons`). |
| `icon` | string | – | Name of a Vaadin icon (case insensitive, hyphen or underscore). When omitted the drawer row renders without an icon. |
| `navigationTarget` | string | – | Vaadin route identifier. Trailing/leading whitespace is trimmed; empty values mean the item does not navigate. |
| `children` | array | – | Nested `MenuItemDefinition` entries. When present, the parent behaves as an expandable section. |
| `requiredScopes` | array | – | OAuth/OIDC scope names that guard visibility of the item (and its children). |
| `requiredScopesLogic` | string (`AND`/`OR`) | – | Determines whether *all* (`AND`, default) or *any* (`OR`) of the listed scopes are needed. |

Whitespace in `group`, `labelKey`, `icon`, and `navigationTarget` is trimmed at
load time. Icons are normalised to upper-case underscores before being resolved
with `VaadinIcon.valueOf`.

## Scope-based filtering

The service evaluates required scopes against the current user by calling the
`UserScopeService` application interface. The default `DemoUserScopeService`
returns an empty set. Production deployments can provide another bean that reads
scopes from the authenticated principal or an external identity provider.

Visibility rules propagate down the tree:

* If a parent item is hidden because of missing scopes, all of its children are
  excluded.
* When a parent stays visible but all of its children are filtered out, the
  parent is also removed to avoid empty expandable sections.
* The `requiredScopesLogic` flag is validated and supports both strict AND and
  inclusive OR semantics.

## Adding or editing menu entries

1. Add the new JSON block under the appropriate `group`. Use trailing commas as
   shown in the existing file so the diff stays clean.
2. Introduce matching translations in `messages.properties` and
   `messages_fa.properties` for both `menu.<group>` (section header) and the
   `labelKey` values.
3. Provide a valid Vaadin icon identifier (see the
   [Vaadin Icon Gallery](https://vaadin.com/docs/latest/components/icon#available-icons)).
   Use uppercase names (e.g. `CLIPBOARD_TEXT`) or lowercase hyphenated forms
   (`clipboard-text`); both formats are supported.
4. Define `requiredScopes` only when the route should be restricted. Keep scope
   names consistent with the issuing identity provider. If multiple scopes are
   required, set `requiredScopesLogic` to `AND`; use `OR` for alternative scope
   lists.
5. For new routes, ensure a Vaadin view exists and is registered with
   `@Route("<navigationTarget>")`.

## Validation and failure modes

`JsonNavigationMenuService` validates that every entry has a non-empty `group`
and `labelKey`. The application fails fast during startup when the JSON file is
malformed, an icon name cannot be resolved, or scope logic contains an unknown
value. This keeps misconfigurations from surfacing as runtime UI issues.
