package com.youtopin.vaadin.samples.application.dynamicrepeatable.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a single repeatable entry with dynamic field values.
 */
public class DynamicEntryRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final DynamicFieldValues values = new DynamicFieldValues();

    public DynamicFieldValues getValues() {
        return values;
    }

    public DynamicEntryRow copy() {
        DynamicEntryRow copy = new DynamicEntryRow();
        copy.getValues().asMap().putAll(values.asMap());
        return copy;
    }
}
