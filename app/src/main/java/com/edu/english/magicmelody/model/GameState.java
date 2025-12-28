package com.edu.english.magicmelody.model;

/**
 * 🎯 GameState Model
 * 
 * Purpose: Represents the current state of gameplay
 * Used by ViewModel to track and broadcast game status
 */
public class GameState {
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE ENUM
    // ═══════════════════════════════════════════════════════════════
    
    public enum State {
        IDLE,           // Not started
        LOADING,        // Loading assets
        COUNTDOWN,      // 3-2-1 countdown
        PLAYING,        // Active gameplay
        PAUSED,         // Game paused
        COMPLETED,      // Game finished successfully
        GAME_OVER,      // Failed (if applicable)
        BOSS_BATTLE     // Special boss mode
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 FIELDS
    // ═══════════════════════════════════════════════════════════════
    
    private State currentState = State.IDLE;
    private String lessonId;
    private String worldId;
    
    // Score tracking
    private int score = 0;
    private int combo = 0;
    private int maxCombo = 0;
    private float comboMultiplier = 1.0f;
    
    // Note tracking
    private int totalNotes = 0;
    private int perfectHits = 0;
    private int goodHits = 0;
    private int okHits = 0;
    private int missCount = 0;
    
    // Progress tracking
    private int currentNoteIndex = 0;
    private float progressPercent = 0f;
    private float elapsedTimeSeconds = 0f;
    
    // Boss battle specific
    private int bossHealth = 100;
    private int bossMaxHealth = 100;
    private boolean isBossMode = false;
    
    // UI state
    private String feedbackText = "";
    private String currentWord = "";
    private boolean showingWordPopup = false;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public GameState() {
        reset();
    }
    
