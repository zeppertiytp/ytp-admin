package com.youtopin.vaadin.samples.application.dynamicrepeatable.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a single repeatable entry collected during the second wizard step.
 */
public class DynamicEntryRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String label = "";
    private String owner = "";
    private String email = "";
    private String phone = "";
    private String notes = "";

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label == null ? "" : label.trim();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner == null ? "" : owner.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? "" : email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? "" : phone.trim();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes.trim();
    }
}
