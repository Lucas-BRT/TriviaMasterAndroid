package com.trivia.masterandroid.ui;

import android.os.Bundle;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.databinding.ActivityDevBinding;
import com.trivia.masterandroid.util.AppExecutors;
import com.trivia.masterandroid.util.DevSettings;
import com.trivia.masterandroid.util.Dialogs;
import com.trivia.masterandroid.util.SystemBars;

/** Hidden developer options: handy while testing on devices, never linked from the UI. */
public class DevSettingsActivity extends AppCompatActivity {

    private ActivityDevBinding binding;
    private LocalDataSource local;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDevBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.enable(this);
        SystemBars.pad(binding.content, true, true);
        local = new LocalDataSource(this);

        binding.switchOffline.setChecked(DevSettings.forceOffline(this));
        binding.switchOffline.setOnCheckedChangeListener((b, on) -> DevSettings.setForceOffline(this, on));
        binding.switchSound.setChecked(DevSettings.sound(this));
        binding.switchSound.setOnCheckedChangeListener((b, on) -> DevSettings.setSound(this, on));
        binding.switchHaptics.setChecked(DevSettings.haptics(this));
        binding.switchHaptics.setOnCheckedChangeListener((b, on) -> DevSettings.setHaptics(this, on));

        binding.timeSeek.setProgress(DevSettings.secondsPerQuestion(this) - DevSettings.MIN_SECONDS);
        showTime(binding.timeSeek.getProgress());
        binding.timeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                showTime(progress);
                if (fromUser) {
                    DevSettings.setSecondsPerQuestion(DevSettingsActivity.this, DevSettings.MIN_SECONDS + progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        binding.btnClearCache.setOnClickListener(v -> Dialogs.confirm(this, 0, R.string.dev_confirm_cache,
                R.string.dev_clear, () -> AppExecutors.io(() -> {
                    local.clearCachedQuestions();
                    refreshCount();
                }), R.string.btn_cancel));
        binding.btnClearHistory.setOnClickListener(v -> Dialogs.confirm(this, 0, R.string.dev_confirm_history,
                R.string.dev_erase, () -> AppExecutors.io(local::clearMatches), R.string.btn_cancel));
        refreshCount();
    }

    private void showTime(int progress) {
        binding.timeValue.setText(getString(R.string.quiz_seconds, DevSettings.MIN_SECONDS + progress));
    }

    private void refreshCount() {
        AppExecutors.io(() -> {
            int count = local.countCachedQuestions();
            AppExecutors.main(() -> {
                if (!isFinishing()) {
                    binding.cacheCount.setText(getString(R.string.dev_cache_count, count));
                }
            });
        });
    }
}
