package com.youtopin.vaadin.component;

import lombok.Getter;

/**
 * Describes a single column in a filterable paginated grid.  Each column
 * consists of a property name to read from the data item, a header key
 * for the column caption, and the type of the property.  The type
 * determines the filtering component used (for example, text field for
 * strings or number field for numbers).
 */
@Getter
public class ColumnDefinition {
    private final String propertyName;
    private final String headerKey;
    private final Class<?> propertyType;

    public ColumnDefinition(String propertyName, String headerKey, Class<?> propertyType) {
        this.propertyName = propertyName;
        this.headerKey = headerKey;
        this.propertyType = propertyType;
    }

}