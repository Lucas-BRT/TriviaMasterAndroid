package com.trivia.masterandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.databinding.ActivityResultBinding;
import com.trivia.masterandroid.model.AnswerRecord;
import com.trivia.masterandroid.model.MatchRecord;

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

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultBinding binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MatchRecord match = (MatchRecord) getIntent().getSerializableExtra(EXTRA_MATCH);
        List<AnswerRecord> answers = (List<AnswerRecord>) getIntent().getSerializableExtra(EXTRA_ANSWERS);
        if (match == null || answers == null) {
            finish();
            return;
        }

        int percent = match.total == 0 ? 0 : Math.round(match.correct * 100f / match.total);
        binding.scoreBig.setText(String.valueOf(match.score));
        binding.correctValue.setText(getString(R.string.result_value_of, match.correct, match.total));
        binding.accuracyValue.setText(getString(R.string.result_percent, percent));
        binding.offlineBadge.setVisibility(match.offline ? View.VISIBLE : View.GONE);

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

        if (percent >= 60 && animationsEnabled()) {
            celebrate(binding);
        }
    }

    private boolean animationsEnabled() {
        return Settings.Global.getFloat(getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f;
    }

    private void celebrate(ActivityResultBinding binding) {
        Party party = new PartyFactory(new Emitter(150L, TimeUnit.MILLISECONDS).max(120))
                .spread(360)
                .setSpeedBetween(5f, 25f)
                .colors(Arrays.asList(
                        getColor(R.color.blue), getColor(R.color.sun),
                        getColor(R.color.correct), getColor(R.color.wrong)))
                .position(new Position.Relative(0.5, 0.2))
                .build();
        binding.konfetti.start(party);
    }
}
