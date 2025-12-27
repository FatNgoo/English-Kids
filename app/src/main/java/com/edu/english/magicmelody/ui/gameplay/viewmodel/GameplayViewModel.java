package com.edu.english.magicmelody.ui.gameplay.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.magicmelody.domain.GameResult;
import com.edu.english.magicmelody.domain.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎮 GameplayViewModel - ViewModel for rhythm gameplay screen
 * 
 * Manages:
 * - Game state (playing, paused, finished)
 * - Score and combo tracking
 * - Note timing and spawning
 */
public class GameplayViewModel extends AndroidViewModel {
    
    // Game state
    public enum GameState { READY, COUNTDOWN, PLAYING, PAUSED, FINISHED }
    
    private MutableLiveData<GameState> gameState = new MutableLiveData<>(GameState.READY);
    private MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private MutableLiveData<Integer> combo = new MutableLiveData<>(0);
    private MutableLiveData<Integer> maxCombo = new MutableLiveData<>(0);
    private MutableLiveData<Note> nextNote = new MutableLiveData<>();
    private MutableLiveData<GameResult> gameResult = new MutableLiveData<>();
    
    // Stats
    private int perfectCount = 0;
    private int goodCount = 0;
    private int okCount = 0;
    private int missCount = 0;
    private List<String> collectedWords = new ArrayList<>();
    
    // Lesson info
    private String lessonId;
    private String worldId;
    private int bpm = 120;
    
    // Timing
    private long gameStartTime = 0;
    private long pauseTime = 0;
    private long totalPausedDuration = 0;
    
    public GameplayViewModel(@NonNull Application application) {
        super(application);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GAME LIFECYCLE
    // ═══════════════════════════════════════════════════════════════
    
    public void startGame(String lessonId, String worldId, int bpm) {
        this.lessonId = lessonId;
        this.worldId = worldId;
        this.bpm = bpm;
        
        // Reset state
        score.setValue(0);
        combo.setValue(0);
        maxCombo.setValue(0);
        perfectCount = 0;
        goodCount = 0;
        okCount = 0;
        missCount = 0;
        collectedWords.clear();
        
        gameState.setValue(GameState.COUNTDOWN);
    }
    
    public void onCountdownFinished() {
        gameStartTime = System.currentTimeMillis();
        totalPausedDuration = 0;
        gameState.setValue(GameState.PLAYING);
    }
    
    public void pauseGame() {
        if (gameState.getValue() == GameState.PLAYING) {
            pauseTime = System.currentTimeMillis();
            gameState.setValue(GameState.PAUSED);
        }
    }
    
    public void resumeGame() {
        if (gameState.getValue() == GameState.PAUSED) {
            totalPausedDuration += System.currentTimeMillis() - pauseTime;
            gameState.setValue(GameState.PLAYING);
        }
    }
    
    public void finishGame() {
        gameState.setValue(GameState.FINISHED);
        
        // Calculate final result
        int finalScore = score.getValue() != null ? score.getValue() : 0;
        int finalMaxCombo = maxCombo.getValue() != null ? maxCombo.getValue() : 0;
        
        // Parse lessonId to long, default to 0 if invalid
        long lessonIdLong = 0;
        try {
            lessonIdLong = Long.parseLong(lessonId);
        } catch (NumberFormatException e) {
            // Use hash code if lessonId is not a number
            lessonIdLong = lessonId != null ? lessonId.hashCode() : 0;
        }
        
        GameResult result = new GameResult(
            lessonIdLong,
            finalScore,
            finalMaxCombo,
            perfectCount,
            goodCount,
            okCount,
            missCount,
            calculateStars(finalScore),
            new ArrayList<>(collectedWords)
        );
        
        gameResult.setValue(result);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎯 NOTE HANDLING
    // ═══════════════════════════════════════════════════════════════
    
    public void onNoteHit(Note note, String hitResult) {
        int currentCombo = combo.getValue() != null ? combo.getValue() : 0;
        int currentScore = score.getValue() != null ? score.getValue() : 0;
        int currentMaxCombo = maxCombo.getValue() != null ? maxCombo.getValue() : 0;
        
        int points = 0;
        
        switch (hitResult) {
            case "PERFECT":
                perfectCount++;
                points = 100;
                currentCombo++;
                break;
            case "GOOD":
                goodCount++;
                points = 75;
                currentCombo++;
                break;
            case "OK":
                okCount++;
                points = 50;
                currentCombo++;
                break;
        }
        
        // Combo multiplier
        float multiplier = 1 + (currentCombo / 10) * 0.1f;
        points = (int)(points * multiplier);
        
        score.setValue(currentScore + points);
        combo.setValue(currentCombo);
        
        if (currentCombo > currentMaxCombo) {
            maxCombo.setValue(currentCombo);
        }
        
        // Collect word if present
        if (note.getWord() != null && !note.getWord().isEmpty()) {
            if (!collectedWords.contains(note.getWord())) {
                collectedWords.add(note.getWord());
            }
        }
    }
    
    public void onNoteMiss(Note note) {
        missCount++;
        combo.setValue(0);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⭐ STAR CALCULATION
    // ═══════════════════════════════════════════════════════════════
    
    private int calculateStars(int finalScore) {
        // Simple star calculation based on score
        if (finalScore >= 9000) return 3;
        if (finalScore >= 7000) return 2;
        if (finalScore >= 5000) return 1;
        return 0;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🕐 TIMING
    // ═══════════════════════════════════════════════════════════════
    
    public long getGameElapsedTime() {
        if (gameStartTime == 0) return 0;
        
        if (gameState.getValue() == GameState.PAUSED) {
            return pauseTime - gameStartTime - totalPausedDuration;
        }
        
        return System.currentTimeMillis() - gameStartTime - totalPausedDuration;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public LiveData<GameState> getGameState() { return gameState; }
    public LiveData<Integer> getScore() { return score; }
    public LiveData<Integer> getCombo() { return combo; }
    public LiveData<Integer> getMaxCombo() { return maxCombo; }
    public LiveData<Note> getNextNote() { return nextNote; }
    public LiveData<GameResult> getGameResult() { return gameResult; }
    
    public String getLessonId() { return lessonId; }
    public String getWorldId() { return worldId; }
    public int getBpm() { return bpm; }
}
