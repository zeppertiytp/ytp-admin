package com.youtopin.vaadin.formengine.definition;

import java.util.Objects;

/**
 * Describes a lifecycle hook registered on the form.
 */
public final class LifecycleHookDefinition {

    private final String id;
    private final String beanName;
    private final String method;

    public LifecycleHookDefinition(String id, String beanName, String method) {
        this.id = Objects.requireNonNull(id, "id");
        this.beanName = beanName == null ? "" : beanName;
        this.method = method == null ? "" : method;
    }

    public String getId() {
        return id;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getMethod() {
        return method;
    }
}
