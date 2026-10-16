package com.changelens.app.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.changelens.app.R;
import com.changelens.app.data.ProjectRepository;
import com.changelens.app.data.entity.PhotoEntry;
import com.changelens.app.data.entity.Project;
import com.changelens.app.databinding.ActivityProjectDetailBinding;
import com.changelens.app.databinding.DialogPhotoDetailsBinding;
import com.changelens.app.service.ReportExportService;
import com.changelens.app.util.AppExecutors;
import com.changelens.app.util.Formatters;
import com.changelens.app.util.ImageStore;
import com.changelens.app.util.LocaleHelper;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/** Project detail: header info, photo timeline, selection, compare, export. */
public class ProjectDetailActivity extends AppCompatActivity implements TimelineAdapter.Listener {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private ActivityProjectDetailBinding binding;
    private TimelineAdapter adapter;
    private Project currentProject;
    private Uri pendingPhotoUri;
    private File pendingCaptureFile;
    private long pendingCapturedAt;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pendingCapturedAt = System.currentTimeMillis();
                    showPhotoDetailsDialog(uri, null);
                }
            });

    private final ActivityResultLauncher<Uri> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && pendingCaptureFile != null && pendingCaptureFile.exists()) {
                    Uri contentUri = FileProvider.getUriForFile(this,
                            getPackageName() + ".fileprovider", pendingCaptureFile);
                    showPhotoDetailsDialog(contentUri, pendingCaptureFile);
                } else {
                    ImageStore.deleteFile(pendingCaptureFile != null
                            ? pendingCaptureFile.getAbsolutePath() : null);
                }
            });

    private final ActivityResultLauncher<String> cameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new TimelineAdapter(this);
        binding.timelineList.setLayoutManager(new LinearLayoutManager(this));
        binding.timelineList.setAdapter(adapter);

        long projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        if (projectId <= 0) {
            finish();
            return;
        }

        ProjectRepository repository = ProjectRepository.getInstance(this);
        repository.observeProject(projectId).observe(this, project -> {
            currentProject = project;
            renderHeader(project);
        });
        repository.observePhotos(projectId).observe(this, photos -> {
            adapter.submitList(photos);
            boolean empty = photos == null || photos.isEmpty();
            binding.emptyTimeline.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.timelineList.setVisibility(empty ? View.GONE : View.VISIBLE);
            updateSelectionUi();
        });

        binding.fabAddPhoto.setOnClickListener(v -> showAddPhotoDialog());
        binding.compareButton.setOnClickListener(v -> compareSelected());
        binding.clearSelectionButton.setOnClickListener(v -> {
            adapter.setSelectionMode(false);
            updateSelectionUi();
        });
        binding.exportReportButton.setOnClickListener(v -> exportReport());
    }

    private void renderHeader(@Nullable Project project) {
        if (project == null) {
            return;
        }
        binding.toolbar.setTitle(project.title);
        binding.projectTitle.setText(project.title);
        binding.projectDescription.setText(project.description);
        binding.projectDescription.setVisibility(
                project.description.isEmpty() ? View.GONE : View.VISIBLE);
        String dates = project.category + " Â· "
                + Formatters.formatDate(this, project.startDate)
                + (project.targetDate != null
                ? " â€“ " + Formatters.formatDate(this, project.targetDate) : "");
        binding.projectDates.setText(dates);
        binding.archiveBadge.setVisibility(project.archived ? View.VISIBLE : View.GONE);
        if (project.coverImage != null && !project.coverImage.isEmpty()) {
            AppExecutors.get().diskIO().execute(() -> {
                android.graphics.Bitmap bm = ImageStore.decodeSampled(project.coverImage, 1200, 800);
                AppExecutors.get().main(() -> {
                    if (bm != null && binding != null) {
                        binding.coverImage.setImageBitmap(bm);
                        binding.coverImage.setVisibility(View.VISIBLE);
                    }
                });
            });
        } else {
            binding.coverImage.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_project_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_project) {
            Intent intent = new Intent(this, EditProjectActivity.class);
            intent.putExtra(EditProjectActivity.EXTRA_PROJECT_ID, currentProject.id);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_archive_project) {
            toggleArchive();
            return true;
        }
        if (id == R.id.action_delete_project) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleArchive() {
        if (currentProject == null) {
            return;
        }
        currentProject.archived = !currentProject.archived;
        Project project = currentProject;
        ProjectRepository.getInstance(this).updateProject(project, () ->
                Toast.makeText(this, project.archived
                        ? R.string.project_archived : R.string.project_unarchived,
                        Toast.LENGTH_SHORT).show());
    }

    private void confirmDelete() {
        if (currentProject == null) {
            return;
        }
        Project project = currentProject;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, project.title))
                .setPositiveButton(R.string.delete, (d, w) ->
                        ProjectRepository.getInstance(this).deleteProjectDeep(project.id, () -> {
                            Toast.makeText(this, R.string.project_deleted, Toast.LENGTH_SHORT).show();
                            finish();
                        }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ---- Add photo ----

    private void showAddPhotoDialog() {
        CharSequence[] options = {
                getString(R.string.take_photo),
                getString(R.string.import_photo)
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_photo)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        ensureCameraThenCapture();
                    } else {
                        pickImage.launch("image/*");
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void ensureCameraThenCapture() {
        boolean hasCameraFeature = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        if (!hasCameraFeature) {
            Toast.makeText(this, R.string.camera_unavailable, Toast.LENGTH_LONG).show();
            return;
        }
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            launchCamera();
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            pendingCaptureFile = new ImageStore(this).newCaptureFile();
            pendingCapturedAt = System.currentTimeMillis();
            takePicture.launch(Uri.fromFile(pendingCaptureFile));
        } catch (Exception e) {
            Toast.makeText(this, R.string.camera_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void showPhotoDetailsDialog(@NonNull Uri sourceUri, @Nullable File captureFile) {
        DialogPhotoDetailsBinding dialogBinding =
                DialogPhotoDetailsBinding.inflate(getLayoutInflater());
        final long[] capturedAtHolder = {pendingCapturedAt};
        dialogBinding.dateButton.setText(
                getString(R.string.date_label) + ": " + Formatters.formatDate(this, capturedAtHolder[0]));
        dialogBinding.dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(capturedAtHolder[0]);
            new DatePickerDialog(this, (view, year, month, day) -> {
                Calendar picked = Calendar.getInstance();
                picked.set(year, month, day, 12, 0, 0);
                picked.set(Calendar.MILLISECOND, 0);
                capturedAtHolder[0] = picked.getTimeInMillis();
                pendingCapturedAt = capturedAtHolder[0];
                dialogBinding.dateButton.setText(
                        getString(R.string.date_label) + ": " + Formatters.formatDate(this, capturedAtHolder[0]));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_photo)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (d, w) -> {
                    if (captureFile != null) {
                        ImageStore.deleteFile(captureFile.getAbsolutePath());
                    }
                })
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String caption = dialogBinding.captionInput.getText() == null
                    ? "" : dialogBinding.captionInput.getText().toString();
            String notes = dialogBinding.notesInput.getText() == null
                    ? "" : dialogBinding.notesInput.getText().toString();
            long projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
            final long finalCapturedAt = pendingCapturedAt;
            dialog.dismiss();
            ProjectRepository.getInstance(ProjectDetailActivity.this).importPhoto(
                    projectId, sourceUri, caption, notes, finalCapturedAt, success -> {
                        if (captureFile != null) {
                            ImageStore.deleteFile(captureFile.getAbsolutePath());
                        }
                        if (isFinishing()) {
                            return;
                        }
                        Toast.makeText(ProjectDetailActivity.this,
                                success ? R.string.photo_saved : R.string.photo_import_failed,
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }

    // ---- Selection / compare ----

    @Override
    public void onPhotoClicked(PhotoEntry entry, int position) {
        Intent intent = new Intent(this, PhotoViewerActivity.class);
        intent.putExtra(PhotoViewerActivity.EXTRA_PHOTO_ID, entry.id);
        startActivity(intent);
    }

    @Override
    public void onPhotoLongClicked(PhotoEntry entry) {
        updateSelectionUi();
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        updateSelectionUi();
    }

    private void updateSelectionUi() {
        int count = adapter.selectedCount();
        boolean selecting = adapter.isSelectionMode() && count > 0;
        binding.selectionHint.setVisibility(adapter.isSelectionMode() ? View.VISIBLE : View.GONE);
        binding.compareButton.setEnabled(count == 2);
        binding.bottomBar.setVisibility(adapter.isSelectionMode() ? View.VISIBLE : View.GONE);
        if (count > 0) {
            binding.selectionHint.setText(getString(R.string.selected_count, count));
        } else {
            binding.selectionHint.setText(R.string.selection_hint);
        }
    }

    private void compareSelected() {
        List<PhotoEntry> selected = adapter.getSelectedPhotos();
        if (selected.size() != 2) {
            Toast.makeText(this, R.string.need_two_photos, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CompareActivity.class);
        intent.putExtra(CompareActivity.EXTRA_PHOTO_BEFORE, selected.get(0).id);
        intent.putExtra(CompareActivity.EXTRA_PHOTO_AFTER, selected.get(1).id);
        intent.putExtra(CompareActivity.EXTRA_PROJECT_ID,
                getIntent().getLongExtra(EXTRA_PROJECT_ID, -1));
        startActivity(intent);
    }

    private void exportReport() {
        long projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        ReportExportService.startExport(this, projectId);
        Toast.makeText(this, R.string.export_started, Toast.LENGTH_SHORT).show();
    }
}
