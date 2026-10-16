package com.changelens.app.util;

import android.content.Context;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Locale-aware date formatting (topic: localization correctness). */
public final class Formatters {

    private Formatters() {
    }

    @NonNull
    public static String formatDate(Context context, long millis) {
        Locale locale = LocaleHelper.resolve(context);
        return DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(new Date(millis));
    }

    @NonNull
    public static String formatDateTime(Context context, long millis) {
        Locale locale = LocaleHelper.resolve(context);
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale)
                .format(new Date(millis));
    }

    @NonNull
    public static String formatTime(Context context, int hour, int minute) {
        Locale locale = LocaleHelper.resolve(context);
        Date date = new Date();
        date.setHours(hour);
        date.setMinutes(minute);
        return DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(date);
    }

    @NonNull
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.US, "%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format(Locale.US, "%.1f MB", mb);
        }
        return String.format(Locale.US, "%.2f GB", mb / 1024.0);
    }

    @NonNull
    public static String isoDate(long millis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(millis));
    }
}
