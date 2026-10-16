package com.changelens.app.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.changelens.app.R;

/** Notification channels (topic 08.1). */
public final class NotificationHelper {

    public static final String CHANNEL_REMINDERS = "reminders";
    public static final String CHANNEL_EXPORTS = "exports";

    private NotificationHelper() {
    }

    public static void ensureChannels(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        NotificationChannel reminders = new NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.notification_channel_reminders_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        reminders.setDescription(context.getString(R.string.notification_channel_reminders_desc));

        NotificationChannel exports = new NotificationChannel(
                CHANNEL_EXPORTS,
                context.getString(R.string.notification_channel_exports_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        exports.setDescription(context.getString(R.string.notification_channel_exports_desc));

        manager.createNotificationChannel(reminders);
        manager.createNotificationChannel(exports);
    }

    public static boolean canNotify(@NonNull Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static void notifyCompat(@NonNull Context context,
                                    @NonNull android.app.Notification notification,
                                    int id) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (manager.areNotificationsEnabled()) {
            manager.notify(id, notification);
        }
    }

    @NonNull
    public static NotificationCompat.Builder base(@NonNull Context context, @NonNull String channelId) {
        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);
    }
}
