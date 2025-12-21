package com.edu.english.coloralchemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Physics-based liquid simulation
 * Simulates realistic liquid behavior with wave motion and gravity response
 */
public class LiquidSimulation {
    
    // Liquid properties
    private int color;
    private float level; // 0.0 to 1.0
    private float targetLevel;
    
    // Wave simulation
    private float waveOffset;
    private float waveAmplitude;
    private float waveFrequency;
    private float wavePhase;
    private float waveDamping;
    
    // Physics
    private float velocityX;
    private float velocityY;
    private float tiltAngle; // Current tilt based on accelerometer
    private float targetTiltAngle;
    private float sloshing; // Additional wave from shaking
    
    // Container dimensions
    private RectF container;
    private float cornerRadius;
    
    // Rendering
    private Paint liquidPaint;
    private Paint highlightPaint;
    private Paint bubblePaint;
    private Path liquidPath;
    
    // Bubbles
    private float[] bubbleX;
    private float[] bubbleY;
    private float[] bubbleSize;
    private float[] bubbleSpeed;
    private int bubbleCount = 8;
    
    // State
    private boolean isPouring;
    private boolean isShaking;
    private float pourProgress;
    
    public LiquidSimulation() {
        color = Color.RED;
        level = 0.7f;
        targetLevel = 0.7f;
        
        waveOffset = 0;
        waveAmplitude = 0;
        waveFrequency = 3.0f;
        wavePhase = 0;
        waveDamping = 0.95f;
        
        velocityX = 0;
        velocityY = 0;
        tiltAngle = 0;
        targetTiltAngle = 0;
        sloshing = 0;
        
        container = new RectF();
        cornerRadius = 20f;
        
        liquidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        liquidPaint.setStyle(Paint.Style.FILL);
        
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);
        
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setStyle(Paint.Style.FILL);
        
        liquidPath = new Path();
        
