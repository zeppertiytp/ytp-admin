package com.example.adminpanel.service;

import com.example.adminpanel.component.PageFetchService;
import com.example.adminpanel.component.CountService;
import com.example.adminpanel.model.Person;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides mock data for Person entities.  Implements both the
 * {@link PageFetchService} and {@link CountService} interfaces for use
 * with the {@code FilterablePaginatedGrid} component.  In a real
 * application this service would be replaced by a repository or
 * database-backed implementation.
 */
@Service
public class MockPersonService implements PageFetchService<Person>, CountService<Person>, com.example.adminpanel.component.PageResultService<Person> {

    private final List<Person> people;

    public MockPersonService() {
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

    @Override
    public List<Person> fetch(int offset, int limit, Map<String, String> filters) {
        return applyFilters(filters).stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
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
                    case "firstName":
                        matches &= person.getFirstName() != null && person.getFirstName().toLowerCase().contains(value);
                        break;
                    case "lastName":
                        matches &= person.getLastName() != null && person.getLastName().toLowerCase().contains(value);
                        break;
                    case "email":
                        matches &= person.getEmail() != null && person.getEmail().toLowerCase().contains(value);
                        break;
                    case "age":
                        // exact match on age
                        try {
                            int intVal = Integer.parseInt(value);
                            matches &= person.getAge() != null && person.getAge() == intVal;
                        } catch (NumberFormatException e) {
                            matches = false;
                        }
                        break;
                    case "age.min":
                        try {
                            int minVal = Integer.parseInt(value);
                            matches &= person.getAge() != null && person.getAge() >= minVal;
                        } catch (NumberFormatException e) {
                            matches = false;
                        }
                        break;
                    case "age.max":
                        try {
                            int maxVal = Integer.parseInt(value);
                            matches &= person.getAge() != null && person.getAge() <= maxVal;
                        } catch (NumberFormatException e) {
                            matches = false;
                        }
                        break;
                    case "city":
                        matches &= person.getCity() != null && person.getCity().equalsIgnoreCase(value);
                        break;
                    case "country":
                        matches &= person.getCountry() != null && person.getCountry().equalsIgnoreCase(value);
                        break;
                    default:
                        break;
                }
                if (!matches) {
                    return false;
                }
            }
            return matches;
        }).collect(Collectors.toList());
    }

    @Override
    public com.example.adminpanel.component.PageResult<Person> fetchPage(int offset, int limit, Map<String, String> filters) {
        List<Person> filtered = applyFilters(filters);
        int total = filtered.size();
        List<Person> items = filtered.stream().skip(offset).limit(limit).collect(Collectors.toList());
        return new com.example.adminpanel.component.PageResult<>(items, total);
    }
}