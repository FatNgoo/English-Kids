package com.edu.english.magicmelody.gameplay.engine;

import com.edu.english.magicmelody.model.HitResult;

/**
 * 💯 Score Manager
 * 
 * Handles scoring for rhythm gameplay:
 * - Point calculation
 * - Combo management
 * - Star rating
 * - Bonus scoring
 */
public class ScoreManager {
    
    private static final String TAG = "ScoreManager";
    
    // ═══════════════════════════════════════════════════════════════
    // 💰 SCORE CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public static class ScoreConfig {
        // Base points per hit type
        public int perfectPoints = 100;
        public int goodPoints = 75;
        public int okPoints = 50;
        public int missPoints = 0;
        
        // Combo bonus thresholds
        public int comboBonus5 = 10;      // Bonus at 5 combo
        public int comboBonus10 = 25;     // Bonus at 10 combo
        public int comboBonus20 = 50;     // Bonus at 20+ combo
        
        // Multiplier caps
        public float maxMultiplier = 4.0f;
        
        // Star thresholds (percentage of max possible score)
        public float star1Threshold = 0.50f;  // 50%
        public float star2Threshold = 0.70f;  // 70%
        public float star3Threshold = 0.90f;  // 90%
        
        public static ScoreConfig defaultConfig() {
            return new ScoreConfig();
        }
        
