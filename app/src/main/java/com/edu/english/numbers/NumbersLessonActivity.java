package com.edu.english.numbers;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.edu.english.R;
import com.edu.english.numbers.data.NumberSceneDataProvider;
import com.edu.english.numbers.models.NumberQuestion;
import com.edu.english.numbers.models.NumberScene;
import com.edu.english.numbers.utils.NumberTTSManager;
import com.edu.english.numbers.utils.QuestionRandomizer;
import com.edu.english.numbers.utils.SpeechRecognizerManager;
import com.edu.english.numbers.views.SceneCanvasView;

/**
 * Numbers Lesson Activity
 * 
 * Gameplay flow:
 * 1. COUNT_STATE: Show scene image + question, user selects a number
 * 2. SPEAK_STATE: User must pronounce the number word correctly
 * 3. Move to next round when both states are completed
 * 
 * Features:
 * - TTS for pronunciation samples
 * - Speech recognition for pronunciation verification
 * - Fallback for devices without speech recognition
 */
public class NumbersLessonActivity extends AppCompatActivity {

    private static final String TAG = "NumbersLessonActivity";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1001;

    // Game States
    private enum GameState {
        COUNT_STATE,    // User is counting and selecting a number
        SPEAK_STATE     // User is pronouncing the number word
    }

    // Current state
    private GameState currentState = GameState.COUNT_STATE;
    private NumberQuestion currentQuestion;
    private QuestionRandomizer questionRandomizer;
    private int roundsCompleted = 0;

    // Managers
    private NumberTTSManager ttsManager;
    private SpeechRecognizerManager speechManager;
    private Handler handler = new Handler(Looper.getMainLooper());

    // Views
    private ImageButton btnBack;
    private TextView tvTitle;
    private TextView tvProgress;
    private CardView cardScene;
    private SceneCanvasView sceneView;
    private TextView tvQuestion;
    
    // COUNT_STATE views
    private LinearLayout containerCountState;
    private TextView[] numberButtons = new TextView[10];
    
    // SPEAK_STATE views
    private LinearLayout containerSpeakState;
    private TextView tvSpeakInstruction;
    private TextView tvWordToSay;
    private LinearLayout btnListen;
    private LinearLayout btnMic;
    private ImageView ivMic;
    private TextView tvMicLabel;
    private Button btnSkipSpeech;
    
    // Feedback overlay
    private FrameLayout overlayFeedback;
    private TextView tvFeedbackEmoji;
    private TextView tvFeedbackText;

