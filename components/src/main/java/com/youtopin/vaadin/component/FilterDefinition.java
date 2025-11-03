package com.youtopin.vaadin.component;

import lombok.Getter;

/**
 * Defines a property that can be filtered in a {@link FilterablePaginatedGrid}.
 * Unlike {@link ColumnDefinition}, a filter definition may correspond to a
 * property that is not shown as a column in the grid.  Each definition
 * specifies the property name, a label for the filter (used as the form field
 * label), and the property type which determines what kind of input
 * component is used for filtering.  The label should be a translation
 * key that can be resolved via {@code Component.getTranslation()} in the
 * consuming view.
 */
@Getter
public class FilterDefinition {
    public enum FilterType {
        TEXT,
        NUMBER_RANGE,
        DATE_RANGE,
        SELECT
    }

    private final String propertyName;
    private final String labelKey;
    private final FilterType type;
    private final Class<?> propertyType;
    private final java.util.List<String> options;
    private final String dependsOn;

    /**
     * Constructor for text, number or date range filters without options or dependencies.
     */
    public FilterDefinition(String propertyName, String labelKey, FilterType type, Class<?> propertyType) {
        this(propertyName, labelKey, type, propertyType, null, null);
    }

    /**
     * Constructor for select filters with options and optional dependency.
     */
    public FilterDefinition(String propertyName, String labelKey, java.util.List<String> options, String dependsOn) {
        this(propertyName, labelKey, FilterType.SELECT, String.class, options, dependsOn);
    }

    private FilterDefinition(String propertyName, String labelKey, FilterType type, Class<?> propertyType,
                             java.util.List<String> options, String dependsOn) {
        this.propertyName = propertyName;
        this.labelKey = labelKey;
        this.type = type;
        this.propertyType = propertyType;
        this.options = options;
        this.dependsOn = dependsOn;
    }

}