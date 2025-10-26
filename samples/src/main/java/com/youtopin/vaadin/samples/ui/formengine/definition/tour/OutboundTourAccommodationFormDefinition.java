package com.youtopin.vaadin.samples.ui.formengine.definition.tour;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.annotation.UiValidation;
import com.youtopin.vaadin.samples.application.tour.model.OutboundTourAccommodationFormData;

/**
 * Wizard definition for configuring tour accommodations.
 */
@UiForm(
        id = "outbound-tour-accommodations",
        titleKey = "tourwizard.accommodation.form.title",
        descriptionKey = "tourwizard.accommodation.form.description",
        bean = OutboundTourAccommodationFormData.class,
        sections = {
                OutboundTourAccommodationFormDefinition.AccommodationsSection.class,
                OutboundTourAccommodationFormDefinition.RoomsSection.class
        },
        actions = {
                @UiAction(id = "outbound-accommodation-save", labelKey = "tourwizard.action.saveDraft",
                        placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SECONDARY, order = 0),
                @UiAction(id = "outbound-accommodation-back", labelKey = "tourwizard.action.previous",
                        placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SECONDARY, order = 1),
                @UiAction(id = "outbound-accommodation-next", labelKey = "tourwizard.action.next",
                        placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SUBMIT, order = 2)
        }
)
public final class OutboundTourAccommodationFormDefinition {

    private OutboundTourAccommodationFormDefinition() {
    }

    @UiSection(id = "tour-accommodation-section", titleKey = "tourwizard.accommodation.section.lodgings",
            groups = AccommodationsGroup.class, order = 0)
    public static class AccommodationsSection {
    }

    @UiSection(id = "tour-rooms-section", titleKey = "tourwizard.accommodation.section.rooms",
            groups = RoomsGroup.class, order = 1)
    public static class RoomsSection {
    }

    @UiGroup(id = "tour-accommodation-group", columns = 3,
            repeatable = @UiRepeatable(enabled = true, min = 0, max = 8,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "tourwizard.accommodation.repeatable.destination",
                    allowDuplicate = true,
                    summaryTemplate = "{destinationCityName}"))
    public static class AccommodationsGroup {

        @UiField(path = "accommodations.destinationCityName", component = UiField.ComponentType.TEXT,
                labelKey = "tourwizard.accommodation.field.destinationCity", readOnlyWhen = "true", colSpan = 3,
                order = 0)
        public void destinationCityName() {
        }

        @UiField(path = "accommodations.accommodationTypeId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.accommodation.field.type", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.accommodationTypes"),
                order = 10)
        public void type() {
        }

        @UiField(path = "accommodations.defaultAccommodationId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.accommodation.field.defaultHotel",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.defaultAccommodations"),
                order = 20)
        public void defaultAccommodation() {
        }

        @UiField(path = "accommodations.accommodationName", component = UiField.ComponentType.TEXT,
                labelKey = "tourwizard.accommodation.field.name",
                validations = {@UiValidation(messageKey = "tourwizard.validation.accommodation.name",
                        expression = "value == null || value.isBlank() || value.length() >= 3")}, order = 30)
        public void accommodationName() {
        }

        @UiField(path = "accommodations.nights", component = UiField.ComponentType.INTEGER,
                labelKey = "tourwizard.accommodation.field.nights", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                validations = {@UiValidation(messageKey = "tourwizard.validation.accommodation.nights",
                        expression = "value != null && value >= 1")}, order = 40)
        public void nights() {
        }

        @UiField(path = "accommodations.qualityId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.accommodation.field.quality",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.hotelQualities"),
                order = 50)
        public void quality() {
        }

        @UiField(path = "accommodations.cateringServiceId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.accommodation.field.catering",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.cateringServices"),
                order = 60)
        public void catering() {
        }

        @UiField(path = "accommodations.amenityServiceId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.accommodation.field.amenities",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.amenityServices"),
                order = 70)
        public void amenities() {
        }

        @UiField(path = "accommodations.gallery", component = UiField.ComponentType.FILE,
                labelKey = "tourwizard.accommodation.field.images", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required", colSpan = 3, order = 80)
        public void gallery() {
        }
    }

    @UiGroup(id = "tour-rooms-group", columns = 2,
            repeatable = @UiRepeatable(enabled = true, min = 0, max = 12,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "tourwizard.accommodation.repeatable.room"))
    public static class RoomsGroup {

        @UiField(path = "rooms.roomTypeId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.accommodation.field.roomType", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.roomTypes"),
                order = 0)
        public void roomType() {
        }

        @UiField(path = "rooms.description", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "tourwizard.accommodation.field.roomNotes", order = 10)
        public void roomNotes() {
        }
    }
}
