package com.edu.english.alphabet_pop_lab;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;

/**
 * AlphabetPopLabActivity - Main activity for Alphabet Pop Lab game
 * Contains the game view and UI controls
 */
public class AlphabetPopLabActivity extends AppCompatActivity {
    
    private AlphabetGameView gameView;
    private ImageButton backButton;
    private ImageButton muteButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge
        EdgeToEdge.enable(this);
        
        // Keep screen on during gameplay
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Set content view
        setContentView(R.layout.activity_alphabet_pop_lab);
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize game view
        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameView = new AlphabetGameView(this);
        gameView.setGameCallback(() -> finish());
        gameContainer.addView(gameView);
        
        // Setup back button
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        // Setup mute button
        muteButton = findViewById(R.id.mute_button);
        muteButton.setOnClickListener(v -> {
            if (gameView != null) {
                gameView.toggleMute();
                updateMuteButton();
            }
        });
    }
    
    private void updateMuteButton() {
        if (gameView != null && muteButton != null) {
            if (gameView.isMuted()) {
                muteButton.setImageResource(R.drawable.ic_volume_off);
            } else {
                muteButton.setImageResource(R.drawable.ic_volume_on);
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Game loop resumes automatically via SurfaceHolder callback
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Game loop pauses automatically via SurfaceHolder callback
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) {
            gameView.cleanup();
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
