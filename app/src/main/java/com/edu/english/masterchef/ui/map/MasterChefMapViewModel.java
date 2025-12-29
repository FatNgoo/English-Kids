package com.edu.english.masterchef.ui.map;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.masterchef.data.model.LevelConfig;
import com.edu.english.masterchef.data.model.PlayerProgress;
import com.edu.english.masterchef.data.repository.LevelRepository;
import com.edu.english.masterchef.data.repository.ProgressRepository;

import java.util.List;

/**
 * ViewModel for MasterChefMapActivity
 * Manages level data and player progress
 */
public class MasterChefMapViewModel extends AndroidViewModel {

    private LevelRepository levelRepository;
    private ProgressRepository progressRepository;

    private MutableLiveData<List<LevelConfig>> levels = new MutableLiveData<>();
    private MutableLiveData<PlayerProgress> progress = new MutableLiveData<>();
    private MutableLiveData<LevelConfig> selectedLevel = new MutableLiveData<>();

    public MasterChefMapViewModel(@NonNull Application application) {
        super(application);
        levelRepository = LevelRepository.getInstance();
        progressRepository = ProgressRepository.getInstance(application);
    }

    /**
     * Load all levels
     */
    public void loadLevels() {
        List<LevelConfig> allLevels = levelRepository.getAllLevels();
        levels.setValue(allLevels);
        refreshProgress();
    }

    /**
     * Refresh player progress
     */
    public void refreshProgress() {
        PlayerProgress currentProgress = progressRepository.getProgress();
        progress.setValue(currentProgress);
    }

    /**
     * Select a level (triggers level intro dialog)
     */
    public void selectLevel(LevelConfig level) {
        selectedLevel.setValue(level);
    }

    /**
     * Check if level is unlocked
     */
    public boolean isLevelUnlocked(int levelId) {
        PlayerProgress currentProgress = progress.getValue();
        if (currentProgress == null) {
            return levelId == 1; // First level always unlocked
        }
        return currentProgress.isLevelUnlocked(levelId);
    }

    /**
     * Get stars for a level
     */
    public int getLevelStars(int levelId) {
        PlayerProgress currentProgress = progress.getValue();
        if (currentProgress == null) {
            return 0;
        }
        return currentProgress.getLevelStars(levelId);
    }

    // LiveData getters
    public LiveData<List<LevelConfig>> getLevels() {
        return levels;
    }

    public LiveData<PlayerProgress> getProgress() {
        return progress;
    }

    public LiveData<LevelConfig> getSelectedLevel() {
        return selectedLevel;
    }
}
