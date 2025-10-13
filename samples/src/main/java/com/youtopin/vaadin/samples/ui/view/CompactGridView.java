package com.youtopin.vaadin.samples.ui.view;

import com.youtopin.vaadin.samples.application.person.PersonDirectory;
import com.youtopin.vaadin.samples.domain.person.Person;
import com.youtopin.vaadin.component.ColumnDefinition;
import com.youtopin.vaadin.component.FilterDefinition;
import com.youtopin.vaadin.component.FilterablePaginatedGrid;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * View demonstrating a compact grid. The grid does not expand to fill the page
 * and instead uses the configured minimum and maximum heights. When there are
 * few rows, the grid will shrink but maintain a small presence; when there
 * are many rows, it will scroll within the specified maximum height.
 */
@Route(value = "compact-grid", layout = MainLayout.class)
public class CompactGridView extends AppPageLayout implements LocaleChangeObserver {

    @Autowired
    public CompactGridView(PersonDirectory personDirectory) {
        this.pageTitle = createPageTitle(getTranslation("compactGrid.title"));
        initTable(personDirectory);
        updatePageTitle();
    }

    private final H1 pageTitle;

    private void initTable(PersonDirectory personDirectory) {
        // Define columns
        List<ColumnDefinition> columns = List.of(
                new ColumnDefinition("firstName", "person.firstName", String.class),
                new ColumnDefinition("lastName", "person.lastName", String.class),
                new ColumnDefinition("email", "person.email", String.class),
                new ColumnDefinition("age", "person.age", Integer.class)
        );
        // Define filters similar to FullGridView
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
        // Create grid with default features
        FilterablePaginatedGrid<Person> grid = new FilterablePaginatedGrid<>(columns, filters, personDirectory, 10);
        // Configure height: do not expand; set min and max heights
        grid.setExpandGrid(false);
        grid.setMinHeight("200px");
        grid.setMaxHeight("400px");
        grid.setWidthFull();

        VerticalLayout gridCard = createCard(grid);
        gridCard.addClassName("stack-lg");
        add(gridCard);
    }

    private void updatePageTitle() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("compactGrid.title"));
        }
        pageTitle.setText(getTranslation("compactGrid.title"));
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updatePageTitle();
    }
}
