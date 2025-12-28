package com.edu.english.magicmelody.model;

/**
 * 🎯 GameResult Model
 * 
 * Purpose: Represents the result of a completed rhythm game session
 * Used to pass data between gameplay and result screens
 */
public class GameResult {
    
    private String lessonId;
    private String worldId;
    private int levelNumber;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SCORE DATA
    // ═══════════════════════════════════════════════════════════════
    
    private int totalScore;
    private int maxPossibleScore;
    private int starsEarned;
    private int newStarsEarned; // Stars gained (improvement)
    
    // ═══════════════════════════════════════════════════════════════
    // 🎵 HIT DATA
    // ═══════════════════════════════════════════════════════════════
    
    private int totalNotes;
    private int perfectHits;
    private int goodHits;
    private int okHits;
    private int missCount;
    
    // ═══════════════════════════════════════════════════════════════
    // 🔥 COMBO DATA
    // ═══════════════════════════════════════════════════════════════
    
    private int maxCombo;
    private int totalComboBonus;
    
    // ═══════════════════════════════════════════════════════════════
    // ⏱️ TIME DATA
    // ═══════════════════════════════════════════════════════════════
    
    private long startTime;
    private long endTime;
    private long durationMs;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏆 ACHIEVEMENTS
    // ═══════════════════════════════════════════════════════════════
    
    private boolean isNewHighScore;
    private boolean isFirstCompletion;
    private boolean isPerfectGame; // No misses
    private boolean isFullCombo;   // Max combo equals total notes
    
    // ═══════════════════════════════════════════════════════════════
    // 📚 COLLECTED ITEMS
    // ═══════════════════════════════════════════════════════════════
    
    private int notesCollected;
    private int wordsLearned;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public GameResult() {
        // Default constructor
    }
    
    /**
     * Builder pattern for creating GameResult
     */
    public static class Builder {
        private GameResult result = new GameResult();
        
        public Builder lessonId(String lessonId) {
            result.lessonId = lessonId;
            return this;
        }
        
        public Builder worldId(String worldId) {
            result.worldId = worldId;
            return this;
        }
        
        public Builder levelNumber(int level) {
            result.levelNumber = level;
            return this;
        }
        
        public Builder score(int total, int max) {
            result.totalScore = total;
            result.maxPossibleScore = max;
            return this;
        }
        
        public Builder hits(int perfect, int good, int ok, int miss) {
            result.perfectHits = perfect;
            result.goodHits = good;
            result.okHits = ok;
            result.missCount = miss;
            result.totalNotes = perfect + good + ok + miss;
            return this;
        }
        
        public Builder combo(int maxCombo, int bonus) {
            result.maxCombo = maxCombo;
            result.totalComboBonus = bonus;
            return this;
        }
        
        public Builder time(long startTime, long endTime) {
            result.startTime = startTime;
            result.endTime = endTime;
            result.durationMs = endTime - startTime;
            return this;
        }
        
        public GameResult build() {
            // Calculate stars
            result.calculateStars();
            // Detect achievements
            result.detectAchievements();
            return result;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getWorldId() { return worldId; }
    public void setWorldId(String worldId) { this.worldId = worldId; }

    public int getLevelNumber() { return levelNumber; }
    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public int getMaxPossibleScore() { return maxPossibleScore; }
    public void setMaxPossibleScore(int maxPossibleScore) { this.maxPossibleScore = maxPossibleScore; }

    public int getStarsEarned() { return starsEarned; }
    public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }

    public int getNewStarsEarned() { return newStarsEarned; }
    public void setNewStarsEarned(int newStarsEarned) { this.newStarsEarned = newStarsEarned; }

    public int getTotalNotes() { return totalNotes; }
    public void setTotalNotes(int totalNotes) { this.totalNotes = totalNotes; }

    public int getPerfectHits() { return perfectHits; }
    public void setPerfectHits(int perfectHits) { this.perfectHits = perfectHits; }

    public int getGoodHits() { return goodHits; }
    public void setGoodHits(int goodHits) { this.goodHits = goodHits; }

    public int getOkHits() { return okHits; }
    public void setOkHits(int okHits) { this.okHits = okHits; }

    public int getMissCount() { return missCount; }
    public void setMissCount(int missCount) { this.missCount = missCount; }

    public int getMaxCombo() { return maxCombo; }
    public void setMaxCombo(int maxCombo) { this.maxCombo = maxCombo; }

    public int getTotalComboBonus() { return totalComboBonus; }
    public void setTotalComboBonus(int totalComboBonus) { this.totalComboBonus = totalComboBonus; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public boolean isNewHighScore() { return isNewHighScore; }
    public void setNewHighScore(boolean newHighScore) { isNewHighScore = newHighScore; }

    public boolean isFirstCompletion() { return isFirstCompletion; }
    public void setFirstCompletion(boolean firstCompletion) { isFirstCompletion = firstCompletion; }

    public boolean isPerfectGame() { return isPerfectGame; }
    public void setPerfectGame(boolean perfectGame) { isPerfectGame = perfectGame; }

    public boolean isFullCombo() { return isFullCombo; }
    public void setFullCombo(boolean fullCombo) { isFullCombo = fullCombo; }

    public int getNotesCollected() { return notesCollected; }
    public void setNotesCollected(int notesCollected) { this.notesCollected = notesCollected; }

    public int getWordsLearned() { return wordsLearned; }
    public void setWordsLearned(int wordsLearned) { this.wordsLearned = wordsLearned; }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Calculate stars based on score percentage
     */
    private void calculateStars() {
        if (maxPossibleScore == 0) {
            starsEarned = 0;
            return;
        }
        
        float percentage = (float) totalScore / maxPossibleScore * 100f;
        
        if (percentage >= 90) {
            starsEarned = 3;
        } else if (percentage >= 70) {
            starsEarned = 2;
        } else if (percentage >= 50) {
            starsEarned = 1;
        } else {
            starsEarned = 0;
        }
    }
    
    /**
     * Detect special achievements
     */
    private void detectAchievements() {
        isPerfectGame = missCount == 0;
        isFullCombo = maxCombo == totalNotes && totalNotes > 0;
    }
    
    /**
     * Get accuracy percentage
     */
    public float getAccuracyPercentage() {
        if (totalNotes == 0) return 0;
        int hits = perfectHits + goodHits + okHits;
        return (float) hits / totalNotes * 100f;
    }
    
    /**
     * Get score percentage
     */
    public float getScorePercentage() {
        if (maxPossibleScore == 0) return 0;
        return (float) totalScore / maxPossibleScore * 100f;
    }
    
    /**
     * Get grade letter based on performance
     */
    public String getGrade() {
        float percentage = getScorePercentage();
        
        if (percentage >= 95) return "S";
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 50) return "D";
        return "F";
    }
    
    /**
     * Get display text for result
     */
    public String getResultMessage() {
        String grade = getGrade();
        
        switch (grade) {
            case "S": return "🏆 PERFECT! Amazing!";
            case "A": return "⭐ Excellent! Great job!";
            case "B": return "👍 Good work!";
            case "C": return "😊 Nice try!";
            case "D": return "💪 Keep practicing!";
            default: return "🎵 Try again!";
        }
    }
    
    /**
     * Get duration in human readable format
     */
    public String getDurationDisplay() {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
