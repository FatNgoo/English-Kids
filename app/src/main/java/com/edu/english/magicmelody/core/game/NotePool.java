package com.edu.english.magicmelody.core.game;

import java.util.ArrayList;
import java.util.List;

/**
 * NotePool - Object pool for Note instances
 * Avoids garbage collection during gameplay by reusing Note objects
 */
public class NotePool {
    
    private static final int DEFAULT_POOL_SIZE = 50;
    
    private final List<Note> pool;
    private final int maxSize;
    
    public NotePool() {
        this(DEFAULT_POOL_SIZE);
    }
    
    public NotePool(int size) {
        this.maxSize = size;
        this.pool = new ArrayList<>(size);
        
        // Pre-populate pool
        for (int i = 0; i < size; i++) {
            pool.add(new Note());
        }
    }
    
    /**
     * Get a note from the pool
     * @return An unused Note, or null if pool exhausted
     */
    public Note obtain() {
        for (Note note : pool) {
            if (!note.isInUse()) {
                note.setInUse(true);
                return note;
            }
        }
        
        // Pool exhausted, create new note if under max
        if (pool.size() < maxSize * 2) {
            Note note = new Note();
            note.setInUse(true);
            pool.add(note);
            return note;
        }
        
        return null;
    }
    
    /**
     * Get and initialize a note from the pool
     */
    public Note obtain(int lane, long targetTimeMs, String vocabKey) {
        Note note = obtain();
        if (note != null) {
            note.init(lane, targetTimeMs, vocabKey);
        }
        return note;
    }
    
    /**
     * Return a note to the pool
     */
    public void recycle(Note note) {
        if (note != null) {
            note.reset();
        }
    }
    
    /**
     * Reset all notes in pool
     */
    public void reset() {
        for (Note note : pool) {
            note.reset();
        }
    }
    
    /**
     * Get all active notes
     */
    public List<Note> getActiveNotes() {
        List<Note> active = new ArrayList<>();
        for (Note note : pool) {
            if (note.isInUse() && note.isActive()) {
                active.add(note);
            }
        }
        return active;
    }
    
    /**
     * Get count of active notes
     */
    public int getActiveCount() {
        int count = 0;
        for (Note note : pool) {
            if (note.isInUse() && note.isActive()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get total pool size
     */
    public int getPoolSize() {
        return pool.size();
    }
    
    /**
     * Get all notes (for rendering)
     */
    public List<Note> getAllNotes() {
        return pool;
    }
}
