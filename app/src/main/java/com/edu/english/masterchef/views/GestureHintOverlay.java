package com.edu.english.masterchef.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.edu.english.masterchef.data.ActionType;

/**
 * Overlay view that shows animated gesture hints to teach players
 * how to perform cooking actions (stir, shake, tap, pour, etc.)
 */
public class GestureHintOverlay extends View {

    private ActionType currentAction;
    private Paint handPaint;
    private Paint trailPaint;
    private Paint textPaint;
    private Paint arrowPaint;
    
    // Animation state
    private float animationProgress = 0f;
    private ValueAnimator animator;
    private boolean isShowing = false;
    
    // Hand position for animation
    private float handX, handY;
    
    // Hint text
    private String hintText = "";
    
    public GestureHintOverlay(Context context) {
        super(context);
        init();
    }
    
    public GestureHintOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public GestureHintOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Hand icon paint (finger/pointer)
        handPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handPaint.setColor(0xFFFFFFFF);
        handPaint.setStyle(Paint.Style.FILL);
        handPaint.setShadowLayer(8f, 2f, 2f, 0x80000000);
        setLayerType(LAYER_TYPE_SOFTWARE, handPaint);
        
        // Trail paint for gesture path
        trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trailPaint.setColor(0x80FFEB3B);
        trailPaint.setStyle(Paint.Style.STROKE);
        trailPaint.setStrokeWidth(8f);
        trailPaint.setStrokeCap(Paint.Cap.ROUND);
        trailPaint.setStrokeJoin(Paint.Join.ROUND);
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(4f, 2f, 2f, 0x80000000);
        
