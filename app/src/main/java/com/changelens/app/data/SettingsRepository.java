package com.changelens.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.changelens.app.util.LocaleHelper;

/**
 * Small settings persisted with SharedPreferences (topic 09.1).
 * Large data (projects, photos) lives in Room instead.
 */
public class SettingsRepository {

    private static final String PREFS = "changelens_settings";

    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    public static final String MODE_SLIDER = "slider";
    public static final String MODE_OVERLAY = "overlay";

    public static final int SORT_RECENT = 0;
    public static final int SORT_OLDEST = 1;
    public static final int SORT_TITLE = 2;
    public static final int SORT_CREATED = 3;

    private final SharedPreferences prefs;

    public SettingsRepository(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        this.prefs = (appContext != null ? appContext : context)
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getLanguageTag() {
        return prefs.getString("language_tag", LocaleHelper.TAG_EN);
    }

    public void setLanguageTag(String tag) {
        prefs.edit().putString("language_tag", tag).apply();
    }

    public String getThemeMode() {
        return prefs.getString("theme_mode", THEME_SYSTEM);
    }

    public void setThemeMode(String mode) {
        prefs.edit().putString("theme_mode", mode).apply();
    }

    public String getDefaultComparisonMode() {
        return prefs.getString("comparison_mode", MODE_SLIDER);
    }

    public void setDefaultComparisonMode(String mode) {
        prefs.edit().putString("comparison_mode", mode).apply();
    }

    public boolean isReminderEnabled() {
        return prefs.getBoolean("reminder_enabled", false);
    }

    public void setReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean("reminder_enabled", enabled).apply();
    }

    public int getReminderHour() {
        return prefs.getInt("reminder_hour", 19);
    }

    public int getReminderMinute() {
        return prefs.getInt("reminder_minute", 0);
    }

    public void setReminderTime(int hour, int minute) {
        prefs.edit().putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply();
    }

    public int getProjectSort() {
        return prefs.getInt("project_sort", SORT_RECENT);
    }

    public void setProjectSort(int sort) {
        prefs.edit().putInt("project_sort", sort).apply();
    }

    public boolean isOnboardingShown() {
        return prefs.getBoolean("onboarding_shown", false);
    }

    public void setOnboardingShown(boolean shown) {
        prefs.edit().putBoolean("onboarding_shown", shown).apply();
    }

    public void resetToDefaults() {
        prefs.edit().clear().apply();
    }
}
