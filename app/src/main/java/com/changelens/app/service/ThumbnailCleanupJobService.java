package com.changelens.app.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.Nullable;

import com.changelens.app.data.AppDatabase;
import com.changelens.app.data.dao.PhotoEntryDao;
import com.changelens.app.util.AppExecutors;
import com.changelens.app.util.ImageStore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deferred background maintenance (topic 08.3): periodically removes thumbnail
 * files that are no longer referenced by the database. Scheduled with
 * JobScheduler; a WorkManager worker would be the modern alternative for more
 * complex constraint chains, documented in COURSE_TOPIC_MAPPING.md.
 */
public class ThumbnailCleanupJobService extends JobService {

    public static final int JOB_ID = 7101;
    private static final long INTERVAL_MS = 6L * 60L * 60L * 1000L; // 6 hours

    private volatile boolean running = false;

    public static void scheduleIfNeeded(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            return;
        }
        for (JobInfo info : scheduler.getAllPendingJobs()) {
            if (info.getId() == JOB_ID) {
                return;
            }
        }
        ComponentName componentName = new ComponentName(context, ThumbnailCleanupJobService.class);
        JobInfo job = new JobInfo.Builder(JOB_ID, componentName)
                .setPeriodic(INTERVAL_MS)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .build();
        scheduler.schedule(job);
    }

    public static void cancel(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) {
            scheduler.cancel(JOB_ID);
        }
    }

    @Override
    public boolean onStartJob(@Nullable JobParameters params) {
        running = true;
        AppExecutors.get().diskIO().execute(() -> {
            int removed = 0;
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                PhotoEntryDao dao = db.photoEntryDao();
                List<String> refs = dao.allThumbnailPathsBlocking();
                Set<String> referenced = new HashSet<>(refs);
                for (String s : refs) {
                    if (s != null) {
                        referenced.add(s);
                    }
                }
                ImageStore store = new ImageStore(getApplicationContext());
                removed = store.cleanupOrphanThumbs(referenced);
            } finally {
                running = false;
                jobFinished(params, false);
            }
        });
        return true;
    }

    @Override
    public boolean onStopJob(@Nullable JobParameters params) {
        running = false;
        return true;
    }
}
