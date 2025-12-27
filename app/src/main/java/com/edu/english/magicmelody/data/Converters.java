package com.edu.english.magicmelody.data;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 🔄 Room Type Converters
 * 
 * Purpose: Convert complex types to/from Room-compatible primitives
 * Used for storing Lists, Maps, and custom objects in SQLite
 */
public class Converters {
    
    private static final Gson gson = new Gson();
    
    // ═══════════════════════════════════════════════════════════════
    // 📋 LIST<STRING> CONVERTERS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Convert List<String> to JSON string
     */
    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }
    
    /**
     * Convert JSON string to List<String>
     */
    @TypeConverter
    public static List<String> toStringList(String json) {
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📋 LIST<INTEGER> CONVERTERS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Convert List<Integer> to JSON string
     */
    @TypeConverter
    public static String fromIntegerList(List<Integer> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }
    
    /**
     * Convert JSON string to List<Integer>
     */
    @TypeConverter
    public static List<Integer> toIntegerList(String json) {
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<List<Integer>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🗺️ MAP<STRING, STRING> CONVERTERS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Convert Map<String, String> to JSON string
     */
    @TypeConverter
    public static String fromStringMap(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        return gson.toJson(map);
    }
    
    /**
     * Convert JSON string to Map<String, String>
     */
    @TypeConverter
    public static Map<String, String> toStringMap(String json) {
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🗺️ MAP<STRING, BOOLEAN> CONVERTERS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Convert Map<String, Boolean> to JSON string
     */
    @TypeConverter
    public static String fromBooleanMap(Map<String, Boolean> map) {
        if (map == null) {
            return null;
        }
        return gson.toJson(map);
    }
    
    /**
     * Convert JSON string to Map<String, Boolean>
     */
    @TypeConverter
    public static Map<String, Boolean> toBooleanMap(String json) {
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<Map<String, Boolean>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🗺️ MAP<STRING, INTEGER> CONVERTERS  
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Convert Map<String, Integer> to JSON string
     */
    @TypeConverter
    public static String fromIntegerMap(Map<String, Integer> map) {
        if (map == null) {
            return null;
        }
        return gson.toJson(map);
    }
    
    /**
     * Convert JSON string to Map<String, Integer>
     */
    @TypeConverter
    public static Map<String, Integer> toIntegerMap(String json) {
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        return gson.fromJson(json, type);
    }
}
