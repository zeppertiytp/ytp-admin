package com.youtopin.vaadin.formengine.options;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Option provider deriving values from an enum type.
 */
public final class EnumOptions implements OptionCatalog {

    private final List<OptionItem> items;

    public EnumOptions(Class<? extends Enum<?>> enumType, Locale locale) {
        Enum<?>[] constants = enumType.getEnumConstants();
        this.items = java.util.Arrays.stream(constants)
                .map(value -> new OptionItem(value.name(), value.toString()))
                .toList();
    }

    @Override
    public OptionPage fetch(SearchQuery query) {
        return OptionPage.of(items, items.size());
    }

    @Override
    public List<OptionItem> byIds(Collection<String> ids) {
        return items.stream().filter(item -> ids.contains(item.getId())).toList();
    }

    @Override
    public boolean supportsCreate() {
        return false;
    }

    @Override
    public OptionItem create(String value, Locale locale, Map<String, Object> context) {
        return OptionCatalog.super.create(value, locale, context);
    }
}
