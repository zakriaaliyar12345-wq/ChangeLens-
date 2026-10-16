package com.changelens.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;

import androidx.annotation.NonNull;

import com.changelens.app.R;
import com.changelens.app.data.entity.Annotation;
import com.changelens.app.data.entity.PhotoEntry;
import com.changelens.app.data.entity.Project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/** Builds a self-contained HTML progress report shareable offline. */
public final class ReportBuilder {

    private static final int REPORT_THUMB = 160;

    private ReportBuilder() {
    }

    @NonNull
    public static File build(@NonNull Context context,
                             @NonNull Project project,
                             @NonNull List<PhotoEntry> photos,
                             @NonNull List<List<Annotation>> annotationsPerPhoto) throws IOException {
        StringBuilder html = new StringBuilder();
        String title = escape(project.title);
        html.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>").append(title).append("</title>")
                .append("<style>")
                .append("body{font-family:sans-serif;margin:16px;color:#171D1B;background:#F6FBF8}")
                .append("h1{color:#00695C}h2{margin-top:24px}")
                .append(".card{background:#fff;border-radius:12px;padding:12px;margin:12px 0;box-shadow:0 1px 3px rgba(0,0,0,.12)}")
                .append(".meta{color:#3F4946;font-size:14px}")
                .append("img{max-width:100%;border-radius:8px;margin-top:8px}")
                .append(".ann{background:#FFF3E0;border-inline-start:4px solid #FF8F00;padding:6px 10px;margin:6px 0;border-radius:6px}")
                .append("</style></head><body dir=\"auto\">");

        html.append("<h1>").append(title).append("</h1>");
        if (!project.description.isEmpty()) {
            html.append("<p>").append(escape(project.description)).append("</p>");
        }
        html.append("<p class=\"meta\">")
                .append(escape(context.getString(R.string.field_category))).append(": ")
                .append(escape(project.category))
                .append(" &middot; ")
                .append(escape(context.getString(R.string.field_start_date))).append(": ")
                .append(Formatters.isoDate(project.startDate));
        if (project.targetDate != null) {
            html.append(" &middot; ")
                    .append(escape(context.getString(R.string.field_target_date))).append(": ")
                    .append(Formatters.isoDate(project.targetDate));
        }
        html.append("</p>");
        html.append("<p class=\"meta\">").append(photos.size()).append(" ")
                .append(escape(context.getResources().getQuantityString(
                        R.plurals.photo_count, photos.size(), photos.size())))
                .append("</p>");

        html.append("<h2>").append(escape(context.getString(R.string.timeline_title))).append("</h2>");
        int index = 0;
        for (PhotoEntry photo : photos) {
            html.append("<div class=\"card\">");
            html.append("<div class=\"meta\"><strong>")
                    .append(Formatters.isoDate(photo.capturedAt)).append("</strong>");
            if (!photo.caption.isEmpty()) {
                html.append(" &mdash; ").append(escape(photo.caption));
            }
            html.append("</div>");
            String thumb = thumbnailDataUri(photo);
            if (thumb != null) {
                html.append("<img src=\"").append(thumb).append("\" alt=\"\">");
            }
            if (!photo.notes.isEmpty()) {
                html.append("<p>").append(escape(photo.notes)).append("</p>");
            }
            if (index < annotationsPerPhoto.size()) {
                for (Annotation a : annotationsPerPhoto.get(index)) {
                    html.append("<div class=\"ann\">")
                            .append(escape(a.text))
                            .append(" (")
                            .append(String.format(Locale.US, "%.0f%%, %.0f%%", a.normX * 100, a.normY * 100))
                            .append(")</div>");
                }
            }
            html.append("</div>");
            index++;
        }
        html.append("<p class=\"meta\">").append(escape(context.getString(R.string.privacy_title)))
                .append(" — ").append(escape(context.getString(R.string.app_name))).append("</p>");
        html.append("</body></html>");

        File dir = new ImageStore(context).reportsDir();
        File out = new File(dir, "report_" + project.id + "_" + System.currentTimeMillis() + ".html");
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return out;
    }

    private static String thumbnailDataUri(PhotoEntry photo) {
        String path = photo.thumbnailPath != null ? photo.thumbnailPath : photo.imagePath;
        Bitmap bitmap = ImageStore.decodeSampled(path, REPORT_THUMB, REPORT_THUMB);
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
        bitmap.recycle();
        byte[] bytes = bos.toByteArray();
        String encoded;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            encoded = Base64.getEncoder().encodeToString(bytes);
        } else {
            encoded = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
        }
        return "data:image/jpeg;base64," + encoded;
    }

    static String escape(String s) {
        return Html.escapeHtml(s);
    }
}
