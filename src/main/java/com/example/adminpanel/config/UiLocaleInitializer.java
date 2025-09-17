package com.example.adminpanel.config;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UiLocaleInitializer implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        // Use VaadinService (event.getSource()) to register a UI init listener
        event.getSource().addUIInitListener(uiInit -> {
            UI ui = uiInit.getUI();
            Locale pref = (Locale) ui.getSession().getAttribute("preferred-locale");
            Locale locale = pref != null ? pref : new Locale("fa");
            ui.setLocale(locale);
            String lang = locale.getLanguage();
            ui.getElement().setAttribute("lang", lang);
            ui.getElement().setAttribute("dir", "fa".equals(lang) ? "rtl" : "ltr");
        });
    }
}