        public static ScoreConfig forKids() {
            ScoreConfig config = new ScoreConfig();
            config.okPoints = 60;  // More forgiving
            config.star1Threshold = 0.40f;
            config.star2Threshold = 0.60f;
            config.star3Threshold = 0.80f;
            return config;
        }
    }
    
    private ScoreConfig config = ScoreConfig.forKids();
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SCORE STATE
    // ═══════════════════════════════════════════════════════════════
    
    private int currentScore = 0;
    private int currentCombo = 0;
    private int maxCombo = 0;
    private int totalNotes = 0;
    
    // Hit counts
    private int perfectCount = 0;
    private int goodCount = 0;
    private int okCount = 0;
    private int missCount = 0;
    
    // Calculated values
    private int maxPossibleScore = 0;
    private float currentMultiplier = 1.0f;
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface ScoreListener {
        void onScoreChanged(int newScore, int pointsAdded);
        void onComboChanged(int newCombo);
        void onComboBreak(int lostCombo);
        void onMultiplierChanged(float newMultiplier);
        void onMilestoneReached(MilestoneType type, int value);
    }
    
    public enum MilestoneType {
        COMBO_5,
        COMBO_10,
        COMBO_20,
        COMBO_50,
        PERFECT_STREAK_5,
        SCORE_1000,
        SCORE_5000,
        SCORE_10000
    }
    
    private ScoreListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public ScoreManager() {
    }
    
    public ScoreManager(ScoreConfig config) {
        this.config = config;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setConfig(ScoreConfig config) {
        this.config = config;
    }
    
    public void setListener(ScoreListener listener) {
        this.listener = listener;
    }
    
    /**
     * Initialize for a new game with total note count
     */
    public void initialize(int totalNotes) {
        reset();
        this.totalNotes = totalNotes;
        this.maxPossibleScore = calculateMaxPossibleScore(totalNotes);
    }
    
    /**
     * Calculate maximum possible score for perfect play
     */
    private int calculateMaxPossibleScore(int noteCount) {
        int score = 0;
        int combo = 0;
        
        for (int i = 0; i < noteCount; i++) {
            combo++;
            float multiplier = calculateMultiplier(combo);
            score += (int)(config.perfectPoints * multiplier);
        }
        
        return score;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎯 SCORING
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Process a hit result and update score
     */
    public int processHit(HitResult hitResult) {
        int basePoints = getBasePoints(hitResult.getHitType());
        
        if (hitResult.getHitType() == HitResult.HitType.MISS) {
            return processMiss();
        }
        
        // Increment combo
        int oldCombo = currentCombo;
        currentCombo++;
        
        if (currentCombo > maxCombo) {
            maxCombo = currentCombo;
        }
        
        // Update hit counts
        updateHitCounts(hitResult.getHitType());
        
        // Calculate multiplier
        float oldMultiplier = currentMultiplier;
        currentMultiplier = calculateMultiplier(currentCombo);
        
        // Calculate points with multiplier
        int points = (int)(basePoints * currentMultiplier);
        
        // Add combo bonus
        int comboBonus = getComboBonus(currentCombo);
        points += comboBonus;
        
        // Update score
        int oldScore = currentScore;
        currentScore += points;
        
        // Notify listeners
        if (listener != null) {
            listener.onScoreChanged(currentScore, points);
            
            if (currentCombo != oldCombo) {
                listener.onComboChanged(currentCombo);
            }
            
            if (currentMultiplier != oldMultiplier) {
                listener.onMultiplierChanged(currentMultiplier);
            }
        }
        
        // Check milestones
        checkMilestones(oldScore, oldCombo);
        
        return points;
    }
    
    /**
     * Process a miss
     */
    public int processMiss() {
        missCount++;
        
        int lostCombo = currentCombo;
        currentCombo = 0;
        currentMultiplier = 1.0f;
        
        if (listener != null) {
            listener.onScoreChanged(currentScore, 0);
            
            if (lostCombo > 0) {
                listener.onComboBreak(lostCombo);
                listener.onComboChanged(0);
            }
            
            listener.onMultiplierChanged(1.0f);
        }
        
        return 0;
    }
    
    /**
     * Get base points for hit type
     */
    private int getBasePoints(HitResult.HitType type) {
        switch (type) {
            case PERFECT: return config.perfectPoints;
            case GOOD: return config.goodPoints;
            case OK: return config.okPoints;
            case MISS: return config.missPoints;
            default: return 0;
        }
    }
    
    /**
     * Update hit counts
     */
    private void updateHitCounts(HitResult.HitType type) {
        switch (type) {
            case PERFECT: perfectCount++; break;
            case GOOD: goodCount++; break;
            case OK: okCount++; break;
            case MISS: missCount++; break;
        }
    }
    
    /**
     * Calculate combo multiplier
     */
    private float calculateMultiplier(int combo) {
        if (combo < 5) {
            return 1.0f;
        } else if (combo < 10) {
            return 1.2f;
        } else if (combo < 20) {
            return 1.5f;
        } else if (combo < 50) {
            return 2.0f;
        } else if (combo < 100) {
            return 3.0f;
        } else {
            return Math.min(config.maxMultiplier, 4.0f);
        }
    }
    
    /**
     * Get combo bonus points
     */
    private int getComboBonus(int combo) {
        if (combo == 5) return config.comboBonus5;
        if (combo == 10) return config.comboBonus10;
        if (combo == 20) return config.comboBonus20;
        if (combo == 50) return 100;
        if (combo == 100) return 200;
        if (combo % 50 == 0) return 100;
        return 0;
    }
    
    /**
     * Check for milestone achievements
     */
    private void checkMilestones(int oldScore, int oldCombo) {
        if (listener == null) return;
        
        // Combo milestones
        if (oldCombo < 5 && currentCombo >= 5) {
            listener.onMilestoneReached(MilestoneType.COMBO_5, 5);
        }
        if (oldCombo < 10 && currentCombo >= 10) {
            listener.onMilestoneReached(MilestoneType.COMBO_10, 10);
        }
        if (oldCombo < 20 && currentCombo >= 20) {
            listener.onMilestoneReached(MilestoneType.COMBO_20, 20);
        }
        if (oldCombo < 50 && currentCombo >= 50) {
            listener.onMilestoneReached(MilestoneType.COMBO_50, 50);
        }
        
        // Score milestones
        if (oldScore < 1000 && currentScore >= 1000) {
            listener.onMilestoneReached(MilestoneType.SCORE_1000, 1000);
        }
        if (oldScore < 5000 && currentScore >= 5000) {
            listener.onMilestoneReached(MilestoneType.SCORE_5000, 5000);
        }
        if (oldScore < 10000 && currentScore >= 10000) {
            listener.onMilestoneReached(MilestoneType.SCORE_10000, 10000);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⭐ STAR RATING
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get star rating (0-3) based on current score
     */
    public int getStarRating() {
        if (maxPossibleScore == 0) return 0;
        
        float percentage = (float) currentScore / maxPossibleScore;
        
        if (percentage >= config.star3Threshold) return 3;
        if (percentage >= config.star2Threshold) return 2;
        if (percentage >= config.star1Threshold) return 1;
        return 0;
    }
    
    /**
     * Get progress towards next star (0-1)
     */
    public float getProgressToNextStar() {
        if (maxPossibleScore == 0) return 0f;
        
        float percentage = (float) currentScore / maxPossibleScore;
        
        if (percentage >= config.star3Threshold) {
            return 1f; // Already at max
        } else if (percentage >= config.star2Threshold) {
            return (percentage - config.star2Threshold) / 
                   (config.star3Threshold - config.star2Threshold);
        } else if (percentage >= config.star1Threshold) {
            return (percentage - config.star1Threshold) / 
                   (config.star2Threshold - config.star1Threshold);
        } else {
            return percentage / config.star1Threshold;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATISTICS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get accuracy percentage
     */
    public float getAccuracyPercent() {
        int totalHits = perfectCount + goodCount + okCount + missCount;
        if (totalHits == 0) return 100f;
        
        // Weighted accuracy
        float weightedScore = (perfectCount * 100f) + (goodCount * 75f) + (okCount * 50f);
        return weightedScore / totalHits;
    }
    
    /**
     * Get completion percentage
     */
    public float getCompletionPercent() {
        if (totalNotes == 0) return 0f;
        int processed = perfectCount + goodCount + okCount + missCount;
        return (float) processed / totalNotes * 100f;
    }
    
    /**
     * Get score as percentage of max possible
     */
    public float getScorePercent() {
        if (maxPossibleScore == 0) return 0f;
        return (float) currentScore / maxPossibleScore * 100f;
    }
    
    /**
     * Generate result summary
     */
    public ScoreSummary getSummary() {
        return new ScoreSummary(
            currentScore,
            maxCombo,
            getStarRating(),
            perfectCount,
            goodCount,
            okCount,
            missCount,
            getAccuracyPercent()
        );
    }
    
    public static class ScoreSummary {
        public final int score;
        public final int maxCombo;
        public final int stars;
        public final int perfectCount;
        public final int goodCount;
        public final int okCount;
        public final int missCount;
        public final float accuracy;
        
        public ScoreSummary(int score, int maxCombo, int stars,
                          int perfect, int good, int ok, int miss, float accuracy) {
            this.score = score;
            this.maxCombo = maxCombo;
            this.stars = stars;
            this.perfectCount = perfect;
            this.goodCount = good;
            this.okCount = ok;
            this.missCount = miss;
            this.accuracy = accuracy;
        }
        
        public int getTotalHits() {
            return perfectCount + goodCount + okCount;
        }
        
        public int getTotalNotes() {
            return perfectCount + goodCount + okCount + missCount;
        }
        
        public boolean isPerfectPlay() {
            return missCount == 0 && okCount == 0 && goodCount == 0;
        }
        
        public boolean isFullCombo() {
            return missCount == 0;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public int getCurrentScore() { return currentScore; }
    public int getCurrentCombo() { return currentCombo; }
    public int getMaxCombo() { return maxCombo; }
    public float getCurrentMultiplier() { return currentMultiplier; }
    public int getPerfectCount() { return perfectCount; }
    public int getGoodCount() { return goodCount; }
    public int getOkCount() { return okCount; }
    public int getMissCount() { return missCount; }
    public int getMaxPossibleScore() { return maxPossibleScore; }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 RESET
    // ═══════════════════════════════════════════════════════════════
    
    public void reset() {
        currentScore = 0;
        currentCombo = 0;
        maxCombo = 0;
        currentMultiplier = 1.0f;
        perfectCount = 0;
        goodCount = 0;
        okCount = 0;
        missCount = 0;
        maxPossibleScore = 0;
        totalNotes = 0;
    }
}
