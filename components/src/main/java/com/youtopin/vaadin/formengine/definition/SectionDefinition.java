package com.youtopin.vaadin.formengine.definition;

import java.util.List;
import java.util.Objects;

/**
 * Definition of a form section.
 */
public final class SectionDefinition {

    private final String id;
    private final String titleKey;
    private final String descriptionKey;
    private final String visibleWhen;
    private final String readOnlyWhen;
    private final String securityGuard;
    private final int order;
    private final List<GroupDefinition> groups;

    public SectionDefinition(String id,
                             String titleKey,
                             String descriptionKey,
                             String visibleWhen,
                             String readOnlyWhen,
                             String securityGuard,
                             int order,
                             List<GroupDefinition> groups) {
        this.id = Objects.requireNonNull(id, "id");
        this.titleKey = titleKey == null ? "" : titleKey;
        this.descriptionKey = descriptionKey == null ? "" : descriptionKey;
        this.visibleWhen = visibleWhen == null ? "" : visibleWhen;
        this.readOnlyWhen = readOnlyWhen == null ? "" : readOnlyWhen;
        this.securityGuard = securityGuard == null ? "" : securityGuard;
        this.order = order;
        this.groups = List.copyOf(groups);
    }

    public String getId() {
        return id;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public String getVisibleWhen() {
        return visibleWhen;
    }

    public String getReadOnlyWhen() {
        return readOnlyWhen;
    }

    public String getSecurityGuard() {
        return securityGuard;
    }

    public int getOrder() {
        return order;
    }

    public List<GroupDefinition> getGroups() {
        return groups;
    }
}
