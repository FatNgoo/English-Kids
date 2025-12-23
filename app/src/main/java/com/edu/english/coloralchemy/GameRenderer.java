package com.edu.english.coloralchemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

/**
 * Game Renderer
 * Handles all drawing operations for the Color Alchemy Lab
 */
public class GameRenderer {
    
    // Screen dimensions
    private int screenWidth;
    private int screenHeight;
    
    // Paints
    private Paint backgroundPaint;
    private Paint tablePaint;
    private Paint tableTopPaint;
    private Paint shadowPaint;
    private Paint textPaint;
    private Paint resultTextPaint;
    private Paint hintTextPaint;
    private Paint sliderTrackPaint;
    private Paint sliderHandlePaint;
    private Paint sunPaint;
    private Paint moonPaint;
    private Paint buttonPaint;
    private Paint buttonTextPaint;
    
    // Background gradient colors
    private int[] bgGradientColors;
    
    // Animation
    private float time;
    
    // Result display animation
    private float resultAlpha;
    private float resultScale;
    private float resultTargetAlpha;
    private float resultTargetScale;
    
    public GameRenderer() {
        initPaints();
        time = 0;
        resultAlpha = 0;
        resultScale = 0;
        resultTargetAlpha = 0;
        resultTargetScale = 0;
    }
    
    private void initPaints() {
        // Background - Dark blue gradient like home page
        backgroundPaint = new Paint();
        bgGradientColors = new int[]{
            Color.parseColor("#0D1259"), // Deep blue dark
            Color.parseColor("#1A237E"), // Deep blue
            Color.parseColor("#283593")  // Lighter deep blue
        };
        
        // Table
        tablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tablePaint.setStyle(Paint.Style.FILL);
        tablePaint.setColor(Color.parseColor("#8D6E63")); // Wood brown
        
        tableTopPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tableTopPaint.setStyle(Paint.Style.FILL);
        
        // Shadow
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(30, 0, 0, 0));
        
        // Main text
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(Color.WHITE);
        
        // Result text (large bounce-in)
        resultTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultTextPaint.setTextAlign(Paint.Align.CENTER);
        resultTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        resultTextPaint.setShadowLayer(8, 2, 2, Color.argb(80, 0, 0, 0));
        
        // Hint text
        hintTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintTextPaint.setTextAlign(Paint.Align.CENTER);
        hintTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        hintTextPaint.setColor(Color.parseColor("#B0BEC5"));
        
        // Slider track
        sliderTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sliderTrackPaint.setStyle(Paint.Style.FILL);
        
        // Slider handle
        sliderHandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sliderHandlePaint.setStyle(Paint.Style.FILL);
        sliderHandlePaint.setColor(Color.WHITE);
        sliderHandlePaint.setShadowLayer(6, 0, 2, Color.argb(60, 0, 0, 0));
        
        // Sun icon
        sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sunPaint.setStyle(Paint.Style.FILL);
        sunPaint.setColor(Color.parseColor("#F39C12"));
        
        // Moon icon
        moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        moonPaint.setStyle(Paint.Style.FILL);
        moonPaint.setColor(Color.parseColor("#34495E"));
        
        // Button
        buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setColor(Color.parseColor("#3498DB"));
        
        buttonTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        buttonTextPaint.setColor(Color.WHITE);
    }
    
    /**
     * Set screen dimensions
     */
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        
        // Update gradient shaders
        LinearGradient bgGradient = new LinearGradient(
            0, 0,
            0, height,
            bgGradientColors,
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(bgGradient);
        
        // Table top gradient - Modern dark theme
        LinearGradient tableGradient = new LinearGradient(
            0, height * 0.6f,
            0, height,
            new int[]{
                Color.parseColor("#37474F"),
                Color.parseColor("#263238")
            },
            null,
            Shader.TileMode.CLAMP
        );
        tableTopPaint.setShader(tableGradient);
    }
    
    /**
     * Update renderer state
     */
    public void update(float deltaTime) {
        time += deltaTime;
        
        // Animate result display
        resultAlpha = EasingFunctions.lerp(resultAlpha, resultTargetAlpha, deltaTime * 5f);
        resultScale = EasingFunctions.lerp(resultScale, resultTargetScale, deltaTime * 8f);
    }
    
    /**
     * Draw background
     */
    public void drawBackground(Canvas canvas) {
        // Gradient background
        canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);
        
        // Animated floating particles in background
        drawFloatingParticles(canvas);
    }
    
    /**
     * Draw floating decorative particles
     */
    private void drawFloatingParticles(Canvas canvas) {
        Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        int[] colors = {
            Color.parseColor("#7C4DFF"), // Purple
            Color.parseColor("#448AFF"), // Blue
            Color.parseColor("#00E5FF"), // Cyan
            Color.parseColor("#FFAB40")  // Orange
        };
        
        for (int i = 0; i < 15; i++) {
            float baseX = (i * 137) % screenWidth;
            float baseY = (i * 89) % (screenHeight / 2);
            
            float offsetX = (float) Math.sin(time * 0.5f + i * 0.5f) * 30;
            float offsetY = (float) Math.cos(time * 0.3f + i * 0.3f) * 20;
            
            float x = baseX + offsetX;
            float y = baseY + offsetY;
            float size = 6 + (i % 4) * 3;
            
            particlePaint.setColor(colors[i % colors.length]);
            particlePaint.setAlpha(40 + (i % 3) * 20);
            
            canvas.drawCircle(x, y, size, particlePaint);
        }
    }
    
    /**
     * Draw laboratory table
     */
    public void drawTable(Canvas canvas) {
        float tableTop = screenHeight * 0.6f;
        
        // Table shadow
        RectF shadowRect = new RectF(-20, tableTop + 10, screenWidth + 20, screenHeight + 50);
        canvas.drawRoundRect(shadowRect, 30, 30, shadowPaint);
        
        // Table top surface
        RectF tableRect = new RectF(0, tableTop, screenWidth, screenHeight + 30);
        canvas.drawRoundRect(tableRect, 30, 30, tableTopPaint);
        
        // Table front edge (3D effect) - Dark theme
        Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgePaint.setColor(Color.parseColor("#1C313A"));
        
        RectF edgeRect = new RectF(0, screenHeight - 40, screenWidth, screenHeight + 30);
        canvas.drawRect(edgeRect, edgePaint);
        
        // Table top highlight - Subtle glow
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.argb(25, 255, 255, 255));
        
        RectF highlightRect = new RectF(30, tableTop + 5, screenWidth - 30, tableTop + 15);
        canvas.drawRoundRect(highlightRect, 5, 5, highlightPaint);
    }
    
    /**
     * Draw title
     */
    public void drawTitle(Canvas canvas) {
        textPaint.setTextSize(48);
        textPaint.setColor(Color.WHITE);
        
        float titleY = 70;
        
        // Title shadow for better visibility
        textPaint.setShadowLayer(6, 2, 2, Color.argb(100, 0, 0, 0));
        canvas.drawText("🧪 Color Lab 🧪", screenWidth / 2f, titleY, textPaint);
        textPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
    }
    
    /**
     * Draw shade slider controller
     */
    public void drawShadeSlider(Canvas canvas, ShadeController controller, int baseColor) {
        if (!controller.isVisible()) return;
        
        float x = controller.getX();
        float y = controller.getY();
        float width = controller.getWidth();
        float trackHeight = controller.getTrackHeight();
        float alpha = controller.getAlpha();
        
        int alphaInt = (int) (alpha * 255);
        
        // Draw label - bigger text
        hintTextPaint.setTextSize(32);
        hintTextPaint.setAlpha(alphaInt);
        canvas.drawText("☀️ Adjust Shade 🌙", x, y - 70, hintTextPaint);
        
        // Draw track background with shadow
        float trackLeft = x - width / 2 + 50;
        float trackRight = x + width / 2 - 50;
        
        // Track shadow
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha((int)(alpha * 40));
        RectF shadowRect = new RectF(trackLeft + 3, y - trackHeight / 2 + 3, trackRight + 3, y + trackHeight / 2 + 3);
        canvas.drawRoundRect(shadowRect, trackHeight / 2, trackHeight / 2, shadowPaint);
        
        // Gradient track from light to dark
        int lightColor = ColorMixer.applyShade(baseColor, 0.7f);
        int darkColor = ColorMixer.applyShade(baseColor, -0.7f);
        
        LinearGradient trackGradient = new LinearGradient(
            trackLeft, y,
            trackRight, y,
            new int[]{lightColor, baseColor, darkColor},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        sliderTrackPaint.setShader(trackGradient);
        sliderTrackPaint.setAlpha(alphaInt);
        
        RectF trackRect = new RectF(trackLeft, y - trackHeight / 2, trackRight, y + trackHeight / 2);
        canvas.drawRoundRect(trackRect, trackHeight / 2, trackHeight / 2, sliderTrackPaint);
        
        // Track border for visibility
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setAlpha((int)(alpha * 150));
        canvas.drawRoundRect(trackRect, trackHeight / 2, trackHeight / 2, borderPaint);
        
        // Draw sun icon (left - light) - bigger
        sunPaint.setAlpha(alphaInt);
        drawSunIcon(canvas, trackLeft - 35, y, 24);
        
        // Draw moon icon (right - dark) - bigger
        moonPaint.setAlpha(alphaInt);
        drawMoonIcon(canvas, trackRight + 35, y, 24);
        
        // Draw handle - MUCH bigger
        float handleX = controller.getHandleX();
        float handleY = controller.getHandleY();
        float handleScale = controller.getHandleScale();
        float handleRadius = 32 * handleScale; // Bigger handle
        
        // Handle outer glow
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(baseColor);
        glowPaint.setAlpha((int) (alpha * 80));
        canvas.drawCircle(handleX, handleY, handleRadius + 15, glowPaint);
        
        // Handle shadow
        Paint handleShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        handleShadow.setColor(Color.BLACK);
        handleShadow.setAlpha((int)(alpha * 60));
        canvas.drawCircle(handleX + 2, handleY + 3, handleRadius, handleShadow);
        
        // Handle white background
        sliderHandlePaint.setAlpha(alphaInt);
        canvas.drawCircle(handleX, handleY, handleRadius, sliderHandlePaint);
        
        // Handle border
        Paint handleBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        handleBorder.setStyle(Paint.Style.STROKE);
        handleBorder.setStrokeWidth(4);
        handleBorder.setColor(baseColor);
        handleBorder.setAlpha(alphaInt);
        canvas.drawCircle(handleX, handleY, handleRadius, handleBorder);
        
        // Handle inner color indicator - bigger
        Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int currentColor = ColorMixer.applyShade(baseColor, controller.getValue());
        innerPaint.setColor(currentColor);
        innerPaint.setAlpha(alphaInt);
        canvas.drawCircle(handleX, handleY, handleRadius - 10, innerPaint);
    }
    
    /**
     * Draw sun icon
     */
    private void drawSunIcon(Canvas canvas, float cx, float cy, float size) {
        // Circle
        canvas.drawCircle(cx, cy, size * 0.5f, sunPaint);
        
        // Rays
        Paint rayPaint = new Paint(sunPaint);
        rayPaint.setStrokeWidth(3);
        rayPaint.setStrokeCap(Paint.Cap.ROUND);
        
        for (int i = 0; i < 8; i++) {
            float angle = (float) (i * Math.PI / 4);
            float innerR = size * 0.6f;
            float outerR = size;
            
            float x1 = cx + (float) Math.cos(angle) * innerR;
            float y1 = cy + (float) Math.sin(angle) * innerR;
            float x2 = cx + (float) Math.cos(angle) * outerR;
            float y2 = cy + (float) Math.sin(angle) * outerR;
            
            canvas.drawLine(x1, y1, x2, y2, rayPaint);
        }
    }
    
    /**
     * Draw moon icon
     */
    private void drawMoonIcon(Canvas canvas, float cx, float cy, float size) {
        // Main circle
        canvas.drawCircle(cx, cy, size * 0.7f, moonPaint);
        
        // Cut out circle to create crescent - Match dark background
        Paint cutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cutPaint.setColor(Color.parseColor("#1A237E")); // Match dark background
        cutPaint.setAlpha(moonPaint.getAlpha());
        canvas.drawCircle(cx + size * 0.35f, cy - size * 0.2f, size * 0.55f, cutPaint);
    }
    
    /**
     * Draw result color name with bounce animation
     */
    public void drawResultText(Canvas canvas, String colorName, int color, float centerY) {
        if (resultAlpha < 0.01f) return;
        
        canvas.save();
        
        float x = screenWidth / 2f;
        float y = centerY;
        
        // Apply bounce scale
        float scale = EasingFunctions.easeOutBack(resultScale);
        canvas.translate(x, y);
        canvas.scale(scale, scale);
        canvas.translate(-x, -y);
        
        // Background pill
        resultTextPaint.setTextSize(56);
        float textWidth = resultTextPaint.measureText(colorName);
        
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setAlpha((int) (resultAlpha * 230));
        bgPaint.setShadowLayer(12, 0, 4, Color.argb((int) (resultAlpha * 80), 0, 0, 0));
        
        RectF bgRect = new RectF(
            x - textWidth / 2 - 40,
            y - 45,
            x + textWidth / 2 + 40,
            y + 25
        );
        canvas.drawRoundRect(bgRect, 35, 35, bgPaint);
        
        // Color indicator circle
        Paint colorIndicator = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorIndicator.setColor(color);
        colorIndicator.setAlpha((int) (resultAlpha * 255));
        canvas.drawCircle(x - textWidth / 2 - 15, y - 10, 15, colorIndicator);
        
        // Text
        resultTextPaint.setColor(color);
        resultTextPaint.setAlpha((int) (resultAlpha * 255));
        canvas.drawText(colorName, x + 10, y, resultTextPaint);
        
        canvas.restore();
    }
    
    /**
     * Draw instruction hint
     */
    public void drawHint(Canvas canvas, String hint, float y) {
        hintTextPaint.setTextSize(32);
        hintTextPaint.setAlpha(200);
        canvas.drawText(hint, screenWidth / 2f, y, hintTextPaint);
    }
    
    /**
     * Draw reset button - BIG and styled like ColorsActivity
     */
    public void drawResetButton(Canvas canvas, float x, float y, float width, float height, boolean isPressed) {
        float scale = isPressed ? 0.92f : 1.0f;
        
        canvas.save();
        canvas.translate(x, y);
        canvas.scale(scale, scale);
        canvas.translate(-x, -y);
        
        // Outer glow effect
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(Color.parseColor("#E040FB"));
        glowPaint.setAlpha(isPressed ? 40 : 60);
        RectF glowRect = new RectF(x - width / 2 - 8, y - height / 2 - 8, x + width / 2 + 8, y + height / 2 + 8);
        canvas.drawRoundRect(glowRect, height / 2 + 8, height / 2 + 8, glowPaint);
        
        // Button shadow
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(50);
        RectF shadowRect = new RectF(x - width / 2 + 4, y - height / 2 + 6, x + width / 2 + 4, y + height / 2 + 6);
        canvas.drawRoundRect(shadowRect, height / 2, height / 2, shadowPaint);
        
        // Gradient background - vibrant purple/pink
        Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient gradient = new LinearGradient(
            x - width / 2, y - height / 2,
            x + width / 2, y + height / 2,
            isPressed ? Color.parseColor("#7B1FA2") : Color.parseColor("#AB47BC"),
            isPressed ? Color.parseColor("#6A1B9A") : Color.parseColor("#8E24AA"),
            Shader.TileMode.CLAMP
        );
        gradientPaint.setShader(gradient);
        
        RectF buttonRect = new RectF(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
        canvas.drawRoundRect(buttonRect, height / 2, height / 2, gradientPaint);
        
        // Button border highlight
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setAlpha(100);
        canvas.drawRoundRect(buttonRect, height / 2, height / 2, borderPaint);
        
        // Text with shadow - BIGGER
        buttonTextPaint.setTextSize(28);
        buttonTextPaint.setShadowLayer(4, 2, 2, Color.argb(100, 0, 0, 0));
        canvas.drawText("🔄  NEW MIX", x, y + 10, buttonTextPaint);
        buttonTextPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        
        canvas.restore();
    }
    
    /**
     * Draw collection button
     */
    public void drawCollectionButton(Canvas canvas, float x, float y, float size, int collectedCount, boolean hasNew) {
        // Background circle
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setShadowLayer(8, 0, 2, Color.argb(40, 0, 0, 0));
        canvas.drawCircle(x, y, size, bgPaint);
        
        // Icon
        textPaint.setTextSize(32);
        canvas.drawText("📚", x, y + 12, textPaint);
        
        // Badge with count
        if (collectedCount > 0) {
            Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgePaint.setColor(hasNew ? Color.parseColor("#E74C3C") : Color.parseColor("#3498DB"));
            
            float badgeX = x + size * 0.6f;
            float badgeY = y - size * 0.6f;
            canvas.drawCircle(badgeX, badgeY, 14, badgePaint);
            
            Paint badgeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgeTextPaint.setColor(Color.WHITE);
            badgeTextPaint.setTextSize(16);
            badgeTextPaint.setTextAlign(Paint.Align.CENTER);
            badgeTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(String.valueOf(collectedCount), badgeX, badgeY + 6, badgeTextPaint);
        }
    }
    
    /**
     * Draw back button - styled like the Colors activity
     */
    public void drawBackButton(Canvas canvas, float x, float y, float size) {
        // Semi-transparent dark background circle (matching ColorsActivity style)
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#44FFFFFF")); // Semi-transparent white
        bgPaint.setShadowLayer(8, 0, 3, Color.argb(50, 0, 0, 0));
        canvas.drawCircle(x, y, size, bgPaint);
        
        // White arrow icon for visibility on dark background
        Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.WHITE);
        arrowPaint.setStrokeWidth(5);
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);
        arrowPaint.setStrokeJoin(Paint.Join.ROUND);
        arrowPaint.setStyle(Paint.Style.STROKE);
        
        Path arrowPath = new Path();
        arrowPath.moveTo(x + 10, y - 14);
        arrowPath.lineTo(x - 8, y);
        arrowPath.lineTo(x + 10, y + 14);
        canvas.drawPath(arrowPath, arrowPaint);
    }
    
    /**
     * Show result with animation
     */
    public void showResult() {
        resultTargetAlpha = 1.0f;
        resultTargetScale = 1.0f;
    }
    
    /**
     * Hide result
     */
    public void hideResult() {
        resultTargetAlpha = 0f;
        resultTargetScale = 0f;
    }
    
    /**
     * Get result alpha
     */
    public float getResultAlpha() {
        return resultAlpha;
    }
    
    // Getters
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
}
