package com.trivia.masterandroid.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.databinding.ActivityPlayerBinding;
import com.trivia.masterandroid.model.PlayerStats;
import com.trivia.masterandroid.util.AppExecutors;
import com.trivia.masterandroid.util.SystemBars;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry screen with simple profiles: continue as the last player, pick another saved player,
 * or create a new one. Profiles live in the local database; only the last one is kept in prefs.
 */
public class PlayerActivity extends AppCompatActivity {

    private static final String PREFS = "trivia_prefs";
    private static final String KEY_NAME = "player_name";
    public static final String EXTRA_NAME = "extra_player_name";

    private ActivityPlayerBinding binding;
    private SharedPreferences prefs;
    private final List<PlayerStats> players = new ArrayList<>();
    private String currentName;
    private boolean chooserShown;
    private int secretTaps;
    private long lastSecretTap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.enable(this);
        SystemBars.pad(binding.content, true, true);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        currentName = prefs.getString(KEY_NAME, "");
        binding.players.setLayoutManager(new LinearLayoutManager(this));

        binding.headline.setOnClickListener(v -> countSecretTap());
        binding.btnContinue.setOnClickListener(v -> play(currentName));
        binding.btnSwitch.setOnClickListener(v -> showChooser());
        binding.btnCreate.setOnClickListener(v -> createPlayer());
        binding.btnHistory.setOnClickListener(v -> startActivity(
                new Intent(this, HistoryActivity.class).putExtra(EXTRA_NAME, currentName)));
        binding.nameInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createPlayer();
                return true;
            }
            return false;
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (chooserShown && hasCurrentPlayer()) {
                    showContinue();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalDataSource local = new LocalDataSource(this);
        AppExecutors.io(() -> {
            List<PlayerStats> loaded = local.getPlayers();
            AppExecutors.main(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                players.clear();
                players.addAll(loaded);
                if (chooserShown) {
                    showChooser();
                } else if (hasCurrentPlayer()) {
                    showContinue();
                } else {
                    showChooser();
                }
            });
        });
    }

    private boolean hasCurrentPlayer() {
        if (currentName.isEmpty()) {
            return false;
        }
        for (PlayerStats p : players) {
            if (p.name.equals(currentName)) {
                return true;
            }
        }
        return false;
    }

    private void showContinue() {
        chooserShown = false;
        binding.currentName.setText(currentName);
        binding.btnContinue.setText(getString(com.trivia.masterandroid.R.string.continue_as, currentName));
        swap(binding.continueGroup, binding.chooserGroup);
    }

    private void showChooser() {
        chooserShown = true;
        boolean any = !players.isEmpty();
        binding.playersLabel.setVisibility(any ? View.VISIBLE : View.GONE);
        binding.players.setVisibility(any ? View.VISIBLE : View.GONE);
        binding.players.setAdapter(new PlayerAdapter(new ArrayList<>(players), currentName, p -> play(p.name)));
        binding.nameError.setVisibility(View.GONE);
        swap(binding.chooserGroup, binding.continueGroup);
    }

    /** Cross-fade between the two groups. */
    private void swap(View show, View hide) {
        hide.setVisibility(View.GONE);
        if (show.getVisibility() != View.VISIBLE) {
            show.setAlpha(0f);
            show.setVisibility(View.VISIBLE);
            show.animate().alpha(1f).setDuration(200).start();
        }
    }

    private void createPlayer() {
        String name = String.valueOf(binding.nameInput.getText()).trim();
        if (name.isEmpty()) {
            binding.nameError.setVisibility(View.VISIBLE);
            return;
        }
        for (PlayerStats p : players) {
            if (p.name.equalsIgnoreCase(name)) {
                name = p.name; // same person typed again: reuse the existing profile
            }
        }
        play(name);
    }

    private void play(String name) {
        currentName = name;
        prefs.edit().putString(KEY_NAME, name).apply();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.nameInput.getWindowToken(), 0);
        binding.nameInput.setText("");
        startActivity(new Intent(this, SetupActivity.class).putExtra(EXTRA_NAME, name));
    }

    /** Seven quick taps on the title open the hidden developer options. */
    private void countSecretTap() {
        long now = SystemClock.uptimeMillis();
        secretTaps = now - lastSecretTap > 1500 ? 1 : secretTaps + 1;
        lastSecretTap = now;
        if (secretTaps >= 7) {
            secretTaps = 0;
            startActivity(new Intent(this, DevSettingsActivity.class));
        }
    }
}
