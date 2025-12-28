package com.edu.english.magicmelody.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 🌍 World Entity - Represents a world/zone in the game
 */
@Entity(tableName = "worlds")
public class WorldEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private String description;
    private String theme;        // "forest", "ocean", "sky", etc.
    private String thumbnailUrl;
    private int orderIndex;
    private boolean isUnlocked;
    private int evolutionLevel;  // 0-100 for gray-to-color
    private int totalLessons;
    private int completedLessons;
    
    public WorldEntity() {
        this.isUnlocked = false;
        this.evolutionLevel = 0;
    }
    
    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getTheme() { return theme; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public int getOrderIndex() { return orderIndex; }
    public boolean isUnlocked() { return isUnlocked; }
    public int getEvolutionLevel() { return evolutionLevel; }
    public int getTotalLessons() { return totalLessons; }
    public int getCompletedLessons() { return completedLessons; }
    
    // Setters
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setTheme(String theme) { this.theme = theme; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
    public void setEvolutionLevel(int evolutionLevel) { this.evolutionLevel = evolutionLevel; }
    public void setTotalLessons(int totalLessons) { this.totalLessons = totalLessons; }
    public void setCompletedLessons(int completedLessons) { this.completedLessons = completedLessons; }
    
    // Computed
    public float getCompletionPercentage() {
        if (totalLessons == 0) return 0f;
        return (float) completedLessons / totalLessons * 100f;
    }
}
