package com.youtopin.vaadin.samples.application.dynamicrepeatable.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Bean backing the wizard's schema configuration step.
 */
public class DynamicFieldSchemaFormData extends DynamicFieldSchema implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int entryCount = 3;

    public int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = Math.max(1, Math.min(entryCount, 10));
    }
}
