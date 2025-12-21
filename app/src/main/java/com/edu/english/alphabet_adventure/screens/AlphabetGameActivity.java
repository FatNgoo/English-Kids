package com.edu.english.alphabet_adventure.screens;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;
import com.edu.english.alphabet_adventure.data.GameData;
import com.edu.english.alphabet_adventure.engine.GameEngine;
import com.edu.english.alphabet_adventure.models.GameState;
import com.edu.english.alphabet_adventure.models.LetterToken;
import com.edu.english.alphabet_adventure.models.Mascot;
import com.edu.english.alphabet_adventure.models.WordItem;
import com.edu.english.alphabet_adventure.services.GamePreferences;
import com.edu.english.alphabet_adventure.services.TTSService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Main game screen for Alphabet Adventure.
 * Handles the lane runner gameplay with letter tokens.
 */
public class AlphabetGameActivity extends AppCompatActivity implements GameEngine.GameEventListener {

    // Views
    private TextView wordEmojiView;
    private TextView progressView;
    private ImageButton speakerButton;
    private ImageButton muteButton;
    private ImageButton pauseButton;
    private LinearLayout heartsContainer;
    private FrameLayout gameArea;
    private TextView mascotView;
    private View laneHighlight;
    private LinearLayout btnUp;
    private LinearLayout btnDown;
    
    // Overlays
    private View winOverlay;
    private View loseOverlay;
    private View pauseOverlay;

    // Game components
    private GameState gameState;
    private GameEngine gameEngine;
    private TTSService ttsService;
    private GamePreferences preferences;
    private GestureDetector gestureDetector;

    // Data
    private List<WordItem> words;
    private int currentWordIndex = 0;
    private Mascot selectedMascot;
    
    // Token views cache
    private Map<Integer, TextView> tokenViews = new HashMap<>();
    private float laneHeight;
    private float gameAreaWidth;
    private ObjectAnimator mascotBounceAnim;
    private ObjectAnimator mascotTiltAnim;
    private ObjectAnimator mascotScaleAnim;
    
