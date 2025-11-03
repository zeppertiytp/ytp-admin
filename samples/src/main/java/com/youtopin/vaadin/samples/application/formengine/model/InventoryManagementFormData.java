package com.youtopin.vaadin.samples.application.formengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Getter;

/**
 * Backing bean for the inventory management sample form.
 */
public class InventoryManagementFormData {

    @Getter
    private final Inventory inventory = new Inventory();

    public static InventoryManagementFormData copyOf(InventoryManagementFormData source) {
        InventoryManagementFormData data = new InventoryManagementFormData();
        if (source != null) {
            data.getInventory().copyFrom(source.getInventory());
        }
        return data;
    }

    @Getter
    public static class Inventory {
        private String code = "";
        private String name = "";
        private String owner = "";
        private String notes = "";
        private final List<InventoryItem> items = new ArrayList<>();

        public void setCode(String code) {
            this.code = Objects.toString(code, "");
        }

        public void setName(String name) {
            this.name = Objects.toString(name, "");
        }

        public void setOwner(String owner) {
            this.owner = Objects.toString(owner, "");
        }

        public void setNotes(String notes) {
            this.notes = Objects.toString(notes, "");
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

    @Getter
    public static class InventoryItem {
        private String sku = "";
        private String description = "";
        private int quantity;
        private String location = "";

        public void setSku(String sku) {
            this.sku = Objects.toString(sku, "");
        }

        public void setDescription(String description) {
            this.description = Objects.toString(description, "");
        }

        public void setQuantity(int quantity) {
            this.quantity = Math.max(quantity, 0);
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
