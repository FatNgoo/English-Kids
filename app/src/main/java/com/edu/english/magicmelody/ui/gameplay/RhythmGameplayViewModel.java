package com.edu.english.magicmelody.ui.gameplay;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.magicmelody.data.entity.UserProfile;
import com.edu.english.magicmelody.data.repository.AssetDataRepository;
import com.edu.english.magicmelody.data.repository.CollectedNoteRepository;
import com.edu.english.magicmelody.data.repository.LessonProgressRepository;
import com.edu.english.magicmelody.data.repository.UserProfileRepository;
import com.edu.english.magicmelody.data.repository.WorldProgressRepository;
import com.edu.english.magicmelody.model.GameResult;
import com.edu.english.magicmelody.model.GameState;
import com.edu.english.magicmelody.model.HitResult;
import com.edu.english.magicmelody.model.LessonConfig;
import com.edu.english.magicmelody.model.NoteEvent;
import com.edu.english.magicmelody.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎮 Rhythm Gameplay ViewModel
 * 
 * Purpose: Handle rhythm game logic
 * - Note timing and spawning
 * - Hit detection and scoring
 * - Game state management
 * - Result calculation
 */
public class RhythmGameplayViewModel extends AndroidViewModel {
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 DEPENDENCIES
    // ═══════════════════════════════════════════════════════════════
    
    private final UserProfileRepository userProfileRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final WorldProgressRepository worldProgressRepository;
    private final CollectedNoteRepository collectedNoteRepository;
    private final AssetDataRepository assetDataRepository;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 LIVE DATA
    // ═══════════════════════════════════════════════════════════════
    
    private final MutableLiveData<GameState> gameState = new MutableLiveData<>(new GameState());
    private final MutableLiveData<LessonConfig> currentLesson = new MutableLiveData<>();
    private final MutableLiveData<NoteSpawnEvent> noteSpawnEvent = new MutableLiveData<>();
    private final MutableLiveData<HitResult> hitResultEvent = new MutableLiveData<>();
    private final MutableLiveData<WordPopupEvent> wordPopupEvent = new MutableLiveData<>();
    private final MutableLiveData<Integer> countdownValue = new MutableLiveData<>();
    private final MutableLiveData<GameResult> gameResultEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPaused = new MutableLiveData<>(false);
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GAME STATE
    // ═══════════════════════════════════════════════════════════════
    
    private long userId = -1;
    private LessonConfig lessonConfig;
    private List<NoteEvent> noteSequence;
    private int currentNoteIndex = 0;
    private long gameStartTime = 0;
    private long pauseStartTime = 0;
    private long totalPausedTime = 0;
    
    private Handler gameHandler;
    private Runnable gameLoopRunnable;
    private boolean isGameRunning = false;
    
    // Track active notes on screen
    private List<ActiveNote> activeNotes = new ArrayList<>();
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 EVENT CLASSES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Note spawn event for UI
     */
    public static class NoteSpawnEvent {
        public final String noteId;
        public final NoteEvent noteEvent;
        public final int laneIndex;
        
        public NoteSpawnEvent(String noteId, NoteEvent noteEvent) {
            this.noteId = noteId;
            this.noteEvent = noteEvent;
            this.laneIndex = noteEvent.getLane();
        }
    }
    
    /**
     * Word popup event
     */
    public static class WordPopupEvent {
        public final String word;
        public final String noteName;
        public final boolean isNew;
        
        public WordPopupEvent(String word, String noteName, boolean isNew) {
            this.word = word;
            this.noteName = noteName;
            this.isNew = isNew;
        }
    }
    
    /**
     * Active note tracking
     */
    private static class ActiveNote {
        String noteId;
        NoteEvent noteEvent;
        long perfectHitTime;
        boolean isHit = false;
        
