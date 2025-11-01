package com.youtopin.vaadin.samples.ui.formengine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Backing bean for the composite contact directory sample showcasing multi-group repeatable entries.
 */
public class CompositeContactFormData implements Serializable {

    private final List<ContactEntry> contacts = new ArrayList<>();

    public List<ContactEntry> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactEntry> entries) {
        contacts.clear();
        if (entries != null) {
            contacts.addAll(entries);
        }
    }

    public static CompositeContactFormData sample() {
        CompositeContactFormData data = new CompositeContactFormData();
        ContactEntry primary = new ContactEntry();
        primary.setFullName("سارا حسینی");
        primary.setEmail("sara.hosseini@youtopin.com");
        primary.setPhone("+98 21 1234 5678");
        primary.getAddress().setCountry("IR");
        primary.getAddress().setCity("تهران");
        primary.getAddress().setStreet("خیابان ولیعصر ۱۲۳");
        primary.getAddress().setPostalCode("1415711111");
        primary.getChannels().setPrimary(true);
        primary.getChannels().setLabel("پشتیبانی");
        primary.getChannels().setNotes("ارتباط اصلی با مشتریان کلیدی");

        ContactEntry backup = new ContactEntry();
        backup.setFullName("Reza Kamali");
        backup.setEmail("reza.kamali@youtopin.com");
        backup.setPhone("+1 415 555 2100");
        backup.getAddress().setCountry("US");
        backup.getAddress().setCity("San Francisco");
        backup.getAddress().setStreet("915 Mission St");
        backup.getAddress().setPostalCode("94103");
        backup.getChannels().setPrimary(false);
        backup.getChannels().setLabel("North America");
        backup.getChannels().setNotes("Handles after-hours escalation");

        data.getContacts().add(primary);
        data.getContacts().add(backup);
        return data;
    }

    public static class ContactEntry implements Serializable {
        private String fullName;
        private String email;
        private String phone;
        private ContactAddress address = new ContactAddress();
        private ContactChannel channels = new ContactChannel();

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public ContactAddress getAddress() {
            if (address == null) {
                address = new ContactAddress();
            }
            return address;
        }

        public void setAddress(ContactAddress address) {
            this.address = Objects.requireNonNullElseGet(address, ContactAddress::new);
        }

        public ContactChannel getChannels() {
            if (channels == null) {
                channels = new ContactChannel();
            }
            return channels;
        }

        public void setChannels(ContactChannel channels) {
            this.channels = Objects.requireNonNullElseGet(channels, ContactChannel::new);
        }
    }

    public static class ContactAddress implements Serializable {
        private String country;
        private String city;
        private String street;
        private String postalCode;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
    }

    public static class ContactChannel implements Serializable {
        private boolean primary;
        private String label;
        private String notes;

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}
