package com.edu.english.magicmelody.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.edu.english.magicmelody.data.entity.WorldProgress;

import java.util.List;

/**
 * 🌍 WorldProgress DAO
 * 
 * Purpose: Data Access Object for WorldProgress entity
 * Tracks world completion, evolution stages, and unlocks
 */
@Dao
public interface WorldProgressDao {
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 QUERY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get progress for a specific world
     */
    @Query("SELECT * FROM world_progress WHERE world_id = :worldId AND user_id = :userId")
    LiveData<WorldProgress> getWorldProgress(String worldId, String userId);
    
    /**
     * Get progress synchronously
     */
    @Query("SELECT * FROM world_progress WHERE world_id = :worldId AND user_id = :userId")
    WorldProgress getWorldProgressSync(String worldId, String userId);
    
    /**
     * Get all world progress for a user
     */
    @Query("SELECT * FROM world_progress WHERE user_id = :userId ORDER BY unlocked_at ASC")
    LiveData<List<WorldProgress>> getAllWorldsForUser(String userId);
    
    /**
     * Get all world progress (synchronous)
     */
    @Query("SELECT * FROM world_progress WHERE user_id = :userId ORDER BY unlocked_at ASC")
    List<WorldProgress> getAllWorldsForUserSync(String userId);
    
    /**
     * Get unlocked worlds
     */
    @Query("SELECT * FROM world_progress WHERE user_id = :userId AND is_unlocked = 1 ORDER BY unlocked_at ASC")
    LiveData<List<WorldProgress>> getUnlockedWorlds(String userId);
    
    /**
     * Get unlocked worlds (synchronous)
     */
    @Query("SELECT * FROM world_progress WHERE user_id = :userId AND is_unlocked = 1")
    List<WorldProgress> getUnlockedWorldsSync(String userId);
    
    /**
     * Get locked worlds
     */
    @Query("SELECT * FROM world_progress WHERE user_id = :userId AND is_unlocked = 0")
    LiveData<List<WorldProgress>> getLockedWorlds(String userId);
    
    /**
     * Count unlocked worlds
     */
    @Query("SELECT COUNT(*) FROM world_progress WHERE user_id = :userId AND is_unlocked = 1")
    int getUnlockedWorldCount(String userId);
    
    /**
     * Get current evolution stage for a world
     */
    @Query("SELECT evolution_stage FROM world_progress WHERE world_id = :worldId AND user_id = :userId")
    int getEvolutionStage(String worldId, String userId);
    
    /**
     * Get saturation level for a world
     */
    @Query("SELECT saturation_level FROM world_progress WHERE world_id = :worldId AND user_id = :userId")
    float getSaturationLevel(String worldId, String userId);
    
    /**
     * Check if a world is unlocked
     */
    @Query("SELECT is_unlocked FROM world_progress WHERE world_id = :worldId AND user_id = :userId")
    boolean isWorldUnlocked(String worldId, String userId);
    
    /**
     * Check if boss is defeated
     */
    @Query("SELECT boss_defeated FROM world_progress WHERE world_id = :worldId AND user_id = :userId")
    boolean isBossDefeated(String worldId, String userId);
    
    /**
     * Get worlds with defeated bosses
     */
    @Query("SELECT * FROM world_progress WHERE user_id = :userId AND boss_defeated = 1")
    LiveData<List<WorldProgress>> getWorldsWithDefeatedBoss(String userId);
    
    /**
     * Get completed worlds (all levels completed)
     */
    @Query("SELECT * FROM world_progress WHERE user_id = :userId AND levels_completed = total_levels")
    LiveData<List<WorldProgress>> getCompletedWorlds(String userId);
    
    /**
     * Get total stars across all worlds
     */
    @Query("SELECT SUM(stars_earned) FROM world_progress WHERE user_id = :userId")
    int getTotalStarsAllWorlds(String userId);
    
    // ═══════════════════════════════════════════════════════════════
    // ✏️ INSERT METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Insert world progress
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WorldProgress progress);
    
    /**
     * Insert multiple world progress entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<WorldProgress> progressList);
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 UPDATE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Update world progress
     */
    @Update
    void update(WorldProgress progress);
    
    /**
     * Unlock a world
     */
    @Query("UPDATE world_progress SET is_unlocked = 1, unlocked_at = :timestamp " +
           "WHERE world_id = :worldId AND user_id = :userId")
    void unlockWorld(String worldId, String userId, long timestamp);
    
    /**
     * Mark world as completed
     */
    @Query("UPDATE world_progress SET levels_completed = total_levels " +
           "WHERE world_id = :worldId AND user_id = :userId")
    void markCompleted(String worldId, String userId);
    
    /**
     * Update evolution stage
     */
    @Query("UPDATE world_progress SET evolution_stage = :stage " +
           "WHERE world_id = :worldId AND user_id = :userId")
    void updateEvolutionStage(String worldId, String userId, int stage);
    
    /**
     * Update saturation level
     */
    @Query("UPDATE world_progress SET saturation_level = :saturation " +
           "WHERE world_id = :worldId AND user_id = :userId")
    void updateSaturationLevel(String worldId, String userId, float saturation);
    
    /**
     * Increment levels completed
     */
    @Query("UPDATE world_progress SET levels_completed = levels_completed + 1 " +
           "WHERE world_id = :worldId AND user_id = :userId")
    void incrementLevelsCompleted(String worldId, String userId);
    
    /**
     * Set levels completed
     */
    @Query("UPDATE world_progress SET levels_completed = :count " +
           "WHERE world_id = :worldId AND user_id = :userId")
    void setLevelsCompleted(String worldId, String userId, int count);
    
    /**
     * Mark boss as defeated
     */
    @Query("UPDATE world_progress SET boss_defeated = 1 " +
           "WHERE world_id = :worldId AND user_id = :userId")
    void markBossDefeated(String worldId, String userId);
    
    /**
     * Add stars to world
     */
    @Query("UPDATE world_progress SET stars_earned = stars_earned + :stars " +
           "WHERE world_id = :worldId AND user_id = :userId")
    void addStars(String worldId, String userId, int stars);
    
    /**
     * Set stars earned for world
     */
    @Query("UPDATE world_progress SET stars_earned = :stars " +
           "WHERE world_id = :worldId AND user_id = :userId")
    void setStarsEarned(String worldId, String userId, int stars);
    
    // ═══════════════════════════════════════════════════════════════
    // 🗑️ DELETE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Delete world progress
     */
    @Delete
    void delete(WorldProgress progress);
    
    /**
     * Delete all progress for user
     */
    @Query("DELETE FROM world_progress WHERE user_id = :userId")
    void deleteAllForUser(String userId);
    
    /**
     * Delete all progress (reset)
     */
    @Query("DELETE FROM world_progress")
    void deleteAll();
}
