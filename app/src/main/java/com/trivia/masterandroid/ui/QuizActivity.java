package com.trivia.masterandroid.ui;

import android.content.Intent;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.util.SystemBars;
import com.trivia.masterandroid.databinding.ActivityQuizBinding;
import com.trivia.masterandroid.model.AnswerRecord;
import com.trivia.masterandroid.receiver.MatchFinishedReceiver;
import com.trivia.masterandroid.model.GameSettings;
import com.trivia.masterandroid.model.MatchRecord;
import com.trivia.masterandroid.model.Question;
import com.trivia.masterandroid.util.AppExecutors;
import com.trivia.masterandroid.util.DevSettings;
import com.trivia.masterandroid.util.Dialogs;
import com.trivia.masterandroid.util.Sfx;
import com.trivia.masterandroid.util.ScoreCalculator;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_QUESTIONS = "extra_questions";
    public static final String EXTRA_SETTINGS = "extra_settings";
    public static final String EXTRA_OFFLINE = "extra_offline";

    private static final long TIMEOUT_ADVANCE_MS = 2500;
    private static final String[] LETTERS = {"A", "B", "C", "D", "E", "F"};

    private ActivityQuizBinding binding;
    private GameSettings settings;
    private List<Question> questions;
    private boolean offline;

    private final List<AnswerRecord> answers = new ArrayList<>();
    private final List<View> optionViews = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LocalDataSource local;
    private long limitMs;
    private CountDownTimer timer;
    private int index;
    private int score;
    private int correctCount;
    private long timeLeftMs;
    private boolean answered;
    private int lastSecond = -1;
    private boolean advancePending;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.enable(this);
        SystemBars.pad(binding.content, true, false);
        SystemBars.pad(binding.feedbackBar, false, true);

        settings = (GameSettings) getIntent().getSerializableExtra(EXTRA_SETTINGS);
        questions = (List<Question>) getIntent().getSerializableExtra(EXTRA_QUESTIONS);
        offline = getIntent().getBooleanExtra(EXTRA_OFFLINE, false);
        local = new LocalDataSource(this);
        limitMs = DevSettings.secondsPerQuestion(this) * 1000L;
        if (settings == null || questions == null || questions.isEmpty()) {
            finish();
            return;
        }

        binding.btnNext.setOnClickListener(v -> next());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmQuit();
            }
        });
        Sfx.preload(this);
        showQuestion();
    }

    private void showQuestion() {
        Question q = questions.get(index);
        answered = false;
        binding.progressLabel.setText(getString(R.string.quiz_progress, index + 1, questions.size()));
        binding.scoreLabel.setText(getString(R.string.quiz_score, score));
        binding.metaLabel.setText(q.category + " · " + difficultyLabel(q.difficulty));
        binding.questionText.setText(q.text);
        binding.feedbackBar.animate().cancel();
        binding.feedbackBar.setVisibility(View.INVISIBLE);
        lastSecond = -1;

        binding.options.removeAllViews();
        optionViews.clear();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < q.options.size(); i++) {
            View row = inflater.inflate(R.layout.item_option, binding.options, false);
            ((TextView) row.findViewById(R.id.letter)).setText(LETTERS[i]);
            ((TextView) row.findViewById(R.id.text)).setText(q.options.get(i));
            final String option = q.options.get(i);
            row.setOnClickListener(v -> onAnswer(option));
            binding.options.addView(row);
            optionViews.add(row);
        }
        animateQuestionIn();
        startTimer();
    }

    private void startTimer() {
        timeLeftMs = limitMs;
        runTimer();
    }

    /** Runs the countdown from the time still left, so it can resume after a pause. */
    private void runTimer() {
        cancelTimer();
        updateTimer();
        timer = new CountDownTimer(timeLeftMs, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMs = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                timeLeftMs = 0;
                updateTimer();
                onTimeout();
            }
        }.start();
    }

    private void updateTimer() {
        binding.timerBar.setProgress(timeLeftMs * 100f / limitMs);
        int seconds = (int) Math.ceil(timeLeftMs / 1000.0);
        binding.timerText.setText(getString(R.string.quiz_seconds, seconds));
        boolean urgent = seconds <= 5;
        binding.timerBar.setProgressColor(getColor(urgent ? R.color.wrong : R.color.blue));
        if (urgent && seconds != lastSecond && seconds > 0 && !answered) {
            Sfx.play(this, Sfx.Sound.TICK);
            binding.timerText.animate().cancel();
            binding.timerText.animate().scaleX(1.18f).scaleY(1.18f).setDuration(110)
                    .withEndAction(() -> binding.timerText.animate().scaleX(1f).scaleY(1f).setDuration(160).start())
                    .start();
        }
        lastSecond = seconds;
    }

    private void onAnswer(String chosen) {
        if (answered) {
            return;
        }
        answered = true;
        cancelTimer();
        Question q = questions.get(index);
        boolean correct = q.isCorrect(chosen);
        int points = ScoreCalculator.points(q.difficulty, correct, timeLeftMs, limitMs);
        score += points;
        if (correct) {
            correctCount++;
        }
        answers.add(new AnswerRecord(q.text, chosen, q.correctAnswer, correct, false, points));
        AppExecutors.io(() -> local.markPlayed(q.text));
        reveal(q, chosen);
        Sfx.play(this, correct ? Sfx.Sound.CORRECT : Sfx.Sound.WRONG);
        String message = correct
                ? getString(R.string.quiz_correct, points)
                : getString(R.string.quiz_wrong, q.correctAnswer);
        showFeedback(message, correct);
    }

    private void onTimeout() {
        if (answered) {
            return;
        }
        answered = true;
        Question q = questions.get(index);
        answers.add(new AnswerRecord(q.text, null, q.correctAnswer, false, true, 0));
        AppExecutors.io(() -> local.markPlayed(q.text));
        reveal(q, null);
        Sfx.play(this, Sfx.Sound.TIMEOUT);
        showFeedback(getString(R.string.quiz_timeout, q.correctAnswer), false);
        advancePending = true;
        handler.postDelayed(this::next, TIMEOUT_ADVANCE_MS);
    }

    private void reveal(Question q, String chosen) {
        for (int i = 0; i < optionViews.size(); i++) {
            View row = optionViews.get(i);
            String option = q.options.get(i);
            TextView mark = row.findViewById(R.id.mark);
            row.setClickable(false);
            if (q.isCorrect(option)) {
                row.setBackgroundResource(R.drawable.bg_option_correct);
                mark.setText("✓");
                mark.setTextColor(getColor(R.color.correct));
                mark.setVisibility(View.VISIBLE);
                pop(row);
            } else if (option.equals(chosen)) {
                row.setBackgroundResource(R.drawable.bg_option_wrong);
                mark.setText("✗");
                mark.setTextColor(getColor(R.color.wrong));
                mark.setVisibility(View.VISIBLE);
                shake(row);
            }
        }
        binding.scoreLabel.setText(getString(R.string.quiz_score, score));
    }

    private void showFeedback(String message, boolean correct) {
        binding.feedbackText.setText(message);
        binding.feedbackBar.setBackgroundResource(R.color.surface);
        binding.feedbackBar.setBackgroundColor(getColor(correct ? R.color.correct_bg : R.color.wrong_bg));
        binding.btnNext.setText(index == questions.size() - 1 ? R.string.btn_finish : R.string.btn_next);
        float dp = getResources().getDisplayMetrics().density;
        binding.feedbackBar.setTranslationY(240 * dp);
        binding.feedbackBar.setVisibility(View.VISIBLE);
        binding.feedbackBar.animate().translationY(0).setDuration(240)
                .setInterpolator(new DecelerateInterpolator()).start();
        if (DevSettings.haptics(this)) binding.feedbackBar.performHapticFeedback(correct
                ? (android.os.Build.VERSION.SDK_INT >= 30 ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.KEYBOARD_TAP)
                : (android.os.Build.VERSION.SDK_INT >= 30 ? HapticFeedbackConstants.REJECT : HapticFeedbackConstants.LONG_PRESS));
    }

    private void animateQuestionIn() {
        float dp = getResources().getDisplayMetrics().density;
        binding.questionText.setAlpha(0f);
        binding.questionText.setTranslationY(12 * dp);
        binding.questionText.animate().alpha(1f).translationY(0).setDuration(220)
                .setInterpolator(new DecelerateInterpolator()).start();
        for (int i = 0; i < optionViews.size(); i++) {
            View row = optionViews.get(i);
            row.setAlpha(0f);
            row.setTranslationY(20 * dp);
            row.animate().alpha(1f).translationY(0).setStartDelay(70 + i * 60L).setDuration(220)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    private void pop(View row) {
        row.animate().scaleX(1.03f).scaleY(1.03f).setDuration(120)
                .withEndAction(() -> row.animate().scaleX(1f).scaleY(1f).setDuration(140).start())
                .start();
    }

    private void shake(View row) {
        float dp = getResources().getDisplayMetrics().density;
        ObjectAnimator.ofFloat(row, View.TRANSLATION_X, 0, -10 * dp, 10 * dp, -7 * dp, 7 * dp, 0)
                .setDuration(360).start();
    }

    private void next() {
        handler.removeCallbacksAndMessages(null);
        advancePending = false;
        if (index >= questions.size() - 1) {
            finishMatch();
            return;
        }
        index++;
        showQuestion();
    }

    private void finishMatch() {
        MatchRecord m = new MatchRecord();
        m.playerId = settings.playerId;
        m.playerName = settings.playerName;
        m.category = settings.categoryName;
        m.difficulty = settings.difficulty.isEmpty() ? "any" : settings.difficulty;
        m.total = questions.size();
        m.correct = correctCount;
        m.score = score;
        m.offline = offline;
        m.playedAt = System.currentTimeMillis();
        AppExecutors.io(() -> {
            if (m.playerId < 0) {
                m.playerId = local.getOrCreatePlayer(m.playerName);
            }
            local.saveMatch(m);
        });
        sendBroadcast(new Intent(MatchFinishedReceiver.ACTION_MATCH_FINISHED).setPackage(getPackageName()));
        startActivity(new Intent(this, ResultActivity.class)
                .putExtra(ResultActivity.EXTRA_MATCH, m)
                .putExtra(ResultActivity.EXTRA_ANSWERS, new ArrayList<>(answers)));
        finish();
    }

    /** The clock stops while the dialog is open and resumes if the player keeps playing. */
    private void confirmQuit() {
        cancelTimer();
        handler.removeCallbacksAndMessages(null);
        Dialogs.confirm(this, R.string.quit_title, R.string.quit_message,
                R.string.quit_confirm, this::finish, R.string.quit_cancel, this::resumeAfterDialog);
    }

    private void resumeAfterDialog() {
        if (isFinishing()) {
            return;
        }
        if (!answered && timeLeftMs > 0) {
            runTimer();
        } else if (answered && advancePending) {
            handler.postDelayed(this::next, 1500);
        }
    }

    private String difficultyLabel(String d) {
        if ("hard".equals(d)) {
            return getString(R.string.difficulty_label_hard);
        }
        if ("medium".equals(d)) {
            return getString(R.string.difficulty_label_medium);
        }
        return getString(R.string.difficulty_label_easy);
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /** The clock must not run while the app is in the background (call, notification, home). */
    @Override
    protected void onPause() {
        cancelTimer();
        handler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinishing() || questions == null) {
            return;
        }
        if (!answered && timer == null && timeLeftMs > 0) {
            runTimer();
        } else if (answered && advancePending) {
            handler.postDelayed(this::next, 1500);
        }
    }

    @Override
    protected void onDestroy() {
        cancelTimer();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
