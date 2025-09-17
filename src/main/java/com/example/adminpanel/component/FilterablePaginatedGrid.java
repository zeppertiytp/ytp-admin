package com.example.adminpanel.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.component.menubar.MenuBar;
// Import the top-level MenuItem class from the context menu package.  In
// Vaadin Flow 24+, MenuItem is not nested within MenuBar, so using
// MenuBar.MenuItem will not compile.  Importing MenuItem explicitly
// allows us to declare root and submenu entries for the configuration
// menu without referencing a non-existent nested type.
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Anchor;
import java.io.ByteArrayInputStream;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.UI;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.adminpanel.component.PageResult;
import com.example.adminpanel.component.PageResultService;

/**
 * A reusable component that displays data in a paginated, filterable table.
 * It uses a {@link Grid} to render items and supports per-column text
 * filters.  When filters or pagination controls are changed, the
 * component invokes the provided {@link PageFetchService} and
 * {@link CountService} to retrieve the appropriate slice of data and
 * update the total count.
 *
 * @param <T> the type of items displayed in the grid
 */
public class FilterablePaginatedGrid<T> extends VerticalLayout implements LocaleChangeObserver {

    /**
     * Enumeration of optional features that can be enabled on a grid.
     * Use this to control which advanced capabilities (column configuration,
     * exports, saved views and row selection) are available to the user.
     */
    public enum GridFeature {
        /** Allow users to show/hide columns via a configuration dialog. */
        COLUMN_CONFIG,
        /** Enable exporting the grid contents to external formats (CSV, Excel). */
        EXPORT,
        /** Allow saving and restoring custom column orders/visibility as named views. */
        VIEWS,
        /** Enable row selection and expose selection related callbacks and actions. */
        ROW_SELECTION
    }

    private final Grid<T> grid = new Grid<>();
    private final HorizontalLayout filterRow = new HorizontalLayout();
    private final HorizontalLayout paginationControls = new HorizontalLayout();
    private Span pageSizeLabel;
    private final Span pageInfo = new Span();
    private final Button prevButton = new Button();
    private final Button nextButton = new Button();
    private final com.vaadin.flow.component.combobox.ComboBox<Integer> pageSizeSelect = new com.vaadin.flow.component.combobox.ComboBox<>();

    // Top control bar containing the filter button and page size selector.
    private final com.vaadin.flow.component.orderedlayout.HorizontalLayout topControls = new com.vaadin.flow.component.orderedlayout.HorizontalLayout();

    private final List<ColumnDefinition> columns;
    private final List<FilterDefinition> filterDefinitions;
    private final PageFetchService<T> fetchService;
    private final CountService<T> countService;
    private final PageResultService<T> pageResultService;
    private final boolean usePageResult;
    private final Map<String, String> filters = new HashMap<>();
    private final Map<String, Component> filterComponents = new HashMap<>();
    private final java.util.Map<com.vaadin.flow.component.HasText, String> filterLabelKeys = new java.util.LinkedHashMap<>();
    private final com.vaadin.flow.component.dialog.Dialog filterDialog = new com.vaadin.flow.component.dialog.Dialog();
    private final Button filterButton = new Button();


    /**
     * List of column components for this grid.  Maintains references so
     * visibility and order can be persisted and restored.
     */
    private final java.util.List<Grid.Column<T>> gridColumns = new java.util.ArrayList<>();

    /**
     * Identifier for this grid instance.  Used when persisting views to
     * LocalStorage.  Populated in the constructor based on column
     * definitions.
     */
    // Identifier for this grid instance.  Used when persisting views to LocalStorage.  Populated lazily in initLayout based on column definitions.
    private String gridId;

    /**
     * The set of enabled features for this grid instance.  Features control
     * whether optional capabilities such as column configuration, export,
     * saved views or row selection are available to users.  Defaults to
     * all features enabled.
     */
    private final java.util.EnumSet<GridFeature> features;

    /**
     * Optional listener invoked when the row selection changes.  Only
     * applicable if {@link GridFeature#ROW_SELECTION} is enabled.  The
     * consumer receives the current set of selected items.
     */
    private java.util.function.Consumer<java.util.Set<T>> selectionListener;

    // Components for the table configuration menu.  These are stored so
    // that translations can be updated on locale changes and view names
    // reloaded dynamically.
    private MenuBar configMenu;
    // Root entries for the configuration menu.  Use the top-level MenuItem
    // type imported from com.vaadin.flow.component.contextmenu.MenuItem.  In
    // Vaadin Flow, MenuItem is a standalone class in the contextmenu package,
    // rather than a nested type of MenuBar.  Declaring these as MenuItem
    // avoids class resolution errors during compilation.
    private MenuItem configRoot;
    private MenuItem exportRoot;
    private MenuItem viewsRoot;

    /**
     * Controls whether this grid will expand to fill the remaining vertical space in its
     * container. When {@code true} (default) the grid will grow to take up all available
     * space in the parent layout. When {@code false}, the grid height is constrained
     * by {@link #minHeight} and {@link #maxHeight} and the grid will scroll if its
     * content exceeds the maximum height.
     */
    private boolean expandGrid = true;

    /**
     * The minimum height of the grid when {@link #expandGrid} is {@code false}. This value
     * accepts any valid CSS height (e.g. "200px", "20rem"). If {@code null},
     * no minimum height is applied.
     */
    private String minHeight = null;

    /**
     * The maximum height of the grid when {@link #expandGrid} is {@code false}. This value
     * accepts any valid CSS height (e.g. "400px", "50vh"). If {@code null},
     * no maximum height is applied and the grid may grow indefinitely.
     */
    private String maxHeight = null;

    /**
     * Set a listener to be notified when the grid selection changes.  When
     * {@link GridFeature#ROW_SELECTION} is enabled, the consumer will be
     * invoked with the current set of selected items.  If row selection is
     * disabled, the listener is ignored.
     *
     * @param listener a consumer receiving the current selection
     */
    public void setSelectionListener(java.util.function.Consumer<java.util.Set<T>> listener) {
        this.selectionListener = listener;
    }
    // Bar showing currently applied filters with remove buttons
    private final com.vaadin.flow.component.orderedlayout.HorizontalLayout appliedFiltersBar = new com.vaadin.flow.component.orderedlayout.HorizontalLayout();
    // Callback to notify parent view when filters change so it can update the URL
    private java.util.function.Consumer<Map<String, String>> filtersChangedCallback;
    private int pageSize = 10;
    private int currentPage = 0;
    private int totalCount = 0;

    /**
     * Button for performing bulk actions on selected rows.  Initially
     * disabled and updated by the grid selection listener.
     */
    private final Button bulkActionButton = new Button();

