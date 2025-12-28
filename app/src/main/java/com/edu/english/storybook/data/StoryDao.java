package com.edu.english.storybook.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for Story entities
 */
@Dao
public interface StoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoryEntity story);
    
    @Update
    void update(StoryEntity story);
    
    @Delete
    void delete(StoryEntity story);
    
    @Query("DELETE FROM stories WHERE id = :storyId")
    void deleteById(String storyId);
    
    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    LiveData<List<StoryEntity>> getAllStories();
    
    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    List<StoryEntity> getAllStoriesSync();
    
    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    LiveData<StoryEntity> getStoryById(String storyId);
    
    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    StoryEntity getStoryByIdSync(String storyId);
    
    @Query("SELECT * FROM stories WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    LiveData<List<StoryEntity>> searchByTitle(String query);
    
    @Query("SELECT * FROM stories WHERE category = :category ORDER BY createdAt DESC")
    LiveData<List<StoryEntity>> getStoriesByCategory(String category);
    
    @Query("SELECT COUNT(*) FROM stories")
    int getStoryCount();
}
