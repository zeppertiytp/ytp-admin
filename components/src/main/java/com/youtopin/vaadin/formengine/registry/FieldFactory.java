package com.youtopin.vaadin.formengine.registry;

import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;

/**
 * Factory for individual field components.
 */
public interface FieldFactory {

    /**
     * Builds a component for the supplied field definition.
     *
     * @param definition field metadata
     * @param context rendering context
     * @return configured instance wrapping the component and binder target
     */
    FieldInstance create(FieldDefinition definition, FieldFactoryContext context);
}
