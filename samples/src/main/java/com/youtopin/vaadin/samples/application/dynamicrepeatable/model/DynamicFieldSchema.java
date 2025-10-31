package com.youtopin.vaadin.samples.application.dynamicrepeatable.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Captures the collection metadata and the list of dynamic field blueprints that drive
 * the second wizard step.
 */
public class DynamicFieldSchema implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String collectionTitle = "";
    private int entryCount = 3;
    private final List<DynamicFieldBlueprint> fields = new ArrayList<>();
    private String summaryFieldKey = "";

    public DynamicFieldSchema() {
        ensureSeedField();
    }

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle == null ? "" : collectionTitle.trim();
    }

    public int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = Math.max(1, Math.min(entryCount, 20));
    }

    public List<DynamicFieldBlueprint> getFields() {
        return fields;
    }

    public List<DynamicFieldBlueprint> getFieldSnapshot() {
        return Collections.unmodifiableList(fields);
    }

    public Map<String, DynamicFieldBlueprint> indexByKey() {
        Map<String, DynamicFieldBlueprint> map = new LinkedHashMap<>();
        for (DynamicFieldBlueprint blueprint : fields) {
            if (blueprint.getFieldKey().isBlank()) {
                continue;
            }
            map.put(blueprint.getFieldKey(), blueprint);
        }
        return map;
    }

    public void ensureSeedField() {
        if (fields.isEmpty()) {
            fields.add(new DynamicFieldBlueprint("name", "Name", DynamicFieldBlueprint.FieldType.TEXT, true));
        }
        if (summaryFieldKey == null || summaryFieldKey.isBlank()) {
            summaryFieldKey = fields.get(0).getFieldKey();
        }
    }

    public void copyFrom(DynamicFieldSchema other) {
        if (other == null) {
            return;
        }
        setCollectionTitle(other.getCollectionTitle());
        setEntryCount(other.getEntryCount());
        setSummaryFieldKey(other.getSummaryFieldKey());
        fields.clear();
        for (DynamicFieldBlueprint blueprint : other.getFields()) {
            DynamicFieldBlueprint copy = new DynamicFieldBlueprint();
            copy.copyFrom(blueprint);
            fields.add(copy);
        }
        ensureSeedField();
    }

    public String getSummaryFieldKey() {
        return summaryFieldKey == null ? "" : summaryFieldKey;
    }

    public void setSummaryFieldKey(String summaryFieldKey) {
        this.summaryFieldKey = summaryFieldKey == null ? "" : summaryFieldKey.trim();
    }

    @Override
    public String toString() {
        return "DynamicFieldSchema{" +
                "collectionTitle='" + collectionTitle + '\'' +
                ", entryCount=" + entryCount +
                ", summaryFieldKey='" + summaryFieldKey + '\'' +
                ", fields=" + fields +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DynamicFieldSchema that)) {
            return false;
        }
        return entryCount == that.entryCount
                && Objects.equals(collectionTitle, that.collectionTitle)
                && Objects.equals(summaryFieldKey, that.summaryFieldKey)
                && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionTitle, entryCount, summaryFieldKey, fields);
    }
}
