package com.edu.english.masterchef.engine;

import com.edu.english.masterchef.data.ActionParams;
import com.edu.english.masterchef.data.ActionType;
import com.edu.english.masterchef.data.Recipe;
import com.edu.english.masterchef.data.RecipeStep;
import com.edu.english.masterchef.data.StepState;
import com.edu.english.masterchef.data.ZoneType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds the current state of a Master Chef game session.
 */
public class GameSessionState {
    
    // Current recipe being cooked
    private Recipe currentRecipe;
    
    // Current step index (0-based)
    private int currentStepIndex;
    
    // State of the current step
    private StepState stepState;
    
    // Progress trackers
    private int cutProgress;
    private int poundProgress;
    private int stirProgress;
    private int shakeProgress;
    private long pourStartTime;
    private long cookStartTime;
    
    // === NEW PROGRESS TRACKERS ===
    
    // Hold action progress
    private long holdStartTime;
    private boolean isHolding;
    
    // Circle/Whisk progress
    private int circleProgress;
    
    // Knead progress
    private int kneadProgress;
    
    // Flip progress
    private int flipProgress;
    private long[] flipTimes; // timestamps of flips
    
    // Rotary knob values
    private float temperatureKnobValue;  // 0-300+ degrees
    private float timerKnobValue;        // 0-60 minutes
    
    // Slider values
    private float heatSliderValue;       // 0-10
    private float oilLevelSliderValue;   // 0-100%
    
    // Timing action
    private long timingStartTime;
    private long timingStopTime;
    private boolean timingActive;
    
    // Temperature state
    private float currentTemperature;
    private boolean isPreheated;
    
    // Steam/Bubble/Sear states
    private boolean isSteaming;
    private boolean isBoiling;
    private boolean isSearing;
    private long stateStartTime;
    
    // Garnish state
    private int garnishPlacedCount;
    private int seasonShakeCount;
    private int drizzleCount;
    
    // Skim foam progress
    private int skimProgress;
    
    // Items placed in each zone
    private Map<ZoneType, List<String>> zoneItems;
    
    // Items that have been processed (cut, pounded, etc.)
    private Set<String> processedItems;
    
    // Items collected from fridge
    private Set<String> inventoryItems;
    
    // Currently dragging item
    private String draggingItemId;
    private boolean isDragging;
    
    // Tool currently in use
    private String activeToolId;
    
    // Game stats
    private int totalScore;
    private int stepsCompleted;
    private int speakingAttempts;
    private int speakingCorrect;
    
    // Flags
    private boolean isGameComplete;
    private boolean isPaused;
    
    public GameSessionState() {
        reset();
    }
    
    public void reset() {
        currentStepIndex = 0;
        stepState = StepState.WAITING_ITEM;
        
        cutProgress = 0;
        poundProgress = 0;
        stirProgress = 0;
        shakeProgress = 0;
        pourStartTime = 0;
        cookStartTime = 0;
        
        // Reset new progress trackers
        holdStartTime = 0;
        isHolding = false;
        circleProgress = 0;
        kneadProgress = 0;
        flipProgress = 0;
        flipTimes = new long[10];
        
        temperatureKnobValue = 0f;
        timerKnobValue = 0f;
        heatSliderValue = 0f;
        oilLevelSliderValue = 0f;
        
        timingStartTime = 0;
        timingStopTime = 0;
        timingActive = false;
        
        currentTemperature = 0f;
        isPreheated = false;
        
        isSteaming = false;
        isBoiling = false;
        isSearing = false;
        stateStartTime = 0;
        
        garnishPlacedCount = 0;
        seasonShakeCount = 0;
        drizzleCount = 0;
        skimProgress = 0;
        
        zoneItems = new HashMap<>();
        for (ZoneType zone : ZoneType.values()) {
            zoneItems.put(zone, new ArrayList<>());
        }
        
        processedItems = new HashSet<>();
        inventoryItems = new HashSet<>();
        
        draggingItemId = null;
        isDragging = false;
        activeToolId = null;
        
        totalScore = 0;
        stepsCompleted = 0;
        speakingAttempts = 0;
        speakingCorrect = 0;
        
        isGameComplete = false;
        isPaused = false;
    }
    
