package com.edu.english.magicmelody.ui.gameplay;

import android.app.Application;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.magicmelody.core.game.GameLoop;
import com.edu.english.magicmelody.core.game.JudgeResult;
import com.edu.english.magicmelody.core.game.Note;
import com.edu.english.magicmelody.core.game.RhythmEngine;
import com.edu.english.magicmelody.data.model.CollectionEntity;
import com.edu.english.magicmelody.data.model.Lesson;
import com.edu.english.magicmelody.data.model.ProgressEntity;
import com.edu.english.magicmelody.data.model.UserProfile;
import com.edu.english.magicmelody.data.repo.CollectionRepository;
import com.edu.english.magicmelody.data.repo.MistakeRepository;
import com.edu.english.magicmelody.data.repo.ProgressRepository;
import com.edu.english.magicmelody.utils.LessonManager;
import com.edu.english.magicmelody.utils.PreferencesManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for GameplayActivity
 * Manages game state, scoring, and environment evolution
 */
public class GameplayViewModel extends AndroidViewModel implements 
        GameLoop.GameLoopListener, RhythmEngine.RhythmEngineListener {
    
    // Repositories
    private final ProgressRepository progressRepository;
    private final CollectionRepository collectionRepository;
    private final MistakeRepository mistakeRepository;
    private final LessonManager lessonManager;
    private final PreferencesManager preferencesManager;
    private final ExecutorService executor;
    
    // Game components
    private final GameLoop gameLoop;
    private final RhythmEngine rhythmEngine;
    
    // LiveData
    private final MutableLiveData<GameState> gameStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> scoreLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> comboLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Float> progressLiveData = new MutableLiveData<>(0f);
    private final MutableLiveData<JudgeResult> hitResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<EnvironmentState> environmentLiveData = new MutableLiveData<>();
    private final MutableLiveData<GameResult> gameResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> feedbackLiveData = new MutableLiveData<>();
    
    // State
    private Lesson currentLesson;
    private UserProfile userProfile;
    private String lessonId;
    private boolean isGameComplete = false;
    private boolean hasTriggeredBloom = false;
    
    public enum GameState {
        LOADING,
        READY,
        COUNTDOWN,
        PLAYING,
        PAUSED,
        COMPLETED,
        FAILED
    }
    
    public GameplayViewModel(@NonNull Application application) {
        super(application);
        
        progressRepository = new ProgressRepository(application);
        collectionRepository = new CollectionRepository(application);
        mistakeRepository = new MistakeRepository(application);
        lessonManager = LessonManager.getInstance(application);
        preferencesManager = PreferencesManager.getInstance(application);
        executor = Executors.newSingleThreadExecutor();
        
        gameLoop = new GameLoop();
        gameLoop.setListener(this);
        
        rhythmEngine = new RhythmEngine();
        rhythmEngine.setListener(this);
        
        gameStateLiveData.setValue(GameState.LOADING);
        environmentLiveData.setValue(new EnvironmentState(0f));
    }
    
    /**
     * Load lesson and initialize game
     */
    public void loadLesson(String lessonId) {
        this.lessonId = lessonId;
        gameStateLiveData.setValue(GameState.LOADING);
        
        executor.execute(() -> {
            currentLesson = lessonManager.getLessonById(lessonId);
            userProfile = preferencesManager.getUserProfile();
            
            if (currentLesson != null && userProfile != null) {
                rhythmEngine.init(currentLesson, userProfile);
                gameStateLiveData.postValue(GameState.READY);
            } else {
                // Create a default lesson if not found
                currentLesson = createFallbackLesson();
                rhythmEngine.init(currentLesson, userProfile != null ? userProfile : new UserProfile());
                gameStateLiveData.postValue(GameState.READY);
            }
        });
    }
    
    private Lesson createFallbackLesson() {
        // Use LessonManager to create default lessons
        return lessonManager.loadAllLessons().get(0);
    }
    
    /**
     * Start countdown before game
     */
    public void startCountdown() {
        gameStateLiveData.setValue(GameState.COUNTDOWN);
    }
    
    /**
     * Start the game
     */
    public void startGame() {
        if (currentLesson == null) return;
        
        isGameComplete = false;
        hasTriggeredBloom = false;
        
        rhythmEngine.reset();
        rhythmEngine.init(currentLesson, userProfile != null ? userProfile : new UserProfile());
        rhythmEngine.start();
        gameLoop.start();
        
        gameStateLiveData.setValue(GameState.PLAYING);
    }
    
    /**
     * Pause the game
     */
    public void pauseGame() {
        gameLoop.pause();
        gameStateLiveData.setValue(GameState.PAUSED);
    }
    
    /**
     * Resume the game
     */
    public void resumeGame() {
        gameLoop.resume();
        gameStateLiveData.setValue(GameState.PLAYING);
    }
    
    /**
     * Handle key press from piano
     */
    public void onKeyPressed(int lane) {
        if (gameStateLiveData.getValue() != GameState.PLAYING) return;
        
        long pressTime = gameLoop.getGameTime();
        JudgeResult result = rhythmEngine.judgeKeyPress(lane, pressTime);
        
        if (result.getJudgment() != JudgeResult.Judgment.NONE) {
            hitResultLiveData.setValue(result);
            
            // Update score
            scoreLiveData.setValue(rhythmEngine.getScore());
            comboLiveData.setValue(rhythmEngine.getCombo());
            
            // Show feedback
            feedbackLiveData.setValue(result.getFeedbackMessage());
        }
    }
    
    // GameLoop.GameLoopListener
    @Override
    public void onUpdate(long gameTimeMs, float deltaTime) {
        if (gameStateLiveData.getValue() == GameState.PLAYING) {
            rhythmEngine.update(gameTimeMs);
        }
    }
    
    @Override
    public void onRender() {
        // Trigger UI update
    }
    
    // RhythmEngine.RhythmEngineListener
    @Override
    public void onNoteSpawned(Note note) {
        // Note spawned - UI will read from note pool
    }
    
    @Override
    public void onNoteHit(JudgeResult result) {
        // Already handled in onKeyPressed
    }
    
    @Override
    public void onNoteMissed(Note note) {
        // Record mistake for Glitch Note system
        if (note.getVocabKey() != null) {
            mistakeRepository.incrementWrong(note.getVocabKey(), lessonId);
        }
        feedbackLiveData.postValue("Oops! Thử lại nha!");
    }
    
    @Override
    public void onComboChanged(int combo) {
        comboLiveData.postValue(combo);
    }
    
    @Override
    public void onProgressChanged(float progress) {
        progressLiveData.postValue(progress);
        updateEnvironment(progress);
    }
    
    @Override
    public void onSongComplete(int score, int stars) {
        if (isGameComplete) return;
        isGameComplete = true;
        
        gameLoop.stop();
        gameStateLiveData.postValue(GameState.COMPLETED);
        
        // Save progress
        executor.execute(() -> {
            // Update progress
            progressRepository.updateScore(lessonId, stars, score);
            
            // Unlock next level
            String[] allLessonIds = lessonManager.getAllLessonIds();
            progressRepository.unlockNextLevel(lessonId, allLessonIds);
            
            // Add to collection
            if (currentLesson != null && currentLesson.getVocabulary() != null) {
                for (Lesson.VocabItem vocab : currentLesson.getVocabulary()) {
                    CollectionEntity collection = new CollectionEntity(
                        lessonId,
                        currentLesson.getTheme(),
                        currentLesson.getMainCharacter(),
                        vocab.getEnglish(),
                        vocab.getVietnamese()
                    );
                    collectionRepository.insert(collection);
                }
            }
        });
        
        // Create game result
        GameResult result = new GameResult();
        result.setScore(score);
        result.setStars(stars);
        result.setAccuracy(rhythmEngine.getAccuracy());
        result.setMaxCombo(rhythmEngine.getMaxCombo());
        result.setPerfectCount(rhythmEngine.getPerfectCount());
        result.setGoodCount(rhythmEngine.getGoodCount());
        result.setMissCount(rhythmEngine.getMissCount());
        
        gameResultLiveData.postValue(result);
    }
    
    /**
     * Update environment based on progress (0.0 - 1.0)
     * This creates the "environmental evolution" effect
     */
    public void updateEnvironment(float progress) {
        EnvironmentState state = new EnvironmentState(progress);
        
        // Saturation: gray at 0, full color at 1
        state.setSaturation(progress);
        
        // Tree scale: 0.5 at start, 1.0 at end
        state.setTreeScale(0.5f + (progress * 0.5f));
        
        // Character happiness
        if (progress >= 1.0f) {
            state.setCharacterState(EnvironmentState.CharacterState.DANCING);
            if (!hasTriggeredBloom) {
                hasTriggeredBloom = true;
                state.setTriggerBloom(true);
            }
        } else if (progress >= 0.7f) {
            state.setCharacterState(EnvironmentState.CharacterState.HAPPY);
        } else if (progress >= 0.3f) {
            state.setCharacterState(EnvironmentState.CharacterState.NORMAL);
        } else {
            state.setCharacterState(EnvironmentState.CharacterState.SAD);
        }
        
        // Bloom particles at 100%
        state.setShowParticles(progress >= 0.9f);
        
        environmentLiveData.postValue(state);
    }
    
    /**
     * Get color filter for saturation effect
     */
    public static ColorMatrixColorFilter getSaturationFilter(float saturation) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(saturation);
        return new ColorMatrixColorFilter(matrix);
    }
    
    // Getters
    public LiveData<GameState> getGameState() { return gameStateLiveData; }
    public LiveData<Integer> getScore() { return scoreLiveData; }
    public LiveData<Integer> getCombo() { return comboLiveData; }
    public LiveData<Float> getProgress() { return progressLiveData; }
    public LiveData<JudgeResult> getHitResult() { return hitResultLiveData; }
    public LiveData<EnvironmentState> getEnvironment() { return environmentLiveData; }
    public LiveData<GameResult> getGameResult() { return gameResultLiveData; }
    public LiveData<String> getFeedback() { return feedbackLiveData; }
    
    public RhythmEngine getRhythmEngine() { return rhythmEngine; }
    public Lesson getCurrentLesson() { return currentLesson; }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        gameLoop.stop();
        executor.shutdown();
    }
    
    /**
     * Environment state for visual effects
     */
    public static class EnvironmentState {
        public enum CharacterState { SAD, NORMAL, HAPPY, DANCING }
        
        private float progress;
        private float saturation;
        private float treeScale;
        private CharacterState characterState;
        private boolean showParticles;
        private boolean triggerBloom;
        
        public EnvironmentState(float progress) {
            this.progress = progress;
            this.saturation = progress;
            this.treeScale = 0.5f + (progress * 0.5f);
            this.characterState = CharacterState.NORMAL;
        }
        
        // Getters and Setters
        public float getProgress() { return progress; }
        public void setProgress(float progress) { this.progress = progress; }
        
        public float getSaturation() { return saturation; }
        public void setSaturation(float saturation) { this.saturation = saturation; }
        
        public float getTreeScale() { return treeScale; }
        public void setTreeScale(float treeScale) { this.treeScale = treeScale; }
        
        public CharacterState getCharacterState() { return characterState; }
        public void setCharacterState(CharacterState state) { this.characterState = state; }
        
        public boolean isShowParticles() { return showParticles; }
        public void setShowParticles(boolean show) { this.showParticles = show; }
        
        public boolean isTriggerBloom() { return triggerBloom; }
        public void setTriggerBloom(boolean trigger) { this.triggerBloom = trigger; }
    }
    
    /**
     * Game result for summary screen
     */
    public static class GameResult {
        private int score;
        private int stars;
        private float accuracy;
        private int maxCombo;
        private int perfectCount;
        private int goodCount;
        private int missCount;
        
        // Getters and Setters
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        
        public int getStars() { return stars; }
        public void setStars(int stars) { this.stars = stars; }
        
        public float getAccuracy() { return accuracy; }
        public void setAccuracy(float accuracy) { this.accuracy = accuracy; }
        
        public int getMaxCombo() { return maxCombo; }
        public void setMaxCombo(int maxCombo) { this.maxCombo = maxCombo; }
        
        public int getPerfectCount() { return perfectCount; }
        public void setPerfectCount(int perfectCount) { this.perfectCount = perfectCount; }
        
        public int getGoodCount() { return goodCount; }
        public void setGoodCount(int goodCount) { this.goodCount = goodCount; }
        
        public int getMissCount() { return missCount; }
        public void setMissCount(int missCount) { this.missCount = missCount; }
    }
}
