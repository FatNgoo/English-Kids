package com.edu.english.alphabet_adventure.models;

/**
 * Model representing a mascot character for the game.
 */
public class Mascot {
    private int id;
    private String name;
    private String emoji;
    private int colorResId;

    public Mascot(int id, String name, String emoji, int colorResId) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.colorResId = colorResId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getColorResId() {
        return colorResId;
    }
}
