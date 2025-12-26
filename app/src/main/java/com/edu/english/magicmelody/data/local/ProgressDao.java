package com.edu.english.magicmelody.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.edu.english.magicmelody.data.model.ProgressEntity;

import java.util.List;

/**
 * Data Access Object for Progress entities
 * Handles level progress, stars, and unlock status
 */
@Dao
public interface ProgressDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ProgressEntity progress);
    
    @Update
    void update(ProgressEntity progress);
    
    @Query("SELECT * FROM progress WHERE lessonId = :lessonId")
    ProgressEntity get(String lessonId);
    
    @Query("SELECT * FROM progress WHERE lessonId = :lessonId")
    LiveData<ProgressEntity> getLive(String lessonId);
    
    @Query("SELECT * FROM progress ORDER BY lessonId ASC")
    LiveData<List<ProgressEntity>> getAllOrdered();
    
    @Query("SELECT * FROM progress ORDER BY lessonId ASC")
    List<ProgressEntity> getAllOrderedSync();
    
    @Query("SELECT * FROM progress WHERE unlocked = 1 ORDER BY lessonId ASC")
    LiveData<List<ProgressEntity>> getUnlockedLevels();
    
    @Query("UPDATE progress SET unlocked = 1 WHERE lessonId = :lessonId")
    void unlockLevel(String lessonId);
    
    @Query("UPDATE progress SET stars = :stars, bestScore = :score, lastPlayedAt = :timestamp, playCount = playCount + 1 WHERE lessonId = :lessonId")
    void updateScore(String lessonId, int stars, int score, long timestamp);
    
    @Query("SELECT SUM(stars) FROM progress")
    LiveData<Integer> getTotalStars();
    
    @Query("SELECT COUNT(*) FROM progress WHERE stars > 0")
    LiveData<Integer> getCompletedCount();
    
    @Query("DELETE FROM progress")
    void deleteAll();
    
    /**
     * Upsert operation - insert or replace
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ProgressEntity progress);
}
