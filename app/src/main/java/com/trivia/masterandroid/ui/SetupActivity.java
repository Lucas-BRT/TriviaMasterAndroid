package com.trivia.masterandroid.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.data.repo.TriviaException;
import com.trivia.masterandroid.data.repo.TriviaRepository;
import com.trivia.masterandroid.databinding.ActivitySetupBinding;
import com.trivia.masterandroid.model.Category;
import com.trivia.masterandroid.model.GameSettings;
import com.trivia.masterandroid.model.Question;
import com.trivia.masterandroid.util.AppExecutors;
import com.trivia.masterandroid.util.ErrorMessages;

import java.util.ArrayList;
import java.util.List;

public class SetupActivity extends AppCompatActivity {

    private static final int MIN_AMOUNT = 5;
    private static final String[] DIFFICULTY_KEYS = {"", "easy", "medium", "hard"};
    private static final int[] DIFFICULTY_LABELS = {
            R.string.difficulty_any, R.string.difficulty_easy,
            R.string.difficulty_medium, R.string.difficulty_hard};

    private ActivitySetupBinding binding;
    private TriviaRepository repository;
    private final List<Category> categories = new ArrayList<>();
    private String playerName;
    private long playerId = -1;
    private boolean starting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = new TriviaRepository(this);

        playerName = getIntent().getStringExtra(PlayerActivity.EXTRA_NAME);
        binding.greeting.setText(getString(R.string.setup_greeting, playerName));
        binding.timeInfo.setText(getString(R.string.setup_time_info, GameSettings.TIME_PER_QUESTION_SECONDS));

        setupDifficulty();
        setupAmount();
        loadCategories();
        registerPlayer();

        binding.btnStart.setOnClickListener(v -> startGame());
    }

    private void registerPlayer() {
        LocalDataSource local = new LocalDataSource(this);
        AppExecutors.io(() -> playerId = local.getOrCreatePlayer(playerName));
    }

    private void setupDifficulty() {
        for (int i = 0; i < DIFFICULTY_KEYS.length; i++) {
            RadioButton b = new RadioButton(this);
            b.setId(View.generateViewId());
            b.setTag(DIFFICULTY_KEYS[i]);
            b.setText(DIFFICULTY_LABELS[i]);
            b.setButton(null);
            b.setBackgroundResource(R.drawable.bg_chip);
            b.setTextColor(getColorStateList(R.color.chip_text));
            b.setTextSize(14);
            b.setGravity(android.view.Gravity.CENTER);
            b.setMinHeight(getResources().getDimensionPixelSize(R.dimen.chip_height));
            RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMarginEnd(i < DIFFICULTY_KEYS.length - 1 ? 8 : 0);
            binding.difficultyGroup.addView(b, lp);
            if (i == 0) {
                binding.difficultyGroup.check(b.getId());
            }
        }
    }

    private void setupAmount() {
        updateAmount(binding.amountSeek.getProgress());
        binding.amountSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateAmount(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void updateAmount(int progress) {
        binding.amountValue.setText(getString(R.string.setup_amount_value, progress + MIN_AMOUNT));
    }

    private void loadCategories() {
        categories.add(new Category(0, getString(R.string.difficulty_any)));
        binding.categorySpinner.setAdapter(adapterFor(categories));
        repository.loadCategories(new TriviaRepository.Callback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result, boolean fromCache) {
                categories.addAll(result);
                binding.categorySpinner.setAdapter(adapterFor(categories));
                binding.offlineBanner.setVisibility(fromCache ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(TriviaException error) {
                binding.offlineBanner.setVisibility(View.VISIBLE);
            }
        });
    }

    private ArrayAdapter<Category> adapterFor(List<Category> list) {
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void startGame() {
        if (starting) {
            return;
        }
        Category category = (Category) binding.categorySpinner.getSelectedItem();
        RadioButton checked = binding.difficultyGroup.findViewById(binding.difficultyGroup.getCheckedRadioButtonId());
        String difficulty = checked == null ? "" : (String) checked.getTag();
        int amount = binding.amountSeek.getProgress() + MIN_AMOUNT;
        GameSettings settings = new GameSettings(playerName, playerId, category.id, category.name, difficulty, amount);

        setLoading(true);
        repository.loadQuestions(settings, new TriviaRepository.Callback<List<Question>>() {
            @Override
            public void onSuccess(List<Question> result, boolean fromCache) {
                setLoading(false);
                binding.offlineBanner.setVisibility(fromCache ? View.VISIBLE : View.GONE);
                // Quiz screen is added in a following step.
                Toast.makeText(SetupActivity.this, result.size() + " perguntas carregadas", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(TriviaException error) {
                setLoading(false);
                Toast.makeText(SetupActivity.this, ErrorMessages.of(error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        starting = loading;
        binding.loading.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            binding.shimmer.startShimmer();
        } else {
            binding.shimmer.stopShimmer();
        }
    }
}
