package com.youtopin.vaadin.formengine.definition;

import java.util.List;

import lombok.Getter;

/**
 * Security metadata resolved from {@link com.youtopin.vaadin.formengine.annotation.UiSecurity}.
 */
@Getter
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

}
