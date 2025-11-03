package com.youtopin.vaadin.formengine.options;

import java.util.Map;
import java.util.Objects;

import lombok.Getter;

/**
 * Represents a selectable option with both internal value and localized label.
 */
@Getter
public final class OptionItem {

    private final String id;
    private final String label;
    private final Map<String, Object> payload;

    public OptionItem(String id, String label) {
        this(id, label, Map.of());
    }

    public OptionItem(String id, String label, Map<String, Object> payload) {
        this.id = Objects.requireNonNull(id, "id");
        this.label = Objects.requireNonNull(label, "label");
        this.payload = Map.copyOf(payload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OptionItem optionItem)) {
            return false;
        }
        return id.equals(optionItem.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return label;
    }
}
