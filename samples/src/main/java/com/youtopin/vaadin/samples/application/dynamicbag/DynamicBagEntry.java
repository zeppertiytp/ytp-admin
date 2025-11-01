package com.youtopin.vaadin.samples.application.dynamicbag;

import com.youtopin.vaadin.formengine.binder.DynamicPropertyBag;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Repeatable entry backed by nested {@link DynamicPropertyBag} maps.
 */
public class DynamicBagEntry implements DynamicPropertyBag, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, Object> groups = new LinkedHashMap<>();

    public DynamicBagEntry() {
        // Pre-create the known groups so nested property paths resolve automatically.
        ensureGroup("profile");
        ensureGroup("address");
        ensureGroup("channels");
    }

    public DynamicBagValues ensureGroup(String name) {
        Objects.requireNonNull(name, "name");
        return (DynamicBagValues) groups.computeIfAbsent(name, key -> new DynamicBagValues());
    }

    @Override
    public Object readDynamicProperty(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return groups.computeIfAbsent(name, key -> new DynamicBagValues());
    }

    @Override
    public boolean writeDynamicProperty(String name, Object value) {
        if (name == null || name.isBlank()) {
            return false;
        }
        groups.put(name, value);
        return true;
    }

    public Map<String, Object> asSerializableMap() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        groups.forEach((key, value) -> {
            if (value instanceof DynamicBagValues dynamicValues) {
                snapshot.put(key, new LinkedHashMap<>(dynamicValues.asMap()));
            } else {
                snapshot.put(key, value);
            }
        });
        return snapshot;
    }
}
