package com.youtopin.vaadin.samples.infrastructure.tour;

import com.youtopin.vaadin.samples.application.tour.OutboundTourWizardRepository;
import com.youtopin.vaadin.samples.application.tour.OutboundTourWizardState;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple session-scoped repository storing a single outbound tour draft.
 */
@Repository
public class InMemoryOutboundTourWizardRepository implements OutboundTourWizardRepository {

    private final AtomicInteger sequence = new AtomicInteger(1);

    @Override
    public OutboundTourWizardState load() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return new OutboundTourWizardState();
        }
        OutboundTourWizardState state = session.getAttribute(OutboundTourWizardState.class);
        if (state == null) {
            state = new OutboundTourWizardState();
            session.setAttribute(OutboundTourWizardState.class, state);
        }
        return state;
    }

    @Override
    public void store(OutboundTourWizardState state) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && state != null) {
            session.setAttribute(OutboundTourWizardState.class, state);
        }
    }

    @Override
    public void reset() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(OutboundTourWizardState.class, new OutboundTourWizardState());
        }
    }

    @Override
    public String ensureProductId(OutboundTourWizardState state) {
        if (state == null) {
            return "";
        }
        if (!state.hasProductId()) {
            String generated = String.format(Locale.ROOT, "TOUR-%04d", sequence.getAndIncrement());
            state.setProductId(generated);
        }
        store(state);
        return state.getProductId();
    }
}
