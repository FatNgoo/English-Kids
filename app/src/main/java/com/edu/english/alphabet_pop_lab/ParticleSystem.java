package com.edu.english.alphabet_pop_lab;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * ParticleSystem - Creates and manages particle effects for bubble pop
 * Handles burst animation with physics simulation
 */
public class ParticleSystem {
    
    private List<Particle> particles;
    private Random random;
    private Paint particlePaint;
    
    // Particle settings
    private static final int PARTICLES_PER_POP = 20;
    private static final float PARTICLE_LIFETIME = 0.8f; // seconds
    private static final float PARTICLE_SPEED_MIN = 200f;
    private static final float PARTICLE_SPEED_MAX = 500f;
    private static final float GRAVITY = 400f;
    
    public ParticleSystem() {
        particles = new ArrayList<>();
        random = new Random();
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setStyle(Paint.Style.FILL);
    }
    
    /**
     * Create a burst of particles at the specified position
     */
    public void createBurst(float x, float y, int color, float radius) {
        for (int i = 0; i < PARTICLES_PER_POP; i++) {
            // Random angle
            float angle = (float) (random.nextFloat() * Math.PI * 2);
            
            // Random speed
            float speed = PARTICLE_SPEED_MIN + random.nextFloat() * (PARTICLE_SPEED_MAX - PARTICLE_SPEED_MIN);
            
            // Velocity components
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;
            
            // Particle size (smaller pieces)
            float size = radius * (0.1f + random.nextFloat() * 0.15f);
            
            // Vary the color slightly
            int particleColor = varyColor(color, 30);
            
            // Create particle
            Particle particle = new Particle(
                x + (random.nextFloat() - 0.5f) * radius * 0.5f,
                y + (random.nextFloat() - 0.5f) * radius * 0.5f,
                vx, vy, size, particleColor
            );
            
            particles.add(particle);
        }
        
        // Add some sparkle particles
        for (int i = 0; i < 8; i++) {
            float angle = (float) (random.nextFloat() * Math.PI * 2);
            float speed = PARTICLE_SPEED_MAX * (0.8f + random.nextFloat() * 0.4f);
            
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;
            
            Particle sparkle = new Particle(x, y, vx, vy, radius * 0.08f, Color.WHITE);
            sparkle.isSparkle = true;
            particles.add(sparkle);
        }
    }
    
    /**
     * Update all particles
     */
    public void update(float deltaTime) {
        Iterator<Particle> iterator = particles.iterator();
        
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.update(deltaTime);
            
            // Remove dead particles
            if (particle.isDead()) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Draw all particles
     */
    public void draw(Canvas canvas) {
        for (Particle particle : particles) {
            particle.draw(canvas, particlePaint);
        }
    }
    
    /**
     * Check if there are active particles
     */
    public boolean hasActiveParticles() {
        return !particles.isEmpty();
    }
    
    /**
     * Clear all particles
     */
    public void clear() {
        particles.clear();
    }
    
    /**
     * Vary a color randomly
     */
    private int varyColor(int color, int variance) {
        int r = Color.red(color) + random.nextInt(variance * 2) - variance;
        int g = Color.green(color) + random.nextInt(variance * 2) - variance;
        int b = Color.blue(color) + random.nextInt(variance * 2) - variance;
        
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        
        return Color.rgb(r, g, b);
    }
    
    /**
     * Inner class for individual particle
     */
    private class Particle {
        float x, y;
        float vx, vy;
        float size;
        int color;
        float lifetime;
        float maxLifetime;
        boolean isSparkle = false;
        float rotation;
        float rotationSpeed;
        
        public Particle(float x, float y, float vx, float vy, float size, int color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.color = color;
            this.lifetime = PARTICLE_LIFETIME * (0.7f + random.nextFloat() * 0.3f);
            this.maxLifetime = lifetime;
            this.rotation = random.nextFloat() * 360f;
            this.rotationSpeed = (random.nextFloat() - 0.5f) * 720f;
        }
        
        public void update(float deltaTime) {
            // Apply velocity
            x += vx * deltaTime;
            y += vy * deltaTime;
            
            // Apply gravity
            vy += GRAVITY * deltaTime;
            
            // Apply drag
            vx *= 0.98f;
            vy *= 0.98f;
            
            // Rotate
            rotation += rotationSpeed * deltaTime;
            
            // Decrease lifetime
            lifetime -= deltaTime;
            
            // Shrink over time
            if (lifetime < maxLifetime * 0.3f) {
                size *= 0.95f;
            }
        }
        
        public void draw(Canvas canvas, Paint paint) {
            if (isDead()) return;
            
            // Calculate alpha based on lifetime
            float progress = 1f - (lifetime / maxLifetime);
            float alpha = 1f - easeInQuad(progress);
            
            if (isSparkle) {
                // Draw sparkle as a small bright dot
                paint.setColor(Color.argb(
                    (int) (alpha * 255),
                    255, 255, 255
                ));
                canvas.drawCircle(x, y, size, paint);
                
                // Add glow effect
                paint.setColor(Color.argb(
                    (int) (alpha * 100),
                    255, 255, 200
                ));
                canvas.drawCircle(x, y, size * 2, paint);
            } else {
                // Draw colored particle
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                
                paint.setColor(Color.argb((int) (alpha * 255), r, g, b));
                
                // Save canvas state
                canvas.save();
                canvas.translate(x, y);
                canvas.rotate(rotation);
                
                // Draw as rounded rectangle for variety
                float halfSize = size / 2;
                canvas.drawRoundRect(-halfSize, -halfSize, halfSize, halfSize, 
                    size * 0.3f, size * 0.3f, paint);
                
                // Restore canvas
                canvas.restore();
            }
        }
        
        public boolean isDead() {
            return lifetime <= 0 || size < 1f;
        }
        
        // Easing function
        private float easeInQuad(float t) {
            return t * t;
        }
    }
}
