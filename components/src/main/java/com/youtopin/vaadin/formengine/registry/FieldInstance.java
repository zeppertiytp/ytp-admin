package com.youtopin.vaadin.formengine.registry;

import java.util.List;
import java.util.Objects;

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

    public FieldInstance(HasValue<?, ?> valueComponent, Component component, List<Component> additionalComponents) {
        this.valueComponent = Objects.requireNonNull(valueComponent, "valueComponent");
        this.component = Objects.requireNonNull(component, "component");
        this.additionalComponents = List.copyOf(additionalComponents);
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
}
