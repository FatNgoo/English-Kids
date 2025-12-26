package com.edu.english.magicmelody.ui.adventure;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.magicmelody.data.model.Lesson;
import com.edu.english.magicmelody.data.model.ProgressEntity;
import com.edu.english.magicmelody.data.model.UserProfile;
import com.edu.english.magicmelody.data.repo.ProgressRepository;
import com.edu.english.magicmelody.utils.LessonManager;
import com.edu.english.magicmelody.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for AdventureActivity (World Map)
 * Manages level data and progress
 */
public class AdventureViewModel extends AndroidViewModel {
    
    private final ProgressRepository progressRepository;
    private final LessonManager lessonManager;
    private final PreferencesManager preferencesManager;
    private final ExecutorService executor;
    
    private final MutableLiveData<List<LevelItem>> levelsLiveData = new MutableLiveData<>();
    private final MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();
    private LiveData<Integer> totalStarsLiveData;
    
    public AdventureViewModel(@NonNull Application application) {
        super(application);
        Log.d("AdventureViewModel", "Constructor started");
        try {
            progressRepository = new ProgressRepository(application);
            Log.d("AdventureViewModel", "ProgressRepository created");
            lessonManager = LessonManager.getInstance(application);
            Log.d("AdventureViewModel", "LessonManager obtained");
            preferencesManager = PreferencesManager.getInstance(application);
            Log.d("AdventureViewModel", "PreferencesManager obtained");
            executor = Executors.newSingleThreadExecutor();
            
            totalStarsLiveData = progressRepository.getTotalStars();
            Log.d("AdventureViewModel", "LiveData obtained");
            
            loadUserProfile();
            initializeLevels();
            Log.d("AdventureViewModel", "Constructor completed");
        } catch (Exception e) {
            Log.e("AdventureViewModel", "Error in constructor", e);
            throw e;
        }
    }
    
    private void loadUserProfile() {
        UserProfile profile = preferencesManager.getUserProfile();
        userProfileLiveData.setValue(profile);
    }
    
    /**
     * Initialize levels and progress
     */
    public void initializeLevels() {
        executor.execute(() -> {
            try {
                Log.d("AdventureViewModel", "initializeLevels started on background thread");
                List<Lesson> lessons = lessonManager.loadAllLessons();
                Log.d("AdventureViewModel", "Lessons loaded: " + (lessons != null ? lessons.size() : "null"));
                
                String[] lessonIds = new String[lessons.size()];
                for (int i = 0; i < lessons.size(); i++) {
                    lessonIds[i] = lessons.get(i).getLessonId();
                }
                
                // Initialize progress for all lessons
                progressRepository.initializeProgress(lessonIds);
                Log.d("AdventureViewModel", "Progress initialized");
                
                // Wait a bit for DB to update
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Load progress and create level items
                List<ProgressEntity> progressList = progressRepository.getAllOrderedSync();
                Log.d("AdventureViewModel", "Progress loaded: " + (progressList != null ? progressList.size() : "null"));
                List<LevelItem> levelItems = new ArrayList<>();
                
                for (int i = 0; i < lessons.size(); i++) {
                    Lesson lesson = lessons.get(i);
                    ProgressEntity progress = findProgress(progressList, lesson.getLessonId());
                    
                    LevelItem item = new LevelItem();
                    item.setLessonId(lesson.getLessonId());
                    item.setTitle(lesson.getTitle());
                    item.setTitleVietnamese(lesson.getTitleVietnamese());
                    item.setTheme(lesson.getTheme());
                    item.setLevelNumber(i + 1);
                    
                    if (progress != null) {
                        item.setUnlocked(progress.isUnlocked());
                        item.setStars(progress.getStars());
                        item.setBestScore(progress.getBestScore());
                    } else {
                        item.setUnlocked(i == 0); // First level unlocked by default
                        item.setStars(0);
                        item.setBestScore(0);
                    }
                    
                    levelItems.add(item);
                }
                
                Log.d("AdventureViewModel", "Level items created: " + levelItems.size());
                levelsLiveData.postValue(levelItems);
            } catch (Exception e) {
                Log.e("AdventureViewModel", "Error in initializeLevels", e);
            }
        });
    }
    
    private ProgressEntity findProgress(List<ProgressEntity> list, String lessonId) {
        if (list == null) return null;
        for (ProgressEntity p : list) {
            if (p.getLessonId().equals(lessonId)) {
                return p;
            }
        }
        return null;
    }
    
    public LiveData<List<LevelItem>> getLevels() {
        return levelsLiveData;
    }
    
    public LiveData<UserProfile> getUserProfile() {
        return userProfileLiveData;
    }
    
    public LiveData<Integer> getTotalStars() {
        return totalStarsLiveData;
    }
    
    /**
     * Refresh levels after returning from gameplay
     */
    public void refreshLevels() {
        initializeLevels();
    }
    
    /**
     * Level item for RecyclerView
     */
    public static class LevelItem {
        private String lessonId;
        private String title;
        private String titleVietnamese;
        private String theme;
        private int levelNumber;
        private boolean unlocked;
        private int stars;
        private int bestScore;
        
        // Getters and Setters
        public String getLessonId() { return lessonId; }
        public void setLessonId(String lessonId) { this.lessonId = lessonId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getTitleVietnamese() { return titleVietnamese; }
        public void setTitleVietnamese(String titleVietnamese) { this.titleVietnamese = titleVietnamese; }
        
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public int getLevelNumber() { return levelNumber; }
        public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
        
        public boolean isUnlocked() { return unlocked; }
        public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
        
        public int getStars() { return stars; }
        public void setStars(int stars) { this.stars = stars; }
        
        public int getBestScore() { return bestScore; }
        public void setBestScore(int bestScore) { this.bestScore = bestScore; }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
