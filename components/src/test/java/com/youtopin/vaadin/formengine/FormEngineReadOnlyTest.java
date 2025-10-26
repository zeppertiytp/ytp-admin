package com.youtopin.vaadin.formengine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.I18NProvider;
import com.youtopin.vaadin.formengine.FormEngine.RenderedForm;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.SectionDefinition;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;
import com.youtopin.vaadin.formengine.registry.FieldInstance;

class FormEngineReadOnlyTest {

    @Test
    void honoursReadOnlyMetadataAcrossHierarchy() {
        FormEngine engine = new FormEngine(new OptionCatalogRegistry());
        RenderedForm<LockableBean> rendered = engine.render(ReadOnlyFormDefinition.class,
                new StubI18NProvider(), Locale.ENGLISH, false);

        rendered.getFields().forEach((definition, instance) ->
                rendered.getOrchestrator().bindField(instance, definition));

        LockableBean locked = new LockableBean();
        locked.setStatus("LOCKED");
        locked.setBillingLocked(true);
        locked.setLastNameLocked(false);

        rendered.initializeWithBean(locked);

        TextField firstName = (TextField) findField(rendered, "firstName").getValueComponent();
        TextField lastName = (TextField) findField(rendered, "lastName").getValueComponent();
        TextField billingCode = (TextField) findField(rendered, "billingCode").getValueComponent();

        assertThat(firstName.isReadOnly()).isTrue();
        assertThat(lastName.isReadOnly()).isTrue();
        assertThat(billingCode.isReadOnly()).isTrue();

        SectionDefinition mainSection = rendered.getDefinition().findSection("main-section");
        assertThat(mainSection).isNotNull();
        assertThat(rendered.getSections().get(mainSection).isVisible()).isTrue();

        LockableBean editable = new LockableBean();
        editable.setStatus("ACTIVE");
        editable.setBillingLocked(false);
        editable.setLastNameLocked(true);

        rendered.initializeWithBean(editable);

        assertThat(firstName.isReadOnly()).isFalse();
        assertThat(billingCode.isReadOnly()).isFalse();
        assertThat(lastName.isReadOnly()).isTrue();
    }

    @Test
    void supportsDynamicReadOnlyOverrides() {
        FormEngine engine = new FormEngine(new OptionCatalogRegistry());
        RenderedForm<LockableBean> rendered = engine.render(ReadOnlyFormDefinition.class,
                new StubI18NProvider(), Locale.ENGLISH, false);

        rendered.getFields().forEach((definition, instance) ->
                rendered.getOrchestrator().bindField(instance, definition));

        LockableBean bean = new LockableBean();
        bean.setStatus("ACTIVE");
        bean.setBillingLocked(false);
        bean.setLastNameLocked(false);

        rendered.initializeWithBean(bean);

        TextField firstName = (TextField) findField(rendered, "firstName").getValueComponent();
        assertThat(firstName.isReadOnly()).isFalse();

        RenderedForm.ReadOnlyOverride<LockableBean> firstNameLock = (definition, context) ->
                "firstName".equals(definition.getPath())
                        && context != null
                        && context.getBean() != null
                        && "ACTIVE".equals(context.getBean().getStatus());
        rendered.addReadOnlyOverride(firstNameLock);

        assertThat(firstName.isReadOnly()).isTrue();

        bean.setStatus("INACTIVE");
        rendered.initializeWithBean(bean);
        assertThat(firstName.isReadOnly()).isFalse();

        bean.setStatus("ACTIVE");
        rendered.refreshReadOnlyState();
        assertThat(firstName.isReadOnly()).isTrue();
    }

    private FieldInstance findField(RenderedForm<?> rendered, String path) {
        return rendered.getFields().entrySet().stream()
                .filter(entry -> path.equals(entry.getKey().getPath()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
    }

    @UiForm(
            id = "read-only-form",
            titleKey = "",
            descriptionKey = "",
            bean = LockableBean.class,
            sections = {MainSection.class, BillingSection.class}
    )
    public static class ReadOnlyFormDefinition {
    }

    @UiSection(id = "main-section",
            titleKey = "",
            groups = MainGroup.class,
            order = 0,
            readOnlyWhen = "status == 'LOCKED'")
    public static class MainSection {
    }

    @UiSection(id = "billing-section",
            titleKey = "",
            groups = BillingGroup.class,
            order = 1)
    public static class BillingSection {
    }

    @UiGroup(id = "main-group", columns = 2)
    public static class MainGroup {

        @UiField(path = "firstName", component = UiField.ComponentType.TEXT, labelKey = "firstName")
        public void firstName() {
        }

        @UiField(path = "lastName", component = UiField.ComponentType.TEXT, labelKey = "lastName",
                readOnlyWhen = "lastNameLocked")
        public void lastName() {
        }
    }

    @UiGroup(id = "billing-group", columns = 1, readOnlyWhen = "billingLocked")
    public static class BillingGroup {

        @UiField(path = "billingCode", component = UiField.ComponentType.TEXT, labelKey = "billingCode")
        public void billingCode() {
        }
    }

    public static class LockableBean {
        private String status;
        private boolean billingLocked;
        private boolean lastNameLocked;
        private String firstName;
        private String lastName;
        private String billingCode;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isBillingLocked() {
            return billingLocked;
        }

        public void setBillingLocked(boolean billingLocked) {
            this.billingLocked = billingLocked;
        }

        public boolean isLastNameLocked() {
            return lastNameLocked;
        }

        public void setLastNameLocked(boolean lastNameLocked) {
            this.lastNameLocked = lastNameLocked;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getBillingCode() {
            return billingCode;
        }

        public void setBillingCode(String billingCode) {
            this.billingCode = billingCode;
        }
    }

    private static final class StubI18NProvider implements I18NProvider {

        @Override
        public java.util.List<Locale> getProvidedLocales() {
            return java.util.List.of(Locale.ENGLISH);
        }

        @Override
        public String getTranslation(String key, Locale locale, Object... params) {
            return key == null ? "" : key;
        }
    }
}
