package com.youtopin.vaadin.formengine.definition;

import java.util.List;

import com.youtopin.vaadin.formengine.annotation.UiOptions;

/**
 * Definition of an option provider.
 */
public final class OptionsDefinition {

    private final boolean enabled;
    private final UiOptions.ProviderType providerType;
    private final List<String> entries;
    private final String enumType;
    private final String callbackRef;
    private final String remoteRef;
    private final String cascadeFrom;
    private final boolean allowCreate;
    private final String allowCreateFormId;
    private final boolean clientFilter;

    public OptionsDefinition(boolean enabled,
                             UiOptions.ProviderType providerType,
                             List<String> entries,
                             String enumType,
                             String callbackRef,
                             String remoteRef,
                             String cascadeFrom,
                             boolean allowCreate,
                             String allowCreateFormId,
                             boolean clientFilter) {
        this.enabled = enabled;
        this.providerType = providerType;
        this.entries = List.copyOf(entries);
        this.enumType = enumType == null ? "" : enumType;
        this.callbackRef = callbackRef == null ? "" : callbackRef;
        this.remoteRef = remoteRef == null ? "" : remoteRef;
        this.cascadeFrom = cascadeFrom == null ? "" : cascadeFrom;
        this.allowCreate = allowCreate;
        this.allowCreateFormId = allowCreateFormId == null ? "" : allowCreateFormId;
        this.clientFilter = clientFilter;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public UiOptions.ProviderType getProviderType() {
        return providerType;
    }

    public List<String> getEntries() {
        return entries;
    }

    public String getEnumType() {
        return enumType;
    }

    public String getCallbackRef() {
        return callbackRef;
    }

    public String getRemoteRef() {
        return remoteRef;
    }

    public String getCascadeFrom() {
        return cascadeFrom;
    }

    public boolean isAllowCreate() {
        return allowCreate;
    }

    public String getAllowCreateFormId() {
        return allowCreateFormId;
    }

    public boolean isClientFilter() {
        return clientFilter;
    }
}
