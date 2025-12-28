package com.edu.english.magicmelody.domain;

import java.util.List;
import java.util.ArrayList;

/**
 * 🏆 Game Result - Final results after gameplay
 */
public class GameResult {
    
    private long lessonId;
    private int score;
    private int maxCombo;
    private int perfectCount;
    private int goodCount;
    private int okCount;
    private int missCount;
    private int starRating;  // 1-3 stars
    private List<String> collectedWords;
    private long completedAt;
    
    public GameResult() {
        this.collectedWords = new ArrayList<>();
        this.completedAt = System.currentTimeMillis();
    }
    
    public GameResult(long lessonId, int score, int maxCombo,
                      int perfectCount, int goodCount, int okCount, int missCount,
                      int starRating, List<String> collectedWords) {
        this.lessonId = lessonId;
        this.score = score;
        this.maxCombo = maxCombo;
        this.perfectCount = perfectCount;
        this.goodCount = goodCount;
        this.okCount = okCount;
        this.missCount = missCount;
        this.starRating = starRating;
        this.collectedWords = collectedWords != null ? collectedWords : new ArrayList<>();
        this.completedAt = System.currentTimeMillis();
    }
    
    // Getters
    public long getLessonId() { return lessonId; }
    public int getScore() { return score; }
    public int getMaxCombo() { return maxCombo; }
    public int getPerfectCount() { return perfectCount; }
    public int getGoodCount() { return goodCount; }
    public int getOkCount() { return okCount; }
    public int getMissCount() { return missCount; }
    public int getStarRating() { return starRating; }
    public List<String> getCollectedWords() { return collectedWords; }
    public long getCompletedAt() { return completedAt; }
    
    // Setters
    public void setLessonId(long lessonId) { this.lessonId = lessonId; }
    public void setScore(int score) { this.score = score; }
    public void setMaxCombo(int maxCombo) { this.maxCombo = maxCombo; }
    public void setPerfectCount(int perfectCount) { this.perfectCount = perfectCount; }
    public void setGoodCount(int goodCount) { this.goodCount = goodCount; }
    public void setOkCount(int okCount) { this.okCount = okCount; }
    public void setMissCount(int missCount) { this.missCount = missCount; }
    public void setStarRating(int starRating) { this.starRating = starRating; }
    public void setCollectedWords(List<String> words) { this.collectedWords = words; }
    
    // Computed
    public int getTotalNotes() {
        return perfectCount + goodCount + okCount + missCount;
    }
    
    public float getAccuracy() {
        int total = getTotalNotes();
        if (total == 0) return 0f;
        return (float)(perfectCount + goodCount + okCount) / total * 100f;
    }
    
    public boolean isPerfectGame() {
        return missCount == 0;
    }
}
