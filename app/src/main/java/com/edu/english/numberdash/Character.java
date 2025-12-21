package com.edu.english.numberdash;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Represents a playable character in Number Dash Race
 * Each character has multiple animation states
 */
public class Character {
    
    // Character identity
    private String name;
    private int primaryColor;
    private int secondaryColor;
    private int eyeColor;
    private CharacterType type;
    
    // Position and size
    private float x;
    private float y;
    private float width;
    private float height;
    
    // Animation state
    public enum AnimationState {
        IDLE, RUNNING, BOOSTING, STUMBLING
    }
    private AnimationState currentState = AnimationState.IDLE;
    private float animationTimer = 0f;
    private int currentFrame = 0;
    
    // Visual effects
    private float scaleX = 1f;
    private float scaleY = 1f;
    private float rotation = 0f;
    private float bounceOffset = 0f;
    private float glowIntensity = 0f;
    
    // Particles for boost effect
    private Particle[] boostParticles;
    private int activeParticles = 0;
    
    // Dust particles for running effect
    private Particle[] dustParticles;
    private float dustSpawnTimer = 0f;
    
    // Character types
    public enum CharacterType {
        BUNNY(0xFFFF9AA2, 0xFFFFB7B2, 0xFF333333, "Bunny"),      // Pink bunny
        BEAR(0xFF8B5E3C, 0xFFA67C52, 0xFF333333, "Bear"),        // Brown bear
        CAT(0xFFFFB366, 0xFFFFCC80, 0xFF4CAF50, "Cat"),          // Orange cat
        DOG(0xFF90A4AE, 0xFFB0BEC5, 0xFF333333, "Dog"),          // Grey dog
        PANDA(0xFFFFFFFF, 0xFF333333, 0xFF333333, "Panda");      // Black and white panda
        
        final int primary;
        final int secondary;
        final int eyes;
        final String displayName;
        
        CharacterType(int primary, int secondary, int eyes, String name) {
            this.primary = primary;
            this.secondary = secondary;
            this.eyes = eyes;
            this.displayName = name;
        }
    }
    
    public Character(CharacterType type) {
        this.type = type;
        this.name = type.displayName;
        this.primaryColor = type.primary;
        this.secondaryColor = type.secondary;
        this.eyeColor = type.eyes;
        this.width = GameConstants.CHARACTER_WIDTH;
        this.height = GameConstants.CHARACTER_HEIGHT;
        
        // Initialize boost particles
        boostParticles = new Particle[GameConstants.BOOST_PARTICLES];
        for (int i = 0; i < boostParticles.length; i++) {
            boostParticles[i] = new Particle();
        }
        
        // Initialize dust particles
        dustParticles = new Particle[10];
        for (int i = 0; i < dustParticles.length; i++) {
            dustParticles[i] = new Particle();
        }
    }
    
    /**
     * Update character animation and effects
     * @param deltaTime Time since last frame in milliseconds
     * @param gameState Current game state
     */
    public void update(float deltaTime, GameState gameState) {
        // Update animation timer
        animationTimer += deltaTime;
        
        // Determine animation state based on game state
        if (gameState.isBoosting()) {
            setAnimationState(AnimationState.BOOSTING);
        } else if (gameState.isSlowing()) {
            setAnimationState(AnimationState.STUMBLING);
        } else if (gameState.getCurrentState() == GameConstants.STATE_RACING) {
            setAnimationState(AnimationState.RUNNING);
        } else {
            setAnimationState(AnimationState.IDLE);
        }
        
        // Update frame based on current state
        float frameDuration = getFrameDuration();
        if (animationTimer >= frameDuration) {
            animationTimer -= frameDuration;
            currentFrame++;
            if (currentFrame >= getFrameCount()) {
                currentFrame = 0;
            }
        }
        
        // Calculate bounce offset for running animation
        if (currentState == AnimationState.RUNNING || currentState == AnimationState.BOOSTING) {
            float bouncePhase = (float) Math.sin(System.currentTimeMillis() * 0.015f);
            bounceOffset = bouncePhase * 8f;
            if (currentState == AnimationState.BOOSTING) {
                bounceOffset *= 1.5f;
            }
        } else if (currentState == AnimationState.STUMBLING) {
            // Shake effect for stumbling
            float shakePhase = (float) Math.sin(System.currentTimeMillis() * 0.05f);
            bounceOffset = shakePhase * 5f;
            rotation = shakePhase * 5f;
        } else {
            // Gentle breathing for idle
            float breathPhase = (float) Math.sin(System.currentTimeMillis() * 0.003f);
            bounceOffset = breathPhase * 3f;
            rotation = 0;
        }
        
        // Update glow intensity
        if (currentState == AnimationState.BOOSTING) {
            glowIntensity = Math.min(1f, glowIntensity + deltaTime * 0.01f);
        } else {
            glowIntensity = Math.max(0f, glowIntensity - deltaTime * 0.005f);
        }
        
        // Update boost particles
        if (currentState == AnimationState.BOOSTING && activeParticles < boostParticles.length) {
            // Spawn new particle
            if (Math.random() < 0.3) {
                spawnBoostParticle();
            }
        }
        
        for (Particle particle : boostParticles) {
            if (particle.isActive()) {
                particle.update(deltaTime);
            }
        }
        
        // Update dust particles when running
        if (currentState == AnimationState.RUNNING || currentState == AnimationState.BOOSTING) {
            dustSpawnTimer += deltaTime;
            if (dustSpawnTimer >= 100f) { // Spawn every 100ms
                dustSpawnTimer = 0;
                spawnDustParticle();
            }
        }
        
        for (Particle particle : dustParticles) {
            if (particle.isActive()) {
                particle.update(deltaTime);
            }
        }
    }
    
