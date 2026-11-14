package com.trivia.masterandroid.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.trivia.masterandroid.util.SystemBars;
import com.trivia.masterandroid.databinding.ActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity {

    private static final String PREFS = "trivia_prefs";
    private static final String KEY_NAME = "player_name";
    public static final String EXTRA_NAME = "extra_player_name";

    private ActivityPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.enable(this);
        SystemBars.pad(binding.content, true, true);

        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        binding.nameInput.setText(prefs.getString(KEY_NAME, ""));

        binding.btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        binding.nameInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnPlay.performClick();
                return true;
            }
            return false;
        });

        binding.btnPlay.setOnClickListener(v -> {
            String name = validName();
            if (name == null) {
                return;
            }
            prefs.edit().putString(KEY_NAME, name).apply();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.nameInput.getWindowToken(), 0);
            startActivity(new Intent(this, SetupActivity.class).putExtra(EXTRA_NAME, name));
        });
    }

    private String validName() {
        String name = String.valueOf(binding.nameInput.getText()).trim();
        boolean empty = name.isEmpty();
        binding.nameError.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
        return empty ? null : name;
    }
}
