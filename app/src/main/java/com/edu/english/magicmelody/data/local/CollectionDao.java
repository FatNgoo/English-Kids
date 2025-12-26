package com.edu.english.magicmelody.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.edu.english.magicmelody.data.model.CollectionEntity;

import java.util.List;

/**
 * Data Access Object for Collection entities
 * Handles all database operations for collected items
 */
@Dao
public interface CollectionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CollectionEntity collection);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CollectionEntity> collections);
    
    @Query("SELECT * FROM collections ORDER BY collectedAt DESC")
    LiveData<List<CollectionEntity>> getAll();
    
    @Query("SELECT * FROM collections ORDER BY collectedAt DESC")
    List<CollectionEntity> getAllSync();
    
    @Query("SELECT * FROM collections WHERE theme = :theme ORDER BY collectedAt DESC")
    LiveData<List<CollectionEntity>> getByTheme(String theme);
    
    @Query("SELECT * FROM collections WHERE lessonId = :lessonId")
    LiveData<List<CollectionEntity>> getByLesson(String lessonId);
    
    @Query("SELECT * FROM collections WHERE lessonId = :lessonId")
    List<CollectionEntity> getByLessonSync(String lessonId);
    
    @Query("SELECT COUNT(*) FROM collections")
    LiveData<Integer> getCollectionCount();
    
    @Query("SELECT COUNT(*) FROM collections WHERE theme = :theme")
    int getCountByTheme(String theme);
    
    @Query("DELETE FROM collections WHERE id = :id")
    void deleteById(long id);
    
    @Query("DELETE FROM collections")
    void deleteAll();
}
