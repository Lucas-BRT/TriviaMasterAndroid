package com.trivia.masterandroid.data.local;

import android.net.Uri;
import android.provider.BaseColumns;

public final class TriviaContract {

    public static final String AUTHORITY = "com.trivia.masterandroid.provider";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    private TriviaContract() {
    }

    public static final class Players implements BaseColumns {
        public static final String TABLE = "players";
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(TABLE).build();
        public static final String NAME = "name";
        public static final String CREATED_AT = "created_at";
    }

    public static final class Matches implements BaseColumns {
        public static final String TABLE = "matches";
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(TABLE).build();
        public static final String PLAYER_ID = "player_id";
        public static final String PLAYER_NAME = "player_name";
        public static final String CATEGORY = "category";
        public static final String DIFFICULTY = "difficulty";
        public static final String TOTAL = "total";
        public static final String CORRECT = "correct";
        public static final String SCORE = "score";
        public static final String OFFLINE = "offline";
        public static final String PLAYED_AT = "played_at";
    }

    public static final class CachedQuestions implements BaseColumns {
        public static final String TABLE = "cached_questions";
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(TABLE).build();
        public static final String CATEGORY_ID = "category_id";
        public static final String CATEGORY = "category";
        public static final String DIFFICULTY = "difficulty";
        public static final String TEXT = "text";
        public static final String CORRECT = "correct";
        public static final String INCORRECT = "incorrect";
        public static final String CREATED_AT = "created_at";
        /** 0 = never played; used to serve the least recently played questions first. */
        public static final String LAST_PLAYED_AT = "last_played_at";
    }

    public static final class Categories implements BaseColumns {
        public static final String TABLE = "categories";
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(TABLE).build();
        public static final String NAME = "name";
    }
}
