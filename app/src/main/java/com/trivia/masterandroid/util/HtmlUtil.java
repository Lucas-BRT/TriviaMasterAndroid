package com.trivia.masterandroid.util;

import android.text.Html;

public final class HtmlUtil {

    private HtmlUtil() {
    }

    public static String decode(String raw) {
        if (raw == null) {
            return "";
        }
        return Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY).toString().trim();
    }
}
