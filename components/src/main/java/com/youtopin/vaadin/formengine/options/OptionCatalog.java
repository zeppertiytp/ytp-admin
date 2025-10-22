package com.youtopin.vaadin.formengine.options;

import java.util.Collection;
import java.util.List;

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
    };
}
