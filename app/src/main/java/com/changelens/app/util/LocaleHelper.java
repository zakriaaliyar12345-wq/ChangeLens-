package com.changelens.app.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.changelens.app.data.SettingsRepository;

import java.util.Locale;

/**
 * Locale persistence and context wrapping for English, Iranian Persian (fa-IR)
 * and Dari (fa-AF). The selected tag is stored in {@link SettingsRepository}
 * and applied by wrapping every activity/receiver base context.
 */
public final class LocaleHelper {

    public static final String TAG_EN = "en";
    public static final String TAG_FA_IR = "fa-IR";
    public static final String TAG_FA_AF = "fa-AF";

    private LocaleHelper() {
    }

    public static Context wrap(Context context) {
        Locale locale = resolve(context);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
        }
        return context.createConfigurationContext(config);
    }

    public static Locale resolve(Context context) {
        String tag = new SettingsRepository(context).getLanguageTag();
        return forTag(tag);
    }

    public static Locale forTag(String tag) {
        if (TAG_FA_AF.equals(tag)) {
            return new Locale("fa", "AF");
        }
        if (TAG_FA_IR.equals(tag)) {
            return new Locale("fa", "IR");
        }
        return Locale.ENGLISH;
    }

    public static boolean isRtl(Context context) {
        return resolve(context).getLanguage().equals("fa");
    }

    /** Applies the locale to framework-declared locales (per-app language on API 33+). */
    public static void applyFrameworkLocales(String tag) {
        try {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
        } catch (Throwable ignored) {
            // Context wrapping remains the source of truth; this is best-effort.
        }
    }

    public static void persistAndApply(Context context, String tag) {
        new SettingsRepository(context).setLanguageTag(tag);
        applyFrameworkLocales(tag);
    }

    public static String currentDisplayTag(Context context) {
        Locale locale = resolve(context);
        if (locale.getLanguage().equals("fa")) {
            return locale.getCountry().equalsIgnoreCase("AF") ? TAG_FA_AF : TAG_FA_IR;
        }
        return TAG_EN;
    }

    public static LocaleList systemLocales(Resources res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return res.getConfiguration().getLocales();
        }
        return new LocaleList(res.getConfiguration().locale);
    }
}
