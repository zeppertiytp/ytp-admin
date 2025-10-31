package com.youtopin.vaadin.formengine.binder;

import java.io.Serializable;

/**
 * Allows beans with dynamic, late-bound properties to participate in form binding.
 *
 * <p>The default {@link BinderOrchestrator} relies on JavaBean getter/setter methods to
 * resolve property paths. Implementing this interface lets a bean expose additional
 * properties that are not known at compile time—for example fields described by a
 * runtime schema. When a segment in the property path cannot be mapped to a standard
 * accessor method, the orchestrator invokes {@link #readDynamicProperty(String)} or
 * {@link #writeDynamicProperty(String, Object)} instead.</p>
 */
public interface DynamicPropertyBag extends Serializable {

    /**
     * Reads the value of a dynamic property.
     *
     * @param name property identifier requested by the form engine
     * @return the associated value, or {@code null} when absent
     */
    Object readDynamicProperty(String name);

    /**
     * Writes a value to the dynamic property if the bag recognises the key.
     *
     * @param name  property identifier requested by the form engine
     * @param value value to associate with the property
     * @return {@code true} when the property was handled, {@code false} to fall back to
     *         standard JavaBean resolution
     */
    boolean writeDynamicProperty(String name, Object value);
}
