package com.youtopin.vaadin.samples.ui.formengine.model;

/**
 * Sample data bean demonstrating read-only transitions after submission.
 */
public class ProfileLockFormData {

    private String username;
    private String email;
    private String phone;
    private boolean locked;
    private boolean contactLocked;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isContactLocked() {
        return contactLocked;
    }

    public void setContactLocked(boolean contactLocked) {
        this.contactLocked = contactLocked;
    }
}
