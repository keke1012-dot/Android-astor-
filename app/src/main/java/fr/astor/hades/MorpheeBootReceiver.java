package fr.astor.hades;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Re-arm inexact daily reminders after reboot or clock/timezone change. */
public final class MorpheeBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // A development session must not silently survive a reboot.
            MorpheeScheduler.stopSession(context);
        } else if (!Intent.ACTION_TIME_CHANGED.equals(action)
                && !Intent.ACTION_TIMEZONE_CHANGED.equals(action)) return;
        MorpheeScheduler.scheduleAll(context);
    }
}
