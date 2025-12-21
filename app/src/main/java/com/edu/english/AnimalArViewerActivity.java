package com.edu.english;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.data.AnimalArRepository;
import com.edu.english.model.AnimalArItem;

import java.util.Locale;

/**
 * Activity for viewing animals with sound and TTS pronunciation
 * Features:
 * - Animal image display with name (EN + VI)
 * - Animal sound playback
 * - TTS pronunciation every 5 seconds
 * - Lifecycle-aware resource management
 * 
 * Note: AR mode is disabled in this version for stability.
 * This uses 2D display mode with full audio/TTS features.
 */
public class AnimalArViewerActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String TAG = "AnimalArViewer";
    private static final long TTS_INTERVAL_MS = 5000; // 5 seconds

    // Views
    private FrameLayout arContainer;
    private FrameLayout arSceneViewContainer;
    private LinearLayout fallback2dContainer;
    private ImageView imgAnimal2d;
    private TextView txtAnimalName;
    private TextView txtAnimalNameVi;
    private TextView txtAnimalName2d;
    private TextView txtAnimalNameVi2d;
    private TextView txtArInstruction;
    private LinearLayout btnReplaySound;
    private LinearLayout btnToggleTts;
    private LinearLayout btnExit;
    private TextView txtTtsIcon;
    private TextView txtTtsLabel;

    // Data
    private AnimalArItem currentAnimal;

    // Audio & TTS
    private MediaPlayer mediaPlayer;
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;
    private boolean isTtsLoopEnabled = true;
    private Handler ttsHandler;
    private Runnable ttsRunnable;
    private boolean isActivityResumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_animal_ar_viewer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get animal ID from intent
        String animalId = getIntent().getStringExtra(AnimalArSelectActivity.EXTRA_ANIMAL_ID);
        if (animalId == null) {
            Toast.makeText(this, "Error: No animal selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get animal data
        currentAnimal = AnimalArRepository.getInstance().getAnimalById(animalId);
        if (currentAnimal == null) {
            Toast.makeText(this, "Error: Animal not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Initialize TTS
        initTts();

        // Initialize handler for TTS loop
        ttsHandler = new Handler(Looper.getMainLooper());

        // Setup 2D display mode (AR disabled for stability)
        setup2dMode();

        // Setup button listeners
        setupButtons();

        // Update UI with animal data
        updateAnimalInfo();
    }

    private void initViews() {
        arContainer = findViewById(R.id.ar_container);
        arSceneViewContainer = findViewById(R.id.ar_scene_view_container);
        fallback2dContainer = findViewById(R.id.fallback_2d_container);
        imgAnimal2d = findViewById(R.id.img_animal_2d);
        txtAnimalName = findViewById(R.id.txt_animal_name);
        txtAnimalNameVi = findViewById(R.id.txt_animal_name_vi);
        txtAnimalName2d = findViewById(R.id.txt_animal_name_2d);
        txtAnimalNameVi2d = findViewById(R.id.txt_animal_name_vi_2d);
        txtArInstruction = findViewById(R.id.txt_ar_instruction);
        btnReplaySound = findViewById(R.id.btn_replay_sound);
        btnToggleTts = findViewById(R.id.btn_toggle_tts);
        btnExit = findViewById(R.id.btn_exit);
        txtTtsIcon = findViewById(R.id.txt_tts_icon);
        txtTtsLabel = findViewById(R.id.txt_tts_label);
    }

    private void initTts() {
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS: English language not supported");
                isTtsReady = false;
            } else {
                isTtsReady = true;
                // Start TTS loop after initialization if activity is resumed
                if (isActivityResumed) {
                    startTtsLoop();
                }
            }
        } else {
            Log.e(TAG, "TTS initialization failed");
            isTtsReady = false;
        }
    }

    private void setup2dMode() {
        // Hide AR container, show 2D mode
        if (arSceneViewContainer != null) {
            arSceneViewContainer.setVisibility(View.GONE);
        }
        fallback2dContainer.setVisibility(View.VISIBLE);
        txtArInstruction.setVisibility(View.GONE);

        // Set animal image
        imgAnimal2d.setImageResource(currentAnimal.getThumbResId());
        txtAnimalName2d.setText(currentAnimal.getNameEn());
        txtAnimalNameVi2d.setText(currentAnimal.getNameVi());

        // Play sound on start (after a short delay to let UI render)
        new Handler(Looper.getMainLooper()).postDelayed(this::playAnimalSound, 500);
    }

    private void updateAnimalInfo() {
        txtAnimalName.setText(currentAnimal.getNameEn());
        txtAnimalNameVi.setText(currentAnimal.getNameVi());
    }

    private void setupButtons() {
        // Replay sound button
        btnReplaySound.setOnClickListener(v -> {
            animateButton(v);
            playAnimalSound();
        });

        // Toggle TTS button
        btnToggleTts.setOnClickListener(v -> {
            animateButton(v);
            toggleTtsLoop();
        });

        // Exit button
        btnExit.setOnClickListener(v -> {
            animateButton(v);
            finish();
        });

        // Update TTS button state
        updateTtsButtonState();
    }

    private void animateButton(View v) {
        v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    // ==================== AUDIO PLAYBACK ====================

    private void playAnimalSound() {
        // Release previous MediaPlayer if exists
        releaseMediaPlayer();

        // Always use TTS for pronunciation (sound files not included yet)
        if (isTtsReady) {
            textToSpeech.speak(currentAnimal.getNameEn(), TextToSpeech.QUEUE_FLUSH, null, "animal_sound");
        }

        // Show a toast with animal emoji
        String emoji = getAnimalEmoji(currentAnimal.getId());
        Toast.makeText(this, emoji + " " + currentAnimal.getNameEn() + "!", Toast.LENGTH_SHORT).show();
    }

    private String getAnimalEmoji(String animalId) {
        switch (animalId) {
            case "cat": return "🐱";
            case "dog": return "🐶";
            case "lion": return "🦁";
            case "elephant": return "🐘";
            case "monkey": return "🐵";
            case "bird": return "🐦";
            default: return "🐾";
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaPlayer", e);
            }
            mediaPlayer = null;
        }
    }

    // ==================== TTS LOOP ====================

    private void startTtsLoop() {
        if (!isTtsReady || !isTtsLoopEnabled) return;

        // Cancel any existing runnable
        stopTtsLoop();

        // Create runnable for TTS loop
        ttsRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTtsReady && isTtsLoopEnabled && isActivityResumed) {
                    speakAnimalName();
                    ttsHandler.postDelayed(this, TTS_INTERVAL_MS);
                }
            }
        };

        // Start first TTS after a short delay
        ttsHandler.postDelayed(ttsRunnable, 1500);
    }

    private void stopTtsLoop() {
        if (ttsHandler != null && ttsRunnable != null) {
            ttsHandler.removeCallbacks(ttsRunnable);
        }
        if (textToSpeech != null) {
            try {
                textToSpeech.stop();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping TTS", e);
            }
        }
    }

    private void speakAnimalName() {
        if (textToSpeech != null && isTtsReady && currentAnimal != null) {
            // Use QUEUE_FLUSH to prevent overlapping speech
            textToSpeech.speak(currentAnimal.getNameEn(), TextToSpeech.QUEUE_FLUSH, null, "animal_name");
        }
    }

    private void toggleTtsLoop() {
        isTtsLoopEnabled = !isTtsLoopEnabled;

        if (isTtsLoopEnabled) {
            startTtsLoop();
            Toast.makeText(this, "Đã bật phát âm tự động", Toast.LENGTH_SHORT).show();
        } else {
            stopTtsLoop();
            Toast.makeText(this, "Đã tắt phát âm tự động", Toast.LENGTH_SHORT).show();
        }

        updateTtsButtonState();
    }

    private void updateTtsButtonState() {
        if (isTtsLoopEnabled) {
            txtTtsIcon.setText("🗣️");
            txtTtsLabel.setText("Tắt phát âm");
        } else {
            txtTtsIcon.setText("🔇");
            txtTtsLabel.setText("Bật phát âm");
        }
    }

    // ==================== LIFECYCLE ====================

    @Override
    protected void onResume() {
        super.onResume();
        isActivityResumed = true;

        // Resume TTS loop if enabled
        if (isTtsLoopEnabled && isTtsReady) {
            startTtsLoop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityResumed = false;

        // Stop TTS loop
        stopTtsLoop();

        // Stop any playing audio
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error pausing audio", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up TTS
        if (textToSpeech != null) {
            try {
                textToSpeech.stop();
                textToSpeech.shutdown();
            } catch (Exception e) {
                Log.e(TAG, "Error shutting down TTS", e);
            }
            textToSpeech = null;
        }

        // Clean up MediaPlayer
        releaseMediaPlayer();

        // Clean up handler
        if (ttsHandler != null) {
            ttsHandler.removeCallbacksAndMessages(null);
        }
    }
}
