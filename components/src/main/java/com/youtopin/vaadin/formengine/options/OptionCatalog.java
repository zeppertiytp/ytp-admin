package com.youtopin.vaadin.formengine.options;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Abstraction for option providers used by select like components.
 */
public interface OptionCatalog {

    /**
     * Fetches a page of options matching the provided query.
     *
     * @param query search request
     * @return page of options containing results and total count information
     */
    OptionPage fetch(SearchQuery query);

    /**
     * Resolves option items by their identifiers.
     *
     * @param ids identifiers to resolve
     * @return resolved options
     */
    List<OptionItem> byIds(Collection<String> ids);

    /**
     * Indicates whether this catalog can persist newly created items.
     *
     * @return {@code true} when {@link #create(String, Locale, Map)} is implemented.
     */
    default boolean supportsCreate() {
        return false;
    }

    /**
     * Persists a newly created option and returns the canonical representation.
     * Implementations that do not support creation should keep the default behaviour
     * which raises an {@link UnsupportedOperationException}.
     *
     * @param value   user provided value or identifier proposal
     * @param locale  active locale for label generation
     * @param context contextual information supplied by the form engine
     * @return persisted option payload
     */
    default OptionItem create(String value, Locale locale, Map<String, Object> context) {
        throw new UnsupportedOperationException("Creation is not supported by this catalog");
    }

    /**
     * Marker implementation used as placeholder when no options are configured.
     */
    OptionCatalog EMPTY = new OptionCatalog() {
        @Override
        public OptionPage fetch(SearchQuery query) {
            return OptionPage.empty();
        }

        @Override
        public List<OptionItem> byIds(Collection<String> ids) {
            return List.of();
        }

        @Override
        public boolean supportsCreate() {
            return false;
        }
    };
}
