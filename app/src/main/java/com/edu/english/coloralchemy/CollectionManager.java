package com.edu.english.coloralchemy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection Manager
 * Manages the color collection board and persistence
 */
public class CollectionManager {
    
    private static final String PREFS_NAME = "color_alchemy_collection";
    private static final String KEY_COLORS = "collected_colors";
    private static final String KEY_TOTAL_MIXES = "total_mixes";
    private static final String KEY_ACHIEVEMENTS = "achievements";
    
    private Context context;
    private SharedPreferences prefs;
    
    // Collected colors
    private List<CollectedColor> collectedColors;
    
    // Stats
    private int totalMixes;
    
    // Callback
    private OnCollectionUpdateListener listener;
    
    /**
     * Represents a collected color
     */
    public static class CollectedColor {
        public int color;
        public String name;
        public float shade; // -1 to 1
        public long timestamp;
        public boolean isNew;
        
        public CollectedColor(int color, String name, float shade) {
            this.color = color;
            this.name = name;
            this.shade = shade;
            this.timestamp = System.currentTimeMillis();
            this.isNew = true;
        }
        
        public String getDisplayName() {
            return name;
        }
        
        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("color", color);
            json.put("name", name);
            json.put("shade", shade);
            json.put("timestamp", timestamp);
            return json;
        }
        
