package com.edu.english.magicmelody.gameplay.engine;

import com.edu.english.magicmelody.data.entity.CollectedNote;
import com.edu.english.magicmelody.model.HitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 📝 Word Collector
 * 
 * Handles word collection during gameplay:
 * - Track collected words
 * - Calculate rarity based on performance
 * - Prepare for Magic Notebook storage
 */
public class WordCollector {
    
    private static final String TAG = "WordCollector";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 COLLECTION STATE
    // ═══════════════════════════════════════════════════════════════
    
    // Words collected in this session
    private final List<CollectedWord> collectedWords = new ArrayList<>();
    
    // Track unique words (don't collect duplicates in same session)
    private final Set<String> collectedWordKeys = new HashSet<>();
    
    // Word statistics for bonus calculation
    private final Map<String, WordStats> wordStats = new HashMap<>();
    
    // Session info
    private String currentLessonId;
    private String currentWorldId;
    private long userId;
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 COLLECTED WORD CLASS
    // ═══════════════════════════════════════════════════════════════
    
    public static class CollectedWord {
        private final String word;
        private final String noteName;
        private final String noteType;  // basic, special, legendary
        private final String lessonId;
        private final String worldId;
        private final int hitQuality;   // 0=miss, 1=ok, 2=good, 3=perfect
        private final long collectedAt;
        
        public CollectedWord(String word, String noteName, String noteType,
                            String lessonId, String worldId, int hitQuality) {
            this.word = word;
            this.noteName = noteName;
            this.noteType = noteType;
            this.lessonId = lessonId;
            this.worldId = worldId;
            this.hitQuality = hitQuality;
            this.collectedAt = System.currentTimeMillis();
        }
        
        // Getters
        public String getWord() { return word; }
        public String getNoteName() { return noteName; }
        public String getNoteType() { return noteType; }
        public String getLessonId() { return lessonId; }
        public String getWorldId() { return worldId; }
        public int getHitQuality() { return hitQuality; }
        public long getCollectedAt() { return collectedAt; }
        
        /**
         * Check if this was a quality collection (not a miss)
         */
        public boolean isQualityCollection() {
            return hitQuality > 0;
        }
        
