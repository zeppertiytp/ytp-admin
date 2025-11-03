package com.youtopin.vaadin.samples.application.wizard.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import lombok.Getter;

/**
 * Stores the basic project details captured in the wizard's first step.
 */
@Getter
public class ProjectLaunchBasicsFormData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name = "";
    private String owner = "";
    private String summary = "";
    private LocalDate kickoffDate;
    private Integer durationWeeks;

    public void setName(String name) {
        this.name = Objects.toString(name, "");
    }

    public void setOwner(String owner) {
        this.owner = Objects.toString(owner, "");
    }

    public void setSummary(String summary) {
        this.summary = Objects.toString(summary, "");
    }

    public void setKickoffDate(LocalDate kickoffDate) {
        this.kickoffDate = kickoffDate;
    }

    public void setDurationWeeks(Integer durationWeeks) {
        if (durationWeeks == null || durationWeeks < 0) {
            this.durationWeeks = null;
        } else {
            this.durationWeeks = durationWeeks;
        }
    }
}
