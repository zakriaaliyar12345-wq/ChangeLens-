package com.changelens.app.ui;

import android.app.Application;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.changelens.app.R;
import com.changelens.app.data.ProjectRepository;
import com.changelens.app.data.entity.Project;
import com.changelens.app.databinding.ActivityEditProjectBinding;
import com.changelens.app.util.AppExecutors;
import com.changelens.app.util.Formatters;
import com.changelens.app.util.ImageStore;
import com.changelens.app.util.LocaleHelper;
import com.changelens.app.util.ProjectValidator;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/** Create or edit a project with validated inputs (topics 04.2, 02.1). */
public class EditProjectActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private ActivityEditProjectBinding binding;
    private EditViewModel viewModel;
    private List<String> categories;
    private long startDate = System.currentTimeMillis();
    private Long targetDate;
    private Uri pendingCoverUri;
    private boolean loaded;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pendingCoverUri = uri;
                    binding.statusText.setText(uri.toString());
                }
            });

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        categories = Arrays.asList(getResources().getStringArray(R.array.project_categories));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categories);
        binding.categorySpinner.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(EditViewModel.class);

        long projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        if (projectId > 0) {
            binding.toolbar.setTitle(R.string.edit_project_title);
            viewModel.load(projectId, project -> {
                if (project == null || isFinishing()) {
                    return;
                }
                binding.titleInput.setText(project.title);
                binding.descriptionInput.setText(project.description);
                binding.categorySpinner.setSelection(
                        ProjectValidator.indexOfCategory(categories, project.category));
                startDate = project.startDate;
                targetDate = project.targetDate;
                pendingCoverUri = null;
                refreshDateLabels();
                loaded = true;
            });
        } else {
            refreshDateLabels();
        }

        binding.startDateButton.setOnClickListener(v -> pickDate(true));
        binding.endDateButton.setOnClickListener(v -> pickDate(false));
        binding.coverButton.setOnClickListener(v -> pickImage.launch("image/*"));
        binding.saveButton.setOnClickListener(v -> save());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Dates survive rotation through the ViewModel.
        startDate = viewModel.startDate;
        targetDate = viewModel.targetDate;
        refreshDateLabels();
    }

    private void pickDate(boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        long base = isStart ? startDate : (targetDate != null ? targetDate : startDate);
        calendar.setTimeInMillis(base);
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth, 12, 0, 0);
            picked.set(Calendar.MILLISECOND, 0);
            if (isStart) {
                startDate = picked.getTimeInMillis();
            } else {
                targetDate = picked.getTimeInMillis();
            }
            viewModel.startDate = startDate;
            viewModel.targetDate = targetDate;
            refreshDateLabels();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void refreshDateLabels() {
        binding.startDateButton.setText(
                getString(R.string.field_start_date) + ": " + Formatters.formatDate(this, startDate));
        binding.endDateButton.setText(getString(R.string.field_target_date)
                + ": " + (targetDate == null ? "â€”" : Formatters.formatDate(this, targetDate)));
    }

    private void save() {
        String title = binding.titleInput.getText() == null
                ? "" : binding.titleInput.getText().toString();
        String description = binding.descriptionInput.getText() == null
                ? "" : binding.descriptionInput.getText().toString();
        ProjectValidator.Result result =
                ProjectValidator.validate(title, description, startDate, targetDate);
        binding.titleLayout.setError(result.titleError == null ? null
                : resolveError(result.titleError));
        binding.descriptionLayout.setError(result.descriptionError == null ? null
                : resolveError(result.descriptionError));
        if (result.dateError != null) {
            Toast.makeText(this, resolveError(result.dateError), Toast.LENGTH_LONG).show();
            return;
        }
        if (!result.valid) {
            return;
        }

        String category = (String) binding.categorySpinner.getSelectedItem();
        binding.saveButton.setEnabled(false);

        long existingId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        if (existingId > 0) {
            viewModel.load(existingId, project -> {
                if (project == null) {
                    binding.saveButton.setEnabled(true);
                    Toast.makeText(this, R.string.error_no_project, Toast.LENGTH_SHORT).show();
                    return;
                }
                project.title = title.trim();
                project.description = description;
                project.category = category == null ? "" : category;
                project.startDate = startDate;
                project.targetDate = targetDate;
                applyCoverThenFinish(project);
            });
        } else {
            Project project = new Project();
            project.title = title.trim();
            project.description = description;
            project.category = category == null ? "" : category;
            project.createdAt = System.currentTimeMillis();
            project.updatedAt = project.createdAt;
            project.startDate = startDate;
            project.targetDate = targetDate;
            applyCoverThenFinish(project);
        }
    }

    private void applyCoverThenFinish(Project project) {
        if (pendingCoverUri != null) {
            Uri uri = pendingCoverUri;
            pendingCoverUri = null;
            AppExecutors.get().diskIO().execute(() -> {
                ImageStore.ImportResult imported = new ImageStore(this).importFromUri(uri);
                if (imported != null) {
                    project.coverImage = imported.thumbnailPath != null
                            ? imported.thumbnailPath : imported.imagePath;
                }
                finishWithSave(project);
            });
        } else {
            finishWithSave(project);
        }
    }

    private void finishWithSave(Project project) {
        ProjectRepository repo = ProjectRepository.getInstance(this);
        Runnable done = () -> {
            Toast.makeText(this, R.string.project_saved, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        };
        if (project.id > 0) {
            repo.updateProject(project, done);
        } else {
            repo.insertProject(project, id -> done.run());
        }
    }

    @NonNull
    private String resolveError(@NonNull String key) {
        switch (key) {
            case ProjectValidator.ERR_TITLE_LONG:
                return getString(R.string.error_title_too_long);
            case ProjectValidator.ERR_DESC_LONG:
                return getString(R.string.error_description_too_long);
            case ProjectValidator.ERR_DATE_ORDER:
                return getString(R.string.error_target_before_start);
            case ProjectValidator.ERR_TITLE_REQUIRED:
            default:
                return getString(R.string.error_title_required);
        }
    }

    /** Survives rotation with form state (topic 02.2). */
    public static class EditViewModel extends AndroidViewModel {
        long startDate = System.currentTimeMillis();
        Long targetDate;
        private final ProjectRepository repository;

        public EditViewModel(@NonNull Application application) {
            super(application);
            repository = ProjectRepository.getInstance(application);
        }

        void load(long id, ProjectRepository.Callback<Project> callback) {
            repository.getProjectBlocking(id, callback);
        }
    }
}
