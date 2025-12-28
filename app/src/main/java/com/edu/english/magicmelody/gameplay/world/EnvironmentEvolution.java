package com.edu.english.magicmelody.gameplay.world;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.HashMap;
import java.util.Map;

/**
 * 🌈 Environment Evolution
 * 
 * Handles the visual transformation from gray to colorful world:
 * - Track world unlock progress
 * - Animate color transitions
 * - Particle effects for evolution
 */
public class EnvironmentEvolution {
    
    private static final String TAG = "EnvironmentEvolution";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 EVOLUTION STATE
    // ═══════════════════════════════════════════════════════════════
    
    public enum EvolutionLevel {
        GRAY(0, 0.0f, "Colorless"),
        FAINT(1, 0.25f, "Awakening"),
        PARTIAL(2, 0.50f, "Emerging"),
        VIBRANT(3, 0.75f, "Flourishing"),
        FULL(4, 1.0f, "Radiant");
        
        private final int level;
        private final float colorIntensity;
        private final String displayName;
        
        EvolutionLevel(int level, float intensity, String name) {
            this.level = level;
            this.colorIntensity = intensity;
            this.displayName = name;
        }
        
        public int getLevel() { return level; }
        public float getColorIntensity() { return colorIntensity; }
        public String getDisplayName() { return displayName; }
    }
    
    // World evolution states
    private final Map<String, WorldEvolutionState> worldStates = new HashMap<>();
    
    public static class WorldEvolutionState {
        public String worldId;
        public String worldName;
        public int totalLessons;
        public int completedLessons;
        public int totalStars;
        public int earnedStars;
        public EvolutionLevel currentLevel;
        public float exactProgress;  // 0.0 - 1.0
        
        public WorldEvolutionState(String worldId) {
            this.worldId = worldId;
            this.currentLevel = EvolutionLevel.GRAY;
            this.exactProgress = 0f;
        }
        
