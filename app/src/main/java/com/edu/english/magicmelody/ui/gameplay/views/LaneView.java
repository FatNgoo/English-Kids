package com.edu.english.magicmelody.ui.gameplay.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

import androidx.annotation.Nullable;

/**
 * 🎹 Lane View
 * 
 * A single lane for falling notes
 * - Visual lane track
 * - Hit zone at bottom
 * - Hit feedback effects
 */
public class LaneView extends View {
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 PAINT & DRAWING
    // ═══════════════════════════════════════════════════════════════
    
    private Paint lanePaint;
    private Paint hitZonePaint;
    private Paint hitZoneGlowPaint;
    private Paint hitFeedbackPaint;
    private Paint borderPaint;
    
    private RectF laneRect;
    private RectF hitZoneRect;
    private Path hitZonePath;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 LANE STATE
    // ═══════════════════════════════════════════════════════════════
    
    private int laneIndex = 0;
    private int laneColor = 0xFF4CAF50;
    private float hitZoneHeightRatio = 0.12f;  // 12% of height
    private boolean isPressed = false;
    private boolean showHitFeedback = false;
    private int hitFeedbackColor = Color.GREEN;
    private float hitFeedbackAlpha = 0f;
    
    private static final int[] LANE_COLORS = {
        0xFFFF6B6B,  // Red
        0xFF4ECDC4,  // Teal  
        0xFFFFE66D,  // Yellow
        0xFF95E1D3,  // Mint
        0xFFDDA0DD   // Plum
    };
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public LaneView(Context context) {
        super(context);
        init();
    }
    
    public LaneView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public LaneView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Lane background paint
        lanePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lanePaint.setStyle(Paint.Style.FILL);
        
        // Hit zone paint
        hitZonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitZonePaint.setStyle(Paint.Style.FILL);
        
        // Hit zone glow
        hitZoneGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitZoneGlowPaint.setStyle(Paint.Style.FILL);
        hitZoneGlowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(20, 
            android.graphics.BlurMaskFilter.Blur.OUTER));
        
        // Hit feedback
        hitFeedbackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitFeedbackPaint.setStyle(Paint.Style.FILL);
        
        // Border
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        borderPaint.setColor(0x40FFFFFF);
        
        laneRect = new RectF();
        hitZoneRect = new RectF();
        hitZonePath = new Path();
        
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 DRAWING
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Update lane rectangle
        laneRect.set(0, 0, w, h);
        
        // Update hit zone rectangle
        float hitZoneTop = h * (1 - hitZoneHeightRatio);
        hitZoneRect.set(0, hitZoneTop, w, h);
        
        // Update shaders
        updateShaders();
    }
    
    private void updateShaders() {
        // Lane gradient (top to bottom, fading)
        lanePaint.setShader(new LinearGradient(
            0, 0, 0, getHeight(),
            new int[]{
                Color.TRANSPARENT,
                adjustAlpha(laneColor, 0.1f),
                adjustAlpha(laneColor, 0.2f)
            },
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        ));
        
        // Hit zone gradient
        hitZonePaint.setShader(new RadialGradient(
            getWidth() / 2f, hitZoneRect.centerY(),
            getWidth() / 2f,
            adjustAlpha(laneColor, 0.6f),
            adjustAlpha(laneColor, 0.2f),
            Shader.TileMode.CLAMP
        ));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw lane background
        canvas.drawRect(laneRect, lanePaint);
        
        // Draw lane borders
        canvas.drawLine(0, 0, 0, getHeight(), borderPaint);
        canvas.drawLine(getWidth(), 0, getWidth(), getHeight(), borderPaint);
        
        // Draw hit zone
        drawHitZone(canvas);
        
        // Draw hit feedback if active
        if (showHitFeedback && hitFeedbackAlpha > 0) {
            drawHitFeedback(canvas);
        }
        
        // Draw pressed state
        if (isPressed) {
            drawPressedState(canvas);
        }
    }
    
    private void drawHitZone(Canvas canvas) {
        // Hit zone glow
        hitZoneGlowPaint.setColor(adjustAlpha(laneColor, 0.5f));
        canvas.drawRoundRect(hitZoneRect, 10, 10, hitZoneGlowPaint);
        
        // Hit zone fill
        canvas.drawRoundRect(hitZoneRect, 10, 10, hitZonePaint);
        
        // Hit zone border
        Paint hitBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitBorderPaint.setStyle(Paint.Style.STROKE);
        hitBorderPaint.setStrokeWidth(3);
        hitBorderPaint.setColor(laneColor);
        canvas.drawRoundRect(hitZoneRect, 10, 10, hitBorderPaint);
    }
    
    private void drawHitFeedback(Canvas canvas) {
        hitFeedbackPaint.setColor(adjustAlpha(hitFeedbackColor, hitFeedbackAlpha));
        
        // Draw expanding circle from hit zone center
        float radius = getWidth() * hitFeedbackAlpha;
        canvas.drawCircle(
            getWidth() / 2f,
            hitZoneRect.centerY(),
            radius,
            hitFeedbackPaint
        );
    }
    
    private void drawPressedState(Canvas canvas) {
        Paint pressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pressedPaint.setStyle(Paint.Style.FILL);
        pressedPaint.setColor(adjustAlpha(laneColor, 0.3f));
        
        canvas.drawRect(laneRect, pressedPaint);
    }
    
    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public void setLaneIndex(int index) {
        this.laneIndex = index;
        this.laneColor = LANE_COLORS[index % LANE_COLORS.length];
        updateShaders();
        invalidate();
    }
    
    public void setLaneColor(int color) {
        this.laneColor = color;
        updateShaders();
        invalidate();
    }
    
    public void setPressed(boolean pressed) {
        this.isPressed = pressed;
        invalidate();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public int getLaneIndex() {
        return laneIndex;
    }
    
    public int getLaneColor() {
        return laneColor;
    }
    
    public RectF getHitZoneRect() {
        return hitZoneRect;
    }
    
    public float getHitZoneY() {
        return hitZoneRect.centerY();
    }
    
    public float getHitZoneTop() {
        return hitZoneRect.top;
    }
    
    public float getHitZoneBottom() {
        return hitZoneRect.bottom;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎬 ANIMATIONS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Show hit feedback animation
     */
    public void showHitFeedback(int color) {
        this.hitFeedbackColor = color;
        this.showHitFeedback = true;
        
        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "hitFeedbackAlphaValue", 0f, 0.8f, 0f);
        alpha.setDuration(300);
        alpha.addUpdateListener(animation -> invalidate());
        alpha.start();
    }
    
    /**
     * Show perfect hit feedback (more intense)
     */
    public void showPerfectHitFeedback() {
        showHitFeedback(0xFFFFD700); // Gold
        
        AnimatorSet animSet = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.05f, 1f);
        
        animSet.playTogether(scaleX, scaleY);
        animSet.setDuration(200);
        animSet.start();
    }
    
    /**
     * Show miss feedback
     */
    public void showMissFeedback() {
        showHitFeedback(0xFFFF0000); // Red
    }
    
    /**
     * Setter for hit feedback alpha (for animation)
     */
    public void setHitFeedbackAlphaValue(float alpha) {
        this.hitFeedbackAlpha = alpha;
        invalidate();
    }
    
    /**
     * Highlight lane (for tutorial or hints)
     */
    public void highlight() {
        ObjectAnimator highlight = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.5f, 1f);
        highlight.setDuration(500);
        highlight.setRepeatCount(2);
        highlight.start();
    }
}
