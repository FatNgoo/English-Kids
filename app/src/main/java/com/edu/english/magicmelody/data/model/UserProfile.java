package com.edu.english.magicmelody.data.model;

/**
 * User profile for personalization
 * Stores age group and theme preferences
 */
public class UserProfile {
    
    public enum AgeGroup {
        TODDLER,   // 3-5 years: 60 BPM, large notes, wide timing
        EXPLORER,  // 6-8 years: 90 BPM, medium
        MASTER     // 9+ years: 120 BPM, challenging
    }
    
    public enum Theme {
        NATURE,
        FANTASY,
        SCIFI
    }
    
    private AgeGroup ageGroup;
    private Theme theme;
    private String displayName;
    private int totalStars;
    private int levelsCompleted;
    
    public UserProfile() {
        this.ageGroup = AgeGroup.EXPLORER; // Default
        this.theme = Theme.NATURE; // Default
        this.displayName = "Vệ Thần Nhí";
    }
    
    public UserProfile(AgeGroup ageGroup, Theme theme) {
        this.ageGroup = ageGroup;
        this.theme = theme;
        this.displayName = "Vệ Thần Nhí";
    }
    
    // Timing windows based on age group (in milliseconds)
    public int getPerfectWindow() {
        switch (ageGroup) {
            case TODDLER: return 220;
            case EXPLORER: return 160;
            case MASTER: return 120;
            default: return 160;
        }
    }
    
    public int getGoodWindow() {
        switch (ageGroup) {
            case TODDLER: return 380;
            case EXPLORER: return 280;
            case MASTER: return 220;
            default: return 280;
        }
    }
    
    public int getBpm() {
        switch (ageGroup) {
            case TODDLER: return 60;
            case EXPLORER: return 90;
            case MASTER: return 120;
            default: return 90;
        }
    }
    
    public int getGlitchNoteInterval() {
        switch (ageGroup) {
            case TODDLER: return 12; // Easier, less frequent
            case EXPLORER: return 8;
            case MASTER: return 5; // More challenging
            default: return 8;
        }
    }
    
    // Getters and Setters
    public AgeGroup getAgeGroup() { return ageGroup; }
    public void setAgeGroup(AgeGroup ageGroup) { this.ageGroup = ageGroup; }
    
    public Theme getTheme() { return theme; }
    public void setTheme(Theme theme) { this.theme = theme; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public int getTotalStars() { return totalStars; }
    public void setTotalStars(int totalStars) { this.totalStars = totalStars; }
    public void addStars(int stars) { this.totalStars += stars; }
    
    public int getLevelsCompleted() { return levelsCompleted; }
    public void setLevelsCompleted(int levelsCompleted) { this.levelsCompleted = levelsCompleted; }
    public void incrementLevelsCompleted() { this.levelsCompleted++; }
}
