package com.edu.english.masterchef.ui.game;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.masterchef.data.model.CookingStep;
import com.edu.english.masterchef.data.model.LevelConfig;
import com.edu.english.masterchef.data.model.PlayerAction;
import com.edu.english.masterchef.data.repository.LevelRepository;
import com.edu.english.masterchef.data.repository.ProgressRepository;
import com.edu.english.masterchef.engine.GameStateMachine;
import com.edu.english.masterchef.engine.ScoreCalculator;
import com.edu.english.masterchef.engine.StepValidator;

/**
 * ViewModel for Master Chef game
 * Manages game state, score, and scene transitions
 */
public class GameViewModel extends AndroidViewModel {

    private LevelRepository levelRepository;
    private ProgressRepository progressRepository;
    private GameStateMachine stateMachine;
    private ScoreCalculator scoreCalculator;
    private StepValidator stepValidator;

    private MutableLiveData<GameStateMachine.GameState> currentState = new MutableLiveData<>();
    private MutableLiveData<Integer> currentScore = new MutableLiveData<>(0);
    private MutableLiveData<LevelConfig> currentLevel = new MutableLiveData<>();
    private MutableLiveData<Integer> currentStepIndex = new MutableLiveData<>(0);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private int levelId;
    private long startTimeMs;
    private int totalErrors = 0;
    private int perfectSteps = 0;
    private int hintsUsed = 0;

    public GameViewModel(@NonNull Application application) {
        super(application);
        levelRepository = LevelRepository.getInstance();
        progressRepository = ProgressRepository.getInstance(application);
        stateMachine = new GameStateMachine();
        scoreCalculator = new ScoreCalculator();
        stepValidator = new StepValidator();

        // Listen to state changes
        stateMachine.setOnStateChangeListener(state -> currentState.postValue(state));
    }

    /**
     * Initialize game with level ID
     */
    public void initializeGame(int levelId) {
        this.levelId = levelId;
        LevelConfig level = levelRepository.getLevel(levelId);
        
        if (level != null) {
            currentLevel.setValue(level);
            startTimeMs = System.currentTimeMillis();
            currentScore.setValue(0);
            totalErrors = 0;
            perfectSteps = 0;
            currentStepIndex.setValue(0);
            
            // Transition directly to ORDERING (skip MAP_LOADED intermediate state)
            stateMachine.transitionTo(GameStateMachine.GameState.MAP_LOADED);
            // Use Handler to ensure LiveData has time to emit
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                startOrderingScene();
            }, 100);
        }
    }

    /**
     * Start ordering scene
     */
    public void startOrderingScene() {
        stateMachine.transitionTo(GameStateMachine.GameState.ORDERING);
    }

    /**
     * Complete ordering scene and move to shopping
     */
    public void completeOrderingScene() {
        stateMachine.transitionTo(GameStateMachine.GameState.SHOPPING);
    }

    /**
     * Complete shopping scene and move to cooking
     */
    public void completeShoppingScene(boolean isValid) {
        if (isValid) {
            stateMachine.transitionTo(GameStateMachine.GameState.COOKING);
        } else {
            errorMessage.setValue("Wrong ingredients selected!");
            incrementErrors();
        }
    }

    /**
     * Validate a cooking step
     * Note: Scene3Fragment handles completion via showCompletion() -> completeCookingScene()
     * @return true if action was valid/correct, false otherwise
     */
    public boolean validateCookingStep(PlayerAction action) {
        LevelConfig level = currentLevel.getValue();
        if (level == null) return false;

        int stepIndex = currentStepIndex.getValue() != null ? currentStepIndex.getValue() : 0;
        if (stepIndex >= level.getCookingScene().getCookingSteps().size()) {
            // All steps done - Scene3 will call completeCookingScene after celebration
            return true;
        }

        CookingStep currentStep = level.getCookingScene().getCookingSteps().get(stepIndex);
        StepValidator.ValidationResult result = stepValidator.validate(currentStep, action);

        if (result.isValid()) {
            // Correct step
            perfectSteps++;
            currentStepIndex.setValue(stepIndex + 1);
            updateScore();
            return true;
        } else {
            // Wrong step
            incrementErrors();
            errorMessage.setValue(result.getMessage());
            updateScore();
            return false;
        }
    }

    /**
     * Complete cooking scene - called from Scene3Fragment after celebration
     */
    public void completeCookingScene() {
        // Ensure we're in COOKING state before transitioning
        if (stateMachine.getCurrentState() == GameStateMachine.GameState.COOKING) {
            saveProgress();
            stateMachine.transitionTo(GameStateMachine.GameState.COMPLETED);
        }
    }

    /**
     * Calculate and update score
     */
    private void updateScore() {
        LevelConfig level = currentLevel.getValue();
        if (level == null) return;

        long elapsedSeconds = (System.currentTimeMillis() - startTimeMs) / 1000;
        int targetTime = level.getCookingScene().getTargetTime();
        int totalSteps = level.getCookingScene().getCookingSteps().size();

        int score = scoreCalculator.calculateScore(
            perfectSteps, 
            totalSteps, 
            totalErrors, 
            hintsUsed, 
            (int) elapsedSeconds, 
            targetTime
        );

        currentScore.setValue(score);
    }

    /**
     * Get number of stars based on score
     */
    public int getStarsForScore(int score) {
        LevelConfig level = currentLevel.getValue();
        if (level == null) return 0;

        if (score >= level.getThreeStarScore()) return 3;
        if (score >= level.getTwoStarScore()) return 2;
        if (score >= level.getOneStarScore()) return 1;
        return 0;
    }

    /**
     * Save progress after level completion
     */
    private void saveProgress() {
        Integer scoreValue = currentScore.getValue();
        int score = scoreValue != null ? scoreValue : 0;
        int stars = getStarsForScore(score);
        LevelConfig level = currentLevel.getValue();
        
        // Use dishId (not dishName) for cookbook lookup
        String dishId = level != null && level.getFood() != null ? level.getFood().getDishId() : "";
        
        // Update level completion in repository
        progressRepository.updateLevelCompletion(levelId, stars, score, dishId);

        // Unlock next level if at least 1 star (for better child-friendly UX)
        if (stars >= 1) {
            progressRepository.unlockLevel(levelId + 1);
        }
    }

    /**
     * Increment error count and update score
     */
    private void incrementErrors() {
        totalErrors++;
        updateScore();
    }

    /**
     * Increment hint usage and apply score penalty
     */
    public void incrementHintUsed() {
        hintsUsed++;
        updateScore();
    }

    /**
     * Get number of hints used
     */
    public int getHintsUsed() {
        return hintsUsed;
    }

    /**
     * Get elapsed time in seconds
     */
    public int getElapsedTimeSeconds() {
        return (int) ((System.currentTimeMillis() - startTimeMs) / 1000);
    }

    // Getters for LiveData
    public LiveData<GameStateMachine.GameState> getCurrentState() {
        return currentState;
    }

    public LiveData<Integer> getCurrentScore() {
        return currentScore;
    }

    public LiveData<LevelConfig> getCurrentLevel() {
        return currentLevel;
    }

    public LiveData<Integer> getCurrentStepIndex() {
        return currentStepIndex;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public int getPerfectSteps() {
        return perfectSteps;
    }

    public int getTotalSteps() {
        LevelConfig level = currentLevel.getValue();
        return level != null ? level.getCookingScene().getCookingSteps().size() : 0;
    }
}
