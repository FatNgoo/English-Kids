package com.edu.english.magicmelody.gameplay.engine;

import com.edu.english.magicmelody.model.HitResult;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 📈 Difficulty Manager
 * 
 * Handles adaptive difficulty based on player performance:
 * - Real-time difficulty adjustment
 * - Performance tracking
 * - Age-appropriate settings
 */
public class DifficultyManager {
    
    private static final String TAG = "DifficultyManager";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 DIFFICULTY LEVELS
    // ═══════════════════════════════════════════════════════════════
    
    public enum DifficultyLevel {
        VERY_EASY(1, "Beginner"),
        EASY(2, "Easy"),
        MEDIUM(3, "Medium"),
        HARD(4, "Hard"),
        VERY_HARD(5, "Expert");
        
        private final int level;
        private final String displayName;
        
        DifficultyLevel(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }
        
        public int getLevel() { return level; }
        public String getDisplayName() { return displayName; }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ DIFFICULTY CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public static class DifficultyConfig {
        // Note speed (1.0 = normal)
        public float noteSpeed = 1.0f;
        
        // Timing window multiplier (1.0 = normal, higher = more forgiving)
        public float timingMultiplier = 1.0f;
        
        // Number of lanes
        public int laneCount = 3;
        
        // Maximum notes per beat
        public int maxNotesPerBeat = 1;
        
        // Whether to show hints
        public boolean showHints = true;
        
        // Time ahead for note preview (ms)
        public float previewTimeMs = 2000f;
        
        // Auto-assist level (0 = none, 1 = some, 2 = full)
        public int assistLevel = 0;
        
        public static DifficultyConfig forLevel(DifficultyLevel level) {
            DifficultyConfig config = new DifficultyConfig();
            
            switch (level) {
                case VERY_EASY:
                    config.noteSpeed = 0.6f;
                    config.timingMultiplier = 1.5f;
                    config.laneCount = 2;
                    config.maxNotesPerBeat = 1;
                    config.showHints = true;
                    config.previewTimeMs = 3000f;
                    config.assistLevel = 2;
                    break;
                    
                case EASY:
                    config.noteSpeed = 0.8f;
                    config.timingMultiplier = 1.3f;
                    config.laneCount = 3;
                    config.maxNotesPerBeat = 1;
                    config.showHints = true;
                    config.previewTimeMs = 2500f;
                    config.assistLevel = 1;
                    break;
                    
                case MEDIUM:
                    config.noteSpeed = 1.0f;
                    config.timingMultiplier = 1.0f;
                    config.laneCount = 4;
                    config.maxNotesPerBeat = 2;
                    config.showHints = false;
                    config.previewTimeMs = 2000f;
                    config.assistLevel = 0;
                    break;
                    
                case HARD:
                    config.noteSpeed = 1.2f;
                    config.timingMultiplier = 0.8f;
                    config.laneCount = 5;
                    config.maxNotesPerBeat = 2;
                    config.showHints = false;
                    config.previewTimeMs = 1500f;
                    config.assistLevel = 0;
                    break;
                    
                case VERY_HARD:
                    config.noteSpeed = 1.5f;
                    config.timingMultiplier = 0.6f;
                    config.laneCount = 5;
                    config.maxNotesPerBeat = 3;
                    config.showHints = false;
                    config.previewTimeMs = 1200f;
                    config.assistLevel = 0;
                    break;
            }
            
            return config;
        }
        
        public static DifficultyConfig forAge(int age) {
            if (age <= 5) {
                return forLevel(DifficultyLevel.VERY_EASY);
            } else if (age <= 7) {
                return forLevel(DifficultyLevel.EASY);
            } else if (age <= 9) {
                return forLevel(DifficultyLevel.MEDIUM);
            } else {
                return forLevel(DifficultyLevel.HARD);
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 STATE VARIABLES
    // ═══════════════════════════════════════════════════════════════
    
    private DifficultyLevel currentLevel = DifficultyLevel.EASY;
    private DifficultyConfig currentConfig;
    
    // Performance tracking
    private static final int PERFORMANCE_WINDOW = 20; // Track last N notes
    private final Queue<Float> recentAccuracies = new LinkedList<>();
    private int consecutivePerfects = 0;
    private int consecutiveMisses = 0;
    
    // Adaptive settings
    private boolean adaptiveEnabled = true;
    private float adaptiveThresholdUp = 0.85f;   // Accuracy to increase difficulty
    private float adaptiveThresholdDown = 0.50f; // Accuracy to decrease difficulty
    
    // Constraints
    private DifficultyLevel minLevel = DifficultyLevel.VERY_EASY;
    private DifficultyLevel maxLevel = DifficultyLevel.VERY_HARD;
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface DifficultyChangeListener {
        void onDifficultyChanged(DifficultyLevel oldLevel, DifficultyLevel newLevel);
        void onConfigUpdated(DifficultyConfig newConfig);
    }
    
    private DifficultyChangeListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public DifficultyManager() {
        this.currentConfig = DifficultyConfig.forLevel(currentLevel);
    }
    
    public DifficultyManager(DifficultyLevel initialLevel) {
        this.currentLevel = initialLevel;
        this.currentConfig = DifficultyConfig.forLevel(currentLevel);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(DifficultyChangeListener listener) {
        this.listener = listener;
    }
    
    /**
     * Enable/disable adaptive difficulty
     */
    public void setAdaptiveEnabled(boolean enabled) {
        this.adaptiveEnabled = enabled;
    }
    
    /**
     * Set difficulty constraints
     */
    public void setLevelConstraints(DifficultyLevel min, DifficultyLevel max) {
        this.minLevel = min;
        this.maxLevel = max;
        
        // Clamp current level
        if (currentLevel.getLevel() < min.getLevel()) {
            setLevel(min);
        } else if (currentLevel.getLevel() > max.getLevel()) {
            setLevel(max);
        }
    }
    
    /**
     * Set level directly
     */
    public void setLevel(DifficultyLevel level) {
        if (level == currentLevel) return;
        
        DifficultyLevel oldLevel = currentLevel;
        currentLevel = level;
        currentConfig = DifficultyConfig.forLevel(level);
        
        if (listener != null) {
            listener.onDifficultyChanged(oldLevel, currentLevel);
            listener.onConfigUpdated(currentConfig);
        }
    }
    
    /**
     * Configure for specific age group
     */
    public void configureForAge(int age) {
        currentConfig = DifficultyConfig.forAge(age);
        
        // Determine level from age
        if (age <= 5) {
            currentLevel = DifficultyLevel.VERY_EASY;
        } else if (age <= 7) {
            currentLevel = DifficultyLevel.EASY;
        } else if (age <= 9) {
            currentLevel = DifficultyLevel.MEDIUM;
        } else {
            currentLevel = DifficultyLevel.HARD;
        }
        
        if (listener != null) {
            listener.onConfigUpdated(currentConfig);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 PERFORMANCE TRACKING
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Record a hit result for performance tracking
     */
    public void recordHit(HitResult result) {
        float accuracy = getAccuracyFromHitType(result.getHitType());
        
        // Update recent accuracies queue
        recentAccuracies.offer(accuracy);
        while (recentAccuracies.size() > PERFORMANCE_WINDOW) {
            recentAccuracies.poll();
        }
        
        // Update streaks
        if (result.getHitType() == HitResult.HitType.PERFECT) {
            consecutivePerfects++;
            consecutiveMisses = 0;
        } else if (result.getHitType() == HitResult.HitType.MISS) {
            consecutiveMisses++;
            consecutivePerfects = 0;
        } else {
            consecutivePerfects = 0;
            consecutiveMisses = 0;
        }
        
        // Check for adaptive adjustment
        if (adaptiveEnabled) {
            checkAdaptiveAdjustment();
        }
    }
    
    private float getAccuracyFromHitType(HitResult.HitType type) {
        switch (type) {
            case PERFECT: return 1.0f;
            case GOOD: return 0.75f;
            case OK: return 0.5f;
            case MISS: return 0f;
            default: return 0f;
        }
    }
    
    /**
     * Get average recent accuracy
     */
    public float getRecentAccuracy() {
        if (recentAccuracies.isEmpty()) return 0.5f;
        
        float sum = 0f;
        for (float acc : recentAccuracies) {
            sum += acc;
        }
        return sum / recentAccuracies.size();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📈 ADAPTIVE ADJUSTMENT
    // ═══════════════════════════════════════════════════════════════
    
    private void checkAdaptiveAdjustment() {
        // Need enough data
        if (recentAccuracies.size() < PERFORMANCE_WINDOW / 2) {
            return;
        }
        
        float recentAcc = getRecentAccuracy();
        
        // Check for difficulty increase
        if (recentAcc >= adaptiveThresholdUp && 
            consecutivePerfects >= 5 &&
            currentLevel.getLevel() < maxLevel.getLevel()) {
            
            increaseDifficulty();
        }
        // Check for difficulty decrease
        else if (recentAcc <= adaptiveThresholdDown && 
                 consecutiveMisses >= 3 &&
                 currentLevel.getLevel() > minLevel.getLevel()) {
            
            decreaseDifficulty();
        }
    }
    
    /**
     * Increase difficulty by one level
     */
    public void increaseDifficulty() {
        DifficultyLevel[] levels = DifficultyLevel.values();
        int currentIndex = currentLevel.ordinal();
        
        if (currentIndex < levels.length - 1 && 
            levels[currentIndex + 1].getLevel() <= maxLevel.getLevel()) {
            setLevel(levels[currentIndex + 1]);
            resetStreaks();
        }
    }
    
    /**
     * Decrease difficulty by one level
     */
    public void decreaseDifficulty() {
        DifficultyLevel[] levels = DifficultyLevel.values();
        int currentIndex = currentLevel.ordinal();
        
        if (currentIndex > 0 && 
            levels[currentIndex - 1].getLevel() >= minLevel.getLevel()) {
            setLevel(levels[currentIndex - 1]);
            resetStreaks();
        }
    }
    
    private void resetStreaks() {
        consecutivePerfects = 0;
        consecutiveMisses = 0;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public DifficultyLevel getCurrentLevel() {
        return currentLevel;
    }
    
    public DifficultyConfig getCurrentConfig() {
        return currentConfig;
    }
    
    public float getNoteSpeed() {
        return currentConfig.noteSpeed;
    }
    
    public float getTimingMultiplier() {
        return currentConfig.timingMultiplier;
    }
    
    public int getLaneCount() {
        return currentConfig.laneCount;
    }
    
    public boolean shouldShowHints() {
        return currentConfig.showHints;
    }
    
    public float getPreviewTimeMs() {
        return currentConfig.previewTimeMs;
    }
    
    public int getAssistLevel() {
        return currentConfig.assistLevel;
    }
    
    public boolean isAdaptiveEnabled() {
        return adaptiveEnabled;
    }
    
    public int getConsecutivePerfects() {
        return consecutivePerfects;
    }
    
    public int getConsecutiveMisses() {
        return consecutiveMisses;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 RESET
    // ═══════════════════════════════════════════════════════════════
    
    public void reset() {
        recentAccuracies.clear();
        consecutivePerfects = 0;
        consecutiveMisses = 0;
    }
    
    /**
     * Full reset including difficulty level
     */
    public void fullReset(DifficultyLevel level) {
        reset();
        setLevel(level);
    }
}
