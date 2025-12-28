package com.edu.english.magicmelody.ui.gameplay.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.edu.english.R;

/**
 * 🎵 Note View
 * 
 * A single falling note in the rhythm game
 * - Star-shaped musical notes with glow effect
 * - Different colors for different note types
 * - Animation for falling and hit feedback
 */
public class NoteView extends View {
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 PAINT & DRAWING
    // ═══════════════════════════════════════════════════════════════
    
    private Paint notePaint;
    private Paint glowPaint;
    private Paint innerPaint;
    private Path starPath;
    
    private int noteColor = 0xFFFFD700;  // Default gold
    private int glowColor = 0x80FFFF00;
    private float glowRadius = 20f;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 NOTE STATE
    // ═══════════════════════════════════════════════════════════════
    
    private String noteId;
    private String noteName;
    private int laneIndex = 0;
    private float progress = 0f;  // 0 = top, 1 = hit zone
    private boolean isHit = false;
    private boolean isMissed = false;
    
    public enum NoteType {
        BASIC,      // Regular note
        SPECIAL,    // Special note with bonus
        GOLDEN,     // Golden note (extra points)
        COMBO       // Part of combo sequence
    }
    
    private NoteType noteType = NoteType.BASIC;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public NoteView(Context context) {
        super(context);
        init();
    }
    
    public NoteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public NoteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Main note paint
        notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        notePaint.setStyle(Paint.Style.FILL);
        notePaint.setColor(noteColor);
        
