package com.youtopin.vaadin.formengine.registry;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;

/**
 * Wrapper around the actual Vaadin component produced by the registry.
 */
public final class FieldInstance {

    private final HasValue<?, ?> valueComponent;
    private final Component component;
    private final List<Component> additionalComponents;
    private final ValidationHandler defaultValidationHandler;
    private ValidationHandler customValidationHandler;

    public FieldInstance(HasValue<?, ?> valueComponent,
                         Component component,
                         List<Component> additionalComponents) {
        this(valueComponent, component, additionalComponents, ValidationHandler.standard());
    }

    public FieldInstance(HasValue<?, ?> valueComponent,
                         Component component,
                         List<Component> additionalComponents,
                         ValidationHandler defaultValidationHandler) {
        this.valueComponent = Objects.requireNonNull(valueComponent, "valueComponent");
        this.component = Objects.requireNonNull(component, "component");
        this.additionalComponents = List.copyOf(additionalComponents);
        this.defaultValidationHandler = Objects.requireNonNull(defaultValidationHandler, "defaultValidationHandler");
    }

    public HasValue<?, ?> getValueComponent() {
        return valueComponent;
    }

    public Component getComponent() {
        return component;
    }

    public List<Component> getAdditionalComponents() {
        return additionalComponents;
    }

    public ValidationHandler getDefaultValidationHandler() {
        return defaultValidationHandler;
    }

    public Optional<ValidationHandler> getCustomValidationHandler() {
        return Optional.ofNullable(customValidationHandler);
    }

    public void setCustomValidationHandler(ValidationHandler handler) {
        this.customValidationHandler = handler;
    }

    /**
     * Strategy interface used to clear and apply validation feedback for a field.
     */
    public interface ValidationHandler {

        /**
         * Clears any validation feedback that was previously applied.
         *
         * @param instance owning field instance
         */
        void clear(FieldInstance instance);

        /**
         * Applies validation feedback for the given message.
         *
         * @param instance owning field instance
         * @param message  localized message to display
         * @return {@code true} when the message was handled, {@code false} to fall back to the default strategy
         */
        boolean apply(FieldInstance instance, String message);

        static ValidationHandler standard() {
            return new DefaultValidationHandler();
        }
    }

    private static final class DefaultValidationHandler implements ValidationHandler {

        @Override
        public void clear(FieldInstance instance) {
            HasValue<?, ?> valueComponent = instance.getValueComponent();
            if (valueComponent instanceof com.vaadin.flow.component.HasValidation hasValidation) {
                hasValidation.setErrorMessage("");
                hasValidation.setInvalid(false);
            }
            if (valueComponent instanceof Component component) {
                component.getElement().removeAttribute("aria-invalid");
                component.getElement().getThemeList().remove("error");
                component.getElement().setProperty("title", "");
            }
            Component component = instance.getComponent();
            if (component instanceof com.vaadin.flow.component.HasValidation hasValidation) {
                hasValidation.setErrorMessage("");
                hasValidation.setInvalid(false);
            }
            component.getElement().removeAttribute("aria-invalid");
            component.getElement().getThemeList().remove("error");
            component.getElement().setProperty("title", "");
        }

        @Override
        public boolean apply(FieldInstance instance, String message) {
            HasValue<?, ?> valueComponent = instance.getValueComponent();
            if (valueComponent instanceof com.vaadin.flow.component.HasValidation hasValidation) {
                hasValidation.setErrorMessage(message);
                hasValidation.setInvalid(true);
                return true;
            }
            Component component = instance.getComponent();
            if (component instanceof com.vaadin.flow.component.HasValidation hasValidation) {
                hasValidation.setErrorMessage(message);
                hasValidation.setInvalid(true);
                return true;
            }
            Component fallback = valueComponent instanceof Component valueAsComponent ? valueAsComponent : instance.getComponent();
            fallback.getElement().setProperty("title", message);
            fallback.getElement().setAttribute("aria-invalid", "true");
            fallback.getElement().getThemeList().add("error");
            return true;
        }
    }
}
