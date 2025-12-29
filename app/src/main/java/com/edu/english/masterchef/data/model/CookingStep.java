package com.edu.english.masterchef.data.model;

/**
 * Represents a single cooking step in Scene 3
 * Step types:
 * - DragDropToZone: Drag item to specific zone
 * - TapOnItem: Tap on item (e.g., crack egg)
 * - TapOnZone: Tap on zone (e.g., toggle stove)
 * - HoldToPour: Hold to pour liquid
 * - StirGesture: Circular gesture in zone
 * - PanShakeGesture: Shake gesture
 * - TimerWait: Wait for timer
 * - PlateServe: Final serving step
 */
public class CookingStep {
    private String stepType;
    private String itemId; // ingredient or tool id
    private String targetZone; // zone id (e.g., "zone_cutting")
    private int requiredCount; // for tap count or quantity
    private int requiredHoldMs; // for hold duration
    private float requiredProgress; // for stir/shake (0.0 - 1.0)
    private String instructionText; // Display text for user
    private String hintImageRes; // Hint icon drawable name

    public CookingStep() {
    }

    public CookingStep(String stepType, String itemId, String targetZone, 
                      int requiredCount, int requiredHoldMs, float requiredProgress,
                      String instructionText, String hintImageRes) {
        this.stepType = stepType;
        this.itemId = itemId;
        this.targetZone = targetZone;
        this.requiredCount = requiredCount;
        this.requiredHoldMs = requiredHoldMs;
        this.requiredProgress = requiredProgress;
        this.instructionText = instructionText;
        this.hintImageRes = hintImageRes;
    }

    // Factory methods for common step types
    public static CookingStep dragDrop(String itemId, String targetZone, String instruction) {
        return new CookingStep("DragDropToZone", itemId, targetZone, 1, 0, 0f, instruction, null);
    }

    public static CookingStep tapItem(String itemId, int count, String instruction) {
        return new CookingStep("TapOnItem", itemId, null, count, 0, 0f, instruction, null);
    }

    public static CookingStep tapZone(String zoneId, int count, String instruction) {
        return new CookingStep("TapOnZone", null, zoneId, count, 0, 0f, instruction, null);
    }

    public static CookingStep hold(String itemId, String targetZone, int holdMs, String instruction) {
        return new CookingStep("HoldToPour", itemId, targetZone, 0, holdMs, 0f, instruction, null);
    }

    public static CookingStep stir(String zoneId, float progress, String instruction) {
        return new CookingStep("StirGesture", null, zoneId, 0, 0, progress, instruction, null);
    }

    public static CookingStep timerWait(int seconds, String instruction) {
        return new CookingStep("TimerWait", null, null, 0, seconds * 1000, 0f, instruction, null);
    }

    // Getters and Setters
    public String getStepType() {
        return stepType;
    }

    public void setStepType(String stepType) {
        this.stepType = stepType;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTargetZone() {
        return targetZone;
    }

    public void setTargetZone(String targetZone) {
        this.targetZone = targetZone;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(int requiredCount) {
        this.requiredCount = requiredCount;
    }

    public int getRequiredHoldMs() {
        return requiredHoldMs;
    }

    public void setRequiredHoldMs(int requiredHoldMs) {
        this.requiredHoldMs = requiredHoldMs;
    }

    public float getRequiredProgress() {
        return requiredProgress;
    }

    public void setRequiredProgress(float requiredProgress) {
        this.requiredProgress = requiredProgress;
    }

    public String getInstructionText() {
        return instructionText;
    }

    public void setInstructionText(String instructionText) {
        this.instructionText = instructionText;
    }

    public String getHintImageRes() {
        return hintImageRes;
    }

    public void setHintImageRes(String hintImageRes) {
        this.hintImageRes = hintImageRes;
    }

    // Convenience methods
    public String getInstruction() {
        return instructionText;
    }

    public String getTargetItemId() {
        return itemId;
    }

    public String getTargetZoneId() {
        return targetZone;
    }
}
