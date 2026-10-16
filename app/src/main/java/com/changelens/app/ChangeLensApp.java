package com.changelens.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.changelens.app.reminder.ReminderScheduler;
import com.changelens.app.service.ThumbnailCleanupJobService;
import com.changelens.app.util.LocaleHelper;
import com.changelens.app.util.NotificationHelper;

public class ChangeLensApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.ensureChannels(this);
        ThumbnailCleanupJobService.scheduleIfNeeded(this);
        ReminderScheduler.rescheduleIfEnabled(this);
    }
}
