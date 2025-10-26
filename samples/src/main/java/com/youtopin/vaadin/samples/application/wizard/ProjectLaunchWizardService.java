package com.youtopin.vaadin.samples.application.wizard;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;


/**
 * Stores and retrieves {@link ProjectLaunchWizardState} instances from the active Vaadin session.
 */
@Service
public class ProjectLaunchWizardService {

    public ProjectLaunchWizardState load() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return new ProjectLaunchWizardState();
        }
        ProjectLaunchWizardState state = session.getAttribute(ProjectLaunchWizardState.class);
        if (state == null) {
            state = new ProjectLaunchWizardState();
            session.setAttribute(ProjectLaunchWizardState.class, state);
        }
        return state;
    }

    public void store(ProjectLaunchWizardState state) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && state != null) {
            session.setAttribute(ProjectLaunchWizardState.class, state);
        }
    }

    public void reset() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(ProjectLaunchWizardState.class, new ProjectLaunchWizardState());
        }
    }
}
