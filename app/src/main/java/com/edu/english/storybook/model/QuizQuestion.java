package com.edu.english.storybook.model;

import java.util.List;

/**
 * Quiz question model
 */
public class QuizQuestion {
    private String question;
    private List<String> choices;
    private int answerIndex;
    private String explanation;

    public QuizQuestion() {}

    public QuizQuestion(String question, List<String> choices, int answerIndex) {
        this.question = question;
        this.choices = choices;
        this.answerIndex = answerIndex;
    }

    public QuizQuestion(String question, List<String> choices, int answerIndex, String explanation) {
        this.question = question;
        this.choices = choices;
        this.answerIndex = answerIndex;
        this.explanation = explanation;
    }

    // Getters and Setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) { this.choices = choices; }

    public int getAnswerIndex() { return answerIndex; }
    public void setAnswerIndex(int answerIndex) { this.answerIndex = answerIndex; }
    
    // Alias for getCorrectIndex
    public int getCorrectIndex() { return answerIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getCorrectAnswer() {
        if (choices != null && answerIndex >= 0 && answerIndex < choices.size()) {
            return choices.get(answerIndex);
        }
        return null;
    }
}
