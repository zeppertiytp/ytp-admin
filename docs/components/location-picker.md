# LocationPicker

`LocationPicker` embeds a Leaflet map with address search, reverse geocoding, and
coordinate output tailored for Vaadin forms.

- **Source:** `components/src/main/java/com/youtopin/vaadin/component/LocationPicker.java`
- **Frontend module:** `components/src/main/resources/META-INF/resources/frontend/components/location-picker.ts`
- **Dependencies:** `leaflet`, `@types/leaflet`

## Key capabilities
- Raises `LocationSelectedEvent` with latitude and longitude whenever the user
  confirms a location (map click or search result selection).
- Locale-aware labels for search inputs, loading states, empty results, and
  errors via `LocaleChangeObserver`.
- Exposes `addLocationSelectedListener` for Flow views to react to user choices.
- Client callable bridge (`setLocation`) invoked from the TypeScript module when
  the map marker moves.

## Usage
```java
LocationPicker picker = new LocationPicker();
picker.addLocationSelectedListener(event ->
        presenter.updateCoordinates(event.getLat(), event.getLng()));
```

## Accessibility & localisation
- Search input placeholders and labels respect the active UI locale; provide
  translations for the keys listed in `applyTranslations()`.
- The frontend module configures keyboard navigation within the search dialog
  and ensures the map container exposes appropriate `aria-label`s.

## Sample references
- Generated forms include a map field (`type": "map"`) in
  `samples/src/main/resources/forms/user_form.json`, rendered by `GeneratedForm`
  within `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/FormGenerationView.java`.
- The annotation form engine supports `@UiField(component = MAP)` which emits the
  same JSON metadata (see `docs/form-engine-reference.md`).

