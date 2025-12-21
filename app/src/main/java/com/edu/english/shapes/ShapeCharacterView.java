package com.edu.english.shapes;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

import com.edu.english.shapes.models.ShapeType;

/**
 * Enhanced ShapeCharacterView - Draws a cute animated shape character
 * with arms, legs, hands, shoes matching the reference images
 */
public class ShapeCharacterView extends View {
    
    private ShapeType shapeType = ShapeType.SQUARE;
    
    // Paints
    private Paint bodyPaint;
    private Paint bodyGradientPaint;
    private Paint bodyHighlightPaint;
    private Paint bodyStrokePaint;
    private Paint eyeWhitePaint;
    private Paint eyeOutlinePaint;
    private Paint eyePupilPaint;
    private Paint eyeShinePaint;
    private Paint mouthPaint;
    private Paint mouthInnerPaint;
    private Paint armPaint;
    private Paint handPaint;
    private Paint handOutlinePaint;
    private Paint legPaint;
    private Paint shoePaint;
    private Paint shoeHighlightPaint;
    private Paint blushPaint;
    
    // Colors
    private int bodyColor;
    private int bodyLightColor;
    private int bodyDarkColor;
    
    // Animation values
    private float bounceOffset = 0f;
    private float waveAngle = 0f;
    private float eyeLookX = 0f;
    private float eyeLookY = 0f;
    private float blinkProgress = 0f;
    private float breatheScale = 1f;
    private float entranceScale = 0f;
    private float jumpOffset = 0f;
    
    // Animators
    private ValueAnimator bounceAnimator;
    private ValueAnimator waveAnimator;
    private ValueAnimator eyeAnimator;
    private ValueAnimator blinkAnimator;
    private ValueAnimator breatheAnimator;
    private ValueAnimator entranceAnimator;
    
    // State
    private boolean isWaving = false;
    private boolean isJumping = false;
    private boolean hasEnteredScreen = false;
    
    public ShapeCharacterView(Context context) {
        super(context);
        init();
    }
    
    public ShapeCharacterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ShapeCharacterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Body paints
        bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setStyle(Paint.Style.FILL);
        
        bodyGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyGradientPaint.setStyle(Paint.Style.FILL);
        
        bodyHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyHighlightPaint.setStyle(Paint.Style.FILL);
        bodyHighlightPaint.setColor(Color.parseColor("#40FFFFFF"));
        
        bodyStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyStrokePaint.setStyle(Paint.Style.STROKE);
        bodyStrokePaint.setStrokeWidth(4f);
        
        // Eye paints
        eyeWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeWhitePaint.setStyle(Paint.Style.FILL);
        eyeWhitePaint.setColor(Color.WHITE);
        
        eyeOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeOutlinePaint.setStyle(Paint.Style.STROKE);
        eyeOutlinePaint.setColor(Color.parseColor("#2C3E50"));
        eyeOutlinePaint.setStrokeWidth(2f);
        
        eyePupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePupilPaint.setStyle(Paint.Style.FILL);
        eyePupilPaint.setColor(Color.parseColor("#2C3E50"));
        
        eyeShinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeShinePaint.setStyle(Paint.Style.FILL);
        eyeShinePaint.setColor(Color.WHITE);
        
        // Mouth paints
        mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthPaint.setStyle(Paint.Style.FILL);
        mouthPaint.setColor(Color.parseColor("#C62828"));
        
        mouthInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthInnerPaint.setStyle(Paint.Style.FILL);
        mouthInnerPaint.setColor(Color.parseColor("#8B0000"));
        
        // Arm and hand paints
        armPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        armPaint.setStyle(Paint.Style.STROKE);
        armPaint.setStrokeCap(Paint.Cap.ROUND);
        armPaint.setStrokeWidth(12f);
        
        handPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handPaint.setStyle(Paint.Style.FILL);
        handPaint.setColor(Color.WHITE);
        
        handOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handOutlinePaint.setStyle(Paint.Style.STROKE);
        handOutlinePaint.setColor(Color.parseColor("#BDBDBD"));
        handOutlinePaint.setStrokeWidth(2f);
        
        // Leg paints
        legPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legPaint.setStyle(Paint.Style.STROKE);
        legPaint.setStrokeCap(Paint.Cap.ROUND);
        legPaint.setStrokeWidth(10f);
        
        // Shoe paints
        shoePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shoePaint.setStyle(Paint.Style.FILL);
        shoePaint.setColor(Color.WHITE);
        
        shoeHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shoeHighlightPaint.setStyle(Paint.Style.FILL);
        shoeHighlightPaint.setColor(Color.parseColor("#E0E0E0"));
        
        // Blush
        blushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blushPaint.setStyle(Paint.Style.FILL);
        blushPaint.setColor(Color.parseColor("#30FF6B6B"));
        
        // Start idle animations
        startIdleAnimations();
    }
    
    public void setShapeType(ShapeType type) {
        this.shapeType = type;
        this.bodyColor = type.getColor();
        this.bodyLightColor = lightenColor(bodyColor, 0.25f);
        this.bodyDarkColor = darkenColor(bodyColor, 0.15f);
        
        bodyPaint.setColor(bodyColor);
        bodyStrokePaint.setColor(bodyDarkColor);
        armPaint.setColor(bodyColor);
        legPaint.setColor(bodyColor);
        
        invalidate();
    }
    
    private void startIdleAnimations() {
        // Bounce animation
        bounceAnimator = ValueAnimator.ofFloat(0f, 1f);
        bounceAnimator.setDuration(1500);
        bounceAnimator.setRepeatCount(ValueAnimator.INFINITE);
        bounceAnimator.setRepeatMode(ValueAnimator.REVERSE);
        bounceAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        bounceAnimator.addUpdateListener(anim -> {
            bounceOffset = (float) anim.getAnimatedValue() * 8f;
            invalidate();
        });
        bounceAnimator.start();
        
        // Eye look animation
        eyeAnimator = ValueAnimator.ofFloat(0f, 1f);
        eyeAnimator.setDuration(3000);
        eyeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        eyeAnimator.setRepeatMode(ValueAnimator.REVERSE);
        eyeAnimator.addUpdateListener(anim -> {
            float val = (float) anim.getAnimatedValue();
            eyeLookX = (val - 0.5f) * 6f;
            eyeLookY = (float) Math.sin(val * Math.PI * 2) * 2f;
        });
        eyeAnimator.start();
        
        // Blink animation
        blinkAnimator = ValueAnimator.ofFloat(0f, 1f, 0f);
        blinkAnimator.setDuration(150);
        blinkAnimator.setStartDelay(3000);
        blinkAnimator.setRepeatCount(ValueAnimator.INFINITE);
        blinkAnimator.setRepeatMode(ValueAnimator.RESTART);
        blinkAnimator.addUpdateListener(anim -> {
            blinkProgress = (float) anim.getAnimatedValue();
            invalidate();
        });
        blinkAnimator.start();
        
        // Breathe animation
        breatheAnimator = ValueAnimator.ofFloat(1f, 1.03f);
        breatheAnimator.setDuration(2000);
        breatheAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breatheAnimator.setRepeatMode(ValueAnimator.REVERSE);
        breatheAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        breatheAnimator.addUpdateListener(anim -> {
            breatheScale = (float) anim.getAnimatedValue();
        });
        breatheAnimator.start();
    }
    
    public void playEntranceAnimation() {
        if (hasEnteredScreen) return;
        hasEnteredScreen = true;
        
        entranceAnimator = ValueAnimator.ofFloat(0f, 1f);
        entranceAnimator.setDuration(800);
        entranceAnimator.setInterpolator(new OvershootInterpolator(1.5f));
        entranceAnimator.addUpdateListener(anim -> {
            entranceScale = (float) anim.getAnimatedValue();
            invalidate();
        });
        entranceAnimator.start();
    }
    
    public void playWaveAnimation() {
        if (isWaving) return;
        isWaving = true;
        
        waveAnimator = ValueAnimator.ofFloat(0f, 30f, -20f, 25f, -15f, 20f, 0f);
        waveAnimator.setDuration(1500);
        waveAnimator.addUpdateListener(anim -> {
            waveAngle = (float) anim.getAnimatedValue();
            invalidate();
        });
        waveAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isWaving = false;
            }
        });
        waveAnimator.start();
    }
    
    public void playJumpAnimation() {
        if (isJumping) return;
        isJumping = true;
        
        ValueAnimator jumpAnimator = ValueAnimator.ofFloat(0f, -50f, 0f);
        jumpAnimator.setDuration(500);
        jumpAnimator.setInterpolator(new BounceInterpolator());
        jumpAnimator.addUpdateListener(anim -> {
            jumpOffset = (float) anim.getAnimatedValue();
            invalidate();
        });
        jumpAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isJumping = false;
                jumpOffset = 0f;
            }
        });
        jumpAnimator.start();
    }
    
    public void pointToRight() {
        // Animation to point right (for vocabulary scene)
        isWaving = true;
        waveAnimator = ValueAnimator.ofFloat(0f, -45f);
        waveAnimator.setDuration(500);
        waveAnimator.setInterpolator(new OvershootInterpolator());
        waveAnimator.addUpdateListener(anim -> {
            waveAngle = (float) anim.getAnimatedValue();
            invalidate();
        });
        waveAnimator.start();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        
        // Apply entrance and bounce animations
        float scale = entranceScale * breatheScale;
        if (scale == 0f) scale = breatheScale;
        
        canvas.save();
        canvas.translate(0, bounceOffset + jumpOffset);
        canvas.scale(scale, scale, centerX, centerY);
        
        // Calculate body dimensions
        float bodySize = Math.min(width, height) * 0.35f;
        float bodyTop = centerY - bodySize * 0.6f;
        float bodyBottom = centerY + bodySize * 0.6f;
        
        // Draw legs first (behind body)
        drawLegs(canvas, centerX, bodyBottom, bodySize);
        
        // Draw arms
        drawArms(canvas, centerX, centerY, bodySize);
        
        // Draw body
        drawBody(canvas, centerX, centerY, bodySize);
        
        // Draw face
        drawFace(canvas, centerX, centerY - bodySize * 0.1f, bodySize);
        
        canvas.restore();
    }
    
    private void drawBody(Canvas canvas, float cx, float cy, float size) {
        Path bodyPath = new Path();
        float cornerRadius = size * 0.15f;
        
        // Create gradient
        LinearGradient gradient = new LinearGradient(
            cx - size, cy - size, cx + size * 0.5f, cy + size,
            bodyLightColor, bodyColor,
            Shader.TileMode.CLAMP
        );
        bodyGradientPaint.setShader(gradient);
        
        switch (shapeType) {
            case SQUARE:
            case RECTANGLE:
                float w = shapeType == ShapeType.RECTANGLE ? size * 1.2f : size;
                RectF rect = new RectF(cx - w, cy - size, cx + w, cy + size);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bodyGradientPaint);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bodyStrokePaint);
                
                // Highlight
                Path highlightPath = new Path();
                highlightPath.moveTo(cx - w * 0.6f, cy - size * 0.8f);
                highlightPath.quadTo(cx - w * 0.8f, cy - size * 0.5f, cx - w * 0.7f, cy - size * 0.2f);
                highlightPath.lineTo(cx - w * 0.5f, cy - size * 0.3f);
                highlightPath.quadTo(cx - w * 0.6f, cy - size * 0.6f, cx - w * 0.4f, cy - size * 0.75f);
                highlightPath.close();
                canvas.drawPath(highlightPath, bodyHighlightPaint);
                break;
                
            case CIRCLE:
                canvas.drawCircle(cx, cy, size, bodyGradientPaint);
                canvas.drawCircle(cx, cy, size, bodyStrokePaint);
                canvas.drawCircle(cx - size * 0.4f, cy - size * 0.3f, size * 0.2f, bodyHighlightPaint);
                break;
                
            case TRIANGLE:
                bodyPath.moveTo(cx, cy - size);
                bodyPath.lineTo(cx + size, cy + size * 0.8f);
                bodyPath.lineTo(cx - size, cy + size * 0.8f);
                bodyPath.close();
                canvas.drawPath(bodyPath, bodyGradientPaint);
                canvas.drawPath(bodyPath, bodyStrokePaint);
                break;
                
            case OVAL:
                RectF oval = new RectF(cx - size * 0.75f, cy - size, cx + size * 0.75f, cy + size);
                canvas.drawOval(oval, bodyGradientPaint);
                canvas.drawOval(oval, bodyStrokePaint);
                break;
                
            case STAR:
                drawStarBody(canvas, cx, cy, size);
                break;
        }
    }
    
    private void drawStarBody(Canvas canvas, float cx, float cy, float size) {
        Path starPath = new Path();
        int points = 5;
        float outerRadius = size;
        float innerRadius = size * 0.45f;
        
        for (int i = 0; i < points * 2; i++) {
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;
            float angle = (float) (Math.PI / 2 + Math.PI * i / points);
            float px = cx + (float) Math.cos(angle) * radius;
            float py = cy - (float) Math.sin(angle) * radius;
            
            if (i == 0) {
                starPath.moveTo(px, py);
            } else {
                starPath.lineTo(px, py);
            }
        }
        starPath.close();
        
        canvas.drawPath(starPath, bodyGradientPaint);
        canvas.drawPath(starPath, bodyStrokePaint);
    }
    
    private void drawFace(Canvas canvas, float cx, float cy, float size) {
        float eyeSpacing = size * 0.35f;
        float eyeY = cy;
        float eyeRadius = size * 0.18f;
        float pupilRadius = eyeRadius * 0.5f;
        
        // Adjust for triangle
        if (shapeType == ShapeType.TRIANGLE) {
            eyeY = cy + size * 0.1f;
        }
        
        // Draw eyes
        if (blinkProgress < 0.5f) {
            // Left eye
            drawEye(canvas, cx - eyeSpacing, eyeY, eyeRadius, pupilRadius);
            // Right eye
            drawEye(canvas, cx + eyeSpacing, eyeY, eyeRadius, pupilRadius);
        } else {
            // Blinking - draw lines
            Paint blinkPaint = new Paint(eyeOutlinePaint);
            blinkPaint.setStrokeWidth(4f);
            canvas.drawLine(cx - eyeSpacing - eyeRadius, eyeY, 
                          cx - eyeSpacing + eyeRadius, eyeY, blinkPaint);
            canvas.drawLine(cx + eyeSpacing - eyeRadius, eyeY, 
                          cx + eyeSpacing + eyeRadius, eyeY, blinkPaint);
        }
        
        // Draw blush
        canvas.drawOval(new RectF(cx - eyeSpacing - eyeRadius * 1.5f - size * 0.1f, 
                                  eyeY + eyeRadius * 0.8f,
                                  cx - eyeSpacing - eyeRadius * 0.3f + size * 0.1f, 
                                  eyeY + eyeRadius * 1.6f), blushPaint);
        canvas.drawOval(new RectF(cx + eyeSpacing + eyeRadius * 0.3f - size * 0.1f, 
                                  eyeY + eyeRadius * 0.8f,
                                  cx + eyeSpacing + eyeRadius * 1.5f + size * 0.1f, 
                                  eyeY + eyeRadius * 1.6f), blushPaint);
        
        // Draw smile
        float mouthY = eyeY + size * 0.4f;
        float mouthWidth = size * 0.35f;
        float mouthHeight = size * 0.2f;
        
        // Smile arc
        Path smilePath = new Path();
        RectF mouthRect = new RectF(cx - mouthWidth, mouthY - mouthHeight, 
                                     cx + mouthWidth, mouthY + mouthHeight);
        smilePath.addArc(mouthRect, 0, 180);
        canvas.drawPath(smilePath, mouthPaint);
        
        // Inner mouth
        RectF innerMouthRect = new RectF(cx - mouthWidth * 0.7f, mouthY - mouthHeight * 0.5f, 
                                         cx + mouthWidth * 0.7f, mouthY + mouthHeight * 0.7f);
        Path innerSmilePath = new Path();
        innerSmilePath.addArc(innerMouthRect, 0, 180);
        canvas.drawPath(innerSmilePath, mouthInnerPaint);
        
        // Tongue (optional for more cuteness)
        Paint tonguePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tonguePaint.setColor(Color.parseColor("#FF8A80"));
        canvas.drawOval(new RectF(cx - mouthWidth * 0.25f, mouthY + mouthHeight * 0.1f,
                                  cx + mouthWidth * 0.25f, mouthY + mouthHeight * 0.6f), tonguePaint);
    }
    
    private void drawEye(Canvas canvas, float cx, float cy, float eyeRadius, float pupilRadius) {
        // Eye white (slightly oval)
        RectF eyeRect = new RectF(cx - eyeRadius, cy - eyeRadius * 1.15f, 
                                   cx + eyeRadius, cy + eyeRadius * 0.85f);
        canvas.drawOval(eyeRect, eyeWhitePaint);
        canvas.drawOval(eyeRect, eyeOutlinePaint);
        
        // Pupil with look offset
        float pupilX = cx + eyeLookX;
        float pupilY = cy + eyeLookY;
        canvas.drawCircle(pupilX, pupilY, pupilRadius, eyePupilPaint);
        
        // Eye shine
        float shineRadius = pupilRadius * 0.35f;
        canvas.drawCircle(pupilX - pupilRadius * 0.25f, pupilY - pupilRadius * 0.25f, 
                         shineRadius, eyeShinePaint);
        canvas.drawCircle(pupilX + pupilRadius * 0.15f, pupilY + pupilRadius * 0.2f, 
                         shineRadius * 0.5f, eyeShinePaint);
    }
    
    private void drawArms(Canvas canvas, float cx, float cy, float size) {
        float armLength = size * 0.6f;
        float armStartY = cy - size * 0.2f;
        float handRadius = size * 0.12f;
        
        // Adjust arm paint color
        armPaint.setColor(bodyColor);
        armPaint.setStrokeWidth(size * 0.1f);
        
        // Left arm
        float leftArmEndX = cx - size - armLength * 0.8f;
        float leftArmEndY = armStartY + armLength * 0.4f;
        canvas.drawLine(cx - size + size * 0.1f, armStartY, leftArmEndX, leftArmEndY, armPaint);
        
        // Left hand
        drawHand(canvas, leftArmEndX, leftArmEndY, handRadius, false);
        
        // Right arm (with wave animation)
        canvas.save();
        float rightArmStartX = cx + size - size * 0.1f;
        canvas.rotate(waveAngle, rightArmStartX, armStartY);
        
        float rightArmEndX = rightArmStartX + armLength * 0.8f;
        float rightArmEndY = armStartY + armLength * 0.4f;
        canvas.drawLine(rightArmStartX, armStartY, rightArmEndX, rightArmEndY, armPaint);
        
        // Right hand
        drawHand(canvas, rightArmEndX, rightArmEndY, handRadius, true);
        
        canvas.restore();
    }
    
    private void drawHand(Canvas canvas, float cx, float cy, float radius, boolean isRight) {
        // Draw gloved hand
        canvas.drawCircle(cx, cy, radius, handPaint);
        canvas.drawCircle(cx, cy, radius, handOutlinePaint);
        
        // Draw fingers (3 small bumps)
        float fingerRadius = radius * 0.35f;
        float fingerSpacing = radius * 0.55f;
        
        for (int i = -1; i <= 1; i++) {
            float fingerX = cx + i * fingerSpacing * (isRight ? 1 : -1);
            float fingerY = cy - radius * 0.5f;
            canvas.drawCircle(fingerX, fingerY, fingerRadius, handPaint);
            canvas.drawCircle(fingerX, fingerY, fingerRadius, handOutlinePaint);
        }
        
        // Thumb
        float thumbX = cx + (isRight ? -1 : 1) * radius * 0.9f;
        float thumbY = cy + radius * 0.2f;
        canvas.drawCircle(thumbX, thumbY, fingerRadius, handPaint);
        canvas.drawCircle(thumbX, thumbY, fingerRadius, handOutlinePaint);
    }
    
    private void drawLegs(Canvas canvas, float cx, float bodyBottom, float size) {
        float legLength = size * 0.4f;
        float legSpacing = size * 0.35f;
        float shoeWidth = size * 0.25f;
        float shoeHeight = size * 0.12f;
        
        legPaint.setColor(bodyColor);
        legPaint.setStrokeWidth(size * 0.08f);
        
        // Left leg
        float leftLegX = cx - legSpacing;
        canvas.drawLine(leftLegX, bodyBottom, leftLegX, bodyBottom + legLength, legPaint);
        
        // Left shoe
        drawShoe(canvas, leftLegX, bodyBottom + legLength, shoeWidth, shoeHeight, false);
        
        // Right leg
        float rightLegX = cx + legSpacing;
        canvas.drawLine(rightLegX, bodyBottom, rightLegX, bodyBottom + legLength, legPaint);
        
        // Right shoe
        drawShoe(canvas, rightLegX, bodyBottom + legLength, shoeWidth, shoeHeight, true);
    }
    
    private void drawShoe(Canvas canvas, float cx, float cy, float width, float height, boolean isRight) {
        // Shoe body
        RectF shoeRect = new RectF(
            cx - width * 0.6f + (isRight ? width * 0.3f : 0),
            cy - height * 0.3f,
            cx + width * 0.6f + (isRight ? width * 0.3f : 0),
            cy + height
        );
        canvas.drawRoundRect(shoeRect, height * 0.5f, height * 0.5f, shoePaint);
        
        // Shoe toe (rounded front)
        float toeX = isRight ? shoeRect.right : shoeRect.left;
        float direction = isRight ? 1 : -1;
        
        RectF toeRect = new RectF(
            toeX - width * 0.3f,
            cy - height * 0.2f,
            toeX + width * 0.4f * direction,
            cy + height * 0.8f
        );
        if (!isRight) {
            toeRect.left = toeX - width * 0.4f;
            toeRect.right = toeX + width * 0.3f;
        }
        canvas.drawOval(toeRect, shoePaint);
        
        // Shoe line detail
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#E0E0E0"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);
        canvas.drawLine(shoeRect.left + width * 0.1f, cy + height * 0.3f,
                       shoeRect.right - width * 0.1f, cy + height * 0.3f, linePaint);
    }
    
    private int lightenColor(int color, float factor) {
        int r = (int) ((Color.red(color) * (1 - factor) + 255 * factor));
        int g = (int) ((Color.green(color) * (1 - factor) + 255 * factor));
        int b = (int) ((Color.blue(color) * (1 - factor) + 255 * factor));
        return Color.rgb(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }
    
    private int darkenColor(int color, float factor) {
        int r = (int) (Color.red(color) * (1 - factor));
        int g = (int) (Color.green(color) * (1 - factor));
        int b = (int) (Color.blue(color) * (1 - factor));
        return Color.rgb(r, g, b);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (bounceAnimator != null) bounceAnimator.cancel();
        if (waveAnimator != null) waveAnimator.cancel();
        if (eyeAnimator != null) eyeAnimator.cancel();
        if (blinkAnimator != null) blinkAnimator.cancel();
        if (breatheAnimator != null) breatheAnimator.cancel();
        if (entranceAnimator != null) entranceAnimator.cancel();
    }
}
