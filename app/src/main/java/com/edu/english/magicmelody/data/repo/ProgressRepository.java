package com.edu.english.magicmelody.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.edu.english.magicmelody.data.local.MagicMelodyDatabase;
import com.edu.english.magicmelody.data.local.ProgressDao;
import com.edu.english.magicmelody.data.model.ProgressEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Progress data
 * Handles level progress, stars, and unlock operations
 */
public class ProgressRepository {
    
    private final ProgressDao progressDao;
    private final ExecutorService executor;
    
    public ProgressRepository(Context context) {
        MagicMelodyDatabase db = MagicMelodyDatabase.getInstance(context);
        progressDao = db.progressDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    public void insert(ProgressEntity progress) {
        executor.execute(() -> progressDao.insert(progress));
    }
    
    public void update(ProgressEntity progress) {
        executor.execute(() -> progressDao.update(progress));
    }
    
    public void upsert(ProgressEntity progress) {
        executor.execute(() -> progressDao.upsert(progress));
    }
    
    public LiveData<ProgressEntity> get(String lessonId) {
        return progressDao.getLive(lessonId);
    }
    
    public ProgressEntity getSync(String lessonId) {
        return progressDao.get(lessonId);
    }
    
    public LiveData<List<ProgressEntity>> getAllOrdered() {
        return progressDao.getAllOrdered();
    }
    
    public List<ProgressEntity> getAllOrderedSync() {
        return progressDao.getAllOrderedSync();
    }
    
    public LiveData<List<ProgressEntity>> getUnlockedLevels() {
        return progressDao.getUnlockedLevels();
    }
    
    public void unlockLevel(String lessonId) {
        executor.execute(() -> progressDao.unlockLevel(lessonId));
    }
    
    public void updateScore(String lessonId, int stars, int score) {
        executor.execute(() -> progressDao.updateScore(lessonId, stars, score, System.currentTimeMillis()));
    }
    
    public LiveData<Integer> getTotalStars() {
        return progressDao.getTotalStars();
    }
    
    public LiveData<Integer> getCompletedCount() {
        return progressDao.getCompletedCount();
    }
    
    public void deleteAll() {
        executor.execute(progressDao::deleteAll);
    }
    
    /**
     * Initialize progress for first-time users
     * Unlocks the first level, locks all others
     */
    public void initializeProgress(String[] lessonIds) {
        executor.execute(() -> {
            for (int i = 0; i < lessonIds.length; i++) {
                ProgressEntity existing = progressDao.get(lessonIds[i]);
                if (existing == null) {
                    boolean unlocked = (i == 0); // Only first level unlocked
                    progressDao.insert(new ProgressEntity(lessonIds[i], unlocked));
                }
            }
        });
    }
    
    /**
     * Unlock next level after completing current one
     */
    public void unlockNextLevel(String currentLessonId, String[] allLessonIds) {
        executor.execute(() -> {
            for (int i = 0; i < allLessonIds.length - 1; i++) {
                if (allLessonIds[i].equals(currentLessonId)) {
                    progressDao.unlockLevel(allLessonIds[i + 1]);
                    break;
                }
            }
        });
    }
}
