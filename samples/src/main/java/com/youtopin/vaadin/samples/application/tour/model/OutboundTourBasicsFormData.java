package com.youtopin.vaadin.samples.application.tour.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Bean storing the primary step of the outbound tour wizard.
 */
public class OutboundTourBasicsFormData implements Serializable {

    private final General general = new General();
    private final Origin origin = new Origin();
    private final Transit transit = new Transit();
    private final DestinationPlan destinationPlan = new DestinationPlan();
    private final Media media = new Media();
    private final Details details = new Details();
    private final Itinerary itinerary = new Itinerary();
    private final Notes notes = new Notes();

    public General getGeneral() {
        return general;
    }

    public Origin getOrigin() {
        return origin;
    }

    public Transit getTransit() {
        return transit;
    }

    public DestinationPlan getDestinationPlan() {
        return destinationPlan;
    }

    public Media getMedia() {
        return media;
    }

    public Details getDetails() {
        return details;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public Notes getNotes() {
        return notes;
    }

    public void ensureItinerarySize() {
        itinerary.ensureSize(details.getDurationDays());
    }

    public static final class General implements Serializable {
        private String title = "";
        private String subtitle = "";
        private String operatorId = "";
        private String leaderId = "";
        private String tourClassId = "";
        private final List<String> tagIds = new ArrayList<>();

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = normalize(title);
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = normalize(subtitle);
        }

        public String getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(String operatorId) {
            this.operatorId = normalize(operatorId);
        }

        public String getLeaderId() {
            return leaderId;
        }

        public void setLeaderId(String leaderId) {
            this.leaderId = normalize(leaderId);
        }

        public String getTourClassId() {
            return tourClassId;
        }

        public void setTourClassId(String tourClassId) {
            this.tourClassId = normalize(tourClassId);
        }

        public List<String> getTagIds() {
            return tagIds;
        }

        public void setTagIds(List<String> ids) {
            tagIds.clear();
            if (ids != null) {
                ids.stream().filter(Objects::nonNull).map(OutboundTourBasicsFormData::normalize)
                        .filter(value -> !value.isBlank()).forEach(tagIds::add);
            }
        }
    }

    public static final class Origin implements Serializable {
        private String provinceId = "";
        private String cityId = "";

        public String getProvinceId() {
            return provinceId;
        }

        public void setProvinceId(String provinceId) {
            this.provinceId = normalize(provinceId);
        }

        public String getCityId() {
            return cityId;
        }

        public void setCityId(String cityId) {
            this.cityId = normalize(cityId);
        }
    }

    public static final class Transit implements Serializable {
        private final List<TransitLeg> legs = new ArrayList<>();

        public List<TransitLeg> getLegs() {
            return legs;
        }

        public void setLegs(List<TransitLeg> entries) {
            legs.clear();
            if (entries != null) {
                entries.stream().filter(Objects::nonNull).map(TransitLeg::copyOf).forEach(legs::add);
            }
        }
    }

    public static final class DestinationPlan implements Serializable {
        private final List<Destination> destinations = new ArrayList<>();

        public List<Destination> getDestinations() {
            return destinations;
        }

        public void setDestinations(List<Destination> entries) {
            destinations.clear();
            if (entries != null) {
                entries.stream().filter(Objects::nonNull).map(Destination::copyOf).forEach(destinations::add);
            }
        }
    }

    public static final class Media implements Serializable {
        private final List<String> coverImages = new ArrayList<>();
        private final List<String> galleryImages = new ArrayList<>();
        private final List<String> videos = new ArrayList<>();

        public List<String> getCoverImages() {
            return coverImages;
        }

        public void setCoverImages(List<String> files) {
            updateList(coverImages, files);
        }

        public List<String> getGalleryImages() {
            return galleryImages;
        }

        public void setGalleryImages(List<String> files) {
            updateList(galleryImages, files);
        }

        public List<String> getVideos() {
            return videos;
        }

        public void setVideos(List<String> files) {
            updateList(videos, files);
        }
    }

    public static final class Details implements Serializable {
        private Integer durationDays;
        private Integer durationNights;
        private Integer baseCapacity;
        private Integer closingDaysBeforeDeparture;
        private final List<String> excludedServiceIds = new ArrayList<>();
        private final List<String> requiredDocumentIds = new ArrayList<>();
        private final List<String> requiredEquipmentIds = new ArrayList<>();
        private String difficultyLevel = "";
        private String slug = "";

        public Integer getDurationDays() {
            return durationDays;
        }

        public void setDurationDays(Integer durationDays) {
            this.durationDays = durationDays;
        }

        public Integer getDurationNights() {
            return durationNights;
        }

        public void setDurationNights(Integer durationNights) {
            this.durationNights = durationNights;
        }

        public Integer getBaseCapacity() {
            return baseCapacity;
        }

        public void setBaseCapacity(Integer baseCapacity) {
            this.baseCapacity = baseCapacity;
        }

        public Integer getClosingDaysBeforeDeparture() {
            return closingDaysBeforeDeparture;
        }