    // The set of enabled features for this grid instance is defined above. Remove duplicate declarations.

    /**
     * Constructs a new FilterablePaginatedGrid.
     *
     * @param columns the definitions of columns to display
     * @param fetchService the service used to fetch data pages
     * @param countService the service used to count total items for filters
     * @param pageSize the number of items per page
     */
    public FilterablePaginatedGrid(List<ColumnDefinition> columns, List<FilterDefinition> filterDefinitions,
                                   PageFetchService<T> fetchService,
                                   CountService<T> countService, int pageSize) {
        this(columns, filterDefinitions, fetchService, countService, pageSize, java.util.EnumSet.allOf(GridFeature.class));
    }

    /**
     * Construct a FilterablePaginatedGrid with explicit feature configuration.  Use
     * this constructor when you want to disable some optional capabilities
     * such as column configuration, export, saved views or row selection.
     *
     * @param columns the definitions of columns to display
     * @param filterDefinitions the definitions of available filters
     * @param fetchService the service used to fetch data pages
     * @param countService the service used to count total items for filters
     * @param pageSize the number of items per page
     * @param features the set of enabled features (defaults to all if null)
     */
    public FilterablePaginatedGrid(List<ColumnDefinition> columns, List<FilterDefinition> filterDefinitions,
                                   PageFetchService<T> fetchService,
                                   CountService<T> countService, int pageSize,
                                   java.util.EnumSet<GridFeature> features) {
        this.columns = columns;
        this.filterDefinitions = filterDefinitions;
        this.fetchService = fetchService;
        this.countService = countService;
        this.pageResultService = null;
        this.usePageResult = false;
        this.pageSize = pageSize;
        this.features = features != null ? java.util.EnumSet.copyOf(features) : java.util.EnumSet.allOf(GridFeature.class);
        this.gridId = generateDefaultGridId(columns);
        initLayout();
        initGrid();
        initFilterDialog();
        initPagination();
        refresh();
        applyTranslations();
    }

    /**
     * Construct a FilterablePaginatedGrid backed by a {@link PageResultService}.
     * This constructor allows retrieving the items and total count in a single
     * call to avoid multiple repository queries.  When this constructor is
     * used, the separate fetch/count services are ignored.
     */
    public FilterablePaginatedGrid(List<ColumnDefinition> columns, List<FilterDefinition> filterDefinitions,
                                   PageResultService<T> pageResultService, int pageSize) {
        this(columns, filterDefinitions, pageResultService, pageSize, java.util.EnumSet.allOf(GridFeature.class));
    }

    /**
     * Construct a FilterablePaginatedGrid backed by a {@link PageResultService} with
     * explicit feature configuration.  This constructor allows retrieving the
     * items and total count in a single call to avoid multiple repository
     * queries and optionally disables certain features.
     *
     * @param columns column definitions
     * @param filterDefinitions filter definitions
     * @param pageResultService the service that returns both items and total count
     * @param pageSize the number of items per page
     * @param features the set of enabled features (defaults to all if null)
     */
    public FilterablePaginatedGrid(List<ColumnDefinition> columns, List<FilterDefinition> filterDefinitions,
                                   PageResultService<T> pageResultService, int pageSize,
                                   java.util.EnumSet<GridFeature> features) {
        this.columns = columns;
        this.filterDefinitions = filterDefinitions;
        this.fetchService = null;
        this.countService = null;
        this.pageResultService = pageResultService;
        this.usePageResult = true;
        this.pageSize = pageSize;
        this.features = features != null ? java.util.EnumSet.copyOf(features) : java.util.EnumSet.allOf(GridFeature.class);
        this.gridId = generateDefaultGridId(columns);
        initLayout();
        initGrid();
        initFilterDialog();
        initPagination();
        refresh();
        applyTranslations();
    }

    /**
     * Convenience constructor that derives filter definitions from the provided
     * columns.  This behaves like the previous version of the component where
     * each visible column automatically has a filter.  Hidden filters are not
     * supported with this constructor; for more control, use the full
     * constructor with an explicit list of {@link FilterDefinition}s.
     */
    public FilterablePaginatedGrid(List<ColumnDefinition> columns, PageFetchService<T> fetchService,
                                   CountService<T> countService, int pageSize) {
        this(columns, fetchService, countService, pageSize, java.util.EnumSet.allOf(GridFeature.class));
    }

    /**
     * Convenience constructor that derives filter definitions from the provided
     * columns and allows feature configuration.  Each visible column
     * automatically has a filter based on its data type.  Hidden filters
     * are not supported with this constructor; for more control, use the full
     * constructor with an explicit list of {@link FilterDefinition}s.
     *
     * @param columns the column definitions
     * @param fetchService the page fetch service
     * @param countService the count service
     * @param pageSize number of items per page
     * @param features the enabled features (or null for all)
     */
    public FilterablePaginatedGrid(List<ColumnDefinition> columns, PageFetchService<T> fetchService,
                                   CountService<T> countService, int pageSize,
                                   java.util.EnumSet<GridFeature> features) {
        this(columns,
             ((java.util.List<FilterDefinition>) columns.stream()
                     .map(col -> {
                         FilterDefinition.FilterType ft;
                         if (Number.class.isAssignableFrom(col.getPropertyType())) {
                             ft = FilterDefinition.FilterType.NUMBER_RANGE;
                         } else if (java.util.Date.class.isAssignableFrom(col.getPropertyType())
                                 || java.time.LocalDate.class.isAssignableFrom(col.getPropertyType())) {
                             ft = FilterDefinition.FilterType.DATE_RANGE;
                         } else {
                             ft = FilterDefinition.FilterType.TEXT;
                         }
                         return new FilterDefinition(col.getPropertyName(), col.getHeaderKey(), ft, col.getPropertyType());
                     })
                     .collect(java.util.stream.Collectors.toList())),
             fetchService, countService, pageSize, features);
    }

