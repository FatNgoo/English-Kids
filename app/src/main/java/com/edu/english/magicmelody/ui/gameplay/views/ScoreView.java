package com.edu.english.magicmelody.ui.gameplay.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 💯 Score View
 * 
 * Display current score with animations
 * - Smooth score counting
 * - Pop animation on score change
 * - Star rating display
 */
public class ScoreView extends View {
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 PAINT
    // ═══════════════════════════════════════════════════════════════
    
    private Paint scorePaint;
    private Paint labelPaint;
    private Paint starPaint;
    private Paint starOutlinePaint;
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SCORE STATE
    // ═══════════════════════════════════════════════════════════════
    
    private int currentScore = 0;
    private int displayedScore = 0;
    private int targetScore = 0;
    private int maxStars = 3;
    private int earnedStars = 0;
    private int[] starThresholds = {1000, 2000, 3000};
    
    private String label = "SCORE";
    private boolean showLabel = true;
    private boolean showStars = true;
    
    private ValueAnimator scoreAnimator;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════
    
    public ScoreView(Context context) {
        super(context);
        init();
    }
    
    public ScoreView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ScoreView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Score text paint
        scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(64);
        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setFakeBoldText(true);
        scorePaint.setShadowLayer(4, 2, 2, 0x80000000);
        
        // Label paint
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(0xAAFFFFFF);
        labelPaint.setTextSize(24);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        
        // Star fill paint
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setColor(0xFFFFD700); // Gold
        
        // Star outline paint
        starOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starOutlinePaint.setStyle(Paint.Style.STROKE);
        starOutlinePaint.setStrokeWidth(2);
        starOutlinePaint.setColor(0xFFFFD700);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎨 DRAWING
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float centerX = getWidth() / 2f;
        float y = getHeight() / 2f;
        
        // Draw label
        if (showLabel) {
            canvas.drawText(label, centerX, y - scorePaint.getTextSize() / 2, labelPaint);
        }
        
        // Draw score
        String scoreText = String.format("%,d", displayedScore);
        
        Rect textBounds = new Rect();
        scorePaint.getTextBounds(scoreText, 0, scoreText.length(), textBounds);
        
        canvas.drawText(scoreText, centerX, y + textBounds.height() / 2f, scorePaint);
        
        // Draw stars
        if (showStars) {
            drawStars(canvas, centerX, y + scorePaint.getTextSize() / 2 + 20);
        }
    }
    
    private void drawStars(Canvas canvas, float centerX, float y) {
        float starSize = 20;
        float starSpacing = 8;
        float totalWidth = maxStars * starSize + (maxStars - 1) * starSpacing;
        float startX = centerX - totalWidth / 2 + starSize / 2;
        
        for (int i = 0; i < maxStars; i++) {
            float x = startX + i * (starSize + starSpacing);
            
            if (i < earnedStars) {
                // Filled star
                drawStar(canvas, x, y, starSize / 2, starPaint);
            } else {
                // Outline star
                drawStar(canvas, x, y, starSize / 2, starOutlinePaint);
            }
        }
    }
    
    private void drawStar(Canvas canvas, float cx, float cy, float radius, Paint paint) {
        android.graphics.Path starPath = new android.graphics.Path();
        
        double angle = Math.PI / 2;
        double angleStep = Math.PI / 5;
        
        for (int i = 0; i < 10; i++) {
            float r = (i % 2 == 0) ? radius : radius * 0.5f;
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
        canvas.drawPath(starPath, paint);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SETTERS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Set score with animation
     */
    public void setScore(int score) {
        this.targetScore = score;
        animateScore(displayedScore, score);
        updateStars(score);
    }
    
    /**
     * Add to current score
     */
    public void addScore(int points) {
        setScore(currentScore + points);
        currentScore += points;
    }
    
    /**
     * Set score instantly without animation
     */
    public void setScoreInstant(int score) {
        this.currentScore = score;
        this.displayedScore = score;
        this.targetScore = score;
        updateStars(score);
        invalidate();
    }
    
    /**
     * Set star thresholds
     */
    public void setStarThresholds(int[] thresholds) {
        this.starThresholds = thresholds;
        this.maxStars = thresholds.length;
        updateStars(currentScore);
    }
    
    /**
     * Set label text
     */
    public void setLabel(String label) {
        this.label = label;
        invalidate();
    }
    
    /**
     * Set text size
     */
    public void setScoreTextSize(float size) {
        scorePaint.setTextSize(size);
        invalidate();
    }
    
    /**
     * Set text color
     */
    public void setScoreColor(int color) {
        scorePaint.setColor(color);
        invalidate();
    }
    
    /**
     * Show/hide label
     */
    public void setShowLabel(boolean show) {
        this.showLabel = show;
        invalidate();
    }
    
    /**
     * Show/hide stars
     */
    public void setShowStars(boolean show) {
        this.showStars = show;
        invalidate();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public int getCurrentScore() {
        return currentScore;
    }
    
    public int getEarnedStars() {
        return earnedStars;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎬 ANIMATIONS
    // ═══════════════════════════════════════════════════════════════
    
    private void animateScore(int from, int to) {
        if (scoreAnimator != null && scoreAnimator.isRunning()) {
            scoreAnimator.cancel();
        }
        
        scoreAnimator = ValueAnimator.ofInt(from, to);
        scoreAnimator.setDuration(500);
        scoreAnimator.addUpdateListener(animation -> {
            displayedScore = (int) animation.getAnimatedValue();
            invalidate();
        });
        scoreAnimator.start();
        
        // Pop animation
        playPopAnimation();
    }
    
    /**
     * Pop animation when score changes
     */
    public void playPopAnimation() {
        AnimatorSet popSet = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.2f, 1f);
        
        popSet.playTogether(scaleX, scaleY);
        popSet.setDuration(200);
        popSet.setInterpolator(new OvershootInterpolator());
        popSet.start();
    }
    
    /**
     * Big pop for significant score (like combo bonus)
     */
    public void playBigPopAnimation() {
        AnimatorSet popSet = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.5f, 1f);
        
        popSet.playTogether(scaleX, scaleY);
        popSet.setDuration(300);
        popSet.setInterpolator(new OvershootInterpolator());
        popSet.start();
    }
    
    /**
     * Update earned stars based on score
     */
    private void updateStars(int score) {
        int newStars = 0;
        
        for (int threshold : starThresholds) {
            if (score >= threshold) {
                newStars++;
            }
        }
        
        if (newStars > earnedStars) {
            // New star earned - animate!
            for (int i = earnedStars; i < newStars; i++) {
                animateNewStar(i);
            }
        }
        
        earnedStars = newStars;
        invalidate();
    }
    
    private void animateNewStar(int starIndex) {
        // Trigger callback or animation for new star
        // Can be overridden or extended
    }
    
    /**
     * Reset score
     */
    public void reset() {
        currentScore = 0;
        displayedScore = 0;
        targetScore = 0;
        earnedStars = 0;
        invalidate();
    }
}
