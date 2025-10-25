package com.youtopin.vaadin.samples.ui.formengine.model;

import java.util.Objects;

/**
 * Backing bean for the agenda builder sample.
 */
public class AgendaBuilderFormData {

    private final Agenda agenda = new Agenda();

    public Agenda getAgenda() {
        return agenda;
    }

    public static class Agenda {
        private String sessionName = "";
        private String facilitator = "";
        private String location = "";
        private String summary = "";
        private String segment1 = "";
        private String segment2 = "";
        private String segment3 = "";
        private String segment4 = "";

        public String getSessionName() {
            return sessionName;
        }

        public void setSessionName(String sessionName) {
            this.sessionName = Objects.toString(sessionName, "");
        }

        public String getFacilitator() {
            return facilitator;
        }

        public void setFacilitator(String facilitator) {
            this.facilitator = Objects.toString(facilitator, "");
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = Objects.toString(location, "");
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = Objects.toString(summary, "");
        }

        public String getSegment1() {
            return segment1;
        }

        public void setSegment1(String segment1) {
            this.segment1 = Objects.toString(segment1, "");
        }

        public String getSegment2() {
            return segment2;
        }

        public void setSegment2(String segment2) {
            this.segment2 = Objects.toString(segment2, "");
        }

        public String getSegment3() {
            return segment3;
        }

        public void setSegment3(String segment3) {
            this.segment3 = Objects.toString(segment3, "");
        }

        public String getSegment4() {
            return segment4;
        }

        public void setSegment4(String segment4) {
            this.segment4 = Objects.toString(segment4, "");
        }
    }
}
