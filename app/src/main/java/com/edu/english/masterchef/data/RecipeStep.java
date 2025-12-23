package com.edu.english.masterchef.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Model representing a single step in a recipe.
 * Now uses ActionParams for data-driven action configuration.
 */
public class RecipeStep implements Serializable {
    
    private int stepNumber;
    private String chefTextEn;           // What the chef says
    private String chefTextVi;           // Vietnamese translation (optional)
    private List<String> requiredItems;  // Ingredient IDs needed
    private String requiredTool;         // Tool ID (nullable)
    private ActionType actionType;       // Type of action to perform
    private ZoneType targetZone;         // Zone where action happens
    private String successTextEn;        // Praise text on success
    private String speakingPhraseEn;     // Phrase user must repeat
    private boolean requiresSpeaking;    // Whether speaking check is needed
    
    // Data-driven action parameters
    private ActionParams actionParams;
    
    // Inventory items to show in tray for this step (max 4-6 items)
    // Format: "itemId" or "itemId:count" for stacked items (e.g. "egg:3")
    private List<String> inventoryForStep;
    
    // Legacy thresholds (kept for backward compatibility)
    private int tapCount;                // For TAP_TO_CUT, TAP_TO_POUND
    private int shakeCount;              // For SHAKE_PAN
    private int stirSwipesCount;         // For SWIPE_TO_STIR
    private long cookTimeMs;             // For WAIT_COOK
    private long pourTimeMs;             // For POUR
    
    public RecipeStep() {
        this.requiresSpeaking = true; // Default: speaking required
        this.tapCount = 5;
        this.shakeCount = 4;
        this.stirSwipesCount = 6;
        this.cookTimeMs = 3000;
        this.pourTimeMs = 1000;
        this.actionParams = new ActionParams();
    }
    
    // Builder pattern for convenience
    public static class Builder {
        private RecipeStep step = new RecipeStep();
        
        public Builder stepNumber(int num) { step.stepNumber = num; return this; }
        public Builder chefText(String en) { step.chefTextEn = en; return this; }
        public Builder chefTextVi(String vi) { step.chefTextVi = vi; return this; }
        public Builder requiredItems(List<String> items) { step.requiredItems = items; return this; }
        public Builder requiredTool(String tool) { step.requiredTool = tool; return this; }
        public Builder action(ActionType type) { step.actionType = type; return this; }
        public Builder targetZone(ZoneType zone) { step.targetZone = zone; return this; }
        public Builder successText(String text) { step.successTextEn = text; return this; }
        public Builder speakingPhrase(String phrase) { step.speakingPhraseEn = phrase; return this; }
        public Builder requiresSpeaking(boolean req) { step.requiresSpeaking = req; return this; }
        public Builder tapCount(int count) { step.tapCount = count; step.actionParams.setTapCount(count); return this; }
        public Builder shakeCount(int count) { step.shakeCount = count; step.actionParams.setShakeCount(count); return this; }
        public Builder stirCount(int count) { step.stirSwipesCount = count; step.actionParams.setSwipeCount(count); return this; }
        public Builder cookTime(long ms) { step.cookTimeMs = ms; step.actionParams.setCookTimeMs(ms); return this; }
        public Builder pourTime(long ms) { step.pourTimeMs = ms; step.actionParams.setPourTimeMs(ms); return this; }
        
        // New builders for ActionParams
        public Builder actionParams(ActionParams params) { step.actionParams = params; return this; }
        public Builder holdMs(long ms) { step.actionParams.setHoldMs(ms); return this; }
        public Builder swipeCount(int count) { step.actionParams.setSwipeCount(count); return this; }
        public Builder circleCount(int count) { step.actionParams.setCircleCount(count); return this; }
        
        public Builder rotaryParams(int min, int max, int target, int tolerance) {
            step.actionParams.setRotaryMin(min);
            step.actionParams.setRotaryMax(max);
            step.actionParams.setRotaryTarget(target);
            step.actionParams.setRotaryTolerance(tolerance);
            return this;
        }
        
        public Builder sliderParams(float target, float tolerance) {
            step.actionParams.setSliderTarget(target);
            step.actionParams.setSliderTolerance(tolerance);
            return this;
        }
        
        public Builder timingParams(long totalMs, long perfectStart, long perfectEnd) {
            step.actionParams.setTotalMs(totalMs);
            step.actionParams.setPerfectStartMs(perfectStart);
            step.actionParams.setPerfectEndMs(perfectEnd);
            return this;
        }
        
        public Builder temperatureParams(int target, long waitMs) {
            step.actionParams.setTargetTemp(target);
            step.actionParams.setTempWaitMs(waitMs);
            return this;
        }
        
