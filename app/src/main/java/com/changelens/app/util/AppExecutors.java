package com.changelens.app.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** Production background processing (replaces deprecated AsyncTask). */
public final class AppExecutors {

    private static final AppExecutors INSTANCE = new AppExecutors();

    private final Executor diskIO = Executors.newSingleThreadExecutor();
    private final Executor networkIO = Executors.newFixedThreadPool(2);
    private final Handler main = new Handler(Looper.getMainLooper());

    private AppExecutors() {
    }

    public static AppExecutors get() {
        return INSTANCE;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public void main(@NonNull Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            main.post(runnable);
        }
    }

    public void disk(@NonNull Runnable runnable, @NonNull Runnable onDone) {
        diskIO.execute(() -> {
            try {
                runnable.run();
            } finally {
                main.post(onDone);
            }
        });
    }
}
