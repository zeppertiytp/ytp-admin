package com.youtopin.vaadin.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.TextField;

class GeneratedFormReadOnlyTest {

    @Test
    void updatesReadOnlyStateFromMetadata() {
        UI ui = new UI();
        ui.setLocale(Locale.ENGLISH);
        UI.setCurrent(ui);
        try {
            GeneratedForm form = new GeneratedForm("readonly_form.json", (formId, fields) -> java.util.Collections.emptyMap());

            Checkbox lockSection = findComponent(form, Checkbox.class, checkbox -> "Lock section".equals(checkbox.getLabel()));
            Checkbox lockLastName = findComponent(form, Checkbox.class,
                    checkbox -> "Lock last name".equals(checkbox.getLabel()));
            Checkbox lockContacts = findComponent(form, Checkbox.class,
                    checkbox -> "Lock contacts".equals(checkbox.getLabel()));
            TextField firstName = findComponent(form, TextField.class, field -> "First name".equals(field.getLabel()));
            TextField lastName = findComponent(form, TextField.class, field -> "Last name".equals(field.getLabel()));
            TextField contactName = findComponent(form, TextField.class, field -> "Contact name".equals(field.getLabel()));
            Button addContact = findButton(form, button -> {
                String text = button.getText();
                return "form.addItem".equals(text) || "Add item".equalsIgnoreCase(text);
            });

            assertThat(firstName.isReadOnly()).isFalse();
            assertThat(lastName.isReadOnly()).isFalse();
            assertThat(contactName.isReadOnly()).isFalse();
            assertThat(addContact.isEnabled()).isTrue();

            lockLastName.setValue(true);
            assertThat(lastName.isReadOnly()).isTrue();

            lockLastName.setValue(false);
            assertThat(lastName.isReadOnly()).isFalse();

            lockSection.setValue(true);
            assertThat(firstName.isReadOnly()).isTrue();
            assertThat(lastName.isReadOnly()).isTrue();

            lockSection.setValue(false);
            assertThat(firstName.isReadOnly()).isFalse();

            lockContacts.setValue(true);
            assertThat(contactName.isReadOnly()).isTrue();
            assertThat(addContact.isEnabled()).isFalse();

            lockContacts.setValue(false);
            assertThat(contactName.isReadOnly()).isFalse();
            assertThat(addContact.isEnabled()).isTrue();
        } finally {
            UI.setCurrent(null);
        }
    }

    private <T extends Component> T findComponent(Component root, Class<T> type, Predicate<T> predicate) {
        Optional<T> match = flatten(root)
                .filter(type::isInstance)
                .map(type::cast)
                .filter(predicate)
                .findFirst();
        return match.orElseThrow();
    }

    private Button findButton(Component root, Predicate<Button> predicate) {
        return findComponent(root, Button.class, predicate);
    }

    private java.util.stream.Stream<Component> flatten(Component root) {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(root), root.getChildren().flatMap(this::flatten));
    }
}