    public GameState(String lessonId, String worldId) {
        this();
        this.lessonId = lessonId;
        this.worldId = worldId;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📝 GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

    public State getCurrentState() { return currentState; }
    public void setCurrentState(State currentState) { this.currentState = currentState; }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getWorldId() { return worldId; }
    public void setWorldId(String worldId) { this.worldId = worldId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getCombo() { return combo; }
    public void setCombo(int combo) { this.combo = combo; }

    public int getMaxCombo() { return maxCombo; }
    public void setMaxCombo(int maxCombo) { this.maxCombo = maxCombo; }

    public float getComboMultiplier() { return comboMultiplier; }
    public void setComboMultiplier(float comboMultiplier) { this.comboMultiplier = comboMultiplier; }

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

    public int getCurrentNoteIndex() { return currentNoteIndex; }
    public void setCurrentNoteIndex(int currentNoteIndex) { this.currentNoteIndex = currentNoteIndex; }

    public float getProgressPercent() { return progressPercent; }
    public void setProgressPercent(float progressPercent) { this.progressPercent = progressPercent; }

    public float getElapsedTimeSeconds() { return elapsedTimeSeconds; }
    public void setElapsedTimeSeconds(float elapsedTimeSeconds) { this.elapsedTimeSeconds = elapsedTimeSeconds; }

    public int getBossHealth() { return bossHealth; }
    public void setBossHealth(int bossHealth) { this.bossHealth = bossHealth; }

    public int getBossMaxHealth() { return bossMaxHealth; }
    public void setBossMaxHealth(int bossMaxHealth) { this.bossMaxHealth = bossMaxHealth; }

    public boolean isBossMode() { return isBossMode; }
    public void setBossMode(boolean bossMode) { isBossMode = bossMode; }

    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }

    public String getCurrentWord() { return currentWord; }
    public void setCurrentWord(String currentWord) { this.currentWord = currentWord; }

    public boolean isShowingWordPopup() { return showingWordPopup; }
    public void setShowingWordPopup(boolean showingWordPopup) { this.showingWordPopup = showingWordPopup; }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ GAME ACTIONS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Reset game state for new game
     */
    public void reset() {
        currentState = State.IDLE;
        score = 0;
        combo = 0;
        maxCombo = 0;
        comboMultiplier = 1.0f;
        perfectHits = 0;
        goodHits = 0;
        okHits = 0;
        missCount = 0;
        currentNoteIndex = 0;
        progressPercent = 0f;
        elapsedTimeSeconds = 0f;
        bossHealth = bossMaxHealth;
        feedbackText = "";
        currentWord = "";
        showingWordPopup = false;
    }
    
    /**
     * Register a perfect hit
     */
    public void registerPerfectHit(int basePoints) {
        perfectHits++;
        incrementCombo();
        addScore((int) (basePoints * comboMultiplier));
        feedbackText = "PERFECT!";
    }
    
    /**
     * Register a good hit
     */
    public void registerGoodHit(int basePoints) {
        goodHits++;
        incrementCombo();
        addScore((int) (basePoints * 0.8f * comboMultiplier));
        feedbackText = "GOOD!";
    }
    
    /**
     * Register an OK hit
     */
    public void registerOkHit(int basePoints) {
        okHits++;
        incrementCombo();
        addScore((int) (basePoints * 0.5f * comboMultiplier));
        feedbackText = "OK";
    }
    
    /**
     * Register a miss
     */
    public void registerMiss() {
        missCount++;
        resetCombo();
        feedbackText = "MISS";
    }
    
    /**
     * Add points to score
     */
    private void addScore(int points) {
        score += points;
    }
    
    /**
     * Increment combo and update multiplier
     */
    private void incrementCombo() {
        combo++;
        if (combo > maxCombo) {
            maxCombo = combo;
        }
        updateComboMultiplier();
    }
    
    /**
     * Reset combo on miss
     */
    private void resetCombo() {
        combo = 0;
        comboMultiplier = 1.0f;
    }
    
    /**
     * Update combo multiplier based on combo count
     */
    private void updateComboMultiplier() {
        if (combo >= 50) {
            comboMultiplier = 2.0f;
        } else if (combo >= 30) {
            comboMultiplier = 1.75f;
        } else if (combo >= 20) {
            comboMultiplier = 1.5f;
        } else if (combo >= 10) {
            comboMultiplier = 1.25f;
        } else {
            comboMultiplier = 1.0f;
        }
    }
    
    /**
     * Damage boss in boss mode
     */
    public void damageBoss(int damage) {
        if (isBossMode) {
            bossHealth = Math.max(0, bossHealth - damage);
        }
    }
    
    /**
     * Check if boss is defeated
     */
    public boolean isBossDefeated() {
        return isBossMode && bossHealth <= 0;
    }
    
    /**
     * Get boss health percentage
     */
    public float getBossHealthPercent() {
        if (bossMaxHealth == 0) return 0;
        return (float) bossHealth / bossMaxHealth * 100f;
    }
    
    /**
     * Check if game is currently active
     */
    public boolean isPlaying() {
        return currentState == State.PLAYING || currentState == State.BOSS_BATTLE;
    }
    
    /**
     * Check if game is paused
     */
    public boolean isPaused() {
        return currentState == State.PAUSED;
    }
    
    /**
     * Check if game has ended
     */
    public boolean isEnded() {
        return currentState == State.COMPLETED || currentState == State.GAME_OVER;
    }
    
    /**
     * Get current accuracy
     */
    public float getAccuracy() {
        int totalHits = perfectHits + goodHits + okHits + missCount;
        if (totalHits == 0) return 100f;
        return (float) (perfectHits + goodHits + okHits) / totalHits * 100f;
    }
    
    /**
     * Create a copy for state snapshots
     */
    public GameState copy() {
        GameState copy = new GameState();
        copy.currentState = this.currentState;
        copy.lessonId = this.lessonId;
        copy.worldId = this.worldId;
        copy.score = this.score;
        copy.combo = this.combo;
        copy.maxCombo = this.maxCombo;
        copy.comboMultiplier = this.comboMultiplier;
        copy.totalNotes = this.totalNotes;
        copy.perfectHits = this.perfectHits;
        copy.goodHits = this.goodHits;
        copy.okHits = this.okHits;
        copy.missCount = this.missCount;
        copy.currentNoteIndex = this.currentNoteIndex;
        copy.progressPercent = this.progressPercent;
        copy.elapsedTimeSeconds = this.elapsedTimeSeconds;
        copy.bossHealth = this.bossHealth;
        copy.bossMaxHealth = this.bossMaxHealth;
        copy.isBossMode = this.isBossMode;
        copy.feedbackText = this.feedbackText;
        copy.currentWord = this.currentWord;
        copy.showingWordPopup = this.showingWordPopup;
        return copy;
    }
}
