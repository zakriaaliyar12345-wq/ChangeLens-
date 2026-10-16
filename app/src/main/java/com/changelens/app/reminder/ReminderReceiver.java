package com.changelens.app.reminder;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.changelens.app.R;
import com.changelens.app.ui.MainActivity;
import com.changelens.app.util.LocaleHelper;
import com.changelens.app.util.NotificationHelper;

/** Receives the daily alarm and posts the progress-reminder notification. */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context rawContext, Intent intent) {
        Context context = LocaleHelper.wrap(rawContext);
        if (!NotificationHelper.canNotify(context)) {
            return;
        }
        Intent open = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(
                context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = NotificationHelper.base(context, NotificationHelper.CHANNEL_REMINDERS)
                .setContentTitle(context.getString(R.string.reminder_title))
                .setContentText(context.getString(R.string.reminder_text))
                .setContentIntent(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationHelper.notifyCompat(context, builder.build(), 1001);
    }
}
