package com.youtopin.vaadin.formengine.definition;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Definition of a form section.
 */
public class SectionDefinition implements Cloneable {

    private String id;
    private String titleKey;
    private String descriptionKey;
    private String visibleWhen;
    private String readOnlyWhen;
    private String securityGuard;
    private int order;
    private List<GroupDefinition> groups;

    public SectionDefinition(String id,
                             String titleKey,
                             String descriptionKey,
                             String visibleWhen,
                             String readOnlyWhen,
                             String securityGuard,
                             int order,
                             List<GroupDefinition> groups) {
        setId(id);
        setTitleKey(titleKey);
        setDescriptionKey(descriptionKey);
        setVisibleWhen(visibleWhen);
        setReadOnlyWhen(readOnlyWhen);
        setSecurityGuard(securityGuard);
        setOrder(order);
        setGroups(groups);
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
        return Collections.unmodifiableList(groups);
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = normalize(titleKey);
    }

    public void setDescriptionKey(String descriptionKey) {
        this.descriptionKey = normalize(descriptionKey);
    }

    public void setVisibleWhen(String visibleWhen) {
        this.visibleWhen = normalize(visibleWhen);
    }

    public void setReadOnlyWhen(String readOnlyWhen) {
        this.readOnlyWhen = normalize(readOnlyWhen);
    }

    public void setSecurityGuard(String securityGuard) {
        this.securityGuard = normalize(securityGuard);
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setGroups(List<GroupDefinition> groups) {
        Objects.requireNonNull(groups, "groups");
        this.groups = List.copyOf(groups);
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    @Override
    public SectionDefinition clone() {
        List<GroupDefinition> clonedGroups = groups.stream()
                .map(group -> group == null ? null : group.clone())
                .toList();
        return new SectionDefinition(id, titleKey, descriptionKey, visibleWhen, readOnlyWhen, securityGuard, order, clonedGroups);
    }
}
