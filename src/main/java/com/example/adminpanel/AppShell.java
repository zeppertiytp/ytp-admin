package com.example.adminpanel;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;

@CssImport(value = "./styles/hide-details-caret.css", themeFor = "vaadin-details-summary")
@Theme(value = "app")
@JsModule("./utils/digit-normalizer.js")
public class AppShell implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        // Keep it simple & compatible across Vaadin 24.x:
        // Append inline JS to <head> without using Position enum.
        final String js =
                "try {"
                        + "  const theme = localStorage.getItem('theme') || 'light';"
                        + "  document.documentElement.setAttribute('theme', theme);"
                        + "} catch(e) {}";
        settings.addInlineWithContents(js, Inline.Wrapping.JAVASCRIPT);
    }
}
