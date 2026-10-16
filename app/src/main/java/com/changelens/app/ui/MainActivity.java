package com.changelens.app.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.changelens.app.R;
import com.changelens.app.data.SettingsRepository;
import com.changelens.app.data.entity.Project;
import com.changelens.app.databinding.ActivityMainBinding;
import com.changelens.app.reminder.ReminderScheduler;
import com.changelens.app.util.LocaleHelper;
import com.changelens.app.util.NotificationHelper;
import com.changelens.app.util.SimpleTextWatcher;

/** Dashboard: branding, search, filters, recent projects, empty states. */
public class MainActivity extends AppCompatActivity implements ProjectsAdapter.Listener {

    private ActivityMainBinding binding;
    private ProjectsViewModel viewModel;
    private ProjectsAdapter adapter;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean offlineBannerForced;

    private final ActivityResultLauncher<Intent> editProjectLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Dashboard observes the database; nothing to do here.
            });

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        adapter = new ProjectsAdapter(this);
        int span = getResources().getInteger(R.integer.projects_span_count);
        if (span > 1) {
            binding.projectsList.setLayoutManager(
                    new androidx.recyclerview.widget.GridLayoutManager(this, span));
        } else {
            binding.projectsList.setLayoutManager(new LinearLayoutManager(this));
        }
        binding.projectsList.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ProjectsViewModel.class);
        viewModel.getVisibleProjects().observe(this, projects -> {
            adapter.submitList(projects);
            boolean empty = projects == null || projects.isEmpty();
            binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.projectsList.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.recentHeader.setVisibility(empty ? View.GONE : View.VISIBLE);
            if (empty) {
                String query = viewModel.getSearchQuery().getValue();
                boolean searching = query != null && !query.trim().isEmpty();
                binding.emptyTitle.setText(searching
                        ? R.string.empty_search_title : R.string.empty_projects_title);
                binding.emptyBody.setText(searching
                        ? R.string.empty_search_body : R.string.empty_projects_body);
            }
        });

        binding.searchInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }
        });

        binding.filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipActive) {
                viewModel.setFilter(ProjectsViewModel.FILTER_ACTIVE);
            } else if (id == R.id.chipArchived) {
                viewModel.setFilter(ProjectsViewModel.FILTER_ARCHIVED);
            } else {
                viewModel.setFilter(ProjectsViewModel.FILTER_ALL);
            }
        });

        binding.fabCreate.setOnClickListener(v -> openCreateProject());
        binding.emptyCreateButton.setOnClickListener(v -> openCreateProject());

        maybeShowOnboarding();
    }

    private void applyTheme() {
        String mode = new SettingsRepository(this).getThemeMode();
        int nightMode;
        if (SettingsRepository.THEME_LIGHT.equals(mode)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (SettingsRepository.THEME_DARK.equals(mode)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private void maybeShowOnboarding() {
        SettingsRepository settings = new SettingsRepository(this);
        if (!settings.isOnboardingShown()) {
            settings.setOnboardingShown(true);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.help_onboarding_title)
                    .setMessage(R.string.help_onboarding_body)
                    .setPositiveButton(R.string.help_got_it, null)
                    .setNeutralButton(R.string.nav_help, (d, w) ->
                            startActivity(new Intent(this, HelpActivity.class)))
                    .show();
        }
    }

    private void openCreateProject() {
        Intent intent = new Intent(this, EditProjectActivity.class);
        editProjectLauncher.launch(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkCallback();
        updateOfflineBanner();
    }

    @Override
    protected void onPause() {
        unregisterNetworkCallback();
        super.onPause();
    }

    private void registerNetworkCallback() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null || networkCallback != null) {
            return;
        }
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> {
                    offlineBannerForced = false;
                    binding.offlineBanner.setText(R.string.online_banner);
                    binding.offlineBanner.postDelayed(() -> binding.offlineBanner.setVisibility(View.GONE), 2500);
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> {
                    offlineBannerForced = true;
                    updateOfflineBanner();
                });
            }
        };
        try {
            cm.registerDefaultNetworkCallback(networkCallback);
        } catch (RuntimeException ignored) {
            networkCallback = null;
        }
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                try {
                    cm.unregisterNetworkCallback(networkCallback);
                } catch (IllegalArgumentException ignored) {
                }
            }
            networkCallback = null;
        }
    }

    private void updateOfflineBanner() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean online = false;
        if (cm != null) {
            Network active = cm.getActiveNetwork();
            if (active != null) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(active);
                online = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        if (!online) {
            binding.offlineBanner.setText(R.string.offline_banner);
            binding.offlineBanner.setVisibility(View.VISIBLE);
        } else if (offlineBannerForced) {
            binding.offlineBanner.setVisibility(View.VISIBLE);
        } else {
            binding.offlineBanner.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        }
        if (id == R.id.action_sort) {
            showSortMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortMenu() {
        android.widget.PopupMenu menu = new android.widget.PopupMenu(this, findViewById(R.id.action_sort));
        getMenuInflater().inflate(R.menu.menu_sort, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            int sortId = item.getItemId();
            if (sortId == R.id.sort_oldest) {
                viewModel.setSortMode(SettingsRepository.SORT_OLDEST);
            } else if (sortId == R.id.sort_title) {
                viewModel.setSortMode(SettingsRepository.SORT_TITLE);
            } else if (sortId == R.id.sort_created) {
                viewModel.setSortMode(SettingsRepository.SORT_CREATED);
            } else {
                viewModel.setSortMode(SettingsRepository.SORT_RECENT);
            }
            return true;
        });
        menu.show();
    }

    // ---- ProjectsAdapter.Listener ----

    @Override
    public void onProjectClicked(Project project) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(ProjectDetailActivity.EXTRA_PROJECT_ID, project.id);
        startActivity(intent);
    }

    @Override
    public void onEditProject(Project project) {
        Intent intent = new Intent(this, EditProjectActivity.class);
        intent.putExtra(EditProjectActivity.EXTRA_PROJECT_ID, project.id);
        startActivity(intent);
    }

    @Override
    public void onToggleArchive(Project project) {
        project.archived = !project.archived;
        com.changelens.app.data.ProjectRepository.getInstance(this)
                .updateProject(project, () ->
                        Toast.makeText(this, project.archived
                                        ? R.string.project_archived : R.string.project_unarchived,
                                Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDeleteProject(Project project) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, project.title))
                .setPositiveButton(R.string.delete, (d, w) ->
                        com.changelens.app.data.ProjectRepository.getInstance(this)
                                .deleteProjectDeep(project.id, () ->
                                        Toast.makeText(this, R.string.project_deleted,
                                                Toast.LENGTH_SHORT).show()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
