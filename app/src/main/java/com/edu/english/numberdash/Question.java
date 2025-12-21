package com.edu.english.numberdash;

/**
 * Represents a question in Number Dash Race
 */
public class Question {
    
    public enum QuestionType {
        COUNTING,
        ADDITION,
        COMPARISON
    }
    
    private String questionText;
    private String visualRepresentation;
    private int correctAnswer;
    private int[] options;
    private QuestionType type;
    
    public Question(String questionText, String visualRepresentation, 
                   int correctAnswer, int[] options, QuestionType type) {
        this.questionText = questionText;
        this.visualRepresentation = visualRepresentation;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.type = type;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public String getVisualRepresentation() {
        return visualRepresentation;
    }
    
    public int getCorrectAnswer() {
        return correctAnswer;
    }
    
    public int[] getOptions() {
        return options;
    }
    
    public QuestionType getType() {
        return type;
    }
    
    public int getOptionCount() {
        return options.length;
    }
    
    public int getOption(int index) {
        if (index >= 0 && index < options.length) {
            return options[index];
        }
        return -1;
    }
}