    public void startRecipe(Recipe recipe) {
        this.currentRecipe = recipe;
        reset();
    }
    
    // === Current Step ===
    
    public RecipeStep getCurrentStep() {
        if (currentRecipe != null && currentStepIndex < currentRecipe.getTotalSteps()) {
            return currentRecipe.getStep(currentStepIndex);
        }
        return null;
    }
    
    public boolean advanceToNextStep() {
        if (currentRecipe != null) {
            currentStepIndex++;
            if (currentStepIndex >= currentRecipe.getTotalSteps()) {
                isGameComplete = true;
                stepState = StepState.RECIPE_DONE;
                return false;
            }
            resetStepProgress();
            return true;
        }
        return false;
    }
    
    public void resetStepProgress() {
        cutProgress = 0;
        poundProgress = 0;
        stirProgress = 0;
        shakeProgress = 0;
        pourStartTime = 0;
        cookStartTime = 0;
        
        // Reset new step-specific progress
        holdStartTime = 0;
        isHolding = false;
        circleProgress = 0;
        kneadProgress = 0;
        flipProgress = 0;
        flipTimes = new long[10];
        
        timingStartTime = 0;
        timingStopTime = 0;
        timingActive = false;
        
        garnishPlacedCount = 0;
        seasonShakeCount = 0;
        drizzleCount = 0;
        skimProgress = 0;
        
        stepState = StepState.WAITING_ITEM;
    }
    
    // === Zone Management ===
    
    public void addItemToZone(ZoneType zone, String itemId) {
        if (!zoneItems.containsKey(zone)) {
            zoneItems.put(zone, new ArrayList<>());
        }
        zoneItems.get(zone).add(itemId);
        inventoryItems.add(itemId);
    }
    
    public void removeItemFromZone(ZoneType zone, String itemId) {
        List<String> items = zoneItems.get(zone);
        if (items != null) {
            items.remove(itemId);
        }
    }
    
    public List<String> getItemsInZone(ZoneType zone) {
        return zoneItems.getOrDefault(zone, new ArrayList<>());
    }
    
    public boolean isItemInZone(ZoneType zone, String itemId) {
        List<String> items = zoneItems.get(zone);
        return items != null && items.contains(itemId);
    }
    
    public void clearZone(ZoneType zone) {
        if (zoneItems.containsKey(zone)) {
            zoneItems.get(zone).clear();
        }
    }
    
    // === Progress ===
    
    public void incrementCutProgress() {
        cutProgress++;
    }
    
    public void incrementPoundProgress() {
        poundProgress++;
    }
    
    public void incrementStirProgress() {
        stirProgress++;
    }
    
    public void incrementShakeProgress() {
        shakeProgress++;
    }
    
    public void startPouring() {
        pourStartTime = System.currentTimeMillis();
    }
    
    public long getPourDuration() {
        if (pourStartTime > 0) {
            return System.currentTimeMillis() - pourStartTime;
        }
        return 0;
    }
    
    public void startCooking() {
        cookStartTime = System.currentTimeMillis();
    }
    
    public long getCookDuration() {
        if (cookStartTime > 0) {
            return System.currentTimeMillis() - cookStartTime;
        }
        return 0;
    }
    
