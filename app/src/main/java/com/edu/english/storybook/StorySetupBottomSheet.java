package com.edu.english.storybook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edu.english.BuildConfig;
import com.edu.english.R;
import com.edu.english.storybook.api.StoryGeneratorService;
import com.edu.english.storybook.model.Story;
import com.edu.english.storybook.model.StoryCategory;
import com.edu.english.storybook.model.StorySetup;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

/**
 * Bottom sheet for story setup configuration.
 * Allows user to select age, level, length, and optional theme/character.
 */
public class StorySetupBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_CATEGORY = "category";

    private StoryCategory category;
    private int age = 7;
    
    private TextView tvCategory;
    private TextView tvAge;
    private MaterialButtonToggleGroup toggleLevel;
    private ChipGroup chipGroupLength;
    private TextInputEditText etTheme;
    private TextInputEditText etCharacter;
    private MaterialButton btnGenerate;
    private MaterialButton btnSurprise;
    private LinearLayout loadingContainer;
    private LinearProgressIndicator progressBar;
    private TextView tvProgress;

    private StoryGeneratorService generatorService;
    private Handler mainHandler;

    public static StorySetupBottomSheet newInstance(StoryCategory category) {
        StorySetupBottomSheet fragment = new StorySetupBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = StoryCategory.valueOf(getArguments().getString(ARG_CATEGORY, "NOVEL"));
        }
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Get API key from BuildConfig (injected from local.properties)
        String apiKey = "";
        try {
            apiKey = BuildConfig.GEMINI_API_KEY;
        } catch (Exception e) {
            // API key not configured
        }
        generatorService = new StoryGeneratorService(apiKey);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_story_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        bindViews(view);
        setupUI();
        setupListeners();
    }

    private void bindViews(View view) {
        tvCategory = view.findViewById(R.id.tv_category);
        tvAge = view.findViewById(R.id.tv_age);
        toggleLevel = view.findViewById(R.id.toggle_level);
        chipGroupLength = view.findViewById(R.id.chip_group_length);
        etTheme = view.findViewById(R.id.et_theme);
        etCharacter = view.findViewById(R.id.et_character);
        btnGenerate = view.findViewById(R.id.btn_generate);
        btnSurprise = view.findViewById(R.id.btn_surprise);
        loadingContainer = view.findViewById(R.id.loading_container);
        progressBar = view.findViewById(R.id.progress_bar);
        tvProgress = view.findViewById(R.id.tv_progress);
    }

    private void setupUI() {
        tvCategory.setText("Category: " + category.getDisplayName());
        updateAgeDisplay();
        
        // Select default level
        toggleLevel.check(R.id.btn_easy);
        
        // Select default length
        chipGroupLength.check(R.id.chip_medium);
    }

    private void setupListeners() {
        ImageButton btnAgeMinus = requireView().findViewById(R.id.btn_age_minus);
        ImageButton btnAgePlus = requireView().findViewById(R.id.btn_age_plus);

        btnAgeMinus.setOnClickListener(v -> {
            if (age > 4) {
                age--;
                updateAgeDisplay();
            }
        });

        btnAgePlus.setOnClickListener(v -> {
            if (age < 12) {
                age++;
                updateAgeDisplay();
            }
        });

        btnGenerate.setOnClickListener(v -> generateStory(false));
        btnSurprise.setOnClickListener(v -> generateStory(true));
    }

    private void updateAgeDisplay() {
        tvAge.setText(age + " years");
    }

    private void generateStory(boolean surprise) {
        StorySetup setup = buildSetup(surprise);
        
        // Show loading state
        showLoading(true);
        updateProgress("Creating story outline...", 10);

        generatorService.generateStory(setup, new StoryGeneratorService.StoryCallback() {
            @Override
            public void onOutlineGenerated() {
                mainHandler.post(() -> updateProgress("Writing chapters...", 30));
            }

            @Override
            public void onChapterGenerated(int chapterIndex, int totalChapters) {
                mainHandler.post(() -> {
                    int progress = 30 + (int)((chapterIndex + 1) * 50.0 / totalChapters);
                    updateProgress("Writing chapter " + (chapterIndex + 1) + " of " + totalChapters + "...", progress);
                });
            }

            @Override
            public void onVocabQuizGenerated() {
                mainHandler.post(() -> updateProgress("Adding vocabulary and quiz...", 95));
            }

            @Override
            public void onSuccess(Story story) {
                mainHandler.post(() -> {
                    updateProgress("Story ready!", 100);
                    mainHandler.postDelayed(() -> {
                        showLoading(false);
                        openStoryReader(story);
                    }, 500);
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private StorySetup buildSetup(boolean surprise) {
        String level = toggleLevel.getCheckedButtonId() == R.id.btn_easy ? "A1" : "A2";
        
        String length = "medium";
        int checkedChipId = chipGroupLength.getCheckedChipId();
        if (checkedChipId == R.id.chip_short) {
            length = "short";
        } else if (checkedChipId == R.id.chip_long) {
            length = "long";
        }

        String theme = "";
        String character = "";

        if (surprise) {
            // Generate random theme from category suggestions
            String[] suggestions = category.getThemeSuggestions();
            theme = suggestions[new Random().nextInt(suggestions.length)];
            character = getRandomCharacterName();
        } else {
            theme = etTheme.getText() != null ? etTheme.getText().toString().trim() : "";
            character = etCharacter.getText() != null ? etCharacter.getText().toString().trim() : "";
        }

        return new StorySetup(category, age, level, length, theme, character);
    }

    private String getRandomCharacterName() {
        String[] names = {"Luna", "Max", "Milo", "Bella", "Finn", "Lily", "Leo", "Sophie", "Oliver", "Emma"};
        return names[new Random().nextInt(names.length)];
    }

    private void showLoading(boolean show) {
        loadingContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!show);
        btnSurprise.setEnabled(!show);
        setCancelable(!show);
    }

    private void updateProgress(String message, int progress) {
        tvProgress.setText(message);
        progressBar.setProgress(progress);
    }

    private void openStoryReader(Story story) {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, StoryReaderActivity.class);
            intent.putExtra(StoryReaderActivity.EXTRA_STORY_JSON, story.toJson());
            startActivity(intent);
            dismiss();
        }
    }
}
