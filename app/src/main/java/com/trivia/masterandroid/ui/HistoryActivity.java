package com.trivia.masterandroid.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.util.SystemBars;
import com.trivia.masterandroid.databinding.ActivityHistoryBinding;
import com.trivia.masterandroid.model.MatchRecord;
import com.trivia.masterandroid.util.AppExecutors;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.enable(this);
        SystemBars.pad(binding.root, true, false);
        SystemBars.pad(binding.list, false, true);
        binding.list.setLayoutManager(new LinearLayoutManager(this));

        LocalDataSource local = new LocalDataSource(this);
        AppExecutors.io(() -> {
            List<MatchRecord> matches = local.getMatches();
            AppExecutors.main(() -> show(matches));
        });
    }

    private void show(List<MatchRecord> matches) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        boolean empty = matches.isEmpty();
        binding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.list.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.stats.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }
        int best = 0;
        int sum = 0;
        for (MatchRecord m : matches) {
            best = Math.max(best, m.score);
            sum += m.score;
        }
        binding.statMatches.setText(String.valueOf(matches.size()));
        binding.statBest.setText(String.valueOf(best));
        binding.statAvg.setText(String.valueOf(Math.round(sum / (float) matches.size())));
        binding.list.setAdapter(new MatchAdapter(matches));
        binding.list.scheduleLayoutAnimation();
    }
}
