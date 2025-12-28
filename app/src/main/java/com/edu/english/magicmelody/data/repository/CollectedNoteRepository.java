package com.edu.english.magicmelody.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.edu.english.magicmelody.data.MagicMelodyDatabase;
import com.edu.english.magicmelody.data.dao.CollectedNoteDao;
import com.edu.english.magicmelody.data.entity.CollectedNote;

import java.util.List;

/**
 * 🎵 CollectedNote Repository
 * 
 * Purpose: Single source of truth for Magic Notebook data
 * Manages note collection and favorites
 */
public class CollectedNoteRepository {
    
    private final CollectedNoteDao collectedNoteDao;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public CollectedNoteRepository(Application application) {
        MagicMelodyDatabase database = MagicMelodyDatabase.getInstance(application);
        collectedNoteDao = database.collectedNoteDao();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 QUERY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get a specific note
     */
    public LiveData<CollectedNote> getNoteById(String noteId) {
        return collectedNoteDao.getNoteById(noteId);
    }
    
    /**
     * Get all notes for user
     */
    public LiveData<List<CollectedNote>> getAllNotesForUser(long userId) {
        return collectedNoteDao.getAllNotesForUser(String.valueOf(userId));
    }
    
    /**
     * Get all notes synchronously
     */
    public List<CollectedNote> getAllNotesForUserSync(long userId) {
        return collectedNoteDao.getAllNotesForUserSync(String.valueOf(userId));
    }
    
    /**
     * Get notes by type
     */
    public LiveData<List<CollectedNote>> getNotesByType(long userId, String type) {
        return collectedNoteDao.getNotesByType(String.valueOf(userId), type);
    }
    
    /**
     * Get notes from a specific world
     */
    public LiveData<List<CollectedNote>> getNotesFromWorld(long userId, String worldId) {
        return collectedNoteDao.getNotesFromWorld(String.valueOf(userId), worldId);
    }
    
    /**
     * Get favorite notes
     */
    public LiveData<List<CollectedNote>> getFavoriteNotes(long userId) {
        return collectedNoteDao.getFavoriteNotes(String.valueOf(userId));
    }
    
    /**
     * Get recently collected notes
     */
    public LiveData<List<CollectedNote>> getRecentNotes(long userId, int limit) {
        return collectedNoteDao.getRecentNotes(String.valueOf(userId), limit);
    }
    
    /**
     * Get mastered notes
     */
    public LiveData<List<CollectedNote>> getMasteredNotes(long userId) {
        return collectedNoteDao.getMasteredNotes(String.valueOf(userId));
    }
    
    /**
     * Get legendary notes
     */
    public LiveData<List<CollectedNote>> getLegendaryNotes(long userId) {
        return collectedNoteDao.getLegendaryNotes(String.valueOf(userId));
    }
    
    /**
     * Get notes with associated words
     */
    public LiveData<List<CollectedNote>> getNotesWithWords(long userId) {
        return collectedNoteDao.getNotesWithWords(String.valueOf(userId));
    }
    
    /**
     * Search notes by word
     */
    public LiveData<List<CollectedNote>> searchNotesByWord(long userId, String searchTerm) {
        return collectedNoteDao.searchNotesByWord(String.valueOf(userId), searchTerm);
    }
    
    /**
     * Get total note count
     */
    public int getTotalNoteCount(long userId) {
        return collectedNoteDao.getTotalNoteCount(String.valueOf(userId));
    }
    
    /**
     * Check if note is collected
     */
    public boolean isNoteCollected(String noteId, long userId) {
        return collectedNoteDao.isNoteCollected(noteId, String.valueOf(userId));
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ✏️ INSERT/UPDATE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Insert a new note
     */
    public void insert(CollectedNote note) {
        MagicMelodyDatabase.runAsync(() -> {
            collectedNoteDao.insert(note);
        });
    }
    
    /**
     * Collect a new note (if not already collected)
     */
    public void collectNote(String noteName, String word, String noteType, 
                           String worldId, String lessonId, long userId,
                           OnNoteCollectedListener listener) {
        MagicMelodyDatabase.runAsync(() -> {
            // Generate unique note ID
            String noteId = worldId + "_" + noteName + "_" + System.currentTimeMillis();
            
            // Create the note
            CollectedNote note = CollectedNote.create(
                String.valueOf(userId), noteName, word, worldId, lessonId, noteType
            );
            
            // Insert (IGNORE if exists)
            long result = collectedNoteDao.insert(note);
            
            if (listener != null) {
                boolean isNew = result != -1;
                listener.onNoteCollected(noteId, isNew);
            }
        });
    }
    
    /**
     * Update note
     */
    public void update(CollectedNote note) {
        MagicMelodyDatabase.runAsync(() -> {
            collectedNoteDao.update(note);
        });
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(String noteId) {
        MagicMelodyDatabase.runAsync(() -> {
            collectedNoteDao.toggleFavorite(noteId);
        });
    }
    
    /**
     * Increment play count (when user plays the note)
     */
    public void playNote(String noteId) {
        MagicMelodyDatabase.runAsync(() -> {
            collectedNoteDao.incrementPlayCount(noteId, System.currentTimeMillis());
        });
    }
    
    /**
     * Mark note as mastered
     */
    public void markAsMastered(String noteId) {
        MagicMelodyDatabase.runAsync(() -> {
            collectedNoteDao.markAsMastered(noteId);
        });
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🗑️ DELETE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Delete a note
     */
    public void delete(CollectedNote note) {
        MagicMelodyDatabase.runAsync(() -> {
            collectedNoteDao.delete(note);
        });
    }
    
    /**
     * Delete all notes for user
     */
    public void deleteAllForUser(long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            collectedNoteDao.deleteAllForUser(String.valueOf(userId));
        });
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔔 CALLBACK INTERFACE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Callback for note collection
     */
    public interface OnNoteCollectedListener {
        void onNoteCollected(String noteId, boolean isNew);
    }
}