    public boolean isActionComplete(ActionType action) {
        RecipeStep step = getCurrentStep();
        if (step == null) return false;
        
        ActionParams params = step.getActionParams();
        
        switch (action) {
            case TAP_TO_CUT:
                return cutProgress >= step.getTapCount();
            case TAP_TO_POUND:
                return poundProgress >= step.getTapCount();
            case SWIPE_TO_STIR:
                return stirProgress >= step.getStirSwipesCount();
            case SHAKE_PAN:
                return shakeProgress >= step.getShakeCount();
            case POUR:
                return getPourDuration() >= step.getPourTimeMs();
            case WAIT_COOK:
                return getCookDuration() >= step.getCookTimeMs();
            
            // === NEW ACTIONS ===
            
            // Hold actions
            case OVEN_PREHEAT_HOLD:
            case GRILL_ON_HOLD:
            case STEAM_START:
                return params != null && getHoldDuration() >= params.getHoldMs();
            
            // Rotary actions
            case ROTATE_KNOB_TEMP:
                return params != null && params.isRotaryValueAcceptable(temperatureKnobValue);
            case ROTATE_KNOB_TIMER:
                return params != null && params.isRotaryValueAcceptable(timerKnobValue);
            
            // Slider actions
            case OIL_LEVEL_SLIDER:
                return params != null && params.isSliderValueAcceptable(oilLevelSliderValue);
            case HEAT_LEVEL_SLIDER:
                return params != null && params.isSliderValueAcceptable(heatSliderValue);
            
            // Timed wait actions
            case WAIT_OIL_HEAT:
            case WAIT_BOIL:
            case WAIT_SEAR:
                return params != null && getStateDuration() >= params.getWarmUpMs();
            
            // Timing stop action
            case TIMING_STOP:
                return params != null && timingStopTime > 0 && 
                       params.isWithinPerfectWindow(timingStopTime - timingStartTime);
            
            // Circle/Whisk action
            case WHISK_CIRCLES:
                return params != null && circleProgress >= params.getCircleCount();
            
            // Knead action
            case KNEAD_DRAG:
                return params != null && kneadProgress >= params.getKneadRepeats();
            
            // Flip actions
            case GRILL_FLIP_SWIPE:
                return params != null && flipProgress >= params.getFlipCount();
            
            // Shake actions
            case SHAKE_BASKET:
            case SEASON_SHAKE:
                return params != null && seasonShakeCount >= params.getShakeCount();
            
            // Tap actions
            case VENT_STEAM_TAP:
            case TAP_TO_CRACK:
            case TAP_TO_DRIZZLE:
                return params != null && drizzleCount >= params.getTapCount();
            
            // Swipe actions
            case WIPE_FOG_SWIPE:
            case SKIM_FOAM_SWIPE:
                return params != null && skimProgress >= params.getSwipeCount();
            
            // Garnish action
            case GARNISH_DRAG_SNAP:
                return params != null && garnishPlacedCount >= params.getSnapPointCount();
            
            default:
                return true;
        }
    }
    
    public float getActionProgress(ActionType action) {
        RecipeStep step = getCurrentStep();
        if (step == null) return 0f;
        
        ActionParams params = step.getActionParams();
        
        switch (action) {
            case TAP_TO_CUT:
                return (float) cutProgress / step.getTapCount();
            case TAP_TO_POUND:
                return (float) poundProgress / step.getTapCount();
            case SWIPE_TO_STIR:
                return (float) stirProgress / step.getStirSwipesCount();
            case SHAKE_PAN:
                return (float) shakeProgress / step.getShakeCount();
            case POUR:
                return Math.min(1f, (float) getPourDuration() / step.getPourTimeMs());
            case WAIT_COOK:
                return Math.min(1f, (float) getCookDuration() / step.getCookTimeMs());
            
            // === NEW ACTIONS ===
            
            // Hold actions
            case OVEN_PREHEAT_HOLD:
            case GRILL_ON_HOLD:
            case STEAM_START:
                if (params == null || params.getHoldMs() == 0) return 0f;
                return Math.min(1f, (float) getHoldDuration() / params.getHoldMs());
            
            // Rotary actions
            case ROTATE_KNOB_TEMP:
                if (params == null) return 0f;
                float tempRange = params.getRotaryMax() - params.getRotaryMin();
                return tempRange > 0 ? (temperatureKnobValue - params.getRotaryMin()) / tempRange : 0f;
            case ROTATE_KNOB_TIMER:
                if (params == null) return 0f;
                float timerRange = params.getRotaryMax() - params.getRotaryMin();
                return timerRange > 0 ? (timerKnobValue - params.getRotaryMin()) / timerRange : 0f;
            
            // Slider actions
            case OIL_LEVEL_SLIDER:
            case HEAT_LEVEL_SLIDER:
                if (params == null || params.getSliderTarget() == 0) return 0f;
                float sliderVal = action == ActionType.OIL_LEVEL_SLIDER ? oilLevelSliderValue : heatSliderValue;
                return Math.min(1f, sliderVal / params.getSliderTarget());
            
            // Timed wait actions
            case WAIT_OIL_HEAT:
            case WAIT_BOIL:
            case WAIT_SEAR:
                if (params == null || params.getWarmUpMs() == 0) return 0f;
                return Math.min(1f, (float) getStateDuration() / params.getWarmUpMs());
            
            // Timing stop action
            case TIMING_STOP:
                if (params == null || params.getTotalMs() == 0) return 0f;
                if (!timingActive && timingStartTime > 0) {
                    return Math.min(1f, (float) (System.currentTimeMillis() - timingStartTime) / params.getTotalMs());
                }
                return 0f;
            
            // Circle/Whisk action
            case WHISK_CIRCLES:
                if (params == null || params.getCircleCount() == 0) return 0f;
                return (float) circleProgress / params.getCircleCount();
            
            // Knead action
            case KNEAD_DRAG:
                if (params == null || params.getKneadRepeats() == 0) return 0f;
                return (float) kneadProgress / params.getKneadRepeats();
            
            // Flip actions
            case GRILL_FLIP_SWIPE:
                if (params == null || params.getFlipCount() == 0) return 0f;
                return (float) flipProgress / params.getFlipCount();
            
            // Shake actions
            case SHAKE_BASKET:
            case SEASON_SHAKE:
                if (params == null || params.getShakeCount() == 0) return 0f;
                return (float) seasonShakeCount / params.getShakeCount();
            
            // Tap actions
            case VENT_STEAM_TAP:
            case TAP_TO_CRACK:
            case TAP_TO_DRIZZLE:
                if (params == null || params.getTapCount() == 0) return 0f;
                return (float) drizzleCount / params.getTapCount();
            
            // Swipe actions
            case WIPE_FOG_SWIPE:
            case SKIM_FOAM_SWIPE:
                if (params == null || params.getSwipeCount() == 0) return 0f;
                return (float) skimProgress / params.getSwipeCount();
            
            // Garnish action
            case GARNISH_DRAG_SNAP:
                if (params == null || params.getSnapPointCount() == 0) return 0f;
                return (float) garnishPlacedCount / params.getSnapPointCount();
            
            default:
                return 1f;
        }
    }
    
