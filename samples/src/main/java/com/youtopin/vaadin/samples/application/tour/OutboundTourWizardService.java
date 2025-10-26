package com.youtopin.vaadin.samples.application.tour;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

/**
 * Application service coordinating access to the outbound tour wizard repository.
 */
@Service
public class OutboundTourWizardService {

    private final OutboundTourWizardRepository repository;

    public OutboundTourWizardService(OutboundTourWizardRepository repository) {
        this.repository = repository;
    }

    public OutboundTourWizardState load() {
        return repository.load();
    }

    public void store(OutboundTourWizardState state) {
        repository.store(state);
    }

    public void reset() {
        repository.reset();
    }

    public String ensureProductId(OutboundTourWizardState state) {
        return repository.ensureProductId(state);
    }

    public void synchronizeDestinations(OutboundTourWizardState state, List<String> destinationCityIds,
                                         Function<String, String> cityNameResolver) {
        if (state == null) {
            return;
        }
        state.syncAccommodationDestinations(destinationCityIds, cityNameResolver);
        repository.store(state);
    }
}
