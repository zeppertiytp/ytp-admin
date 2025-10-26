package com.youtopin.vaadin.samples.ui.formengine.definition.tour;

import com.youtopin.vaadin.formengine.annotation.UiAction;
import com.youtopin.vaadin.formengine.annotation.UiCrossField;
import com.youtopin.vaadin.formengine.annotation.UiField;
import com.youtopin.vaadin.formengine.annotation.UiForm;
import com.youtopin.vaadin.formengine.annotation.UiGroup;
import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.annotation.UiRepeatable;
import com.youtopin.vaadin.formengine.annotation.UiSection;
import com.youtopin.vaadin.formengine.annotation.UiValidation;
import com.youtopin.vaadin.samples.application.tour.model.OutboundTourBasicsFormData;

/**
 * Wizard definition for the outbound tour basics step.
 */
@UiForm(
        id = "outbound-tour-basics",
        titleKey = "tourwizard.basics.form.title",
        descriptionKey = "tourwizard.basics.form.description",
        bean = OutboundTourBasicsFormData.class,
        sections = {
                OutboundTourBasicsFormDefinition.GeneralSection.class,
                OutboundTourBasicsFormDefinition.OriginSection.class,
                OutboundTourBasicsFormDefinition.TransitSection.class,
                OutboundTourBasicsFormDefinition.DestinationSection.class,
                OutboundTourBasicsFormDefinition.CoverSection.class,
                OutboundTourBasicsFormDefinition.GallerySection.class,
                OutboundTourBasicsFormDefinition.VideoSection.class,
                OutboundTourBasicsFormDefinition.DetailSection.class,
                OutboundTourBasicsFormDefinition.ItinerarySection.class,
                OutboundTourBasicsFormDefinition.NotesSection.class
        },
        actions = {
                @UiAction(id = "outbound-basics-save", labelKey = "tourwizard.action.saveDraft",
                        placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SECONDARY, order = 0),
                @UiAction(id = "outbound-basics-next", labelKey = "tourwizard.action.next",
                        placement = UiAction.Placement.FOOTER, type = UiAction.ActionType.SUBMIT, order = 1)
        }
)
public final class OutboundTourBasicsFormDefinition {

    private OutboundTourBasicsFormDefinition() {
    }

    @UiSection(id = "tour-basics-general", titleKey = "tourwizard.basics.section.general",
            groups = GeneralGroup.class, order = 0)
    public static class GeneralSection {
    }

    @UiSection(id = "tour-basics-origin", titleKey = "tourwizard.basics.section.origin",
            groups = OriginGroup.class, order = 1)
    public static class OriginSection {
    }

    @UiSection(id = "tour-basics-transit", titleKey = "tourwizard.basics.section.transit",
            groups = TransitGroup.class, order = 2)
    public static class TransitSection {
    }

    @UiSection(id = "tour-basics-destination", titleKey = "tourwizard.basics.section.destination",
            groups = DestinationGroup.class, order = 3)
    public static class DestinationSection {
    }

    @UiSection(id = "tour-basics-cover", titleKey = "tourwizard.basics.section.cover",
            groups = CoverGroup.class, order = 4)
    public static class CoverSection {
    }

    @UiSection(id = "tour-basics-gallery", titleKey = "tourwizard.basics.section.gallery",
            groups = GalleryGroup.class, order = 5)
    public static class GallerySection {
    }

    @UiSection(id = "tour-basics-video", titleKey = "tourwizard.basics.section.videos",
            groups = VideoGroup.class, order = 6)
    public static class VideoSection {
    }

    @UiSection(id = "tour-basics-detail", titleKey = "tourwizard.basics.section.details",
            groups = DetailGroup.class, order = 7)
    public static class DetailSection {
    }

    @UiSection(id = "tour-basics-itinerary", titleKey = "tourwizard.basics.section.itinerary",
            groups = ItineraryGroup.class, order = 8)
    public static class ItinerarySection {
    }

    @UiSection(id = "tour-basics-notes", titleKey = "tourwizard.basics.section.notes",
            groups = NotesGroup.class, order = 9)
    public static class NotesSection {
    }

    @UiGroup(id = "tour-general-group", columns = 2)
    public static class GeneralGroup {

        @UiField(path = "general.title", component = UiField.ComponentType.TEXT,
                labelKey = "tourwizard.basics.field.title", requiredWhen = "true",
                validations = {
                        @UiValidation(messageKey = "tourwizard.validation.title.min", expression = "value != null && value.length() >= 5"),
                        @UiValidation(messageKey = "tourwizard.validation.title.max", expression = "value != null && value.length() <= 30")
                }, order = 0)
        public void title() {
        }

