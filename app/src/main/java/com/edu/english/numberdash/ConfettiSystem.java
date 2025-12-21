package com.edu.english.numberdash;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Random;

/**
 * Confetti particle effect for celebration
 */
public class ConfettiSystem {
    
    private ConfettiParticle[] particles;
    private boolean active = false;
    private int screenWidth;
    private int screenHeight;
    private Random random;
    
    // Confetti colors
    private int[] colors = {
        0xFFFF6B6B, // Red
        0xFF4ECDC4, // Teal
        0xFFFFE66D, // Yellow
        0xFF95E1D3, // Mint
        0xFFF38181, // Coral
        0xFF6C5CE7, // Purple
        0xFFFF9F43  // Orange
    };
    
    public ConfettiSystem(int screenWidth, int screenHeight, int particleCount) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.random = new Random();
        
        particles = new ConfettiParticle[particleCount];
        for (int i = 0; i < particleCount; i++) {
            particles[i] = new ConfettiParticle();
        }
    }
    
    /**
     * Start the confetti effect
     */
    public void start() {
        active = true;
        
        for (ConfettiParticle particle : particles) {
            spawnParticle(particle);
        }
    }
    
    /**
     * Stop the confetti effect
     */
    public void stop() {
        active = false;
    }
    
    private void spawnParticle(ConfettiParticle particle) {
        particle.x = random.nextFloat() * screenWidth;
        particle.y = -random.nextFloat() * screenHeight * 0.5f - 50;
        particle.vx = (random.nextFloat() - 0.5f) * 100;
        particle.vy = random.nextFloat() * 200 + 100;
        particle.rotation = random.nextFloat() * 360;
        particle.rotationSpeed = (random.nextFloat() - 0.5f) * 500;
        particle.size = random.nextFloat() * 15 + 8;
        particle.color = colors[random.nextInt(colors.length)];
        particle.shape = random.nextInt(3); // 0=rect, 1=circle, 2=triangle
        particle.active = true;
    }
    
    /**
     * Update all particles
     */
    public void update(float deltaTime) {
        if (!active) return;
        
        float dt = deltaTime / 1000f;
        
        for (ConfettiParticle particle : particles) {
            if (particle.active) {
                // Apply gravity
                particle.vy += 150 * dt;
                
                // Apply air resistance
                particle.vx *= 0.99f;
                
                // Add swaying motion
                particle.vx += (float) Math.sin(particle.y * 0.02f) * 50 * dt;
                
                // Update position
                particle.x += particle.vx * dt;
                particle.y += particle.vy * dt;
                particle.rotation += particle.rotationSpeed * dt;
                
                // Respawn if off screen
                if (particle.y > screenHeight + 50) {
                    spawnParticle(particle);
                }
            }
        }
    }
    
    /**
     * Draw all particles
     */
    public void draw(Canvas canvas, Paint paint) {
        if (!active) return;
        
        for (ConfettiParticle particle : particles) {
            if (particle.active) {
                paint.setColor(particle.color);
                paint.setStyle(Paint.Style.FILL);
                
                canvas.save();
                canvas.translate(particle.x, particle.y);
                canvas.rotate(particle.rotation);
                
                switch (particle.shape) {
                    case 0: // Rectangle
                        canvas.drawRect(-particle.size / 2, -particle.size / 4,
                                       particle.size / 2, particle.size / 4, paint);
                        break;
                    case 1: // Circle
                        canvas.drawCircle(0, 0, particle.size / 2, paint);
                        break;
                    case 2: // Triangle
                        android.graphics.Path path = new android.graphics.Path();
                        path.moveTo(0, -particle.size / 2);
                        path.lineTo(-particle.size / 2, particle.size / 2);
                        path.lineTo(particle.size / 2, particle.size / 2);
                        path.close();
                        canvas.drawPath(path, paint);
                        break;
                }
                
                canvas.restore();
            }
        }
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
    
    /**
     * Inner class for confetti particles
     */
    private static class ConfettiParticle {
        float x, y;
        float vx, vy;
        float rotation;
        float rotationSpeed;
        float size;
        int color;
        int shape;
        boolean active;
    }
}
