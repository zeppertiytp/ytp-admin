package com.youtopin.vaadin.samples.ui.view.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Samples for the Vaadin {@link ConfirmDialog} component highlighting
 * everyday confirmation flows with translations and helper copy that
 * match the application design system.
 */
@Route(value = "components/confirm-dialog", layout = MainLayout.class)
public class ConfirmDialogSampleView extends AppPageLayout implements LocaleChangeObserver {

    private enum AsyncOperationState {
        IDLE,
        RUNNING,
        CANCELLED,
        COMPLETED
    }

    private final H1 pageTitle;

    private final H2 basicSectionTitle;
    private final Span basicSectionDescription;
    private final Button basicOpenButton;
    private final ConfirmDialog basicDialog;

    private final H2 customSectionTitle;
    private final Span customSectionDescription;
    private final Button customOpenButton;
    private final ConfirmDialog customDialog;
    private final H3 customHeaderTitle;
    private final Span customHeaderSubtitle;
    private final Paragraph customPrimaryMessage;
    private final Paragraph customSecondaryMessage;

    private final H2 asyncSectionTitle;
    private final Span asyncSectionDescription;
    private final Span asyncStatusLabel;
    private final Button asyncOpenButton;
    private final ConfirmDialog asyncDialog;

    private AsyncOperationState asyncOperationState = AsyncOperationState.IDLE;

    public ConfirmDialogSampleView() {
        pageTitle = createPageTitle(getTranslation("components.confirmDialog.title"));

        basicSectionTitle = new H2();
        basicSectionDescription = createSubtitle();
        basicOpenButton = createPrimaryButton();
        basicDialog = createBasicDialog();
        basicOpenButton.addClickListener(event -> {
            basicDialog.open();
        });
        VerticalLayout basicCard = createCard(basicSectionTitle, basicSectionDescription, basicOpenButton);
        basicCard.addClassName("stack-lg");
        add(basicCard);

        customSectionTitle = new H2();
        customSectionDescription = createSubtitle();
        customOpenButton = createPrimaryButton();
        customHeaderTitle = new H3();
        customHeaderTitle.getStyle().set("margin", "0");
        customHeaderSubtitle = new Span();
        customHeaderSubtitle.getStyle().set("color", "var(--text-secondary)");
        customHeaderSubtitle.getStyle().set("font-size", "var(--lumo-font-size-s)");
        customPrimaryMessage = new Paragraph();
        customSecondaryMessage = new Paragraph();
        customSecondaryMessage.getStyle().set("color", "var(--text-secondary)");
        customDialog = createCustomDialog();
        customOpenButton.addClickListener(event -> customDialog.open());
        VerticalLayout customCard = createCard(customSectionTitle, customSectionDescription, customOpenButton);
        customCard.addClassName("stack-lg");
        add(customCard);

        asyncSectionTitle = new H2();
        asyncSectionDescription = createSubtitle();
        asyncStatusLabel = new Span();
        asyncStatusLabel.getStyle().set("color", "var(--text-secondary)");
        asyncStatusLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");
        asyncOpenButton = createPrimaryButton();
        asyncDialog = createAsyncDialog();
        asyncOpenButton.addClickListener(event -> asyncDialog.open());
        VerticalLayout asyncContent = new VerticalLayout(asyncOpenButton, asyncStatusLabel);
        asyncContent.setPadding(false);
        asyncContent.setSpacing(false);
        asyncContent.addClassName("stack-sm");
        VerticalLayout asyncCard = createCard(asyncSectionTitle, asyncSectionDescription, asyncContent);
        asyncCard.addClassName("stack-lg");
        add(asyncCard);

        updateTexts();
    }

    private Span createSubtitle() {
        Span subtitle = new Span();
        subtitle.addClassName("page-subtitle");
        return subtitle;
    }

    private Button createPrimaryButton() {
        Button button = new Button();
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.setWidthFull();
        return button;
    }