    private void initLayout() {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        // Configure top controls: will be filled later in initPagination()
        topControls.setWidthFull();
        topControls.setSpacing(true);
        topControls.setAlignItems(Alignment.CENTER);
        topControls.getStyle().set("flex-wrap", "wrap");

        // Add filter icon to the filter button
        filterButton.setIcon(new com.example.adminpanel.components.AppIcon("filter", "18"));
        filterButton.getElement().getStyle().set("margin-right", "0.25rem");

        // Arrange the filter row, top controls, grid, and pagination controls vertically.
        // The grid should take up the available vertical space within this layout,
        // while control bars occupy only the space needed for their contents.
        add(filterRow, topControls, appliedFiltersBar, grid, paginationControls);
        // Expand the grid to fill vertical space only if expandGrid is enabled.
        if (expandGrid) {
            expand(grid);
        } else {
            // When not expanding, ensure the grid does not grow. Setting flex-grow to 0
            // prevents the grid from consuming all available space.
            grid.getElement().getStyle().set("flex-grow", "0");
        }
        // Hide the legacy filter row since filters are now controlled via a dialog
        filterRow.setVisible(false);
        filterRow.setWidthFull();
        paginationControls.setWidthFull();
        paginationControls.setAlignItems(Alignment.CENTER);

        // Populate the top control bar with its components (page size selector and other controls)
        initTopControls();
        // Load any previously saved view names from LocalStorage.  This is
        // performed after the layout is initialised so that the configuration
        // menu has been created and the views submenu is available.
        loadViewNames();

        // Apply the initial height settings for the grid.  This will set flex-grow,
        // min/max heights and overflow based on the current expand/min/max properties.
        applyGridHeightStyle();
    }

    private void initGrid() {
        grid.setWidthFull();
        // Make all rows visible so that the grid height adapts to the number of rows
        // displayed.  This avoids the need to explicitly set a height or rely on
        // deprecated API such as setHeightByRows().  When pagination is used, only
        // the current page of rows is visible so the grid does not grow unbounded.
        try {
            // setAllRowsVisible(boolean) was introduced to replace the removed
            // setHeightByRows(boolean) API.  Invoke it reflectively to avoid
            // compilation issues on older Vaadin versions where the method may
            // not exist.
            Grid.class.getMethod("setAllRowsVisible", boolean.class)
                    .invoke(grid, true);
        } catch (Exception e) {
            // If the API is unavailable, fallback to a fixed height so that rows
            // are visible.  Adjust as needed in your own application.
            grid.setHeight("400px");
        }
        // Allow multi-sorting so users can sort by multiple columns.  Without
        // enabling this, only single-column sorting is possible.
        grid.setMultiSort(true);
        // Allow users to reorder columns by dragging the headers.
        grid.setColumnReorderingAllowed(true);
        // Configure row selection based on feature flags.  If the
        // ROW_SELECTION feature is disabled, selection mode is NONE.
        if (features.contains(GridFeature.ROW_SELECTION)) {
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
        } else {
            grid.setSelectionMode(Grid.SelectionMode.NONE);
        }
        // Add columns based on definitions.  Keep references so that we
        // can persist order and visibility later.  Assign a key equal to
        // the property name so columns can be looked up when restoring
        // saved views.
        for (ColumnDefinition def : columns) {
            Grid.Column<T> col = grid.addColumn(item -> getPropertyValue(item, def.getPropertyName()))
                    .setHeader(getTranslation(def.getHeaderKey()))
                    .setKey(def.getPropertyName());
            col.setResizable(true);
            gridColumns.add(col);
        }
        // Listen for selection changes to update the bulk action caption and notify listener
        grid.addSelectionListener(event -> {
            java.util.Set<T> selectedItems = event.getAllSelectedItems();
            int selected = selectedItems.size();
            if (features.contains(GridFeature.ROW_SELECTION)) {
                // Update the button text with the number of selected rows using a translation key. Pass
                // the integer directly so MessageFormat replaces {0} properly.
                bulkActionButton.setText(getTranslation("grid.selectedCount", selected));
                bulkActionButton.setEnabled(selected > 0);
                // Invoke external listener if set
                if (selectionListener != null) {
                    selectionListener.accept(selectedItems);
                }
            }
        });
    }

