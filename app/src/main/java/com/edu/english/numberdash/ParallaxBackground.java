package com.edu.english.numberdash;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Renders the parallax scrolling background for Number Dash Race
 */
public class ParallaxBackground {
    
    // Layer offsets (for scrolling)
    private float farLayerOffset = 0;
    private float midLayerOffset = 0;
    private float nearLayerOffset = 0;
    private float trackOffset = 0;
    
    // Screen dimensions
    private int screenWidth;
    private int screenHeight;
    
    // Paints
    private Paint skyPaint;
    private Paint sunPaint;
    private Paint cloudPaint;
    private Paint mountainPaint;
    private Paint hillPaint;
    private Paint grassPaint;
    private Paint trackPaint;
    private Paint trackLinePaint;
    private Paint treePaint;
    private Paint treeTopPaint;
    
    // Decorative elements positions
    private float[] cloudPositions;
    private float[] cloudSizes;
    private float[] treePositions;
    private float[] bushPositions;
    
    public ParallaxBackground(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        initPaints();
        initDecorations();
    }
    
    private void initPaints() {
        skyPaint = new Paint();
        skyPaint.setAntiAlias(true);
        
        sunPaint = new Paint();
        sunPaint.setAntiAlias(true);
        sunPaint.setColor(0xFFFFEB3B);
        
        cloudPaint = new Paint();
        cloudPaint.setAntiAlias(true);
        cloudPaint.setColor(0xFFFFFFFF);
        
        mountainPaint = new Paint();
        mountainPaint.setAntiAlias(true);
        mountainPaint.setColor(0xFF90A4AE);
        
        hillPaint = new Paint();
        hillPaint.setAntiAlias(true);
        hillPaint.setColor(0xFF81C784);
        
        grassPaint = new Paint();
        grassPaint.setAntiAlias(true);
        grassPaint.setColor(0xFF66BB6A);
        
        trackPaint = new Paint();
        trackPaint.setAntiAlias(true);
        trackPaint.setColor(0xFFFFCC80);
        
        trackLinePaint = new Paint();
        trackLinePaint.setAntiAlias(true);
        trackLinePaint.setColor(0xFFFFFFFF);
        trackLinePaint.setStrokeWidth(4);
        
        treePaint = new Paint();
        treePaint.setAntiAlias(true);
        treePaint.setColor(0xFF5D4037);
        
        treeTopPaint = new Paint();
        treeTopPaint.setAntiAlias(true);
        treeTopPaint.setColor(0xFF388E3C);
    }
    
    private void initDecorations() {
        // Generate random cloud positions (will tile)
        cloudPositions = new float[]{0.1f, 0.3f, 0.55f, 0.75f, 0.9f};
        cloudSizes = new float[]{60, 80, 50, 70, 55};
        
        // Tree positions (will tile)
        treePositions = new float[]{0.15f, 0.4f, 0.65f, 0.85f};
        
        // Bush positions
        bushPositions = new float[]{0.1f, 0.25f, 0.5f, 0.7f, 0.9f};
    }
    
    /**
     * Update parallax offsets based on current speed
     */
    public void update(float deltaTime, float currentSpeed) {
        float baseMovement = currentSpeed * (deltaTime / 1000f);
        
        farLayerOffset += baseMovement * GameConstants.BG_FAR_SPEED_RATIO;
        midLayerOffset += baseMovement * GameConstants.BG_MID_SPEED_RATIO;
        nearLayerOffset += baseMovement * GameConstants.BG_NEAR_SPEED_RATIO;
        trackOffset += baseMovement * GameConstants.TRACK_SPEED_RATIO;
        
        // Wrap offsets to prevent floating point issues
        float tileWidth = screenWidth;
        farLayerOffset %= tileWidth;
        midLayerOffset %= tileWidth;
        nearLayerOffset %= tileWidth;
        trackOffset %= tileWidth;
    }
    
