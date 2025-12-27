package com.edu.english.storybook.model;

/**
 * Chapter model for story
 */
public class Chapter {
    private String heading;
    private String summary;
    private String content;

    public Chapter() {}

    public Chapter(String heading, String content) {
        this.heading = heading;
        this.content = content;
    }

    public Chapter(String heading, String summary, String content) {
        this.heading = heading;
        this.summary = summary;
        this.content = content;
    }

    // Getters and Setters
    public String getHeading() { return heading; }
    public void setHeading(String heading) { this.heading = heading; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
