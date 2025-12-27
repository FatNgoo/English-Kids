package com.edu.english.magicmelody.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.edu.english.magicmelody.data.entity.BossProgress;

import java.util.List;

/**
 * 👹 BossProgress DAO
 * 
 * Purpose: Data Access Object for BossProgress entity
 * Tracks boss battle history and achievements
 */
@Dao
public interface BossProgressDao {
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 QUERY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get progress for a specific boss
     */
    @Query("SELECT * FROM boss_progress WHERE boss_id = :bossId AND user_id = :userId")
    LiveData<BossProgress> getBossProgress(String bossId, String userId);
    
    /**
     * Get boss progress synchronously
     */
    @Query("SELECT * FROM boss_progress WHERE boss_id = :bossId AND user_id = :userId")
    BossProgress getBossProgressSync(String bossId, String userId);
    
    /**
     * Get all boss progress for a user
     */
    @Query("SELECT * FROM boss_progress WHERE user_id = :userId ORDER BY first_defeated_at DESC")
    LiveData<List<BossProgress>> getAllBossProgressForUser(String userId);
    
    /**
     * Get all boss progress (synchronous)
     */
    @Query("SELECT * FROM boss_progress WHERE user_id = :userId")
    List<BossProgress> getAllBossProgressForUserSync(String userId);
    
    /**
     * Get boss for a specific world
     */
    @Query("SELECT * FROM boss_progress WHERE world_id = :worldId AND user_id = :userId")
    LiveData<BossProgress> getBossForWorld(String worldId, String userId);
    
    /**
     * Get boss for world (synchronous)
     */
    @Query("SELECT * FROM boss_progress WHERE world_id = :worldId AND user_id = :userId")
    BossProgress getBossForWorldSync(String worldId, String userId);
    
    /**
     * Get defeated bosses
     */
    @Query("SELECT * FROM boss_progress WHERE user_id = :userId AND is_defeated = 1 ORDER BY first_defeated_at DESC")
    LiveData<List<BossProgress>> getDefeatedBosses(String userId);
    
    /**
     * Get undefeated bosses
     */
    @Query("SELECT * FROM boss_progress WHERE user_id = :userId AND is_defeated = 0")
    LiveData<List<BossProgress>> getUndefeatedBosses(String userId);
    
    /**
     * Get perfect victories (no damage taken)
     */
    @Query("SELECT * FROM boss_progress WHERE user_id = :userId AND perfect_victory = 1")
    LiveData<List<BossProgress>> getPerfectVictories(String userId);
    
    /**
     * Count defeated bosses
     */
    @Query("SELECT COUNT(*) FROM boss_progress WHERE user_id = :userId AND is_defeated = 1")
    int getDefeatedBossCount(String userId);
    
    /**
     * Count perfect victories
     */
    @Query("SELECT COUNT(*) FROM boss_progress WHERE user_id = :userId AND perfect_victory = 1")
    int getPerfectVictoryCount(String userId);
    
    /**
     * Check if boss is defeated
     */
    @Query("SELECT is_defeated FROM boss_progress WHERE boss_id = :bossId AND user_id = :userId")
    boolean isBossDefeated(String bossId, String userId);
    
    /**
     * Get best score for a boss
     */
    @Query("SELECT best_score FROM boss_progress WHERE boss_id = :bossId AND user_id = :userId")
    int getBestScore(String bossId, String userId);
    
    /**
     * Get total AR battles count
     */
    @Query("SELECT SUM(ar_battles_count) FROM boss_progress WHERE user_id = :userId")
    int getTotalArBattles(String userId);
    
    /**
     * Get total voice attacks used
     */
    @Query("SELECT SUM(voice_attacks_used) FROM boss_progress WHERE user_id = :userId")
    int getTotalVoiceAttacks(String userId);
    
    /**
     * Check if reward is claimed
     */
    @Query("SELECT reward_claimed FROM boss_progress WHERE boss_id = :bossId AND user_id = :userId")
    boolean isRewardClaimed(String bossId, String userId);
    
    // ═══════════════════════════════════════════════════════════════
    // ✏️ INSERT METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Insert boss progress
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BossProgress progress);
    
    /**
     * Insert multiple boss progress entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<BossProgress> progressList);
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 UPDATE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Update boss progress
     */
    @Update
    void update(BossProgress progress);
    
    /**
     * Mark boss as defeated
     */
    @Query("UPDATE boss_progress SET is_defeated = 1, first_defeated_at = :timestamp " +
           "WHERE boss_id = :bossId AND user_id = :userId AND is_defeated = 0")
    void markDefeated(String bossId, String userId, long timestamp);
    
    /**
     * Update best score if higher
     */
    @Query("UPDATE boss_progress SET best_score = :score " +
           "WHERE boss_id = :bossId AND user_id = :userId AND best_score < :score")
    void updateBestScoreIfHigher(String bossId, String userId, int score);
    
    /**
     * Update fastest time if lower
     */
    @Query("UPDATE boss_progress SET fastest_defeat_seconds = :timeSeconds " +
           "WHERE boss_id = :bossId AND user_id = :userId AND (fastest_defeat_seconds = 0 OR fastest_defeat_seconds > :timeSeconds)")
    void updateFastestTimeIfLower(String bossId, String userId, int timeSeconds);
    
    /**
     * Increment attempt count
     */
    @Query("UPDATE boss_progress SET times_attempted = times_attempted + 1 " +
           "WHERE boss_id = :bossId AND user_id = :userId")
    void incrementAttemptCount(String bossId, String userId);
    
    /**
     * Increment AR battles count
     */
    @Query("UPDATE boss_progress SET ar_battles_count = ar_battles_count + 1 " +
           "WHERE boss_id = :bossId AND user_id = :userId")
    void incrementArBattles(String bossId, String userId);
    
    /**
     * Add voice attacks used
     */
    @Query("UPDATE boss_progress SET voice_attacks_used = voice_attacks_used + :count " +
           "WHERE boss_id = :bossId AND user_id = :userId")
    void addVoiceAttacks(String bossId, String userId, int count);
    
    /**
     * Set perfect victory
     */
    @Query("UPDATE boss_progress SET perfect_victory = 1 " +
           "WHERE boss_id = :bossId AND user_id = :userId")
    void setPerfectVictory(String bossId, String userId);
    
    /**
     * Claim reward
     */
    @Query("UPDATE boss_progress SET reward_claimed = 1 " +
           "WHERE boss_id = :bossId AND user_id = :userId")
    void claimReward(String bossId, String userId);
    
    /**
     * Update damage dealt
     */
    @Query("UPDATE boss_progress SET highest_damage_dealt = :damage " +
           "WHERE boss_id = :bossId AND user_id = :userId AND highest_damage_dealt < :damage")
    void updateDamageDealtIfHigher(String bossId, String userId, int damage);
    
    // ═══════════════════════════════════════════════════════════════
    // 🗑️ DELETE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Delete boss progress
     */
    @Delete
    void delete(BossProgress progress);
    
    /**
     * Delete all progress for user
     */
    @Query("DELETE FROM boss_progress WHERE user_id = :userId")
    void deleteAllForUser(String userId);
    
    /**
     * Delete all progress (reset)
     */
    @Query("DELETE FROM boss_progress")
    void deleteAll();
}
