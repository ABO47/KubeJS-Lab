package com.abo47.kubejslab.client.ui.picker;

import java.util.Locale;


public final class LabSearchFilter {
    private LabSearchFilter() {
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeKey(String value) {
        return LabSearchNormalizer.normalizeQuery(value);
    }

    public static boolean matches(String query, String id, String displayName) {
        String rawQuery = normalize(query);
        if (rawQuery.isBlank()) {
            return true;
        }
        String compactQuery = normalizeKey(rawQuery);
        String rawId = normalize(id);
        String rawDisplay = normalize(displayName);
        return rawId.contains(rawQuery)
                || rawDisplay.contains(rawQuery)
                || (!compactQuery.isBlank()
                        && (normalizeKey(id).contains(compactQuery)
                                || normalizeKey(displayName).contains(compactQuery)));
    }
}
