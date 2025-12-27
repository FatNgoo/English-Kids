package com.edu.english.magicmelody.model;

import java.util.List;
import java.util.Map;

/**
 * 🌍 ThemeConfig Model
 * 
 * Purpose: Represents a world theme loaded from JSON
 * Contains visual, audio, and character configuration
 */
public class ThemeConfig {
    
    private String themeId;
    private String name;
    private String displayName;
    private String description;
    private String bgMusic;
    private String ambientSound;
    private ColorPalette colorPalette;
    private List<ThemeCharacter> characters;
    private BossConfig boss;
    private Map<String, EvolutionStage> evolutionStages;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ INNER CLASSES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Color palette for the theme
     */
    public static class ColorPalette {
        private String primary;
        private String secondary;
        private String accent;
        private String background;
        private String grayState;
        
        public String getPrimary() { return primary; }
        public void setPrimary(String primary) { this.primary = primary; }
        
        public String getSecondary() { return secondary; }
        public void setSecondary(String secondary) { this.secondary = secondary; }
        
        public String getAccent() { return accent; }
        public void setAccent(String accent) { this.accent = accent; }
        
        public String getBackground() { return background; }
        public void setBackground(String background) { this.background = background; }
        
        public String getGrayState() { return grayState; }
        public void setGrayState(String grayState) { this.grayState = grayState; }
    }
    
    /**
     * Character configuration for the theme
     */
    public static class ThemeCharacter {
        private String id;
        private String name;
        private String role;
        private String modelFile;
        private List<String> greetings;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getModelFile() { return modelFile; }
        public void setModelFile(String modelFile) { this.modelFile = modelFile; }
        
        public List<String> getGreetings() { return greetings; }
        public void setGreetings(List<String> greetings) { this.greetings = greetings; }
        
        /**
         * Get a random greeting
         */
        public String getRandomGreeting() {
            if (greetings == null || greetings.isEmpty()) {
                return "Hello!";
            }
            int index = (int) (Math.random() * greetings.size());
            return greetings.get(index);
        }
    }
    
    /**
     * Boss configuration for the theme
     */
    public static class BossConfig {
        private String id;
        private String name;
        private int health;
        private List<String> weakness;
        private String modelFile;
        private BossDialogue dialogue;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getHealth() { return health; }
        public void setHealth(int health) { this.health = health; }
        
        public List<String> getWeakness() { return weakness; }
        public void setWeakness(List<String> weakness) { this.weakness = weakness; }
        
        public String getModelFile() { return modelFile; }
        public void setModelFile(String modelFile) { this.modelFile = modelFile; }
        
        public BossDialogue getDialogue() { return dialogue; }
        public void setDialogue(BossDialogue dialogue) { this.dialogue = dialogue; }
        
        /**
         * Check if a note is the boss's weakness
         */
        public boolean isWeakTo(String note) {
            return weakness != null && weakness.contains(note.toLowerCase());
        }
    }
    
    /**
     * Boss dialogue strings
     */
    public static class BossDialogue {
        private String intro;
        private String damage;
        private String defeat;
        
        public String getIntro() { return intro; }
        public void setIntro(String intro) { this.intro = intro; }
        
        public String getDamage() { return damage; }
        public void setDamage(String damage) { this.damage = damage; }
        
        public String getDefeat() { return defeat; }
        public void setDefeat(String defeat) { this.defeat = defeat; }
    }
    
    /**
     * Evolution stage configuration
     */
    public static class EvolutionStage {
        private String description;
        private float saturation;
        private String particleEffect;
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public float getSaturation() { return saturation; }
        public void setSaturation(float saturation) { this.saturation = saturation; }
        
        public String getParticleEffect() { return particleEffect; }
        public void setParticleEffect(String particleEffect) { this.particleEffect = particleEffect; }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBgMusic() {
        return bgMusic;
    }

    public void setBgMusic(String bgMusic) {
        this.bgMusic = bgMusic;
    }

    public String getAmbientSound() {
        return ambientSound;
    }

    public void setAmbientSound(String ambientSound) {
        this.ambientSound = ambientSound;
    }

    public ColorPalette getColorPalette() {
        return colorPalette;
    }

    public void setColorPalette(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
    }

    public List<ThemeCharacter> getCharacters() {
        return characters;
    }

    public void setCharacters(List<ThemeCharacter> characters) {
        this.characters = characters;
    }

    public BossConfig getBoss() {
        return boss;
    }

    public void setBoss(BossConfig boss) {
        this.boss = boss;
    }

    public Map<String, EvolutionStage> getEvolutionStages() {
        return evolutionStages;
    }

    public void setEvolutionStages(Map<String, EvolutionStage> evolutionStages) {
        this.evolutionStages = evolutionStages;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get guide character for this theme
     */
    public ThemeCharacter getGuideCharacter() {
        if (characters == null) return null;
        for (ThemeCharacter character : characters) {
            if ("guide".equals(character.getRole())) {
                return character;
            }
        }
        return characters.isEmpty() ? null : characters.get(0);
    }
    
    /**
     * Get evolution stage by index
     */
    public EvolutionStage getEvolutionStage(int stage) {
        if (evolutionStages == null) return null;
        return evolutionStages.get("stage" + stage);
    }
    
    /**
     * Get audio path for background music
     */
    public String getBgMusicPath() {
        return "magicmelody/audio/themes/" + bgMusic;
    }
    
    /**
     * Get audio path for ambient sound
     */
    public String getAmbientSoundPath() {
        return "magicmelody/audio/themes/" + ambientSound;
    }
}
