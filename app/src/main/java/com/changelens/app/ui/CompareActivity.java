package com.changelens.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.changelens.app.R;
import com.changelens.app.data.ProjectRepository;
import com.changelens.app.data.SettingsRepository;
import com.changelens.app.data.entity.Annotation;
import com.changelens.app.data.entity.PhotoEntry;
import com.changelens.app.data.entity.Project;
import com.changelens.app.databinding.ActivityCompareBinding;
import com.changelens.app.databinding.DialogAnnotationBinding;
import com.changelens.app.util.AppExecutors;
import com.changelens.app.util.ExportUtils;
import com.changelens.app.util.Formatters;
import com.changelens.app.util.ImageStore;
import com.changelens.app.util.LocaleHelper;
import com.changelens.app.util.ShareUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Before/after comparison with slider and overlay modes, annotations,
 * export, and sharing (core product workflow).
 */
public class CompareActivity extends AppCompatActivity implements AnnotationsAdapter.Listener {

    public static final String EXTRA_PHOTO_BEFORE = "photo_before";
    public static final String EXTRA_PHOTO_AFTER = "photo_after";
    public static final String EXTRA_PROJECT_ID = "project_id";

    private ActivityCompareBinding binding;
    private ProjectRepository repository;
    private PhotoEntry before;
    private PhotoEntry after;
    private List<Annotation> annotations = new ArrayList<>();
    private AnnotationsAdapter annotationsAdapter;
    private boolean annotateMode = false;
    private Project project;
    private boolean rtl;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        rtl = LocaleHelper.isRtl(this);

        repository = ProjectRepository.getInstance(this);
        annotationsAdapter = new AnnotationsAdapter(this);
        binding.annotationsList.setLayoutManager(new LinearLayoutManager(this));
        binding.annotationsList.setAdapter(annotationsAdapter);

        String defaultMode = new SettingsRepository(this).getDefaultComparisonMode();
        if (SettingsRepository.MODE_OVERLAY.equals(defaultMode)) {
            binding.modeGroup.check(R.id.modeOverlay);
        } else {
            binding.modeGroup.check(R.id.modeSlider);
        }
        applyMode(binding.modeGroup.getCheckedButtonId() == R.id.modeOverlay
                ? CompareView.MODE_OVERLAY : CompareView.MODE_SLIDER);

