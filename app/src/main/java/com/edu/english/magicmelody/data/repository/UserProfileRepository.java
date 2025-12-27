package com.edu.english.magicmelody.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.edu.english.magicmelody.data.MagicMelodyDatabase;
import com.edu.english.magicmelody.data.dao.UserProfileDao;
import com.edu.english.magicmelody.data.entity.UserProfile;

import java.util.List;

/**
 * 👤 UserProfile Repository
 * 
 * Purpose: Single source of truth for user profile data
 * Abstracts data operations from ViewModels
 */
public class UserProfileRepository {
    
    private final UserProfileDao userProfileDao;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public UserProfileRepository(Application application) {
        MagicMelodyDatabase database = MagicMelodyDatabase.getInstance(application);
        userProfileDao = database.userProfileDao();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 QUERY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get user by ID (LiveData)
     */
    public LiveData<UserProfile> getUserById(long userId) {
        return userProfileDao.getUserById(String.valueOf(userId));
    }
    
    /**
     * Get current active user
     */
    public LiveData<UserProfile> getCurrentUser() {
        return userProfileDao.getCurrentUser();
    }
    
    /**
     * Get current user synchronously (for background work)
     */
    public UserProfile getCurrentUserSync() {
        return userProfileDao.getCurrentUserSync();
    }
    
    /**
     * Get all profiles
     */
    public LiveData<List<UserProfile>> getAllProfiles() {
        return userProfileDao.getAllProfiles();
    }
    
    /**
     * Get total stars for user
     */
    public LiveData<Integer> getTotalStars(long userId) {
        return userProfileDao.getTotalStars(String.valueOf(userId));
    }
    
    /**
     * Check if any profile exists
     */
    public boolean hasAnyProfile() {
        return userProfileDao.hasAnyProfile();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ✏️ INSERT/UPDATE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Insert a new profile
     */
    public void insert(UserProfile profile) {
        MagicMelodyDatabase.runAsync(() -> {
            userProfileDao.insert(profile);
        });
    }
    
    /**
     * Insert and get ID (synchronous - call from background thread)
     */
    public long insertSync(UserProfile profile) {
        return userProfileDao.insert(profile);
    }
    
    /**
     * Update existing profile
     */
    public void update(UserProfile profile) {
        MagicMelodyDatabase.runAsync(() -> {
            userProfileDao.update(profile);
        });
    }
    
    /**
     * Add stars to user's total
     */
    public void addStars(long userId, int starsToAdd) {
        MagicMelodyDatabase.runAsync(() -> {
            userProfileDao.addStars(String.valueOf(userId), starsToAdd);
        });
    }
    
    /**
     * Update current world
     */
    public void updateCurrentWorld(long userId, String worldId) {
        MagicMelodyDatabase.runAsync(() -> {
            userProfileDao.updateCurrentWorld(String.valueOf(userId), worldId);
        });
    }
    
    /**
     * Update last played timestamp
     */
    public void updateLastPlayed(long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            userProfileDao.updateLastPlayed(String.valueOf(userId), System.currentTimeMillis());
        });
    }
    
    /**
     * Add experience points
     */
    public void addExperience(long userId, int xp) {
        MagicMelodyDatabase.runAsync(() -> {
            userProfileDao.addExperience(String.valueOf(userId), xp);
        });
    }
    
    /**
     * Update streak days
     */
    public void updateStreak(long userId, int streak) {
        MagicMelodyDatabase.runAsync(() -> {
            userProfileDao.updateStreak(String.valueOf(userId), streak);
        });
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🗑️ DELETE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Delete profile
     */
    public void delete(UserProfile profile) {
        MagicMelodyDatabase.runAsync(() -> {
            userProfileDao.delete(profile);
        });
    }
    
    /**
     * Delete by ID
     */
    public void deleteById(long userId) {
        MagicMelodyDatabase.runAsync(() -> {
            userProfileDao.deleteById(String.valueOf(userId));
        });
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Create a new user profile with defaults
     */
    public void createNewUser(String ageGroup, OnUserCreatedListener listener) {
        MagicMelodyDatabase.runAsync(() -> {
            String uniqueId = java.util.UUID.randomUUID().toString();
            UserProfile profile = UserProfile.createNew(uniqueId, "Player", ageGroup);
            long insertedId = userProfileDao.insert(profile);
            if (listener != null) {
                listener.onUserCreated(insertedId);
            }
        });
    }
    
    /**
     * Callback interface for user creation
     */
    public interface OnUserCreatedListener {
        void onUserCreated(long userId);
    }
}
