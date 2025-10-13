package com.youtopin.vaadin.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * {@link I18NProvider} implementation that aggregates translations from YAML configuration files.
 * <p>
 * The provider scans the classpath for files matching {@code translations/*.yaml}. Each file can
 * declare a default locale, the list of supported locales, and a map of translation keys per locale.
 * Files are processed in alphabetical order so that applications can override defaults provided by
 * the component library simply by choosing a filename that sorts later (for example,
 * {@code 10-application.yaml} overrides {@code 00-default.yaml}).
 */
@Component
public class TranslationProvider implements I18NProvider {

    static final String RESOURCE_PATTERN = "classpath*:translations/*.yaml";
    private static final Locale FALLBACK_LOCALE = Locale.forLanguageTag("fa");

    private final Locale defaultLocale;
    private final List<Locale> providedLocales;
    private final Map<Locale, Map<String, String>> messages;

    public TranslationProvider() {
        this(new PathMatchingResourcePatternResolver(), new ObjectMapper(new YAMLFactory()));
    }

    TranslationProvider(PathMatchingResourcePatternResolver resolver, ObjectMapper objectMapper) {
        Map<Locale, LinkedHashMap<String, String>> collectedMessages = new LinkedHashMap<>();
        LinkedHashSet<Locale> locales = new LinkedHashSet<>();
        Locale defaultLocaleCandidate = FALLBACK_LOCALE;

        try {
            Resource[] resources = resolver.getResources(RESOURCE_PATTERN);
            Arrays.sort(resources, Comparator.comparing(TranslationProvider::resourceOrderKey));
            for (Resource resource : resources) {
                try (InputStream stream = resource.getInputStream()) {
                    TranslationBundle bundle = objectMapper.readValue(stream, TranslationBundle.class);
                    if (bundle.getDefaultLocale() != null && !bundle.getDefaultLocale().isBlank()) {
                        defaultLocaleCandidate = parseLocale(bundle.getDefaultLocale(), defaultLocaleCandidate);
                    }
                    if (bundle.getProvidedLocales() != null) {
                        final Locale effectiveDefault = defaultLocaleCandidate;
                        bundle.getProvidedLocales().stream()
                                .map(tag -> parseLocale(tag, effectiveDefault))
                                .forEach(locales::add);
                    }
                    if (bundle.getMessages() != null) {
                        final Locale effectiveDefault = defaultLocaleCandidate;
                        bundle.getMessages().forEach((localeTag, entries) -> {
                            Locale locale = parseLocale(localeTag, effectiveDefault);
                            locales.add(locale);
                            LinkedHashMap<String, String> localeMessages =
                                    collectedMessages.computeIfAbsent(locale, key -> new LinkedHashMap<>());
                            if (entries != null) {
                                entries.forEach((key, value) -> {
                                    if (key != null && value != null) {
                                        localeMessages.put(key, value);
                                    }
                                });
                            }
                        });
                    }
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load translation resources", ex);
        }

        if (locales.isEmpty()) {
            locales.add(defaultLocaleCandidate);
        } else if (!locales.contains(defaultLocaleCandidate)) {
            locales.add(defaultLocaleCandidate);
        }

        this.defaultLocale = defaultLocaleCandidate;
        this.providedLocales = List.copyOf(locales);

        Map<Locale, Map<String, String>> immutableMessages = new LinkedHashMap<>();
        collectedMessages.forEach((locale, values) -> immutableMessages.put(locale, Map.copyOf(values)));
        this.messages = Map.copyOf(immutableMessages);
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return providedLocales;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        if (key == null) {
            return "";
        }
        Locale effectiveLocale = Optional.ofNullable(locale).orElse(defaultLocale);
        Map<String, String> localeMessages = findMessages(effectiveLocale);

        String message = localeMessages.get(key);
        if (message == null) {
            message = findMessages(defaultLocale).getOrDefault(key, key);
        }

        if (params != null && params.length > 0) {
            MessageFormat formatter = new MessageFormat(message, effectiveLocale);
            return formatter.format(params);
        }
        return message;
    }

    /**
     * Exposes the configured default locale so other infrastructure (such as the UI initializer)
     * can stay in sync with the translation provider.
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    private Map<String, String> findMessages(Locale locale) {
        Map<String, String> localeMessages = messages.get(locale);
        if (localeMessages != null) {
            return localeMessages;
        }
        if (locale != null && !locale.getCountry().isEmpty()) {
            Locale languageOnly = new Locale(locale.getLanguage());
            localeMessages = messages.get(languageOnly);
            if (localeMessages != null) {
                return localeMessages;
            }
        }
        return messages.getOrDefault(defaultLocale, Collections.emptyMap());
    }

    private static Locale parseLocale(String candidate, Locale fallback) {
        if (candidate == null || candidate.isBlank()) {
            return fallback;
        }
        Locale locale = Locale.forLanguageTag(candidate.replace('_', '-'));
        if (locale.getLanguage().isEmpty()) {
            return fallback;
        }
        return locale;
    }

    private static String resourceOrderKey(Resource resource) {
        try {
            return resource.getFilename() + "|" + resource.getURL();
        } catch (IOException ex) {
            return resource.getDescription();
        }
    }

    private static final class TranslationBundle {
        private String defaultLocale;
        private List<String> providedLocales = new ArrayList<>();
        private Map<String, Map<String, String>> messages = new LinkedHashMap<>();

        public String getDefaultLocale() {
            return defaultLocale;
        }

        public void setDefaultLocale(String defaultLocale) {
            this.defaultLocale = defaultLocale;
        }

        public List<String> getProvidedLocales() {
            return providedLocales;
        }

        public void setProvidedLocales(List<String> providedLocales) {
            this.providedLocales = providedLocales != null ? new ArrayList<>(providedLocales) : new ArrayList<>();
        }

        public Map<String, Map<String, String>> getMessages() {
            return messages;
        }

        public void setMessages(Map<String, Map<String, String>> messages) {
                this.messages = messages != null ? new LinkedHashMap<>(messages) : new LinkedHashMap<>();
        }
    }
}