    // Scrolling decorations
    private List<TextView> decorationViews = new ArrayList<>();
    private Handler scrollHandler = new Handler(Looper.getMainLooper());
    private boolean isScrolling = false;
    private static final float SCROLL_SPEED = 3f;  // Match token speed

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alphabet_game);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initServices();
        initGame();
        setupControls();
        setupGestures();
        
        // Wait for layout to be ready
        gameArea.post(() -> {
            gameAreaWidth = gameArea.getWidth();
            laneHeight = gameArea.getHeight() / 5f;
            gameEngine.setScreenWidth(gameAreaWidth);
            updateMascotPosition(false);
            updateLaneHighlight();
            addScrollingDecorations();  // Add scrolling scenery
            startMascotWalkingAnimation();  // Start walking animation
            startScrollingBackground();  // Start background scrolling
            gameEngine.start();
            ttsService.setWord(gameState.getCurrentWord().getWord());
            // Speak word immediately when game starts
            ttsService.speakNow();
        });
    }

    private void initViews() {
        wordEmojiView = findViewById(R.id.word_emoji);
        progressView = findViewById(R.id.word_progress);
        speakerButton = findViewById(R.id.btn_speaker);
        muteButton = findViewById(R.id.btn_mute);
        pauseButton = findViewById(R.id.btn_pause);
        heartsContainer = findViewById(R.id.hearts_container);
        gameArea = findViewById(R.id.game_area);
        mascotView = findViewById(R.id.mascot);
        laneHighlight = findViewById(R.id.lane_highlight);
        btnUp = findViewById(R.id.btn_up);
        btnDown = findViewById(R.id.btn_down);
        
        winOverlay = findViewById(R.id.win_overlay);
        loseOverlay = findViewById(R.id.lose_overlay);
        pauseOverlay = findViewById(R.id.pause_overlay);

        // Back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            gameEngine.stop();
            ttsService.shutdown();
            finish();
        });
    }

    private void initServices() {
        preferences = new GamePreferences(this);
        ttsService = new TTSService(this);
        ttsService.setMuted(preferences.isMuted());
        
        // Load selected mascot
        int mascotId = preferences.getSelectedMascot();
        selectedMascot = GameData.getMascotById(mascotId);
        mascotView.setText(selectedMascot.getEmoji());
    }

    private void initGame() {
        words = GameData.getShuffledWords();
        currentWordIndex = 0;

        gameState = new GameState();
        gameState.setCurrentWord(words.get(currentWordIndex));
        gameState.setMuted(preferences.isMuted());

        gameEngine = new GameEngine(gameState, this);
        
        updateUI();
        updateHearts();
        updateMuteButton();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupControls() {
        // Speaker button - replay word
        speakerButton.setOnClickListener(v -> {
            animateButton(v);
            ttsService.speakNow();
        });

        // Mute button
        muteButton.setOnClickListener(v -> {
            animateButton(v);
            boolean muted = ttsService.toggleMute();
            gameState.setMuted(muted);
            preferences.setMuted(muted);
            updateMuteButton();
        });

        // Pause button
        pauseButton.setOnClickListener(v -> {
            animateButton(v);
            togglePause();
        });

        // Up/Down buttons
        btnUp.setOnClickListener(v -> {
            animateButton(v);
            moveUp();
        });

        btnDown.setOnClickListener(v -> {
            animateButton(v);
            moveDown();
        });

        // Win overlay buttons
        findViewById(R.id.btn_next_word).setOnClickListener(v -> nextWord());
        
        // Lose overlay buttons
        findViewById(R.id.btn_retry).setOnClickListener(v -> retryGame());

        // Pause overlay
        findViewById(R.id.btn_resume).setOnClickListener(v -> togglePause());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 30;  // Reduced for easier swipe
            private static final int SWIPE_VELOCITY_THRESHOLD = 30;  // Reduced for easier swipe

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) {
                        moveUp();
                    } else {
                        moveDown();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;  // Must return true to detect fling events
            }
        });

        // Enable touch on entire game area for better swipe detection
        gameArea.setOnTouchListener((v, event) -> {
            v.performClick();
            return gestureDetector.onTouchEvent(event);
        });
    }

    private void moveUp() {
        if (!gameState.isPlaying()) return;
        gameEngine.moveUp();
        animateMascotLaneChange();
        updateLaneHighlight();
    }

    private void moveDown() {
        if (!gameState.isPlaying()) return;
        gameEngine.moveDown();
        animateMascotLaneChange();
        updateLaneHighlight();
    }
    
    private void animateMascotLaneChange() {
        int lane = gameState.getCurrentLane();
        float targetY = lane * laneHeight + (laneHeight - mascotView.getHeight()) / 2;
        
        // Pause walking animation during lane change
        stopWalkingAnimation();
        
        // Jump animation when changing lane
        mascotView.animate()
            .y(targetY)
            .scaleX(1.2f)
            .scaleY(0.8f)
            .setDuration(80)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .withEndAction(() -> {
                // Squash and stretch effect
                mascotView.animate()
                    .scaleX(0.9f)
                    .scaleY(1.1f)
                    .setDuration(80)
                    .withEndAction(() -> {
                        mascotView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                // Resume walking animation
                                restartWalkingAnimation();
                            })
                            .start();
                    })
                    .start();
            })
            .start();
    }

    private void updateMascotPosition(boolean animate) {
        int lane = gameState.getCurrentLane();
        float targetY = lane * laneHeight + (laneHeight - mascotView.getHeight()) / 2;
        mascotView.setY(targetY);
    }

    private void updateLaneHighlight() {
        int lane = gameState.getCurrentLane();
        float targetY = lane * laneHeight;
        
        ViewGroup.LayoutParams params = laneHighlight.getLayoutParams();
        params.height = (int) laneHeight;
        laneHighlight.setLayoutParams(params);
        
        laneHighlight.animate()
            .y(targetY)
            .setDuration(150)
            .start();
    }

    private void updateUI() {
        WordItem word = gameState.getCurrentWord();
        if (word != null) {
            wordEmojiView.setText(word.getImageEmoji());
            progressView.setText(gameState.getProgressString());
        }
    }

    private void updateHearts() {
        int lives = gameState.getLives();
        for (int i = 0; i < heartsContainer.getChildCount(); i++) {
            View heart = heartsContainer.getChildAt(i);
            if (i < lives) {
                heart.setAlpha(1f);
                heart.setScaleX(1f);
                heart.setScaleY(1f);
            } else {
                heart.setAlpha(0.3f);
                heart.setScaleX(0.8f);
                heart.setScaleY(0.8f);
            }
        }
    }

    private void updateMuteButton() {
        if (gameState.isMuted()) {
            muteButton.setImageResource(R.drawable.ic_volume_off);
        } else {
            muteButton.setImageResource(R.drawable.ic_volume_on);
        }
    }

    private void togglePause() {
        if (gameState.isPlaying()) {
            gameEngine.pause();
            ttsService.pause();
            showPauseOverlay();
        } else if (gameState.isPaused()) {
            hidePauseOverlay();
            gameEngine.resume();
            ttsService.resume();
        }
    }

    // ==================== GameEngine.GameEventListener ====================

    @Override
    public void onTokensUpdated(List<LetterToken> tokens) {
        runOnUiThread(() -> {
            // Remove old token views that are no longer active (but keep decorations)
            List<Integer> toRemove = new ArrayList<>();
            for (Map.Entry<Integer, TextView> entry : tokenViews.entrySet()) {
                boolean found = false;
                for (LetterToken token : tokens) {
                    if (token.hashCode() == entry.getKey() && token.isActive()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    TextView view = entry.getValue();
                    // Only remove if it's actually a token (not decoration)
                    if (view.getParent() == gameArea && !"decoration".equals(view.getTag())) {
                        gameArea.removeView(view);
                    }
                    toRemove.add(entry.getKey());
                }
            }
            for (Integer key : toRemove) {
                tokenViews.remove(key);
            }

            // Update or create token views
            for (LetterToken token : tokens) {
                if (!token.isActive()) continue;

                TextView tokenView = tokenViews.get(token.hashCode());
                if (tokenView == null) {
                    tokenView = createTokenView(token);
                    tokenViews.put(token.hashCode(), tokenView);
                    gameArea.addView(tokenView);
                }

                // Update position
                float y = token.getLane() * laneHeight + (laneHeight - tokenView.getHeight()) / 2;
                tokenView.setX(token.getPositionX());
                tokenView.setY(y);
            }
        });
    }

    private TextView createTokenView(LetterToken token) {
        TextView tv = new TextView(this);
        tv.setText(String.valueOf(token.getLetter()));
        tv.setTextSize(32);
        tv.setTextColor(getResources().getColor(R.color.white, null));
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setBackground(getResources().getDrawable(R.drawable.bg_letter_token_gradient, null));
        tv.setPadding(28, 20, 28, 20);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setElevation(8f);
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tv.setLayoutParams(params);
        
        // Animate entry with bounce effect
        tv.setAlpha(0f);
        tv.setScaleX(0.3f);
        tv.setScaleY(0.3f);
        tv.setRotation(-10f);
        tv.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(0f)
            .setDuration(300)
            .setInterpolator(new OvershootInterpolator(1.5f))
            .start();
        
        return tv;
    }

    @Override
    public void onCorrectLetter(char letter) {
        runOnUiThread(() -> {
            updateUI();
            
            // Play success animation on progress
            progressView.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(100)
                .withEndAction(() -> {
                    progressView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                })
                .start();
            
            // Sparkle effect on mascot with color flash
            mascotView.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .rotation(15f)
                .setDuration(100)
                .withEndAction(() -> {
                    mascotView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .rotation(-15f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            mascotView.animate().rotation(0f).setDuration(100).start();
                        })
                        .start();
                })
                .start();

            // Green success flash on game area
            View successFlash = new View(this);
            successFlash.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ));
            successFlash.setBackgroundColor(getResources().getColor(R.color.success_green, null));
            successFlash.setAlpha(0.3f);
            gameArea.addView(successFlash);
            successFlash.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> gameArea.removeView(successFlash))
                .start();

            // TTS speaks the letter
            if (!gameState.isMuted()) {
                ttsService.speak(String.valueOf(letter));
            }
        });
    }

    @Override
    public void onWrongLetter(char letter, char expected) {
        runOnUiThread(() -> {
            try {
                // Update hearts display first
                updateHearts();
                
                // Vibrate with pattern
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    long[] pattern = {0, 100, 50, 100};
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                }
                
                // Screen shake with intensity
                if (gameArea != null) {
                    ObjectAnimator shakeX = ObjectAnimator.ofFloat(gameArea, "translationX", 0, 15, -15, 15, -15, 10, -10, 5, -5, 0);
                    shakeX.setDuration(400);
                    shakeX.start();
                }

                // Mascot sad animation - stop walking animations first
                if (mascotView != null) {
                    if (mascotBounceAnim != null && mascotBounceAnim.isRunning()) {
                        mascotBounceAnim.pause();
                    }
                    if (mascotTiltAnim != null && mascotTiltAnim.isRunning()) {
                        mascotTiltAnim.pause();
                    }
                    
                    mascotView.animate()
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .setDuration(150)
                        .withEndAction(() -> {
                            if (mascotView != null) {
                                mascotView.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(200)
                                    .setInterpolator(new OvershootInterpolator())
                                    .withEndAction(() -> {
                                        // Resume walking animations
                                        restartWalkingAnimation();
                                    })
                                    .start();
                            }
                        })
                        .start();
                }

                // Flash wrong indicator with gradient
                View wrongIndicator = findViewById(R.id.wrong_indicator);
                if (wrongIndicator != null) {
                    wrongIndicator.setVisibility(View.VISIBLE);
                    wrongIndicator.setAlpha(0.7f);
                    wrongIndicator.animate()
                        .alpha(0f)
                        .setDuration(600)
                        .withEndAction(() -> {
                            if (wrongIndicator != null) {
                                wrongIndicator.setVisibility(View.GONE);
                            }
                        })
                        .start();
                }
                
                // Animate losing heart with null checks
                int lives = gameState.getLives();
                if (lives >= 0 && lives < heartsContainer.getChildCount()) {
                    View heart = heartsContainer.getChildAt(lives);
                    if (heart != null) {
                        heart.animate()
                            .scaleX(1.5f)
                            .scaleY(1.5f)
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(() -> {
                                if (heart != null) {
                                    heart.setScaleX(0.8f);
                                    heart.setScaleY(0.8f);
                                    heart.setAlpha(0.3f);
                                }
                            })
                            .start();
                    }
                }
                
                // TTS says "Oops" or similar
                if (!gameState.isMuted() && ttsService != null) {
                    ttsService.speak("Oops! Try again!");
                }
            } catch (Exception e) {
                // Log error but don't crash
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onLivesChanged(int lives) {
        runOnUiThread(() -> {
            updateHearts();
        });
    }

    @Override
    public void onWordComplete() {
        runOnUiThread(() -> {
            ttsService.stopAutoSpeak();
            preferences.incrementWordsCompleted();
            preferences.setHighScore(gameState.getScore());
            showWinOverlay();
        });
    }

    @Override
    public void onGameOver() {
        runOnUiThread(() -> {
            ttsService.stopAutoSpeak();
            showLoseOverlay();
        });
    }

    // ==================== Overlays ====================

    private void showWinOverlay() {
        winOverlay.setVisibility(View.VISIBLE);
        winOverlay.setAlpha(0f);
        winOverlay.animate().alpha(1f).setDuration(300).start();
        
        // Confetti animation placeholder - animate stars
        View confetti = findViewById(R.id.confetti_container);
        if (confetti != null) {
            animateConfetti(confetti);
        }
        
        // Speak congratulations
        if (!gameState.isMuted()) {
            ttsService.speak("Congratulations! Well done!");
        }
    }

    private void animateConfetti(View container) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(container, "rotation", 0f, 360f);
        rotation.setDuration(2000);
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.start();
    }

    private void hideWinOverlay() {
        winOverlay.animate().alpha(0f).setDuration(200)
            .withEndAction(() -> winOverlay.setVisibility(View.GONE))
            .start();
    }

    private void showLoseOverlay() {
        loseOverlay.setVisibility(View.VISIBLE);
        loseOverlay.setAlpha(0f);
        loseOverlay.animate().alpha(1f).setDuration(300).start();
        
        // Show correct word
        TextView correctWordView = loseOverlay.findViewById(R.id.correct_word);
        if (correctWordView != null && gameState.getCurrentWord() != null) {
            correctWordView.setText(gameState.getCurrentWord().getWord().toUpperCase());
        }
        
        if (!gameState.isMuted()) {
            ttsService.speak("Oh no! The word was " + gameState.getCurrentWord().getWord());
        }
    }

    private void hideLoseOverlay() {
        loseOverlay.animate().alpha(0f).setDuration(200)
            .withEndAction(() -> loseOverlay.setVisibility(View.GONE))
            .start();
    }

    private void showPauseOverlay() {
        pauseOverlay.setVisibility(View.VISIBLE);
        pauseOverlay.setAlpha(0f);
        pauseOverlay.animate().alpha(1f).setDuration(200).start();
    }

    private void hidePauseOverlay() {
        pauseOverlay.animate().alpha(0f).setDuration(200)
            .withEndAction(() -> pauseOverlay.setVisibility(View.GONE))
            .start();
    }

    // ==================== Game Flow ====================

    private void nextWord() {
        hideWinOverlay();
        clearTokenViews();
        
        currentWordIndex++;
        if (currentWordIndex >= words.size()) {
            currentWordIndex = 0;
            words = GameData.getShuffledWords();
        }

        gameState.nextWord(words.get(currentWordIndex));
        updateUI();
        updateHearts();
        
        gameEngine.start();
        ttsService.setWord(gameState.getCurrentWord().getWord());
    }

    private void retryGame() {
        hideLoseOverlay();
        clearTokenViews();
        
        gameState.reset();
        gameState.setCurrentWord(words.get(currentWordIndex));
        updateUI();
        updateHearts();
        
        gameEngine.start();
        ttsService.setWord(gameState.getCurrentWord().getWord());
    }

    private void clearTokenViews() {
        // Only remove token views, preserve decorations and other views
        for (TextView tv : tokenViews.values()) {
            if (tv.getParent() == gameArea) {
                gameArea.removeView(tv);
            }
        }
        tokenViews.clear();
    }

    private void animateButton(View view) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(50)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(50)
                    .start();
            })
            .start();
    }

    private void startMascotWalkingAnimation() {
        // Create bouncing walk animation - hop effect
        mascotBounceAnim = ObjectAnimator.ofFloat(mascotView, "translationY", 0f, -15f, 0f);
        mascotBounceAnim.setDuration(350);
        mascotBounceAnim.setRepeatCount(ObjectAnimator.INFINITE);
        mascotBounceAnim.setRepeatMode(ObjectAnimator.RESTART);
        mascotBounceAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mascotBounceAnim.start();

        // Tilt rotation for walking
        mascotTiltAnim = ObjectAnimator.ofFloat(mascotView, "rotation", -5f, 5f, -5f);
        mascotTiltAnim.setDuration(500);
        mascotTiltAnim.setRepeatCount(ObjectAnimator.INFINITE);
        mascotTiltAnim.setRepeatMode(ObjectAnimator.RESTART);
        mascotTiltAnim.start();
        
        // Breathing/pulse scale effect
        mascotScaleAnim = ObjectAnimator.ofFloat(mascotView, "scaleX", 1f, 1.05f, 1f);
        mascotScaleAnim.setDuration(600);
        mascotScaleAnim.setRepeatCount(ObjectAnimator.INFINITE);
        mascotScaleAnim.setRepeatMode(ObjectAnimator.RESTART);
        mascotScaleAnim.start();
        
        // Sync scaleY
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mascotView, "scaleY", 1f, 1.05f, 1f);
        scaleY.setDuration(600);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatMode(ObjectAnimator.RESTART);
        scaleY.start();
    }

    private void restartWalkingAnimation() {
        if (mascotBounceAnim != null && !mascotBounceAnim.isRunning()) {
            mascotBounceAnim.start();
        }
        if (mascotTiltAnim != null && !mascotTiltAnim.isRunning()) {
            mascotTiltAnim.start();
        }
        if (mascotScaleAnim != null && !mascotScaleAnim.isRunning()) {
            mascotScaleAnim.start();
        }
    }
    
    private void stopWalkingAnimation() {
        if (mascotBounceAnim != null) mascotBounceAnim.pause();
        if (mascotTiltAnim != null) mascotTiltAnim.pause();
        if (mascotScaleAnim != null) mascotScaleAnim.pause();
    }

    private void addScrollingDecorations() {
        // Clear existing decorations
        for (TextView deco : decorationViews) {
            if (deco.getParent() == gameArea) {
                gameArea.removeView(deco);
            }
        }
        decorationViews.clear();
        
        // Ground elements on each lane (grass, flowers)
        String[] groundItems = {"🌿", "🍀", "🌻", "🌼", "🌷", "🌸", "🌹", "🌺"};
        // Sky/floating elements
        String[] skyItems = {"☁️", "⭐", "🪶", "🦋", "🐦"};
        // Trees (bigger, on edges)
        String[] treeItems = {"🌳", "🌲", "🌴", "🌵"};
        
        Random random = new Random();
        
        // Add ground decorations spread across screen width - more items
        for (int i = 0; i < 20; i++) {
            TextView deco = createDecoration(
                groundItems[random.nextInt(groundItems.length)],
                random.nextInt(12) + 14,  // size 14-26sp
                0.4f + random.nextFloat() * 0.3f  // alpha 0.4-0.7
            );
            
            // Spread across screen width with some going offscreen right
            float x = random.nextFloat() * (gameAreaWidth + 200) - 50;
            int lane = random.nextInt(5);
            float yOffset = laneHeight * 0.6f + random.nextFloat() * (laneHeight * 0.3f);  // Bottom of lane
            float y = lane * laneHeight + yOffset;
            
            deco.setX(x);
            deco.setY(y);
            deco.setElevation(0.5f);
            
            decorationViews.add(deco);
            gameArea.addView(deco, 0);
        }
        
        // Add sky decorations (clouds, stars, birds)
        for (int i = 0; i < 8; i++) {
            TextView deco = createDecoration(
                skyItems[random.nextInt(skyItems.length)],
                random.nextInt(15) + 16,  // size 16-31sp
                0.3f + random.nextFloat() * 0.4f  // alpha 0.3-0.7
            );
            
            float x = random.nextFloat() * (gameAreaWidth + 300) - 100;
            int lane = random.nextInt(3);  // Top 3 lanes for sky items
            float yOffset = random.nextFloat() * (laneHeight * 0.4f);  // Top of lane
            float y = lane * laneHeight + yOffset;
            
            deco.setX(x);
            deco.setY(y);
            deco.setElevation(0.3f);
            
            // Add floating animation for sky items
            ObjectAnimator floatAnim = ObjectAnimator.ofFloat(deco, "translationY", 0f, -8f, 0f);
            floatAnim.setDuration(1500 + random.nextInt(1000));
            floatAnim.setRepeatCount(ObjectAnimator.INFINITE);
            floatAnim.start();
            
            decorationViews.add(deco);
            gameArea.addView(deco, 0);
        }
        
        // Add trees on the edges
        for (int i = 0; i < 6; i++) {
            TextView deco = createDecoration(
                treeItems[random.nextInt(treeItems.length)],
                random.nextInt(15) + 24,  // size 24-39sp (bigger)
                0.5f + random.nextFloat() * 0.3f  // alpha 0.5-0.8
            );
            
            float x = random.nextFloat() * (gameAreaWidth + 200) - 50;
            int lane = random.nextInt(5);
            float yOffset = laneHeight * 0.2f + random.nextFloat() * (laneHeight * 0.3f);
            float y = lane * laneHeight + yOffset;
            
            deco.setX(x);
            deco.setY(y);
            deco.setElevation(0.8f);
            
            decorationViews.add(deco);
            gameArea.addView(deco, 0);
        }
    }
    
    private TextView createDecoration(String emoji, int textSize, float alpha) {
        TextView decoration = new TextView(this);
        decoration.setText(emoji);
        decoration.setTextSize(textSize);
        decoration.setAlpha(alpha);
        decoration.setTag("decoration");
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        decoration.setLayoutParams(params);
        
        return decoration;
    }
    
    private void startScrollingBackground() {
        isScrolling = true;
        scrollHandler.post(scrollRunnable);
    }
    
    private void stopScrollingBackground() {
        isScrolling = false;
        scrollHandler.removeCallbacks(scrollRunnable);
    }
    
    private Runnable scrollRunnable = new Runnable() {
        Random random = new Random();
        
        @Override
        public void run() {
            if (!isScrolling || !gameState.isPlaying()) {
                scrollHandler.postDelayed(this, 16);
                return;
            }
            
            // Move all decorations to the left
            for (TextView deco : decorationViews) {
                float currentX = deco.getX();
                float newX = currentX - SCROLL_SPEED;
                
                // If decoration goes off left side, respawn on right
                if (newX < -100) {
                    newX = gameAreaWidth + random.nextInt(100);
                    // Randomize Y position within its lane type
                    int lane = random.nextInt(5);
                    float yOffset = random.nextFloat() * (laneHeight * 0.6f) + laneHeight * 0.2f;
                    deco.setY(lane * laneHeight + yOffset);
                }
                
                deco.setX(newX);
            }
            
            // Continue scrolling at ~60fps
            scrollHandler.postDelayed(this, 16);
        }
    };

    // ==================== Lifecycle ====================

    @Override
    protected void onPause() {
        super.onPause();
        if (gameState != null && gameState.isPlaying()) {
            togglePause();
        }
        // Pause walking animations
        stopWalkingAnimation();
        stopScrollingBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume walking animations if game is playing
        if (gameState != null && gameState.isPlaying()) {
            restartWalkingAnimation();
            startScrollingBackground();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Stop all animations
        stopWalkingAnimation();
        stopScrollingBackground();
        
        if (mascotBounceAnim != null) mascotBounceAnim.cancel();
        if (mascotTiltAnim != null) mascotTiltAnim.cancel();
        if (mascotScaleAnim != null) mascotScaleAnim.cancel();
        
        if (gameEngine != null) {
            gameEngine.stop();
        }
        if (ttsService != null) {
            ttsService.shutdown();
        }
    }
}
