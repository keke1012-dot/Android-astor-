package fr.astor.hades;

import java.net.IDN;
import java.net.URI;
import java.util.Locale;
import java.util.Set;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/** Only the explicitly entered query may leave the device. */
public final class InternetPolicy {
    private static final Set<String> PUBLIC_SOURCES = Set.of(
            "www.service-public.fr",
            "www.legifrance.gouv.fr",
            "fr.wikipedia.org",
            "www.wikipedia.org",
            "developer.android.com"
    );

    private InternetPolicy() {}
    public static String query(String raw) {
        if (raw == null) throw new IllegalArgumentException("Requête vide");
        String value = raw.trim();
        if (value.isEmpty() || value.length() > 300 || value.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Requête invalide (300 caractères maximum)");
        return value;
    }
    public static String encoded(String raw) {
        try { return URLEncoder.encode(query(raw), "UTF-8"); }
        catch (UnsupportedEncodingException impossible) { throw new AssertionError(impossible); }
    }
    public static String endpoint(String raw) {
        return "https://fr.wikipedia.org/w/api.php?action=query&format=json&formatversion=2"
            + "&generator=search&gsrnamespace=0&gsrlimit=3&prop=extracts&exintro=1&explaintext=1"
            + "&exchars=1800&gsrsearch=" + encoded(raw);
    }
    /** Rejects arbitrary destinations, credentials, IP literals and non-HTTPS URLs. */
    public static boolean allowedPublicUrl(String raw) {
        if (raw == null || raw.length() > 2048) return false;
        try {
            URI uri = new URI(raw);
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getRawUserInfo() != null
                    || uri.getHost() == null
                    || uri.getRawFragment() != null
                    || uri.getPort() != -1 && uri.getPort() != 443) return false;
            String host = IDN.toASCII(uri.getHost()).toLowerCase(Locale.ROOT);
            return PUBLIC_SOURCES.contains(host);
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Consent is per request, never persisted or inferred from a prior approval.
     * No private memory, transcript or credential may be attached automatically.
     */
    public static boolean mayFetch(String url, boolean explicitConsent,
                                   boolean containsPrivateContext) {
        return explicitConsent && !containsPrivateContext && allowedPublicUrl(url);
    }
}
