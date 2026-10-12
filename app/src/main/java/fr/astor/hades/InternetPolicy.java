package fr.astor.hades;

import java.net.IDN;
import java.net.URI;
import java.util.Locale;
import java.util.Set;

/**
 * HADES: policy for future consent-gated public web lookups.
 * This class performs NO network operation and grants NO permissions.
 */
public final class InternetPolicy {
    private static final Set<String> PUBLIC_SOURCES = Set.of(
            "www.service-public.fr",
            "www.legifrance.gouv.fr",
            "fr.wikipedia.org",
            "www.wikipedia.org",
            "developer.android.com"
    );

    private InternetPolicy() {}

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
