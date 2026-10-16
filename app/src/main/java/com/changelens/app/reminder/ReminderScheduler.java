package com.changelens.app.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.changelens.app.data.SettingsRepository;

import java.util.Calendar;

/**
 * User-controlled daily reminders (topic 08.2).
 * Uses inexact repeating alarms so no SCHEDULE_EXACT_ALARM permission is needed
 * and battery impact stays low, respecting current platform restrictions.
 */
public final class ReminderScheduler {

    private static final int REQUEST_CODE = 4212;

    private ReminderScheduler() {
    }

    @NonNull
    public static PendingIntent pendingIntent(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void scheduleDaily(@NonNull Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }
        long interval = AlarmManager.INTERVAL_DAY;
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                next.getTimeInMillis(),
                interval,
                pendingIntent(context));
    }

    public static void cancel(@NonNull Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        alarmManager.cancel(pendingIntent(context));
        pendingIntent(context).cancel();
    }

    /** Called on app start and after boot/time changes so the alarm survives restarts. */
    public static void rescheduleIfEnabled(@NonNull Context context) {
        SettingsRepository settings = new SettingsRepository(context);
        if (settings.isReminderEnabled()) {
            scheduleDaily(context, settings.getReminderHour(), settings.getReminderMinute());
        }
    }

    public static void apply(@NonNull Context context, boolean enabled, int hour, int minute) {
        SettingsRepository settings = new SettingsRepository(context);
        settings.setReminderEnabled(enabled);
        settings.setReminderTime(hour, minute);
        if (enabled) {
            scheduleDaily(context, hour, minute);
        } else {
            cancel(context);
        }
    }
}
