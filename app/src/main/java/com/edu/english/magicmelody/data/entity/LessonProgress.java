package com.edu.english.magicmelody.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.edu.english.magicmelody.data.converter.StringListConverter;

import java.util.List;

/**
 * 🎵 LessonProgress Entity
 * 
 * Purpose: Track user's progress on each lesson
 * 
 * Features:
 * - Stores best score, stars earned, completion status
 * - Tracks how many times the lesson was played
 * - Records the best combo achieved
 * 
 * Note: LessonConfig (static data) is loaded from JSON assets.
 * This entity only stores user-specific progress.
 */
@Entity(tableName = "lesson_progress")
public class LessonProgress {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "lesson_id")
    private String lessonId;
    
    @ColumnInfo(name = "user_id")
    private String userId;
    
    @ColumnInfo(name = "world_id")
    private String worldId;
    
    @ColumnInfo(name = "level_number")
    private int levelNumber;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SCORE & PROGRESS
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;
    
    @ColumnInfo(name = "is_unlocked")
    private boolean isUnlocked;
    
    @ColumnInfo(name = "stars_earned")
    private int starsEarned; // 0-3 stars
    
    @ColumnInfo(name = "best_score")
    private int bestScore;
    
    @ColumnInfo(name = "best_combo")
    private int bestCombo;
    
    @ColumnInfo(name = "perfect_hits")
    private int perfectHits;
    
    @ColumnInfo(name = "good_hits")
    private int goodHits;
    
    @ColumnInfo(name = "ok_hits")
    private int okHits;
    
    @ColumnInfo(name = "miss_count")
    private int missCount;
    
    // ═══════════════════════════════════════════════════════════════
    // 📈 STATISTICS
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "times_played")
    private int timesPlayed;
    
    @ColumnInfo(name = "total_score_all_attempts")
    private int totalScoreAllAttempts;
    
    @ColumnInfo(name = "first_completed_at")
    private long firstCompletedAt;
    
    @ColumnInfo(name = "last_played_at")
    private long lastPlayedAt;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public LessonProgress() {
        // Default constructor for Room
    }
    
    /**
     * Constructor with essential fields
     */
    public LessonProgress(String lessonId, long userId, String worldId) {
        this.lessonId = lessonId;
        this.userId = String.valueOf(userId);
        this.worldId = worldId;
        this.isUnlocked = true;
        this.isCompleted = false;
        this.starsEarned = 0;
        this.bestScore = 0;
    }
    
    /**
     * Create a new lesson progress entry
     */
    public static LessonProgress create(String lessonId, String userId, String worldId, int levelNumber) {
        LessonProgress progress = new LessonProgress();
        progress.setLessonId(lessonId);
        progress.setUserId(userId);
        progress.setWorldId(worldId);
        progress.setLevelNumber(levelNumber);
        progress.setUnlocked(levelNumber == 1); // First level always unlocked
        progress.setCompleted(false);
        progress.setStarsEarned(0);
        progress.setBestScore(0);
        return progress;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    @NonNull
    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(@NonNull String lessonId) {
        this.lessonId = lessonId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWorldId() {
        return worldId;
    }

    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public int getStarsEarned() {
        return starsEarned;
    }

    public void setStarsEarned(int starsEarned) {
        this.starsEarned = starsEarned;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public int getBestCombo() {
        return bestCombo;
    }

    public void setBestCombo(int bestCombo) {
        this.bestCombo = bestCombo;
    }

    public int getPerfectHits() {
        return perfectHits;
    }

    public void setPerfectHits(int perfectHits) {
        this.perfectHits = perfectHits;
    }

    public int getGoodHits() {
        return goodHits;
    }

    public void setGoodHits(int goodHits) {
        this.goodHits = goodHits;
    }

    public int getOkHits() {
        return okHits;
    }

    public void setOkHits(int okHits) {
        this.okHits = okHits;
    }

    public int getMissCount() {
        return missCount;
    }

    public void setMissCount(int missCount) {
        this.missCount = missCount;
    }

    public int getTimesPlayed() {
        return timesPlayed;
    }

    public void setTimesPlayed(int timesPlayed) {
        this.timesPlayed = timesPlayed;
    }

    public int getTotalScoreAllAttempts() {
        return totalScoreAllAttempts;
    }

    public void setTotalScoreAllAttempts(int totalScoreAllAttempts) {
        this.totalScoreAllAttempts = totalScoreAllAttempts;
    }

    public long getFirstCompletedAt() {
        return firstCompletedAt;
    }

    public void setFirstCompletedAt(long firstCompletedAt) {
        this.firstCompletedAt = firstCompletedAt;
    }

    public long getLastPlayedAt() {
        return lastPlayedAt;
    }

    public void setLastPlayedAt(long lastPlayedAt) {
        this.lastPlayedAt = lastPlayedAt;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Update progress with new game result
     * @return Number of NEW stars earned (0 if not improved)
     */
    public int updateWithResult(int score, int combo, int perfect, int good, int ok, int miss) {
        timesPlayed++;
        totalScoreAllAttempts += score;
        lastPlayedAt = System.currentTimeMillis();
        
        // Update best stats
        if (combo > bestCombo) bestCombo = combo;
        if (perfect > perfectHits) perfectHits = perfect;
        if (good > goodHits) goodHits = good;
        if (ok > okHits) okHits = ok;
        
        // Update miss count (we want the lowest)
        if (missCount == 0 || miss < missCount) missCount = miss;
        
        // Calculate stars based on score
        int newStars = calculateStars(score);
        int starsGained = 0;
        
        if (score > bestScore) {
            bestScore = score;
        }
        
        if (newStars > starsEarned) {
            starsGained = newStars - starsEarned;
            starsEarned = newStars;
        }
        
        // Mark as completed if at least 1 star
        if (!isCompleted && starsEarned > 0) {
            isCompleted = true;
            firstCompletedAt = System.currentTimeMillis();
        }
        
        return starsGained;
    }
    
    /**
     * Calculate stars based on score percentage
     */
    private int calculateStars(int score) {
        // Assuming max score per level is around 10000
        float percentage = (float) score / 10000f * 100f;
        
        if (percentage >= 90) return 3;
        if (percentage >= 70) return 2;
        if (percentage >= 50) return 1;
        return 0;
    }
    
    /**
     * Calculate average score
     */
    public float getAverageScore() {
        if (timesPlayed == 0) return 0;
        return (float) totalScoreAllAttempts / timesPlayed;
    }
    
    /**
     * Calculate accuracy percentage
     */
    public float getAccuracyPercentage() {
        int totalHits = perfectHits + goodHits + okHits;
        int totalNotes = totalHits + missCount;
        if (totalNotes == 0) return 0;
        return (float) totalHits / totalNotes * 100f;
    }
}
