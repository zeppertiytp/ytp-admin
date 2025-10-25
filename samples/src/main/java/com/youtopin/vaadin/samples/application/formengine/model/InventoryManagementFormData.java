package com.youtopin.vaadin.samples.application.formengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Backing bean for the inventory management sample form.
 */
public class InventoryManagementFormData {

    private final Inventory inventory = new Inventory();

    public Inventory getInventory() {
        return inventory;
    }

    public static InventoryManagementFormData copyOf(InventoryManagementFormData source) {
        InventoryManagementFormData data = new InventoryManagementFormData();
        if (source != null) {
            data.getInventory().copyFrom(source.getInventory());
        }
        return data;
    }

    public static class Inventory {
        private String code = "";
        private String name = "";
        private String owner = "";
        private String notes = "";
        private final List<InventoryItem> items = new ArrayList<>();

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = Objects.toString(code, "");
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = Objects.toString(name, "");
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = Objects.toString(owner, "");
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = Objects.toString(notes, "");
        }

        public List<InventoryItem> getItems() {
            return items;
        }

        public void setItems(List<InventoryItem> source) {
            this.items.clear();
            if (source != null) {
                source.stream()
                        .filter(Objects::nonNull)
                        .map(InventoryItem::copyOf)
                        .forEach(this.items::add);
            }
        }

        public void copyFrom(Inventory other) {
            if (other == null) {
                setCode(null);
                setName(null);
                setOwner(null);
                setNotes(null);
                setItems(List.of());
                return;
            }
            setCode(other.getCode());
            setName(other.getName());
            setOwner(other.getOwner());
            setNotes(other.getNotes());
            setItems(other.getItems());
        }
    }

    public static class InventoryItem {
        private String sku = "";
        private String description = "";
        private int quantity;
        private String location = "";

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = Objects.toString(sku, "");
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = Objects.toString(description, "");
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = Math.max(quantity, 0);
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = Objects.toString(location, "");
        }

        private static InventoryItem copyOf(InventoryItem source) {
            InventoryItem item = new InventoryItem();
            if (source != null) {
                item.setSku(source.getSku());
                item.setDescription(source.getDescription());
                item.setQuantity(source.getQuantity());
                item.setLocation(source.getLocation());
            }
            return item;
        }
    }
}
