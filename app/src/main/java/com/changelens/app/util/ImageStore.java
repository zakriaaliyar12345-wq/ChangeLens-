package com.changelens.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Copies user photos into private app storage and produces small thumbnails
 * so the UI can render timelines without loading full-size bitmaps.
 * Originals supplied by the user are never modified in place.
 */
public class ImageStore {

    private static final String TAG = "ImageStore";
    public static final int THUMBNAIL_MAX = 320;
    public static final int VIEWER_MAX = 2048;
    public static final int EXPORT_MAX = 2000;

    public static class ImportResult {
        public final String imagePath;
        @Nullable
        public final String thumbnailPath;

        ImportResult(String imagePath, @Nullable String thumbnailPath) {
            this.imagePath = imagePath;
            this.thumbnailPath = thumbnailPath;
        }
    }

    private final Context context;

    public ImageStore(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    public File imagesDir() {
        File dir = new File(context.getFilesDir(), "images");
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    public File thumbsDir() {
        File dir = new File(context.getFilesDir(), "thumbs");
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    public File exportsDir() {
        File base = context.getExternalFilesDir(null);
        File dir = new File(base != null ? base : context.getFilesDir(), "Documents/exports");
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    public File reportsDir() {
        File base = context.getExternalFilesDir(null);
        File dir = new File(base != null ? base : context.getFilesDir(), "Documents/reports");
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    public File captureCacheDir() {
        File dir = new File(context.getCacheDir(), "capture");
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    /** Creates an empty file for ACTION_IMAGE_CAPTURE output (cleared after import). */
    @NonNull
    public File newCaptureFile() throws IOException {
        return File.createTempFile("capture_", ".jpg", captureCacheDir());
    }

    @Nullable
    public ImportResult importFromUri(@NonNull Uri uri) {
        String name = "img_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        File dest = new File(imagesDir(), name + ".jpg");
        try {
            copyUriToFile(uri, dest);
            String thumbPath = createThumbnail(dest.getAbsolutePath(), new File(thumbsDir(), name + "_t.jpg"));
            return new ImportResult(dest.getAbsolutePath(), thumbPath);
        } catch (OutOfMemoryError | Exception e) {
            Log.w(TAG, "import failed: " + e.getMessage());
            //noinspection ResultOfMethodCallIgnored
            dest.delete();
            return null;
        }
    }

    private void copyUriToFile(@NonNull Uri uri, @NonNull File dest) throws IOException {
        InputStream in = context.getContentResolver().openInputStream(uri);
        if (in == null) {
            throw new IOException("Cannot open " + uri);
        }
        try (OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Nullable
    public String createThumbnail(@NonNull String sourcePath, @NonNull File thumbFile) {
        Bitmap bitmap = decodeSampled(sourcePath, THUMBNAIL_MAX, THUMBNAIL_MAX);
        if (bitmap == null) {
            return null;
        }
        try (FileOutputStream out = new FileOutputStream(thumbFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            return thumbFile.getAbsolutePath();
        } catch (IOException e) {
            Log.w(TAG, "thumb write failed: " + e.getMessage());
            return null;
        } finally {
            bitmap.recycle();
        }
    }

    /** Decodes a file downsampled to at most maxW x maxH, preserving aspect ratio. */
    @Nullable
    public static Bitmap decodeSampled(@NonNull String path, int maxW, int maxH) {
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(new File(path));
                return ImageDecoder.decodeBitmap(source, (decoder, info, s) -> {
                    int[] max = maxFor(info.getSize().getWidth(), info.getSize().getHeight(), maxW, maxH);
                    decoder.setTargetSampleSize(Math.max(1, info.getSize().getWidth() / max[0]));
                    decoder.setMutableRequired(true);
                });
            }
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxW, maxH);
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeFile(path, opts);
        } catch (OutOfMemoryError oom) {
            Log.w(TAG, "OOM decoding " + path);
            System.gc();
            return decodeSampled(path, maxW / 2, maxH / 2);
        } catch (Exception e) {
            Log.w(TAG, "decode failed: " + e.getMessage());
            return null;
        }
    }

    @NonNull
    private static int[] maxFor(int w, int h, int maxW, int maxH) {
        float scale = Math.min((float) maxW / w, (float) maxH / h);
        if (scale >= 1f) {
            return new int[]{w, h};
        }
        return new int[]{Math.max(1, (int) (w * scale)), Math.max(1, (int) (h * scale))};
    }

    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public void deleteManagedFiles(@Nullable String imagePath, @Nullable String thumbPath) {
        deleteFile(imagePath);
        deleteFile(thumbPath);
    }

    public static void deleteFile(@Nullable String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    /** Deletes thumbnails in the thumbs dir whose base name is not referenced. */
    public int cleanupOrphanThumbs(@NonNull java.util.Set<String> referencedThumbPaths) {
        int removed = 0;
        File[] files = thumbsDir().listFiles();
        if (files == null) {
            return 0;
        }
        for (File f : files) {
            if (!referencedThumbPaths.contains(f.getAbsolutePath())) {
                if (f.delete()) {
                    removed++;
                }
            }
        }
        return removed;
    }
}
