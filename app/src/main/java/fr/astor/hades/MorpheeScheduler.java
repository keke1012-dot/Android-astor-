package fr.astor.hades;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/** Inexact reminders: Android may delay delivery under battery optimizations. */
final class MorpheeScheduler {
    static final String ACTION = "fr.astor.hades.MORPHEE_REMINDER";
    static final String PREPARE = "prepare", BED = "bed", WAKE = "wake", BREAK = "break";
    private static final int CODE_PREPARE = 301, CODE_BED = 302, CODE_WAKE = 303, CODE_BREAK = 304;

    private MorpheeScheduler() { }

    private static AlarmManager alarms(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private static PendingIntent pending(Context context, int code, String kind) {
        Intent intent = new Intent(context, MorpheeReceiver.class).setAction(ACTION).putExtra("kind", kind);
        return PendingIntent.getBroadcast(context, code, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void set(Context context, int code, String kind, long millis) {
        AlarmManager alarm = alarms(context);
        if (alarm != null) alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis,
                pending(context, code, kind));
    }

    private static void cancel(Context context, int code, String kind) {
        AlarmManager alarm = alarms(context);
        if (alarm != null) alarm.cancel(pending(context, code, kind));
    }

    static void scheduleDaily(Context context, String kind) {
        MorpheeStore store = new MorpheeStore(context);
        if (!store.preferences().getBoolean("reminders", true)) {
            cancel(context, code(kind), kind);
            return;
        }
        LocalTime time = PREPARE.equals(kind) ? MorpheePolicy.PREPARE
                : BED.equals(kind) ? MorpheePolicy.BEDTIME : MorpheePolicy.WAKE;
        ZonedDateTime next = MorpheePolicy.nextOccurrence(ZonedDateTime.now(), time);
        set(context, code(kind), kind, next.toInstant().toEpochMilli());
    }

    private static int code(String kind) {
        if (PREPARE.equals(kind)) return CODE_PREPARE;
        if (BED.equals(kind)) return CODE_BED;
        if (WAKE.equals(kind)) return CODE_WAKE;
        throw new IllegalArgumentException("Rappel inconnu");
    }

    static void scheduleAll(Context context) {
        scheduleDaily(context, PREPARE);
        scheduleDaily(context, BED);
        scheduleDaily(context, WAKE);
        scheduleBreak(context);
    }

    static void startSession(Context context) {
        long now = System.currentTimeMillis();
        new MorpheeStore(context).preferences().edit()
                .putBoolean("session_active", true).putLong("session_start", now)
                .putLong("break_due", now + MorpheePolicy.BREAK_MINUTES * 60_000L).apply();
        scheduleBreak(context);
    }

    static void stopSession(Context context) {
        new MorpheeStore(context).preferences().edit().putBoolean("session_active", false)
                .remove("session_start").remove("break_due").apply();
        cancel(context, CODE_BREAK, BREAK);
    }

    static void postponeBreak(Context context, int minutes) {
        if (minutes < 1 || minutes > 60) throw new IllegalArgumentException("Report invalide");
        SharedPreferences preferences = new MorpheeStore(context).preferences();
        if (!preferences.getBoolean("session_active", false)) return;
        preferences.edit().putLong("break_due", System.currentTimeMillis() + minutes * 60_000L).apply();
        scheduleBreak(context);
    }

    static void scheduleBreak(Context context) {
        SharedPreferences preferences = new MorpheeStore(context).preferences();
        if (!preferences.getBoolean("breaks", true) || !preferences.getBoolean("session_active", false)) {
            cancel(context, CODE_BREAK, BREAK);
            return;
        }
        long due = preferences.getLong("break_due", 0);
        if (due <= 0) {
            due = System.currentTimeMillis() + MorpheePolicy.BREAK_MINUTES * 60_000L;
            preferences.edit().putLong("break_due", due).apply();
        }
        set(context, CODE_BREAK, BREAK, Math.max(due, System.currentTimeMillis() + 1000L));
    }
}
