package com.youtopin.vaadin.samples.ui.view.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;

/**
 * Showcase view for the Vaadin Accordion component. Demonstrates
 * everyday usage patterns including default behaviour, disabled
 * panels and themed variants that align with the design system.
 */
@Route(value = "components/accordion", layout = MainLayout.class)
public class AccordionSampleView extends AppPageLayout implements LocaleChangeObserver {

    private final H1 pageTitle;

    private final H2 basicSectionTitle;
    private final Span basicSectionDescription;
    private final Accordion basicAccordion;
    private final AccordionPanel basicIntroductionPanel;
    private final Paragraph basicIntroductionContent;
    private final AccordionPanel basicSetupPanel;
    private final Paragraph basicSetupContent;
    private final AccordionPanel basicRolloutPanel;
    private final Paragraph basicRolloutContent;

    private final H2 disabledSectionTitle;
    private final Span disabledSectionDescription;
    private final Accordion disabledAccordion;
    private final AccordionPanel disabledLivePanel;
    private final Paragraph disabledLiveContent;
    private final AccordionPanel disabledRoadmapPanel;
    private final Paragraph disabledRoadmapContent;

    private final H2 themedSectionTitle;
    private final Span themedSectionDescription;
    private final Accordion themedAccordion;
    private final AccordionPanel themedFilledPanel;
    private final Paragraph themedFilledContent;
    private final AccordionPanel themedReversePanel;
    private final Paragraph themedReverseContent;

    public AccordionSampleView() {
        pageTitle = createPageTitle(getTranslation("components.accordion.title"));

        basicSectionTitle = new H2();
        basicSectionDescription = new Span();
        basicSectionDescription.addClassName("page-subtitle");
        basicAccordion = new Accordion();
        basicAccordion.setWidthFull();
        basicIntroductionContent = new Paragraph();
        basicIntroductionPanel = basicAccordion.add("", basicIntroductionContent);
        basicSetupContent = new Paragraph();
        basicSetupPanel = basicAccordion.add("", basicSetupContent);
        basicRolloutContent = new Paragraph();
        basicRolloutPanel = basicAccordion.add("", basicRolloutContent);
        VerticalLayout basicCard = createCard(basicSectionTitle, basicSectionDescription, basicAccordion);
        basicCard.addClassName("stack-lg");
        add(basicCard);

        disabledSectionTitle = new H2();
        disabledSectionDescription = new Span();
        disabledSectionDescription.addClassName("page-subtitle");
        disabledAccordion = new Accordion();
        disabledAccordion.setWidthFull();
        disabledLiveContent = new Paragraph();
        disabledLivePanel = disabledAccordion.add("", disabledLiveContent);
        disabledRoadmapContent = new Paragraph();
        disabledRoadmapPanel = disabledAccordion.add("", disabledRoadmapContent);
        VerticalLayout disabledCard = createCard(disabledSectionTitle, disabledSectionDescription, disabledAccordion);
        disabledCard.addClassName("stack-lg");
        add(disabledCard);

        themedSectionTitle = new H2();
        themedSectionDescription = new Span();
        themedSectionDescription.addClassName("page-subtitle");
        themedAccordion = new Accordion();
        themedAccordion.setWidthFull();
        themedFilledContent = new Paragraph();
        themedFilledPanel = themedAccordion.add("", themedFilledContent);
        themedFilledPanel.addThemeVariants(DetailsVariant.FILLED);
        themedReverseContent = new Paragraph();
        themedReversePanel = themedAccordion.add("", themedReverseContent);
        themedReversePanel.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.SMALL);
        VerticalLayout themedCard = createCard(themedSectionTitle, themedSectionDescription, themedAccordion);
        themedCard.addClassName("stack-lg");
        add(themedCard);

        updateTexts();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        pageTitle.setText(getTranslation("components.accordion.title"));

        basicSectionTitle.setText(getTranslation("components.accordion.basic.title"));
        basicSectionDescription.setText(getTranslation("components.accordion.basic.description"));
        basicIntroductionPanel.setSummaryText(getTranslation("components.accordion.basic.panel1.summary"));
        basicIntroductionContent.setText(getTranslation("components.accordion.basic.panel1.content"));
        basicSetupPanel.setSummaryText(getTranslation("components.accordion.basic.panel2.summary"));
        basicSetupContent.setText(getTranslation("components.accordion.basic.panel2.content"));
        basicRolloutPanel.setSummaryText(getTranslation("components.accordion.basic.panel3.summary"));
        basicRolloutContent.setText(getTranslation("components.accordion.basic.panel3.content"));
        basicIntroductionPanel.setOpened(true);
        basicSetupPanel.setOpened(false);
        basicRolloutPanel.setOpened(false);

        disabledSectionTitle.setText(getTranslation("components.accordion.disabled.title"));
        disabledSectionDescription.setText(getTranslation("components.accordion.disabled.description"));
        disabledLivePanel.setSummaryText(getTranslation("components.accordion.disabled.panel1.summary"));
        disabledLiveContent.setText(getTranslation("components.accordion.disabled.panel1.content"));
        disabledLivePanel.setOpened(true);
        disabledRoadmapPanel.setSummaryText(getTranslation("components.accordion.disabled.panel2.summary"));
        disabledRoadmapContent.setText(getTranslation("components.accordion.disabled.panel2.content"));
        disabledRoadmapPanel.setEnabled(false);

        themedSectionTitle.setText(getTranslation("components.accordion.themed.title"));
        themedSectionDescription.setText(getTranslation("components.accordion.themed.description"));
        themedFilledPanel.setSummaryText(getTranslation("components.accordion.themed.panel1.summary"));
        themedFilledContent.setText(getTranslation("components.accordion.themed.panel1.content"));
        themedReversePanel.setSummaryText(getTranslation("components.accordion.themed.panel2.summary"));
        themedReverseContent.setText(getTranslation("components.accordion.themed.panel2.content"));

        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("components.accordion.title"));
        }
    }
}
