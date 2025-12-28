package com.edu.english.storybook.model;

/**
 * Story category enum with display info
 */
public enum StoryCategory {
    NOVEL("Novel", "Cozy adventures and friends", "novel"),
    FAIRY_TALES("Fairy Tales", "Magic, castles, happy endings", "fairy_tales"),
    SEE_THE_WORLD("See the world", "Discover places & fun facts", "see_the_world"),
    HISTORY("History", "Time travel to the past", "history");

    private final String displayName;
    private final String description;
    private final String id;

    StoryCategory(String displayName, String description, String id) {
        this.displayName = displayName;
        this.description = description;
        this.id = id;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getId() { return id; }

    public static StoryCategory fromId(String id) {
        for (StoryCategory category : values()) {
            if (category.id.equals(id)) {
                return category;
            }
        }
        return NOVEL;
    }

    /**
     * Get random kid-safe theme suggestions for "Surprise me" feature
     */
    public String[] getThemeSuggestions() {
        switch (this) {
            case NOVEL:
                return new String[]{
                    "a brave little mouse who saves the day",
                    "best friends on a camping adventure",
                    "a curious kitten exploring the neighborhood",
                    "a friendly robot learning about feelings",
                    "a young artist who paints magical pictures"
                };
            case FAIRY_TALES:
                return new String[]{
                    "a kind princess who befriends a dragon",
                    "a tiny fairy who grants wishes",
                    "a magical garden where flowers sing",
                    "a brave knight who makes peace with trolls",
                    "a talking frog who helps lost children"
                };
            case SEE_THE_WORLD:
                return new String[]{
                    "discovering the wonders of the rainforest",
                    "a journey to see the Northern Lights",
                    "exploring an ancient temple in the jungle",
                    "learning about sea creatures in the ocean",
                    "visiting a village in the mountains"
                };
            case HISTORY:
                return new String[]{
                    "meeting a young inventor in ancient times",
                    "a day in the life of a medieval baker",
                    "discovering how pyramids were built",
                    "exploring an ancient library",
                    "learning from a wise teacher long ago"
                };
            default:
                return new String[]{"a fun adventure with friends"};
        }
    }
}
