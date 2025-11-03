package com.youtopin.vaadin.formengine.definition;

import java.util.List;
import java.util.Objects;

import lombok.Getter;

/**
 * Definition of a single validator.
 */
@Getter
public final class ValidationDefinition {

    private final String messageKey;
    private final String expression;
    private final List<Class<?>> groups;
    private final String asyncValidatorBean;
    private final String validatorBean;

    public ValidationDefinition(String messageKey, String expression, List<Class<?>> groups, String asyncValidatorBean) {
        this(messageKey, expression, groups, asyncValidatorBean, "");
    }

    public ValidationDefinition(String messageKey,
                                String expression,
                                List<Class<?>> groups,
                                String asyncValidatorBean,
                                String validatorBean) {
        this.messageKey = Objects.requireNonNull(messageKey, "messageKey");
        this.expression = expression == null ? "" : expression;
        this.groups = List.copyOf(groups);
        this.asyncValidatorBean = asyncValidatorBean == null ? "" : asyncValidatorBean;
        this.validatorBean = validatorBean == null ? "" : validatorBean;
    }

}
