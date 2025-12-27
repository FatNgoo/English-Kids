package com.edu.english.storybook.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.edu.english.storybook.model.Story;
import com.google.gson.Gson;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Story data operations
 */
public class StoryRepository {
    
    private final StoryDao storyDao;
    private final Gson gson;
    private final ExecutorService executorService;
    
    public StoryRepository(Context context) {
        StoryDatabase database = StoryDatabase.getInstance(context);
        this.storyDao = database.storyDao();
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Save a story to the database
     */
    public void saveStory(Story story, OnSaveCallback callback) {
        executorService.execute(() -> {
            try {
                StoryEntity entity = storyToEntity(story);
                storyDao.insert(entity);
                if (callback != null) {
                    callback.onSuccess(story.getId());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * Insert entity directly with callback
     */
    public void insert(StoryEntity entity, InsertCallback callback) {
        executorService.execute(() -> {
            try {
                storyDao.insert(entity);
                if (callback != null) {
                    callback.onInserted(entity.getId());
                }
            } catch (Exception e) {
                // Silent fail
            }
        });
    }
    
    /**
     * Delete story entity
     */
    public void delete(StoryEntity entity, Runnable callback) {
        executorService.execute(() -> {
            try {
                storyDao.delete(entity);
                if (callback != null) {
                    callback.run();
                }
            } catch (Exception e) {
                // Silent fail
            }
        });
    }
    
    /**
     * Get all stories with callback
     */
    public void getAllStories(ListCallback callback) {
        executorService.execute(() -> {
            try {
                List<StoryEntity> stories = storyDao.getAllStoriesSync();
                if (callback != null) {
                    callback.onResult(stories);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onResult(java.util.Collections.emptyList());
                }
            }
        });
    }
    
    /**
     * Delete a story from database
     */
    public void deleteStory(String storyId, OnDeleteCallback callback) {
        executorService.execute(() -> {
            try {
                storyDao.deleteById(storyId);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get all stories as LiveData
     */
    public LiveData<List<StoryEntity>> getAllStories() {
        return storyDao.getAllStories();
    }
    
    /**
     * Get story by ID as LiveData
     */
    public LiveData<StoryEntity> getStoryById(String storyId) {
        return storyDao.getStoryById(storyId);
    }
    
    /**
     * Get story by ID synchronously (call from background thread)
     */
    public void getStoryByIdAsync(String storyId, OnStoryLoadedCallback callback) {
        executorService.execute(() -> {
            try {
                StoryEntity entity = storyDao.getStoryByIdSync(storyId);
                if (entity != null) {
                    Story story = entityToStory(entity);
                    callback.onSuccess(story);
                } else {
                    callback.onError("Story not found");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    /**
     * Search stories by title
     */
    public LiveData<List<StoryEntity>> searchByTitle(String query) {
        return storyDao.searchByTitle(query);
    }
    
    /**
     * Get stories by category
     */
    public LiveData<List<StoryEntity>> getStoriesByCategory(String category) {
        return storyDao.getStoriesByCategory(category);
    }
    
    /**
     * Convert Story model to StoryEntity
     */
    private StoryEntity storyToEntity(Story story) {
        StoryEntity entity = new StoryEntity();
        
        String id = story.getId();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            story.setId(id);
        }
        
        entity.setId(id);
        entity.setTitle(story.getTitle());
        entity.setCategory(story.getCategory().name());
        entity.setAge(story.getTargetAge());
        entity.setReadingLevel(story.getLevel());
        entity.setCreatedAt(story.getCreatedAt());
        entity.setStoryJson(gson.toJson(story));
        
        return entity;
    }
    
    /**
     * Convert StoryEntity to Story model
     */
    public Story entityToStory(StoryEntity entity) {
        if (entity == null || entity.getStoryJson() == null) {
            return null;
        }
        return gson.fromJson(entity.getStoryJson(), Story.class);
    }
    
    // Callbacks
    public interface OnSaveCallback {
        void onSuccess(String storyId);
        void onError(String message);
    }
    
    public interface OnDeleteCallback {
        void onSuccess();
        void onError(String message);
    }
    
    public interface OnStoryLoadedCallback {
        void onSuccess(Story story);
        void onError(String message);
    }
    
    public interface InsertCallback {
        void onInserted(String id);
    }
    
    public interface ListCallback {
        void onResult(List<StoryEntity> stories);
    }
}