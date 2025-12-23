package com.edu.english.coloralchemy;

import android.graphics.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Color mixing logic for the alchemy lab
 * Handles combining primary colors to create secondary and tertiary colors
 * Supports mixing up to 3 colors together
 */
public class ColorMixer {
    
    // Primary colors
    public static final int COLOR_RED = Color.parseColor("#E74C3C");
    public static final int COLOR_BLUE = Color.parseColor("#3498DB");
    public static final int COLOR_YELLOW = Color.parseColor("#F1C40F");
    
    // Secondary colors (mixing results)
    public static final int COLOR_PURPLE = Color.parseColor("#9B59B6");
    public static final int COLOR_ORANGE = Color.parseColor("#E67E22");
    public static final int COLOR_GREEN = Color.parseColor("#27AE60");
    
    // Additional colors for more variety
    public static final int COLOR_PINK = Color.parseColor("#FF69B4");
    public static final int COLOR_CYAN = Color.parseColor("#00BCD4");
    public static final int COLOR_WHITE = Color.parseColor("#FFFFFF");
    public static final int COLOR_BLACK = Color.parseColor("#2C3E50");
    public static final int COLOR_LIME = Color.parseColor("#CDDC39");
    public static final int COLOR_MAGENTA = Color.parseColor("#E91E63");
    
    // Tertiary colors (3-color mixing results)
    public static final int COLOR_BROWN = Color.parseColor("#8D6E63");
    public static final int COLOR_GRAY = Color.parseColor("#9E9E9E");
    public static final int COLOR_TEAL = Color.parseColor("#009688");
    public static final int COLOR_CORAL = Color.parseColor("#FF7043");
    public static final int COLOR_OLIVE = Color.parseColor("#827717");
    public static final int COLOR_MAROON = Color.parseColor("#880E4F");
    public static final int COLOR_NAVY = Color.parseColor("#1A237E");
    public static final int COLOR_PEACH = Color.parseColor("#FFAB91");
    public static final int COLOR_LAVENDER = Color.parseColor("#CE93D8");
    public static final int COLOR_TURQUOISE = Color.parseColor("#4DD0E1");
    
    // Color names
    private static final Map<Integer, String> colorNames = new HashMap<>();
    private static final Map<String, int[]> mixingRecipes = new HashMap<>();
    private static final Map<String, String> mixingSentences = new HashMap<>();
    
    // Triple color mixing recipes
    private static final Map<String, int[]> tripleMixingRecipes = new HashMap<>();
    private static final Map<String, String> tripleMixingSentences = new HashMap<>();
    
