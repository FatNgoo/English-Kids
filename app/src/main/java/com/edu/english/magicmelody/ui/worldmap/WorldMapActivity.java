package com.edu.english.magicmelody.ui.worldmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;
import com.edu.english.magicmelody.ui.gameplay.RhythmGameplayActivity;
import com.edu.english.magicmelody.ui.notebook.MagicNotebookActivity;
import com.edu.english.magicmelody.util.AnimationUtils;
import com.edu.english.magicmelody.util.Constants;

/**
 * 🗺️ World Map Screen
 * 
 * Purpose: 3D World Map navigation for Magic Melody
 * - Displays unlocked worlds (Forest, Ocean, Mountain, Desert, Sky)
 * - Shows level progression within each world
 * - Environmental evolution based on player progress
 * - Access to Magic Notebook
 * 
 * Technical Notes:
 * - Uses SceneView for 3D world rendering
 * - Grayscale → Color evolution as levels complete
 * - Particle effects for completed areas
 * 
 * UX Writing:
 * - "Welcome back, little maestro! 🎵"
 * - "Tap a world to explore!"
 * - "This area needs your music to come alive!"
 */
public class WorldMapActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "MagicMelodyPrefs";
    
    private TextView welcomeText;
    private TextView starsCountText;
    private LinearLayout worldContainer;
    private ImageButton notebookButton;
    private ImageButton settingsButton;
    
    // World views with progress text
    private View worldForest;
    private View worldOcean;
    private View worldMountain;
    private View worldDesert;
    private View worldSky;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_world_map);
        
        setupSystemBars();
        initViews();
        setupClickListeners();
        startAnimations();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload progress when returning from gameplay
        loadUserProgress();
        updateWorldsLockStatus();
    }
    
    private void setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void initViews() {
        welcomeText = findViewById(R.id.welcome_text);
        starsCountText = findViewById(R.id.stars_count);
        worldContainer = findViewById(R.id.world_container);
        notebookButton = findViewById(R.id.notebook_button);
        settingsButton = findViewById(R.id.settings_button);
        
        // Get world views
        worldForest = findViewById(R.id.world_forest);
        worldOcean = findViewById(R.id.world_ocean);
        worldMountain = findViewById(R.id.world_mountain);
        worldDesert = findViewById(R.id.world_desert);
        worldSky = findViewById(R.id.world_sky);
    }
    
    private void setupClickListeners() {
        // World buttons - only unlocked worlds can be clicked
        setupWorldButton(R.id.world_forest, Constants.WORLD_FOREST, "Forest Kingdom", true); // Forest is always unlocked
        setupWorldButton(R.id.world_ocean, Constants.WORLD_OCEAN, "Ocean Kingdom", false);
        setupWorldButton(R.id.world_mountain, Constants.WORLD_MOUNTAIN, "Mountain Kingdom", false);
        setupWorldButton(R.id.world_desert, Constants.WORLD_DESERT, "Desert Kingdom", false);
        setupWorldButton(R.id.world_sky, Constants.WORLD_SKY, "Sky Kingdom", false);
        
        // Magic Notebook button
        notebookButton.setOnClickListener(v -> {
            AnimationUtils.animateButtonPress(v);
            openMagicNotebook();
        });
        
        // Settings button
        settingsButton.setOnClickListener(v -> {
            AnimationUtils.animateButtonPress(v);
            openSettings();
        });
    }
    
    private void setupWorldButton(int buttonId, String worldId, String worldName, boolean isDefaultUnlocked) {
        View worldButton = findViewById(buttonId);
        if (worldButton != null) {
            worldButton.setOnClickListener(v -> {
                if (isWorldUnlocked(worldId) || isDefaultUnlocked) {
                    AnimationUtils.animateButtonPress(v);
                    navigateToWorld(worldId, worldName);
                } else {
                    // Show locked message
                    Toast.makeText(this, "🔒 Complete previous world to unlock!", Toast.LENGTH_SHORT).show();
                    // Shake animation for feedback
                    v.animate()
                        .translationX(10f).setDuration(50)
                        .withEndAction(() -> v.animate()
                            .translationX(-10f).setDuration(50)
                            .withEndAction(() -> v.animate()
                                .translationX(0f).setDuration(50).start())
                            .start())
                        .start();
                }
            });
        }
    }
    
    private boolean isWorldUnlocked(String worldId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Forest is always unlocked
        if (Constants.WORLD_FOREST.equals(worldId)) {
            return true;
        }
        
        // Check if previous world is completed with 2+ stars
        String previousWorldId = getPreviousWorld(worldId);
        if (previousWorldId != null) {
            String key = "unlocked_" + worldId + "_level_1";
            return prefs.getBoolean(key, false);
        }
        
        return false;
    }
    
    private String getPreviousWorld(String worldId) {
        switch (worldId) {
            case Constants.WORLD_OCEAN: return Constants.WORLD_FOREST;
            case Constants.WORLD_MOUNTAIN: return Constants.WORLD_OCEAN;
            case Constants.WORLD_DESERT: return Constants.WORLD_MOUNTAIN;
            case Constants.WORLD_SKY: return Constants.WORLD_DESERT;
            default: return null;
        }
    }
    
    private void updateWorldsLockStatus() {
        updateWorldVisual(worldForest, Constants.WORLD_FOREST, true);
        updateWorldVisual(worldOcean, Constants.WORLD_OCEAN, isWorldUnlocked(Constants.WORLD_OCEAN));
        updateWorldVisual(worldMountain, Constants.WORLD_MOUNTAIN, isWorldUnlocked(Constants.WORLD_MOUNTAIN));
        updateWorldVisual(worldDesert, Constants.WORLD_DESERT, isWorldUnlocked(Constants.WORLD_DESERT));
        updateWorldVisual(worldSky, Constants.WORLD_SKY, isWorldUnlocked(Constants.WORLD_SKY));
    }
    
    private void updateWorldVisual(View worldView, String worldId, boolean isUnlocked) {
        if (worldView == null) return;
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        if (isUnlocked) {
            // Bright and clickable
            worldView.setAlpha(1.0f);
            worldView.setClickable(true);
            
            // Find progress text and update it
            if (worldView instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) worldView;
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    if (child instanceof TextView) {
                        TextView tv = (TextView) child;
                        String text = tv.getText().toString();
                        // Update the progress text (the one with stars or "Locked")
                        if (text.contains("⭐") || text.contains("Locked")) {
                            int stars = prefs.getInt("stars_" + worldId + "_level_1", 0);
                            tv.setText(stars + "/10 ⭐");
                            tv.setTextColor(getResources().getColor(R.color.badge_gold, null));
                        }
                    }
                }
            }
        } else {
            // Grey and dim
            worldView.setAlpha(0.5f);
            
            // Update text to show locked
            if (worldView instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) worldView;
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    if (child instanceof TextView) {
                        TextView tv = (TextView) child;
                        String text = tv.getText().toString();
                        if (text.contains("⭐") || text.contains("Locked")) {
                            tv.setText("🔒 Locked");
                            tv.setTextColor(getResources().getColor(R.color.text_secondary, null));
                        }
                    }
                }
            }
        }
    }
    
    private void loadUserProgress() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Calculate total stars from all worlds
        int totalStars = 0;
        String[] worlds = {Constants.WORLD_FOREST, Constants.WORLD_OCEAN, Constants.WORLD_MOUNTAIN, 
                          Constants.WORLD_DESERT, Constants.WORLD_SKY};
        
        for (String world : worlds) {
            for (int level = 1; level <= 10; level++) {
                totalStars += prefs.getInt("stars_" + world + "_level_" + level, 0);
            }
        }
        
        starsCountText.setText(String.valueOf(totalStars));
        welcomeText.setText("Welcome back, little maestro! 🎵");
    }
    
    private void applyWorldEvolution() {
        // This is now handled by updateWorldsLockStatus()
        updateWorldsLockStatus();
    }
    
    private void startAnimations() {
        // Floating animation for world icons
        animateWorldIcons();
    }
    
    private void animateWorldIcons() {
        int[] worldIds = {
            R.id.world_forest,
            R.id.world_ocean,
            R.id.world_mountain,
            R.id.world_desert,
            R.id.world_sky
        };
        
        for (int i = 0; i < worldIds.length; i++) {
            View world = findViewById(worldIds[i]);
            if (world != null) {
                // Staggered floating animation
                AnimationUtils.animateFloating(world, 2000 + (i * 200));
            }
        }
    }
    
    private void navigateToWorld(String worldId, String worldName) {
        Intent intent = new Intent(this, RhythmGameplayActivity.class);
        intent.putExtra(Constants.EXTRA_WORLD_ID, worldId);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    private void openMagicNotebook() {
        Intent intent = new Intent(this, MagicNotebookActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    
    private void openSettings() {
        // TODO: Open settings dialog
    }
    
    @Override
    public void onBackPressed() {
        // Show exit confirmation or return to main app
        super.onBackPressed();
    }
}
