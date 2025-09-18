package com.example.adminpanel.view;

import com.example.adminpanel.component.ColumnDefinition;
import com.example.adminpanel.component.FilterDefinition;
import com.example.adminpanel.component.FilterablePaginatedGrid;
import com.example.adminpanel.component.layout.AppPageLayout;
import com.example.adminpanel.model.Person;
import com.example.adminpanel.service.MockPersonService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * View demonstrating a full-height grid. The grid expands to fill the available
 * space of the page. This view is useful for scenarios where the grid is the
 * primary content on the page and should take up all remaining vertical space.
 */
@Route(value = "full-grid", layout = MainLayout.class)
public class FullGridView extends AppPageLayout implements LocaleChangeObserver {

    @Autowired
    public FullGridView(MockPersonService personService) {
        pageTitle = createPageTitle(getTranslation("fullGrid.title"));
        initTable(personService);
        updatePageTitle();
    }

    private final H1 pageTitle;

    private void initTable(MockPersonService personService) {
        // Define the columns: property name, header key and type
        List<ColumnDefinition> columns = List.of(
                new ColumnDefinition("firstName", "person.firstName", String.class),
                new ColumnDefinition("lastName", "person.lastName", String.class),
                new ColumnDefinition("email", "person.email", String.class),
                new ColumnDefinition("age", "person.age", Integer.class)
        );
        // Define filters. Include a few additional filters for demonstration
        List<FilterDefinition> filters = List.of(
                new FilterDefinition("firstName", "person.firstName", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("lastName", "person.lastName", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("email", "person.email", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("age", "person.age", FilterDefinition.FilterType.NUMBER_RANGE, Integer.class),
                new FilterDefinition("country", "person.country", List.of("USA", "UK", "Iran"), null),
                new FilterDefinition(
                        "city",
                        "person.city",
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
        grid.setWidthFull();
        grid.setHeightFull();

        VerticalLayout gridCard = createCard(grid);
        gridCard.addClassName("app-card--fill");
        gridCard.setSizeFull();
        add(gridCard);
        setFlexGrow(1, gridCard);
    }

    private void updatePageTitle() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("fullGrid.title"));
        }
        pageTitle.setText(getTranslation("fullGrid.title"));
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updatePageTitle();
    }
}