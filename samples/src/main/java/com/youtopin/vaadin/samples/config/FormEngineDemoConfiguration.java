package com.youtopin.vaadin.samples.config;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.options.OptionCatalog;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;
import com.youtopin.vaadin.formengine.options.OptionItem;
import com.youtopin.vaadin.formengine.options.OptionPage;
import com.youtopin.vaadin.formengine.options.SearchQuery;
import com.youtopin.vaadin.i18n.TranslationProvider;
import com.youtopin.vaadin.samples.application.tour.TourReferenceDataService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Spring configuration wiring the form engine and registering option catalogs used by the demo samples.
 */
@Configuration
public class FormEngineDemoConfiguration {

    private final TranslationProvider translationProvider;

    public FormEngineDemoConfiguration(TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
    }

    @Bean
    public OptionCatalogRegistry optionCatalogRegistry(TourReferenceDataService tourReferenceDataService) {
        OptionCatalogRegistry registry = new OptionCatalogRegistry();
        registry.register("catalog.departments", messageCatalog(Map.of(
                "engineering", "forms.options.department.engineering",
                "design", "forms.options.department.design",
                "operations", "forms.options.department.operations",
                "support", "forms.options.department.support"
        )));
        registry.register("catalog.contract-types", messageCatalog(Map.of(
                "full-time", "forms.options.contract.fullTime",
                "part-time", "forms.options.contract.partTime",
                "contractor", "forms.options.contract.contractor"
        )));
        registry.register("catalog.equipment", messageCatalog(Map.of(
                "laptop", "forms.options.equipment.laptop",
                "desktop", "forms.options.equipment.desktop",
                "tablet", "forms.options.equipment.tablet"
        )));
        registry.register("catalog.skills", messageCatalog(Map.of(
                "java", "forms.options.skills.java",
                "ux", "forms.options.skills.ux",
                "analytics", "forms.options.skills.analytics",
                "support", "forms.options.skills.support"
        )));
        registry.register("catalog.currencies", messageCatalog(Map.of(
                "IRR", "forms.options.currency.irr",
                "USD", "forms.options.currency.usd",
                "EUR", "forms.options.currency.eur"
        )));
        registry.register("catalog.tax", messageCatalog(Map.of(
                "standard", "forms.options.tax.standard",
                "reduced", "forms.options.tax.reduced",
                "exempt", "forms.options.tax.exempt"
        )));
        registry.register("catalog.stock-status", messageCatalog(Map.of(
                "in-stock", "forms.options.stock.inStock",
                "preorder", "forms.options.stock.preorder",
                "backorder", "forms.options.stock.backorder"
        )));
        registry.register("catalog.channels", messageCatalog(Map.of(
                "web", "forms.options.channels.web",
                "mobile", "forms.options.channels.mobile",
                "retail", "forms.options.channels.retail"
        )));
        registry.register("catalog.product-tags", creatableCatalog(Map.of(
                "premium", "forms.options.productTags.premium",
                "bundle", "forms.options.productTags.bundle",
                "seasonal", "forms.options.productTags.seasonal",
                "limited", "forms.options.productTags.limited"
        )));
        registry.register("catalog.policy-effect", messageCatalog(Map.of(
                "allow", "forms.options.policyEffect.allow",
                "deny", "forms.options.policyEffect.deny"
        )));
        registry.register("catalog.policy-resources", messageCatalog(Map.of(
                "reports", "forms.options.policyResources.reports",
                "billing", "forms.options.policyResources.billing",
                "settings", "forms.options.policyResources.settings"
        )));
        registry.register("catalog.policy-actions", messageCatalog(Map.of(
                "read", "forms.options.policyActions.read",
                "write", "forms.options.policyActions.write",
                "approve", "forms.options.policyActions.approve"
        )));
        registry.register("catalog.timezones", messageCatalog(Map.of(
                "Asia/Tehran", "forms.options.timezones.tehran",
                "Europe/Helsinki", "forms.options.timezones.helsinki",
                "America/New_York", "forms.options.timezones.newYork"
        )));
        registry.register("catalog.reviewers", messageCatalog(Map.of(
                "fatemeh", "forms.options.reviewers.fatemeh",
                "hossein", "forms.options.reviewers.hossein",
                "sara", "forms.options.reviewers.sara"
        )));
        registry.register("catalog.work-model", messageCatalog(Map.of(
                "remote", "wizardform.options.workModel.remote",
                "hybrid", "wizardform.options.workModel.hybrid",
                "onsite", "wizardform.options.workModel.onsite"
        )));
        registry.register("catalog.launch-risk", messageCatalog(Map.of(
                "low", "wizardform.options.risk.low",
                "medium", "wizardform.options.risk.medium",
                "high", "wizardform.options.risk.high"
        )));
        registry.register("catalog.condition-operators", messageCatalog(Map.of(
                "equals", "forms.options.conditionOperator.equals",
                "contains", "forms.options.conditionOperator.contains",
                "startsWith", "forms.options.conditionOperator.startsWith"
        )));
        registry.register("tour.operators", searchCatalog(tourReferenceDataService::searchOperators));
        registry.register("tour.leaders", searchCatalog(tourReferenceDataService::searchTourLeaders));
        registry.register("tour.classes", searchCatalog(tourReferenceDataService::searchTourClasses));
        registry.register("tour.tags", searchCatalog(tourReferenceDataService::searchTags));
        registry.register("tour.provinces", searchCatalog(tourReferenceDataService::searchProvinces));
        registry.register("tour.provinceCities", searchCatalog(tourReferenceDataService::searchAllProvinceCities));
        registry.register("tour.continents", searchCatalog(tourReferenceDataService::searchContinents));
        registry.register("tour.countries", searchCatalog(tourReferenceDataService::searchAllCountries));
        registry.register("tour.worldCities", searchCatalog(tourReferenceDataService::searchAllWorldCities));
        registry.register("tour.excludedServices", searchCatalog(tourReferenceDataService::searchExcludedServices));
        registry.register("tour.requiredDocuments", searchCatalog(tourReferenceDataService::searchRequiredDocuments));
        registry.register("tour.requiredEquipment", searchCatalog(tourReferenceDataService::searchRequiredEquipment));
        registry.register("tour.difficulty", searchCatalog(tourReferenceDataService::searchDifficultyLevels));
        registry.register("tour.accommodationTypes", searchCatalog(tourReferenceDataService::searchAccommodationTypes));
        registry.register("tour.defaultAccommodations", searchCatalog(tourReferenceDataService::searchDefaultAccommodations));
        registry.register("tour.hotelQualities", searchCatalog(tourReferenceDataService::searchHotelQualities));
        registry.register("tour.cateringServices", searchCatalog(tourReferenceDataService::searchCateringServices));
        registry.register("tour.amenityServices", searchCatalog(tourReferenceDataService::searchAmenityServices));
        registry.register("tour.roomTypes", searchCatalog(tourReferenceDataService::searchRoomTypes));
        return registry;
    }

