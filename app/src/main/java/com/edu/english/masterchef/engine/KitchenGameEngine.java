package com.edu.english.masterchef.engine;

import android.os.Handler;
import android.os.Looper;

import com.edu.english.masterchef.data.ActionParams;
import com.edu.english.masterchef.data.ActionType;
import com.edu.english.masterchef.data.Ingredient;
import com.edu.english.masterchef.data.MasterChefRepository;
import com.edu.english.masterchef.data.Recipe;
import com.edu.english.masterchef.data.RecipeStep;
import com.edu.english.masterchef.data.StepState;
import com.edu.english.masterchef.data.Tool;
import com.edu.english.masterchef.data.ZoneType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main game engine for Master Chef - handles state machine logic.
 */
public class KitchenGameEngine {
    
    public interface GameEventListener {
        void onStepChanged(RecipeStep step, int stepIndex, int totalSteps);
        void onStepStateChanged(StepState state);
        void onActionProgress(ActionType action, float progress);
        void onActionComplete(ActionType action);
        void onItemPlaced(String itemId, ZoneType zone);
        void onStepSuccess(String successText);
        void onSpeakingRequired(String phrase);
        void onSpeakingResult(boolean correct, String phrase);
        void onRecipeComplete(int score);
        void onChefSpeak(String text);
        void onHintRequested(String hint);
        void onCookingTimer(long remaining);
        void onError(String message);
    }
    
    private static final int COOK_TICK_INTERVAL = 100; // 100ms
    
    private GameSessionState state;
    private MasterChefRepository repository;
    private GameEventListener listener;
    private Handler handler;
    private Runnable cookingTimerRunnable;
    private boolean isRunning;
    
    public KitchenGameEngine() {
        this.state = new GameSessionState();
        this.repository = MasterChefRepository.getInstance();
        this.handler = new Handler(Looper.getMainLooper());
        this.isRunning = false;
    }
    
    public void setListener(GameEventListener listener) {
        this.listener = listener;
    }
    
    public GameSessionState getState() {
        return state;
    }
    
    // ========================
    // GAME LIFECYCLE
    // ========================
    
    public void startRecipe(String recipeId) {
        Recipe recipe = repository.getRecipe(recipeId);
        if (recipe == null) {
            notifyError("Recipe not found: " + recipeId);
            return;
        }
        
        state.startRecipe(recipe);
        isRunning = true;
        
        // Start first step
        RecipeStep firstStep = state.getCurrentStep();
        if (firstStep != null) {
            initializeStepState(firstStep);
            notifyStepChanged(firstStep);
            notifyChefSpeak(firstStep.getChefTextEn());
        }
    }
    
    public void pauseGame() {
        state.setPaused(true);
        stopCookingTimer();
    }
    
    public void resumeGame() {
        state.setPaused(false);
        // Resume cooking timer if needed
        if (state.getStepState() == StepState.ACTION_IN_PROGRESS) {
            RecipeStep step = state.getCurrentStep();
            if (step != null && step.getActionType() == ActionType.WAIT_COOK) {
                startCookingTimer();
            }
        }
    }
    
    public void stopGame() {
        isRunning = false;
        stopCookingTimer();
    }
    
    /**
     * Cleanup all resources and timers
     */
    public void cleanup() {
        stopGame();
        stopCookingTimer();
        stopTimingTimer();
        handler.removeCallbacksAndMessages(null);
        listener = null;
    }
    
    // ========================
    // DRAG & DROP HANDLING
    // ========================
    
    public void onDragStart(String itemId) {
        if (!isRunning || state.isPaused()) return;
        
        state.setDraggingItemId(itemId);
        state.setDragging(true);
    }
    
    public void onDragEnd() {
        state.setDraggingItemId(null);
        state.setDragging(false);
    }
    
    public boolean onDropItem(String itemId, ZoneType targetZone) {
        if (!isRunning || state.isPaused()) return false;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) return false;
        
        // Check if this drop is valid for current step
        if (!isValidDrop(itemId, targetZone, step)) {
            onDragEnd();
            return false;
        }
        
        // Add item to zone
        state.addItemToZone(targetZone, itemId);
        notifyItemPlaced(itemId, targetZone);
        
