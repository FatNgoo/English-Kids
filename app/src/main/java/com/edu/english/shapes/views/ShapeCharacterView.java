package com.edu.english.shapes.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

import com.edu.english.shapes.models.ShapeType;

/**
 * Animated shape character view with cute expressions
 */
public class ShapeCharacterView extends View {

    private ShapeType shapeType = ShapeType.SQUARE;
    private Paint shapePaint;
    private Paint eyePaint;
    private Paint eyeWhitePaint;
    private Paint mouthPaint;
    private Paint blushPaint;
    private Paint shadowPaint;

    private float bounceOffset = 0f;
    private float waveAngle = 0f;
    private float blinkProgress = 1f;
    private float scale = 1f;
    
    private boolean isAnimating = false;
    private ValueAnimator bounceAnimator;
    private ValueAnimator waveAnimator;

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
        shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shapePaint.setStyle(Paint.Style.FILL);

        eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.parseColor("#2C3E50"));
        eyePaint.setStyle(Paint.Style.FILL);

        eyeWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeWhitePaint.setColor(Color.WHITE);
        eyeWhitePaint.setStyle(Paint.Style.FILL);

        mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthPaint.setColor(Color.parseColor("#E74C3C"));
        mouthPaint.setStyle(Paint.Style.STROKE);
        mouthPaint.setStrokeWidth(8f);
        mouthPaint.setStrokeCap(Paint.Cap.ROUND);

        blushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blushPaint.setColor(Color.parseColor("#40FF69B4"));
        blushPaint.setStyle(Paint.Style.FILL);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.parseColor("#20000000"));
        shadowPaint.setStyle(Paint.Style.FILL);

        setClickable(true);
    }

    public void setShapeType(ShapeType type) {
        this.shapeType = type;
        updatePaintColors();
        invalidate();
    }

    private void updatePaintColors() {
        int color = shapeType.getColor();
        int darkerColor = adjustBrightness(color, 0.8f);
        
        shapePaint.setShader(new LinearGradient(
                0, 0, getWidth(), getHeight(),
                color, darkerColor, Shader.TileMode.CLAMP
        ));
    }

    private int adjustBrightness(int color, float factor) {
        int r = Math.min(255, (int) (Color.red(color) * factor));
        int g = Math.min(255, (int) (Color.green(color) * factor));
        int b = Math.min(255, (int) (Color.blue(color) * factor));
        return Color.rgb(r, g, b);
    }

    public void playEntranceAnimation() {
        setScaleX(0f);
        setScaleY(0f);
        setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);

        AnimatorSet entranceSet = new AnimatorSet();
        entranceSet.playTogether(scaleX, scaleY, alpha);
        entranceSet.setDuration(600);
        entranceSet.setInterpolator(new OvershootInterpolator(1.5f));
        entranceSet.start();

        // Start idle animation after entrance
        postDelayed(this::startIdleAnimation, 700);
    }

    public void startIdleAnimation() {
        if (isAnimating) return;
        isAnimating = true;

        // Bounce animation
        bounceAnimator = ValueAnimator.ofFloat(0f, 1f);
        bounceAnimator.setDuration(1500);
        bounceAnimator.setRepeatCount(ValueAnimator.INFINITE);
        bounceAnimator.setRepeatMode(ValueAnimator.REVERSE);
        bounceAnimator.addUpdateListener(animation -> {
            bounceOffset = (float) animation.getAnimatedValue() * 20f;
            invalidate();
        });
        bounceAnimator.start();

        // Periodic blink
        scheduleBlink();
    }

    private void scheduleBlink() {
        postDelayed(() -> {
            if (isAnimating) {
                performBlink();
                scheduleBlink();
            }
        }, 3000 + (long) (Math.random() * 2000));
    }

    private void performBlink() {
        ValueAnimator blinkAnim = ValueAnimator.ofFloat(1f, 0.1f, 1f);
        blinkAnim.setDuration(200);
        blinkAnim.addUpdateListener(animation -> {
            blinkProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        blinkAnim.start();
    }

    public void playWaveAnimation() {
        if (waveAnimator != null && waveAnimator.isRunning()) {
            waveAnimator.cancel();
        }

        waveAnimator = ValueAnimator.ofFloat(0f, 30f, -20f, 15f, -10f, 0f);
        waveAnimator.setDuration(1000);
        waveAnimator.addUpdateListener(animation -> {
            waveAngle = (float) animation.getAnimatedValue();
            invalidate();
        });
        waveAnimator.start();
    }

    public void playJumpAnimation() {
        ObjectAnimator jump = ObjectAnimator.ofFloat(this, "translationY", 0f, -80f, 0f);
        jump.setDuration(500);
        jump.setInterpolator(new BounceInterpolator());
        jump.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updatePaintColors();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f + bounceOffset;
        float size = Math.min(width, height) * 0.35f;

        // Save canvas state
        canvas.save();
        canvas.rotate(waveAngle, centerX, centerY);

        // Draw shadow
        canvas.drawOval(
                centerX - size * 0.8f,
                height - 40,
                centerX + size * 0.8f,
                height - 20,
                shadowPaint
        );

        // Draw shape based on type
        drawShape(canvas, centerX, centerY, size);

        // Draw face
        drawFace(canvas, centerX, centerY, size);

        canvas.restore();
    }

    private void drawShape(Canvas canvas, float cx, float cy, float size) {
        Path path = new Path();

        switch (shapeType) {
            case SQUARE:
                canvas.drawRoundRect(
                        cx - size, cy - size, cx + size, cy + size,
                        size * 0.2f, size * 0.2f, shapePaint
                );
                break;

            case CIRCLE:
                canvas.drawCircle(cx, cy, size, shapePaint);
                break;

            case TRIANGLE:
                path.moveTo(cx, cy - size);
                path.lineTo(cx + size, cy + size * 0.7f);
                path.lineTo(cx - size, cy + size * 0.7f);
                path.close();
                canvas.drawPath(path, shapePaint);
                break;

            case RECTANGLE:
                canvas.drawRoundRect(
                        cx - size * 1.3f, cy - size * 0.8f,
                        cx + size * 1.3f, cy + size * 0.8f,
                        size * 0.15f, size * 0.15f, shapePaint
                );
                break;

            case OVAL:
                RectF ovalRect = new RectF(cx - size * 0.7f, cy - size, cx + size * 0.7f, cy + size);
                canvas.drawOval(ovalRect, shapePaint);
                break;

            case STAR:
                drawStar(canvas, cx, cy, size);
                break;
        }
    }

    private void drawStar(Canvas canvas, float cx, float cy, float size) {
        Path path = new Path();
        float outerRadius = size;
        float innerRadius = size * 0.4f;
        int points = 5;

        path.moveTo(cx, cy - outerRadius);

        for (int i = 0; i < points * 2; i++) {
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;
            float angle = (float) (Math.PI / 2 + i * Math.PI / points);
            float x = cx + (float) (radius * Math.cos(angle));
            float y = cy - (float) (radius * Math.sin(angle));
            path.lineTo(x, y);
        }
        path.close();
        canvas.drawPath(path, shapePaint);
    }

    private void drawFace(Canvas canvas, float cx, float cy, float size) {
        float eyeOffsetX = size * 0.3f;
        float eyeOffsetY = size * 0.1f;
        float eyeSize = size * 0.15f;

        // Adjust face position for triangle
        if (shapeType == ShapeType.TRIANGLE) {
            cy += size * 0.1f;
        }

        // Draw eye whites
        canvas.drawCircle(cx - eyeOffsetX, cy - eyeOffsetY, eyeSize * 1.3f, eyeWhitePaint);
        canvas.drawCircle(cx + eyeOffsetX, cy - eyeOffsetY, eyeSize * 1.3f, eyeWhitePaint);

        // Draw pupils (with blink effect)
        float pupilHeight = eyeSize * blinkProgress;
        canvas.drawOval(
                cx - eyeOffsetX - eyeSize * 0.7f,
                cy - eyeOffsetY - pupilHeight,
                cx - eyeOffsetX + eyeSize * 0.7f,
                cy - eyeOffsetY + pupilHeight,
                eyePaint
        );
        canvas.drawOval(
                cx + eyeOffsetX - eyeSize * 0.7f,
                cy - eyeOffsetY - pupilHeight,
                cx + eyeOffsetX + eyeSize * 0.7f,
                cy - eyeOffsetY + pupilHeight,
                eyePaint
        );

        // Eye shine
        Paint shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shinePaint.setColor(Color.WHITE);
        canvas.drawCircle(cx - eyeOffsetX + 3, cy - eyeOffsetY - 3, eyeSize * 0.3f, shinePaint);
        canvas.drawCircle(cx + eyeOffsetX + 3, cy - eyeOffsetY - 3, eyeSize * 0.3f, shinePaint);

        // Draw blush
        float blushY = cy + size * 0.15f;
        canvas.drawOval(
                cx - eyeOffsetX * 1.8f - size * 0.15f,
                blushY - size * 0.08f,
                cx - eyeOffsetX * 1.8f + size * 0.15f,
                blushY + size * 0.08f,
                blushPaint
        );
        canvas.drawOval(
                cx + eyeOffsetX * 1.8f - size * 0.15f,
                blushY - size * 0.08f,
                cx + eyeOffsetX * 1.8f + size * 0.15f,
                blushY + size * 0.08f,
                blushPaint
        );

        // Draw smile
        float mouthY = cy + size * 0.35f;
        Path mouthPath = new Path();
        mouthPath.moveTo(cx - size * 0.25f, mouthY);
        mouthPath.quadTo(cx, mouthY + size * 0.2f, cx + size * 0.25f, mouthY);
        canvas.drawPath(mouthPath, mouthPaint);
    }

    public void stopAnimations() {
        isAnimating = false;
        if (bounceAnimator != null) {
            bounceAnimator.cancel();
        }
        if (waveAnimator != null) {
            waveAnimator.cancel();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimations();
    }
}
