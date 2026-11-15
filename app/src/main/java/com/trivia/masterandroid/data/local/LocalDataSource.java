package com.trivia.masterandroid.data.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.trivia.masterandroid.data.local.TriviaContract.CachedQuestions;
import com.trivia.masterandroid.data.local.TriviaContract.Categories;
import com.trivia.masterandroid.data.local.TriviaContract.Matches;
import com.trivia.masterandroid.data.local.TriviaContract.Players;
import com.trivia.masterandroid.model.Category;
import com.trivia.masterandroid.model.MatchRecord;
import com.trivia.masterandroid.model.PlayerStats;
import com.trivia.masterandroid.model.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/** All disk access goes through the ContentProvider. Blocking: call off the main thread. */
public class LocalDataSource {

    private static final String SEP = "\u001F";

    private final ContentResolver resolver;

    public LocalDataSource(Context context) {
        this.resolver = context.getApplicationContext().getContentResolver();
    }

    public long getOrCreatePlayer(String name) {
        try (Cursor c = resolver.query(Players.CONTENT_URI, new String[]{Players._ID},
                Players.NAME + "=?", new String[]{name}, null)) {
            if (c != null && c.moveToFirst()) {
                return c.getLong(0);
            }
        }
        ContentValues v = new ContentValues();
        v.put(Players.NAME, name);
        v.put(Players.CREATED_AT, System.currentTimeMillis());
        Uri uri = resolver.insert(Players.CONTENT_URI, v);
        return uri == null ? -1 : ContentUris.parseId(uri);
    }

    public void saveMatch(MatchRecord m) {
        ContentValues v = new ContentValues();
        v.put(Matches.PLAYER_ID, m.playerId);
        v.put(Matches.PLAYER_NAME, m.playerName);
        v.put(Matches.CATEGORY, m.category);
        v.put(Matches.DIFFICULTY, m.difficulty);
        v.put(Matches.TOTAL, m.total);
        v.put(Matches.CORRECT, m.correct);
        v.put(Matches.SCORE, m.score);
        v.put(Matches.OFFLINE, m.offline ? 1 : 0);
        v.put(Matches.PLAYED_AT, m.playedAt);
        resolver.insert(Matches.CONTENT_URI, v);
    }

    public List<MatchRecord> getMatches() {
        List<MatchRecord> list = new ArrayList<>();
        try (Cursor c = resolver.query(Matches.CONTENT_URI, null, null, null,
                Matches.PLAYED_AT + " DESC")) {
            while (c != null && c.moveToNext()) {
                MatchRecord m = new MatchRecord();
                m.id = c.getLong(c.getColumnIndexOrThrow(Matches._ID));
                m.playerId = c.getLong(c.getColumnIndexOrThrow(Matches.PLAYER_ID));
                m.playerName = c.getString(c.getColumnIndexOrThrow(Matches.PLAYER_NAME));
                m.category = c.getString(c.getColumnIndexOrThrow(Matches.CATEGORY));
                m.difficulty = c.getString(c.getColumnIndexOrThrow(Matches.DIFFICULTY));
                m.total = c.getInt(c.getColumnIndexOrThrow(Matches.TOTAL));
                m.correct = c.getInt(c.getColumnIndexOrThrow(Matches.CORRECT));
                m.score = c.getInt(c.getColumnIndexOrThrow(Matches.SCORE));
                m.offline = c.getInt(c.getColumnIndexOrThrow(Matches.OFFLINE)) == 1;
                m.playedAt = c.getLong(c.getColumnIndexOrThrow(Matches.PLAYED_AT));
                list.add(m);
            }
        }
        return list;
    }

    public void cacheQuestions(List<Question> questions, int categoryId) {
        ContentValues[] values = new ContentValues[questions.size()];
        for (int i = 0; i < values.length; i++) {
            Question q = questions.get(i);
            List<String> wrong = new ArrayList<>();
            for (String o : q.options) {
                if (!q.isCorrect(o)) {
                    wrong.add(o);
                }
            }
            ContentValues v = new ContentValues();
            v.put(CachedQuestions.CATEGORY_ID, categoryId);
            v.put(CachedQuestions.CATEGORY, q.category);
            v.put(CachedQuestions.DIFFICULTY, q.difficulty);
            v.put(CachedQuestions.TEXT, q.text);
            v.put(CachedQuestions.CORRECT, q.correctAnswer);
            v.put(CachedQuestions.INCORRECT, String.join(SEP, wrong));
            v.put(CachedQuestions.CREATED_AT, System.currentTimeMillis());
            values[i] = v;
        }
        resolver.bulkInsert(CachedQuestions.CONTENT_URI, values);
    }

