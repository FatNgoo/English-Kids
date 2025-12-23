package com.edu.english.masterchef.data;

import java.io.Serializable;

/**
 * Data-driven parameters for various action types.
 * Each action type uses relevant fields; unused fields have default values.
 */
public class ActionParams implements Serializable {
    
    // === TAP/PRESS PARAMETERS ===
    /** Number of taps required */
    private int tapCount = 5;
    
    /** Hold duration in milliseconds */
    private long holdMs = 1500;
    
    // === SWIPE PARAMETERS ===
    /** Number of swipes required */
    private int swipeCount = 6;
    
    /** Number of circular motions required (for whisk) */
    private int circleCount = 8;
    
    // === SHAKE PARAMETERS ===
    /** Number of shakes required */
    private int shakeCount = 4;
    
    // === ROTATION PARAMETERS ===
    /** Minimum value for rotary knob */
    private int rotaryMin = 0;
    
    /** Maximum value for rotary knob */
    private int rotaryMax = 250;
    
    /** Target value user must reach */
    private int rotaryTarget = 180;
    
    /** Tolerance for target value (+/- tolerance is acceptable) */
    private int rotaryTolerance = 10;
    
    // === SLIDER PARAMETERS ===
    /** Target level (0.0 to 1.0) */
    private float sliderTarget = 0.7f;
    
    /** Tolerance for slider level */
    private float sliderTolerance = 0.1f;
    
    // === TIMING PARAMETERS ===
    /** Total duration in milliseconds */
    private long totalMs = 5000;
    
    /** Perfect window start (ms from start) */
    private long perfectStartMs = 3500;
    
    /** Perfect window end (ms from start) */
    private long perfectEndMs = 4500;
    
    /** Cook/Steam/Fry duration */
    private long cookTimeMs = 3000;
    
    /** Pour duration */
    private long pourTimeMs = 1000;
    
    /** Warm-up duration (for steam) */
    private long warmUpMs = 2000;
    
    // === TEMPERATURE PARAMETERS ===
    /** Target temperature */
    private int targetTemp = 180;
    
    /** Wait time for reaching temperature */
    private long tempWaitMs = 3000;
    
    // === TIMER PARAMETERS ===
    /** Target timer in seconds */
    private int timerTargetSeconds = 600; // 10 minutes
    
    // === GARNISH/SNAP PARAMETERS ===
    /** Number of snap points to place */
    private int snapPointCount = 3;
    
    /** Number of items that must be placed */
    private int requiredPlaceCount = 3;
    
    // === FLIP/SEAR PARAMETERS ===
    /** Number of flips required */
    private int flipCount = 2;
    
    /** Timing windows for flips (array of pairs: startMs, endMs) */
    private long[] flipWindowsMs;
    
    // === KNEAD PARAMETERS ===
    /** Number of knead repetitions */
    private int kneadRepeats = 6;
    
    // === Constructors ===
    
    public ActionParams() {
        this.flipWindowsMs = new long[]{2000, 3000, 5000, 6000}; // Default 2 windows
    }
    
    // === Builder pattern ===
    
    public static class Builder {
        private ActionParams params = new ActionParams();
        
        public Builder tapCount(int count) { params.tapCount = count; return this; }
        public Builder holdMs(long ms) { params.holdMs = ms; return this; }
        public Builder swipeCount(int count) { params.swipeCount = count; return this; }
        public Builder circleCount(int count) { params.circleCount = count; return this; }
        public Builder shakeCount(int count) { params.shakeCount = count; return this; }
        
        public Builder rotary(int min, int max, int target, int tolerance) {
            params.rotaryMin = min;
            params.rotaryMax = max;
            params.rotaryTarget = target;
            params.rotaryTolerance = tolerance;
            return this;
        }
        
        public Builder slider(float target, float tolerance) {
            params.sliderTarget = target;
            params.sliderTolerance = tolerance;
            return this;
        }
        
        public Builder timing(long totalMs, long perfectStart, long perfectEnd) {
            params.totalMs = totalMs;
            params.perfectStartMs = perfectStart;
            params.perfectEndMs = perfectEnd;
            return this;
        }
        
        public Builder cookTime(long ms) { params.cookTimeMs = ms; return this; }
        public Builder pourTime(long ms) { params.pourTimeMs = ms; return this; }
        public Builder warmUpMs(long ms) { params.warmUpMs = ms; return this; }
        
        public Builder temperature(int target, long waitMs) {
            params.targetTemp = target;
            params.tempWaitMs = waitMs;
            return this;
        }
        
        public Builder timerSeconds(int seconds) { params.timerTargetSeconds = seconds; return this; }
        
        public Builder snapPoints(int count, int required) {
            params.snapPointCount = count;
            params.requiredPlaceCount = required;
            return this;
        }
        
        public Builder flipCount(int count) { params.flipCount = count; return this; }
        public Builder flipWindows(long... windows) { params.flipWindowsMs = windows; return this; }
        public Builder kneadRepeats(int repeats) { params.kneadRepeats = repeats; return this; }
        
        public ActionParams build() { return params; }
    }
    
    // === Getters ===
    
