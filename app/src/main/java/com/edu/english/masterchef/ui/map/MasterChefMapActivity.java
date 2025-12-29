package com.edu.english.masterchef.ui.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.LevelConfig;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Master Chef Map Activity - Level Selection Screen
 * Shows all levels with lock/unlock status
 */
public class MasterChefMapActivity extends AppCompatActivity {

    private MasterChefMapViewModel viewModel;
    private LinearLayout levelNodesContainer;
    private FloatingActionButton fabCookbook;
    private HorizontalScrollView mapScroll;
    private ImageView bgLayerBack, bgLayerMid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_masterchef_map);

        // Apply window insets for notch/cutout support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MasterChefMapViewModel.class);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup UI
        levelNodesContainer = findViewById(R.id.level_nodes_container);
        fabCookbook = findViewById(R.id.fab_cookbook);
        mapScroll = findViewById(R.id.map_scroll);
        bgLayerBack = findViewById(R.id.bg_layer_back);
        bgLayerMid = findViewById(R.id.bg_layer_mid);

        // Setup parallax scrolling
        setupParallaxScrolling();

        fabCookbook.setOnClickListener(v -> {
            // Open Cookbook Activity
            Intent intent = new Intent(this, com.edu.english.masterchef.ui.cookbook.CookbookActivity.class);
            startActivity(intent);
        });

        // Observe levels
        viewModel.getLevels().observe(this, levels -> {
            populateLevelNodes(levels);
        });

        // Observe progress changes to refresh level nodes
        viewModel.getProgress().observe(this, progress -> {
            List<LevelConfig> levels = viewModel.getLevels().getValue();
            if (levels != null) {
                populateLevelNodes(levels);
            }
        });

        // Observe level selection
        viewModel.getSelectedLevel().observe(this, levelConfig -> {
            if (levelConfig != null) {
                showLevelIntro(levelConfig);
            }
        });

        // Load levels
        viewModel.loadLevels();
    }

    /**
     * Populate level nodes in the map
     */
    private void populateLevelNodes(java.util.List<LevelConfig> levels) {
        levelNodesContainer.removeAllViews();

        for (LevelConfig level : levels) {
            LevelNodeView nodeView = new LevelNodeView(this);
            
            // Set level data
            boolean isUnlocked = viewModel.isLevelUnlocked(level.getLevelId());
            int stars = viewModel.getLevelStars(level.getLevelId());
            
            nodeView.setLevel(level, isUnlocked, stars);
            nodeView.setOnClickListener(v -> {
                if (isUnlocked) {
                    viewModel.selectLevel(level);
                } else {
                    nodeView.playLockedAnimation();
                    // Different message for Level 2-10 (coming soon) vs requires completion
                    String message = level.getLevelId() > 1 
                        ? "🔒 Level " + level.getLevelId() + " - Coming Soon!\nStay tuned for more delicious dishes!" 
                        : "Complete previous level to unlock!";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            });

            // Add to container with margin
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(32, 0, 32, 0);
            levelNodesContainer.addView(nodeView, params);
        }
    }

    /**
     * Show level intro dialog
     */
    private void showLevelIntro(LevelConfig level) {
        LevelIntroDialog dialog = new LevelIntroDialog(this, level);
        dialog.setOnStartListener(() -> {
            dialog.dismiss();
            
            // Start game activity
            Intent intent = new Intent(this, com.edu.english.masterchef.ui.game.MasterChefGameActivity.class);
            intent.putExtra(com.edu.english.masterchef.ui.game.MasterChefGameActivity.EXTRA_LEVEL_ID, level.getLevelId());
            startActivity(intent);
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh progress when returning from game
        viewModel.refreshProgress();
    }

    /**
     * Setup parallax scrolling effect
     */
    private void setupParallaxScrolling() {
        mapScroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Parallax effect: back layer moves slower than front
            float parallaxFactorBack = 0.3f;
            float parallaxFactorMid = 0.6f;
            
            bgLayerBack.setTranslationX(-scrollX * parallaxFactorBack);
            bgLayerMid.setTranslationX(-scrollX * parallaxFactorMid);
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
