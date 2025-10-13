package com.youtopin.vaadin.i18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;

@Service
public class LocaleDirectionService {

    private static final String RTL_LANGUAGE = "fa";

    public void applyLocale(UI ui, Locale locale) {
        if (ui == null) {
            return;
        }
        Locale target = locale != null ? locale : Locale.ENGLISH;
        if (!Objects.equals(ui.getLocale(), target)) {
            ui.setLocale(target);
        } else {
            // Even if locale didn't change, ensure direction stays in sync.
            applyDirection(ui, target);
            return;
        }
        applyDirection(ui, target);
    }

    public void applyDirection(UI ui) {
        if (ui == null) {
            return;
        }
        applyDirection(ui, ui.getLocale());
    }

    private void applyDirection(UI ui, Locale locale) {
        Locale target = locale != null ? locale : Locale.ENGLISH;
        boolean rtl = RTL_LANGUAGE.equalsIgnoreCase(target.getLanguage());
        String lang = target.getLanguage();
        String dir = rtl ? "rtl" : "ltr";

        ui.getElement().setAttribute("lang", lang);
        ui.getElement().setAttribute("dir", dir);

        if (rtl) {
            ui.getElement().getStyle().set("--lumo-font-family", "'Vazir', sans-serif");
        } else {
            ui.getElement().getStyle().remove("--lumo-font-family");
        }

        Page page = ui.getPage();
        page.executeJs(
                "document.documentElement.setAttribute('dir',$0);" +
                        "document.body.setAttribute('dir',$0);" +
                        "document.documentElement.setAttribute('lang',$1);" +
                        "document.body.setAttribute('lang',$1);",
                dir,
                lang
        );
    }
}
