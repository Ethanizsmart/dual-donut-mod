package dev.dualdonut.runtime;

import java.text.Normalizer;
import java.util.Locale;

/** A response to /sell must also have a recognized sale title before claiming it. */
public final class SaleScreenRules {
    private SaleScreenRules() {}
    public static boolean matches(String title, String keywords) {
        if (title == null || keywords == null) return false;
        String normalized = normalize(title);
        for (String keyword : keywords.split("\\|")) {
            String token = normalize(keyword).strip();
            if (!token.isEmpty() && normalized.contains(token)) return true;
        }
        return false;
    }
    private static String normalize(String value) {
        return Normalizer.normalize(value.replaceAll("§.", ""), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
