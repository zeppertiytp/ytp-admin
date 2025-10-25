package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.ui.formengine.model.AccessPolicyFormData;

/**
 * Annotation-driven definition for the access policy designer sample.
 */
@UiForm(
        id = "access-policy",
        titleKey = "forms.policy.title",
        descriptionKey = "forms.policy.description",
        bean = AccessPolicyFormData.class,
        sections = {
                AccessPolicyFormDefinition.PolicySection.class,
                AccessPolicyFormDefinition.ScheduleSection.class,
                AccessPolicyFormDefinition.ReviewSection.class
        },
        actions = {
                @UiAction(id = "policy-simulate", labelKey = "forms.policy.action.simulate", placement = UiAction.Placement.HEADER,
                        type = UiAction.ActionType.SECONDARY),
                @UiAction(id = "policy-duplicate", labelKey = "forms.policy.action.duplicate", placement = UiAction.Placement.HEADER,
                        type = UiAction.ActionType.SECONDARY, order = 1),
                @UiAction(id = "policy-create", labelKey = "forms.policy.action.create", placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SUBMIT, order = 1)
        }
)
public final class AccessPolicyFormDefinition {

    private AccessPolicyFormDefinition() {
    }

    @UiSection(id = "policy", titleKey = "forms.policy.section.policy", groups = {
            PolicyGroup.class,
            ConditionGroup.class
    }, order = 0)
    public static class PolicySection {
    }

    @UiSection(id = "schedule", titleKey = "forms.policy.section.schedule", groups = {
            ScheduleGroup.class
    }, order = 1)
    public static class ScheduleSection {
    }

    @UiSection(id = "review", titleKey = "forms.policy.section.review", groups = {
            ReviewGroup.class
    }, order = 2)
    public static class ReviewSection {
    }

    @UiGroup(id = "policy-details", columns = 2)
    public static class PolicyGroup {

        @UiField(path = "policy.name", component = UiField.ComponentType.TEXT,
                labelKey = "forms.policy.policy.name", requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void name() {
        }

        @UiField(path = "policy.effect", component = UiField.ComponentType.RADIO,
                labelKey = "forms.policy.policy.effect",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.policy-effect"))
        public void effect() {
        }

        @UiField(path = "policy.resources", component = UiField.ComponentType.TAGS,
                labelKey = "forms.policy.policy.resources",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.policy-resources"),
                colSpan = 2)
        public void resources() {
        }

        @UiField(path = "policy.actions", component = UiField.ComponentType.MULTI_SELECT,
                labelKey = "forms.policy.policy.actions",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.policy-actions"))
        public void actions() {
        }

        @UiField(path = "policy.description", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.policy.policy.description", colSpan = 2)
        public void description() {
        }

        @UiField(path = "policy.delegation", component = UiField.ComponentType.SUBFORM,
                labelKey = "forms.policy.policy.delegation", helperKey = "forms.policy.policy.delegation.helper", colSpan = 2)
        public void delegation() {
        }
    }

    @UiGroup(id = "policy-conditions", titleKey = "forms.policy.conditions.title", columns = 3,
            repeatable = @UiRepeatable(enabled = true, min = 1, max = 5,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "forms.policy.condition.entryTitle"))
    public static class ConditionGroup {

        @UiField(path = "policy.conditions.attribute", component = UiField.ComponentType.TEXT,
                labelKey = "forms.policy.conditions.attribute")
        public void attribute() {
        }

        @UiField(path = "policy.conditions.operator", component = UiField.ComponentType.SELECT,
                labelKey = "forms.policy.conditions.operator",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.condition-operators"))
        public void operator() {
        }

        @UiField(path = "policy.conditions.value", component = UiField.ComponentType.TEXT,
                labelKey = "forms.policy.conditions.value")
        public void value() {
        }
    }

    @UiGroup(id = "schedule-details", columns = 2)
    public static class ScheduleGroup {

        @UiField(path = "schedule.start", component = UiField.ComponentType.DATETIME,
                labelKey = "forms.policy.schedule.start")
        public void start() {
        }

        @UiField(path = "schedule.end", component = UiField.ComponentType.DATETIME,
                labelKey = "forms.policy.schedule.end")
        public void end() {
        }

        @UiField(path = "schedule.timezone", component = UiField.ComponentType.SELECT,
                labelKey = "forms.policy.schedule.timezone",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.timezones"),
                colSpan = 2)
        public void timezone() {
        }
    }

    @UiGroup(id = "review-details", columns = 2)
    public static class ReviewGroup {

        @UiField(path = "review.owner", component = UiField.ComponentType.TEXT,
                labelKey = "forms.policy.review.owner", requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void owner() {
        }

        @UiField(path = "review.reviewers", component = UiField.ComponentType.MULTI_SELECT,
                labelKey = "forms.policy.review.reviewers",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.reviewers"))
        public void reviewers() {
        }

        @UiField(path = "review.escalationEmail", component = UiField.ComponentType.EMAIL,
                labelKey = "forms.policy.review.escalation", helperKey = "forms.policy.review.escalation.helper")
        public void escalationEmail() {
        }
    }
}
