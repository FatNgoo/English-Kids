package com.edu.english.shapes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.edu.english.shapes.models.ShapeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Enhanced ShapeTraceView - Interactive tracing with cute face inside shape
 * Matches the reference image with white dotted path, green completion line,
 * large checkpoints at corners, and sparkle effects
 */
public class ShapeTraceView extends View {
    
    // Shape type
    private ShapeType shapeType = ShapeType.SQUARE;
    
    // Paints
    private Paint shapeFillPaint;
    private Paint shapeStrokePaint;
    private Paint dottedPathPaint;
    private Paint tracedPathPaint;
    private Paint checkpointPaint;
    private Paint checkpointInnerPaint;
    private Paint checkpointGlowPaint;
    private Paint sparklePaint;
    private Paint eyePaint;
    private Paint eyeWhitePaint;
    private Paint eyePupilPaint;
    private Paint mouthPaint;
    private Paint blushPaint;
    
    // Paths
    private Path shapePath;
    private Path tracePath;
    private Path tracedSegmentPath;
    
    // Tracing
    private PathMeasure pathMeasure;
    private float pathLength;
    private float tracedLength = 0f;
    private float[] pathPos = new float[2];
    private float[] pathTan = new float[2];
    
    // Checkpoints - larger circles at corners
    private List<PointF> checkpoints = new ArrayList<>();
    private int currentCheckpoint = 0;
    private float checkpointRadius = 25f;
    private float checkpointInnerRadius = 15f;
    
    // Touch tracking
    private float lastTouchX, lastTouchY;
    private boolean isTracing = false;
    private float tracingTolerance = 120f; // Increased for easier tracing for kids
    
    // Sparkles
    private List<Sparkle> sparkles = new ArrayList<>();
    private Random random = new Random();
    
    // Animation
    private float eyeAnimationOffset = 0f;
    private float breatheScale = 1f;
    private ValueAnimator eyeAnimator;
    private ValueAnimator breatheAnimator;
    private ValueAnimator pulseAnimator;
    private float checkpointPulse = 1f;
    
    // State
    private boolean isCompleted = false;
    private float completionProgress = 0f;
    private OnTraceCompleteListener listener;
    
    // Colors based on shape
    private int shapeColor;
    private int shapeLightColor;
    private int tracedColor = Color.parseColor("#4CAF50"); // Green for traced path
    
    public interface OnTraceCompleteListener {
        void onTraceComplete();
        void onProgressUpdate(float progress);
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
        // Shape fill paint - the main colored shape
        shapeFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shapeFillPaint.setStyle(Paint.Style.FILL);
        
        // Shape stroke paint
        shapeStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shapeStrokePaint.setStyle(Paint.Style.STROKE);
        shapeStrokePaint.setStrokeWidth(8f);
        
        // Dotted path paint - white dots to trace
        dottedPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dottedPathPaint.setStyle(Paint.Style.STROKE);
        dottedPathPaint.setColor(Color.WHITE);
        dottedPathPaint.setStrokeWidth(12f);
        dottedPathPaint.setPathEffect(new DashPathEffect(new float[]{15f, 15f}, 0f));
        dottedPathPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Traced path paint - green solid line
        tracedPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tracedPathPaint.setStyle(Paint.Style.STROKE);
        tracedPathPaint.setColor(tracedColor);
        tracedPathPaint.setStrokeWidth(16f);
        tracedPathPaint.setStrokeCap(Paint.Cap.ROUND);
        tracedPathPaint.setStrokeJoin(Paint.Join.ROUND);
        
        // Checkpoint paint - outer ring
        checkpointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkpointPaint.setStyle(Paint.Style.STROKE);
        checkpointPaint.setColor(Color.WHITE);
        checkpointPaint.setStrokeWidth(6f);
        
        // Checkpoint inner paint
        checkpointInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkpointInnerPaint.setStyle(Paint.Style.FILL);
        checkpointInnerPaint.setColor(Color.WHITE);
        
        // Checkpoint glow
        checkpointGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkpointGlowPaint.setStyle(Paint.Style.FILL);
        
        // Sparkle paint
        sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sparklePaint.setStyle(Paint.Style.FILL);
        
        // Eye paints
        eyeWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeWhitePaint.setStyle(Paint.Style.FILL);
        eyeWhitePaint.setColor(Color.WHITE);
        
        eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setStyle(Paint.Style.STROKE);
        eyePaint.setColor(Color.parseColor("#2C3E50"));
        eyePaint.setStrokeWidth(3f);
        
        eyePupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePupilPaint.setStyle(Paint.Style.FILL);
        eyePupilPaint.setColor(Color.parseColor("#2C3E50"));
        
        // Mouth paint
        mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthPaint.setStyle(Paint.Style.FILL);
        mouthPaint.setColor(Color.parseColor("#2C3E50"));
        
        // Blush paint
        blushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blushPaint.setStyle(Paint.Style.FILL);
        blushPaint.setColor(Color.parseColor("#40FF6B6B"));
        
        // Initialize paths
        shapePath = new Path();
        tracePath = new Path();
        tracedSegmentPath = new Path();
        
        // Start animations
        startEyeAnimation();
        startBreatheAnimation();
        startCheckpointPulse();
    }
    
    public void setShapeType(ShapeType type) {
        this.shapeType = type;
        this.shapeColor = type.getColor();
        this.shapeLightColor = lightenColor(shapeColor, 0.3f);
        
        // Update paints with shape color
        shapeFillPaint.setColor(shapeLightColor);
        shapeStrokePaint.setColor(shapeColor);
        
        // Rebuild paths
        post(this::buildShapePath);
    }
    
    public void setOnTraceCompleteListener(OnTraceCompleteListener listener) {
        this.listener = listener;
    }
    
    public void reset() {
        tracedLength = 0f;
        currentCheckpoint = 0;
        isCompleted = false;
        completionProgress = 0f;
        tracedSegmentPath.reset();
        sparkles.clear();
        invalidate();
    }
    
    private void buildShapePath() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;
        
        float centerX = width / 2f;
        float centerY = height / 2f;
        float size = Math.min(width, height) * 0.38f;
        float cornerRadius = size * 0.15f;
        
        shapePath.reset();
        tracePath.reset();
        checkpoints.clear();
        
        switch (shapeType) {
            case SQUARE:
            case RECTANGLE:
                buildRoundedRectPath(centerX, centerY, size, cornerRadius);
                break;
            case CIRCLE:
                buildCirclePath(centerX, centerY, size);
                break;
            case TRIANGLE:
                buildTrianglePath(centerX, centerY, size);
                break;
            case OVAL:
                buildOvalPath(centerX, centerY, size);
                break;
            case STAR:
                buildStarPath(centerX, centerY, size);
                break;
        }
        
        pathMeasure = new PathMeasure(tracePath, true);
        pathLength = pathMeasure.getLength();
        
