package com.edu.english.magicmelody;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.magicmelody.audio.AudioMixer;
import com.edu.english.magicmelody.data.entity.LessonEntity;
import com.edu.english.magicmelody.data.entity.WorldEntity;
import com.edu.english.magicmelody.domain.GameResult;
import com.edu.english.magicmelody.domain.Note;
import com.edu.english.magicmelody.gameplay.achievement.AchievementManager;
import com.edu.english.magicmelody.gameplay.world.EnvironmentEvolution;
import com.edu.english.magicmelody.gameplay.notebook.MagicNotebookManager;
import com.edu.english.magicmelody.gameplay.engine.GameStateManager;
import com.edu.english.magicmelody.gameplay.engine.HitDetector;
import com.edu.english.magicmelody.gameplay.engine.NoteSpawner;
import com.edu.english.magicmelody.gameplay.engine.RhythmEngine;
import com.edu.english.magicmelody.gameplay.engine.ScoreManager;
import com.edu.english.magicmelody.gameplay.engine.WordCollector;
import com.edu.english.magicmelody.model.HitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎼 Game Coordinator
 * 
 * Central coordinator that manages:
 * - Game session lifecycle
 * - Component communication
 * - Event dispatching
 * - State synchronization
 */
public class GameCoordinator implements
        RhythmEngine.RhythmEngineListener,
        NoteSpawner.NoteSpawnListener,
        HitDetector.HitDetectorListener,
        ScoreManager.ScoreListener,
        GameStateManager.GameStateListener {
    
    private static final String TAG = "GameCoordinator";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 GAME SESSION
    // ═══════════════════════════════════════════════════════════════
    
    public static class GameSession {
        public final long lessonId;
        public final long worldId;
        public final int difficulty;
        public final long startTime;
        
        private LessonEntity lesson;
        private WorldEntity world;
        private List<String> wordList = new ArrayList<>();
        
        public GameSession(long lessonId, long worldId, int difficulty) {
            this.lessonId = lessonId;
            this.worldId = worldId;
            this.difficulty = difficulty;
            this.startTime = System.currentTimeMillis();
        }
        
        public void setLesson(LessonEntity lesson) { this.lesson = lesson; }
        public void setWorld(WorldEntity world) { this.world = world; }
        public void setWordList(List<String> words) { this.wordList = words; }
        
        public LessonEntity getLesson() { return lesson; }
        public WorldEntity getWorld() { return world; }
        public List<String> getWordList() { return wordList; }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 STATE
    // ═══════════════════════════════════════════════════════════════
    
    private Context context;
    private GameSession currentSession;
    
    // Core components
    private RhythmEngine rhythmEngine;
    private NoteSpawner noteSpawner;
    private HitDetector hitDetector;
    private ScoreManager scoreManager;
    private GameStateManager gameStateManager;
    private WordCollector wordCollector;
    
    // Audio
    private AudioMixer audioMixer;
    
    // Features
    private EnvironmentEvolution environmentEvolution;
    private AchievementManager achievementManager;
    private MagicNotebookManager notebookManager;
    
    // LiveData for UI updates
    private MutableLiveData<Integer> scoreLiveData = new MutableLiveData<>(0);
    private MutableLiveData<Integer> comboLiveData = new MutableLiveData<>(0);
    private MutableLiveData<GameStateManager.GameState> stateLiveData = new MutableLiveData<>();
    private MutableLiveData<GameResult> resultLiveData = new MutableLiveData<>();
    private MutableLiveData<Note> noteSpawnedLiveData = new MutableLiveData<>();
    private MutableLiveData<HitEvent> hitEventLiveData = new MutableLiveData<>();
    
    // Listeners
    private List<GameEventListener> eventListeners = new ArrayList<>();
    
    // ═══════════════════════════════════════════════════════════════
    // 🔔 LISTENER INTERFACE
    // ═══════════════════════════════════════════════════════════════
    
    public interface GameEventListener {
        default void onGameReady() {}
        default void onCountdownTick(int remaining) {}
        default void onGameStarted() {}
        default void onGamePaused() {}
        default void onGameResumed() {}
        default void onGameEnded(GameResult result) {}
        default void onNoteSpawned(Note note) {}
        default void onNoteHit(Note note, String hitType) {}
        default void onNoteMissed(Note note) {}
        default void onScoreChanged(int score) {}
        default void onComboChanged(int combo) {}
        default void onWordCollected(String word) {}
        default void onBeat(int beatNumber) {}
    }
    
    public static class HitEvent {
        public final Note note;
        public final String hitType;
        public final boolean isHit;
        
        public HitEvent(Note note, String hitType, boolean isHit) {
            this.note = note;
            this.hitType = hitType;
            this.isHit = isHit;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public GameCoordinator(Context context) {
        this.context = context.getApplicationContext();
        
        // Get service instances
        ServiceLocator locator = ServiceLocator.getInstance();
        
        audioMixer = locator.getAudioMixer();
        achievementManager = locator.getAchievementManager();
        notebookManager = locator.getMagicNotebookManager();
        environmentEvolution = locator.getEnvironmentEvolution();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 SESSION MANAGEMENT
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Create a new game session
     */
    public void createSession(long lessonId, long worldId, int difficulty) {
        Log.d(TAG, "Creating session: lesson=" + lessonId + ", world=" + worldId);
        
        currentSession = new GameSession(lessonId, worldId, difficulty);
        
        // Create fresh gameplay components
        createGameplayComponents();
        
        // Load audio
        audioMixer.loadGameplayAudio();
    }
    
    private void createGameplayComponents() {
        ServiceLocator locator = ServiceLocator.getInstance();
        
        // Create new instances
        rhythmEngine = locator.createRhythmEngine();
        rhythmEngine.addListener(this);
        
        noteSpawner = new NoteSpawner();
        noteSpawner.setListener(this);
        
        hitDetector = new HitDetector();
        hitDetector.setListener(this);
        
        scoreManager = locator.createScoreManager();
        scoreManager.setListener(this);
        
        gameStateManager = locator.createGameStateManager();
        gameStateManager.addListener(this);
        
        wordCollector = locator.createWordCollector();
    }
    
    /**
     * Configure session with loaded data
     */
    public void configureSession(LessonEntity lesson, WorldEntity world, List<String> words) {
        if (currentSession == null) {
            Log.e(TAG, "No active session!");
            return;
        }
        
        currentSession.setLesson(lesson);
        currentSession.setWorld(world);
        currentSession.setWordList(words);
        
        // Configure components using correct APIs
        rhythmEngine.prepare(lesson.getBpm());
        noteSpawner.setSpawnAheadTimeMs(2000f);
        noteSpawner.setNoteSpeed(1.0f + currentSession.difficulty * 0.2f);
        
        // Notify ready
        notifyListeners(listener -> listener.onGameReady());
        
        Log.d(TAG, "Session configured: " + lesson.getTitle());
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GAME CONTROL
    // ═══════════════════════════════════════════════════════════════
    
    public void startGame() {
        Log.d(TAG, "🎮 Starting game");
        
        gameStateManager.startPlaying();
        rhythmEngine.start();
        
        if (currentSession != null && currentSession.getLesson() != null) {
            String musicPath = "magicmelody/music/" + currentSession.getLesson().getMusicFile();
            audioMixer.playMusic(musicPath);
        }
        
        notifyListeners(listener -> listener.onGameStarted());
    }
    
    public void pauseGame() {
        Log.d(TAG, "⏸️ Pausing game");
        
        gameStateManager.pause();
        rhythmEngine.pause();
        audioMixer.pauseMusic();
        
        notifyListeners(listener -> listener.onGamePaused());
    }
    
    public void resumeGame() {
        Log.d(TAG, "▶️ Resuming game");
        
        gameStateManager.resume();
        rhythmEngine.resume();
        audioMixer.resumeMusic();
        
        notifyListeners(listener -> listener.onGameResumed());
    }
    
    public void endGame() {
        Log.d(TAG, "🏁 Ending game");
        
        gameStateManager.complete();
        rhythmEngine.stop();
        audioMixer.fadeOutMusic(1000);
        
        // Calculate result
        GameResult result = calculateResult();
        resultLiveData.postValue(result);
        
        notifyListeners(listener -> listener.onGameEnded(result));
    }
    
    public void restartGame() {
        Log.d(TAG, "🔄 Restarting game");
        
        if (currentSession != null) {
            createGameplayComponents();
            
            if (currentSession.getLesson() != null) {
                configureSession(
                    currentSession.getLesson(),
                    currentSession.getWorld(),
                    currentSession.getWordList()
                );
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 👆 INPUT HANDLING
    // ═══════════════════════════════════════════════════════════════
    
    public void onLaneTap(int laneIndex, long tapTimeMs) {
        if (gameStateManager.getCurrentState() != GameStateManager.GameState.PLAYING) {
            return;
        }
        
        hitDetector.checkHit(laneIndex, (float)tapTimeMs);
    }
    
    public void onLaneRelease(int laneIndex) {
        // Future: implement hold note logic
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 RESULTS & PROGRESS
    // ═══════════════════════════════════════════════════════════════
    
    private GameResult calculateResult() {
        List<String> wordStrings = new ArrayList<>();
        for (WordCollector.CollectedWord cw : wordCollector.getCollectedWords()) {
            wordStrings.add(cw.getWord());
        }
        
        return new GameResult(
            currentSession != null ? currentSession.lessonId : 0,
            scoreManager.getCurrentScore(),
            scoreManager.getMaxCombo(),
            scoreManager.getPerfectCount(),
            scoreManager.getGoodCount(),
            scoreManager.getOkCount(),
            scoreManager.getMissCount(),
            scoreManager.getStarRating(),
            wordStrings
        );
    }
    
    private int calculateStarRating() {
        return scoreManager.getStarRating();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎵 RHYTHM ENGINE LISTENER (RhythmEngineListener)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onEngineUpdate(float deltaTimeMs, float songTimeMs) {
        // Update components with current time
    }
    
    @Override
    public void onBeatTick(int beatNumber, float beatProgress) {
        notifyListeners(listener -> listener.onBeat(beatNumber));
    }
    
    @Override
    public void onStateChanged(RhythmEngine.EngineState newState) {
        if (newState == RhythmEngine.EngineState.STOPPED) {
            endGame();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 NOTE SPAWNER LISTENER (NoteSpawnListener)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onNoteSpawn(NoteSpawner.SpawnedNote note) {
        long currentTime = System.currentTimeMillis();
        Note domainNote = new Note(note.getLaneIndex(), currentTime, (long)note.getTargetTimeMs());
        noteSpawnedLiveData.postValue(domainNote);
        notifyListeners(listener -> listener.onNoteSpawned(domainNote));
    }
    
    @Override
    public void onAllNotesSpawned() {
        Log.d(TAG, "All notes spawned");
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎯 HIT DETECTOR LISTENER (HitDetectorListener)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onNoteHit(NoteSpawner.SpawnedNote note, HitResult result) {
        String hitType = result.getHitType().name();
        scoreManager.processHit(result);
        audioMixer.playHitSound(hitType);
        audioMixer.playNoteForLane(note.getLaneIndex(), 4);
        
        long currentTime = System.currentTimeMillis();
        Note domainNote = new Note(note.getLaneIndex(), currentTime, (long)note.getTargetTimeMs());
        
        if (note.getWord() != null && !note.getWord().isEmpty()) {
            wordCollector.processHit(note, result);
            notifyListeners(listener -> listener.onWordCollected(note.getWord()));
        }
        
        hitEventLiveData.postValue(new HitEvent(domainNote, hitType, true));
        notifyListeners(listener -> listener.onNoteHit(domainNote, hitType));
    }
    
    @Override
    public void onNoteMiss(NoteSpawner.SpawnedNote note) {
        scoreManager.processMiss();
        audioMixer.playSfx(com.edu.english.magicmelody.audio.SoundPoolManager.SoundType.HIT_MISS);
        
        long currentTime = System.currentTimeMillis();
        Note domainNote = new Note(note.getLaneIndex(), currentTime, (long)note.getTargetTimeMs());
        hitEventLiveData.postValue(new HitEvent(domainNote, "miss", false));
        notifyListeners(listener -> listener.onNoteMissed(domainNote));
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SCORE LISTENER (ScoreListener)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onScoreChanged(int newScore, int pointsAdded) {
        scoreLiveData.postValue(newScore);
        notifyListeners(listener -> listener.onScoreChanged(newScore));
    }
    
    @Override
    public void onComboChanged(int newCombo) {
        comboLiveData.postValue(newCombo);
        notifyListeners(listener -> listener.onComboChanged(newCombo));
        
        if (newCombo > 0 && newCombo % 10 == 0) {
            audioMixer.playComboSound(newCombo);
        }
    }
    
    @Override
    public void onComboBreak(int lostCombo) {
        audioMixer.playSfx(com.edu.english.magicmelody.audio.SoundPoolManager.SoundType.HIT_MISS);
    }
    
    @Override
    public void onMultiplierChanged(float newMultiplier) {
        // Handle multiplier change
    }
    
    @Override
    public void onMilestoneReached(ScoreManager.MilestoneType type, int value) {
        // Handle milestone
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GAME STATE LISTENER (GameStateListener)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onStateChanged(GameStateManager.GameState oldState,
                               GameStateManager.GameState newState) {
        stateLiveData.postValue(newState);
    }
    
    @Override
    public void onCountdownTick(int secondsRemaining) {
        notifyListeners(listener -> listener.onCountdownTick(secondsRemaining));
    }
    
    @Override
    public void onGameStarted() {
        notifyListeners(listener -> listener.onGameStarted());
    }
    
    @Override
    public void onGamePaused() {
        notifyListeners(listener -> listener.onGamePaused());
    }
    
    @Override
    public void onGameResumed() {
        notifyListeners(listener -> listener.onGameResumed());
    }
    
    @Override
    public void onGameEnded(boolean completed) {
        GameResult result = calculateResult();
        resultLiveData.postValue(result);
        notifyListeners(listener -> listener.onGameEnded(result));
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 LIVE DATA GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public LiveData<Integer> getScoreLiveData() { return scoreLiveData; }
    public LiveData<Integer> getComboLiveData() { return comboLiveData; }
    public LiveData<GameStateManager.GameState> getStateLiveData() { return stateLiveData; }
    public LiveData<GameResult> getResultLiveData() { return resultLiveData; }
    public LiveData<Note> getNoteSpawnedLiveData() { return noteSpawnedLiveData; }
    public LiveData<HitEvent> getHitEventLiveData() { return hitEventLiveData; }
    
    public GameSession getCurrentSession() { return currentSession; }
    public GameStateManager.GameState getCurrentState() { 
        return gameStateManager != null ? gameStateManager.getCurrentState() : null; 
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔔 LISTENER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════
    
    public void addEventListener(GameEventListener listener) {
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }
    
    public void removeEventListener(GameEventListener listener) {
        eventListeners.remove(listener);
    }
    
    private void notifyListeners(java.util.function.Consumer<GameEventListener> action) {
        for (GameEventListener listener : eventListeners) {
            action.accept(listener);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    public void release() {
        if (rhythmEngine != null) {
            rhythmEngine.stop();
        }
        
        eventListeners.clear();
        currentSession = null;
    }
}
