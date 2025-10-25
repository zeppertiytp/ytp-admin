package com.youtopin.vaadin.formengine.options;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Wraps another catalog and injects context from parent field values.
 */
public final class CascadingOptions implements OptionCatalog {

    private final OptionCatalog delegate;
    private final Function<Map<String, Object>, Map<String, Object>> contextTransformer;

    public CascadingOptions(OptionCatalog delegate,
                            Function<Map<String, Object>, Map<String, Object>> contextTransformer) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.contextTransformer = Objects.requireNonNull(contextTransformer, "contextTransformer");
    }

    @Override
    public OptionPage fetch(SearchQuery query) {
        Map<String, Object> newContext = contextTransformer.apply(query.getContext());
        SearchQuery cascaded = new SearchQuery(query.getSearch(), query.getPage(), query.getPageSize(),
                query.getLocale(), newContext);
        return delegate.fetch(cascaded);
    }

    @Override
    public List<OptionItem> byIds(Collection<String> ids) {
        return delegate.byIds(ids);
    }
}
