package com.youtopin.vaadin.samples.ui.view.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.youtopin.vaadin.component.AppCard;
import com.youtopin.vaadin.component.AppCardVariant;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;

/**
 * Sample view that demonstrates different ways of composing the Vaadin Card
 * component. The examples highlight responsive grids, media usage and dedicated
 * header/footer slots so teams can copy/paste proven patterns.
 */
@Route(value = "components/card", layout = MainLayout.class)
public class CardSampleView extends AppPageLayout implements LocaleChangeObserver {

    private final H1 pageTitle;

    private final H2 basicSectionTitle;
    private final Span basicSectionDescription;
    private final AppCard basicAnalyticsCard;
    private final Paragraph basicAnalyticsBody;
    private final Span basicAnalyticsSubtitle;
    private final AppCard basicTeamCard;
    private final Paragraph basicTeamBody;
    private final Span basicTeamSubtitle;
    private final AppCard basicFilesCard;
    private final Paragraph basicFilesBody;
    private final Span basicFilesSubtitle;

    private final H2 layoutsSectionTitle;
    private final Span layoutsSectionDescription;
    private final AppCard layoutsStoryCard;
    private final Paragraph layoutsStoryBody;
    private final Button layoutsStoryAction;
    private final AppCard layoutsMediaCard;
    private final Paragraph layoutsMediaBody;
    private final AppCard layoutsChecklistCard;
    private final Paragraph layoutsChecklistBody;

    private final H2 actionsSectionTitle;
    private final Span actionsSectionDescription;
    private final AppCard actionsProjectCard;
    private final H3 actionsProjectTitle;
    private final Span actionsProjectSubtitle;
    private final Paragraph actionsProjectBody;
    private final Span actionsProjectStatusBadge;
    private final Button actionsProjectPrimaryAction;
    private final Button actionsProjectSecondaryAction;
    private final AppCard actionsBacklogCard;
    private final Paragraph actionsBacklogBody;
    private final Button actionsBacklogAction;

