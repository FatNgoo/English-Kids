package com.edu.english.magicmelody.util;

/**
 * 🎮 Magic Melody Constants
 * 
 * Purpose: Centralized constants for the Magic Melody game
 * Usage: Reference these constants throughout the game modules
 */
public final class Constants {
    
    private Constants() {
        // Prevent instantiation
    }
    
    // ══════════════════════════════════════════════════════════════════
    // 🎯 GAME CONFIGURATION
    // ══════════════════════════════════════════════════════════════════
    
    public static final String GAME_NAME = "Magic Melody";
    public static final String GAME_VERSION = "1.0.0";
    
    // Age groups
    public static final String AGE_GROUP_5_6 = "5-6";
    public static final String AGE_GROUP_7_8 = "7-8";
    public static final String AGE_GROUP_9_10 = "9-10";
    
    // ══════════════════════════════════════════════════════════════════
    // 🎵 AUDIO CONFIGURATION
    // ══════════════════════════════════════════════════════════════════
    
    // Maximum simultaneous audio streams
    public static final int MAX_AUDIO_STREAMS = 10;
    
    // Default BPM (Beats Per Minute)
    public static final int DEFAULT_BPM = 100;
    public static final int MIN_BPM = 60;
    public static final int MAX_BPM = 180;
    
    // Note timing tolerance (milliseconds)
    public static final int PERFECT_TIMING_MS = 50;
    public static final int GOOD_TIMING_MS = 100;
    public static final int OK_TIMING_MS = 150;
    
    // Voice detection threshold
    public static final float VOICE_THRESHOLD_LOW = 0.3f;
    public static final float VOICE_THRESHOLD_MEDIUM = 0.5f;
    public static final float VOICE_THRESHOLD_HIGH = 0.7f;
    
    // ══════════════════════════════════════════════════════════════════
    // 🎨 VISUAL CONFIGURATION
    // ══════════════════════════════════════════════════════════════════
    
    // Evolution stages (gray → color)
    public static final int EVOLUTION_STAGE_GRAY = 0;
    public static final int EVOLUTION_STAGE_PARTIAL = 1;
    public static final int EVOLUTION_STAGE_COLORFUL = 2;
    public static final int EVOLUTION_STAGE_BLOOM = 3;
    
    // Animation durations (milliseconds)
    public static final long ANIMATION_SHORT = 200;
    public static final long ANIMATION_MEDIUM = 400;
    public static final long ANIMATION_LONG = 800;
    public static final long ANIMATION_EVOLUTION = 1500;
    
    // ══════════════════════════════════════════════════════════════════
    // 🌍 WORLD MAP CONFIGURATION
    // ══════════════════════════════════════════════════════════════════
    
    public static final int MAX_WORLDS = 5;
    public static final int LEVELS_PER_WORLD = 10;
    
    // World theme IDs
    public static final String WORLD_FOREST = "forest";
    public static final String WORLD_OCEAN = "ocean";
    public static final String WORLD_MOUNTAIN = "mountain";
    public static final String WORLD_DESERT = "desert";
    public static final String WORLD_SKY = "sky";
    
    // ══════════════════════════════════════════════════════════════════
    // 🎯 SCORING CONFIGURATION
    // ══════════════════════════════════════════════════════════════════
    
    public static final int SCORE_PERFECT = 100;
    public static final int SCORE_GOOD = 75;
    public static final int SCORE_OK = 50;
    public static final int SCORE_MISS = 0;
    
    // Combo multipliers
    public static final float COMBO_MULTIPLIER_5 = 1.5f;
    public static final float COMBO_MULTIPLIER_10 = 2.0f;
    public static final float COMBO_MULTIPLIER_20 = 3.0f;
    
    // Stars thresholds
    public static final int STARS_1_THRESHOLD = 50;
    public static final int STARS_2_THRESHOLD = 70;
    public static final int STARS_3_THRESHOLD = 90;
    
    // ══════════════════════════════════════════════════════════════════
    // 📚 MAGIC NOTEBOOK CONFIGURATION
    // ══════════════════════════════════════════════════════════════════
    
    public static final int NOTEBOOK_MAX_PAGES = 100;
    public static final int NOTES_PER_PAGE = 8;
    
