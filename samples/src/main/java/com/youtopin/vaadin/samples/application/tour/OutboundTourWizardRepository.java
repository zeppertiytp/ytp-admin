package com.youtopin.vaadin.samples.application.tour;

/**
 * Contract for persisting the outbound tour wizard state.
 */
public interface OutboundTourWizardRepository {

    OutboundTourWizardState load();

    void store(OutboundTourWizardState state);

    void reset();

    String ensureProductId(OutboundTourWizardState state);
}
