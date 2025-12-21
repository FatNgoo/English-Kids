package com.edu.english.coloralchemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Test Tube game object
 * Draggable container with liquid that can be poured
 */
public class TestTube {
    
    // Position and dimensions
    private float x, y;
    private float width, height;
    private float originalX, originalY;
    
    // Animation
    private float targetX, targetY;
    private float rotation; // Current rotation angle in degrees
    private float targetRotation;
    private float scale;
    private float targetScale;
    
    // State
    private boolean isDragging;
    private boolean isPouring;
    private boolean isAnimating;
    private boolean isEnabled;
    
    // Liquid
    private LiquidSimulation liquid;
    private int originalColor;
    
    // Rendering
    private Paint tubePaint;
    private Paint tubeStrokePaint;
    private Paint tubeShinePaint;
    private Paint capPaint;
    private Paint shadowPaint;
    private Path tubePath;
    private Path capPath;
    
    // Pour animation
    private float pourProgress;
    private float pourTargetX, pourTargetY;
    private Path pourStreamPath;
    private Paint pourPaint;
    
    // Constants
    private static final float TUBE_CORNER_RADIUS = 15f;
    private static final float CAP_HEIGHT = 30f;
    private static final float POUR_TILT_ANGLE = 60f;
    
    public TestTube(float x, float y, float width, float height, int liquidColor) {
        this.x = x;
        this.y = y;
        this.originalX = x;
        this.originalY = y;
        this.targetX = x;
        this.targetY = y;
        this.width = width;
        this.height = height;
        
        this.rotation = 0;
        this.targetRotation = 0;
        this.scale = 1.0f;
        this.targetScale = 1.0f;
        
        this.isDragging = false;
        this.isPouring = false;
        this.isAnimating = false;
        this.isEnabled = true;
        
        this.originalColor = liquidColor;
        
        // Initialize liquid
        liquid = new LiquidSimulation();
        liquid.setColor(liquidColor);
        liquid.setLevel(0.75f);
        
        // Initialize paints
        initPaints();
        
        // Initialize paths
        tubePath = new Path();
        capPath = new Path();
        pourStreamPath = new Path();
    }
    
    private void initPaints() {
        // Glass tube paint
        tubePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tubePaint.setStyle(Paint.Style.FILL);
        tubePaint.setColor(Color.argb(60, 255, 255, 255));
        
        // Tube stroke
        tubeStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tubeStrokePaint.setStyle(Paint.Style.STROKE);
        tubeStrokePaint.setStrokeWidth(3f);
        tubeStrokePaint.setColor(Color.argb(100, 200, 200, 200));
        
        // Glass shine
        tubeShinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tubeShinePaint.setStyle(Paint.Style.FILL);
        
        // Cap paint
        capPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        capPaint.setStyle(Paint.Style.FILL);
        capPaint.setColor(Color.parseColor("#34495E"));
        
        // Shadow paint
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(40, 0, 0, 0));
        
