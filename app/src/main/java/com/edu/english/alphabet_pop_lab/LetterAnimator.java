package com.edu.english.alphabet_pop_lab;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * LetterAnimator - Handles letter animation after bubble pop
 * Manages zoom to center, shrink to corner transitions
 */
public class LetterAnimator {
    
    // Letter properties
    private char letter;
    private int color;
    
    // Position animation
    private float currentX, currentY;
    private float targetX, targetY;
    private float startX, startY;
    
    // Size animation
    private float currentSize;
    private float targetSize;
    private float startSize;
    
    // Alpha animation
    private float currentAlpha = 1f;
    private float targetAlpha = 1f;
    
    // Animation state
    public enum State {
        IDLE,
        ZOOM_TO_CENTER,
        HOLD,
        SHRINK_TO_CORNER,
        IN_CORNER,
        FADE_OUT
    }
    
    private State currentState = State.IDLE;
    
    // Animation timing
    private float animationProgress = 0f;
    private float animationDuration = 0f;
    private float holdTimer = 0f;
    
    // Screen dimensions
    private int screenWidth, screenHeight;
    
    // Paint
    private Paint letterPaint;
    private Paint shadowPaint;
    private Paint glowPaint;
    
    // Constants
    private static final float ZOOM_DURATION = 0.4f;
    private static final float HOLD_DURATION = 0.5f;
    private static final float SHRINK_DURATION = 0.5f;
    private static final float CORNER_SIZE = 120f;
    private static final float CENTER_SIZE = 300f;
    private static final float OVERSHOOT = 1.15f;
    
    // Corner position
    private float cornerX, cornerY;
    
    public LetterAnimator(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // Corner position (top center)
        cornerX = screenWidth / 2f;
        cornerY = CORNER_SIZE + 60f;
        
        initPaints();
    }
    
    private void initPaints() {
        letterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        letterPaint.setTextAlign(Paint.Align.CENTER);
        letterPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        letterPaint.setColor(Color.WHITE);
        
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setTextAlign(Paint.Align.CENTER);
        shadowPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        shadowPaint.setColor(Color.argb(60, 0, 0, 0));
        
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setTextAlign(Paint.Align.CENTER);
        glowPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }
    
    /**
     * Start zoom to center animation
     */
    public void startZoomToCenter(char letter, int color, float fromX, float fromY) {
        this.letter = letter;
        this.color = color;
        
        // Set start position (from bubble)
        startX = fromX;
        startY = fromY;
        startSize = 60f;
        
        // Target position (center)
        targetX = screenWidth / 2f;
        targetY = screenHeight / 2f - 50f;
        targetSize = CENTER_SIZE;
        
        // Initialize current values
        currentX = startX;
        currentY = startY;
        currentSize = startSize;
        currentAlpha = 1f;
        
        // Start animation
        currentState = State.ZOOM_TO_CENTER;
        animationProgress = 0f;
        animationDuration = ZOOM_DURATION;
    }
    
    /**
     * Start shrink to corner animation (after hold)
     */
    public void startShrinkToCorner() {
        startX = currentX;
        startY = currentY;
        startSize = currentSize;
        
        targetX = cornerX;
        targetY = cornerY;
        targetSize = CORNER_SIZE;
        
        currentState = State.SHRINK_TO_CORNER;
        animationProgress = 0f;
        animationDuration = SHRINK_DURATION;
    }
    
    /**
     * Update animation
     */
    public void update(float deltaTime) {
        switch (currentState) {
            case ZOOM_TO_CENTER:
                updateZoomToCenter(deltaTime);
                break;
                
            case HOLD:
                holdTimer -= deltaTime;
                if (holdTimer <= 0) {
                    startShrinkToCorner();
                }
                break;
                
            case SHRINK_TO_CORNER:
                updateShrinkToCorner(deltaTime);
                break;
                
            case IN_CORNER:
                // Stay in corner
                break;
                
            case FADE_OUT:
                updateFadeOut(deltaTime);
                break;
        }
    }
    
