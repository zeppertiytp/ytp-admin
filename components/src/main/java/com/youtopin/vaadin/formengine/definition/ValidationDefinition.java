package com.youtopin.vaadin.formengine.definition;

import java.util.List;
import java.util.Objects;

/**
 * Definition of a single validator.
 */
public final class ValidationDefinition {

    private final String messageKey;
    private final String expression;
    private final List<Class<?>> groups;
    private final String asyncValidatorBean;

    public ValidationDefinition(String messageKey, String expression, List<Class<?>> groups, String asyncValidatorBean) {
        this.messageKey = Objects.requireNonNull(messageKey, "messageKey");
        this.expression = expression == null ? "" : expression;
        this.groups = List.copyOf(groups);
        this.asyncValidatorBean = asyncValidatorBean == null ? "" : asyncValidatorBean;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getExpression() {
        return expression;
    }

    public List<Class<?>> getGroups() {
        return groups;
    }

    public String getAsyncValidatorBean() {
        return asyncValidatorBean;
    }
}
