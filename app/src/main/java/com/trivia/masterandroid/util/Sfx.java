package com.trivia.masterandroid.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.trivia.masterandroid.R;

import java.util.EnumMap;
import java.util.Map;

/**
 * Short sound effects played with a SoundPool (low latency, uses the game stream so it follows the
 * media volume and stays quiet in silent mode). Sounds are synthesized tones stored in res/raw.
 */
public final class Sfx {

    public enum Sound { CORRECT, WRONG, TIMEOUT, TICK, WIN, COMPLETE, RECORD }

    private static Sfx instance;

    private final Context appContext;
    private final SoundPool pool;
    private final Map<Sound, Integer> ids = new EnumMap<>(Sound.class);

    private Sfx(Context context) {
        appContext = context.getApplicationContext();
        pool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        ids.put(Sound.CORRECT, pool.load(appContext, R.raw.sfx_correct, 1));
        ids.put(Sound.WRONG, pool.load(appContext, R.raw.sfx_wrong, 1));
        ids.put(Sound.TIMEOUT, pool.load(appContext, R.raw.sfx_timeout, 1));
        ids.put(Sound.TICK, pool.load(appContext, R.raw.sfx_tick, 1));
        ids.put(Sound.WIN, pool.load(appContext, R.raw.sfx_win, 1));
        ids.put(Sound.COMPLETE, pool.load(appContext, R.raw.sfx_complete, 1));
        ids.put(Sound.RECORD, pool.load(appContext, R.raw.sfx_record, 1));
    }

    private static synchronized Sfx get(Context context) {
        if (instance == null) {
            instance = new Sfx(context);
        }
        return instance;
    }

    /** Loads the sounds ahead of time so the first effect is not lost. */
    public static void preload(Context context) {
        get(context);
    }

    public static void play(Context context, Sound sound) {
        if (!DevSettings.sound(context)) {
            return;
        }
        Sfx sfx = get(context);
        Integer id = sfx.ids.get(sound);
        if (id != null) {
            float volume = sound == Sound.TICK ? 0.5f : 1f;
            sfx.pool.play(id, volume, volume, 1, 0, 1f);
        }
    }
}
