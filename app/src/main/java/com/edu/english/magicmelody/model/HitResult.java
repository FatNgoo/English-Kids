package com.edu.english.magicmelody.model;

/**
 * 🎵 HitResult Model
 * 
 * Purpose: Represents the result of hitting a note
 * Captures timing accuracy and scoring
 */
public class HitResult {
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 HIT TYPE ENUM
    // ═══════════════════════════════════════════════════════════════
    
    public enum HitType {
        PERFECT("PERFECT!", 100, "#FFD700", 1.0f),   // Gold
        GOOD("GOOD!", 80, "#00FF00", 0.8f),           // Green
        OK("OK", 50, "#87CEEB", 0.5f),                // Light blue
        MISS("MISS", 0, "#FF4444", 0.0f);             // Red
        
        private final String displayText;
        private final int baseScore;
        private final String color;
        private final float multiplier;
        
        HitType(String displayText, int baseScore, String color, float multiplier) {
            this.displayText = displayText;
            this.baseScore = baseScore;
            this.color = color;
            this.multiplier = multiplier;
        }
        
        public String getDisplayText() { return displayText; }
        public int getBaseScore() { return baseScore; }
        public String getColor() { return color; }
        public float getMultiplier() { return multiplier; }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 FIELDS
    // ═══════════════════════════════════════════════════════════════
    
    private HitType hitType;
    private NoteEvent noteEvent;
    private long timingDelta;    // Difference from perfect timing in ms
    private int laneIndex;
    private int scoreEarned;
    private boolean isEarly;     // Hit too early
    private boolean isLate;      // Hit too late
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public HitResult() {
    }
    
    public HitResult(HitType hitType, NoteEvent noteEvent, long timingDelta) {
        this.hitType = hitType;
        this.noteEvent = noteEvent;
        this.timingDelta = timingDelta;
        this.isEarly = timingDelta < 0;
        this.isLate = timingDelta > 0;
    }
    
    /**
     * Factory method to create hit result based on timing
     */
    public static HitResult fromTiming(NoteEvent note, long actualTime, long perfectTime) {
        long delta = actualTime - perfectTime;
        long absDelta = Math.abs(delta);
        
        HitType type;
        if (absDelta <= 50) {           // Within 50ms = Perfect
            type = HitType.PERFECT;
        } else if (absDelta <= 100) {   // Within 100ms = Good
            type = HitType.GOOD;
        } else if (absDelta <= 200) {   // Within 200ms = OK
            type = HitType.OK;
        } else {                         // Beyond 200ms = Miss
            type = HitType.MISS;
        }
        
        HitResult result = new HitResult(type, note, delta);
        result.laneIndex = note.getLane();
        return result;
    }
    
    /**
     * Factory method to create hit result from timing offset and type (for HitDetector)
     */
    public static HitResult fromTiming(float timingOffsetMs, HitType hitType) {
        HitResult result = new HitResult();
        result.hitType = hitType;
        result.timingDelta = (long) timingOffsetMs;
        result.isEarly = timingOffsetMs < 0;
        result.isLate = timingOffsetMs > 0;
        return result;
    }
    
    /**
     * Factory method to create a miss result
     */
    public static HitResult miss(NoteEvent note) {
        HitResult result = new HitResult(HitType.MISS, note, 0);
        result.laneIndex = note.getLane();
        return result;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    public HitType getHitType() { return hitType; }
    public void setHitType(HitType hitType) { this.hitType = hitType; }

    public NoteEvent getNoteEvent() { return noteEvent; }
    public void setNoteEvent(NoteEvent noteEvent) { this.noteEvent = noteEvent; }

    public long getTimingDelta() { return timingDelta; }
    public void setTimingDelta(long timingDelta) { this.timingDelta = timingDelta; }

    public int getLaneIndex() { return laneIndex; }
    public void setLaneIndex(int laneIndex) { this.laneIndex = laneIndex; }

    public int getScoreEarned() { return scoreEarned; }
    public void setScoreEarned(int scoreEarned) { this.scoreEarned = scoreEarned; }

    public boolean isEarly() { return isEarly; }
    public void setEarly(boolean early) { isEarly = early; }

    public boolean isLate() { return isLate; }
    public void setLate(boolean late) { isLate = late; }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Calculate score with combo multiplier
     */
    public int calculateScore(float comboMultiplier) {
        scoreEarned = (int) (hitType.getBaseScore() * comboMultiplier);
        return scoreEarned;
    }
    
    /**
     * Check if this is a successful hit
     */
    public boolean isHit() {
        return hitType != HitType.MISS;
    }
    
    /**
     * Check if this is a perfect hit
     */
    public boolean isPerfect() {
        return hitType == HitType.PERFECT;
    }
    
    /**
     * Check if this result should maintain combo
     */
    public boolean maintainsCombo() {
        return hitType != HitType.MISS;
    }
    
    /**
     * Get timing feedback text
     */
    public String getTimingFeedback() {
        if (hitType == HitType.MISS) {
            return "MISS";
        }
        
        if (Math.abs(timingDelta) <= 10) {
            return hitType.getDisplayText();
        }
        
        if (isEarly) {
            return hitType.getDisplayText() + " (Early)";
        } else {
            return hitType.getDisplayText() + " (Late)";
        }
    }
    
    /**
     * Check if there's an associated word to learn
     */
    public boolean hasWord() {
        return noteEvent != null && noteEvent.hasWord();
    }
    
    /**
     * Get the word if available
     */
    public String getWord() {
        return noteEvent != null ? noteEvent.getWord() : null;
    }
    
    @Override
    public String toString() {
        return "HitResult{" +
                "type=" + hitType +
                ", delta=" + timingDelta + "ms" +
                ", lane=" + laneIndex +
                ", score=" + scoreEarned +
                '}';
    }
}
