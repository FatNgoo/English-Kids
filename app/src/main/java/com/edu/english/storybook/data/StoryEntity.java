package com.edu.english.storybook.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.UUID;

/**
 * Room entity for storing stories offline
 */
@Entity(tableName = "stories")
public class StoryEntity {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String title;
    private String category;
    private int age;
    private String readingLevel;
    private long createdAt;
    
    // Store full story as JSON for simplicity
    private String storyJson;

    public StoryEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
    }
    
    public StoryEntity(String title, String category, int age, String readingLevel, String storyJson) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.category = category;
        this.age = age;
        this.readingLevel = readingLevel;
        this.storyJson = storyJson;
        this.createdAt = System.currentTimeMillis();
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    // Alias for activities expecting getTargetAge
    public int getTargetAge() { return age; }

    public String getReadingLevel() { return readingLevel; }
    public void setReadingLevel(String readingLevel) { this.readingLevel = readingLevel; }
    
    // Alias for activities expecting getLevel
    public String getLevel() { return readingLevel; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getStoryJson() { return storyJson; }
    public void setStoryJson(String storyJson) { this.storyJson = storyJson; }
}
