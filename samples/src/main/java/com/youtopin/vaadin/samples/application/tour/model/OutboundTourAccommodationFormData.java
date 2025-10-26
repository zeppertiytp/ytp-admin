package com.youtopin.vaadin.samples.application.tour.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bean representing the accommodation step of the outbound tour wizard.
 */
public class OutboundTourAccommodationFormData implements Serializable {

    private final List<DestinationAccommodation> accommodations = new ArrayList<>();
    private final List<RoomDefinition> rooms = new ArrayList<>();

    public List<DestinationAccommodation> getAccommodations() {
        return accommodations;
    }

    public void setAccommodations(List<DestinationAccommodation> entries) {
        accommodations.clear();
        if (entries != null) {
            entries.stream().filter(Objects::nonNull)
                    .map(DestinationAccommodation::copyOf)
                    .forEach(accommodations::add);
        }
    }

    public List<RoomDefinition> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomDefinition> entries) {
        rooms.clear();
        if (entries != null) {
            entries.stream().filter(Objects::nonNull)
                    .map(RoomDefinition::copyOf)
                    .forEach(rooms::add);
        }
    }

    public DestinationAccommodation ensureAccommodationForCity(String cityId, String cityName) {
        return accommodations.stream()
                .filter(entry -> Objects.equals(entry.getDestinationCityId(), cityId))
                .findFirst()
                .orElseGet(() -> {
                    DestinationAccommodation created = new DestinationAccommodation();
                    created.setDestinationCityId(cityId);
                    created.setDestinationCityName(cityName);
                    accommodations.add(created);
                    return created;
                });
    }

    public void removeAccommodationForUnknownCities(List<String> validCityIds) {
        accommodations.removeIf(entry -> entry == null
                || entry.getDestinationCityId().isBlank()
                || !validCityIds.contains(entry.getDestinationCityId()));
    }

    public static final class DestinationAccommodation implements Serializable {
        private String destinationCityId = "";
        private String destinationCityName = "";
        private String accommodationTypeId = "";
        private String defaultAccommodationId = "";
        private String accommodationName = "";
        private Integer nights;
        private String qualityId = "";
        private String cateringServiceId = "";
        private String amenityServiceId = "";
        private final List<String> gallery = new ArrayList<>();

        public String getDestinationCityId() {
            return destinationCityId;
        }

        public void setDestinationCityId(String destinationCityId) {
            this.destinationCityId = normalize(destinationCityId);
        }

        public String getDestinationCityName() {
            return destinationCityName;
        }

        public void setDestinationCityName(String destinationCityName) {
            this.destinationCityName = normalize(destinationCityName);
        }

        public String getAccommodationTypeId() {
            return accommodationTypeId;
        }

        public void setAccommodationTypeId(String accommodationTypeId) {
            this.accommodationTypeId = normalize(accommodationTypeId);
        }

        public String getDefaultAccommodationId() {
            return defaultAccommodationId;
        }

        public void setDefaultAccommodationId(String defaultAccommodationId) {
            this.defaultAccommodationId = normalize(defaultAccommodationId);
        }

        public String getAccommodationName() {
            return accommodationName;
        }

        public void setAccommodationName(String accommodationName) {
            this.accommodationName = normalize(accommodationName);
        }

        public Integer getNights() {
            return nights;
        }

        public void setNights(Integer nights) {
            this.nights = nights;
        }

        public String getQualityId() {
            return qualityId;
        }

        public void setQualityId(String qualityId) {
            this.qualityId = normalize(qualityId);
        }

        public String getCateringServiceId() {
            return cateringServiceId;
        }

        public void setCateringServiceId(String cateringServiceId) {
            this.cateringServiceId = normalize(cateringServiceId);
        }

        public String getAmenityServiceId() {
            return amenityServiceId;
        }

        public void setAmenityServiceId(String amenityServiceId) {
            this.amenityServiceId = normalize(amenityServiceId);
        }

        public List<String> getGallery() {
            return gallery;
        }

        public void setGallery(List<String> images) {
            gallery.clear();
            if (images != null) {
                images.stream().filter(Objects::nonNull)
                        .map(OutboundTourAccommodationFormData::normalize)
                        .filter(value -> !value.isBlank())
                        .forEach(gallery::add);
            }
        }

        private static DestinationAccommodation copyOf(DestinationAccommodation source) {
            DestinationAccommodation copy = new DestinationAccommodation();
            if (source == null) {
                return copy;
            }
            copy.setDestinationCityId(source.getDestinationCityId());
            copy.setDestinationCityName(source.getDestinationCityName());
            copy.setAccommodationTypeId(source.getAccommodationTypeId());
            copy.setDefaultAccommodationId(source.getDefaultAccommodationId());
            copy.setAccommodationName(source.getAccommodationName());
            copy.setNights(source.getNights());
            copy.setQualityId(source.getQualityId());
            copy.setCateringServiceId(source.getCateringServiceId());
            copy.setAmenityServiceId(source.getAmenityServiceId());
            copy.setGallery(source.getGallery());
            return copy;
        }
    }

    public static final class RoomDefinition implements Serializable {
        private String roomTypeId = "";
        private String description = "";

        public String getRoomTypeId() {
            return roomTypeId;
        }

        public void setRoomTypeId(String roomTypeId) {
            this.roomTypeId = normalize(roomTypeId);
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = normalize(description);
        }

        private static RoomDefinition copyOf(RoomDefinition source) {
            RoomDefinition copy = new RoomDefinition();
            if (source == null) {
                return copy;
            }
            copy.setRoomTypeId(source.getRoomTypeId());
            copy.setDescription(source.getDescription());
            return copy;
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
