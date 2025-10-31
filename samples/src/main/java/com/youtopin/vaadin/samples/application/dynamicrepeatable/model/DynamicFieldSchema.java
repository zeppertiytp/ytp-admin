package com.youtopin.vaadin.samples.application.dynamicrepeatable.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Captures the configuration that controls which attributes appear inside the
 * repeatable collection step.
 */
public class DynamicFieldSchema implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String collectionTitle = "";
    private boolean requireOwner = true;
    private boolean includeEmail = true;
    private boolean includePhone = false;
    private boolean includeNotes = false;

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        if (collectionTitle == null) {
            this.collectionTitle = "";
        } else {
            this.collectionTitle = collectionTitle.trim();
        }
    }

    public boolean isRequireOwner() {
        return requireOwner;
    }

    public void setRequireOwner(boolean requireOwner) {
        this.requireOwner = requireOwner;
    }

    public boolean isIncludeEmail() {
        return includeEmail;
    }

    public void setIncludeEmail(boolean includeEmail) {
        this.includeEmail = includeEmail;
    }

    public boolean isIncludePhone() {
        return includePhone;
    }

    public void setIncludePhone(boolean includePhone) {
        this.includePhone = includePhone;
    }

    public boolean isIncludeNotes() {
        return includeNotes;
    }

    public void setIncludeNotes(boolean includeNotes) {
        this.includeNotes = includeNotes;
    }

    public void copyFrom(DynamicFieldSchema other) {
        if (other == null) {
            return;
        }
        setCollectionTitle(other.getCollectionTitle());
        setRequireOwner(other.isRequireOwner());
        setIncludeEmail(other.isIncludeEmail());
        setIncludePhone(other.isIncludePhone());
        setIncludeNotes(other.isIncludeNotes());
    }

    @Override
    public String toString() {
        return "DynamicFieldSchema{" +
                "collectionTitle='" + collectionTitle + '\'' +
                ", requireOwner=" + requireOwner +
                ", includeEmail=" + includeEmail +
                ", includePhone=" + includePhone +
                ", includeNotes=" + includeNotes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DynamicFieldSchema that)) {
            return false;
        }
        return requireOwner == that.requireOwner
                && includeEmail == that.includeEmail
                && includePhone == that.includePhone
                && includeNotes == that.includeNotes
                && Objects.equals(collectionTitle, that.collectionTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionTitle, requireOwner, includeEmail, includePhone, includeNotes);
    }
}
