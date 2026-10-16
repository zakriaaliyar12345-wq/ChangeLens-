package com.changelens.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.changelens.app.data.entity.Project;
import com.changelens.app.ui.CompareView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Renders comparison images and writes progress reports.
 * A comparison image is produced by measuring and drawing the same
 * {@link CompareView} used on screen, so exports always match what the user sees.
 */
public final class ExportUtils {

    private ExportUtils() {
    }

    @NonNull
    public static Bitmap renderCompareView(@NonNull CompareView view, int width, int height) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /** Draws project title and date labels above the rendered comparison. */
    @NonNull
    public static Bitmap stampLabels(@NonNull Context context, @NonNull Bitmap comparison,
                                     @NonNull String title, @NonNull String beforeDate,
                                     @NonNull String afterDate, boolean rtl) {
        int pad = (int) (16 * context.getResources().getDisplayMetrics().density);
        int labelBand = pad * 3 + (int) (18 * context.getResources().getDisplayMetrics().density * 2);
        Bitmap out = Bitmap.createBitmap(
                comparison.getWidth(),
                comparison.getHeight() + labelBand,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.drawColor(Color.parseColor("#101418"));

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(16 * context.getResources().getDisplayMetrics().density);
        Paint titlePaint = new Paint(textPaint);
        titlePaint.setFakeBoldText(true);

        float titleY = pad + textPaint.getTextSize();
        if (rtl) {
            canvas.drawText(title, comparison.getWidth() - pad, titleY, titlePaint);
        } else {
            canvas.drawText(title, pad, titleY, titlePaint);
        }
        float dateY = titleY + textPaint.getTextSize() + pad / 2f;
        String beforeLabel = context.getString(com.changelens.app.R.string.before_label) + ": " + beforeDate;
        String afterLabel = context.getString(com.changelens.app.R.string.after_label) + ": " + afterDate;
        if (rtl) {
            canvas.drawText(afterLabel, comparison.getWidth() - pad, dateY, textPaint);
            canvas.drawText(beforeLabel, comparison.getWidth() / 2f, dateY, textPaint);
        } else {
            canvas.drawText(beforeLabel, pad, dateY, textPaint);
            canvas.drawText(afterLabel, comparison.getWidth() / 2f, dateY, textPaint);
        }
        canvas.drawBitmap(comparison, 0, labelBand, null);
        return out;
    }

    @Nullable
    public static File saveJpeg(@NonNull Context context, @NonNull Bitmap bitmap, @NonNull String name) {
        File dir = new ImageStore(context).exportsDir();
        File out = new File(dir, name + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(out)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos);
            fos.flush();
            return out.exists() ? out : null;
        } catch (IOException | OutOfMemoryError e) {
            return null;
        }
    }

    public static String uniqueName(@NonNull String prefix) {
        return prefix + "_" + System.currentTimeMillis()
                + "_" + ((int) (Math.random() * 10000));
    }
}
