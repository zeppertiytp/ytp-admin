package com.youtopin.vaadin.samples.ui.formengine.definition;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.samples.ui.formengine.model.CompositeContactFormData;

/**
 * Demonstrates multi-group repeatable entries combining identity, address, and communication metadata.
 */
@UiForm(
        id = "composite-contact-directory",
        titleKey = "forms.composite.title",
        descriptionKey = "forms.composite.description",
        bean = CompositeContactFormData.class,
        sections = CompositeContactFormDefinition.DirectorySection.class,
        actions = @UiAction(id = "composite-save", labelKey = "forms.composite.action.save",
                placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SUBMIT, order = 0)
)
public final class CompositeContactFormDefinition {

    private CompositeContactFormDefinition() {
    }

    @UiSection(id = "composite-directory-section",
            titleKey = "forms.composite.section.directory",
            descriptionKey = "forms.composite.section.directory.description",
            groups = ContactDirectoryGroup.class,
            order = 0)
    public static class DirectorySection {
    }

    @UiGroup(id = "composite-directory-group",
            columns = 2,
            repeatable = @UiRepeatable(enabled = true,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    min = 1,
                    max = 8,
                    summaryTemplate = "{fullName}",
                    itemTitleKey = "forms.composite.entry.caption",
                    allowDuplicate = true),
            entryGroups = {ContactAddressGroup.class, ContactChannelGroup.class})
    public static class ContactDirectoryGroup {

        @UiField(path = "contacts.fullName", component = UiField.ComponentType.TEXT,
                labelKey = "forms.composite.field.fullName",
                requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                order = 0)
        public void fullName() {
        }

        @UiField(path = "contacts.email", component = UiField.ComponentType.EMAIL,
                labelKey = "forms.composite.field.email",
                helperKey = "forms.composite.field.email.helper",
                order = 10)
        public void email() {
        }

        @UiField(path = "contacts.phone", component = UiField.ComponentType.TEXT,
                labelKey = "forms.composite.field.phone",
                helperKey = "forms.composite.field.phone.helper",
                order = 20)
        public void phone() {
        }
    }

    @UiGroup(id = "composite-address-group",
            titleKey = "forms.composite.group.address",
            columns = 2)
    public static class ContactAddressGroup {

        @UiField(path = "contacts.address.country", component = UiField.ComponentType.TEXT,
                labelKey = "forms.composite.field.address.country",
                order = 0)
        public void country() {
        }

        @UiField(path = "contacts.address.city", component = UiField.ComponentType.TEXT,
                labelKey = "forms.composite.field.address.city",
                order = 10)
        public void city() {
        }

        @UiField(path = "contacts.address.street", component = UiField.ComponentType.TEXT,
                labelKey = "forms.composite.field.address.street",
                colSpan = 2,
                order = 20)
        public void street() {
        }

        @UiField(path = "contacts.address.postalCode", component = UiField.ComponentType.TEXT,
                labelKey = "forms.composite.field.address.postal",
                order = 30)
        public void postalCode() {
        }
    }

    @UiGroup(id = "composite-channel-group",
            titleKey = "forms.composite.group.channel",
            columns = 2)
    public static class ContactChannelGroup {

        @UiField(path = "contacts.channels.primary", component = UiField.ComponentType.SWITCH,
                labelKey = "forms.composite.field.channel.primary",
                helperKey = "forms.composite.field.channel.primary.helper",
                colSpan = 2,
                order = 0)
        public void primary() {
        }

        @UiField(path = "contacts.channels.label", component = UiField.ComponentType.TEXT,
                labelKey = "forms.composite.field.channel.label",
                order = 10)
        public void label() {
        }

        @UiField(path = "contacts.channels.notes", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "forms.composite.field.channel.notes",
                helperKey = "forms.composite.field.channel.notes.helper",
                colSpan = 2,
                order = 20)
        public void notes() {
        }
    }
}
