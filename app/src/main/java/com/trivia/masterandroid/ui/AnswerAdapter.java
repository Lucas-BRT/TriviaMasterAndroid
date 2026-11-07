package com.trivia.masterandroid.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.model.AnswerRecord;

import java.util.List;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.Holder> {

    private final List<AnswerRecord> items;

    public AnswerAdapter(List<AnswerRecord> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_answer, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        AnswerRecord a = items.get(position);
        h.question.setText(a.question);
        if (a.correct) {
            h.mark.setText("✓");
            h.mark.setTextColor(h.itemView.getContext().getColor(R.color.correct));
        } else {
            h.mark.setText(a.timedOut ? "⏱" : "✗");
            h.mark.setTextColor(h.itemView.getContext().getColor(R.color.wrong));
        }
        h.yours.setText(a.chosen == null
                ? h.itemView.getContext().getString(R.string.answer_none)
                : h.itemView.getContext().getString(R.string.answer_yours, a.chosen));
        h.right.setText(h.itemView.getContext().getString(R.string.answer_right, a.correctAnswer));
        h.right.setVisibility(a.correct ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView mark;
        final TextView question;
        final TextView yours;
        final TextView right;

        Holder(@NonNull View v) {
            super(v);
            mark = v.findViewById(R.id.mark);
            question = v.findViewById(R.id.question);
            yours = v.findViewById(R.id.yours);
            right = v.findViewById(R.id.right);
        }
    }
}
