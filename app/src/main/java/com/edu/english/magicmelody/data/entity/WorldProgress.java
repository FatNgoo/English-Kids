package com.edu.english.magicmelody.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 🌍 WorldProgress Entity
 * 
 * Purpose: Track user's progress on each world (kingdom)
 * 
 * Features:
 * - Overall world completion status
 * - Evolution stage (gray → color transformation)
 * - Boss battle status
 * - Total stars collected in this world
 */
@Entity(tableName = "world_progress")
public class WorldProgress {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "world_id")
    private String worldId;
    
    @ColumnInfo(name = "user_id")
    private String userId;
    
    @ColumnInfo(name = "world_name")
    private String worldName;
    
    @ColumnInfo(name = "world_order")
    private int worldOrder; // 1=forest, 2=ocean, etc.
    
    // ═══════════════════════════════════════════════════════════════
    // 🔓 UNLOCK STATUS
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "is_unlocked")
    private boolean isUnlocked;
    
    @ColumnInfo(name = "stars_to_unlock")
    private int starsToUnlock;
    
    @ColumnInfo(name = "unlocked_at")
    private long unlockedAt;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 PROGRESS
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "levels_completed")
    private int levelsCompleted;
    
    @ColumnInfo(name = "total_levels")
    private int totalLevels;
    
    @ColumnInfo(name = "stars_earned")
    private int starsEarned; // Stars earned in this world
    
    @ColumnInfo(name = "max_possible_stars")
    private int maxPossibleStars; // totalLevels * 3
    
    // ═══════════════════════════════════════════════════════════════
    // 🌈 EVOLUTION STATUS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Evolution stage determines visual appearance:
     * 0 = Grayscale (locked/no progress)
     * 1 = Partial color (25% complete)
     * 2 = More color (50% complete)
     * 3 = Full bloom (100% complete)
     */
    @ColumnInfo(name = "evolution_stage")
    private int evolutionStage;
    
    /**
     * Saturation value for color filter (0.0 - 1.0)
     */
    @ColumnInfo(name = "saturation_level")
    private float saturationLevel;
    
    // ═══════════════════════════════════════════════════════════════
    // 👹 BOSS STATUS
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "boss_unlocked")
    private boolean bossUnlocked;
    
    @ColumnInfo(name = "boss_defeated")
    private boolean bossDefeated;
    
    @ColumnInfo(name = "boss_defeated_at")
    private long bossDefeatedAt;
    
    @ColumnInfo(name = "boss_best_score")
    private int bossBestScore;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public WorldProgress() {
        // Default constructor for Room
    }
    
    /**
     * Create a new world progress entry
     */
    public static WorldProgress create(String worldId, String userId, String worldName, 
                                        int worldOrder, int starsToUnlock) {
        WorldProgress progress = new WorldProgress();
        progress.setWorldId(worldId);
        progress.setUserId(userId);
        progress.setWorldName(worldName);
        progress.setWorldOrder(worldOrder);
        progress.setStarsToUnlock(starsToUnlock);
        progress.setUnlocked(worldOrder == 1); // First world always unlocked
        progress.setTotalLevels(10);
        progress.setMaxPossibleStars(30); // 10 levels * 3 stars
        progress.setEvolutionStage(worldOrder == 1 ? 0 : -1); // -1 for locked
        progress.setSaturationLevel(0f);
        return progress;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    @NonNull
    public String getWorldId() {
        return worldId;
    }

    public void setWorldId(@NonNull String worldId) {
        this.worldId = worldId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public int getWorldOrder() {
        return worldOrder;
    }

    public void setWorldOrder(int worldOrder) {
        this.worldOrder = worldOrder;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public int getStarsToUnlock() {
        return starsToUnlock;
    }

    public void setStarsToUnlock(int starsToUnlock) {
        this.starsToUnlock = starsToUnlock;
    }

    public long getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(long unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    public int getLevelsCompleted() {
        return levelsCompleted;
    }

    public void setLevelsCompleted(int levelsCompleted) {
        this.levelsCompleted = levelsCompleted;
    }

    public int getTotalLevels() {
        return totalLevels;
    }

    public void setTotalLevels(int totalLevels) {
        this.totalLevels = totalLevels;
    }

    public int getStarsEarned() {
        return starsEarned;
    }

    public void setStarsEarned(int starsEarned) {
        this.starsEarned = starsEarned;
    }

    public int getMaxPossibleStars() {
        return maxPossibleStars;
    }

    public void setMaxPossibleStars(int maxPossibleStars) {
        this.maxPossibleStars = maxPossibleStars;
    }

    public int getEvolutionStage() {
        return evolutionStage;
    }

    public void setEvolutionStage(int evolutionStage) {
        this.evolutionStage = evolutionStage;
    }

    public float getSaturationLevel() {
        return saturationLevel;
    }

    public void setSaturationLevel(float saturationLevel) {
        this.saturationLevel = saturationLevel;
    }

    public boolean isBossUnlocked() {
        return bossUnlocked;
    }

    public void setBossUnlocked(boolean bossUnlocked) {
        this.bossUnlocked = bossUnlocked;
    }

    public boolean isBossDefeated() {
        return bossDefeated;
    }

    public void setBossDefeated(boolean bossDefeated) {
        this.bossDefeated = bossDefeated;
    }

    public long getBossDefeatedAt() {
        return bossDefeatedAt;
    }

    public void setBossDefeatedAt(long bossDefeatedAt) {
        this.bossDefeatedAt = bossDefeatedAt;
    }

    public int getBossBestScore() {
        return bossBestScore;
    }

    public void setBossBestScore(int bossBestScore) {
        this.bossBestScore = bossBestScore;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get total stars earned (alias for getStarsEarned for compatibility)
     */
    public int getTotalStars() {
        return starsEarned;
    }
    
    /**
     * Check if world is completed (all levels done)
     */
    public boolean isCompleted() {
        return levelsCompleted >= totalLevels;
    }
    
    /**
     * Update evolution based on completion percentage
     */
    public void updateEvolution() {
        if (!isUnlocked) {
            evolutionStage = -1;
            saturationLevel = 0f;
            return;
        }
        
        float completionPercentage = (float) levelsCompleted / totalLevels;
        
        if (completionPercentage >= 1.0f) {
            evolutionStage = 3;
            saturationLevel = 1.0f;
        } else if (completionPercentage >= 0.5f) {
            evolutionStage = 2;
            saturationLevel = 0.6f;
        } else if (completionPercentage >= 0.25f) {
            evolutionStage = 1;
            saturationLevel = 0.3f;
        } else {
            evolutionStage = 0;
            saturationLevel = 0.1f;
        }
    }
    
    /**
     * Check if all levels are completed
     */
    public boolean isFullyCompleted() {
        return levelsCompleted >= totalLevels;
    }
    
    /**
     * Get completion percentage
     */
    public float getCompletionPercentage() {
        return (float) levelsCompleted / totalLevels * 100f;
    }
    
    /**
     * Unlock this world
     */
    public void unlock() {
        if (!isUnlocked) {
            isUnlocked = true;
            unlockedAt = System.currentTimeMillis();
            evolutionStage = 0;
            saturationLevel = 0.1f;
        }
    }
    
    /**
     * Add level completion
     */
    public void addLevelCompletion(int starsForLevel) {
        levelsCompleted++;
        starsEarned += starsForLevel;
        updateEvolution();
        
        // Check if boss should be unlocked (after level 9)
        if (levelsCompleted >= 9 && !bossUnlocked) {
            bossUnlocked = true;
        }
    }
}