    /**
     * Draw the character
     */
    public void draw(Canvas canvas, Paint paint) {
        canvas.save();
        
        // Apply transformations
        float centerX = x + width / 2;
        float centerY = y + height / 2 + bounceOffset;
        
        canvas.translate(centerX, centerY);
        canvas.rotate(rotation);
        canvas.scale(scaleX, scaleY);
        canvas.translate(-width / 2, -height / 2);
        
        // Draw glow effect when boosting
        if (glowIntensity > 0) {
            paint.setColor(0x60FFFF00);
            paint.setStyle(Paint.Style.FILL);
            float glowSize = width * 0.2f * glowIntensity;
            canvas.drawOval(new RectF(-glowSize, -glowSize, width + glowSize, height + glowSize), paint);
        }
        
        // Draw shadow
        paint.setColor(GameConstants.COLOR_SHADOW);
        canvas.drawOval(new RectF(10, height - 10, width - 10, height + 10), paint);
        
        // Draw character based on type
        drawCharacterSprite(canvas, paint);
        
        canvas.restore();
        
        // Draw dust particles (behind character)
        for (Particle particle : dustParticles) {
            if (particle.isActive()) {
                particle.draw(canvas, paint);
            }
        }
        
        // Draw boost particles (in world space)
        for (Particle particle : boostParticles) {
            if (particle.isActive()) {
                particle.draw(canvas, paint);
            }
        }
    }
    
