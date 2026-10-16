package com.changelens.app.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.changelens.app.R;
import com.changelens.app.data.ProjectRepository;
import com.changelens.app.data.entity.Project;
import com.changelens.app.databinding.ActivityEducationBinding;
import com.changelens.app.education.LegacyAsyncTaskDemo;
import com.changelens.app.education.LegacyLoaderDemo;
import com.changelens.app.service.ReportExportService;
import com.changelens.app.service.ThumbnailCleanupJobService;
import com.changelens.app.util.LocaleHelper;

import java.util.List;

/**
 * Course demos screen (topics 03.1, 07.1, 07.2, 07.4, 08.3).
 * Legacy APIs are isolated in the education package; these buttons run them
 * alongside the production services for side-by-side comparison.
 */
public class EducationActivity extends AppCompatActivity {

    private ActivityEducationBinding binding;
    private LegacyAsyncTaskDemo runningTask;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEducationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.asyncTaskButton.setOnClickListener(v -> runAsyncTaskDemo());
        binding.loaderButton.setOnClickListener(v -> runLoaderDemo());
        binding.jobButton.setOnClickListener(v -> {
            ThumbnailCleanupJobService.scheduleIfNeeded(this);
            showResult(getString(R.string.demo_job_scheduled));
        });
        binding.serviceButton.setOnClickListener(v -> runServiceDemo());
        binding.connectivityButton.setOnClickListener(v -> showConnectivity());
    }

    @Override
    protected void onDestroy() {
        if (runningTask != null) {
            runningTask.cancel(true);
            runningTask = null;
        }
        super.onDestroy();
    }

    private void runAsyncTaskDemo() {
        binding.asyncTaskButton.setEnabled(false);
        showResult(getString(R.string.demo_async_task_running));
        runningTask = new LegacyAsyncTaskDemo(this, new LegacyAsyncTaskDemo.Callback() {
            @Override
            public void onProgress(int percent) {
                showResult(getString(R.string.demo_async_task_running) + " " + percent + "%");
            }

            @Override
            public void onFinished(String result) {
                binding.asyncTaskButton.setEnabled(true);
                showResult(getString(R.string.demo_async_task_done));
                Toast.makeText(EducationActivity.this,
                        R.string.demo_async_task_done, Toast.LENGTH_SHORT).show();
            }
        });
        runningTask.execute();
    }

    private void runLoaderDemo() {
        LegacyLoaderDemo demo = new LegacyLoaderDemo(this, tips -> {
            String message = getString(R.string.demo_loader_result, tips == null ? 0 : tips.size());
            showResult(message);
            if (tips != null) {
                StringBuilder all = new StringBuilder(message);
                for (String tip : tips) {
                    all.append("\n• ").append(tip);
                }
                showResult(all.toString());
            }
        });
        demo.start();
    }

    private void runServiceDemo() {
        ProjectRepository.getInstance(this).getNewestActiveProject(new ProjectRepository.Callback<Project>() {
            @Override
            public void onResult(Project result) {
                if (result == null) {
                    showResult(getString(R.string.demo_service_none));
                    return;
                }
                ReportExportService.startExport(EducationActivity.this, result.id);
                showResult(getString(R.string.demo_service_started));
            }
        });
    }

    private void showConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean online = false;
        if (cm != null) {
            Network active = cm.getActiveNetwork();
            if (active != null) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(active);
                online = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        showResult(getString(R.string.demo_result,
                online ? getString(R.string.help_offline_body) : getString(R.string.offline_banner)));
    }

    private void showResult(String text) {
        binding.resultText.setText(text);
    }
}