    @Bean
    public FormEngine formEngine(OptionCatalogRegistry optionCatalogRegistry, ApplicationContext applicationContext) {
        return new FormEngine(optionCatalogRegistry, name -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            if (applicationContext.containsBean(name)) {
                return applicationContext.getBean(name);
            }
            return null;
        });
    }

    private OptionCatalog messageCatalog(Map<String, String> entries) {
        return new MessageOptionCatalog(translationProvider, new LinkedHashMap<>(entries), false);
    }

    private OptionCatalog creatableCatalog(Map<String, String> entries) {
        return new MessageOptionCatalog(translationProvider, new LinkedHashMap<>(entries), true);
    }

    private OptionCatalog searchCatalog(Function<String, List<OptionItem>> searchFunction) {
        return new OptionCatalog() {
            @Override
            public OptionPage fetch(SearchQuery query) {
                List<OptionItem> allItems = searchFunction.apply(query.getSearch());
                int fromIndex = Math.min(query.getPage() * query.getPageSize(), allItems.size());
                int toIndex = Math.min(fromIndex + query.getPageSize(), allItems.size());
                return OptionPage.of(allItems.subList(fromIndex, toIndex), allItems.size());
            }

            @Override
            public List<OptionItem> byIds(Collection<String> ids) {
                if (ids == null || ids.isEmpty()) {
                    return List.of();
                }
                List<OptionItem> allItems = searchFunction.apply("");
                return ids.stream()
                        .filter(Objects::nonNull)
                        .flatMap(id -> allItems.stream().filter(item -> item.getId().equals(id)))
                        .distinct()
                        .toList();
            }

            @Override
            public boolean supportsCreate() {
                return false;
            }

            @Override
            public OptionItem create(String value, Locale locale, Map<String, Object> context) {
                return OptionCatalog.super.create(value, locale, context);
            }
        };
    }

    private static final class MessageOptionCatalog implements OptionCatalog {
        private final TranslationProvider translationProvider;
        private final LinkedHashMap<String, String> entries;
        private final boolean creationEnabled;
        private final Map<String, String> createdEntries;
        private final AtomicInteger sequence;

        private MessageOptionCatalog(TranslationProvider translationProvider,
                                    LinkedHashMap<String, String> entries,
                                    boolean creationEnabled) {
            this.translationProvider = translationProvider;
            this.entries = entries;
            this.creationEnabled = creationEnabled;
            if (creationEnabled) {
                this.createdEntries = Collections.synchronizedMap(new LinkedHashMap<>());
                this.sequence = new AtomicInteger(1);
            } else {
                this.createdEntries = Map.of();
                this.sequence = null;
            }
        }

        @Override
        public OptionPage fetch(SearchQuery query) {
            Locale locale = effectiveLocale(query.getLocale());
            List<OptionItem> allItems = allItems(locale).stream()
                    .filter(item -> matchesFilter(item, query, locale))
                    .toList();
            int fromIndex = Math.min(query.getPage() * query.getPageSize(), allItems.size());
            int toIndex = Math.min(fromIndex + query.getPageSize(), allItems.size());
            return OptionPage.of(allItems.subList(fromIndex, toIndex), allItems.size());
        }

        @Override
        public List<OptionItem> byIds(java.util.Collection<String> ids) {
            if (ids == null || ids.isEmpty()) {
                return List.of();
            }
            Locale locale = Locale.getDefault();
            return ids.stream()
                    .map(id -> resolveById(id, locale))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean supportsCreate() {
            return creationEnabled;
        }

        @Override
        public OptionItem create(String value, Locale locale, Map<String, Object> context) {
            if (!creationEnabled) {
                return OptionCatalog.super.create(value, locale, context);
            }
            Locale effectiveLocale = effectiveLocale(locale);
            String label = value == null ? "" : value.trim();
            if (label.isEmpty()) {
                String message = translationProvider.getTranslation("forms.catalog.tags.create.invalid", effectiveLocale);
                throw new IllegalArgumentException(message);
            }
            String slug = slugify(label, effectiveLocale);
            synchronized (createdEntries) {
                if (!slug.isBlank()) {
                    OptionItem existing = resolveById(slug, effectiveLocale);
                    if (existing != null) {
                        return existing;
                    }
                }
                String identifier = nextAvailableIdentifier(slug);
                createdEntries.put(identifier, label);
                return new OptionItem(identifier, label);
            }
        }

        private OptionItem toOptionItem(String id, String messageKey, Locale locale) {
            String label = translationProvider.getTranslation(messageKey, locale);
            return new OptionItem(id, label);
        }

        private boolean matchesFilter(OptionItem item, SearchQuery query, Locale locale) {
            String filter = query.getSearch();
            if (filter == null || filter.isBlank()) {
                return true;
            }
            String normalized = filter.toLowerCase(locale);
            return item.getLabel().toLowerCase(locale).contains(normalized);
        }

        private List<OptionItem> allItems(Locale locale) {
            List<OptionItem> baseItems = entries.entrySet().stream()
                    .map(entry -> toOptionItem(entry.getKey(), entry.getValue(), locale))
                    .toList();
            if (!creationEnabled) {
                return baseItems;
            }
            List<OptionItem> dynamicItems;
            synchronized (createdEntries) {
                dynamicItems = createdEntries.entrySet().stream()
                        .map(entry -> new OptionItem(entry.getKey(), entry.getValue()))
                        .toList();
            }
            return java.util.stream.Stream.concat(baseItems.stream(), dynamicItems.stream())
                    .toList();
        }

        private OptionItem resolveById(String id, Locale locale) {
            if (id == null) {
                return null;
            }
            if (entries.containsKey(id)) {
                return toOptionItem(id, entries.get(id), locale);
            }
            if (!creationEnabled) {
                return null;
            }
            String label;
            synchronized (createdEntries) {
                label = createdEntries.get(id);
            }
            return label == null ? null : new OptionItem(id, label);
        }

        private Locale effectiveLocale(Locale locale) {
            return locale != null ? locale : Locale.getDefault();
        }

        private String slugify(String value, Locale locale) {
            String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                    .toLowerCase(locale);
            String collapsed = normalized.replaceAll("[^\\p{L}\\p{Nd}]+", "-");
            return collapsed.replaceAll("^-+", "").replaceAll("-+$", "");
        }

        private String nextAvailableIdentifier(String slug) {
            String prefix = (slug == null || slug.isBlank()) ? "tag" : slug;
            String candidate = (slug == null || slug.isBlank()) ? prefix + '-' + sequence.getAndIncrement() : slug;
            while (entries.containsKey(candidate) || createdEntries.containsKey(candidate)) {
                candidate = prefix + '-' + sequence.getAndIncrement();
            }
            return candidate;
        }
    }
}
