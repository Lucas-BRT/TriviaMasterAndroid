package com.trivia.masterandroid.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.trivia.masterandroid.data.local.TriviaContract.CachedQuestions;
import com.trivia.masterandroid.data.local.TriviaContract.Matches;
import com.trivia.masterandroid.data.local.TriviaContract.Players;

public class TriviaDbHelper extends SQLiteOpenHelper {

    private static final String NAME = "trivia.db";
    private static final int VERSION = 1;

    public TriviaDbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Players.TABLE + " ("
                + Players._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Players.NAME + " TEXT NOT NULL UNIQUE, "
                + Players.CREATED_AT + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Matches.TABLE + " ("
                + Matches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Matches.PLAYER_ID + " INTEGER NOT NULL REFERENCES " + Players.TABLE + "(" + Players._ID + "), "
                + Matches.PLAYER_NAME + " TEXT NOT NULL, "
                + Matches.CATEGORY + " TEXT NOT NULL, "
                + Matches.DIFFICULTY + " TEXT NOT NULL, "
                + Matches.TOTAL + " INTEGER NOT NULL, "
                + Matches.CORRECT + " INTEGER NOT NULL, "
                + Matches.SCORE + " INTEGER NOT NULL, "
                + Matches.OFFLINE + " INTEGER NOT NULL DEFAULT 0, "
                + Matches.PLAYED_AT + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + CachedQuestions.TABLE + " ("
                + CachedQuestions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CachedQuestions.CATEGORY_ID + " INTEGER NOT NULL DEFAULT 0, "
                + CachedQuestions.CATEGORY + " TEXT NOT NULL, "
                + CachedQuestions.DIFFICULTY + " TEXT NOT NULL, "
                + CachedQuestions.TEXT + " TEXT NOT NULL UNIQUE, "
                + CachedQuestions.CORRECT + " TEXT NOT NULL, "
                + CachedQuestions.INCORRECT + " TEXT NOT NULL, "
                + CachedQuestions.CREATED_AT + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Matches.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CachedQuestions.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Players.TABLE);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }
}
