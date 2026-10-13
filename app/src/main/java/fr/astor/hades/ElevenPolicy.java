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
        String prefix = "ElevenLabs · HTTP " + status + " : ";
        String reason;
        if (status == 400 || status == 422) reason = "Requête refusée. Vérifiez que la voix choisie fonctionne dans votre compte ElevenLabs.";
        else if (status == 401) reason = "Clé API refusée. Vérifiez la clé dans Configurer ElevenLabs.";
        else if (status == 403) reason = "Accès refusé. Vérifiez les permissions de synthèse vocale de la clé et l’accès à cette voix dans votre compte.";
        else if (status == 404) reason = "Cette voix n’est pas accessible. Ajoutez la voix choisie à votre bibliothèque ElevenLabs.";
        else if (status == 402 || status == 429) reason = "Crédits ou limite ElevenLabs atteints. Vérifiez votre compte.";
        else if (status >= 500 && status <= 599) reason = "Erreur du service distant. Un nouvel essai manuel pourra être effectué plus tard.";
        else if (status >= 300 && status <= 399) reason = "Redirection du service refusée pour protéger la clé API.";
        else reason = "Réponse inattendue du service. Communiquez ce code pour le diagnostic.";
        return prefix + reason + " Aucun nouvel essai automatique ; la voix Android locale reste disponible.";
    }
}
