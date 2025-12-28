package com.edu.english.magicmelody.ui.gameplay.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 🔥 Combo View
 * 
 * Display current combo with fire effects
 * - Combo counter with animations
 * - Fire/glow intensity based on combo
 * - Combo break animation
 */
public class ComboView extends View {
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 PAINT
    // ═══════════════════════════════════════════════════════════════
    
    private Paint comboPaint;
    private Paint labelPaint;
    private Paint firePaint;
    private Paint glowPaint;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 COMBO STATE
    // ═══════════════════════════════════════════════════════════════
    
    private int currentCombo = 0;
    private int maxCombo = 0;
    private float fireIntensity = 0f;
    private float shakeOffset = 0f;
    
    private boolean showFire = true;
    private boolean isShaking = false;
    
    // Fire particles
    private float[] fireParticleX = new float[10];
    private float[] fireParticleY = new float[10];
    private float[] fireParticleSize = new float[10];
    
    private ValueAnimator fireAnimator;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public ComboView(Context context) {
        super(context);
        init();
    }
    
    public ComboView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ComboView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Combo text paint
        comboPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        comboPaint.setColor(Color.WHITE);
        comboPaint.setTextSize(80);
        comboPaint.setTextAlign(Paint.Align.CENTER);
        comboPaint.setFakeBoldText(true);
        comboPaint.setShadowLayer(8, 0, 0, 0xFFFF6600);
        
