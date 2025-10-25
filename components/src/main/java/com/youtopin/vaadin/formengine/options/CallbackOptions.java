package com.youtopin.vaadin.formengine.options;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Delegates option resolution to a callback.
 */
public final class CallbackOptions implements OptionCatalog {

    private final Function<SearchQuery, OptionPage> fetcher;
    private final Function<Collection<String>, List<OptionItem>> resolver;

    public CallbackOptions(Function<SearchQuery, OptionPage> fetcher,
                           Function<Collection<String>, List<OptionItem>> resolver) {
        this.fetcher = Objects.requireNonNull(fetcher, "fetcher");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    @Override
    public OptionPage fetch(SearchQuery query) {
        return fetcher.apply(query);
    }

    @Override
    public List<OptionItem> byIds(Collection<String> ids) {
        return resolver.apply(ids);
    }
}
