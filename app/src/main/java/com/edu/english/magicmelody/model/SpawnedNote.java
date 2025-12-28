package com.edu.english.magicmelody.model;

/**
 * 🎵 SpawnedNote Model
 * 
 * Purpose: Represents a note that has been spawned and is active in the game.
 * This is different from NoteEvent - NoteEvent is the definition,
 * SpawnedNote is the runtime instance currently on screen.
 * 
 * Lifecycle:
 * 1. Created from NoteEvent when note should appear
 * 2. Travels down the screen during gameplay
 * 3. Either hit by player or missed
 * 4. Destroyed and recycled
 */
public class SpawnedNote {
    
    // ═══════════════════════════════════════════════════════════════
    // 🎵 NOTE STATE ENUM
    // ═══════════════════════════════════════════════════════════════
    
    public enum NoteState {
        SPAWNING,    // Just appeared, animating in
        FALLING,     // Actively falling down the screen
        HIT,         // Player hit this note
        MISSED,      // Player missed this note
        DESTROYED    // Ready for recycling
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📋 FIELDS
    // ═══════════════════════════════════════════════════════════════
    
    private final String id;
    private final NoteEvent noteEvent;
    private final long spawnTimeMs;
    private final long targetTimeMs;
    
    private int lane;
    private float yPosition;
    private float scale = 1.0f;
    private float alpha = 1.0f;
    private NoteState state = NoteState.FALLING;
    
    // Hit detection
    private long hitTimeMs = -1;
    private HitResult hitResult = null;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public SpawnedNote(String id, NoteEvent noteEvent, long spawnTimeMs, long targetTimeMs) {
        this.id = id;
        this.noteEvent = noteEvent;
        this.lane = noteEvent.getLane();
        this.spawnTimeMs = spawnTimeMs;
        this.targetTimeMs = targetTimeMs;
    }
    
    /**
     * Create a spawned note from a NoteEvent
     */
    public static SpawnedNote fromNoteEvent(NoteEvent event, int noteIndex, long currentTimeMs, long travelTimeMs) {
        String id = "note_" + noteIndex + "_" + System.currentTimeMillis();
        long targetTimeMs = (long)(event.getTime() * 1000);
        long spawnTimeMs = targetTimeMs - travelTimeMs;
        
        return new SpawnedNote(id, event, spawnTimeMs, targetTimeMs);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 GAMEPLAY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Update position based on current song time
     * @param currentTimeMs Current song time in milliseconds
     * @param screenHeight Screen height for position calculation
     * @param hitLineY Y position of the hit line
     */
    public void updatePosition(long currentTimeMs, float screenHeight, float hitLineY) {
        if (state == NoteState.FALLING || state == NoteState.SPAWNING) {
            // Calculate progress from spawn to target
            float totalTime = targetTimeMs - spawnTimeMs;
            float elapsed = currentTimeMs - spawnTimeMs;
            float progress = Math.max(0, Math.min(1, elapsed / totalTime));
            
            // Linear interpolation from top to hit line
            yPosition = progress * hitLineY;
            
            // Check if past hit window
            if (currentTimeMs > targetTimeMs + 300) { // 300ms grace period
                if (state != NoteState.HIT) {
                    state = NoteState.MISSED;
                }
            }
        }
    }
    
    /**
     * Mark this note as hit
     */
    public void markHit(long hitTimeMs, HitResult result) {
        this.state = NoteState.HIT;
        this.hitTimeMs = hitTimeMs;
        this.hitResult = result;
    }
    
    /**
     * Mark this note as missed
     */
    public void markMissed() {
        this.state = NoteState.MISSED;
    }
    
    /**
     * Check if this note is active (not destroyed)
     */
    public boolean isActive() {
        return state == NoteState.SPAWNING || state == NoteState.FALLING;
    }
    
    /**
     * Check if this note should be removed
     */
    public boolean shouldRemove() {
        return state == NoteState.DESTROYED || 
               (state == NoteState.MISSED && alpha <= 0) ||
               (state == NoteState.HIT && alpha <= 0);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public String getId() {
        return id;
    }
    
    public NoteEvent getNoteEvent() {
        return noteEvent;
    }
    
    public String getNote() {
        return noteEvent.getNote();
    }
    
    public String getWord() {
        return noteEvent.getWord();
    }
    
    public int getLane() {
        return lane;
    }
    
    public void setLane(int lane) {
        this.lane = lane;
    }
    
    public float getYPosition() {
        return yPosition;
    }
    
    public void setYPosition(float yPosition) {
        this.yPosition = yPosition;
    }
    
    public float getScale() {
        return scale;
    }
    
    public void setScale(float scale) {
        this.scale = scale;
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
    
    public NoteState getState() {
        return state;
    }
    
    public void setState(NoteState state) {
        this.state = state;
    }
    
    public long getSpawnTimeMs() {
        return spawnTimeMs;
    }
    
    public long getTargetTimeMs() {
        return targetTimeMs;
    }
    
    public long getHitTimeMs() {
        return hitTimeMs;
    }
    
    public HitResult getHitResult() {
        return hitResult;
    }
    
    /**
     * Get the timing offset (positive = late, negative = early)
     */
    public long getTimingOffset() {
        if (hitTimeMs >= 0) {
            return hitTimeMs - targetTimeMs;
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return "SpawnedNote{" +
                "id='" + id + '\'' +
                ", note='" + getNote() + '\'' +
                ", lane=" + lane +
                ", state=" + state +
                ", y=" + yPosition +
                '}';
    }
}
