package com.edu.english.alphabet_pop_lab;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;

/**
 * Bubble class representing a floating alphabet bubble
 * Contains physics for movement, collision detection, and rendering
 */
public class Bubble {
    // Position
    public float x, y;
    
    // Velocity
    public float vx, vy;
    
    // Size
    public float radius;
    public float baseRadius;
    
    // Letter
    public char letter;
    
    // Visual properties
    public int baseColor;
    public float alpha = 1.0f;
    public float scale = 1.0f;
    
    // State
    public boolean isPopped = false;
    public boolean isSelected = false;
    
    // Animation
    public float wobblePhase = 0f;
    public float wobbleAmplitude = 0f;
    
    // Paints
    private Paint bubblePaint;
    private Paint highlightPaint;
    private Paint letterPaint;
    private Paint shadowPaint;
    
    // Screen bounds
    private int screenWidth, screenHeight;
    
    // Constants
    private static final float FRICTION = 0.995f;
    private static final float BOUNCE_DAMPING = 0.7f;
    private static final float MAX_VELOCITY = 150f;
    private static final float WOBBLE_DECAY = 0.95f;
    
    public Bubble(char letter, float x, float y, float radius, int color, int screenWidth, int screenHeight) {
        this.letter = letter;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.baseRadius = radius;
        this.baseColor = color;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // Random initial velocity (slow drift)
        this.vx = (float) (Math.random() * 60 - 30);
        this.vy = (float) (Math.random() * 60 - 30);
        
        // Random wobble phase
        this.wobblePhase = (float) (Math.random() * Math.PI * 2);
        
        initPaints();
    }
    
    private void initPaints() {
        // Main bubble paint with gradient
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setStyle(Paint.Style.FILL);
        
        // Highlight paint for glossy effect
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);
        