        /**
         * Check if this was a perfect collection
         */
        public boolean isPerfect() {
            return hitQuality == 3;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 WORD STATS
    // ═══════════════════════════════════════════════════════════════
    
    private static class WordStats {
        int perfectHits = 0;
        int goodHits = 0;
        int okHits = 0;
        int misses = 0;
        
        float getAverageQuality() {
            int total = perfectHits + goodHits + okHits + misses;
            if (total == 0) return 0;
            
            float weighted = (perfectHits * 3) + (goodHits * 2) + okHits;
            return weighted / (total * 3);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface WordCollectorListener {
        void onWordCollected(CollectedWord word);
        void onNewWordDiscovered(CollectedWord word);
        void onSpecialWordCollected(CollectedWord word);
        void onLegendaryWordCollected(CollectedWord word);
    }
    
    private WordCollectorListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public WordCollector() {
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(WordCollectorListener listener) {
        this.listener = listener;
    }
    
    /**
     * Initialize for a new lesson
     */
    public void initialize(String lessonId, String worldId, long userId) {
        this.currentLessonId = lessonId;
        this.currentWorldId = worldId;
        this.userId = userId;
        
        // Clear previous session data
        collectedWords.clear();
        collectedWordKeys.clear();
        wordStats.clear();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 COLLECTION LOGIC
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Process a hit note and potentially collect the word
     */
    public CollectedWord processHit(NoteSpawner.SpawnedNote note, HitResult result) {
        String word = note.getWord();
        String noteName = note.getNoteName();
        
        if (word == null || word.isEmpty()) {
            return null;
        }
        
        // Update word stats
        updateWordStats(word, result);
        
        // Determine hit quality
        int hitQuality = getHitQuality(result.getHitType());
        
        // Only collect on successful hits (not misses)
        if (hitQuality == 0) {
            return null;
        }
        
        // Check for duplicate in this session
        String wordKey = word.toLowerCase();
        if (collectedWordKeys.contains(wordKey)) {
            return null; // Already collected this word in this session
        }
        
        // Determine note type based on hit quality and patterns
        String noteType = determineNoteType(note, result);
        
        // Create collected word
        CollectedWord collected = new CollectedWord(
            word, noteName, noteType,
            currentLessonId, currentWorldId, hitQuality
        );
        
        // Add to collection
        collectedWords.add(collected);
        collectedWordKeys.add(wordKey);
        
        // Notify listener
        notifyCollection(collected);
        
        return collected;
    }
    
    /**
     * Get hit quality from hit type
     */
    private int getHitQuality(HitResult.HitType type) {
        switch (type) {
            case PERFECT: return 3;
            case GOOD: return 2;
            case OK: return 1;
            case MISS: return 0;
            default: return 0;
        }
    }
    
    /**
     * Determine note type based on various factors
     */
    private String determineNoteType(NoteSpawner.SpawnedNote note, HitResult result) {
        // Check if note has a predefined type
        if (note.getType() != null) {
            switch (note.getType()) {
                case GOLDEN:
                    return "legendary";
                case SPECIAL:
                    return "special";
                default:
                    break;
            }
        }
        
        // Perfect hits on combo streaks get special notes
        WordStats stats = wordStats.get(note.getWord().toLowerCase());
        if (stats != null && stats.perfectHits >= 3) {
            return "special";
        }
        
        // Perfect hit gets slightly better chance for special
        if (result.getHitType() == HitResult.HitType.PERFECT) {
            if (Math.random() < 0.1) { // 10% chance
                return "special";
            }
        }
        
        return "basic";
    }
    
    /**
     * Update word statistics
     */
    private void updateWordStats(String word, HitResult result) {
        String key = word.toLowerCase();
        WordStats stats = wordStats.computeIfAbsent(key, k -> new WordStats());
        
        switch (result.getHitType()) {
            case PERFECT: stats.perfectHits++; break;
            case GOOD: stats.goodHits++; break;
            case OK: stats.okHits++; break;
            case MISS: stats.misses++; break;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════
    
    private void notifyCollection(CollectedWord word) {
        if (listener == null) return;
        
        listener.onWordCollected(word);
        
        // Check for special types
        switch (word.getNoteType()) {
            case "legendary":
                listener.onLegendaryWordCollected(word);
                break;
            case "special":
                listener.onSpecialWordCollected(word);
                break;
        }
        
        // First time collecting this word overall?
        // (This would need to check against repository in real implementation)
        listener.onNewWordDiscovered(word);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 QUERIES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get all collected words in this session
     */
    public List<CollectedWord> getCollectedWords() {
        return new ArrayList<>(collectedWords);
    }
    
    /**
     * Get count of collected words
     */
    public int getCollectedCount() {
        return collectedWords.size();
    }
    
    /**
     * Get count by type
     */
    public int getCountByType(String type) {
        int count = 0;
        for (CollectedWord word : collectedWords) {
            if (word.getNoteType().equals(type)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get basic word count
     */
    public int getBasicCount() {
        return getCountByType("basic");
    }
    
    /**
     * Get special word count
     */
    public int getSpecialCount() {
        return getCountByType("special");
    }
    
    /**
     * Get legendary word count
     */
    public int getLegendaryCount() {
        return getCountByType("legendary");
    }
    
    /**
     * Get perfect collection count
     */
    public int getPerfectCollectionCount() {
        int count = 0;
        for (CollectedWord word : collectedWords) {
            if (word.isPerfect()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Calculate collection bonus score
     */
    public int calculateCollectionBonus() {
        int bonus = 0;
        
        for (CollectedWord word : collectedWords) {
            switch (word.getNoteType()) {
                case "legendary":
                    bonus += 500;
                    break;
                case "special":
                    bonus += 100;
                    break;
                case "basic":
                    bonus += 25;
                    break;
            }
            
            // Extra bonus for perfect collections
            if (word.isPerfect()) {
                bonus += 25;
            }
        }
        
        return bonus;
    }
    
    /**
     * Get session summary
     */
    public CollectionSummary getSummary() {
        return new CollectionSummary(
            collectedWords.size(),
            getBasicCount(),
            getSpecialCount(),
            getLegendaryCount(),
            getPerfectCollectionCount(),
            calculateCollectionBonus()
        );
    }
    
    public static class CollectionSummary {
        public final int totalCollected;
        public final int basicCount;
        public final int specialCount;
        public final int legendaryCount;
        public final int perfectCount;
        public final int bonusScore;
        
        public CollectionSummary(int total, int basic, int special, 
                                int legendary, int perfect, int bonus) {
            this.totalCollected = total;
            this.basicCount = basic;
            this.specialCount = special;
            this.legendaryCount = legendary;
            this.perfectCount = perfect;
            this.bonusScore = bonus;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 RESET
    // ═══════════════════════════════════════════════════════════════
    
    public void reset() {
        collectedWords.clear();
        collectedWordKeys.clear();
        wordStats.clear();
    }
    
    /**
     * Clear session but keep configuration
     */
    public void clearSession() {
        collectedWords.clear();
        collectedWordKeys.clear();
        // Keep wordStats for next session
    }
}
