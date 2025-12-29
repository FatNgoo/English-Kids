package com.edu.english.masterchef.engine;

/**
 * Calculates score and stars for level completion
 * Factors: correct actions, time taken, hints used, errors made
 */
public class ScoreCalculator {
    
    private static final int BASE_SCORE = 100;
    private static final int TIME_BONUS_MAX = 50;
    private static final int PERFECT_BONUS = 30;
    private static final int ERROR_PENALTY = -10;
    private static final int HINT_PENALTY = -5;

    /**
     * Calculate final score for a level
     * 
     * @param correctActions Number of correct actions performed
     * @param totalActions Total actions required
     * @param errors Number of errors made
     * @param hintsUsed Number of hints used
     * @param timeSeconds Time taken in seconds
     * @param targetTimeSeconds Target time for bonus
     * @return Final score (0-200+)
     */
    public int calculateScore(int correctActions, int totalActions, int errors, 
                             int hintsUsed, int timeSeconds, int targetTimeSeconds) {
        
        // Base score from correct actions
        int score = BASE_SCORE;
        
        // Completion accuracy
        float accuracy = (float) correctActions / totalActions;
        int accuracyScore = (int) (BASE_SCORE * accuracy);
        
        // Time bonus (faster = more bonus)
        int timeBonus = 0;
        if (timeSeconds <= targetTimeSeconds) {
            float timeRatio = 1f - ((float) timeSeconds / targetTimeSeconds);
            timeBonus = (int) (TIME_BONUS_MAX * timeRatio);
        }
        
        // Perfect run bonus (no errors)
        int perfectBonus = (errors == 0) ? PERFECT_BONUS : 0;
        
        // Penalties
        int errorPenalty = errors * ERROR_PENALTY;
        int hintPenalty = hintsUsed * HINT_PENALTY;
        
        // Calculate final score
        int finalScore = accuracyScore + timeBonus + perfectBonus + errorPenalty + hintPenalty;
        
        // Clamp to minimum 0
        return Math.max(0, finalScore);
    }

    /**
     * Simple score calculation for quick wins
     */
    public int calculateSimpleScore(int errors, int hintsUsed) {
        int score = BASE_SCORE;
        score += errors * ERROR_PENALTY;
        score += hintsUsed * HINT_PENALTY;
        return Math.max(0, score);
    }

    /**
     * Calculate stars based on score and thresholds
     */
    public int calculateStars(int score, int star1Threshold, int star2Threshold, int star3Threshold) {
        if (score >= star3Threshold) return 3;
        if (score >= star2Threshold) return 2;
        if (score >= star1Threshold) return 1;
        return 0;
    }
}
