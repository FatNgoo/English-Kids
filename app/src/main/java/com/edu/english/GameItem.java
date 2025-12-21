package com.edu.english;

public class GameItem {
    private String title;
    private int backgroundColor;
    private String gameType;

    public GameItem(String title, int backgroundColor, String gameType) {
        this.title = title;
        this.backgroundColor = backgroundColor;
        this.gameType = gameType;
    }

    public String getTitle() {
        return title;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public String getGameType() {
        return gameType;
    }
}
