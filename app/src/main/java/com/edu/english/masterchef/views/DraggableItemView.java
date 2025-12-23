package com.edu.english.masterchef.views;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.edu.english.masterchef.data.ZoneType;

/**
 * Individual draggable ingredient/tool view that can be placed on tray or in kitchen zones.
 */
public class DraggableItemView extends View {

    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint textPaint;
    
    private String itemId;
    private String itemName;
    private Drawable itemDrawable;
    private ZoneType currentZone = null; // null = tray
    private int stackCount = 1; // For stacked items (e.g., 3 eggs)
    
    private boolean isDragging = false;
    private boolean isSelected = false;
    
    public DraggableItemView(Context context) {
        super(context);
        init();
    }
    
    public DraggableItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public DraggableItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0xFFFFF8E1); // Light cream
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(0xFFBCAAA4); // Light brown
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF5D4037);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        setClickable(true);
        setFocusable(true);
        
        // Touch-to-drag: Start drag immediately when finger moves (no long-press required)
        setOnTouchListener(new OnTouchListener() {
            private float startX, startY;
            private boolean dragStarted = false;
            private static final int TOUCH_SLOP = 10; // pixels to move before drag starts
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        dragStarted = false;
                        return true; // Consume to receive further events
                        
                    case MotionEvent.ACTION_MOVE:
                        if (!dragStarted) {
                            float dx = Math.abs(event.getX() - startX);
                            float dy = Math.abs(event.getY() - startY);
                            // Start drag when moved beyond touch slop
                            if (dx > TOUCH_SLOP || dy > TOUCH_SLOP) {
                                dragStarted = true;
                                startDragOperation();
                            }
                        }
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // If not dragged, treat as click
                        if (!dragStarted) {
                            performClick();
                        }
                        dragStarted = false;
                        return true;
                }
                return false;
            }
        });
    }
    
    private void startDragOperation() {
        if (itemId == null) return;
        
        isDragging = true;
        
        // Create a custom drag shadow that's visible and properly sized
        DragShadowBuilder shadowBuilder = new DragShadowBuilder(this) {
            @Override
            public void onProvideShadowMetrics(android.graphics.Point outShadowSize, android.graphics.Point outShadowTouchPoint) {
                int width = getWidth();
                int height = getHeight();
                // Make shadow slightly larger for better visibility
                outShadowSize.set((int)(width * 1.1f), (int)(height * 1.1f));
                outShadowTouchPoint.set(outShadowSize.x / 2, outShadowSize.y / 2);
            }
            
            @Override
            public void onDrawShadow(android.graphics.Canvas canvas) {
                // Scale up slightly and draw with full opacity
                canvas.save();
                float scale = 1.1f;
                canvas.scale(scale, scale);
                DraggableItemView.this.draw(canvas);
                canvas.restore();
            }
        };
        
        // Create clip data with item info
        ClipData clipData = ClipData.newPlainText("itemId", itemId);
        
        // Create local state with zone info
        KitchenView.DragData dragData = new KitchenView.DragData(itemId, currentZone);
        
        // Start drag
        boolean dragStarted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dragStarted = startDragAndDrop(clipData, shadowBuilder, dragData, View.DRAG_FLAG_OPAQUE);
        } else {
            dragStarted = startDrag(clipData, shadowBuilder, dragData, 0);
        }
        
        // Make item semi-visible during drag (ghost effect)
        if (dragStarted) {
            setAlpha(0.4f);
            setScaleX(0.9f);
            setScaleY(0.9f);
        }
    }
    
    /**
     * Called when drag operation ends (regardless of success/failure)
     * Restores item to full visibility with smooth animation
     */
    public void onDragEnded() {
        isDragging = false;
        // Animate back to normal state
        animate()
            .alpha(1.0f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(150)
            .start();
        setVisibility(VISIBLE);
    }
    
    /**
     * Called when item was successfully placed in a zone
     * Can hide this item from tray if needed
     */
    public void onPlacedInZone(ZoneType zone) {
        this.currentZone = zone;
        isDragging = false;
        // Item stays in tray but can be visually marked as "used"
        // For now, just restore visibility
        setAlpha(1.0f);
        setScaleX(1.0f);
        setScaleY(1.0f);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultSize = (int) (80 * getResources().getDisplayMetrics().density);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            width = defaultSize;
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            height = defaultSize + 24; // Extra space for text
        }
        
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        float cornerRadius = 12f;
        
        // Calculate icon area - leave more room for text at bottom
        int textAreaHeight = 28; // Space for text label
        int iconAreaHeight = height - textAreaHeight;
        int padding = 10;
        float iconSize = Math.min(width - padding * 2, iconAreaHeight - padding * 2);
        
        // Draw background
        if (isSelected) {
            backgroundPaint.setColor(0xFFFFE082); // Highlight amber
            borderPaint.setColor(0xFFFF9800);
            borderPaint.setStrokeWidth(5f);
        } else {
            backgroundPaint.setColor(0xFFFFF8E1);
            borderPaint.setColor(0xFFBCAAA4);
            borderPaint.setStrokeWidth(3f);
        }
        
        canvas.drawRoundRect(4, 4, width - 4, height - 4, cornerRadius, cornerRadius, backgroundPaint);
        canvas.drawRoundRect(4, 4, width - 4, height - 4, cornerRadius, cornerRadius, borderPaint);
        
        // Draw icon - centered in icon area, properly scaled
        if (itemDrawable != null) {
            float iconLeft = (width - iconSize) / 2f;
            float iconTop = (iconAreaHeight - iconSize) / 2f;
            itemDrawable.setBounds(
                (int) iconLeft,
                (int) iconTop,
                (int) (iconLeft + iconSize),
                (int) (iconTop + iconSize)
            );
            itemDrawable.draw(canvas);
        }
        
        // Draw name
        if (itemName != null && !itemName.isEmpty()) {
            float textY = height - 8;
            canvas.drawText(itemName, width / 2f, textY, textPaint);
        }
        
        // Draw stack count badge if more than 1
        if (stackCount > 1) {
            Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgePaint.setColor(0xFFFF5722); // Orange badge
            
            Paint badgeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgeTextPaint.setColor(Color.WHITE);
            badgeTextPaint.setTextSize(20f);
            badgeTextPaint.setTextAlign(Paint.Align.CENTER);
            badgeTextPaint.setFakeBoldText(true);
            
            // Draw badge in top-right corner
            float badgeRadius = 14f;
            float badgeX = width - badgeRadius - 4;
            float badgeY = badgeRadius + 4;
            canvas.drawCircle(badgeX, badgeY, badgeRadius, badgePaint);
            
            // Draw count text
            String countText = String.valueOf(stackCount);
            canvas.drawText(countText, badgeX, badgeY + 7, badgeTextPaint);
        }
    }
    
    // Public API
    
    public void setItem(String id, String name, int drawableResId) {
        this.itemId = id;
        this.itemName = name;
        this.itemDrawable = ContextCompat.getDrawable(getContext(), drawableResId);
        invalidate();
    }
    
    public void setItem(String id, String name, Drawable drawable) {
        this.itemId = id;
        this.itemName = name;
        this.itemDrawable = drawable;
        invalidate();
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    /**
     * Get the drawable for this item (for rendering in zones)
     */
    public Drawable getItemDrawable() {
        return itemDrawable;
    }
    
    public ZoneType getCurrentZone() {
        return currentZone;
    }
    
    public void setCurrentZone(ZoneType zone) {
        this.currentZone = zone;
    }
    
    public void setSelected(boolean selected) {
        if (this.isSelected != selected) {
            this.isSelected = selected;
            invalidate();
        }
    }
    
    public boolean isItemSelected() {
        return isSelected;
    }
    
    /**
     * Set the stack count for this item (e.g., 3 eggs)
     * Shows a badge with the count when count > 1
     */
    public void setStackCount(int count) {
        this.stackCount = Math.max(1, count);
        invalidate();
    }
    
    public int getStackCount() {
        return stackCount;
    }
    
    /**
     * Decrease stack count by 1 (when item is used)
     * Returns the new count
     */
    public int decrementStack() {
        if (stackCount > 1) {
            stackCount--;
            invalidate();
        }
        return stackCount;
    }
}
