package com.edu.english.magicmelody.gameplay.achievement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 🏆 Achievement Manager
 * 
 * Manages player achievements and unlocks:
 * - Track achievement progress
 * - Unlock rewards
 * - Display badges
 */
public class AchievementManager {
    
    private static final String TAG = "AchievementManager";
    
    // Singleton instance
    private static AchievementManager instance;
    
    public static synchronized AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 ACHIEVEMENT TYPES
    // ═══════════════════════════════════════════════════════════════
    
    public enum AchievementCategory {
        COLLECTION,     // Word collection achievements
        MASTERY,        // Skill mastery achievements
        EXPLORATION,    // World exploration achievements
        COMBAT,         // Boss battle achievements
        STREAK,         // Daily/weekly streaks
        SPECIAL         // Special/secret achievements
    }
    
    public enum AchievementTier {
        BRONZE(1, "#CD7F32"),
        SILVER(2, "#C0C0C0"),
        GOLD(3, "#FFD700"),
        PLATINUM(4, "#E5E4E2"),
        DIAMOND(5, "#B9F2FF");
        
        private final int level;
        private final String color;
        
        AchievementTier(int level, String color) {
            this.level = level;
            this.color = color;
        }
        
        public int getLevel() { return level; }
        public String getColor() { return color; }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 ACHIEVEMENT DEFINITION
    // ═══════════════════════════════════════════════════════════════
    
    public static class Achievement {
        public String id;
        public String name;
        public String description;
        public String iconName;
        public AchievementCategory category;
        public AchievementTier tier;
        public int targetValue;
        public String rewardType;    // "badge", "title", "theme", "note"
        public String rewardId;
        public boolean isSecret;
        
        public Achievement(String id, String name, String description,
                          AchievementCategory category, AchievementTier tier,
                          int targetValue) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.tier = tier;
            this.targetValue = targetValue;
            this.iconName = "ic_achievement_" + id;
        }
        
        public Achievement withReward(String type, String rewardId) {
            this.rewardType = type;
            this.rewardId = rewardId;
            return this;
        }
        
