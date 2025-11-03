package com.youtopin.vaadin.formengine.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.youtopin.vaadin.formengine.annotation.UiForm;

/**
 * Runtime representation of an annotated {@link UiForm}. Instances are mutable to allow
 * post-scan adjustments while still guarding internal collections with defensive copies.
 */
public class FormDefinition implements Cloneable {

    private String id;
    private String titleKey;
    private String descriptionKey;
    private Class<?> beanType;
    private List<SectionDefinition> sections;
    private List<ActionDefinition> actions;
    private Map<String, LifecycleHookDefinition> lifecycleHooks;

    public FormDefinition(String id,
                          String titleKey,
                          String descriptionKey,
                          Class<?> beanType,
                          List<SectionDefinition> sections,
                          List<ActionDefinition> actions,
                          Map<String, LifecycleHookDefinition> lifecycleHooks) {
        setId(id);
        setTitleKey(titleKey);
        setDescriptionKey(descriptionKey);
        setBeanType(beanType);
        setSections(sections);
        setActions(actions);
        setLifecycleHooks(lifecycleHooks);
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

    public Class<?> getBeanType() {
        return beanType;
    }

    public List<SectionDefinition> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public List<ActionDefinition> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public Map<String, LifecycleHookDefinition> getLifecycleHooks() {
        return Collections.unmodifiableMap(lifecycleHooks);
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

    public void setBeanType(Class<?> beanType) {
        this.beanType = Objects.requireNonNull(beanType, "beanType");
    }

    public void setSections(List<SectionDefinition> sections) {
        Objects.requireNonNull(sections, "sections");
        this.sections = List.copyOf(sections);
    }

    public void setActions(List<ActionDefinition> actions) {
        Objects.requireNonNull(actions, "actions");
        this.actions = List.copyOf(actions);
    }

    public void setLifecycleHooks(Map<String, LifecycleHookDefinition> lifecycleHooks) {
        Objects.requireNonNull(lifecycleHooks, "lifecycleHooks");
        this.lifecycleHooks = Map.copyOf(lifecycleHooks);
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    public SectionDefinition findSection(String id) {
        return sections.stream().filter(section -> section.getId().equals(id)).findFirst().orElse(null);
    }

    public ActionDefinition findAction(String id) {
        return actions.stream().filter(action -> action.getId().equals(id)).findFirst().orElse(null);
    }

    public Map<String, FieldDefinition> indexFieldsByPath() {
        return sections.stream()
                .flatMap(section -> section.getGroups().stream())
                .flatMap(group -> group.getFields().stream())
                .collect(java.util.stream.Collectors.toUnmodifiableMap(FieldDefinition::getPath, field -> field));
    }

    public List<FieldDefinition> getAllFields() {
        return sections.stream()
                .flatMap(section -> section.getGroups().stream())
                .flatMap(group -> group.getFields().stream())
                .toList();
    }

    public List<SectionDefinition> getSectionsUnmodifiable() {
        return Collections.unmodifiableList(sections);
    }

    @Override
    public FormDefinition clone() {
        List<SectionDefinition> clonedSections = sections.stream()
                .map(section -> section == null ? null : section.clone())
                .toList();
        List<ActionDefinition> clonedActions = new ArrayList<>(actions);
        Map<String, LifecycleHookDefinition> clonedHooks = new LinkedHashMap<>(lifecycleHooks);
        return new FormDefinition(id, titleKey, descriptionKey, beanType, clonedSections, clonedActions, clonedHooks);
    }
}
