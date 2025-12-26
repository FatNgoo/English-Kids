package com.edu.english.magicmelody.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.edu.english.magicmelody.data.local.CollectionDao;
import com.edu.english.magicmelody.data.local.MagicMelodyDatabase;
import com.edu.english.magicmelody.data.model.CollectionEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Collection data
 * Handles data operations with background threading
 */
public class CollectionRepository {
    
    private final CollectionDao collectionDao;
    private final ExecutorService executor;
    
    public CollectionRepository(Context context) {
        MagicMelodyDatabase db = MagicMelodyDatabase.getInstance(context);
        collectionDao = db.collectionDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    public void insert(CollectionEntity collection) {
        executor.execute(() -> collectionDao.insert(collection));
    }
    
    public void insertAll(List<CollectionEntity> collections) {
        executor.execute(() -> collectionDao.insertAll(collections));
    }
    
    public LiveData<List<CollectionEntity>> getAll() {
        return collectionDao.getAll();
    }
    
    public LiveData<List<CollectionEntity>> getByTheme(String theme) {
        return collectionDao.getByTheme(theme);
    }
    
    public LiveData<List<CollectionEntity>> getByLesson(String lessonId) {
        return collectionDao.getByLesson(lessonId);
    }
    
    public LiveData<Integer> getCollectionCount() {
        return collectionDao.getCollectionCount();
    }
    
    public void deleteById(long id) {
        executor.execute(() -> collectionDao.deleteById(id));
    }
    
    public void deleteAll() {
        executor.execute(collectionDao::deleteAll);
    }
    
    /**
     * Get collections synchronously (call from background thread only)
     */
    public List<CollectionEntity> getAllSync() {
        return collectionDao.getAllSync();
    }
    
    public List<CollectionEntity> getByLessonSync(String lessonId) {
        return collectionDao.getByLessonSync(lessonId);
    }
}
