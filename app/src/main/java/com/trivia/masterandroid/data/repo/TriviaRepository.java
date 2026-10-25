package com.trivia.masterandroid.data.repo;

import android.content.Context;

import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.data.remote.RemoteDataSource;
import com.trivia.masterandroid.model.GameSettings;
import com.trivia.masterandroid.model.Question;
import com.trivia.masterandroid.util.AppExecutors;

import java.util.List;

/**
 * Single entry point for questions: tries the API first and, on connection problems,
 * falls back to the questions cached in SQLite (through the ContentProvider).
 */
public class TriviaRepository {

    public interface Callback<T> {
        void onSuccess(T result, boolean fromCache);

        void onError(TriviaException error);
    }

    private final RemoteDataSource remote = new RemoteDataSource();
    private final LocalDataSource local;

    public TriviaRepository(Context context) {
        this.local = new LocalDataSource(context);
    }

    public void loadQuestions(GameSettings s, Callback<List<Question>> callback) {
        AppExecutors.io(() -> {
            try {
                List<Question> questions = remote.fetchQuestions(s.amount, s.categoryId, s.difficulty);
                local.cacheQuestions(questions, s.categoryId);
                AppExecutors.main(() -> callback.onSuccess(questions, false));
            } catch (TriviaException e) {
                if (canUseCache(e)) {
                    List<Question> cached = local.getCachedQuestions(s.categoryId, s.difficulty, s.amount);
                    if (!cached.isEmpty()) {
                        AppExecutors.main(() -> callback.onSuccess(cached, true));
                        return;
                    }
                    if (e.type == TriviaException.Type.NO_CONNECTION || e.type == TriviaException.Type.TIMEOUT) {
                        AppExecutors.main(() -> callback.onError(
                                new TriviaException(TriviaException.Type.NO_OFFLINE_DATA, e)));
                        return;
                    }
                }
                AppExecutors.main(() -> callback.onError(e));
            }
        });
    }

    private static boolean canUseCache(TriviaException e) {
        switch (e.type) {
            case NO_CONNECTION:
            case TIMEOUT:
            case SERVER:
            case RATE_LIMIT:
            case NO_RESULTS:
                return true;
            default:
                return false;
        }
    }
}
