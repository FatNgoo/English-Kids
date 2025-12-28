package com.edu.english.magicmelody.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.edu.english.magicmelody.data.entity.LessonProgress;

import java.util.List;

/**
 * 📚 LessonProgress DAO
 * 
 * Purpose: Data Access Object for LessonProgress entity
 * Tracks lesson completion, scores, and learning progress
 */
@Dao
public interface LessonProgressDao {
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 QUERY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get progress for a specific lesson
     */
    @Query("SELECT * FROM lesson_progress WHERE lesson_id = :lessonId AND user_id = :userId")
    LiveData<LessonProgress> getProgress(String lessonId, String userId);
    
    /**
     * Get progress synchronously
     */
    @Query("SELECT * FROM lesson_progress WHERE lesson_id = :lessonId AND user_id = :userId")
    LessonProgress getProgressSync(String lessonId, String userId);
    
    /**
     * Get all lesson progress for a user
     */
    @Query("SELECT * FROM lesson_progress WHERE user_id = :userId ORDER BY last_played_at DESC")
    LiveData<List<LessonProgress>> getAllProgressForUser(String userId);
    
    /**
     * Get all lesson progress for a user (synchronous)
     */
    @Query("SELECT * FROM lesson_progress WHERE user_id = :userId ORDER BY last_played_at DESC")
    List<LessonProgress> getAllProgressForUserSync(String userId);
    
    /**
     * Get lesson progress for a specific world
     */
    @Query("SELECT * FROM lesson_progress WHERE world_id = :worldId AND user_id = :userId ORDER BY lesson_id ASC")
    LiveData<List<LessonProgress>> getProgressForWorld(String worldId, String userId);
    
    /**
     * Get lesson progress for a world (synchronous)
     */
    @Query("SELECT * FROM lesson_progress WHERE world_id = :worldId AND user_id = :userId ORDER BY lesson_id ASC")
    List<LessonProgress> getProgressForWorldSync(String worldId, String userId);
    
    /**
     * Get completed lessons for a world
     */
    @Query("SELECT * FROM lesson_progress WHERE world_id = :worldId AND user_id = :userId AND is_completed = 1")
    LiveData<List<LessonProgress>> getCompletedLessonsForWorld(String worldId, String userId);
    
    /**
     * Count completed lessons for a world
     */
    @Query("SELECT COUNT(*) FROM lesson_progress WHERE world_id = :worldId AND user_id = :userId AND is_completed = 1")
    int getCompletedLessonCount(String worldId, String userId);
    
    /**
     * Get total stars earned in a world
     */
    @Query("SELECT SUM(stars_earned) FROM lesson_progress WHERE world_id = :worldId AND user_id = :userId")
    int getTotalStarsForWorld(String worldId, String userId);
    
    /**
     * Get total stars earned by user
     */
    @Query("SELECT SUM(stars_earned) FROM lesson_progress WHERE user_id = :userId")
    int getTotalStarsForUser(String userId);
    
    /**
     * Get best score for a lesson
     */
    @Query("SELECT best_score FROM lesson_progress WHERE lesson_id = :lessonId AND user_id = :userId")
    int getBestScore(String lessonId, String userId);
    
    /**
     * Get lessons with 3 stars
     */
    @Query("SELECT * FROM lesson_progress WHERE user_id = :userId AND stars_earned = 3")
    LiveData<List<LessonProgress>> getPerfectLessons(String userId);
    
    /**
     * Count lessons with 3 stars
     */
    @Query("SELECT COUNT(*) FROM lesson_progress WHERE user_id = :userId AND stars_earned = 3")
    int getPerfectLessonCount(String userId);
    
    /**
     * Get recently played lessons
     */
    @Query("SELECT * FROM lesson_progress WHERE user_id = :userId ORDER BY last_played_at DESC LIMIT :limit")
    LiveData<List<LessonProgress>> getRecentlyPlayed(String userId, int limit);
    
    /**
     * Check if lesson is unlocked (previous lesson completed or first lesson)
     */
    @Query("SELECT COUNT(*) > 0 FROM lesson_progress WHERE lesson_id = :previousLessonId AND user_id = :userId AND is_completed = 1")
    boolean isPreviousLessonCompleted(String previousLessonId, String userId);
    
    // ═══════════════════════════════════════════════════════════════
    // ✏️ INSERT METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Insert new progress entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LessonProgress progress);
    
    /**
     * Insert multiple progress entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<LessonProgress> progressList);
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 UPDATE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Update progress entry
     */
    @Update
    void update(LessonProgress progress);
    
    /**
     * Update best score if new score is higher
     */
    @Query("UPDATE lesson_progress SET best_score = :newScore, last_played_at = :timestamp " +
           "WHERE lesson_id = :lessonId AND user_id = :userId AND best_score < :newScore")
    void updateBestScoreIfHigher(String lessonId, String userId, int newScore, long timestamp);
    
    /**
     * Update stars if new stars are higher
     */
    @Query("UPDATE lesson_progress SET stars_earned = :stars, last_played_at = :timestamp " +
           "WHERE lesson_id = :lessonId AND user_id = :userId AND stars_earned < :stars")
    void updateStarsIfHigher(String lessonId, String userId, int stars, long timestamp);
    
    /**
     * Mark lesson as completed
     */
    @Query("UPDATE lesson_progress SET is_completed = 1, first_completed_at = :timestamp " +
           "WHERE lesson_id = :lessonId AND user_id = :userId")
    void markCompleted(String lessonId, String userId, long timestamp);
    
    /**
     * Increment times played counter
     */
    @Query("UPDATE lesson_progress SET times_played = times_played + 1, last_played_at = :timestamp " +
           "WHERE lesson_id = :lessonId AND user_id = :userId")
    void incrementTimesPlayed(String lessonId, String userId, long timestamp);
    
    /**
     * Update best combo
     */
    @Query("UPDATE lesson_progress SET best_combo = :combo " +
           "WHERE lesson_id = :lessonId AND user_id = :userId AND best_combo < :combo")
    void updateBestComboIfHigher(String lessonId, String userId, int combo);
    
    /**
     * Update perfect hits
     */
    @Query("UPDATE lesson_progress SET perfect_hits = :hits " +
           "WHERE lesson_id = :lessonId AND user_id = :userId AND perfect_hits < :hits")
    void updatePerfectHitsIfHigher(String lessonId, String userId, int hits);
    
    // ═══════════════════════════════════════════════════════════════
    // 🗑️ DELETE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Delete progress entry
     */
    @Delete
    void delete(LessonProgress progress);
    
    /**
     * Delete all progress for a user
     */
    @Query("DELETE FROM lesson_progress WHERE user_id = :userId")
    void deleteAllForUser(String userId);
    
    /**
     * Delete all progress for a world
     */
    @Query("DELETE FROM lesson_progress WHERE world_id = :worldId AND user_id = :userId")
    void deleteAllForWorld(String worldId, String userId);
    
    /**
     * Delete all progress (reset)
     */
    @Query("DELETE FROM lesson_progress")
    void deleteAll();
}
