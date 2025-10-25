package com.youtopin.vaadin.samples.ui.formengine.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Bean backing the access policy designer sample.
 */
public class AccessPolicyFormData {

    private final Policy policy = new Policy();
    private final Schedule schedule = new Schedule();
    private final Review review = new Review();

    public Policy getPolicy() {
        return policy;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public Review getReview() {
        return review;
    }

    public static class Policy {
        private String name = "";
        private String effect = "";
        private String description = "";
        private final List<String> resources = new ArrayList<>();
        private final List<String> actions = new ArrayList<>();
        private final List<Condition> conditions = new ArrayList<>();
        private Map<String, Object> delegation = new LinkedHashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = Objects.toString(name, "");
        }

        public String getEffect() {
            return effect;
        }

        public void setEffect(String effect) {
            this.effect = Objects.toString(effect, "");
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = Objects.toString(description, "");
        }

        public List<String> getResources() {
            return resources;
        }

        public void setResources(List<String> resources) {
            this.resources.clear();
            if (resources != null) {
                this.resources.addAll(resources);
            }
        }

        public List<String> getActions() {
            return actions;
        }

        public void setActions(List<String> actions) {
            this.actions.clear();
            if (actions != null) {
                this.actions.addAll(actions);
            }
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public void setConditions(List<Condition> conditions) {
            this.conditions.clear();
            if (conditions != null) {
                conditions.stream()
                        .filter(Objects::nonNull)
                        .map(Condition::copyOf)
                        .forEach(this.conditions::add);
            }
        }

        public Map<String, Object> getDelegation() {
            return delegation;
        }

        public void setDelegation(Map<String, Object> delegation) {
            this.delegation = delegation == null ? new LinkedHashMap<>() : new LinkedHashMap<>(delegation);
        }

        public static class Condition {
            private String attribute = "";
            private String operator = "";
            private String value = "";

            public Condition() {
            }

            public Condition(String attribute, String operator, String value) {
                this.attribute = Objects.toString(attribute, "");
                this.operator = Objects.toString(operator, "");
                this.value = Objects.toString(value, "");
            }

            public String getAttribute() {
                return attribute;
            }

            public void setAttribute(String attribute) {
                this.attribute = Objects.toString(attribute, "");
            }

            public String getOperator() {
                return operator;
            }

            public void setOperator(String operator) {
                this.operator = Objects.toString(operator, "");
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = Objects.toString(value, "");
            }

            private static Condition copyOf(Condition source) {
                if (source == null) {
                    return new Condition();
                }
                return new Condition(source.getAttribute(), source.getOperator(), source.getValue());
            }
        }
    }

    public static class Schedule {
        private LocalDateTime start;
        private LocalDateTime end;
        private String timezone = "";

        public LocalDateTime getStart() {
            return start;
        }

        public void setStart(LocalDateTime start) {
            this.start = start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public void setEnd(LocalDateTime end) {
            this.end = end;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = Objects.toString(timezone, "");
        }
    }

    public static class Review {
        private String owner = "";
        private final List<String> reviewers = new ArrayList<>();
        private String escalationEmail = "";

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = Objects.toString(owner, "");
        }

        public List<String> getReviewers() {
            return reviewers;
        }

        public void setReviewers(List<String> reviewers) {
            this.reviewers.clear();
            if (reviewers != null) {
                this.reviewers.addAll(reviewers);
            }
        }

        public String getEscalationEmail() {
            return escalationEmail;
        }

        public void setEscalationEmail(String escalationEmail) {
            this.escalationEmail = Objects.toString(escalationEmail, "");
        }
    }
}
