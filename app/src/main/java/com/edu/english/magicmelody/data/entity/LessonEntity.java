package com.edu.english.magicmelody.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 🎵 Lesson Entity - Represents a lesson in the game
 */
@Entity(tableName = "lessons")
public class LessonEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long worldId;
    private String title;
    private String description;
    private int bpm;            // Beats per minute
    private int difficulty;     // 1-5
    private String musicFile;   // Path to music file
    private String thumbnailUrl;
    private int orderIndex;
    private boolean isUnlocked;
    private boolean isCompleted;
    private int bestScore;
    private int bestStars;
    
    public LessonEntity() {
        this.bpm = 120;
        this.difficulty = 1;
        this.isUnlocked = false;
        this.isCompleted = false;
    }
    
    // Getters
    public long getId() { return id; }
    public long getWorldId() { return worldId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getBpm() { return bpm; }
    public int getDifficulty() { return difficulty; }
    public String getMusicFile() { return musicFile; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public int getOrderIndex() { return orderIndex; }
    public boolean isUnlocked() { return isUnlocked; }
    public boolean isCompleted() { return isCompleted; }
    public int getBestScore() { return bestScore; }
    public int getBestStars() { return bestStars; }
    
    // Setters
    public void setId(long id) { this.id = id; }
    public void setWorldId(long worldId) { this.worldId = worldId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setBpm(int bpm) { this.bpm = bpm; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    public void setMusicFile(String musicFile) { this.musicFile = musicFile; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }
    public void setBestStars(int bestStars) { this.bestStars = bestStars; }
}
