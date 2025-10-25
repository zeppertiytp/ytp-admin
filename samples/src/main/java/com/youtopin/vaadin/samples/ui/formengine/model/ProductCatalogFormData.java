package com.youtopin.vaadin.samples.ui.formengine.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bean backing the product catalog editor sample.
 */
public class ProductCatalogFormData {

    private final ProductDetails product = new ProductDetails();
    private final PricingDetails pricing = new PricingDetails();
    private final Availability availability = new Availability();

    public ProductDetails getProduct() {
        return product;
    }

    public PricingDetails getPricing() {
        return pricing;
    }

    public Availability getAvailability() {
        return availability;
    }

    public static class ProductDetails {
        private String name = "";
        private String sku = "";
        private String summary = "";
        private String description = "";
        private LocalDateTime releaseDate;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = Objects.toString(name, "");
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = Objects.toString(sku, "");
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = Objects.toString(summary, "");
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = Objects.toString(description, "");
        }

        public LocalDateTime getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(LocalDateTime releaseDate) {
            this.releaseDate = releaseDate;
        }
    }

    public static class PricingDetails {
        private BigDecimal price = BigDecimal.ZERO;
        private String currency = "";
        private String taxCategory = "";

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price == null ? BigDecimal.ZERO : price;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = Objects.toString(currency, "");
        }

        public String getTaxCategory() {
            return taxCategory;
        }

        public void setTaxCategory(String taxCategory) {
            this.taxCategory = Objects.toString(taxCategory, "");
        }
    }

    public static class Availability {
        private String status = "";
        private final List<String> channels = new ArrayList<>();
        private final List<String> tags = new ArrayList<>();

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = Objects.toString(status, "");
        }

        public List<String> getChannels() {
            return channels;
        }

        public void setChannels(List<String> channels) {
            this.channels.clear();
            if (channels != null) {
                this.channels.addAll(channels);
            }
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags.clear();
            if (tags != null) {
                this.tags.addAll(tags);
            }
        }
    }
}
