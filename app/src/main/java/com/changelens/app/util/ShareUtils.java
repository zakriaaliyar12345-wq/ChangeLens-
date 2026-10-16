package com.changelens.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.changelens.app.R;

import java.io.File;

/** Native Android sharing via content URIs granted by FileProvider (topic 02.3). */
public final class ShareUtils {

    public static final String AUTHORITY_SUFFIX = ".fileprovider";

    private ShareUtils() {
    }

    @NonNull
    public static Uri uriForFile(@NonNull Context context, @NonNull File file) {
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + AUTHORITY_SUFFIX,
                file);
    }

    public static void shareImage(@NonNull Context context, @NonNull File file, @NonNull String title) {
        Uri uri = uriForFile(context, file);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("image/jpeg");
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(send, title));
    }

    public static void shareTextFile(@NonNull Context context, @NonNull File file,
                                     @NonNull String mimeType, @NonNull String title) {
        Uri uri = uriForFile(context, file);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType(mimeType);
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(send, title));
    }

    public static void openFile(@NonNull Context context, @NonNull File file, @NonNull String mimeType) {
        Uri uri = uriForFile(context, file);
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(uri, mimeType);
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(view, context.getString(R.string.report_open)));
    }
}
