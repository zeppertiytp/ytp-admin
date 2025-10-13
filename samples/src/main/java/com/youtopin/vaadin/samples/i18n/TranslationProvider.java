package com.youtopin.vaadin.samples.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

/**
 * Implementation of Vaadin's {@link I18NProvider} that loads
 * translations from Java property files.  Vaadin will invoke this
 * provider whenever a call to {@link com.vaadin.flow.i18n.I18N#get(String)}
 * or {@code UI.getTranslation(String)} is made.
 *
 * <p>
 * The translations are stored in the {@code src/main/resources}
 * directory under the names {@code messages.properties} (default
 * language) and {@code messages_fa.properties} (Farsi).  You can add
 * more languages by providing additional files following the
 * convention {@code messages_xx.properties} where {@code xx} is the
 * two‑letter ISO 639 language code.
 */
@Component
public class TranslationProvider implements I18NProvider {

    private static final String BUNDLE_PREFIX = "messages";
    private final Map<Locale, ResourceBundle> bundles = new HashMap<>();

    /**
     * Returns the translation for the given key in the specified locale.
     * If the key or locale is not found a fallback is returned.
     *
     * @param key    the message key
     * @param locale the locale requesting the translation
     * @param params optional parameters to format into the translation
     * @return the translated string or the key itself if no translation exists
     */
    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        if (key == null) {
            return "";
        }
        locale = Objects.requireNonNullElse(locale, getDefaultLocale());
        ResourceBundle bundle = bundles.computeIfAbsent(locale, this::loadBundle);
        String value;
        try {
            value = bundle.getString(key);
        } catch (MissingResourceException e) {
            // Key not found: return key itself to aid debugging
            value = key;
        }
        if (params != null && params.length > 0) {
            MessageFormat formatter = new MessageFormat(value, locale);
            value = formatter.format(params);
        }
        return value;
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(new Locale("fa"), Locale.ENGLISH);
    }

    /**
     * Default locale used when none is provided.  The admin panel defaults
     * to Farsi in accordance with the project requirements.
     *
     * @return the default locale
     */
    private Locale getDefaultLocale() {
        return new Locale("fa");
    }

    /**
     * Loads a resource bundle for the given locale.  Resource bundles
     * follow the naming convention {@code messages[_language].properties}.
     *
     * @param locale the locale whose bundle to load
     * @return the loaded resource bundle
     */
    private ResourceBundle loadBundle(Locale locale) {
        /*
         * Always load the base bundle name "messages" for the given locale.
         * ResourceBundle will automatically resolve to more specific files such
         * as messages_fa.properties when available or fall back to
         * messages.properties for English and other languages.  Avoid
         * constructing names like "messages_en" which cause missing
         * resource exceptions when no such file is present.
         */
        return ResourceBundle.getBundle(BUNDLE_PREFIX, locale);
    }
}
