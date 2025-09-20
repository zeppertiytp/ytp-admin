package com.example.adminpanel.application.pagination;

import java.util.Map;

/**
 * Functional interface used by {@link com.example.adminpanel.ui.component.FilterablePaginatedGrid} to fetch a page of
 * results and the total count in a single call.  Implementations can
 * retrieve data from a repository or external service and return both
 * the items for the current page and the overall number of items that
 * match the provided filters.
 *
 * @param <T> the type of items returned in the page
 */
@FunctionalInterface
public interface PageResultService<T> {
    /**
     * Fetch a page of items along with the total count.
     *
     * @param offset   the zero-based index of the first item to retrieve
     * @param limit    the maximum number of items to return
     * @param filters  a map of property names to filter values
     * @return a {@link PageResult} containing the items and total count
     */
    PageResult<T> fetchPage(int offset, int limit, Map<String, String> filters);
}