        public Builder timerSeconds(int seconds) { step.actionParams.setTimerTargetSeconds(seconds); return this; }
        public Builder warmUpMs(long ms) { step.actionParams.setWarmUpMs(ms); return this; }
        
        public Builder snapPoints(int count, int required) {
            step.actionParams.setSnapPointCount(count);
            step.actionParams.setRequiredPlaceCount(required);
            return this;
        }
        
        public Builder flipParams(int count, long... windows) {
            step.actionParams.setFlipCount(count);
            step.actionParams.setFlipWindowsMs(windows);
            return this;
        }
        
        public Builder kneadRepeats(int repeats) { step.actionParams.setKneadRepeats(repeats); return this; }
        
        // Inventory items to show in tray for this step
        public Builder inventoryForStep(List<String> items) { step.inventoryForStep = items; return this; }
        public Builder inventoryForStep(String... items) { step.inventoryForStep = Arrays.asList(items); return this; }
        
        public RecipeStep build() { return step; }
    }
    
    // Getters
    public int getStepNumber() { return stepNumber; }
    public String getChefTextEn() { return chefTextEn; }
    public String getChefTextVi() { return chefTextVi; }
    public List<String> getRequiredItems() { return requiredItems; }
    public String getRequiredTool() { return requiredTool; }
    public ActionType getActionType() { return actionType; }
    public ZoneType getTargetZone() { return targetZone; }
    public String getSuccessTextEn() { return successTextEn; }
    public String getSpeakingPhraseEn() { return speakingPhraseEn; }
    public boolean isRequiresSpeaking() { return requiresSpeaking; }
    public int getTapCount() { return tapCount; }
    public int getShakeCount() { return shakeCount; }
    public int getStirSwipesCount() { return stirSwipesCount; }
    /** Alias for getStirSwipesCount() for consistency */
    public int getStirCount() { return stirSwipesCount; }
    public long getCookTimeMs() { return cookTimeMs; }
    /** Alias for getCookTimeMs() for consistency */
    public long getCookTime() { return cookTimeMs; }
    public long getPourTimeMs() { return pourTimeMs; }
    /** Alias for getPourTimeMs() for consistency */
    public long getPourTime() { return pourTimeMs; }
    
    // ========== ActionParams Convenience Getters ==========
    /** Get hold duration in ms (for OVEN_PREHEAT_HOLD, GRILL_ON_HOLD) */
    public long getHoldMs() { return actionParams != null ? actionParams.getHoldMs() : 1500; }
    
    /** Get warm up duration in ms (for STEAM_START) */
    public long getWarmUpMs() { return actionParams != null ? actionParams.getWarmUpMs() : 3000; }
    
    /** Get swipe count (for WIPE_FOG_SWIPE, etc) */
    public int getSwipeCount() { return actionParams != null ? actionParams.getSwipeCount() : 6; }
    
    /** Get the data-driven action parameters */
    public ActionParams getActionParams() { return actionParams; }
    
    /** Get inventory items to show in tray for this step */
    public List<String> getInventoryForStep() { return inventoryForStep; }
    
    /**
     * Check if this step has specific inventory items defined.
     * If true, tray should only show these items instead of all recipe items.
     */
    public boolean hasInventoryForStep() { 
        return inventoryForStep != null && !inventoryForStep.isEmpty(); 
    }
    
    // Setters
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }
    public void setChefTextEn(String chefTextEn) { this.chefTextEn = chefTextEn; }
    public void setChefTextVi(String chefTextVi) { this.chefTextVi = chefTextVi; }
    public void setRequiredItems(List<String> requiredItems) { this.requiredItems = requiredItems; }
    public void setRequiredTool(String requiredTool) { this.requiredTool = requiredTool; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    public void setTargetZone(ZoneType targetZone) { this.targetZone = targetZone; }
    public void setSuccessTextEn(String successTextEn) { this.successTextEn = successTextEn; }
    public void setSpeakingPhraseEn(String speakingPhraseEn) { this.speakingPhraseEn = speakingPhraseEn; }
    public void setRequiresSpeaking(boolean requiresSpeaking) { this.requiresSpeaking = requiresSpeaking; }
    public void setTapCount(int tapCount) { this.tapCount = tapCount; }
    public void setShakeCount(int shakeCount) { this.shakeCount = shakeCount; }
    public void setStirSwipesCount(int stirSwipesCount) { this.stirSwipesCount = stirSwipesCount; }
    public void setCookTimeMs(long cookTimeMs) { this.cookTimeMs = cookTimeMs; }
    public void setPourTimeMs(long pourTimeMs) { this.pourTimeMs = pourTimeMs; }
    public void setActionParams(ActionParams actionParams) { this.actionParams = actionParams; }
}
