package com.youtopin.vaadin.formengine.options;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Delegates option resolution to a remote client stub.
 */
public final class RemoteOptions implements OptionCatalog {

    public interface RemoteClient {
        OptionPage fetch(SearchQuery query);

        List<OptionItem> byIds(Collection<String> ids);
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
}
