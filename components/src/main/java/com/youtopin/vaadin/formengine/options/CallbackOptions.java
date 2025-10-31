package com.youtopin.vaadin.formengine.options;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Delegates option resolution to a callback.
 */
public final class CallbackOptions implements OptionCatalog {

    private final Function<SearchQuery, OptionPage> fetcher;
    private final Function<Collection<String>, List<OptionItem>> resolver;
    private final CreationCallback creator;

    public CallbackOptions(Function<SearchQuery, OptionPage> fetcher,
                           Function<Collection<String>, List<OptionItem>> resolver) {
        this(fetcher, resolver, null);
    }

    public CallbackOptions(Function<SearchQuery, OptionPage> fetcher,
                           Function<Collection<String>, List<OptionItem>> resolver,
                           CreationCallback creator) {
        this.fetcher = Objects.requireNonNull(fetcher, "fetcher");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.creator = creator;
    }

    @Override
    public OptionPage fetch(SearchQuery query) {
        return fetcher.apply(query);
    }

    @Override
    public List<OptionItem> byIds(Collection<String> ids) {
        return resolver.apply(ids);
    }

    @Override
    public boolean supportsCreate() {
        return creator != null;
    }

    @Override
    public OptionItem create(String value, Locale locale, Map<String, Object> context) {
        if (!supportsCreate()) {
            return OptionCatalog.super.create(value, locale, context);
        }
        return creator.create(value, locale, context);
    }

    @FunctionalInterface
    public interface CreationCallback {
        OptionItem create(String value, Locale locale, Map<String, Object> context);
    }
}
