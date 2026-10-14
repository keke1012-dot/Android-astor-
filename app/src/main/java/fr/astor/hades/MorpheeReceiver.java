package fr.astor.hades;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

/** Internal-only receiver: notification text deliberately excludes sleep records. */
public final class MorpheeReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "astor_morphee_reminders";

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !MorpheeScheduler.ACTION.equals(intent.getAction())) return;
        String kind = intent.getStringExtra("kind");
        if (!MorpheeScheduler.PREPARE.equals(kind) && !MorpheeScheduler.BED.equals(kind)
                && !MorpheeScheduler.WAKE.equals(kind) && !MorpheeScheduler.BREAK.equals(kind)) return;
        SharedPreferences preferences = new MorpheeStore(context).preferences();
        if (MorpheeScheduler.BREAK.equals(kind)) {
            if (!preferences.getBoolean("breaks", true) || !preferences.getBoolean("session_active", false)) return;
            long due = preferences.getLong("break_due", 0);
            if (System.currentTimeMillis() + 1000 < due) {
                MorpheeScheduler.scheduleBreak(context);
                return;
            }
            preferences.edit().putLong("break_due", System.currentTimeMillis() + 60 * 60_000L).apply();
            MorpheeScheduler.scheduleBreak(context);
            notifyUser(context, 404, "MORPHÉE · Pause", "Une pause de développement est proposée.");
            return;
        }
        if (!preferences.getBoolean("reminders", true)) return;
        // Re-arm for the next day before posting a notification.
        MorpheeScheduler.scheduleDaily(context, kind);
        if (MorpheeScheduler.PREPARE.equals(kind))
            notifyUser(context, 401, "MORPHÉE · Préparation", "Le moment de ralentir approche.");
        else if (MorpheeScheduler.BED.equals(kind))
            notifyUser(context, 402, "MORPHÉE · Sommeil", "Votre heure de coucher cible est arrivée.");
        else
            notifyUser(context, 403, "MORPHÉE · Réveil", "Bonne journée. Prenez votre temps.");
    }

    private void notifyUser(Context context, int id, String title, String message) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        manager.createNotificationChannel(new NotificationChannel(CHANNEL, "MORPHÉE · Bien-être",
                NotificationManager.IMPORTANCE_DEFAULT));
        Intent open = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(context, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title).setContentText(message).setContentIntent(pending)
                .setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build();
        manager.notify(id, notification);
    }
}