    public CardSampleView() {
        pageTitle = createPageTitle(getTranslation("components.card.title"));

        basicSectionTitle = new H2();
        basicSectionDescription = subtitle();
        Div basicGrid = responsiveGrid();
        basicAnalyticsCard = metricCard(VaadinIcon.TRENDING_UP.create());
        basicAnalyticsSubtitle = metricSubtitle();
        basicAnalyticsBody = cardParagraph();
        basicAnalyticsCard.setSubtitle(basicAnalyticsSubtitle);
        basicAnalyticsCard.add(basicAnalyticsBody);

        basicTeamCard = metricCard(VaadinIcon.USERS.create());
        basicTeamSubtitle = metricSubtitle();
        basicTeamBody = cardParagraph();
        basicTeamCard.setSubtitle(basicTeamSubtitle);
        basicTeamCard.add(basicTeamBody);

        basicFilesCard = metricCard(VaadinIcon.FOLDER_OPEN.create());
        basicFilesSubtitle = metricSubtitle();
        basicFilesBody = cardParagraph();
        basicFilesCard.setSubtitle(basicFilesSubtitle);
        basicFilesCard.add(basicFilesBody);

        basicGrid.add(basicAnalyticsCard, basicTeamCard, basicFilesCard);
        VerticalLayout basicCardLayout = createCard(basicSectionTitle, basicSectionDescription, basicGrid);
        basicCardLayout.addClassName("stack-lg");
        add(basicCardLayout);

        layoutsSectionTitle = new H2();
        layoutsSectionDescription = subtitle();
        Div layoutsGrid = responsiveGrid();

        layoutsStoryCard = sampleCard();
        layoutsStoryCard.addThemeVariants(AppCardVariant.ELEVATED, AppCardVariant.HORIZONTAL);
        layoutsStoryCard.setMedia(mediaIcon(VaadinIcon.NEWSPAPER));
        layoutsStoryBody = cardParagraph();
        layoutsStoryAction = new Button();
        layoutsStoryAction.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        layoutsStoryCard.add(layoutsStoryBody, layoutsStoryAction);

        layoutsMediaCard = sampleCard();
        layoutsMediaCard.addThemeVariants(AppCardVariant.STRETCH_MEDIA);
        layoutsMediaCard.setMedia(mediaIcon(VaadinIcon.CAMERA));
        layoutsMediaBody = cardParagraph();
        layoutsMediaCard.add(layoutsMediaBody);

        layoutsChecklistCard = sampleCard();
        layoutsChecklistCard.addThemeVariants(AppCardVariant.OUTLINED);
        layoutsChecklistBody = cardParagraph();
        layoutsChecklistCard.add(layoutsChecklistBody);

        layoutsGrid.add(layoutsStoryCard, layoutsMediaCard, layoutsChecklistCard);
        VerticalLayout layoutsCardLayout = createCard(layoutsSectionTitle, layoutsSectionDescription, layoutsGrid);
        layoutsCardLayout.addClassName("stack-lg");
        add(layoutsCardLayout);

        actionsSectionTitle = new H2();
        actionsSectionDescription = subtitle();
        Div actionsGrid = responsiveGrid();

        actionsProjectCard = sampleCard();
        actionsProjectCard.addThemeVariants(AppCardVariant.ELEVATED);
        actionsProjectTitle = new H3();
        actionsProjectTitle.getStyle().set("margin", "0");
        actionsProjectSubtitle = new Span();
        actionsProjectSubtitle.addClassName("text-secondary");
        VerticalLayout projectHeader = new VerticalLayout(actionsProjectTitle, actionsProjectSubtitle);
        projectHeader.setSpacing(false);
        projectHeader.setPadding(false);
        projectHeader.setDefaultHorizontalComponentAlignment(Alignment.START);
        projectHeader.addClassName("stack-xs");
        actionsProjectStatusBadge = new Span();
        actionsProjectStatusBadge.getElement().setAttribute("theme", "badge success");
        HorizontalLayout projectHeaderLayout = new HorizontalLayout(projectHeader, actionsProjectStatusBadge);
        projectHeaderLayout.setWidthFull();
        projectHeaderLayout.setAlignItems(Alignment.CENTER);
        projectHeaderLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        projectHeaderLayout.addClassName("stack-xs");
        actionsProjectCard.setHeader(projectHeaderLayout);
        actionsProjectBody = cardParagraph();
        actionsProjectCard.add(actionsProjectBody);
        actionsProjectPrimaryAction = new Button();
        actionsProjectPrimaryAction.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionsProjectSecondaryAction = new Button();
        actionsProjectSecondaryAction.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout projectFooter = footer(actionsProjectPrimaryAction, actionsProjectSecondaryAction);
        actionsProjectCard.addToFooter(projectFooter);

        actionsBacklogCard = sampleCard();
        actionsBacklogCard.addThemeVariants(AppCardVariant.OUTLINED);
        actionsBacklogBody = cardParagraph();
        actionsBacklogCard.add(actionsBacklogBody);
        actionsBacklogAction = new Button();
        actionsBacklogAction.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        HorizontalLayout backlogFooter = footer(actionsBacklogAction);
        actionsBacklogCard.addToFooter(backlogFooter);

        actionsGrid.add(actionsProjectCard, actionsBacklogCard);
        VerticalLayout actionsCardLayout = createCard(actionsSectionTitle, actionsSectionDescription, actionsGrid);
        actionsCardLayout.addClassName("stack-lg");
        add(actionsCardLayout);

        updateTexts();
    }

    private Span subtitle() {
        Span subtitle = new Span();
        subtitle.addClassName("page-subtitle");
        return subtitle;
    }

    private Div responsiveGrid() {
        Div grid = new Div();
        grid.getStyle().set("display", "grid");
        grid.getStyle().set("grid-template-columns", "repeat(auto-fit, minmax(220px, 1fr))");
        grid.getStyle().set("gap", "var(--space-3)");
        grid.setWidthFull();
        return grid;
    }

    private AppCard sampleCard() {
        AppCard card = new AppCard();
        card.setWidthFull();
        card.addClassNames("app-card", "stack-md");
        return card;
    }

    private AppCard metricCard(Icon icon) {
        AppCard card = sampleCard();
        card.addThemeVariants(AppCardVariant.ELEVATED);
        icon.addClassName("card-icon");
        icon.getStyle().set("color", "var(--color-primary-500)");
        icon.getStyle().set("font-size", "32px");
        card.setMedia(icon);
        return card;
    }