        ActiveNote(String noteId, NoteEvent noteEvent, long perfectHitTime) {
            this.noteId = noteId;
            this.noteEvent = noteEvent;
            this.perfectHitTime = perfectHitTime;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public RhythmGameplayViewModel(@NonNull Application application) {
        super(application);
        
        userProfileRepository = new UserProfileRepository(application);
        lessonProgressRepository = new LessonProgressRepository(application);
        worldProgressRepository = new WorldProgressRepository(application);
        collectedNoteRepository = new CollectedNoteRepository(application);
        assetDataRepository = new AssetDataRepository(application);
        
        gameHandler = new Handler(Looper.getMainLooper());
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public LiveData<GameState> getGameState() {
        return gameState;
    }
    
    public LiveData<LessonConfig> getCurrentLesson() {
        return currentLesson;
    }
    
    public LiveData<NoteSpawnEvent> getNoteSpawnEvent() {
        return noteSpawnEvent;
    }
    
    public LiveData<HitResult> getHitResultEvent() {
        return hitResultEvent;
    }
    
    public LiveData<WordPopupEvent> getWordPopupEvent() {
        return wordPopupEvent;
    }
    
    public LiveData<Integer> getCountdownValue() {
        return countdownValue;
    }
    
    public LiveData<GameResult> getGameResultEvent() {
        return gameResultEvent;
    }
    
    public LiveData<Boolean> getIsPaused() {
        return isPaused;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GAME LIFECYCLE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Initialize game with lesson
     */
    public void initialize(String lessonId, long userId) {
        this.userId = userId;
        this.lessonConfig = assetDataRepository.getLessonById(lessonId);
        
        if (lessonConfig == null) {
            // Handle error - lesson not found
            return;
        }
        
        this.noteSequence = lessonConfig.getNoteSequence();
        this.currentNoteIndex = 0;
        this.activeNotes.clear();
        
        // Initialize game state
        GameState state = new GameState(lessonId, lessonConfig.getWorldId());
        state.setTotalNotes(noteSequence != null ? noteSequence.size() : 0);
        gameState.setValue(state);
        
        currentLesson.setValue(lessonConfig);
    }
    
    /**
     * Start game with countdown
     */
    public void startGame() {
        GameState state = gameState.getValue();
        if (state == null) return;
        
        state.setCurrentState(GameState.State.COUNTDOWN);
        gameState.setValue(state);
        
        // Countdown 3-2-1
        countdownValue.setValue(3);
        
        gameHandler.postDelayed(() -> countdownValue.setValue(2), 1000);
        gameHandler.postDelayed(() -> countdownValue.setValue(1), 2000);
        gameHandler.postDelayed(() -> {
            countdownValue.setValue(0);
            beginGameplay();
        }, 3000);
    }
    
    /**
     * Begin actual gameplay after countdown
     */
    private void beginGameplay() {
        GameState state = gameState.getValue();
        if (state == null) return;
        
        state.setCurrentState(GameState.State.PLAYING);
        state.reset();
        state.setTotalNotes(noteSequence != null ? noteSequence.size() : 0);
        gameState.setValue(state);
        
        gameStartTime = System.currentTimeMillis();
        totalPausedTime = 0;
        currentNoteIndex = 0;
        activeNotes.clear();
        isGameRunning = true;
        
        // Start game loop
        startGameLoop();
    }
    
    /**
     * Main game loop
     */
    private void startGameLoop() {
        gameLoopRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isGameRunning) return;
                
                updateGame();
                
                // Run at 60fps
                gameHandler.postDelayed(this, 16);
            }
        };
        
        gameHandler.post(gameLoopRunnable);
    }
    
    /**
     * Update game state each frame
     */
    private void updateGame() {
        if (noteSequence == null || isPaused.getValue() == Boolean.TRUE) return;
        
        long currentTime = System.currentTimeMillis();
        float elapsedSeconds = (currentTime - gameStartTime - totalPausedTime) / 1000f;
        
        GameState state = gameState.getValue();
        if (state == null) return;
        
        state.setElapsedTimeSeconds(elapsedSeconds);
        
        // Spawn upcoming notes
        spawnNotes(elapsedSeconds);
        
        // Check for missed notes
        checkMissedNotes(currentTime);
        
        // Update progress
        float duration = lessonConfig.getDurationSeconds();
        state.setProgressPercent(Math.min(elapsedSeconds / duration * 100f, 100f));
        
        gameState.setValue(state);
        
        // Check if game is complete
        if (elapsedSeconds >= duration && activeNotes.isEmpty()) {
            endGame();
        }
    }
    
