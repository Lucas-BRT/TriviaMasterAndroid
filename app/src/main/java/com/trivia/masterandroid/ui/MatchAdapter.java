package com.trivia.masterandroid.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.model.MatchRecord;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.Holder> {

    private final List<MatchRecord> items;

    public MatchAdapter(List<MatchRecord> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        MatchRecord m = items.get(position);
        Context c = h.itemView.getContext();
        h.player.setText(m.playerName);
        h.meta.setText(c.getString(R.string.history_item_meta, m.category, difficultyLabel(c, m.difficulty)));
        String when = DateUtils.formatDateTime(c, m.playedAt,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH);
        if (m.offline) {
            when = when + " · " + c.getString(R.string.history_offline_tag);
        }
        h.detail.setText(c.getString(R.string.history_item_detail, m.correct, m.total, when));
        h.score.setText(String.valueOf(m.score));
    }

    static String difficultyLabel(Context c, String d) {
        if ("hard".equals(d)) {
            return c.getString(R.string.difficulty_label_hard);
        }
        if ("medium".equals(d)) {
            return c.getString(R.string.difficulty_label_medium);
        }
        if ("easy".equals(d)) {
            return c.getString(R.string.difficulty_label_easy);
        }
        return c.getString(R.string.difficulty_label_any);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView player;
        final TextView meta;
        final TextView detail;
        final TextView score;

        Holder(@NonNull View v) {
            super(v);
            player = v.findViewById(R.id.player);
            meta = v.findViewById(R.id.meta);
            detail = v.findViewById(R.id.detail);
            score = v.findViewById(R.id.score);
        }
    }
}
