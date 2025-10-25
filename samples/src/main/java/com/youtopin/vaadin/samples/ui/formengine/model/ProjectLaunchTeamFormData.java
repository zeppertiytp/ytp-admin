package com.youtopin.vaadin.samples.ui.formengine.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Stores team information captured during the wizard flow.
 */
public class ProjectLaunchTeamFormData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String leadName = "";
    private String leadEmail = "";
    private Integer teamSize;
    private String workModel = "";
    private String communicationChannel = "";

    public String getLeadName() {
        return leadName;
    }

    public void setLeadName(String leadName) {
        this.leadName = Objects.toString(leadName, "");
    }

    public String getLeadEmail() {
        return leadEmail;
    }

    public void setLeadEmail(String leadEmail) {
        this.leadEmail = Objects.toString(leadEmail, "");
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        if (teamSize == null || teamSize < 0) {
            this.teamSize = null;
        } else {
            this.teamSize = teamSize;
        }
    }

    public String getWorkModel() {
        return workModel;
    }

    public void setWorkModel(String workModel) {
        this.workModel = Objects.toString(workModel, "");
    }

    public String getCommunicationChannel() {
        return communicationChannel;
    }

    public void setCommunicationChannel(String communicationChannel) {
        this.communicationChannel = Objects.toString(communicationChannel, "");
    }
}
