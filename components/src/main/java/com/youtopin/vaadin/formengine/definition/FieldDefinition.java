package com.youtopin.vaadin.formengine.definition;

import java.util.List;
import java.util.Objects;

import com.youtopin.vaadin.formengine.annotation.UiField;

/**
 * Definition for a field discovered from {@link UiField}.
 */
public final class FieldDefinition {

    private final String path;
    private final UiField.ComponentType componentType;
    private final String labelKey;
    private final String helperKey;
    private final String placeholderKey;
    private final String requiredWhen;
    private final String requiredMessageKey;
    private final String visibleWhen;
    private final String enabledWhen;
    private final String readOnlyWhen;
    private final String defaultValue;
    private final OptionsDefinition optionsDefinition;
    private final List<ValidationDefinition> validations;
    private final List<CrossFieldValidationDefinition> crossFieldValidations;
    private final SecurityDefinition securityDefinition;
    private final int order;
    private final int colSpan;
    private final int rowSpan;

    public FieldDefinition(String path,
                           UiField.ComponentType componentType,
                           String labelKey,
                           String helperKey,
                           String placeholderKey,
                           String requiredWhen,
                           String requiredMessageKey,
                           String visibleWhen,
                           String enabledWhen,
                           String readOnlyWhen,
                           String defaultValue,
                           OptionsDefinition optionsDefinition,
                           List<ValidationDefinition> validations,
                           List<CrossFieldValidationDefinition> crossFieldValidations,
                           SecurityDefinition securityDefinition,
                           int order,
                           int colSpan,
                           int rowSpan) {
        this.path = Objects.requireNonNull(path, "path");
        this.componentType = Objects.requireNonNull(componentType, "componentType");
        this.labelKey = Objects.requireNonNull(labelKey, "labelKey");
        this.helperKey = helperKey == null ? "" : helperKey;
        this.placeholderKey = placeholderKey == null ? "" : placeholderKey;
        this.requiredWhen = requiredWhen == null ? "" : requiredWhen;
        this.requiredMessageKey = requiredMessageKey == null ? "" : requiredMessageKey;
        this.visibleWhen = visibleWhen == null ? "" : visibleWhen;
        this.enabledWhen = enabledWhen == null ? "" : enabledWhen;
        this.readOnlyWhen = readOnlyWhen == null ? "" : readOnlyWhen;
        this.defaultValue = defaultValue == null ? "" : defaultValue;
        this.optionsDefinition = optionsDefinition;
        this.validations = List.copyOf(validations);
        this.crossFieldValidations = List.copyOf(crossFieldValidations);
        this.securityDefinition = securityDefinition;
        this.order = order;
        this.colSpan = Math.max(1, colSpan);
        this.rowSpan = Math.max(1, rowSpan);
    }

    public String getPath() {
        return path;
    }

    public UiField.ComponentType getComponentType() {
        return componentType;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public String getHelperKey() {
        return helperKey;
    }

    public String getPlaceholderKey() {
        return placeholderKey;
    }

    public String getRequiredWhen() {
        return requiredWhen;
    }

    public String getRequiredMessageKey() {
        return requiredMessageKey;
    }

    public String getVisibleWhen() {
        return visibleWhen;
    }

    public String getEnabledWhen() {
        return enabledWhen;
    }

    public String getReadOnlyWhen() {
        return readOnlyWhen;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public OptionsDefinition getOptionsDefinition() {
        return optionsDefinition;
    }

    public List<ValidationDefinition> getValidations() {
        return validations;
    }

    public List<CrossFieldValidationDefinition> getCrossFieldValidations() {
        return crossFieldValidations;
    }

    public SecurityDefinition getSecurityDefinition() {
        return securityDefinition;
    }

    public int getOrder() {
        return order;
    }

    public int getColSpan() {
        return colSpan;
    }

    public int getRowSpan() {
        return rowSpan;
    }
}
