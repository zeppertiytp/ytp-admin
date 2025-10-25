package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.ui.formengine.model.DailyPlanListFormData;

/**
 * Definition for the repeatable daily plan sample that persists entries as a list.
 */
@UiForm(
        id = "daily-plan-repeatable",
        titleKey = "forms.planlist.title",
        descriptionKey = "forms.planlist.description",
        bean = DailyPlanListFormData.class,
        sections = {
                DailyPlanListFormDefinition.PlanListSetupSection.class,
                DailyPlanListFormDefinition.PlanEntriesSection.class
        },
        actions = {
                @UiAction(id = "planlist-reset", labelKey = "forms.planlist.action.reset",
                        placement = UiAction.Placement.HEADER, type = UiAction.ActionType.SECONDARY, order = 0),
                @UiAction(id = "planlist-submit", labelKey = "forms.planlist.action.submit",
                        placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SUBMIT, order = 1)
        }
)
public final class DailyPlanListFormDefinition {

    private DailyPlanListFormDefinition() {
    }

    @UiSection(id = "plan-list-setup", titleKey = "forms.planlist.section.setup", groups = PlanListSetupGroup.class, order = 0)
    public static class PlanListSetupSection {
    }

    @UiSection(id = "plan-list-days", titleKey = "forms.planlist.section.days", groups = PlanEntryGroup.class, order = 1)
    public static class PlanEntriesSection {
    }

    @UiGroup(id = "plan-list-setup-group", columns = 2)
    public static class PlanListSetupGroup {

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

    @UiGroup(id = "plan-list-entry-group", columns = 1,
            repeatable = @UiRepeatable(enabled = true, min = 0, max = 7,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "forms.planlist.entry.title", allowDuplicate = false,
                    summaryTemplate = "{plan}"))
    public static class PlanEntryGroup {

        @UiField(path = "schedule.days.plan", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.plan.field.dayPlan", helperKey = "forms.plan.field.dayPlan.helper", colSpan = 1)
        public void plan() {
        }
    }
}
