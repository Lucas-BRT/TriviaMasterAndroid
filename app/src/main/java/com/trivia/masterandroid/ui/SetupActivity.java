package com.trivia.masterandroid.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.trivia.masterandroid.R;
import com.trivia.masterandroid.data.local.LocalDataSource;
import com.trivia.masterandroid.data.repo.TriviaException;
import com.trivia.masterandroid.data.repo.TriviaRepository;
import com.trivia.masterandroid.util.SystemBars;
import com.trivia.masterandroid.databinding.ActivitySetupBinding;
import com.trivia.masterandroid.model.Category;
import com.trivia.masterandroid.model.GameSettings;
import com.trivia.masterandroid.model.Question;
import com.trivia.masterandroid.receiver.NetworkReceiver;
import com.trivia.masterandroid.util.AppExecutors;
import com.trivia.masterandroid.util.Dialogs;
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
    private Category selectedCategory;
    private CategoryAdapter pickerAdapter;
    private String playerName;
    private long playerId = -1;
    private boolean starting;
    private int loadToken;
    private final android.os.Handler hintHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.enable(this);
        SystemBars.pad(binding.content, true, true);
        repository = new TriviaRepository(this);

        playerName = getIntent().getStringExtra(PlayerActivity.EXTRA_NAME);
        binding.greeting.setText(getString(R.string.setup_greeting, playerName));
        binding.timeInfo.setText(getString(R.string.setup_time_info, GameSettings.TIME_PER_QUESTION_SECONDS));

        setupDifficulty();
        setupAmount();
        loadCategories();
        registerPlayer();

        binding.btnStart.setOnClickListener(v -> startGame());
        binding.btnCancelLoading.setOnClickListener(v -> {
            loadToken++;
            setLoading(false);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        networkReceiver = new NetworkReceiver(online ->
                binding.offlineBanner.setVisibility(online ? View.GONE : View.VISIBLE));
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        binding.offlineBanner.setVisibility(NetworkReceiver.isOnline(this) ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        hintHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
            networkReceiver = null;
        }
        super.onStop();
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
            b.setButtonDrawable(null);
            b.setBackgroundResource(R.drawable.bg_chip);
            b.setTextColor(getColorStateList(R.color.chip_text));
            b.setTextSize(14);
            b.setGravity(android.view.Gravity.CENTER);
            b.setMinHeight(getResources().getDimensionPixelSize(R.dimen.chip_height));
            RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMarginEnd(i < DIFFICULTY_KEYS.length - 1 ? (int) (8 * getResources().getDisplayMetrics().density) : 0);
            binding.difficultyGroup.addView(b, lp);
        }
        binding.difficultyGroup.check(binding.difficultyGroup.getChildAt(0).getId());
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
        categories.add(new Category(0, getString(R.string.category_any)));
        selectCategory(categories.get(0));
        binding.categoryField.setOnClickListener(v -> showCategoryPicker());
        repository.loadCategories(new TriviaRepository.Callback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result, boolean fromCache) {
                categories.addAll(result);
                if (pickerAdapter != null) {
                    pickerAdapter.notifyDataSetChanged();
                }
                binding.offlineBanner.setVisibility(fromCache ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(TriviaException error) {
                binding.offlineBanner.setVisibility(View.VISIBLE);
            }
        });
    }

    private void selectCategory(Category category) {
        selectedCategory = category;
        binding.categoryValue.setText(category.name);
        binding.categoryField.setContentDescription(getString(R.string.setup_category) + ": " + category.name);
    }

    private void showCategoryPicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.sheet_categories, null);
        RecyclerView list = sheet.findViewById(R.id.category_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        pickerAdapter = new CategoryAdapter(categories, selectedCategory.id, c -> {
            selectCategory(c);
            dialog.dismiss();
        });
        list.setAdapter(pickerAdapter);
        dialog.setOnDismissListener(d -> pickerAdapter = null);
        list.getLayoutParams().height = (int) (getResources().getDisplayMetrics().heightPixels * 0.55f);
        list.scrollToPosition(Math.max(0, categories.indexOf(selectedCategory)));
        dialog.setContentView(sheet);
        dialog.setOnShowListener(d -> {
            FrameLayout container = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (container != null) {
                container.setBackgroundColor(Color.TRANSPARENT);
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(container);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        if (dialog.getWindow() != null) {
            dialog.getWindow().setNavigationBarColor(getColor(R.color.paper));
        }
        dialog.show();
    }

    private void startGame() {
        if (starting) {
            return;
        }
        Category category = selectedCategory;
        RadioButton checked = binding.difficultyGroup.findViewById(binding.difficultyGroup.getCheckedRadioButtonId());
        String difficulty = checked == null ? "" : (String) checked.getTag();
        int amount = binding.amountSeek.getProgress() + MIN_AMOUNT;
        GameSettings settings = new GameSettings(playerName, playerId, category.id, category.name, difficulty, amount);

        setLoading(true);
        final int token = ++loadToken;
        repository.loadQuestions(settings, new TriviaRepository.Callback<List<Question>>() {
            @Override
            public void onSuccess(List<Question> result, boolean fromCache) {
                if (token != loadToken || isFinishing()) {
                    return;
                }
                setLoading(false);
                binding.offlineBanner.setVisibility(fromCache ? View.VISIBLE : View.GONE);
                startActivity(new Intent(SetupActivity.this, QuizActivity.class)
                        .putExtra(QuizActivity.EXTRA_QUESTIONS, new ArrayList<>(result))
                        .putExtra(QuizActivity.EXTRA_SETTINGS, settings)
                        .putExtra(QuizActivity.EXTRA_OFFLINE, fromCache));
            }

            @Override
            public void onError(TriviaException error) {
                if (token != loadToken || isFinishing()) {
                    return;
                }
                setLoading(false);
                Dialogs.confirm(SetupActivity.this, 0, ErrorMessages.of(error),
                        R.string.btn_retry, () -> startGame(), R.string.btn_cancel);
            }
        });
    }

    private void setLoading(boolean loading) {
        starting = loading;
        binding.loading.setVisibility(loading ? View.VISIBLE : View.GONE);
        hintHandler.removeCallbacksAndMessages(null);
        binding.loadingHint.setAlpha(0f);
        if (loading) {
            binding.shimmer.startShimmer();
            hintHandler.postDelayed(() -> binding.loadingHint.animate().alpha(1f).setDuration(300).start(), 4000);
        } else {
            binding.shimmer.stopShimmer();
        }
    }
}
