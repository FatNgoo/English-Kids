package com.edu.english.shapes.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.edu.english.shapes.models.ShapeType;
import com.edu.english.shapes.utils.ShapeAudioManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom view for tracing shapes - Kids trace along the shape border
 * and the traced portion fills with color as they go
 */
public class ShapeTraceView extends View {

    private static final float TRACE_TOLERANCE = 120f;  // Very forgiving for kids
    private static final float START_TOLERANCE = 150f;  // Even more forgiving at start
    private static final int CHECKPOINT_COUNT = 4;

    private ShapeType shapeType = ShapeType.SQUARE;
    private Path shapePath;
    private Paint outlinePaint;      // Light outline for untraced part
    private Paint tracedPaint;       // Colored traced portion
    private Paint glowPaint;         // Glow effect
    private Paint fillPaint;         // Final fill
    private Paint startPointPaint;   // Start point indicator
    private Paint sparkPaint;        // Sparkles

    private PathMeasure pathMeasure;
    private float pathLength;
    private float tracedLength = 0f;
    private float[] pathPos = new float[2];
    private float[] pathTan = new float[2];

    private PointF startPoint;       // Where tracing should begin
    private List<PointF> checkpoints;
    private int checkpointsReached = 0;
    private boolean isTracing = false;
    private boolean isCompleted = false;
    private boolean hasStarted = false;

    private List<Sparkle> sparkles;
    private Random random = new Random();

    private float glowPhase = 0f;
    private float startPulse = 0f;
    private ValueAnimator glowAnimator;
    private ValueAnimator startPulseAnimator;
    private OnTraceCompleteListener listener;

    private ShapeAudioManager audioManager;

    public interface OnTraceCompleteListener {
        void onCheckpointReached(int checkpoint);
        void onTraceComplete();
    }

    public ShapeTraceView(Context context) {
        super(context);
        init();
    }

    public ShapeTraceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShapeTraceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        sparkles = new ArrayList<>();
        checkpoints = new ArrayList<>();

        // Light outline for untraced portion
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(20f);
        outlinePaint.setColor(Color.parseColor("#B3E5FC")); // Light blue

        // Glow paint behind the outline
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(40f);
        glowPaint.setColor(Color.parseColor("#4FC3F7"));

        // Traced portion - thicker, colored
        tracedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tracedPaint.setStyle(Paint.Style.STROKE);
        tracedPaint.setStrokeWidth(28f);
        tracedPaint.setStrokeCap(Paint.Cap.ROUND);
        tracedPaint.setStrokeJoin(Paint.Join.ROUND);

        // Fill paint for completion
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        // Start point indicator
        startPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        startPointPaint.setStyle(Paint.Style.FILL);
        startPointPaint.setColor(Color.parseColor("#4CAF50")); // Green

        // Sparkle paint
        sparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sparkPaint.setStyle(Paint.Style.FILL);

        shapePath = new Path();

        // Start animations
        startGlowAnimation();
        startStartPointAnimation();

