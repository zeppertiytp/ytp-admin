package com.youtopin.vaadin.samples.application.tour.model;

import java.io.Serializable;

/**
 * Simple bean backing placeholder steps in the outbound tour wizard.
 */
public class OutboundTourPlaceholderFormData implements Serializable {

    private String notes = "";

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes.trim();
    }
}