        // Check step action type
        if (step.getActionType() == ActionType.DRAG_TO_ZONE) {
            if (areAllRequiredItemsInZone(step)) {
                transitionToActionComplete(step);
            }
        } else if (step.getActionType() == ActionType.POUR) {
            // Start pour action
            state.startPouring();
            state.setStepState(StepState.ACTION_IN_PROGRESS);
            notifyStepStateChanged(StepState.ACTION_IN_PROGRESS);
        } else {
            // Item placed, ready for action
            state.setStepState(StepState.READY_ACTION);
            notifyStepStateChanged(StepState.READY_ACTION);
        }
        
        onDragEnd();
        return true;
    }
    
    private boolean isValidDrop(String itemId, ZoneType targetZone, RecipeStep step) {
        android.util.Log.d("GameEngine", "isValidDrop: itemId=" + itemId + 
            ", targetZone=" + targetZone + ", stepTargetZone=" + step.getTargetZone() +
            ", requiredItems=" + step.getRequiredItems() + ", requiredTool=" + step.getRequiredTool());
        
        // First check zone matches
        if (targetZone != step.getTargetZone()) {
            android.util.Log.d("GameEngine", "isValidDrop FAILED: zone mismatch!");
            return false;
        }
        
        // Check if item matches requiredItems or requiredTool
        List<String> requiredItems = step.getRequiredItems();
        String requiredTool = step.getRequiredTool();
        
        // Case 1: Item is in requiredItems list
        if (requiredItems != null && requiredItems.contains(itemId)) {
            android.util.Log.d("GameEngine", "isValidDrop OK: item in requiredItems");
            return true;
        }
        
        // Case 2: Item is the requiredTool
        if (requiredTool != null && itemId.equals(requiredTool)) {
            android.util.Log.d("GameEngine", "isValidDrop OK: item is requiredTool");
            return true;
        }
        
        // Case 3: No required items and no tool specified - accept any item
        if ((requiredItems == null || requiredItems.isEmpty()) && requiredTool == null) {
            android.util.Log.d("GameEngine", "isValidDrop OK: no requirements");
            return true;
        }
        
        // Item doesn't match requirements
        android.util.Log.d("GameEngine", "isValidDrop FAILED: item not in requirements!");
        return false;
    }
    
    private boolean areAllRequiredItemsInZone(RecipeStep step) {
        List<String> itemsInZone = state.getItemsInZone(step.getTargetZone());
        if (itemsInZone == null) itemsInZone = new ArrayList<>();
        
        // Check required items
        List<String> requiredItems = step.getRequiredItems();
        if (requiredItems != null && !requiredItems.isEmpty()) {
            Set<String> requiredSet = new HashSet<>(requiredItems);
            for (String item : itemsInZone) {
                requiredSet.remove(item);
            }
            if (!requiredSet.isEmpty()) {
                return false; // Not all required items are in zone
            }
        }
        
        // Check required tool
        String requiredTool = step.getRequiredTool();
        if (requiredTool != null && !requiredTool.isEmpty()) {
            if (!itemsInZone.contains(requiredTool)) {
                return false; // Required tool not in zone
            }
        }
        
        // All requirements met (or no requirements)
        return true;
    }
    
    // ========================
    // ACTION HANDLING
    // ========================
    
    public void onTap() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        StepState currentState = state.getStepState();
        if (currentState != StepState.READY_ACTION && 
            currentState != StepState.ACTION_IN_PROGRESS) {
            return;
        }
        
        ActionType action = step.getActionType();
        
        switch (action) {
            case TAP_TO_CUT:
                state.setStepState(StepState.ACTION_IN_PROGRESS);
                state.incrementCutProgress();
                checkActionComplete(action, step);
                break;
            case TAP_TO_POUND:
                state.setStepState(StepState.ACTION_IN_PROGRESS);
                state.incrementPoundProgress();
                checkActionComplete(action, step);
                break;
            case VENT_STEAM_TAP:
            case TAP_TO_CRACK:
            case TAP_TO_DRIZZLE:
                onAdvancedTap();
                break;
        }
    }
    
    public void onSwipe() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        StepState currentState = state.getStepState();
        if (currentState != StepState.READY_ACTION && 
            currentState != StepState.ACTION_IN_PROGRESS) {
            return;
        }
        
        ActionType action = step.getActionType();
        
        switch (action) {
            case SWIPE_TO_STIR:
                state.setStepState(StepState.ACTION_IN_PROGRESS);
                state.incrementStirProgress();
                checkActionComplete(action, step);
                break;
            case SHAKE_PAN:
            case SHAKE_BASKET:
            case SEASON_SHAKE:
                onShakeGesture();
                break;
            case GRILL_FLIP_SWIPE:
                onFlipGesture();
                break;
            case WIPE_FOG_SWIPE:
            case SKIM_FOAM_SWIPE:
                onAdvancedSwipe();
                break;
        }
    }
    
    public void onPourComplete() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null || step.getActionType() != ActionType.POUR) return;
        
        if (state.isActionComplete(ActionType.POUR)) {
            transitionToActionComplete(step);
        }
    }
    
    public void startCookingAction() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null || step.getActionType() != ActionType.WAIT_COOK) return;
        
        state.startCooking();
        state.setStepState(StepState.ACTION_IN_PROGRESS);
        notifyStepStateChanged(StepState.ACTION_IN_PROGRESS);
        startCookingTimer();
    }
    
    public void performServeAction() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null || step.getActionType() != ActionType.SERVE) return;
        
        transitionToActionComplete(step);
    }
    
    // ========================
    // ADVANCED ACTION HANDLING
    // ========================
    
    /**
     * Handle hold start (for OVEN_PREHEAT_HOLD, GRILL_ON_HOLD, STEAM_START, POUR)
     * Safe to call multiple times - will not restart if already holding.
     */
    public void onHoldStart() {
        android.util.Log.d("GameEngine", "onHoldStart called! isRunning=" + isRunning + 
            ", isPaused=" + state.isPaused() + ", isHolding=" + state.isHolding());
        
        if (!isRunning || state.isPaused()) {
            android.util.Log.d("GameEngine", "onHoldStart: Skipped - game not running or paused");
            return;
        }
        
        // FAILSAFE: Don't restart if already holding
        if (state.isHolding()) {
            android.util.Log.d("GameEngine", "onHoldStart: Skipped - already holding");
            return;
        }
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) {
            android.util.Log.d("GameEngine", "onHoldStart: Skipped - no current step");
            return;
        }
        
        ActionType action = step.getActionType();
        android.util.Log.d("GameEngine", "onHoldStart: action=" + action + 
            ", isHoldAction=" + (action != null ? action.isHoldAction() : "null"));
        
        if (action == null) {
            android.util.Log.d("GameEngine", "onHoldStart: Skipped - action is null");
            return;
        }
        if (!action.isHoldAction() && action != ActionType.POUR) {
            android.util.Log.d("GameEngine", "onHoldStart: Skipped - not a hold action");
            return;
        }
        
        android.util.Log.d("GameEngine", "onHoldStart: Starting hold timer!");
        
        // Reset completion flag before starting
        holdCompleted = false;
        
        state.startHolding();
        state.setStepState(StepState.ACTION_IN_PROGRESS);
        notifyStepStateChanged(StepState.ACTION_IN_PROGRESS);
        startHoldTimer();
    }
    
    /**
     * Handle hold end - MUST be called when finger is released
     * Safe to call even if not currently holding.
     */
    public void onHoldEnd() {
        // CRITICAL: Always stop timer first, regardless of game state
        stopHoldTimer();
        
        // Safe null check
        if (state != null) {
            state.stopHolding();
        }
        
        // Early return if game not running
        if (!isRunning || state == null || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        ActionType action = step.getActionType();
        if (action == null) return;
        
        // Only complete if action was finished
        if (!holdCompleted && state.isActionComplete(action)) {
            holdCompleted = true;
            notifyActionComplete(action);
            transitionToActionComplete(step);
        }
    }
    
    private Runnable holdTimerRunnable;
    private boolean holdCompleted = false; // Failsafe: prevent multiple completions
    
    private void startHoldTimer() {
        stopHoldTimer();
        holdCompleted = false; // Reset completion flag
        
        holdTimerRunnable = new Runnable() {
            @Override
            public void run() {
                // FAILSAFE: Multiple guards to prevent runaway timer
                if (!isRunning || state.isPaused() || !state.isHolding()) {
                    stopHoldTimer();
                    return;
                }
                
                // FAILSAFE: If already completed, don't run again
                if (holdCompleted) {
                    stopHoldTimer();
                    return;
                }
                
                RecipeStep step = state.getCurrentStep();
                if (step == null) {
                    stopHoldTimer();
                    return;
                }
                
                ActionType action = step.getActionType();
                if (action == null || !action.isHoldAction()) {
                    stopHoldTimer();
                    return;
                }
                
                try {
                    float progress = state.getActionProgress(action);
                    // FAILSAFE: Clamp progress to prevent overflow
                    progress = Math.min(1f, Math.max(0f, progress));
                    notifyActionProgress(action, progress);
                    
                    if (state.isActionComplete(action)) {
                        // Mark as completed to prevent re-entry
                        holdCompleted = true;
                        stopHoldTimer();
                        notifyActionComplete(action);
                        transitionToActionComplete(step);
                        // DON'T continue - action is complete
                        return;
                    } else {
                        handler.postDelayed(this, COOK_TICK_INTERVAL);
                    }
                } catch (Exception e) {
                    // FAILSAFE: Log and stop timer on any exception
                    android.util.Log.e("KitchenGameEngine", "Hold timer error", e);
                    stopHoldTimer();
                }
            }
        };
        
        handler.postDelayed(holdTimerRunnable, COOK_TICK_INTERVAL);
    }
    
    private void stopHoldTimer() {
        if (holdTimerRunnable != null) {
            handler.removeCallbacks(holdTimerRunnable);
            holdTimerRunnable = null;
        }
        // Reset holding state to be safe
        if (state != null) {
            state.stopHolding();
        }
    }
    
    /**
     * Handle rotary knob change (for ROTATE_KNOB_TEMP, ROTATE_KNOB_TIMER)
     */
    public void onRotaryChange(float value) {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        ActionType action = step.getActionType();
        
        if (action == ActionType.ROTATE_KNOB_TEMP) {
            state.setTemperatureKnobValue(value);
            state.setStepState(StepState.ACTION_IN_PROGRESS);
            checkActionComplete(action, step);
        } else if (action == ActionType.ROTATE_KNOB_TIMER) {
            state.setTimerKnobValue(value);
            state.setStepState(StepState.ACTION_IN_PROGRESS);
            checkActionComplete(action, step);
        }
    }
    
    /**
     * Handle slider change (for OIL_LEVEL_SLIDER, HEAT_LEVEL_SLIDER)
     */
    public void onSliderChange(float value) {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        ActionType action = step.getActionType();
        
        if (action == ActionType.OIL_LEVEL_SLIDER) {
            state.setOilLevelSliderValue(value);
            state.setStepState(StepState.ACTION_IN_PROGRESS);
            checkActionComplete(action, step);
        } else if (action == ActionType.HEAT_LEVEL_SLIDER) {
            state.setHeatSliderValue(value);
            state.setStepState(StepState.ACTION_IN_PROGRESS);
            checkActionComplete(action, step);
        }
    }
    
    /**
     * Handle timing stop action
     */
    public void onTimingStart() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null || step.getActionType() != ActionType.TIMING_STOP) return;
        
        state.startTiming();
        state.setStepState(StepState.ACTION_IN_PROGRESS);
        notifyStepStateChanged(StepState.ACTION_IN_PROGRESS);
        startTimingTimer();
    }
    
    public void onTimingStop() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null || step.getActionType() != ActionType.TIMING_STOP) return;
        
        state.stopTiming();
        stopTimingTimer();
        
        // Check if stopped in perfect window
        ActionParams params = step.getActionParams();
        if (params != null) {
            long elapsed = state.getTimingElapsed();
            boolean isPerfect = params.isWithinPerfectWindow(elapsed);
            
            if (isPerfect) {
                state.addScore(20); // Bonus for perfect timing
            }
            
            notifyActionComplete(ActionType.TIMING_STOP);
            transitionToActionComplete(step);
        }
    }
    
    private Runnable timingTimerRunnable;
    
    private void startTimingTimer() {
        stopTimingTimer();
        
        timingTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning || state.isPaused() || !state.isTimingActive()) return;
                
                RecipeStep step = state.getCurrentStep();
                if (step == null) return;
                
                float progress = state.getActionProgress(ActionType.TIMING_STOP);
                notifyActionProgress(ActionType.TIMING_STOP, progress);
                
                ActionParams params = step.getActionParams();
                if (params != null && state.getTimingElapsed() > params.getTotalMs()) {
                    // Time exceeded
                    state.stopTiming();
                    notifyActionComplete(ActionType.TIMING_STOP);
                    transitionToActionComplete(step);
                } else {
                    handler.postDelayed(this, COOK_TICK_INTERVAL);
                }
            }
        };
        
        handler.postDelayed(timingTimerRunnable, COOK_TICK_INTERVAL);
    }
    
    private void stopTimingTimer() {
        if (timingTimerRunnable != null) {
            handler.removeCallbacks(timingTimerRunnable);
            timingTimerRunnable = null;
        }
    }
    
    /**
     * Handle circle gesture (for WHISK_CIRCLES)
     */
    public void onCircleGesture() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null || step.getActionType() != ActionType.WHISK_CIRCLES) return;
        
        state.setStepState(StepState.ACTION_IN_PROGRESS);
        state.incrementCircleProgress();
        checkActionComplete(ActionType.WHISK_CIRCLES, step);
    }
    
    /**
     * Handle knead gesture (for KNEAD_DRAG)
     */
    public void onKneadGesture() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null || step.getActionType() != ActionType.KNEAD_DRAG) return;
        
        state.setStepState(StepState.ACTION_IN_PROGRESS);
        state.incrementKneadProgress();
        checkActionComplete(ActionType.KNEAD_DRAG, step);
    }
    
    /**
     * Handle flip gesture (for GRILL_FLIP_SWIPE)
     */
    public void onFlipGesture() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null || step.getActionType() != ActionType.GRILL_FLIP_SWIPE) return;
        
        state.setStepState(StepState.ACTION_IN_PROGRESS);
        state.recordFlip();
        checkActionComplete(ActionType.GRILL_FLIP_SWIPE, step);
    }
    
    /**
     * Handle shake (for SHAKE_BASKET, SEASON_SHAKE)
     */
    public void onShakeGesture() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        ActionType action = step.getActionType();
        if (action == ActionType.SHAKE_BASKET || action == ActionType.SEASON_SHAKE) {
            state.setStepState(StepState.ACTION_IN_PROGRESS);
            state.incrementSeasonShake();
            checkActionComplete(action, step);
        } else if (action == ActionType.SHAKE_PAN) {
            state.setStepState(StepState.ACTION_IN_PROGRESS);
            state.incrementShakeProgress();
            checkActionComplete(action, step);
        }
    }
    
    /**
     * Handle tap-based actions (for VENT_STEAM_TAP, TAP_TO_CRACK, TAP_TO_DRIZZLE)
     */
    public void onAdvancedTap() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        ActionType action = step.getActionType();
        
        switch (action) {
            case VENT_STEAM_TAP:
            case TAP_TO_CRACK:
            case TAP_TO_DRIZZLE:
                state.setStepState(StepState.ACTION_IN_PROGRESS);
                state.incrementDrizzle();
                checkActionComplete(action, step);
                break;
        }
    }
    
    /**
     * Handle swipe-based actions (for WIPE_FOG_SWIPE, SKIM_FOAM_SWIPE)
     */
    public void onAdvancedSwipe() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        ActionType action = step.getActionType();
        
        switch (action) {
            case WIPE_FOG_SWIPE:
            case SKIM_FOAM_SWIPE:
                state.setStepState(StepState.ACTION_IN_PROGRESS);
                state.incrementSkimProgress();
                checkActionComplete(action, step);
                break;
        }
    }
    
    /**
     * Handle garnish placed at snap point
     */
    public void onGarnishPlaced() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null || step.getActionType() != ActionType.GARNISH_DRAG_SNAP) return;
        
        state.setStepState(StepState.ACTION_IN_PROGRESS);
        state.incrementGarnishPlaced();
        checkActionComplete(ActionType.GARNISH_DRAG_SNAP, step);
    }
    
    /**
     * Start wait actions (WAIT_OIL_HEAT, WAIT_BOIL, WAIT_SEAR)
     */
    public void startWaitAction() {
        if (!isRunning || state.isPaused()) return;
        
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        ActionType action = step.getActionType();
        
        switch (action) {
            case WAIT_OIL_HEAT:
            case WAIT_BOIL:
            case WAIT_SEAR:
                state.startStateTimer();
                state.setStepState(StepState.ACTION_IN_PROGRESS);
                notifyStepStateChanged(StepState.ACTION_IN_PROGRESS);
                startWaitTimer(action);
                break;
        }
    }
    
    private Runnable waitTimerRunnable;
    
    private void startWaitTimer(ActionType action) {
        stopWaitTimer();
        
        waitTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning || state.isPaused()) return;
                
                RecipeStep step = state.getCurrentStep();
                if (step == null) return;
                
                float progress = state.getActionProgress(action);
                notifyActionProgress(action, progress);
                
                if (state.isActionComplete(action)) {
                    notifyActionComplete(action);
                    transitionToActionComplete(step);
                } else {
                    handler.postDelayed(this, COOK_TICK_INTERVAL);
                }
            }
        };
        
        handler.postDelayed(waitTimerRunnable, COOK_TICK_INTERVAL);
    }
    
    private void stopWaitTimer() {
        if (waitTimerRunnable != null) {
            handler.removeCallbacks(waitTimerRunnable);
            waitTimerRunnable = null;
        }
    }
    
    private void checkActionComplete(ActionType action, RecipeStep step) {
        float progress = state.getActionProgress(action);
        notifyActionProgress(action, progress);
        
        if (state.isActionComplete(action)) {
            notifyActionComplete(action);
            transitionToActionComplete(step);
        }
    }
    
    private void startCookingTimer() {
        stopCookingTimer();
        
        cookingTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning || state.isPaused()) return;
                
                RecipeStep step = state.getCurrentStep();
                if (step == null) return;
                
                long elapsed = state.getCookDuration();
                long total = step.getCookTimeMs();
                long remaining = total - elapsed;
                
                notifyActionProgress(ActionType.WAIT_COOK, (float) elapsed / total);
                notifyCookingTimer(remaining);
                
                if (remaining <= 0) {
                    notifyActionComplete(ActionType.WAIT_COOK);
                    transitionToActionComplete(step);
                } else {
                    handler.postDelayed(this, COOK_TICK_INTERVAL);
                }
            }
        };
        
        handler.postDelayed(cookingTimerRunnable, COOK_TICK_INTERVAL);
    }
    
    private void stopCookingTimer() {
        if (cookingTimerRunnable != null) {
            handler.removeCallbacks(cookingTimerRunnable);
            cookingTimerRunnable = null;
        }
    }
    
    // ========================
    // STATE TRANSITIONS
    // ========================
    
    private void transitionToActionComplete(RecipeStep step) {
        state.setStepState(StepState.STEP_DONE);
        notifyStepStateChanged(StepState.STEP_DONE);
        notifyStepSuccess(step.getSuccessTextEn());
        
        // Mark items as processed
        if (step.getRequiredItems() != null) {
            for (String item : step.getRequiredItems()) {
                state.markItemProcessed(item);
            }
        }
        
        // Add score
        state.completeStep();
        
        // Check if speaking is required
        if (step.isRequiresSpeaking() && step.getSpeakingPhraseEn() != null) {
            // Short delay before speaking check
            handler.postDelayed(() -> {
                state.setStepState(StepState.SPEAKING_CHECK);
                notifyStepStateChanged(StepState.SPEAKING_CHECK);
                notifySpeakingRequired(step.getSpeakingPhraseEn());
            }, 1500);
        } else {
            // Skip speaking, move to next step
            handler.postDelayed(this::moveToNextStep, 2000);
        }
    }
    
    public void onSpeakingResult(boolean correct, String spokenText) {
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        state.recordSpeakingAttempt(correct);
        notifySpeakingResult(correct, step.getSpeakingPhraseEn());
        
        if (correct) {
            // Move to next step
            handler.postDelayed(this::moveToNextStep, 1500);
        }
        // If incorrect, UI will prompt retry (listener handles this)
    }
    
    public void skipSpeaking() {
        // Allow skipping but don't award speaking points
        moveToNextStep();
    }
    
    private void moveToNextStep() {
        if (state.advanceToNextStep()) {
            RecipeStep nextStep = state.getCurrentStep();
            if (nextStep != null) {
                initializeStepState(nextStep);
                notifyStepChanged(nextStep);
                notifyChefSpeak(nextStep.getChefTextEn());
            }
        } else {
            // Recipe complete!
            notifyRecipeComplete(state.getTotalScore());
        }
    }
    
    /**
     * Initialize the step state based on action type.
     * Actions that don't require dragging items first should start in READY_ACTION state.
     */
    private void initializeStepState(RecipeStep step) {
        if (step == null) return;
        
        ActionType action = step.getActionType();
        if (action == null) return;
        
        // Check if this action requires dragging items first
        if (isDirectAction(action)) {
            // Direct actions can be performed immediately without dragging
            state.setStepState(StepState.READY_ACTION);
            notifyStepStateChanged(StepState.READY_ACTION);
            android.util.Log.d("GameEngine", "Step initialized to READY_ACTION for action: " + action);
        } else {
            // DRAG_TO_ZONE and other drag-based actions stay in WAITING_ITEM
            android.util.Log.d("GameEngine", "Step stays in WAITING_ITEM for action: " + action);
        }
    }
    
    /**
     * Check if the action is a "direct action" that can be performed without dragging items first.
     * These include tap actions, hold actions, wait actions, stir/shake actions, etc.
     */
    private boolean isDirectAction(ActionType action) {
        switch (action) {
            // Tap actions
            case TAP_TO_CUT:
            case TAP_TO_CRACK:
            case TAP_TO_POUND:
            case TAP_TO_DRIZZLE:
            case VENT_STEAM_TAP:
            case TIMING_STOP:
            // Hold actions
            case POUR:
            case OVEN_PREHEAT_HOLD:
            case GRILL_ON_HOLD:
            // Stir/shake actions
            case SWIPE_TO_STIR:
            case SHAKE_PAN:
            case SHAKE_BASKET:
            case SEASON_SHAKE:
            case WHISK_CIRCLES:
            case KNEAD_DRAG:
            // Grill/flip actions
            case GRILL_FLIP_SWIPE:
            // Wait actions
            case WAIT_COOK:
            case WAIT_BOIL:
            case WAIT_OIL_HEAT:
            case WAIT_SEAR:
            case STEAM_START:
            // Swipe actions
            case WIPE_FOG_SWIPE:
            case SKIM_FOAM_SWIPE:
            // Other
            case GARNISH_DRAG_SNAP:
            case SERVE:
                return true;
            // Drag-based actions require items first
            case DRAG_TO_ZONE:
            default:
                return false;
        }
    }
    
    // ========================
    // HINTS
    // ========================
    
    public void requestHint() {
        RecipeStep step = state.getCurrentStep();
        if (step == null) return;
        
        String hint = generateHint(step);
        notifyHint(hint);
    }
    
    private String generateHint(RecipeStep step) {
        StringBuilder hint = new StringBuilder();
        ActionParams params = step.getActionParams();
        
        switch (state.getStepState()) {
            case WAITING_ITEM:
                if (step.getRequiredItems() != null && !step.getRequiredItems().isEmpty()) {
                    hint.append("Drag the ");
                    for (int i = 0; i < step.getRequiredItems().size(); i++) {
                        if (i > 0) hint.append(" and ");
                        Ingredient ing = repository.getIngredient(step.getRequiredItems().get(i));
                        if (ing != null) hint.append(ing.getNameEn());
                    }
                    hint.append(" to the ").append(step.getTargetZone().getDisplayName());
                }
                break;
                
            case READY_ACTION:
            case ACTION_IN_PROGRESS:
                ActionType action = step.getActionType();
                
                // Use hint text from action type if available
                if (action.getHintText() != null && !action.getHintText().isEmpty()) {
                    hint.append(action.getHintText());
                } else {
                    // Fallback hints
                    switch (action) {
                        case TAP_TO_CUT:
                            hint.append("Tap on the cutting board to cut! ");
                            hint.append(state.getCutProgress()).append("/").append(step.getTapCount());
                            break;
                        case TAP_TO_POUND:
                            hint.append("Tap to pound the meat! ");
                            hint.append(state.getPoundProgress()).append("/").append(step.getTapCount());
                            break;
                        case SWIPE_TO_STIR:
                            hint.append("Swipe left and right to stir! ");
                            hint.append(state.getStirProgress()).append("/").append(step.getStirSwipesCount());
                            break;
                        case SHAKE_PAN:
                            hint.append("Swipe the pan left and right! ");
                            hint.append(state.getShakeProgress()).append("/").append(step.getShakeCount());
                            break;
                        case POUR:
                            hint.append("Press and hold to pour!");
                            break;
                        case WAIT_COOK:
                            hint.append("Wait for the food to cook!");
                            break;
                        case SERVE:
                            hint.append("Drag the food to the plate!");
                            break;
                        
                        // Advanced action hints with progress
                        case OVEN_PREHEAT_HOLD:
                        case GRILL_ON_HOLD:
                        case STEAM_START:
                            if (params != null) {
                                int percent = (int)(state.getActionProgress(action) * 100);
                                hint.append(action.getHintText()).append(" ").append(percent).append("%");
                            }
                            break;
                            
                        case ROTATE_KNOB_TEMP:
                            if (params != null) {
                                hint.append("Set temperature to ").append((int)params.getRotaryTarget()).append("°F");
                            }
                            break;
                            
                        case ROTATE_KNOB_TIMER:
                            if (params != null) {
                                hint.append("Set timer to ").append((int)params.getRotaryTarget()).append(" minutes");
                            }
                            break;
                            
                        case WHISK_CIRCLES:
                            if (params != null) {
                                hint.append("Whisk in circles! ");
                                hint.append(state.getCircleProgress()).append("/").append(params.getCircleCount());
                            }
                            break;
                            
                        case KNEAD_DRAG:
                            if (params != null) {
                                hint.append("Knead the dough! ");
                                hint.append(state.getKneadProgress()).append("/").append(params.getKneadRepeats());
                            }
                            break;
                            
                        case GRILL_FLIP_SWIPE:
                            if (params != null) {
                                hint.append("Flip it! ");
                                hint.append(state.getFlipProgress()).append("/").append(params.getFlipCount());
                            }
                            break;
                            
                        case GARNISH_DRAG_SNAP:
                            if (params != null) {
                                hint.append("Place garnish! ");
                                hint.append(state.getGarnishPlacedCount()).append("/").append(params.getSnapPointCount());
                            }
                            break;
                            
                        case TIMING_STOP:
                            if (params != null) {
                                long perfect = params.getPerfectStartMs();
                                hint.append("Press STOP when timer reaches ").append(perfect/1000).append(" seconds!");
                            }
                            break;
                            
                        default:
                            hint.append(step.getChefTextEn());
                    }
                }
                break;
                
            case SPEAKING_CHECK:
                hint.append("Say: \"").append(step.getSpeakingPhraseEn()).append("\"");
                break;
                
            default:
                hint.append(step.getChefTextEn());
        }
        
        return hint.toString();
    }
    
    // ========================
    // NOTIFICATIONS
    // ========================
    
    private void notifyStepChanged(RecipeStep step) {
        if (listener != null) {
            listener.onStepChanged(step, state.getCurrentStepIndex(), 
                state.getCurrentRecipe().getTotalSteps());
        }
    }
    
    private void notifyStepStateChanged(StepState stepState) {
        if (listener != null) {
            listener.onStepStateChanged(stepState);
        }
    }
    
    private void notifyActionProgress(ActionType action, float progress) {
        if (listener != null) {
            listener.onActionProgress(action, Math.min(1f, progress));
        }
    }
    
    private void notifyActionComplete(ActionType action) {
        if (listener != null) {
            listener.onActionComplete(action);
        }
    }
    
    private void notifyItemPlaced(String itemId, ZoneType zone) {
        if (listener != null) {
            listener.onItemPlaced(itemId, zone);
        }
    }
    
    private void notifyStepSuccess(String text) {
        if (listener != null) {
            listener.onStepSuccess(text);
        }
    }
    
    private void notifySpeakingRequired(String phrase) {
        if (listener != null) {
            listener.onSpeakingRequired(phrase);
        }
    }
    
    private void notifySpeakingResult(boolean correct, String phrase) {
        if (listener != null) {
            listener.onSpeakingResult(correct, phrase);
        }
    }
    
    private void notifyRecipeComplete(int score) {
        if (listener != null) {
            listener.onRecipeComplete(score);
        }
    }
    
    private void notifyChefSpeak(String text) {
        if (listener != null) {
            listener.onChefSpeak(text);
        }
    }
    
    private void notifyHint(String hint) {
        if (listener != null) {
            listener.onHintRequested(hint);
        }
    }
    
    private void notifyCookingTimer(long remaining) {
        if (listener != null) {
            listener.onCookingTimer(remaining);
        }
    }
    
    private void notifyError(String message) {
        if (listener != null) {
            listener.onError(message);
        }
    }
    
    /**
     * Called when the user completes or skips the speaking check.
     */
    public void onSpeakingCheckDone(boolean correct) {
        if (state.getStepState() != StepState.SPEAKING_CHECK) {
            return;
        }
        
        notifySpeakingResult(correct, "");
        
        // Move to next step
        moveToNextStep();
    }
}
