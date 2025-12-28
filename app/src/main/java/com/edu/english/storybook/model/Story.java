package com.edu.english.storybook.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

/**
 * Main Story model containing all story data
 */
public class Story {
    private String id;
    private String title;
    private String category;
    private int age;
    private String readingLevel;
    private List<Chapter> chapters;
    private List<VocabularyItem> vocabulary;
    private List<QuizQuestion> questions;
    private long createdAt;

    private static final Gson gson = new GsonBuilder().create();

    public Story() {
        this.createdAt = System.currentTimeMillis();
    }

    public Story(String id, String title, String category, int age, String readingLevel,
                 List<Chapter> chapters, List<VocabularyItem> vocabulary, List<QuizQuestion> questions) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.age = age;
        this.readingLevel = readingLevel;
        this.chapters = chapters;
        this.vocabulary = vocabulary;
        this.questions = questions;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public StoryCategory getCategory() { 
        try {
            return StoryCategory.valueOf(category);
        } catch (Exception e) {
            return StoryCategory.NOVEL;
        }
    }
    public void setCategory(String category) { this.category = category; }
    public void setCategory(StoryCategory category) { this.category = category.name(); }

    public int getTargetAge() { return age; }
    public void setTargetAge(int age) { this.age = age; }
    
    // Alias methods for compatibility
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getLevel() { return readingLevel; }
    public void setLevel(String readingLevel) { this.readingLevel = readingLevel; }
    
    public String getReadingLevel() { return readingLevel; }
    public void setReadingLevel(String readingLevel) { this.readingLevel = readingLevel; }

    public List<Chapter> getChapters() { return chapters; }
    public void setChapters(List<Chapter> chapters) { this.chapters = chapters; }

    public List<VocabularyItem> getVocabulary() { return vocabulary; }
    public void setVocabulary(List<VocabularyItem> vocabulary) { this.vocabulary = vocabulary; }

    public List<QuizQuestion> getQuizQuestions() { return questions; }
    public void setQuizQuestions(List<QuizQuestion> questions) { this.questions = questions; }
    
    // Alias for setQuestions
    public List<QuizQuestion> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestion> questions) { this.questions = questions; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /**
     * Get total word count of story
     */
    public int getTotalWordCount() {
        if (chapters == null) return 0;
        int count = 0;
        for (Chapter chapter : chapters) {
            if (chapter.getContent() != null) {
                count += chapter.getContent().split("\\s+").length;
            }
        }
        return count;
    }

    /**
     * Serialize story to JSON string
     */
    public String toJson() {
        return gson.toJson(this);
    }

    /**
     * Deserialize story from JSON string
     */
    public static Story fromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return gson.fromJson(json, Story.class);
        } catch (Exception e) {
            return null;
        }
    }
}