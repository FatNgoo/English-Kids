package com.edu.english.magicmelody.model;

/**
 * 🎵 NoteEvent Model
 * 
 * Purpose: Represents a single note in the rhythm sequence
 * 
 * Used for:
 * - Defining when a note appears
 * - Which lane it appears in
 * - Duration of the note
 * - Associated word for learning
 */
public class NoteEvent {
    
    // ═══════════════════════════════════════════════════════════════
    // 🎵 NOTE TYPE ENUM
    // ═══════════════════════════════════════════════════════════════
    
    public enum NoteType {
        TAP,        // Single tap note
        HOLD,       // Hold note (tap and hold)
        SWIPE,      // Swipe note (directional)
        SPECIAL,    // Special/bonus note
        GOLDEN      // Golden/legendary note
    }
    
    private String note;      // "do", "re", "mi", "fa", "sol", "la", "si"
    private float time;       // Time in seconds when note appears
    private int lane;         // Lane number (0-3)
    private float duration;   // Duration in seconds (default 0.5)
    private String word;      // Associated English word (optional)
    private NoteType type = NoteType.TAP;  // Type of note (default TAP)
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public NoteEvent() {
        this.duration = 0.5f; // Default duration
    }
    
    public NoteEvent(String note, float time, int lane) {
        this.note = note;
        this.time = time;
        this.lane = lane;
        this.duration = 0.5f;
    }
    
    public NoteEvent(String note, float time, int lane, String word) {
        this(note, time, lane);
        this.word = word;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
    
    public NoteType getType() {
        return type;
    }
    
    public void setType(NoteType type) {
        this.type = type;
    }
    
    /**
     * Check if note has associated word
     */
    public boolean hasWord() {
        return word != null && !word.isEmpty();
    }
    
    /**
     * Get time in milliseconds
     */
    public long getTimeMs() {
        return (long) (time * 1000);
    }
    
    /**
     * Get duration in milliseconds
     */
    public long getDurationMs() {
        return (long) (duration * 1000);
    }
    
    /**
     * Get the end time of this note
     */
    public float getEndTime() {
        return time + duration;
    }
    
    /**
     * Get display name with emoji
     */
    public String getDisplayName() {
        switch (note.toLowerCase()) {
            case "do": return "🎵 Do";
            case "re": return "🎵 Re";
            case "mi": return "🎵 Mi";
            case "fa": return "🎵 Fa";
            case "sol": return "🎵 Sol";
            case "la": return "🎵 La";
            case "si": return "🎵 Si";
            default: return "🎵 " + note;
        }
    }
    
    /**
     * Get color for this note (for UI)
     */
    public String getNoteColor() {
        switch (note.toLowerCase()) {
            case "do": return "#FF6B6B";  // Red
            case "re": return "#FFA94D";  // Orange
            case "mi": return "#FFD43B";  // Yellow
            case "fa": return "#69DB7C";  // Green
            case "sol": return "#4DABF7"; // Blue
            case "la": return "#9775FA";  // Purple
            case "si": return "#F783AC";  // Pink
            default: return "#FFFFFF";
        }
    }
    
    @Override
    public String toString() {
        return "NoteEvent{" +
                "note='" + note + '\'' +
                ", time=" + time +
                ", lane=" + lane +
                ", word='" + word + '\'' +
                '}';
    }
}
