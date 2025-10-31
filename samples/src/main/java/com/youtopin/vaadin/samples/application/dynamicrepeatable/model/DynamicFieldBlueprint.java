package com.youtopin.vaadin.samples.application.dynamicrepeatable.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Describes a single dynamically generated field within the repeatable editor.
 */
public class DynamicFieldBlueprint implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String fieldKey = "";
    private String label = "";
    private String helper = "";
    private String placeholder = "";
    private FieldType fieldType = FieldType.TEXT;
    private boolean required = true;

    public DynamicFieldBlueprint() {
    }

    public DynamicFieldBlueprint(String fieldKey, String label, FieldType fieldType, boolean required) {
        setFieldKey(fieldKey);
        setLabel(label);
        setFieldType(fieldType);
        setRequired(required);
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = normaliseKey(fieldKey);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label == null ? "" : label.trim();
    }

    public String getHelper() {
        return helper;
    }

    public void setHelper(String helper) {
        this.helper = helper == null ? "" : helper.trim();
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder.trim();
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType == null ? FieldType.TEXT : fieldType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void copyFrom(DynamicFieldBlueprint other) {
        if (other == null) {
            return;
        }
        setFieldKey(other.getFieldKey());
        setLabel(other.getLabel());
        setHelper(other.getHelper());
        setPlaceholder(other.getPlaceholder());
        setFieldType(other.getFieldType());
        setRequired(other.isRequired());
    }

    public static String normaliseKey(String key) {
        if (key == null || key.isBlank()) {
            return "field";
        }
        String trimmed = key.trim();
        StringBuilder builder = new StringBuilder(trimmed.length());
        boolean lastWasSeparator = false;
        for (char ch : trimmed.toCharArray()) {
            if (Character.isLetterOrDigit(ch)) {
                builder.append(Character.toLowerCase(ch));
                lastWasSeparator = false;
            } else {
                if (!lastWasSeparator) {
                    builder.append('_');
                    lastWasSeparator = true;
                }
            }
        }
        String normalised = builder.toString().replaceAll("_+", "_");
        normalised = normalised.replaceAll("^_+|_+$", "");
        if (normalised.isBlank()) {
            return "field";
        }
        return normalised;
    }

    @Override
    public String toString() {
        return "DynamicFieldBlueprint{" +
                "fieldKey='" + fieldKey + '\'' +
                ", label='" + label + '\'' +
                ", helper='" + helper + '\'' +
                ", placeholder='" + placeholder + '\'' +
                ", fieldType=" + fieldType +
                ", required=" + required +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DynamicFieldBlueprint that)) {
            return false;
        }
        return required == that.required
                && Objects.equals(fieldKey, that.fieldKey)
                && Objects.equals(label, that.label)
                && Objects.equals(helper, that.helper)
                && Objects.equals(placeholder, that.placeholder)
                && fieldType == that.fieldType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldKey, label, helper, placeholder, fieldType, required);
    }

    public enum FieldType {
        TEXT,
        TEXT_AREA,
        INTEGER,
        NUMBER,
        EMAIL,
        DATE;

        @Override
        public String toString() {
            return switch (this) {
                case TEXT -> "Text";
                case TEXT_AREA -> "Multiline text";
                case INTEGER -> "Integer";
                case NUMBER -> "Number";
                case EMAIL -> "Email";
                case DATE -> "Date";
            };
        }
    }
}