        invalidate();
    }
    
    private void buildRoundedRectPath(float cx, float cy, float size, float cornerRadius) {
        float halfSize = size;
        float rectWidth = shapeType == ShapeType.RECTANGLE ? size * 1.3f : size;
        
        // Shape fill path (rounded rectangle)
        RectF rect = new RectF(cx - rectWidth, cy - halfSize, cx + rectWidth, cy + halfSize);
        shapePath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
        
        // Trace path (follows the outline)
        tracePath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
        
        // Add checkpoints at corners (clockwise from top-left)
        checkpoints.add(new PointF(cx - rectWidth + cornerRadius, cy - halfSize)); // Top-left
        checkpoints.add(new PointF(cx + rectWidth - cornerRadius, cy - halfSize)); // Top-right
        checkpoints.add(new PointF(cx + rectWidth, cy + halfSize - cornerRadius)); // Right-bottom area
        checkpoints.add(new PointF(cx - rectWidth, cy + halfSize - cornerRadius)); // Left-bottom area
    }
    
    private void buildCirclePath(float cx, float cy, float size) {
        shapePath.addCircle(cx, cy, size, Path.Direction.CW);
        tracePath.addCircle(cx, cy, size, Path.Direction.CW);
        
        // Add checkpoints around circle
        for (int i = 0; i < 4; i++) {
            float angle = (float) (Math.PI / 2 * i - Math.PI / 2);
            float px = cx + (float) Math.cos(angle) * size;
            float py = cy + (float) Math.sin(angle) * size;
            checkpoints.add(new PointF(px, py));
        }
    }
    
    private void buildTrianglePath(float cx, float cy, float size) {
        float height = size * 1.7f;
        float halfBase = size;
        
        // Move slightly down to center visually
        cy += size * 0.1f;
        
        shapePath.moveTo(cx, cy - height / 2);
        shapePath.lineTo(cx + halfBase, cy + height / 2);
        shapePath.lineTo(cx - halfBase, cy + height / 2);
        shapePath.close();
        
        tracePath.moveTo(cx, cy - height / 2);
        tracePath.lineTo(cx + halfBase, cy + height / 2);
        tracePath.lineTo(cx - halfBase, cy + height / 2);
        tracePath.close();
        
        checkpoints.add(new PointF(cx, cy - height / 2)); // Top
        checkpoints.add(new PointF(cx + halfBase, cy + height / 2)); // Bottom-right
        checkpoints.add(new PointF(cx - halfBase, cy + height / 2)); // Bottom-left
    }
    
    private void buildOvalPath(float cx, float cy, float size) {
        RectF oval = new RectF(cx - size * 0.7f, cy - size, cx + size * 0.7f, cy + size);
        shapePath.addOval(oval, Path.Direction.CW);
        tracePath.addOval(oval, Path.Direction.CW);
        
        // Checkpoints at top, right, bottom, left
        checkpoints.add(new PointF(cx, cy - size));
        checkpoints.add(new PointF(cx + size * 0.7f, cy));
        checkpoints.add(new PointF(cx, cy + size));
        checkpoints.add(new PointF(cx - size * 0.7f, cy));
    }
    
    private void buildStarPath(float cx, float cy, float size) {
        int points = 5;
        float outerRadius = size;
        float innerRadius = size * 0.4f;
        
        for (int i = 0; i < points * 2; i++) {
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;
            float angle = (float) (Math.PI / 2 + Math.PI * i / points);
            float px = cx + (float) Math.cos(angle) * radius;
            float py = cy - (float) Math.sin(angle) * radius;
            
            if (i == 0) {
                shapePath.moveTo(px, py);
                tracePath.moveTo(px, py);
            } else {
                shapePath.lineTo(px, py);
                tracePath.lineTo(px, py);
            }
            
            // Add checkpoint at outer points
            if (i % 2 == 0) {
                checkpoints.add(new PointF(px, py));
            }
        }
        shapePath.close();
        tracePath.close();
    }
    
    private void startEyeAnimation() {
        eyeAnimator = ValueAnimator.ofFloat(0f, 1f);
        eyeAnimator.setDuration(3000);
        eyeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        eyeAnimator.setRepeatMode(ValueAnimator.REVERSE);
        eyeAnimator.addUpdateListener(anim -> {
            eyeAnimationOffset = (float) anim.getAnimatedValue();
            invalidate();
        });
        eyeAnimator.start();
    }
    
    private void startBreatheAnimation() {
        breatheAnimator = ValueAnimator.ofFloat(1f, 1.02f);
        breatheAnimator.setDuration(2000);
        breatheAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breatheAnimator.setRepeatMode(ValueAnimator.REVERSE);
        breatheAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        breatheAnimator.addUpdateListener(anim -> {
            breatheScale = (float) anim.getAnimatedValue();
            invalidate();
        });
        breatheAnimator.start();
    }
    
    private void startCheckpointPulse() {
        pulseAnimator = ValueAnimator.ofFloat(0.8f, 1.2f);
        pulseAnimator.setDuration(800);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(anim -> {
            checkpointPulse = (float) anim.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        buildShapePath();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        
        canvas.save();
        canvas.scale(breatheScale, breatheScale, centerX, centerY);
        
        // 1. Draw filled shape with gradient
        drawFilledShape(canvas, centerX, centerY);
        
        // 2. Draw cute face inside shape
        drawCuteFace(canvas, centerX, centerY);
        
        canvas.restore();
        
        // 3. Draw remaining dotted path (white)
        drawDottedPath(canvas);
        
        // 4. Draw traced path (green)
        drawTracedPath(canvas);
        
        // 5. Draw checkpoints
        drawCheckpoints(canvas);
        
        // 6. Draw sparkles
        drawSparkles(canvas);
    }
    
    private void drawFilledShape(Canvas canvas, float cx, float cy) {
        // Create gradient for shape
        float size = Math.min(getWidth(), getHeight()) * 0.38f;
        LinearGradient gradient = new LinearGradient(
            cx - size, cy - size, cx + size, cy + size,
            shapeLightColor, shapeColor,
            Shader.TileMode.CLAMP
        );
        shapeFillPaint.setShader(gradient);
        
        canvas.drawPath(shapePath, shapeFillPaint);
        
        // Draw subtle border
        shapeStrokePaint.setColor(darkenColor(shapeColor, 0.2f));
        canvas.drawPath(shapePath, shapeStrokePaint);
    }
    
    private void drawCuteFace(Canvas canvas, float cx, float cy) {
        float size = Math.min(getWidth(), getHeight()) * 0.38f;
        float eyeSpacing = size * 0.4f;
        float eyeY = cy - size * 0.15f;
        float eyeRadius = size * 0.2f;
        float pupilRadius = eyeRadius * 0.55f;
        
        // Eye offset for looking animation
        float lookOffsetX = (eyeAnimationOffset - 0.5f) * 8f;
        float lookOffsetY = (float) Math.sin(eyeAnimationOffset * Math.PI) * 3f;
        
        // Left eye
        drawEye(canvas, cx - eyeSpacing, eyeY, eyeRadius, pupilRadius, lookOffsetX, lookOffsetY);
        
        // Right eye
        drawEye(canvas, cx + eyeSpacing, eyeY, eyeRadius, pupilRadius, lookOffsetX, lookOffsetY);
        
        // Draw mouth - "O" shape surprised look
        float mouthY = cy + size * 0.25f;
        float mouthRadius = size * 0.12f;
        canvas.drawCircle(cx, mouthY, mouthRadius, mouthPaint);
        
        // Inner mouth (darker)
        Paint innerMouthPaint = new Paint(mouthPaint);
        innerMouthPaint.setColor(Color.parseColor("#1A252F"));
        canvas.drawCircle(cx, mouthY, mouthRadius * 0.7f, innerMouthPaint);
    }
    
    private void drawEye(Canvas canvas, float cx, float cy, float eyeRadius, float pupilRadius, 
                         float lookX, float lookY) {
        // Eye white with slight oval shape
        RectF eyeRect = new RectF(cx - eyeRadius, cy - eyeRadius * 1.1f, 
                                   cx + eyeRadius, cy + eyeRadius * 0.9f);
        canvas.drawOval(eyeRect, eyeWhitePaint);
        
        // Eye outline
        canvas.drawOval(eyeRect, eyePaint);
        
        // Pupil (slightly offset based on look direction)
        float pupilX = cx + lookX;
        float pupilY = cy + lookY;
        canvas.drawCircle(pupilX, pupilY, pupilRadius, eyePupilPaint);
        
        // Eye shine
        Paint shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shinePaint.setColor(Color.WHITE);
        float shineRadius = pupilRadius * 0.3f;
        canvas.drawCircle(pupilX - pupilRadius * 0.3f, pupilY - pupilRadius * 0.3f, 
                          shineRadius, shinePaint);
    }
    
    private void drawDottedPath(Canvas canvas) {
        if (isCompleted) return;
        
        // Draw the portion that hasn't been traced yet
        if (tracedLength < pathLength) {
            Path remainingPath = new Path();
            pathMeasure.getSegment(tracedLength, pathLength, remainingPath, true);
            canvas.drawPath(remainingPath, dottedPathPaint);
        }
    }
    
    private void drawTracedPath(Canvas canvas) {
        if (tracedLength <= 0) return;
        
        // Create gradient effect for traced path
        tracedPathPaint.setColor(tracedColor);
        
        // Add glow effect
        Paint glowPaint = new Paint(tracedPathPaint);
        glowPaint.setStrokeWidth(28f);
        glowPaint.setColor(Color.parseColor("#8000FF00"));
        glowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(15f, 
            android.graphics.BlurMaskFilter.Blur.NORMAL));
        setLayerType(LAYER_TYPE_SOFTWARE, glowPaint);
        
        Path tracedPath = new Path();
        pathMeasure.getSegment(0, tracedLength, tracedPath, true);
        
        canvas.drawPath(tracedPath, glowPaint);
        canvas.drawPath(tracedPath, tracedPathPaint);
    }
    
    private void drawCheckpoints(Canvas canvas) {
        for (int i = 0; i < checkpoints.size(); i++) {
            PointF cp = checkpoints.get(i);
            
            float radius = checkpointRadius;
            float innerRadius = checkpointInnerRadius;
            
            // Current checkpoint pulses
            if (i == currentCheckpoint && !isCompleted) {
                radius *= checkpointPulse;
                innerRadius *= checkpointPulse;
                
                // Glow for current checkpoint
                RadialGradient glow = new RadialGradient(
                    cp.x, cp.y, radius * 1.5f,
                    new int[]{Color.parseColor("#80FFFFFF"), Color.TRANSPARENT},
                    null, Shader.TileMode.CLAMP
                );
                checkpointGlowPaint.setShader(glow);
                canvas.drawCircle(cp.x, cp.y, radius * 1.5f, checkpointGlowPaint);
            }
            
            // Draw checkpoint based on state
            if (i < currentCheckpoint || isCompleted) {
                // Completed checkpoint - filled green
                Paint completedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                completedPaint.setColor(tracedColor);
                completedPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cp.x, cp.y, radius, completedPaint);
                
                // Checkmark
                Paint checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                checkPaint.setColor(Color.WHITE);
                checkPaint.setStrokeWidth(4f);
                checkPaint.setStyle(Paint.Style.STROKE);
                checkPaint.setStrokeCap(Paint.Cap.ROUND);
                
                Path checkPath = new Path();
                checkPath.moveTo(cp.x - radius * 0.35f, cp.y);
                checkPath.lineTo(cp.x - radius * 0.05f, cp.y + radius * 0.3f);
                checkPath.lineTo(cp.x + radius * 0.4f, cp.y - radius * 0.3f);
                canvas.drawPath(checkPath, checkPaint);
            } else {
                // Uncompleted checkpoint - white ring with pink/magenta center
                canvas.drawCircle(cp.x, cp.y, radius, checkpointPaint);
                
                // Inner circle - match background or use contrasting color
                Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                innerPaint.setColor(Color.parseColor("#E91E63")); // Pink center
                innerPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cp.x, cp.y, innerRadius, innerPaint);
            }
        }
    }
    
    private void drawSparkles(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        
        for (int i = sparkles.size() - 1; i >= 0; i--) {
            Sparkle sparkle = sparkles.get(i);
            float age = (currentTime - sparkle.createTime) / (float) sparkle.lifetime;
            
            if (age >= 1f) {
                sparkles.remove(i);
                continue;
            }
            
            float alpha = 1f - age;
            float scale = 0.5f + age * 0.5f;
            
            sparklePaint.setColor(sparkle.color);
            sparklePaint.setAlpha((int) (alpha * 255));
            
            canvas.save();
            canvas.translate(sparkle.x + sparkle.vx * age * 30f, 
                           sparkle.y + sparkle.vy * age * 30f - age * 20f);
            canvas.scale(scale, scale);
            
            // Draw star sparkle
            drawStarSparkle(canvas, 0, 0, sparkle.size);
            
            canvas.restore();
        }
        
        if (!sparkles.isEmpty()) {
            postInvalidateDelayed(16);
        }
    }
    
    private void drawStarSparkle(Canvas canvas, float cx, float cy, float size) {
        Path star = new Path();
        for (int i = 0; i < 8; i++) {
            float radius = (i % 2 == 0) ? size : size * 0.4f;
            float angle = (float) (Math.PI / 4 * i - Math.PI / 2);
            float px = cx + (float) Math.cos(angle) * radius;
            float py = cy + (float) Math.sin(angle) * radius;
            
            if (i == 0) {
                star.moveTo(px, py);
            } else {
                star.lineTo(px, py);
            }
        }
        star.close();
        canvas.drawPath(star, sparklePaint);
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
            case MotionEvent.ACTION_CANCEL:
                isTracing = false;
                break;
        }
        
        return true;
    }
    
    private void handleTouchDown(float x, float y) {
        // Check if touch is near current position on path or first checkpoint
        if (currentCheckpoint == 0 && tracedLength == 0) {
            // Starting - check if near first checkpoint or anywhere on the starting area
            PointF firstCp = checkpoints.get(0);
            float dist = distance(x, y, firstCp.x, firstCp.y);
            if (dist < tracingTolerance * 2f) { // Very forgiving for start point
                isTracing = true;
                lastTouchX = x;
                lastTouchY = y;
                addSparkles(x, y, 3);
            }
        } else {
            // Check if near current traced position - more forgiving
            pathMeasure.getPosTan(tracedLength, pathPos, pathTan);
            float dist = distance(x, y, pathPos[0], pathPos[1]);
            if (dist < tracingTolerance * 1.5f) {
                isTracing = true;
                lastTouchX = x;
                lastTouchY = y;
            }
        }
    }
    
    private void handleTouchMove(float x, float y) {
        if (!isTracing) {
            // Try to resume tracing if touch moves near the current trace point
            pathMeasure.getPosTan(tracedLength, pathPos, pathTan);
            float dist = distance(x, y, pathPos[0], pathPos[1]);
            if (dist < tracingTolerance) {
                isTracing = true;
            }
            return;
        }
        
        // Find the best point on path near touch
        float bestDist = Float.MAX_VALUE;
        float bestLength = tracedLength;
        
        // Search ahead on the path - increased search range
        for (float testLen = tracedLength; testLen <= Math.min(tracedLength + 150f, pathLength); testLen += 3f) {
            pathMeasure.getPosTan(testLen, pathPos, pathTan);
            float dist = distance(x, y, pathPos[0], pathPos[1]);
            
            if (dist < bestDist) {
                bestDist = dist;
                bestLength = testLen;
            }
        }
        
        // If close enough to path, advance traced length
        if (bestDist < tracingTolerance && bestLength > tracedLength) {
            tracedLength = bestLength;
            
            // Add sparkles along the way
            if (random.nextFloat() < 0.3f) {
                addSparkles(x, y, 2);
            }
            
            // Update progress
            completionProgress = tracedLength / pathLength;
            if (listener != null) {
                listener.onProgressUpdate(completionProgress);
            }
            
            // Check checkpoint crossing
            checkCheckpointCrossing();
            
            // Check completion
            if (tracedLength >= pathLength * 0.95f) {
                completeTracing();
            }
            
            invalidate();
        }
        
        lastTouchX = x;
        lastTouchY = y;
    }
    
    private void checkCheckpointCrossing() {
        if (currentCheckpoint >= checkpoints.size()) return;
        
        pathMeasure.getPosTan(tracedLength, pathPos, pathTan);
        PointF cp = checkpoints.get(currentCheckpoint);
        
        float dist = distance(pathPos[0], pathPos[1], cp.x, cp.y);
        if (dist < tracingTolerance) {
            currentCheckpoint++;
            addSparkles(cp.x, cp.y, 8); // Burst of sparkles at checkpoint
            
            // Haptic feedback
            performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
        }
    }
    
    private void completeTracing() {
        isCompleted = true;
        tracedLength = pathLength;
        currentCheckpoint = checkpoints.size();
        
        // Celebration sparkles
        for (PointF cp : checkpoints) {
            addSparkles(cp.x, cp.y, 10);
        }
        
        // Animate completion
        ValueAnimator celebrationAnim = ValueAnimator.ofFloat(0f, 1f);
        celebrationAnim.setDuration(1000);
        celebrationAnim.addUpdateListener(anim -> {
            float val = (float) anim.getAnimatedValue();
            // Add random sparkles during celebration
            if (random.nextFloat() < 0.2f) {
                float randX = getWidth() * random.nextFloat();
                float randY = getHeight() * random.nextFloat();
                addSparkles(randX, randY, 2);
            }
            invalidate();
        });
        celebrationAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) {
                    listener.onTraceComplete();
                }
            }
        });
        celebrationAnim.start();
        
        invalidate();
    }
    
    private void addSparkles(float x, float y, int count) {
        int[] colors = {
            Color.parseColor("#FFD700"), // Gold
            Color.parseColor("#FFF176"), // Light yellow
            Color.parseColor("#FFEB3B"), // Yellow
            Color.parseColor("#76FF03"), // Light green
            Color.parseColor("#00E676"), // Green
            Color.WHITE
        };
        
        for (int i = 0; i < count; i++) {
            Sparkle sparkle = new Sparkle();
            sparkle.x = x + (random.nextFloat() - 0.5f) * 30f;
            sparkle.y = y + (random.nextFloat() - 0.5f) * 30f;
            sparkle.vx = (random.nextFloat() - 0.5f) * 2f;
            sparkle.vy = -random.nextFloat() * 2f;
            sparkle.size = 8f + random.nextFloat() * 12f;
            sparkle.color = colors[random.nextInt(colors.length)];
            sparkle.createTime = System.currentTimeMillis();
            sparkle.lifetime = 600 + random.nextInt(400);
            sparkles.add(sparkle);
        }
    }
    
    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
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
        if (eyeAnimator != null) eyeAnimator.cancel();
        if (breatheAnimator != null) breatheAnimator.cancel();
        if (pulseAnimator != null) pulseAnimator.cancel();
    }
    
    // Sparkle class
    private static class Sparkle {
        float x, y;
        float vx, vy;
        float size;
        int color;
        long createTime;
        int lifetime;
    }
}