    // Flags
    private boolean isSpeechAvailable = true;
    private boolean isListening = false;
    private int speechAttempts = 0;
    private static final int MAX_SPEECH_ATTEMPTS_BEFORE_FALLBACK = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen setup
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_numbers_lesson);
        
        hideSystemBars();
        initViews();
        initData();
        initTTS();
        initSpeechRecognizer();
        setupClickListeners();
        setupBackHandler();
        
        // Start game
        loadNextQuestion();
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvProgress = findViewById(R.id.tvProgress);
        cardScene = findViewById(R.id.cardScene);
        sceneView = findViewById(R.id.sceneView);
        tvQuestion = findViewById(R.id.tvQuestion);
        
        // COUNT_STATE
        containerCountState = findViewById(R.id.containerCountState);
        numberButtons[0] = findViewById(R.id.btnNum1);
        numberButtons[1] = findViewById(R.id.btnNum2);
        numberButtons[2] = findViewById(R.id.btnNum3);
        numberButtons[3] = findViewById(R.id.btnNum4);
        numberButtons[4] = findViewById(R.id.btnNum5);
        numberButtons[5] = findViewById(R.id.btnNum6);
        numberButtons[6] = findViewById(R.id.btnNum7);
        numberButtons[7] = findViewById(R.id.btnNum8);
        numberButtons[8] = findViewById(R.id.btnNum9);
        numberButtons[9] = findViewById(R.id.btnNum10);
        
        // SPEAK_STATE
        containerSpeakState = findViewById(R.id.containerSpeakState);
        tvSpeakInstruction = findViewById(R.id.tvSpeakInstruction);
        tvWordToSay = findViewById(R.id.tvWordToSay);
        btnListen = findViewById(R.id.btnListen);
        btnMic = findViewById(R.id.btnMic);
        ivMic = findViewById(R.id.ivMic);
        tvMicLabel = findViewById(R.id.tvMicLabel);
        btnSkipSpeech = findViewById(R.id.btnSkipSpeech);
        
        // Feedback overlay
        overlayFeedback = findViewById(R.id.overlayFeedback);
        tvFeedbackEmoji = findViewById(R.id.tvFeedbackEmoji);
        tvFeedbackText = findViewById(R.id.tvFeedbackText);
    }

    private void initData() {
        NumberSceneDataProvider dataProvider = NumberSceneDataProvider.getInstance();
        questionRandomizer = new QuestionRandomizer(dataProvider.getAllQuestions());
    }

    private void initTTS() {
        ttsManager = new NumberTTSManager(this, new NumberTTSManager.OnTTSListener() {
            @Override
            public void onTTSReady() {
                // TTS is ready
            }

            @Override
            public void onSpeechStart() {
                // TTS started speaking
            }

            @Override
            public void onSpeechDone() {
                // TTS finished speaking
            }

            @Override
            public void onSpeechError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(NumbersLessonActivity.this, 
                            "TTS Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void initSpeechRecognizer() {
        speechManager = new SpeechRecognizerManager(this, new SpeechRecognizerManager.OnSpeechResultListener() {
            @Override
            public void onSpeechReady() {
                runOnUiThread(() -> {
                    ivMic.setBackgroundResource(R.drawable.bg_mic_button_listening);
                    tvMicLabel.setText("Listening...");
                });
            }

            @Override
            public void onSpeechStart() {
                runOnUiThread(() -> {
                    tvMicLabel.setText("Speaking...");
                });
            }

            @Override
            public void onSpeechResult(String result) {
                isListening = false;
                runOnUiThread(() -> {
                    resetMicButton();
                    handleSpeechResult(result);
                });
            }

            @Override
            public void onSpeechError(String error, boolean canRetry) {
                isListening = false;
                runOnUiThread(() -> {
                    resetMicButton();
                    if (canRetry) {
                        Toast.makeText(NumbersLessonActivity.this, error, Toast.LENGTH_SHORT).show();
                    } else {
                        showSpeechUnavailableFallback();
                    }
                });
            }

            @Override
            public void onSpeechEnd() {
                isListening = false;
                runOnUiThread(() -> resetMicButton());
            }

            @Override
            public void onSpeechNotAvailable() {
                isSpeechAvailable = false;
                runOnUiThread(() -> showSpeechUnavailableFallback());
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        // Number buttons
        for (int i = 0; i < numberButtons.length; i++) {
            final int number = i + 1;
            numberButtons[i].setOnClickListener(v -> handleNumberClick(number, v));
        }
        
        // Listen button (TTS)
        btnListen.setOnClickListener(v -> {
            if (currentQuestion != null && ttsManager != null) {
                ttsManager.speakNumber(currentQuestion.getAnswerWord());
            }
        });
        
        // Mic button (Speech Recognition)
        btnMic.setOnClickListener(v -> handleMicClick());
        
        // Skip speech button (fallback)
        btnSkipSpeech.setOnClickListener(v -> {
            // User claims they said it - proceed to next round
            showCorrectSpeechFeedback();
        });
        
        // Feedback overlay tap to dismiss (if needed)
        overlayFeedback.setOnClickListener(v -> {
            // Do nothing - auto-dismiss
        });
    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cleanup();
                finish();
            }
        });
    }

    private void loadNextQuestion() {
        currentQuestion = questionRandomizer.getNextQuestion();
        currentState = GameState.COUNT_STATE;
        speechAttempts = 0;
        
        // Update progress
        tvProgress.setText(String.format("%d / %d", 
                roundsCompleted + 1, 
                questionRandomizer.getTotalQuestions()));
        
        // Load scene
        sceneView.setSceneById(currentQuestion.getSceneId());
        
        // Set question
        tvQuestion.setText(currentQuestion.getPrompt());
        
        // Show COUNT_STATE, hide SPEAK_STATE
        containerCountState.setVisibility(View.VISIBLE);
        containerSpeakState.setVisibility(View.GONE);
        
        // Reset all number buttons
        resetNumberButtons();
        
        // Animate scene card
        animateSceneEntry();
    }

    private void handleNumberClick(int number, View clickedView) {
        if (currentState != GameState.COUNT_STATE || currentQuestion == null) {
            return;
        }
        
        // Animate button
        animateButtonPress(clickedView);
        
        if (currentQuestion.isCorrectNumber(number)) {
            // Correct answer!
            clickedView.setBackgroundResource(R.drawable.bg_number_button_correct);
            showCountCorrectFeedback();
        } else {
            // Wrong answer
            clickedView.setBackgroundResource(R.drawable.bg_number_button_wrong);
            showCountWrongFeedback(clickedView);
        }
    }

    private void showCountCorrectFeedback() {
        showFeedback("🎉", "Great job!", true, () -> {
            // Transition to SPEAK_STATE
            transitionToSpeakState();
        });
    }

    private void showCountWrongFeedback(View wrongButton) {
        // Quick shake animation
        ObjectAnimator shake = ObjectAnimator.ofFloat(wrongButton, "translationX", 
                0, 10, -10, 10, -10, 5, -5, 0);
        shake.setDuration(400);
        shake.start();
        
        // Show brief feedback
        tvQuestion.setText("Try again! 🤔");
        
        // Reset button after delay
        handler.postDelayed(() -> {
            wrongButton.setBackgroundResource(R.drawable.bg_number_button);
            tvQuestion.setText(currentQuestion.getPrompt());
        }, 800);
    }

    private void transitionToSpeakState() {
        currentState = GameState.SPEAK_STATE;
        
        // Update UI
        containerCountState.setVisibility(View.GONE);
        containerSpeakState.setVisibility(View.VISIBLE);
        
        // Set word to pronounce
        String word = currentQuestion.getAnswerWord().toUpperCase();
        tvWordToSay.setText(word);
        
        // Update question text
        tvQuestion.setText("Say the number: " + currentQuestion.getAnswerNumber());
        
        // Show/hide fallback button based on speech availability
        if (!isSpeechAvailable) {
            showSpeechUnavailableFallback();
        } else {
            btnSkipSpeech.setVisibility(View.GONE);
        }
        
        // Auto-play TTS after short delay
        handler.postDelayed(() -> {
            if (ttsManager != null && currentState == GameState.SPEAK_STATE) {
                ttsManager.speakNumber(currentQuestion.getAnswerWord());
            }
        }, 500);
        
        // Animate entry
        animateSpeakStateEntry();
    }

    private void handleMicClick() {
        if (!isSpeechAvailable) {
            showSpeechUnavailableFallback();
            return;
        }
        
        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermission();
            return;
        }
        
        if (isListening) {
            // Stop listening
            speechManager.stopListening();
            isListening = false;
            resetMicButton();
        } else {
            // Stop TTS if playing
            if (ttsManager != null) {
                ttsManager.stop();
            }
            
            // Start listening
            isListening = true;
            speechManager.startListening();
        }
    }

    private void handleSpeechResult(String result) {
        if (currentQuestion == null || currentState != GameState.SPEAK_STATE) {
            return;
        }
        
        speechAttempts++;
        
        if (currentQuestion.isCorrectSpeech(result)) {
            // Correct pronunciation!
            showCorrectSpeechFeedback();
        } else {
            // Wrong pronunciation
            showWrongSpeechFeedback(result);
        }
    }

    private void showCorrectSpeechFeedback() {
        showFeedback("⭐", "Excellent!", true, () -> {
            // Move to next round
            roundsCompleted++;
            loadNextQuestion();
        });
    }

    private void showWrongSpeechFeedback(String whatWasHeard) {
        // Update instruction
        tvSpeakInstruction.setText("Not quite! Listen and try again:");
        
        // Show what was heard
        Toast.makeText(this, "Heard: \"" + whatWasHeard + "\"", Toast.LENGTH_SHORT).show();
        
        // Play TTS sample
        handler.postDelayed(() -> {
            if (ttsManager != null) {
                ttsManager.speakNumber(currentQuestion.getAnswerWord());
            }
        }, 500);
        
        // After several attempts, show fallback
        if (speechAttempts >= MAX_SPEECH_ATTEMPTS_BEFORE_FALLBACK) {
            handler.postDelayed(() -> {
                btnSkipSpeech.setVisibility(View.VISIBLE);
            }, 1500);
        }
    }

    private void showSpeechUnavailableFallback() {
        isSpeechAvailable = false;
        btnMic.setAlpha(0.5f);
        tvMicLabel.setText("Not available");
        btnSkipSpeech.setVisibility(View.VISIBLE);
        
        // Show explanation dialog (only once)
        new AlertDialog.Builder(this)
                .setTitle("Speech Recognition")
                .setMessage("Voice recognition is not available on this device. " +
                        "You can listen to the pronunciation and tap 'I said it!' to continue.")
                .setPositiveButton("Got it", null)
                .show();
    }

    private void requestAudioPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, 
                Manifest.permission.RECORD_AUDIO)) {
            // Explain why we need the permission
            new AlertDialog.Builder(this)
                    .setTitle("Microphone Permission")
                    .setMessage("We need microphone access to hear your pronunciation. " +
                            "This helps us check if you're saying the numbers correctly!")
                    .setPositiveButton("Grant", (d, w) -> {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                PERMISSION_REQUEST_RECORD_AUDIO);
                    })
                    .setNegativeButton("Cancel", (d, w) -> {
                        showSpeechUnavailableFallback();
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start listening
                handler.postDelayed(() -> handleMicClick(), 300);
            } else {
                // Permission denied
                showSpeechUnavailableFallback();
            }
        }
    }

    // ============ UI Helpers ============

    private void resetNumberButtons() {
        for (TextView btn : numberButtons) {
            btn.setBackgroundResource(R.drawable.bg_number_button);
        }
    }

    private void resetMicButton() {
        ivMic.setBackgroundResource(R.drawable.bg_mic_button);
        tvMicLabel.setText("Tap to speak");
    }

    private void showFeedback(String emoji, String text, boolean isPositive, Runnable onDismiss) {
        tvFeedbackEmoji.setText(emoji);
        tvFeedbackText.setText(text);
        
        // Show overlay with animation
        overlayFeedback.setAlpha(0f);
        overlayFeedback.setVisibility(View.VISIBLE);
        overlayFeedback.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
        
        // Scale animation for emoji
        tvFeedbackEmoji.setScaleX(0f);
        tvFeedbackEmoji.setScaleY(0f);
        tvFeedbackEmoji.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .start();
        
        // Auto-dismiss after delay
        handler.postDelayed(() -> {
            overlayFeedback.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            overlayFeedback.setVisibility(View.GONE);
                            overlayFeedback.animate().setListener(null);
                            if (onDismiss != null) {
                                onDismiss.run();
                            }
                        }
                    })
                    .start();
        }, isPositive ? 1200 : 800);
    }

    private void animateButtonPress(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f);
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    private void animateSceneEntry() {
        cardScene.setTranslationY(-50f);
        cardScene.setAlpha(0f);
        cardScene.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateSpeakStateEntry() {
        containerSpeakState.setTranslationY(50f);
        containerSpeakState.setAlpha(0f);
        containerSpeakState.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    // ============ Lifecycle ============

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop ongoing operations
        if (ttsManager != null) {
            ttsManager.stop();
        }
        if (speechManager != null) {
            speechManager.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        cleanup();
        super.onDestroy();
    }

    private void cleanup() {
        handler.removeCallbacksAndMessages(null);
        
        if (ttsManager != null) {
            ttsManager.shutdown();
            ttsManager = null;
        }
        
        if (speechManager != null) {
            speechManager.destroy();
            speechManager = null;
        }
    }
}
