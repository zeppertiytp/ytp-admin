package com.youtopin.vaadin.formengine.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.youtopin.vaadin.formengine.annotation.UiField;

/**
 * Definition for a field discovered from {@link UiField}.
 */
public class FieldDefinition implements Cloneable {

    private String path;
    private UiField.ComponentType componentType;
    private String labelKey;
    private String helperKey;
    private String placeholderKey;
    private String requiredWhen;
    private String requiredMessageKey;
    private String visibleWhen;
    private String enabledWhen;
    private String readOnlyWhen;
    private String defaultValue;
    private OptionsDefinition optionsDefinition;
    private List<ValidationDefinition> validations;
    private List<CrossFieldValidationDefinition> crossFieldValidations;
    private SecurityDefinition securityDefinition;
    private int order;
    private int colSpan;
    private int rowSpan;

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
        setPath(path);
        setComponentType(componentType);
        setLabelKey(labelKey);
        setHelperKey(helperKey);
        setPlaceholderKey(placeholderKey);
        setRequiredWhen(requiredWhen);
        setRequiredMessageKey(requiredMessageKey);
        setVisibleWhen(visibleWhen);
        setEnabledWhen(enabledWhen);
        setReadOnlyWhen(readOnlyWhen);
        setDefaultValue(defaultValue);
        setOptionsDefinition(optionsDefinition);
        setValidations(validations);
        setCrossFieldValidations(crossFieldValidations);
        setSecurityDefinition(securityDefinition);
        setOrder(order);
        setColSpan(colSpan);
        setRowSpan(rowSpan);
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
        return Collections.unmodifiableList(validations);
    }

    public List<CrossFieldValidationDefinition> getCrossFieldValidations() {
        return Collections.unmodifiableList(crossFieldValidations);
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

    public void setPath(String path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    public void setComponentType(UiField.ComponentType componentType) {
        this.componentType = Objects.requireNonNull(componentType, "componentType");
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = Objects.requireNonNull(labelKey, "labelKey");
    }

    public void setHelperKey(String helperKey) {
        this.helperKey = normalize(helperKey);
    }

    public void setPlaceholderKey(String placeholderKey) {
        this.placeholderKey = normalize(placeholderKey);
    }

    public void setRequiredWhen(String requiredWhen) {
        this.requiredWhen = normalize(requiredWhen);
    }

    public void setRequiredMessageKey(String requiredMessageKey) {
        this.requiredMessageKey = normalize(requiredMessageKey);
    }

    public void setVisibleWhen(String visibleWhen) {
        this.visibleWhen = normalize(visibleWhen);
    }

    public void setEnabledWhen(String enabledWhen) {
        this.enabledWhen = normalize(enabledWhen);
    }

    public void setReadOnlyWhen(String readOnlyWhen) {
        this.readOnlyWhen = normalize(readOnlyWhen);
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = normalize(defaultValue);
    }

    public void setOptionsDefinition(OptionsDefinition optionsDefinition) {
        this.optionsDefinition = optionsDefinition;
    }

    public void setValidations(List<ValidationDefinition> validations) {
        Objects.requireNonNull(validations, "validations");
        this.validations = List.copyOf(validations);
    }

    public void setCrossFieldValidations(List<CrossFieldValidationDefinition> crossFieldValidations) {
        Objects.requireNonNull(crossFieldValidations, "crossFieldValidations");
        this.crossFieldValidations = List.copyOf(crossFieldValidations);
    }

    public void setSecurityDefinition(SecurityDefinition securityDefinition) {
        this.securityDefinition = securityDefinition;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setColSpan(int colSpan) {
        this.colSpan = Math.max(1, colSpan);
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = Math.max(1, rowSpan);
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    @Override
    public FieldDefinition clone() {
        List<ValidationDefinition> clonedValidations = new ArrayList<>(validations);
        List<CrossFieldValidationDefinition> clonedCrossFieldValidations = new ArrayList<>(crossFieldValidations);
        return new FieldDefinition(path, componentType, labelKey, helperKey, placeholderKey, requiredWhen,
                requiredMessageKey, visibleWhen, enabledWhen, readOnlyWhen, defaultValue, optionsDefinition,
                clonedValidations, clonedCrossFieldValidations, securityDefinition, order, colSpan, rowSpan);
    }
}