    private void updateZoomToCenter(float deltaTime) {
        animationProgress += deltaTime / animationDuration;
        
        if (animationProgress >= 1f) {
            animationProgress = 1f;
            currentState = State.HOLD;
            holdTimer = HOLD_DURATION;
            
            currentX = targetX;
            currentY = targetY;
            currentSize = targetSize;
        } else {
            // Use overshoot easing
            float t = easeOutBack(animationProgress);
            
            currentX = lerp(startX, targetX, t);
            currentY = lerp(startY, targetY, t);
            currentSize = lerp(startSize, targetSize, t);
        }
    }
    
    private void updateShrinkToCorner(float deltaTime) {
        animationProgress += deltaTime / animationDuration;
        
        if (animationProgress >= 1f) {
            animationProgress = 1f;
            currentState = State.IN_CORNER;
            
            currentX = targetX;
            currentY = targetY;
            currentSize = targetSize;
        } else {
            float t = easeInOutCubic(animationProgress);
            
            currentX = lerp(startX, targetX, t);
            currentY = lerp(startY, targetY, t);
            currentSize = lerp(startSize, targetSize, t);
        }
    }
    
    private void updateFadeOut(float deltaTime) {
        animationProgress += deltaTime / 0.3f;
        
        if (animationProgress >= 1f) {
            currentState = State.IDLE;
            currentAlpha = 0f;
        } else {
            currentAlpha = 1f - easeInQuad(animationProgress);
        }
    }
    
    /**
     * Draw the animated letter
     */
    public void draw(Canvas canvas) {
        if (currentState == State.IDLE) return;
        
        letterPaint.setTextSize(currentSize);
        shadowPaint.setTextSize(currentSize);
        glowPaint.setTextSize(currentSize);
        
        letterPaint.setAlpha((int) (currentAlpha * 255));
        shadowPaint.setAlpha((int) (currentAlpha * 60));
        
        // Center text vertically
        Paint.FontMetrics fm = letterPaint.getFontMetrics();
        float textY = currentY - (fm.ascent + fm.descent) / 2;
        
        // Draw glow when zooming
        if (currentState == State.ZOOM_TO_CENTER || currentState == State.HOLD) {
            // Outer glow
            glowPaint.setColor(Color.argb((int) (currentAlpha * 80), 
                Color.red(color), Color.green(color), Color.blue(color)));
            glowPaint.setTextSize(currentSize * 1.1f);
            canvas.drawText(String.valueOf(letter), currentX, textY, glowPaint);
        }
        
        // Draw shadow
        canvas.drawText(String.valueOf(letter), currentX + 4, textY + 6, shadowPaint);
        
        // Draw letter with color
        letterPaint.setColor(Color.argb((int) (currentAlpha * 255),
            Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawText(String.valueOf(letter), currentX, textY, letterPaint);
        
        // Draw white overlay for highlight
        letterPaint.setColor(Color.argb((int) (currentAlpha * 60), 255, 255, 255));
        canvas.drawText(String.valueOf(letter), currentX - 2, textY - 2, letterPaint);
    }
    
    /**
     * Fade out and reset
     */
    public void fadeOut() {
        if (currentState != State.IDLE) {
            currentState = State.FADE_OUT;
            animationProgress = 0f;
        }
    }
    
    /**
     * Reset to idle state
     */
    public void reset() {
        currentState = State.IDLE;
        currentAlpha = 0f;
    }
    
    /**
     * Check if animation is complete (in corner or idle)
     */
    public boolean isInCorner() {
        return currentState == State.IN_CORNER;
    }
    
    public boolean isAnimating() {
        return currentState != State.IDLE && currentState != State.IN_CORNER;
    }
    
    public State getCurrentState() {
        return currentState;
    }
    
    public char getCurrentLetter() {
        return letter;
    }
    
    public int getCurrentColor() {
        return color;
    }
    
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        cornerX = width / 2f;
        cornerY = CORNER_SIZE + 60f;
    }
    
    // Easing functions
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
    
    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return (float) (1f + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2));
    }
    
    private float easeInOutCubic(float t) {
        return t < 0.5f 
            ? 4f * t * t * t 
            : (float) (1f - Math.pow(-2f * t + 2f, 3) / 2f);
    }
    
    private float easeInQuad(float t) {
        return t * t;
    }
}
