package com.youtopin.vaadin.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

@Tag("app-icon")
@JsModule("./components/app-icon.js")
public class AppIcon extends Component {
    public AppIcon(String name) { getElement().setAttribute("name", name); }
    public AppIcon(String name, String sizePx) { this(name); getElement().setAttribute("size", sizePx); }
}
