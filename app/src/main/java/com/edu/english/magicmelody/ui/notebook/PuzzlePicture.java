package com.edu.english.magicmelody.ui.notebook;

import java.util.List;

/**
 * 🧩 Puzzle Picture Data Model
 * Represents a collected puzzle picture from a completed level
 */
public class PuzzlePicture {
    private String worldId;
    private int level;
    private String worldName;
    private String worldEmoji;
    private int awakenedCount;
    private int totalPieces;
    private List<String> awakenedWords;
    private String keyword; // Từ khóa cho mini game

    public PuzzlePicture(String worldId, int level, String worldName, String worldEmoji, 
                        int awakenedCount, int totalPieces, List<String> awakenedWords, String keyword) {
        this.worldId = worldId;
        this.level = level;
        this.worldName = worldName;
        this.worldEmoji = worldEmoji;
        this.awakenedCount = awakenedCount;
        this.totalPieces = totalPieces;
        this.awakenedWords = awakenedWords;
        this.keyword = keyword;
    }

    // Getters
    public String getWorldId() { return worldId; }
    public int getLevel() { return level; }
    public String getWorldName() { return worldName; }
    public String getWorldEmoji() { return worldEmoji; }
    public int getAwakenedCount() { return awakenedCount; }
    public int getTotalPieces() { return totalPieces; }
    public List<String> getAwakenedWords() { return awakenedWords; }
    public String getKeyword() { return keyword; }

    public boolean isComplete() {
        return awakenedCount == totalPieces;
    }
}
