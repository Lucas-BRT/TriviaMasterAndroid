package com.trivia.masterandroid.data.local;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trivia.masterandroid.data.local.TriviaContract.CachedQuestions;
import com.trivia.masterandroid.data.local.TriviaContract.Matches;
import com.trivia.masterandroid.data.local.TriviaContract.Players;

public class TriviaProvider extends ContentProvider {

    private static final int PLAYERS = 1;
    private static final int PLAYER_ID = 2;
    private static final int MATCHES = 3;
    private static final int MATCH_ID = 4;
    private static final int QUESTIONS = 5;

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(TriviaContract.AUTHORITY, Players.TABLE, PLAYERS);
        MATCHER.addURI(TriviaContract.AUTHORITY, Players.TABLE + "/#", PLAYER_ID);
        MATCHER.addURI(TriviaContract.AUTHORITY, Matches.TABLE, MATCHES);
        MATCHER.addURI(TriviaContract.AUTHORITY, Matches.TABLE + "/#", MATCH_ID);
        MATCHER.addURI(TriviaContract.AUTHORITY, CachedQuestions.TABLE, QUESTIONS);
    }

    private TriviaDbHelper helper;

    @Override
    public boolean onCreate() {
        helper = new TriviaDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String table = tableFor(uri);
        if (MATCHER.match(uri) == PLAYER_ID || MATCHER.match(uri) == MATCH_ID) {
            selection = appendId(selection, uri);
        }
        Cursor cursor = helper.getReadableDatabase()
                .query(table, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(requireContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String base = "vnd.android.cursor";
        switch (MATCHER.match(uri)) {
            case PLAYER_ID:
            case MATCH_ID:
                return base + ".item/vnd." + TriviaContract.AUTHORITY + "." + tableFor(uri);
            default:
                return base + ".dir/vnd." + TriviaContract.AUTHORITY + "." + tableFor(uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String table = tableFor(uri);
        long id = helper.getWritableDatabase()
                .insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id < 0) {
            return null;
        }
        notifyChange(uri);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        String table = tableFor(uri);
        SQLiteDatabase db = helper.getWritableDatabase();
        int inserted = 0;
        db.beginTransaction();
        try {
            for (ContentValues v : values) {
                if (db.insertWithOnConflict(table, null, v, SQLiteDatabase.CONFLICT_IGNORE) >= 0) {
                    inserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        notifyChange(uri);
        return inserted;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table = tableFor(uri);
        if (MATCHER.match(uri) == PLAYER_ID || MATCHER.match(uri) == MATCH_ID) {
            selection = appendId(selection, uri);
        }
        int count = helper.getWritableDatabase().delete(table, selection, selectionArgs);
        if (count > 0) {
            notifyChange(uri);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        String table = tableFor(uri);
        if (MATCHER.match(uri) == PLAYER_ID || MATCHER.match(uri) == MATCH_ID) {
            selection = appendId(selection, uri);
        }
        int count = helper.getWritableDatabase().update(table, values, selection, selectionArgs);
        if (count > 0) {
            notifyChange(uri);
        }
        return count;
    }

    private String tableFor(Uri uri) {
        switch (MATCHER.match(uri)) {
            case PLAYERS:
            case PLAYER_ID:
                return Players.TABLE;
            case MATCHES:
            case MATCH_ID:
                return Matches.TABLE;
            case QUESTIONS:
                return CachedQuestions.TABLE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    private static String appendId(String selection, Uri uri) {
        String idClause = "_id=" + ContentUris.parseId(uri);
        return selection == null || selection.isEmpty() ? idClause : idClause + " AND (" + selection + ")";
    }

    private void notifyChange(Uri uri) {
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
    }
}
