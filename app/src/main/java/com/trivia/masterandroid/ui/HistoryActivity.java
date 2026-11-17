package com.trivia.masterandroid.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.databinding.ActivityHistoryBinding;
import com.trivia.masterandroid.model.MatchRecord;
import com.trivia.masterandroid.util.AppExecutors;
import com.trivia.masterandroid.util.SystemBars;

import java.util.ArrayList;
import java.util.List;

/** Match history. When opened for a player it starts filtered on that player, with a "Todos" chip. */
public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private final List<MatchRecord> all = new ArrayList<>();
    private String playerName;
    private boolean onlyPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.enable(this);
        SystemBars.pad(binding.root, true, false);
        SystemBars.pad(binding.list, false, true);
        binding.list.setLayoutManager(new LinearLayoutManager(this));

        String name = getIntent().getStringExtra(PlayerActivity.EXTRA_NAME);
        playerName = name == null || name.isEmpty() ? null : name;
        onlyPlayer = playerName != null;
        if (playerName != null) {
            setupFilter();
        }

        LocalDataSource local = new LocalDataSource(this);
        AppExecutors.io(() -> {
            List<MatchRecord> matches = local.getMatches();
            AppExecutors.main(() -> {
                all.clear();
                all.addAll(matches);
                show();
            });
        });
    }

    private void setupFilter() {
        binding.filterGroup.setVisibility(View.VISIBLE);
        String[] labels = {playerName, getString(R.string.history_filter_all)};
        int margin = (int) (8 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < labels.length; i++) {
            RadioButton b = new RadioButton(this);
            b.setId(View.generateViewId());
            b.setTag(i == 0);
            b.setText(labels[i]);
            b.setSingleLine(true);
            b.setEllipsize(android.text.TextUtils.TruncateAt.END);
            b.setButtonDrawable(null);
            b.setBackgroundResource(R.drawable.bg_chip);
            b.setTextColor(getColorStateList(R.color.chip_text));
            b.setTextSize(14);
            b.setGravity(Gravity.CENTER);
            b.setPadding(margin * 2, 0, margin * 2, 0);
            b.setMinHeight(getResources().getDimensionPixelSize(R.dimen.chip_height));
            RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMarginEnd(i == 0 ? margin : 0);
            binding.filterGroup.addView(b, lp);
        }
        binding.filterGroup.check(binding.filterGroup.getChildAt(0).getId());
        binding.filterGroup.setOnCheckedChangeListener((group, id) -> {
            RadioButton checked = group.findViewById(id);
            if (checked != null) {
                onlyPlayer = (boolean) checked.getTag();
                show();
            }
        });
    }

    private void show() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        List<MatchRecord> matches = new ArrayList<>();
        for (MatchRecord m : all) {
            if (!onlyPlayer || m.playerName.equals(playerName)) {
                matches.add(m);
            }
        }
        boolean empty = matches.isEmpty();
        binding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.list.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.stats.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            binding.emptyTitle.setText(onlyPlayer
                    ? getString(R.string.history_empty_player, playerName)
                    : getString(R.string.history_empty_title));
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
