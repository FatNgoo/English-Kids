package com.edu.english.masterchef.ui.game;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.LevelConfig;
import com.edu.english.masterchef.engine.GameStateMachine;
import com.edu.english.masterchef.ui.dialog.LevelCompleteDialog;
import com.edu.english.masterchef.ui.scene1.Scene1Fragment;
import com.edu.english.masterchef.ui.scene2.Scene2Fragment;
import com.edu.english.masterchef.ui.scene3.Scene3Fragment;
import com.edu.english.masterchef.util.TTSManager;

/**
 * Container activity for Master Chef game
 * Hosts Scene1, Scene2, Scene3 fragments and manages transitions
 */
public class MasterChefGameActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL_ID = "level_id";

    private GameViewModel viewModel;
    private TTSManager ttsManager;

    private TextView tvLevelTitle, tvScore;
    private ImageView btnBack, btnHint;

    private int levelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_masterchef_game);

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get level ID from intent
        levelId = getIntent().getIntExtra(EXTRA_LEVEL_ID, 1);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        // Initialize TTS
        ttsManager = TTSManager.getInstance(this);

        // Find views
        findViews();
        setupListeners();
        observeViewModel();

        // Initialize game
        viewModel.initializeGame(levelId);
    }

    private void findViews() {
        tvLevelTitle = findViewById(R.id.tv_level_title);
        tvScore = findViewById(R.id.tv_score);
        btnBack = findViewById(R.id.btn_back);
        btnHint = findViewById(R.id.btn_hint);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnHint.setOnClickListener(v -> showHint());
    }

    private void observeViewModel() {
        // Observe level config
        viewModel.getCurrentLevel().observe(this, this::updateLevelInfo);

        // Observe score
        viewModel.getCurrentScore().observe(this, score -> {
            tvScore.setText("Score: " + score);
        });

        // Observe state changes
        viewModel.getCurrentState().observe(this, this::handleStateChange);

        // Observe errors
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLevelInfo(LevelConfig level) {
        if (level != null) {
            String title = "Level " + level.getLevelId() + ": " + level.getFood().getName();
            tvLevelTitle.setText(title);
        }
    }

    /**
     * Handle game state changes
     */
    private void handleStateChange(GameStateMachine.GameState state) {
        if (state == null) return;
        
        switch (state) {
            case MAP_LOADED:
                // State handled in ViewModel - will auto-transition to ORDERING
                break;

            case ORDERING:
                loadFragment(new Scene1Fragment());
                break;

            case SHOPPING:
                loadFragment(new Scene2Fragment());
                break;

            case COOKING:
                loadFragment(new Scene3Fragment());
                break;

            case COMPLETED:
                showLevelComplete();
                break;

            default:
                break;
        }
    }

    /**
     * Load a fragment into the container with smooth transition animation
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,      // enter animation
                android.R.anim.fade_out,     // exit animation  
                android.R.anim.fade_in,      // pop enter (when going back)
                android.R.anim.fade_out      // pop exit (when going back)
            )
            .replace(R.id.fragment_container, fragment)
            .commit();
    }

    /**
     * Show level complete dialog
     */
    private void showLevelComplete() {
        Integer scoreValue = viewModel.getCurrentScore().getValue();
        int score = scoreValue != null ? scoreValue : 0;
        int stars = viewModel.getStarsForScore(score);
        int timeSeconds = viewModel.getElapsedTimeSeconds();
        int errors = viewModel.getTotalErrors();
        int perfectSteps = viewModel.getPerfectSteps();
        int totalSteps = viewModel.getTotalSteps();
        
        // Next level unlocked if at least 1 star (changed from 3 for better UX)
        boolean nextLevelUnlocked = stars >= 1;

        LevelCompleteDialog dialog = new LevelCompleteDialog(
            this, 
            score, 
            stars, 
            timeSeconds, 
            errors, 
            perfectSteps, 
            totalSteps,
            nextLevelUnlocked
        );

        dialog.setOnActionListener(new LevelCompleteDialog.OnActionListener() {
            @Override
            public void onReplay() {
                // Restart the level
                viewModel.initializeGame(levelId);
            }

            @Override
            public void onContinue() {
                // Go back to map
                finish();
            }
        });

        dialog.show();
    }

    /**
     * Get the ViewModel instance (for fragments to access)
     */
    public GameViewModel getGameViewModel() {
        return viewModel;
    }

    /**
     * Show hint for current scene
     */
    private void showHint() {
        viewModel.incrementHintUsed();
        
        // Get current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        
        if (currentFragment instanceof Scene1Fragment) {
            ((Scene1Fragment) currentFragment).showHint();
        } else if (currentFragment instanceof Scene2Fragment) {
            ((Scene2Fragment) currentFragment).showHint();
        } else if (currentFragment instanceof Scene3Fragment) {
            ((Scene3Fragment) currentFragment).showHint();
        }
        
        Toast.makeText(this, "Hint used! -5 points", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before exiting
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit Game")
            .setMessage("Are you sure you want to exit? Progress will be lost.")
            .setPositiveButton("Yes", (dialog, which) -> finish())
            .setNegativeButton("No", null)
            .show();
    }
}
