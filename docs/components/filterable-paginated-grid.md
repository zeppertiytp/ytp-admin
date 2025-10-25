# FilterablePaginatedGrid

`FilterablePaginatedGrid<T>` combines a Vaadin `Grid` with pagination,
filtering, column configuration, export, and saved view features. It targets
data sets served by backend services rather than in-memory collections.

- **Source:** `components/src/main/java/com/youtopin/vaadin/component/FilterablePaginatedGrid.java`
- **Supporting classes:**
  - `ColumnDefinition` – column metadata (key, header, renderer, width)
  - `FilterDefinition` – filter inputs and match strategies
  - `PageFetchService` / `CountService` / `PageResultService` – back-end
    interfaces under `components/src/main/java/com/youtopin/vaadin/data/pagination`
- **Extended design notes:** `docs/grid-contract.md`

## Key capabilities
- Server-driven pagination with explicit `pageSize` and `currentPage` controls.
- Persisted saved views (column order & visibility) stored in `localStorage` per
  grid identifier.
- Filter dialog that maps `FilterDefinition` entries into text fields, range
  pickers, or select components, plus a chips bar that lists active filters.
- Column configuration dialog with checkbox toggles when `GridFeature.COLUMN_CONFIG`
  is enabled.
- CSV/Excel export actions provided by `PageResultService` and rendered in the
  configuration menu when `GridFeature.EXPORT` is active.
- Bulk selection support and callback hooks when `GridFeature.ROW_SELECTION`
  is enabled.
- Locale-aware labels for controls and placeholders through `LocaleChangeObserver`.

## Usage
```java
List<ColumnDefinition<Person>> columns = List.of(
        ColumnDefinition.text("fullName", person -> person.fullName(), "grid.person.fullName"),
        ColumnDefinition.number("age", Person::age, "grid.person.age")
);
List<FilterDefinition> filters = List.of(
        FilterDefinition.text("fullName", "grid.person.filter.fullName"),
        FilterDefinition.numberRange("age", "grid.person.filter.age")
);
FilterablePaginatedGrid<Person> grid = new FilterablePaginatedGrid<>(
        columns,
        filters,
        personService::findPage,
        personService::count,
        25
);
grid.setGridId("people-directory");
grid.setSelectionListener(selection -> bulkDelete.setEnabled(!selection.isEmpty()));
```

The grid requests data through the supplied services whenever pagination, sort,
or filters change. Implement `PageFetchService` to accept the filter map and
return a `PageResult<T>` describing items, total size, and export payloads.

## Feature toggles
Pass an `EnumSet<GridFeature>` to the constructor to control optional behaviour:
- `COLUMN_CONFIG`
- `EXPORT`
- `VIEWS`
- `ROW_SELECTION`

Example:
```java
EnumSet<GridFeature> features = EnumSet.of(GridFeature.COLUMN_CONFIG, GridFeature.VIEWS);
FilterablePaginatedGrid<Person> grid = new FilterablePaginatedGrid<>(
        columns,
        filters,
        personService::findPage,
        personService::count,
        25,
        features
);
```

## Accessibility & localisation
- Uses Vaadin `Grid`, inheriting keyboard navigation, focus management, and
  ARIA grid semantics.
- Filter dialog buttons, pagination controls, and exported labels pull messages
  from translation bundles. Ensure the relevant keys exist in both English and
  Farsi resource files (`samples/src/main/resources/messages*.properties`).
- When integrating with screen readers, provide descriptive column headers via
  `ColumnDefinition` label keys.

## Sample references
- Full-height layout: `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/FullGridView.java`
- Compact layout with min/max heights: `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/CompactGridView.java`
- Mixed feature demos: `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/MultipleGridsView.java`
- CRUD-style view: `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/PersonTableView.java`

