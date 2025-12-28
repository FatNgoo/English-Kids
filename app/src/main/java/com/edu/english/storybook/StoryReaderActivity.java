package com.edu.english.storybook;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.edu.english.R;
import com.edu.english.storybook.data.StoryDatabase;
import com.edu.english.storybook.data.StoryEntity;
import com.edu.english.storybook.data.StoryRepository;
import com.edu.english.storybook.model.Chapter;
import com.edu.english.storybook.model.Story;
import com.edu.english.storybook.model.StoryCategory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Locale;

/**
 * Story Reader screen with chapter navigation, TTS, and themed frames.
 */
public class StoryReaderActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    public static final String EXTRA_STORY_JSON = "story_json";
    public static final String EXTRA_STORY_ID = "story_id";

    private Story story;
    private int currentChapterIndex = 0;
    private boolean isSaved = false;

    private FrameLayout frameBackground;
    private TextView tvCategoryBadge;
    private TextView tvTitle;
    private TextView tvReadingInfo;
    private ChipGroup chipGroupChapters;
    private TextView tvChapterHeading;
    private TextView tvChapterContent;
    private MaterialButton btnPrevChapter;
    private MaterialButton btnNextChapter;
    private ImageButton btnBack;
    private ImageButton btnBookmark;
    private ImageButton btnTtsPlay;
    private ImageButton btnTtsStop;
    private TextView tvTtsSpeed;
    private ImageButton btnVocab;
    private ImageButton btnQuiz;

    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;
    private boolean isSpeaking = false;
    private float ttsSpeed = 1.0f;

    private StoryRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_reader);

        repository = new StoryRepository(this);

        loadStory();
        if (story == null) {
            Toast.makeText(this, "Error loading story", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupUI();
        setupListeners();
        initTts();
    }

    private void loadStory() {
        String storyJson = getIntent().getStringExtra(EXTRA_STORY_JSON);
        long storyId = getIntent().getLongExtra(EXTRA_STORY_ID, -1);

        if (storyJson != null && !storyJson.isEmpty()) {
            story = Story.fromJson(storyJson);
            isSaved = false;
        } else if (storyId > 0) {
            // Load from database synchronously for simplicity
            // In production, use ViewModel with LiveData
            isSaved = true;
        }
    }

    private void bindViews() {
        frameBackground = findViewById(R.id.frame_background);
        tvCategoryBadge = findViewById(R.id.tv_category_badge);
        tvTitle = findViewById(R.id.tv_title);
        tvReadingInfo = findViewById(R.id.tv_reading_info);
        chipGroupChapters = findViewById(R.id.chip_group_chapters);
        tvChapterHeading = findViewById(R.id.tv_chapter_heading);
        tvChapterContent = findViewById(R.id.tv_chapter_content);
        btnPrevChapter = findViewById(R.id.btn_prev_chapter);
        btnNextChapter = findViewById(R.id.btn_next_chapter);
        btnBack = findViewById(R.id.btn_back);
        btnBookmark = findViewById(R.id.btn_bookmark);
        btnTtsPlay = findViewById(R.id.btn_tts_play);
        btnTtsStop = findViewById(R.id.btn_tts_stop);
        tvTtsSpeed = findViewById(R.id.tv_tts_speed);
        btnVocab = findViewById(R.id.btn_vocab);
        btnQuiz = findViewById(R.id.btn_quiz);
    }

    private void setupUI() {
        // Set background based on category
        setBackgroundForCategory(story.getCategory());

        // Set title and info
        tvTitle.setText(story.getTitle());
        tvCategoryBadge.setText(story.getCategory().getDisplayName());
        
        String info = "Age " + story.getTargetAge() + " • " + story.getLevel() + " Level • " + 
                      story.getChapters().size() + " chapters";
        tvReadingInfo.setText(info);

        // Setup chapter chips
        setupChapterChips();

        // Display first chapter
        displayChapter(0);

        // Update bookmark icon
        updateBookmarkIcon();
    }

    private void setBackgroundForCategory(StoryCategory category) {
        int bgRes;
        switch (category) {
            case FAIRY_TALES:
                bgRes = R.drawable.bg_reader_fairytales;
                break;
            case SEE_THE_WORLD:
                bgRes = R.drawable.bg_reader_world;
                break;
            case HISTORY:
                bgRes = R.drawable.bg_reader_history;
                break;
            default:
                bgRes = R.drawable.bg_reader_novel;
        }
        frameBackground.setBackgroundResource(bgRes);
    }

    private void setupChapterChips() {
        chipGroupChapters.removeAllViews();
        List<Chapter> chapters = story.getChapters();

        for (int i = 0; i < chapters.size(); i++) {
            Chip chip = new Chip(this);
            chip.setText(String.valueOf(i + 1));
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            
            final int index = i;
            chip.setOnClickListener(v -> {
                displayChapter(index);
                updateChipSelection(index);
            });
            
            chipGroupChapters.addView(chip);
        }
    }

    private void updateChipSelection(int selectedIndex) {
        for (int i = 0; i < chipGroupChapters.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupChapters.getChildAt(i);
            chip.setChecked(i == selectedIndex);
        }
    }

    private void displayChapter(int index) {
        if (index < 0 || index >= story.getChapters().size()) return;

        currentChapterIndex = index;
        Chapter chapter = story.getChapters().get(index);

        tvChapterHeading.setText(chapter.getHeading());
        tvChapterContent.setText(chapter.getContent());

        // Update navigation buttons
        btnPrevChapter.setEnabled(index > 0);
        btnNextChapter.setEnabled(index < story.getChapters().size() - 1);

        // Stop TTS when changing chapters
        stopTts();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnBookmark.setOnClickListener(v -> toggleSave());

        btnPrevChapter.setOnClickListener(v -> {
            if (currentChapterIndex > 0) {
                displayChapter(currentChapterIndex - 1);
                updateChipSelection(currentChapterIndex);
            }
        });

        btnNextChapter.setOnClickListener(v -> {
            if (currentChapterIndex < story.getChapters().size() - 1) {
                displayChapter(currentChapterIndex + 1);
                updateChipSelection(currentChapterIndex);
            }
        });

        btnTtsPlay.setOnClickListener(v -> playTts());
        btnTtsStop.setOnClickListener(v -> stopTts());

        tvTtsSpeed.setOnClickListener(v -> cycleTtsSpeed());

        btnVocab.setOnClickListener(v -> showVocabulary());
        btnQuiz.setOnClickListener(v -> showQuiz());
    }

    // === TTS Methods ===

    private void initTts() {
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && 
                         result != TextToSpeech.LANG_NOT_SUPPORTED;

            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    runOnUiThread(() -> {
                        isSpeaking = true;
                        updateTtsButtons();
                    });
                }

                @Override
                public void onDone(String utteranceId) {
                    runOnUiThread(() -> {
                        isSpeaking = false;
                        updateTtsButtons();
                    });
                }

                @Override
                public void onError(String utteranceId) {
                    runOnUiThread(() -> {
                        isSpeaking = false;
                        updateTtsButtons();
                    });
                }
            });
        }
    }

    private void playTts() {
        if (!isTtsReady) {
            Toast.makeText(this, "Text-to-speech not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSpeaking) {
            textToSpeech.stop();
            isSpeaking = false;
            updateTtsButtons();
            return;
        }

        Chapter chapter = story.getChapters().get(currentChapterIndex);
        String text = chapter.getHeading() + ". " + chapter.getContent();

        textToSpeech.setSpeechRate(ttsSpeed);
        Bundle params = new Bundle();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "chapter_" + currentChapterIndex);
    }

    private void stopTts() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            isSpeaking = false;
            updateTtsButtons();
        }
    }

    private void cycleTtsSpeed() {
        if (ttsSpeed == 1.0f) {
            ttsSpeed = 0.75f;
        } else if (ttsSpeed == 0.75f) {
            ttsSpeed = 0.5f;
        } else {
            ttsSpeed = 1.0f;
        }
        tvTtsSpeed.setText(ttsSpeed + "x");

        if (isSpeaking) {
            // Restart with new speed
            stopTts();
            playTts();
        }
    }

    private void updateTtsButtons() {
        btnTtsPlay.setImageResource(isSpeaking ? R.drawable.ic_stop : R.drawable.ic_play);
    }

    // === Save Methods ===

    private void toggleSave() {
        if (isSaved) {
            // Already saved, no unsave in this version
            Toast.makeText(this, "Story already in library", Toast.LENGTH_SHORT).show();
        } else {
            saveStory();
        }
    }

    private void saveStory() {
        StoryEntity entity = new StoryEntity(
            story.getTitle(),
            story.getCategory().name(),
            story.getTargetAge(),
            story.getLevel(),
            story.toJson()
        );

        repository.insert(entity, id -> {
            runOnUiThread(() -> {
                isSaved = true;
                updateBookmarkIcon();
                Toast.makeText(this, "Story saved to library!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateBookmarkIcon() {
        btnBookmark.setImageResource(isSaved ? R.drawable.ic_bookmark_dark : R.drawable.ic_bookmark_outline_dark);
    }

    // === Vocabulary & Quiz ===

    private void showVocabulary() {
        if (story.getVocabulary() == null || story.getVocabulary().isEmpty()) {
            Toast.makeText(this, "No vocabulary available", Toast.LENGTH_SHORT).show();
            return;
        }

        VocabularyBottomSheet sheet = VocabularyBottomSheet.newInstance(story.toJson());
        sheet.show(getSupportFragmentManager(), "VocabularyBottomSheet");
    }

    private void showQuiz() {
        if (story.getQuizQuestions() == null || story.getQuizQuestions().isEmpty()) {
            Toast.makeText(this, "No quiz available", Toast.LENGTH_SHORT).show();
            return;
        }

        QuizBottomSheet sheet = QuizBottomSheet.newInstance(story.toJson());
        sheet.show(getSupportFragmentManager(), "QuizBottomSheet");
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
