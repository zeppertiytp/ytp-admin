package com.youtopin.vaadin.formengine.definition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.youtopin.vaadin.formengine.annotation.UiForm;

/**
 * Immutable runtime representation of an annotated {@link UiForm}.
 */
public final class FormDefinition {

    private final String id;
    private final String titleKey;
    private final String descriptionKey;
    private final Class<?> beanType;
    private final List<SectionDefinition> sections;
    private final List<ActionDefinition> actions;
    private final Map<String, LifecycleHookDefinition> lifecycleHooks;

    public FormDefinition(String id,
                          String titleKey,
                          String descriptionKey,
                          Class<?> beanType,
                          List<SectionDefinition> sections,
                          List<ActionDefinition> actions,
                          Map<String, LifecycleHookDefinition> lifecycleHooks) {
        this.id = Objects.requireNonNull(id, "id");
        this.titleKey = titleKey == null ? "" : titleKey;
        this.descriptionKey = descriptionKey == null ? "" : descriptionKey;
        this.beanType = Objects.requireNonNull(beanType, "beanType");
        this.sections = List.copyOf(sections);
        this.actions = List.copyOf(actions);
        this.lifecycleHooks = Map.copyOf(lifecycleHooks);
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
        return sections;
    }

    public List<ActionDefinition> getActions() {
        return actions;
    }

    public Map<String, LifecycleHookDefinition> getLifecycleHooks() {
        return lifecycleHooks;
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
}
