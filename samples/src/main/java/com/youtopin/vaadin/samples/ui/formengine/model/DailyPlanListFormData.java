package com.youtopin.vaadin.samples.ui.formengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bean backing the repeatable daily plan sample that stores plans as a list.
 */
public class DailyPlanListFormData {

    private final Schedule schedule = new Schedule();

    public Schedule getSchedule() {
        return schedule;
    }

    public static class Schedule {
        private static final int MAX_DAYS = 7;

        private Integer dayCount = 1;
        private String objective = "";
        private final List<DayPlan> days = new ArrayList<>();

        public Schedule() {
            ensureSize();
        }

        public Integer getDayCount() {
            return dayCount;
        }

        public void setDayCount(Integer dayCount) {
            int sanitized = dayCount == null ? 0 : Math.max(dayCount, 0);
            this.dayCount = Math.min(sanitized, MAX_DAYS);
            ensureSize();
        }

        public String getObjective() {
            return objective;
        }

        public void setObjective(String objective) {
            this.objective = Objects.toString(objective, "");
        }

        public List<DayPlan> getDays() {
            return days;
        }

        public void setDays(List<DayPlan> plans) {
            days.clear();
            if (plans != null) {
                plans.stream()
                        .filter(Objects::nonNull)
                        .map(DayPlan::copyOf)
                        .forEach(days::add);
            }
            ensureSize();
        }

        private void ensureSize() {
            int desired = Math.max(0, dayCount == null ? 0 : dayCount);
            while (days.size() < desired) {
                days.add(new DayPlan());
            }
            while (days.size() > desired) {
                days.remove(days.size() - 1);
            }
        }
    }

    public static class DayPlan {
        private String plan = "";

        public DayPlan() {
        }

        public DayPlan(String plan) {
            this.plan = Objects.toString(plan, "");
        }

        public String getPlan() {
            return plan;
        }

        public void setPlan(String plan) {
            this.plan = Objects.toString(plan, "");
        }

        private static DayPlan copyOf(DayPlan source) {
            if (source == null) {
                return new DayPlan();
            }
            return new DayPlan(source.getPlan());
        }
    }
}
