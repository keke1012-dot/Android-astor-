package fr.astor.hades;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Only the explicitly entered query may leave the device. */
public final class InternetPolicy {
    private InternetPolicy() {}
    public static String query(String raw) {
        if (raw == null) throw new IllegalArgumentException("Requête vide");
        String value = raw.trim();
        if (value.isEmpty() || value.length() > 300 || value.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Requête invalide (300 caractères maximum)");
        return value;
    }
    public static String encoded(String raw) {
        return URLEncoder.encode(query(raw), StandardCharsets.UTF_8);
    }
    public static String endpoint(String raw) {
        return "https://fr.wikipedia.org/w/api.php?action=query&format=json&formatversion=2"
            + "&generator=search&gsrnamespace=0&gsrlimit=3&prop=extracts&exintro=1&explaintext=1"
            + "&exchars=1800&gsrsearch=" + encoded(raw);
    }
}
