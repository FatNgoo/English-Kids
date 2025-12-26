package com.edu.english.magicmelody.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

/**
 * BouncyPianoKeyView - Single piano key with spring animation bounce effect
 * Used in PianoKeyBarView for the rhythm game
 */
public class BouncyPianoKeyView extends View {
    
    // Key colors (rainbow for kids)
    private static final int[] KEY_COLORS = {
        0xFFFF5252,  // C - Red
        0xFFFF9800,  // D - Orange
        0xFFFFEB3B,  // E - Yellow
        0xFF4CAF50,  // F - Green
        0xFF2196F3,  // G - Blue
        0xFF9C27B0,  // H - Purple
        0xFFE91E63   // I - Pink
    };
    
    private static final String[] NOTE_NAMES = {"C", "D", "E", "F", "G", "A", "B"};
    
    private int noteIndex = 0;
    private boolean isPressed = false;
    private boolean isHighlighted = false;
    
    private Paint keyPaint;
    private Paint borderPaint;
    private Paint textPaint;
    private Paint shadowPaint;
    private RectF keyRect;
    
    private SpringAnimation scaleXAnimation;
    private SpringAnimation scaleYAnimation;
    private float currentScale = 1f;
    
    private OnKeyPressListener listener;
    
    public interface OnKeyPressListener {
        void onKeyPressed(int noteIndex);
        void onKeyReleased(int noteIndex);
    }
    
    public BouncyPianoKeyView(Context context) {
        super(context);
        init();
    }
    
    public BouncyPianoKeyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public BouncyPianoKeyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Key paint
        keyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        keyPaint.setStyle(Paint.Style.FILL);
        
        // Border paint
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setColor(Color.WHITE);
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        
        // Shadow paint
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(0x40000000);
        
        keyRect = new RectF();
        
        // Setup spring animations
        setupSpringAnimations();
        
        setClickable(true);
    }
    
    private void setupSpringAnimations() {
        SpringForce springForce = new SpringForce(1f)
            .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
            .setStiffness(SpringForce.STIFFNESS_MEDIUM);
        
        scaleXAnimation = new SpringAnimation(this, DynamicAnimation.SCALE_X);
        scaleXAnimation.setSpring(springForce);
        
        scaleYAnimation = new SpringAnimation(this, DynamicAnimation.SCALE_Y);
        scaleYAnimation.setSpring(springForce);
    }
    
    public void setNoteIndex(int index) {
        this.noteIndex = Math.max(0, Math.min(6, index));
        invalidate();
    }
    
    public int getNoteIndex() {
        return noteIndex;
    }
    
    public void setHighlighted(boolean highlighted) {
        this.isHighlighted = highlighted;
        invalidate();
    }
    
    public void setOnKeyPressListener(OnKeyPressListener listener) {
        this.listener = listener;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float width = getWidth();
        float height = getHeight();
        float padding = 4f;
        float cornerRadius = 16f;
        
        // Key rect
        keyRect.set(padding, padding, width - padding, height - padding);
        
        // Draw shadow
        canvas.save();
        canvas.translate(4f, 4f);
        canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, shadowPaint);
        canvas.restore();
        
        // Key color with gradient
        int baseColor = KEY_COLORS[noteIndex % KEY_COLORS.length];
        if (isPressed) {
            // Darken when pressed
            baseColor = darkenColor(baseColor, 0.7f);
        } else if (isHighlighted) {
            // Brighten when highlighted
            baseColor = lightenColor(baseColor, 1.3f);
        }
        
        // Create gradient
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, height,
            lightenColor(baseColor, 1.2f),
            baseColor,
            Shader.TileMode.CLAMP
        );
        keyPaint.setShader(gradient);
        
        // Draw key
        canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, keyPaint);
        
        // Draw border (glow effect when highlighted)
        if (isHighlighted) {
            borderPaint.setColor(Color.WHITE);
            borderPaint.setStrokeWidth(6f);
        } else {
            borderPaint.setColor(0x80FFFFFF);
            borderPaint.setStrokeWidth(3f);
        }
        canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, borderPaint);
        
        // Draw note name
        textPaint.setTextSize(height * 0.3f);
        float textY = height / 2f + textPaint.getTextSize() / 3f;
        canvas.drawText(NOTE_NAMES[noteIndex], width / 2f, textY, textPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onPress();
                return true;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onRelease();
                return true;
        }
        return super.onTouchEvent(event);
    }
    
    private void onPress() {
        isPressed = true;
        
        // Animate scale down
        scaleXAnimation.animateToFinalPosition(0.9f);
        scaleYAnimation.animateToFinalPosition(0.9f);
        
        // Notify listener
        if (listener != null) {
            listener.onKeyPressed(noteIndex);
        }
        
        invalidate();
    }
    
    private void onRelease() {
        isPressed = false;
        
        // Bounce back
        scaleXAnimation.animateToFinalPosition(1f);
        scaleYAnimation.animateToFinalPosition(1f);
        
        // Notify listener
        if (listener != null) {
            listener.onKeyReleased(noteIndex);
        }
        
        invalidate();
    }
    
    /**
     * Programmatically trigger a press (for visual feedback)
     */
    public void simulatePress() {
        isPressed = true;
        scaleXAnimation.animateToFinalPosition(0.9f);
        scaleYAnimation.animateToFinalPosition(0.9f);
        invalidate();
        
        postDelayed(() -> {
            isPressed = false;
            scaleXAnimation.animateToFinalPosition(1f);
            scaleYAnimation.animateToFinalPosition(1f);
            invalidate();
        }, 150);
    }
    
    private int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = (int) (Color.red(color) * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color) * factor);
        return Color.argb(a, Math.max(0, r), Math.max(0, g), Math.max(0, b));
    }
    
    private int lightenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = (int) Math.min(255, Color.red(color) * factor);
        int g = (int) Math.min(255, Color.green(color) * factor);
        int b = (int) Math.min(255, Color.blue(color) * factor);
        return Color.argb(a, r, g, b);
    }
}