        // Pour stream paint
        pourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pourPaint.setStyle(Paint.Style.FILL);
    }
    
    /**
     * Update liquid container bounds
     */
    private void updateLiquidContainer() {
        float tubeLeft = x - width / 2;
        float tubeTop = y - height / 2 + CAP_HEIGHT;
        float tubeRight = x + width / 2;
        float tubeBottom = y + height / 2;
        
        // Offset for liquid area (inside glass)
        float padding = 5f;
        liquid.setContainer(
            tubeLeft + padding,
            tubeTop + padding,
            tubeRight - padding,
            tubeBottom - TUBE_CORNER_RADIUS - padding,
            TUBE_CORNER_RADIUS
        );
    }
    
    /**
     * Update test tube state
     */
    public void update(float deltaTime) {
        // Smooth position animation
        if (!isDragging) {
            x = EasingFunctions.lerp(x, targetX, deltaTime * 8f);
            y = EasingFunctions.lerp(y, targetY, deltaTime * 8f);
        }
        
        // Smooth rotation animation
        rotation = EasingFunctions.lerp(rotation, targetRotation, deltaTime * 10f);
        
        // Smooth scale animation
        scale = EasingFunctions.lerp(scale, targetScale, deltaTime * 12f);
        
        // Check if still animating
        isAnimating = Math.abs(x - targetX) > 0.5f || 
                     Math.abs(y - targetY) > 0.5f ||
                     Math.abs(rotation - targetRotation) > 0.5f ||
                     Math.abs(scale - targetScale) > 0.01f;
        
        // Update liquid container bounds
        updateLiquidContainer();
        
        // Update liquid simulation
        liquid.update(deltaTime);
        
        // Update pour animation
        if (isPouring) {
            pourProgress += deltaTime * 2f;
            liquid.animateLevelTo(Math.max(0, liquid.getLevel() - deltaTime * 0.4f));
            
            if (liquid.isEmpty()) {
                stopPouring();
            }
        }
    }
    
    /**
     * Update accelerometer values
     */
    public void updateAccelerometer(float accelX, float accelY, float accelZ) {
        liquid.updateAccelerometer(accelX, accelY, accelZ);
    }
    
    /**
     * Draw the test tube
     */
    public void draw(Canvas canvas) {
        canvas.save();
        
        // Apply transformations
        canvas.translate(x, y);
        canvas.rotate(rotation);
        canvas.scale(scale, scale);
        
        // Draw shadow (offset)
        canvas.save();
        canvas.translate(5, 8);
        drawTubeShape(canvas, shadowPaint, null);
        canvas.restore();
        
        // Draw glass tube
        drawTubeShape(canvas, tubePaint, tubeStrokePaint);
        
        // Draw liquid
        canvas.save();
        canvas.translate(-x, -y);
        canvas.translate(x, y);
        
        // Adjust liquid position for rotation
        liquid.setContainer(
            -width / 2 + 5,
            -height / 2 + CAP_HEIGHT + 5,
            width / 2 - 5,
            height / 2 - TUBE_CORNER_RADIUS - 5,
            TUBE_CORNER_RADIUS
        );
        liquid.draw(canvas);
        canvas.restore();
        
        // Draw glass shine effect
        drawGlassShine(canvas);
        
        // Draw cap
        drawCap(canvas);
        
        canvas.restore();
        
        // Draw pour stream if pouring
        if (isPouring && !liquid.isEmpty()) {
            drawPourStream(canvas);
        }
    }
    
    /**
     * Draw tube shape
     */
    private void drawTubeShape(Canvas canvas, Paint fillPaint, Paint strokePaint) {
        tubePath.reset();
        
        float left = -width / 2;
        float top = -height / 2 + CAP_HEIGHT;
        float right = width / 2;
        float bottom = height / 2;
        
        // Draw rounded bottom tube
        RectF tubeRect = new RectF(left, top, right, bottom);
        float[] radii = new float[]{
            5f, 5f,  // Top left
            5f, 5f,  // Top right
            TUBE_CORNER_RADIUS, TUBE_CORNER_RADIUS,  // Bottom right
            TUBE_CORNER_RADIUS, TUBE_CORNER_RADIUS   // Bottom left
        };
        tubePath.addRoundRect(tubeRect, radii, Path.Direction.CW);
        
        if (fillPaint != null) {
            canvas.drawPath(tubePath, fillPaint);
        }
        if (strokePaint != null) {
            canvas.drawPath(tubePath, strokePaint);
        }
    }
    
    /**
     * Draw glass shine effect
     */
    private void drawGlassShine(Canvas canvas) {
        float shineWidth = width * 0.15f;
        float shineLeft = -width / 2 + width * 0.15f;
        float shineTop = -height / 2 + CAP_HEIGHT + 10;
        float shineBottom = height / 2 - 20;
        
        LinearGradient shineGradient = new LinearGradient(
            shineLeft, shineTop,
            shineLeft + shineWidth, shineTop,
            new int[]{
                Color.argb(0, 255, 255, 255),
                Color.argb(80, 255, 255, 255),
                Color.argb(0, 255, 255, 255)
            },
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        tubeShinePaint.setShader(shineGradient);
        
        RectF shineRect = new RectF(shineLeft, shineTop, shineLeft + shineWidth, shineBottom);
        canvas.drawRoundRect(shineRect, shineWidth / 2, shineWidth / 2, tubeShinePaint);
    }
    
    /**
     * Draw tube cap
     */
    private void drawCap(Canvas canvas) {
        float capTop = -height / 2;
        float capBottom = -height / 2 + CAP_HEIGHT;
        float capWidth = width * 0.7f;
        
        // Cap body
        RectF capRect = new RectF(-capWidth / 2, capTop, capWidth / 2, capBottom);
        canvas.drawRoundRect(capRect, 8f, 8f, capPaint);
        
        // Cap lip (wider part at bottom)
        RectF lipRect = new RectF(-width / 2 - 2, capBottom - 8, width / 2 + 2, capBottom + 2);
        canvas.drawRoundRect(lipRect, 4f, 4f, capPaint);
        
        // Cap shine
        Paint capShinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        capShinePaint.setColor(Color.argb(60, 255, 255, 255));
        RectF capShineRect = new RectF(-capWidth / 2 + 5, capTop + 5, -capWidth / 2 + 12, capBottom - 10);
        canvas.drawRoundRect(capShineRect, 3f, 3f, capShinePaint);
    }
    
    /**
     * Draw pour stream
     */
    private void drawPourStream(Canvas canvas) {
        if (pourTargetX == 0 && pourTargetY == 0) return;
        
        // Calculate pour start point (top of tilted tube)
        float startX = x + (float) Math.sin(Math.toRadians(rotation)) * (height / 2 - CAP_HEIGHT);
        float startY = y - (float) Math.cos(Math.toRadians(rotation)) * (height / 2 - CAP_HEIGHT);
        
        // Stream width varies with pour progress
        float streamWidth = 8f + (float) Math.sin(pourProgress * 5) * 2f;
        
        pourPaint.setColor(liquid.getColor());
        pourPaint.setAlpha(220);
        
        // Draw curved stream
        pourStreamPath.reset();
        
        float ctrlX = (startX + pourTargetX) / 2;
        float ctrlY = startY + (pourTargetY - startY) * 0.4f;
        
        // Left edge
        pourStreamPath.moveTo(startX - streamWidth / 2, startY);
        pourStreamPath.quadTo(ctrlX - streamWidth / 3, ctrlY, pourTargetX - streamWidth / 4, pourTargetY);
        
        // Right edge
        pourStreamPath.lineTo(pourTargetX + streamWidth / 4, pourTargetY);
        pourStreamPath.quadTo(ctrlX + streamWidth / 3, ctrlY, startX + streamWidth / 2, startY);
        pourStreamPath.close();
        
        canvas.drawPath(pourStreamPath, pourPaint);
        
        // Add drip effect at end
        float dripSize = 4f + (float) Math.sin(pourProgress * 8) * 2f;
        canvas.drawCircle(pourTargetX, pourTargetY + dripSize, dripSize, pourPaint);
    }
    
    // ==================== Interaction Methods ====================
    
    /**
     * Start dragging
     */
    public void startDrag(float touchX, float touchY) {
        if (!isEnabled) return;
        isDragging = true;
        targetScale = 1.1f;
        x = touchX;
        y = touchY;
    }
    
    /**
     * Update drag position
     */
    public void drag(float touchX, float touchY) {
        if (!isDragging) return;
        x = touchX;
        y = touchY;
    }
    
    /**
     * End dragging
     */
    public void endDrag() {
        isDragging = false;
        targetScale = 1.0f;
    }
    
    /**
     * Return to original position
     */
    public void returnToOrigin() {
        targetX = originalX;
        targetY = originalY;
        targetRotation = 0;
        targetScale = 1.0f;
        isDragging = false;
    }
    
    /**
     * Start pouring animation
     */
    public void startPouring(float targetX, float targetY) {
        if (liquid.isEmpty()) return;
        
        isPouring = true;
        pourProgress = 0;
        pourTargetX = targetX;
        pourTargetY = targetY;
        targetRotation = POUR_TILT_ANGLE;
        liquid.startPour();
    }
    
    /**
     * Stop pouring
     */
    public void stopPouring() {
        isPouring = false;
        liquid.stopPour();
        targetRotation = 0;
    }
    
    /**
     * Refill the tube
     */
    public void refill() {
        liquid.setColor(originalColor);
        liquid.setLevel(0.75f);
    }
    
    /**
     * Check if point is inside tube bounds
     */
    public boolean contains(float touchX, float touchY) {
        float halfWidth = width / 2 * scale;
        float halfHeight = height / 2 * scale;
        return touchX >= x - halfWidth && touchX <= x + halfWidth &&
               touchY >= y - halfHeight && touchY <= y + halfHeight;
    }
    
    // ==================== Getters & Setters ====================
    
    public float getX() { return x; }
    public float getY() { return y; }
    public void setPosition(float x, float y) { 
        this.x = x; 
        this.y = y; 
        this.targetX = x;
        this.targetY = y;
    }
    public void setOriginalPosition(float x, float y) {
        this.originalX = x;
        this.originalY = y;
    }
    
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    
    public boolean isDragging() { return isDragging; }
    public boolean isPouring() { return isPouring; }
    public boolean isAnimating() { return isAnimating; }
    
    public int getColor() { return liquid.getColor(); }
    public float getLiquidLevel() { return liquid.getLevel(); }
    public boolean isEmpty() { return liquid.isEmpty(); }
    
    public void setEnabled(boolean enabled) { this.isEnabled = enabled; }
    public boolean isEnabled() { return isEnabled; }
    
    public LiquidSimulation getLiquid() { return liquid; }
}
