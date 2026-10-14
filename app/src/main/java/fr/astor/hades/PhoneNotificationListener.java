package fr.astor.hades;

import android.app.Notification;
import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;
import java.util.List;

/** No background extraction, persistence, dismissal, reply, or PendingIntent execution. */
public final class PhoneNotificationListener extends NotificationListenerService {
    private static volatile PhoneNotificationListener connected;
    @Override public void onListenerConnected() { connected = this; }
    @Override public void onListenerDisconnected() { if (connected == this) connected = null; }
    @Override public void onDestroy() { if (connected == this) connected = null; super.onDestroy(); }
    static void unbind() {
        PhoneNotificationListener current = connected;
        if (current != null) current.requestUnbind();
        connected = null;
    }
    static List<String> snapshot(Context context, boolean includeText) {
        List<String> result = new ArrayList<>();
        if (!context.getSharedPreferences(PhoneAccessActivity.PREFS, Context.MODE_PRIVATE)
                .getBoolean("notifications", false)) return result;
        PhoneNotificationListener current = connected;
        if (current == null) throw new IllegalStateException("Accès Android non connecté : vérifiez l’autorisation de notifications.");
        StatusBarNotification[] active = current.getActiveNotifications();
        if (active == null) return result;
        for (StatusBarNotification item : active) {
            if (result.size() >= 30) break;
            Notification notification = item.getNotification();
            if (notification == null || notification.visibility == Notification.VISIBILITY_SECRET) continue;
            String line = PhoneAccessPolicy.text(item.getPackageName(), 160);
            if (includeText && notification.extras != null) {
                CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
                CharSequence body = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
                line += "\n" + PhoneAccessPolicy.notificationText(title == null ? "" : title.toString())
                        + "\n" + PhoneAccessPolicy.notificationText(body == null ? "" : body.toString());
            }
            result.add(line);
        }
        return result;
    }
}