    private ConfirmDialog createBasicDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setCancelable(true);
        dialog.setConfirmButtonTheme(ButtonVariant.LUMO_PRIMARY.getVariantName());
        dialog.setCancelButtonTheme(ButtonVariant.LUMO_TERTIARY.getVariantName());
        dialog.addConfirmListener(event -> Notification
                .show(getTranslation("components.confirmDialog.basic.notification.confirm"), 3000,
                        Position.BOTTOM_START));
        dialog.addCancelListener(event -> Notification
                .show(getTranslation("components.confirmDialog.basic.notification.cancel"), 3000,
                        Position.BOTTOM_START));
        return dialog;
    }

    private ConfirmDialog createCustomDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setCancelable(true);
        dialog.setConfirmButtonTheme(ButtonVariant.LUMO_SUCCESS.getVariantName());
        dialog.setCancelButtonTheme(ButtonVariant.LUMO_TERTIARY_INLINE.getVariantName());

        Div headerWrapper = new Div(customHeaderTitle, customHeaderSubtitle);
        headerWrapper.addClassName("stack-xs");
        dialog.setHeader(headerWrapper);

        VerticalLayout body = new VerticalLayout(customPrimaryMessage, customSecondaryMessage);
        body.setPadding(false);
        body.setSpacing(false);
        body.addClassName("stack-sm");
        dialog.setText(body);

        dialog.addConfirmListener(event -> Notification
                .show(getTranslation("components.confirmDialog.custom.notification.confirm"), 3000,
                        Position.BOTTOM_CENTER));
        dialog.addCancelListener(event -> Notification
                .show(getTranslation("components.confirmDialog.custom.notification.cancel"), 3000,
                        Position.BOTTOM_START));
        return dialog;
    }

    private ConfirmDialog createAsyncDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setCancelable(true);
        dialog.setConfirmButtonTheme(ButtonVariant.LUMO_PRIMARY.getVariantName());
        dialog.setCancelButtonTheme(ButtonVariant.LUMO_TERTIARY.getVariantName());

        dialog.addConfirmListener(event -> {
            asyncOperationState = AsyncOperationState.RUNNING;
            updateAsyncStatusText();
            event.getSource().close();
            UI ui = UI.getCurrent();
            if (ui != null) {
                CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute(() -> ui.access(() -> {
                    if (!getElement().getNode().isAttached()) {
                        return;
                    }
                    asyncOperationState = AsyncOperationState.COMPLETED;
                    updateAsyncStatusText();
                    Notification
                            .show(getTranslation("components.confirmDialog.async.notification.success"), 4000,
                                    Position.BOTTOM_CENTER);
                }));
            } else {
                asyncOperationState = AsyncOperationState.COMPLETED;
                updateAsyncStatusText();
            }
        });

        dialog.addCancelListener(event -> {
            asyncOperationState = AsyncOperationState.CANCELLED;
            updateAsyncStatusText();
            Notification
                    .show(getTranslation("components.confirmDialog.async.notification.cancel"), 3000,
                            Position.BOTTOM_START);
        });
        return dialog;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        pageTitle.setText(getTranslation("components.confirmDialog.title"));

        basicSectionTitle.setText(getTranslation("components.confirmDialog.basic.title"));
        basicSectionDescription.setText(getTranslation("components.confirmDialog.basic.description"));
        basicOpenButton.setText(getTranslation("components.confirmDialog.basic.openAction"));
        basicDialog.setHeader(getTranslation("components.confirmDialog.basic.dialog.title"));
        basicDialog.setText(getTranslation("components.confirmDialog.basic.dialog.message"));
        basicDialog.setConfirmText(getTranslation("components.confirmDialog.basic.dialog.confirmButton"));
        basicDialog.setCancelText(getTranslation("components.confirmDialog.basic.dialog.cancelButton"));

        customSectionTitle.setText(getTranslation("components.confirmDialog.custom.title"));
        customSectionDescription.setText(getTranslation("components.confirmDialog.custom.description"));
        customOpenButton.setText(getTranslation("components.confirmDialog.custom.openAction"));
        customHeaderTitle.setText(getTranslation("components.confirmDialog.custom.dialog.headerTitle"));
        customHeaderSubtitle.setText(getTranslation("components.confirmDialog.custom.dialog.headerSubtitle"));
        customPrimaryMessage.setText(getTranslation("components.confirmDialog.custom.dialog.primaryMessage"));
        customSecondaryMessage.setText(getTranslation("components.confirmDialog.custom.dialog.secondaryMessage"));
        customDialog.setConfirmText(getTranslation("components.confirmDialog.custom.dialog.confirmButton"));
        customDialog.setCancelText(getTranslation("components.confirmDialog.custom.dialog.cancelButton"));

        asyncSectionTitle.setText(getTranslation("components.confirmDialog.async.title"));
        asyncSectionDescription.setText(getTranslation("components.confirmDialog.async.description"));
        asyncOpenButton.setText(getTranslation("components.confirmDialog.async.openAction"));
        asyncDialog.setHeader(getTranslation("components.confirmDialog.async.dialog.title"));
        asyncDialog.setText(getTranslation("components.confirmDialog.async.dialog.message"));
        asyncDialog.setConfirmText(getTranslation("components.confirmDialog.async.dialog.confirmButton"));
        asyncDialog.setCancelText(getTranslation("components.confirmDialog.async.dialog.cancelButton"));
        updateAsyncStatusText();

        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("components.confirmDialog.title"));
        }
    }

    private void updateAsyncStatusText() {
        String statusKey;
        switch (asyncOperationState) {
            case RUNNING -> statusKey = "components.confirmDialog.async.status.running";
            case CANCELLED -> statusKey = "components.confirmDialog.async.status.cancelled";
            case COMPLETED -> statusKey = "components.confirmDialog.async.status.completed";
            case IDLE -> statusKey = "components.confirmDialog.async.status.idle";
            default -> statusKey = "components.confirmDialog.async.status.idle";
        }
        asyncStatusLabel.setText(getTranslation(statusKey));
    }
}
