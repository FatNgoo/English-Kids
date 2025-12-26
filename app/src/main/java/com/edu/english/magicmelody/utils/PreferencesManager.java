package com.edu.english.magicmelody.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.edu.english.magicmelody.data.model.UserProfile;
import com.google.gson.Gson;

/**
 * PreferencesManager - Manages SharedPreferences for Magic Melody
 * Stores user profile, settings, and app state
 */
public class PreferencesManager {
    
    private static final String PREFS_NAME = "magic_melody_prefs";
    private static final String KEY_USER_PROFILE = "user_profile";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_SFX_VOLUME = "sfx_volume";
    private static final String KEY_LAST_PLAYED_LESSON = "last_played_lesson";
    
    private final SharedPreferences prefs;
    private final Gson gson;
    
    private static PreferencesManager instance;
    
    public static PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    // User Profile
    public void saveUserProfile(UserProfile profile) {
        String json = gson.toJson(profile);
        prefs.edit().putString(KEY_USER_PROFILE, json).apply();
    }
    
    public UserProfile getUserProfile() {
        String json = prefs.getString(KEY_USER_PROFILE, null);
        if (json != null) {
            try {
                return gson.fromJson(json, UserProfile.class);
            } catch (Exception e) {
                return new UserProfile();
            }
        }
        return new UserProfile();
    }
    
    // First launch
    public boolean isFirstLaunch() {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }
    
    public void setFirstLaunchComplete() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }
    
    // Sound settings
    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true);
    }
    
    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
    }
    
    public float getMusicVolume() {
        return prefs.getFloat(KEY_MUSIC_VOLUME, 0.8f);
    }
    
    public void setMusicVolume(float volume) {
        prefs.edit().putFloat(KEY_MUSIC_VOLUME, volume).apply();
    }
    
    public float getSfxVolume() {
        return prefs.getFloat(KEY_SFX_VOLUME, 1.0f);
    }
    
    public void setSfxVolume(float volume) {
        prefs.edit().putFloat(KEY_SFX_VOLUME, volume).apply();
    }
    
    // Last played lesson
    public String getLastPlayedLesson() {
        return prefs.getString(KEY_LAST_PLAYED_LESSON, null);
    }
    
    public void setLastPlayedLesson(String lessonId) {
        prefs.edit().putString(KEY_LAST_PLAYED_LESSON, lessonId).apply();
    }
    
    // Clear all data
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
