package com.youtopin.vaadin.component;

import lombok.Getter;

/**
 * Theme variants supported by {@link AppCard}. Mirrors the official Card
 * variants so the samples stay aligned with the design tokens.
 */
public enum AppCardVariant {
    ELEVATED("elevated"),
    OUTLINED("outlined"),
    HORIZONTAL("horizontal"),
    STRETCH_MEDIA("stretch-media"),
    COVER_MEDIA("cover-media");

    @Getter
    private final String variantName;

    AppCardVariant(String variantName) {
        this.variantName = variantName;
    }
}
