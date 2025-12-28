package com.edu.english.magicmelody.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.edu.english.magicmelody.data.entity.CollectedNote;

import java.util.List;

/**
 * 🎵 CollectedNote DAO
 * 
 * Purpose: Data Access Object for CollectedNote entity
 * Manages the Magic Notebook - note collection and favorites
 */
@Dao
public interface CollectedNoteDao {
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 QUERY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get a specific note by ID
     */
    @Query("SELECT * FROM collected_notes WHERE note_id = :noteId")
    LiveData<CollectedNote> getNoteById(String noteId);
    
    /**
     * Get a specific note synchronously
     */
    @Query("SELECT * FROM collected_notes WHERE note_id = :noteId")
    CollectedNote getNoteByIdSync(String noteId);
    
    /**
     * Get all collected notes for a user
     */
    @Query("SELECT * FROM collected_notes WHERE user_id = :userId ORDER BY collected_at DESC")
    LiveData<List<CollectedNote>> getAllNotesForUser(String userId);
    
    /**
     * Get all collected notes (synchronous)
     */
    @Query("SELECT * FROM collected_notes WHERE user_id = :userId ORDER BY collected_at DESC")
    List<CollectedNote> getAllNotesForUserSync(String userId);
    
    /**
     * Get notes by type
     */
    @Query("SELECT * FROM collected_notes WHERE user_id = :userId AND note_type = :type ORDER BY collected_at DESC")
    LiveData<List<CollectedNote>> getNotesByType(String userId, String type);
    
    /**
     * Get notes from a specific world
     */
    @Query("SELECT * FROM collected_notes WHERE user_id = :userId AND world_id = :worldId ORDER BY collected_at DESC")
    LiveData<List<CollectedNote>> getNotesFromWorld(String userId, String worldId);
    
    /**
     * Get favorite notes
     */
    @Query("SELECT * FROM collected_notes WHERE user_id = :userId AND is_favorite = 1 ORDER BY collected_at DESC")
    LiveData<List<CollectedNote>> getFavoriteNotes(String userId);
    
    /**
     * Get recently collected notes
     */
    @Query("SELECT * FROM collected_notes WHERE user_id = :userId ORDER BY collected_at DESC LIMIT :limit")
    LiveData<List<CollectedNote>> getRecentNotes(String userId, int limit);
    
    /**
     * Get mastered notes (times_played >= 10)
     */
    @Query("SELECT * FROM collected_notes WHERE user_id = :userId AND times_played >= 10")
    LiveData<List<CollectedNote>> getMasteredNotes(String userId);
    
    /**
     * Get legendary notes
     */
    @Query("SELECT * FROM collected_notes WHERE user_id = :userId AND note_type = 'legendary'")
    LiveData<List<CollectedNote>> getLegendaryNotes(String userId);
    
    /**
     * Count total collected notes
     */
    @Query("SELECT COUNT(*) FROM collected_notes WHERE user_id = :userId")
    int getTotalNoteCount(String userId);
    
    /**
     * Count notes by type
     */
    @Query("SELECT COUNT(*) FROM collected_notes WHERE user_id = :userId AND note_type = :type")
    int getNoteCountByType(String userId, String type);
    
    /**
     * Check if a note is collected
     */
    @Query("SELECT COUNT(*) > 0 FROM collected_notes WHERE note_id = :noteId AND user_id = :userId")
    boolean isNoteCollected(String noteId, String userId);
    
    /**
     * Get notes with associated words
     */
    @Query("SELECT * FROM collected_notes WHERE user_id = :userId AND word IS NOT NULL AND word != '' ORDER BY word ASC")
    LiveData<List<CollectedNote>> getNotesWithWords(String userId);
    
    /**
     * Search notes by word
     */
    @Query("SELECT * FROM collected_notes WHERE user_id = :userId AND word LIKE '%' || :searchTerm || '%'")
    LiveData<List<CollectedNote>> searchNotesByWord(String userId, String searchTerm);
    
    // ═══════════════════════════════════════════════════════════════
    // ✏️ INSERT METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Insert a new collected note
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(CollectedNote note);
    
    /**
     * Insert multiple notes
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertAll(List<CollectedNote> notes);
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 UPDATE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Update a note
     */
    @Update
    void update(CollectedNote note);
    
    /**
     * Toggle favorite status
     */
    @Query("UPDATE collected_notes SET is_favorite = NOT is_favorite WHERE note_id = :noteId")
    void toggleFavorite(String noteId);
    
    /**
     * Set favorite status
     */
    @Query("UPDATE collected_notes SET is_favorite = :isFavorite WHERE note_id = :noteId")
    void setFavorite(String noteId, boolean isFavorite);
    
    /**
     * Increment play count
     */
    @Query("UPDATE collected_notes SET times_played = times_played + 1, last_played_at = :timestamp WHERE note_id = :noteId")
    void incrementPlayCount(String noteId, long timestamp);
    
    /**
     * Update audio file path
     */
    @Query("UPDATE collected_notes SET audio_file = :audioPath WHERE note_id = :noteId")
    void updateAudioFile(String noteId, String audioPath);
    
    /**
     * Mark note as mastered (set mastery_level to max)
     */
    @Query("UPDATE collected_notes SET mastery_level = 5 WHERE note_id = :noteId")
    void markAsMastered(String noteId);
    
    // ═══════════════════════════════════════════════════════════════
    // 🗑️ DELETE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Delete a note
     */
    @Delete
    void delete(CollectedNote note);
    
    /**
     * Delete note by ID
     */
    @Query("DELETE FROM collected_notes WHERE note_id = :noteId")
    void deleteById(String noteId);
    
    /**
     * Delete all notes for user
     */
    @Query("DELETE FROM collected_notes WHERE user_id = :userId")
    void deleteAllForUser(String userId);
    
    /**
     * Delete all notes (reset)
     */
    @Query("DELETE FROM collected_notes")
    void deleteAll();
}
