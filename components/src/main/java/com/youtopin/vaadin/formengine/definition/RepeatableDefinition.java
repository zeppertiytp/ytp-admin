package com.youtopin.vaadin.formengine.definition;

import com.youtopin.vaadin.formengine.RepeatableTitleGenerator;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;

/**
 * Definition of repeatable configuration.
 */
public class RepeatableDefinition implements Cloneable {

    private boolean enabled;
    private UiRepeatable.RepeatableMode mode;
    private int min;
    private int max;
    private String uniqueBy;
    private String summaryTemplate;
    private String itemTitleKey;
    private int itemTitleOffset;
    private RepeatableTitleGenerator titleGenerator;
    private boolean allowReorder;
    private boolean allowDuplicate;
    private boolean allowManualAdd;
    private boolean allowManualRemove;

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
                                boolean allowDuplicate,
                                boolean allowManualAdd,
                                boolean allowManualRemove) {
        setEnabled(enabled);
        setMode(mode);
        setMin(min);
        setMax(max);
        setUniqueBy(uniqueBy);
        setSummaryTemplate(summaryTemplate);
        setItemTitleKey(itemTitleKey);
        setItemTitleOffset(itemTitleOffset);
        setTitleGenerator(titleGenerator);
        setAllowReorder(allowReorder);
        setAllowDuplicate(allowDuplicate);
        setAllowManualAdd(allowManualAdd);
        setAllowManualRemove(allowManualRemove);
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

    public boolean isAllowManualAdd() {
        return allowManualAdd;
    }

    public boolean isAllowManualRemove() {
        return allowManualRemove;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMode(UiRepeatable.RepeatableMode mode) {
        this.mode = mode;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setUniqueBy(String uniqueBy) {
        this.uniqueBy = normalize(uniqueBy);
    }

    public void setSummaryTemplate(String summaryTemplate) {
        this.summaryTemplate = normalize(summaryTemplate);
    }

    public void setItemTitleKey(String itemTitleKey) {
        this.itemTitleKey = normalize(itemTitleKey);
    }

    public void setItemTitleOffset(int itemTitleOffset) {
        this.itemTitleOffset = itemTitleOffset;
    }

    public void setTitleGenerator(RepeatableTitleGenerator titleGenerator) {
        this.titleGenerator = titleGenerator == null ? new RepeatableTitleGenerator.Default() : titleGenerator;
    }

    public void setAllowReorder(boolean allowReorder) {
        this.allowReorder = allowReorder;
    }

    public void setAllowDuplicate(boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
    }

    public void setAllowManualAdd(boolean allowManualAdd) {
        this.allowManualAdd = allowManualAdd;
    }

    public void setAllowManualRemove(boolean allowManualRemove) {
        this.allowManualRemove = allowManualRemove;
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    @Override
    public RepeatableDefinition clone() {
        return new RepeatableDefinition(enabled, mode, min, max, uniqueBy, summaryTemplate, itemTitleKey,
                itemTitleOffset, titleGenerator, allowReorder, allowDuplicate, allowManualAdd, allowManualRemove);
    }
}