        // Arrow paint
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(0xFFFFEB3B);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(6f);
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);
    }
    
    /**
     * Show hint animation for a specific action type
     */
    public void showHint(ActionType action) {
        this.currentAction = action;
        this.isShowing = true;
        
        // Set hint text based on action
        switch (action) {
            case TAP_TO_CUT:
            case TAP_TO_POUND:
            case TAP_TO_CRACK:
            case TAP_TO_DRIZZLE:
                hintText = "👆 TAP";
                break;
            case SWIPE_TO_STIR:
                hintText = "🌀 STIR";
                break;
            case SHAKE_PAN:
            case SHAKE_BASKET:
            case SEASON_SHAKE:
                hintText = "↔️ SHAKE";
                break;
            case POUR:
                hintText = "⏱️ HOLD";
                break;
            default:
                hintText = "👆 FOLLOW";
        }
        
        setVisibility(View.VISIBLE);
        startAnimation();
    }
    
    public void hideHint() {
        isShowing = false;
        stopAnimation();
        setVisibility(View.GONE);
    }
    
    private void startAnimation() {
        stopAnimation();
        
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(getAnimationDuration());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        
        if (currentAction == ActionType.SWIPE_TO_STIR) {
            animator.setInterpolator(new LinearInterpolator());
        } else {
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        
        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            updateHandPosition();
            invalidate();
        });
        
        animator.start();
    }
    
    private void stopAnimation() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
    
    private long getAnimationDuration() {
        if (currentAction == null) return 1500;
        
        switch (currentAction) {
            case TAP_TO_CUT:
            case TAP_TO_POUND:
                return 600; // Quick tap animation
            case SWIPE_TO_STIR:
                return 2000; // Full circle stir
            case SHAKE_PAN:
            case SHAKE_BASKET:
                return 800; // Quick shake
            case POUR:
                return 3000; // Long hold
            default:
                return 1500;
        }
    }
    
    private void updateHandPosition() {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) * 0.25f;
        
        if (currentAction == null) {
            handX = centerX;
            handY = centerY;
            return;
        }
        
        switch (currentAction) {
            case TAP_TO_CUT:
            case TAP_TO_POUND:
            case TAP_TO_CRACK:
            case TAP_TO_DRIZZLE:
                // Tap animation: move down then up quickly
                if (animationProgress < 0.5f) {
                    handY = centerY - 30f + animationProgress * 60f;
                } else {
                    handY = centerY + 30f - (animationProgress - 0.5f) * 60f;
                }
                handX = centerX;
                break;
                
            case SWIPE_TO_STIR:
                // Circular motion
                float angle = animationProgress * (float)(2 * Math.PI);
                handX = centerX + radius * (float) Math.cos(angle);
                handY = centerY + radius * (float) Math.sin(angle);
                break;
                
            case SHAKE_PAN:
            case SHAKE_BASKET:
            case SEASON_SHAKE:
                // Left-right shake
                handX = centerX + radius * (float) Math.sin(animationProgress * 4 * Math.PI);
                handY = centerY;
                break;
                
            case POUR:
                // Stationary with pulse
                handX = centerX;
                handY = centerY;
                break;
                
            default:
                handX = centerX;
                handY = centerY;
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!isShowing || currentAction == null) return;
        
        // Draw semi-transparent background
        canvas.drawColor(0x60000000);
        
        // Draw gesture path trail based on action type
        drawGestureTrail(canvas);
        
        // Draw hand/finger pointer
        drawHand(canvas);
        
        // Draw hint text at top
        drawHintText(canvas);
    }
    
    private void drawGestureTrail(Canvas canvas) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) * 0.25f;
        
        if (currentAction == ActionType.SWIPE_TO_STIR) {
            // Draw circular trail
            Path circlePath = new Path();
            RectF oval = new RectF(
                centerX - radius, centerY - radius,
                centerX + radius, centerY + radius
            );
            circlePath.addArc(oval, 0, animationProgress * 360);
            canvas.drawPath(circlePath, trailPaint);
            
            // Draw arrow at end
            if (animationProgress > 0.1f) {
                float angle = animationProgress * (float)(2 * Math.PI);
                float arrowX = centerX + radius * (float) Math.cos(angle);
                float arrowY = centerY + radius * (float) Math.sin(angle);
                drawArrow(canvas, arrowX, arrowY, angle + (float)(Math.PI / 2));
            }
            
        } else if (currentAction == ActionType.SHAKE_PAN || 
                   currentAction == ActionType.SHAKE_BASKET ||
                   currentAction == ActionType.SEASON_SHAKE) {
            // Draw horizontal arrows
            float left = centerX - radius;
            float right = centerX + radius;
            
            arrowPaint.setColor(0xFFFFEB3B);
            // Left arrow
            canvas.drawLine(left, centerY, left + 30, centerY - 20, arrowPaint);
            canvas.drawLine(left, centerY, left + 30, centerY + 20, arrowPaint);
            // Right arrow  
            canvas.drawLine(right, centerY, right - 30, centerY - 20, arrowPaint);
            canvas.drawLine(right, centerY, right - 30, centerY + 20, arrowPaint);
            // Horizontal line
            canvas.drawLine(left, centerY, right, centerY, trailPaint);
            
        } else if (currentAction == ActionType.TAP_TO_CUT || 
                   currentAction == ActionType.TAP_TO_POUND) {
            // Draw tap ripple effect
            float rippleRadius = 40f + (1 - Math.abs(animationProgress - 0.5f) * 2) * 30f;
            int alpha = (int)(255 * (1 - Math.abs(animationProgress - 0.5f) * 2));
            trailPaint.setColor(Color.argb(alpha, 255, 235, 59));
            canvas.drawCircle(centerX, centerY, rippleRadius, trailPaint);
            trailPaint.setColor(0x80FFEB3B); // Reset
            
        } else if (currentAction == ActionType.POUR) {
            // Draw progress circle for hold
            RectF progressRect = new RectF(
                centerX - 60, centerY - 60,
                centerX + 60, centerY + 60
            );
            
            // Background circle
            trailPaint.setColor(0x40FFFFFF);
            canvas.drawArc(progressRect, 0, 360, false, trailPaint);
            
            // Progress arc
            trailPaint.setColor(0xFF4CAF50);
            trailPaint.setStrokeWidth(10f);
            canvas.drawArc(progressRect, -90, animationProgress * 360, false, trailPaint);
            trailPaint.setStrokeWidth(8f);
            trailPaint.setColor(0x80FFEB3B); // Reset
        }
    }
    
    private void drawHand(Canvas canvas) {
        // Draw finger emoji or simple hand indicator
        float fingerSize = 60f;
        
        // Finger shadow
        handPaint.setColor(0x40000000);
        canvas.drawCircle(handX + 4, handY + 4, fingerSize / 2, handPaint);
        
        // Finger
        handPaint.setColor(0xFFFFCC80); // Skin tone
        canvas.drawCircle(handX, handY, fingerSize / 2, handPaint);
        
        // Nail
        handPaint.setColor(0xFFFFE0B2);
        canvas.drawCircle(handX, handY - 10, fingerSize / 4, handPaint);
        
        // Finger tip highlight
        handPaint.setColor(0x40FFFFFF);
        canvas.drawCircle(handX - 5, handY - 5, 8f, handPaint);
    }
    
    private void drawArrow(Canvas canvas, float x, float y, float angle) {
        float arrowSize = 20f;
        float ax1 = x + arrowSize * (float) Math.cos(angle + Math.PI * 0.75);
        float ay1 = y + arrowSize * (float) Math.sin(angle + Math.PI * 0.75);
        float ax2 = x + arrowSize * (float) Math.cos(angle - Math.PI * 0.75);
        float ay2 = y + arrowSize * (float) Math.sin(angle - Math.PI * 0.75);
        
        arrowPaint.setColor(0xFFFFEB3B);
        canvas.drawLine(x, y, ax1, ay1, arrowPaint);
        canvas.drawLine(x, y, ax2, ay2, arrowPaint);
    }
    
    private void drawHintText(Canvas canvas) {
        float textY = 100f;
        
        // Background for text
        float textWidth = textPaint.measureText(hintText);
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0x80000000);
        canvas.drawRoundRect(new RectF(
            getWidth() / 2f - textWidth / 2 - 24,
            textY - 50,
            getWidth() / 2f + textWidth / 2 + 24,
            textY + 10
        ), 16f, 16f, bgPaint);
        
        // Draw text
        canvas.drawText(hintText, getWidth() / 2f, textY, textPaint);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}
