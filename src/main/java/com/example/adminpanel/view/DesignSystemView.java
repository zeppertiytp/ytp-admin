package com.example.adminpanel.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Design System")
@Route(value = "design-system", layout = MainLayout.class)
public class DesignSystemView extends VerticalLayout {

    public DesignSystemView() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        add(new H2("پیش‌نمایش رنگ‌ها / Design System"));

        // Buttons (responsive grid)
        Div buttons = responsiveGrid();
        buttons.add(btn("Primary", "primary"),
                    btn("Secondary", "secondary"),
                    btn("Info", "info"),
                    btn("Success", "success"),
                    btn("Warning", "warning"),
                    btn("Danger", "danger"));
        add(card("دکمه‌ها / Buttons", buttons));

        // Badges (responsive grid)
        Div badges = responsiveGrid();
        badges.add(badge("primary", "Primary"),
                   badge("secondary", "Secondary"),
                   badge("info", "Info"),
                   badge("success", "Success"),
                   badge("warning", "Warning"),
                   badge("danger", "Danger"));
        add(card("نشان‌ها / Badges", badges));
    }

    private Div responsiveGrid() {
        Div grid = new Div();
        grid.getStyle().set("display", "grid");
        grid.getStyle().set("grid-template-columns", "repeat(auto-fit, minmax(140px, 1fr))");
        grid.getStyle().set("gap", "var(--space-3)");
        grid.setWidthFull();
        return grid;
    }

    private Button btn(String text, String themeName) {
        Button b = new Button(text);
        b.getElement().setAttribute("theme", themeName);
        b.setWidthFull();
        return b;
    }

    private Span badge(String theme, String text) {
        Span s = new Span(text);
        s.getElement().setAttribute("theme", "badge");
        s.getElement().getClassList().add(theme);
        return s;
    }

    private VerticalLayout card(String title, com.vaadin.flow.component.Component content) {
        VerticalLayout card = new VerticalLayout();
        card.add(new H2(title));
        card.add(content);
        card.setPadding(true);
        card.setSpacing(true);
        card.setWidthFull();
        card.getElement().getClassList().add("app-card");
        return card;
    }
}