    /**
     * Draw the character sprite based on type and animation state
     */
    private void drawCharacterSprite(Canvas canvas, Paint paint) {
        // Body
        paint.setColor(primaryColor);
        paint.setStyle(Paint.Style.FILL);
        
        float bodyTop = height * 0.2f;
        float bodyHeight = height * 0.65f;
        
        // Main body - rounded rectangle
        RectF bodyRect = new RectF(width * 0.15f, bodyTop, width * 0.85f, bodyTop + bodyHeight);
        canvas.drawRoundRect(bodyRect, width * 0.3f, width * 0.3f, paint);
        
        // Head
        float headRadius = width * 0.32f;
        float headCenterX = width * 0.5f;
        float headCenterY = bodyTop + headRadius * 0.3f;
        canvas.drawCircle(headCenterX, headCenterY, headRadius, paint);
        
        // Ears based on character type
        drawEars(canvas, paint, headCenterX, headCenterY, headRadius);
        
        // Face features
        // Belly/chest (lighter color)
        paint.setColor(secondaryColor);
        RectF bellyRect = new RectF(width * 0.3f, bodyTop + bodyHeight * 0.2f, 
                                    width * 0.7f, bodyTop + bodyHeight * 0.7f);
        canvas.drawOval(bellyRect, paint);
        
        // Eyes
        paint.setColor(0xFFFFFFFF);
        float eyeRadius = headRadius * 0.25f;
        float eyeY = headCenterY - headRadius * 0.1f;
        float eyeSpacing = headRadius * 0.4f;
        canvas.drawCircle(headCenterX - eyeSpacing, eyeY, eyeRadius, paint);
        canvas.drawCircle(headCenterX + eyeSpacing, eyeY, eyeRadius, paint);
        
        // Pupils - animate based on state
        paint.setColor(eyeColor);
        float pupilRadius = eyeRadius * 0.5f;
        float pupilOffset = 0;
        if (currentState == AnimationState.BOOSTING) {
            pupilOffset = eyeRadius * 0.2f; // Looking forward
        } else if (currentState == AnimationState.STUMBLING) {
            pupilOffset = -eyeRadius * 0.1f; // Looking down
        }
        canvas.drawCircle(headCenterX - eyeSpacing + pupilOffset, eyeY, pupilRadius, paint);
        canvas.drawCircle(headCenterX + eyeSpacing + pupilOffset, eyeY, pupilRadius, paint);
        
        // Eye highlights
        paint.setColor(0xFFFFFFFF);
        float highlightRadius = pupilRadius * 0.4f;
        canvas.drawCircle(headCenterX - eyeSpacing + pupilOffset - highlightRadius, 
                          eyeY - highlightRadius, highlightRadius, paint);
        canvas.drawCircle(headCenterX + eyeSpacing + pupilOffset - highlightRadius, 
                          eyeY - highlightRadius, highlightRadius, paint);
        
        // Mouth - changes based on state
        drawMouth(canvas, paint, headCenterX, headCenterY + headRadius * 0.4f);
        
        // Cheeks (blush)
        paint.setColor(0x40FF6B6B);
        float cheekRadius = headRadius * 0.15f;
        canvas.drawCircle(headCenterX - eyeSpacing - cheekRadius, eyeY + cheekRadius * 2, cheekRadius, paint);
        canvas.drawCircle(headCenterX + eyeSpacing + cheekRadius, eyeY + cheekRadius * 2, cheekRadius, paint);
        
        // Legs - animate based on state
        drawLegs(canvas, paint);
    }
    
    private void drawEars(Canvas canvas, Paint paint, float headX, float headY, float headRadius) {
        paint.setColor(primaryColor);
        
        switch (type) {
            case BUNNY:
                // Long bunny ears
                float earWidth = headRadius * 0.35f;
                float earHeight = headRadius * 1.2f;
                RectF leftEar = new RectF(headX - headRadius * 0.6f - earWidth/2, 
                                          headY - headRadius - earHeight,
                                          headX - headRadius * 0.6f + earWidth/2, 
                                          headY - headRadius * 0.3f);
                RectF rightEar = new RectF(headX + headRadius * 0.6f - earWidth/2, 
                                           headY - headRadius - earHeight,
                                           headX + headRadius * 0.6f + earWidth/2, 
                                           headY - headRadius * 0.3f);
                canvas.drawRoundRect(leftEar, earWidth/2, earWidth/2, paint);
                canvas.drawRoundRect(rightEar, earWidth/2, earWidth/2, paint);
                // Inner ear
                paint.setColor(secondaryColor);
                RectF leftInner = new RectF(leftEar.left + earWidth * 0.2f, leftEar.top + earHeight * 0.1f,
                                           leftEar.right - earWidth * 0.2f, leftEar.bottom - earHeight * 0.3f);
                RectF rightInner = new RectF(rightEar.left + earWidth * 0.2f, rightEar.top + earHeight * 0.1f,
                                            rightEar.right - earWidth * 0.2f, rightEar.bottom - earHeight * 0.3f);
                canvas.drawRoundRect(leftInner, earWidth/3, earWidth/3, paint);
                canvas.drawRoundRect(rightInner, earWidth/3, earWidth/3, paint);
                break;
                
            case BEAR:
            case PANDA:
                // Round bear ears
                float bearEarRadius = headRadius * 0.35f;
                float earY = headY - headRadius * 0.7f;
                canvas.drawCircle(headX - headRadius * 0.65f, earY, bearEarRadius, paint);
                canvas.drawCircle(headX + headRadius * 0.65f, earY, bearEarRadius, paint);
                if (type == CharacterType.PANDA) {
                    paint.setColor(secondaryColor); // Black inner ears for panda
                    canvas.drawCircle(headX - headRadius * 0.65f, earY, bearEarRadius * 0.5f, paint);
                    canvas.drawCircle(headX + headRadius * 0.65f, earY, bearEarRadius * 0.5f, paint);
                }
                break;
                
            case CAT:
                // Triangle cat ears
                float catEarSize = headRadius * 0.5f;
                android.graphics.Path leftCatEar = new android.graphics.Path();
                leftCatEar.moveTo(headX - headRadius * 0.5f, headY - headRadius * 0.5f);
                leftCatEar.lineTo(headX - headRadius * 0.8f, headY - headRadius - catEarSize);
                leftCatEar.lineTo(headX - headRadius * 0.1f, headY - headRadius * 0.5f);
                leftCatEar.close();
                canvas.drawPath(leftCatEar, paint);
                
                android.graphics.Path rightCatEar = new android.graphics.Path();
                rightCatEar.moveTo(headX + headRadius * 0.5f, headY - headRadius * 0.5f);
                rightCatEar.lineTo(headX + headRadius * 0.8f, headY - headRadius - catEarSize);
                rightCatEar.lineTo(headX + headRadius * 0.1f, headY - headRadius * 0.5f);
                rightCatEar.close();
                canvas.drawPath(rightCatEar, paint);
                break;
                
            case DOG:
                // Floppy dog ears
                float dogEarWidth = headRadius * 0.4f;
                float dogEarHeight = headRadius * 0.8f;
                RectF leftDogEar = new RectF(headX - headRadius - dogEarWidth * 0.3f, 
                                             headY - headRadius * 0.2f,
                                             headX - headRadius * 0.5f, 
                                             headY + dogEarHeight);
                RectF rightDogEar = new RectF(headX + headRadius * 0.5f, 
                                              headY - headRadius * 0.2f,
                                              headX + headRadius + dogEarWidth * 0.3f, 
                                              headY + dogEarHeight);
                canvas.drawRoundRect(leftDogEar, dogEarWidth/2, dogEarWidth/2, paint);
                canvas.drawRoundRect(rightDogEar, dogEarWidth/2, dogEarWidth/2, paint);
                break;
        }
    }
    
