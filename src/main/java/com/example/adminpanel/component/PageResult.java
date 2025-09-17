package com.example.adminpanel.component;

import java.util.List;

/**
 * Simple DTO representing a page of results along with the total count of
 * items matching the current filter.  It is used by the
 * {@link FilterablePaginatedGrid} to avoid making separate calls to fetch
 * data and to count the total number of items.
 *
 * @param <T> the type of items in the page
 */
public class PageResult<T> {
    private final List<T> items;
    private final int totalCount;

    public PageResult(List<T> items, int totalCount) {
        this.items = items;
        this.totalCount = totalCount;
    }

    public List<T> getItems() {
        return items;
    }

    public int getTotalCount() {
        return totalCount;
    }
}