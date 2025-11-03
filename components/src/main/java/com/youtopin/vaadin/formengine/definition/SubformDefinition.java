package com.youtopin.vaadin.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiSubform;

/**
 * Definition for subform metadata.
 */
public class SubformDefinition implements Cloneable {

    private boolean enabled;
    private String formId;
    private UiSubform.SubformMode mode;
    private boolean autoOpen;

    public SubformDefinition(boolean enabled, String formId, UiSubform.SubformMode mode, boolean autoOpen) {
        setEnabled(enabled);
        setFormId(formId);
        setMode(mode);
        setAutoOpen(autoOpen);
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setFormId(String formId) {
        this.formId = formId == null ? "" : formId;
    }

    public void setMode(UiSubform.SubformMode mode) {
        this.mode = mode;
    }

    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }

    @Override
    public SubformDefinition clone() {
        return new SubformDefinition(enabled, formId, mode, autoOpen);
    }
}
