package com.youtopin.vaadin.samples.ui.formengine.definition.dynamic;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.samples.application.dynamicbag.DynamicBagFormData;

/**
 * Single-form sample that persists repeatable entries into {@link com.youtopin.vaadin.formengine.binder.DynamicPropertyBag}
 * maps instead of strongly typed beans.
 */
@UiForm(
        id = "dynamic-bag-directory",
        titleKey = "forms.dynamicBag.title",
        descriptionKey = "forms.dynamicBag.description",
        bean = DynamicBagFormData.class,
        sections = DynamicPropertyBagDirectoryFormDefinition.DirectorySection.class,
        actions = @UiAction(id = "dynamic-bag-submit",
                labelKey = "forms.dynamicBag.action.submit",
                placement = UiAction.Placement.FOOTER,
                type = UiAction.ActionType.SUBMIT,
                order = 0)
)
public final class DynamicPropertyBagDirectoryFormDefinition {

    private DynamicPropertyBagDirectoryFormDefinition() {
    }

    @UiSection(id = "dynamic-bag-section",
            titleKey = "forms.dynamicBag.section.directory",
            descriptionKey = "forms.dynamicBag.section.directory.description",
            groups = DynamicDirectoryGroup.class,
            order = 0)
    public static class DirectorySection {
    }

    @UiGroup(id = "dynamic-bag-entry",
            columns = 2,
            repeatable = @UiRepeatable(enabled = true,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    min = 1,
                    max = 8,
                    summaryTemplate = "{profile.fullName}",
                    itemTitleKey = "forms.dynamicBag.entry.caption",
                    allowDuplicate = true),
            entryGroups = {AddressGroup.class, ChannelGroup.class})
    public static class DynamicDirectoryGroup {

        @UiField(path = "entries.profile.fullName",
                component = UiField.ComponentType.TEXT,
                labelKey = "forms.dynamicBag.field.fullName",
                requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                order = 0)
        public void fullName() {
        }

        @UiField(path = "entries.profile.role",
                component = UiField.ComponentType.TEXT,
                labelKey = "forms.dynamicBag.field.role",
                helperKey = "forms.dynamicBag.field.role.helper",
                order = 10)
        public void role() {
        }
    }

    @UiGroup(id = "dynamic-bag-address",
            titleKey = "forms.dynamicBag.group.address",
            columns = 2)
    public static class AddressGroup {

        @UiField(path = "entries.address.country",
                component = UiField.ComponentType.TEXT,
                labelKey = "forms.dynamicBag.field.country",
                order = 0)
        public void country() {
        }

        @UiField(path = "entries.address.city",
                component = UiField.ComponentType.TEXT,
                labelKey = "forms.dynamicBag.field.city",
                order = 10)
        public void city() {
        }

        @UiField(path = "entries.address.street",
                component = UiField.ComponentType.TEXT,
                labelKey = "forms.dynamicBag.field.street",
                colSpan = 2,
                order = 20)
        public void street() {
        }
    }

    @UiGroup(id = "dynamic-bag-channels",
            titleKey = "forms.dynamicBag.group.channels",
            columns = 2)
    public static class ChannelGroup {

        @UiField(path = "entries.channels.email",
                component = UiField.ComponentType.EMAIL,
                labelKey = "forms.dynamicBag.field.email",
                helperKey = "forms.dynamicBag.field.email.helper",
                order = 0)
        public void email() {
        }

        @UiField(path = "entries.channels.phone",
                component = UiField.ComponentType.TEXT,
                labelKey = "forms.dynamicBag.field.phone",
                helperKey = "forms.dynamicBag.field.phone.helper",
                order = 10)
        public void phone() {
        }

        @UiField(path = "entries.channels.preferred",
                component = UiField.ComponentType.SELECT,
                labelKey = "forms.dynamicBag.field.preferred",
                helperKey = "forms.dynamicBag.field.preferred.helper",
                colSpan = 2,
                options = @UiOptions(enabled = true,
                        type = UiOptions.ProviderType.STATIC,
                        entries = {
                                "Email|forms.dynamicBag.option.email",
                                "Phone|forms.dynamicBag.option.phone",
                                "Chat|forms.dynamicBag.option.chat"
                        }),
                order = 20)
        public void preferredChannel() {
        }
    }
}