    /**
     * Draw the complete background
     */
    public void draw(Canvas canvas) {
        // Layer 1: Sky gradient
        drawSky(canvas);
        
        // Layer 2: Sun
        drawSun(canvas);
        
        // Layer 3: Clouds (far)
        drawClouds(canvas, farLayerOffset);
        
        // Layer 4: Mountains (far)
        drawMountains(canvas, farLayerOffset);
        
        // Layer 5: Hills (mid)
        drawHills(canvas, midLayerOffset);
        
        // Layer 6: Trees (near)
        drawTrees(canvas, nearLayerOffset);
        
        // Layer 7: Grass
        drawGrass(canvas);
        
        // Layer 8: Track
        drawTrack(canvas, trackOffset);
    }
    
    private void drawSky(Canvas canvas) {
        LinearGradient skyGradient = new LinearGradient(
            0, 0, 0, screenHeight * 0.6f,
            GameConstants.COLOR_SKY_TOP,
            GameConstants.COLOR_SKY_BOTTOM,
            Shader.TileMode.CLAMP
        );
        skyPaint.setShader(skyGradient);
        canvas.drawRect(0, 0, screenWidth, screenHeight * 0.6f, skyPaint);
        skyPaint.setShader(null);
    }
    
    private void drawSun(Canvas canvas) {
        float sunX = screenWidth * 0.85f;
        float sunY = screenHeight * 0.15f;
        float sunRadius = 50;
        
        // Sun glow
        sunPaint.setColor(0x40FFEB3B);
        canvas.drawCircle(sunX, sunY, sunRadius * 1.5f, sunPaint);
        sunPaint.setColor(0x60FFEB3B);
        canvas.drawCircle(sunX, sunY, sunRadius * 1.2f, sunPaint);
        
        // Sun
        sunPaint.setColor(0xFFFFEB3B);
        canvas.drawCircle(sunX, sunY, sunRadius, sunPaint);
    }
    
    private void drawClouds(Canvas canvas, float offset) {
        float tileWidth = screenWidth;
        
        for (int tile = -1; tile <= 1; tile++) {
            float tileOffset = tile * tileWidth - offset;
            
            for (int i = 0; i < cloudPositions.length; i++) {
                float cloudX = tileOffset + cloudPositions[i] * tileWidth;
                float cloudY = screenHeight * (0.08f + (i % 3) * 0.05f);
                float size = cloudSizes[i];
                
                if (cloudX > -size * 2 && cloudX < screenWidth + size) {
                    drawCloud(canvas, cloudX, cloudY, size);
                }
            }
        }
    }
    
    private void drawCloud(Canvas canvas, float x, float y, float size) {
        // Draw fluffy cloud with multiple circles
        cloudPaint.setColor(0xFFFFFFFF);
        canvas.drawCircle(x, y, size * 0.6f, cloudPaint);
        canvas.drawCircle(x - size * 0.4f, y + size * 0.1f, size * 0.45f, cloudPaint);
        canvas.drawCircle(x + size * 0.4f, y + size * 0.1f, size * 0.5f, cloudPaint);
        canvas.drawCircle(x - size * 0.2f, y - size * 0.2f, size * 0.4f, cloudPaint);
        canvas.drawCircle(x + size * 0.2f, y - size * 0.15f, size * 0.35f, cloudPaint);
    }
    
    private void drawMountains(Canvas canvas, float offset) {
        float mountainY = screenHeight * 0.35f;
        float mountainHeight = screenHeight * 0.2f;
        float tileWidth = screenWidth;
        
        mountainPaint.setColor(0xFF78909C);
        
        for (int tile = -1; tile <= 1; tile++) {
            float tileOffset = tile * tileWidth - offset * 0.3f;
            
            // Draw multiple mountains
            drawMountain(canvas, tileOffset + tileWidth * 0.2f, mountainY, 
                        tileWidth * 0.25f, mountainHeight);
            drawMountain(canvas, tileOffset + tileWidth * 0.5f, mountainY, 
                        tileWidth * 0.3f, mountainHeight * 1.2f);
            drawMountain(canvas, tileOffset + tileWidth * 0.8f, mountainY, 
                        tileWidth * 0.22f, mountainHeight * 0.9f);
        }
    }
    
