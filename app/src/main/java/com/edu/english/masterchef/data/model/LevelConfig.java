package com.edu.english.masterchef.data.model;

/**
 * Configuration for a single level in Master Chef
 */
public class LevelConfig {
    private int levelId;
    private String dishId;
    private Food food; // The dish for this level
    private String difficulty; // "easy", "normal", "hard"
    private int unlockCondition; // Previous level id that must be completed (0 for first level)
    private int star1Threshold; // Minimum score for 1 star
    private int star2Threshold; // Minimum score for 2 stars
    private int star3Threshold; // Minimum score for 3 stars
    
    // Scene scripts
    private SceneScript scene1Script; // Ordering
    private SceneScript scene2Script; // Shopping
    private SceneScript scene3Script; // Cooking

    public LevelConfig() {
    }

    public LevelConfig(int levelId, String dishId, String difficulty, int unlockCondition,
                      int star1Threshold, int star2Threshold, int star3Threshold) {
        this.levelId = levelId;
        this.dishId = dishId;
        this.difficulty = difficulty;
        this.unlockCondition = unlockCondition;
        this.star1Threshold = star1Threshold;
        this.star2Threshold = star2Threshold;
        this.star3Threshold = star3Threshold;
    }

    // Getters and Setters
    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public String getDishId() {
        return dishId;
    }

    public void setDishId(String dishId) {
        this.dishId = dishId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getUnlockCondition() {
        return unlockCondition;
    }

    public void setUnlockCondition(int unlockCondition) {
        this.unlockCondition = unlockCondition;
    }

    public int getStar1Threshold() {
        return star1Threshold;
    }

    public void setStar1Threshold(int star1Threshold) {
        this.star1Threshold = star1Threshold;
    }

    public int getStar2Threshold() {
        return star2Threshold;
    }

    public void setStar2Threshold(int star2Threshold) {
        this.star2Threshold = star2Threshold;
    }

    public int getStar3Threshold() {
        return star3Threshold;
    }

    public void setStar3Threshold(int star3Threshold) {
        this.star3Threshold = star3Threshold;
    }

    public SceneScript getScene1Script() {
        return scene1Script;
    }

    public void setScene1Script(SceneScript scene1Script) {
        this.scene1Script = scene1Script;
    }

    public SceneScript getScene2Script() {
        return scene2Script;
    }

    public void setScene2Script(SceneScript scene2Script) {
        this.scene2Script = scene2Script;
    }

    public SceneScript getScene3Script() {
        return scene3Script;
    }

    public void setScene3Script(SceneScript scene3Script) {
        this.scene3Script = scene3Script;
    }

    /**
     * Calculate stars based on score
     */
    public int getStarsForScore(int score) {
        if (score >= star3Threshold) return 3;
        if (score >= star2Threshold) return 2;
        if (score >= star1Threshold) return 1;
        return 0;
    }

    // Convenience methods with better names
    public SceneScript getOrderingScene() {
        return scene1Script;
    }

    public SceneScript getShoppingScene() {
        return scene2Script;
    }

    public SceneScript getCookingScene() {
        return scene3Script;
    }

    public int getOneStarScore() {
        return star1Threshold;
    }

    public int getTwoStarScore() {
        return star2Threshold;
    }

    public int getThreeStarScore() {
        return star3Threshold;
    }

    // Note: Food object should be retrieved from repository using dishId
    // This is a placeholder that returns null - the calling code should handle this
    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }
}
