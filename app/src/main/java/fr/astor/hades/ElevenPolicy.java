package fr.astor.hades;

/** Fixed provider/voice and bounded user-selected text. */
public final class ElevenPolicy {
    public static final String VOICE_ID = "wDsJlOXPqcvIUKdLXjDs";
    public static final String ENDPOINT = "https://api.elevenlabs.io/v1/text-to-speech/" + VOICE_ID + "?output_format=mp3_44100_128";
    public static final String MODEL = "eleven_multilingual_v2";
    public static final int MAX_AUDIO_BYTES = 10_000_000;
    private ElevenPolicy() {}
    public static String text(String raw) {
        if (raw == null) throw new IllegalArgumentException("Missing text");
        String result = raw.trim();
        if (result.isEmpty() || result.length() > 800) throw new IllegalArgumentException("Text bounds");
        return result;
    }
    public static boolean validKey(String key) {
        return key != null && key.length() >= 16 && key.length() <= 256
                && key.chars().allMatch(c -> c >= 33 && c <= 126);
    }
    public static boolean mp3(byte[] bytes) {
        return bytes.length >= 4 && bytes.length <= MAX_AUDIO_BYTES
                && ((bytes[0] == 'I' && bytes[1] == 'D' && bytes[2] == '3')
                || ((bytes[0] & 255) == 255 && (bytes[1] & 224) == 224));
    }
    public static String error(int status) {
        if (status == 401) return "Clé API refusée. Vérifiez la clé dans Configurer ElevenLabs.";
        if (status == 403) return "Accès à la voix refusé. Vérifiez les permissions de la clé et l’accès à cette voix dans votre compte.";
        if (status == 404) return "Cette voix n’est pas accessible. Ajoutez la voix choisie à votre bibliothèque ElevenLabs.";
        if (status == 402 || status == 429) return "Crédits ou limite ElevenLabs atteints. Vérifiez votre compte ; aucun nouvel essai automatique.";
        return "Service vocal indisponible. Aucun nouvel essai automatique ; la voix Android locale reste disponible.";
    }
}
