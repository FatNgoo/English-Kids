package com.edu.english.magicmelody.ui.gameplay;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
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
import androidx.gridlayout.widget.GridLayout;

import com.edu.english.R;
import com.edu.english.magicmelody.domain.Note;
import com.edu.english.magicmelody.ui.gameplay.views.RhythmGameView;
import com.edu.english.magicmelody.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 🎵 Rhythm Gameplay Activity
 * 
 * Purpose: Main rhythm game screen with 3 phases
 * - Phase 1: Normal speed (notes 1-7)
 * - Phase 2: Faster speed (notes 8-14)
 * - Phase 3: Fastest speed (notes 15-20)
 */
public class RhythmGameplayActivity extends AppCompatActivity implements RhythmGameView.GameViewListener {
    
    private static final String TAG = "RhythmGameplay";
    private static final String PREFS_NAME = "MagicMelodyPrefs";
    
    // Phase settings - 3 phases
    private static final int PHASE_1_NOTES = 7;   // Notes 1-7
    private static final int PHASE_2_NOTES = 14;  // Notes 8-14
    private static final int PHASE_3_NOTES = 20;  // Notes 15-20
    
    // Speed for each phase
    private static final long PHASE_1_INTERVAL = 1400; // ms between notes (slow)
    private static final long PHASE_2_INTERVAL = 1100; // ms between notes (medium)
    private static final long PHASE_3_INTERVAL = 850;  // ms between notes (fast)
    
    private static final float PHASE_1_SPEED = 350f;  // pixels per second
    private static final float PHASE_2_SPEED = 450f;  // pixels per second
    private static final float PHASE_3_SPEED = 550f;  // pixels per second
    
    // Game completion settings
    private static final int TOTAL_NOTES_PER_LEVEL = 20;
    private static final int MAX_MISSES = 5;
    private static final int THREE_STAR_SCORE = 1800;
    private static final int TWO_STAR_SCORE = 1200;
    private static final int ONE_STAR_SCORE = 600;
    private static final int UNLOCK_MIN_STARS = 2; // Need 2 stars to unlock next level
    
    private String currentWorldId;
    private int currentLevel = 1;
    private int currentPhase = 1;
    
    // Views
    private RhythmGameView gameView;
    private TextView worldNameText;
    private TextView scoreText;
    private TextView progressText;
    private TextView phaseText;
    private TextView puzzleProgressText;
    private ImageButton pauseButton;
    private GridLayout puzzleGrid;
    
    // Piano keys
    private View[] pianoKeys = new View[4];
    
    // Background Music
    private MediaPlayer backgroundMusic;
    private float musicSpeed = 1.0f;
    
    // Game state
    private int score = 0;
    private int combo = 0;
    private int notesSpawned = 0;
    private int notesHit = 0;
    private int notesMissed = 0;
    private boolean isPlaying = false;
    private boolean isGameEnded = false;
    private long gameStartTime;
    private long currentNoteInterval = PHASE_1_INTERVAL;
    private float currentNoteSpeed = PHASE_1_SPEED;
    
    // Text-to-Speech for pronunciation
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;
    
    // Handlers
    private Handler gameHandler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    
    // � World Scene - Objects that awaken
    private Map<String, FrameLayout> puzzlePieces = new HashMap<>();
    private Set<String> awakenedWords = new HashSet<>();
    private List<String> puzzleWords = new ArrayList<>();
    
    // 🌲 FOREST vocabulary - words that match objects in the scene
    private String[][] forestWords = {
        {"Tree", "Grass", "Flower", "Mushroom"},  // Lane 1 - Red
        {"Bird", "Butterfly", "Sun", "Cloud"},    // Lane 2 - Teal
        {"Tree", "Flower", "Grass", "Bird"},      // Lane 3 - Yellow
        {"Sun", "Cloud", "Butterfly", "Mushroom"} // Lane 4 - Mint
    };
    
    // 🌊 OCEAN vocabulary
    private String[][] oceanWords = {
        {"Fish", "Coral", "Seaweed", "Shell"},    // Lane 1 - Red
        {"Whale", "Jellyfish", "Starfish", "Crab"}, // Lane 2 - Teal
        {"Fish", "Shell", "Coral", "Whale"},      // Lane 3 - Yellow
        {"Jellyfish", "Seaweed", "Crab", "Starfish"} // Lane 4 - Mint
    };
    
    // ⛰️ MOUNTAIN vocabulary
    private String[][] mountainWords = {
        {"Goat", "Rock", "Pine", "River"},        // Lane 1 - Red
        {"Eagle", "Snow", "Cabin", "Peak"},       // Lane 2 - Teal
        {"Goat", "Pine", "Rock", "Eagle"},        // Lane 3 - Yellow
        {"Snow", "River", "Cabin", "Peak"}        // Lane 4 - Mint
    };
    
