package com.example.adminpanel.component;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

@Tag("location-picker")
@NpmPackage(value = "leaflet", version = "1.9.4")
@NpmPackage(value = "@types/leaflet", version = "1.9.7")
@JsModule("./components/location-picker.ts")
public class LocationPicker extends Component {

    @ClientCallable
    private void setLocation(double lat, double lng) {
        fireEvent(new LocationSelectedEvent(this, lat, lng));
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
