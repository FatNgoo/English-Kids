package com.edu.english.masterchef.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.edu.english.masterchef.data.model.PlayerProgress;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for player progress data
 * Saves/loads from SharedPreferences
 */
public class ProgressRepository {
    
    private static final String PREFS_NAME = "masterchef_progress";
    private static final String KEY_UNLOCKED_LEVELS = "unlocked_levels";
    private static final String KEY_LEVEL_STARS = "level_stars";
    private static final String KEY_LEVEL_SCORES = "level_scores";
    private static final String KEY_UNLOCKED_DISHES = "unlocked_dishes";

    private static ProgressRepository instance;
    private SharedPreferences prefs;
    private Gson gson;
    private PlayerProgress cachedProgress;

    private ProgressRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        cachedProgress = loadProgress();
    }

    public static synchronized ProgressRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ProgressRepository(context);
        }
        return instance;
    }

    /**
     * Get current player progress
     */
    public PlayerProgress getProgress() {
        if (cachedProgress == null) {
            cachedProgress = loadProgress();
        }
        return cachedProgress;
    }

    /**
     * Save player progress
     */
    public void saveProgress(PlayerProgress progress) {
        SharedPreferences.Editor editor = prefs.edit();

        // Save unlocked levels
        String unlockedLevelsJson = gson.toJson(progress.getUnlockedLevels());
        editor.putString(KEY_UNLOCKED_LEVELS, unlockedLevelsJson);

        // Save level stars
        String levelStarsJson = gson.toJson(progress.getLevelStars());
        editor.putString(KEY_LEVEL_STARS, levelStarsJson);

        // Save level scores
        String levelScoresJson = gson.toJson(progress.getLevelScores());
        editor.putString(KEY_LEVEL_SCORES, levelScoresJson);

        // Save unlocked dishes
        String unlockedDishesJson = gson.toJson(progress.getUnlockedDishIds());
        editor.putString(KEY_UNLOCKED_DISHES, unlockedDishesJson);

        editor.apply();
        cachedProgress = progress;
    }

    /**
     * Load player progress from SharedPreferences
     */
    private PlayerProgress loadProgress() {
        PlayerProgress progress = new PlayerProgress();

        // Load unlocked levels
        String unlockedLevelsJson = prefs.getString(KEY_UNLOCKED_LEVELS, null);
        if (unlockedLevelsJson != null) {
            Type type = new TypeToken<List<Integer>>(){}.getType();
            List<Integer> unlockedLevels = gson.fromJson(unlockedLevelsJson, type);
            progress.setUnlockedLevels(unlockedLevels);
        }

        // Load level stars
        String levelStarsJson = prefs.getString(KEY_LEVEL_STARS, null);
        if (levelStarsJson != null) {
            Type type = new TypeToken<Map<Integer, Integer>>(){}.getType();
            Map<Integer, Integer> levelStars = gson.fromJson(levelStarsJson, type);
            progress.setLevelStars(levelStars);
        }

        // Load level scores
        String levelScoresJson = prefs.getString(KEY_LEVEL_SCORES, null);
        if (levelScoresJson != null) {
            Type type = new TypeToken<Map<Integer, Integer>>(){}.getType();
            Map<Integer, Integer> levelScores = gson.fromJson(levelScoresJson, type);
            progress.setLevelScores(levelScores);
        }

        // Load unlocked dishes
        String unlockedDishesJson = prefs.getString(KEY_UNLOCKED_DISHES, null);
        if (unlockedDishesJson != null) {
            Type type = new TypeToken<List<String>>(){}.getType();
            List<String> unlockedDishes = gson.fromJson(unlockedDishesJson, type);
            progress.setUnlockedDishIds(unlockedDishes);
        }

        return progress;
    }

    /**
     * Update level completion
     */
    public void updateLevelCompletion(int levelId, int stars, int score, String dishId) {
        PlayerProgress progress = getProgress();
        progress.updateLevelCompletion(levelId, stars, score, dishId);
        saveProgress(progress);
    }

    /**
     * Unlock a level
     */
    public void unlockLevel(int levelId) {
        PlayerProgress progress = getProgress();
        progress.unlockLevel(levelId);
        saveProgress(progress);
    }

    /**
     * Check if level is unlocked
     */
    public boolean isLevelUnlocked(int levelId) {
        return getProgress().isLevelUnlocked(levelId);
    }

    /**
     * Get stars for a level
     */
    public int getLevelStars(int levelId) {
        return getProgress().getLevelStars(levelId);
    }

    /**
     * Get list of completed dish IDs
     */
    public List<String> getCompletedDishes() {
        return getProgress().getUnlockedDishIds();
    }

    /**
     * Reset all progress (for testing/debug)
     */
    public void resetProgress() {
        prefs.edit().clear().apply();
        cachedProgress = new PlayerProgress();
    }
}
