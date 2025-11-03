package com.youtopin.vaadin.formengine.definition;

import java.util.Objects;

import lombok.Getter;

/**
 * Describes a lifecycle hook registered on the form.
 */
@Getter
public final class LifecycleHookDefinition {

    private final String id;
    private final String beanName;
    private final String method;

    public LifecycleHookDefinition(String id, String beanName, String method) {
        this.id = Objects.requireNonNull(id, "id");
        this.beanName = beanName == null ? "" : beanName;
        this.method = method == null ? "" : method;
    }

}