    private void drawMouth(Canvas canvas, Paint paint, float mouthX, float mouthY) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(0xFF333333);
        
        RectF mouthRect;
        switch (currentState) {
            case BOOSTING:
                // Big happy smile
                mouthRect = new RectF(mouthX - 15, mouthY - 15, mouthX + 15, mouthY + 15);
                canvas.drawArc(mouthRect, 0, 180, false, paint);
                break;
            case STUMBLING:
                // Worried mouth
                mouthRect = new RectF(mouthX - 10, mouthY, mouthX + 10, mouthY + 20);
                canvas.drawArc(mouthRect, 180, 180, false, paint);
                break;
            default:
                // Normal smile
                mouthRect = new RectF(mouthX - 12, mouthY - 8, mouthX + 12, mouthY + 8);
                canvas.drawArc(mouthRect, 0, 180, false, paint);
                break;
        }
        
        paint.setStyle(Paint.Style.FILL);
    }
    
    private void drawLegs(Canvas canvas, Paint paint) {
        paint.setColor(primaryColor);
        
        float legWidth = width * 0.18f;
        float legHeight = height * 0.25f;
        float legY = height * 0.75f;
        
        // Animate legs based on state
        float leftLegOffset = 0;
        float rightLegOffset = 0;
        
        if (currentState == AnimationState.RUNNING || currentState == AnimationState.BOOSTING) {
            // Faster leg animation for running
            float runSpeed = currentState == AnimationState.BOOSTING ? 0.03f : 0.02f;
            float runPhase = (float) Math.sin(System.currentTimeMillis() * runSpeed);
            float runAmplitude = currentState == AnimationState.BOOSTING ? 20f : 15f;
            leftLegOffset = runPhase * runAmplitude;
            rightLegOffset = -runPhase * runAmplitude;
        }
        
        // Left leg
        RectF leftLeg = new RectF(width * 0.2f, legY + leftLegOffset, 
                                  width * 0.2f + legWidth, legY + legHeight + leftLegOffset);
        canvas.drawRoundRect(leftLeg, legWidth/2, legWidth/2, paint);
        
        // Right leg
        RectF rightLeg = new RectF(width * 0.8f - legWidth, legY + rightLegOffset, 
                                   width * 0.8f, legY + legHeight + rightLegOffset);
        canvas.drawRoundRect(rightLeg, legWidth/2, legWidth/2, paint);
        
        // Feet
        paint.setColor(secondaryColor);
        canvas.drawOval(new RectF(width * 0.15f, legY + legHeight + leftLegOffset - 5,
                                  width * 0.2f + legWidth + 5, legY + legHeight + leftLegOffset + 10), paint);
        canvas.drawOval(new RectF(width * 0.8f - legWidth - 5, legY + legHeight + rightLegOffset - 5,
                                  width * 0.85f, legY + legHeight + rightLegOffset + 10), paint);
    }
    
    private void spawnBoostParticle() {
        for (Particle particle : boostParticles) {
            if (!particle.isActive()) {
                float startX = x + width * 0.2f;
                float startY = y + height * 0.5f + bounceOffset + (float)(Math.random() * height * 0.3f);
                particle.spawn(startX, startY, 0xFFFFD700);
                activeParticles++;
                break;
            }
        }
    }
    
    private void spawnDustParticle() {
        for (Particle particle : dustParticles) {
            if (!particle.isActive()) {
                float startX = x + width * 0.3f;
                float startY = y + height * 0.9f + bounceOffset;
                particle.spawn(startX, startY, 0xFFD7CCC8); // Dust color
                particle.vx = -(float)(Math.random() * 150 + 50); // Move backward
                particle.vy = (float)(Math.random() * 20 - 10); // Slight vertical
                particle.lifetime = 300f; // Shorter lifetime
                particle.maxLifetime = 300f;
                particle.size = (float)(Math.random() * 10 + 5); // Smaller
                break;
            }
        }
    }
    
    private float getFrameDuration() {
        switch (currentState) {
            case IDLE: return GameConstants.IDLE_FRAME_DURATION;
            case RUNNING: return GameConstants.RUN_FRAME_DURATION;
            case BOOSTING: return GameConstants.BOOST_FRAME_DURATION;
            case STUMBLING: return GameConstants.STUMBLE_FRAME_DURATION;
            default: return GameConstants.IDLE_FRAME_DURATION;
        }
    }
    
    private int getFrameCount() {
        return 4; // 4 frames for all animations
    }
    
    public void setAnimationState(AnimationState state) {
        if (this.currentState != state) {
            this.currentState = state;
            this.animationTimer = 0;
            this.currentFrame = 0;
        }
    }
    
    // Getters and setters
    public String getName() { return name; }
    public CharacterType getType() { return type; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public void setScale(float sx, float sy) { this.scaleX = sx; this.scaleY = sy; }
    public int getPrimaryColor() { return primaryColor; }
    public AnimationState getAnimationState() { return currentState; }
    
    /**
     * Inner class for boost particles
     */
    public static class Particle {
        private float x, y;
        private float vx, vy;
        private float lifetime;
        private float maxLifetime;
        private int color;
        private float size;
        private boolean active = false;
        
        public void spawn(float x, float y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.vx = -(float)(Math.random() * GameConstants.PARTICLE_SPEED + 50);
            this.vy = (float)(Math.random() * 100 - 50);
            this.lifetime = GameConstants.PARTICLE_LIFETIME;
            this.maxLifetime = GameConstants.PARTICLE_LIFETIME;
            this.size = (float)(Math.random() * 15 + 8);
            this.active = true;
        }
        
        public void update(float deltaTime) {
            if (!active) return;
            
            x += vx * (deltaTime / 1000f);
            y += vy * (deltaTime / 1000f);
            lifetime -= deltaTime;
            
            if (lifetime <= 0) {
                active = false;
            }
        }
        
        public void draw(Canvas canvas, Paint paint) {
            if (!active) return;
            
            float alpha = lifetime / maxLifetime;
            int particleColor = (((int)(alpha * 255) & 0xFF) << 24) | (color & 0x00FFFFFF);
            paint.setColor(particleColor);
            paint.setStyle(Paint.Style.FILL);
            
            float currentSize = size * (0.5f + alpha * 0.5f);
            canvas.drawCircle(x, y, currentSize, paint);
        }
        
        public boolean isActive() { return active; }
    }
}