    private void initFilterDialog() {
        // Configure the filter dialog: it contains a form layout of controls for
        // each filter definition and apply/clear buttons.  The dialog is
        // resizable and draggable and closes on escape or outside click.
        filterDialog.setCloseOnEsc(true);
        filterDialog.setCloseOnOutsideClick(true);
        filterDialog.setDraggable(true);
        filterDialog.setResizable(true);
        filterDialog.setHeaderTitle(getTranslation("grid.filters"));

        com.vaadin.flow.component.formlayout.FormLayout formLayout = new com.vaadin.flow.component.formlayout.FormLayout();
        filterLabelKeys.clear();

        // Map to store parent-child relationships for dependent filters
        java.util.Map<String, com.vaadin.flow.component.combobox.ComboBox<String>> selectComponents = new java.util.HashMap<>();

        for (FilterDefinition def : filterDefinitions) {
            switch (def.getType()) {
                case TEXT -> {
                    TextField tf = new TextField();
                    tf.setClearButtonVisible(true);
                    tf.setPlaceholder(getTranslation("grid.filterPlaceholder"));
                    filterComponents.put(def.getPropertyName(), tf);
                    com.vaadin.flow.component.html.Span label = new com.vaadin.flow.component.html.Span(getTranslation(def.getLabelKey()));
                    formLayout.addFormItem(tf, label);
                    filterLabelKeys.put(label, def.getLabelKey());
                }
                case NUMBER_RANGE -> {
                    // Two integer fields: min and max
                    IntegerField minField = new IntegerField();
                    minField.setPlaceholder(getTranslation("grid.minPlaceholder"));
                    minField.setClearButtonVisible(true);
                    IntegerField maxField = new IntegerField();
                    maxField.setPlaceholder(getTranslation("grid.maxPlaceholder"));
                    maxField.setClearButtonVisible(true);
                    filterComponents.put(def.getPropertyName() + ".min", minField);
                    filterComponents.put(def.getPropertyName() + ".max", maxField);
                    com.vaadin.flow.component.orderedlayout.HorizontalLayout rangeLayout = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(minField, maxField);
                    rangeLayout.setWidthFull();
                    rangeLayout.setSpacing(true);
                    com.vaadin.flow.component.html.Span label = new com.vaadin.flow.component.html.Span(getTranslation(def.getLabelKey()));
                    formLayout.addFormItem(rangeLayout, label);
                    filterLabelKeys.put(label, def.getLabelKey());
                }
                case DATE_RANGE -> {
                    com.vaadin.flow.component.datepicker.DatePicker from = new com.vaadin.flow.component.datepicker.DatePicker();
                    from.setPlaceholder(getTranslation("grid.fromPlaceholder"));
                    com.vaadin.flow.component.datepicker.DatePicker to = new com.vaadin.flow.component.datepicker.DatePicker();
                    to.setPlaceholder(getTranslation("grid.toPlaceholder"));
                    filterComponents.put(def.getPropertyName() + ".from", from);
                    filterComponents.put(def.getPropertyName() + ".to", to);
                    com.vaadin.flow.component.orderedlayout.HorizontalLayout dateLayout = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(from, to);
                    dateLayout.setWidthFull();
                    dateLayout.setSpacing(true);
                    com.vaadin.flow.component.html.Span label = new com.vaadin.flow.component.html.Span(getTranslation(def.getLabelKey()));
                    formLayout.addFormItem(dateLayout, label);
                    filterLabelKeys.put(label, def.getLabelKey());
                }
                case SELECT -> {
                    com.vaadin.flow.component.combobox.ComboBox<String> cb = new com.vaadin.flow.component.combobox.ComboBox<>();
                    cb.setPlaceholder(getTranslation("grid.selectPlaceholder"));
                    cb.setItems(def.getOptions());
                    cb.setClearButtonVisible(true);
                    filterComponents.put(def.getPropertyName(), cb);
                    com.vaadin.flow.component.html.Span label = new com.vaadin.flow.component.html.Span(getTranslation(def.getLabelKey()));
                    formLayout.addFormItem(cb, label);
                    filterLabelKeys.put(label, def.getLabelKey());
                    selectComponents.put(def.getPropertyName(), cb);
                }
            }
        }

        // Handle dependencies: for each select filter that depends on another select filter,
        // add a value change listener to update its options based on the parent value.
        for (FilterDefinition def : filterDefinitions) {
            if (def.getType() == FilterDefinition.FilterType.SELECT && def.getDependsOn() != null) {
                String childProp = def.getPropertyName();
                String parentProp = def.getDependsOn();
                com.vaadin.flow.component.combobox.ComboBox<String> child = selectComponents.get(childProp);
                com.vaadin.flow.component.combobox.ComboBox<String> parent = selectComponents.get(parentProp);
                if (child != null && parent != null) {
                    // When parent value changes, update child's options to those options
                    // that match the selected parent value.  The options list is
                    // encoded as "parent:value" in the FilterDefinition options list.
                    parent.addValueChangeListener(ev -> {
                        String selected = ev.getValue();
                        java.util.List<String> newOptions;
                        if (selected == null || selected.isEmpty()) {
                            // Show all options for child if parent not selected
                            newOptions = def.getOptions();
                        } else {
                            newOptions = def.getOptions().stream()
                                    .filter(opt -> {
                                        // Option format: "country|city" or just "city" if no encoding
                                        if (opt.contains("|")) {
                                            String[] parts = opt.split("\\|");
                                            return parts[0].equalsIgnoreCase(selected);
                                        }
                                        return true;
                                    })
                                    .map(opt -> opt.contains("|") ? opt.split("\\|")[1] : opt)
                                    .toList();
                        }
                        child.setItems(newOptions);
                    });
                }
            }
        }

        Button apply = new Button(getTranslation("grid.apply"), event -> {
            filters.clear();
            for (FilterDefinition def : filterDefinitions) {
                switch (def.getType()) {
                    case TEXT -> {
                        Component comp = filterComponents.get(def.getPropertyName());
                        if (comp instanceof TextField) {
                            String val = ((TextField) comp).getValue();
                            if (val != null && !val.isEmpty()) {
                                filters.put(def.getPropertyName(), val.toLowerCase());
                            }
                        }
                    }
                    case NUMBER_RANGE -> {
                        Component minComp = filterComponents.get(def.getPropertyName() + ".min");
                        Component maxComp = filterComponents.get(def.getPropertyName() + ".max");
                        if (minComp instanceof IntegerField) {
                            Integer val = ((IntegerField) minComp).getValue();
                            if (val != null) {
                                filters.put(def.getPropertyName() + ".min", val.toString());
                            }
                        }
                        if (maxComp instanceof IntegerField) {
                            Integer val = ((IntegerField) maxComp).getValue();
                            if (val != null) {
                                filters.put(def.getPropertyName() + ".max", val.toString());
                            }
                        }
                    }
                    case DATE_RANGE -> {
                        Component fromComp = filterComponents.get(def.getPropertyName() + ".from");
                        Component toComp = filterComponents.get(def.getPropertyName() + ".to");
                        if (fromComp instanceof com.vaadin.flow.component.datepicker.DatePicker) {
                            java.time.LocalDate date = ((com.vaadin.flow.component.datepicker.DatePicker) fromComp).getValue();
                            if (date != null) {
                                filters.put(def.getPropertyName() + ".from", date.toString());
                            }
                        }
                        if (toComp instanceof com.vaadin.flow.component.datepicker.DatePicker) {
                            java.time.LocalDate date = ((com.vaadin.flow.component.datepicker.DatePicker) toComp).getValue();
                            if (date != null) {
                                filters.put(def.getPropertyName() + ".to", date.toString());
                            }
                        }
                    }
                    case SELECT -> {
                        Component comp = filterComponents.get(def.getPropertyName());
                        if (comp instanceof com.vaadin.flow.component.combobox.ComboBox<?> cb) {
                            Object val = cb.getValue();
                            if (val != null && !val.toString().isEmpty()) {
                                filters.put(def.getPropertyName(), val.toString());
                            }
                        }
                    }
                }
            }
            currentPage = 0;
            refresh();
            updateAppliedFiltersBar();
            // Notify callback
            if (filtersChangedCallback != null) {
                filtersChangedCallback.accept(new java.util.HashMap<>(filters));
            }
            filterDialog.close();
        });

        Button clear = new Button(getTranslation("grid.clear"), event -> {
            filters.clear();
            filterComponents.values().forEach(comp -> {
                if (comp instanceof TextField) {
                    ((TextField) comp).clear();
                } else if (comp instanceof IntegerField) {
                    ((IntegerField) comp).clear();
                } else if (comp instanceof com.vaadin.flow.component.datepicker.DatePicker) {
                    ((com.vaadin.flow.component.datepicker.DatePicker) comp).clear();
                } else if (comp instanceof com.vaadin.flow.component.combobox.ComboBox<?> cb) {
                    cb.clear();
                }
            });
            currentPage = 0;
            refresh();
            updateAppliedFiltersBar();
            // Notify callback
            if (filtersChangedCallback != null) {
                filtersChangedCallback.accept(new java.util.HashMap<>(filters));
            }
            filterDialog.close();
        });
        com.vaadin.flow.component.orderedlayout.HorizontalLayout buttons = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(apply, clear);
        buttons.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END);
        filterDialog.add(formLayout, buttons);

