package com.edu.english.magicmelody.data.repository;

import android.content.Context;

import com.edu.english.magicmelody.model.LessonConfig;
import com.edu.english.magicmelody.model.NoteEvent;
import com.edu.english.magicmelody.model.ThemeConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 📦 AssetDataRepository
 * 
 * Purpose: Load game configuration from JSON assets
 * Handles lessons, themes, and other static game data
 */
public class AssetDataRepository {
    
    private static final String LESSONS_PATH = "magicmelody/lessons/lessons.json";
    private static final String THEMES_PATH = "magicmelody/themes/themes.json";
    
    private final Context context;
    private final Gson gson;
    
    // Cache
    private List<LessonConfig> cachedLessons;
    private Map<String, ThemeConfig> cachedThemes;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public AssetDataRepository(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📚 LESSON METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get all lessons
     */
    public List<LessonConfig> getAllLessons() {
        if (cachedLessons != null) {
            return cachedLessons;
        }
        
        try {
            String json = loadJsonFromAssets(LESSONS_PATH);
            Type type = new TypeToken<LessonsWrapper>(){}.getType();
            LessonsWrapper wrapper = gson.fromJson(json, type);
            cachedLessons = wrapper.lessons;
            return cachedLessons;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get lessons for a specific world
     */
    public List<LessonConfig> getLessonsForWorld(String worldId) {
        List<LessonConfig> allLessons = getAllLessons();
        List<LessonConfig> worldLessons = new ArrayList<>();
        
        for (LessonConfig lesson : allLessons) {
            if (worldId.equals(lesson.getWorldId())) {
                worldLessons.add(lesson);
            }
        }
        
        return worldLessons;
    }
    
    /**
     * Get lessons for a specific age group
     */
    public List<LessonConfig> getLessonsForAgeGroup(String ageGroup) {
        List<LessonConfig> allLessons = getAllLessons();
        List<LessonConfig> filteredLessons = new ArrayList<>();
        
        for (LessonConfig lesson : allLessons) {
            if (ageGroup.equals(lesson.getAgeGroup())) {
                filteredLessons.add(lesson);
            }
        }
        
        return filteredLessons;
    }
    
    /**
     * Get lessons for world and age group
     */
    public List<LessonConfig> getLessonsForWorldAndAge(String worldId, String ageGroup) {
        List<LessonConfig> allLessons = getAllLessons();
        List<LessonConfig> filteredLessons = new ArrayList<>();
        
        for (LessonConfig lesson : allLessons) {
            if (worldId.equals(lesson.getWorldId()) && ageGroup.equals(lesson.getAgeGroup())) {
                filteredLessons.add(lesson);
            }
        }
        
        return filteredLessons;
    }
    
    /**
     * Get a specific lesson by ID
     */
    public LessonConfig getLessonById(String lessonId) {
        List<LessonConfig> allLessons = getAllLessons();
        
        for (LessonConfig lesson : allLessons) {
            if (lessonId.equals(lesson.getLessonId())) {
                return lesson;
            }
        }
        
        return null;
    }
    
    /**
     * Get boss level for a world
     */
    public LessonConfig getBossLevelForWorld(String worldId) {
        List<LessonConfig> worldLessons = getLessonsForWorld(worldId);
        
        for (LessonConfig lesson : worldLessons) {
            if (lesson.isBossLevel()) {
                return lesson;
            }
        }
        
        return null;
    }
    
    /**
     * Get total levels in a world
     */
    public int getTotalLevelsInWorld(String worldId) {
        return getLessonsForWorld(worldId).size();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 THEME METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get all themes
     */
    public Map<String, ThemeConfig> getAllThemes() {
        if (cachedThemes != null) {
            return cachedThemes;
        }
        
        try {
            String json = loadJsonFromAssets(THEMES_PATH);
            Type type = new TypeToken<ThemesWrapper>(){}.getType();
            ThemesWrapper wrapper = gson.fromJson(json, type);
            
            cachedThemes = new HashMap<>();
            if (wrapper.themes != null) {
                for (ThemeConfig theme : wrapper.themes) {
                    cachedThemes.put(theme.getThemeId(), theme);
                }
            }
            
            return cachedThemes;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    /**
     * Get a specific theme
     */
    public ThemeConfig getTheme(String themeId) {
        return getAllThemes().get(themeId);
    }
    
    /**
     * Get theme list
     */
    public List<ThemeConfig> getThemeList() {
        return new ArrayList<>(getAllThemes().values());
    }
    
    /**
     * Get theme for a world
     */
    public ThemeConfig getThemeForWorld(String worldId) {
        // World ID and Theme ID are the same in our design
        return getTheme(worldId);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔧 UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Load JSON content from assets folder
     */
    private String loadJsonFromAssets(String path) throws IOException {
        InputStream inputStream = context.getAssets().open(path);
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[1024];
        int read;
        
        while ((read = reader.read(buffer)) != -1) {
            stringBuilder.append(buffer, 0, read);
        }
        
        reader.close();
        inputStream.close();
        
        return stringBuilder.toString();
    }
    
    /**
     * Clear cache (call when assets might have changed)
     */
    public void clearCache() {
        cachedLessons = null;
        cachedThemes = null;
    }
    
    /**
     * Preload all data (call during splash screen)
     */
    public void preloadData() {
        getAllLessons();
        getAllThemes();
    }
    
    /**
     * Get audio path for a note
     */
    public String getNoteAudioPath(String noteName) {
        return "magicmelody/audio/notes/" + noteName.toLowerCase() + ".mp3";
    }
    
    /**
     * Get audio path for background music
     */
    public String getBgMusicPath(String worldId) {
        ThemeConfig theme = getTheme(worldId);
        if (theme != null) {
            return theme.getBgMusicPath();
        }
        return "magicmelody/audio/themes/default_bgm.mp3";
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 WRAPPER CLASSES (for GSON)
    // ═══════════════════════════════════════════════════════════════
    
    private static class LessonsWrapper {
        List<LessonConfig> lessons;
    }
    
    private static class ThemesWrapper {
        List<ThemeConfig> themes;
    }
}
