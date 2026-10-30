package com.trivia.masterandroid.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.databinding.ActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity {

    private static final String PREFS = "trivia_prefs";
    private static final String KEY_NAME = "player_name";
    public static final String EXTRA_NAME = "extra_player_name";

    private ActivityPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        binding.nameInput.setText(prefs.getString(KEY_NAME, ""));

        binding.btnPlay.setOnClickListener(v -> {
            String name = validName();
            if (name == null) {
                return;
            }
            prefs.edit().putString(KEY_NAME, name).apply();
            // Setup screen is added in a following step.
        });
    }

    private String validName() {
        String name = String.valueOf(binding.nameInput.getText()).trim();
        if (name.isEmpty()) {
            binding.nameLayout.setError(getString(R.string.player_error_empty));
            return null;
        }
        if (name.length() > 20) {
            binding.nameLayout.setError(getString(R.string.player_error_long));
            return null;
        }
        binding.nameLayout.setError(null);
        return name;
    }
}
