package com.trivia.masterandroid.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Hidden developer options, reachable only through the secret gesture on the home screen. */
public final class DevSettings {

    public static final int MIN_SECONDS = 10;
    public static final int MAX_SECONDS = 60;
    public static final int DEFAULT_SECONDS = 20;

    private static final String PREFS = "dev_settings";
    private static final String KEY_FORCE_OFFLINE = "force_offline";
    private static final String KEY_HAPTICS = "haptics";
    private static final String KEY_SECONDS = "seconds_per_question";

    private DevSettings() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean forceOffline(Context c) {
        return prefs(c).getBoolean(KEY_FORCE_OFFLINE, false);
    }

    public static void setForceOffline(Context c, boolean value) {
        prefs(c).edit().putBoolean(KEY_FORCE_OFFLINE, value).apply();
    }

    public static boolean haptics(Context c) {
        return prefs(c).getBoolean(KEY_HAPTICS, true);
    }

    public static void setHaptics(Context c, boolean value) {
        prefs(c).edit().putBoolean(KEY_HAPTICS, value).apply();
    }

    /** Seconds per question, always inside the 10-60 range required by the assignment. */
    public static int secondsPerQuestion(Context c) {
        int v = prefs(c).getInt(KEY_SECONDS, DEFAULT_SECONDS);
        return Math.max(MIN_SECONDS, Math.min(MAX_SECONDS, v));
    }

    public static void setSecondsPerQuestion(Context c, int seconds) {
        prefs(c).edit().putInt(KEY_SECONDS, Math.max(MIN_SECONDS, Math.min(MAX_SECONDS, seconds))).apply();
    }
}
