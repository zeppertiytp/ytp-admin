package com.youtopin.vaadin.formengine.options;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Delegates option resolution to a remote client stub.
 */
public final class RemoteOptions implements OptionCatalog {

    public interface RemoteClient {
        OptionPage fetch(SearchQuery query);

        List<OptionItem> byIds(Collection<String> ids);

        default boolean supportsCreate() {
            return false;
        }

        default OptionItem create(String value, Locale locale, Map<String, Object> context) {
            throw new UnsupportedOperationException("Creation is not supported by this client");
        }
    }

    private final RemoteClient client;

    public RemoteOptions(RemoteClient client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    @Override
    public OptionPage fetch(SearchQuery query) {
        return client.fetch(query);
    }

    @Override
    public List<OptionItem> byIds(Collection<String> ids) {
        return client.byIds(ids);
    }

    @Override
    public boolean supportsCreate() {
        return client.supportsCreate();
    }

    @Override
    public OptionItem create(String value, Locale locale, Map<String, Object> context) {
        return client.create(value, locale, context);
    }
}
