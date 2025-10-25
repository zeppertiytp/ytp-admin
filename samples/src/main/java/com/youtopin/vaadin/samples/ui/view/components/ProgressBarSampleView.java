package com.youtopin.vaadin.samples.ui.view.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;

/**
 * Samples for the Vaadin Progress Bar component, including determinate,
 * indeterminate and semantic variants.
 */
@Route(value = "components/progress-bar", layout = MainLayout.class)
public class ProgressBarSampleView extends AppPageLayout implements LocaleChangeObserver {

    private final H1 pageTitle;

    private final H2 determinateTitle;
    private final Span determinateDescription;
    private final ProgressBar determinateProgressBar;
    private final Button determinateAdvanceButton;
    private final Button determinateResetButton;
    private final Span determinateStatus;
    private double determinateValue = 0.3d;

    private final H2 indeterminateTitle;
    private final Span indeterminateDescription;
    private final ProgressBar indeterminateProgressBar;
    private final Paragraph indeterminateStatus;

    private final H2 themedTitle;
    private final Span themedDescription;
    private final ProgressBar successProgressBar;
    private final Span successLabel;
    private final ProgressBar errorProgressBar;
    private final Span errorLabel;

    public ProgressBarSampleView() {
        pageTitle = createPageTitle(getTranslation("components.progressBar.title"));

        determinateTitle = new H2();
        determinateDescription = new Span();
        determinateDescription.addClassName("page-subtitle");
        determinateProgressBar = new ProgressBar();
        determinateProgressBar.setMin(0);
        determinateProgressBar.setMax(1);
        determinateProgressBar.setValue(determinateValue);
        determinateProgressBar.setWidthFull();

        determinateAdvanceButton = new Button();
        determinateAdvanceButton.addClickListener(event -> {
            determinateValue = Math.min(1.0d, determinateValue + 0.2d);
            determinateProgressBar.setValue(determinateValue);
            updateDeterminateStatus();
        });

        determinateResetButton = new Button();
        determinateResetButton.addClickListener(event -> {
            determinateValue = 0d;
            determinateProgressBar.setValue(determinateValue);
            updateDeterminateStatus();
        });

        HorizontalLayout determinateActions = new HorizontalLayout(determinateAdvanceButton, determinateResetButton);
        determinateActions.setPadding(false);
        determinateActions.setSpacing(true);
        determinateActions.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        determinateStatus = new Span();

        VerticalLayout determinateCard = createCard(
                determinateTitle,
                determinateDescription,
                determinateProgressBar,
                determinateActions,
                determinateStatus);
        determinateCard.addClassName("stack-lg");
        add(determinateCard);

        indeterminateTitle = new H2();
        indeterminateDescription = new Span();
        indeterminateDescription.addClassName("page-subtitle");
        indeterminateProgressBar = new ProgressBar();
        indeterminateProgressBar.setIndeterminate(true);
        indeterminateProgressBar.setWidthFull();
        indeterminateStatus = new Paragraph();

        VerticalLayout indeterminateCard = createCard(
                indeterminateTitle,
                indeterminateDescription,
                indeterminateProgressBar,
                indeterminateStatus);
        indeterminateCard.addClassName("stack-lg");
        add(indeterminateCard);

        themedTitle = new H2();
        themedDescription = new Span();
        themedDescription.addClassName("page-subtitle");
        successProgressBar = new ProgressBar();
        successProgressBar.setValue(0.85d);
        successProgressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
        successProgressBar.setWidthFull();
        successLabel = new Span();

        errorProgressBar = new ProgressBar();
        errorProgressBar.setValue(0.35d);
        errorProgressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
        errorProgressBar.setWidthFull();
        errorLabel = new Span();

        VerticalLayout themedCard = createCard(
                themedTitle,
                themedDescription,
                successProgressBar,
                successLabel,
                errorProgressBar,
                errorLabel);
        themedCard.addClassName("stack-lg");
        add(themedCard);

        updateTexts();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        pageTitle.setText(getTranslation("components.progressBar.title"));

        determinateTitle.setText(getTranslation("components.progressBar.basic.title"));
        determinateDescription.setText(getTranslation("components.progressBar.basic.description"));
        determinateAdvanceButton.setText(getTranslation("components.progressBar.basic.advance"));
        determinateResetButton.setText(getTranslation("components.progressBar.basic.reset"));
        updateDeterminateStatus();

        indeterminateTitle.setText(getTranslation("components.progressBar.indeterminate.title"));
        indeterminateDescription.setText(getTranslation("components.progressBar.indeterminate.description"));
        indeterminateStatus.setText(getTranslation("components.progressBar.indeterminate.status"));

        themedTitle.setText(getTranslation("components.progressBar.themed.title"));
        themedDescription.setText(getTranslation("components.progressBar.themed.description"));
        successLabel.setText(getTranslation("components.progressBar.themed.success"));
        errorLabel.setText(getTranslation("components.progressBar.themed.error"));

        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("components.progressBar.title"));
        }
    }

    private void updateDeterminateStatus() {
        long percentage = Math.round(determinateValue * 100);
        if (determinateValue >= 1.0d) {
            determinateStatus.setText(getTranslation("components.progressBar.basic.completed"));
        } else {
            determinateStatus.setText(getTranslation("components.progressBar.basic.status", (int) percentage));
        }
        determinateAdvanceButton.setEnabled(determinateValue < 1.0d);
        determinateResetButton.setEnabled(determinateValue > 0);
    }
}