    // Note types
    public static final String NOTE_TYPE_BASIC = "basic";
    public static final String NOTE_TYPE_SPECIAL = "special";
    public static final String NOTE_TYPE_LEGENDARY = "legendary";
    
    // ══════════════════════════════════════════════════════════════════
    // 👹 BOSS BATTLE CONFIGURATION
    // ══════════════════════════════════════════════════════════════════
    
    public static final int BOSS_HEALTH_EASY = 100;
    public static final int BOSS_HEALTH_MEDIUM = 200;
    public static final int BOSS_HEALTH_HARD = 300;
    
    // Boss attack patterns
    public static final int BOSS_PATTERN_SEQUENCE = 0;
    public static final int BOSS_PATTERN_RANDOM = 1;
    public static final int BOSS_PATTERN_ADAPTIVE = 2;
    
    // ══════════════════════════════════════════════════════════════════
    // 📁 ASSET PATHS
    // ══════════════════════════════════════════════════════════════════
    
    public static final String ASSETS_AUDIO = "magicmelody/audio/";
    public static final String ASSETS_MODELS = "magicmelody/models/";
    public static final String ASSETS_LESSONS = "magicmelody/lessons/";
    public static final String ASSETS_THEMES = "magicmelody/themes/";
    
    // ══════════════════════════════════════════════════════════════════
    // 🔑 SHARED PREFERENCES KEYS
    // ══════════════════════════════════════════════════════════════════
    
    public static final String PREF_NAME = "magic_melody_prefs";
    public static final String PREF_USER_AGE_GROUP = "user_age_group";
    public static final String PREF_CURRENT_WORLD = "current_world";
    public static final String PREF_CURRENT_LEVEL = "current_level";
    public static final String PREF_TOTAL_STARS = "total_stars";
    public static final String PREF_SOUND_ENABLED = "sound_enabled";
    public static final String PREF_MUSIC_ENABLED = "music_enabled";
    public static final String PREF_VOICE_ENABLED = "voice_enabled";
    
    // ══════════════════════════════════════════════════════════════════
    // 📝 INTENT EXTRAS
    // ══════════════════════════════════════════════════════════════════
    
    public static final String EXTRA_WORLD_ID = "extra_world_id";
    public static final String EXTRA_LEVEL_ID = "extra_level_id";
    public static final String EXTRA_LESSON_ID = "extra_lesson_id";
    public static final String EXTRA_BOSS_ID = "extra_boss_id";
    public static final String EXTRA_IS_BOSS_BATTLE = "extra_is_boss_battle";
    
    // ══════════════════════════════════════════════════════════════════
    // 🎮 GAMEPLAY NESTED CLASS (for RhythmGameplayViewModel)
    // ══════════════════════════════════════════════════════════════════
    
    public static final class Gameplay {
        private Gameplay() {}
        
        /** Time for note to fall from top to hit zone (seconds) */
        public static final float NOTE_FALL_TIME_SECONDS = 2.0f;
        
        /** Window for miss detection (milliseconds) */
        public static final long MISS_WINDOW_MS = 200;
        
        /** Perfect hit timing window (milliseconds) */
        public static final long PERFECT_WINDOW_MS = PERFECT_TIMING_MS;
        
        /** Good hit timing window (milliseconds) */
        public static final long GOOD_WINDOW_MS = GOOD_TIMING_MS;
        
        /** OK hit timing window (milliseconds) */
        public static final long OK_WINDOW_MS = OK_TIMING_MS;
    }
    
    // ══════════════════════════════════════════════════════════════════
    // 🏆 SCORING NESTED CLASS (for RhythmGameplayViewModel)
    // ══════════════════════════════════════════════════════════════════
    
    public static final class Scoring {
        private Scoring() {}
        
        /** Score for perfect hit */
        public static final int PERFECT_SCORE = SCORE_PERFECT;
        
        /** Score for good hit */
        public static final int GOOD_SCORE = SCORE_GOOD;
        
        /** Score for OK hit */
        public static final int OK_SCORE = SCORE_OK;
        
        /** Score for miss */
        public static final int MISS_SCORE = SCORE_MISS;
    }
}
