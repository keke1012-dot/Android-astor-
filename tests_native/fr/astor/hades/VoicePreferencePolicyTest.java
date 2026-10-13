package fr.astor.hades;
public final class VoicePreferencePolicyTest {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    public static void main(String[] args) {
        check(VoicePreferencePolicy.masculineName("fr-FR-male-local"));
        check(VoicePreferencePolicy.masculineName("Français masculin"));
        check(!VoicePreferencePolicy.masculineName("fr-FR-female-local"));
        check(!VoicePreferencePolicy.masculineName("fr-fr-x-vlf-local"));
        check(!VoicePreferencePolicy.masculineName(null));
        check(VoicePreferencePolicy.profile(null).equals("jarvis"));
        check(VoicePreferencePolicy.profile("injected").equals("jarvis"));
        System.out.println("PASS: explicit masculine names only, no opaque ID guesses and bounded profiles");
    }
}
