package com.edu.english.magicmelody.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.edu.english.R;
import com.edu.english.magicmelody.ServiceLocator;
import com.edu.english.magicmelody.audio.AudioMixer;
import com.edu.english.magicmelody.audio.SoundPoolManager;
import com.edu.english.magicmelody.domain.Note;
import com.edu.english.magicmelody.gameplay.engine.GameStateManager;
import com.edu.english.magicmelody.gameplay.engine.HitDetector;
import com.edu.english.magicmelody.gameplay.engine.NoteSpawner;
import com.edu.english.magicmelody.gameplay.engine.RhythmEngine;
import com.edu.english.magicmelody.gameplay.engine.ScoreManager;
import com.edu.english.magicmelody.gameplay.engine.WordCollector;
import com.edu.english.magicmelody.model.HitResult;
import com.edu.english.magicmelody.ui.gameplay.views.RhythmGameView;
import com.edu.english.magicmelody.ui.gameplay.viewmodel.GameplayViewModel;

/**
 * 🎮 Game Activity
 * 
 * Main gameplay screen - simplified version for compilation.
 * Implements all required listener interfaces.
 */
public class GameActivity extends AppCompatActivity implements
        RhythmEngine.RhythmEngineListener,
        NoteSpawner.NoteSpawnListener,
        HitDetector.HitDetectorListener,
        ScoreManager.ScoreListener,
        GameStateManager.GameStateListener {
    
    private static final String TAG = "GameActivity";
    
    public static final String EXTRA_LESSON_ID = "lesson_id";
    public static final String EXTRA_WORLD_ID = "world_id";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    
    // Components
    private RhythmEngine rhythmEngine;
    private NoteSpawner noteSpawner;
    private HitDetector hitDetector;
    private ScoreManager scoreManager;
    private GameStateManager gameStateManager;
    private WordCollector wordCollector;
    private AudioMixer audioMixer;
    private RhythmGameView gameView;
    private GameplayViewModel viewModel;
    
    // State
    private boolean isGameActive = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFullscreen();
        setContentView(R.layout.activity_game);
        
        initializeComponents();
        initializeViewModel();
    }
    
    private void setupFullscreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }
    
    private void initializeComponents() {
        gameView = findViewById(R.id.rhythmGameView);
        
        // Get services from ServiceLocator
        audioMixer = ServiceLocator.getInstance().getAudioMixer();
        
        // Create gameplay components
        rhythmEngine = new RhythmEngine();
        noteSpawner = new NoteSpawner();
        hitDetector = new HitDetector();
        scoreManager = new ScoreManager();
        gameStateManager = new GameStateManager();
        wordCollector = new WordCollector();
        
        // Set listeners
        rhythmEngine.addListener(this);
        noteSpawner.setListener(this);
        hitDetector.setListener(this);
        scoreManager.setListener(this);
        gameStateManager.addListener(this);
    }
    
    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(GameplayViewModel.class);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (isGameActive && rhythmEngine != null) {
            rhythmEngine.resume();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (rhythmEngine != null) {
            rhythmEngine.pause();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rhythmEngine != null) {
            rhythmEngine.stop();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎵 RhythmEngineListener Implementation
    // Parameters: (float deltaTimeMs, float songTimeMs)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onEngineUpdate(float deltaTimeMs, float songTimeMs) {
        // Update game state based on engine timing
    }
    
    @Override
    public void onBeatTick(int beatNumber, float beatProgress) {
        // Handle beat tick for visual feedback
    }
    
    @Override
    public void onStateChanged(RhythmEngine.EngineState newState) {
        Log.d(TAG, "Engine state changed: " + newState);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎯 NoteSpawnListener Implementation
    // Uses NoteSpawner.SpawnedNote (inner class)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onNoteSpawn(NoteSpawner.SpawnedNote note) {
        if (gameView != null) {
            // Convert SpawnedNote to Note for RhythmGameView
            Note displayNote = convertSpawnedNote(note);
            gameView.addNote(displayNote);
        }
    }
    
    @Override
    public void onAllNotesSpawned() {
        Log.d(TAG, "All notes spawned");
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎯 HitDetectorListener Implementation
    // Uses NoteSpawner.SpawnedNote (inner class)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onNoteHit(NoteSpawner.SpawnedNote note, HitResult result) {
        // Process hit
        scoreManager.processHit(result);
        wordCollector.processHit(note, result);
        
        // Visual feedback
        if (gameView != null) {
            String hitType = result.getHitType() != null ? result.getHitType().name() : "OK";
            gameView.showHitFeedback(note.getLaneIndex(), hitType);
        }
        
        // Audio feedback based on hit quality
        if (audioMixer != null) {
            audioMixer.playSfx(SoundPoolManager.SoundType.HIT_PERFECT);
        }
    }
    
    @Override
    public void onNoteMiss(NoteSpawner.SpawnedNote note) {
        scoreManager.processMiss();
        
        if (gameView != null) {
            gameView.showMissFeedback(note.getLaneIndex());
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 ScoreListener Implementation
    // Parameters: onScoreChanged(int newScore, int pointsAdded)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onScoreChanged(int newScore, int pointsAdded) {
        // Update score display
    }
    
    @Override
    public void onComboChanged(int newCombo) {
        // Update combo display
    }
    
    @Override
    public void onComboBreak(int lostCombo) {
        // Handle combo break
    }
    
    @Override
    public void onMultiplierChanged(float newMultiplier) {
        // Update multiplier display
    }
    
    @Override
    public void onMilestoneReached(ScoreManager.MilestoneType type, int value) {
        // Handle milestone achievement
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GameStateListener Implementation
    // onStateChanged takes (GameState oldState, GameState newState)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onStateChanged(GameStateManager.GameState oldState, GameStateManager.GameState newState) {
        Log.d(TAG, "Game state changed: " + oldState + " -> " + newState);
    }
    
    @Override
    public void onCountdownTick(int secondsRemaining) {
        // Update countdown display
    }
    
    @Override
    public void onGameStarted() {
        isGameActive = true;
    }
    
    @Override
    public void onGamePaused() {
        isGameActive = false;
    }
    
    @Override
    public void onGameResumed() {
        isGameActive = true;
    }
    
    @Override
    public void onGameEnded(boolean completed) {
        isGameActive = false;
        // Show results
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Convert NoteSpawner.SpawnedNote to domain.Note for display
     */
    private Note convertSpawnedNote(NoteSpawner.SpawnedNote spawnedNote) {
        Note note = new Note();
        note.setLane(spawnedNote.getLaneIndex());
        note.setSpawnTime((long) spawnedNote.getSpawnTimeMs());
        note.setHitTime((long) spawnedNote.getTargetTimeMs());
        note.setWord(spawnedNote.getWord());
        return note;
    }
}
