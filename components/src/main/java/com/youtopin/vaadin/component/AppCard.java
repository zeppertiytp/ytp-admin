package com.youtopin.vaadin.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Lightweight server-side wrapper around {@code vaadin-card}. It exposes only
 * the features required by the sample gallery while keeping the API close to
 * the official Vaadin Card component so that it can be swapped out later when
 * the project upgrades to the bundled version.
 */
@Tag("vaadin-card")
@NpmPackage(value = "@vaadin/card", version = "24.7.14")
@JsModule("@vaadin/card/src/vaadin-card.js")
public class AppCard extends Component implements HasComponents, HasSize {

    private static final String SLOT_MEDIA = "media";
    private static final String SLOT_SUBTITLE = "subtitle";
    private static final String SLOT_HEADER = "header";
    private static final String SLOT_FOOTER = "footer";
    private static final String CARD_TITLE_PROPERTY = "cardTitle";

    private final Element contentRoot;
    private Component mediaComponent;
    private Component subtitleComponent;
    private Component headerComponent;
    private final List<Component> footerComponents = new ArrayList<>();

    public AppCard() {
        contentRoot = new Element("div");
        contentRoot.getStyle().set("display", "contents");
        getElement().appendChild(contentRoot);
    }

    public void setMedia(Component media) {
        mediaComponent = replaceSlotComponent(mediaComponent, media, SLOT_MEDIA);
    }

    public void setSubtitle(Component subtitle) {
        subtitleComponent = replaceSlotComponent(subtitleComponent, subtitle, SLOT_SUBTITLE);
    }

    public void setHeader(Component header) {
        headerComponent = replaceSlotComponent(headerComponent, header, SLOT_HEADER);
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            getElement().removeProperty(CARD_TITLE_PROPERTY);
        } else {
            getElement().setProperty(CARD_TITLE_PROPERTY, title);
        }
    }

    public void addToFooter(Component... components) {
        for (Component component : components) {
            Objects.requireNonNull(component, "Footer component must not be null");
            component.getElement().setAttribute("slot", SLOT_FOOTER);
            getElement().appendChild(component.getElement());
            footerComponents.add(component);
        }
    }

    public void addThemeVariants(AppCardVariant... variants) {
        for (AppCardVariant variant : variants) {
            if (variant != null) {
                getElement().getThemeList().add(variant.getVariantName());
            }
        }
    }

    public void removeThemeVariants(AppCardVariant... variants) {
        for (AppCardVariant variant : variants) {
            if (variant != null) {
                getElement().getThemeList().remove(variant.getVariantName());
            }
        }
    }

    @Override
    public void add(Component... components) {
        for (Component component : components) {
            Objects.requireNonNull(component, "Component to add must not be null");
            contentRoot.appendChild(component.getElement());
        }
    }

    @Override
    public void remove(Component... components) {
        for (Component component : components) {
            if (component == null) {
                continue;
            }
            Element parent = component.getElement().getParent();
            if (parent == null) {
                continue;
            }
            if (parent.equals(contentRoot)) {
                contentRoot.removeChild(component.getElement());
            } else if (parent.equals(getElement())) {
                getElement().removeChild(component.getElement());
                if (component.equals(mediaComponent)) {
                    mediaComponent = null;
                } else if (component.equals(subtitleComponent)) {
                    subtitleComponent = null;
                } else if (component.equals(headerComponent)) {
                    headerComponent = null;
                } else {
                    footerComponents.remove(component);
                }
            }
        }
    }

    @Override
    public void removeAll() {
        contentRoot.removeAllChildren();
        if (mediaComponent != null) {
            remove(mediaComponent);
            mediaComponent = null;
        }
        if (subtitleComponent != null) {
            remove(subtitleComponent);
            subtitleComponent = null;
        }
        if (headerComponent != null) {
            remove(headerComponent);
            headerComponent = null;
        }
        for (Component footer : new ArrayList<>(footerComponents)) {
            remove(footer);
        }
        footerComponents.clear();
        getElement().removeProperty(CARD_TITLE_PROPERTY);
    }

    @Override
    public Stream<Component> getChildren() {
        Stream<Component> contentChildren = contentRoot.getChildren()
                .map(element -> element.getComponent().orElse(null))
                .filter(Objects::nonNull);
        Stream<Component> slotted = Stream.of(mediaComponent, subtitleComponent, headerComponent)
                .filter(Objects::nonNull);
        Stream<Component> footers = footerComponents.stream();
        return Stream.concat(contentChildren, Stream.concat(slotted, footers));
    }

    private Component replaceSlotComponent(Component current, Component replacement, String slotName) {
        if (current != null) {
            current.getElement().removeAttribute("slot");
            if (current.getElement().getParent() == getElement()) {
                getElement().removeChild(current.getElement());
            }
        }
        if (replacement != null) {
            replacement.getElement().setAttribute("slot", slotName);
            getElement().appendChild(replacement.getElement());
        }
        return replacement;
    }
}
