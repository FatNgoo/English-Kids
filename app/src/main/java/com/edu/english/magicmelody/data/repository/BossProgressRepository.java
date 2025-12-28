package com.edu.english.magicmelody.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.edu.english.magicmelody.data.MagicMelodyDatabase;
import com.edu.english.magicmelody.data.dao.BossProgressDao;
import com.edu.english.magicmelody.data.entity.BossProgress;

import java.util.List;

/**
 * 👹 BossProgress Repository
 * 
 * Purpose: Single source of truth for boss battle data
 * Manages boss fight history and achievements
 */
public class BossProgressRepository {
    
    private final BossProgressDao bossProgressDao;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public BossProgressRepository(Application application) {
        MagicMelodyDatabase database = MagicMelodyDatabase.getInstance(application);
        bossProgressDao = database.bossProgressDao();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 QUERY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get progress for a specific boss
     */
    public LiveData<BossProgress> getBossProgress(String bossId, long userId) {
        return bossProgressDao.getBossProgress(bossId, String.valueOf(userId));
    }
    
    /**
     * Get boss progress synchronously
     */
    public BossProgress getBossProgressSync(String bossId, long userId) {
        return bossProgressDao.getBossProgressSync(bossId, String.valueOf(userId));
    }
    
    /**
     * Get all boss progress for user
     */
    public LiveData<List<BossProgress>> getAllBossProgressForUser(long userId) {
        return bossProgressDao.getAllBossProgressForUser(String.valueOf(userId));
    }
    
    /**
     * Get boss for a specific world
     */
    public LiveData<BossProgress> getBossForWorld(String worldId, long userId) {
        return bossProgressDao.getBossForWorld(worldId, String.valueOf(userId));
    }
    
    /**
     * Get defeated bosses
     */
    public LiveData<List<BossProgress>> getDefeatedBosses(long userId) {
        return bossProgressDao.getDefeatedBosses(String.valueOf(userId));
    }
    
    /**
     * Get undefeated bosses
     */
    public LiveData<List<BossProgress>> getUndefeatedBosses(long userId) {
        return bossProgressDao.getUndefeatedBosses(String.valueOf(userId));
    }
    
    /**
     * Get perfect victories
     */
    public LiveData<List<BossProgress>> getPerfectVictories(long userId) {
        return bossProgressDao.getPerfectVictories(String.valueOf(userId));
    }
    
    /**
     * Count defeated bosses
     */
    public int getDefeatedBossCount(long userId) {
        return bossProgressDao.getDefeatedBossCount(String.valueOf(userId));
    }
    
    /**
     * Check if boss is defeated
     */
    public boolean isBossDefeated(String bossId, long userId) {
        return bossProgressDao.isBossDefeated(bossId, String.valueOf(userId));
    }
    
    /**
     * Get total AR battles
     */
    public int getTotalArBattles(long userId) {
        return bossProgressDao.getTotalArBattles(String.valueOf(userId));
    }
    
    /**
     * Check if reward is claimed
     */
    public boolean isRewardClaimed(String bossId, long userId) {
        return bossProgressDao.isRewardClaimed(bossId, String.valueOf(userId));
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ✏️ INSERT/UPDATE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Insert boss progress
     */
    public void insert(BossProgress progress) {
        MagicMelodyDatabase.runAsync(() -> {
            bossProgressDao.insert(progress);
        });
    }
    
    /**
     * Update boss progress
     */
    public void update(BossProgress progress) {
        MagicMelodyDatabase.runAsync(() -> {
            bossProgressDao.update(progress);
        });
    }
    
    /**
     * Initialize boss for a world
     */
    public void initializeBoss(String bossId, String bossName, String worldId, long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            BossProgress existing = bossProgressDao.getBossProgressSync(bossId, String.valueOf(userId));
            if (existing == null) {
                BossProgress newProgress = BossProgress.create(bossId, String.valueOf(userId), worldId, bossName);
                bossProgressDao.insert(newProgress);
            }
        });
    }
    
    /**
     * Record a boss battle attempt
     */
    public void recordBattleAttempt(String bossId, long userId, int score, 
                                    long timeMs, boolean usedAr, int voiceAttacks,
                                    boolean victory, boolean perfectVictory,
                                    OnBattleRecordedListener listener) {
        MagicMelodyDatabase.runAsync(() -> {
            String userIdStr = String.valueOf(userId);
            // Get or create boss progress
            BossProgress progress = bossProgressDao.getBossProgressSync(bossId, userIdStr);
            
            if (progress == null) {
                // Should not happen if initialized properly
                return;
            }
            
            // Increment attempt count
            bossProgressDao.incrementAttemptCount(bossId, userIdStr);
            
            // Update best score
            bossProgressDao.updateBestScoreIfHigher(bossId, userIdStr, score);
            
            // Update fastest time (convert ms to seconds)
            bossProgressDao.updateFastestTimeIfLower(bossId, userIdStr, (int)(timeMs / 1000));
            
            // Track AR usage
            if (usedAr) {
                bossProgressDao.incrementArBattles(bossId, userIdStr);
            }
            
            // Track voice attacks
            if (voiceAttacks > 0) {
                bossProgressDao.addVoiceAttacks(bossId, userIdStr, voiceAttacks);
            }
            
            // Handle victory
            if (victory) {
                bossProgressDao.markDefeated(bossId, userIdStr, System.currentTimeMillis());
                
                if (perfectVictory) {
                    bossProgressDao.setPerfectVictory(bossId, userIdStr);
                }
            }
            
            if (listener != null) {
                listener.onBattleRecorded(victory, perfectVictory);
            }
        });
    }
    
    /**
     * Claim boss reward
     */
    public void claimReward(String bossId, long userId, OnRewardClaimedListener listener) {
        MagicMelodyDatabase.runAsync(() -> {
            String userIdStr = String.valueOf(userId);
            // Check if already claimed
            boolean alreadyClaimed = bossProgressDao.isRewardClaimed(bossId, userIdStr);
            
            if (!alreadyClaimed) {
                bossProgressDao.claimReward(bossId, userIdStr);
                if (listener != null) {
                    listener.onRewardClaimed(true);
                }
            } else {
                if (listener != null) {
                    listener.onRewardClaimed(false);
                }
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
            bossProgressDao.deleteAllForUser(String.valueOf(userId));
        });
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔔 CALLBACK INTERFACES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Callback for battle recorded
     */
    public interface OnBattleRecordedListener {
        void onBattleRecorded(boolean victory, boolean perfectVictory);
    }
    
    /**
     * Callback for reward claimed
     */
    public interface OnRewardClaimedListener {
        void onRewardClaimed(boolean success);
    }
}
