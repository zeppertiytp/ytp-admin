package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.ui.formengine.model.DailyPlanFormData;

/**
 * Definition for the daily plan dynamic section sample.
 */
@UiForm(
        id = "daily-plan",
        titleKey = "forms.plan.title",
        descriptionKey = "forms.plan.description",
        bean = DailyPlanFormData.class,
        sections = {
                DailyPlanFormDefinition.PlanSetupSection.class,
                DailyPlanFormDefinition.DayOneSection.class,
                DailyPlanFormDefinition.DayTwoSection.class,
                DailyPlanFormDefinition.DayThreeSection.class,
                DailyPlanFormDefinition.DayFourSection.class,
                DailyPlanFormDefinition.DayFiveSection.class,
                DailyPlanFormDefinition.DaySixSection.class,
                DailyPlanFormDefinition.DaySevenSection.class
        },
        actions = {
                @UiAction(id = "plan-reset", labelKey = "forms.plan.action.reset", placement = UiAction.Placement.HEADER,
                        type = UiAction.ActionType.SECONDARY),
                @UiAction(id = "plan-submit", labelKey = "forms.plan.action.submit", placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SUBMIT, order = 1)
        }
)
public final class DailyPlanFormDefinition {

    private DailyPlanFormDefinition() {
    }

    @UiSection(id = "plan-setup", titleKey = "forms.plan.section.setup", groups = PlanSetupGroup.class, order = 0)
    public static class PlanSetupSection {
    }

    @UiSection(id = "day-1", titleKey = "forms.plan.section.day1", groups = DayOneGroup.class, order = 1)
    public static class DayOneSection {
    }

    @UiSection(id = "day-2", titleKey = "forms.plan.section.day2", groups = DayTwoGroup.class, order = 2)
    public static class DayTwoSection {
    }

    @UiSection(id = "day-3", titleKey = "forms.plan.section.day3", groups = DayThreeGroup.class, order = 3)
    public static class DayThreeSection {
    }

    @UiSection(id = "day-4", titleKey = "forms.plan.section.day4", groups = DayFourGroup.class, order = 4)
    public static class DayFourSection {
    }

    @UiSection(id = "day-5", titleKey = "forms.plan.section.day5", groups = DayFiveGroup.class, order = 5)
    public static class DayFiveSection {
    }

    @UiSection(id = "day-6", titleKey = "forms.plan.section.day6", groups = DaySixGroup.class, order = 6)
    public static class DaySixSection {
    }

    @UiSection(id = "day-7", titleKey = "forms.plan.section.day7", groups = DaySevenGroup.class, order = 7)
    public static class DaySevenSection {
    }

    @UiGroup(id = "plan-setup-group", columns = 2)
    public static class PlanSetupGroup {

        @UiField(path = "schedule.dayCount", component = UiField.ComponentType.INTEGER,
                labelKey = "forms.plan.field.dayCount", helperKey = "forms.plan.field.dayCount.helper",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void dayCount() {
        }

        @UiField(path = "schedule.objective", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.plan.field.objective", colSpan = 2)
        public void objective() {
        }
    }

    @UiGroup(id = "day-one-group")
    public static class DayOneGroup {

        @UiField(path = "schedule.day1Plan", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.plan.field.dayPlan", helperKey = "forms.plan.field.dayPlan.helper")
        public void plan() {
        }
    }

    @UiGroup(id = "day-two-group")
    public static class DayTwoGroup {

        @UiField(path = "schedule.day2Plan", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.plan.field.dayPlan", helperKey = "forms.plan.field.dayPlan.helper")
        public void plan() {
        }
    }

    @UiGroup(id = "day-three-group")
    public static class DayThreeGroup {

        @UiField(path = "schedule.day3Plan", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.plan.field.dayPlan", helperKey = "forms.plan.field.dayPlan.helper")
        public void plan() {
        }
    }

    @UiGroup(id = "day-four-group")
    public static class DayFourGroup {

        @UiField(path = "schedule.day4Plan", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.plan.field.dayPlan", helperKey = "forms.plan.field.dayPlan.helper")
        public void plan() {
        }
    }

    @UiGroup(id = "day-five-group")
    public static class DayFiveGroup {

        @UiField(path = "schedule.day5Plan", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.plan.field.dayPlan", helperKey = "forms.plan.field.dayPlan.helper")
        public void plan() {
        }
    }

    @UiGroup(id = "day-six-group")
    public static class DaySixGroup {

        @UiField(path = "schedule.day6Plan", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.plan.field.dayPlan", helperKey = "forms.plan.field.dayPlan.helper")
        public void plan() {
        }
    }

    @UiGroup(id = "day-seven-group")
    public static class DaySevenGroup {

        @UiField(path = "schedule.day7Plan", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.plan.field.dayPlan", helperKey = "forms.plan.field.dayPlan.helper")
        public void plan() {
        }
    }
}
