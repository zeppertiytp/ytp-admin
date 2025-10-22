package com.youtopin.vaadin.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiRepeatable;

/**
 * Definition of repeatable configuration.
 */
public final class RepeatableDefinition {

    private final boolean enabled;
    private final UiRepeatable.RepeatableMode mode;
    private final int min;
    private final int max;
    private final String uniqueBy;
    private final String summaryTemplate;
    private final boolean allowReorder;
    private final boolean allowDuplicate;

    public RepeatableDefinition(boolean enabled,
                                UiRepeatable.RepeatableMode mode,
                                int min,
                                int max,
                                String uniqueBy,
                                String summaryTemplate,
                                boolean allowReorder,
                                boolean allowDuplicate) {
        this.enabled = enabled;
        this.mode = mode;
        this.min = min;
        this.max = max;
        this.uniqueBy = uniqueBy == null ? "" : uniqueBy;
        this.summaryTemplate = summaryTemplate == null ? "" : summaryTemplate;
        this.allowReorder = allowReorder;
        this.allowDuplicate = allowDuplicate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public UiRepeatable.RepeatableMode getMode() {
        return mode;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getUniqueBy() {
        return uniqueBy;
    }

    public String getSummaryTemplate() {
        return summaryTemplate;
    }

    public boolean isAllowReorder() {
        return allowReorder;
    }

    public boolean isAllowDuplicate() {
        return allowDuplicate;
    }
}
