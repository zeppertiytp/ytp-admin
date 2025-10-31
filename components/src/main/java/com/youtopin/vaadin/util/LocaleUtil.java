package com.youtopin.vaadin.util;

import java.util.Locale;

public class LocaleUtil {
    public static boolean isRtl(Locale locale) {
        return locale != null && "fa".equalsIgnoreCase(locale.getLanguage());
    }
}
