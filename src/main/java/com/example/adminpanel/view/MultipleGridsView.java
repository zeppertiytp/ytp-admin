package com.example.adminpanel.view;

import com.example.adminpanel.component.ColumnDefinition;
import com.example.adminpanel.component.FilterDefinition;
import com.example.adminpanel.component.FilterablePaginatedGrid;
import com.example.adminpanel.component.FilterablePaginatedGrid.GridFeature;
import com.example.adminpanel.model.Person;
import com.example.adminpanel.service.MockPersonService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.EnumSet;
import java.util.List;

/**
 * View demonstrating multiple grids on a single page. Each grid is configured
 * with its own height constraints and feature set to illustrate how grids can
 * coexist within a complex view without competing for space.
 */
@Route(value = "multi-grids", layout = MainLayout.class)
public class MultipleGridsView extends VerticalLayout implements LocaleChangeObserver {

    @Autowired
    public MultipleGridsView(MockPersonService personService) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        initTables(personService);
        updatePageTitle();
    }

    private void initTables(MockPersonService personService) {
        // Define common columns and filters
        List<ColumnDefinition> columns = List.of(
                new ColumnDefinition("firstName", "person.firstName", String.class),
                new ColumnDefinition("lastName", "person.lastName", String.class),
                new ColumnDefinition("email", "person.email", String.class),
                new ColumnDefinition("age", "person.age", Integer.class)
        );
        List<FilterDefinition> filters = List.of(
                new FilterDefinition("firstName", "person.firstName", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("lastName", "person.lastName", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("email", "person.email", FilterDefinition.FilterType.TEXT, String.class),
                new FilterDefinition("age", "person.age", FilterDefinition.FilterType.NUMBER_RANGE, Integer.class)
        );
        // Grid 1: all features enabled, compact height
        FilterablePaginatedGrid<Person> grid1 = new FilterablePaginatedGrid<>(columns, filters, personService::fetchPage, 10);
        grid1.setExpandGrid(false);
        grid1.setMinHeight("250px");
        grid1.setMaxHeight("500px");
        // Grid 2: limited features (no export or views), different height
        EnumSet<GridFeature> features2 = EnumSet.of(GridFeature.COLUMN_CONFIG, GridFeature.ROW_SELECTION);
        FilterablePaginatedGrid<Person> grid2 = new FilterablePaginatedGrid<>(columns, filters, personService::fetchPage, 10, features2);
        grid2.setExpandGrid(false);
        grid2.setMinHeight("200px");
        grid2.setMaxHeight("400px");
        // Add both grids to the layout. Do not expand; let each control its own height
        add(grid1, grid2);
    }

    private void updatePageTitle() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("multipleGrids.title"));
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updatePageTitle();
    }
}