    private void drawMountain(Canvas canvas, float x, float baseY, float width, float height) {
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(x - width / 2, baseY);
        path.lineTo(x, baseY - height);
        path.lineTo(x + width / 2, baseY);
        path.close();
        
        canvas.drawPath(path, mountainPaint);
        
        // Snow cap
        cloudPaint.setColor(0xFFFFFFFF);
        android.graphics.Path snowPath = new android.graphics.Path();
        float snowHeight = height * 0.25f;
        snowPath.moveTo(x - width * 0.15f, baseY - height + snowHeight);
        snowPath.lineTo(x, baseY - height);
        snowPath.lineTo(x + width * 0.15f, baseY - height + snowHeight);
        snowPath.close();
        canvas.drawPath(snowPath, cloudPaint);
    }
    
    private void drawHills(Canvas canvas, float offset) {
        float hillY = screenHeight * 0.45f;
        float hillHeight = screenHeight * 0.15f;
        float tileWidth = screenWidth;
        
        for (int tile = -1; tile <= 1; tile++) {
            float tileOffset = tile * tileWidth - offset * 0.5f;
            
            // Draw rolling hills
            hillPaint.setColor(0xFF81C784);
            drawHill(canvas, tileOffset + tileWidth * 0.15f, hillY, tileWidth * 0.35f, hillHeight);
            
            hillPaint.setColor(0xFF66BB6A);
            drawHill(canvas, tileOffset + tileWidth * 0.5f, hillY, tileWidth * 0.4f, hillHeight * 1.1f);
            
            hillPaint.setColor(0xFF81C784);
            drawHill(canvas, tileOffset + tileWidth * 0.85f, hillY, tileWidth * 0.3f, hillHeight * 0.9f);
        }
    }
    
    private void drawHill(Canvas canvas, float x, float baseY, float width, float height) {
        RectF hillRect = new RectF(x - width / 2, baseY - height, x + width / 2, baseY + height);
        canvas.drawOval(hillRect, hillPaint);
    }
    
    private void drawTrees(Canvas canvas, float offset) {
        float tileWidth = screenWidth;
        float treeBaseY = screenHeight * 0.55f;
        
        for (int tile = -1; tile <= 1; tile++) {
            float tileOffset = tile * tileWidth - offset;
            
            for (int i = 0; i < treePositions.length; i++) {
                float treeX = tileOffset + treePositions[i] * tileWidth;
                float treeHeight = 60 + (i % 2) * 20;
                
                if (treeX > -50 && treeX < screenWidth + 50) {
                    drawTree(canvas, treeX, treeBaseY, treeHeight);
                }
            }
        }
    }
    
    private void drawTree(Canvas canvas, float x, float baseY, float height) {
        float trunkWidth = height * 0.15f;
        float trunkHeight = height * 0.4f;
        
        // Trunk
        treePaint.setColor(0xFF5D4037);
        RectF trunkRect = new RectF(x - trunkWidth / 2, baseY - trunkHeight, 
                                    x + trunkWidth / 2, baseY);
        canvas.drawRoundRect(trunkRect, 5, 5, treePaint);
        
        // Tree top (triangular shape made of circles)
        treeTopPaint.setColor(0xFF388E3C);
        float topRadius = height * 0.35f;
        canvas.drawCircle(x, baseY - trunkHeight - topRadius * 0.3f, topRadius, treeTopPaint);
        
        treeTopPaint.setColor(0xFF43A047);
        canvas.drawCircle(x - topRadius * 0.4f, baseY - trunkHeight, topRadius * 0.7f, treeTopPaint);
        canvas.drawCircle(x + topRadius * 0.4f, baseY - trunkHeight, topRadius * 0.7f, treeTopPaint);
    }
    
