package com.youtopin.vaadin.samples.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.youtopin.vaadin.formengine.FormEngine;
import com.youtopin.vaadin.formengine.options.OptionCatalog;
import com.youtopin.vaadin.formengine.options.OptionCatalogRegistry;
import com.youtopin.vaadin.formengine.options.OptionItem;
import com.youtopin.vaadin.formengine.options.OptionPage;
import com.youtopin.vaadin.formengine.options.SearchQuery;
import com.youtopin.vaadin.i18n.TranslationProvider;
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
    public OptionCatalogRegistry optionCatalogRegistry() {
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
        registry.register("catalog.product-tags", messageCatalog(Map.of(
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
        registry.register("catalog.condition-operators", messageCatalog(Map.of(
                "equals", "forms.options.conditionOperator.equals",
                "contains", "forms.options.conditionOperator.contains",
                "startsWith", "forms.options.conditionOperator.startsWith"
        )));
        return registry;
    }

    @Bean
    public FormEngine formEngine(OptionCatalogRegistry optionCatalogRegistry) {
        return new FormEngine(optionCatalogRegistry);
    }

    private OptionCatalog messageCatalog(Map<String, String> entries) {
        return new MessageOptionCatalog(translationProvider, new LinkedHashMap<>(entries));
    }

    private static final class MessageOptionCatalog implements OptionCatalog {
        private final TranslationProvider translationProvider;
        private final LinkedHashMap<String, String> entries;

        private MessageOptionCatalog(TranslationProvider translationProvider, LinkedHashMap<String, String> entries) {
            this.translationProvider = translationProvider;
            this.entries = entries;
        }

        @Override
        public OptionPage fetch(SearchQuery query) {
            List<OptionItem> allItems = entries.entrySet().stream()
                    .map(entry -> toOptionItem(entry.getKey(), entry.getValue(), query.getLocale()))
                    .filter(item -> matchesFilter(item, query))
                    .toList();
            int fromIndex = Math.min(query.getPage() * query.getPageSize(), allItems.size());
            int toIndex = Math.min(fromIndex + query.getPageSize(), allItems.size());
            return OptionPage.of(allItems.subList(fromIndex, toIndex), allItems.size());
        }

        @Override
        public List<OptionItem> byIds(java.util.Collection<String> ids) {
            return ids.stream()
                    .filter(entries::containsKey)
                    .map(id -> toOptionItem(id, entries.get(id), Locale.getDefault()))
                    .collect(Collectors.toList());
        }

        private OptionItem toOptionItem(String id, String messageKey, Locale locale) {
            String label = translationProvider.getTranslation(messageKey, locale);
            return new OptionItem(id, label);
        }

        private boolean matchesFilter(OptionItem item, SearchQuery query) {
            String filter = query.getSearch();
            if (filter == null || filter.isBlank()) {
                return true;
            }
            Locale locale = query.getLocale();
            String normalized = filter.toLowerCase(locale);
            return item.getLabel().toLowerCase(locale).contains(normalized);
        }
    }
}
