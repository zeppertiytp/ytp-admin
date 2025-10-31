package com.youtopin.vaadin.samples.application.dynamicrepeatable.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds the repeatable rows alongside a schema snapshot for conditional rendering.
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

    public List<DynamicEntryRow> getEntriesSnapshot() {
        return Collections.unmodifiableList(entries);
    }

    public void ensureEntryCount(int desired) {
        int target = Math.max(1, Math.min(desired, 20));
        while (entries.size() < target) {
            entries.add(new DynamicEntryRow());
        }
        while (entries.size() > target) {
            entries.remove(entries.size() - 1);
        }
    }

    public void syncWithSchema(DynamicFieldSchema sourceSchema) {
        schema.copyFrom(sourceSchema);
        ensureEntryCount(schema.getEntryCount());
        Set<String> keys = new LinkedHashSet<>(schema.indexByKey().keySet());
        if (!keys.contains(schema.getSummaryFieldKey()) && !keys.isEmpty()) {
            schema.setSummaryFieldKey(keys.iterator().next());
        }
        for (DynamicEntryRow entry : entries) {
            entry.getValues().ensureKeys(keys);
        }
    }
}