        audioManager = ShapeAudioManager.getInstance(getContext());
    }

    private void startGlowAnimation() {
        glowAnimator = ValueAnimator.ofFloat(0f, 1f);
        glowAnimator.setDuration(1500);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.addUpdateListener(animation -> {
            glowPhase = (float) animation.getAnimatedValue();
            if (!isCompleted) {
                invalidate();
            }
        });
        glowAnimator.start();
    }

    private void startStartPointAnimation() {
        startPulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        startPulseAnimator.setDuration(800);
        startPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        startPulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        startPulseAnimator.addUpdateListener(animation -> {
            startPulse = (float) animation.getAnimatedValue();
            if (!hasStarted && !isCompleted) {
                invalidate();
            }
        });
        startPulseAnimator.start();
    }

    public void setShapeType(ShapeType type) {
        this.shapeType = type;
        reset();
        updateShapePath();
        invalidate();
    }

    public void setOnTraceCompleteListener(OnTraceCompleteListener listener) {
        this.listener = listener;
    }

    private void updateShapePath() {
        shapePath.reset();
        checkpoints.clear();

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float centerX = width / 2f;
        float centerY = height / 2f;
        float size = Math.min(width, height) * 0.35f;

        switch (shapeType) {
            case SQUARE:
                createSquarePath(centerX, centerY, size);
                break;
            case CIRCLE:
                createCirclePath(centerX, centerY, size);
                break;
            case TRIANGLE:
                createTrianglePath(centerX, centerY, size);
                break;
            case RECTANGLE:
                createRectanglePath(centerX, centerY, size);
                break;
            case OVAL:
                createOvalPath(centerX, centerY, size);
                break;
            case STAR:
                createStarPath(centerX, centerY, size);
                break;
        }

        pathMeasure = new PathMeasure(shapePath, true);
        pathLength = pathMeasure.getLength();

        // Get start point (position 0 on the path)
        pathMeasure.getPosTan(0, pathPos, pathTan);
        startPoint = new PointF(pathPos[0], pathPos[1]);

        // Create checkpoints along the path
        for (int i = 1; i <= CHECKPOINT_COUNT; i++) {
            float pos = (pathLength / CHECKPOINT_COUNT) * i;
            pathMeasure.getPosTan(pos % pathLength, pathPos, pathTan);
            checkpoints.add(new PointF(pathPos[0], pathPos[1]));
        }

        updatePaintColors();
    }

    private void createSquarePath(float cx, float cy, float size) {
        float left = cx - size;
        float top = cy - size;
        float right = cx + size;
        float bottom = cy + size;
        
        // Start from top-left corner
        shapePath.moveTo(left, top);
        shapePath.lineTo(right, top);
        shapePath.lineTo(right, bottom);
        shapePath.lineTo(left, bottom);
        shapePath.close();
    }

    private void createCirclePath(float cx, float cy, float size) {
        RectF rect = new RectF(cx - size, cy - size, cx + size, cy + size);
        shapePath.addOval(rect, Path.Direction.CW);
    }

    private void createTrianglePath(float cx, float cy, float size) {
        // Start from top point
        shapePath.moveTo(cx, cy - size);
        shapePath.lineTo(cx + size, cy + size * 0.7f);
        shapePath.lineTo(cx - size, cy + size * 0.7f);
        shapePath.close();
    }

    private void createRectanglePath(float cx, float cy, float size) {
        float width = size * 1.5f;
        float height = size;
        
        shapePath.moveTo(cx - width, cy - height);
        shapePath.lineTo(cx + width, cy - height);
        shapePath.lineTo(cx + width, cy + height);
        shapePath.lineTo(cx - width, cy + height);
        shapePath.close();
    }

    private void createOvalPath(float cx, float cy, float size) {
        RectF rect = new RectF(cx - size * 0.7f, cy - size, cx + size * 0.7f, cy + size);
        shapePath.addOval(rect, Path.Direction.CW);
    }

    private void createStarPath(float cx, float cy, float size) {
        float outerRadius = size;
        float innerRadius = size * 0.4f;
        int points = 5;
        
        // Start from top point
        shapePath.moveTo(cx, cy - outerRadius);
        
        for (int i = 1; i < points * 2; i++) {
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;
            float angle = (float) (-Math.PI / 2 + i * Math.PI / points);
            float x = cx + (float) (radius * Math.cos(angle));
            float y = cy + (float) (radius * Math.sin(angle));
            shapePath.lineTo(x, y);
        }
        shapePath.close();
    }

    private void updatePaintColors() {
        int color = shapeType.getColor();
        
        // Traced paint with gradient
        tracedPaint.setShader(new LinearGradient(
                0, 0, getWidth(), getHeight(),
                color, adjustBrightness(color, 1.2f), Shader.TileMode.CLAMP
        ));
        
        // Fill paint with gradient
        fillPaint.setShader(new LinearGradient(
                0, 0, getWidth(), getHeight(),
                color, adjustBrightness(color, 1.3f), Shader.TileMode.CLAMP
        ));
        
        glowPaint.setColor(adjustAlpha(color, 0.4f));
    }

    private int adjustBrightness(int color, float factor) {
        int r = Math.min(255, (int) (Color.red(color) * factor));
        int g = Math.min(255, (int) (Color.green(color) * factor));
        int b = Math.min(255, (int) (Color.blue(color) * factor));
        return Color.rgb(r, g, b);
    }

    private int adjustAlpha(int color, float factor) {
        return Color.argb((int) (255 * factor), Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateShapePath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (shapePath.isEmpty()) return;

        if (isCompleted) {
            // Draw filled shape with thick border
            canvas.drawPath(shapePath, fillPaint);
            
            // Draw border on top
            Paint borderPaint = new Paint(tracedPaint);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(30f);
            canvas.drawPath(shapePath, borderPaint);
        } else {
            // Draw glow behind
            float glowAlpha = 0.2f + 0.3f * glowPhase;
            glowPaint.setAlpha((int) (255 * glowAlpha));
            canvas.drawPath(shapePath, glowPaint);
            
            // Draw full outline (untraced portion) - light color
            canvas.drawPath(shapePath, outlinePaint);

            // Draw traced portion on top of outline
            if (tracedLength > 0 && pathMeasure != null) {
                Path tracedSegment = new Path();
                pathMeasure.getSegment(0, tracedLength, tracedSegment, true);
                canvas.drawPath(tracedSegment, tracedPaint);
            }

            // Draw checkpoints
            drawCheckpoints(canvas);

            // Draw start point with pulsing animation (if not started yet)
            if (!hasStarted && startPoint != null) {
                drawStartPoint(canvas);
            }
            
            // Draw current tracing position indicator
            if (hasStarted && tracedLength > 0 && tracedLength < pathLength) {
                drawCurrentPosition(canvas);
            }
        }

        // Draw sparkles
        for (Sparkle sparkle : sparkles) {
            sparkPaint.setColor(sparkle.color);
            sparkPaint.setAlpha((int) (255 * sparkle.alpha));
            canvas.drawCircle(sparkle.x, sparkle.y, sparkle.size, sparkPaint);
        }
    }

    private void drawCheckpoints(Canvas canvas) {
        for (int i = 0; i < checkpoints.size(); i++) {
            PointF cp = checkpoints.get(i);
            Paint cpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            
            if (i < checkpointsReached) {
                // Reached checkpoint - green with checkmark effect
                cpPaint.setColor(Color.parseColor("#4CAF50"));
                canvas.drawCircle(cp.x, cp.y, 18f, cpPaint);
                
                // White inner circle
                cpPaint.setColor(Color.WHITE);
                canvas.drawCircle(cp.x, cp.y, 8f, cpPaint);
            } else {
                // Upcoming checkpoint - yellow pulsing
                float pulse = 14f + 4f * (float) Math.sin(glowPhase * Math.PI * 2);
                cpPaint.setColor(Color.parseColor("#FFEB3B"));
                canvas.drawCircle(cp.x, cp.y, pulse, cpPaint);
                
                // White border
                cpPaint.setStyle(Paint.Style.STROKE);
                cpPaint.setStrokeWidth(3f);
                cpPaint.setColor(Color.WHITE);
                canvas.drawCircle(cp.x, cp.y, pulse, cpPaint);
            }
        }
    }

    private void drawStartPoint(Canvas canvas) {
        if (startPoint == null) return;
        
        // Outer pulsing ring
        float outerSize = 35f + 10f * startPulse;
        Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(4f);
        ringPaint.setColor(Color.parseColor("#4CAF50"));
        ringPaint.setAlpha((int) (255 * (1f - startPulse * 0.5f)));
        canvas.drawCircle(startPoint.x, startPoint.y, outerSize, ringPaint);
        
        // Inner solid circle
        float innerSize = 22f + 5f * startPulse;
        startPointPaint.setColor(Color.parseColor("#4CAF50"));
        canvas.drawCircle(startPoint.x, startPoint.y, innerSize, startPointPaint);
        
        // White arrow/finger indicator
        Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.WHITE);
        arrowPaint.setTextSize(24f);
        arrowPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("START", startPoint.x, startPoint.y + 8f, arrowPaint);
    }

    private void drawCurrentPosition(Canvas canvas) {
        pathMeasure.getPosTan(tracedLength, pathPos, pathTan);
        
        // Draw finger position indicator
        Paint posPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        posPaint.setColor(shapeType.getColor());
        posPaint.setStyle(Paint.Style.FILL);
        
        float size = 20f + 5f * glowPhase;
        canvas.drawCircle(pathPos[0], pathPos[1], size, posPaint);
        
        // White inner
        posPaint.setColor(Color.WHITE);
        canvas.drawCircle(pathPos[0], pathPos[1], size * 0.5f, posPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isCompleted) return true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                handleTouchMove(x, y);
                break;

            case MotionEvent.ACTION_UP:
                handleTouchUp();
                break;
        }

        invalidate();
        return true;
    }

    private void handleTouchDown(float x, float y) {
        if (!hasStarted) {
            // Must start near the start point
            if (startPoint != null) {
                float dist = distance(x, y, startPoint.x, startPoint.y);
                if (dist < START_TOLERANCE) {
                    hasStarted = true;
                    isTracing = true;
                    tracedLength = 0;
                    addSparkles(startPoint.x, startPoint.y, 8);
                    audioManager.playSound(ShapeAudioManager.SOUND_CLICK);
                }
            }
        } else {
            // Resume tracing - check if near current position
            pathMeasure.getPosTan(tracedLength, pathPos, pathTan);
            float dist = distance(x, y, pathPos[0], pathPos[1]);
            if (dist < TRACE_TOLERANCE) {
                isTracing = true;
            }
        }
    }

    private void handleTouchMove(float x, float y) {
        if (!isTracing || !hasStarted) return;

        // Find the best match point ahead on the path
        float bestLength = tracedLength;
        float bestDist = Float.MAX_VALUE;
        
        // Search ahead on the path (don't allow going backwards)
        float searchEnd = Math.min(tracedLength + 200f, pathLength);
        for (float testLen = tracedLength; testLen <= searchEnd; testLen += 3f) {
            pathMeasure.getPosTan(testLen, pathPos, pathTan);
            float dist = distance(x, y, pathPos[0], pathPos[1]);
            
            if (dist < bestDist) {
                bestDist = dist;
                bestLength = testLen;
            }
        }

        // Update traced length if touch is close enough to path
        if (bestDist < TRACE_TOLERANCE && bestLength > tracedLength) {
            float previousLength = tracedLength;
            tracedLength = bestLength;
            
            // Add sparkles along the traced portion
            if (tracedLength - previousLength > 10f) {
                pathMeasure.getPosTan(tracedLength, pathPos, pathTan);
                addSparkles(pathPos[0], pathPos[1], 2);
            }
            
            // Check checkpoints
            checkCheckpoints();
            
            // Check completion
            if (tracedLength >= pathLength * 0.95f) {
                tracedLength = pathLength;
                completeTracing();
            }
        } else if (bestDist >= TRACE_TOLERANCE * 1.5f) {
            // Touch is too far from path - pause tracing but don't reset
            isTracing = false;
        }
    }

    private void handleTouchUp() {
        isTracing = false;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private void checkCheckpoints() {
        float checkpointDistance = pathLength / CHECKPOINT_COUNT;
        
        int newCheckpointsReached = (int) (tracedLength / checkpointDistance);
        newCheckpointsReached = Math.min(newCheckpointsReached, CHECKPOINT_COUNT);
        
        if (newCheckpointsReached > checkpointsReached) {
            for (int i = checkpointsReached; i < newCheckpointsReached; i++) {
                PointF cp = checkpoints.get(i);
                addSparkles(cp.x, cp.y, 12);
                audioManager.playSound(ShapeAudioManager.SOUND_POP);
                
                if (i == 2) {
                    // 3rd checkpoint - "Wow!" moment
                    audioManager.playSound(ShapeAudioManager.SOUND_WOW);
                    addSparkles(cp.x, cp.y, 25);
                }
                
                if (listener != null) {
                    listener.onCheckpointReached(i + 1);
                }
            }
            checkpointsReached = newCheckpointsReached;
        }
    }

    private void completeTracing() {
        isCompleted = true;
        isTracing = false;
        
        performCompletionAnimation();
        audioManager.playSound(ShapeAudioManager.SOUND_SUCCESS);
        
        if (listener != null) {
            listener.onTraceComplete();
        }
    }

    private void performCompletionAnimation() {
        // Stop animations
        if (glowAnimator != null) glowAnimator.cancel();
        if (startPulseAnimator != null) startPulseAnimator.cancel();

        // Bounce animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.15f, 1f);
        
        AnimatorSet bounceSet = new AnimatorSet();
        bounceSet.playTogether(scaleX, scaleY);
        bounceSet.setDuration(600);
        bounceSet.setInterpolator(new OvershootInterpolator());
        bounceSet.start();

        // Celebration sparkles around the shape
        int sparkleCount = 40;
        for (int i = 0; i < sparkleCount; i++) {
            float angle = (float) (i * 2 * Math.PI / sparkleCount);
            float distance = 80f + random.nextFloat() * 60f;
            float sx = getWidth() / 2f + (float) Math.cos(angle) * distance;
            float sy = getHeight() / 2f + (float) Math.sin(angle) * distance;
            
            // Delay sparkles for wave effect
            final float fx = sx, fy = sy;
            postDelayed(() -> addSparkles(fx, fy, 3), i * 30L);
        }

        invalidate();
    }

    private void addSparkles(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            Sparkle sparkle = new Sparkle();
            sparkle.x = x + random.nextFloat() * 50 - 25;
            sparkle.y = y + random.nextFloat() * 50 - 25;
            sparkle.size = random.nextFloat() * 12 + 4;
            sparkle.alpha = 1f;
            sparkle.color = getRandomSparkleColor();
            sparkles.add(sparkle);

            // Animate sparkle
            ValueAnimator fadeAnim = ValueAnimator.ofFloat(1f, 0f);
            fadeAnim.setDuration(1000);
            final Sparkle s = sparkle;
            fadeAnim.addUpdateListener(animation -> {
                s.alpha = (float) animation.getAnimatedValue();
                s.y -= 1.5f;
                s.x += (random.nextFloat() - 0.5f) * 2f;
                invalidate();
            });
            fadeAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    sparkles.remove(s);
                }
            });
            fadeAnim.start();
        }
    }

    private int getRandomSparkleColor() {
        int[] colors = {
                Color.parseColor("#FFD700"),  // Gold
                Color.parseColor("#FF69B4"),  // Pink
                Color.parseColor("#00BFFF"),  // Deep Sky Blue
                Color.parseColor("#7CFC00"),  // Lawn Green
                Color.parseColor("#FF6347"),  // Tomato
                Color.parseColor("#9370DB"),  // Medium Purple
                Color.parseColor("#00FF7F"),  // Spring Green
                Color.parseColor("#FF1493")   // Deep Pink
        };
        return colors[random.nextInt(colors.length)];
    }

    public void reset() {
        isCompleted = false;
        isTracing = false;
        hasStarted = false;
        tracedLength = 0;
        checkpointsReached = 0;
        sparkles.clear();
        
        // Restart animations
        if (glowAnimator == null || !glowAnimator.isRunning()) {
            startGlowAnimation();
        }
        if (startPulseAnimator == null || !startPulseAnimator.isRunning()) {
            startStartPointAnimation();
        }
        
        invalidate();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (glowAnimator != null) glowAnimator.cancel();
        if (startPulseAnimator != null) startPulseAnimator.cancel();
    }

    private static class Sparkle {
        float x, y, size, alpha;
        int color;
    }
}
