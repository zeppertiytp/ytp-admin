package com.example.adminpanel.view;

import com.example.adminpanel.component.ColumnDefinition;
import com.example.adminpanel.component.FilterablePaginatedGrid;
import com.example.adminpanel.component.FilterDefinition;
import com.example.adminpanel.model.Person;
import com.example.adminpanel.service.MockPersonService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
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
@PageTitle("Persons")
public class PersonTableView extends VerticalLayout implements BeforeEnterObserver {

    private final MockPersonService personService;
    private FilterablePaginatedGrid<Person> grid;

    @Autowired
    public PersonTableView(MockPersonService personService) {
        this.personService = personService;
        setSizeFull();
        setPadding(true);
        initTable();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (grid != null) {
            java.util.Map<String, java.util.List<String>> params = event.getLocation().getQueryParameters().getParameters();
            grid.applyFiltersFromQueryParams(params);
        }
    }

    private void initTable() {
        // Define columns: property name, header, and type
        List<ColumnDefinition> columns = List.of(
                new ColumnDefinition("firstName", "First Name", String.class),
                new ColumnDefinition("lastName", "Last Name", String.class),
                new ColumnDefinition("email", "Email", String.class),
                new ColumnDefinition("age", "Age", Integer.class)
        );
        // Define filters.  Include the city property even though it is not displayed
        // as a column.  Labels can later be externalised via translation keys.
        List<FilterDefinition> filters = List.of(
                new FilterDefinition("firstName", "First Name", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("lastName", "Last Name", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("email", "Email", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("age", "Age", FilterDefinition.FilterType.NUMBER_RANGE, Integer.class),
                // Country select: standalone list
                new FilterDefinition("country", "Country", java.util.List.of("USA", "UK", "Iran"), null),
                // City select depends on country.  Encode options with "country|city" so that
                // the dependent filter can extract the appropriate list.
                new FilterDefinition(
                        "city",
                        "City",
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
        add(grid);
        expand(grid);
    }
}