        initBubbles();
    }
    
    /**
     * Initialize bubble particles
     */
    private void initBubbles() {
        bubbleX = new float[bubbleCount];
        bubbleY = new float[bubbleCount];
        bubbleSize = new float[bubbleCount];
        bubbleSpeed = new float[bubbleCount];
        
        for (int i = 0; i < bubbleCount; i++) {
            resetBubble(i);
        }
    }
    
    /**
     * Reset a bubble to starting position
     */
    private void resetBubble(int index) {
        bubbleX[index] = (float) Math.random();
        bubbleY[index] = 1.0f + (float) Math.random() * 0.2f;
        bubbleSize[index] = 3f + (float) Math.random() * 6f;
        bubbleSpeed[index] = 0.1f + (float) Math.random() * 0.2f;
    }
    
    /**
     * Set the container bounds
     */
    public void setContainer(float left, float top, float right, float bottom, float radius) {
        container.set(left, top, right, bottom);
        cornerRadius = radius;
    }
    
    /**
     * Set liquid color
     */
    public void setColor(int newColor) {
        this.color = newColor;
        updatePaints();
    }
    
    /**
     * Get liquid color
     */
    public int getColor() {
        return color;
    }
    
    /**
     * Blend current color with another color
     */
    public void blendColor(int otherColor, float ratio) {
        color = EasingFunctions.lerpColor(color, otherColor, ratio);
        updatePaints();
    }
    
    /**
     * Apply shade adjustment (lighter/darker)
     */
    public void applyShade(float shadeValue) {
        // shadeValue: -1 = darkest, 0 = normal, 1 = lightest
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        
        if (shadeValue > 0) {
            // Add white (lighter)
            r = (int) (r + (255 - r) * shadeValue);
            g = (int) (g + (255 - g) * shadeValue);
            b = (int) (b + (255 - b) * shadeValue);
        } else if (shadeValue < 0) {
            // Add black (darker)
            float factor = 1 + shadeValue;
            r = (int) (r * factor);
            g = (int) (g * factor);
            b = (int) (b * factor);
        }
        
        color = Color.rgb(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b))
        );
        updatePaints();
    }
    
    /**
     * Update paint shaders
     */
    private void updatePaints() {
        // Create gradient for liquid depth effect
        int lighterColor = adjustBrightness(color, 1.3f);
        int darkerColor = adjustBrightness(color, 0.7f);
        
        if (container.width() > 0 && container.height() > 0) {
            LinearGradient gradient = new LinearGradient(
                container.left, container.top,
                container.right, container.bottom,
                new int[]{lighterColor, color, darkerColor},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
            );
            liquidPaint.setShader(gradient);
        } else {
            liquidPaint.setShader(null);
            liquidPaint.setColor(color);
        }
        
        // Highlight paint
        highlightPaint.setColor(Color.argb(80, 255, 255, 255));
        
        // Bubble paint
        bubblePaint.setColor(Color.argb(100, 255, 255, 255));
    }
    
    /**
     * Adjust color brightness
     */
    private int adjustBrightness(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.min(255, (int) (Color.red(color) * factor));
        int g = Math.min(255, (int) (Color.green(color) * factor));
        int b = Math.min(255, (int) (Color.blue(color) * factor));
        return Color.argb(a, r, g, b);
    }
    
    /**
     * Set liquid level (0.0 to 1.0)
     */
    public void setLevel(float newLevel) {
        this.level = EasingFunctions.clamp(newLevel, 0f, 1f);
        this.targetLevel = this.level;
    }
    
    /**
     * Animate level change
     */
    public void animateLevelTo(float newLevel) {
        this.targetLevel = EasingFunctions.clamp(newLevel, 0f, 1f);
    }
    
    /**
     * Get current level
     */
    public float getLevel() {
        return level;
    }
    
    /**
     * Update accelerometer values for gravity simulation
     */
    public void updateAccelerometer(float accelX, float accelY, float accelZ) {
        // Calculate tilt angle from accelerometer
        targetTiltAngle = -accelX * 3f; // Inverted and scaled
        
        // Add to wave amplitude based on movement
        float movement = Math.abs(accelX) + Math.abs(accelY);
        if (movement > 2f) {
            waveAmplitude = Math.min(waveAmplitude + movement * 0.5f, 15f);
        }
    }
    
    /**
     * Apply shake effect
     */
    public void shake(float intensity) {
        isShaking = true;
        sloshing = intensity * 20f;
        waveAmplitude = Math.min(waveAmplitude + intensity * 10f, 20f);
    }
    
    /**
     * Start pouring animation
     */
    public void startPour() {
        isPouring = true;
        pourProgress = 0;
    }
    
    /**
     * Stop pouring
     */
    public void stopPour() {
        isPouring = false;
    }
    
    /**
     * Check if empty
     */
    public boolean isEmpty() {
        return level <= 0.01f;
    }
    
    /**
     * Update simulation
     */
    public void update(float deltaTime) {
        // Smooth tilt transition
        tiltAngle = EasingFunctions.lerp(tiltAngle, targetTiltAngle, deltaTime * 5f);
        
        // Update wave phase
        wavePhase += deltaTime * waveFrequency * 2f * (float) Math.PI;
        
        // Dampen wave amplitude
        waveAmplitude *= (float) Math.pow(waveDamping, deltaTime * 60f);
        if (waveAmplitude < 0.5f) {
            waveAmplitude = 0;
        }
        
        // Dampen sloshing
        sloshing *= (float) Math.pow(0.9f, deltaTime * 60f);
        if (Math.abs(sloshing) < 0.5f) {
            sloshing = 0;
            isShaking = false;
        }
        
        // Animate level change
        if (Math.abs(level - targetLevel) > 0.001f) {
            level = EasingFunctions.lerp(level, targetLevel, deltaTime * 3f);
        }
        
        // Update pour progress
        if (isPouring) {
            pourProgress += deltaTime;
            targetLevel = Math.max(0, level - deltaTime * 0.3f);
        }
        
        // Update bubbles
        updateBubbles(deltaTime);
    }
    
    /**
     * Update bubble positions
     */
    private void updateBubbles(float deltaTime) {
        for (int i = 0; i < bubbleCount; i++) {
            bubbleY[i] -= bubbleSpeed[i] * deltaTime;
            
            // Add slight horizontal wobble
            bubbleX[i] += (float) Math.sin(wavePhase + i) * 0.01f;
            
            // Reset bubble if it reaches surface
            if (bubbleY[i] < (1 - level)) {
                resetBubble(i);
            }
        }
    }
    
    /**
     * Draw liquid
     */
    public void draw(Canvas canvas) {
        if (level <= 0 || container.isEmpty()) return;
        
        updatePaints();
        
        // Calculate liquid surface
        float containerHeight = container.height();
        float liquidTop = container.top + containerHeight * (1 - level);
        
        // Build liquid path with wave effect
        liquidPath.reset();
        
        float waveHeight = waveAmplitude + Math.abs(sloshing);
        
        // Start from bottom left
        liquidPath.moveTo(container.left, container.bottom);
        
        // Draw left side
        liquidPath.lineTo(container.left, liquidTop + tiltAngle);
        
        // Draw wavy top surface
        int segments = 20;
        float segmentWidth = container.width() / segments;
        
        for (int i = 0; i <= segments; i++) {
            float x = container.left + i * segmentWidth;
            float normalizedX = (float) i / segments;
            
            // Combine multiple waves for realistic effect
            float wave1 = (float) Math.sin(wavePhase + normalizedX * Math.PI * 2) * waveHeight;
            float wave2 = (float) Math.sin(wavePhase * 1.5f + normalizedX * Math.PI * 3) * waveHeight * 0.3f;
            float tiltOffset = tiltAngle * (normalizedX - 0.5f) * 2f;
            float sloshOffset = sloshing * (float) Math.sin(normalizedX * Math.PI);
            
            float y = liquidTop + wave1 + wave2 + tiltOffset + sloshOffset;
            
            if (i == 0) {
                liquidPath.lineTo(x, y);
            } else {
                liquidPath.lineTo(x, y);
            }
        }
        
        // Draw right side and close path
        liquidPath.lineTo(container.right, container.bottom);
        liquidPath.close();
        
        // Clip to container bounds
        canvas.save();
        
        // Draw rounded container clip
        Path clipPath = new Path();
        clipPath.addRoundRect(container, cornerRadius, cornerRadius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        
        // Draw liquid
        canvas.drawPath(liquidPath, liquidPaint);
        
        // Draw highlight reflection
        float highlightWidth = container.width() * 0.15f;
        float highlightLeft = container.left + container.width() * 0.1f;
        RectF highlightRect = new RectF(
            highlightLeft,
            liquidTop + 10,
            highlightLeft + highlightWidth,
            container.bottom - 10
        );
        canvas.drawRoundRect(highlightRect, highlightWidth / 2, highlightWidth / 2, highlightPaint);
        
        // Draw bubbles
        drawBubbles(canvas, liquidTop);
        
        canvas.restore();
    }
    
    /**
     * Draw bubbles inside liquid
     */
    private void drawBubbles(Canvas canvas, float liquidTop) {
        float liquidHeight = container.bottom - liquidTop;
        
        for (int i = 0; i < bubbleCount; i++) {
            if (bubbleY[i] > (1 - level) && bubbleY[i] < 1) {
                float x = container.left + bubbleX[i] * container.width();
                float normalizedY = (bubbleY[i] - (1 - level)) / level;
                float y = liquidTop + normalizedY * liquidHeight;
                
                // Draw bubble with gradient
                RadialGradient bubbleGradient = new RadialGradient(
                    x - bubbleSize[i] * 0.3f,
                    y - bubbleSize[i] * 0.3f,
                    bubbleSize[i],
                    Color.argb(150, 255, 255, 255),
                    Color.argb(30, 255, 255, 255),
                    Shader.TileMode.CLAMP
                );
                bubblePaint.setShader(bubbleGradient);
                canvas.drawCircle(x, y, bubbleSize[i], bubblePaint);
            }
        }
    }
    
    /**
     * Create pouring stream path
     */
    public Path getPourStreamPath(float startX, float startY, float endX, float endY, float width) {
        Path stream = new Path();
        
        // Control points for curved stream
        float ctrlX = (startX + endX) / 2;
        float ctrlY = startY + (endY - startY) * 0.3f;
        
        // Left edge of stream
        stream.moveTo(startX - width / 2, startY);
        stream.quadTo(ctrlX - width / 2, ctrlY, endX - width / 4, endY);
        
        // Right edge of stream
        stream.lineTo(endX + width / 4, endY);
        stream.quadTo(ctrlX + width / 2, ctrlY, startX + width / 2, startY);
        stream.close();
        
        return stream;
    }
}
