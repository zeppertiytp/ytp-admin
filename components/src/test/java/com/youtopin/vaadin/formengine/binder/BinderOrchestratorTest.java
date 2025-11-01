package com.youtopin.vaadin.formengine.binder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.youtopin.vaadin.formengine.definition.CrossFieldValidationDefinition;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.OptionsDefinition;
import com.youtopin.vaadin.formengine.definition.SecurityDefinition;
import com.youtopin.vaadin.formengine.definition.ValidationDefinition;
import com.youtopin.vaadin.formengine.options.OptionItem;
import com.youtopin.vaadin.formengine.registry.FieldInstance;
import com.youtopin.vaadin.formengine.binder.DynamicPropertyBag;

class BinderOrchestratorTest {

    @Test
    void convertsNumberField() throws ValidationException {
        BinderOrchestrator<TestBean> orchestrator = new BinderOrchestrator<>(TestBean.class, key -> key);
        NumberField field = new NumberField();
        FieldDefinition definition = new FieldDefinition(
                "amount",
                UiField.ComponentType.NUMBER,
                "amount",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.<ValidationDefinition>of(),
                List.<CrossFieldValidationDefinition>of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
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
        FieldDefinition definition = new FieldDefinition(
                "tags",
                UiField.ComponentType.MULTI_SELECT,
                "tags",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.<ValidationDefinition>of(),
                List.<CrossFieldValidationDefinition>of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
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
        FieldDefinition definition = new FieldDefinition(
                "amount",
                UiField.ComponentType.TEXT,
                "amount",
                "",
                "",
                "true",
                "required",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.<ValidationDefinition>of(),
                List.<CrossFieldValidationDefinition>of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
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
        FieldDefinition definition = new FieldDefinition(
                "amount",
                UiField.ComponentType.NUMBER,
                "amount",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.of(rule),
                List.<CrossFieldValidationDefinition>of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
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
        FieldDefinition definition = new FieldDefinition(
                "amount",
                UiField.ComponentType.TEXT,
                "amount",
                "",
                "",
                "true",
                "required",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.<ValidationDefinition>of(),
                List.<CrossFieldValidationDefinition>of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
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
    void evaluatesExpressionAgainstSiblingField() throws ValidationException {
        BinderOrchestrator<ConfirmBean> orchestrator = new BinderOrchestrator<>(ConfirmBean.class, key -> key);
        NumberField amountField = new NumberField();
        amountField.setValue(10d);
        FieldDefinition amountDefinition = new FieldDefinition(
                "amount",
                UiField.ComponentType.NUMBER,
                "amount",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.of(),
                List.of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
        orchestrator.bindField(new FieldInstance(amountField, amountField, List.of()), amountDefinition);

        NumberField confirmField = new NumberField();
        confirmField.setValue(10d);
        ValidationDefinition confirmationRule = new ValidationDefinition("match", "value == amount", List.of(), "");
        FieldDefinition confirmDefinition = new FieldDefinition(
                "confirmation",
                UiField.ComponentType.NUMBER,
                "confirmation",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.of(confirmationRule),
                List.of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
        orchestrator.bindField(new FieldInstance(confirmField, confirmField, List.of()), confirmDefinition);

        ConfirmBean bean = new ConfirmBean();
        orchestrator.writeBean(bean);
        assertThat(bean.amount).isEqualTo(10d);
        assertThat(bean.confirmation).isEqualTo(10d);

        confirmField.setValue(12d);
        ValidationException exception = assertThrows(ValidationException.class, () -> orchestrator.writeBean(bean));
        assertThat(exception.getValidationErrors()).isNotEmpty();
        assertThat(confirmField.isInvalid()).isTrue();
        assertThat(confirmField.getErrorMessage()).isEqualTo("match");
    }

    @Test
    void evaluatesExpressionAgainstDynamicBagEntry() {
        BinderOrchestrator<DynamicBagBean> orchestrator = new BinderOrchestrator<>(DynamicBagBean.class, key -> key);
        TextField fullName = new TextField();
        fullName.setValue("Alice");
        FieldDefinition fullNameDefinition = new FieldDefinition(
                "profile.fullName",
                UiField.ComponentType.TEXT,
                "profile.fullName",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.of(),
                List.of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
        orchestrator.bindField(new FieldInstance(fullName, fullName, List.of()), fullNameDefinition);

        TextField nickname = new TextField();
        nickname.setValue("Alice");
        ValidationDefinition nicknameRule = new ValidationDefinition("distinct", "value != profile.fullName", List.of(), "");
        FieldDefinition nicknameDefinition = new FieldDefinition(
                "profile.nickname",
                UiField.ComponentType.TEXT,
                "profile.nickname",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.of(nicknameRule),
                List.of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
        orchestrator.bindField(new FieldInstance(nickname, nickname, List.of()), nicknameDefinition);

        DynamicBagBean bean = new DynamicBagBean();
        ValidationException failure = assertThrows(ValidationException.class, () -> orchestrator.writeBean(bean));
        assertThat(failure.getValidationErrors()).isNotEmpty();
        assertThat(nickname.isInvalid()).isTrue();
        assertThat(nickname.getErrorMessage()).isEqualTo("distinct");

        nickname.clear();
        nickname.setValue("Al");
        fullName.setValue("Alice");
        nickname.setInvalid(false);
        assertThatCode(() -> orchestrator.writeBean(bean)).doesNotThrowAnyException();
        DynamicBagBean.NestedBag profile = (DynamicBagBean.NestedBag) bean.readDynamicProperty("profile");
        assertThat(profile.asMap()).containsEntry("fullName", "Alice").containsEntry("nickname", "Al");
    }

    @Test
    void skipsPrimitiveSetterWhenValueNull() throws ValidationException {
        BinderOrchestrator<PrimitiveBean> orchestrator = new BinderOrchestrator<>(PrimitiveBean.class, key -> key);
        IntegerField field = new IntegerField();
        FieldDefinition definition = new FieldDefinition(
                "count",
                UiField.ComponentType.INTEGER,
                "count",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.<ValidationDefinition>of(),
                List.<CrossFieldValidationDefinition>of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
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
        FieldDefinition definition = new FieldDefinition(
                "status",
                UiField.ComponentType.TEXT,
                "status",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new OptionsDefinition(false, UiOptions.ProviderType.STATIC, List.of(), "", "", "", "", false, "", true),
                List.<ValidationDefinition>of(),
                List.<CrossFieldValidationDefinition>of(),
                new SecurityDefinition("", "", List.of(), false),
                0,
                1,
                1);
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

    static class ConfirmBean {
        double amount;
        double confirmation;

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public double getConfirmation() {
            return confirmation;
        }

        public void setConfirmation(double confirmation) {
            this.confirmation = confirmation;
        }
    }

    static class DynamicBagBean implements DynamicPropertyBag {
        private final Map<String, Object> values = new LinkedHashMap<>();

        @Override
        public Object readDynamicProperty(String name) {
            if (name == null || name.isBlank()) {
                return null;
            }
            return values.computeIfAbsent(name, key -> new NestedBag());
        }

        @Override
        public boolean writeDynamicProperty(String name, Object value) {
            if (name == null || name.isBlank()) {
                return false;
            }
            values.put(name, value);
            return true;
        }

        static class NestedBag implements DynamicPropertyBag {
            private final Map<String, Object> values = new LinkedHashMap<>();

            @Override
            public Object readDynamicProperty(String name) {
                if (name == null || name.isBlank()) {
                    return null;
                }
                return values.get(name);
            }

            @Override
            public boolean writeDynamicProperty(String name, Object value) {
                if (name == null || name.isBlank()) {
                    return false;
                }
                values.put(name, value);
                return true;
            }

            Map<String, Object> asMap() {
                return values;
            }
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
