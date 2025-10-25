package com.youtopin.vaadin.formengine.binder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.OptionsDefinition;
import com.youtopin.vaadin.formengine.definition.SecurityDefinition;
import com.youtopin.vaadin.formengine.definition.ValidationDefinition;
import com.youtopin.vaadin.formengine.options.OptionItem;
import com.youtopin.vaadin.formengine.registry.FieldInstance;

class BinderOrchestratorTest {

    @Test
    void convertsNumberField() throws ValidationException {
        BinderOrchestrator<TestBean> orchestrator = new BinderOrchestrator<>(TestBean.class, key -> key);
        NumberField field = new NumberField();
        FieldDefinition definition = new FieldDefinition("amount", UiField.ComponentType.NUMBER, "amount", "", "", "",
                "", "", "", "", new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false,
                        "", true), List.of(), List.of(), new SecurityDefinition("", "", List.of(), false), 0, 1, 1);
        orchestrator.bindField(new FieldInstance(field, field, List.of()), definition);
        TestBean bean = new TestBean();
        field.setValue(42.0d);
        orchestrator.writeBean(bean);
        assertThat(bean.amount).isEqualTo(42.0d);
    }

    @Test
    void convertsMultiSelectToList() throws ValidationException {
        BinderOrchestrator<TestBean> orchestrator = new BinderOrchestrator<>(TestBean.class, key -> key);
        MultiSelectComboBox<OptionItem> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(new OptionItem("A", "A"), new OptionItem("B", "B"));
        FieldDefinition definition = new FieldDefinition("tags", UiField.ComponentType.MULTI_SELECT, "tags", "", "", "",
                "", "", "", "", new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false,
                        "", true), List.of(), List.of(), new SecurityDefinition("", "", List.of(), false), 0, 1, 1);
        orchestrator.bindField(new FieldInstance(comboBox, comboBox, List.of()), definition);
        comboBox.setValue(Set.of(new OptionItem("A", "A"), new OptionItem("B", "B")));
        TestBean bean = new TestBean();
        orchestrator.writeBean(bean);
        assertThat(bean.tags).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void marksFieldInvalidWhenRequired() {
        BinderOrchestrator<TestBean> orchestrator = new BinderOrchestrator<>(TestBean.class, key -> key);
        TextField field = new TextField();
        FieldDefinition definition = new FieldDefinition("amount", UiField.ComponentType.TEXT, "amount", "", "", "true",
                "required", "", "", "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.of(), List.of(), new SecurityDefinition("", "", List.of(), false), 0, 1, 1);
        orchestrator.bindField(new FieldInstance(field, field, List.of()), definition);

        ValidationException exception = assertThrows(ValidationException.class, () -> orchestrator.writeBean(new TestBean()));

        assertThat(exception.getValidationErrors()).isNotEmpty();
        assertThat(field.isInvalid()).isTrue();
        assertThat(field.getErrorMessage()).isEqualTo("required");
    }

    @Test
    void appliesExpressionValidation() {
        BinderOrchestrator<TestBean> orchestrator = new BinderOrchestrator<>(TestBean.class, key -> key);
        NumberField field = new NumberField();
        field.setValue(-5d);
        ValidationDefinition rule = new ValidationDefinition("positive", "value > 0", List.of(), "");
        FieldDefinition definition = new FieldDefinition("amount", UiField.ComponentType.NUMBER, "amount", "", "", "",
                "", "", "", "", new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "",
                        false, "", true), List.of(rule), List.of(), new SecurityDefinition("", "", List.of(), false), 0, 1, 1);
        orchestrator.bindField(new FieldInstance(field, field, List.of()), definition);

        ValidationException exception = assertThrows(ValidationException.class, () -> orchestrator.writeBean(new TestBean()));

        assertThat(exception.getValidationErrors()).isNotEmpty();
        assertThat(field.isInvalid()).isTrue();
        assertThat(field.getErrorMessage()).isEqualTo("positive");
    }

    @Test
    void supportsCustomValidationHandler() {
        BinderOrchestrator<TestBean> orchestrator = new BinderOrchestrator<>(TestBean.class, key -> key);
        TextField field = new TextField();
        FieldDefinition definition = new FieldDefinition("amount", UiField.ComponentType.TEXT, "amount", "", "", "true",
                "required", "", "", "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.of(), List.of(), new SecurityDefinition("", "", List.of(), false), 0, 1, 1);
        FieldInstance instance = new FieldInstance(field, field, List.of());
        AtomicBoolean cleared = new AtomicBoolean();
        AtomicReference<String> appliedMessage = new AtomicReference<>();
        instance.setCustomValidationHandler(new FieldInstance.ValidationHandler() {
            @Override
            public void clear(FieldInstance fieldInstance) {
                cleared.set(true);
            }

            @Override
            public boolean apply(FieldInstance fieldInstance, String message) {
                appliedMessage.set(message);
                return true;
            }
        });
        orchestrator.bindField(instance, definition);

        ValidationException exception = assertThrows(ValidationException.class, () -> orchestrator.writeBean(new TestBean()));

        assertThat(exception.getValidationErrors()).isNotEmpty();
        assertThat(cleared).isTrue();
        assertThat(appliedMessage.get()).isEqualTo("required");
        assertThat(field.isInvalid()).isFalse();
    }

    @Test
    void skipsPrimitiveSetterWhenValueNull() throws ValidationException {
        BinderOrchestrator<PrimitiveBean> orchestrator = new BinderOrchestrator<>(PrimitiveBean.class, key -> key);
        IntegerField field = new IntegerField();
        FieldDefinition definition = new FieldDefinition("count", UiField.ComponentType.INTEGER, "count", "", "", "", "",
                "", "", "", new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false,
                "", true), List.of(), List.of(), new SecurityDefinition("", "", List.of(), false), 0, 1, 1);
        orchestrator.bindField(new FieldInstance(field, field, List.of()), definition);

        PrimitiveBean bean = new PrimitiveBean();
        orchestrator.writeBean(bean);

        assertThat(bean.count).isZero();
    }

    @Test
    void coercesStringToEnumValue() throws ValidationException {
        BinderOrchestrator<EnumBean> orchestrator = new BinderOrchestrator<>(EnumBean.class, key -> key);
        TextField field = new TextField();
        field.setValue("ACTIVE");
        FieldDefinition definition = new FieldDefinition("status", UiField.ComponentType.TEXT, "status", "", "", "", "",
                "", "", "", new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false,
                "", true), List.of(), List.of(), new SecurityDefinition("", "", List.of(), false), 0, 1, 1);
        orchestrator.bindField(new FieldInstance(field, field, List.of()), definition);

        EnumBean bean = new EnumBean();
        orchestrator.writeBean(bean);

        assertThat(bean.status).isEqualTo(EnumBean.Status.ACTIVE);
    }

    static class TestBean {
        double amount;
        List<String> tags;

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    static class PrimitiveBean {
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    static class EnumBean {
        private Status status = Status.NEW;

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        enum Status {
            NEW,
            ACTIVE
        }
    }
}