    // === Processed Items ===
    
    public void markItemProcessed(String itemId) {
        processedItems.add(itemId);
    }
    
    public boolean isItemProcessed(String itemId) {
        return processedItems.contains(itemId);
    }
    
    // === Scoring ===
    
    public void addScore(int points) {
        totalScore += points;
    }
    
    public void recordSpeakingAttempt(boolean correct) {
        speakingAttempts++;
        if (correct) {
            speakingCorrect++;
            addScore(10);
        }
    }
    
    public void completeStep() {
        stepsCompleted++;
        addScore(20);
    }
    
    // === Getters & Setters ===
    
    public Recipe getCurrentRecipe() { return currentRecipe; }
    public int getCurrentStepIndex() { return currentStepIndex; }
    public StepState getStepState() { return stepState; }
    public void setStepState(StepState state) { this.stepState = state; }
    
    public String getDraggingItemId() { return draggingItemId; }
    public void setDraggingItemId(String id) { this.draggingItemId = id; }
    public boolean isDragging() { return isDragging; }
    public void setDragging(boolean dragging) { this.isDragging = dragging; }
    
    public String getActiveToolId() { return activeToolId; }
    public void setActiveToolId(String id) { this.activeToolId = id; }
    
    public int getTotalScore() { return totalScore; }
    public int getStepsCompleted() { return stepsCompleted; }
    public int getSpeakingAttempts() { return speakingAttempts; }
    public int getSpeakingCorrect() { return speakingCorrect; }
    
    public boolean isGameComplete() { return isGameComplete; }
    public void setGameComplete(boolean complete) { this.isGameComplete = complete; }
    
    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { this.isPaused = paused; }
    
    public int getCutProgress() { return cutProgress; }
    public int getPoundProgress() { return poundProgress; }
    public int getStirProgress() { return stirProgress; }
    public int getShakeProgress() { return shakeProgress; }
    
    public Set<String> getInventoryItems() { return inventoryItems; }
    
    public String getProgressText() {
        if (currentRecipe == null) return "";
        return "Step " + (currentStepIndex + 1) + " / " + currentRecipe.getTotalSteps();
    }
    
    // ========================
    // NEW HELPER METHODS
    // ========================
    
    // === Hold Actions ===
    
    public void startHolding() {
        holdStartTime = System.currentTimeMillis();
        isHolding = true;
    }
    
