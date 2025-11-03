package com.youtopin.vaadin.formengine.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Definition of a field group inside a section.
 */
public class GroupDefinition implements Cloneable {

    private String id;
    private String titleKey;
    private int columns;
    private RepeatableDefinition repeatableDefinition;
    private SubformDefinition subformDefinition;
    private String readOnlyWhen;
    private List<FieldDefinition> fields;
    private List<GroupDefinition> entryGroups;

    public GroupDefinition(String id,
                           String titleKey,
                           int columns,
                           RepeatableDefinition repeatableDefinition,
                           SubformDefinition subformDefinition,
                           String readOnlyWhen,
                           List<FieldDefinition> fields,
                           List<GroupDefinition> entryGroups) {
        setId(id);
        setTitleKey(titleKey);
        setColumns(columns);
        setRepeatableDefinition(repeatableDefinition);
        setSubformDefinition(subformDefinition);
        setReadOnlyWhen(readOnlyWhen);
        setFields(fields);
        setEntryGroups(entryGroups);
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
        return Collections.unmodifiableList(fields);
    }

    public List<GroupDefinition> getEntryGroups() {
        return Collections.unmodifiableList(entryGroups);
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = normalize(titleKey);
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setRepeatableDefinition(RepeatableDefinition repeatableDefinition) {
        this.repeatableDefinition = repeatableDefinition;
    }

    public void setSubformDefinition(SubformDefinition subformDefinition) {
        this.subformDefinition = subformDefinition;
    }

    public void setReadOnlyWhen(String readOnlyWhen) {
        this.readOnlyWhen = normalize(readOnlyWhen);
    }

    public void setFields(List<FieldDefinition> fields) {
        Objects.requireNonNull(fields, "fields");
        this.fields = List.copyOf(fields);
    }

    public void setEntryGroups(List<GroupDefinition> entryGroups) {
        if (entryGroups == null) {
            this.entryGroups = List.of();
        } else {
            this.entryGroups = List.copyOf(entryGroups);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    @Override
    public GroupDefinition clone() {
        List<FieldDefinition> clonedFields = fields.stream()
                .map(field -> field == null ? null : field.clone())
                .toList();
        List<GroupDefinition> clonedEntryGroups = entryGroups.stream()
                .map(group -> group == null ? null : group.clone())
                .toList();
        return new GroupDefinition(id, titleKey, columns,
                repeatableDefinition == null ? null : repeatableDefinition.clone(),
                subformDefinition == null ? null : subformDefinition.clone(),
                readOnlyWhen, clonedFields, clonedEntryGroups);
    }
}