        @UiField(path = "general.subtitle", component = UiField.ComponentType.TEXT,
                labelKey = "tourwizard.basics.field.subtitle",
                validations = {
                        @UiValidation(messageKey = "tourwizard.validation.subtitle.min",
                                expression = "value == null || value.isBlank() || value.length() >= 5"),
                        @UiValidation(messageKey = "tourwizard.validation.subtitle.max",
                                expression = "value == null || value.isBlank() || value.length() <= 100")
                }, order = 10)
        public void subtitle() {
        }

        @UiField(path = "general.operatorId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.operator", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.operators"),
                order = 20)
        public void operator() {
        }

        @UiField(path = "general.leaderId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.leader", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.leaders"),
                order = 30)
        public void leader() {
        }

        @UiField(path = "general.tourClassId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.class", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.classes"),
                order = 40)
        public void tourClass() {
        }

        @UiField(path = "general.tagIds", component = UiField.ComponentType.TAGS,
                labelKey = "tourwizard.basics.field.tags",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.tags"),
                colSpan = 1, order = 50)
        public void tags() {
        }
    }

    @UiGroup(id = "tour-origin-group", columns = 2)
    public static class OriginGroup {

        @UiField(path = "origin.provinceId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.province", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.provinces"),
                order = 0)
        public void province() {
        }

        @UiField(path = "origin.cityId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.city", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.provinceCities"),
                order = 10)
        public void city() {
        }
    }

    @UiGroup(id = "tour-transit-group", columns = 3,
            repeatable = @UiRepeatable(enabled = true, min = 0, max = 6,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "tourwizard.basics.repeatable.transit",
                    allowDuplicate = true,
                    summaryTemplate = "{cityId}"))
    public static class TransitGroup {

        @UiField(path = "transit.legs.continentId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.transit.continent", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.continents"),
                colSpan = 1, order = 0)
        public void continent() {
        }

        @UiField(path = "transit.legs.countryId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.transit.country", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.countries"),
                colSpan = 1, order = 10)
        public void country() {
        }

        @UiField(path = "transit.legs.cityId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.transit.city", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.worldCities"),
                colSpan = 1, order = 20)
        public void city() {
        }
    }

    @UiGroup(id = "tour-destination-group", columns = 3,
            repeatable = @UiRepeatable(enabled = true, min = 0, max = 6,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    itemTitleKey = "tourwizard.basics.repeatable.destination",
                    allowDuplicate = true,
                    summaryTemplate = "{cityId}"))
    public static class DestinationGroup {

        @UiField(path = "destinationPlan.destinations.continentId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.destination.continent", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.continents"),
                order = 0)
        public void continent() {
        }

        @UiField(path = "destinationPlan.destinations.countryId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.destination.country", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.countries"),
                order = 10)
        public void country() {
        }

        @UiField(path = "destinationPlan.destinations.cityId", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.destination.city", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.worldCities"),
                order = 20)
        public void city() {
        }