        // configure filter button
        filterButton.addClickListener(e -> filterDialog.open());
    }

    private void initPagination() {
        prevButton.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                refresh();
            }
        });
        nextButton.addClickListener(e -> {
            if ((currentPage + 1) * pageSize < totalCount) {
                currentPage++;
                refresh();
            }
        });
        // Configure pagination controls to be evenly spaced
        paginationControls.setWidthFull();
        paginationControls.setSpacing(true);
        paginationControls.setAlignItems(Alignment.CENTER);
        paginationControls.getStyle().set("flex-wrap", "wrap");
        // Add previous/next buttons and page info.  Page size selector and filter button are added to topControls.
        paginationControls.add(prevButton, pageInfo, nextButton);
    }

    /**
     * Initialise the top control bar.  This bar contains the filter button and
     * a page size selector that allows the user to choose how many rows
     * appear on each page.  The page size selector does not have a label to
     * conserve horizontal space; instead, a small caption is placed next to it.
     */
    private void initTopControls() {
        // Remove existing controls
        topControls.removeAll();
        // Configure page size selector
        pageSizeSelect.setItems(10, 20, 50);
        pageSizeSelect.setValue(pageSize);
        pageSizeSelect.setWidth("6em");
        pageSizeSelect.addValueChangeListener(e -> {
            Integer val = e.getValue();
            if (val != null && val > 0) {
                pageSize = val;
                currentPage = 0;
                refresh();
            }
        });
        // Create or update the label for the page size selector
        pageSizeLabel = new Span(getTranslation("grid.rowsPerPage"));
        HorizontalLayout pageSizeGroup = new HorizontalLayout(pageSizeLabel, pageSizeSelect);
        pageSizeGroup.setAlignItems(Alignment.CENTER);
        pageSizeGroup.setSpacing(true);
        pageSizeGroup.getStyle().set("flex-wrap", "nowrap");

        // Ensure filter button text updated in initLayout.  Provide small margin between controls.
        filterButton.getStyle().set("margin-inline-start", "0.5rem");

        // Create a new configuration menu only if at least one of the config features is enabled
        boolean hasConfigFeatures = features.contains(GridFeature.COLUMN_CONFIG) || features.contains(GridFeature.EXPORT) || features.contains(GridFeature.VIEWS);
        if (hasConfigFeatures) {
            configMenu = new MenuBar();
            configMenu.setOpenOnHover(true);
            // Clear any existing items
            configMenu.getItems().clear();
            configRoot = configMenu.addItem(getTranslation("grid.config"));
            // Build sub menus
            var rootSub = configRoot.getSubMenu();
            // Column configuration
            if (features.contains(GridFeature.COLUMN_CONFIG)) {
                rootSub.addItem(getTranslation("grid.columns"), e -> openColumnsDialog());
            }
            // Export submenu
            if (features.contains(GridFeature.EXPORT)) {
                exportRoot = rootSub.addItem(getTranslation("grid.export"));
                var exportSub = exportRoot.getSubMenu();
                exportSub.addItem(getTranslation("grid.exportCsv"), e -> exportCsv());
                exportSub.addItem(getTranslation("grid.exportExcel"), e -> exportXlsx());
            } else {
                exportRoot = null;
            }
            // Views submenu
            if (features.contains(GridFeature.VIEWS)) {
                viewsRoot = rootSub.addItem(getTranslation("grid.views"));
                var viewsSub = viewsRoot.getSubMenu();
                // Save current view item
                viewsSub.addItem(getTranslation("grid.saveView"), e -> saveCurrentView());
                // Loaded saved views will be added in loadViewNames()
            } else {
                viewsRoot = null;
            }
        } else {
            configMenu = null;
            configRoot = null;
            exportRoot = null;
            viewsRoot = null;
        }

        // Configure bulk action button: visible only if row selection is enabled
        if (features.contains(GridFeature.ROW_SELECTION)) {
            bulkActionButton.setVisible(true);
            // Initialize with current selection count (likely zero) using integer parameter
            bulkActionButton.setText(getTranslation("grid.selectedCount", grid.getSelectedItems().size()));
            bulkActionButton.setEnabled(false);
            bulkActionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            bulkActionButton.setVisible(false);
        }

        // Assemble controls into topControls
        topControls.setSpacing(true);
        topControls.getStyle().set("flex-wrap", "wrap");
        // Always add page size group and filter button
        if (features.contains(GridFeature.ROW_SELECTION)) {
            // Bulk actions placed next to page size and filter if available
            if (configMenu != null) {
                topControls.add(pageSizeGroup, filterButton, bulkActionButton, configMenu);
            } else {
                topControls.add(pageSizeGroup, filterButton, bulkActionButton);
            }
        } else {
            if (configMenu != null) {
                topControls.add(pageSizeGroup, filterButton, configMenu);
            } else {
                topControls.add(pageSizeGroup, filterButton);
            }
        }
    }

    private void refresh() {
        List<T> items;
        if (usePageResult && pageResultService != null) {
            // Fetch both items and total count in a single call
            PageResult<T> result = pageResultService.fetchPage(currentPage * pageSize, pageSize, filters);
            items = result.getItems();
            totalCount = result.getTotalCount();
        } else {
            // Update total count via count service
            totalCount = countService.count(filters);
            // Fetch current page via fetch service
            items = fetchService.fetch(currentPage * pageSize, pageSize, filters);
        }
        // Update items in the grid
        grid.setItems(items);
        try {
            grid.getDataProvider().refreshAll();
        } catch (Exception ignored) {
        }
        updatePageInfo();
        // Update button states
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled((currentPage + 1) * pageSize < totalCount);
    }

    private void updatePageInfo() {
        int from = totalCount == 0 ? 0 : currentPage * pageSize + 1;
        int to = Math.min((currentPage + 1) * pageSize, totalCount);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        if (totalPages == 0) totalPages = 1;
        pageInfo.setText(getTranslation("grid.pageInfo", from, to, totalCount));
    }

    private void applyTranslations() {
        filterButton.setText(getTranslation("grid.filter"));
        filterDialog.setHeaderTitle(getTranslation("grid.filters"));
        prevButton.setText(getTranslation("grid.previous"));
        nextButton.setText(getTranslation("grid.next"));
        if (pageSizeLabel != null) {
            pageSizeLabel.setText(getTranslation("grid.rowsPerPage"));
        }

        for (int i = 0; i < gridColumns.size(); i++) {
            Grid.Column<T> col = gridColumns.get(i);
            ColumnDefinition def = columns.get(i);
            col.setHeader(getTranslation(def.getHeaderKey()));
        }

        filterLabelKeys.forEach((label, key) -> label.setText(getTranslation(key)));

        filterComponents.forEach((key, comp) -> {
            if (comp instanceof TextField tf) {
                tf.setPlaceholder(getTranslation("grid.filterPlaceholder"));
            } else if (comp instanceof IntegerField field) {
                if (key.endsWith(".min")) {
                    field.setPlaceholder(getTranslation("grid.minPlaceholder"));
                } else if (key.endsWith(".max")) {
                    field.setPlaceholder(getTranslation("grid.maxPlaceholder"));
                }
            } else if (comp instanceof com.vaadin.flow.component.datepicker.DatePicker dp) {
                if (key.endsWith(".from")) {
                    dp.setPlaceholder(getTranslation("grid.fromPlaceholder"));
                } else if (key.endsWith(".to")) {
                    dp.setPlaceholder(getTranslation("grid.toPlaceholder"));
                }
            } else if (comp instanceof com.vaadin.flow.component.combobox.ComboBox<?> cb) {
                cb.setPlaceholder(getTranslation("grid.selectPlaceholder"));
            }
        });

        if (configRoot != null) {
            configRoot.setText(getTranslation("grid.config"));
            var rootSub = configRoot.getSubMenu();
            int idx = 0;
            if (features.contains(GridFeature.COLUMN_CONFIG)) {
                MenuItem colItem = (MenuItem) rootSub.getItems().get(idx++);
                colItem.setText(getTranslation("grid.columns"));
            }
            if (features.contains(GridFeature.EXPORT) && exportRoot != null) {
                exportRoot.setText(getTranslation("grid.export"));
                var exportSub = exportRoot.getSubMenu();
                if (exportSub != null && exportSub.getItems().size() >= 2) {
                    exportSub.getItems().get(0).setText(getTranslation("grid.exportCsv"));
                    exportSub.getItems().get(1).setText(getTranslation("grid.exportExcel"));
                }
                idx++;
            }
            if (features.contains(GridFeature.VIEWS) && viewsRoot != null) {
                viewsRoot.setText(getTranslation("grid.views"));
                var viewsSub = viewsRoot.getSubMenu();
                if (viewsSub != null && !viewsSub.getItems().isEmpty()) {
                    viewsSub.getItems().get(0).setText(getTranslation("grid.saveView"));
                }
            }
        }

        if (features.contains(GridFeature.ROW_SELECTION) && bulkActionButton != null) {
            int selected = grid.getSelectedItems().size();
            bulkActionButton.setText(getTranslation("grid.selectedCount", selected));
        }

        updatePageInfo();
    }

    /**
     * Set a callback to be invoked whenever the filter set changes (applied, cleared, or removed individually).
     * The callback receives a copy of the current filters map.  Use this to
     * update the browser URL from the parent view.
     */
    public void setFiltersChangedCallback(java.util.function.Consumer<Map<String, String>> callback) {
        this.filtersChangedCallback = callback;
    }

    /**
     * Apply initial filters from query parameters.  Parameter names map to filter keys
     * (e.g. "age.min" or "firstName").  Values are lists of strings; only the
     * first value is used.  This method updates the internal filters map,
     * populates filter controls, refreshes the grid, updates applied filter chips,
     * and does not fire the change callback (to avoid URL updates during initial load).
     */
    public void applyFiltersFromQueryParams(java.util.Map<String, java.util.List<String>> queryParams) {
        filters.clear();
        // Populate filters map
        for (java.util.Map.Entry<String, java.util.List<String>> entry : queryParams.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                filters.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        // Update filter component values accordingly
        for (Map.Entry<String, Component> e : filterComponents.entrySet()) {
            String key = e.getKey();
            Component comp = e.getValue();
            String val = filters.get(key);
            if (comp instanceof TextField) {
                ((TextField) comp).setValue(val != null ? val : "");
            } else if (comp instanceof IntegerField) {
                IntegerField nf = (IntegerField) comp;
                if (val != null && !val.isEmpty()) {
                    try {
                        nf.setValue(Integer.parseInt(val));
                    } catch (NumberFormatException ex) {
                        nf.clear();
                    }
                } else {
                    nf.clear();
                }
            } else if (comp instanceof com.vaadin.flow.component.datepicker.DatePicker) {
                com.vaadin.flow.component.datepicker.DatePicker dp = (com.vaadin.flow.component.datepicker.DatePicker) comp;
                if (val != null && !val.isEmpty()) {
                    try {
                        dp.setValue(java.time.LocalDate.parse(val));
                    } catch (Exception ex) {
                        dp.clear();
                    }
                } else {
                    dp.clear();
                }
            } else if (comp instanceof com.vaadin.flow.component.combobox.ComboBox<?> cb) {
                com.vaadin.flow.component.combobox.ComboBox combo = (com.vaadin.flow.component.combobox.ComboBox) comp;
                if (val != null && !val.isEmpty()) {
                    combo.setValue(val);
                } else {
                    combo.clear();
                }
            }
        }
        currentPage = 0;
        refresh();
        updateAppliedFiltersBar();
    }

    private String getPropertyValue(T item, String propertyName) {
        try {
            // First try to use a JavaBean getter via PropertyDescriptor.  This
            // works when a standard getXxx() method exists for the property.
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, item.getClass());
            Method getter = pd.getReadMethod();
            if (getter != null) {
                Object value = getter.invoke(item);
                return value == null ? "" : value.toString();
            }
        } catch (Exception ignore) {
            // Ignore and fallback to direct field access below
        }
        try {
            // Fallback: attempt to read the field directly.  This avoids issues
            // where PropertyDescriptor fails (e.g. due to modules or missing
            // bean conventions).  We set the field accessible in case it is
            // private.
            java.lang.reflect.Field field = item.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            Object value = field.get(item);
            return value == null ? "" : value.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Generate a stable identifier for this grid based on its column
     * definitions.  The identifier is a dash-separated list of property
     * names.  This helper is invoked from the constructors to populate
     * {@link #gridId}.  It is static so it can be called without a grid
     * instance.
     *
     * @param columns the list of column definitions
     * @return a dash-separated identifier string
     */
    private static String generateDefaultGridId(List<ColumnDefinition> columns) {
        if (columns == null || columns.isEmpty()) {
            return "grid";
        }
        return columns.stream()
                .map(ColumnDefinition::getPropertyName)
                .collect(java.util.stream.Collectors.joining("-"));
    }

    /**
     * Update the bar that displays currently applied filters as chips with
     * removable buttons.  Range filters (e.g. age.min and age.max) are
     * consolidated into a single chip where possible.  Called after
     * applying filters, clearing filters, or removing an individual filter.
     */
    private void updateAppliedFiltersBar() {
        appliedFiltersBar.removeAll();
        appliedFiltersBar.setSpacing(true);
        appliedFiltersBar.setAlignItems(Alignment.CENTER);
        // Ensure a bit of space below the chips so they don't butt up against the table
        // This margin separates the chips from the grid below
        appliedFiltersBar.getStyle().set("margin-bottom", "0.5rem");
        // Consolidate range filters
        java.util.Map<String, String> consolidated = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.endsWith(".min") || key.endsWith(".max") || key.endsWith(".from") || key.endsWith(".to")) {
                String base = key.substring(0, key.lastIndexOf('.'));
                String suffix = key.substring(key.lastIndexOf('.') + 1);
                String existing = consolidated.get(base);
                if (existing == null) existing = "";
                if (suffix.equals("min") || suffix.equals("from")) {
                    existing = value + (existing.isEmpty() ? "" : existing.startsWith("-") ? existing : "-" + existing);
                } else {
                    existing = (existing.endsWith("-") ? existing : existing + "-") + value;
                }
                consolidated.put(base, existing);
            } else {
                consolidated.put(key, value);
            }
        }
        // Create chips
        for (Map.Entry<String, String> entry : consolidated.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // Build label: use property name as is.  Could be improved with translation.
            String label = key + ": " + value;
            Span text = new Span(label);
            // Use a small button with an X icon to remove
            Button close = new Button(new com.vaadin.flow.component.icon.Icon(com.vaadin.flow.component.icon.VaadinIcon.CLOSE_SMALL));
            close.addClassNames("filter-chip-close");
            // Remove the background and make the button small so chips appear compact
            // LUMO_TERTIARY_INLINE removes the default button background and border
            // LUMO_SMALL reduces the overall height and padding
            close.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE,
                    com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);
            close.getElement().getStyle().set("margin-left", "0.25rem");
            // On click remove filter
            close.addClickListener(e -> {
                // Remove from filters map
                // For range keys, we need to remove both min and max keys
                if (filters.containsKey(key)) {
                    filters.remove(key);
                } else {
                    // remove min/max/from/to
                    filters.remove(key + ".min");
                    filters.remove(key + ".max");
                    filters.remove(key + ".from");
                    filters.remove(key + ".to");
                }
                // Reset corresponding component values
                filterComponents.forEach((compKey, comp) -> {
                    if (compKey.equals(key) || compKey.startsWith(key + ".")) {
                        if (comp instanceof TextField) {
                            ((TextField) comp).clear();
                        } else if (comp instanceof IntegerField) {
                            ((IntegerField) comp).clear();
                        } else if (comp instanceof com.vaadin.flow.component.datepicker.DatePicker) {
                            ((com.vaadin.flow.component.datepicker.DatePicker) comp).clear();
                        } else if (comp instanceof com.vaadin.flow.component.combobox.ComboBox<?> cb) {
                            cb.clear();
                        }
                    }
                });
                currentPage = 0;
                refresh();
                updateAppliedFiltersBar();
                // Notify callback
                if (filtersChangedCallback != null) {
                    filtersChangedCallback.accept(new java.util.HashMap<>(filters));
                }
            });
            HorizontalLayout chip = new HorizontalLayout(text, close);
            chip.setSpacing(false);
            chip.setAlignItems(Alignment.CENTER);
            // Reduce padding to make chips smaller
            chip.getStyle().set("padding", "0.1em 0.4em");
            chip.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
            chip.getStyle().set("border-radius", "var(--lumo-border-radius-s)");
            // Reduce font size slightly for a more compact chip appearance
            chip.getStyle().set("font-size", "var(--lumo-font-size-s)");
            appliedFiltersBar.add(chip);
        }
        // If no filters, hide bar
        appliedFiltersBar.setVisible(!filters.isEmpty());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        applyTranslations();
    }

    /**
     * Open a dialog allowing the user to toggle column visibility.  A checkbox
     * is rendered for each column; toggling it updates the corresponding
     * column's {@code visible} property.  The dialog header uses the
     * {@code grid.columns} translation key.
     */
    private void openColumnsDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("grid.columns"));
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setDraggable(true);
        dialog.setResizable(true);
        // Use a vertical layout for checkboxes
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();
        // Add a checkbox for each column
        for (int i = 0; i < gridColumns.size(); i++) {
            Grid.Column<T> col = gridColumns.get(i);
            ColumnDefinition def = columns.get(i);
            Checkbox cb = new Checkbox();
            cb.setLabel(getTranslation(def.getHeaderKey()));
            cb.setValue(col.isVisible());
            // When toggled, update column visibility
            cb.addValueChangeListener(ev -> col.setVisible(ev.getValue()));
            layout.add(cb);
        }
        // Add layout to dialog
        dialog.add(layout);
        // Add a close button in the footer
        Button close = new Button(getTranslation("grid.apply"), e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(close);
        dialog.open();
    }

    /**
     * Export the current grid data to CSV.  This method fetches all items
     * matching the current filters and builds a CSV file with translated
     * column headers.  The file is downloaded automatically via a
     * temporary anchor.
     */
    private void exportCsv() {
        // Ensure totalCount is up to date
        int count;
        if (usePageResult && pageResultService != null) {
            PageResult<T> result = pageResultService.fetchPage(0, pageSize, filters);
            count = result.getTotalCount();
        } else {
            count = countService.count(filters);
        }
        // Fetch all items (careful: may be large)
        java.util.List<T> allItems;
        if (usePageResult && pageResultService != null) {
            PageResult<T> result = pageResultService.fetchPage(0, count, filters);
            allItems = result.getItems();
        } else {
            allItems = fetchService.fetch(0, count, filters);
        }
        // Build CSV content
        StringBuilder sb = new StringBuilder();
        // Header row
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition def = columns.get(i);
            sb.append(getTranslation(def.getHeaderKey()));
            if (i < columns.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("\n");
        // Rows
        for (T item : allItems) {
            for (int i = 0; i < columns.size(); i++) {
                String val = getPropertyValue(item, columns.get(i).getPropertyName());
                // Escape quotes and commas by wrapping in quotes if necessary
                if (val != null) {
                    String escaped = val.replace("\"", "\"\"");
                    if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
                        sb.append("\"" + escaped + "\"");
                    } else {
                        sb.append(escaped);
                    }
                }
                if (i < columns.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        // Create resource
        String filename = gridId + ".csv";
        StreamResource resource = new StreamResource(filename, () -> new ByteArrayInputStream(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        resource.setContentType("text/csv;charset=UTF-8");
        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);
        // Trigger download via JS
        com.vaadin.flow.component.UI ui = UI.getCurrent();
        ui.add(downloadLink);
        ui.getPage().executeJs("var link = $0; link.click();", downloadLink.getElement());
        ui.remove(downloadLink);
    }

    /**
     * Stub for Excel export.  Currently shows a notification that Excel
     * export is not yet implemented.
     */
    private void exportXlsx() {
        com.vaadin.flow.component.notification.Notification.show(getTranslation("grid.exportExcel"));
    }

    /**
     * Prompt the user for a view name and save the current grid layout to
     * LocalStorage under that name.  After saving, refresh the view
     * selection list.
     */
    private void saveCurrentView() {
        UI ui = UI.getCurrent();
        // Use a prompt to get the view name; fallback to auto-generated if null
        ui.getPage().executeJs("return window.prompt($0, '')", getTranslation("grid.saveView")).then(String.class, name -> {
            if (name == null) {
                return;
            }
            name = name.trim();
            if (name.isEmpty()) {
                name = getTranslation("grid.defaultViewName") + System.currentTimeMillis();
            }
            // Build a simple JSON string representing the view: columns order and visibility
            StringBuilder json = new StringBuilder();
            json.append("{\"columns\":[");
            java.util.List<Grid.Column<T>> currentCols = grid.getColumns();
            for (int i = 0; i < currentCols.size(); i++) {
                Grid.Column<T> col = currentCols.get(i);
                String prop = col.getKey();
                boolean vis = col.isVisible();
                json.append("{\"property\":\"").append(prop).append("\",\"visible\":").append(vis);
                json.append("}");
                if (i < currentCols.size() - 1) json.append(",");
            }
            json.append("]}");
            String viewJson = json.toString();
            // Save to localStorage: get existing views, add or replace
            String storageKey = gridId + ".views";
            ui.getPage().executeJs(
                "var key = $0; var viewName = $1; var viewData = $2; " +
                "var views = JSON.parse(localStorage.getItem(key) || '{}'); " +
                "views[viewName] = JSON.parse(viewData); " +
                "localStorage.setItem(key, JSON.stringify(views));",
                storageKey, name, viewJson);
            // Reload view names
            loadViewNames();
        });
    }

    /**
     * Load saved view names from LocalStorage and populate the views sub menu
     * under the table configuration menu.  Existing view entries (other than
     * the "Save view" action) are removed and re-added for each call.
     */
    private void loadViewNames() {
        // Load saved view names from LocalStorage and populate the views sub menu
        if (!features.contains(GridFeature.VIEWS) || viewsRoot == null) {
            return;
        }
        UI ui = UI.getCurrent();
        String key = gridId + ".views";
        ui.getPage().executeJs(
            "var views = JSON.parse(localStorage.getItem($0) || '{}'); return Object.keys(views);", key)
            .then(JsonArray.class, names -> {
                var viewsSub = viewsRoot.getSubMenu();
                if (viewsSub == null) return;
                // Remove existing view items after the first (save view button)
                java.util.List<MenuItem> items = new java.util.ArrayList<>(viewsSub.getItems());
                // Keep the first item as Save View (index 0)
                for (int i = items.size() - 1; i >= 1; i--) {
                    viewsSub.remove(items.get(i));
                }
                // Add a menu item for each saved view
                for (int i = 0; i < names.length(); i++) {
                    String name = names.getString(i);
                    viewsSub.addItem(name, e -> applyView(name));
                }
            });
    }

    /**
     * Apply a saved view by name.  Retrieves the stored configuration from
     * LocalStorage and updates column order and visibility accordingly.
     * @param viewName the name of the view to apply
     */
    private void applyView(String viewName) {
        UI ui = UI.getCurrent();
        String key = gridId + ".views";
        ui.getPage().executeJs(
            "var views = JSON.parse(localStorage.getItem($0) || '{}'); return views[$1] || null;", key, viewName)
            .then(JsonObject.class, obj -> {
                if (obj == null) return;
                JsonArray colsArr = obj.getArray("columns");
                if (colsArr == null) return;
                // Build order and visibility lists
                java.util.List<Grid.Column<T>> newOrder = new java.util.ArrayList<>();
                for (int i = 0; i < colsArr.length(); i++) {
                    JsonObject colObj = colsArr.getObject(i);
                    String prop = colObj.getString("property");
                    boolean vis = colObj.getBoolean("visible");
                    // Find column by key
                    for (Grid.Column<T> col : gridColumns) {
                        if (prop.equals(col.getKey())) {
                            newOrder.add(col);
                            col.setVisible(vis);
                            break;
                        }
                    }
                }
                // Apply column order
                if (!newOrder.isEmpty()) {
                    grid.setColumnOrder(newOrder);
                }
            });
    }

    /**
     * Configure whether the grid should expand to fill the available space. When set
     * to {@code true}, the grid will grow to take up remaining space in its parent
     * layout. When set to {@code false}, the grid height is limited by
     * {@link #minHeight} and {@link #maxHeight}.
     *
     * @param expand whether the grid should expand vertically
     */
    public void setExpandGrid(boolean expand) {
        this.expandGrid = expand;
        applyGridHeightStyle();
    }

    /**
     * Set a minimum height for the grid when {@code expandGrid} is disabled. The value
     * should be a valid CSS height, such as "200px", "20rem" or "30vh". Passing
     * {@code null} removes any previously set minimum height.
     *
     * @param minHeight CSS value for the minimum height or {@code null} to remove
     */
    public void setMinHeight(String minHeight) {
        this.minHeight = minHeight;
        applyGridHeightStyle();
    }

    /**
     * Set a maximum height for the grid when {@code expandGrid} is disabled. The value
     * should be a valid CSS height, such as "400px", "50vh" or "30rem". Passing
     * {@code null} removes any previously set maximum height.
     *
     * @param maxHeight CSS value for the maximum height or {@code null} to remove
     */
    public void setMaxHeight(String maxHeight) {
        this.maxHeight = maxHeight;
        applyGridHeightStyle();
    }

    /**
     * Apply the current height configuration to the grid component. This method
     * sets the grid's flex-grow property, minimum and maximum heights and
     * vertical overflow based on the values of {@link #expandGrid},
     * {@link #minHeight} and {@link #maxHeight}. It should be called after
     * adding the grid to a layout and whenever any of these properties change.
     */
    private void applyGridHeightStyle() {
        if (grid == null) {
            return;
        }
        if (expandGrid) {
            // When expanding, allow the grid to grow and remove height constraints
            grid.getElement().getStyle().set("flex-grow", "1");
            grid.getElement().getStyle().remove("min-height");
            grid.getElement().getStyle().remove("max-height");
            // Always allow scroll bars when content exceeds available height
            grid.getElement().getStyle().set("overflow-y", "auto");
        } else {
            // Do not expand vertically
            grid.getElement().getStyle().set("flex-grow", "0");
            if (minHeight != null) {
                grid.getElement().getStyle().set("min-height", minHeight);
            } else {
                grid.getElement().getStyle().remove("min-height");
            }
            if (maxHeight != null) {
                grid.getElement().getStyle().set("max-height", maxHeight);
            } else {
                grid.getElement().getStyle().remove("max-height");
            }
            grid.getElement().getStyle().set("overflow-y", "auto");
        }
    }
}