    private Span metricSubtitle() {
        Span subtitle = new Span();
        subtitle.addClassName("text-secondary");
        subtitle.getStyle().set("font-size", "var(--lumo-font-size-s)");
        return subtitle;
    }

    private Paragraph cardParagraph() {
        Paragraph paragraph = new Paragraph();
        paragraph.getStyle().set("margin", "0");
        paragraph.getStyle().set("font-size", "var(--lumo-font-size-m)");
        paragraph.getStyle().set("color", "var(--text-secondary)");
        return paragraph;
    }

    private Component mediaIcon(VaadinIcon icon) {
        Icon media = icon.create();
        media.getStyle().set("font-size", "42px");
        media.getStyle().set("color", "var(--color-info-500)");
        media.getStyle().set("align-self", "center");
        return media;
    }

    private HorizontalLayout footer(Button... buttons) {
        HorizontalLayout footer = new HorizontalLayout(buttons);
        footer.setWidthFull();
        footer.setSpacing(true);
        footer.setPadding(false);
        footer.setAlignItems(Alignment.CENTER);
        footer.getStyle().set("justify-content", "flex-end");
        footer.addClassName("stack-sm");
        return footer;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        pageTitle.setText(getTranslation("components.card.title"));

        basicSectionTitle.setText(getTranslation("components.card.basic.title"));
        basicSectionDescription.setText(getTranslation("components.card.basic.description"));
        basicAnalyticsCard.setTitle(getTranslation("components.card.basic.analytics.title"));
        basicAnalyticsSubtitle.setText(getTranslation("components.card.basic.analytics.subtitle"));
        basicAnalyticsBody.setText(getTranslation("components.card.basic.analytics.body"));
        basicTeamCard.setTitle(getTranslation("components.card.basic.team.title"));
        basicTeamSubtitle.setText(getTranslation("components.card.basic.team.subtitle"));
        basicTeamBody.setText(getTranslation("components.card.basic.team.body"));
        basicFilesCard.setTitle(getTranslation("components.card.basic.files.title"));
        basicFilesSubtitle.setText(getTranslation("components.card.basic.files.subtitle"));
        basicFilesBody.setText(getTranslation("components.card.basic.files.body"));

        layoutsSectionTitle.setText(getTranslation("components.card.layouts.title"));
        layoutsSectionDescription.setText(getTranslation("components.card.layouts.description"));
        layoutsStoryCard.setTitle(getTranslation("components.card.layouts.story.title"));
        layoutsStoryBody.setText(getTranslation("components.card.layouts.story.body"));
        layoutsStoryAction.setText(getTranslation("components.card.layouts.story.action"));
        layoutsMediaCard.setTitle(getTranslation("components.card.layouts.media.title"));
        layoutsMediaBody.setText(getTranslation("components.card.layouts.media.body"));
        layoutsChecklistCard.setTitle(getTranslation("components.card.layouts.checklist.title"));
        layoutsChecklistBody.setText(getTranslation("components.card.layouts.checklist.body"));

        actionsSectionTitle.setText(getTranslation("components.card.actions.title"));
        actionsSectionDescription.setText(getTranslation("components.card.actions.description"));
        actionsProjectTitle.setText(getTranslation("components.card.actions.project.title"));
        actionsProjectSubtitle.setText(getTranslation("components.card.actions.project.subtitle"));
        actionsProjectBody.setText(getTranslation("components.card.actions.project.body"));
        actionsProjectStatusBadge.setText(getTranslation("components.card.actions.project.status"));
        actionsProjectPrimaryAction.setText(getTranslation("components.card.actions.project.primary"));
        actionsProjectSecondaryAction.setText(getTranslation("components.card.actions.project.secondary"));
        actionsBacklogCard.setTitle(getTranslation("components.card.actions.backlog.title"));
        actionsBacklogBody.setText(getTranslation("components.card.actions.backlog.body"));
        actionsBacklogAction.setText(getTranslation("components.card.actions.backlog.action"));

        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("components.card.title"));
        }
    }
}
