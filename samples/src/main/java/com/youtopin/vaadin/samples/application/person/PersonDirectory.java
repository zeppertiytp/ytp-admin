package com.youtopin.vaadin.samples.application.person;

import com.youtopin.vaadin.data.pagination.PageResultService;
import com.youtopin.vaadin.samples.domain.person.Person;

/**
 * Abstraction for retrieving {@link Person} information for read-only views.
 * The UI layer depends on this interface to comply with the dependency
 * inversion principle; concrete implementations can fetch data from
 * in-memory fixtures, databases or remote services without requiring
 * changes in the Vaadin views.
 */
public interface PersonDirectory extends PageResultService<Person> {
}
