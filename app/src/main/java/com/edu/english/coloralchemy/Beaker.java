package com.edu.english.coloralchemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Beaker game object
 * Central mixing container with liquid simulation and effects
 */
public class Beaker {
    
    // Position and dimensions
    private float x, y;
    private float width, height;
    
    // Animation
    private float scale;
    private float targetScale;
    private float bounceOffset;
    private float glowIntensity;
    
    // Liquid
    private LiquidSimulation liquid;
    private int[] mixedColors;
    private int colorCount;
    private static final int MAX_COLORS = 3;
    
    // State
    public enum State {
        EMPTY,
        HAS_ONE_COLOR,
        HAS_TWO_COLORS,
        READY_TO_MIX,
        MIXING,
        RESULT_READY
    }
    private State currentState;
    
    // Mixing animation
    private float mixProgress;
    private boolean isMixing;
    private int resultColor;
    private String resultColorName;
    private String resultSentence;
    
    // Shake hint
    private boolean showShakeHint;
    private float shakeHintAlpha;
    private float shakeHintBounce;
    
    // Particle effects
    private List<Particle> particles;
    private List<Sparkle> sparkles;
    private Random random;
    
    // Rendering
    private Paint beakerPaint;
    private Paint beakerStrokePaint;
    private Paint beakerShinePaint;
    private Paint shadowPaint;
    private Paint glowPaint;
    private Paint textPaint;
    private Paint hintPaint;
    private Path beakerPath;
    
    // Constants
    private static final float BEAKER_CORNER_RADIUS = 25f;
    private static final float HANDLE_WIDTH = 30f;
    
    /**
     * Particle for bubble/splash effects
     */
    private static class Particle {
        float x, y;
        float vx, vy;
        float size;
        float life;
        float maxLife;
        int color;
        
        Particle(float x, float y, float vx, float vy, float size, float life, int color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.life = life;
            this.maxLife = life;
            this.color = color;
        }
        
        void update(float deltaTime) {
            x += vx * deltaTime;
            y += vy * deltaTime;
            vy += 200 * deltaTime; // Gravity
            life -= deltaTime;
        }
        
        boolean isDead() {
            return life <= 0;
        }
        
        float getAlpha() {
            return Math.max(0, life / maxLife);
        }
    }
    
    /**
     * Sparkle effect for success
     */
    private static class Sparkle {
        float x, y;
        float size;
        float rotation;
        float rotationSpeed;
        float life;
        float maxLife;
        int color;
        
        Sparkle(float x, float y, float size, int color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.rotation = (float) (Math.random() * 360);
            this.rotationSpeed = 100 + (float) Math.random() * 200;
            this.life = 1.5f;
            this.maxLife = 1.5f;
            this.color = color;
        }
        
        void update(float deltaTime) {
            rotation += rotationSpeed * deltaTime;
            life -= deltaTime;
        }
        
        boolean isDead() {
            return life <= 0;
        }
        
        float getAlpha() {
            return Math.max(0, life / maxLife);
        }
        
        float getScale() {
            float t = 1 - (life / maxLife);
            return EasingFunctions.easeOutBack(Math.min(1, t * 2));
        }
    }
    
    public Beaker(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        this.scale = 1.0f;
        this.targetScale = 1.0f;
        this.bounceOffset = 0;
        this.glowIntensity = 0;
        
        this.mixedColors = new int[MAX_COLORS];
        this.colorCount = 0;
        this.currentState = State.EMPTY;
        
        this.mixProgress = 0;
        this.isMixing = false;
        this.resultColor = Color.TRANSPARENT;
        
        this.showShakeHint = false;
        this.shakeHintAlpha = 0;
        this.shakeHintBounce = 0;
        
        this.particles = new ArrayList<>();
        this.sparkles = new ArrayList<>();
        this.random = new Random();
        
        // Initialize liquid
        liquid = new LiquidSimulation();
        liquid.setLevel(0);
        updateLiquidContainer();
        
        // Initialize paints
        initPaints();
        
        beakerPath = new Path();
    }
    
    private void initPaints() {
        // Glass beaker paint
        beakerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        beakerPaint.setStyle(Paint.Style.FILL);
        beakerPaint.setColor(Color.argb(50, 255, 255, 255));
        
        // Stroke
        beakerStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        beakerStrokePaint.setStyle(Paint.Style.STROKE);
        beakerStrokePaint.setStrokeWidth(4f);
        beakerStrokePaint.setColor(Color.argb(120, 200, 200, 200));
        
        // Shine
        beakerShinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        beakerShinePaint.setStyle(Paint.Style.FILL);
        
        // Shadow
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(50, 0, 0, 0));
        
