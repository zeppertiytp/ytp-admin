package com.youtopin.vaadin.samples.application.dynamicrepeatable.model;

import java.io.Serial;

/**
 * Form bean backing the schema configuration step.
 */
public class DynamicFieldSchemaFormData extends DynamicFieldSchema {

    @Serial
    private static final long serialVersionUID = 1L;

    public void sanitiseFieldKeys() {
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
        int counter = 1;
        for (DynamicFieldBlueprint blueprint : getFields()) {
            String candidate = DynamicFieldBlueprint.normaliseKey(blueprint.getFieldKey());
            while (candidate.isBlank() || seen.contains(candidate)) {
                candidate = "field" + counter++;
            }
            blueprint.setFieldKey(candidate);
            seen.add(candidate);
        }
        ensureSeedField();
        if (seen.isEmpty()) {
            getFields().forEach(blueprint -> seen.add(blueprint.getFieldKey()));
        }
        if (!seen.contains(getSummaryFieldKey()) && !seen.isEmpty()) {
            setSummaryFieldKey(seen.iterator().next());
        }
    }
}
