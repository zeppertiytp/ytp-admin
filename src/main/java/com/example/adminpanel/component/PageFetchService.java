package com.example.adminpanel.component;

import java.util.List;
import java.util.Map;

/**
 * Functional interface used by {@link FilterablePaginatedGrid} to fetch
 * data pages from a backend.  Implementations should apply the
 * supplied filters when retrieving results and return only the
 * requested slice defined by {@code offset} and {@code limit}.
 *
 * @param <T> the type of data items
 */
@FunctionalInterface
public interface PageFetchService<T> {
    /**
     * Fetches a page of data applying the given filters.
     *
     * @param offset the index of the first item in the page (zero-based)
     * @param limit the maximum number of items to return
     * @param filters a map from property name to filter value
     * @return a list of items; size may be less than {@code limit}
     */
    List<T> fetch(int offset, int limit, Map<String, String> filters);
}