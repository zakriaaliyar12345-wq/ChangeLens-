package com.changelens.app.education;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * EDUCATIONAL DEMONSTRATION ONLY (topic 07.1).
 * AsyncTask is deprecated; production ChangeLens code uses {@code AppExecutors}.
 * This class is isolated from all production paths and exists solely so the
 * legacy API can be studied and demonstrated in the Course demos screen.
 */
@SuppressWarnings({"deprecation", "unchecked"})
public class LegacyAsyncTaskDemo extends AsyncTask<Void, Integer, String> {

    public interface Callback {
        void onProgress(int percent);

        void onFinished(String result);
    }

    private static final String TAG = "LegacyAsyncTaskDemo";
    private final Callback callback;
    private final Context appContext;

    public LegacyAsyncTaskDemo(Context context, Callback callback) {
        this.appContext = context.getApplicationContext();
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "legacy AsyncTask started (educational only)");
    }

    @Override
    protected String doInBackground(Void... voids) {
        // Simulated background work: thumbnail crunching stand-in.
        for (int i = 1; i <= 5; i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            publishProgress(i * 20);
        }
        return "finished-" + appContext.getPackageName();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (callback != null && values.length > 0) {
            callback.onProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (callback != null) {
            callback.onFinished(result);
        }
    }
}
