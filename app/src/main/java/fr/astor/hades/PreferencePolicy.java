package fr.astor.hades;

/** Validated settings; a stored value cannot disable the inactivity lock. */
final class PreferencePolicy {
    private PreferencePolicy() { }

    static int idleMinutes(int value) {
        return value == 1 || value == 2 || value == 5 ? value : 5;
    }

    static int volume(int value) {
        return Math.max(0, Math.min(100, value));
    }

    static int textZoom(int value) {
        return value == 90 || value == 100 || value == 110 || value == 120 ? value : 100;
    }
}
