package com.edu.english.masterchef.data;

/**
 * Represents a chef character that guides the player.
 * Each chef has unique appearance and voice settings.
 */
public class ChefCharacter {
    
    private String id;
    private String displayName;
    private String displayNameVi;
    private String avatarDrawable;    // drawable resource name
    private String bubbleColor;       // hex color for speech bubble
    
    // Voice configuration
    private float speechRate;         // 0.5 - 2.0 (1.0 = normal)
    private float pitch;              // 0.5 - 2.0 (1.0 = normal)
    
    // Unlock status
    private boolean isUnlocked;
    private int unlockStarsRequired;
    
    // Phrases
    private String greetingEn;
    private String praiseEn;
    private String encourageEn;
    
    public ChefCharacter() {}
    
    public ChefCharacter(String id, String displayName, String displayNameVi,
                         String avatarDrawable, String bubbleColor,
                         float speechRate, float pitch,
                         boolean isUnlocked, int unlockStarsRequired,
                         String greetingEn, String praiseEn, String encourageEn) {
        this.id = id;
        this.displayName = displayName;
        this.displayNameVi = displayNameVi;
        this.avatarDrawable = avatarDrawable;
        this.bubbleColor = bubbleColor;
        this.speechRate = speechRate;
        this.pitch = pitch;
        this.isUnlocked = isUnlocked;
        this.unlockStarsRequired = unlockStarsRequired;
        this.greetingEn = greetingEn;
        this.praiseEn = praiseEn;
        this.encourageEn = encourageEn;
    }
    
    // === Builder Pattern ===
    
    public static class Builder {
        private ChefCharacter chef = new ChefCharacter();
        
        public Builder id(String id) { chef.id = id; return this; }
        public Builder displayName(String name) { chef.displayName = name; return this; }
        public Builder displayNameVi(String name) { chef.displayNameVi = name; return this; }
        public Builder avatar(String drawable) { chef.avatarDrawable = drawable; return this; }
        public Builder bubbleColor(String color) { chef.bubbleColor = color; return this; }
        public Builder speechRate(float rate) { chef.speechRate = rate; return this; }
        public Builder pitch(float pitch) { chef.pitch = pitch; return this; }
        public Builder unlocked(boolean unlocked) { chef.isUnlocked = unlocked; return this; }
        public Builder unlockStars(int stars) { chef.unlockStarsRequired = stars; return this; }
        public Builder greeting(String greeting) { chef.greetingEn = greeting; return this; }
        public Builder praise(String praise) { chef.praiseEn = praise; return this; }
        public Builder encourage(String encourage) { chef.encourageEn = encourage; return this; }
        
        public ChefCharacter build() { return chef; }
    }
    
    // === Getters ===
    
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDisplayNameVi() { return displayNameVi; }
    public String getAvatarDrawable() { return avatarDrawable; }
    public String getBubbleColor() { return bubbleColor; }
    public float getSpeechRate() { return speechRate; }
    public float getPitch() { return pitch; }
    public boolean isUnlocked() { return isUnlocked; }
    public int getUnlockStarsRequired() { return unlockStarsRequired; }
    public String getGreetingEn() { return greetingEn; }
    public String getPraiseEn() { return praiseEn; }
    public String getEncourageEn() { return encourageEn; }
    
    // === Setters ===
    
    public void setId(String id) { this.id = id; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDisplayNameVi(String displayNameVi) { this.displayNameVi = displayNameVi; }
    public void setAvatarDrawable(String avatarDrawable) { this.avatarDrawable = avatarDrawable; }
    public void setBubbleColor(String bubbleColor) { this.bubbleColor = bubbleColor; }
    public void setSpeechRate(float speechRate) { this.speechRate = speechRate; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
    public void setUnlockStarsRequired(int unlockStarsRequired) { this.unlockStarsRequired = unlockStarsRequired; }
    public void setGreetingEn(String greetingEn) { this.greetingEn = greetingEn; }
    public void setPraiseEn(String praiseEn) { this.praiseEn = praiseEn; }
    public void setEncourageEn(String encourageEn) { this.encourageEn = encourageEn; }
}
