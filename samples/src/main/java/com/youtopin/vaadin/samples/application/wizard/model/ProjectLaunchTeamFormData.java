package com.youtopin.vaadin.samples.application.wizard.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;

/**
 * Stores team information captured during the wizard flow.
 */
@Getter
public class ProjectLaunchTeamFormData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String leadName = "";
    private String leadEmail = "";
    private Integer teamSize;
    private String workModel = "";
    private String communicationChannel = "";

    public void setLeadName(String leadName) {
        this.leadName = Objects.toString(leadName, "");
    }

    public void setLeadEmail(String leadEmail) {
        this.leadEmail = Objects.toString(leadEmail, "");
    }

    public void setTeamSize(Integer teamSize) {
        if (teamSize == null || teamSize < 0) {
            this.teamSize = null;
        } else {
            this.teamSize = teamSize;
        }
    }

    public void setWorkModel(String workModel) {
        this.workModel = Objects.toString(workModel, "");
    }

    public void setCommunicationChannel(String communicationChannel) {
        this.communicationChannel = Objects.toString(communicationChannel, "");
    }
}
