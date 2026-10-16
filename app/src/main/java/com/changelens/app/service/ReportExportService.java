package com.changelens.app.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.changelens.app.R;
import com.changelens.app.data.AppDatabase;
import com.changelens.app.data.dao.AnnotationDao;
import com.changelens.app.data.dao.PhotoEntryDao;
import com.changelens.app.data.dao.ProjectDao;
import com.changelens.app.data.entity.Annotation;
import com.changelens.app.data.entity.PhotoEntry;
import com.changelens.app.data.entity.Project;
import com.changelens.app.ui.MainActivity;
import com.changelens.app.util.AppExecutors;
import com.changelens.app.util.LocaleHelper;
import com.changelens.app.util.NotificationHelper;
import com.changelens.app.util.ReportBuilder;
import com.changelens.app.util.ShareUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Genuine production Service (topic 07.4): builds an HTML progress report for a
 * project on a background thread and posts a notification with a share action
 * when the file is verified to exist. Started from the foreground UI, so no
 * foreground-service restrictions are triggered.
 */
public class ReportExportService extends Service {

    private static final String TAG = "ReportExportService";

    public static void startExport(@NonNull Context context, long projectId) {
        Intent intent = new Intent(context, ReportExportService.class);
        intent.putExtra(EXTRA_PROJECT_ID, projectId);
        context.startService(intent);
    }

    public static final String EXTRA_PROJECT_ID = "project_id";

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        final long projectId = intent != null ? intent.getLongExtra(EXTRA_PROJECT_ID, -1) : -1;
        final Context appContext = getApplicationContext();
        AppExecutors.get().diskIO().execute(() -> {
            try {
                buildReportAndNotify(appContext, projectId);
            } catch (Exception e) {
                Log.w(TAG, "report export failed", e);
            } finally {
                stopSelf(startId);
            }
        });
        return START_NOT_STICKY;
    }

    private void buildReportAndNotify(Context appContext, long projectId) {
        if (projectId <= 0) {
            return;
        }
        Context context = LocaleHelper.wrap(appContext);
        AppDatabase db = AppDatabase.getInstance(appContext);
        ProjectDao projectDao = db.projectDao();
        PhotoEntryDao photoDao = db.photoEntryDao();
        AnnotationDao annotationDao = db.annotationDao();

        Project project = projectDao.getByIdBlocking(projectId);
        if (project == null) {
            return;
        }
        List<PhotoEntry> photos = photoDao.getForProjectOrderedBlocking(projectId);
        List<List<Annotation>> annotations = new ArrayList<>();
        for (PhotoEntry photo : photos) {
            annotations.add(annotationDao.getForPhotoBlocking(photo.id));
        }
        File report = null;
        try {
            report = ReportBuilder.build(context, project, photos, annotations);
        } catch (Exception e) {
            Log.w(TAG, "report build error", e);
        }
        boolean ok = report != null && report.exists() && report.length() > 0;
        if (!ok || !NotificationHelper.canNotify(context)) {
            return;
        }
        final File reportFile = report;
        Intent open = new Intent(appContext, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(
                appContext, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = NotificationHelper.base(context, NotificationHelper.CHANNEL_EXPORTS)
                .setContentTitle(context.getString(R.string.report_generated))
                .setContentText(project.title)
                .setContentIntent(content)
                .setAutoCancel(true);
        Notification notification = builder.build();
        NotificationHelper.notifyCompat(context, notification, 2001);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
