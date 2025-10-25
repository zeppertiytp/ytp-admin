package com.youtopin.vaadin.formengine.definition;

import java.util.List;

/**
 * Security metadata resolved from {@link com.youtopin.vaadin.formengine.annotation.UiSecurity}.
 */
public final class SecurityDefinition {

    private final String guardId;
    private final String expression;
    private final List<String> requiredAuthorities;
    private final boolean showWhenDenied;

    public SecurityDefinition(String guardId, String expression, List<String> requiredAuthorities, boolean showWhenDenied) {
        this.guardId = guardId == null ? "" : guardId;
        this.expression = expression == null ? "" : expression;
        this.requiredAuthorities = List.copyOf(requiredAuthorities);
        this.showWhenDenied = showWhenDenied;
    }

    public String getGuardId() {
        return guardId;
    }

    public String getExpression() {
        return expression;
    }

    public List<String> getRequiredAuthorities() {
        return requiredAuthorities;
    }

    public boolean isShowWhenDenied() {
        return showWhenDenied;
    }
}
