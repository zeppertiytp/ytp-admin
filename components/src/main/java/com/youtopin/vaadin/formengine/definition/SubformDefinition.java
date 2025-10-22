package com.youtopin.vaadin.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiSubform;

/**
 * Definition for subform metadata.
 */
public final class SubformDefinition {

    private final boolean enabled;
    private final String formId;
    private final UiSubform.SubformMode mode;
    private final boolean autoOpen;

    public SubformDefinition(boolean enabled, String formId, UiSubform.SubformMode mode, boolean autoOpen) {
        this.enabled = enabled;
        this.formId = formId == null ? "" : formId;
        this.mode = mode;
        this.autoOpen = autoOpen;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getFormId() {
        return formId;
    }

    public UiSubform.SubformMode getMode() {
        return mode;
    }

    public boolean isAutoOpen() {
        return autoOpen;
    }
}
