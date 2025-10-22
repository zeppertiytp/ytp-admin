package com.youtopin.vaadin.formengine.binder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.ValidationException;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;
import com.youtopin.vaadin.formengine.definition.OptionsDefinition;
import com.youtopin.vaadin.formengine.definition.SecurityDefinition;
import com.youtopin.vaadin.formengine.options.OptionItem;
import com.youtopin.vaadin.formengine.registry.FieldInstance;

class BinderOrchestratorTest {

    @Test
    void convertsNumberField() throws ValidationException {
        BinderOrchestrator<TestBean> orchestrator = new BinderOrchestrator<>(TestBean.class, Locale.US);
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
        BinderOrchestrator<TestBean> orchestrator = new BinderOrchestrator<>(TestBean.class, Locale.US);
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
}
