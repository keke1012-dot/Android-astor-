package fr.astor.hades;
public final class ElevenPolicyTest {
    private static void check(boolean condition) { if (!condition) throw new AssertionError(); }
    public static void main(String[] args) {
        check(ElevenPolicy.ENDPOINT.equals("https://api.elevenlabs.io/v1/text-to-speech/wDsJlOXPqcvIUKdLXjDs?output_format=mp3_44100_128"));
        check(ElevenPolicy.text(" Bonjour ").equals("Bonjour"));
        try { ElevenPolicy.text("x".repeat(801)); throw new AssertionError(); } catch (IllegalArgumentException expected) {}
        try { ElevenPolicy.text("  "); throw new AssertionError(); } catch (IllegalArgumentException expected) {}
        check(ElevenPolicy.validKey("EXAMPLE_ONLY_KEY_123456789"));
        check(!ElevenPolicy.validKey("EXAMPLE_ONLY_KEY\r\nInjected: value"));
        check(!ElevenPolicy.validKey("EXAMPLE ONLY KEY 123456789"));
        check(!ElevenPolicy.validKey("x".repeat(257)));
        check(ElevenPolicy.mp3(new byte[]{'I','D','3',0}));
        check(ElevenPolicy.mp3(new byte[]{(byte)255,(byte)251,0,0}));
        check(!ElevenPolicy.mp3("{error}".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        check(ElevenPolicy.error(401).contains("Clé API"));
        check(ElevenPolicy.error(404).contains("Ajoutez"));
        check(ElevenPolicy.error(429).contains("Aucun nouvel essai"));
        for (int code : new int[]{400,401,402,403,404,422,429,500,502,503,307,418}) {
            check(ElevenPolicy.error(code).contains("HTTP " + code));
            check(ElevenPolicy.error(code).contains("Aucun nouvel essai automatique"));
        }
        check(ElevenPolicy.error(503).contains("service distant"));
        check(ElevenPolicy.error(400).contains("Requête refusée"));
        check(ElevenPolicy.error(307).contains("Redirection"));
        System.out.println("PASS: fixed ElevenLabs voice/endpoint, bounded exact text, header-injection rejection, MP3 format and actionable errors");
    }
}
