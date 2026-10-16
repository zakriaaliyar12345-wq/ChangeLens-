package com.changelens.app.education;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import java.util.ArrayList;
import java.util.List;

/**
 * EDUCATIONAL DEMONSTRATION ONLY (topic 07.1).
 * Hosts a legacy AsyncTaskLoader through LoaderManager to demonstrate the
 * managed-loader lifecycle (onStartLoading / onLoadFinished / onLoaderReset).
 * Production code observes Room LiveData instead.
 */
public class LegacyLoaderDemo implements LoaderManager.LoaderCallbacks<List<String>> {

    public static final int LOADER_ID = 7001;

    public interface Callback {
        void onLoaded(List<String> tips);
    }

    private final androidx.fragment.app.FragmentActivity host;
    private final Callback callback;

    public LegacyLoaderDemo(androidx.fragment.app.FragmentActivity host, Callback callback) {
        this.host = host;
        this.callback = callback;
    }

    public void start() {
        LoaderManager.getInstance(host).initLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<List<String>> onCreateLoader(int id, @Nullable Bundle args) {
        return new TipsLoader(host);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<String>> loader, List<String> data) {
        if (callback != null) {
            callback.onLoaded(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<String>> loader) {
    }

    static class TipsLoader extends AsyncTaskLoader<List<String>> {
        TipsLoader(Context context) {
            super(context);
        }

        @Nullable
        @Override
        public List<String> loadInBackground() {
            List<String> tips = new ArrayList<>();
            tips.add("Tip: shoot from the same spot each time for the best comparisons.");
            tips.add("Tip: add captions on the day you take the photo.");
            tips.add("Tip: use the overlay mode to spot subtle changes.");
            tips.add("Tip: archive finished projects to keep the dashboard tidy.");
            return tips;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }
}
