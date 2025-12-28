package com.edu.english.storybook.model;

/**
 * Story setup configuration from BottomSheet
 */
public class StorySetup {
    private String category;
    private int age;
    private String readingLevel; // "A1" or "A2"
    private String length; // "short", "medium", "long"
    private String theme; // optional
    private String characterName; // optional

    public StorySetup() {
        // Defaults
        this.age = 7;
        this.readingLevel = "A1";
        this.length = "medium";
    }

    public StorySetup(String category) {
        this();
        this.category = category;
    }
    
    public StorySetup(StoryCategory category, int age, String readingLevel, String length, String theme, String characterName) {
        this.category = category.name();
        this.age = age;
        this.readingLevel = readingLevel;
        this.length = length;
        this.theme = theme;
        this.characterName = characterName;
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public void setCategory(StoryCategory category) { this.category = category.name(); }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = Math.max(4, Math.min(12, age)); }

    public String getReadingLevel() { return readingLevel; }
    public void setReadingLevel(String readingLevel) { this.readingLevel = readingLevel; }
    
    // Alias for getLevel
    public String getLevel() { return readingLevel; }

    public String getLength() { return length; }
    public void setLength(String length) { this.length = length; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { 
        if (theme != null && theme.length() > 60) {
            this.theme = theme.substring(0, 60);
        } else {
            this.theme = theme;
        }
    }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) {
        if (characterName != null && characterName.length() > 20) {
            this.characterName = characterName.substring(0, 20);
        } else {
            this.characterName = characterName;
        }
    }

    /**
     * Get chapter count based on length
     */
    public int getChapterCount() {
        switch (length) {
            case "short": return 3;
            case "medium": return 5;
            case "long": return 7;
            default: return 5;
        }
    }

    /**
     * Get word count range based on length
     */
    public int[] getWordCountRange() {
        switch (length) {
            case "short": return new int[]{500, 800};
            case "medium": return new int[]{800, 1200};
            case "long": return new int[]{1200, 1800};
            default: return new int[]{800, 1200};
        }
    }

    /**
     * Validate setup
     */
    public boolean isValid() {
        return category != null && !category.isEmpty() 
            && age >= 5 && age <= 10
            && (readingLevel.equals("A1") || readingLevel.equals("A2"))
            && (length.equals("short") || length.equals("medium") || length.equals("long"));
    }
}
