package com.edu.english.magicmelody.gameplay.engine;

import com.edu.english.magicmelody.model.LessonConfig;
import com.edu.english.magicmelody.model.NoteEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎵 Note Spawner
 * 
 * Handles spawning notes at the right time based on:
 * - Lesson configuration
 * - Current song time
 * - Look-ahead timing
 */
public class NoteSpawner {
    
    private static final String TAG = "NoteSpawner";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * How far ahead to spawn notes (in milliseconds)
     * Notes need time to travel from top to hit zone
     */
    private float spawnAheadTimeMs = 2000f;
    
    /**
     * Speed multiplier for note travel
     * Higher = faster notes
     */
    private float noteSpeed = 1.0f;
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 DATA
    // ═══════════════════════════════════════════════════════════════
    
    private List<NoteEvent> noteSequence = new ArrayList<>();
    private int nextNoteIndex = 0;
    private boolean isActive = false;
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface NoteSpawnListener {
        void onNoteSpawn(SpawnedNote note);
        void onAllNotesSpawned();
    }
    
    private NoteSpawnListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 SPAWNED NOTE CLASS
    // ═══════════════════════════════════════════════════════════════
    
    public static class SpawnedNote {
        private final String noteId;
        private final String noteName;
        private final int laneIndex;
        private final float targetTimeMs;  // When note should be hit
        private final float spawnTimeMs;   // When note was spawned
        private final String word;
        private final NoteEvent.NoteType type;
        
        private float currentY = 0f;
        private boolean isHit = false;
        private boolean isMissed = false;
        private boolean isActive = true;
        
        public SpawnedNote(NoteEvent event, float spawnTimeMs, String noteId) {
            this.noteId = noteId;
            this.noteName = event.getNote();
            this.laneIndex = event.getLane();
            this.targetTimeMs = event.getTimeMs();
            this.spawnTimeMs = spawnTimeMs;
            this.word = event.getWord();
            this.type = event.getType();
        }
        
        // Getters
        public String getNoteId() { return noteId; }
        public String getNoteName() { return noteName; }
        public int getLaneIndex() { return laneIndex; }
        public float getTargetTimeMs() { return targetTimeMs; }
        public float getSpawnTimeMs() { return spawnTimeMs; }
        public String getWord() { return word; }
        public NoteEvent.NoteType getType() { return type; }
        public float getCurrentY() { return currentY; }
        public boolean isHit() { return isHit; }
        public boolean isMissed() { return isMissed; }
        public boolean isActive() { return isActive; }
        
        // Setters
        public void setCurrentY(float y) { this.currentY = y; }
        public void markAsHit() { 
            this.isHit = true; 
            this.isActive = false;
        }
        public void markAsMissed() { 
            this.isMissed = true; 
            this.isActive = false;
        }
        
        /**
         * Calculate progress (0 = spawned, 1 = at hit zone)
         */
        public float getProgress(float currentTimeMs, float travelTimeMs) {
            float elapsed = currentTimeMs - spawnTimeMs;
            return Math.min(1f, elapsed / travelTimeMs);
        }
        
        /**
         * Get timing offset from perfect hit
         * Negative = early, Positive = late
         */
        public float getTimingOffset(float currentTimeMs) {
            return currentTimeMs - targetTimeMs;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public NoteSpawner() {
    }
    
    public NoteSpawner(float spawnAheadTimeMs) {
        this.spawnAheadTimeMs = spawnAheadTimeMs;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Load notes from lesson configuration
     */
    public void loadLesson(LessonConfig lesson) {
        this.noteSequence = new ArrayList<>(lesson.getNoteSequence());
        this.nextNoteIndex = 0;
        this.isActive = true;
        
        // Sort by time just in case
        noteSequence.sort((a, b) -> Float.compare(a.getTimeMs(), b.getTimeMs()));
    }
    
    /**
     * Load notes directly
     */
    public void loadNotes(List<NoteEvent> notes) {
        this.noteSequence = new ArrayList<>(notes);
        this.nextNoteIndex = 0;
        this.isActive = true;
        
        noteSequence.sort((a, b) -> Float.compare(a.getTimeMs(), b.getTimeMs()));
    }
    
    /**
     * Set spawn ahead time
     */
    public void setSpawnAheadTimeMs(float timeMs) {
        this.spawnAheadTimeMs = timeMs;
    }
    
    /**
     * Set note speed multiplier
     */
    public void setNoteSpeed(float speed) {
        this.noteSpeed = speed;
        // Adjust spawn ahead time based on speed
        this.spawnAheadTimeMs = 2000f / speed;
    }
    
    /**
     * Set spawn listener
     */
    public void setListener(NoteSpawnListener listener) {
        this.listener = listener;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 SPAWNING LOGIC
    // ═══════════════════════════════════════════════════════════════
    
    private int spawnCounter = 0;
    
    /**
     * Update and spawn notes if needed
     * Call this every frame
     */
    public void update(float currentSongTimeMs) {
        if (!isActive || noteSequence.isEmpty()) {
            return;
        }
        
        // Check if we need to spawn notes
        float spawnWindowEnd = currentSongTimeMs + spawnAheadTimeMs;
        
        while (nextNoteIndex < noteSequence.size()) {
            NoteEvent nextNote = noteSequence.get(nextNoteIndex);
            
            if (nextNote.getTimeMs() <= spawnWindowEnd) {
                // Time to spawn this note!
                spawnNote(nextNote, currentSongTimeMs);
                nextNoteIndex++;
            } else {
                // No more notes to spawn in this frame
                break;
            }
        }
        
        // Check if all notes have been spawned
        if (nextNoteIndex >= noteSequence.size()) {
            isActive = false;
            if (listener != null) {
                listener.onAllNotesSpawned();
            }
        }
    }
    
    private void spawnNote(NoteEvent event, float currentTimeMs) {
        String noteId = "note_" + (spawnCounter++);
        SpawnedNote spawned = new SpawnedNote(event, currentTimeMs, noteId);
        
        if (listener != null) {
            listener.onNoteSpawn(spawned);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 QUERIES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get total number of notes in current sequence
     */
    public int getTotalNotes() {
        return noteSequence.size();
    }
    
    /**
     * Get number of notes spawned so far
     */
    public int getSpawnedCount() {
        return nextNoteIndex;
    }
    
    /**
     * Get number of notes remaining to spawn
     */
    public int getRemainingCount() {
        return noteSequence.size() - nextNoteIndex;
    }
    
    /**
     * Check if there are more notes to spawn
     */
    public boolean hasMoreNotes() {
        return nextNoteIndex < noteSequence.size();
    }
    
    /**
     * Get progress (0-1) through the note sequence
     */
    public float getProgress() {
        if (noteSequence.isEmpty()) return 1f;
        return (float) nextNoteIndex / noteSequence.size();
    }
    
    /**
     * Get spawn ahead time in ms
     */
    public float getSpawnAheadTimeMs() {
        return spawnAheadTimeMs;
    }
    
    /**
     * Get note travel time (spawn to hit zone)
     */
    public float getTravelTimeMs() {
        return spawnAheadTimeMs;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 RESET
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Reset spawner to beginning
     */
    public void reset() {
        nextNoteIndex = 0;
        spawnCounter = 0;
        isActive = !noteSequence.isEmpty();
    }
    
    /**
     * Clear all notes
     */
    public void clear() {
        noteSequence.clear();
        nextNoteIndex = 0;
        spawnCounter = 0;
        isActive = false;
    }
}
