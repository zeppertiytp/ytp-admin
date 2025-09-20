package com.example.adminpanel.infrastructure.person;

import com.example.adminpanel.application.pagination.PageResult;
import com.example.adminpanel.application.person.PersonDirectory;
import com.example.adminpanel.domain.person.Person;
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
    }

    /**
     * Convenience method that exposes the raw item list for scenarios where
     * pagination is handled outside the {@link PageResult} abstraction.
     */
    public List<Person> fetch(int offset, int limit, Map<String, String> filters) {
        return applyFilters(filters).stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Returns the total number of items after the provided filters have been
     * applied.  Useful when consumers need the count but do not work with
     * {@link PageResult}.
     */
    public int count(Map<String, String> filters) {
        return applyFilters(filters).size();
    }

    private List<Person> applyFilters(Map<String, String> filters) {
        return people.stream().filter(person -> {
            boolean matches = true;
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String property = entry.getKey();
                String value = entry.getValue().toLowerCase();
                switch (property) {
                    case "firstName" ->
                            matches &= person.getFirstName() != null && person.getFirstName().toLowerCase().contains(value);
                    case "lastName" ->
                            matches &= person.getLastName() != null && person.getLastName().toLowerCase().contains(value);
                    case "email" ->
                            matches &= person.getEmail() != null && person.getEmail().toLowerCase().contains(value);
                    case "age" -> {
                        try {
                            int intVal = Integer.parseInt(value);
                            matches &= person.getAge() != null && person.getAge() == intVal;
                        } catch (NumberFormatException e) {
                            matches = false;
                        }
                    }
                    case "age.min" -> {
                        try {
                            int minVal = Integer.parseInt(value);
                            matches &= person.getAge() != null && person.getAge() >= minVal;
                        } catch (NumberFormatException e) {
                            matches = false;
                        }
                    }
                    case "age.max" -> {
                        try {
                            int maxVal = Integer.parseInt(value);
                            matches &= person.getAge() != null && person.getAge() <= maxVal;
                        } catch (NumberFormatException e) {
                            matches = false;
                        }
                    }
                    case "city" ->
                            matches &= person.getCity() != null && person.getCity().equalsIgnoreCase(value);
                    case "country" ->
                            matches &= person.getCountry() != null && person.getCountry().equalsIgnoreCase(value);
                    default -> {
                    }
                }
                if (!matches) {
                    return false;
                }
            }
            return matches;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<Person> fetchPage(int offset, int limit, Map<String, String> filters) {
        List<Person> filtered = applyFilters(filters);
        int total = filtered.size();
        List<Person> items = filtered.stream().skip(offset).limit(limit).collect(Collectors.toList());
        return new PageResult<>(items, total);
    }
}