    private void drawGrass(Canvas canvas) {
        float grassTop = screenHeight * 0.55f;
        
        // Main grass area
        grassPaint.setColor(0xFF66BB6A);
        canvas.drawRect(0, grassTop, screenWidth, screenHeight, grassPaint);
        
        // Grass texture lines
        grassPaint.setColor(0xFF4CAF50);
        for (int i = 0; i < screenWidth; i += 30) {
            float x = i + (float)(Math.sin(i * 0.1) * 5);
            canvas.drawLine(x, grassTop, x + 5, grassTop + 15, grassPaint);
        }
    }
    
    private void drawTrack(Canvas canvas, float offset) {
        float trackTop = screenHeight * 0.70f;
        float trackHeight = screenHeight * 0.20f;
        
        // Track base
        trackPaint.setColor(0xFFFFCC80);
        canvas.drawRect(0, trackTop, screenWidth, trackTop + trackHeight, trackPaint);
        
        // Track border (top)
        trackPaint.setColor(0xFFFF8A65);
        canvas.drawRect(0, trackTop, screenWidth, trackTop + 8, trackPaint);
        
        // Track border (bottom)
        canvas.drawRect(0, trackTop + trackHeight - 8, screenWidth, trackTop + trackHeight, trackPaint);
        
        // Dashed center line
        trackLinePaint.setColor(0xFFFFFFFF);
        float dashWidth = 40;
        float dashGap = 30;
        float dashY = trackTop + trackHeight / 2;
        
        float tileWidth = dashWidth + dashGap;
        float startX = -(offset % tileWidth);
        
        for (float x = startX; x < screenWidth + dashWidth; x += tileWidth) {
            canvas.drawLine(x, dashY, x + dashWidth, dashY, trackLinePaint);
        }
        
        // Track dust/texture
        trackPaint.setColor(0x20000000);
        for (int i = 0; i < 10; i++) {
            float dustX = ((i * 137 + offset * 0.5f) % screenWidth);
            float dustY = trackTop + trackHeight * 0.3f + (i % 3) * trackHeight * 0.2f;
            canvas.drawCircle(dustX, dustY, 5 + i % 3, trackPaint);
        }
    }
    
    /**
     * Draw finish line elements
     */
    public void drawFinishLine(Canvas canvas, float finishX) {
        if (finishX > 0 && finishX < screenWidth + 100) {
            float trackTop = screenHeight * 0.68f;
            float trackHeight = screenHeight * 0.18f;
            
            // Checkered pattern
            Paint checkerPaint = new Paint();
            checkerPaint.setAntiAlias(true);
            
            float checkerSize = 20;
            int cols = 3;
            int rows = (int)(trackHeight / checkerSize);
            
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    checkerPaint.setColor((row + col) % 2 == 0 ? 0xFFFFFFFF : 0xFF333333);
                    float x = finishX + col * checkerSize;
                    float y = trackTop + row * checkerSize;
                    canvas.drawRect(x, y, x + checkerSize, y + checkerSize, checkerPaint);
                }
            }
            
            // Finish banner
            Paint bannerPaint = new Paint();
            bannerPaint.setColor(0xFFE53935);
            bannerPaint.setAntiAlias(true);
            
            float bannerY = trackTop - 50;
            RectF bannerRect = new RectF(finishX - 20, bannerY, finishX + 80, bannerY + 35);
            canvas.drawRoundRect(bannerRect, 10, 10, bannerPaint);
            
            // Finish text
            Paint textPaint = new Paint();
            textPaint.setColor(0xFFFFFFFF);
            textPaint.setTextSize(22);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setFakeBoldText(true);
            textPaint.setAntiAlias(true);
            canvas.drawText("FINISH", finishX + 30, bannerY + 25, textPaint);
        }
    }
    
    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
}
