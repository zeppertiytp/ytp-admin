package com.youtopin.vaadin.formengine.options;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.youtopin.vaadin.formengine.annotation.UiOptions;
import com.youtopin.vaadin.formengine.definition.FieldDefinition;

/**
 * Registry creating option providers based on {@link UiOptions} metadata.
 */
public final class OptionCatalogRegistry {

    private final Map<String, OptionCatalog> catalogById = new ConcurrentHashMap<>();
    private final Map<String, OptionCatalog> explicitCatalogs = new ConcurrentHashMap<>();

    public void register(String key, OptionCatalog catalog) {
        explicitCatalogs.put(key, catalog);
    }

    public OptionCatalog resolve(FieldDefinition fieldDefinition, Locale locale) {
        if (!fieldDefinition.getOptionsDefinition().isEnabled()) {
            return OptionCatalog.EMPTY;
        }
        String cacheKey = fieldDefinition.getPath() + locale.toLanguageTag();
        OptionCatalog explicit = explicitCatalogs.get(fieldDefinition.getOptionsDefinition().getCallbackRef());
        if (explicit != null) {
            return explicit;
        }
        return catalogById.computeIfAbsent(cacheKey, key -> create(fieldDefinition, locale));
    }

    private OptionCatalog create(FieldDefinition fieldDefinition, Locale locale) {
        UiOptions.ProviderType providerType = fieldDefinition.getOptionsDefinition().getProviderType();
        return switch (providerType) {
            case STATIC -> new StaticOptions(fieldDefinition.getOptionsDefinition().getEntries().toArray(String[]::new),
                    locale);
            case ENUM -> buildEnum(fieldDefinition, locale);
            case CALLBACK -> OptionCatalog.EMPTY; // callback wiring provided by runtime integrator
            case REMOTE -> OptionCatalog.EMPTY;
            case CASCADING -> OptionCatalog.EMPTY;
        };
    }

    private OptionCatalog buildEnum(FieldDefinition fieldDefinition, Locale locale) {
        try {
            Class<?> enumType = Class.forName(fieldDefinition.getOptionsDefinition().getEnumType());
            if (!enumType.isEnum()) {
                throw new IllegalArgumentException(enumType + " is not an enum");
            }
            @SuppressWarnings("unchecked")
            Class<? extends Enum<?>> typed = (Class<? extends Enum<?>>) enumType;
            return new EnumOptions(typed, locale);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to load enum type "
                    + fieldDefinition.getOptionsDefinition().getEnumType(), e);
        }
    }
}
