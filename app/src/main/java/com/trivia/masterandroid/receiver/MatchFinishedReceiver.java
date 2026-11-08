package com.trivia.masterandroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.trivia.masterandroid.service.QuestionPrefetchService;

/** Declared in the manifest. After each match, refills the offline question cache. */
public class MatchFinishedReceiver extends BroadcastReceiver {

    public static final String ACTION_MATCH_FINISHED = "com.trivia.masterandroid.ACTION_MATCH_FINISHED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_MATCH_FINISHED.equals(intent.getAction())) {
            try {
                context.startService(new Intent(context, QuestionPrefetchService.class));
            } catch (IllegalStateException ignored) {
                // App moved to background before the service could start; next launch retries.
            }
        }
    }
}
