package com.trivia.masterandroid.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Background pool for network/disk work plus a handler to post results to the UI thread. */
public final class AppExecutors {

    private static final ExecutorService IO = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AppExecutors() {
    }

    public static void io(Runnable task) {
        IO.execute(task);
    }

    public static void main(Runnable task) {
        MAIN.post(task);
    }
}
