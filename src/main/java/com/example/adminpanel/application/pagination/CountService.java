package com.example.adminpanel.application.pagination;

import java.util.Map;

/**
 * Provides the total number of items available for the specified filters.
 * Implemented alongside {@link PageFetchService} to support pagination in
 * {@link com.example.adminpanel.ui.component.FilterablePaginatedGrid}.
 *
 * @param <T> the data item type
 */
@FunctionalInterface
public interface CountService<T> {
    /**
     * Counts the total number of items matching the given filters.
     *
     * @param filters a map from property name to filter value
     * @return the number of matching items
     */
    int count(Map<String, String> filters);
}