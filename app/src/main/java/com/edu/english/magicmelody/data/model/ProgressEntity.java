package com.edu.english.magicmelody.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Entity representing player progress for each level
 * Tracks stars, best score, and unlock status
 */
@Entity(tableName = "progress")
public class ProgressEntity {
    
    @PrimaryKey
    @NonNull
    private String lessonId;
    
    private int stars; // 0-3 stars
    private int bestScore;
    private boolean unlocked;
    private int playCount;
    private long lastPlayedAt;
    
    public ProgressEntity() {
        this.lessonId = "";
        this.stars = 0;
        this.bestScore = 0;
        this.unlocked = false;
        this.playCount = 0;
    }
    
    @Ignore
    public ProgressEntity(@NonNull String lessonId, boolean unlocked) {
        this.lessonId = lessonId;
        this.unlocked = unlocked;
        this.stars = 0;
        this.bestScore = 0;
        this.playCount = 0;
    }
    
    // Getters and Setters
    @NonNull
    public String getLessonId() { return lessonId; }
    public void setLessonId(@NonNull String lessonId) { this.lessonId = lessonId; }
    
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = Math.max(0, Math.min(3, stars)); }
    
    public int getBestScore() { return bestScore; }
    public void setBestScore(int bestScore) { this.bestScore = Math.max(this.bestScore, bestScore); }
    
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    
    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }
    public void incrementPlayCount() { this.playCount++; }
    
    public long getLastPlayedAt() { return lastPlayedAt; }
    public void setLastPlayedAt(long lastPlayedAt) { this.lastPlayedAt = lastPlayedAt; }
}
