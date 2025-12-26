package com.edu.english.magicmelody.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Entity tracking vocabulary mistakes for Glitch Note system
 * Used for intelligent review - notes player got wrong appear more frequently
 */
@Entity(tableName = "mistakes")
public class MistakeEntity {
    
    @PrimaryKey
    @NonNull
    private String vocab;
    
    private int wrongCount;
    private long lastWrongAt;
    private String lessonId; // Track which lesson this vocab belongs to
    
    public MistakeEntity() {
        this.vocab = "";
        this.wrongCount = 0;
    }
    
    @Ignore
    public MistakeEntity(@NonNull String vocab, String lessonId) {
        this.vocab = vocab;
        this.lessonId = lessonId;
        this.wrongCount = 1;
        this.lastWrongAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    @NonNull
    public String getVocab() { return vocab; }
    public void setVocab(@NonNull String vocab) { this.vocab = vocab; }
    
    public int getWrongCount() { return wrongCount; }
    public void setWrongCount(int wrongCount) { this.wrongCount = wrongCount; }
    
    public void incrementWrongCount() {
        this.wrongCount++;
        this.lastWrongAt = System.currentTimeMillis();
    }
    
    public long getLastWrongAt() { return lastWrongAt; }
    public void setLastWrongAt(long lastWrongAt) { this.lastWrongAt = lastWrongAt; }
    
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }
}
