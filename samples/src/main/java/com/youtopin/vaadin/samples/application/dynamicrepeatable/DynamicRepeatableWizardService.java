package com.youtopin.vaadin.samples.application.dynamicrepeatable;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;

/**
 * Persists {@link DynamicRepeatableWizardState} objects in the Vaadin session so wizard steps can share data.
 */
@Service
public class DynamicRepeatableWizardService {

    public DynamicRepeatableWizardState load() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return new DynamicRepeatableWizardState();
        }
        DynamicRepeatableWizardState state = session.getAttribute(DynamicRepeatableWizardState.class);
        if (state == null) {
            state = new DynamicRepeatableWizardState();
            session.setAttribute(DynamicRepeatableWizardState.class, state);
        }
        return state;
    }

    public void store(DynamicRepeatableWizardState state) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && state != null) {
            session.setAttribute(DynamicRepeatableWizardState.class, state);
        }
    }

    public void reset() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(DynamicRepeatableWizardState.class, new DynamicRepeatableWizardState());
        }
    }
}