    /**
     * Spawn notes that should appear
     */
    private void spawnNotes(float elapsedSeconds) {
        if (noteSequence == null) return;
        
        // Spawn notes that should appear (with lead time for falling)
        float spawnLeadTime = Constants.Gameplay.NOTE_FALL_TIME_SECONDS;
        
        while (currentNoteIndex < noteSequence.size()) {
            NoteEvent note = noteSequence.get(currentNoteIndex);
            
            if (note.getTime() <= elapsedSeconds + spawnLeadTime) {
                // Spawn this note
                String noteId = "note_" + currentNoteIndex + "_" + System.currentTimeMillis();
                long perfectHitTime = gameStartTime + totalPausedTime + (long)(note.getTime() * 1000);
                
                ActiveNote activeNote = new ActiveNote(noteId, note, perfectHitTime);
                activeNotes.add(activeNote);
                
                noteSpawnEvent.setValue(new NoteSpawnEvent(noteId, note));
                
                currentNoteIndex++;
            } else {
                break;
            }
        }
    }
    
    /**
     * Check for notes that were missed
     */
    private void checkMissedNotes(long currentTime) {
        List<ActiveNote> missedNotes = new ArrayList<>();
        
        for (ActiveNote activeNote : activeNotes) {
            if (!activeNote.isHit) {
                // Check if past hit window
                long timeDelta = currentTime - activeNote.perfectHitTime;
                if (timeDelta > Constants.Gameplay.MISS_WINDOW_MS) {
                    missedNotes.add(activeNote);
                    
                    // Register miss
                    GameState state = gameState.getValue();
                    if (state != null) {
                        state.registerMiss();
                        gameState.setValue(state);
                    }
                    
                    // Emit miss result
                    hitResultEvent.setValue(HitResult.miss(activeNote.noteEvent));
                }
            }
        }
        
        activeNotes.removeAll(missedNotes);
    }
    
    /**
     * Handle lane tap
     */
    public void onLaneTap(int laneIndex) {
        if (!isGameRunning || isPaused.getValue() == Boolean.TRUE) return;
        
        long tapTime = System.currentTimeMillis();
        
        // Find closest note in this lane
        ActiveNote closestNote = null;
        long closestDelta = Long.MAX_VALUE;
        
        for (ActiveNote activeNote : activeNotes) {
            if (!activeNote.isHit && activeNote.noteEvent.getLane() == laneIndex) {
                long delta = Math.abs(tapTime - activeNote.perfectHitTime);
                if (delta < closestDelta && delta <= Constants.Gameplay.MISS_WINDOW_MS) {
                    closestNote = activeNote;
                    closestDelta = delta;
                }
            }
        }
        
        if (closestNote != null) {
            // Calculate hit result
            HitResult result = HitResult.fromTiming(
                closestNote.noteEvent, 
                tapTime, 
                closestNote.perfectHitTime
            );
            
            // Mark as hit
            closestNote.isHit = true;
            
            // Update game state
            GameState state = gameState.getValue();
            if (state != null) {
                switch (result.getHitType()) {
                    case PERFECT:
                        state.registerPerfectHit(Constants.Scoring.PERFECT_SCORE);
                        break;
                    case GOOD:
                        state.registerGoodHit(Constants.Scoring.GOOD_SCORE);
                        break;
                    case OK:
                        state.registerOkHit(Constants.Scoring.OK_SCORE);
                        break;
                    default:
                        state.registerMiss();
                }
                
                result.calculateScore(state.getComboMultiplier());
                gameState.setValue(state);
            }
            
            // Emit hit result
            hitResultEvent.setValue(result);
            
            // Handle word collection
            if (result.hasWord() && result.isHit()) {
                collectWord(closestNote.noteEvent);
            }
            
            // Remove hit note from active list
            activeNotes.remove(closestNote);
        }
    }
    
