package com.youtopin.vaadin.formengine.definition;

import java.util.List;
import java.util.Objects;

/**
 * Definition of a field group inside a section.
 */
public final class GroupDefinition {

    private final String id;
    private final String titleKey;
    private final int columns;
    private final RepeatableDefinition repeatableDefinition;
    private final SubformDefinition subformDefinition;
    private final String readOnlyWhen;
    private final List<FieldDefinition> fields;
    private final List<GroupDefinition> entryGroups;

    public GroupDefinition(String id,
                           String titleKey,
                           int columns,
                           RepeatableDefinition repeatableDefinition,
                           SubformDefinition subformDefinition,
                           String readOnlyWhen,
                           List<FieldDefinition> fields,
                           List<GroupDefinition> entryGroups) {
        this.id = Objects.requireNonNull(id, "id");
        this.titleKey = titleKey == null ? "" : titleKey;
        this.columns = columns;
        this.repeatableDefinition = repeatableDefinition;
        this.subformDefinition = subformDefinition;
        this.readOnlyWhen = readOnlyWhen == null ? "" : readOnlyWhen;
        this.fields = List.copyOf(fields);
        this.entryGroups = entryGroups == null ? List.of() : List.copyOf(entryGroups);
    }

    public String getId() {
        return id;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public int getColumns() {
        return columns;
    }

    public RepeatableDefinition getRepeatableDefinition() {
        return repeatableDefinition;
    }

    public SubformDefinition getSubformDefinition() {
        return subformDefinition;
    }

    public String getReadOnlyWhen() {
        return readOnlyWhen;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public List<GroupDefinition> getEntryGroups() {
        return entryGroups;
    }
}
