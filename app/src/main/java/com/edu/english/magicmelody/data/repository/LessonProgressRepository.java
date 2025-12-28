package com.edu.english.magicmelody.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.edu.english.magicmelody.data.MagicMelodyDatabase;
import com.edu.english.magicmelody.data.dao.LessonProgressDao;
import com.edu.english.magicmelody.data.entity.LessonProgress;
import com.edu.english.magicmelody.model.GameResult;

import java.util.List;

/**
 * 📚 LessonProgress Repository
 * 
 * Purpose: Single source of truth for lesson progress data
 * Handles lesson completion tracking and statistics
 */
public class LessonProgressRepository {
    
    private final LessonProgressDao lessonProgressDao;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public LessonProgressRepository(Application application) {
        MagicMelodyDatabase database = MagicMelodyDatabase.getInstance(application);
        lessonProgressDao = database.lessonProgressDao();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 QUERY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get progress for a specific lesson
     */
    public LiveData<LessonProgress> getProgress(String lessonId, long userId) {
        return lessonProgressDao.getProgress(lessonId, String.valueOf(userId));
    }
    
    /**
     * Get progress synchronously
     */
    public LessonProgress getProgressSync(String lessonId, long userId) {
        return lessonProgressDao.getProgressSync(lessonId, String.valueOf(userId));
    }
    
    /**
     * Get all progress for user
     */
    public LiveData<List<LessonProgress>> getAllProgressForUser(long userId) {
        return lessonProgressDao.getAllProgressForUser(String.valueOf(userId));
    }
    
    /**
     * Get progress for a world
     */
    public LiveData<List<LessonProgress>> getProgressForWorld(String worldId, long userId) {
        return lessonProgressDao.getProgressForWorld(worldId, String.valueOf(userId));
    }
    
    /**
     * Get completed lessons for a world
     */
    public LiveData<List<LessonProgress>> getCompletedLessonsForWorld(String worldId, long userId) {
        return lessonProgressDao.getCompletedLessonsForWorld(worldId, String.valueOf(userId));
    }
    
    /**
     * Get total stars for world
     */
    public int getTotalStarsForWorld(String worldId, long userId) {
        return lessonProgressDao.getTotalStarsForWorld(worldId, String.valueOf(userId));
    }
    
    /**
     * Get total stars for user
     */
    public int getTotalStarsForUser(long userId) {
        return lessonProgressDao.getTotalStarsForUser(String.valueOf(userId));
    }
    
    /**
     * Get completed lesson count for world
     */
    public int getCompletedLessonCount(String worldId, long userId) {
        return lessonProgressDao.getCompletedLessonCount(worldId, String.valueOf(userId));
    }
    
    /**
     * Get recently played lessons
     */
    public LiveData<List<LessonProgress>> getRecentlyPlayed(long userId, int limit) {
        return lessonProgressDao.getRecentlyPlayed(String.valueOf(userId), limit);
    }
    
    /**
     * Get lessons with 3 stars
     */
    public LiveData<List<LessonProgress>> getPerfectLessons(long userId) {
        return lessonProgressDao.getPerfectLessons(String.valueOf(userId));
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ✏️ INSERT/UPDATE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Insert new progress
     */
    public void insert(LessonProgress progress) {
        MagicMelodyDatabase.runAsync(() -> {
            lessonProgressDao.insert(progress);
        });
    }
    
    /**
     * Update progress
     */
    public void update(LessonProgress progress) {
        MagicMelodyDatabase.runAsync(() -> {
            lessonProgressDao.update(progress);
        });
    }
    
    /**
     * Save game result - updates or creates progress entry
     */
    public void saveGameResult(GameResult result, long userId, OnProgressSavedListener listener) {
        MagicMelodyDatabase.runAsync(() -> {
            String lessonId = result.getLessonId();
            String worldId = result.getWorldId();
            long now = System.currentTimeMillis();
            
            // Check if progress exists
            LessonProgress existing = lessonProgressDao.getProgressSync(lessonId, String.valueOf(userId));
            
            int newStarsEarned = 0;
            
            if (existing == null) {
                // Create new progress
                LessonProgress newProgress = new LessonProgress(lessonId, userId, worldId);
                newProgress.updateWithResult(
                    result.getTotalScore(),
                    result.getMaxCombo(),
                    result.getPerfectHits(),
                    result.getGoodHits(),
                    result.getOkHits(),
                    result.getMissCount()
                );
                lessonProgressDao.insert(newProgress);
                newStarsEarned = newProgress.getStarsEarned();
            } else {
                // Update existing
                int oldStars = existing.getStarsEarned();
                existing.updateWithResult(
                    result.getTotalScore(),
                    result.getMaxCombo(),
                    result.getPerfectHits(),
                    result.getGoodHits(),
                    result.getOkHits(),
                    result.getMissCount()
                );
                lessonProgressDao.update(existing);
                newStarsEarned = Math.max(0, existing.getStarsEarned() - oldStars);
            }
            
            if (listener != null) {
                final int starsGained = newStarsEarned;
                listener.onProgressSaved(starsGained);
            }
        });
    }
    
    /**
     * Mark lesson as completed
     */
    public void markCompleted(String lessonId, long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            lessonProgressDao.markCompleted(lessonId, String.valueOf(userId), System.currentTimeMillis());
        });
    }
    
    /**
     * Update best score if higher
     */
    public void updateBestScoreIfHigher(String lessonId, long userId, int newScore) {
        MagicMelodyDatabase.runAsync(() -> {
            lessonProgressDao.updateBestScoreIfHigher(
                lessonId, String.valueOf(userId), newScore, System.currentTimeMillis()
            );
        });
    }
    
    /**
     * Increment play count
     */
    public void incrementTimesPlayed(String lessonId, long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            lessonProgressDao.incrementTimesPlayed(lessonId, String.valueOf(userId), System.currentTimeMillis());
        });
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🗑️ DELETE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Delete all progress for user
     */
    public void deleteAllForUser(long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            lessonProgressDao.deleteAllForUser(String.valueOf(userId));
        });
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔔 CALLBACK INTERFACE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Callback for progress saved
     */
    public interface OnProgressSavedListener {
        void onProgressSaved(int newStarsEarned);
    }
}
