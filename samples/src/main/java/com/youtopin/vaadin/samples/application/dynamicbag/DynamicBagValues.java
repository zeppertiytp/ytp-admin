package com.youtopin.vaadin.samples.application.dynamicbag;

import com.youtopin.vaadin.formengine.binder.DynamicPropertyBag;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Map-backed holder that exposes arbitrary keys as dynamic form properties.
 */
public class DynamicBagValues implements DynamicPropertyBag, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    public Map<String, Object> asMap() {
        return values;
    }

    public Object get(String key) {
        return values.get(key);
    }

    public void set(String key, Object value) {
        values.put(key, value);
    }

    @Override
    public String toString() {
        return "DynamicBagValues{" + values + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DynamicBagValues that)) {
            return false;
        }
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
