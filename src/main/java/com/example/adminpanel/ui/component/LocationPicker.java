package com.example.adminpanel.ui.component;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;

@Tag("location-picker")
@NpmPackage(value = "leaflet", version = "1.9.4")
@NpmPackage(value = "@types/leaflet", version = "1.9.7")
@JsModule("./components/location-picker.ts")
public class LocationPicker extends Component implements LocaleChangeObserver {

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        applyTranslations();
    }

    @ClientCallable
    private void setLocation(double lat, double lng) {
        fireEvent(new LocationSelectedEvent(this, lat, lng));
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        applyTranslations();
    }

    private void applyTranslations() {
        getElement().setProperty("searchPlaceholder", getTranslation("form.location.searchPlaceholder"));
        getElement().setProperty("searchLabel", getTranslation("form.location.search"));
        getElement().setProperty("searchingLabel", getTranslation("form.location.searching"));
        getElement().setProperty("noResultsLabel", getTranslation("form.location.noResults"));
        getElement().setProperty("fetchErrorLabel", getTranslation("form.location.fetchError"));
        getElement().setProperty("noSelectionLabel", getTranslation("form.location.noSelection"));
        getElement().setProperty("selectedLabel", getTranslation("form.location.selectedLabel"));
    }

    public Registration addLocationSelectedListener(ComponentEventListener<LocationSelectedEvent> l) {
        return addListener(LocationSelectedEvent.class, l);
    }

    public static class LocationSelectedEvent extends ComponentEvent<LocationPicker> {
        private final double lat, lng;
        public LocationSelectedEvent(LocationPicker src, double lat, double lng) {
            super(src, false); this.lat = lat; this.lng = lng;
        }
        public double getLat() { return lat; }
        public double getLng() { return lng; }
    }
}
