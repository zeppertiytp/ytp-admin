# AppCard

`AppCard` wraps the `vaadin-card` web component, exposing strongly typed helper
methods for common slot content and theme variants.

- **Source:** `components/src/main/java/com/youtopin/vaadin/component/AppCard.java`
- **Variants:** `components/src/main/java/com/youtopin/vaadin/component/AppCardVariant.java`

## Key capabilities
- Dedicated setters for media, subtitle, header, title, and footer slots.
- Theme variants (`ELEVATED`, `OUTLINED`, `HORIZONTAL`, `STRETCH_MEDIA`,
  `COVER_MEDIA`) applied through `AppCardVariant` enums.
- Slot-aware `remove` and `removeAll` implementations that clean up existing
  content safely.
- Maintains compatibility with the official Vaadin component so migration to the
  upstream Flow wrapper is straightforward.

## Usage
```java
AppCard card = new AppCard();
card.setTitle("Quarterly Results");
card.setHeader(new H3(getTranslation("card.analytics.header")));
card.setMedia(new AppIcon("bar-chart", "40"));
card.add(new Paragraph(getTranslation("card.analytics.description")));
card.addToFooter(new Button(getTranslation("card.analytics.view")));
card.addThemeVariants(AppCardVariant.ELEVATED, AppCardVariant.HORIZONTAL);
```

The wrapper stores slot components (media, subtitle, header, footer) internally
so subsequent calls replace the previous content instead of appending duplicates.

## Accessibility & localisation
- Cards inherit Vaadin's default semantics. Provide descriptive headings and
  accessible button labels to communicate context.
- When using media content, ensure the slotted component supplies suitable
  `aria-label` or `alt` text.

## Sample reference
- `samples/src/main/java/com/youtopin/vaadin/samples/ui/view/components/CardSampleView.java`
  showcases analytic summaries, content layouts, and action-heavy cards with
  various theme combinations.

