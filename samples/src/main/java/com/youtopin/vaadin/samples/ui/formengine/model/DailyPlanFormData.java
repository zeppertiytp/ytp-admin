package com.youtopin.vaadin.samples.ui.formengine.model;

import java.util.Objects;

/**
 * Bean backing the daily plan dynamic section sample.
 */
public class DailyPlanFormData {

    private final Schedule schedule = new Schedule();

    public Schedule getSchedule() {
        return schedule;
    }

    public static class Schedule {
        private Integer dayCount = 1;
        private String objective = "";
        private String day1Plan = "";
        private String day2Plan = "";
        private String day3Plan = "";
        private String day4Plan = "";
        private String day5Plan = "";
        private String day6Plan = "";
        private String day7Plan = "";

        public Integer getDayCount() {
            return dayCount;
        }

        public void setDayCount(Integer dayCount) {
            int sanitized = dayCount == null ? 0 : Math.max(dayCount, 0);
            this.dayCount = Math.min(sanitized, 7);
        }

        public String getObjective() {
            return objective;
        }

        public void setObjective(String objective) {
            this.objective = Objects.toString(objective, "");
        }

        public String getDay1Plan() {
            return day1Plan;
        }

        public void setDay1Plan(String day1Plan) {
            this.day1Plan = Objects.toString(day1Plan, "");
        }

        public String getDay2Plan() {
            return day2Plan;
        }

        public void setDay2Plan(String day2Plan) {
            this.day2Plan = Objects.toString(day2Plan, "");
        }

        public String getDay3Plan() {
            return day3Plan;
        }

        public void setDay3Plan(String day3Plan) {
            this.day3Plan = Objects.toString(day3Plan, "");
        }

        public String getDay4Plan() {
            return day4Plan;
        }

        public void setDay4Plan(String day4Plan) {
            this.day4Plan = Objects.toString(day4Plan, "");
        }

        public String getDay5Plan() {
            return day5Plan;
        }

        public void setDay5Plan(String day5Plan) {
            this.day5Plan = Objects.toString(day5Plan, "");
        }

        public String getDay6Plan() {
            return day6Plan;
        }

        public void setDay6Plan(String day6Plan) {
            this.day6Plan = Objects.toString(day6Plan, "");
        }

        public String getDay7Plan() {
            return day7Plan;
        }

        public void setDay7Plan(String day7Plan) {
            this.day7Plan = Objects.toString(day7Plan, "");
        }
    }
}
