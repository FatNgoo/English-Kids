package com.edu.english.numbers.models;

/**
 * Model representing a counting question for a scene
 */
public class NumberQuestion {
    
    private final int sceneId;
    private final String prompt;           // English question text
    private final int answerNumber;        // Correct answer as number
    private final String answerWord;       // Correct answer as word (e.g., "five")
    private final String focusKeyword;     // Optional highlight text (e.g., "red apples")
    
    public NumberQuestion(int sceneId, String prompt, int answerNumber, String answerWord) {
        this(sceneId, prompt, answerNumber, answerWord, null);
    }
    
    public NumberQuestion(int sceneId, String prompt, int answerNumber, String answerWord, String focusKeyword) {
        this.sceneId = sceneId;
        this.prompt = prompt;
        this.answerNumber = answerNumber;
        this.answerWord = answerWord;
        this.focusKeyword = focusKeyword;
    }
    
    public int getSceneId() {
        return sceneId;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public int getAnswerNumber() {
        return answerNumber;
    }
    
    public String getAnswerWord() {
        return answerWord;
    }
    
    public String getFocusKeyword() {
        return focusKeyword;
    }
    
    public boolean hasFocusKeyword() {
        return focusKeyword != null && !focusKeyword.isEmpty();
    }
    
    /**
     * Check if the given number is the correct answer
     */
    public boolean isCorrectNumber(int number) {
        return answerNumber == number;
    }
    
    /**
     * Check if the spoken word matches the answer word
     * Handles common speech recognition variants
     */
    public boolean isCorrectSpeech(String spokenText) {
        if (spokenText == null || spokenText.isEmpty()) {
            return false;
        }
        
        // Normalize: lowercase, trim, remove punctuation
        String normalized = spokenText.toLowerCase().trim()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim();
        
        // Direct match
        if (normalized.equals(answerWord.toLowerCase())) {
            return true;
        }
        
        // Handle common speech recognition errors/variants
        return matchWithVariants(normalized);
    }
    
    private boolean matchWithVariants(String normalized) {
        String target = answerWord.toLowerCase();
        
        // Common homophones and recognition errors
        switch (target) {
            case "one":
                return normalized.equals("one") || normalized.equals("won") || normalized.equals("1");
            case "two":
                return normalized.equals("two") || normalized.equals("to") || normalized.equals("too") || normalized.equals("2");
            case "three":
                return normalized.equals("three") || normalized.equals("tree") || normalized.equals("3");
            case "four":
                return normalized.equals("four") || normalized.equals("for") || normalized.equals("fore") || normalized.equals("4");
            case "five":
                return normalized.equals("five") || normalized.equals("fife") || normalized.equals("5");
            case "six":
                return normalized.equals("six") || normalized.equals("6");
            case "seven":
                return normalized.equals("seven") || normalized.equals("7");
            case "eight":
                return normalized.equals("eight") || normalized.equals("ate") || normalized.equals("8");
            case "nine":
                return normalized.equals("nine") || normalized.equals("9");
            case "ten":
                return normalized.equals("ten") || normalized.equals("10");
            default:
                return normalized.equals(target);
        }
    }
    
    @Override
    public String toString() {
        return "NumberQuestion{" +
                "sceneId=" + sceneId +
                ", prompt='" + prompt + '\'' +
                ", answer=" + answerNumber + " (" + answerWord + ")" +
                '}';
    }
}
