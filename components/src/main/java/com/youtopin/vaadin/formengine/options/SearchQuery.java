package com.youtopin.vaadin.formengine.options;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;

/**
 * Encapsulates search parameters for option providers.
 */
@Getter
public final class SearchQuery {

    private final String search;
    private final int page;
    private final int pageSize;
    private final Locale locale;
    private final Map<String, Object> context;

    public SearchQuery(String search, int page, int pageSize, Locale locale, Map<String, Object> context) {
        this.search = search == null ? "" : search;
        this.page = page;
        this.pageSize = pageSize;
        this.locale = Objects.requireNonNull(locale, "locale");
        this.context = Map.copyOf(context);
    }

}
