package com.edu.english.magicmelody.core.game;

/**
 * Note - Represents a single note in the rhythm game
 * Used with ObjectPool for efficient memory management
 */
public class Note {
    
    // Note states
    public static final int STATE_INACTIVE = 0;
    public static final int STATE_FALLING = 1;
    public static final int STATE_HIT = 2;
    public static final int STATE_MISSED = 3;
    
    // Properties
    private int lane;           // 0-6 for piano keys C-B
    private long targetTimeMs;  // When the note should be hit
    private String vocabKey;    // Reference to vocabulary
    private int state;
    private float y;            // Current Y position (0-1, where 1 is hit line)
    private float alpha;        // For fade effects
    
    // For object pooling
    private boolean inUse;
    
    public Note() {
        reset();
    }
    
    /**
     * Initialize note for use
     */
    public void init(int lane, long targetTimeMs, String vocabKey) {
        this.lane = lane;
        this.targetTimeMs = targetTimeMs;
        this.vocabKey = vocabKey;
        this.state = STATE_FALLING;
        this.y = 0f;
        this.alpha = 1f;
        this.inUse = true;
    }
    
    /**
     * Reset note for reuse
     */
    public void reset() {
        this.lane = 0;
        this.targetTimeMs = 0;
        this.vocabKey = null;
        this.state = STATE_INACTIVE;
        this.y = 0f;
        this.alpha = 1f;
        this.inUse = false;
    }
    
    /**
     * Update note position based on current time
     * @param currentTimeMs Current game time
     * @param approachTimeMs How long notes take to fall
     * @return true if note is still active
     */
    public boolean update(long currentTimeMs, long approachTimeMs) {
        if (state == STATE_INACTIVE) return false;
        
        // Calculate progress (0 = spawn, 1 = hit line)
        long timeUntilHit = targetTimeMs - currentTimeMs;
        y = 1f - ((float) timeUntilHit / approachTimeMs);
        
        // Handle missed notes
        if (state == STATE_FALLING && y > 1.2f) {
            state = STATE_MISSED;
            return false;
        }
        
        // Handle hit/missed fade out
        if (state == STATE_HIT || state == STATE_MISSED) {
            alpha -= 0.1f;
            if (alpha <= 0) {
                reset();
                return false;
            }
        }
        
        return true;
    }
    
    // Getters and Setters
    public int getLane() { return lane; }
    public void setLane(int lane) { this.lane = lane; }
    
    public long getTargetTimeMs() { return targetTimeMs; }
    public void setTargetTimeMs(long targetTimeMs) { this.targetTimeMs = targetTimeMs; }
    
    public String getVocabKey() { return vocabKey; }
    public void setVocabKey(String vocabKey) { this.vocabKey = vocabKey; }
    
    public int getState() { return state; }
    public void setState(int state) { this.state = state; }
    
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    
    public float getAlpha() { return alpha; }
    public void setAlpha(float alpha) { this.alpha = alpha; }
    
    public boolean isInUse() { return inUse; }
    public void setInUse(boolean inUse) { this.inUse = inUse; }
    
    public boolean isActive() {
        return state == STATE_FALLING;
    }
    
    public boolean isHit() {
        return state == STATE_HIT;
    }
    
    public boolean isMissed() {
        return state == STATE_MISSED;
    }
}