        // Letter paint
        letterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        letterPaint.setColor(Color.WHITE);
        letterPaint.setTextAlign(Paint.Align.CENTER);
        letterPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        // Shadow paint
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(30, 0, 0, 0));
    }
    
    /**
     * Update bubble position and physics
     * @param deltaTime Time since last frame in seconds
     */
    public void update(float deltaTime) {
        if (isPopped) return;
        
        // Apply velocity
        x += vx * deltaTime;
        y += vy * deltaTime;
        
        // Apply friction
        vx *= FRICTION;
        vy *= FRICTION;
        
        // Clamp velocity
        vx = Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, vx));
        vy = Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, vy));
        
        // Wall collision with bounce
        float margin = radius;
        
        if (x - margin < 0) {
            x = margin;
            vx = Math.abs(vx) * BOUNCE_DAMPING;
            wobbleAmplitude = 0.1f;
        } else if (x + margin > screenWidth) {
            x = screenWidth - margin;
            vx = -Math.abs(vx) * BOUNCE_DAMPING;
            wobbleAmplitude = 0.1f;
        }
        
        if (y - margin < 0) {
            y = margin;
            vy = Math.abs(vy) * BOUNCE_DAMPING;
            wobbleAmplitude = 0.1f;
        } else if (y + margin > screenHeight) {
            y = screenHeight - margin;
            vy = -Math.abs(vy) * BOUNCE_DAMPING;
            wobbleAmplitude = 0.1f;
        }
        
        // Update wobble
        wobblePhase += deltaTime * 5f;
        wobbleAmplitude *= WOBBLE_DECAY;
        
        // Calculate wobbled radius
        float wobble = (float) Math.sin(wobblePhase) * wobbleAmplitude;
        radius = baseRadius * scale * (1f + wobble);
    }
    
    /**
     * Draw the bubble with all visual effects
     */
    public void draw(Canvas canvas) {
        if (isPopped) return;
        
        float currentAlpha = alpha;
        float drawRadius = radius;
        
        // Draw shadow
        canvas.drawCircle(x + 4, y + 6, drawRadius, shadowPaint);
        
        // Create gradient for bubble
        RadialGradient gradient = new RadialGradient(
            x - drawRadius * 0.3f,
            y - drawRadius * 0.3f,
            drawRadius * 2,
            new int[] {
                adjustAlpha(lightenColor(baseColor, 0.4f), currentAlpha),
                adjustAlpha(baseColor, currentAlpha),
                adjustAlpha(darkenColor(baseColor, 0.2f), currentAlpha)
            },
            new float[] {0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        bubblePaint.setShader(gradient);
        
        // Draw main bubble
        canvas.drawCircle(x, y, drawRadius, bubblePaint);
        
        // Draw highlight (glossy effect)
        RadialGradient highlightGradient = new RadialGradient(
            x - drawRadius * 0.35f,
            y - drawRadius * 0.35f,
            drawRadius * 0.6f,
            new int[] {
                adjustAlpha(Color.WHITE, currentAlpha * 0.7f),
                adjustAlpha(Color.WHITE, currentAlpha * 0.2f),
                adjustAlpha(Color.WHITE, 0f)
            },
            new float[] {0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        highlightPaint.setShader(highlightGradient);
        canvas.drawCircle(x - drawRadius * 0.2f, y - drawRadius * 0.25f, drawRadius * 0.5f, highlightPaint);
        
        // Draw letter
        letterPaint.setTextSize(drawRadius * 1.2f);
        letterPaint.setAlpha((int) (currentAlpha * 255));
        
        // Center text vertically
        Paint.FontMetrics fm = letterPaint.getFontMetrics();
        float textY = y - (fm.ascent + fm.descent) / 2;
        
        canvas.drawText(String.valueOf(letter), x, textY, letterPaint);
    }
    
    /**
     * Check if point is inside bubble (for touch detection)
     */
    public boolean contains(float touchX, float touchY) {
        float dx = touchX - x;
        float dy = touchY - y;
        return (dx * dx + dy * dy) <= (radius * radius);
    }
    
    /**
     * Handle collision with another bubble
     */
    public void handleCollision(Bubble other) {
        if (isPopped || other.isPopped) return;
        
        float dx = other.x - x;
        float dy = other.y - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float minDist = radius + other.radius;
        
        if (distance < minDist && distance > 0) {
            // Normalize direction
            float nx = dx / distance;
            float ny = dy / distance;
            
            // Separate bubbles
            float overlap = (minDist - distance) / 2f;
            x -= nx * overlap;
            y -= ny * overlap;
            other.x += nx * overlap;
            other.y += ny * overlap;
            
            // Exchange velocities (soft bounce)
            float dampingFactor = 0.3f;
            float relativeVx = vx - other.vx;
            float relativeVy = vy - other.vy;
            float dotProduct = relativeVx * nx + relativeVy * ny;
            
            if (dotProduct > 0) {
                vx -= dampingFactor * dotProduct * nx;
                vy -= dampingFactor * dotProduct * ny;
                other.vx += dampingFactor * dotProduct * nx;
                other.vy += dampingFactor * dotProduct * ny;
            }
            
            // Add wobble effect
            wobbleAmplitude = 0.05f;
            other.wobbleAmplitude = 0.05f;
        }
    }
    
    /**
     * Start pop animation
     */
    public void pop() {
        isPopped = true;
    }
    
    // Helper methods for color manipulation
    private int lightenColor(int color, float factor) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        
        r = (int) Math.min(255, r + (255 - r) * factor);
        g = (int) Math.min(255, g + (255 - g) * factor);
        b = (int) Math.min(255, b + (255 - b) * factor);
        
        return Color.rgb(r, g, b);
    }
    
    private int darkenColor(int color, float factor) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        
        r = (int) (r * (1 - factor));
        g = (int) (g * (1 - factor));
        b = (int) (b * (1 - factor));
        
        return Color.rgb(r, g, b);
    }
    
    private int adjustAlpha(int color, float alphaFactor) {
        int alpha = (int) (Color.alpha(color) * alphaFactor);
        if (Color.alpha(color) == 0) {
            alpha = (int) (255 * alphaFactor);
        }
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
