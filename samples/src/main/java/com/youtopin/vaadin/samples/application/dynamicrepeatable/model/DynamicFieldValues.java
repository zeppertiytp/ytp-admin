package com.youtopin.vaadin.samples.application.dynamicrepeatable.model;

import com.youtopin.vaadin.formengine.binder.DynamicPropertyBag;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Map-backed holder for row values that exposes map entries as dynamic form properties.
 */
public class DynamicFieldValues implements DynamicPropertyBag, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, Object> values = new LinkedHashMap<>();

    @Override
    public Object readDynamicProperty(String name) {
        return values.get(name);
    }

    @Override
    public boolean writeDynamicProperty(String name, Object value) {
        values.put(name, value);
        return true;
    }

    public Map<String, Object> asMap() {
        return values;
    }

    public void ensureKeys(Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        values.keySet().retainAll(keys);
        for (String key : keys) {
            values.putIfAbsent(key, null);
        }
    }

    public Object get(String key) {
        return values.get(key);
    }

    public void set(String key, Object value) {
        values.put(key, value);
    }

    public DynamicFieldValues copy() {
        DynamicFieldValues copy = new DynamicFieldValues();
        copy.values.putAll(values);
        return copy;
    }

    @Override
    public String toString() {
        return "DynamicFieldValues{" + values + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DynamicFieldValues that)) {
            return false;
        }
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
