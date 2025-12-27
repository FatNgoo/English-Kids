package com.edu.english.magicmelody.ui.worldmap;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.magicmelody.data.entity.UserProfile;
import com.edu.english.magicmelody.data.entity.WorldProgress;
import com.edu.english.magicmelody.data.repository.AssetDataRepository;
import com.edu.english.magicmelody.data.repository.LessonProgressRepository;
import com.edu.english.magicmelody.data.repository.UserProfileRepository;
import com.edu.english.magicmelody.data.repository.WorldProgressRepository;
import com.edu.english.magicmelody.model.LessonConfig;
import com.edu.english.magicmelody.model.ThemeConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 🌍 World Map ViewModel
 * 
 * Purpose: Handle world map screen logic
 * - Display worlds with unlock status
 * - Track evolution stages
 * - Handle world/lesson selection
 */
public class WorldMapViewModel extends AndroidViewModel {
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 DEPENDENCIES
    // ═══════════════════════════════════════════════════════════════
    
    private final UserProfileRepository userProfileRepository;
    private final WorldProgressRepository worldProgressRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final AssetDataRepository assetDataRepository;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 LIVE DATA
    // ═══════════════════════════════════════════════════════════════
    
    private final LiveData<UserProfile> currentUser;
    private final MediatorLiveData<List<WorldDisplayItem>> worldItems = new MediatorLiveData<>();
    private final MutableLiveData<WorldDisplayItem> selectedWorld = new MutableLiveData<>();
    private final MutableLiveData<List<LessonDisplayItem>> lessonsForWorld = new MutableLiveData<>();
    private final MutableLiveData<NavigationEvent> navigationEvent = new MutableLiveData<>();
    
    private long userId = -1;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 DISPLAY MODELS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * World display item for UI
     */
    public static class WorldDisplayItem {
        public final String worldId;
        public final String displayName;
        public final String description;
        public final int evolutionStage;
        public final float saturation;
        public final boolean isUnlocked;
        public final boolean isCompleted;
        public final boolean bossDefeated;
        public final int totalStars;
        public final int maxStars;
        public final int levelsCompleted;
        public final int totalLevels;
        public final ThemeConfig theme;
        
        public WorldDisplayItem(WorldProgress progress, ThemeConfig theme, int totalLevels, int maxStars) {
            this.worldId = progress.getWorldId();
            this.displayName = theme != null ? theme.getDisplayName() : progress.getWorldId();
            this.description = theme != null ? theme.getDescription() : "";
            this.evolutionStage = progress.getEvolutionStage();
            this.saturation = progress.getSaturationLevel();
            this.isUnlocked = progress.isUnlocked();
            this.isCompleted = progress.isCompleted();
            this.bossDefeated = progress.isBossDefeated();
            this.totalStars = progress.getTotalStars();
            this.maxStars = maxStars;
            this.levelsCompleted = progress.getLevelsCompleted();
            this.totalLevels = totalLevels;
            this.theme = theme;
        }
        
        // Getter methods for WorldAdapter compatibility
        public String getWorldId() {
            return worldId;
        }
        
        public String getName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getEvolutionStage() {
            return evolutionStage;
        }
        
        public boolean isUnlocked() {
            return isUnlocked;
        }
        
        public boolean isCompleted() {
            return isCompleted;
        }
        
        public int getCompletedLessons() {
            return levelsCompleted;
        }
        
        public int getTotalLessons() {
            return totalLevels;
        }
        
        public int getTotalStars() {
            return totalStars;
        }
        
        public int getMaxStars() {
            return maxStars;
        }
    }
    
    /**
     * Lesson display item for UI
     */
    public static class LessonDisplayItem {
        public final String lessonId;
        public final String title;
        public final int level;
        public final int difficulty;
        public final boolean isBossLevel;
        public final int starsEarned;
        public final boolean isCompleted;
        public final boolean isUnlocked;
        public final int bestScore;
        
        public LessonDisplayItem(LessonConfig config, int starsEarned, boolean isCompleted, 
                                 boolean isUnlocked, int bestScore) {
            this.lessonId = config.getLessonId();
            this.title = config.getTitle();
            this.level = config.getLevel();
            this.difficulty = config.getDifficulty();
            this.isBossLevel = config.isBossLevel();
            this.starsEarned = starsEarned;
            this.isCompleted = isCompleted;
            this.isUnlocked = isUnlocked;
            this.bestScore = bestScore;
        }
        
        public String getLessonId() {
            return lessonId;
        }
        
        public String getTitle() {
            return title;
        }
        
        public boolean isUnlocked() {
            return isUnlocked;
        }
        
        public int getLessonNumber() {
            return level;
        }
        
        public int getStars() {
            return starsEarned;
        }
        
        public int getDifficulty() {
            return difficulty;
        }
        
        public boolean isCompleted() {
            return isCompleted;
        }
        
        public boolean isBossLevel() {
            return isBossLevel;
        }
        
        public int getBestScore() {
            return bestScore;
        }
    }
    
