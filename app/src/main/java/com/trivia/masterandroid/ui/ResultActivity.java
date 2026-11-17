package com.trivia.masterandroid.ui;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.util.SystemBars;
import com.trivia.masterandroid.databinding.ActivityResultBinding;
import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.model.AnswerRecord;
import com.trivia.masterandroid.model.MatchRecord;
import com.trivia.masterandroid.util.AppExecutors;
import com.trivia.masterandroid.util.DevSettings;
import com.trivia.masterandroid.util.Sfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_MATCH = "extra_match";
    public static final String EXTRA_ANSWERS = "extra_answers";

    private final Handler handler = new Handler(Looper.getMainLooper());

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultBinding binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.enable(this);
        SystemBars.pad(binding.content, true, false);
        SystemBars.pad(binding.actionBar, false, true);

        MatchRecord match = (MatchRecord) getIntent().getSerializableExtra(EXTRA_MATCH);
        List<AnswerRecord> answers = (List<AnswerRecord>) getIntent().getSerializableExtra(EXTRA_ANSWERS);
        if (match == null || answers == null) {
            finish();
            return;
        }

        int percent = match.total == 0 ? 0 : Math.round(match.correct * 100f / match.total);
        if (animationsEnabled() && match.score > 0) {
            ValueAnimator counter = ValueAnimator.ofInt(0, match.score);
            counter.setDuration(Math.min(1200, 400 + match.score * 8L));
            counter.setInterpolator(new DecelerateInterpolator());
            counter.addUpdateListener(a -> binding.scoreBig.setText(String.valueOf(a.getAnimatedValue())));
            counter.start();
        } else {
            binding.scoreBig.setText(String.valueOf(match.score));
        }
        binding.correctValue.setText(getString(R.string.result_value_of, match.correct, match.total));
        binding.accuracyValue.setText(getString(R.string.result_percent, percent));
        binding.offlineBadge.setVisibility(match.offline ? View.VISIBLE : View.GONE);
        binding.resultMessage.setText(percent >= 80 ? R.string.result_msg_great
                : percent >= 50 ? R.string.result_msg_good : R.string.result_msg_try);

        binding.answers.setLayoutManager(new LinearLayoutManager(this));
        binding.answers.setAdapter(new AnswerAdapter(new ArrayList<>(answers)));

        binding.btnAgain.setOnClickListener(v -> {
            startActivity(new Intent(this, SetupActivity.class)
                    .putExtra(PlayerActivity.EXTRA_NAME, match.playerName));
            finish();
        });
        binding.btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, PlayerActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });

        Sfx.preload(this);
        Sfx.play(this, Sfx.Sound.COMPLETE);
        checkRecord(binding, match, percent);
    }

    /**
     * "New record" only when there are earlier matches and this one beats all of them.
     * The completion chord plays first; the record fanfare (or the regular win jingle for 60%+)
     * follows half a second later so the sounds do not overlap.
     */
    private void checkRecord(ActivityResultBinding binding, MatchRecord match, int percent) {
        LocalDataSource local = new LocalDataSource(this);
        AppExecutors.io(() -> {
            int previousBest = -1;
            for (MatchRecord m : local.getMatches()) {
                if (m.playedAt != match.playedAt) {
                    previousBest = Math.max(previousBest, m.score);
                }
            }
            boolean record = previousBest >= 0 && match.score > previousBest;
            AppExecutors.main(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (record) {
                    binding.recordBadge.setVisibility(View.VISIBLE);
                    handler.postDelayed(() -> {
                        Sfx.play(this, Sfx.Sound.RECORD);
                        binding.recordBadge.animate().scaleX(1.12f).scaleY(1.12f).setDuration(160)
                                .withEndAction(() -> binding.recordBadge.animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                                .start();
                        if (DevSettings.haptics(this)) {
                            binding.getRoot().performHapticFeedback(Build.VERSION.SDK_INT >= 30
                                    ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.LONG_PRESS);
                        }
                        celebrate(binding, true);
                    }, 550);
                } else if (percent >= 60) {
                    handler.postDelayed(() -> {
                        Sfx.play(this, Sfx.Sound.WIN);
                        celebrate(binding, false);
                    }, 550);
                }
            });
        });
    }

    private boolean animationsEnabled() {
        return Settings.Global.getFloat(getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f;
    }

    private void celebrate(ActivityResultBinding binding, boolean big) {
        if (!animationsEnabled()) {
            return;
        }
        List<Integer> colors = Arrays.asList(
                getColor(R.color.blue), getColor(R.color.sun),
                getColor(R.color.correct), getColor(R.color.wrong));
        binding.konfetti.start(new PartyFactory(new Emitter(150L, TimeUnit.MILLISECONDS).max(big ? 200 : 120))
                .spread(360).setSpeedBetween(5f, big ? 35f : 25f).colors(colors)
                .position(new Position.Relative(0.5, 0.2)).build());
        if (big) {
            binding.konfetti.start(
                    new PartyFactory(new Emitter(250L, TimeUnit.MILLISECONDS).max(100))
                            .angle(315).spread(60).setSpeedBetween(10f, 40f).colors(colors)
                            .position(new Position.Relative(0.0, 0.3)).build(),
                    new PartyFactory(new Emitter(250L, TimeUnit.MILLISECONDS).max(100))
                            .angle(225).spread(60).setSpeedBetween(10f, 40f).colors(colors)
                            .position(new Position.Relative(1.0, 0.3)).build());
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
