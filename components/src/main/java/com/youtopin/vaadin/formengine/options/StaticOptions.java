package com.youtopin.vaadin.formengine.options;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Static options defined via annotation entries.
 */
public final class StaticOptions implements OptionCatalog {

    private final List<OptionItem> items;

    public StaticOptions(String[] entries, Locale locale) {
        this.items = Arrays.stream(entries)
                .map(entry -> entry.split("\\|", 2))
                .map(parts -> new OptionItem(parts[0], parts.length > 1 ? parts[1] : parts[0]))
                .collect(Collectors.toList());
    }

    @Override
    public OptionPage fetch(SearchQuery query) {
        return OptionPage.of(items, items.size());
    }

    @Override
    public List<OptionItem> byIds(Collection<String> ids) {
        return items.stream().filter(item -> ids.contains(item.getId())).toList();
    }
}
