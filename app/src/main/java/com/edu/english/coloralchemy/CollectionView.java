package com.edu.english.coloralchemy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

import java.util.List;

/**
 * Custom View for displaying color collection
 * Shows all collected colors in a beautiful grid layout
 */
public class CollectionView extends View {
    
    private List<CollectionManager.CollectedColor> colors;
    private int totalPossible;
    private float progress;
    
    private Paint backgroundPaint;
    private Paint cardPaint;
    private Paint cardShadowPaint;
    private Paint colorPaint;
    private Paint colorStrokePaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint progressBgPaint;
    private Paint progressFillPaint;
    private Paint emptySlotPaint;
    
    private int columns = 4;
    private float cardSize;
    private float padding;
    private float colorRadius;
    
    // Animation
    private float animationProgress = 0;
    
    public CollectionView(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        // Background
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Card
        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setColor(Color.WHITE);
        cardPaint.setShadowLayer(8, 0, 4, Color.argb(40, 0, 0, 0));
        
        cardShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardShadowPaint.setColor(Color.argb(30, 0, 0, 0));
        
        // Color circle
        colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        colorStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorStrokePaint.setStyle(Paint.Style.STROKE);
        colorStrokePaint.setStrokeWidth(3);
        colorStrokePaint.setColor(Color.WHITE);
        
        // Text
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(Color.parseColor("#2C3E50"));
        
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(Color.parseColor("#7F8C8D"));
        
        // Progress bar
        progressBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressBgPaint.setColor(Color.parseColor("#ECEFF1"));
        
        progressFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Empty slot
        emptySlotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptySlotPaint.setStyle(Paint.Style.STROKE);
        emptySlotPaint.setStrokeWidth(3);
        emptySlotPaint.setColor(Color.parseColor("#E0E0E0"));
        emptySlotPaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{10, 10}, 0));
        
        padding = 16;
    }
    
    public void setColors(List<CollectionManager.CollectedColor> colors, int totalPossible) {
        this.colors = colors;
        this.totalPossible = totalPossible;
        this.progress = (float) colors.size() / totalPossible;
        invalidate();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Calculate card size based on width
        float availableWidth = w - padding * 2 - padding * (columns - 1);
        cardSize = availableWidth / columns;
        colorRadius = cardSize * 0.35f;
        
        // Update text sizes
        textPaint.setTextSize(cardSize * 0.15f);
        labelPaint.setTextSize(cardSize * 0.12f);
        
        // Setup background gradient
        LinearGradient bgGradient = new LinearGradient(
            0, 0, 0, h,
            new int[]{
                Color.parseColor("#FAFAFA"),
                Color.parseColor("#F5F5F5"),
                Color.parseColor("#EEEEEE")
            },
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(bgGradient);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw background
        canvas.drawRect(0, 0, width, height, backgroundPaint);
        
        float y = padding;
        
        // Draw title
        textPaint.setTextSize(48);
        canvas.drawText("🎨 Color Collection", width / 2f, y + 45, textPaint);
        y += 80;
        
        // Draw progress bar
        y = drawProgressBar(canvas, y, width);
        y += 40;
        
        // Draw stats
        labelPaint.setTextSize(24);
        String statsText = colors.size() + " of " + totalPossible + " colors collected";
        canvas.drawText(statsText, width / 2f, y, labelPaint);
        y += 50;
        
        // Draw section headers and colors
        y = drawColorSection(canvas, "Primary Colors", getPrimaryColors(), y, width);
        y = drawColorSection(canvas, "Mixed Colors", getSecondaryColors(), y, width);
        y = drawColorSection(canvas, "Light Shades", getLightShades(), y, width);
        y = drawColorSection(canvas, "Dark Shades", getDarkShades(), y, width);
        
        // Draw empty slots hint if collection is incomplete
        if (colors.size() < totalPossible) {
            y += 20;
            labelPaint.setTextSize(20);
            canvas.drawText("Keep mixing to discover more colors!", width / 2f, y, labelPaint);
        }
    }
    
    private float drawProgressBar(Canvas canvas, float y, int width) {
        float barWidth = width - padding * 4;
        float barHeight = 20;
        float barX = (width - barWidth) / 2f;
        
        // Background
        RectF bgRect = new RectF(barX, y, barX + barWidth, y + barHeight);
        canvas.drawRoundRect(bgRect, barHeight / 2, barHeight / 2, progressBgPaint);
        
        // Fill gradient
        int[] progressColors = {
            Color.parseColor("#3498DB"),
            Color.parseColor("#9B59B6"),
            Color.parseColor("#E74C3C")
        };
        LinearGradient progressGradient = new LinearGradient(
            barX, y, barX + barWidth * progress, y,
            progressColors,
            null,
            Shader.TileMode.CLAMP
        );
        progressFillPaint.setShader(progressGradient);
        
        // Fill
        float fillWidth = barWidth * progress;
        if (fillWidth > barHeight) {
            RectF fillRect = new RectF(barX, y, barX + fillWidth, y + barHeight);
            canvas.drawRoundRect(fillRect, barHeight / 2, barHeight / 2, progressFillPaint);
        }
        
        // Percentage text
        textPaint.setTextSize(18);
        String percentText = (int) (progress * 100) + "%";
        canvas.drawText(percentText, barX + barWidth + 30, y + barHeight - 3, textPaint);
        
        return y + barHeight;
    }
    
    private float drawColorSection(Canvas canvas, String title, List<CollectionManager.CollectedColor> sectionColors, 
                                   float startY, int width) {
        if (sectionColors == null || sectionColors.isEmpty()) {
            return startY;
        }
        
        float y = startY;
        
        // Section header
        labelPaint.setTextSize(22);
        canvas.drawText(title, width / 2f, y + 20, labelPaint);
        y += 40;
        
        // Draw color cards in grid
        int colIndex = 0;
        float rowStartX = (width - (columns * cardSize + (columns - 1) * padding)) / 2f;
        
        for (CollectionManager.CollectedColor cc : sectionColors) {
            float x = rowStartX + colIndex * (cardSize + padding);
            
            drawColorCard(canvas, cc, x, y);
            
            colIndex++;
            if (colIndex >= columns) {
                colIndex = 0;
                y += cardSize + padding;
            }
        }
        
        // Complete the row if needed
        if (colIndex > 0) {
            y += cardSize + padding;
        }
        
        return y + padding;
    }
    
    private void drawColorCard(Canvas canvas, CollectionManager.CollectedColor cc, float x, float y) {
        float cx = x + cardSize / 2;
        float cy = y + cardSize * 0.4f;
        
        // Card background with shadow
        RectF cardRect = new RectF(x, y, x + cardSize, y + cardSize);
        
        // Shadow
        canvas.save();
        canvas.translate(2, 4);
        canvas.drawRoundRect(cardRect, 12, 12, cardShadowPaint);
        canvas.restore();
        
        // Card
        canvas.drawRoundRect(cardRect, 12, 12, cardPaint);
        
        // Color circle with glow
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(cc.color);
        glowPaint.setAlpha(50);
        canvas.drawCircle(cx, cy, colorRadius + 8, glowPaint);
        
        // Color circle
        colorPaint.setColor(cc.color);
        canvas.drawCircle(cx, cy, colorRadius, colorPaint);
        canvas.drawCircle(cx, cy, colorRadius, colorStrokePaint);
        
        // Highlight on color circle
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.argb(80, 255, 255, 255));
        canvas.drawCircle(cx - colorRadius * 0.3f, cy - colorRadius * 0.3f, colorRadius * 0.25f, highlightPaint);
        
        // Color name
        textPaint.setTextSize(cardSize * 0.13f);
        
        // Split long names
        String name = cc.getDisplayName();
        float textY = y + cardSize * 0.75f;
        
        if (name.contains(" ")) {
            String[] parts = name.split(" ", 2);
            canvas.drawText(parts[0], cx, textY, textPaint);
            if (parts.length > 1) {
                canvas.drawText(parts[1], cx, textY + cardSize * 0.15f, textPaint);
            }
        } else {
            canvas.drawText(name, cx, textY + cardSize * 0.07f, textPaint);
        }
        
        // New badge
        if (cc.isNew) {
            Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgePaint.setColor(Color.parseColor("#E74C3C"));
            
            float badgeX = x + cardSize - 20;
            float badgeY = y + 8;
            canvas.drawCircle(badgeX, badgeY + 8, 12, badgePaint);
            
            Paint badgeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgeTextPaint.setColor(Color.WHITE);
            badgeTextPaint.setTextSize(10);
            badgeTextPaint.setTextAlign(Paint.Align.CENTER);
            badgeTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("✨", badgeX, badgeY + 12, badgeTextPaint);
        }
    }
    
    // Helper methods to categorize colors
    private List<CollectionManager.CollectedColor> getPrimaryColors() {
        java.util.ArrayList<CollectionManager.CollectedColor> result = new java.util.ArrayList<>();
        if (colors == null) return result;
        
        for (CollectionManager.CollectedColor cc : colors) {
            String name = cc.name;
            if (name.equals("Red") || name.equals("Blue") || name.equals("Yellow")) {
                result.add(cc);
            }
        }
        return result;
    }
    
    private List<CollectionManager.CollectedColor> getSecondaryColors() {
        java.util.ArrayList<CollectionManager.CollectedColor> result = new java.util.ArrayList<>();
        if (colors == null) return result;
        
        for (CollectionManager.CollectedColor cc : colors) {
            String name = cc.name;
            if (name.equals("Purple") || name.equals("Orange") || name.equals("Green")) {
                result.add(cc);
            }
        }
        return result;
    }
    
    private List<CollectionManager.CollectedColor> getLightShades() {
        java.util.ArrayList<CollectionManager.CollectedColor> result = new java.util.ArrayList<>();
        if (colors == null) return result;
        
        for (CollectionManager.CollectedColor cc : colors) {
            if (cc.name.startsWith("Light ")) {
                result.add(cc);
            }
        }
        return result;
    }
    
    private List<CollectionManager.CollectedColor> getDarkShades() {
        java.util.ArrayList<CollectionManager.CollectedColor> result = new java.util.ArrayList<>();
        if (colors == null) return result;
        
        for (CollectionManager.CollectedColor cc : colors) {
            if (cc.name.startsWith("Dark ")) {
                result.add(cc);
            }
        }
        return result;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        
        // Calculate height based on content
        float availableWidth = width - padding * 2 - padding * (columns - 1);
        float cardHeight = availableWidth / columns;
        
        // Estimate rows needed
        int totalCards = colors != null ? colors.size() : 0;
        int rows = (totalCards + columns - 1) / columns;
        rows = Math.max(rows, 3); // Minimum 3 rows
        
        // Header + progress + stats + 4 sections
        float contentHeight = 80 + 60 + 50 + (rows * (cardHeight + padding)) + 200;
        
        int height = Math.max((int) contentHeight, MeasureSpec.getSize(heightMeasureSpec));
        
        setMeasuredDimension(width, height);
    }
}
