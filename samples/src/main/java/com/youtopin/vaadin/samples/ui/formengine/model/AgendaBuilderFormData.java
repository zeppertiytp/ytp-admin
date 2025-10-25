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
        private final java.util.List<Segment> segments = new java.util.ArrayList<>();

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

        public java.util.List<Segment> getSegments() {
            return segments;
        }

        public void setSegments(java.util.List<Segment> segments) {
            this.segments.clear();
            if (segments != null) {
                segments.stream()
                        .filter(Objects::nonNull)
                        .map(Segment::copyOf)
                        .forEach(this.segments::add);
            }
        }

        public static class Segment {
            private String title = "";
            private String description = "";

            public Segment() {
            }

            public Segment(String title, String description) {
                this.title = Objects.toString(title, "");
                this.description = Objects.toString(description, "");
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = Objects.toString(title, "");
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = Objects.toString(description, "");
            }

            private static Segment copyOf(Segment source) {
                if (source == null) {
                    return new Segment();
                }
                return new Segment(source.getTitle(), source.getDescription());
            }
        }
    }
}