    static {
        // Initialize color names - Primary
        colorNames.put(COLOR_RED, "Red");
        colorNames.put(COLOR_BLUE, "Blue");
        colorNames.put(COLOR_YELLOW, "Yellow");
        
        // Initialize color names - Secondary
        colorNames.put(COLOR_PURPLE, "Purple");
        colorNames.put(COLOR_ORANGE, "Orange");
        colorNames.put(COLOR_GREEN, "Green");
        
        // Initialize color names - Additional
        colorNames.put(COLOR_PINK, "Pink");
        colorNames.put(COLOR_CYAN, "Cyan");
        colorNames.put(COLOR_WHITE, "White");
        colorNames.put(COLOR_BLACK, "Black");
        colorNames.put(COLOR_LIME, "Lime");
        colorNames.put(COLOR_MAGENTA, "Magenta");
        
        // Initialize color names - Tertiary
        colorNames.put(COLOR_BROWN, "Brown");
        colorNames.put(COLOR_GRAY, "Gray");
        colorNames.put(COLOR_TEAL, "Teal");
        colorNames.put(COLOR_CORAL, "Coral");
        colorNames.put(COLOR_OLIVE, "Olive");
        colorNames.put(COLOR_MAROON, "Maroon");
        colorNames.put(COLOR_NAVY, "Navy");
        colorNames.put(COLOR_PEACH, "Peach");
        colorNames.put(COLOR_LAVENDER, "Lavender");
        colorNames.put(COLOR_TURQUOISE, "Turquoise");
        
        // ========== Two-color mixing recipes ==========
        // Primary + Primary
        mixingRecipes.put(getMixKey(COLOR_RED, COLOR_BLUE), new int[]{COLOR_PURPLE, COLOR_RED, COLOR_BLUE});
        mixingRecipes.put(getMixKey(COLOR_RED, COLOR_YELLOW), new int[]{COLOR_ORANGE, COLOR_RED, COLOR_YELLOW});
        mixingRecipes.put(getMixKey(COLOR_BLUE, COLOR_YELLOW), new int[]{COLOR_GREEN, COLOR_BLUE, COLOR_YELLOW});
        
        // With Pink
        mixingRecipes.put(getMixKey(COLOR_PINK, COLOR_WHITE), new int[]{COLOR_LAVENDER, COLOR_PINK, COLOR_WHITE});
        mixingRecipes.put(getMixKey(COLOR_PINK, COLOR_BLUE), new int[]{COLOR_PURPLE, COLOR_PINK, COLOR_BLUE});
        mixingRecipes.put(getMixKey(COLOR_PINK, COLOR_YELLOW), new int[]{COLOR_PEACH, COLOR_PINK, COLOR_YELLOW});
        
        // With Cyan
        mixingRecipes.put(getMixKey(COLOR_CYAN, COLOR_BLUE), new int[]{COLOR_NAVY, COLOR_CYAN, COLOR_BLUE});
        mixingRecipes.put(getMixKey(COLOR_CYAN, COLOR_GREEN), new int[]{COLOR_TEAL, COLOR_CYAN, COLOR_GREEN});
        mixingRecipes.put(getMixKey(COLOR_CYAN, COLOR_WHITE), new int[]{COLOR_TURQUOISE, COLOR_CYAN, COLOR_WHITE});
        
        // With White (lightening)
        mixingRecipes.put(getMixKey(COLOR_RED, COLOR_WHITE), new int[]{COLOR_PINK, COLOR_RED, COLOR_WHITE});
        mixingRecipes.put(getMixKey(COLOR_BLUE, COLOR_WHITE), new int[]{COLOR_CYAN, COLOR_BLUE, COLOR_WHITE});
        mixingRecipes.put(getMixKey(COLOR_ORANGE, COLOR_WHITE), new int[]{COLOR_PEACH, COLOR_ORANGE, COLOR_WHITE});
        mixingRecipes.put(getMixKey(COLOR_PURPLE, COLOR_WHITE), new int[]{COLOR_LAVENDER, COLOR_PURPLE, COLOR_WHITE});
        
        // With Black (darkening)
        mixingRecipes.put(getMixKey(COLOR_RED, COLOR_BLACK), new int[]{COLOR_MAROON, COLOR_RED, COLOR_BLACK});
        mixingRecipes.put(getMixKey(COLOR_BLUE, COLOR_BLACK), new int[]{COLOR_NAVY, COLOR_BLUE, COLOR_BLACK});
        mixingRecipes.put(getMixKey(COLOR_GREEN, COLOR_BLACK), new int[]{COLOR_OLIVE, COLOR_GREEN, COLOR_BLACK});
        mixingRecipes.put(getMixKey(COLOR_YELLOW, COLOR_BLACK), new int[]{COLOR_OLIVE, COLOR_YELLOW, COLOR_BLACK});
        
        // With Lime
        mixingRecipes.put(getMixKey(COLOR_LIME, COLOR_BLUE), new int[]{COLOR_TEAL, COLOR_LIME, COLOR_BLUE});
        mixingRecipes.put(getMixKey(COLOR_LIME, COLOR_YELLOW), new int[]{COLOR_GREEN, COLOR_LIME, COLOR_YELLOW});
        
        // With Magenta
        mixingRecipes.put(getMixKey(COLOR_MAGENTA, COLOR_BLUE), new int[]{COLOR_PURPLE, COLOR_MAGENTA, COLOR_BLUE});
        mixingRecipes.put(getMixKey(COLOR_MAGENTA, COLOR_WHITE), new int[]{COLOR_PINK, COLOR_MAGENTA, COLOR_WHITE});
        mixingRecipes.put(getMixKey(COLOR_MAGENTA, COLOR_YELLOW), new int[]{COLOR_CORAL, COLOR_MAGENTA, COLOR_YELLOW});
        
        // Special combinations
        mixingRecipes.put(getMixKey(COLOR_ORANGE, COLOR_RED), new int[]{COLOR_CORAL, COLOR_ORANGE, COLOR_RED});
        mixingRecipes.put(getMixKey(COLOR_GREEN, COLOR_YELLOW), new int[]{COLOR_LIME, COLOR_GREEN, COLOR_YELLOW});
        mixingRecipes.put(getMixKey(COLOR_BLACK, COLOR_WHITE), new int[]{COLOR_GRAY, COLOR_BLACK, COLOR_WHITE});
        
        // Two-color mixing sentences
        mixingSentences.put(getMixKey(COLOR_RED, COLOR_BLUE), "Red and Blue make Purple!");
        mixingSentences.put(getMixKey(COLOR_RED, COLOR_YELLOW), "Red and Yellow make Orange!");
        mixingSentences.put(getMixKey(COLOR_BLUE, COLOR_YELLOW), "Blue and Yellow make Green!");
        mixingSentences.put(getMixKey(COLOR_RED, COLOR_WHITE), "Red and White make Pink!");
        mixingSentences.put(getMixKey(COLOR_BLUE, COLOR_WHITE), "Blue and White make Cyan!");
        mixingSentences.put(getMixKey(COLOR_RED, COLOR_BLACK), "Red and Black make Maroon!");
        mixingSentences.put(getMixKey(COLOR_BLUE, COLOR_BLACK), "Blue and Black make Navy!");
        mixingSentences.put(getMixKey(COLOR_BLACK, COLOR_WHITE), "Black and White make Gray!");
        mixingSentences.put(getMixKey(COLOR_ORANGE, COLOR_WHITE), "Orange and White make Peach!");
        mixingSentences.put(getMixKey(COLOR_PURPLE, COLOR_WHITE), "Purple and White make Lavender!");
        mixingSentences.put(getMixKey(COLOR_CYAN, COLOR_GREEN), "Cyan and Green make Teal!");
        mixingSentences.put(getMixKey(COLOR_CYAN, COLOR_WHITE), "Cyan and White make Turquoise!");
        mixingSentences.put(getMixKey(COLOR_GREEN, COLOR_YELLOW), "Green and Yellow make Lime!");
        mixingSentences.put(getMixKey(COLOR_ORANGE, COLOR_RED), "Orange and Red make Coral!");
        mixingSentences.put(getMixKey(COLOR_GREEN, COLOR_BLACK), "Green and Black make Olive!");
        mixingSentences.put(getMixKey(COLOR_PINK, COLOR_YELLOW), "Pink and Yellow make Peach!");
        mixingSentences.put(getMixKey(COLOR_MAGENTA, COLOR_YELLOW), "Magenta and Yellow make Coral!");
        
        // ========== Three-color mixing recipes ==========
        // Primary colors together = Brown
        tripleMixingRecipes.put(getTripleMixKey(COLOR_RED, COLOR_BLUE, COLOR_YELLOW), 
            new int[]{COLOR_BROWN, COLOR_RED, COLOR_BLUE, COLOR_YELLOW});
        
        // Special triple combinations
        tripleMixingRecipes.put(getTripleMixKey(COLOR_RED, COLOR_WHITE, COLOR_YELLOW), 
            new int[]{COLOR_PEACH, COLOR_RED, COLOR_WHITE, COLOR_YELLOW});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_BLUE, COLOR_WHITE, COLOR_PINK), 
            new int[]{COLOR_LAVENDER, COLOR_BLUE, COLOR_WHITE, COLOR_PINK});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_BLUE, COLOR_GREEN, COLOR_WHITE), 
            new int[]{COLOR_TURQUOISE, COLOR_BLUE, COLOR_GREEN, COLOR_WHITE});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_RED, COLOR_BLUE, COLOR_WHITE), 
            new int[]{COLOR_LAVENDER, COLOR_RED, COLOR_BLUE, COLOR_WHITE});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_RED, COLOR_YELLOW, COLOR_WHITE), 
            new int[]{COLOR_PEACH, COLOR_RED, COLOR_YELLOW, COLOR_WHITE});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_BLUE, COLOR_YELLOW, COLOR_WHITE), 
            new int[]{COLOR_TEAL, COLOR_BLUE, COLOR_YELLOW, COLOR_WHITE});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_RED, COLOR_BLUE, COLOR_BLACK), 
            new int[]{COLOR_MAROON, COLOR_RED, COLOR_BLUE, COLOR_BLACK});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_BLUE, COLOR_YELLOW, COLOR_BLACK), 
            new int[]{COLOR_OLIVE, COLOR_BLUE, COLOR_YELLOW, COLOR_BLACK});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_RED, COLOR_YELLOW, COLOR_BLACK), 
            new int[]{COLOR_BROWN, COLOR_RED, COLOR_YELLOW, COLOR_BLACK});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_CYAN, COLOR_MAGENTA, COLOR_YELLOW), 
            new int[]{COLOR_GRAY, COLOR_CYAN, COLOR_MAGENTA, COLOR_YELLOW});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_PINK, COLOR_CYAN, COLOR_YELLOW), 
            new int[]{COLOR_PEACH, COLOR_PINK, COLOR_CYAN, COLOR_YELLOW});
        tripleMixingRecipes.put(getTripleMixKey(COLOR_LIME, COLOR_CYAN, COLOR_BLUE), 
            new int[]{COLOR_TEAL, COLOR_LIME, COLOR_CYAN, COLOR_BLUE});
        
        // Triple mixing sentences
        tripleMixingSentences.put(getTripleMixKey(COLOR_RED, COLOR_BLUE, COLOR_YELLOW), 
            "Red, Blue and Yellow make Brown!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_RED, COLOR_WHITE, COLOR_YELLOW), 
            "Red, White and Yellow make Peach!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_BLUE, COLOR_WHITE, COLOR_PINK), 
            "Blue, White and Pink make Lavender!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_BLUE, COLOR_GREEN, COLOR_WHITE), 
            "Blue, Green and White make Turquoise!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_RED, COLOR_BLUE, COLOR_WHITE), 
            "Red, Blue and White make Lavender!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_RED, COLOR_YELLOW, COLOR_WHITE), 
            "Red, Yellow and White make Peach!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_BLUE, COLOR_YELLOW, COLOR_WHITE), 
            "Blue, Yellow and White make Teal!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_RED, COLOR_BLUE, COLOR_BLACK), 
            "Red, Blue and Black make Maroon!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_BLUE, COLOR_YELLOW, COLOR_BLACK), 
            "Blue, Yellow and Black make Olive!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_RED, COLOR_YELLOW, COLOR_BLACK), 
            "Red, Yellow and Black make Brown!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_CYAN, COLOR_MAGENTA, COLOR_YELLOW), 
            "Cyan, Magenta and Yellow make Gray!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_PINK, COLOR_CYAN, COLOR_YELLOW), 
            "Pink, Cyan and Yellow make Peach!");
        tripleMixingSentences.put(getTripleMixKey(COLOR_LIME, COLOR_CYAN, COLOR_BLUE), 
            "Lime, Cyan and Blue make Teal!");
    }
    
    /**
     * Color combination result
     */
    public static class MixResult {
        public int resultColor;
        public String colorName;
        public String sentence;
        public int color1;
        public int color2;
        public int color3; // For 3-color mixing
        public boolean isValid;
        public int colorCount; // 2 or 3 colors mixed
        
        // Constructor for 2 colors
        public MixResult(int resultColor, String colorName, String sentence, 
                        int color1, int color2, boolean isValid) {
            this.resultColor = resultColor;
            this.colorName = colorName;
            this.sentence = sentence;
            this.color1 = color1;
            this.color2 = color2;
            this.color3 = 0;
            this.isValid = isValid;
            this.colorCount = 2;
        }
        
        // Constructor for 3 colors
        public MixResult(int resultColor, String colorName, String sentence, 
                        int color1, int color2, int color3, boolean isValid) {
            this.resultColor = resultColor;
            this.colorName = colorName;
            this.sentence = sentence;
            this.color1 = color1;
            this.color2 = color2;
            this.color3 = color3;
            this.isValid = isValid;
            this.colorCount = 3;
        }
    }
    
    /**
     * Generate unique key for color pair (order independent)
     */
    private static String getMixKey(int color1, int color2) {
        int min = Math.min(color1, color2);
        int max = Math.max(color1, color2);
        return min + "_" + max;
    }
    
    /**
     * Generate unique key for three colors (order independent)
     */
    private static String getTripleMixKey(int color1, int color2, int color3) {
        int[] colors = {color1, color2, color3};
        Arrays.sort(colors);
        return colors[0] + "_" + colors[1] + "_" + colors[2];
    }
    
    /**
     * Mix two colors together
     */
    public static MixResult mixColors(int color1, int color2) {
        String key = getMixKey(color1, color2);
        
        if (mixingRecipes.containsKey(key)) {
            int[] recipe = mixingRecipes.get(key);
            int resultColor = recipe[0];
            String colorName = colorNames.get(resultColor);
            String sentence = mixingSentences.containsKey(key) ? 
                mixingSentences.get(key) : colorName + " created!";
            
            return new MixResult(resultColor, colorName, sentence, color1, color2, true);
        }
        
        // If no valid recipe, blend colors and find nearest named color
        int blendedColor = blendColors(color1, color2, 0.5f);
        String nearestName = findNearestColorName(blendedColor);
        return new MixResult(blendedColor, nearestName, nearestName + " created!", 
                           color1, color2, true);
    }
    
    /**
     * Mix three colors together
     */
    public static MixResult mixThreeColors(int color1, int color2, int color3) {
        String key = getTripleMixKey(color1, color2, color3);
        
        if (tripleMixingRecipes.containsKey(key)) {
            int[] recipe = tripleMixingRecipes.get(key);
            int resultColor = recipe[0];
            String colorName = colorNames.get(resultColor);
            if (colorName == null) colorName = "Mixed Color";
            String sentence = tripleMixingSentences.containsKey(key) ? 
                tripleMixingSentences.get(key) : colorName + " created!";
            
            return new MixResult(resultColor, colorName, sentence, color1, color2, color3, true);
        }
        
        // If no valid recipe, blend all three colors and find nearest named color
        int blendedColor = blendThreeColors(color1, color2, color3);
        String nearestName = findNearestColorName(blendedColor);
        return new MixResult(blendedColor, nearestName, nearestName + " created!", 
                           color1, color2, color3, true);
    }
    
    /**
     * Mix colors - supports 2 or 3 colors
     */
    public static MixResult mixColors(int[] colors) {
        if (colors.length == 2) {
            return mixColors(colors[0], colors[1]);
        } else if (colors.length == 3) {
            return mixThreeColors(colors[0], colors[1], colors[2]);
        } else if (colors.length == 1) {
            return new MixResult(colors[0], getColorName(colors[0]), 
                getColorName(colors[0]) + "!", colors[0], 0, true);
        }
        return new MixResult(Color.GRAY, "Unknown", "Something went wrong!", 0, 0, false);
    }
    
    /**
     * Blend two colors with a ratio
     */
    public static int blendColors(int color1, int color2, float ratio) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        
        int r = (int) (r1 * (1 - ratio) + r2 * ratio);
        int g = (int) (g1 * (1 - ratio) + g2 * ratio);
        int b = (int) (b1 * (1 - ratio) + b2 * ratio);
        
        return Color.rgb(r, g, b);
    }
    
    /**
     * Blend three colors equally
     */
    public static int blendThreeColors(int color1, int color2, int color3) {
        int r = (Color.red(color1) + Color.red(color2) + Color.red(color3)) / 3;
        int g = (Color.green(color1) + Color.green(color2) + Color.green(color3)) / 3;
        int b = (Color.blue(color1) + Color.blue(color2) + Color.blue(color3)) / 3;
        
        return Color.rgb(r, g, b);
    }
    
    /**
     * Find nearest named color for a given color
     * This ensures we always return a color name, never "Mixed Color"
     */
    public static String findNearestColorName(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        
        String nearestName = "Brown"; // Default fallback
        double nearestDistance = Double.MAX_VALUE;
        
        for (Map.Entry<Integer, String> entry : colorNames.entrySet()) {
            int namedColor = entry.getKey();
            int nr = Color.red(namedColor);
            int ng = Color.green(namedColor);
            int nb = Color.blue(namedColor);
            
            // Calculate color distance (Euclidean in RGB space)
            double distance = Math.sqrt(
                Math.pow(r - nr, 2) + 
                Math.pow(g - ng, 2) + 
                Math.pow(b - nb, 2)
            );
            
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestName = entry.getValue();
            }
        }
        
        return nearestName;
    }
    
    /**
     * Get color name
     */
    public static String getColorName(int color) {
        if (colorNames.containsKey(color)) {
            return colorNames.get(color);
        }
        // Try to find nearest named color
        return findNearestColorName(color);
    }
    
    /**
     * Apply shade to color
     * @param color Base color
     * @param shade -1.0 (darkest) to 1.0 (lightest), 0 = original
     * @return Shaded color
     */
    public static int applyShade(int color, float shade) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        
        if (shade > 0) {
            // Lighten - blend toward white
            r = (int) (r + (255 - r) * shade);
            g = (int) (g + (255 - g) * shade);
            b = (int) (b + (255 - b) * shade);
        } else if (shade < 0) {
            // Darken - blend toward black
            float factor = 1 + shade;
            r = (int) (r * factor);
            g = (int) (g * factor);
            b = (int) (b * factor);
        }
        
        return Color.rgb(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b))
        );
    }
    
    /**
     * Get shade name
     */
    public static String getShadeName(String baseColorName, float shade) {
        if (shade > 0.3f) {
            return "Light " + baseColorName;
        } else if (shade < -0.3f) {
            return "Dark " + baseColorName;
        }
        return baseColorName;
    }
    
    /**
     * Check if color is a primary color
     */
    public static boolean isPrimaryColor(int color) {
        return color == COLOR_RED || color == COLOR_BLUE || color == COLOR_YELLOW;
    }
    
    /**
     * Check if color is a secondary color
     */
    public static boolean isSecondaryColor(int color) {
        return color == COLOR_PURPLE || color == COLOR_ORANGE || color == COLOR_GREEN;
    }
    
    /**
     * Check if color is an additional color (available for mixing)
     */
    public static boolean isAdditionalColor(int color) {
        return color == COLOR_PINK || color == COLOR_CYAN || 
               color == COLOR_WHITE || color == COLOR_BLACK ||
               color == COLOR_LIME || color == COLOR_MAGENTA;
    }
    
    /**
     * Get all primary colors
     */
    public static int[] getPrimaryColors() {
        return new int[]{COLOR_RED, COLOR_BLUE, COLOR_YELLOW};
    }
    
    /**
     * Get all secondary colors
     */
    public static int[] getSecondaryColors() {
        return new int[]{COLOR_PURPLE, COLOR_ORANGE, COLOR_GREEN};
    }
    
    /**
     * Get all additional colors for tubes
     */
    public static int[] getAdditionalColors() {
        return new int[]{COLOR_PINK, COLOR_CYAN, COLOR_WHITE, COLOR_BLACK, COLOR_LIME, COLOR_MAGENTA};
    }
    
    /**
     * Get all available colors for test tubes
     * Returns primary colors first, then additional colors
     */
    public static int[] getAllTubeColors() {
        return new int[]{
            COLOR_RED, COLOR_YELLOW, COLOR_BLUE,     // Primary colors
            COLOR_PINK, COLOR_CYAN, COLOR_WHITE,     // Row 2 - light colors
            COLOR_BLACK, COLOR_LIME, COLOR_MAGENTA   // Row 3 - dark/special colors
        };
    }
    
    /**
     * Animate color transition
     */
    public static int animateColorTransition(int fromColor, int toColor, float progress) {
        progress = EasingFunctions.easeInOutCubic(progress);
        return EasingFunctions.lerpColor(fromColor, toColor, progress);
    }
    
    /**
     * Create sparkle effect color
     */
    public static int getSparkleColor(int baseColor, float time) {
        float sparkle = (float) (Math.sin(time * 10) * 0.5 + 0.5);
        return blendColors(baseColor, Color.WHITE, sparkle * 0.3f);
    }
}
