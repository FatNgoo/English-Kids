package com.edu.english.magicmelody.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * CameraOverlayView - Star-shaped frame overlay for camera preview
 * Used in ConcertBossActivity for fun selfie recording
 */
public class CameraOverlayView extends View {
    
    private Paint framePaint;
    private Paint glowPaint;
    private Paint starPaint;
    private Path starPath;
    private Path framePath;
    
    private int frameColor = 0xFFFFD700; // Gold
    private float glowRadius = 20f;
    private boolean isRecording = false;
    
    public CameraOverlayView(Context context) {
        super(context);
        init();
    }
    
    public CameraOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CameraOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(8f);
        framePaint.setColor(frameColor);
        
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(16f);
        glowPaint.setColor(0x60FFD700);
        
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setColor(frameColor);
        
        starPath = new Path();
        framePath = new Path();
        
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
    
    public void setFrameColor(int color) {
        this.frameColor = color;
        framePaint.setColor(color);
        starPaint.setColor(color);
        glowPaint.setColor((color & 0x00FFFFFF) | 0x60000000);
        invalidate();
    }
    
    public void setRecording(boolean recording) {
        this.isRecording = recording;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) return;
        
        float centerX = width / 2f;
        float centerY = height / 2f;
        float size = Math.min(width, height) * 0.4f;
        
        // Draw glow effect
        if (isRecording) {
            // Pulsing glow when recording
            float pulse = (float) (Math.sin(System.currentTimeMillis() / 200.0) * 0.5 + 0.5);
            glowPaint.setAlpha((int) (96 + pulse * 80));
            glowPaint.setColor(0x80FF4444); // Red glow when recording
        }
        
        // Draw outer decorative frame
        drawDecorativeFrame(canvas, centerX, centerY, size);
        
        // Draw corner stars
        drawCornerStars(canvas, width, height);
        
        // Draw recording indicator
        if (isRecording) {
            drawRecordingIndicator(canvas, width, height);
        }
    }
    
    private void drawDecorativeFrame(Canvas canvas, float centerX, float centerY, float size) {
        // Draw rounded rectangle frame
        float left = centerX - size;
        float top = centerY - size;
        float right = centerX + size;
        float bottom = centerY + size;
        
        RectF frameRect = new RectF(left, top, right, bottom);
        
        // Glow
        canvas.drawRoundRect(frameRect, 30, 30, glowPaint);
        
        // Main frame
        canvas.drawRoundRect(frameRect, 30, 30, framePaint);
        
        // Corner decorations (small stars)
        float starSize = 20f;
        drawStar(canvas, left - 10, top - 10, starSize);
        drawStar(canvas, right + 10, top - 10, starSize);
        drawStar(canvas, left - 10, bottom + 10, starSize);
        drawStar(canvas, right + 10, bottom + 10, starSize);
    }
    
    private void drawCornerStars(Canvas canvas, int width, int height) {
        float margin = 40f;
        float starSize = 30f;
        
        // Top left
        drawStar(canvas, margin, margin, starSize);
        // Top right
        drawStar(canvas, width - margin, margin, starSize);
        // Bottom left
        drawStar(canvas, margin, height - margin, starSize);
        // Bottom right
        drawStar(canvas, width - margin, height - margin, starSize);
    }
    
    private void drawStar(Canvas canvas, float cx, float cy, float size) {
        starPath.reset();
        
        int numPoints = 5;
        float outerRadius = size;
        float innerRadius = size * 0.4f;
        
        for (int i = 0; i < numPoints * 2; i++) {
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double angle = Math.PI * i / numPoints - Math.PI / 2;
            float x = (float) (cx + radius * Math.cos(angle));
            float y = (float) (cy + radius * Math.sin(angle));
            
            if (i == 0) {
                starPath.moveTo(x, y);
            } else {
                starPath.lineTo(x, y);
            }
        }
        
        starPath.close();
        canvas.drawPath(starPath, starPaint);
    }
    
    private void drawRecordingIndicator(Canvas canvas, int width, int height) {
        // Recording dot
        float dotRadius = 15f;
        float dotX = width - 50;
        float dotY = 50;
        
        // Pulsing effect
        float pulse = (float) (Math.sin(System.currentTimeMillis() / 300.0) * 0.3 + 0.7);
        
        Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(0xFFFF4444);
        dotPaint.setAlpha((int) (255 * pulse));
        
        canvas.drawCircle(dotX, dotY, dotRadius * pulse, dotPaint);
        
        // REC text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFF4444);
        textPaint.setTextSize(24f);
        textPaint.setFakeBoldText(true);
        canvas.drawText("REC", dotX - 60, dotY + 8, textPaint);
        
        // Request redraw for animation
        postInvalidateDelayed(50);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isRecording = false;
    }
}
