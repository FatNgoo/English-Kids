package com.edu.english.storybook.model;

import java.util.List;

/**
 * Model for story outline returned from first API call
 */
public class StoryOutline {
    private String title;
    private List<ChapterOutline> chapters;
    private List<String> vocabSeed;
    private List<String> questionTopics;

    public StoryOutline() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<ChapterOutline> getChapters() { return chapters; }
    public void setChapters(List<ChapterOutline> chapters) { this.chapters = chapters; }

    public List<String> getVocabSeed() { return vocabSeed; }
    public void setVocabSeed(List<String> vocabSeed) { this.vocabSeed = vocabSeed; }

    public List<String> getQuestionTopics() { return questionTopics; }
    public void setQuestionTopics(List<String> questionTopics) { this.questionTopics = questionTopics; }

    /**
     * Chapter outline for initial outline
     */
    public static class ChapterOutline {
        private String heading;
        private String summary;

        public ChapterOutline() {}

        public ChapterOutline(String heading, String summary) {
            this.heading = heading;
            this.summary = summary;
        }

        public String getHeading() { return heading; }
        public void setHeading(String heading) { this.heading = heading; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }
}
