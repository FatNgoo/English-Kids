package com.edu.english.magicmelody.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 👹 BossProgress Entity
 * 
 * Purpose: Track boss battle progress and achievements
 * 
 * Features:
 * - Track boss defeat status
 * - Store best performance stats
 * - AR battle recording info
 */
@Entity(tableName = "boss_progress")
public class BossProgress {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "boss_id")
    private String bossId;
    
    @ColumnInfo(name = "user_id")
    private String userId;
    
    @ColumnInfo(name = "world_id")
    private String worldId;
    
    @ColumnInfo(name = "boss_name")
    private String bossName;
    
    // ═══════════════════════════════════════════════════════════════
    // 🎯 BATTLE STATUS
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "is_unlocked")
    private boolean isUnlocked;
    
    @ColumnInfo(name = "is_defeated")
    private boolean isDefeated;
    
    @ColumnInfo(name = "times_attempted")
    private int timesAttempted;
    
    @ColumnInfo(name = "times_defeated")
    private int timesDefeated;
    
    @ColumnInfo(name = "first_defeated_at")
    private long firstDefeatedAt;
    
    @ColumnInfo(name = "last_attempted_at")
    private long lastAttemptedAt;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 BEST PERFORMANCE
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "best_score")
    private int bestScore;
    
    @ColumnInfo(name = "fastest_defeat_seconds")
    private int fastestDefeatSeconds;
    
    @ColumnInfo(name = "best_combo")
    private int bestCombo;
    
    @ColumnInfo(name = "highest_damage_dealt")
    private int highestDamageDealt;
    
    @ColumnInfo(name = "perfect_victory")
    private boolean perfectVictory; // Defeated without taking damage
    
    // ═══════════════════════════════════════════════════════════════
    // 🎤 AR BATTLE INFO
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "ar_battles_count")
    private int arBattlesCount;
    
    @ColumnInfo(name = "voice_attacks_used")
    private int voiceAttacksUsed;
    
    @ColumnInfo(name = "video_recorded")
    private boolean videoRecorded;
    
    @ColumnInfo(name = "last_video_path")
    private String lastVideoPath;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏆 REWARDS
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "reward_claimed")
    private boolean rewardClaimed;
    
    @ColumnInfo(name = "legendary_note_earned")
    private boolean legendaryNoteEarned;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public BossProgress() {
        // Default constructor for Room
    }
    
    /**
     * Create a new boss progress entry
     */
    public static BossProgress create(String bossId, String userId, String worldId, String bossName) {
        BossProgress progress = new BossProgress();
        progress.setBossId(bossId);
        progress.setUserId(userId);
        progress.setWorldId(worldId);
        progress.setBossName(bossName);
        progress.setUnlocked(false);
        progress.setDefeated(false);
        return progress;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    @NonNull
    public String getBossId() {
        return bossId;
    }

    public void setBossId(@NonNull String bossId) {
        this.bossId = bossId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWorldId() {
        return worldId;
    }

    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }

    public String getBossName() {
        return bossName;
    }

    public void setBossName(String bossName) {
        this.bossName = bossName;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public boolean isDefeated() {
        return isDefeated;
    }

    public void setDefeated(boolean defeated) {
        isDefeated = defeated;
    }

    public int getTimesAttempted() {
        return timesAttempted;
    }

    public void setTimesAttempted(int timesAttempted) {
        this.timesAttempted = timesAttempted;
    }

    public int getTimesDefeated() {
        return timesDefeated;
    }

    public void setTimesDefeated(int timesDefeated) {
        this.timesDefeated = timesDefeated;
    }

    public long getFirstDefeatedAt() {
        return firstDefeatedAt;
    }

    public void setFirstDefeatedAt(long firstDefeatedAt) {
        this.firstDefeatedAt = firstDefeatedAt;
    }

    public long getLastAttemptedAt() {
        return lastAttemptedAt;
    }

    public void setLastAttemptedAt(long lastAttemptedAt) {
        this.lastAttemptedAt = lastAttemptedAt;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public int getFastestDefeatSeconds() {
        return fastestDefeatSeconds;
    }

    public void setFastestDefeatSeconds(int fastestDefeatSeconds) {
        this.fastestDefeatSeconds = fastestDefeatSeconds;
    }

    public int getBestCombo() {
        return bestCombo;
    }

    public void setBestCombo(int bestCombo) {
        this.bestCombo = bestCombo;
    }

    public int getHighestDamageDealt() {
        return highestDamageDealt;
    }

    public void setHighestDamageDealt(int highestDamageDealt) {
        this.highestDamageDealt = highestDamageDealt;
    }

    public boolean isPerfectVictory() {
        return perfectVictory;
    }

    public void setPerfectVictory(boolean perfectVictory) {
        this.perfectVictory = perfectVictory;
    }

    public int getArBattlesCount() {
        return arBattlesCount;
    }

    public void setArBattlesCount(int arBattlesCount) {
        this.arBattlesCount = arBattlesCount;
    }

    public int getVoiceAttacksUsed() {
        return voiceAttacksUsed;
    }

    public void setVoiceAttacksUsed(int voiceAttacksUsed) {
        this.voiceAttacksUsed = voiceAttacksUsed;
    }

    public boolean isVideoRecorded() {
        return videoRecorded;
    }

    public void setVideoRecorded(boolean videoRecorded) {
        this.videoRecorded = videoRecorded;
    }

    public String getLastVideoPath() {
        return lastVideoPath;
    }

    public void setLastVideoPath(String lastVideoPath) {
        this.lastVideoPath = lastVideoPath;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public void setRewardClaimed(boolean rewardClaimed) {
        this.rewardClaimed = rewardClaimed;
    }

    public boolean isLegendaryNoteEarned() {
        return legendaryNoteEarned;
    }

    public void setLegendaryNoteEarned(boolean legendaryNoteEarned) {
        this.legendaryNoteEarned = legendaryNoteEarned;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Record a battle attempt
     */
    public void recordAttempt(boolean victory, int score, int combo, int duration, boolean usedAR) {
        timesAttempted++;
        lastAttemptedAt = System.currentTimeMillis();
        
        if (usedAR) {
            arBattlesCount++;
        }
        
        if (score > bestScore) {
            bestScore = score;
        }
        
        if (combo > bestCombo) {
            bestCombo = combo;
        }
        
        if (victory) {
            timesDefeated++;
            
            if (!isDefeated) {
                isDefeated = true;
                firstDefeatedAt = System.currentTimeMillis();
            }
            
            if (fastestDefeatSeconds == 0 || duration < fastestDefeatSeconds) {
                fastestDefeatSeconds = duration;
            }
        }
    }
    
    /**
     * Unlock this boss
     */
    public void unlock() {
        if (!isUnlocked) {
            isUnlocked = true;
        }
    }
    
    /**
     * Claim rewards for defeating this boss
     */
    public void claimReward() {
        if (isDefeated && !rewardClaimed) {
            rewardClaimed = true;
            legendaryNoteEarned = true;
        }
    }
    
    /**
     * Get win rate percentage
     */
    public float getWinRate() {
        if (timesAttempted == 0) return 0;
        return (float) timesDefeated / timesAttempted * 100f;
    }
}
