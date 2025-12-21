package com.edu.english.numbers.views;

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

import com.edu.english.numbers.data.NumberSceneDataProvider;
import com.edu.english.numbers.models.NumberScene;
import com.edu.english.numbers.models.NumberScene.SceneObject;
import com.edu.english.numbers.models.NumberScene.SceneObject.ObjectType;

/**
 * Custom View that draws counting scenes using Canvas
 * Renders objects (apples, cars, balloons, stars) for kids to count
 */
public class SceneCanvasView extends View {
    
    private static final String TAG = "SceneCanvasView";
    
    private NumberScene currentScene;
    private Paint paint;
    private Paint strokePaint;
    private Paint shadowPaint;
    private Paint highlightPaint;
    private Path starPath;
    
    // Scene decorations
    private Paint tablePaint;
    private Paint grassPaint;
    private Paint skyGradientPaint;
    
    public SceneCanvasView(Context context) {
        super(context);
        init();
    }
    
    public SceneCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public SceneCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3f);
        strokePaint.setColor(Color.WHITE);
        
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(0x40000000);
        
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setColor(0x40FFFFFF);
        
        tablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        grassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        skyGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        starPath = new Path();
    }
    
    /**
     * Set the scene to display
     */
    public void setScene(NumberScene scene) {
        this.currentScene = scene;
        invalidate();
    }
    
    /**
     * Set scene by ID
     */
    public void setSceneById(int sceneId) {
        NumberSceneDataProvider provider = NumberSceneDataProvider.getInstance();
        this.currentScene = provider.getScene(sceneId);
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (currentScene == null) {
            drawPlaceholder(canvas);
            return;
        }
        
        // Draw background based on scene type
        drawSceneBackground(canvas);
        
        // Draw all objects in the scene
        for (SceneObject obj : currentScene.getObjects()) {
            drawObject(canvas, obj);
        }
        
        // Draw scene-specific decorations
        drawSceneDecorations(canvas);
    }
    
    private void drawPlaceholder(Canvas canvas) {
        paint.setColor(0xFFE0E0E0);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        
        paint.setColor(0xFF9E9E9E);
        paint.setTextSize(48f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Loading...", getWidth() / 2f, getHeight() / 2f, paint);
    }
    
    private void drawSceneBackground(Canvas canvas) {
        int bgColor = NumberSceneDataProvider.getInstance()
                .getSceneBackgroundColor(currentScene.getSceneId());
        
        switch (currentScene.getSceneId()) {
            case NumberScene.SCENE_APPLES:
                drawTableBackground(canvas);
                break;
            case NumberScene.SCENE_CARS:
                drawParkingBackground(canvas);
                break;
            case NumberScene.SCENE_BALLOONS:
                drawSkyBackground(canvas);
                break;
            case NumberScene.SCENE_STARS:
                drawNightSkyBackground(canvas);
                break;
            default:
                paint.setColor(bgColor);
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }
    }
    
    private void drawTableBackground(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        
        // Wall background (light cream)
        paint.setColor(0xFFFFF8E1);
        canvas.drawRect(0, 0, width, height, paint);
        
        // Table surface
        paint.setColor(0xFF8D6E63); // Brown wood
        RectF tableTop = new RectF(0, height * 0.3f, width, height);
        canvas.drawRect(tableTop, paint);
        
        // Table top edge (darker)
        paint.setColor(0xFF6D4C41);
        canvas.drawRect(0, height * 0.3f, width, height * 0.35f, paint);
        
        // Wood grain lines
        paint.setColor(0xFF5D4037);
        paint.setStrokeWidth(2f);
        for (int i = 0; i < 5; i++) {
            float y = height * 0.4f + i * (height * 0.12f);
            canvas.drawLine(0, y, width, y + 5, paint);
        }
    }
    
    private void drawParkingBackground(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        
        // Asphalt
        paint.setColor(0xFF424242);
        canvas.drawRect(0, 0, width, height, paint);
        
        // Parking lines (white)
        paint.setColor(0xFFFFFFFF);
        paint.setStrokeWidth(4f);
        
        // Vertical lines
        int numSpots = 4;
        float spotWidth = width / (float) numSpots;
        for (int i = 0; i <= numSpots; i++) {
            float x = i * spotWidth;
            canvas.drawLine(x, height * 0.1f, x, height * 0.45f, paint);
            canvas.drawLine(x, height * 0.55f, x, height * 0.9f, paint);
        }
        
        // Middle divider (dashed yellow)
        paint.setColor(0xFFFFC107);
        paint.setStrokeWidth(6f);
        canvas.drawLine(0, height * 0.5f, width, height * 0.5f, paint);
    }
    
    private void drawSkyBackground(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        
        // Sky gradient
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, height,
                new int[]{0xFF87CEEB, 0xFFB3E5FC, 0xFFE1F5FE},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        );
        skyGradientPaint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, skyGradientPaint);
        
        // Simple clouds
        paint.setColor(0xCCFFFFFF);
        drawCloud(canvas, width * 0.15f, height * 0.7f, width * 0.12f);
        drawCloud(canvas, width * 0.7f, height * 0.75f, width * 0.15f);
        drawCloud(canvas, width * 0.45f, height * 0.8f, width * 0.1f);
    }
    
    private void drawCloud(Canvas canvas, float cx, float cy, float size) {
        canvas.drawCircle(cx, cy, size, paint);
        canvas.drawCircle(cx - size * 0.6f, cy + size * 0.2f, size * 0.7f, paint);
        canvas.drawCircle(cx + size * 0.6f, cy + size * 0.15f, size * 0.8f, paint);
        canvas.drawCircle(cx - size * 0.3f, cy - size * 0.3f, size * 0.5f, paint);
        canvas.drawCircle(cx + size * 0.4f, cy - size * 0.25f, size * 0.6f, paint);
    }
    
    private void drawNightSkyBackground(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        
        // Dark blue gradient
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, height,
                new int[]{0xFF0D1B2A, 0xFF1B263B, 0xFF415A77},
                new float[]{0f, 0.6f, 1f},
                Shader.TileMode.CLAMP
        );
        skyGradientPaint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, skyGradientPaint);
        
        // Small background stars (twinkle effect)
        paint.setColor(0x80FFFFFF);
        for (int i = 0; i < 30; i++) {
            float x = (float) (Math.random() * width);
            float y = (float) (Math.random() * height);
            float size = (float) (1 + Math.random() * 2);
            canvas.drawCircle(x, y, size, paint);
        }
    }
    
    private void drawObject(Canvas canvas, SceneObject obj) {
        float width = getWidth();
        float height = getHeight();
        
        // Calculate actual position and size
        float cx = obj.getX() * width;
        float cy = obj.getY() * height;
        float size = obj.getSize() * Math.min(width, height);
        
        switch (obj.getType()) {
            case APPLE_RED:
            case APPLE_GREEN:
                drawApple(canvas, cx, cy, size, obj.getColor());
                break;
            case CAR_RED:
            case CAR_BLUE:
            case CAR_YELLOW:
                drawCar(canvas, cx, cy, size, obj.getColor());
                break;
            case BALLOON_PURPLE:
            case BALLOON_PINK:
                drawBalloon(canvas, cx, cy, size, obj.getColor());
                break;
            case STAR_BIG:
            case STAR_SMALL:
                drawStar(canvas, cx, cy, size, obj.getColor());
                break;
        }
    }
    
    private void drawApple(Canvas canvas, float cx, float cy, float size, int color) {
        // Shadow
        shadowPaint.setColor(0x30000000);
        canvas.drawOval(cx - size * 0.8f, cy + size * 0.8f, 
                cx + size * 0.8f, cy + size * 1.1f, shadowPaint);
        
        // Apple body
        paint.setColor(color);
        RectF appleRect = new RectF(cx - size, cy - size * 0.9f, cx + size, cy + size * 0.9f);
        canvas.drawOval(appleRect, paint);
        
        // Apple indent at top
        paint.setColor(darkenColor(color, 0.2f));
        canvas.drawArc(cx - size * 0.3f, cy - size * 1.1f, 
                cx + size * 0.3f, cy - size * 0.4f, 
                0, 180, true, paint);
        
        // Stem
        paint.setColor(0xFF5D4037);
        RectF stemRect = new RectF(cx - size * 0.08f, cy - size * 1.2f, 
                cx + size * 0.08f, cy - size * 0.7f);
        canvas.drawRect(stemRect, paint);
        
        // Leaf
        paint.setColor(0xFF4CAF50);
        Path leafPath = new Path();
        leafPath.moveTo(cx + size * 0.05f, cy - size * 1.0f);
        leafPath.quadTo(cx + size * 0.4f, cy - size * 1.3f, 
                cx + size * 0.5f, cy - size * 0.9f);
        leafPath.quadTo(cx + size * 0.3f, cy - size * 0.95f, 
                cx + size * 0.05f, cy - size * 1.0f);
        canvas.drawPath(leafPath, paint);
        
        // Highlight
        highlightPaint.setColor(0x40FFFFFF);
        canvas.drawOval(cx - size * 0.6f, cy - size * 0.6f, 
                cx - size * 0.2f, cy - size * 0.2f, highlightPaint);
    }
    
    private void drawCar(Canvas canvas, float cx, float cy, float size, int color) {
        float carWidth = size * 2f;
        float carHeight = size * 0.8f;
        
        // Shadow
        shadowPaint.setColor(0x30000000);
        canvas.drawOval(cx - carWidth * 0.4f, cy + carHeight * 0.8f, 
                cx + carWidth * 0.4f, cy + carHeight * 1.1f, shadowPaint);
        
        // Car body (bottom part)
        paint.setColor(color);
        RectF bodyRect = new RectF(cx - carWidth * 0.5f, cy - carHeight * 0.2f, 
                cx + carWidth * 0.5f, cy + carHeight * 0.5f);
        canvas.drawRoundRect(bodyRect, size * 0.15f, size * 0.15f, paint);
        
        // Car top (cabin)
        paint.setColor(color);
        RectF topRect = new RectF(cx - carWidth * 0.3f, cy - carHeight * 0.7f, 
                cx + carWidth * 0.25f, cy);
        canvas.drawRoundRect(topRect, size * 0.1f, size * 0.1f, paint);
        
        // Windows
        paint.setColor(0xFF81D4FA);
        RectF windowRect = new RectF(cx - carWidth * 0.25f, cy - carHeight * 0.6f, 
                cx + carWidth * 0.2f, cy - carHeight * 0.1f);
        canvas.drawRoundRect(windowRect, size * 0.05f, size * 0.05f, paint);
        
        // Window divider
        paint.setColor(color);
        canvas.drawRect(cx - carWidth * 0.02f, cy - carHeight * 0.6f, 
                cx + carWidth * 0.02f, cy - carHeight * 0.1f, paint);
        
        // Wheels
        paint.setColor(0xFF212121);
        float wheelRadius = size * 0.2f;
        canvas.drawCircle(cx - carWidth * 0.3f, cy + carHeight * 0.5f, wheelRadius, paint);
        canvas.drawCircle(cx + carWidth * 0.3f, cy + carHeight * 0.5f, wheelRadius, paint);
        
        // Wheel caps
        paint.setColor(0xFF757575);
        canvas.drawCircle(cx - carWidth * 0.3f, cy + carHeight * 0.5f, wheelRadius * 0.5f, paint);
        canvas.drawCircle(cx + carWidth * 0.3f, cy + carHeight * 0.5f, wheelRadius * 0.5f, paint);
        
        // Headlight
        paint.setColor(0xFFFFEB3B);
        canvas.drawCircle(cx + carWidth * 0.45f, cy + carHeight * 0.1f, size * 0.1f, paint);
        
        // Highlight on body
        highlightPaint.setColor(0x20FFFFFF);
        canvas.drawRect(cx - carWidth * 0.45f, cy - carHeight * 0.1f, 
                cx + carWidth * 0.45f, cy + carHeight * 0.05f, highlightPaint);
    }
    
    private void drawBalloon(Canvas canvas, float cx, float cy, float size, int color) {
        // String
        paint.setColor(0xFF9E9E9E);
        paint.setStrokeWidth(2f);
        paint.setStyle(Paint.Style.STROKE);
        
        Path stringPath = new Path();
        stringPath.moveTo(cx, cy + size * 1.1f);
        stringPath.quadTo(cx + size * 0.2f, cy + size * 1.5f, 
                cx - size * 0.1f, cy + size * 2f);
        canvas.drawPath(stringPath, paint);
        paint.setStyle(Paint.Style.FILL);
        
        // Balloon body
        paint.setColor(color);
        RectF balloonRect = new RectF(cx - size, cy - size * 1.2f, 
                cx + size, cy + size * 0.8f);
        canvas.drawOval(balloonRect, paint);
        
        // Balloon knot
        paint.setColor(darkenColor(color, 0.2f));
        Path knotPath = new Path();
        knotPath.moveTo(cx - size * 0.15f, cy + size * 0.8f);
        knotPath.lineTo(cx, cy + size * 1.1f);
        knotPath.lineTo(cx + size * 0.15f, cy + size * 0.8f);
        knotPath.close();
        canvas.drawPath(knotPath, paint);
        
        // Highlight
        highlightPaint.setColor(0x50FFFFFF);
        canvas.drawOval(cx - size * 0.6f, cy - size * 0.9f, 
                cx - size * 0.1f, cy - size * 0.3f, highlightPaint);
    }
    
    private void drawStar(Canvas canvas, float cx, float cy, float size, int color) {
        // Glow effect for stars
        if (currentScene.getSceneId() == NumberScene.SCENE_STARS) {
            paint.setColor(0x30FFD700);
            canvas.drawCircle(cx, cy, size * 1.5f, paint);
        }
        
        // Create star path
        createStarPath(cx, cy, size, size * 0.4f, 5);
        
        // Draw star
        paint.setColor(color);
        canvas.drawPath(starPath, paint);
        
        // Star outline
        strokePaint.setColor(0xFFFFFFFF);
        strokePaint.setStrokeWidth(2f);
        canvas.drawPath(starPath, strokePaint);
        
        // Center highlight
        highlightPaint.setColor(0x60FFFFFF);
        canvas.drawCircle(cx, cy, size * 0.3f, highlightPaint);
    }
    
    private void createStarPath(float cx, float cy, float outerRadius, float innerRadius, int points) {
        starPath.reset();
        
        double angleStep = Math.PI / points;
        double startAngle = -Math.PI / 2; // Start from top
        
        for (int i = 0; i < points * 2; i++) {
            double angle = startAngle + i * angleStep;
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;
            float x = cx + (float) (radius * Math.cos(angle));
            float y = cy + (float) (radius * Math.sin(angle));
            
            if (i == 0) {
                starPath.moveTo(x, y);
            } else {
                starPath.lineTo(x, y);
            }
        }
        starPath.close();
    }
    
    private void drawSceneDecorations(Canvas canvas) {
        // Additional scene-specific decorations can go here
    }
    
    /**
     * Darken a color by a factor
     */
    private int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.max(0, (int) (Color.red(color) * (1 - factor)));
        int g = Math.max(0, (int) (Color.green(color) * (1 - factor)));
        int b = Math.max(0, (int) (Color.blue(color) * (1 - factor)));
        return Color.argb(a, r, g, b);
    }
    
    /**
     * Lighten a color by a factor
     */
    private int lightenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.min(255, (int) (Color.red(color) + (255 - Color.red(color)) * factor));
        int g = Math.min(255, (int) (Color.green(color) + (255 - Color.green(color)) * factor));
        int b = Math.min(255, (int) (Color.blue(color) + (255 - Color.blue(color)) * factor));
        return Color.argb(a, r, g, b);
    }
}
