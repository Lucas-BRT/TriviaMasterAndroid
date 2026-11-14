package com.trivia.masterandroid.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.model.Category;

import java.util.List;
import java.util.function.Consumer;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Holder> {

    private final List<Category> items;
    private final int selectedId;
    private final Consumer<Category> onPick;

    public CategoryAdapter(List<Category> items, int selectedId, Consumer<Category> onPick) {
        this.items = items;
        this.selectedId = selectedId;
        this.onPick = onPick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Category c = items.get(position);
        boolean selected = c.id == selectedId;
        h.name.setText(c.name);
        h.mark.setVisibility(selected ? View.VISIBLE : View.GONE);
        h.itemView.setBackgroundResource(selected ? R.drawable.bg_option_selected : R.drawable.bg_option_selector);
        ViewCompat.setStateDescription(h.itemView, selected ? h.itemView.getContext().getString(R.string.state_selected) : null);
        h.itemView.setOnClickListener(v -> onPick.accept(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView mark;

        Holder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.category_name);
            mark = v.findViewById(R.id.category_mark);
        }
    }
}