        binding.modeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                applyMode(checkedId == R.id.modeOverlay
                        ? CompareView.MODE_OVERLAY : CompareView.MODE_SLIDER);
            }
        });

        binding.swapButton.setOnClickListener(v -> swapImages());
        binding.annotateButton.setOnClickListener(v -> startAnnotation());
        binding.exportImageButton.setOnClickListener(v -> exportImage());

        binding.adjustSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                float fraction = progress / (float) seekBar.getMax();
                if (binding.compareView.getMode() == CompareView.MODE_OVERLAY) {
                    binding.compareView.setOverlayAlpha(fraction);
                } else {
                    binding.compareView.setDivider(fraction);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        binding.compareView.setOnDividerChangedListener(fraction -> {
            binding.adjustSeekBar.setProgress(Math.round(fraction * binding.adjustSeekBar.getMax()));
        });

        binding.compareView.setOnTouchListener((v, event) -> {
            if (annotateMode) {
                if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    placeAnnotation(event.getX(), event.getY());
                }
                return true;
            }
            return binding.compareView.onTouchEvent(event);
        });

        binding.beforeDate.setOnClickListener(v -> openViewer(before));
        binding.afterDate.setOnClickListener(v -> openViewer(after));

        long beforeId = getIntent().getLongExtra(EXTRA_PHOTO_BEFORE, -1);
        long afterId = getIntent().getLongExtra(EXTRA_PHOTO_AFTER, -1);
        long projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        if (projectId > 0) {
            repository.observeProject(projectId).observe(this, p -> project = p);
        }
        loadPhoto(beforeId, true);
        loadPhoto(afterId, false);
        loadAnnotationsForBoth(beforeId, afterId);
    }

    private void loadPhoto(long id, boolean isBefore) {
        repository.getPhotoBlocking(id, entry -> {
            if (entry == null) {
                Toast.makeText(this, R.string.error_file_missing, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (isBefore) {
                before = entry;
                binding.beforeDate.setText(Formatters.formatDate(this, entry.capturedAt));
            } else {
                after = entry;
                binding.afterDate.setText(Formatters.formatDate(this, entry.capturedAt));
            }
            decodeInto(entry, isBefore);
        });
    }

    private void decodeInto(PhotoEntry entry, boolean isBefore) {
        AppExecutors.get().diskIO().execute(() -> {
            Bitmap bm = ImageStore.decodeSampled(entry.imagePath,
                    ImageStore.EXPORT_MAX, ImageStore.EXPORT_MAX);
            AppExecutors.get().main(() -> {
                if (isFinishing() || bm == null) {
                    if (bm == null) {
                        Toast.makeText(this, R.string.error_image_decode, Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                if (isBefore) {
                    binding.compareView.setBefore(bm);
                } else {
                    binding.compareView.setAfter(bm);
                }
            });
        });
    }

    private void loadAnnotationsForBoth(long beforeId, long afterId) {
        // Annotations are stored on the "after" photo by default.
        repository.observeAnnotations(afterId).observe(this, list -> {
            annotations = list == null ? new ArrayList<>() : list;
            annotationsAdapter.submitList(annotations);
            binding.compareView.setAnnotations(annotations);
            binding.noAnnotations.setVisibility(
                    annotations.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void applyMode(int mode) {
        binding.compareView.setMode(mode);
        if (mode == CompareView.MODE_OVERLAY) {
            binding.adjustHint.setText(R.string.overlay_opacity);
            binding.adjustSeekBar.setProgress(
                    Math.round(binding.compareView.getOverlayAlpha() * binding.adjustSeekBar.getMax()));
        } else {
            binding.adjustHint.setText(R.string.divider_position);
            binding.adjustSeekBar.setProgress(
                    Math.round(binding.compareView.getDivider() * binding.adjustSeekBar.getMax()));
        }
    }

    private void swapImages() {
        binding.compareView.swapImages();
        PhotoEntry tmp = before;
        before = after;
        after = tmp;
        binding.beforeDate.setText(Formatters.formatDate(this, before.capturedAt));
        binding.afterDate.setText(Formatters.formatDate(this, after.capturedAt));
    }

    private void openViewer(@Nullable PhotoEntry entry) {
        if (entry == null) {
            return;
        }
        Intent intent = new Intent(this, PhotoViewerActivity.class);
        intent.putExtra(PhotoViewerActivity.EXTRA_PHOTO_ID, entry.id);
        startActivity(intent);
    }

    // ---- Annotations ----

    private void startAnnotation() {
        DialogAnnotationBinding dialogBinding =
                DialogAnnotationBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.annotate)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String text = dialogBinding.annotationInput.getText() == null
                    ? "" : dialogBinding.annotationInput.getText().toString().trim();
            if (text.isEmpty()) {
                dialogBinding.annotationInput.setError(getString(R.string.error_title_required));
                return;
            }
            pendingAnnotationText = text;
            annotateMode = true;
            binding.annotateButton.setText(R.string.adding_annotation);
            dialog.dismiss();
        });
    }

    private String pendingAnnotationText;

    private void placeAnnotation(float x, float y) {
        annotateMode = false;
        binding.annotateButton.setText(R.string.annotate);
        if (after == null || binding.compareView.getWidth() == 0) {
            return;
        }
        Annotation annotation = new Annotation();
        annotation.photoId = after.id;
        annotation.text = pendingAnnotationText == null ? "" : pendingAnnotationText;
        annotation.normX = Math.max(0f, Math.min(1f, x / binding.compareView.getWidth()));
        annotation.normY = Math.max(0f, Math.min(1f, y / binding.compareView.getHeight()));
        annotation.createdAt = System.currentTimeMillis();
        repository.addAnnotation(annotation, id ->
                Toast.makeText(this, R.string.annotation_added, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDeleteAnnotation(Annotation annotation) {
        repository.deleteAnnotation(annotation.id, () ->
                Toast.makeText(this, R.string.annotation_deleted, Toast.LENGTH_SHORT).show());
    }

    // ---- Export ----

    private void exportImage() {
        if (binding.compareView.getBefore() == null && binding.compareView.getAfter() == null) {
            Toast.makeText(this, R.string.need_six_two_photos, Toast.LENGTH_SHORT).show();
            return;
        }
        binding.exportImageButton.setEnabled(false);
        final int width = 1600;
        final int height = 1200;
        AppExecutors.get().diskIO().execute(() -> {
            CompareView renderView = new CompareView(this);
            renderView.setMode(binding.compareView.getMode());
            renderView.setDivider(binding.compareView.getDivider());
            renderView.setOverlayAlpha(binding.compareView.getOverlayAlpha());
            renderView.setAnnotations(new ArrayList<>(annotations));
            renderView.setBefore(binding.compareView.getBefore());
            renderView.setAfter(binding.compareView.getAfter());
            Bitmap comparison = ExportUtils.renderCompareView(renderView, width, height);
            String title = project != null ? project.title : getString(R.string.app_name);
            String beforeDate = Formatters.formatDate(this,
                    before != null ? before.capturedAt : System.currentTimeMillis());
            String afterDate = Formatters.formatDate(this,
                    after != null ? after.capturedAt : System.currentTimeMillis());
            Bitmap stamped = ExportUtils.stampLabels(this, comparison, title, beforeDate, afterDate, rtl);
            File file = ExportUtils.saveJpeg(this, stamped,
                    ExportUtils.uniqueName("comparison"));
            AppExecutors.get().main(() -> {
                binding.exportImageButton.setEnabled(true);
                if (file == null) {
                    Toast.makeText(this, R.string.export_failed, Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(this, R.string.export_image_done, Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(this)
                        .setTitle(R.string.export_image_done)
                        .setMessage(file.getAbsolutePath())
                        .setPositiveButton(R.string.share, (d, w) ->
                                ShareUtils.shareImage(this, file, getString(R.string.share_image)))
                        .setNegativeButton(R.string.ok, null)
                        .show();
            });
        });
    }
}
