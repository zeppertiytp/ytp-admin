package com.youtopin.vaadin.samples.application.dynamicrepeatable.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the repeatable entries while mirroring the schema configuration to drive visibility rules.
 */
public class DynamicEntryCollectionFormData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final DynamicFieldSchema schema = new DynamicFieldSchema();
    private final List<DynamicEntryRow> entries = new ArrayList<>();

    public DynamicFieldSchema getSchema() {
        return schema;
    }

    public List<DynamicEntryRow> getEntries() {
        return entries;
    }

    public void ensureEntryCount(int desired) {
        int target = Math.max(1, Math.min(desired, 10));
        while (entries.size() < target) {
            entries.add(new DynamicEntryRow());
        }
        while (entries.size() > target) {
            entries.remove(entries.size() - 1);
        }
    }

    public List<DynamicEntryRow> getEntriesSnapshot() {
        return Collections.unmodifiableList(entries);
    }
}
