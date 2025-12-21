package com.edu.english;

public class LessonItem {
    private String title;
    private int backgroundColor;
    private String lessonType;

    public LessonItem(String title, int backgroundColor, String lessonType) {
        this.title = title;
        this.backgroundColor = backgroundColor;
        this.lessonType = lessonType;
    }

    public String getTitle() {
        return title;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public String getLessonType() {
        return lessonType;
    }
}
