package com.youtopin.vaadin.samples.ui.formengine.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Bean used by the employee onboarding form sample. Includes nested structures so the
 * form engine exercises dotted property paths and complex value types such as lists and maps.
 */
public class EmployeeOnboardingFormData {

    private final PersonalDetails personal = new PersonalDetails();
    private final EmploymentDetails employment = new EmploymentDetails();
    private final ContactDetails contact = new ContactDetails();
    private Map<String, Double> preferredLocation = Map.of();
    private List<String> skillTags = new ArrayList<>();

    public PersonalDetails getPersonal() {
        return personal;
    }

    public EmploymentDetails getEmployment() {
        return employment;
    }

    public ContactDetails getContact() {
        return contact;
    }

    public Map<String, Double> getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(Map<String, Double> preferredLocation) {
        this.preferredLocation = preferredLocation == null ? Map.of() : Map.copyOf(preferredLocation);
    }

    public List<String> getSkillTags() {
        return skillTags;
    }

    public void setSkillTags(List<String> skillTags) {
        this.skillTags = skillTags == null ? new ArrayList<>() : new ArrayList<>(skillTags);
    }

    public static class PersonalDetails {
        private String firstName = "";
        private String lastName = "";
        private String nationalId = "";
        private LocalDate birthDate;
        private boolean relocationReady;
        private String bio = "";

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = Objects.toString(firstName, "");
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = Objects.toString(lastName, "");
        }

        public String getNationalId() {
            return nationalId;
        }

        public void setNationalId(String nationalId) {
            this.nationalId = Objects.toString(nationalId, "");
        }

        public LocalDate getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
        }

        public boolean isRelocationReady() {
            return relocationReady;
        }

        public void setRelocationReady(boolean relocationReady) {
            this.relocationReady = relocationReady;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = Objects.toString(bio, "");
        }
    }

    public static class EmploymentDetails {
        private String department = "";
        private String contractType = "";
        private LocalDateTime onboardingDateTime;
        private LocalDate startDate;
        private String equipmentPreference = "";

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = Objects.toString(department, "");
        }

        public String getContractType() {
            return contractType;
        }

        public void setContractType(String contractType) {
            this.contractType = Objects.toString(contractType, "");
        }

        public LocalDateTime getOnboardingDateTime() {
            return onboardingDateTime;
        }

        public void setOnboardingDateTime(LocalDateTime onboardingDateTime) {
            this.onboardingDateTime = onboardingDateTime;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public String getEquipmentPreference() {
            return equipmentPreference;
        }

        public void setEquipmentPreference(String equipmentPreference) {
            this.equipmentPreference = Objects.toString(equipmentPreference, "");
        }
    }

    public static class ContactDetails {
        private String email = "";
        private String phone = "";
        private Address address = new Address();

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = Objects.toString(email, "");
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = Objects.toString(phone, "");
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address == null ? new Address() : address;
        }
    }

    public static class Address {
        private String city = "";
        private String street = "";
        private String postalCode = "";

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = Objects.toString(city, "");
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = Objects.toString(street, "");
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = Objects.toString(postalCode, "");
        }
    }
}
