package com.youtopin.vaadin.samples.application.dynamicrepeatable;

import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicEntryCollectionFormData;
import com.youtopin.vaadin.samples.application.dynamicrepeatable.model.DynamicFieldSchemaFormData;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stores the state for the dynamic repeatable wizard inside the Vaadin session.
 */
public class DynamicRepeatableWizardState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String STEP_SCHEMA = "dynamic-schema";
    public static final String STEP_ENTRIES = "dynamic-entries";

    private final DynamicFieldSchemaFormData schema = new DynamicFieldSchemaFormData();
    private final DynamicEntryCollectionFormData entries = new DynamicEntryCollectionFormData();
    private final LinkedHashSet<String> completedSteps = new LinkedHashSet<>();

    private String currentStepId = STEP_SCHEMA;

    public DynamicRepeatableWizardState() {
        syncEntriesWithSchema();
    }

    public DynamicFieldSchemaFormData getSchema() {
        return schema;
    }

    public DynamicEntryCollectionFormData getEntries() {
        return entries;
    }

    public void syncEntriesWithSchema() {
        entries.getSchema().copyFrom(schema);
        entries.ensureEntryCount(schema.getEntryCount());
    }

    public Set<String> getCompletedSteps() {
        return Collections.unmodifiableSet(completedSteps);
    }

    public void markStepCompleted(String stepId) {
        if (stepId != null && !stepId.isBlank()) {
            completedSteps.add(stepId);
        }
    }

    public void setCompletedSteps(Set<String> steps) {
        completedSteps.clear();
        if (steps != null) {
            completedSteps.addAll(steps);
        }
    }

    public String getCurrentStepId() {
        return currentStepId;
    }

    public void setCurrentStepId(String currentStepId) {
        if (currentStepId != null && !currentStepId.isBlank()) {
            this.currentStepId = currentStepId;
        }
    }

    public void reset() {
        completedSteps.clear();
        currentStepId = STEP_SCHEMA;
        schema.setCollectionTitle("");
        schema.setRequireOwner(true);
        schema.setIncludeEmail(true);
        schema.setIncludePhone(false);
        schema.setIncludeNotes(false);
        schema.setEntryCount(3);
        entries.getSchema().copyFrom(schema);
        entries.getEntries().clear();
        entries.ensureEntryCount(schema.getEntryCount());
    }
}