    public int getTapCount() { return tapCount; }
    public long getHoldMs() { return holdMs; }
    public int getSwipeCount() { return swipeCount; }
    public int getCircleCount() { return circleCount; }
    public int getShakeCount() { return shakeCount; }
    
    public int getRotaryMin() { return rotaryMin; }
    public int getRotaryMax() { return rotaryMax; }
    public int getRotaryTarget() { return rotaryTarget; }
    public int getRotaryTolerance() { return rotaryTolerance; }
    
    public float getSliderTarget() { return sliderTarget; }
    public float getSliderTolerance() { return sliderTolerance; }
    
    public long getTotalMs() { return totalMs; }
    public long getPerfectStartMs() { return perfectStartMs; }
    public long getPerfectEndMs() { return perfectEndMs; }
    public long getCookTimeMs() { return cookTimeMs; }
    public long getPourTimeMs() { return pourTimeMs; }
    public long getWarmUpMs() { return warmUpMs; }
    
    public int getTargetTemp() { return targetTemp; }
    public long getTempWaitMs() { return tempWaitMs; }
    
    public int getTimerTargetSeconds() { return timerTargetSeconds; }
    
    public int getSnapPointCount() { return snapPointCount; }
    public int getRequiredPlaceCount() { return requiredPlaceCount; }
    
    public int getFlipCount() { return flipCount; }
    public long[] getFlipWindowsMs() { return flipWindowsMs; }
    public int getKneadRepeats() { return kneadRepeats; }
    
    // === Setters ===
    
    public void setTapCount(int tapCount) { this.tapCount = tapCount; }
    public void setHoldMs(long holdMs) { this.holdMs = holdMs; }
    public void setSwipeCount(int swipeCount) { this.swipeCount = swipeCount; }
    public void setCircleCount(int circleCount) { this.circleCount = circleCount; }
    public void setShakeCount(int shakeCount) { this.shakeCount = shakeCount; }
    
    public void setRotaryMin(int rotaryMin) { this.rotaryMin = rotaryMin; }
    public void setRotaryMax(int rotaryMax) { this.rotaryMax = rotaryMax; }
    public void setRotaryTarget(int rotaryTarget) { this.rotaryTarget = rotaryTarget; }
    public void setRotaryTolerance(int rotaryTolerance) { this.rotaryTolerance = rotaryTolerance; }
    
    public void setSliderTarget(float sliderTarget) { this.sliderTarget = sliderTarget; }
    public void setSliderTolerance(float sliderTolerance) { this.sliderTolerance = sliderTolerance; }
    
    public void setTotalMs(long totalMs) { this.totalMs = totalMs; }
    public void setPerfectStartMs(long perfectStartMs) { this.perfectStartMs = perfectStartMs; }
    public void setPerfectEndMs(long perfectEndMs) { this.perfectEndMs = perfectEndMs; }
    public void setCookTimeMs(long cookTimeMs) { this.cookTimeMs = cookTimeMs; }
    public void setPourTimeMs(long pourTimeMs) { this.pourTimeMs = pourTimeMs; }
    public void setWarmUpMs(long warmUpMs) { this.warmUpMs = warmUpMs; }
    
    public void setTargetTemp(int targetTemp) { this.targetTemp = targetTemp; }
    public void setTempWaitMs(long tempWaitMs) { this.tempWaitMs = tempWaitMs; }
    
    public void setTimerTargetSeconds(int timerTargetSeconds) { this.timerTargetSeconds = timerTargetSeconds; }
    
    public void setSnapPointCount(int snapPointCount) { this.snapPointCount = snapPointCount; }
    public void setRequiredPlaceCount(int requiredPlaceCount) { this.requiredPlaceCount = requiredPlaceCount; }
    
    public void setFlipCount(int flipCount) { this.flipCount = flipCount; }
    public void setFlipWindowsMs(long[] flipWindowsMs) { this.flipWindowsMs = flipWindowsMs; }
    public void setKneadRepeats(int kneadRepeats) { this.kneadRepeats = kneadRepeats; }
    
    // === Utility methods ===
    
    /**
     * Check if a value is within target range for rotary actions.
     */
    public boolean isRotaryValueAcceptable(float value) {
        return Math.abs(value - rotaryTarget) <= rotaryTolerance;
    }
    
    /**
     * Check if a level is within target range for slider actions.
     */
    public boolean isSliderValueAcceptable(float value) {
        return Math.abs(value - sliderTarget) <= sliderTolerance;
    }
    
    /**
     * Check if timing is within perfect window.
     */
    public boolean isWithinPerfectWindow(long elapsedMs) {
        return elapsedMs >= perfectStartMs && elapsedMs <= perfectEndMs;
    }
    
    /**
     * Check if current time is within any flip window.
     */
    public int getCurrentFlipWindow(long elapsedMs) {
        if (flipWindowsMs == null) return -1;
        for (int i = 0; i < flipWindowsMs.length - 1; i += 2) {
            if (elapsedMs >= flipWindowsMs[i] && elapsedMs <= flipWindowsMs[i + 1]) {
                return i / 2;
            }
        }
        return -1;
    }
}
