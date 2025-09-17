package com.example.adminpanel.view;

import com.example.adminpanel.component.ColumnDefinition;
import com.example.adminpanel.component.FilterablePaginatedGrid;
import com.example.adminpanel.component.FilterDefinition;
import com.example.adminpanel.model.Person;
import com.example.adminpanel.service.MockPersonService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * View demonstrating the {@link FilterablePaginatedGrid} with mock
 * Person data.  The grid supports pagination and per-column filters.
 */
@Route(value = "persons", layout = MainLayout.class)
public class PersonTableView extends ViewFrame implements BeforeEnterObserver, LocaleChangeObserver {

    private final MockPersonService personService;
    private FilterablePaginatedGrid<Person> grid;
    private H2 heading;

    @Autowired
    public PersonTableView(MockPersonService personService) {
        this.personService = personService;
        VerticalLayout content = createContentSection(ContentWidth.FULL);
        content.addClassName("grid-view");

        heading = new H2(getTranslation("persons.title"));
        heading.addClassName("view-title");
        content.add(heading);

        VerticalLayout gridSection = buildGridSection();
        content.add(gridSection);
        content.setFlexGrow(1, gridSection);
        updatePageTitle();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (grid != null) {
            java.util.Map<String, java.util.List<String>> params = event.getLocation().getQueryParameters().getParameters();
            grid.applyFiltersFromQueryParams(params);
        }
    }

    private VerticalLayout buildGridSection() {
        // Define columns: property name, header, and type
        List<ColumnDefinition> columns = List.of(
                new ColumnDefinition("firstName", "person.firstName", String.class),
                new ColumnDefinition("lastName", "person.lastName", String.class),
                new ColumnDefinition("email", "person.email", String.class),
                new ColumnDefinition("age", "person.age", Integer.class)
        );
        // Define filters.  Include the city property even though it is not displayed
        // as a column.  Labels can later be externalised via translation keys.
        List<FilterDefinition> filters = List.of(
                new FilterDefinition("firstName", "person.firstName", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("lastName", "person.lastName", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("email", "person.email", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("age", "person.age", FilterDefinition.FilterType.NUMBER_RANGE, Integer.class),
                // Country select: standalone list
                new FilterDefinition("country", "person.country", java.util.List.of("USA", "UK", "Iran"), null),
                // City select depends on country.  Encode options with "country|city" so that
                // the dependent filter can extract the appropriate list.
                new FilterDefinition(
                        "city",
                        "person.city",
                        java.util.List.of(
                                "USA|New York",
                                "USA|Los Angeles",
                                "UK|London",
                                "Iran|Tehran",
                                "Iran|Mashhad",
                                "Iran|Shiraz"
                        ),
                        "country")
        );
        grid = new FilterablePaginatedGrid<>(
                columns,
                filters,
                personService::fetchPage,
                10);
        // Set callback to update URL when filters change
        grid.setFiltersChangedCallback(updatedFilters -> {
            // Convert filters map to multi-value map for QueryParameters
            java.util.Map<String, java.util.List<String>> qp = new java.util.HashMap<>();
            updatedFilters.forEach((k, v) -> qp.put(k, java.util.List.of(v)));
            QueryParameters params = new QueryParameters(qp);
            // Navigate to same view with updated parameters
            com.vaadin.flow.component.UI.getCurrent().navigate(PersonTableView.class, params);
        });
        grid.setSizeFull();

        VerticalLayout wrapper = new VerticalLayout();
        wrapper.addClassNames("surface-card", "view-section");
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setWidthFull();
        wrapper.setHeightFull();
        wrapper.add(grid);
        wrapper.setFlexGrow(1, grid);
        return wrapper;
    }

    private void updatePageTitle() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("persons.title"));
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        heading.setText(getTranslation("persons.title"));
        updatePageTitle();
    }
}