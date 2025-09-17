package com.example.adminpanel.model;

/**
 * Simple POJO representing a person.  Used to demonstrate the
 * FilterablePaginatedGrid component with mock data.
 */
public class Person {
    private String firstName;
    private String lastName;
    private String email;
    private Integer age;
    // Additional field to demonstrate filtering on hidden data.  This field
    // is not displayed in the table columns by default but can be used for
    // filtering through the table's filter dialog.
    private String city;

    // Country for demonstrating select filters and dependencies.  The city
    // options may depend on the selected country in the filter dialog.
    private String country;

    public Person(String firstName, String lastName, String email, Integer age) {
        this(firstName, lastName, email, age, null, null);
    }

    public Person(String firstName, String lastName, String email, Integer age, String city) {
        this(firstName, lastName, email, age, city, null);
    }

    public Person(String firstName, String lastName, String email, Integer age, String city, String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
        this.city = city;
        this.country = country;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }
}