    // 🏜️ DESERT vocabulary
    private String[][] desertWords = {
        {"Camel", "Cactus", "Sand", "Pyramid"},   // Lane 1 - Red
        {"Scorpion", "Lizard", "Oasis", "Sun"},   // Lane 2 - Teal
        {"Camel", "Sand", "Cactus", "Scorpion"},  // Lane 3 - Yellow
        {"Lizard", "Pyramid", "Oasis", "Sun"}     // Lane 4 - Mint
    };
    
    // ☁️ SKY vocabulary
    private String[][] skyWords = {
        {"Plane", "Balloon", "Kite", "Star"},     // Lane 1 - Red
        {"Rocket", "Moon", "Rainbow", "UFO"},     // Lane 2 - Teal
        {"Plane", "Star", "Balloon", "Rocket"},   // Lane 3 - Yellow
        {"Moon", "Kite", "Rainbow", "UFO"}        // Lane 4 - Mint
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rhythm_gameplay);
        
        setupFullscreen();
        setupSystemBars();
        loadIntentData();
        initTextToSpeech();
        initBackgroundMusic();
        initViews();
        initWorldScene(); // 🌍 Initialize world-specific scene
        initPianoKeys();
        initGameplay();
    }
    
    private void setupFullscreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }
    
    private void setupSystemBars() {
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }
    
    private void loadIntentData() {
        currentWorldId = getIntent().getStringExtra(Constants.EXTRA_WORLD_ID);
        currentLevel = getIntent().getIntExtra(Constants.EXTRA_LEVEL_ID, 1);
        
        if (currentWorldId == null) {
            currentWorldId = Constants.WORLD_FOREST;
        }
    }
    
    private void initViews() {
        gameView = findViewById(R.id.rhythmGameView);
        worldNameText = findViewById(R.id.world_name_text);
        scoreText = findViewById(R.id.score_text);
        progressText = findViewById(R.id.progress_text);
        phaseText = findViewById(R.id.phase_text);
        puzzleProgressText = findViewById(R.id.puzzle_progress_text);
        pauseButton = findViewById(R.id.pause_button);
        puzzleGrid = findViewById(R.id.puzzle_grid);
        
        if (gameView != null) {
            gameView.setGameViewListener(this);
        }
        
        if (pauseButton != null) {
            pauseButton.setOnClickListener(v -> togglePause());
        }
    }
    
    // 🧩 Initialize Puzzle Grid based on current world
    private void initWorldScene() {
        if (puzzleGrid == null) return;
        puzzleGrid.removeAllViews();
        puzzlePieces.clear();
        awakenedWords.clear();
        puzzleWords.clear();
        
        // Get 8 unique words for current world
        puzzleWords = getWorldPuzzleWords();
        
        // Create 8 puzzle pieces in 3x3 grid (center piece with world icon)
        float density = getResources().getDisplayMetrics().density;
        int pieceSize = (int) (90 * density);
        int margin = (int) (4 * density);
        
        int wordIndex = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (row == 1 && col == 1) {
                    // Center piece - World icon
                    FrameLayout centerPiece = createPuzzlePiece("", getWorldIconDrawable(), pieceSize, true);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = pieceSize;
                    params.height = pieceSize;
                    params.setMargins(margin, margin, margin, margin);
                    params.rowSpec = GridLayout.spec(row, 1f);
                    params.columnSpec = GridLayout.spec(col, 1f);
                    centerPiece.setLayoutParams(params);
                    puzzleGrid.addView(centerPiece);
                } else {
                    if (wordIndex < puzzleWords.size()) {
                        String word = puzzleWords.get(wordIndex);
                        int iconRes = getWordIconDrawable(word);
                        FrameLayout piece = createPuzzlePiece(word, iconRes, pieceSize, false);
                        
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = pieceSize;
                        params.height = pieceSize;
                        params.setMargins(margin, margin, margin, margin);
                        params.rowSpec = GridLayout.spec(row, 1f);
                        params.columnSpec = GridLayout.spec(col, 1f);
                        piece.setLayoutParams(params);
                        
                        puzzleGrid.addView(piece);
                        puzzlePieces.put(word, piece);
                        wordIndex++;
                    }
                }
            }
        }
        updatePuzzleProgress();
    }
    
    private FrameLayout createPuzzlePiece(String word, int iconRes, int size, boolean isCenter) {
        FrameLayout piece = new FrameLayout(this);
        piece.setBackgroundResource(R.drawable.bg_puzzle_piece_dark);
        piece.setTag(word);
        
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int padding = (int) (12 * getResources().getDisplayMetrics().density);
        icon.setPadding(padding, padding, padding, padding);
        
        if (!isCenter) {
            icon.setColorFilter(android.graphics.Color.argb(200, 60, 60, 60));
            icon.setAlpha(0.4f);
        } else {
            icon.setAlpha(0.6f);
        }
        
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        icon.setLayoutParams(iconParams);
        piece.addView(icon);
        
        if (!isCenter && !word.isEmpty()) {
            TextView label = new TextView(this);
            label.setText(word);
            label.setTextColor(android.graphics.Color.WHITE);
            label.setTextSize(10);
            label.setGravity(android.view.Gravity.CENTER);
            label.setAlpha(0.5f);
            label.setBackgroundColor(android.graphics.Color.argb(100, 0, 0, 0));
            
            FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            labelParams.gravity = android.view.Gravity.BOTTOM;
            label.setLayoutParams(labelParams);
            piece.addView(label);
        }
        return piece;
    }
    
    private List<String> getWorldPuzzleWords() {
        List<String> words = new ArrayList<>();
        switch (currentWorldId) {
            case Constants.WORLD_FOREST:
                words.add("Tree"); words.add("Flower"); words.add("Bird"); words.add("Sun");
                words.add("Cloud"); words.add("Butterfly"); words.add("Mushroom"); words.add("Grass");
                break;
            case Constants.WORLD_OCEAN:
                words.add("Fish"); words.add("Whale"); words.add("Jellyfish"); words.add("Coral");
                words.add("Seaweed"); words.add("Shell"); words.add("Crab"); words.add("Starfish");
                break;
            case Constants.WORLD_MOUNTAIN:
                words.add("Goat"); words.add("Eagle"); words.add("Snow"); words.add("Rock");
                words.add("Pine"); words.add("Cabin"); words.add("Peak"); words.add("River");
                break;
            case Constants.WORLD_DESERT:
                words.add("Camel"); words.add("Cactus"); words.add("Scorpion"); words.add("Lizard");
                words.add("Oasis"); words.add("Pyramid"); words.add("Sand"); words.add("Sun");
                break;
            case Constants.WORLD_SKY:
                words.add("Plane"); words.add("Balloon"); words.add("Rainbow"); words.add("Star");
                words.add("Moon"); words.add("Rocket"); words.add("Kite"); words.add("UFO");
                break;
            default:
                words.add("Tree"); words.add("Flower"); words.add("Bird"); words.add("Sun");
                words.add("Cloud"); words.add("Butterfly"); words.add("Mushroom"); words.add("Grass");
                break;
        }
        return words;
    }
    
    private int getWorldIconDrawable() {
        switch (currentWorldId) {
            case Constants.WORLD_FOREST: return R.drawable.ic_forest_tree;
            case Constants.WORLD_OCEAN: return R.drawable.ic_ocean_whale;
            case Constants.WORLD_MOUNTAIN: return R.drawable.ic_mountain_peak;
            case Constants.WORLD_DESERT: return R.drawable.ic_desert_pyramid;
            case Constants.WORLD_SKY: return R.drawable.ic_sky_rocket;
            default: return R.drawable.ic_forest_tree;
        }
    }
    
    private int getWordIconDrawable(String word) {
        switch (word) {
            case "Tree": return R.drawable.ic_forest_tree;
            case "Flower": return R.drawable.ic_forest_flower;
            case "Bird": return R.drawable.ic_forest_bird;
            case "Sun": return R.drawable.ic_forest_sun;
            case "Cloud": return R.drawable.ic_forest_cloud;
            case "Butterfly": return R.drawable.ic_forest_butterfly;
            case "Mushroom": return R.drawable.ic_forest_mushroom;
            case "Grass": return R.drawable.ic_forest_grass;
            case "Fish": return R.drawable.ic_ocean_fish;
            case "Whale": return R.drawable.ic_ocean_whale;
            case "Jellyfish": return R.drawable.ic_ocean_jellyfish;
            case "Coral": return R.drawable.ic_ocean_coral;
            case "Seaweed": return R.drawable.ic_ocean_seaweed;
            case "Shell": return R.drawable.ic_ocean_shell;
            case "Crab": return R.drawable.ic_ocean_crab;
            case "Starfish": return R.drawable.ic_ocean_starfish;
            case "Goat": return R.drawable.ic_mountain_goat;
            case "Eagle": return R.drawable.ic_mountain_eagle;
            case "Snow": return R.drawable.ic_mountain_snow;
            case "Rock": return R.drawable.ic_mountain_rock;
            case "Pine": return R.drawable.ic_mountain_pine;
            case "Cabin": return R.drawable.ic_mountain_cabin;
            case "Peak": return R.drawable.ic_mountain_peak;
            case "River": return R.drawable.ic_mountain_river;
            case "Camel": return R.drawable.ic_desert_camel;
            case "Cactus": return R.drawable.ic_desert_cactus;
            case "Scorpion": return R.drawable.ic_desert_scorpion;
            case "Lizard": return R.drawable.ic_desert_lizard;
            case "Oasis": return R.drawable.ic_desert_oasis;
            case "Pyramid": return R.drawable.ic_desert_pyramid;
            case "Sand": return R.drawable.ic_desert_sand;
            case "Plane": return R.drawable.ic_sky_plane;
            case "Balloon": return R.drawable.ic_sky_balloon;
            case "Rainbow": return R.drawable.ic_sky_rainbow;
            case "Star": return R.drawable.ic_sky_star;
            case "Moon": return R.drawable.ic_sky_moon;
            case "Rocket": return R.drawable.ic_sky_rocket;
            case "Kite": return R.drawable.ic_sky_kite;
            case "UFO": return R.drawable.ic_sky_ufo;
            default: return R.drawable.ic_forest_tree;
        }
    }
    
    private void awakenPuzzlePiece(String word) {
        if (awakenedWords.contains(word)) {
            return;
        }
        FrameLayout piece = puzzlePieces.get(word);
        if (piece != null) {
            awakenedWords.add(word);
            if (piece.getChildCount() > 0 && piece.getChildAt(0) instanceof ImageView) {
                ImageView icon = (ImageView) piece.getChildAt(0);
                icon.clearColorFilter();
                icon.setAlpha(1.0f);
                if (piece.getChildCount() > 1 && piece.getChildAt(1) instanceof TextView) {
                    TextView label = (TextView) piece.getChildAt(1);
                    label.setAlpha(1.0f);
                    label.setTextColor(android.graphics.Color.YELLOW);
                }
            }
            piece.setBackgroundResource(R.drawable.bg_puzzle_piece);
            piece.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(200)
                .withEndAction(() -> {
                    piece.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start();
                })
                .start();
            updatePuzzleProgress();
            if (awakenedWords.size() == puzzleWords.size()) {
                celebratePuzzleComplete();
            }
        }
    }
    
    private void celebratePuzzleComplete() {
        Toast.makeText(this, "🎉 Puzzle Complete!", Toast.LENGTH_LONG).show();
        for (FrameLayout piece : puzzlePieces.values()) {
            piece.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150)
                .withEndAction(() -> piece.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start())
                .start();
        }
    }
    
    private void updatePuzzleProgress() {
        if (puzzleProgressText != null) {
            puzzleProgressText.setText("🧩 " + awakenedWords.size() + "/" + puzzleWords.size());
        }
    }
    
    private void resetWorldScene() {
        // Reset all puzzle pieces to dark state
        awakenedWords.clear();
        for (FrameLayout piece : puzzlePieces.values()) {
            piece.setBackgroundResource(R.drawable.bg_puzzle_piece_dark);
            if (piece.getChildCount() > 0 && piece.getChildAt(0) instanceof ImageView) {
                ImageView icon = (ImageView) piece.getChildAt(0);
                icon.setColorFilter(android.graphics.Color.argb(200, 60, 60, 60));
                icon.setAlpha(0.4f);
            }
            if (piece.getChildCount() > 1 && piece.getChildAt(1) instanceof TextView) {
                TextView label = (TextView) piece.getChildAt(1);
                label.setAlpha(0.5f);
                label.setTextColor(android.graphics.Color.WHITE);
            }
        }
        updatePuzzleProgress();
    }
    
    private int countAwakenedObjects() {
        return awakenedWords.size();
    }
    
    private int getTotalSceneWords() {
        return puzzleWords.size();
    }
    
    // Get current world vocabulary
    private String[][] getCurrentWorldWords() {
        switch (currentWorldId) {
            case Constants.WORLD_FOREST: return forestWords;
            case Constants.WORLD_OCEAN: return oceanWords;
            case Constants.WORLD_MOUNTAIN: return mountainWords;
            case Constants.WORLD_DESERT: return desertWords;
            case Constants.WORLD_SKY: return skyWords;
            default: return forestWords;
        }
    }
    
    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true;
                    textToSpeech.setSpeechRate(0.9f); // Slightly slower for kids
                }
            }
        });
    }
    
    private void initBackgroundMusic() {
        try {
            backgroundMusic = MediaPlayer.create(this, R.raw.bg_music_gameplay);
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.3f, 0.3f); // Low volume so kids can hear words
            }
        } catch (Exception e) {
            // Music file might not exist yet, that's okay
        }
    }
    
    private void startBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }
    
    private void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }
    
    private void updateMusicSpeed(float speed) {
        if (backgroundMusic != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                backgroundMusic.setPlaybackParams(backgroundMusic.getPlaybackParams().setSpeed(speed));
            } catch (Exception e) {
                // Older devices might not support this
            }
        }
    }
    
    private void initPianoKeys() {
        pianoKeys[0] = findViewById(R.id.piano_key_1);
        pianoKeys[1] = findViewById(R.id.piano_key_2);
        pianoKeys[2] = findViewById(R.id.piano_key_3);
        pianoKeys[3] = findViewById(R.id.piano_key_4);
        
        for (int i = 0; i < 4; i++) {
            final int lane = i;
            View key = pianoKeys[i];
            if (key != null) {
                // Make sure key is clickable
                key.setClickable(true);
                key.setFocusable(true);
                
                key.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // INSTANT hit - no delay!
                            if (gameView != null) {
                                gameView.hitLane(lane);
                            }
                            // Quick visual feedback
                            v.setAlpha(0.7f);
                            v.setScaleY(0.92f);
                            return true;
                            
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            // Reset visual
                            v.setAlpha(1.0f);
                            v.setScaleY(1.0f);
                            return true;
                    }
                    return false;
                });
            }
        }
    }
    
    private void speakWord(String word) {
        if (isTtsReady && textToSpeech != null && word != null && !word.isEmpty()) {
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_" + System.currentTimeMillis());
        }
    }
    
    private void initGameplay() {
        if (worldNameText != null) {
            worldNameText.setText("World: " + getWorldDisplayName(currentWorldId));
        }
        
        updateScoreDisplay();
        updateProgressDisplay();
        updatePhaseDisplay();
        
        // Start game after short delay
        gameHandler.postDelayed(() -> {
            startGame();
        }, 1500);
    }
    
    private void startGame() {
        isPlaying = true;
        isGameEnded = false;
        notesSpawned = 0;
        notesHit = 0;
        notesMissed = 0;
        score = 0;
        combo = 0;
        currentPhase = 1;
        currentNoteInterval = PHASE_1_INTERVAL;
        currentNoteSpeed = PHASE_1_SPEED;
        gameStartTime = System.currentTimeMillis();
        
        // Reset scene to withered state
        resetWorldScene();
        
        updateScoreDisplay();
        updateProgressDisplay();
        updatePhaseDisplay();
        
        // Start background music
        startBackgroundMusic();
        updateMusicSpeed(1.0f);
        
        Toast.makeText(this, "🎵 Phase 1 - Let's Go!", Toast.LENGTH_SHORT).show();
        
        // Start spawning notes
        scheduleNextNote();
        
        // Start game loop
        startGameLoop();
    }
    
    private void checkAndUpdatePhase() {
        int newPhase = currentPhase;
        
        if (notesSpawned > PHASE_2_NOTES) {
            newPhase = 3;
        } else if (notesSpawned > PHASE_1_NOTES) {
            newPhase = 2;
        } else {
            newPhase = 1;
        }
        
        if (newPhase != currentPhase) {
            currentPhase = newPhase;
            
            switch (currentPhase) {
                case 2:
                    currentNoteInterval = PHASE_2_INTERVAL;
                    currentNoteSpeed = PHASE_2_SPEED;
                    updateMusicSpeed(1.15f); // 15% faster
                    Toast.makeText(this, "🔥 Phase 2 - Faster!", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    currentNoteInterval = PHASE_3_INTERVAL;
                    currentNoteSpeed = PHASE_3_SPEED;
                    updateMusicSpeed(1.3f); // 30% faster
                    Toast.makeText(this, "⚡ Phase 3 - Maximum Speed!", Toast.LENGTH_SHORT).show();
                    break;
            }
            
            updatePhaseDisplay();
        }
    }
    
    private void scheduleNextNote() {
        if (!isPlaying || isGameEnded) return;
        
        // Stop spawning if we've reached the total
        if (notesSpawned >= TOTAL_NOTES_PER_LEVEL) return;
        
        gameHandler.postDelayed(() -> {
            if (isPlaying && !isGameEnded && notesSpawned < TOTAL_NOTES_PER_LEVEL) {
                spawnRandomNote();
                scheduleNextNote();
            }
        }, currentNoteInterval); // Use dynamic interval based on phase
    }
    
    private void spawnRandomNote() {
        if (gameView == null || isGameEnded) return;
        
        notesSpawned++;
        updateProgressDisplay();
        
        // Check if phase needs to change
        checkAndUpdatePhase();
        
        int lane = random.nextInt(4);
        String word;
        
        // Use world-specific vocabulary
        String[][] worldWords = getCurrentWorldWords();
        word = worldWords[lane][random.nextInt(worldWords[lane].length)];
        
        Note note = new Note();
        note.setLane(lane);
        note.setWord(word);
        note.setSpawnTime(System.currentTimeMillis());
        note.setHitTime(System.currentTimeMillis() + 2000); // 2 seconds to reach hit line
        
        gameView.addNote(note);
    }
    
    private void startGameLoop() {
        Runnable gameLoop = new Runnable() {
            @Override
            public void run() {
                if (isPlaying && !isGameEnded && gameView != null) {
                    long currentTime = System.currentTimeMillis();
                    gameView.updateNotes(currentTime, currentNoteSpeed); // Use dynamic speed
                    
                    // Check if level is complete
                    checkLevelComplete();
                    
                    gameHandler.postDelayed(this, 16); // ~60 FPS
                }
            }
        };
        gameHandler.post(gameLoop);
    }
    
    private void togglePause() {
        if (isGameEnded) return;
        
        isPlaying = !isPlaying;
        if (isPlaying) {
            startGameLoop();
            scheduleNextNote();
            startBackgroundMusic();
            Toast.makeText(this, "▶️ Resumed", Toast.LENGTH_SHORT).show();
        } else {
            pauseBackgroundMusic();
            Toast.makeText(this, "⏸️ Paused", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateScoreDisplay() {
        if (scoreText != null) {
            scoreText.setText(String.valueOf(score));
        }
    }
    
    private void updateProgressDisplay() {
        if (progressText != null) {
            progressText.setText(notesHit + "/" + TOTAL_NOTES_PER_LEVEL + " ❤️" + (MAX_MISSES - notesMissed));
        }
    }
    
    private void updatePhaseDisplay() {
        if (phaseText != null) {
            String phaseEmoji = "";
            switch (currentPhase) {
                case 1: phaseEmoji = "🎵 Phase 1"; break;
                case 2: phaseEmoji = "🔥 Phase 2"; break;
                case 3: phaseEmoji = "⚡ Phase 3"; break;
            }
            phaseText.setText(phaseEmoji);
        }
    }
    
    private void checkLevelComplete() {
        if (isGameEnded) return;
        
        // Check for game over (too many misses)
        if (notesMissed >= MAX_MISSES) {
            endGame(false);
            return;
        }
        
        // Check for level complete (all notes spawned and processed)
        if (notesSpawned >= TOTAL_NOTES_PER_LEVEL && (notesHit + notesMissed) >= TOTAL_NOTES_PER_LEVEL) {
            endGame(true);
        }
    }
    
    private void endGame(boolean isWin) {
        isGameEnded = true;
        isPlaying = false;
        gameHandler.removeCallbacksAndMessages(null);
        
        // Stop music
        pauseBackgroundMusic();
        
        if (gameView != null) {
            gameView.clearNotes();
        }
        
        // Calculate stars
        int stars = calculateStars();
        
        // Unlock next level if won with 2+ stars
        boolean unlockedNext = false;
        if (isWin && stars >= UNLOCK_MIN_STARS) {
            unlockedNext = unlockNextLevel();
        }
        
        // Show result dialog
        showResultDialog(isWin, stars, unlockedNext);
    }
    
    private boolean unlockNextLevel() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Unlock next level in current world
        String levelKey = "unlocked_" + currentWorldId + "_level_" + (currentLevel + 1);
        boolean levelWasLocked = !prefs.getBoolean(levelKey, false);
        editor.putBoolean(levelKey, true);
        
        // Also unlock the next world (level 1 of next world)
        String nextWorldId = getNextWorld(currentWorldId);
        boolean worldWasLocked = false;
        if (nextWorldId != null) {
            String worldKey = "unlocked_" + nextWorldId + "_level_1";
            worldWasLocked = !prefs.getBoolean(worldKey, false);
            editor.putBoolean(worldKey, true);
        }
        
        // Save best score for current level
        String scoreKey = "score_" + currentWorldId + "_level_" + currentLevel;
        int bestScore = prefs.getInt(scoreKey, 0);
        if (score > bestScore) {
            editor.putInt(scoreKey, score);
        }
        
        // Save stars for current level
        String starsKey = "stars_" + currentWorldId + "_level_" + currentLevel;
        int bestStars = prefs.getInt(starsKey, 0);
        int currentStars = calculateStars();
        if (currentStars > bestStars) {
            editor.putInt(starsKey, currentStars);
        }
        
        editor.apply();
        
        return levelWasLocked || worldWasLocked;
    }
    
    private String getNextWorld(String worldId) {
        switch (worldId) {
            case Constants.WORLD_FOREST: return Constants.WORLD_OCEAN;
            case Constants.WORLD_OCEAN: return Constants.WORLD_MOUNTAIN;
            case Constants.WORLD_MOUNTAIN: return Constants.WORLD_DESERT;
            case Constants.WORLD_DESERT: return Constants.WORLD_SKY;
            default: return null;
        }
    }
    
    private int calculateStars() {
        if (score >= THREE_STAR_SCORE) return 3;
        if (score >= TWO_STAR_SCORE) return 2;
        if (score >= ONE_STAR_SCORE) return 1;
        return 0;
    }
    
    private void showResultDialog(boolean isWin, int stars, boolean unlockedNext) {
        String title;
        String message;
        String starDisplay = "";
        
        for (int i = 0; i < stars; i++) starDisplay += "⭐";
        for (int i = stars; i < 3; i++) starDisplay += "☆";
        
        if (isWin) {
            title = "🎉 Level Complete!";
            message = starDisplay + "\n\n" +
                      "Score: " + score + "\n" +
                      "Notes Hit: " + notesHit + "/" + TOTAL_NOTES_PER_LEVEL + "\n" +
                      "Missed: " + notesMissed;
            
            if (unlockedNext) {
                message += "\n\n🔓 Level " + (currentLevel + 1) + " Unlocked!";
            } else if (stars < UNLOCK_MIN_STARS) {
                message += "\n\n🔒 Need " + UNLOCK_MIN_STARS + " stars to unlock next level";
            }
        } else {
            title = "😢 Game Over";
            message = "Too many misses!\n\n" +
                      "Score: " + score + "\n" +
                      "Notes Hit: " + notesHit + "\n" +
                      "Missed: " + notesMissed + "/" + MAX_MISSES;
        }
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false);
        
        // If WIN with at least 1 star: show Next + Exit
        // If LOSE or 0 stars: show Play Again + Exit
        if (isWin && stars >= 1) {
            builder.setPositiveButton("▶️ NEXT", (dialog, which) -> {
                // Show badge reward dialog before going to next level
                showBadgeRewardDialog();
            });
            builder.setNegativeButton("🏠 EXIT", (dialog, which) -> {
                finish();
            });
        } else {
            builder.setPositiveButton("🔄 PLAY AGAIN", (dialog, which) -> {
                restartGame();
            });
            builder.setNegativeButton("🏠 EXIT", (dialog, which) -> {
                finish();
            });
        }
        
        builder.show();
    }
    
    private void showBadgeRewardDialog() {
        String badgeName = getBadgeForWorld(currentWorldId);
        String badgeEmoji = getBadgeEmojiForWorld(currentWorldId);
        int stars = calculateStars();
        
        String message = "🏅 You earned a badge!\n\n" +
                         badgeEmoji + " " + badgeName + "\n\n" +
                         "⭐ Stars: " + stars + "/3";
        
        // Add scene picture info for all worlds
        int awakened = countAwakenedObjects();
        int total = getTotalSceneWords();
        String worldName = getWorldDisplayName(currentWorldId).replace(" Kingdom", "").replace(" 🌲", "").replace(" 🌊", "").replace(" ⛰️", "").replace(" 🏜️", "").replace(" ☁️", "").trim();
        message += "\n\n🖼️ " + worldName + " Picture: " + awakened + "/" + total + " objects awakened!";
        
        if (awakened == total) {
            message += "\n✨ Perfect Picture! All objects alive!";
        }
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("🎁 Claim Your Reward!")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("🎁 CLAIM REWARD", (dialog, which) -> {
                // Save badge to Magic Notebook
                saveBadgeToNotebook(currentWorldId, currentLevel, stars);
                
                // Save world picture for all worlds
                saveWorldPictureToNotebook();
                
                Toast.makeText(this, "🏅 Badge & Picture saved to Magic Notebook!", Toast.LENGTH_SHORT).show();
                
                // Go to next level or back to world map
                goToNextLevel();
            })
            .setNegativeButton("SKIP", (dialog, which) -> {
                goToNextLevel();
            })
            .show();
    }
    
    private void saveWorldPictureToNotebook() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save picture as earned
        String pictureKey = "picture_" + currentWorldId + "_level_" + currentLevel;
        editor.putBoolean(pictureKey, true);
        
        // Save awakened objects
        int awakened = countAwakenedObjects();
        int total = getTotalSceneWords();
        String awakenedKey = "picture_awakened_" + currentWorldId + "_level_" + currentLevel;
        editor.putInt(awakenedKey, awakened);
        
        String totalKey = "picture_total_" + currentWorldId + "_level_" + currentLevel;
        editor.putInt(totalKey, total);
        
        // Save awakened words list
        String wordsKey = "picture_words_" + currentWorldId + "_level_" + currentLevel;
        editor.putString(wordsKey, String.join(",", awakenedWords));
        
        // Increment total pictures count
        int totalPictures = prefs.getInt("total_pictures", 0);
        editor.putInt("total_pictures", totalPictures + 1);
        
        editor.apply();
    }
    
    private String getBadgeForWorld(String worldId) {
        switch (worldId) {
            case Constants.WORLD_FOREST: return "Forest Guardian";
            case Constants.WORLD_OCEAN: return "Ocean Explorer";
            case Constants.WORLD_MOUNTAIN: return "Mountain Climber";
            case Constants.WORLD_DESERT: return "Desert Wanderer";
            case Constants.WORLD_SKY: return "Sky Master";
            default: return "Champion Badge";
        }
    }
    
    private String getBadgeEmojiForWorld(String worldId) {
        switch (worldId) {
            case Constants.WORLD_FOREST: return "🌲";
            case Constants.WORLD_OCEAN: return "🌊";
            case Constants.WORLD_MOUNTAIN: return "⛰️";
            case Constants.WORLD_DESERT: return "🏜️";
            case Constants.WORLD_SKY: return "☁️";
            default: return "🏆";
        }
    }
    
    private void saveBadgeToNotebook(String worldId, int level, int stars) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save badge as earned
        String badgeKey = "badge_" + worldId + "_level_" + level;
        editor.putBoolean(badgeKey, true);
        
        // Save badge details
        String badgeNameKey = "badge_name_" + worldId + "_level_" + level;
        editor.putString(badgeNameKey, getBadgeForWorld(worldId));
        
        String badgeStarsKey = "badge_stars_" + worldId + "_level_" + level;
        editor.putInt(badgeStarsKey, stars);
        
        // Save timestamp when earned
        String badgeTimeKey = "badge_time_" + worldId + "_level_" + level;
        editor.putLong(badgeTimeKey, System.currentTimeMillis());
        
        // Increment total badges count
        int totalBadges = prefs.getInt("total_badges", 0);
        editor.putInt("total_badges", totalBadges + 1);
        
        editor.apply();
    }
    
    private void goToNextLevel() {
        // Return to world map
        finish();
    }
    
    private void restartGame() {
        if (gameView != null) {
            gameView.reset();
        }
        
        // Reset all state
        score = 0;
        combo = 0;
        notesSpawned = 0;
        notesHit = 0;
        notesMissed = 0;
        isGameEnded = false;
        
        updateScoreDisplay();
        updateProgressDisplay();
        
        // Start game again
        gameHandler.postDelayed(() -> {
            startGame();
        }, 500);
    }
    
    private String getWorldDisplayName(String worldId) {
        switch (worldId) {
            case Constants.WORLD_FOREST: return "Forest Kingdom 🌲";
            case Constants.WORLD_OCEAN: return "Ocean Kingdom 🌊";
            case Constants.WORLD_MOUNTAIN: return "Mountain Kingdom ⛰️";
            case Constants.WORLD_DESERT: return "Desert Kingdom 🏜️";
            case Constants.WORLD_SKY: return "Sky Kingdom ☁️";
            default: return "Unknown Kingdom";
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GameViewListener Implementation
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onNoteHit(Note note, RhythmGameView.HitResult result) {
        int points = 0;
        switch (result) {
            case PERFECT:
                points = 100;
                combo++;
                break;
            case GOOD:
                points = 75;
                combo++;
                break;
            case OK:
                points = 50;
                combo++;
                break;
            default:
                break;
        }
        
        // Apply combo multiplier
        if (combo > 5) {
            points = (int)(points * 1.5f);
        }
        
        score += points;
        notesHit++;
        updateScoreDisplay();
        updateProgressDisplay();
        
        // 🎤 Speak the word when hit successfully!
        if (note != null && note.getWord() != null) {
            speakWord(note.getWord());
            
            // 🧩 Awaken puzzle piece for this word!
            awakenPuzzlePiece(note.getWord());
        }
    }
    
    @Override
    public void onNoteMiss(Note note) {
        combo = 0;
        notesMissed++;
        updateProgressDisplay();
        
        // Check if game over
        checkLevelComplete();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        isPlaying = false;
        pauseBackgroundMusic();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Don't auto-resume - let user tap to continue
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameHandler.removeCallbacksAndMessages(null);
        
        // Release music
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
        
        // Release TTS
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }
    
    @Override
    public void onBackPressed() {
        isPlaying = false;
        pauseBackgroundMusic();
        gameHandler.removeCallbacksAndMessages(null);
        super.onBackPressed();
    }
}

