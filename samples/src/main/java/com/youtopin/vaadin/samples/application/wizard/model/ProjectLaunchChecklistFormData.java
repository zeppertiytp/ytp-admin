package com.youtopin.vaadin.samples.application.wizard.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import lombok.Getter;

/**
 * Stores launch readiness information from the final wizard step.
 */
@Getter
public class ProjectLaunchChecklistFormData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate launchDate;
    private String riskLevel = "";
    private boolean communicationReady;
    private String supportContact = "";
    private String notes = "";

    public void setLaunchDate(LocalDate launchDate) {
        this.launchDate = launchDate;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = Objects.toString(riskLevel, "");
    }

    public void setCommunicationReady(Boolean communicationReady) {
        this.communicationReady = Boolean.TRUE.equals(communicationReady);
    }

    public void setSupportContact(String supportContact) {
        this.supportContact = Objects.toString(supportContact, "");
    }

    public void setNotes(String notes) {
        this.notes = Objects.toString(notes, "");
    }
}
