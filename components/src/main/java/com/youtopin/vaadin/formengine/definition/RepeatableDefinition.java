package com.youtopin.vaadin.formengine.definition;

import com.youtopin.vaadin.formengine.RepeatableTitleGenerator;
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
    private final String itemTitleKey;
    private final int itemTitleOffset;
    private final RepeatableTitleGenerator titleGenerator;
    private final boolean allowReorder;
    private final boolean allowDuplicate;

    public RepeatableDefinition(boolean enabled,
                                UiRepeatable.RepeatableMode mode,
                                int min,
                                int max,
                                String uniqueBy,
                                String summaryTemplate,
                                String itemTitleKey,
                                int itemTitleOffset,
                                RepeatableTitleGenerator titleGenerator,
                                boolean allowReorder,
                                boolean allowDuplicate) {
        this.enabled = enabled;
        this.mode = mode;
        this.min = min;
        this.max = max;
        this.uniqueBy = uniqueBy == null ? "" : uniqueBy;
        this.summaryTemplate = summaryTemplate == null ? "" : summaryTemplate;
        this.itemTitleKey = itemTitleKey == null ? "" : itemTitleKey;
        this.itemTitleOffset = itemTitleOffset;
        this.titleGenerator = titleGenerator == null ? new RepeatableTitleGenerator.Default() : titleGenerator;
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

    public String getItemTitleKey() {
        return itemTitleKey;
    }

    public int getItemTitleOffset() {
        return itemTitleOffset;
    }

    public RepeatableTitleGenerator getTitleGenerator() {
        return titleGenerator;
    }

    public boolean isAllowReorder() {
        return allowReorder;
    }

    public boolean isAllowDuplicate() {
        return allowDuplicate;
    }
}
