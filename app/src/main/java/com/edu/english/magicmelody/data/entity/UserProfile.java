package com.edu.english.magicmelody.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 👤 UserProfile Entity
 * 
 * Purpose: Store user profile information for personalized learning experience
 * 
 * Features:
 * - Age group determines lesson difficulty and content
 * - Preferred theme affects visual presentation
 * - Tracks total stars, current progress, and collected notes
 * 
 * Room annotations:
 * - @Entity: Marks this as a Room database table
 * - @PrimaryKey: Unique identifier for each user
 * - @ColumnInfo: Custom column names for clarity
 */
@Entity(tableName = "user_profiles")
public class UserProfile {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;
    
    @ColumnInfo(name = "display_name")
    private String displayName;
    
    @ColumnInfo(name = "avatar_id")
    private String avatarId;
    
    /**
     * Age group determines:
     * - Lesson content complexity
     * - BPM (tempo) range
     * - Word difficulty
     * Values: "5-6", "7-8", "9-10"
     */
    @ColumnInfo(name = "age_group")
    private String ageGroup;
    
    /**
     * Preferred theme for visual customization
     * Values: "forest", "ocean", "mountain", "desert", "sky"
     */
    @ColumnInfo(name = "preferred_theme")
    private String preferredTheme;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 PROGRESS TRACKING
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "total_stars")
    private int totalStars;
    
    @ColumnInfo(name = "current_world")
    private String currentWorld;
    
    @ColumnInfo(name = "current_level")
    private int currentLevel;
    
    @ColumnInfo(name = "highest_combo")
    private int highestCombo;
    
    @ColumnInfo(name = "total_notes_hit")
    private int totalNotesHit;
    
    @ColumnInfo(name = "perfect_count")
    private int perfectCount;
    
    // ═══════════════════════════════════════════════════════════════
    // 📚 COLLECTION TRACKING
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "collected_notes_count")
    private int collectedNotesCount;
    
    @ColumnInfo(name = "bosses_defeated")
    private int bossesDefeated;
    
    @ColumnInfo(name = "worlds_completed")
    private int worldsCompleted;
    
    // ═══════════════════════════════════════════════════════════════
    // 🎯 EXPERIENCE & STREAK
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "experience_points")
    private int experiencePoints;
    
    @ColumnInfo(name = "streak_days")
    private int streakDays;
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ SETTINGS
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "sound_enabled")
    private boolean soundEnabled;
    
    @ColumnInfo(name = "music_enabled")
    private boolean musicEnabled;
    
    @ColumnInfo(name = "voice_guide_enabled")
    private boolean voiceGuideEnabled;
    
    // ═══════════════════════════════════════════════════════════════
    // 🕐 TIMESTAMPS
    // ═══════════════════════════════════════════════════════════════
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "last_played_at")
    private long lastPlayedAt;
    
    @ColumnInfo(name = "total_play_time_minutes")
    private int totalPlayTimeMinutes;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public UserProfile() {
        // Default constructor for Room
    }
    
    /**
     * Create a new user profile with default settings
     */
    public static UserProfile createNew(String userId, String displayName, String ageGroup) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setDisplayName(displayName);
        profile.setAgeGroup(ageGroup);
        profile.setPreferredTheme("forest"); // Default theme
        profile.setCurrentWorld("forest");
        profile.setCurrentLevel(1);
        profile.setTotalStars(0);
        profile.setSoundEnabled(true);
        profile.setMusicEnabled(true);
        profile.setVoiceGuideEnabled(true);
        profile.setCreatedAt(System.currentTimeMillis());
        profile.setLastPlayedAt(System.currentTimeMillis());
        return profile;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    public String getPreferredTheme() {
        return preferredTheme;
    }

    public void setPreferredTheme(String preferredTheme) {
        this.preferredTheme = preferredTheme;
    }

    public int getTotalStars() {
        return totalStars;
    }

    public void setTotalStars(int totalStars) {
        this.totalStars = totalStars;
    }

    public String getCurrentWorld() {
        return currentWorld;
    }

    public void setCurrentWorld(String currentWorld) {
        this.currentWorld = currentWorld;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getHighestCombo() {
        return highestCombo;
    }

    public void setHighestCombo(int highestCombo) {
        this.highestCombo = highestCombo;
    }

    public int getTotalNotesHit() {
        return totalNotesHit;
    }

    public void setTotalNotesHit(int totalNotesHit) {
        this.totalNotesHit = totalNotesHit;
    }

    public int getPerfectCount() {
        return perfectCount;
    }

    public void setPerfectCount(int perfectCount) {
        this.perfectCount = perfectCount;
    }

    public int getCollectedNotesCount() {
        return collectedNotesCount;
    }

    public void setCollectedNotesCount(int collectedNotesCount) {
        this.collectedNotesCount = collectedNotesCount;
    }

    public int getBossesDefeated() {
        return bossesDefeated;
    }

    public void setBossesDefeated(int bossesDefeated) {
        this.bossesDefeated = bossesDefeated;
    }

    public int getWorldsCompleted() {
        return worldsCompleted;
    }

    public void setWorldsCompleted(int worldsCompleted) {
        this.worldsCompleted = worldsCompleted;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(int streakDays) {
        this.streakDays = streakDays;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }

    public boolean isVoiceGuideEnabled() {
        return voiceGuideEnabled;
    }

    public void setVoiceGuideEnabled(boolean voiceGuideEnabled) {
        this.voiceGuideEnabled = voiceGuideEnabled;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastPlayedAt() {
        return lastPlayedAt;
    }

    public void setLastPlayedAt(long lastPlayedAt) {
        this.lastPlayedAt = lastPlayedAt;
    }

    public int getTotalPlayTimeMinutes() {
        return totalPlayTimeMinutes;
    }

    public void setTotalPlayTimeMinutes(int totalPlayTimeMinutes) {
        this.totalPlayTimeMinutes = totalPlayTimeMinutes;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Add stars and update last played time
     */
    public void addStars(int stars) {
        this.totalStars += stars;
        this.lastPlayedAt = System.currentTimeMillis();
    }
    
    /**
     * Check if a specific world is unlocked based on stars
     */
    public boolean isWorldUnlocked(String worldId) {
        switch (worldId) {
            case "forest": return true; // Always unlocked
            case "ocean": return totalStars >= 25;
            case "mountain": return totalStars >= 60;
            case "desert": return totalStars >= 100;
            case "sky": return totalStars >= 150;
            default: return false;
        }
    }
    
    /**
     * Get the maximum unlocked world
     */
    public String getMaxUnlockedWorld() {
        if (totalStars >= 150) return "sky";
        if (totalStars >= 100) return "desert";
        if (totalStars >= 60) return "mountain";
        if (totalStars >= 25) return "ocean";
        return "forest";
    }
}
