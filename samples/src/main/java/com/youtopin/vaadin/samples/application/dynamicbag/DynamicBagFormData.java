package com.youtopin.vaadin.samples.application.dynamicbag;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Container bean exposing a list of {@link DynamicBagEntry} objects.
 */
public class DynamicBagFormData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<DynamicBagEntry> entries = new ArrayList<>();

    public DynamicBagFormData() {
        ensureMinimumEntries(1);
    }

    public List<DynamicBagEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<DynamicBagEntry> newEntries) {
        entries.clear();
        if (newEntries != null) {
            newEntries.stream()
                    .filter(Objects::nonNull)
                    .forEach(entry -> {
                        entry.ensureGroup("profile");
                        entry.ensureGroup("address");
                        entry.ensureGroup("channels");
                        entries.add(entry);
                    });
        }
        ensureMinimumEntries(1);
    }

    public void ensureMinimumEntries(int minimum) {
        int target = Math.max(1, minimum);
        while (entries.size() < target) {
            entries.add(new DynamicBagEntry());
        }
    }

    public static DynamicBagFormData sample() {
        DynamicBagFormData data = new DynamicBagFormData();
        data.entries.clear();

        DynamicBagEntry primary = new DynamicBagEntry();
        primary.ensureGroup("profile").set("fullName", "نازنین راد");
        primary.ensureGroup("profile").set("role", "مدیر پروژه");
        primary.ensureGroup("address").set("country", "IR");
        primary.ensureGroup("address").set("city", "تهران");
        primary.ensureGroup("address").set("street", "خیابان مطهری ۴۲");
        primary.ensureGroup("channels").set("email", "nazanin.rad@youtopin.com");
        primary.ensureGroup("channels").set("phone", "+98 912 123 4567");
        primary.ensureGroup("channels").set("preferred", "ایمیل");

        DynamicBagEntry secondary = new DynamicBagEntry();
        secondary.ensureGroup("profile").set("fullName", "Farid Movahed");
        secondary.ensureGroup("profile").set("role", "Product Owner");
        secondary.ensureGroup("address").set("country", "DE");
        secondary.ensureGroup("address").set("city", "Berlin");
        secondary.ensureGroup("address").set("street", "Friedrichstrasse 17");
        secondary.ensureGroup("channels").set("email", "farid.movahed@youtopin.com");
        secondary.ensureGroup("channels").set("phone", "+49 30 1234 9876");
        secondary.ensureGroup("channels").set("preferred", "Phone");

        data.entries.add(primary);
        data.entries.add(secondary);
        return data;
    }

    public List<Map<String, Object>> toSerializableEntries() {
        List<Map<String, Object>> snapshot = new ArrayList<>();
        for (DynamicBagEntry entry : entries) {
            if (entry == null) {
                continue;
            }
            snapshot.add(new LinkedHashMap<>(entry.asSerializableMap()));
        }
        return snapshot;
    }
}
