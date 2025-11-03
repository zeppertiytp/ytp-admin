package com.youtopin.vaadin.data.pagination;

import java.util.List;

import lombok.Getter;

/**
 * Simple DTO representing a page of results along with the total count of
 * items matching the current filter.  It is used by the
 * {@link com.youtopin.vaadin.component.FilterablePaginatedGrid} to avoid making separate calls to fetch
 * data and to count the total number of items.
 *
 * @param <T> the type of items in the page
 */
@Getter
public class PageResult<T> {
    private final List<T> items;
    private final int totalCount;

    public PageResult(List<T> items, int totalCount) {
        this.items = items;
        this.totalCount = totalCount;
    }

}