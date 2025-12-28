package com.edu.english.storybook.model;

/**
 * Vocabulary item model
 */
public class VocabularyItem {
    private String word;
    private String meaningVi;
    private String exampleSentence;

    public VocabularyItem() {}

    public VocabularyItem(String word, String meaningVi, String exampleSentence) {
        this.word = word;
        this.meaningVi = meaningVi;
        this.exampleSentence = exampleSentence;
    }

    // Getters and Setters
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getMeaningVi() { return meaningVi; }
    public void setMeaningVi(String meaningVi) { this.meaningVi = meaningVi; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }
}
