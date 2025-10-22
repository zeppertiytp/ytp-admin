package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.ui.formengine.model.EmployeeOnboardingFormData;

/**
 * Annotation-driven definition describing the employee onboarding sample.
 */
@UiForm(
        id = "employee-onboarding",
        titleKey = "forms.onboarding.title",
        descriptionKey = "forms.onboarding.description",
        bean = EmployeeOnboardingFormData.class,
        sections = {
                EmployeeOnboardingFormDefinition.PersonalSection.class,
                EmployeeOnboardingFormDefinition.EmploymentSection.class,
                EmployeeOnboardingFormDefinition.ContactSection.class
        },
        actions = {
                @UiAction(id = "onboarding-draft", labelKey = "forms.onboarding.action.draft", placement = UiAction.Placement.HEADER,
                        type = UiAction.ActionType.SECONDARY),
                @UiAction(id = "onboarding-submit", labelKey = "forms.onboarding.action.submit", placement = UiAction.Placement.FOOTER,
                        type = UiAction.ActionType.SUBMIT, order = 1)
        }
)
public final class EmployeeOnboardingFormDefinition {

    private EmployeeOnboardingFormDefinition() {
    }

    @UiSection(id = "personal", titleKey = "forms.onboarding.section.personal", groups = {
            PersonalIdentityGroup.class,
            PersonalBackgroundGroup.class
    }, order = 0)
    public static class PersonalSection {
    }

    @UiSection(id = "employment", titleKey = "forms.onboarding.section.employment", groups = {
            EmploymentGroup.class
    }, order = 1)
    public static class EmploymentSection {
    }

    @UiSection(id = "contact", titleKey = "forms.onboarding.section.contact", groups = {
            ContactGroup.class,
            TeamPreferencesGroup.class
    }, order = 2)
    public static class ContactSection {
    }

    @UiGroup(id = "personal-identity", columns = 2)
    public static class PersonalIdentityGroup {

        @UiField(path = "personal.firstName", component = UiField.ComponentType.TEXT,
                labelKey = "forms.onboarding.personal.firstName", placeholderKey = "forms.onboarding.personal.firstName.placeholder",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void firstName() {
        }

        @UiField(path = "personal.lastName", component = UiField.ComponentType.TEXT,
                labelKey = "forms.onboarding.personal.lastName", placeholderKey = "forms.onboarding.personal.lastName.placeholder",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void lastName() {
        }

        @UiField(path = "personal.nationalId", component = UiField.ComponentType.TEXT,
                labelKey = "forms.onboarding.personal.nationalId", helperKey = "forms.onboarding.personal.nationalId.helper")
        public void nationalId() {
        }

        @UiField(path = "personal.birthDate", component = UiField.ComponentType.DATE,
                labelKey = "forms.onboarding.personal.birthDate", colSpan = 2)
        public void birthDate() {
        }
    }

    @UiGroup(id = "personal-background", columns = 1)
    public static class PersonalBackgroundGroup {

        @UiField(path = "personal.relocationReady", component = UiField.ComponentType.SWITCH,
                labelKey = "forms.onboarding.personal.relocation", helperKey = "forms.onboarding.personal.relocation.helper")
        public void relocationReady() {
        }

        @UiField(path = "personal.bio", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.onboarding.personal.bio", helperKey = "forms.onboarding.personal.bio.helper")
        public void biography() {
        }
    }

    @UiGroup(id = "employment-details", columns = 2)
    public static class EmploymentGroup {

        @UiField(path = "employment.department", component = UiField.ComponentType.SELECT,
                labelKey = "forms.onboarding.employment.department",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.departments"))
        public void department() {
        }

        @UiField(path = "employment.contractType", component = UiField.ComponentType.RADIO,
                labelKey = "forms.onboarding.employment.contractType",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.contract-types"))
        public void contractType() {
        }

        @UiField(path = "employment.startDate", component = UiField.ComponentType.DATE,
                labelKey = "forms.onboarding.employment.startDate")
        public void startDate() {
        }

        @UiField(path = "employment.onboardingDateTime", component = UiField.ComponentType.JALALI_DATE_TIME,
                labelKey = "forms.onboarding.employment.orientation",
                placeholderKey = "forms.onboarding.employment.orientation.placeholder", colSpan = 2)
        public void orientation() {
        }

        @UiField(path = "employment.equipmentPreference", component = UiField.ComponentType.SELECT,
                labelKey = "forms.onboarding.employment.equipment",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.equipment"))
        public void equipment() {
        }
    }

    @UiGroup(id = "contact-details", columns = 2)
    public static class ContactGroup {

        @UiField(path = "contact.email", component = UiField.ComponentType.EMAIL,
                labelKey = "forms.onboarding.contact.email", helperKey = "forms.onboarding.contact.email.helper",
                requiredWhen = "true", requiredMessageKey = "forms.validation.required")
        public void email() {
        }

        @UiField(path = "contact.phone", component = UiField.ComponentType.PHONE,
                labelKey = "forms.onboarding.contact.phone", helperKey = "forms.onboarding.contact.phone.helper")
        public void phone() {
        }

        @UiField(path = "contact.address.city", component = UiField.ComponentType.TEXT,
                labelKey = "forms.onboarding.contact.city")
        public void city() {
        }

        @UiField(path = "contact.address.street", component = UiField.ComponentType.TEXT,
                labelKey = "forms.onboarding.contact.street")
        public void street() {
        }

        @UiField(path = "contact.address.postalCode", component = UiField.ComponentType.TEXT,
                labelKey = "forms.onboarding.contact.postal", colSpan = 2)
        public void postalCode() {
        }

        @UiField(path = "preferredLocation", component = UiField.ComponentType.MAP,
                labelKey = "forms.onboarding.contact.location",
                placeholderKey = "forms.onboarding.contact.location.placeholder", colSpan = 2)
        public void preferredLocation() {
        }
    }

    @UiGroup(id = "team-preferences", columns = 1)
    public static class TeamPreferencesGroup {

        @UiField(path = "skillTags", component = UiField.ComponentType.TAGS,
                labelKey = "forms.onboarding.contact.skills",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "catalog.skills"))
        public void skillTags() {
        }
    }
}
