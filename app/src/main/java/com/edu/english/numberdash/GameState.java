package com.edu.english.numberdash;

/**
 * Manages the global game state for Number Dash Race
 */
public class GameState {
    
    // Current game state
    private int currentState = GameConstants.STATE_CHARACTER_SELECT;
    
    // Selected character index
    private int selectedCharacter = 0;
    
    // Race progress
    private float distanceTraveled = 0f;
    private float currentSpeed = GameConstants.BASE_SPEED;
    private float targetSpeed = GameConstants.BASE_SPEED;
    
    // Question progress
    private int questionsAnswered = 0;
    private int correctAnswers = 0;
    private float distanceSinceLastQuestion = 0f;
    private boolean isShowingQuestion = false;
    
    // Speed effect timers
    private float speedEffectTimer = 0f;
    private boolean isBoosting = false;
    private boolean isSlowing = false;
    
    // Countdown
    private int countdownValue = GameConstants.COUNTDOWN_SECONDS;
    private float countdownTimer = 0f;
    
    // Finish state
    private boolean raceFinished = false;
    private float finishAnimationTimer = 0f;
    
    // Screen dimensions (set at runtime)
    private int screenWidth = 0;
    private int screenHeight = 0;
    
    public GameState() {
        reset();
    }
    
    /**
     * Reset game to initial state
     */
    public void reset() {
        distanceTraveled = 0f;
        currentSpeed = GameConstants.BASE_SPEED;
        targetSpeed = GameConstants.BASE_SPEED;
        questionsAnswered = 0;
        correctAnswers = 0;
        distanceSinceLastQuestion = 0f;
        isShowingQuestion = false;
        speedEffectTimer = 0f;
        isBoosting = false;
        isSlowing = false;
        countdownValue = GameConstants.COUNTDOWN_SECONDS;
        countdownTimer = 0f;
        raceFinished = false;
        finishAnimationTimer = 0f;
    }
    
    /**
     * Update game state
     * @param deltaTime Time since last frame in milliseconds
     */
    public void update(float deltaTime) {
        // Update speed effect timers
        if (speedEffectTimer > 0) {
            speedEffectTimer -= deltaTime;
            if (speedEffectTimer <= 0) {
                speedEffectTimer = 0;
                isBoosting = false;
                isSlowing = false;
                targetSpeed = GameConstants.BASE_SPEED;
            }
        }
        
        // Smoothly interpolate current speed towards target
        if (currentSpeed != targetSpeed) {
            float speedDiff = targetSpeed - currentSpeed;
            float speedChange = speedDiff * 0.1f; // Smooth factor
            if (Math.abs(speedChange) < 1f) {
                currentSpeed = targetSpeed;
            } else {
                currentSpeed += speedChange;
            }
        }
        
        // Update distance (only when racing and not showing question)
        if (currentState == GameConstants.STATE_RACING && !isShowingQuestion) {
            float frameDistance = currentSpeed * (deltaTime / 1000f);
            distanceTraveled += frameDistance;
            distanceSinceLastQuestion += frameDistance;
        }
        
        // Check for race finish
        if (currentState == GameConstants.STATE_RACING && 
            questionsAnswered >= GameConstants.TOTAL_QUESTIONS && !raceFinished) {
            // Continue for a bit after last question before finishing
            if (distanceSinceLastQuestion > GameConstants.QUESTION_INTERVAL * 0.5f) {
                raceFinished = true;
                currentState = GameConstants.STATE_RESULT;
            }
        }
    }
    
    /**
     * Apply speed boost (correct answer)
     */
    public void applyBoost() {
        isBoosting = true;
        isSlowing = false;
        targetSpeed = GameConstants.BOOST_SPEED;
        speedEffectTimer = GameConstants.BOOST_DURATION;
    }
    
    /**
     * Apply slowdown (wrong answer)
     */
    public void applySlow() {
        isBoosting = false;
        isSlowing = true;
        targetSpeed = GameConstants.SLOW_SPEED;
        speedEffectTimer = GameConstants.SLOW_DURATION;
    }
    
    /**
     * Check if it's time to show a question
     */
    public boolean shouldShowQuestion() {
        if (isShowingQuestion || questionsAnswered >= GameConstants.TOTAL_QUESTIONS) {
            return false;
        }
        
        // First question after initial delay
        if (questionsAnswered == 0) {
            return distanceTraveled >= GameConstants.FIRST_QUESTION_DELAY;
        }
        
        // Subsequent questions after interval
        return distanceSinceLastQuestion >= GameConstants.QUESTION_INTERVAL;
    }
    
    /**
     * Show a question
     */
    public void showQuestion() {
        isShowingQuestion = true;
        distanceSinceLastQuestion = 0;
    }
    
    /**
     * Answer the current question
     */
    public void answerQuestion(boolean correct) {
        isShowingQuestion = false;
        questionsAnswered++;
        
        if (correct) {
            correctAnswers++;
            applyBoost();
        } else {
            applySlow();
        }
    }
    
    /**
     * Get star rating based on correct answers
     */
    public int getStarRating() {
        if (correctAnswers >= GameConstants.STARS_THREE_THRESHOLD) {
            return 3;
        } else if (correctAnswers >= GameConstants.STARS_TWO_THRESHOLD) {
            return 2;
        } else {
            return 1;
        }
    }
    
    /**
     * Get race progress (0-1)
     */
    public float getRaceProgress() {
        float progress = (float) questionsAnswered / GameConstants.TOTAL_QUESTIONS;
        return Math.min(1f, progress);
    }
    
    // Getters and setters
    
    public int getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(int state) {
        this.currentState = state;
    }
    
    public int getSelectedCharacter() {
        return selectedCharacter;
    }
    
    public void setSelectedCharacter(int index) {
        this.selectedCharacter = index;
    }
    
    public float getDistanceTraveled() {
        return distanceTraveled;
    }
    
    public float getCurrentSpeed() {
        return currentSpeed;
    }
    
    public int getQuestionsAnswered() {
        return questionsAnswered;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public boolean isShowingQuestion() {
        return isShowingQuestion;
    }
    
    public boolean isBoosting() {
        return isBoosting;
    }
    
    public boolean isSlowing() {
        return isSlowing;
    }
    
    public boolean isRaceFinished() {
        return raceFinished;
    }
    
    public int getCountdownValue() {
        return countdownValue;
    }
    
    public void setCountdownValue(int value) {
        this.countdownValue = value;
    }
    
    public float getCountdownTimer() {
        return countdownTimer;
    }
    
    public void setCountdownTimer(float timer) {
        this.countdownTimer = timer;
    }
    
    public int getScreenWidth() {
        return screenWidth;
    }
    
    public void setScreenWidth(int width) {
        this.screenWidth = width;
    }
    
    public int getScreenHeight() {
        return screenHeight;
    }
    
    public void setScreenHeight(int height) {
        this.screenHeight = height;
    }
    
    public float getFinishAnimationTimer() {
        return finishAnimationTimer;
    }
    
    public void setFinishAnimationTimer(float timer) {
        this.finishAnimationTimer = timer;
    }
}
