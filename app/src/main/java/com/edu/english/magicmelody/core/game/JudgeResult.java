package com.edu.english.magicmelody.core.game;

/**
 * JudgeResult - Result of judging a note hit
 * Contains timing accuracy and score information
 */
public class JudgeResult {
    
    // Judgment types
    public enum Judgment {
        PERFECT,    // Within perfect timing window
        GOOD,       // Within good timing window
        MISS,       // Missed entirely
        NONE        // No note to judge
    }
    
    private Judgment judgment;
    private int lane;
    private long timingDiff;    // Difference from perfect timing (negative = early, positive = late)
    private int score;
    private String vocabKey;
    private boolean isGlitchNote;
    
    // Score values
    public static final int SCORE_PERFECT = 100;
    public static final int SCORE_GOOD = 50;
    public static final int SCORE_MISS = 0;
    
    public JudgeResult() {
        this.judgment = Judgment.NONE;
    }
    
    public JudgeResult(Judgment judgment, int lane, long timingDiff, String vocabKey) {
        this.judgment = judgment;
        this.lane = lane;
        this.timingDiff = timingDiff;
        this.vocabKey = vocabKey;
        this.score = calculateScore(judgment);
    }
    
    /**
     * Calculate score based on judgment
     */
    public static int calculateScore(Judgment judgment) {
        switch (judgment) {
            case PERFECT: return SCORE_PERFECT;
            case GOOD: return SCORE_GOOD;
            case MISS: return SCORE_MISS;
            default: return 0;
        }
    }
    
    /**
     * Get feedback message in Vietnamese
     */
    public String getFeedbackMessage() {
        switch (judgment) {
            case PERFECT: return "Tuyệt vời!";
            case GOOD: return "Tốt lắm!";
            case MISS: return "Oops! Thử lại nha!";
            default: return "";
        }
    }
    
    /**
     * Get timing feedback
     */
    public String getTimingFeedback() {
        if (judgment == Judgment.MISS || judgment == Judgment.NONE) {
            return "";
        }
        
        if (Math.abs(timingDiff) < 30) {
            return "Chính xác!";
        } else if (timingDiff < 0) {
            return "Hơi sớm";
        } else {
            return "Hơi muộn";
        }
    }
    
    /**
     * Check if this is a successful hit (PERFECT or GOOD)
     */
    public boolean isHit() {
        return judgment == Judgment.PERFECT || judgment == Judgment.GOOD;
    }
    
    /**
     * Get combo multiplier based on judgment
     */
    public float getComboMultiplier() {
        switch (judgment) {
            case PERFECT: return 1.5f;
            case GOOD: return 1.0f;
            default: return 0f;
        }
    }
    
    // Getters and Setters
    public Judgment getJudgment() { return judgment; }
    public void setJudgment(Judgment judgment) { 
        this.judgment = judgment;
        this.score = calculateScore(judgment);
    }
    
    public int getLane() { return lane; }
    public void setLane(int lane) { this.lane = lane; }
    
    public long getTimingDiff() { return timingDiff; }
    public void setTimingDiff(long timingDiff) { this.timingDiff = timingDiff; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public String getVocabKey() { return vocabKey; }
    public void setVocabKey(String vocabKey) { this.vocabKey = vocabKey; }
    
    public boolean isGlitchNote() { return isGlitchNote; }
    public void setGlitchNote(boolean glitchNote) { isGlitchNote = glitchNote; }
}