        /**
         * Calculate evolution level from progress
         */
        public EvolutionLevel calculateLevel() {
            if (totalLessons == 0) return EvolutionLevel.GRAY;
            
            exactProgress = (float) completedLessons / totalLessons;
            
            // Also factor in stars for more nuanced progression
            float starBonus = (totalStars > 0) 
                ? (float) earnedStars / totalStars * 0.1f 
                : 0f;
            
            float totalProgress = Math.min(1.0f, exactProgress + starBonus);
            
            if (totalProgress >= 0.9f) return EvolutionLevel.FULL;
            if (totalProgress >= 0.65f) return EvolutionLevel.VIBRANT;
            if (totalProgress >= 0.4f) return EvolutionLevel.PARTIAL;
            if (totalProgress >= 0.15f) return EvolutionLevel.FAINT;
            return EvolutionLevel.GRAY;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 COLOR DEFINITIONS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * World color themes
     */
    public static class WorldColors {
        public int primaryColor;
        public int secondaryColor;
        public int accentColor;
        public int backgroundColor;
        
        // Gray versions
        public int primaryGray;
        public int secondaryGray;
        public int accentGray;
        public int backgroundGray;
        
        public WorldColors(int primary, int secondary, int accent, int background) {
            this.primaryColor = primary;
            this.secondaryColor = secondary;
            this.accentColor = accent;
            this.backgroundColor = background;
            
            // Generate gray versions
            this.primaryGray = toGrayscale(primary);
            this.secondaryGray = toGrayscale(secondary);
            this.accentGray = toGrayscale(accent);
            this.backgroundGray = toGrayscale(background);
        }
        
        private int toGrayscale(int color) {
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            return Color.rgb(gray, gray, gray);
        }
        
        /**
         * Interpolate between gray and colored based on intensity
         */
        public int getPrimaryAtIntensity(float intensity) {
            return interpolateColor(primaryGray, primaryColor, intensity);
        }
        
        public int getSecondaryAtIntensity(float intensity) {
            return interpolateColor(secondaryGray, secondaryColor, intensity);
        }
        
        public int getAccentAtIntensity(float intensity) {
            return interpolateColor(accentGray, accentColor, intensity);
        }
        
        public int getBackgroundAtIntensity(float intensity) {
            return interpolateColor(backgroundGray, backgroundColor, intensity);
        }
    }
    
    // Predefined world themes
    private static final Map<String, WorldColors> WORLD_THEMES = new HashMap<>();
    static {
        // Alphabet Kingdom - Blue theme
        WORLD_THEMES.put("world_alphabet", new WorldColors(
            Color.parseColor("#2196F3"),  // Primary blue
            Color.parseColor("#64B5F6"),  // Secondary light blue
            Color.parseColor("#FFC107"),  // Accent gold
            Color.parseColor("#E3F2FD")   // Background light blue
        ));
        
        // Animal Safari - Green theme
        WORLD_THEMES.put("world_animals", new WorldColors(
            Color.parseColor("#4CAF50"),  // Primary green
            Color.parseColor("#81C784"),  // Secondary light green
            Color.parseColor("#FF9800"),  // Accent orange
            Color.parseColor("#E8F5E9")   // Background light green
        ));
        
        // Food Festival - Orange theme
        WORLD_THEMES.put("world_food", new WorldColors(
            Color.parseColor("#FF9800"),  // Primary orange
            Color.parseColor("#FFB74D"),  // Secondary light orange
            Color.parseColor("#E91E63"),  // Accent pink
            Color.parseColor("#FFF3E0")   // Background cream
        ));
        
        // Color Carnival - Rainbow theme
        WORLD_THEMES.put("world_colors", new WorldColors(
            Color.parseColor("#9C27B0"),  // Primary purple
            Color.parseColor("#BA68C8"),  // Secondary light purple
            Color.parseColor("#00BCD4"),  // Accent cyan
            Color.parseColor("#F3E5F5")   // Background light purple
        ));
        
        // Number Land - Red theme
        WORLD_THEMES.put("world_numbers", new WorldColors(
            Color.parseColor("#F44336"),  // Primary red
            Color.parseColor("#EF5350"),  // Secondary light red
            Color.parseColor("#FFEB3B"),  // Accent yellow
            Color.parseColor("#FFEBEE")   // Background light red
        ));
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface EvolutionListener {
        void onEvolutionLevelChanged(String worldId, EvolutionLevel oldLevel, EvolutionLevel newLevel);
        void onColorIntensityChanged(String worldId, float intensity);
        void onWorldFullyEvolved(String worldId);
    }
    
    private EvolutionListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public EnvironmentEvolution() {
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(EvolutionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Register a world with its color theme
     */
    public void registerWorld(String worldId, int totalLessons) {
        WorldEvolutionState state = new WorldEvolutionState(worldId);
        state.totalLessons = totalLessons;
        state.totalStars = totalLessons * 3;  // 3 stars per lesson
        worldStates.put(worldId, state);
    }
    
    /**
     * Add custom world theme
     */
    public void addWorldTheme(String worldId, WorldColors colors) {
        WORLD_THEMES.put(worldId, colors);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 PROGRESS UPDATE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Update world progress
     */
    public void updateProgress(String worldId, int completedLessons, int earnedStars) {
        WorldEvolutionState state = worldStates.get(worldId);
        if (state == null) {
            state = new WorldEvolutionState(worldId);
            worldStates.put(worldId, state);
        }
        
        EvolutionLevel oldLevel = state.currentLevel;
        
        state.completedLessons = completedLessons;
        state.earnedStars = earnedStars;
        state.currentLevel = state.calculateLevel();
        
        // Notify if level changed
        if (state.currentLevel != oldLevel && listener != null) {
            listener.onEvolutionLevelChanged(worldId, oldLevel, state.currentLevel);
            
            if (state.currentLevel == EvolutionLevel.FULL) {
                listener.onWorldFullyEvolved(worldId);
            }
        }
    }
    
    /**
     * Get current evolution state for a world
     */
    public WorldEvolutionState getWorldState(String worldId) {
        return worldStates.get(worldId);
    }
    
    /**
     * Get color intensity for a world
     */
    public float getColorIntensity(String worldId) {
        WorldEvolutionState state = worldStates.get(worldId);
        if (state == null) return 0f;
        return state.currentLevel.getColorIntensity();
    }
    
    /**
     * Get world colors at current evolution
     */
    public WorldColors getWorldColors(String worldId) {
        return WORLD_THEMES.getOrDefault(worldId, 
            WORLD_THEMES.get("world_alphabet"));  // Default theme
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 COLOR UTILITIES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Interpolate between two colors
     */
    public static int interpolateColor(int colorFrom, int colorTo, float fraction) {
        float clampedFraction = Math.max(0, Math.min(1, fraction));
        
        int fromA = Color.alpha(colorFrom);
        int fromR = Color.red(colorFrom);
        int fromG = Color.green(colorFrom);
        int fromB = Color.blue(colorFrom);
        
        int toA = Color.alpha(colorTo);
        int toR = Color.red(colorTo);
        int toG = Color.green(colorTo);
        int toB = Color.blue(colorTo);
        
        int a = (int)(fromA + (toA - fromA) * clampedFraction);
        int r = (int)(fromR + (toR - fromR) * clampedFraction);
        int g = (int)(fromG + (toG - fromG) * clampedFraction);
        int b = (int)(fromB + (toB - fromB) * clampedFraction);
        
        return Color.argb(a, r, g, b);
    }
    
    /**
     * Apply saturation to a color (0 = gray, 1 = full color)
     */
    public static int applySaturation(int color, float saturation) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] *= saturation;  // Modify saturation
        return Color.HSVToColor(Color.alpha(color), hsv);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎬 ANIMATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Animate color evolution on a view
     */
    public void animateEvolution(View view, String worldId, 
                                  EvolutionLevel fromLevel, EvolutionLevel toLevel,
                                  long durationMs) {
        
        WorldColors colors = getWorldColors(worldId);
        if (colors == null) return;
        
        float fromIntensity = fromLevel.getColorIntensity();
        float toIntensity = toLevel.getColorIntensity();
        
        ValueAnimator animator = ValueAnimator.ofFloat(fromIntensity, toIntensity);
        animator.setDuration(durationMs);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        animator.addUpdateListener(animation -> {
            float intensity = (float) animation.getAnimatedValue();
            int color = colors.getBackgroundAtIntensity(intensity);
            view.setBackgroundColor(color);
            
            if (listener != null) {
                listener.onColorIntensityChanged(worldId, intensity);
            }
        });
        
        animator.start();
    }
    
    /**
     * Animate single color transition
     */
    public static ValueAnimator animateColorTransition(View view, int fromColor, int toColor, 
                                                       long durationMs) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(durationMs);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int color = interpolateColor(fromColor, toColor, fraction);
            view.setBackgroundColor(color);
        });
        
        animator.start();
        return animator;
    }
    
    /**
     * Create pulsing glow effect for newly evolved elements
     */
    public static ValueAnimator createGlowAnimation(View view, int baseColor, long durationMs) {
        int lightColor = brightenColor(baseColor, 0.3f);
        
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f, 0f);
        animator.setDuration(durationMs);
        animator.setRepeatCount(3);
        
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int color = interpolateColor(baseColor, lightColor, fraction);
            view.setBackgroundColor(color);
        });
        
        return animator;
    }
    
    /**
     * Brighten a color
     */
    private static int brightenColor(int color, float amount) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.min(1f, hsv[2] + amount);  // Increase value
        return Color.HSVToColor(Color.alpha(color), hsv);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 QUERIES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Check if world is fully evolved
     */
    public boolean isWorldFullyEvolved(String worldId) {
        WorldEvolutionState state = worldStates.get(worldId);
        return state != null && state.currentLevel == EvolutionLevel.FULL;
    }
    
    /**
     * Get overall game evolution progress
     */
    public float getOverallProgress() {
        if (worldStates.isEmpty()) return 0f;
        
        float totalProgress = 0f;
        for (WorldEvolutionState state : worldStates.values()) {
            totalProgress += state.exactProgress;
        }
        
        return totalProgress / worldStates.size();
    }
    
    /**
     * Count fully evolved worlds
     */
    public int getFullyEvolvedWorldCount() {
        int count = 0;
        for (WorldEvolutionState state : worldStates.values()) {
            if (state.currentLevel == EvolutionLevel.FULL) {
                count++;
            }
        }
        return count;
    }
}
