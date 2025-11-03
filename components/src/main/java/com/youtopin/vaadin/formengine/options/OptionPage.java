package com.youtopin.vaadin.formengine.options;

import java.util.List;

import lombok.Getter;

/**
 * Represents a page of options.
 */
@Getter
public final class OptionPage {

    private final List<OptionItem> items;
    private final int totalSize;

    public OptionPage(List<OptionItem> items, int totalSize) {
        this.items = List.copyOf(items);
        this.totalSize = totalSize;
    }

    public static OptionPage of(List<OptionItem> items, int totalSize) {
        return new OptionPage(items, totalSize);
    }

    public static OptionPage empty() {
        return new OptionPage(List.of(), 0);
    }

}