    /**
     * Navigation events
     */
    public static class NavigationEvent {
        public enum Type {
            GO_TO_LESSON,
            GO_TO_BOSS,
            GO_TO_NOTEBOOK,
            SHOW_LOCKED_MESSAGE
        }
        
        public final Type type;
        public final String targetId;
        public final String worldId;
        
        public NavigationEvent(Type type, String targetId, String worldId) {
            this.type = type;
            this.targetId = targetId;
            this.worldId = worldId;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public WorldMapViewModel(@NonNull Application application) {
        super(application);
        
        userProfileRepository = new UserProfileRepository(application);
        worldProgressRepository = new WorldProgressRepository(application);
        lessonProgressRepository = new LessonProgressRepository(application);
        assetDataRepository = new AssetDataRepository(application);
        
        currentUser = userProfileRepository.getCurrentUser();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public LiveData<UserProfile> getCurrentUser() {
        return currentUser;
    }
    
    public LiveData<List<WorldDisplayItem>> getWorldItems() {
        return worldItems;
    }
    
    public LiveData<WorldDisplayItem> getSelectedWorld() {
        return selectedWorld;
    }
    
    public LiveData<List<LessonDisplayItem>> getLessonsForWorld() {
        return lessonsForWorld;
    }
    
    public LiveData<NavigationEvent> getNavigationEvent() {
        return navigationEvent;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 ACTIONS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Initialize with user ID
     */
    public void initialize(long userId) {
        this.userId = userId;
        loadWorlds();
    }
    
    /**
     * Load all worlds
     */
    public void loadWorlds() {
        if (userId < 0) return;
        
        LiveData<List<WorldProgress>> worldsLiveData = worldProgressRepository.getAllWorldsForUser(userId);
        
        worldItems.addSource(worldsLiveData, worldProgresses -> {
            if (worldProgresses == null) return;
            
            List<WorldDisplayItem> items = new ArrayList<>();
            
            for (WorldProgress progress : worldProgresses) {
                ThemeConfig theme = assetDataRepository.getTheme(progress.getWorldId());
                int totalLevels = assetDataRepository.getTotalLevelsInWorld(progress.getWorldId());
                int maxStars = totalLevels * 3;
                
                items.add(new WorldDisplayItem(progress, theme, totalLevels, maxStars));
            }
            
            worldItems.setValue(items);
        });
    }
    
    /**
     * Select a world to view lessons
     */
    public void selectWorld(WorldDisplayItem world) {
        if (!world.isUnlocked) {
            navigationEvent.setValue(new NavigationEvent(
                NavigationEvent.Type.SHOW_LOCKED_MESSAGE, 
                world.worldId, 
                world.worldId
            ));
            return;
        }
        
        selectedWorld.setValue(world);
        loadLessonsForWorld(world.worldId);
    }
    
    /**
     * Load lessons for a world
     */
    public void loadLessonsForWorld(String worldId) {
        if (userId < 0) return;
        
        UserProfile user = currentUser.getValue();
        String ageGroup = user != null ? user.getAgeGroup() : "5-6";
        
        List<LessonConfig> lessonConfigs = assetDataRepository.getLessonsForWorldAndAge(worldId, ageGroup);
        List<LessonDisplayItem> items = new ArrayList<>();
        
        // Background thread for database access
        new Thread(() -> {
            boolean previousCompleted = true; // First lesson is always unlocked
            
            for (LessonConfig config : lessonConfigs) {
                var progress = lessonProgressRepository.getProgressSync(config.getLessonId(), userId);
                
                int stars = progress != null ? progress.getStarsEarned() : 0;
                boolean completed = progress != null && progress.isCompleted();
                int bestScore = progress != null ? progress.getBestScore() : 0;
                boolean unlocked = previousCompleted;
                
                items.add(new LessonDisplayItem(config, stars, completed, unlocked, bestScore));
                
                previousCompleted = completed;
            }
            
            lessonsForWorld.postValue(items);
        }).start();
    }
    
    /**
     * Select a lesson to play
     */
    public void selectLesson(LessonDisplayItem lesson) {
        if (!lesson.isUnlocked) {
            navigationEvent.setValue(new NavigationEvent(
                NavigationEvent.Type.SHOW_LOCKED_MESSAGE,
                lesson.lessonId,
                selectedWorld.getValue().worldId
            ));
            return;
        }
        
        NavigationEvent.Type type = lesson.isBossLevel 
            ? NavigationEvent.Type.GO_TO_BOSS 
            : NavigationEvent.Type.GO_TO_LESSON;
            
        navigationEvent.setValue(new NavigationEvent(
            type,
            lesson.lessonId,
            selectedWorld.getValue().worldId
        ));
    }
    
    /**
     * Open Magic Notebook
     */
    public void openNotebook() {
        navigationEvent.setValue(new NavigationEvent(
            NavigationEvent.Type.GO_TO_NOTEBOOK,
            null,
            null
        ));
    }
    
    /**
     * Clear navigation event after handling
     */
    public void onNavigationHandled() {
        navigationEvent.setValue(null);
    }
    
    /**
     * Close world detail panel
     */
    public void closeWorldDetail() {
        selectedWorld.setValue(null);
        lessonsForWorld.setValue(null);
    }
}