    /** categoryId 0 and empty difficulty mean "any". */
    public List<Question> getCachedQuestions(int categoryId, String difficulty, int amount) {
        List<String> where = new ArrayList<>();
        List<String> args = new ArrayList<>();
        if (categoryId > 0) {
            where.add(CachedQuestions.CATEGORY_ID + "=?");
            args.add(String.valueOf(categoryId));
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            where.add(CachedQuestions.DIFFICULTY + "=?");
            args.add(difficulty);
        }
        String selection = where.isEmpty() ? null : String.join(" AND ", where);
        List<Question> list = new ArrayList<>();
        try (Cursor c = resolver.query(CachedQuestions.CONTENT_URI, null, selection,
                args.isEmpty() ? null : args.toArray(new String[0]),
                CachedQuestions.LAST_PLAYED_AT + " ASC, RANDOM() LIMIT " + amount)) {
            while (c != null && c.moveToNext()) {
                String incorrect = c.getString(c.getColumnIndexOrThrow(CachedQuestions.INCORRECT));
                list.add(new Question(
                        c.getString(c.getColumnIndexOrThrow(CachedQuestions.CATEGORY)),
                        c.getString(c.getColumnIndexOrThrow(CachedQuestions.DIFFICULTY)),
                        c.getString(c.getColumnIndexOrThrow(CachedQuestions.TEXT)),
                        c.getString(c.getColumnIndexOrThrow(CachedQuestions.CORRECT)),
                        Arrays.asList(incorrect.split(SEP))));
            }
        }
        return list;
    }

    public int countCachedQuestions() {
        try (Cursor c = resolver.query(CachedQuestions.CONTENT_URI,
                new String[]{"COUNT(*)"}, null, null, null)) {
            return c != null && c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    /** Replaces the cached category list (only when the new list is not empty). */
    public void saveCategories(List<Category> list) {
        if (list.isEmpty()) {
            return;
        }
        resolver.delete(Categories.CONTENT_URI, null, null);
        ContentValues[] values = new ContentValues[list.size()];
        for (int i = 0; i < values.length; i++) {
            ContentValues v = new ContentValues();
            v.put(Categories._ID, list.get(i).id);
            v.put(Categories.NAME, list.get(i).name);
            values[i] = v;
        }
        resolver.bulkInsert(Categories.CONTENT_URI, values);
    }

    public List<Category> getCategories() {
        List<Category> list = new ArrayList<>();
        try (Cursor c = resolver.query(Categories.CONTENT_URI, null, null, null, Categories._ID + " ASC")) {
            while (c != null && c.moveToNext()) {
                list.add(new Category(c.getInt(c.getColumnIndexOrThrow(Categories._ID)),
                        c.getString(c.getColumnIndexOrThrow(Categories.NAME))));
            }
        }
        return list;
    }

    /** Remembers when a cached question was shown, so the next match prefers unseen ones. */
    public void markPlayed(String questionText) {
        ContentValues v = new ContentValues();
        v.put(CachedQuestions.LAST_PLAYED_AT, System.currentTimeMillis());
        resolver.update(CachedQuestions.CONTENT_URI, v, CachedQuestions.TEXT + "=?", new String[]{questionText});
    }

    /** All players with their match count and best score, most recently active first. */
    public List<PlayerStats> getPlayers() {
        Map<Long, PlayerStats> byId = new HashMap<>();
        List<PlayerStats> list = new ArrayList<>();
        try (Cursor c = resolver.query(Players.CONTENT_URI, null, null, null, Players.CREATED_AT + " DESC")) {
            while (c != null && c.moveToNext()) {
                PlayerStats p = new PlayerStats();
                p.id = c.getLong(c.getColumnIndexOrThrow(Players._ID));
                p.name = c.getString(c.getColumnIndexOrThrow(Players.NAME));
                p.lastActivity = c.getLong(c.getColumnIndexOrThrow(Players.CREATED_AT));
                byId.put(p.id, p);
                list.add(p);
            }
        }
        for (MatchRecord m : getMatches()) {
            PlayerStats p = byId.get(m.playerId);
            if (p != null) {
                p.matches++;
                p.bestScore = Math.max(p.bestScore, m.score);
                p.lastActivity = Math.max(p.lastActivity, m.playedAt);
            }
        }
        list.sort((a, b) -> Long.compare(b.lastActivity, a.lastActivity));
        return list;
    }
}
