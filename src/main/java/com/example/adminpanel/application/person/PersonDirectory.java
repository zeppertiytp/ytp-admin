package com.example.adminpanel.application.person;

import com.example.adminpanel.application.pagination.PageResultService;
import com.example.adminpanel.domain.person.Person;

/**
 * Abstraction for retrieving {@link Person} information for read-only views.
 * The UI layer depends on this interface to comply with the dependency
 * inversion principle; concrete implementations can fetch data from
 * in-memory fixtures, databases or remote services without requiring
 * changes in the Vaadin views.
 */
public interface PersonDirectory extends PageResultService<Person> {
}
