package com.changelens.app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.changelens.app.R;
import com.changelens.app.data.ProjectRepository;
import com.changelens.app.data.entity.PhotoEntry;
import com.changelens.app.databinding.ActivityPhotoViewerBinding;
import com.changelens.app.util.AppExecutors;
import com.changelens.app.util.Formatters;
import com.changelens.app.util.ImageStore;
import com.changelens.app.util.LocaleHelper;

/** Full-screen photo inspection with pinch zoom (topics 04.1, 04.4). */
public class PhotoViewerActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO_ID = "photo_id";

    private ActivityPhotoViewerBinding binding;
    private float scale = 1f;
    private ScaleGestureDetector scaleDetector;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhotoViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setNavigationIcon(R.drawable.ic_back);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        scaleDetector = new ScaleGestureDetector(this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        scale = Math.max(1f, Math.min(5f, scale * detector.getScaleFactor()));
                        binding.photoView.setScaleX(scale);
                        binding.photoView.setScaleY(scale);
                        return true;
                    }
                });

        binding.photoView.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            return true;
        });

        long photoId = getIntent().getLongExtra(EXTRA_PHOTO_ID, -1);
        ProjectRepository.getInstance(this).getPhotoBlocking(photoId, entry -> {
            if (entry == null || isFinishing()) {
                Toast.makeText(this, R.string.error_file_missing, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            binding.toolbar.setTitle(entry.caption.isEmpty()
                    ? Formatters.formatDate(this, entry.capturedAt) : entry.caption);
            binding.photoMeta.setText(Formatters.formatDateTime(this, entry.capturedAt)
                    + (entry.notes.isEmpty() ? "" : " — " + entry.notes));
            AppExecutors.get().diskIO().execute(() -> {
                Bitmap bm = ImageStore.decodeSampled(entry.imagePath,
                        ImageStore.VIEWER_MAX, ImageStore.VIEWER_MAX);
                AppExecutors.get().main(() -> {
                    if (isFinishing()) {
                        return;
                    }
                    if (bm == null) {
                        binding.photoView.setImageResource(R.drawable.ic_picture);
                        Toast.makeText(this, R.string.photo_missing, Toast.LENGTH_SHORT).show();
                    } else {
                        binding.photoView.setImageBitmap(bm);
                    }
                });
            });
        });

        binding.photoView.setOnClickListener(v -> toggleSystemUi());
    }

    private void toggleSystemUi() {
        View decor = getWindow().getDecorView();
        int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if ((decor.getSystemUiVisibility() & flags) == 0) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }
}
