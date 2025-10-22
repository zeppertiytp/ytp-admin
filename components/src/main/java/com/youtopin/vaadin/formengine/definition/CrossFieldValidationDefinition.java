package com.youtopin.vaadin.formengine.definition;

import java.util.List;
import java.util.Objects;

/**
 * Definition of a cross field validation rule.
 */
public final class CrossFieldValidationDefinition {

    private final String expression;
    private final String messageKey;
    private final List<Class<?>> groups;
    private final List<String> targetPaths;

    public CrossFieldValidationDefinition(String expression,
                                          String messageKey,
                                          List<Class<?>> groups,
                                          List<String> targetPaths) {
        this.expression = Objects.requireNonNull(expression, "expression");
        this.messageKey = Objects.requireNonNull(messageKey, "messageKey");
        this.groups = List.copyOf(groups);
        this.targetPaths = List.copyOf(targetPaths);
    }

    public String getExpression() {
        return expression;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public List<Class<?>> getGroups() {
        return groups;
    }

    public List<String> getTargetPaths() {
        return targetPaths;
    }
}
