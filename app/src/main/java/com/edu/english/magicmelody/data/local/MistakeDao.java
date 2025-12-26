package com.edu.english.magicmelody.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.edu.english.magicmelody.data.model.MistakeEntity;

import java.util.List;

/**
 * Data Access Object for Mistake entities
 * Tracks vocabulary mistakes for Glitch Note intelligent review system
 */
@Dao
public interface MistakeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MistakeEntity mistake);
    
    @Update
    void update(MistakeEntity mistake);
    
    @Query("SELECT * FROM mistakes WHERE vocab = :vocab")
    MistakeEntity get(String vocab);
    
    @Query("SELECT * FROM mistakes ORDER BY wrongCount DESC LIMIT :limit")
    List<MistakeEntity> topMistakes(int limit);
    
    @Query("SELECT * FROM mistakes ORDER BY wrongCount DESC")
    LiveData<List<MistakeEntity>> getAllMistakes();
    
    @Query("SELECT * FROM mistakes WHERE lessonId = :lessonId ORDER BY wrongCount DESC")
    List<MistakeEntity> getMistakesByLesson(String lessonId);
    
    @Query("UPDATE mistakes SET wrongCount = wrongCount + 1, lastWrongAt = :timestamp WHERE vocab = :vocab")
    void incrementWrongCount(String vocab, long timestamp);
    
    @Query("DELETE FROM mistakes WHERE vocab = :vocab")
    void delete(String vocab);
    
    @Query("DELETE FROM mistakes")
    void deleteAll();
    
    /**
     * Increment wrong count or create new entry
     */
    default void incrementWrong(String vocab, String lessonId) {
        MistakeEntity existing = get(vocab);
        if (existing != null) {
            existing.incrementWrongCount();
            update(existing);
        } else {
            insert(new MistakeEntity(vocab, lessonId));
        }
    }
}
