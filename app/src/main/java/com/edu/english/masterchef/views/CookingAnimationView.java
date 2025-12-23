package com.edu.english.masterchef.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom View for cooking animations (sizzle effect, boiling, steam, etc.)
 */
public class CookingAnimationView extends View {

    public enum AnimationType {
        SIZZLE,      // Pan sizzle effect
        BOIL,        // Pot boiling effect
        STEAM,       // Steam rising
        CUT,         // Cutting effect
        STIR,        // Stirring effect
        POUR         // Pouring effect
    }
    
    private AnimationType currentAnimation = null;
    private boolean isAnimating = false;
    
    private Paint sizzlePaint;
    private Paint bubblePaint;
    private Paint steamPaint;
    private Paint cutPaint;
    
    private ValueAnimator animator;
    private float animProgress = 0f;
    
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    
    public CookingAnimationView(Context context) {
        super(context);
        init();
    }
    
    public CookingAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CookingAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Sizzle effect paint (yellow-orange sparks)
        sizzlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sizzlePaint.setColor(0xFFFFAB00);
        sizzlePaint.setStyle(Paint.Style.FILL);
        
        // Bubble paint
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setColor(0x88FFFFFF);
        bubblePaint.setStyle(Paint.Style.FILL);
        
        // Steam paint
        steamPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        steamPaint.setColor(0x44FFFFFF);
        steamPaint.setStyle(Paint.Style.FILL);
        
        // Cut effect paint
        cutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cutPaint.setColor(0xFFE0E0E0);
        cutPaint.setStrokeWidth(4f);
        cutPaint.setStyle(Paint.Style.STROKE);
        
        // Set up animator
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            animProgress = (float) animation.getAnimatedValue();
            updateParticles();
            invalidate();
        });
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!isAnimating || currentAnimation == null) return;
        
        switch (currentAnimation) {
            case SIZZLE:
                drawSizzle(canvas);
                break;
            case BOIL:
                drawBoil(canvas);
                break;
            case STEAM:
                drawSteam(canvas);
                break;
            case CUT:
                drawCut(canvas);
                break;
            case STIR:
                drawStir(canvas);
                break;
            case POUR:
                drawPour(canvas);
                break;
        }
    }
    
    private void drawSizzle(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        
        for (Particle p : particles) {
            sizzlePaint.setAlpha((int) (255 * p.alpha));
            canvas.drawCircle(p.x, p.y, p.size, sizzlePaint);
        }
    }
    
    private void drawBoil(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        
        for (Particle p : particles) {
            bubblePaint.setAlpha((int) (200 * p.alpha));
            canvas.drawCircle(p.x, p.y, p.size, bubblePaint);
        }
    }
    
    private void drawSteam(Canvas canvas) {
        for (Particle p : particles) {
            steamPaint.setAlpha((int) (150 * p.alpha));
            
            // Wavy steam
            float waveX = p.x + (float) Math.sin(p.y * 0.1f) * 10f;
            canvas.drawCircle(waveX, p.y, p.size, steamPaint);
        }
    }
    
    private void drawCut(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        
        // Flash effect
        cutPaint.setAlpha((int) (255 * (1f - animProgress)));
        float lineX = width * animProgress;
        canvas.drawLine(lineX, 0, lineX, height, cutPaint);
        
        // Sparks
        for (Particle p : particles) {
            sizzlePaint.setAlpha((int) (255 * p.alpha));
            canvas.drawCircle(p.x, p.y, p.size, sizzlePaint);
        }
    }
    
    private void drawStir(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        
        // Rotating arc
        float radius = Math.min(width, height) * 0.3f;
        float startAngle = animProgress * 360f;
        
        Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setColor(0x88795548);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(8f);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        
        RectF arcRect = new RectF(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius
        );
        canvas.drawArc(arcRect, startAngle, 90f, false, arcPaint);
    }
    
    private void drawPour(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        
        // Stream of liquid
        Paint liquidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        liquidPaint.setShader(new LinearGradient(
            width / 2f, 0,
            width / 2f, height,
            0xFF81D4FA, 0x4481D4FA,
            Shader.TileMode.CLAMP
        ));
        
        Path streamPath = new Path();
        float streamWidth = 20f;
        float streamX = width / 2f;
        float streamBottom = height * Math.min(animProgress * 1.5f, 1f);
        
        streamPath.moveTo(streamX - streamWidth / 2, 0);
        streamPath.lineTo(streamX + streamWidth / 2, 0);
        streamPath.lineTo(streamX + streamWidth / 2 + streamBottom * 0.05f, streamBottom);
        streamPath.lineTo(streamX - streamWidth / 2 - streamBottom * 0.05f, streamBottom);
        streamPath.close();
        
        canvas.drawPath(streamPath, liquidPaint);
    }
    
    private void updateParticles() {
        int width = getWidth();
        int height = getHeight();
        
        // Update existing particles
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            
            // Remove dead particles
            if (p.alpha <= 0) {
                particles.remove(i);
            }
        }
        
        // Spawn new particles based on animation type
        if (currentAnimation == AnimationType.SIZZLE) {
            if (random.nextFloat() < 0.3f) {
                float x = width * 0.2f + random.nextFloat() * width * 0.6f;
                float y = height * 0.8f;
                particles.add(new Particle(x, y, 3f + random.nextFloat() * 5f, 0f, -2f - random.nextFloat() * 3f));
            }
        } else if (currentAnimation == AnimationType.BOIL) {
            if (random.nextFloat() < 0.4f) {
                float x = width * 0.2f + random.nextFloat() * width * 0.6f;
                float y = height * 0.9f;
                particles.add(new Particle(x, y, 8f + random.nextFloat() * 12f, 0f, -1f - random.nextFloat() * 2f));
            }
        } else if (currentAnimation == AnimationType.STEAM) {
            if (random.nextFloat() < 0.2f) {
                float x = width * 0.3f + random.nextFloat() * width * 0.4f;
                float y = height * 0.7f;
                Particle p = new Particle(x, y, 15f + random.nextFloat() * 15f, 0f, -0.5f - random.nextFloat());
                p.fadeRate = 0.005f;
                particles.add(p);
            }
        } else if (currentAnimation == AnimationType.CUT) {
            if (animProgress < 0.5f && random.nextFloat() < 0.5f) {
                float x = width * animProgress;
                float y = height * 0.4f + random.nextFloat() * height * 0.2f;
                particles.add(new Particle(x, y, 2f + random.nextFloat() * 3f, 
                    (random.nextFloat() - 0.5f) * 4f, (random.nextFloat() - 0.5f) * 4f));
            }
        }
    }
    
    // Public API
    
    public void startAnimation(AnimationType type) {
        currentAnimation = type;
        particles.clear();
        isAnimating = true;
        
        if (!animator.isRunning()) {
            animator.start();
        }
    }
    
    public void stopAnimation() {
        isAnimating = false;
        animator.cancel();
        particles.clear();
        invalidate();
    }
    
    public boolean isAnimating() {
        return isAnimating;
    }
    
    public AnimationType getCurrentAnimation() {
        return currentAnimation;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animator.cancel();
    }
    
    // Particle class for effects
    private static class Particle {
        float x, y;
        float size;
        float vx, vy;
        float alpha = 1f;
        float fadeRate = 0.02f;
        
        Particle(float x, float y, float size, float vx, float vy) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.vx = vx;
            this.vy = vy;
        }
        
        void update() {
            x += vx;
            y += vy;
            alpha -= fadeRate;
            size *= 0.98f;
        }
    }
}
