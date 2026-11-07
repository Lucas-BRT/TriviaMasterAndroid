package com.trivia.masterandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.databinding.ActivityQuizBinding;
import com.trivia.masterandroid.model.AnswerRecord;
import com.trivia.masterandroid.model.GameSettings;
import com.trivia.masterandroid.model.MatchRecord;
import com.trivia.masterandroid.model.Question;
import com.trivia.masterandroid.util.AppExecutors;
import com.trivia.masterandroid.util.ScoreCalculator;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_QUESTIONS = "extra_questions";
    public static final String EXTRA_SETTINGS = "extra_settings";
    public static final String EXTRA_OFFLINE = "extra_offline";

    private static final long LIMIT_MS = GameSettings.TIME_PER_QUESTION_SECONDS * 1000L;
    private static final long TIMEOUT_ADVANCE_MS = 2500;
    private static final String[] LETTERS = {"A", "B", "C", "D", "E", "F"};

    private ActivityQuizBinding binding;
    private GameSettings settings;
    private List<Question> questions;
    private boolean offline;

    private final List<AnswerRecord> answers = new ArrayList<>();
    private final List<View> optionViews = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private CountDownTimer timer;
    private int index;
    private int score;
    private int correctCount;
    private long timeLeftMs;
    private boolean answered;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settings = (GameSettings) getIntent().getSerializableExtra(EXTRA_SETTINGS);
        questions = (List<Question>) getIntent().getSerializableExtra(EXTRA_QUESTIONS);
        offline = getIntent().getBooleanExtra(EXTRA_OFFLINE, false);
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
        showQuestion();
    }

    private void showQuestion() {
        Question q = questions.get(index);
        answered = false;
        binding.progressLabel.setText(getString(R.string.quiz_progress, index + 1, questions.size()));
        binding.scoreLabel.setText(getString(R.string.quiz_score, score));
        binding.metaLabel.setText(q.category + " · " + difficultyLabel(q.difficulty));
        binding.questionText.setText(q.text);
        binding.feedbackBar.setVisibility(View.INVISIBLE);

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
        startTimer();
    }

    private void startTimer() {
        cancelTimer();
        timeLeftMs = LIMIT_MS;
        updateTimer();
        timer = new CountDownTimer(LIMIT_MS, 100) {
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
        binding.timerBar.setProgress(timeLeftMs * 100f / LIMIT_MS);
        int seconds = (int) Math.ceil(timeLeftMs / 1000.0);
        binding.timerText.setText(getString(R.string.quiz_seconds, seconds));
        boolean urgent = seconds <= 5;
        binding.timerBar.setProgressColor(getColor(urgent ? R.color.wrong : R.color.blue));
    }

    private void onAnswer(String chosen) {
        if (answered) {
            return;
        }
        answered = true;
        cancelTimer();
        Question q = questions.get(index);
        boolean correct = q.isCorrect(chosen);
        int points = ScoreCalculator.points(q.difficulty, correct, timeLeftMs, LIMIT_MS);
        score += points;
        if (correct) {
            correctCount++;
        }
        answers.add(new AnswerRecord(q.text, chosen, q.correctAnswer, correct, false, points));
        reveal(q, chosen);
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
        reveal(q, null);
        showFeedback(getString(R.string.quiz_timeout, q.correctAnswer), false);
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
            } else if (option.equals(chosen)) {
                row.setBackgroundResource(R.drawable.bg_option_wrong);
                mark.setText("✗");
                mark.setTextColor(getColor(R.color.wrong));
                mark.setVisibility(View.VISIBLE);
            }
        }
        binding.scoreLabel.setText(getString(R.string.quiz_score, score));
    }

    private void showFeedback(String message, boolean correct) {
        binding.feedbackText.setText(message);
        binding.feedbackBar.setBackgroundResource(R.color.surface);
        binding.feedbackBar.setBackgroundColor(getColor(correct ? R.color.correct_bg : R.color.wrong_bg));
        binding.btnNext.setText(index == questions.size() - 1 ? R.string.btn_finish : R.string.btn_next);
        binding.feedbackBar.setVisibility(View.VISIBLE);
    }

    private void next() {
        handler.removeCallbacksAndMessages(null);
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
        LocalDataSource local = new LocalDataSource(this);
        AppExecutors.io(() -> {
            if (m.playerId < 0) {
                m.playerId = local.getOrCreatePlayer(m.playerName);
            }
            local.saveMatch(m);
        });
        startActivity(new Intent(this, ResultActivity.class)
                .putExtra(ResultActivity.EXTRA_MATCH, m)
                .putExtra(ResultActivity.EXTRA_ANSWERS, new ArrayList<>(answers)));
        finish();
    }

    private void confirmQuit() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.quit_title)
                .setMessage(R.string.quit_message)
                .setPositiveButton(R.string.quit_confirm, (d, w) -> finish())
                .setNegativeButton(R.string.quit_cancel, null)
                .show();
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

    @Override
    protected void onDestroy() {
        cancelTimer();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
