package com.edu.english.masterchef.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks player progress across all levels
 */
public class PlayerProgress {
    private List<Integer> unlockedLevels;
    private Map<Integer, Integer> levelStars; // levelId -> stars (0-3)
    private List<String> unlockedDishIds; // For cookbook
    private Map<Integer, Integer> levelScores; // levelId -> best score
    private int totalStars;

    public PlayerProgress() {
        this.unlockedLevels = new ArrayList<>();
        this.levelStars = new HashMap<>();
        this.unlockedDishIds = new ArrayList<>();
        this.levelScores = new HashMap<>();
        this.totalStars = 0;
        
        // Unlock first level by default
        this.unlockedLevels.add(1);
    }

    // Check if level is unlocked
    public boolean isLevelUnlocked(int levelId) {
        return unlockedLevels.contains(levelId);
    }

    // Unlock a level
    public void unlockLevel(int levelId) {
        if (!unlockedLevels.contains(levelId)) {
            unlockedLevels.add(levelId);
        }
    }

    // Get stars for a level (0 if not completed)
    public int getLevelStars(int levelId) {
        return levelStars.getOrDefault(levelId, 0);
    }

    // Update level completion
    public void updateLevelCompletion(int levelId, int stars, int score, String dishId) {
        // Update stars (keep highest)
        int currentStars = levelStars.getOrDefault(levelId, 0);
        if (stars > currentStars) {
            levelStars.put(levelId, stars);
            recalculateTotalStars();
        }

        // Update score (keep highest)
        int currentScore = levelScores.getOrDefault(levelId, 0);
        if (score > currentScore) {
            levelScores.put(levelId, score);
        }

        // Unlock dish in cookbook
        if (!unlockedDishIds.contains(dishId)) {
            unlockedDishIds.add(dishId);
        }
    }

    private void recalculateTotalStars() {
        totalStars = 0;
        for (int stars : levelStars.values()) {
            totalStars += stars;
        }
    }

    // Check if dish is unlocked in cookbook
    public boolean isDishUnlocked(String dishId) {
        return unlockedDishIds.contains(dishId);
    }

    // Getters and Setters
    public List<Integer> getUnlockedLevels() {
        return unlockedLevels;
    }

    public void setUnlockedLevels(List<Integer> unlockedLevels) {
        this.unlockedLevels = unlockedLevels;
    }

    public Map<Integer, Integer> getLevelStars() {
        return levelStars;
    }

    public void setLevelStars(Map<Integer, Integer> levelStars) {
        this.levelStars = levelStars;
        recalculateTotalStars();
    }

    public List<String> getUnlockedDishIds() {
        return unlockedDishIds;
    }

    public void setUnlockedDishIds(List<String> unlockedDishIds) {
        this.unlockedDishIds = unlockedDishIds;
    }

    public Map<Integer, Integer> getLevelScores() {
        return levelScores;
    }

    public void setLevelScores(Map<Integer, Integer> levelScores) {
        this.levelScores = levelScores;
    }

    public int getTotalStars() {
        return totalStars;
    }
}
