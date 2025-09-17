package com.example.adminpanel.view;

import com.example.adminpanel.component.ColumnDefinition;
import com.example.adminpanel.component.FilterDefinition;
import com.example.adminpanel.component.FilterablePaginatedGrid;
import com.example.adminpanel.component.FilterablePaginatedGrid.GridFeature;
import com.example.adminpanel.model.Person;
import com.example.adminpanel.service.MockPersonService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
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
public class MultipleGridsView extends ViewFrame implements LocaleChangeObserver {

    private H2 heading;

    @Autowired
    public MultipleGridsView(MockPersonService personService) {
        VerticalLayout content = createContentSection();
        content.addClassName("grid-view");

        heading = new H2(getTranslation("multipleGrids.title"));
        heading.addClassName("view-title");
        content.add(heading);

        VerticalLayout sections = buildGridSections(personService);
        content.add(sections);
        updatePageTitle();
    }

    private VerticalLayout buildGridSections(MockPersonService personService) {
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
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.addClassNames("view-section-stack");
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setWidthFull();

        wrapper.add(buildGridCard(grid1));
        wrapper.add(buildGridCard(grid2));
        return wrapper;
    }

    private VerticalLayout buildGridCard(FilterablePaginatedGrid<Person> grid) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("surface-card", "view-section");
        card.setPadding(false);
        card.setSpacing(false);
        card.setWidthFull();
        card.add(grid);
        return card;
    }

    private void updatePageTitle() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("multipleGrids.title"));
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        heading.setText(getTranslation("multipleGrids.title"));
        updatePageTitle();
    }
}