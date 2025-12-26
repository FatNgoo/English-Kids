package com.edu.english.magicmelody.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Entity representing a collected item in Magic Notebook
 * Stores vocabulary and character info after completing a level
 */
@Entity(tableName = "collections")
public class CollectionEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String lessonId;
    private String theme;
    private String characterName;
    private String vocab;
    private String vocabVietnamese;
    private String melodyFile; // Optional audio file path
    private long collectedAt;
    
    public CollectionEntity() {
        this.collectedAt = System.currentTimeMillis();
    }
    
    @Ignore
    public CollectionEntity(String lessonId, String theme, String characterName, 
                           String vocab, String vocabVietnamese) {
        this.lessonId = lessonId;
        this.theme = theme;
        this.characterName = characterName;
        this.vocab = vocab;
        this.vocabVietnamese = vocabVietnamese;
        this.collectedAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }
    
    public String getVocab() { return vocab; }
    public void setVocab(String vocab) { this.vocab = vocab; }
    
    public String getVocabVietnamese() { return vocabVietnamese; }
    public void setVocabVietnamese(String vocabVietnamese) { this.vocabVietnamese = vocabVietnamese; }
    
    public String getMelodyFile() { return melodyFile; }
    public void setMelodyFile(String melodyFile) { this.melodyFile = melodyFile; }
    
    public long getCollectedAt() { return collectedAt; }
    public void setCollectedAt(long collectedAt) { this.collectedAt = collectedAt; }
}
