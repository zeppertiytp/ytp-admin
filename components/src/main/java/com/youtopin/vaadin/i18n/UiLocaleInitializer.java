package com.youtopin.vaadin.i18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Ensures each newly created UI uses the default locale and direction metadata defined by the
 * {@link TranslationProvider}. Users can still switch language at runtime; the chosen locale is
 * stored in the session and restored on subsequent initializations.
 */
@Component
public class UiLocaleInitializer implements VaadinServiceInitListener {

    private final LocaleDirectionService localeDirectionService;
    private final TranslationProvider translationProvider;

    public UiLocaleInitializer(LocaleDirectionService localeDirectionService,
                               TranslationProvider translationProvider) {
        this.localeDirectionService = localeDirectionService;
        this.translationProvider = translationProvider;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInit -> {
            UI ui = uiInit.getUI();
            Locale preferred = (Locale) ui.getSession().getAttribute("preferred-locale");
            Locale locale = preferred != null ? preferred : translationProvider.getDefaultLocale();
            localeDirectionService.applyLocale(ui, locale);
        });
    }
}
