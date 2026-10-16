package com.changelens.app.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.changelens.app.R;
import com.changelens.app.data.ProjectRepository;
import com.changelens.app.data.SettingsRepository;
import com.changelens.app.databinding.ActivitySettingsBinding;
import com.changelens.app.reminder.ReminderScheduler;
import com.changelens.app.util.AppExecutors;
import com.changelens.app.util.Formatters;
import com.changelens.app.util.ImageStore;
import com.changelens.app.util.LocaleHelper;
import com.changelens.app.util.NotificationHelper;

import java.io.File;

/** App settings: language, theme, comparison mode, reminders, storage, privacy. */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SettingsRepository settings;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    enableReminders();
                } else {
                    binding.reminderSwitch.setChecked(false);
                    Toast.makeText(this, R.string.notification_permission_needed,
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        settings = new SettingsRepository(this);
        refreshSummaries();
        loadStorageInfo();

        binding.languageButton.setOnClickListener(v -> pickLanguage());
        binding.themeButton.setOnClickListener(v -> pickTheme());
        binding.modeButton.setOnClickListener(v -> pickComparisonMode());
        binding.reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                maybeEnableReminders();
            } else {
                ReminderScheduler.apply(this, false, settings.getReminderHour(), settings.getReminderMinute());
                Toast.makeText(this, R.string.reminder_disabled, Toast.LENGTH_SHORT).show();
            }
        });
        binding.reminderTimeButton.setOnClickListener(v -> pickReminderTime());
        binding.resetButton.setOnClickListener(v -> confirmReset());
    }

    private void refreshSummaries() {
        String tag = settings.getLanguageTag();
        int languageRes;
        if (LocaleHelper.TAG_FA_AF.equals(tag)) {
            languageRes = R.string.language_dari;
        } else if (LocaleHelper.TAG_FA_IR.equals(tag)) {
            languageRes = R.string.language_persian;
        } else {
            languageRes = R.string.language_english;
        }
        binding.languageButton.setText(languageRes);

        String theme = settings.getThemeMode();
        int themeRes;
        if (SettingsRepository.THEME_LIGHT.equals(theme)) {
            themeRes = R.string.settings_theme_light;
        } else if (SettingsRepository.THEME_DARK.equals(theme)) {
            themeRes = R.string.settings_theme_dark;
        } else {
            themeRes = R.string.settings_theme_system;
        }
        binding.themeButton.setText(themeRes);

        boolean overlay = SettingsRepository.MODE_OVERLAY.equals(settings.getDefaultComparisonMode());
        binding.modeButton.setText(overlay ? R.string.mode_overlay : R.string.mode_slider);

        binding.reminderSwitch.setChecked(settings.isReminderEnabled());
        binding.reminderTimeButton.setText(Formatters.formatTime(this,
                settings.getReminderHour(), settings.getReminderMinute()));
        binding.privacyText.setText(R.string.privacy_body);
    }

    private void pickLanguage() {
        final String[] tags = {LocaleHelper.TAG_EN, LocaleHelper.TAG_FA_IR, LocaleHelper.TAG_FA_AF};
        CharSequence[] labels = {
                getString(R.string.language_english),
                getString(R.string.language_persian),
                getString(R.string.language_dari)
        };
        int current = 0;
        for (int i = 0; i < tags.length; i++) {
            if (tags[i].equals(settings.getLanguageTag())) {
                current = i;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_language)
                .setSingleChoiceItems(labels, current, (dialog, which) -> {
                    dialog.dismiss();
                    if (!tags[which].equals(settings.getLanguageTag())) {
                        LocaleHelper.persistAndApply(this, tags[which]);
                        restartForLanguage();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void restartForLanguage() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void pickTheme() {
        final String[] modes = {
                SettingsRepository.THEME_SYSTEM,
                SettingsRepository.THEME_LIGHT,
                SettingsRepository.THEME_DARK
        };
        CharSequence[] labels = {
                getString(R.string.settings_theme_system),
                getString(R.string.settings_theme_light),
                getString(R.string.settings_theme_dark)
        };
        int current = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(settings.getThemeMode())) {
                current = i;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_theme)
                .setSingleChoiceItems(labels, current, (dialog, which) -> {
                    settings.setThemeMode(modes[which]);
                    applyTheme(modes[which]);
                    dialog.dismiss();
                    refreshSummaries();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void applyTheme(String mode) {
        int nightMode;
        if (SettingsRepository.THEME_LIGHT.equals(mode)) {
            nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
        } else if (SettingsRepository.THEME_DARK.equals(mode)) {
            nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private void pickComparisonMode() {
        final String[] modes = {SettingsRepository.MODE_SLIDER, SettingsRepository.MODE_OVERLAY};
        CharSequence[] labels = {getString(R.string.mode_slider), getString(R.string.mode_overlay)};
        int current = SettingsRepository.MODE_OVERLAY.equals(settings.getDefaultComparisonMode()) ? 1 : 0;
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_default_mode)
                .setSingleChoiceItems(labels, current, (dialog, which) -> {
                    settings.setDefaultComparisonMode(modes[which]);
                    dialog.dismiss();
                    refreshSummaries();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void maybeEnableReminders() {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            enableReminders();
        }
    }

    private void enableReminders() {
        ReminderScheduler.apply(this, true, settings.getReminderHour(), settings.getReminderMinute());
        Toast.makeText(this, R.string.reminder_enabled, Toast.LENGTH_SHORT).show();
        refreshSummaries();
    }

    private void pickReminderTime() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            settings.setReminderTime(hourOfDay, minute);
            if (settings.isReminderEnabled()) {
                ReminderScheduler.apply(this, true, hourOfDay, minute);
            }
            refreshSummaries();
        }, settings.getReminderHour(), settings.getReminderMinute(), true).show();
    }

    private void loadStorageInfo() {
        AppExecutors.get().diskIO().execute(() -> {
            ProjectRepository repo = ProjectRepository.getInstance(this);
            // Blocking counts via repository internals (database reads on background thread).
            final int[] counts = new int[3];
            try {
                com.changelens.app.data.AppDatabase db =
                        com.changelens.app.data.AppDatabase.getInstance(this);
                counts[0] = db.projectDao().countBlocking();
                counts[1] = db.photoEntryDao().countBlocking();
                counts[2] = db.annotationDao().countBlocking();
            } catch (Exception ignored) {
            }
            ImageStore store = new ImageStore(this);
            long imagesSize = directorySize(store.imagesDir()) + directorySize(store.thumbsDir());
            long exportsSize = directorySize(store.exportsDir()) + directorySize(store.reportsDir());
            File dbFile = getDatabasePath("changelens.db");
            long dbSize = dbFile != null && dbFile.exists() ? dbFile.length() : 0;
            long total = imagesSize + exportsSize + dbSize;
            String text = getString(R.string.storage_projects, counts[0]) + "\n"
                    + getString(R.string.storage_photos, counts[1]) + "\n"
                    + getString(R.string.storage_annotations, counts[2]) + "\n"
                    + getString(R.string.storage_database, Formatters.formatFileSize(dbSize)) + "\n"
                    + getString(R.string.storage_images, Formatters.formatFileSize(imagesSize)) + "\n"
                    + getString(R.string.storage_exports, Formatters.formatFileSize(exportsSize)) + "\n"
                    + getString(R.string.storage_total, Formatters.formatFileSize(total));
            runOnUiThread(() -> binding.storageText.setText(text));
        });
    }

    private static long directorySize(File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    size += f.length();
                } else {
                    size += directorySize(f);
                }
            }
        }
        return size;
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_reset_title)
                .setMessage(R.string.confirm_reset_message)
                .setPositiveButton(R.string.yes, (d, w) -> {
                    settings.resetToDefaults();
                    applyTheme(SettingsRepository.THEME_SYSTEM);
                    ReminderScheduler.cancel(this);
                    refreshSummaries();
                    Toast.makeText(this, R.string.settings_reset_done, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