        public void setClosingDaysBeforeDeparture(Integer closingDaysBeforeDeparture) {
            this.closingDaysBeforeDeparture = closingDaysBeforeDeparture;
        }

        public List<String> getExcludedServiceIds() {
            return Collections.unmodifiableList(excludedServiceIds);
        }

        public void setExcludedServiceIds(List<String> ids) {
            updateList(excludedServiceIds, ids);
        }

        public List<String> getRequiredDocumentIds() {
            return Collections.unmodifiableList(requiredDocumentIds);
        }

        public void setRequiredDocumentIds(List<String> ids) {
            updateList(requiredDocumentIds, ids);
        }

        public List<String> getRequiredEquipmentIds() {
            return Collections.unmodifiableList(requiredEquipmentIds);
        }

        public void setRequiredEquipmentIds(List<String> ids) {
            updateList(requiredEquipmentIds, ids);
        }

        public String getDifficultyLevel() {
            return difficultyLevel;
        }

        public void setDifficultyLevel(String difficultyLevel) {
            this.difficultyLevel = normalize(difficultyLevel);
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = normalize(slug);
        }
    }

    public static final class Itinerary implements Serializable {
        private final List<DayPlan> days = new ArrayList<>();

        public List<DayPlan> getDays() {
            return days;
        }

        public void setDays(List<DayPlan> plans) {
            days.clear();
            if (plans != null) {
                plans.stream().filter(Objects::nonNull).map(DayPlan::copyOf).forEach(days::add);
            }
        }

        public void ensureSize(Integer desiredDayCount) {
            int desired = desiredDayCount == null ? 0 : Math.max(desiredDayCount, 0);
            while (days.size() < desired) {
                days.add(new DayPlan());
            }
            while (days.size() > desired) {
                days.remove(days.size() - 1);
            }
        }
    }

    public static final class DayPlan implements Serializable {
        private String description = "";
        private Integer overnightType;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = normalize(description);
        }

        public Integer getOvernightType() {
            return overnightType;
        }

        public void setOvernightType(Integer overnightType) {
            this.overnightType = overnightType;
        }

        private static DayPlan copyOf(DayPlan source) {
            DayPlan copy = new DayPlan();
            if (source == null) {
                return copy;
            }
            copy.setDescription(source.getDescription());
            copy.setOvernightType(source.getOvernightType());
            return copy;
        }
    }

    public static final class Notes implements Serializable {
        private String cancellationPolicy = "";
        private String description = "";
        private String terms = "";

        public String getCancellationPolicy() {
            return cancellationPolicy;
        }

        public void setCancellationPolicy(String cancellationPolicy) {
            this.cancellationPolicy = normalize(cancellationPolicy);
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = normalize(description);
        }

        public String getTerms() {
            return terms;
        }

        public void setTerms(String terms) {
            this.terms = normalize(terms);
        }
    }

    public static final class TransitLeg implements Serializable {
        private String continentId = "";
        private String countryId = "";
        private String cityId = "";

        public String getContinentId() {
            return continentId;
        }

        public void setContinentId(String continentId) {
            this.continentId = normalize(continentId);
        }

        public String getCountryId() {
            return countryId;
        }

        public void setCountryId(String countryId) {
            this.countryId = normalize(countryId);
        }

        public String getCityId() {
            return cityId;
        }

        public void setCityId(String cityId) {
            this.cityId = normalize(cityId);
        }

        private static TransitLeg copyOf(TransitLeg source) {
            TransitLeg copy = new TransitLeg();
            if (source == null) {
                return copy;
            }
            copy.setContinentId(source.getContinentId());
            copy.setCountryId(source.getCountryId());
            copy.setCityId(source.getCityId());
            return copy;
        }
    }

    public static final class Destination implements Serializable {
        private String continentId = "";
        private String countryId = "";
        private String cityId = "";
        private boolean overnightStay;

        public String getContinentId() {
            return continentId;
        }

        public void setContinentId(String continentId) {
            this.continentId = normalize(continentId);
        }

        public String getCountryId() {
            return countryId;
        }

        public void setCountryId(String countryId) {
            this.countryId = normalize(countryId);
        }

        public String getCityId() {
            return cityId;
        }

        public void setCityId(String cityId) {
            this.cityId = normalize(cityId);
        }

        public boolean isOvernightStay() {
            return overnightStay;
        }

        public void setOvernightStay(boolean overnightStay) {
            this.overnightStay = overnightStay;
        }

        private static Destination copyOf(Destination source) {
            Destination copy = new Destination();
            if (source == null) {
                return copy;
            }
            copy.setContinentId(source.getContinentId());
            copy.setCountryId(source.getCountryId());
            copy.setCityId(source.getCityId());
            copy.setOvernightStay(source.isOvernightStay());
            return copy;
        }
    }

    private static void updateList(List<String> target, List<String> values) {
        target.clear();
        if (values != null) {
            values.stream().filter(Objects::nonNull)
                    .map(OutboundTourBasicsFormData::normalize)
                    .filter(value -> !value.isBlank())
                    .forEach(target::add);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
