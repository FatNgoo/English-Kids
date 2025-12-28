package com.edu.english.magicmelody.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;
import com.edu.english.magicmelody.ui.worldmap.WorldMapActivity;
import com.edu.english.magicmelody.util.AnimationUtils;
import com.edu.english.magicmelody.util.Constants;

/**
 * 🎵 Magic Melody Splash Screen
 * 
 * Purpose: Entry point for Magic Melody game
 * - Shows game logo and title with animations
 * - Loads game assets in background
 * - Transitions to World Map after loading
 * 
 * UX Writing:
 * - "Welcome to the Sound Kingdom! ✨"
 * - "Loading magical melodies..."
 */
public class MagicMelodySplashActivity extends AppCompatActivity {
    
    private static final long SPLASH_DURATION = 3000; // 3 seconds
    private static final long ANIMATION_DELAY = 500;
    
    private ImageView logoImage;
    private TextView titleText;
    private TextView subtitleText;
    private ProgressBar loadingProgress;
    private TextView loadingText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_magic_melody_splash);
        
        setupSystemBars();
        initViews();
        startAnimations();
        loadGameAssets();
    }
    
    private void setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void initViews() {
        logoImage = findViewById(R.id.logo_image);
        titleText = findViewById(R.id.title_text);
        subtitleText = findViewById(R.id.subtitle_text);
        loadingProgress = findViewById(R.id.loading_progress);
        loadingText = findViewById(R.id.loading_text);
        
        // Initially hide all views for animation
        logoImage.setAlpha(0f);
        titleText.setAlpha(0f);
        subtitleText.setAlpha(0f);
        loadingProgress.setAlpha(0f);
        loadingText.setAlpha(0f);
    }
    
    private void startAnimations() {
        Handler handler = new Handler(Looper.getMainLooper());
        
        // 1. Logo bounces in
        handler.postDelayed(() -> {
            AnimationUtils.animateBounceIn(logoImage, 0);
        }, ANIMATION_DELAY);
        
        // 2. Title fades in
        handler.postDelayed(() -> {
            AnimationUtils.animateSlideInFromBottom(titleText, 0);
        }, ANIMATION_DELAY + 400);
        
        // 3. Subtitle fades in
        handler.postDelayed(() -> {
            AnimationUtils.animateSlideInFromBottom(subtitleText, 0);
        }, ANIMATION_DELAY + 600);
        
        // 4. Loading indicator appears
        handler.postDelayed(() -> {
            AnimationUtils.animateSlideInFromBottom(loadingProgress, 0);
            AnimationUtils.animateSlideInFromBottom(loadingText, 100);
        }, ANIMATION_DELAY + 800);
    }
    
    private void loadGameAssets() {
        // Simulate loading game assets
        // In production, this would load:
        // - Audio files (notes, background music)
        // - 3D models
        // - Lesson configurations
        // - User progress from Room database
        
        Handler handler = new Handler(Looper.getMainLooper());
        
        // Update loading text
        String[] loadingMessages = {
            "Loading magical melodies... 🎵",
            "Preparing the Sound Kingdom... 🏰",
            "Gathering musical notes... 🎶",
            "Almost ready! ✨"
        };
        
        for (int i = 0; i < loadingMessages.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                if (loadingText != null) {
                    loadingText.setText(loadingMessages[index]);
                }
            }, SPLASH_DURATION / loadingMessages.length * i);
        }
        
        // Navigate to World Map
        handler.postDelayed(this::navigateToWorldMap, SPLASH_DURATION);
    }
    
    private void navigateToWorldMap() {
        Intent intent = new Intent(this, WorldMapActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Disable back button during splash
        // User must wait for loading to complete
    }
}
