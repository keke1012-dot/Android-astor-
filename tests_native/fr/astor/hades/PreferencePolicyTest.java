package fr.astor.hades;

public final class PreferencePolicyTest {
    public static void main(String[] args) {
        for (int value : new int[]{-1, 0, 3, 10, 60, Integer.MAX_VALUE}) {
            require(PreferencePolicy.idleMinutes(value) == 5, "invalid delay must stay locked");
        }
        for (int value : new int[]{1, 2, 5}) {
            require(PreferencePolicy.idleMinutes(value) == value, "allowed delay");
        }
        require(PreferencePolicy.volume(-1) == 0, "negative volume");
        require(PreferencePolicy.volume(101) == 100, "excess volume");
        require(PreferencePolicy.volume(35) == 35, "default volume");
        for (int value : new int[]{0, 80, 99, 130, Integer.MAX_VALUE}) {
            require(PreferencePolicy.textZoom(value) == 100, "invalid zoom");
        }
        for (int value : new int[]{90, 100, 110, 120}) {
            require(PreferencePolicy.textZoom(value) == value, "allowed zoom");
        }
        System.out.println("PASS: settings bounds and mandatory inactivity lock");
    }

    private static void require(boolean valid, String description) {
        if (!valid) throw new AssertionError(description);
    }
}