        // Label paint
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(0xFFFFAA00);
        labelPaint.setTextSize(24);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);
        
        // Fire paint
        firePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        firePaint.setStyle(Paint.Style.FILL);
        
        // Glow paint
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(30, 
            android.graphics.BlurMaskFilter.Blur.NORMAL));
        
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        
        // Initialize fire particles
        for (int i = 0; i < fireParticleX.length; i++) {
            resetFireParticle(i);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 DRAWING
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (currentCombo == 0) {
            return; // Don't draw if no combo
        }
        
        float centerX = getWidth() / 2f + shakeOffset;
        float centerY = getHeight() / 2f;
        
        // Draw fire effect if combo is high enough
        if (showFire && currentCombo >= 5) {
            drawFireEffect(canvas, centerX, centerY);
        }
        
        // Draw glow
        if (currentCombo >= 3) {
            drawGlow(canvas, centerX, centerY);
        }
        
        // Draw combo label
        canvas.drawText("COMBO", centerX, centerY - 40, labelPaint);
        
        // Draw combo number
        String comboText = String.valueOf(currentCombo);
        canvas.drawText(comboText, centerX, centerY + 40, comboPaint);
        
        // Draw multiplier if high combo
        if (currentCombo >= 10) {
            String multiplier = "x" + getComboMultiplier();
            Paint multiplierPaint = new Paint(labelPaint);
            multiplierPaint.setTextSize(32);
            canvas.drawText(multiplier, centerX, centerY + 80, multiplierPaint);
        }
    }
    
    private void drawFireEffect(Canvas canvas, float cx, float cy) {
        // Draw fire particles
        for (int i = 0; i < fireParticleX.length; i++) {
            float px = cx + fireParticleX[i];
            float py = cy + fireParticleY[i];
            float size = fireParticleSize[i];
            
            // Color based on position (yellow at bottom, red at top)
            float ratio = fireParticleY[i] / -50f;
            int color = blendColors(0xFFFF0000, 0xFFFFFF00, ratio);
            
            firePaint.setColor(adjustAlpha(color, 0.7f * fireIntensity));
            canvas.drawCircle(px, py, size, firePaint);
        }
    }
    
    private void drawGlow(Canvas canvas, float cx, float cy) {
        float glowSize = 80 + (currentCombo * 2);
        
        glowPaint.setShader(new RadialGradient(
            cx, cy, glowSize,
            new int[]{
                adjustAlpha(0xFFFF6600, 0.5f * fireIntensity),
                adjustAlpha(0xFFFF0000, 0.3f * fireIntensity),
                Color.TRANSPARENT
            },
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        ));
        
        canvas.drawCircle(cx, cy, glowSize, glowPaint);
    }
    
    private int blendColors(int color1, int color2, float ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        
        int r = (int)(Color.red(color1) * (1 - ratio) + Color.red(color2) * ratio);
        int g = (int)(Color.green(color1) * (1 - ratio) + Color.green(color2) * ratio);
        int b = (int)(Color.blue(color1) * (1 - ratio) + Color.blue(color2) * ratio);
        
        return Color.rgb(r, g, b);
    }
    
    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(255 * factor);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
    
    private void resetFireParticle(int index) {
        fireParticleX[index] = (float)(Math.random() * 60 - 30);
        fireParticleY[index] = (float)(Math.random() * -50);
        fireParticleSize[index] = (float)(5 + Math.random() * 10);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SETTERS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Set combo count
     */
    public void setCombo(int combo) {
        int previousCombo = this.currentCombo;
        this.currentCombo = combo;
        
        if (combo > maxCombo) {
            maxCombo = combo;
        }
        
        // Update fire intensity
        updateFireIntensity();
        
        // Play animation if combo increased
        if (combo > previousCombo) {
            playComboIncreaseAnimation();
        }
        
        invalidate();
    }
    
    /**
     * Increment combo
     */
    public void incrementCombo() {
        setCombo(currentCombo + 1);
    }
    
    /**
     * Break combo (reset to 0)
     */
    public void breakCombo() {
        if (currentCombo > 0) {
            playComboBreakAnimation();
        }
        currentCombo = 0;
        fireIntensity = 0f;
        invalidate();
    }
    
    /**
     * Set text size
     */
    public void setComboTextSize(float size) {
        comboPaint.setTextSize(size);
        invalidate();
    }
    
    /**
     * Enable/disable fire effect
     */
    public void setShowFire(boolean show) {
        this.showFire = show;
        if (show && currentCombo >= 5) {
            startFireAnimation();
        } else {
            stopFireAnimation();
        }
        invalidate();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public int getCurrentCombo() {
        return currentCombo;
    }
    
    public int getMaxCombo() {
        return maxCombo;
    }
    
    public float getComboMultiplier() {
        if (currentCombo >= 50) return 3.0f;
        if (currentCombo >= 30) return 2.5f;
        if (currentCombo >= 20) return 2.0f;
        if (currentCombo >= 10) return 1.5f;
        if (currentCombo >= 5) return 1.2f;
        return 1.0f;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎬 ANIMATIONS
    // ═══════════════════════════════════════════════════════════════
    
    private void updateFireIntensity() {
        float targetIntensity = Math.min(1f, currentCombo / 20f);
        
        ObjectAnimator.ofFloat(this, "fireIntensityValue", fireIntensity, targetIntensity)
            .setDuration(200)
            .start();
        
        if (currentCombo >= 5 && showFire) {
            startFireAnimation();
        }
    }
    
    /**
     * Setter for fire intensity (for animation)
     */
    public void setFireIntensityValue(float intensity) {
        this.fireIntensity = intensity;
        invalidate();
    }
    
    /**
     * Play animation when combo increases
     */
    private void playComboIncreaseAnimation() {
        AnimatorSet popSet = new AnimatorSet();
        
        // Scale factor based on combo milestone
        float scaleFactor = 1.2f;
        if (currentCombo % 10 == 0) {
            scaleFactor = 1.5f; // Bigger pop for milestones
        } else if (currentCombo % 5 == 0) {
            scaleFactor = 1.3f;
        }
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, scaleFactor, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, scaleFactor, 1f);
        
        popSet.playTogether(scaleX, scaleY);
        popSet.setDuration(150);
        popSet.setInterpolator(new OvershootInterpolator());
        popSet.start();
    }
    
    /**
     * Play animation when combo breaks
     */
    private void playComboBreakAnimation() {
        stopFireAnimation();
        
        // Shake animation
        isShaking = true;
        
        ObjectAnimator shake = ObjectAnimator.ofFloat(this, "shakeOffsetValue", 
            0f, -10f, 10f, -8f, 8f, -5f, 5f, 0f);
        shake.setDuration(300);
        shake.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {}
            
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                isShaking = false;
            }
            
            @Override
            public void onAnimationCancel(@NonNull Animator animation) {}
            
            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });
        shake.start();
        
        // Fade out
        ObjectAnimator fade = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        fade.setDuration(300);
        fade.start();
    }
    
    /**
     * Setter for shake offset (for animation)
     */
    public void setShakeOffsetValue(float offset) {
        this.shakeOffset = offset;
        invalidate();
    }
    
    /**
     * Start fire particle animation
     */
    private void startFireAnimation() {
        if (fireAnimator != null && fireAnimator.isRunning()) {
            return;
        }
        
        fireAnimator = ValueAnimator.ofFloat(0f, 1f);
        fireAnimator.setDuration(100);
        fireAnimator.setRepeatCount(ValueAnimator.INFINITE);
        fireAnimator.addUpdateListener(animation -> {
            // Update fire particles
            for (int i = 0; i < fireParticleY.length; i++) {
                fireParticleY[i] -= 3;
                fireParticleSize[i] -= 0.2f;
                
                if (fireParticleY[i] < -60 || fireParticleSize[i] <= 0) {
                    resetFireParticle(i);
                }
            }
            invalidate();
        });
        fireAnimator.start();
    }
    
    /**
     * Stop fire animation
     */
    private void stopFireAnimation() {
        if (fireAnimator != null) {
            fireAnimator.cancel();
            fireAnimator = null;
        }
    }
    
    /**
     * Reset combo view
     */
    public void reset() {
        currentCombo = 0;
        maxCombo = 0;
        fireIntensity = 0f;
        stopFireAnimation();
        setAlpha(1f);
        invalidate();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopFireAnimation();
    }
}