        @UiField(path = "destinationPlan.destinations.overnightStay", component = UiField.ComponentType.CHECKBOX,
                labelKey = "tourwizard.basics.field.destination.overnight", colSpan = 1, order = 30)
        public void overnightStay() {
        }
    }

    @UiGroup(id = "tour-cover-group", columns = 1)
    public static class CoverGroup {

        @UiField(path = "media.coverImages", component = UiField.ComponentType.FILE,
                labelKey = "tourwizard.basics.field.cover", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required", colSpan = 1, order = 0)
        public void cover() {
        }
    }

    @UiGroup(id = "tour-gallery-group", columns = 1)
    public static class GalleryGroup {

        @UiField(path = "media.galleryImages", component = UiField.ComponentType.FILE,
                labelKey = "tourwizard.basics.field.gallery", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required", colSpan = 1, order = 0)
        public void gallery() {
        }
    }

    @UiGroup(id = "tour-video-group", columns = 1)
    public static class VideoGroup {

        @UiField(path = "media.videos", component = UiField.ComponentType.FILE,
                labelKey = "tourwizard.basics.field.videos", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required", colSpan = 1, order = 0)
        public void videos() {
        }
    }

    @UiGroup(id = "tour-detail-group", columns = 2)
    public static class DetailGroup {

        @UiField(path = "details.durationDays", component = UiField.ComponentType.INTEGER,
                labelKey = "tourwizard.basics.field.durationDays", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                validations = {
                        @UiValidation(messageKey = "tourwizard.validation.days.min", expression = "value != null && value >= 1"),
                        @UiValidation(messageKey = "tourwizard.validation.days.max", expression = "value != null && value <= 30")
                }, order = 0)
        public void durationDays() {
        }

        @UiField(path = "details.durationNights", component = UiField.ComponentType.INTEGER,
                labelKey = "tourwizard.basics.field.durationNights", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                validations = {
                        @UiValidation(messageKey = "tourwizard.validation.nights.min",
                                expression = "value != null && value >= 0")
                },
                crossField = {
                        @UiCrossField(expression = "bean.details.durationDays != null && bean.details.durationNights != null && bean.details.durationNights > bean.details.durationDays",
                                messageKey = "tourwizard.validation.nights.max",
                                targetPaths = {"details.durationNights"}),
                        @UiCrossField(expression = "bean.details.durationDays != null && bean.details.durationNights != null && bean.details.durationNights < bean.details.durationDays - 1",
                                messageKey = "tourwizard.validation.nights.minRelation",
                                targetPaths = {"details.durationNights"})
                }, order = 10)
        public void durationNights() {
        }

        @UiField(path = "details.baseCapacity", component = UiField.ComponentType.INTEGER,
                labelKey = "tourwizard.basics.field.baseCapacity", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                validations = {
                        @UiValidation(messageKey = "tourwizard.validation.capacity.min", expression = "value != null && value >= 1"),
                        @UiValidation(messageKey = "tourwizard.validation.capacity.max", expression = "value != null && value <= 100")
                }, order = 20)
        public void baseCapacity() {
        }

        @UiField(path = "details.closingDaysBeforeDeparture", component = UiField.ComponentType.INTEGER,
                labelKey = "tourwizard.basics.field.closingDays", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required", order = 30)
        public void closingDays() {
        }

        @UiField(path = "details.excludedServiceIds", component = UiField.ComponentType.MULTI_SELECT,
                labelKey = "tourwizard.basics.field.excludedServices", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.excludedServices"),
                order = 40)
        public void excludedServices() {
        }

        @UiField(path = "details.requiredDocumentIds", component = UiField.ComponentType.MULTI_SELECT,
                labelKey = "tourwizard.basics.field.requiredDocuments",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.requiredDocuments"),
                order = 50)
        public void requiredDocuments() {
        }

        @UiField(path = "details.requiredEquipmentIds", component = UiField.ComponentType.MULTI_SELECT,
                labelKey = "tourwizard.basics.field.requiredEquipment",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.requiredEquipment"),
                order = 60)
        public void requiredEquipment() {
        }

        @UiField(path = "details.difficultyLevel", component = UiField.ComponentType.SELECT,
                labelKey = "tourwizard.basics.field.difficulty",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.CALLBACK, callbackRef = "tour.difficulty"),
                order = 70)
        public void difficulty() {
        }

        @UiField(path = "details.slug", component = UiField.ComponentType.TEXT,
                labelKey = "tourwizard.basics.field.slug",
                validations = {
                        @UiValidation(messageKey = "tourwizard.validation.slug.max",
                                expression = "value == null || value.isBlank() || value.length() <= 40")
                }, colSpan = 1, order = 80)
        public void slug() {
        }
    }

    @UiGroup(id = "tour-itinerary-group", columns = 1,
            repeatable = @UiRepeatable(enabled = true, min = 0, max = 30,
                    mode = UiRepeatable.RepeatableMode.INLINE_PANEL,
                    allowManualAdd = false, allowManualRemove = false,
                    itemTitleKey = "tourwizard.basics.repeatable.itinerary"))
    public static class ItineraryGroup {

        @UiField(path = "itinerary.days.description", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "tourwizard.basics.field.itinerary.description", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required", colSpan = 1, order = 0)
        public void description() {
        }

        @UiField(path = "itinerary.days.overnightType", component = UiField.ComponentType.RADIO,
                labelKey = "tourwizard.basics.field.itinerary.overnight", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                options = @UiOptions(enabled = true, type = UiOptions.ProviderType.STATIC,
                        entries = {
                                "1|tourwizard.basics.option.overnight.stay",
                                "2|tourwizard.basics.option.overnight.transport"
                        }), order = 10)
        public void overnightType() {
        }
    }

    @UiGroup(id = "tour-notes-group", columns = 1)
    public static class NotesGroup {

        @UiField(path = "notes.cancellationPolicy", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "tourwizard.basics.field.cancellation", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                validations = {@UiValidation(messageKey = "tourwizard.validation.text.min",
                        expression = "value != null && value.length() >= 10")}, order = 0)
        public void cancellation() {
        }

        @UiField(path = "notes.description", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "tourwizard.basics.field.description", requiredWhen = "true",
                requiredMessageKey = "forms.validation.required",
                validations = {@UiValidation(messageKey = "tourwizard.validation.text.min",
                        expression = "value != null && value.length() >= 10")}, order = 10)
        public void description() {
        }

        @UiField(path = "notes.terms", component = UiField.ComponentType.TEXT_AREA,
                labelKey = "tourwizard.basics.field.terms",
                validations = {@UiValidation(messageKey = "tourwizard.validation.terms.min",
                        expression = "value == null || value.isBlank() || value.length() >= 10")}, order = 20)
        public void terms() {
        }
    }
}
