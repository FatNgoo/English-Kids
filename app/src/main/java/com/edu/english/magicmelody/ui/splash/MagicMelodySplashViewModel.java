package com.edu.english.magicmelody.ui.splash;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.magicmelody.data.entity.UserProfile;
import com.edu.english.magicmelody.data.repository.AssetDataRepository;
import com.edu.english.magicmelody.data.repository.UserProfileRepository;
import com.edu.english.magicmelody.data.repository.WorldProgressRepository;

/**
 * 🎬 Magic Melody Splash ViewModel
 * 
 * Purpose: Handle splash screen logic
 * - Check if user profile exists
 * - Preload assets
 * - Determine navigation destination
 */
public class MagicMelodySplashViewModel extends AndroidViewModel {
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 DEPENDENCIES
    // ═══════════════════════════════════════════════════════════════
    
    private final UserProfileRepository userProfileRepository;
    private final WorldProgressRepository worldProgressRepository;
    private final AssetDataRepository assetDataRepository;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 LIVE DATA
    // ═══════════════════════════════════════════════════════════════
    
    private final MutableLiveData<SplashState> splashState = new MutableLiveData<>(SplashState.LOADING);
    private final MutableLiveData<Float> loadingProgress = new MutableLiveData<>(0f);
    private final MutableLiveData<String> loadingMessage = new MutableLiveData<>("Loading...");
    private final MutableLiveData<NavigationTarget> navigationTarget = new MutableLiveData<>();
    
    private UserProfile currentUser;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE ENUM
    // ═══════════════════════════════════════════════════════════════
    
    public enum SplashState {
        LOADING,
        READY,
        ERROR
    }
    
    public enum NavigationTarget {
        AGE_SELECTION,    // New user - show age selection
        WORLD_MAP         // Existing user - go to world map
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public MagicMelodySplashViewModel(@NonNull Application application) {
        super(application);
        
        userProfileRepository = new UserProfileRepository(application);
        worldProgressRepository = new WorldProgressRepository(application);
        assetDataRepository = new AssetDataRepository(application);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public LiveData<SplashState> getSplashState() {
        return splashState;
    }
    
    public LiveData<Float> getLoadingProgress() {
        return loadingProgress;
    }
    
    public LiveData<String> getLoadingMessage() {
        return loadingMessage;
    }
    
    public LiveData<NavigationTarget> getNavigationTarget() {
        return navigationTarget;
    }
    
    public UserProfile getCurrentUser() {
        return currentUser;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 ACTIONS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Start loading sequence
     */
    public void startLoading() {
        splashState.setValue(SplashState.LOADING);
        
        // Run loading in background
        new Thread(() -> {
            try {
                // Step 1: Load assets (40%)
                updateProgress(0.1f, "Loading game assets...");
                assetDataRepository.preloadData();
                updateProgress(0.4f, "Assets loaded!");
                
                // Step 2: Check user profile (60%)
                updateProgress(0.5f, "Checking profile...");
                boolean hasProfile = userProfileRepository.hasAnyProfile();
                
                if (hasProfile) {
                    currentUser = userProfileRepository.getCurrentUserSync();
                    updateProgress(0.6f, "Welcome back!");
                } else {
                    updateProgress(0.6f, "Welcome to Magic Melody!");
                }
                
                // Step 3: Prepare navigation (80%)
                updateProgress(0.8f, "Preparing world...");
                Thread.sleep(300); // Brief pause for UX
                
                // Step 4: Complete (100%)
                updateProgress(1.0f, "Ready!");
                Thread.sleep(500); // Let user see "Ready!"
                
                // Determine navigation
                splashState.postValue(SplashState.READY);
                
                if (hasProfile && currentUser != null) {
                    navigationTarget.postValue(NavigationTarget.WORLD_MAP);
                } else {
                    navigationTarget.postValue(NavigationTarget.AGE_SELECTION);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                splashState.postValue(SplashState.ERROR);
            }
        }).start();
    }
    
    /**
     * Create new user profile
     */
    public void createUser(String ageGroup) {
        userProfileRepository.createNewUser(ageGroup, userId -> {
            // Initialize worlds for new user
            worldProgressRepository.initializeWorldsForUser(userId);
            
            // Navigate to world map
            navigationTarget.postValue(NavigationTarget.WORLD_MAP);
        });
    }
    
    /**
     * Update loading progress (thread-safe)
     */
    private void updateProgress(float progress, String message) {
        loadingProgress.postValue(progress);
        loadingMessage.postValue(message);
    }
    
    /**
     * Retry loading after error
     */
    public void retryLoading() {
        startLoading();
    }
}
