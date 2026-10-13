package fr.astor.hades;

/** No undocumented mapping from opaque Android voice IDs to gender. */
public final class VoicePreferencePolicy {
    private VoicePreferencePolicy() {}
    public static boolean masculineName(String name) {
        return name != null && java.util.regex.Pattern.compile("(?:^|[^a-z])(?:male|masculin|homme|masculine)(?:$|[^a-z])")
                .matcher(name.toLowerCase(java.util.Locale.ROOT)).find();
    }
    public static String profile(String value) {
        return "metal".equals(value) ? "metal" : "grave".equals(value) ? "grave" : "jarvis";
    }
}
