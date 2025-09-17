package com.example.adminpanel.view;

import com.example.adminpanel.component.ColumnDefinition;
import com.example.adminpanel.component.FilterDefinition;
import com.example.adminpanel.component.FilterablePaginatedGrid;
import com.example.adminpanel.model.Person;
import com.example.adminpanel.service.MockPersonService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * View demonstrating a full-height grid. The grid expands to fill the available
 * space of the page. This view is useful for scenarios where the grid is the
 * primary content on the page and should take up all remaining vertical space.
 */
@Route(value = "full-grid", layout = MainLayout.class)
@PageTitle("Full Grid")
public class FullGridView extends VerticalLayout {

    @Autowired
    public FullGridView(MockPersonService personService) {
        setSizeFull();
        setPadding(true);
        initTable(personService);
    }

    private void initTable(MockPersonService personService) {
        // Define the columns: property name, header key and type
        List<ColumnDefinition> columns = List.of(
                new ColumnDefinition("firstName", "First Name", String.class),
                new ColumnDefinition("lastName", "Last Name", String.class),
                new ColumnDefinition("email", "Email", String.class),
                new ColumnDefinition("age", "Age", Integer.class)
        );
        // Define filters. Include a few additional filters for demonstration
        List<FilterDefinition> filters = List.of(
                new FilterDefinition("firstName", "First Name", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("lastName", "Last Name", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("email", "Email", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("age", "Age", FilterDefinition.FilterType.NUMBER_RANGE, Integer.class),
                new FilterDefinition("country", "Country", List.of("USA", "UK", "Iran"), null),
                new FilterDefinition(
                        "city",
                        "City",
                        List.of(
                                "USA|New York",
                                "USA|Los Angeles",
                                "UK|London",
                                "Iran|Tehran",
                                "Iran|Mashhad",
                                "Iran|Shiraz"
                        ),
                        "country")
        );
        // Create grid with default feature set (all enabled). It will expand by default.
        FilterablePaginatedGrid<Person> grid = new FilterablePaginatedGrid<>(columns, filters, personService::fetchPage, 10);
        // Add grid and make it expand to consume available space
        add(grid);
        expand(grid);
    }
}