        // Glow
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        
        // Text
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        // Hint text
        hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        hintPaint.setColor(Color.parseColor("#2C3E50"));
    }
    
    private void updateLiquidContainer() {
        float beakerLeft = x - width / 2;
        float beakerTop = y - height / 2 + 20; // Account for rim
        float beakerRight = x + width / 2;
        float beakerBottom = y + height / 2;
        
        float padding = 8f;
        liquid.setContainer(
            beakerLeft + padding,
            beakerTop + padding,
            beakerRight - padding,
            beakerBottom - BEAKER_CORNER_RADIUS - padding,
            BEAKER_CORNER_RADIUS
        );
    }
    
    /**
     * Update beaker state
     */
    public void update(float deltaTime) {
        // Scale animation
        scale = EasingFunctions.lerp(scale, targetScale, deltaTime * 10f);
        
        // Bounce animation
        if (bounceOffset != 0) {
            bounceOffset *= (float) Math.pow(0.9f, deltaTime * 60f);
            if (Math.abs(bounceOffset) < 0.5f) {
                bounceOffset = 0;
            }
        }
        
        // Glow animation
        if (currentState == State.RESULT_READY) {
            glowIntensity = 0.5f + (float) Math.sin(System.currentTimeMillis() * 0.003) * 0.3f;
        } else if (currentState == State.READY_TO_MIX || currentState == State.HAS_TWO_COLORS) {
            glowIntensity = 0.3f + (float) Math.sin(System.currentTimeMillis() * 0.005) * 0.2f;
        } else {
            glowIntensity = EasingFunctions.lerp(glowIntensity, 0, deltaTime * 5f);
        }
        
        // Shake hint animation
        if (showShakeHint) {
            shakeHintAlpha = Math.min(1, shakeHintAlpha + deltaTime * 2f);
            shakeHintBounce += deltaTime * 8f;
        } else {
            shakeHintAlpha = Math.max(0, shakeHintAlpha - deltaTime * 3f);
        }
        
        // Mixing animation
        if (isMixing) {
            mixProgress += deltaTime * 0.5f;
            
            // Color transition - works for 2 or 3 colors
            if (colorCount >= 2 && mixProgress > 0.3f) {
                float colorProgress = (mixProgress - 0.3f) / 0.7f;
                colorProgress = EasingFunctions.clamp(colorProgress, 0, 1);
                
                int blendedColor;
                if (colorCount == 2) {
                    blendedColor = ColorMixer.blendColors(mixedColors[0], mixedColors[1], 0.5f);
                } else {
                    blendedColor = ColorMixer.blendThreeColors(mixedColors[0], mixedColors[1], mixedColors[2]);
                }
                
                int transitionColor = EasingFunctions.lerpColor(
                    blendedColor,
                    resultColor,
                    EasingFunctions.easeInOutCubic(colorProgress)
                );
                liquid.setColor(transitionColor);
            }
            
            if (mixProgress >= 1.0f) {
                completeMixing();
            }
        }
        
        // Update liquid
        updateLiquidContainer();
        liquid.update(deltaTime);
        
        // Update particles
        updateParticles(deltaTime);
        
        // Update sparkles
        updateSparkles(deltaTime);
    }
    
    private void updateParticles(float deltaTime) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(deltaTime);
            if (p.isDead()) {
                particles.remove(i);
            }
        }
    }
    
    private void updateSparkles(float deltaTime) {
        for (int i = sparkles.size() - 1; i >= 0; i--) {
            Sparkle s = sparkles.get(i);
            s.update(deltaTime);
            if (s.isDead()) {
                sparkles.remove(i);
            }
        }
    }
    
    /**
     * Update accelerometer for liquid physics
     */
    public void updateAccelerometer(float accelX, float accelY, float accelZ) {
        liquid.updateAccelerometer(accelX, accelY, accelZ);
    }
    
    /**
     * Draw the beaker
     */
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(x, y + bounceOffset);
        canvas.scale(scale, scale);
        
        // Draw glow effect
        if (glowIntensity > 0) {
            drawGlow(canvas);
        }
        
        // Draw shadow
        canvas.save();
        canvas.translate(6, 10);
        drawBeakerShape(canvas, shadowPaint, null);
        canvas.restore();
        
        // Draw glass beaker
        drawBeakerShape(canvas, beakerPaint, beakerStrokePaint);
        
        // Draw liquid (in local space)
        canvas.save();
        canvas.translate(-x, -y - bounceOffset);
        canvas.translate(x, y + bounceOffset);
        
        float padding = 8f;
        liquid.setContainer(
            -width / 2 + padding,
            -height / 2 + 20 + padding,
            width / 2 - padding,
            height / 2 - BEAKER_CORNER_RADIUS - padding,
            BEAKER_CORNER_RADIUS
        );
        liquid.draw(canvas);
        canvas.restore();
        
        // Draw glass shine
        drawGlassShine(canvas);
        
        // Draw handle
        drawHandle(canvas);
        
        // Draw measurement marks
        drawMeasurementMarks(canvas);
        
        canvas.restore();
        
        // Draw particles (in world space)
        drawParticles(canvas);
        
        // Draw sparkles
        drawSparkles(canvas);
        
        // Draw shake hint
        if (shakeHintAlpha > 0 && showShakeHint) {
            drawShakeHint(canvas);
        }
    }
    
    private void drawBeakerShape(Canvas canvas, Paint fillPaint, Paint strokePaint) {
        beakerPath.reset();
        
        float left = -width / 2;
        float top = -height / 2;
        float right = width / 2;
        float bottom = height / 2;
        
        // Beaker shape with tapered top
        float rimWidth = width * 0.8f;
        float rimLeft = -rimWidth / 2;
        float rimRight = rimWidth / 2;
        
        beakerPath.moveTo(rimLeft, top);
        beakerPath.lineTo(left, top + 40);
        beakerPath.lineTo(left, bottom - BEAKER_CORNER_RADIUS);
        beakerPath.quadTo(left, bottom, left + BEAKER_CORNER_RADIUS, bottom);
        beakerPath.lineTo(right - BEAKER_CORNER_RADIUS, bottom);
        beakerPath.quadTo(right, bottom, right, bottom - BEAKER_CORNER_RADIUS);
        beakerPath.lineTo(right, top + 40);
        beakerPath.lineTo(rimRight, top);
        beakerPath.close();
        
        // Draw rim at top
        if (fillPaint != null) {
            canvas.drawPath(beakerPath, fillPaint);
            
            // Rim
            RectF rimRect = new RectF(rimLeft - 5, top - 8, rimRight + 5, top + 8);
            canvas.drawRoundRect(rimRect, 5f, 5f, fillPaint);
        }
        
        if (strokePaint != null) {
            canvas.drawPath(beakerPath, strokePaint);
            
            // Rim stroke
            RectF rimRect = new RectF(rimLeft - 5, top - 8, rimRight + 5, top + 8);
            canvas.drawRoundRect(rimRect, 5f, 5f, strokePaint);
        }
    }
    
    private void drawGlassShine(Canvas canvas) {
        float shineWidth = width * 0.12f;
        float shineLeft = -width / 2 + width * 0.12f;
        float shineTop = -height / 2 + 50;
        float shineBottom = height / 2 - 30;
        
        LinearGradient shineGradient = new LinearGradient(
            shineLeft, shineTop,
            shineLeft + shineWidth, shineTop,
            new int[]{
                Color.argb(0, 255, 255, 255),
                Color.argb(100, 255, 255, 255),
                Color.argb(0, 255, 255, 255)
            },
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        beakerShinePaint.setShader(shineGradient);
        
        RectF shineRect = new RectF(shineLeft, shineTop, shineLeft + shineWidth, shineBottom);
        canvas.drawRoundRect(shineRect, shineWidth / 2, shineWidth / 2, beakerShinePaint);
    }
    
    private void drawHandle(Canvas canvas) {
        Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setStyle(Paint.Style.STROKE);
        handlePaint.setStrokeWidth(8f);
        handlePaint.setColor(Color.argb(100, 200, 200, 200));
        handlePaint.setStrokeCap(Paint.Cap.ROUND);
        
        Path handlePath = new Path();
        float handleX = width / 2 + 5;
        float handleTop = -height / 4;
        float handleBottom = height / 4;
        
        handlePath.moveTo(handleX, handleTop);
        handlePath.quadTo(handleX + HANDLE_WIDTH, (handleTop + handleBottom) / 2, handleX, handleBottom);
        
        canvas.drawPath(handlePath, handlePaint);
    }
    
    private void drawMeasurementMarks(Canvas canvas) {
        Paint markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markPaint.setColor(Color.argb(60, 100, 100, 100));
        markPaint.setStrokeWidth(2f);
        
        float left = -width / 2 + 15;
        float bottom = height / 2 - 30;
        float markHeight = height - 100;
        
        for (int i = 1; i <= 3; i++) {
            float markY = bottom - (markHeight * i / 4);
            float markWidth = (i == 2) ? 20f : 12f;
            canvas.drawLine(left, markY, left + markWidth, markY, markPaint);
        }
    }
    
    private void drawGlow(Canvas canvas) {
        int glowColor = (currentState == State.RESULT_READY) ? resultColor : liquid.getColor();
        int alpha = (int) (glowIntensity * 100);
        
        RadialGradient glowGradient = new RadialGradient(
            0, 0,
            Math.max(width, height),
            new int[]{
                Color.argb(alpha, Color.red(glowColor), Color.green(glowColor), Color.blue(glowColor)),
                Color.argb(0, Color.red(glowColor), Color.green(glowColor), Color.blue(glowColor))
            },
            new float[]{0f, 1f},
            Shader.TileMode.CLAMP
        );
        glowPaint.setShader(glowGradient);
        canvas.drawCircle(0, 0, Math.max(width, height), glowPaint);
    }
    
    private void drawParticles(Canvas canvas) {
        Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        for (Particle p : particles) {
            particlePaint.setColor(p.color);
            particlePaint.setAlpha((int) (p.getAlpha() * 255));
            canvas.drawCircle(p.x, p.y, p.size * p.getAlpha(), particlePaint);
        }
    }
    
    private void drawSparkles(Canvas canvas) {
        Paint sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        for (Sparkle s : sparkles) {
            canvas.save();
            canvas.translate(s.x, s.y);
            canvas.rotate(s.rotation);
            canvas.scale(s.getScale(), s.getScale());
            
            sparklePaint.setColor(s.color);
            sparklePaint.setAlpha((int) (s.getAlpha() * 255));
            
            // Draw star shape
            Path starPath = new Path();
            float outer = s.size;
            float inner = s.size * 0.4f;
            for (int i = 0; i < 8; i++) {
                float angle = (float) (i * Math.PI / 4);
                float radius = (i % 2 == 0) ? outer : inner;
                float px = (float) Math.cos(angle) * radius;
                float py = (float) Math.sin(angle) * radius;
                if (i == 0) {
                    starPath.moveTo(px, py);
                } else {
                    starPath.lineTo(px, py);
                }
            }
            starPath.close();
            canvas.drawPath(starPath, sparklePaint);
            
            canvas.restore();
        }
    }
    
    private void drawShakeHint(Canvas canvas) {
        float hintY = y - height / 2 - 60 + (float) Math.sin(shakeHintBounce) * 10;
        
        hintPaint.setAlpha((int) (shakeHintAlpha * 255));
        hintPaint.setTextSize(36);
        
        // Draw background pill
        String hintText = "🫨 Shake me!";
        float textWidth = hintPaint.measureText(hintText);
        
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setAlpha((int) (shakeHintAlpha * 230));
        
        RectF bgRect = new RectF(
            x - textWidth / 2 - 20,
            hintY - 30,
            x + textWidth / 2 + 20,
            hintY + 15
        );
        canvas.drawRoundRect(bgRect, 25, 25, bgPaint);
        
        canvas.drawText(hintText, x, hintY, hintPaint);
    }
    
    // ==================== Color Management ====================
    
    /**
     * Add color from a test tube
     */
    public void addColor(int color) {
        if (colorCount >= MAX_COLORS) return;
        
        mixedColors[colorCount] = color;
        colorCount++;
        
        // Update liquid based on color count
        if (colorCount == 1) {
            liquid.setColor(color);
            liquid.animateLevelTo(0.25f);
            currentState = State.HAS_ONE_COLOR;
        } else if (colorCount == 2) {
            // Blend first two colors
            int blendedColor = ColorMixer.blendColors(mixedColors[0], mixedColors[1], 0.5f);
            liquid.setColor(blendedColor);
            liquid.animateLevelTo(0.5f);
            currentState = State.HAS_TWO_COLORS;
            // Show hint that they can add one more or shake now
            showShakeHint = true;
        } else if (colorCount == 3) {
            // Blend all three colors
            int blendedColor = ColorMixer.blendThreeColors(mixedColors[0], mixedColors[1], mixedColors[2]);
            liquid.setColor(blendedColor);
            liquid.animateLevelTo(0.7f);
            currentState = State.READY_TO_MIX;
            showShakeHint = true;
        }
        
        // Add splash particles
        addSplashEffect(color);
        
        // Bounce effect
        bounceOffset = -20;
        targetScale = 1.05f;
    }
    
    /**
     * Check if ready to mix (2 or 3 colors added)
     */
    public boolean canMix() {
        return colorCount >= 2 && (currentState == State.HAS_TWO_COLORS || currentState == State.READY_TO_MIX);
    }
    
    /**
     * Check if can add more colors
     */
    public boolean canAddMoreColors() {
        return colorCount < MAX_COLORS;
    }
    
    /**
     * Add splash particle effect
     */
    private void addSplashEffect(int color) {
        float splashY = y - height / 4;
        
        for (int i = 0; i < 15; i++) {
            float angle = (float) (Math.random() * Math.PI * 2);
            float speed = 100 + (float) Math.random() * 150;
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed - 100;
            
            particles.add(new Particle(
                x + (float) (Math.random() - 0.5) * 30,
                splashY,
                vx, vy,
                4 + (float) Math.random() * 6,
                0.8f + (float) Math.random() * 0.4f,
                color
            ));
        }
    }
    
    /**
     * Start mixing process (called when shake detected)
     */
    public void startMixing() {
        // Can mix with 2 or 3 colors
        if (!canMix()) return;
        
        currentState = State.MIXING;
        isMixing = true;
        mixProgress = 0;
        showShakeHint = false;
        
        // Calculate result based on number of colors
        ColorMixer.MixResult result;
        if (colorCount == 2) {
            result = ColorMixer.mixColors(mixedColors[0], mixedColors[1]);
        } else {
            result = ColorMixer.mixThreeColors(mixedColors[0], mixedColors[1], mixedColors[2]);
        }
        
        resultColor = result.resultColor;
        resultColorName = result.colorName;
        resultSentence = result.sentence;
        
        // Apply shake to liquid
        liquid.shake(1.0f);
    }
    
    /**
     * Apply shake effect
     */
    public void applyShake(float intensity) {
        liquid.shake(intensity);
        
        // Can start mixing with 2 or 3 colors
        if (canMix() && !isMixing && intensity > 0.5f) {
            startMixing();
        } else if (currentState == State.MIXING) {
            // Speed up mixing with continued shaking
            mixProgress += intensity * 0.05f;
        }
    }
    
    /**
     * Complete mixing and show result
     */
    private void completeMixing() {
        isMixing = false;
        currentState = State.RESULT_READY;
        
        // Set final color
        liquid.setColor(resultColor);
        
        // Add sparkle effects
        addSuccessSparkles();
        
        // Big bounce
        bounceOffset = -30;
    }
    
    /**
     * Add success sparkle effects
     */
    private void addSuccessSparkles() {
        int[] sparkleColors = {
            Color.WHITE,
            Color.parseColor("#F1C40F"), // Gold
            Color.parseColor("#E74C3C"), // Red
            Color.parseColor("#3498DB"), // Blue
            resultColor
        };
        
        for (int i = 0; i < 20; i++) {
            float angle = (float) (Math.random() * Math.PI * 2);
            float distance = 50 + (float) Math.random() * 100;
            float sx = x + (float) Math.cos(angle) * distance;
            float sy = y + (float) Math.sin(angle) * distance;
            float size = 10 + (float) Math.random() * 15;
            int color = sparkleColors[random.nextInt(sparkleColors.length)];
            
            sparkles.add(new Sparkle(sx, sy, size, color));
        }
    }
    
    /**
     * Reset beaker to empty state
     */
    public void reset() {
        colorCount = 0;
        currentState = State.EMPTY;
        mixProgress = 0;
        isMixing = false;
        showShakeHint = false;
        liquid.setLevel(0);
        particles.clear();
        sparkles.clear();
        resultColor = Color.TRANSPARENT;
        resultColorName = null;
        resultSentence = null;
    }
    
    /**
     * Check if point is inside beaker
     */
    public boolean contains(float touchX, float touchY) {
        float halfWidth = width / 2 * scale;
        float halfHeight = height / 2 * scale;
        return touchX >= x - halfWidth && touchX <= x + halfWidth &&
               touchY >= y - halfHeight && touchY <= y + halfHeight;
    }
    
    /**
     * Get pour target position (top center of beaker)
     */
    public float[] getPourTargetPosition() {
        return new float[]{x, y - height / 3};
    }
    
    // ==================== Getters ====================
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    
    public State getCurrentState() { return currentState; }
    public boolean isMixing() { return isMixing; }
    public boolean isResultReady() { return currentState == State.RESULT_READY; }
    
    public int getResultColor() { return resultColor; }
    public String getResultColorName() { return resultColorName; }
    public String getResultSentence() { return resultSentence; }
    
    public int getColorCount() { return colorCount; }
    public LiquidSimulation getLiquid() { return liquid; }
    
    public void setShowShakeHint(boolean show) { this.showShakeHint = show; }
}
