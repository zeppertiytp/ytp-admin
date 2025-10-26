package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.ui.formengine.model.ProfileLockFormData;

/**
 * Demonstrates read-only transitions at section, group, and field level.
 */
@UiForm(
        id = "profile-lock",
        titleKey = "forms.locking.title",
        descriptionKey = "forms.locking.description",
        bean = ProfileLockFormData.class,
        sections = {
                ProfileLockFormDefinition.ProfileSection.class,
                ProfileLockFormDefinition.ContactSection.class
        },
        actions = @UiAction(id = "profile-lock-submit", labelKey = "forms.locking.action.submit",
                placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SUBMIT, order = 0)
)
public final class ProfileLockFormDefinition {

    private ProfileLockFormDefinition() {
    }

    @UiSection(id = "profile-lock-profile",
            titleKey = "forms.locking.section.profile",
            groups = ProfileGroup.class,
            order = 0,
            readOnlyWhen = "locked")
    public static class ProfileSection {
    }

    @UiSection(id = "profile-lock-contact",
            titleKey = "forms.locking.section.contact",
            groups = ContactGroup.class,
            order = 1)
    public static class ContactSection {
    }

    @UiGroup(id = "profile-lock-profile-group", columns = 2)
    public static class ProfileGroup {

        @UiField(path = "username", component = UiField.ComponentType.TEXT,
                labelKey = "forms.locking.profile.username")
        public void username() {
        }

        @UiField(path = "email", component = UiField.ComponentType.EMAIL,
                labelKey = "forms.locking.profile.email")
        public void email() {
        }

        @UiField(path = "locked", component = UiField.ComponentType.SWITCH,
                labelKey = "forms.locking.profile.locked")
        public void locked() {
        }
    }

    @UiGroup(id = "profile-lock-contact-group", columns = 2, readOnlyWhen = "contactLocked")
    public static class ContactGroup {

        @UiField(path = "phone", component = UiField.ComponentType.PHONE,
                labelKey = "forms.locking.contact.phone")
        public void phone() {
        }

        @UiField(path = "contactLocked", component = UiField.ComponentType.SWITCH,
                labelKey = "forms.locking.contact.contactLocked")
        public void contactLocked() {
        }
    }
}