    /**
     * Collect word/note when successfully hit
     */
    private void collectWord(NoteEvent noteEvent) {
        if (!noteEvent.hasWord()) return;
        
        String worldId = lessonConfig.getWorldId();
        String lessonId = lessonConfig.getLessonId();
        
        collectedNoteRepository.collectNote(
            noteEvent.getNote(),
            noteEvent.getWord(),
            "basic",
            worldId,
            lessonId,
            userId,
            (noteId, isNew) -> {
                wordPopupEvent.postValue(new WordPopupEvent(
                    noteEvent.getWord(),
                    noteEvent.getNote(),
                    isNew
                ));
            }
        );
    }
    
    /**
     * Pause game
     */
    public void pauseGame() {
        if (!isGameRunning) return;
        
        isPaused.setValue(true);
        pauseStartTime = System.currentTimeMillis();
        
        GameState state = gameState.getValue();
        if (state != null) {
            state.setCurrentState(GameState.State.PAUSED);
            gameState.setValue(state);
        }
    }
    
    /**
     * Resume game
     */
    public void resumeGame() {
        if (!isGameRunning) return;
        
        isPaused.setValue(false);
        totalPausedTime += System.currentTimeMillis() - pauseStartTime;
        
        GameState state = gameState.getValue();
        if (state != null) {
            state.setCurrentState(GameState.State.PLAYING);
            gameState.setValue(state);
        }
    }
    
    /**
     * End game and calculate results
     */
    private void endGame() {
        isGameRunning = false;
        gameHandler.removeCallbacks(gameLoopRunnable);
        
        GameState state = gameState.getValue();
        if (state == null) return;
        
        state.setCurrentState(GameState.State.COMPLETED);
        gameState.setValue(state);
        
        // Build game result
        GameResult result = new GameResult.Builder()
            .lessonId(lessonConfig.getLessonId())
            .worldId(lessonConfig.getWorldId())
            .levelNumber(lessonConfig.getLevel())
            .score(state.getScore(), lessonConfig.getMaxPossibleScore())
            .hits(state.getPerfectHits(), state.getGoodHits(), state.getOkHits(), state.getMissCount())
            .combo(state.getMaxCombo(), 0) // TODO: Calculate combo bonus
            .time(gameStartTime, System.currentTimeMillis())
            .build();
        
        // Save progress
        lessonProgressRepository.saveGameResult(result, userId, newStars -> {
            result.setNewStarsEarned(newStars);
            
            // Update world progress
            if (newStars > 0) {
                worldProgressRepository.addStars(lessonConfig.getWorldId(), userId, newStars);
            }
            
            // Update evolution
            int totalLevels = assetDataRepository.getTotalLevelsInWorld(lessonConfig.getWorldId());
            worldProgressRepository.updateEvolutionFromLevelCompletion(
                lessonConfig.getWorldId(), userId, totalLevels
            );
            
            // Add XP to user
            userProfileRepository.addExperience(userId, result.getTotalScore() / 10);
        });
        
        gameResultEvent.setValue(result);
    }
    
    /**
     * Quit game early
     */
    public void quitGame() {
        isGameRunning = false;
        gameHandler.removeCallbacks(gameLoopRunnable);
        
        GameState state = gameState.getValue();
        if (state != null) {
            state.setCurrentState(GameState.State.IDLE);
            gameState.setValue(state);
        }
    }
    
    /**
     * Retry current lesson
     */
    public void retryGame() {
        initialize(lessonConfig.getLessonId(), userId);
        startGame();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🧹 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    protected void onCleared() {
        super.onCleared();
        isGameRunning = false;
        if (gameHandler != null && gameLoopRunnable != null) {
            gameHandler.removeCallbacks(gameLoopRunnable);
        }
    }
}
