package com.youtopin.vaadin.formengine.definition;

import java.util.Objects;

import com.youtopin.vaadin.formengine.annotation.UiAction;

import lombok.Getter;

/**
 * Metadata for a form action (submit, approve, cancel...).
 */
@Getter
public final class ActionDefinition {

    private final String id;
    private final String labelKey;
    private final String descriptionKey;
    private final String visibleWhen;
    private final String enabledWhen;
    private final UiAction.Placement placement;
    private final String sectionId;
    private final UiAction.ActionType type;
    private final int order;
    private final SecurityDefinition securityDefinition;

    public ActionDefinition(String id,
                            String labelKey,
                            String descriptionKey,
                            String visibleWhen,
                            String enabledWhen,
                            UiAction.Placement placement,
                            String sectionId,
                            UiAction.ActionType type,
                            int order,
                            SecurityDefinition securityDefinition) {
        this.id = Objects.requireNonNull(id, "id");
        this.labelKey = labelKey == null ? id : labelKey;
        this.descriptionKey = descriptionKey == null ? "" : descriptionKey;
        this.visibleWhen = visibleWhen == null ? "" : visibleWhen;
        this.enabledWhen = enabledWhen == null ? "" : enabledWhen;
        this.placement = Objects.requireNonNull(placement, "placement");
        this.sectionId = sectionId == null ? "" : sectionId;
        this.type = Objects.requireNonNull(type, "type");
        this.order = order;
        this.securityDefinition = securityDefinition;
    }

}
