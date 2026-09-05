package com.abo47.kubejslab.client.ui.picker;

import java.util.Locale;


public final class SearchNormalizer {
    private SearchNormalizer() {
    }

    public static String normalizeUserSearch(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("_+$", "");
    }

    public static String normalizeQuery(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