        public Achievement asSecret() {
            this.isSecret = true;
            return this;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 ACHIEVEMENT PROGRESS
    // ═══════════════════════════════════════════════════════════════
    
    public static class AchievementProgress {
        public String achievementId;
        public int currentValue;
        public boolean isUnlocked;
        public long unlockedAt;
        public boolean rewardClaimed;
        
        public AchievementProgress(String id) {
            this.achievementId = id;
            this.currentValue = 0;
            this.isUnlocked = false;
        }
        
        public float getProgressPercent(int targetValue) {
            if (targetValue == 0) return 0;
            return Math.min(1f, (float) currentValue / targetValue);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 STATE
    // ═══════════════════════════════════════════════════════════════
    
    private final Map<String, Achievement> achievements = new HashMap<>();
    private final Map<String, AchievementProgress> progress = new HashMap<>();
    private final Set<String> recentlyUnlocked = new HashSet<>();
    
    // Stats for achievement tracking
    private int totalWordsCollected = 0;
    private int totalPerfectHits = 0;
    private int totalBossDefeated = 0;
    private int currentStreak = 0;
    private int maxCombo = 0;
    private int worldsCompleted = 0;
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface AchievementListener {
        void onAchievementUnlocked(Achievement achievement);
        void onProgressUpdated(String achievementId, int current, int target);
        void onRewardClaimed(Achievement achievement);
    }
    
    private AchievementListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public AchievementManager() {
        initializeAchievements();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(AchievementListener listener) {
        this.listener = listener;
    }
    
    /**
     * Initialize all achievements
     */
    private void initializeAchievements() {
        // ═══════════════════════════════════════════════════════════
        // 📚 COLLECTION ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════
        
        registerAchievement(new Achievement(
            "first_word", "First Steps", "Collect your first word",
            AchievementCategory.COLLECTION, AchievementTier.BRONZE, 1
        ).withReward("badge", "badge_beginner"));
        
        registerAchievement(new Achievement(
            "words_10", "Word Explorer", "Collect 10 words",
            AchievementCategory.COLLECTION, AchievementTier.BRONZE, 10
        ));
        
        registerAchievement(new Achievement(
            "words_50", "Word Collector", "Collect 50 words",
            AchievementCategory.COLLECTION, AchievementTier.SILVER, 50
        ).withReward("title", "title_collector"));
        
        registerAchievement(new Achievement(
            "words_100", "Word Master", "Collect 100 words",
            AchievementCategory.COLLECTION, AchievementTier.GOLD, 100
        ).withReward("theme", "theme_golden"));
        
        registerAchievement(new Achievement(
            "words_500", "Word Legend", "Collect 500 words",
            AchievementCategory.COLLECTION, AchievementTier.PLATINUM, 500
        ).withReward("badge", "badge_legend"));
        
        registerAchievement(new Achievement(
            "first_legendary", "Legendary Find", "Collect a legendary word",
            AchievementCategory.COLLECTION, AchievementTier.GOLD, 1
        ).withReward("note", "note_special_legendary"));
        
        // ═══════════════════════════════════════════════════════════
        // ⭐ MASTERY ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════
        
        registerAchievement(new Achievement(
            "perfect_10", "Perfectionist", "Get 10 perfect hits",
            AchievementCategory.MASTERY, AchievementTier.BRONZE, 10
        ));
        
        registerAchievement(new Achievement(
            "perfect_100", "Precision Master", "Get 100 perfect hits",
            AchievementCategory.MASTERY, AchievementTier.SILVER, 100
        ));
        
        registerAchievement(new Achievement(
            "perfect_500", "Perfect Virtuoso", "Get 500 perfect hits",
            AchievementCategory.MASTERY, AchievementTier.GOLD, 500
        ).withReward("badge", "badge_virtuoso"));
        
        registerAchievement(new Achievement(
            "combo_25", "Combo Starter", "Reach a 25 combo",
            AchievementCategory.MASTERY, AchievementTier.BRONZE, 25
        ));
        
        registerAchievement(new Achievement(
            "combo_50", "Combo Master", "Reach a 50 combo",
            AchievementCategory.MASTERY, AchievementTier.SILVER, 50
        ));
        
        registerAchievement(new Achievement(
            "combo_100", "Combo Legend", "Reach a 100 combo",
            AchievementCategory.MASTERY, AchievementTier.GOLD, 100
        ).withReward("theme", "theme_fire"));
        
        registerAchievement(new Achievement(
            "full_combo", "Full Combo!", "Complete a lesson with no misses",
            AchievementCategory.MASTERY, AchievementTier.SILVER, 1
        ));
        
        registerAchievement(new Achievement(
            "three_stars_10", "Star Student", "Get 3 stars on 10 lessons",
            AchievementCategory.MASTERY, AchievementTier.SILVER, 10
        ));
        
        // ═══════════════════════════════════════════════════════════
        // 🌍 EXPLORATION ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════
        
        registerAchievement(new Achievement(
            "first_world", "World Traveler", "Complete your first world",
            AchievementCategory.EXPLORATION, AchievementTier.SILVER, 1
        ).withReward("badge", "badge_explorer"));
        
        registerAchievement(new Achievement(
            "all_worlds", "World Conqueror", "Complete all worlds",
            AchievementCategory.EXPLORATION, AchievementTier.DIAMOND, 5
        ).withReward("title", "title_champion"));
        
        registerAchievement(new Achievement(
            "evolution_full", "Rainbow Restorer", "Fully evolve a world's colors",
            AchievementCategory.EXPLORATION, AchievementTier.GOLD, 1
        ).withReward("theme", "theme_rainbow"));
        
        // ═══════════════════════════════════════════════════════════
        // ⚔️ COMBAT ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════
        
        registerAchievement(new Achievement(
            "first_boss", "Dragon Slayer", "Defeat your first boss",
            AchievementCategory.COMBAT, AchievementTier.SILVER, 1
        ).withReward("badge", "badge_warrior"));
        
        registerAchievement(new Achievement(
            "boss_5", "Boss Hunter", "Defeat 5 bosses",
            AchievementCategory.COMBAT, AchievementTier.GOLD, 5
        ));
        
        registerAchievement(new Achievement(
            "boss_flawless", "Untouchable", "Defeat a boss without taking damage",
            AchievementCategory.COMBAT, AchievementTier.PLATINUM, 1
        ).withReward("badge", "badge_invincible"));
        
        registerAchievement(new Achievement(
            "voice_attack_10", "Voice Warrior", "Use 10 voice attacks",
            AchievementCategory.COMBAT, AchievementTier.BRONZE, 10
        ));
        
        // ═══════════════════════════════════════════════════════════
        // 📅 STREAK ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════
        
        registerAchievement(new Achievement(
            "streak_3", "Getting Started", "Play 3 days in a row",
            AchievementCategory.STREAK, AchievementTier.BRONZE, 3
        ));
        
        registerAchievement(new Achievement(
            "streak_7", "Weekly Warrior", "Play 7 days in a row",
            AchievementCategory.STREAK, AchievementTier.SILVER, 7
        ).withReward("badge", "badge_dedicated"));
        
        registerAchievement(new Achievement(
            "streak_30", "Monthly Master", "Play 30 days in a row",
            AchievementCategory.STREAK, AchievementTier.GOLD, 30
        ).withReward("title", "title_devoted"));
        
        // ═══════════════════════════════════════════════════════════
        // 🔮 SECRET ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════
        
        registerAchievement(new Achievement(
            "secret_night", "Night Owl", "Play between midnight and 5 AM",
            AchievementCategory.SPECIAL, AchievementTier.SILVER, 1
        ).asSecret());
        
        registerAchievement(new Achievement(
            "secret_speed", "Speed Demon", "Complete a lesson in under 60 seconds",
            AchievementCategory.SPECIAL, AchievementTier.GOLD, 1
        ).asSecret().withReward("badge", "badge_speed"));
    }
    
    private void registerAchievement(Achievement achievement) {
        achievements.put(achievement.id, achievement);
        progress.put(achievement.id, new AchievementProgress(achievement.id));
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 PROGRESS TRACKING
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Update progress for an achievement
     */
    public void updateProgress(String achievementId, int newValue) {
        AchievementProgress prog = progress.get(achievementId);
        Achievement achievement = achievements.get(achievementId);
        
        if (prog == null || achievement == null || prog.isUnlocked) return;
        
        prog.currentValue = newValue;
        
        if (listener != null) {
            listener.onProgressUpdated(achievementId, newValue, achievement.targetValue);
        }
        
        // Check if unlocked
        if (newValue >= achievement.targetValue) {
            unlock(achievementId);
        }
    }
    
    /**
     * Increment progress
     */
    public void incrementProgress(String achievementId, int amount) {
        AchievementProgress prog = progress.get(achievementId);
        if (prog != null) {
            updateProgress(achievementId, prog.currentValue + amount);
        }
    }
    
    /**
     * Unlock an achievement
     */
    private void unlock(String achievementId) {
        AchievementProgress prog = progress.get(achievementId);
        Achievement achievement = achievements.get(achievementId);
        
        if (prog == null || achievement == null || prog.isUnlocked) return;
        
        prog.isUnlocked = true;
        prog.unlockedAt = System.currentTimeMillis();
        recentlyUnlocked.add(achievementId);
        
        if (listener != null) {
            listener.onAchievementUnlocked(achievement);
        }
    }
    
    /**
     * Claim reward for an achievement
     */
    public boolean claimReward(String achievementId) {
        AchievementProgress prog = progress.get(achievementId);
        Achievement achievement = achievements.get(achievementId);
        
        if (prog == null || achievement == null) return false;
        if (!prog.isUnlocked || prog.rewardClaimed) return false;
        if (achievement.rewardType == null) return false;
        
        prog.rewardClaimed = true;
        
        if (listener != null) {
            listener.onRewardClaimed(achievement);
        }
        
        return true;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GAME EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Called when a word is collected
     */
    public void onWordCollected(boolean isLegendary) {
        totalWordsCollected++;
        
        incrementProgress("first_word", 1);
        updateProgress("words_10", totalWordsCollected);
        updateProgress("words_50", totalWordsCollected);
        updateProgress("words_100", totalWordsCollected);
        updateProgress("words_500", totalWordsCollected);
        
        if (isLegendary) {
            incrementProgress("first_legendary", 1);
        }
    }
    
    /**
     * Called on perfect hit
     */
    public void onPerfectHit() {
        totalPerfectHits++;
        
        updateProgress("perfect_10", totalPerfectHits);
        updateProgress("perfect_100", totalPerfectHits);
        updateProgress("perfect_500", totalPerfectHits);
    }
    
    /**
     * Called when combo changes
     */
    public void onComboReached(int combo) {
        if (combo > maxCombo) {
            maxCombo = combo;
            
            updateProgress("combo_25", maxCombo);
            updateProgress("combo_50", maxCombo);
            updateProgress("combo_100", maxCombo);
        }
    }
    
    /**
     * Called on lesson complete
     */
    public void onLessonComplete(int stars, boolean fullCombo) {
        if (fullCombo) {
            incrementProgress("full_combo", 1);
        }
        
        if (stars == 3) {
            AchievementProgress prog = progress.get("three_stars_10");
            if (prog != null) {
                incrementProgress("three_stars_10", 1);
            }
        }
    }
    
    /**
     * Called on boss defeat
     */
    public void onBossDefeated(boolean flawless) {
        totalBossDefeated++;
        
        incrementProgress("first_boss", 1);
        updateProgress("boss_5", totalBossDefeated);
        
        if (flawless) {
            incrementProgress("boss_flawless", 1);
        }
    }
    
    /**
     * Called on world complete
     */
    public void onWorldCompleted() {
        worldsCompleted++;
        
        incrementProgress("first_world", 1);
        updateProgress("all_worlds", worldsCompleted);
    }
    
    /**
     * Called on daily login
     */
    public void onDailyLogin(int currentStreak) {
        this.currentStreak = currentStreak;
        
        updateProgress("streak_3", currentStreak);
        updateProgress("streak_7", currentStreak);
        updateProgress("streak_30", currentStreak);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 QUERIES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get all achievements
     */
    public List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements.values());
    }
    
    /**
     * Get achievements by category
     */
    public List<Achievement> getAchievementsByCategory(AchievementCategory category) {
        List<Achievement> result = new ArrayList<>();
        for (Achievement a : achievements.values()) {
            if (a.category == category) {
                result.add(a);
            }
        }
        return result;
    }
    
    /**
     * Get unlocked achievements
     */
    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> result = new ArrayList<>();
        for (Map.Entry<String, AchievementProgress> entry : progress.entrySet()) {
            if (entry.getValue().isUnlocked) {
                Achievement a = achievements.get(entry.getKey());
                if (a != null) result.add(a);
            }
        }
        return result;
    }
    
    /**
     * Get progress for achievement
     */
    public AchievementProgress getProgress(String achievementId) {
        return progress.get(achievementId);
    }
    
    /**
     * Check if achievement is unlocked
     */
    public boolean isUnlocked(String achievementId) {
        AchievementProgress prog = progress.get(achievementId);
        return prog != null && prog.isUnlocked;
    }
    
    /**
     * Get recently unlocked achievements
     */
    public List<Achievement> getRecentlyUnlocked() {
        List<Achievement> result = new ArrayList<>();
        for (String id : recentlyUnlocked) {
            Achievement a = achievements.get(id);
            if (a != null) result.add(a);
        }
        return result;
    }
    
    /**
     * Clear recently unlocked (after showing to user)
     */
    public void clearRecentlyUnlocked() {
        recentlyUnlocked.clear();
    }
    
    /**
     * Get completion statistics
     */
    public AchievementStats getStats() {
        AchievementStats stats = new AchievementStats();
        stats.totalAchievements = achievements.size();
        
        for (AchievementProgress prog : progress.values()) {
            if (prog.isUnlocked) {
                stats.unlockedCount++;
                if (!prog.rewardClaimed) {
                    stats.unclaimedRewards++;
                }
            }
        }
        
        stats.completionPercent = (float) stats.unlockedCount / stats.totalAchievements;
        
        return stats;
    }
    
    public static class AchievementStats {
        public int totalAchievements;
        public int unlockedCount;
        public int unclaimedRewards;
        public float completionPercent;
    }
}