        // Glow effect paint
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(glowRadius, 
            android.graphics.BlurMaskFilter.Blur.NORMAL));
        
        // Inner highlight paint
        innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setColor(0xFFFFFFFF);
        
        // Star path
        starPath = new Path();
        
        // Enable layer for blur
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 DRAWING
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (isMissed) {
            setAlpha(0.3f);
        }
        
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - glowRadius;
        
        // Draw glow
        glowPaint.setColor(glowColor);
        glowPaint.setShader(new RadialGradient(
            centerX, centerY, radius * 1.5f,
            glowColor, 0x00000000,
            Shader.TileMode.CLAMP
        ));
        canvas.drawCircle(centerX, centerY, radius * 1.3f, glowPaint);
        
        // Draw star shape
        updateStarPath(centerX, centerY, radius, radius * 0.5f, 5);
        
        // Main color with gradient
        notePaint.setShader(new RadialGradient(
            centerX, centerY * 0.8f, radius,
            lightenColor(noteColor, 0.3f), noteColor,
            Shader.TileMode.CLAMP
        ));
        canvas.drawPath(starPath, notePaint);
        
        // Inner highlight
        updateStarPath(centerX, centerY, radius * 0.4f, radius * 0.2f, 5);
        canvas.drawPath(starPath, innerPaint);
        
        // Musical note symbol (optional)
        if (noteType == NoteType.GOLDEN) {
            drawMusicalNote(canvas, centerX, centerY, radius * 0.5f);
        }
    }
    
    /**
     * Create star path
     */
    private void updateStarPath(float cx, float cy, float outerRadius, float innerRadius, int points) {
        starPath.reset();
        
        double angle = Math.PI / 2; // Start at top
        double angleStep = Math.PI / points;
        
        for (int i = 0; i < points * 2; i++) {
            float r = (i % 2 == 0) ? outerRadius : innerRadius;
            float x = (float)(cx + r * Math.cos(angle));
            float y = (float)(cy - r * Math.sin(angle));
            
            if (i == 0) {
                starPath.moveTo(x, y);
            } else {
                starPath.lineTo(x, y);
            }
            
            angle -= angleStep;
        }
        
        starPath.close();
    }
    
    /**
     * Draw musical note symbol
     */
    private void drawMusicalNote(Canvas canvas, float cx, float cy, float size) {
        Paint symbolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        symbolPaint.setColor(0x80000000);
        symbolPaint.setTextSize(size);
        symbolPaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText("♪", cx, cy + size / 3f, symbolPaint);
    }
    
    /**
     * Lighten a color
     */
    private int lightenColor(int color, float amount) {
        int r = Math.min(255, (int)(((color >> 16) & 0xFF) * (1 + amount)));
        int g = Math.min(255, (int)(((color >> 8) & 0xFF) * (1 + amount)));
        int b = Math.min(255, (int)((color & 0xFF) * (1 + amount)));
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }
    
    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }
    
    public void setLaneIndex(int laneIndex) {
        this.laneIndex = laneIndex;
    }
    
    public void setProgress(float progress) {
        this.progress = progress;
    }
    
    public void setNoteColor(int color) {
        this.noteColor = color;
        this.glowColor = (color & 0x00FFFFFF) | 0x80000000;
        invalidate();
    }
    
    public void setNoteType(NoteType type) {
        this.noteType = type;
        
        switch (type) {
            case SPECIAL:
                setNoteColor(0xFF00FFFF); // Cyan
                break;
            case GOLDEN:
                setNoteColor(0xFFFFD700); // Gold
                break;
            case COMBO:
                setNoteColor(0xFFFF00FF); // Magenta
                break;
            case BASIC:
            default:
                setNoteColor(0xFF4CAF50); // Green
                break;
        }
    }
    
    public void setColorForLane(int laneIndex) {
        int[] laneColors = {
            0xFFFF6B6B, // Red
            0xFF4ECDC4, // Teal
            0xFFFFE66D, // Yellow
            0xFF95E1D3, // Mint
            0xFFDDA0DD  // Plum
        };
        
        setNoteColor(laneColors[laneIndex % laneColors.length]);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public String getNoteId() {
        return noteId;
    }
    
    public String getNoteName() {
        return noteName;
    }
    
    public int getLaneIndex() {
        return laneIndex;
    }
    
    public float getProgress() {
        return progress;
    }
    
    public boolean isHit() {
        return isHit;
    }
    
    public boolean isMissed() {
        return isMissed;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎬 ANIMATIONS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Play hit animation (expand and fade)
     */
    public void playHitAnimation(@Nullable Runnable onComplete) {
        this.isHit = true;
        
        AnimatorSet animSet = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.5f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        
        animSet.playTogether(scaleX, scaleY, alpha);
        animSet.setDuration(200);
        
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {}
            
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if (onComplete != null) onComplete.run();
            }
            
            @Override
            public void onAnimationCancel(@NonNull Animator animation) {}
            
            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });
        
        animSet.start();
    }
    
    /**
     * Play perfect hit animation (sparkle effect)
     */
    public void playPerfectHitAnimation(@Nullable Runnable onComplete) {
        this.isHit = true;
        this.noteColor = 0xFFFFD700; // Gold
        invalidate();
        
        AnimatorSet animSet = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 2f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f);
        
        animSet.playTogether(scaleX, scaleY, alpha, rotation);
        animSet.setDuration(300);
        
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {}
            
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if (onComplete != null) onComplete.run();
            }
            
            @Override
            public void onAnimationCancel(@NonNull Animator animation) {}
            
            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });
        
        animSet.start();
    }
    
    /**
     * Play miss animation (fade and fall)
     */
    public void playMissAnimation(@Nullable Runnable onComplete) {
        this.isMissed = true;
        
        AnimatorSet animSet = new AnimatorSet();
        
        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        ObjectAnimator transY = ObjectAnimator.ofFloat(this, "translationY", 
            getTranslationY(), getTranslationY() + 100);
        
        animSet.playTogether(alpha, transY);
        animSet.setDuration(300);
        
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {}
            
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if (onComplete != null) onComplete.run();
            }
            
            @Override
            public void onAnimationCancel(@NonNull Animator animation) {}
            
            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });
        
        animSet.start();
    }
    
    /**
     * Pulse animation (for idle/waiting)
     */
    public void startPulseAnimation() {
        ObjectAnimator pulse = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.1f, 1f);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setDuration(800);
        pulse.start();
        
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.1f, 1f);
        pulseY.setRepeatCount(ValueAnimator.INFINITE);
        pulseY.setDuration(800);
        pulseY.start();
    }
}
