package com.edu.english;

public class GameItem {
    private String title;
    private int backgroundColor;
    private String gameType;
    private String subtitle;

    public GameItem(String title, int backgroundColor, String gameType) {
        this.title = title;
        this.backgroundColor = backgroundColor;
        this.gameType = gameType;
        this.subtitle = null;
    }

    public GameItem(String title, int backgroundColor, String gameType, String subtitle) {
        this.title = title;
        this.backgroundColor = backgroundColor;
        this.gameType = gameType;
        this.subtitle = subtitle;
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

    public String getSubtitle() {
        return subtitle;
    }

    public boolean hasSubtitle() {
        return subtitle != null && !subtitle.isEmpty();
    }
}
