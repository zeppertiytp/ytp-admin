package com.youtopin.vaadin.formengine.binder;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.youtopin.vaadin.formengine.definition.FieldDefinition;

/**
 * Snapshot of the current form state supplied to validation expressions.
 *
 * @param <T>
 *            bean type managed by the orchestrator
 */
public final class ValidationContext<T> {

    private final T bean;
    private final Map<String, Object> values;
    private final Set<String> valueKeys;
    private final Map<FieldDefinition, Function<String, Object>> scopedReaders;
    private final BiFunction<T, String, Object> beanReader;

    private ValidationContext(T bean,
                              Map<String, Object> values,
                              Map<FieldDefinition, Function<String, Object>> scopedReaders,
                              BiFunction<T, String, Object> beanReader) {
        this.bean = bean;
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        this.valueKeys = Set.copyOf(values.keySet());
        this.scopedReaders = Collections.unmodifiableMap(new IdentityHashMap<>(scopedReaders));
        this.beanReader = beanReader;
    }

    public T getBean() {
        return bean;
    }

    /**
     * Returns an unmodifiable map of the converted field values captured from the UI.
     */
    public Map<String, Object> getValues() {
        return values;
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public boolean containsValue(String key) {
        return valueKeys.contains(key);
    }

    /**
     * Resolves a value for the supplied path within the scope of the current field definition.
     */
    public Object read(FieldDefinition definition, String path) {
        if (definition != null) {
            Function<String, Object> reader = scopedReaders.get(definition);
            if (reader != null) {
                Object scoped = reader.apply(path);
                if (scoped != null) {
                    return scoped;
                }
            }
        }
        return read(path);
    }

    /**
     * Resolves a value for the supplied path using the global snapshot.
     */
    public Object read(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String trimmed = path.trim();
        if ("bean".equals(trimmed)) {
            return bean;
        }
        Object direct = lookupValue(trimmed);
        if (direct != null || containsValue(trimmed)) {
            return direct;
        }
        String normalized = normalizePath(trimmed);
        if (!normalized.equals(trimmed)) {
            Object normalizedValue = lookupValue(normalized);
            if (normalizedValue != null || containsValue(normalized)) {
                return normalizedValue;
            }
        }
        if (bean == null || beanReader == null) {
            return null;
        }
        try {
            return beanReader.apply(bean, normalized);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private Object lookupValue(String key) {
        Object direct = values.get(key);
        if (direct != null) {
            return direct;
        }
        IndexLookup indexLookup = IndexLookup.parse(key);
        if (indexLookup == null) {
            return null;
        }
        Object aggregate = values.get(indexLookup.basePath());
        if (aggregate instanceof java.util.List<?> list) {
            int index = indexLookup.index();
            if (index >= 0 && index < list.size()) {
                Object nested = list.get(index);
                if (indexLookup.suffix().isEmpty()) {
                    return nested;
                }
                if (nested instanceof Map<?, ?> map) {
                    return map.get(indexLookup.suffix());
                }
            }
        }
        return null;
    }

    private String normalizePath(String path) {
        String normalized = path;
        if (normalized.startsWith("bean.")) {
            normalized = normalized.substring("bean.".length());
        }
        return normalized;
    }

    public static <T> ValidationContext<T> of(T bean,
                                              Map<String, Object> values,
                                              Map<FieldDefinition, Function<String, Object>> scopedReaders,
                                              BiFunction<T, String, Object> beanReader) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(scopedReaders, "scopedReaders");
        return new ValidationContext<>(bean, values, scopedReaders, beanReader);
    }

    public static <T> ValidationContext<T> empty(T bean) {
        return new ValidationContext<>(bean, Map.of(), Map.of(), null);
    }

    private record IndexLookup(String basePath, int index, String suffix) {

        private static IndexLookup parse(String token) {
            int bracket = token.indexOf('[');
            int closing = token.indexOf(']');
            if (bracket < 0 || closing < bracket) {
                return null;
            }
            String base = token.substring(0, bracket);
            String indexPart = token.substring(bracket + 1, closing);
            int parsedIndex;
            try {
                parsedIndex = Integer.parseInt(indexPart.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
            String suffix = "";
            if (closing + 1 < token.length()) {
                String remainder = token.substring(closing + 1);
                if (remainder.startsWith(".")) {
                    suffix = remainder.substring(1);
                }
            }
            return new IndexLookup(base, parsedIndex, suffix);
        }
    }
}
