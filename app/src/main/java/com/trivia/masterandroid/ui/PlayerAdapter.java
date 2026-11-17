package com.trivia.masterandroid.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.model.PlayerStats;

import java.util.List;
import java.util.function.Consumer;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.Holder> {

    private final List<PlayerStats> items;
    private final String currentName;
    private final Consumer<PlayerStats> onPick;

    public PlayerAdapter(List<PlayerStats> items, String currentName, Consumer<PlayerStats> onPick) {
        this.items = items;
        this.currentName = currentName;
        this.onPick = onPick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        PlayerStats p = items.get(position);
        Context c = h.itemView.getContext();
        boolean current = p.name.equals(currentName);
        h.name.setText(p.name);
        h.stats.setText(p.matches == 0 ? c.getString(R.string.player_stats_none)
                : c.getString(R.string.player_stats, p.matches, p.bestScore));
        h.mark.setVisibility(current ? View.VISIBLE : View.GONE);
        h.itemView.setBackgroundResource(current ? R.drawable.bg_option_selected : R.drawable.bg_option_selector);
        ViewCompat.setStateDescription(h.itemView, current ? c.getString(R.string.state_selected) : null);
        h.itemView.setOnClickListener(v -> onPick.accept(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView stats;
        final TextView mark;

        Holder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.player_name);
            stats = v.findViewById(R.id.player_stats);
            mark = v.findViewById(R.id.player_mark);
        }
    }
}
