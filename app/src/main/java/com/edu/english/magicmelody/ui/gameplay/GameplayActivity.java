package com.edu.english.magicmelody.ui.gameplay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.edu.english.magicmelody.core.audio.SoundEngine;
import com.edu.english.magicmelody.core.game.JudgeResult;
import com.edu.english.magicmelody.ui.adventure.AdventureActivity;
import com.edu.english.magicmelody.ui.components.PianoKeyBarView;
import com.edu.english.databinding.ActivityGameplayBinding;

/**
 * GameplayActivity - Main rhythm game screen
 * Features falling notes, piano keys, scoring, and environment evolution
 */
public class GameplayActivity extends AppCompatActivity implements PianoKeyBarView.OnKeyPressedListener {
    
    private ActivityGameplayBinding binding;
    private GameplayViewModel viewModel;
    private SoundEngine soundEngine;
    
    private Handler uiHandler;
    private Runnable renderRunnable;
    private boolean isRenderingActive = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameplayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        uiHandler = new Handler(Looper.getMainLooper());
        
        setupViewModel();
        setupSoundEngine();
        setupViews();
        observeViewModel();
        
        // Load lesson from intent
        String lessonId = getIntent().getStringExtra(AdventureActivity.EXTRA_LESSON_ID);
        if (lessonId == null) {
            lessonId = "forest_level_1"; // Default
        }
        viewModel.loadLesson(lessonId);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(GameplayViewModel.class);
    }
    
    private void setupSoundEngine() {
        soundEngine = new SoundEngine(this);
    }
    
    private void setupViews() {
        // Piano keys
        binding.pianoKeyBar.setOnKeyPressedListener(this);
        
        // Rhythm track view
        binding.rhythmTrackView.setNotePool(viewModel.getRhythmEngine().getNotePool());
        
        // Pause button
        binding.btnPause.setOnClickListener(v -> showPauseDialog());
        
        // Hide result panel initially
        binding.resultPanel.setVisibility(View.GONE);
    }
    
    private void observeViewModel() {
        // Game state
        viewModel.getGameState().observe(this, state -> {
            switch (state) {
                case LOADING:
                    showLoading();
                    break;
                case READY:
                    hideLoading();
                    startCountdown();
                    break;
                case COUNTDOWN:
                    // Countdown handled separately
                    break;
                case PLAYING:
                    startRendering();
                    break;
                case PAUSED:
                    stopRendering();
                    break;
                case COMPLETED:
                    stopRendering();
                    showResult();
                    break;
                case FAILED:
                    stopRendering();
                    showFailure();
                    break;
            }
        });
        
        // Score
        viewModel.getScore().observe(this, score -> {
            binding.txtScore.setText(String.valueOf(score));
            animateScoreChange();
        });
        
        // Combo
        viewModel.getCombo().observe(this, combo -> {
            if (combo > 0) {
                binding.txtCombo.setText(combo + " Combo!");
                binding.txtCombo.setVisibility(View.VISIBLE);
                animateCombo();
            } else {
                binding.txtCombo.setVisibility(View.GONE);
            }
        });
        
        // Progress
        viewModel.getProgress().observe(this, progress -> {
            binding.progressGame.setProgress((int) (progress * 100));
        });
        
        // Hit result
        viewModel.getHitResult().observe(this, result -> {
            if (result != null && result.getJudgment() != JudgeResult.Judgment.NONE) {
                onHitResult(result);
            }
        });
        
        // Environment
        viewModel.getEnvironment().observe(this, state -> {
            updateEnvironmentVisuals(state);
        });
        
        // Feedback
        viewModel.getFeedback().observe(this, feedback -> {
            if (feedback != null && !feedback.isEmpty()) {
                showFeedback(feedback);
            }
        });
        
        // Game result
        viewModel.getGameResult().observe(this, result -> {
            if (result != null) {
                displayResult(result);
            }
        });
    }
    
    private void startCountdown() {
        binding.txtCountdown.setVisibility(View.VISIBLE);
        viewModel.startCountdown();
        
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000) + 1;
                binding.txtCountdown.setText(String.valueOf(seconds));
                animateCountdown();
            }
            
            @Override
            public void onFinish() {
                binding.txtCountdown.setText("GO!");
                animateCountdown();
                
                uiHandler.postDelayed(() -> {
                    binding.txtCountdown.setVisibility(View.GONE);
                    viewModel.startGame();
                }, 500);
            }
        }.start();
    }
    
    private void animateCountdown() {
        binding.txtCountdown.setScaleX(1.5f);
        binding.txtCountdown.setScaleY(1.5f);
        binding.txtCountdown.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
    
    private void startRendering() {
        if (isRenderingActive) return;
        isRenderingActive = true;
        
        renderRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRenderingActive) return;
                
                // Update rhythm track view
                binding.rhythmTrackView.invalidate();
                
                // Schedule next frame
                uiHandler.postDelayed(this, 16); // ~60fps
            }
        };
        
        uiHandler.post(renderRunnable);
    }
    
    private void stopRendering() {
        isRenderingActive = false;
        if (renderRunnable != null) {
            uiHandler.removeCallbacks(renderRunnable);
        }
    }
    
    @Override
    public void onKeyPressed(int lane) {
        // Play sound
        soundEngine.playNote(lane);
        
        // Notify viewmodel
        viewModel.onKeyPressed(lane);
        
        // Show hit effect
        binding.rhythmTrackView.showHitEffect(lane);
    }
    
    @Override
    public void onKeyReleased(int lane) {
        // Optional: handle key release
    }
    
    private void onHitResult(JudgeResult result) {
        // Play feedback sound
        if (result.getJudgment() == JudgeResult.Judgment.PERFECT) {
            // Extra sparkle for perfect
        } else if (result.getJudgment() == JudgeResult.Judgment.MISS) {
            soundEngine.playWrongSound();
        }
        
        // Show feedback text
        String feedback = result.getFeedbackMessage();
        showFeedback(feedback);
    }
    
    private void showFeedback(String message) {
        binding.txtHitFeedback.setText(message);
        binding.txtHitFeedback.setVisibility(View.VISIBLE);
        binding.txtHitFeedback.setAlpha(1f);
        binding.txtHitFeedback.setScaleX(1.2f);
        binding.txtHitFeedback.setScaleY(1.2f);
        
        binding.txtHitFeedback.animate()
                .alpha(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.txtHitFeedback.setVisibility(View.GONE);
                    }
                })
                .start();
    }
    
    private void animateScoreChange() {
        binding.txtScore.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(100)
                .withEndAction(() -> {
                    binding.txtScore.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }
    
    private void animateCombo() {
        binding.txtCombo.setScaleX(0.5f);
        binding.txtCombo.setScaleY(0.5f);
        binding.txtCombo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
    
    private void updateEnvironmentVisuals(GameplayViewModel.EnvironmentState state) {
        // Update saturation (grayscale to color)
        ColorMatrixColorFilter filter = GameplayViewModel.getSaturationFilter(state.getSaturation());
        binding.imgBackground.setColorFilter(filter);
        
        // Update tree/environment scale
        float scale = state.getTreeScale();
        binding.imgEnvironment.setScaleX(scale);
        binding.imgEnvironment.setScaleY(scale);
        
        // Update character state
        updateCharacterAnimation(state.getCharacterState());
        
        // Trigger bloom effect
        if (state.isTriggerBloom()) {
            triggerBloomEffect();
        }
        
        // Show particles
        if (state.isShowParticles()) {
            binding.lottieParticles.setVisibility(View.VISIBLE);
            binding.lottieParticles.playAnimation();
        }
    }
    
    private void updateCharacterAnimation(GameplayViewModel.EnvironmentState.CharacterState state) {
        switch (state) {
            case DANCING:
                // Rotation dance animation
                ObjectAnimator rotator = ObjectAnimator.ofFloat(binding.imgCharacter, "rotation", -10f, 10f);
                rotator.setDuration(200);
                rotator.setRepeatCount(ValueAnimator.INFINITE);
                rotator.setRepeatMode(ValueAnimator.REVERSE);
                rotator.start();
                break;
            case HAPPY:
                binding.imgCharacter.setRotation(0f);
                binding.imgCharacter.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(300)
                        .start();
                break;
            case NORMAL:
                binding.imgCharacter.setRotation(0f);
                binding.imgCharacter.setScaleX(1f);
                binding.imgCharacter.setScaleY(1f);
                break;
            case SAD:
                binding.imgCharacter.setRotation(-5f);
                binding.imgCharacter.setScaleX(0.9f);
                binding.imgCharacter.setScaleY(0.9f);
                break;
        }
    }
    
    private void triggerBloomEffect() {
        // Flash effect
        binding.viewFlash.setVisibility(View.VISIBLE);
        binding.viewFlash.setAlpha(1f);
        binding.viewFlash.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.viewFlash.setVisibility(View.GONE);
                    }
                })
                .start();
        
        // Play celebration sound
        soundEngine.playPerfectSound();
        
        // Spirit orb animation to notebook icon
        animateSpiritOrb();
    }
    
    private void animateSpiritOrb() {
        binding.imgSpiritOrb.setVisibility(View.VISIBLE);
        
        // Animate from center to notebook icon position
        float startX = binding.getRoot().getWidth() / 2f;
        float startY = binding.getRoot().getHeight() / 2f;
        float endX = binding.btnNotebook.getX() + binding.btnNotebook.getWidth() / 2f;
        float endY = binding.btnNotebook.getY() + binding.btnNotebook.getHeight() / 2f;
        
        binding.imgSpiritOrb.setX(startX);
        binding.imgSpiritOrb.setY(startY);
        binding.imgSpiritOrb.setAlpha(1f);
        binding.imgSpiritOrb.setScaleX(1.5f);
        binding.imgSpiritOrb.setScaleY(1.5f);
        
        binding.imgSpiritOrb.animate()
                .x(endX)
                .y(endY)
                .scaleX(0.3f)
                .scaleY(0.3f)
                .alpha(0f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.imgSpiritOrb.setVisibility(View.GONE);
                        // Pulse notebook icon
                        binding.btnNotebook.animate()
                                .scaleX(1.3f)
                                .scaleY(1.3f)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    binding.btnNotebook.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(200)
                                            .start();
                                })
                                .start();
                    }
                })
                .start();
    }
    
    private void showLoading() {
        binding.loadingView.setVisibility(View.VISIBLE);
    }
    
    private void hideLoading() {
        binding.loadingView.setVisibility(View.GONE);
    }
    
    private void showResult() {
        binding.resultPanel.setVisibility(View.VISIBLE);
        binding.resultPanel.setAlpha(0f);
        binding.resultPanel.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
    }
    
    private void displayResult(GameplayViewModel.GameResult result) {
        binding.txtResultScore.setText("Điểm: " + result.getScore());
        binding.txtResultAccuracy.setText(String.format("Chính xác: %.1f%%", result.getAccuracy()));
        binding.txtResultMaxCombo.setText("Combo cao nhất: " + result.getMaxCombo());
        
        // Stars
        String stars = "";
        for (int i = 0; i < result.getStars(); i++) {
            stars += "⭐";
        }
        for (int i = result.getStars(); i < 3; i++) {
            stars += "☆";
        }
        binding.txtResultStars.setText(stars);
        
        // Buttons
        binding.btnRetry.setOnClickListener(v -> {
            binding.resultPanel.setVisibility(View.GONE);
            viewModel.loadLesson(getIntent().getStringExtra(AdventureActivity.EXTRA_LESSON_ID));
        });
        
        binding.btnNext.setOnClickListener(v -> {
            finish();
        });
    }
    
    private void showFailure() {
        Toast.makeText(this, "Cố gắng lần sau nhé!", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void showPauseDialog() {
        viewModel.pauseGame();
        
        new AlertDialog.Builder(this)
                .setTitle("Tạm dừng")
                .setMessage("Bạn muốn tiếp tục chơi không?")
                .setPositiveButton("Tiếp tục", (dialog, which) -> {
                    viewModel.resumeGame();
                })
                .setNegativeButton("Thoát", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (viewModel.getGameState().getValue() == GameplayViewModel.GameState.PLAYING) {
            viewModel.pauseGame();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRendering();
        if (soundEngine != null) {
            soundEngine.release();
        }
        binding = null;
    }
}
