package com.youtopin.vaadin.samples.infrastructure.person;

import com.youtopin.vaadin.data.pagination.PageResult;
import com.youtopin.vaadin.samples.application.person.PersonDirectory;
import com.youtopin.vaadin.samples.domain.person.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides mock data for {@link Person} entities.  Implements the
 * {@link PersonDirectory} contract so Vaadin views can operate solely on
 * interfaces while this class offers an in-memory data source that can be
 * swapped out for database-backed implementations.
 */
@Slf4j
@Service
public class InMemoryPersonDirectory implements PersonDirectory {
    private final List<Person> people;

    public InMemoryPersonDirectory() {
        people = new ArrayList<>();
        // Populate mock data
        people.add(new Person("John", "Doe", "john.doe@example.com", 28, "New York", "USA"));
        people.add(new Person("Jane", "Smith", "jane.smith@example.com", 34, "London", "UK"));
        people.add(new Person("Ahmad", "Rashidi", "ahmad.rashidi@example.com", 42, "Tehran", "Iran"));
        people.add(new Person("Sara", "Amini", "sara.amini@example.com", 25, "Mashhad", "Iran"));
        people.add(new Person("Michael", "Brown", "michael.brown@example.com", 31, "Los Angeles", "USA"));
        people.add(new Person("Fatemeh", "Ghafari", "fatemeh.ghafari@example.com", 29, "Shiraz", "Iran"));
        // Add more entries to demonstrate pagination
        for (int i = 0; i < 50; i++) {
            String country;
            String city;
            switch (i % 3) {
                case 0 -> {
                    country = "USA";
                    city = (i % 2 == 0) ? "New York" : "Los Angeles";
                }
                case 1 -> {
                    country = "UK";
                    city = "London";
                }
                default -> {
                    country = "Iran";
                    city = switch (i % 3) {
                        case 0 -> "Tehran";
                        case 1 -> "Mashhad";
                        default -> "Shiraz";
                    };
                }
            }
            people.add(new Person("User" + i, "Test", "user" + i + "@example.com", 20 + (i % 30), city, country));
        }
        log.info("Initialised in-memory person directory with {} demo entries", people.size());
    }

    /**
     * Convenience method that exposes the raw item list for scenarios where
     * pagination is handled outside the {@link PageResult} abstraction.
     */
    public List<Person> fetch(int offset, int limit, Map<String, String> filters) {
        Map<String, String> safeFilters = filters != null ? filters : Map.of();
        List<Person> filtered = applyFilters(safeFilters);
        List<Person> result = filtered.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
        log.debug("Fetched {} person record(s) from offset {} with limit {} using {} filter(s)",
                result.size(), offset, limit, safeFilters.size());
        return result;
    }

    /**
     * Returns the total number of items after the provided filters have been
     * applied.  Useful when consumers need the count but do not work with
     * {@link PageResult}.
     */
    public int count(Map<String, String> filters) {
        Map<String, String> safeFilters = filters != null ? filters : Map.of();
        int count = applyFilters(safeFilters).size();
        log.debug("Counted {} person record(s) using {} filter(s)", count, safeFilters.size());
        return count;
    }

    private List<Person> applyFilters(Map<String, String> filters) {
        if (filters.isEmpty()) {
            return people;
        }

        Integer ageFilter = null;
        Integer minAgeFilter = null;
        Integer maxAgeFilter = null;

        if (filters.containsKey("age")) {
            ageFilter = parseIntegerFilter(filters.get("age"), "age");
            if (ageFilter == null && hasText(filters.get("age"))) {
                return List.of();
            }
        }
        if (filters.containsKey("age.min")) {
            minAgeFilter = parseIntegerFilter(filters.get("age.min"), "minimum age");
            if (minAgeFilter == null && hasText(filters.get("age.min"))) {
                return List.of();
            }
        }
        if (filters.containsKey("age.max")) {
            maxAgeFilter = parseIntegerFilter(filters.get("age.max"), "maximum age");
            if (maxAgeFilter == null && hasText(filters.get("age.max"))) {
                return List.of();
            }
        }

        String firstNameFilter = normalize(filters.get("firstName"));
        String lastNameFilter = normalize(filters.get("lastName"));
        String emailFilter = normalize(filters.get("email"));
        String cityFilter = normalize(filters.get("city"));
        String countryFilter = normalize(filters.get("country"));

        Integer exactAge = ageFilter;
        Integer minAge = minAgeFilter;
        Integer maxAge = maxAgeFilter;

        return people.stream().filter(person -> {
            if (firstNameFilter != null) {
                String firstName = person.getFirstName();
                if (firstName == null || !firstName.toLowerCase().contains(firstNameFilter)) {
                    return false;
                }
            }
            if (lastNameFilter != null) {
                String lastName = person.getLastName();
                if (lastName == null || !lastName.toLowerCase().contains(lastNameFilter)) {
                    return false;
                }
            }
            if (emailFilter != null) {
                String email = person.getEmail();
                if (email == null || !email.toLowerCase().contains(emailFilter)) {
                    return false;
                }
            }
            if (exactAge != null) {
                Integer age = person.getAge();
                if (age == null || age != exactAge) {
                    return false;
                }
            }
            if (minAge != null) {
                Integer age = person.getAge();
                if (age == null || age < minAge) {
                    return false;
                }
            }
            if (maxAge != null) {
                Integer age = person.getAge();
                if (age == null || age > maxAge) {
                    return false;
                }
            }
            if (cityFilter != null) {
                String city = person.getCity();
                if (city == null || !city.toLowerCase().equals(cityFilter)) {
                    return false;
                }
            }
            if (countryFilter != null) {
                String country = person.getCountry();
                if (country == null || !country.toLowerCase().equals(countryFilter)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    private Integer parseIntegerFilter(String rawValue, String description) {
        if (!hasText(rawValue)) {
            return null;
        }
        String trimmed = rawValue.trim();
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            log.warn("Ignoring non-numeric {} filter '{}'", description, rawValue);
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalize(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    @Override
    public PageResult<Person> fetchPage(int offset, int limit, Map<String, String> filters) {
        Map<String, String> safeFilters = filters != null ? filters : Map.of();
        List<Person> filtered = applyFilters(safeFilters);
        int total = filtered.size();
        List<Person> items = filtered.stream().skip(offset).limit(limit).collect(Collectors.toList());
        log.debug("Returning page with {} item(s) (offset={}, limit={}, total={})", items.size(), offset, limit, total);
        return new PageResult<>(items, total);
    }
}
