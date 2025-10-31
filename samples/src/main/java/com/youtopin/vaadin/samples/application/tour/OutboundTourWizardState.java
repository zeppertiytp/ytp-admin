package com.youtopin.vaadin.samples.application.tour;

import com.youtopin.vaadin.samples.application.tour.model.OutboundTourAccommodationFormData;
import com.youtopin.vaadin.samples.application.tour.model.OutboundTourBasicsFormData;
import com.youtopin.vaadin.samples.application.tour.model.OutboundTourPlaceholderFormData;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Session-level state shared across the outbound tour wizard steps.
 */
public class OutboundTourWizardState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String STEP_BASICS = "outbound-basics";
    public static final String STEP_ACCOMMODATIONS = "outbound-accommodations";
    public static final String STEP_TRANSPORT = "outbound-transport";
    public static final String STEP_SERVICES = "outbound-services";
    public static final String STEP_DATES = "outbound-dates";
    public static final String STEP_PRICING = "outbound-pricing";
    public static final String STEP_SUMMARY = "outbound-summary";

    private final OutboundTourBasicsFormData basics = new OutboundTourBasicsFormData();
    private final OutboundTourAccommodationFormData accommodations = new OutboundTourAccommodationFormData();
    private final OutboundTourPlaceholderFormData transport = new OutboundTourPlaceholderFormData();
    private final OutboundTourPlaceholderFormData services = new OutboundTourPlaceholderFormData();
    private final OutboundTourPlaceholderFormData dates = new OutboundTourPlaceholderFormData();
    private final OutboundTourPlaceholderFormData pricing = new OutboundTourPlaceholderFormData();
    private final OutboundTourPlaceholderFormData summary = new OutboundTourPlaceholderFormData();
    private final LinkedHashSet<String> completedSteps = new LinkedHashSet<>();

    private String productId = "";
    private String currentStepId = STEP_BASICS;

    public OutboundTourBasicsFormData getBasics() {
        return basics;
    }

    public OutboundTourAccommodationFormData getAccommodations() {
        return accommodations;
    }

    public OutboundTourPlaceholderFormData getTransport() {
        return transport;
    }

    public OutboundTourPlaceholderFormData getServices() {
        return services;
    }

    public OutboundTourPlaceholderFormData getDates() {
        return dates;
    }

    public OutboundTourPlaceholderFormData getPricing() {
        return pricing;
    }

    public OutboundTourPlaceholderFormData getSummary() {
        return summary;
    }

    public Set<String> getCompletedSteps() {
        return Set.copyOf(completedSteps);
    }

    public boolean isStepCompleted(String stepId) {
        return completedSteps.contains(stepId);
    }

    public void markStepCompleted(String stepId) {
        if (stepId != null && !stepId.isBlank()) {
            completedSteps.add(stepId);
        }
    }

    public void clearCompletedSteps() {
        completedSteps.clear();
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId == null ? "" : productId.trim();
    }

    public boolean hasProductId() {
        return productId != null && !productId.isBlank();
    }

    public String getCurrentStepId() {
        return currentStepId;
    }

    public void setCurrentStepId(String currentStepId) {
        if (currentStepId != null && !currentStepId.isBlank()) {
            this.currentStepId = currentStepId;
        }
    }

    public void syncItinerary() {
        basics.ensureItinerarySize();
    }

    public void syncAccommodationLocations(String originCityId,
                                           List<String> destinationCityIds,
                                           java.util.function.Function<String, String> cityNameResolver) {
        Objects.requireNonNull(destinationCityIds, "destinationCityIds");
        Objects.requireNonNull(cityNameResolver, "cityNameResolver");

        List<String> orderedIds = new java.util.ArrayList<>();
        if (originCityId != null && !originCityId.isBlank()) {
            orderedIds.add(originCityId);
        }
        destinationCityIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .forEach(orderedIds::add);

        accommodations.synchronizeLocations(orderedIds, id -> {
            String resolved = cityNameResolver.apply(id);
            return resolved == null ? "" : resolved;
        });
    }

    public void reset() {
        basics.getGeneral().setTitle("");
        basics.getGeneral().setSubtitle("");
        basics.getGeneral().setOperatorId("");
        basics.getGeneral().setLeaderId("");
        basics.getGeneral().setTourClassId("");
        basics.getGeneral().setTagIds(List.of());
        basics.getOrigin().setProvinceId("");
        basics.getOrigin().setCityId("");
        basics.getTransit().setLegs(List.of());
        basics.getDestinationPlan().setDestinations(List.of());
        basics.getMedia().setCoverImages(List.of());
        basics.getMedia().setGalleryImages(List.of());
        basics.getMedia().setVideos(List.of());
        basics.getDetails().setDurationDays(null);
        basics.getDetails().setDurationNights(null);
        basics.getDetails().setBaseCapacity(null);
        basics.getDetails().setClosingDaysBeforeDeparture(null);
        basics.getDetails().setExcludedServiceIds(List.of());
        basics.getDetails().setRequiredDocumentIds(List.of());
        basics.getDetails().setRequiredEquipmentIds(List.of());
        basics.getDetails().setDifficultyLevel("");
        basics.getDetails().setSlug("");
        basics.getItinerary().setDays(List.of());
        basics.getNotes().setCancellationPolicy("");
        basics.getNotes().setDescription("");
        basics.getNotes().setTerms("");
        accommodations.setAccommodations(List.of());
        accommodations.setRooms(List.of());
        transport.setNotes("");
        services.setNotes("");
        dates.setNotes("");
        pricing.setNotes("");
        summary.setNotes("");
        completedSteps.clear();
        currentStepId = STEP_BASICS;
        productId = "";
    }
}