        public static CollectedColor fromJson(JSONObject json) throws JSONException {
            CollectedColor cc = new CollectedColor(
                json.getInt("color"),
                json.getString("name"),
                (float) json.getDouble("shade")
            );
            cc.timestamp = json.getLong("timestamp");
            cc.isNew = false;
            return cc;
        }
    }
    
    /**
     * Listener for collection updates
     */
    public interface OnCollectionUpdateListener {
        void onColorCollected(CollectedColor color);
        void onAchievementUnlocked(String achievement);
    }
    
    public CollectionManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.collectedColors = new ArrayList<>();
        
        loadCollection();
    }
    
    /**
     * Load collection from SharedPreferences
     */
    private void loadCollection() {
        collectedColors.clear();
        
        String colorsJson = prefs.getString(KEY_COLORS, "[]");
        totalMixes = prefs.getInt(KEY_TOTAL_MIXES, 0);
        
        try {
            JSONArray jsonArray = new JSONArray(colorsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject colorJson = jsonArray.getJSONObject(i);
                collectedColors.add(CollectedColor.fromJson(colorJson));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Save collection to SharedPreferences
     */
    private void saveCollection() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (CollectedColor cc : collectedColors) {
                jsonArray.put(cc.toJson());
            }
            
            prefs.edit()
                .putString(KEY_COLORS, jsonArray.toString())
                .putInt(KEY_TOTAL_MIXES, totalMixes)
                .apply();
                
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Add a new color to collection
     */
    public boolean addColor(int color, String name, float shade) {
        // Check if already exists
        for (CollectedColor cc : collectedColors) {
            if (cc.name.equals(name) && Math.abs(cc.shade - shade) < 0.1f) {
                return false; // Already collected
            }
        }
        
        CollectedColor newColor = new CollectedColor(color, name, shade);
        collectedColors.add(newColor);
        totalMixes++;
        
        saveCollection();
        
        // Notify listener
        if (listener != null) {
            listener.onColorCollected(newColor);
        }
        
        // Check achievements
        checkAchievements();
        
        return true;
    }
    
    /**
     * Add color from mix result
     */
    public boolean addColorFromMix(int resultColor, String colorName) {
        return addColor(resultColor, colorName, 0);
    }
    
    /**
     * Add color shade variation
     */
    public boolean addColorShade(int shadedColor, String baseColorName, float shadeValue) {
        String shadeName = ColorMixer.getShadeName(baseColorName, shadeValue);
        return addColor(shadedColor, shadeName, shadeValue);
    }
    
    /**
     * Check if a color has been collected
     */
    public boolean hasColor(String name) {
        for (CollectedColor cc : collectedColors) {
            if (cc.name.equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if a specific shade has been collected
     */
    public boolean hasShade(String baseName, float shade) {
        String shadeName = ColorMixer.getShadeName(baseName, shade);
        return hasColor(shadeName);
    }
    
    /**
     * Get all collected colors
     */
    public List<CollectedColor> getCollectedColors() {
        return new ArrayList<>(collectedColors);
    }
    
    /**
     * Get collected color count
     */
    public int getCollectedCount() {
        return collectedColors.size();
    }
    
    /**
     * Get total possible colors (including shades)
     */
    public int getTotalPossibleColors() {
        // 3 primary + 3 secondary + (6 colors × 2 shades each) = 18
        return 18;
    }
    
    /**
     * Get collection progress as percentage
     */
    public float getProgress() {
        return (float) collectedColors.size() / getTotalPossibleColors();
    }
    
    /**
     * Get total mixes performed
     */
    public int getTotalMixes() {
        return totalMixes;
    }
    
    /**
     * Check and unlock achievements
     */
    private void checkAchievements() {
        SharedPreferences achievementPrefs = context.getSharedPreferences(KEY_ACHIEVEMENTS, Context.MODE_PRIVATE);
        
        // First Mix achievement
        if (totalMixes == 1 && !achievementPrefs.getBoolean("first_mix", false)) {
            achievementPrefs.edit().putBoolean("first_mix", true).apply();
            if (listener != null) {
                listener.onAchievementUnlocked("First Mix!");
            }
        }
        
        // All Secondaries achievement
        if (hasAllSecondaryColors() && !achievementPrefs.getBoolean("all_secondary", false)) {
            achievementPrefs.edit().putBoolean("all_secondary", true).apply();
            if (listener != null) {
                listener.onAchievementUnlocked("Color Expert!");
            }
        }
        
        // Shade Master achievement
        int shadeCount = countShades();
        if (shadeCount >= 6 && !achievementPrefs.getBoolean("shade_master", false)) {
            achievementPrefs.edit().putBoolean("shade_master", true).apply();
            if (listener != null) {
                listener.onAchievementUnlocked("Shade Master!");
            }
        }
        
        // Collection Complete achievement
        if (collectedColors.size() >= getTotalPossibleColors() && 
            !achievementPrefs.getBoolean("collection_complete", false)) {
            achievementPrefs.edit().putBoolean("collection_complete", true).apply();
            if (listener != null) {
                listener.onAchievementUnlocked("Collection Complete!");
            }
        }
    }
    
    /**
     * Check if all secondary colors are collected
     */
    private boolean hasAllSecondaryColors() {
        return hasColor("Purple") && hasColor("Orange") && hasColor("Green");
    }
    
    /**
     * Count shade variations collected
     */
    private int countShades() {
        int count = 0;
        for (CollectedColor cc : collectedColors) {
            if (cc.name.startsWith("Light ") || cc.name.startsWith("Dark ")) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Mark all colors as not new (after viewing collection)
     */
    public void markAllAsSeen() {
        for (CollectedColor cc : collectedColors) {
            cc.isNew = false;
        }
    }
    
    /**
     * Count new colors
     */
    public int getNewColorCount() {
        int count = 0;
        for (CollectedColor cc : collectedColors) {
            if (cc.isNew) count++;
        }
        return count;
    }
    
    /**
     * Reset collection (for testing)
     */
    public void resetCollection() {
        collectedColors.clear();
        totalMixes = 0;
        
        prefs.edit()
            .remove(KEY_COLORS)
            .remove(KEY_TOTAL_MIXES)
            .apply();
            
        context.getSharedPreferences(KEY_ACHIEVEMENTS, Context.MODE_PRIVATE)
            .edit().clear().apply();
    }
    
    /**
     * Set listener for collection updates
     */
    public void setListener(OnCollectionUpdateListener listener) {
        this.listener = listener;
    }
    
    /**
     * Get colors organized by type for display
     */
    public List<List<CollectedColor>> getOrganizedCollection() {
        List<List<CollectedColor>> organized = new ArrayList<>();
        
        // Primary colors
        List<CollectedColor> primary = new ArrayList<>();
        // Secondary colors
        List<CollectedColor> secondary = new ArrayList<>();
        // Light shades
        List<CollectedColor> light = new ArrayList<>();
        // Dark shades
        List<CollectedColor> dark = new ArrayList<>();
        
        for (CollectedColor cc : collectedColors) {
            if (cc.name.startsWith("Light ")) {
                light.add(cc);
            } else if (cc.name.startsWith("Dark ")) {
                dark.add(cc);
            } else if (cc.name.equals("Red") || cc.name.equals("Blue") || cc.name.equals("Yellow")) {
                primary.add(cc);
            } else {
                secondary.add(cc);
            }
        }
        
        organized.add(primary);
        organized.add(secondary);
        organized.add(light);
        organized.add(dark);
        
        return organized;
    }
}
