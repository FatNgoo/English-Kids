package com.edu.english.coloralchemy;

/**
 * Shade Controller
 * Manages the lightness slider for adjusting color shades
 */
public class ShadeController {
    
    // Position and dimensions
    private float x, y;
    private float width, height;
    private float trackHeight;
    
    // Slider value
    private float value; // -1.0 (dark) to 1.0 (light), 0 = original
    private float targetValue;
    
    // State
    private boolean isDragging;
    private boolean isVisible;
    private boolean isEnabled;
    
    // Animation
    private float alpha;
    private float targetAlpha;
    private float handleScale;
    private float handleTargetScale;
    
    // Callback
    public interface OnShadeChangeListener {
        void onShadeChanged(float shadeValue, String shadeName);
        void onShadeChangeStart();
        void onShadeChangeEnd();
    }
    
    private OnShadeChangeListener listener;
    private String baseColorName;
    
    public ShadeController(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = 80;
        this.trackHeight = 16;
        
        this.value = 0;
        this.targetValue = 0;
        
        this.isDragging = false;
        this.isVisible = false;
        this.isEnabled = false;
        
        this.alpha = 0;
        this.targetAlpha = 0;
        this.handleScale = 1.0f;
        this.handleTargetScale = 1.0f;
        
        this.baseColorName = "";
    }
    
    /**
     * Update controller state
     */
    public void update(float deltaTime) {
        // Smooth value animation
        if (!isDragging) {
            value = EasingFunctions.lerp(value, targetValue, deltaTime * 8f);
        }
        
        // Alpha animation
        alpha = EasingFunctions.lerp(alpha, targetAlpha, deltaTime * 5f);
        
        // Handle scale animation
        handleScale = EasingFunctions.lerp(handleScale, handleTargetScale, deltaTime * 12f);
    }
    
    /**
     * Show the controller
     */
    public void show(String colorName) {
        this.baseColorName = colorName;
        this.isVisible = true;
        this.isEnabled = true;
        this.targetAlpha = 1.0f;
        this.value = 0;
        this.targetValue = 0;
    }
    
    /**
     * Hide the controller
     */
    public void hide() {
        this.isVisible = false;
        this.isEnabled = false;
        this.targetAlpha = 0;
    }
    
    /**
     * Handle touch start
     */
    public boolean onTouchDown(float touchX, float touchY) {
        if (!isEnabled || !isVisible || alpha < 0.5f) return false;
        
        // Check if touch is on handle
        float handleX = getHandleX();
        float handleY = y;
        float handleRadius = 25;
        
        float dx = touchX - handleX;
        float dy = touchY - handleY;
        
        if (dx * dx + dy * dy <= handleRadius * handleRadius * 4) {
            isDragging = true;
            handleTargetScale = 1.3f;
            
            if (listener != null) {
                listener.onShadeChangeStart();
            }
            return true;
        }
        
        // Check if touch is on track
        if (touchX >= x - width / 2 && touchX <= x + width / 2 &&
            touchY >= y - height / 2 && touchY <= y + height / 2) {
            
            isDragging = true;
            handleTargetScale = 1.3f;
            updateValueFromTouch(touchX);
            
            if (listener != null) {
                listener.onShadeChangeStart();
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle touch move
     */
    public void onTouchMove(float touchX, float touchY) {
        if (!isDragging) return;
        updateValueFromTouch(touchX);
    }
    
    /**
     * Handle touch end
     */
    public void onTouchUp() {
        if (isDragging) {
            isDragging = false;
            handleTargetScale = 1.0f;
            
            if (listener != null) {
                listener.onShadeChangeEnd();
            }
        }
    }
    
    /**
     * Update value based on touch position
     */
    private void updateValueFromTouch(float touchX) {
        float trackLeft = x - width / 2 + 30;
        float trackRight = x + width / 2 - 30;
        float trackWidth = trackRight - trackLeft;
        
        // Map touch to value
        float normalizedX = (touchX - trackLeft) / trackWidth;
        normalizedX = EasingFunctions.clamp(normalizedX, 0, 1);
        
        // Convert to -1 to 1 range (left = light, right = dark, center = normal)
        // Actually: left = light (+1), center = normal (0), right = dark (-1)
        value = 1 - normalizedX * 2;
        targetValue = value;
        
        // Notify listener
        if (listener != null) {
            String shadeName = ColorMixer.getShadeName(baseColorName, value);
            listener.onShadeChanged(value, shadeName);
        }
    }
    
    /**
     * Get handle X position based on current value
     */
    public float getHandleX() {
        float trackLeft = x - width / 2 + 30;
        float trackRight = x + width / 2 - 30;
        float trackWidth = trackRight - trackLeft;
        
        // Convert value to position
        float normalizedValue = (1 - value) / 2; // -1..1 -> 0..1
        return trackLeft + normalizedValue * trackWidth;
    }
    
    /**
     * Get handle Y position
     */
    public float getHandleY() {
        return y;
    }
    
    /**
     * Reset value to center
     */
    public void reset() {
        targetValue = 0;
        isDragging = false;
        handleTargetScale = 1.0f;
    }
    
    // ==================== Getters & Setters ====================
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getTrackHeight() { return trackHeight; }
    
    public float getValue() { return value; }
    public float getAlpha() { return alpha; }
    public float getHandleScale() { return handleScale; }
    
    public boolean isVisible() { return isVisible && alpha > 0.01f; }
    public boolean isDragging() { return isDragging; }
    public boolean isEnabled() { return isEnabled; }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void setListener(OnShadeChangeListener listener) {
        this.listener = listener;
    }
    
    public String getBaseColorName() { return baseColorName; }
}
