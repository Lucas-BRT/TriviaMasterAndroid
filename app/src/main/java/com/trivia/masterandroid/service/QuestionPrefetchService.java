package com.trivia.masterandroid.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.data.remote.RemoteDataSource;
import com.trivia.masterandroid.data.repo.TriviaException;
import com.trivia.masterandroid.model.Question;
import com.trivia.masterandroid.util.AppExecutors;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Started service that downloads a batch of mixed questions in the background and stores them
 * in the local cache, so matches stay playable when the connection drops.
 * Skips the download when the cache already holds {@link #CACHE_TARGET} questions.
 */
public class QuestionPrefetchService extends Service {

    private static final String TAG = "PrefetchService";
    private static final int CACHE_TARGET = 100;
    private static final int BATCH = 50;
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!RUNNING.compareAndSet(false, true)) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }
        LocalDataSource local = new LocalDataSource(this);
        AppExecutors.io(() -> {
            try {
                if (local.countCachedQuestions() < CACHE_TARGET) {
                    List<Question> questions = new RemoteDataSource().fetchQuestions(BATCH, 0, null);
                    local.cacheQuestions(questions, 0);
                    Log.i(TAG, "Cached " + questions.size() + " questions");
                }
            } catch (TriviaException e) {
                Log.w(TAG, "Prefetch skipped: " + e.type);
            } finally {
                RUNNING.set(false);
                stopSelf(startId);
            }
        });
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
