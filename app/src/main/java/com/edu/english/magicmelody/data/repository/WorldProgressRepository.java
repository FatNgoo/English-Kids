package com.edu.english.magicmelody.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.edu.english.magicmelody.data.MagicMelodyDatabase;
import com.edu.english.magicmelody.data.dao.WorldProgressDao;
import com.edu.english.magicmelody.data.entity.WorldProgress;

import java.util.List;

/**
 * 🌍 WorldProgress Repository
 * 
 * Purpose: Single source of truth for world progress data
 * Handles world unlocks, evolution stages, and completion tracking
 */
public class WorldProgressRepository {
    
    private final WorldProgressDao worldProgressDao;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public WorldProgressRepository(Application application) {
        MagicMelodyDatabase database = MagicMelodyDatabase.getInstance(application);
        worldProgressDao = database.worldProgressDao();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 QUERY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get progress for a specific world
     */
    public LiveData<WorldProgress> getWorldProgress(String worldId, long userId) {
        return worldProgressDao.getWorldProgress(worldId, String.valueOf(userId));
    }
    
    /**
     * Get world progress synchronously
     */
    public WorldProgress getWorldProgressSync(String worldId, long userId) {
        return worldProgressDao.getWorldProgressSync(worldId, String.valueOf(userId));
    }
    
    /**
     * Get all worlds for user
     */
    public LiveData<List<WorldProgress>> getAllWorldsForUser(long userId) {
        return worldProgressDao.getAllWorldsForUser(String.valueOf(userId));
    }
    
    /**
     * Get all worlds synchronously
     */
    public List<WorldProgress> getAllWorldsForUserSync(long userId) {
        return worldProgressDao.getAllWorldsForUserSync(String.valueOf(userId));
    }
    
    /**
     * Get unlocked worlds
     */
    public LiveData<List<WorldProgress>> getUnlockedWorlds(long userId) {
        return worldProgressDao.getUnlockedWorlds(String.valueOf(userId));
    }
    
    /**
     * Get completed worlds
     */
    public LiveData<List<WorldProgress>> getCompletedWorlds(long userId) {
        return worldProgressDao.getCompletedWorlds(String.valueOf(userId));
    }
    
    /**
     * Get worlds with defeated bosses
     */
    public LiveData<List<WorldProgress>> getWorldsWithDefeatedBoss(long userId) {
        return worldProgressDao.getWorldsWithDefeatedBoss(String.valueOf(userId));
    }
    
    /**
     * Get evolution stage for a world
     */
    public int getEvolutionStage(String worldId, long userId) {
        return worldProgressDao.getEvolutionStage(worldId, String.valueOf(userId));
    }
    
    /**
     * Check if world is unlocked
     */
    public boolean isWorldUnlocked(String worldId, long userId) {
        return worldProgressDao.isWorldUnlocked(worldId, String.valueOf(userId));
    }
    
    /**
     * Get total stars across all worlds
     */
    public int getTotalStarsAllWorlds(long userId) {
        return worldProgressDao.getTotalStarsAllWorlds(String.valueOf(userId));
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ✏️ INSERT/UPDATE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Insert new world progress
     */
    public void insert(WorldProgress progress) {
        MagicMelodyDatabase.runAsync(() -> {
            worldProgressDao.insert(progress);
        });
    }
    
    /**
     * Update world progress
     */
    public void update(WorldProgress progress) {
        MagicMelodyDatabase.runAsync(() -> {
            worldProgressDao.update(progress);
        });
    }
    
    /**
     * Unlock a world
     */
    public void unlockWorld(String worldId, long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            // Check if already exists
            WorldProgress existing = worldProgressDao.getWorldProgressSync(worldId, String.valueOf(userId));
            if (existing == null) {
                // Create new entry
                WorldProgress newProgress = new WorldProgress();
                newProgress.setWorldId(worldId);
                newProgress.setUserId(String.valueOf(userId));
                newProgress.unlock();
                worldProgressDao.insert(newProgress);
            } else {
                // Just unlock
                worldProgressDao.unlockWorld(worldId, String.valueOf(userId), System.currentTimeMillis());
            }
        });
    }
    
    /**
     * Initialize default worlds for a new user
     */
    public void initializeWorldsForUser(long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            String[] worldIds = {
                "forest_harmony",
                "ocean_rhythm",
                "sky_melody",
                "crystal_symphony",
                "volcano_beat"
            };
            
            String[] worldNames = {
                "Forest Harmony",
                "Ocean Rhythm",
                "Sky Melody",
                "Crystal Symphony",
                "Volcano Beat"
            };
            
            int[] starsToUnlock = {0, 10, 25, 45, 70};
            
            for (int i = 0; i < worldIds.length; i++) {
                WorldProgress world = WorldProgress.create(
                    worldIds[i],
                    String.valueOf(userId),
                    worldNames[i],
                    i + 1,
                    starsToUnlock[i]
                );
                
                // First world is unlocked by default (already handled in create)
                if (i == 0) {
                    world.unlock();
                }
                
                worldProgressDao.insert(world);
            }
        });
    }
    
    /**
     * Mark world as completed
     */
    public void markCompleted(String worldId, long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            worldProgressDao.markCompleted(worldId, String.valueOf(userId));
        });
    }
    
    /**
     * Update evolution stage
     */
    public void updateEvolutionStage(String worldId, long userId, int stage) {
        MagicMelodyDatabase.runAsync(() -> {
            worldProgressDao.updateEvolutionStage(worldId, String.valueOf(userId), stage);
        });
    }
    
    /**
     * Update saturation level
     */
    public void updateSaturationLevel(String worldId, long userId, float saturation) {
        MagicMelodyDatabase.runAsync(() -> {
            worldProgressDao.updateSaturationLevel(worldId, String.valueOf(userId), saturation);
        });
    }
    
    /**
     * Increment levels completed
     */
    public void incrementLevelsCompleted(String worldId, long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            worldProgressDao.incrementLevelsCompleted(worldId, String.valueOf(userId));
        });
    }
    
    /**
     * Mark boss defeated
     */
    public void markBossDefeated(String worldId, long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            worldProgressDao.markBossDefeated(worldId, String.valueOf(userId));
        });
    }
    
    /**
     * Add stars to world
     */
    public void addStars(String worldId, long userId, int stars) {
        MagicMelodyDatabase.runAsync(() -> {
            worldProgressDao.addStars(worldId, String.valueOf(userId), stars);
        });
    }
    
    /**
     * Update evolution based on level completion
     * Call after completing a level
     */
    public void updateEvolutionFromLevelCompletion(String worldId, long userId, int totalLevels) {
        MagicMelodyDatabase.runAsync(() -> {
            WorldProgress progress = worldProgressDao.getWorldProgressSync(worldId, String.valueOf(userId));
            if (progress != null) {
                progress.updateEvolution();
                worldProgressDao.update(progress);
            }
        });
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🗑️ DELETE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Delete all progress for user
     */
    public void deleteAllForUser(long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            worldProgressDao.deleteAllForUser(String.valueOf(userId));
        });
    }
}