    public void stopHolding() {
        isHolding = false;
    }
    
    public long getHoldDuration() {
        if (holdStartTime > 0 && isHolding) {
            return System.currentTimeMillis() - holdStartTime;
        }
        return 0;
    }
    
    public boolean isHolding() { return isHolding; }
    
    // === State Duration (for WAIT_* actions) ===
    
    public void startStateTimer() {
        stateStartTime = System.currentTimeMillis();
    }
    
    public long getStateDuration() {
        if (stateStartTime > 0) {
            return System.currentTimeMillis() - stateStartTime;
        }
        return 0;
    }
    
    // === Circle/Whisk ===
    
    public void incrementCircleProgress() {
        circleProgress++;
    }
    
    public int getCircleProgress() { return circleProgress; }
    
    // === Knead ===
    
    public void incrementKneadProgress() {
        kneadProgress++;
    }
    
    public int getKneadProgress() { return kneadProgress; }
    
    // === Flip ===
    
    public void recordFlip() {
        if (flipProgress < flipTimes.length) {
            flipTimes[flipProgress] = System.currentTimeMillis();
            flipProgress++;
        }
    }
    
    public int getFlipProgress() { return flipProgress; }
    public long[] getFlipTimes() { return flipTimes; }
    
    // === Rotary Knobs ===
    
    public void setTemperatureKnobValue(float value) {
        this.temperatureKnobValue = value;
    }
    
    public float getTemperatureKnobValue() { return temperatureKnobValue; }
    
    public void setTimerKnobValue(float value) {
        this.timerKnobValue = value;
    }
    
    public float getTimerKnobValue() { return timerKnobValue; }
    
    // === Sliders ===
    
    public void setHeatSliderValue(float value) {
        this.heatSliderValue = value;
    }
    
    public float getHeatSliderValue() { return heatSliderValue; }
    
    public void setOilLevelSliderValue(float value) {
        this.oilLevelSliderValue = value;
    }
    
    public float getOilLevelSliderValue() { return oilLevelSliderValue; }
    
    // === Timing Stop ===
    
    public void startTiming() {
        timingStartTime = System.currentTimeMillis();
        timingActive = true;
    }
    
    public void stopTiming() {
        if (timingActive) {
            timingStopTime = System.currentTimeMillis();
            timingActive = false;
        }
    }
    
    public long getTimingElapsed() {
        if (timingStartTime > 0) {
            if (timingActive) {
                return System.currentTimeMillis() - timingStartTime;
            } else {
                return timingStopTime - timingStartTime;
            }
        }
        return 0;
    }
    
    public boolean isTimingActive() { return timingActive; }
    
    // === Temperature ===
    
    public void setCurrentTemperature(float temp) {
        this.currentTemperature = temp;
    }
    
    public float getCurrentTemperature() { return currentTemperature; }
    
    public void setPreheated(boolean preheated) {
        this.isPreheated = preheated;
    }
    
    public boolean isPreheated() { return isPreheated; }
    
    // === Steam/Boil/Sear States ===
    
    public void setSteaming(boolean steaming) {
        this.isSteaming = steaming;
        if (steaming) startStateTimer();
    }
    
    public boolean isSteaming() { return isSteaming; }
    
    public void setBoiling(boolean boiling) {
        this.isBoiling = boiling;
        if (boiling) startStateTimer();
    }
    
    public boolean isBoiling() { return isBoiling; }
    
    public void setSearing(boolean searing) {
        this.isSearing = searing;
        if (searing) startStateTimer();
    }
    
    public boolean isSearing() { return isSearing; }
    
    // === Garnish/Season/Drizzle ===
    
    public void incrementGarnishPlaced() {
        garnishPlacedCount++;
    }
    
    public int getGarnishPlacedCount() { return garnishPlacedCount; }
    
    public void incrementSeasonShake() {
        seasonShakeCount++;
    }
    
    public int getSeasonShakeCount() { return seasonShakeCount; }
    
    public void incrementDrizzle() {
        drizzleCount++;
    }
    
    public int getDrizzleCount() { return drizzleCount; }
    
    // === Skim ===
    
    public void incrementSkimProgress() {
        skimProgress++;
    }
    
    public int getSkimProgress() { return skimProgress; }
}
