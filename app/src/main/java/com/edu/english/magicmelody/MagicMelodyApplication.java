package com.edu.english.magicmelody;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.edu.english.magicmelody.audio.AudioMixer;
import com.edu.english.magicmelody.data.MagicMelodyDatabase;
import com.edu.english.magicmelody.gameplay.achievement.AchievementManager;
import com.edu.english.magicmelody.gameplay.notebook.MagicNotebookManager;

/**
 * 🎵 Magic Melody Application
 * 
 * Main Application class - Initializes all core components:
 * - Database
 * - Audio system
 * - Achievement tracking
 * - Magic Notebook
 */
public class MagicMelodyApplication extends Application {
    
    private static final String TAG = "MagicMelodyApp";
    private static final String PREFS_NAME = "magic_melody_prefs";
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 SINGLETON INSTANCE
    // ═══════════════════════════════════════════════════════════════
    
    private static MagicMelodyApplication instance;
    
    public static MagicMelodyApplication getInstance() {
        return instance;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 COMPONENTS
    // ═══════════════════════════════════════════════════════════════
    
    private MagicMelodyDatabase database;
    private AudioMixer audioMixer;
    private AchievementManager achievementManager;
    private MagicNotebookManager notebookManager;
    private SharedPreferences preferences;
    
    // Current player session
    private long currentPlayerId = -1;
    private String currentPlayerName = "";
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ LIFECYCLE
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        Log.d(TAG, "🎵 Magic Melody Application Starting...");
        
        // Initialize preferences
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Initialize core components
        initializeDatabase();
        initializeAudio();
        initializeManagers();
        
        // Load saved settings
        loadSettings();
        
        Log.d(TAG, "✅ Magic Melody Application Ready!");
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ INITIALIZATION
    // ═══════════════════════════════════════════════════════════════
    
    private void initializeDatabase() {
        Log.d(TAG, "📊 Initializing Database...");
        database = MagicMelodyDatabase.getInstance(this);
    }
    
    private void initializeAudio() {
        Log.d(TAG, "🔊 Initializing Audio System...");
        audioMixer = AudioMixer.getInstance();
        audioMixer.initialize(this);
        audioMixer.loadSettings(preferences);
    }
    
    private void initializeManagers() {
        Log.d(TAG, "📋 Initializing Managers...");
        
        // Achievement Manager
        achievementManager = new AchievementManager();
        
        // Magic Notebook Manager
        notebookManager = new MagicNotebookManager();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 💾 SETTINGS
    // ═══════════════════════════════════════════════════════════════
    
    private void loadSettings() {
        currentPlayerId = preferences.getLong("current_player_id", -1);
        currentPlayerName = preferences.getString("current_player_name", "");
        
        if (currentPlayerId > 0) {
            Log.d(TAG, "👤 Loaded player: " + currentPlayerName);
            // Player ID is tracked at application level
        }
    }
    
    public void saveSettings() {
        preferences.edit()
            .putLong("current_player_id", currentPlayerId)
            .putString("current_player_name", currentPlayerName)
            .apply();
        
        audioMixer.saveSettings(preferences);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 👤 PLAYER SESSION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Set current player for this session
     */
    public void setCurrentPlayer(long playerId, String playerName) {
        this.currentPlayerId = playerId;
        this.currentPlayerName = playerName;
        
        // Save to preferences
        saveSettings();
        
        Log.d(TAG, "👤 Player set: " + playerName + " (ID: " + playerId + ")");
    }
    
    public long getCurrentPlayerId() {
        return currentPlayerId;
    }
    
    public String getCurrentPlayerName() {
        return currentPlayerName;
    }
    
    public boolean hasActivePlayer() {
        return currentPlayerId > 0;
    }
    
    /**
     * Clear current player session
     */
    public void clearCurrentPlayer() {
        currentPlayerId = -1;
        currentPlayerName = "";
        saveSettings();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public MagicMelodyDatabase getDatabase() {
        return database;
    }
    
    public AudioMixer getAudioMixer() {
        return audioMixer;
    }
    
    public AchievementManager getAchievementManager() {
        return achievementManager;
    }
    
    public MagicNotebookManager getNotebookManager() {
        return notebookManager;
    }
    
    public SharedPreferences getPreferences() {
        return preferences;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 QUICK ACCESS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get database instance statically
     */
    public static MagicMelodyDatabase db() {
        return instance != null ? instance.database : null;
    }
    
    /**
     * Get audio mixer statically
     */
    public static AudioMixer audio() {
        return instance != null ? instance.audioMixer : null;
    }
    
    /**
     * Get achievement manager statically
     */
    public static AchievementManager achievements() {
        return instance != null ? instance.achievementManager : null;
    }
    
    /**
     * Get notebook manager statically
     */
    public static MagicNotebookManager notebook() {
        return instance != null ? instance.notebookManager : null;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 LIFECYCLE CALLBACKS
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        // Save settings
        saveSettings();
        
        // Release audio
        if (audioMixer != null) {
            audioMixer.release();
        }
        
        Log.d(TAG, "👋 Magic Melody Application Terminated");
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "⚠️ Low memory warning");
        
        // Release non-essential resources
        if (audioMixer != null) {
            audioMixer.getSoundPoolManager().unloadUnusedSounds();
        }
    }
    
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        
        if (level >= TRIM_MEMORY_MODERATE) {
            Log.w(TAG, "⚠️ Trim memory: " + level);
            if (audioMixer != null) {
                audioMixer.getSoundPoolManager().unloadUnusedSounds();
            }
        }
    }
}
