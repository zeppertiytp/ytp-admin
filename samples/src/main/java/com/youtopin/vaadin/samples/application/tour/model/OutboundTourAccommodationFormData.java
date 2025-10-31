package com.youtopin.vaadin.samples.application.tour.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public void synchronizeLocations(List<String> orderedCityIds, Function<String, String> cityNameResolver) {
        Objects.requireNonNull(orderedCityIds, "orderedCityIds");
        Objects.requireNonNull(cityNameResolver, "cityNameResolver");

        LinkedHashSet<String> uniqueOrdered = orderedCityIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, DestinationAccommodation> existingById = accommodations.stream()
                .filter(Objects::nonNull)
                .filter(entry -> entry.getDestinationCityId() != null && !entry.getDestinationCityId().isBlank())
                .collect(Collectors.toMap(DestinationAccommodation::getDestinationCityId, Function.identity(),
                        (left, right) -> left, LinkedHashMap::new));

        List<DestinationAccommodation> reordered = new ArrayList<>();
        for (String cityId : uniqueOrdered) {
            DestinationAccommodation entry = existingById.get(cityId);
            if (entry == null) {
                entry = new DestinationAccommodation();
            }
            entry.setDestinationCityId(cityId);
            String resolvedName = cityNameResolver.apply(cityId);
            entry.setDestinationCityName(resolvedName == null ? "" : resolvedName);
            if (entry.getAccommodationTypeId().isBlank()) {
                entry.setAccommodationTypeId(DestinationAccommodation.DEFAULT_TYPE);
            }
            reordered.add(entry);
        }

        accommodations.clear();
        accommodations.addAll(reordered);
    }

    public static final class DestinationAccommodation implements Serializable {
        private String destinationCityId = "";
        private String destinationCityName = "";
        static final String DEFAULT_TYPE = "hotel";

        private String accommodationTypeId = DEFAULT_TYPE;
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
            String normalized = normalize(accommodationTypeId);
            this.accommodationTypeId = normalized.isBlank() ? DEFAULT_TYPE : normalized;
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
