package com.edu.english.magicmelody.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.edu.english.magicmelody.data.entity.UserProfile;

import java.util.List;

/**
 * 👤 UserProfile DAO
 * 
 * Purpose: Data Access Object for UserProfile entity
 * Provides methods to query and manipulate user profile data
 */
@Dao
public interface UserProfileDao {
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 QUERY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get user profile by ID
     */
    @Query("SELECT * FROM user_profiles WHERE user_id = :userId")
    LiveData<UserProfile> getUserById(String userId);
    
    /**
     * Get user profile by ID (synchronous)
     */
    @Query("SELECT * FROM user_profiles WHERE user_id = :userId")
    UserProfile getUserByIdSync(String userId);
    
    /**
     * Get the current active user (most recently updated)
     */
    @Query("SELECT * FROM user_profiles ORDER BY last_played_at DESC LIMIT 1")
    LiveData<UserProfile> getCurrentUser();
    
    /**
     * Get the current active user (synchronous)
     */
    @Query("SELECT * FROM user_profiles ORDER BY last_played_at DESC LIMIT 1")
    UserProfile getCurrentUserSync();
    
    /**
     * Get all user profiles
     */
    @Query("SELECT * FROM user_profiles ORDER BY created_at DESC")
    LiveData<List<UserProfile>> getAllProfiles();
    
    /**
     * Get all user profiles (synchronous)
     */
    @Query("SELECT * FROM user_profiles ORDER BY created_at DESC")
    List<UserProfile> getAllProfilesSync();
    
    /**
     * Get users by age group
     */
    @Query("SELECT * FROM user_profiles WHERE age_group = :ageGroup")
    LiveData<List<UserProfile>> getUsersByAgeGroup(String ageGroup);
    
    /**
     * Get total star count for a user
     */
    @Query("SELECT total_stars FROM user_profiles WHERE user_id = :userId")
    LiveData<Integer> getTotalStars(String userId);
    
    /**
     * Get total star count (synchronous)
     */
    @Query("SELECT total_stars FROM user_profiles WHERE user_id = :userId")
    int getTotalStarsSync(String userId);
    
    /**
     * Check if any profile exists
     */
    @Query("SELECT COUNT(*) > 0 FROM user_profiles")
    boolean hasAnyProfile();
    
    /**
     * Get count of profiles
     */
    @Query("SELECT COUNT(*) FROM user_profiles")
    int getProfileCount();
    
    // ═══════════════════════════════════════════════════════════════
    // ✏️ INSERT METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Insert a new user profile
     * Returns the new userId
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserProfile profile);
    
    /**
     * Insert multiple profiles
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<UserProfile> profiles);
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 UPDATE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Update an existing profile
     */
    @Update
    void update(UserProfile profile);
    
    /**
     * Update user's total stars
     */
    @Query("UPDATE user_profiles SET total_stars = total_stars + :starsToAdd WHERE user_id = :userId")
    void addStars(String userId, int starsToAdd);
    
    /**
     * Update user's current world
     */
    @Query("UPDATE user_profiles SET current_world = :worldId WHERE user_id = :userId")
    void updateCurrentWorld(String userId, String worldId);
    
    /**
     * Update last played time
     */
    @Query("UPDATE user_profiles SET last_played_at = :timestamp WHERE user_id = :userId")
    void updateLastPlayed(String userId, long timestamp);
    
    /**
     * Update age group
     */
    @Query("UPDATE user_profiles SET age_group = :ageGroup WHERE user_id = :userId")
    void updateAgeGroup(String userId, String ageGroup);
    
    /**
     * Update preferred theme
     */
    @Query("UPDATE user_profiles SET preferred_theme = :theme WHERE user_id = :userId")
    void updatePreferredTheme(String userId, String theme);

    /**
     * Add experience points to user
     */
    @Query("UPDATE user_profiles SET experience_points = experience_points + :xp WHERE user_id = :userId")
    void addExperience(String userId, int xp);

    /**
     * Update streak days for user
     */
    @Query("UPDATE user_profiles SET streak_days = :streak WHERE user_id = :userId")
    void updateStreak(String userId, int streak);

    // ═══════════════════════════════════════════════════════════════
    // 🗑️ DELETE METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Delete a profile
     */
    @Delete
    void delete(UserProfile profile);
    
    /**
     * Delete profile by ID
     */
    @Query("DELETE FROM user_profiles WHERE user_id = :userId")
    void deleteById(String userId);
    
    /**
     * Delete all profiles (reset)
     */
    @Query("DELETE FROM user_profiles")
    void deleteAll();
}
