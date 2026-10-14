package fr.astor.hades;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

/** Small, offline policy shared by the native N2 interface and its tests. */
final class PhoneAccessPolicy {
    static final Set<String> FEATURES = Set.of("files", "apps", "calendar", "notifications");
    static boolean allowed(boolean foreground, boolean authenticated, boolean enabled, long request, long current) {
        return foreground && authenticated && enabled && request == current;
    }
    static String text(String raw, int limit) {
        if (raw == null) return "";
        String value = raw.replaceAll("[\\p{Cntrl}&&[^\\n]]", " ").trim();
        return value.substring(0, Math.min(limit, value.length()));
    }
    static String notificationText(String raw) {
        String value = text(raw, 240);
        if (value.matches("(?is).*\\b(?:password|otp|verification|vérification|authentification|mot de passe|code)\\b.*"))
            return "Contenu sensible masqué";
        return value.replaceAll("\\b[0-9]{4,}\\b", "[masqué]");
    }
    static long eventStart(String date, String time, ZoneId zone) {
        return MorpheePolicy.parseDate(date).atTime(MorpheePolicy.parseTime(time)).atZone(zone).toInstant().toEpochMilli();
    }
    static long eventEnd(long begin, int minutes) {
        if (minutes < 5 || minutes > 24 * 60) throw new IllegalArgumentException("Durée : 5 minutes à 24 heures");
        return Math.addExact(begin, minutes * 60_000L);
    }
    static void feature(String key) {
        if (!FEATURES.contains(key)) throw new IllegalArgumentException("Accès inconnu");
    }
}
