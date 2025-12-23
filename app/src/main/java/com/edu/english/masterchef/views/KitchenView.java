package com.edu.english.masterchef.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.edu.english.masterchef.data.ActionType;
import com.edu.english.masterchef.data.Ingredient;
import com.edu.english.masterchef.data.Tool;
import com.edu.english.masterchef.data.ZoneType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom View that renders the kitchen zones and handles drag-drop, tap, swipe gestures.
 * This is the main gameplay area.
 * 
 * ENHANCED GESTURE DETECTION:
 * - Circular stir: Track circular motion in POT/PAN zones
 * - Pan shake: Detect rapid horizontal swipes in PAN zone  
 * - Long press pour: Hold to pour liquids with progress indicator
 * - Tap to cut: Count taps in BOARD zone for cutting actions
 * - Visual feedback: Highlight, animations, haptic feedback
 */
public class KitchenView extends View {

    // Zone rectangles (computed in onSizeChanged)
    private final Map<ZoneType, RectF> zoneRects = new HashMap<>();
    
    // Items currently in each zone
    private final Map<ZoneType, List<KitchenItem>> zoneItems = new HashMap<>();
    
    // Paint objects
    private Paint zonePaint;
    private Paint zoneBorderPaint;
    private Paint highlightPaint;
    private Paint textPaint;
    private Paint labelBgPaint;
    
    // Enhanced gesture tracking
    private Paint progressPaint;
    private Paint gestureTrailPaint;
    
    // Gesture detector for taps and swipes
    private GestureDetector gestureDetector;
    
    // Current highlighted zone (during drag or action)
    private ZoneType highlightedZone = null;
    
    // Listener for kitchen events
    private KitchenEventListener listener;
    
    // ========== ENHANCED GESTURE STATE ==========
    // Circular stir detection
    private List<PointF> stirPath = new ArrayList<>();
    private float stirAngle = 0f;
    private int stirCount = 0;
    private long lastStirTime = 0;
    private static final float STIR_MIN_RADIUS = 30f;
    private static final float STIR_ANGLE_THRESHOLD = 300f; // degrees for one full stir
    
    // Pan shake detection
    private int shakeCount = 0;
    private long lastShakeTime = 0;
    private float lastShakeX = 0f;
    private int shakeDirection = 0; // -1 left, 1 right, 0 none
    private static final float SHAKE_DISTANCE_THRESHOLD = 60f;
    private static final long SHAKE_TIME_WINDOW = 1000; // 1 second
    
    // Pour/hold detection
    private boolean isPouring = false;
    private long pourStartTime = 0;
    private float pourProgress = 0f;
    private ZoneType pouringZone = null;
    private ValueAnimator pourAnimator;
    
    // Tap counting
    private int tapCount = 0;
    private long lastTapTime = 0;
    private ZoneType lastTapZone = null;
    private static final long TAP_COMBO_WINDOW = 800; // 800ms between taps for combo
    
    // Current action mode (set by game engine)
    private ActionType currentAction = null;
    private ZoneType targetActionZone = null;
    private int requiredTaps = 0;
    private int requiredStirs = 0;
    private int requiredShakes = 0;
    private long requiredPourTime = 0;
    
    // Visual feedback state
    private float highlightPulse = 0f;
    private ValueAnimator pulseAnimator;
    private float actionProgress = 0f;
    private List<PointF> gestureTrail = new ArrayList<>();
    private boolean showGestureTrail = false;
    
    // Zone colors - more vibrant for kids (GAME-LIKE KITCHEN STYLE)
    private static final int COLOR_FRIDGE = 0xFFE3F2FD;   // Light blue (fridge)
    private static final int COLOR_COUNTER = 0xFFBCAAA4;  // Light brown (granite countertop)
    private static final int COLOR_BOARD = 0xFFD7B98E;    // Wood cutting board
    private static final int COLOR_PAN = 0xFF37474F;      // Dark gray (cast iron pan)
    private static final int COLOR_POT = 0xFF455A64;      // Stainless pot
    private static final int COLOR_PLATE = 0xFFFFFBF5;    // White ceramic plate
    private static final int COLOR_TOOLS = 0xFFECEFF1;    // Light gray (tool rack)
    private static final int COLOR_HIGHLIGHT = 0xFF4CAF50; // Green highlight
    private static final int COLOR_HIGHLIGHT_GLOW = 0x664CAF50; // Semi-transparent green
    
    public interface KitchenEventListener {
        void onItemDropped(String itemId, ZoneType fromZone, ZoneType toZone);
        void onZoneTapped(ZoneType zone);
        void onZoneSwiped(ZoneType zone, float velocityX, float velocityY);
        void onZoneLongPressed(ZoneType zone);
        
        // Enhanced gesture callbacks
        default void onStirProgress(ZoneType zone, int stirCount, float progress) {}
        default void onStirComplete(ZoneType zone, int totalStirs) {}
        default void onShakeProgress(ZoneType zone, int shakeCount, float progress) {}
        default void onShakeComplete(ZoneType zone, int totalShakes) {}
        default void onPourProgress(ZoneType zone, float progress) {}
        default void onPourComplete(ZoneType zone, long duration) {}
        default void onTapProgress(ZoneType zone, int tapCount, float progress) {}
        default void onTapComplete(ZoneType zone, int totalTaps) {}
        
        /** Called when hold gesture is released (finger up) - for cleanup */
        default void onHoldReleased(ZoneType zone) {}
        
        /** Called when item is successfully placed in a zone */
        default void onItemPlacedInZone(String itemId, ZoneType zone, android.graphics.drawable.Drawable drawable) {}
    }
    
    public KitchenView(Context context) {
        super(context);
        init();
    }
    
    public KitchenView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public KitchenView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Initialize paints
        zonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zonePaint.setStyle(Paint.Style.FILL);
        
        zoneBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zoneBorderPaint.setStyle(Paint.Style.STROKE);
        zoneBorderPaint.setStrokeWidth(4f);
        zoneBorderPaint.setColor(0xFF8D6E63);
        
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(12f);
        highlightPaint.setColor(COLOR_HIGHLIGHT);
        // Add glow effect via blur
        highlightPaint.setShadowLayer(16f, 0, 0, COLOR_HIGHLIGHT);
        setLayerType(LAYER_TYPE_SOFTWARE, highlightPaint); // Required for shadow
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF5D4037);
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        labelBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelBgPaint.setColor(0xCCFFFFFF);
        labelBgPaint.setStyle(Paint.Style.FILL);
        
        // Enhanced gesture paints
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(8f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(0xFF4CAF50);
        
        gestureTrailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gestureTrailPaint.setStyle(Paint.Style.STROKE);
        gestureTrailPaint.setStrokeWidth(6f);
        gestureTrailPaint.setStrokeCap(Paint.Cap.ROUND);
        gestureTrailPaint.setColor(0x80FFEB3B); // Semi-transparent yellow trail
        
        // Initialize zone items map
        for (ZoneType zone : ZoneType.values()) {
            zoneItems.put(zone, new ArrayList<>());
        }
        
        // Set up gesture detector with enhanced detection
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                ZoneType zone = getZoneAt(e.getX(), e.getY());
                if (zone != null) {
                    handleTap(zone, e.getX(), e.getY());
                }
                return true;
            }
            
            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null) return false;
                ZoneType zone = getZoneAt(e1.getX(), e1.getY());
                if (zone != null) {
                    handleSwipe(zone, velocityX, velocityY);
                }
                return true;
            }
            
            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                ZoneType zone = getZoneAt(e.getX(), e.getY());
                android.util.Log.d("KitchenView", "onLongPress: x=" + e.getX() + ", y=" + e.getY() + 
                    " -> zone=" + zone + ", currentAction=" + currentAction + ", targetActionZone=" + targetActionZone);
                if (zone != null) {
                    // Handle POUR action (uses pourAnimator)
                    if (currentAction == ActionType.POUR) {
                        startPouring(zone);
                    } 
                    // Handle other hold actions (OVEN_PREHEAT_HOLD, GRILL_ON_HOLD, etc.)
                    // These use holdTimerRunnable in gameEngine
                    else if (currentAction != null && currentAction.isHoldAction()) {
                        android.util.Log.d("KitchenView", "Hold action detected! Notifying listener");
                        // Notify listener to start hold timer in game engine
                        if (listener != null) {
                            listener.onZoneLongPressed(zone);
                        }
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    } else {
                        android.util.Log.d("KitchenView", "NOT a hold action: currentAction=" + currentAction + 
                            ", isHoldAction=" + (currentAction != null ? currentAction.isHoldAction() : "null"));
                    }
                }
            }
            
            @Override
            public boolean onDown(MotionEvent e) {
                // Start tracking gesture path for circular detection
                gestureTrail.clear();
                stirPath.clear();
                showGestureTrail = false;
                return true;
            }
            
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (e1 == null || e2 == null) return false;
                
                ZoneType zone = getZoneAt(e1.getX(), e1.getY());
                if (zone == null) return false;
                
                // Track gesture trail
                PointF point = new PointF(e2.getX(), e2.getY());
                gestureTrail.add(point);
                
                // Keep trail limited
                if (gestureTrail.size() > 50) {
                    gestureTrail.remove(0);
                }
                
                // Check for circular motion (stir)
                if (currentAction == ActionType.SWIPE_TO_STIR) {
                    trackCircularMotion(zone, e2.getX(), e2.getY());
                    showGestureTrail = true;
                }
                
                // Check for horizontal shake
                if (currentAction == ActionType.SHAKE_PAN || currentAction == ActionType.SHAKE_BASKET) {
                    trackShakeMotion(zone, e2.getX(), e2.getY());
                    showGestureTrail = true;
                }
                
                invalidate();
                return true;
            }
        });
        
        // Set up drag listener
        setOnDragListener(this::handleDrag);
        
        // Start pulse animation for highlights
        startPulseAnimation();
    }
    
    /**
     * Start pulsing animation for highlighted zones
     */
    private void startPulseAnimation() {
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            highlightPulse = (float) animation.getAnimatedValue();
            if (highlightedZone != null) {
                invalidate();
            }
        });
        pulseAnimator.start();
    }
    
    // ========== ENHANCED TAP HANDLING ==========
    
    private void handleTap(ZoneType zone, float x, float y) {
        long now = System.currentTimeMillis();
        
        // Check if this is a combo tap (same zone, within time window)
        if (zone == lastTapZone && (now - lastTapTime) < TAP_COMBO_WINDOW) {
            tapCount++;
        } else {
            tapCount = 1;
            lastTapZone = zone;
        }
        lastTapTime = now;
        
        // Haptic feedback
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        
        // Calculate progress
        float progress = requiredTaps > 0 ? (float) tapCount / requiredTaps : 0f;
        actionProgress = Math.min(1f, progress);
        
        // Notify listener
        if (listener != null) {
            listener.onZoneTapped(zone);
            
            // Handle all tap-based actions
            if (isTapAction(currentAction)) {
                listener.onTapProgress(zone, tapCount, actionProgress);
                
                if (tapCount >= requiredTaps && requiredTaps > 0) {
                    listener.onTapComplete(zone, tapCount);
                    // Visual success
                    showActionSuccess(zone);
                    tapCount = 0;
                }
            }
        }
        
        invalidate();
    }
    
    // ========== ENHANCED STIR (CIRCULAR) DETECTION ==========
    
    private void trackCircularMotion(ZoneType zone, float x, float y) {
        stirPath.add(new PointF(x, y));
        
        // Need at least 10 points to detect circular motion
        if (stirPath.size() < 10) return;
        
        // Keep only recent points
        while (stirPath.size() > 50) {
            stirPath.remove(0);
        }
        
        // Calculate center of the path
        float centerX = 0, centerY = 0;
        for (PointF p : stirPath) {
            centerX += p.x;
            centerY += p.y;
        }
        centerX /= stirPath.size();
        centerY /= stirPath.size();
        
        // Calculate cumulative angle change
        float totalAngle = 0f;
        for (int i = 1; i < stirPath.size(); i++) {
            PointF prev = stirPath.get(i - 1);
            PointF curr = stirPath.get(i);
            
            float angle1 = (float) Math.atan2(prev.y - centerY, prev.x - centerX);
            float angle2 = (float) Math.atan2(curr.y - centerY, curr.x - centerX);
            
            float delta = angle2 - angle1;
            // Normalize to -PI to PI
            if (delta > Math.PI) delta -= 2 * Math.PI;
            if (delta < -Math.PI) delta += 2 * Math.PI;
            
            totalAngle += Math.abs(delta);
        }
        
        stirAngle = (float) Math.toDegrees(totalAngle);
        
        // Check if we completed a stir (360 degree rotation)
        if (stirAngle >= STIR_ANGLE_THRESHOLD) {
            stirCount++;
            stirPath.clear();
            stirAngle = 0f;
            lastStirTime = System.currentTimeMillis();
            
            // Haptic feedback for stir
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            
            // Calculate progress
            float progress = requiredStirs > 0 ? (float) stirCount / requiredStirs : 0f;
            actionProgress = Math.min(1f, progress);
            
            // Notify listener
            if (listener != null) {
                listener.onStirProgress(zone, stirCount, actionProgress);
                
                if (stirCount >= requiredStirs && requiredStirs > 0) {
                    listener.onStirComplete(zone, stirCount);
                    showActionSuccess(zone);
                    stirCount = 0;
                }
            }
        }
    }
    
    // ========== SHAKE (HORIZONTAL SWIPE) DETECTION ==========
    
    private void trackShakeMotion(ZoneType zone, float x, float y) {
        long now = System.currentTimeMillis();
        
        // Reset shake count if too much time passed
        if (now - lastShakeTime > SHAKE_TIME_WINDOW) {
            shakeCount = 0;
            shakeDirection = 0;
        }
        
        float deltaX = x - lastShakeX;
        
        if (Math.abs(deltaX) > SHAKE_DISTANCE_THRESHOLD) {
            int newDirection = deltaX > 0 ? 1 : -1;
            
            // Count a shake when direction reverses
            if (shakeDirection != 0 && newDirection != shakeDirection) {
                shakeCount++;
                lastShakeTime = now;
                
                // Haptic feedback for shake
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                
                // Calculate progress
                float progress = requiredShakes > 0 ? (float) shakeCount / requiredShakes : 0f;
                actionProgress = Math.min(1f, progress);
                
                // Notify listener
                if (listener != null) {
                    listener.onShakeProgress(zone, shakeCount, actionProgress);
                    
                    if (shakeCount >= requiredShakes && requiredShakes > 0) {
                        listener.onShakeComplete(zone, shakeCount);
                        showActionSuccess(zone);
                        shakeCount = 0;
                    }
                }
            }
            
            shakeDirection = newDirection;
            lastShakeX = x;
        }
    }
    
    private void handleSwipe(ZoneType zone, float velocityX, float velocityY) {
        // Haptic feedback
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        
        if (listener != null) {
            listener.onZoneSwiped(zone, velocityX, velocityY);
        }
    }
    
    // ========== POUR (LONG PRESS HOLD) DETECTION ==========
    
    private void startPouring(ZoneType zone) {
        if (currentAction != ActionType.POUR || isPouring) return;
        
        isPouring = true;
        pouringZone = zone;
        pourStartTime = System.currentTimeMillis();
        pourProgress = 0f;
        
        // Haptic feedback for start
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        
        // Animate pour progress
        if (pourAnimator != null) {
            pourAnimator.cancel();
        }
        
        pourAnimator = ValueAnimator.ofFloat(0f, 1f);
        pourAnimator.setDuration(requiredPourTime > 0 ? requiredPourTime : 2000);
        pourAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pourAnimator.addUpdateListener(animation -> {
            pourProgress = (float) animation.getAnimatedValue();
            actionProgress = pourProgress;
            
            if (listener != null) {
                listener.onPourProgress(pouringZone, pourProgress);
            }
            
            invalidate();
        });
        pourAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isPouring && pourProgress >= 0.95f) {
                    long duration = System.currentTimeMillis() - pourStartTime;
                    if (listener != null) {
                        listener.onPourComplete(pouringZone, duration);
                    }
                    showActionSuccess(pouringZone);
                }
                isPouring = false;
                pouringZone = null;
                pourProgress = 0f;
            }
        });
        pourAnimator.start();
        
        // Notify listener that pour started (for UI feedback)
        if (listener != null) {
            listener.onZoneLongPressed(zone);
        }
    }
    
    /**
     * Stop pouring (when finger lifted)
     */
    public void stopPouring() {
        if (isPouring && pourAnimator != null) {
            pourAnimator.cancel();
        }
        isPouring = false;
        pourProgress = 0f;
        invalidate();
    }
    
    // ========== VISUAL FEEDBACK ==========
    
    private void showActionSuccess(ZoneType zone) {
        // Strong haptic feedback
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(100);
            }
        }
        
        // Flash highlight
        final ZoneType originalHighlight = highlightedZone;
        highlightedZone = zone;
        
        postDelayed(() -> {
            highlightedZone = originalHighlight;
            invalidate();
        }, 300);
        
        invalidate();
    }
    
    // ========== ACTION MODE SETUP ==========
    
    /**
     * Configure the view for a specific action
     */
    public void setCurrentAction(ActionType action, ZoneType targetZone, int requiredCount, long duration) {
        this.currentAction = action;
        this.targetActionZone = targetZone;
        this.actionProgress = 0f;
        
        // Reset gesture state
        tapCount = 0;
        stirCount = 0;
        shakeCount = 0;
        stirPath.clear();
        gestureTrail.clear();
        
        switch (action) {
            case TAP_TO_CUT:
            case TAP_TO_POUND:
            case TAP_TO_CRACK:
            case TAP_TO_DRIZZLE:
                this.requiredTaps = requiredCount;
                break;
            case SWIPE_TO_STIR:
                this.requiredStirs = requiredCount;
                break;
            case SHAKE_PAN:
            case SHAKE_BASKET:
            case SEASON_SHAKE:
                this.requiredShakes = requiredCount;
                break;
            case POUR:
                this.requiredPourTime = duration;
                break;
        }
        
        // Highlight target zone
        highlightZone(targetZone);
        invalidate();
    }
    
    /**
     * Clear current action mode
     */
    public void clearAction() {
        this.currentAction = null;
        this.targetActionZone = null;
        this.actionProgress = 0f;
        clearHighlight();
        stopPouring();
        gestureTrail.clear();
        showGestureTrail = false;
        invalidate();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        android.util.Log.d("KitchenView", "onSizeChanged: " + w + "x" + h + " (old: " + oldw + "x" + oldh + ")");
        computeZoneRects(w, h);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        android.util.Log.d("KitchenView", "onMeasure: " + w + "x" + h);
        
        // Ensure minimum height for zones to be visible
        if (h < 200) {
            android.util.Log.w("KitchenView", "Height too small! Setting minimum 400dp");
            int minHeight = (int) (400 * getResources().getDisplayMetrics().density);
            setMeasuredDimension(w, Math.max(h, minHeight));
        }
    }
    
    private void computeZoneRects(int width, int height) {
        zoneRects.clear();
        
        float padding = 16f;
        
        // ========== REALISTIC KITCHEN LAYOUT ==========
        // Layout giống bếp thật:
        // - Top: Countertop area (cutting board ở giữa)
        // - Middle: Stove area (pan trái, pot phải trên burners)
        // - Bottom left: Fridge (dọc, cao)
        // - Bottom right: Serve plate
        // - Tools: floating drawer (không phải zone riêng)
        
        float countertopTop = height * 0.05f;
        float countertopHeight = height * 0.28f;
        float stoveTop = countertopTop + countertopHeight + padding;
        float stoveHeight = height * 0.35f;
        float bottomTop = stoveTop + stoveHeight + padding;
        float bottomHeight = height - bottomTop - padding;
        
        // COUNTER_ZONE: Small staging area top-left
        float counterWidth = width * 0.25f;
        zoneRects.put(ZoneType.COUNTER_ZONE, new RectF(
            padding, countertopTop,
            padding + counterWidth, countertopTop + countertopHeight
        ));
        
        // BOARD_ZONE: Cutting board - center of countertop (main work area)
        float boardWidth = width * 0.40f;
        float boardLeft = (width - boardWidth) / 2f;
        zoneRects.put(ZoneType.BOARD_ZONE, new RectF(
            boardLeft, countertopTop + 10f,
            boardLeft + boardWidth, countertopTop + countertopHeight - 10f
        ));
        
        // TOOL_ZONE: Small tools area top-right
        zoneRects.put(ZoneType.TOOL_ZONE, new RectF(
            width - padding - counterWidth, countertopTop,
            width - padding, countertopTop + countertopHeight
        ));
        
        // STOVE AREA: Pan left, Pot right (on burners)
        float stoveZoneWidth = (width - 3 * padding) / 2f;
        
        // PAN_ZONE: Left burner
        zoneRects.put(ZoneType.PAN_ZONE, new RectF(
            padding, stoveTop,
            padding + stoveZoneWidth, stoveTop + stoveHeight
        ));
        
        // POT_ZONE: Right burner
        zoneRects.put(ZoneType.POT_ZONE, new RectF(
            padding * 2 + stoveZoneWidth, stoveTop,
            width - padding, stoveTop + stoveHeight
        ));
        
        // BOTTOM ROW: Fridge (tall, left) and Serve Plate (right)
        float fridgeWidth = width * 0.30f;
        
        // FRIDGE_ZONE: Tall fridge on left
        zoneRects.put(ZoneType.FRIDGE_ZONE, new RectF(
            padding, bottomTop,
            padding + fridgeWidth, height - padding
        ));
        
        // SERVE_ZONE: Serving plate on right
        float serveWidth = width * 0.45f;
        zoneRects.put(ZoneType.SERVE_ZONE, new RectF(
            width - padding - serveWidth, bottomTop,
            width - padding, height - padding
        ));
        
        // ========== ADDITIONAL COOKING ZONES (share positions with basic zones) ==========
        // These zones overlay on existing zones when the recipe needs them
        
        // GRILL_ZONE: Overlays on PAN_ZONE position (left burner)
        RectF panRect = zoneRects.get(ZoneType.PAN_ZONE);
        if (panRect != null) {
            zoneRects.put(ZoneType.GRILL_ZONE, new RectF(panRect));
        }
        
        // OVEN_ZONE: Overlays on POT_ZONE position (right side)
        RectF potRect = zoneRects.get(ZoneType.POT_ZONE);
        if (potRect != null) {
            zoneRects.put(ZoneType.OVEN_ZONE, new RectF(potRect));
        }
        
        // STEAMER_ZONE: Also overlays on POT_ZONE position
        if (potRect != null) {
            zoneRects.put(ZoneType.STEAMER_ZONE, new RectF(potRect));
        }
        
        // FRYER_ZONE: Overlays on PAN_ZONE position
        if (panRect != null) {
            zoneRects.put(ZoneType.FRYER_ZONE, new RectF(panRect));
        }
        
        // MIXING_ZONE: Overlays on BOARD_ZONE
        RectF boardRect = zoneRects.get(ZoneType.BOARD_ZONE);
        if (boardRect != null) {
            zoneRects.put(ZoneType.MIXING_ZONE, new RectF(boardRect));
        }
        
        // DRAIN_ZONE: Small zone overlaying COUNTER_ZONE
        RectF counterRect = zoneRects.get(ZoneType.COUNTER_ZONE);
        if (counterRect != null) {
            zoneRects.put(ZoneType.DRAIN_ZONE, new RectF(counterRect));
        }
        
        // GARNISH_ZONE: Overlays SERVE_ZONE
        RectF serveRect = zoneRects.get(ZoneType.SERVE_ZONE);
        if (serveRect != null) {
            zoneRects.put(ZoneType.GARNISH_ZONE, new RectF(serveRect));
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Debug: Log zone count
        if (zoneRects.isEmpty()) {
            android.util.Log.w("KitchenView", "onDraw: NO ZONES! Width=" + getWidth() + " Height=" + getHeight());
            // Draw debug message
            Paint debugPaint = new Paint();
            debugPaint.setColor(0xFFFF5722);
            debugPaint.setTextSize(40f);
            debugPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("⚠️ Kitchen View - No zones computed", getWidth() / 2f, getHeight() / 2f, debugPaint);
            canvas.drawText("Width=" + getWidth() + " Height=" + getHeight(), getWidth() / 2f, getHeight() / 2f + 50, debugPaint);
            return;
        }
        
        // ========== DRAW STOVE DECORATIONS (BEHIND ZONES) ==========
        drawKitchenDecorations(canvas);
        
        // Define base zones that are always visible
        java.util.Set<ZoneType> baseZones = java.util.EnumSet.of(
            ZoneType.FRIDGE_ZONE, ZoneType.COUNTER_ZONE, ZoneType.BOARD_ZONE,
            ZoneType.PAN_ZONE, ZoneType.POT_ZONE, ZoneType.SERVE_ZONE, ZoneType.TOOL_ZONE
        );
        
        // Overlay zones that replace base zones when targeted
        java.util.Map<ZoneType, ZoneType> overlayToBase = new java.util.HashMap<>();
        overlayToBase.put(ZoneType.OVEN_ZONE, ZoneType.POT_ZONE);
        overlayToBase.put(ZoneType.STEAMER_ZONE, ZoneType.POT_ZONE);
        overlayToBase.put(ZoneType.GRILL_ZONE, ZoneType.PAN_ZONE);
        overlayToBase.put(ZoneType.FRYER_ZONE, ZoneType.PAN_ZONE);
        overlayToBase.put(ZoneType.MIXING_ZONE, ZoneType.BOARD_ZONE);
        overlayToBase.put(ZoneType.DRAIN_ZONE, ZoneType.COUNTER_ZONE);
        overlayToBase.put(ZoneType.GARNISH_ZONE, ZoneType.SERVE_ZONE);
        
        // Determine which zones to skip (base zones replaced by overlay)
        java.util.Set<ZoneType> skipZones = new java.util.HashSet<>();
        if (targetActionZone != null && overlayToBase.containsKey(targetActionZone)) {
            skipZones.add(overlayToBase.get(targetActionZone));
        }
        if (highlightedZone != null && overlayToBase.containsKey(highlightedZone)) {
            skipZones.add(overlayToBase.get(highlightedZone));
        }
        
        // Draw each zone - use transparent overlays instead of solid colors
        for (Map.Entry<ZoneType, RectF> entry : zoneRects.entrySet()) {
            ZoneType zone = entry.getKey();
            RectF rect = entry.getValue();
            
            // Skip overlay zones that are not currently targeted or highlighted
            if (!baseZones.contains(zone)) {
                // This is an overlay zone - only draw if it's the target or highlighted
                if (zone != targetActionZone && zone != highlightedZone) {
                    continue;
                }
            }
            
            // Skip base zones that are replaced by active overlay
            if (skipZones.contains(zone)) {
                continue;
            }
            
            // Skip background for main zones (decorations already drawn)
            // Only draw subtle overlay for interactive zones
            if (zone == ZoneType.COUNTER_ZONE || zone == ZoneType.TOOL_ZONE) {
                // These zones get very subtle backgrounds
                zonePaint.setColor(0x20FFFFFF);
                canvas.drawRoundRect(rect, 16f, 16f, zonePaint);
                zonePaint.setColor(0x40000000);
                zonePaint.setStyle(android.graphics.Paint.Style.STROKE);
                zonePaint.setStrokeWidth(1f);
                canvas.drawRoundRect(rect, 16f, 16f, zonePaint);
                zonePaint.setStyle(android.graphics.Paint.Style.FILL);
            }
            
            // Draw highlight if this zone is highlighted (during drag) with pulsing effect
            if (zone == highlightedZone) {
                // Pulsing glow intensity - LIGHT GREEN for valid drop zone
                int alpha = (int) (80 + 60 * highlightPulse);
                highlightPaint.setColor(Color.argb(alpha, 129, 199, 132)); // Light green #81C784
                highlightPaint.setStyle(Paint.Style.FILL);
                canvas.drawRoundRect(rect, 16f, 16f, highlightPaint);
                
                // Pulsing border - bright green
                float strokeWidth = 4f + 4f * highlightPulse;
                highlightPaint.setStyle(Paint.Style.STROKE);
                highlightPaint.setStrokeWidth(strokeWidth);
                highlightPaint.setColor(0xFF4CAF50); // Green border
                canvas.drawRoundRect(rect, 16f, 16f, highlightPaint);
                highlightPaint.setStyle(Paint.Style.FILL);
                
                // Draw "Drop here!" tooltip above the zone
                drawDropHereTooltip(canvas, rect);
            }
            
            // Draw zone label (smaller, positioned at corner)
            drawZoneLabelCompact(canvas, zone, rect);
            
            // Draw items in zone
            drawZoneItems(canvas, zone, rect);
            
            // Draw action progress indicator on target zone
            if (zone == targetActionZone && actionProgress > 0) {
                drawActionProgress(canvas, rect, actionProgress);
            }
            
            // Draw TAP indicator if this is a tap action target zone
            if (zone == targetActionZone && isTapAction(currentAction)) {
                drawTapTargetIndicator(canvas, rect);
            }
            
            // Draw HOLD indicator for hold actions (GRILL_ON_HOLD, OVEN_PREHEAT_HOLD)
            if (zone == targetActionZone && isHoldAction(currentAction)) {
                drawHoldTargetIndicator(canvas, rect);
            }
            
            // Draw POUR indicator for pour action
            if (zone == targetActionZone && currentAction == ActionType.POUR) {
                drawPourTargetIndicator(canvas, rect);
            }
        }
        
        // ========== DRAW GESTURE TRAIL ==========
        if (showGestureTrail && gestureTrail.size() > 1) {
            drawGestureTrail(canvas);
        }
    }
    
    /**
     * Draw circular progress indicator around a zone
     */
    private void drawActionProgress(Canvas canvas, RectF rect, float progress) {
        // Draw progress arc around the zone
        float padding = 6f;
        RectF arcRect = new RectF(
            rect.left - padding, rect.top - padding,
            rect.right + padding, rect.bottom + padding
        );
        
        // Background arc (gray)
        progressPaint.setColor(0x40000000);
        progressPaint.setStrokeWidth(8f);
        canvas.drawArc(arcRect, 0, 360, false, progressPaint);
        
        // Progress arc (green gradient based on progress)
        int progressColor;
        if (progress < 0.5f) {
            progressColor = 0xFF4CAF50; // Green
        } else if (progress < 0.8f) {
            progressColor = 0xFF8BC34A; // Light green
        } else {
            progressColor = 0xFFFFEB3B; // Yellow (almost done)
        }
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeWidth(10f);
        canvas.drawArc(arcRect, -90, progress * 360, false, progressPaint);
        
        // Draw progress text in center
        String progressText = String.format("%.0f%%", progress * 100);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setShadowLayer(4f, 2f, 2f, 0x80000000);
        
        // Position text
        float textX = rect.centerX();
        float textY = rect.top - 16f;
        
        // Background for text
        float textWidth = textPaint.measureText(progressText);
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xCC000000);
        canvas.drawRoundRect(new RectF(
            textX - textWidth / 2 - 8, textY - 24,
            textX + textWidth / 2 + 8, textY + 6
        ), 8f, 8f, bgPaint);
        
        canvas.drawText(progressText, textX, textY, textPaint);
    }
    
    /**
     * Draw "Drop here!" tooltip above a highlighted zone with bouncing animation
     */
    private void drawDropHereTooltip(Canvas canvas, RectF zoneRect) {
        String tooltipText = "👆 Drop here!";
        
        // Create tooltip paint
        Paint tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setTextSize(32f);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setColor(0xFFFFFFFF);
        tooltipTextPaint.setFakeBoldText(true);
        tooltipTextPaint.setShadowLayer(4f, 2f, 2f, 0x80000000);
        
        // Calculate tooltip position (above zone, with bounce effect)
        float bounceOffset = 6f * highlightPulse; // Subtle bounce using pulse
        float textX = zoneRect.centerX();
        float textY = zoneRect.top - 20f - bounceOffset;
        
        // Make sure tooltip doesn't go off screen
        if (textY < 50) {
            textY = zoneRect.bottom + 50f + bounceOffset;
        }
        
        // Measure text for background
        float textWidth = tooltipTextPaint.measureText(tooltipText);
        float paddingH = 20f;
        float paddingV = 12f;
        
        RectF tooltipBg = new RectF(
            textX - textWidth / 2 - paddingH,
            textY - 30f,
            textX + textWidth / 2 + paddingH,
            textY + paddingV
        );
        
        // Draw tooltip background (green pill shape)
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xFF4CAF50); // Green background
        canvas.drawRoundRect(tooltipBg, 24f, 24f, bgPaint);
        
        // Draw white border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
        borderPaint.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(tooltipBg, 24f, 24f, borderPaint);
        
        // Draw tooltip text
        canvas.drawText(tooltipText, textX, textY, tooltipTextPaint);
        
        // Draw arrow pointing to zone
        Path arrow = new Path();
        float arrowX = zoneRect.centerX();
        float arrowTop = tooltipBg.bottom;
        float arrowBottom = arrowTop + 12f;
        
        // Only draw arrow if tooltip is above zone
        if (textY < zoneRect.top) {
            arrow.moveTo(arrowX - 12f, arrowTop);
            arrow.lineTo(arrowX, arrowBottom);
            arrow.lineTo(arrowX + 12f, arrowTop);
            arrow.close();
            canvas.drawPath(arrow, bgPaint);
        }
    }
    
    /**
     * Check if the action is a tap-based action
     */
    private boolean isTapAction(ActionType action) {
        if (action == null) return false;
        return action == ActionType.TAP_TO_CUT 
            || action == ActionType.TAP_TO_CRACK 
            || action == ActionType.TAP_TO_POUND
            || action == ActionType.TAP_TO_DRIZZLE
            || action == ActionType.VENT_STEAM_TAP
            || action == ActionType.TIMING_STOP;
    }
    
    /**
     * Draw a prominent TAP indicator on the target zone
     * Shows: pulsing circle + hand icon + "TAP HERE" text + tap count remaining
     */
    private void drawTapTargetIndicator(Canvas canvas, RectF zoneRect) {
        float centerX = zoneRect.centerX();
        float centerY = zoneRect.centerY();
        
        // Calculate tap progress
        int tapsRemaining = requiredTaps - tapCount;
        float progress = requiredTaps > 0 ? (float) tapCount / requiredTaps : 0f;
        
        // ========== 1. PULSING CIRCLE TARGET ==========
        float baseRadius = Math.min(zoneRect.width(), zoneRect.height()) * 0.3f;
        float pulseRadius = baseRadius + 15f * highlightPulse;
        
        // Outer glow ring
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int glowAlpha = (int) (100 + 80 * highlightPulse);
        glowPaint.setColor(Color.argb(glowAlpha, 255, 193, 7)); // Amber glow
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(8f + 4f * highlightPulse);
        canvas.drawCircle(centerX, centerY, pulseRadius, glowPaint);
        
        // Inner filled circle (semi-transparent)
        Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetPaint.setColor(Color.argb(60, 255, 235, 59)); // Light yellow fill
        targetPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, baseRadius, targetPaint);
        
        // Target crosshair (subtle)
        Paint crossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crossPaint.setColor(0x60FFFFFF);
        crossPaint.setStrokeWidth(2f);
        float crossLen = baseRadius * 0.6f;
        canvas.drawLine(centerX - crossLen, centerY, centerX + crossLen, centerY, crossPaint);
        canvas.drawLine(centerX, centerY - crossLen, centerX, centerY + crossLen, crossPaint);
        
        // ========== 2. HAND ICON (animated tap) ==========
        drawTappingHandIcon(canvas, centerX, centerY, baseRadius);
        
        // ========== 3. "TAP HERE" TOOLTIP ==========
        String tapText = tapsRemaining > 1 ? "👆 TAP x" + tapsRemaining : "👆 TAP!";
        
        Paint tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setTextSize(28f);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setColor(0xFFFFFFFF);
        tooltipTextPaint.setFakeBoldText(true);
        tooltipTextPaint.setShadowLayer(4f, 2f, 2f, 0x80000000);
        
        float textY = zoneRect.top - 25f;
        float textWidth = tooltipTextPaint.measureText(tapText);
        
        // Tooltip background
        RectF tooltipBg = new RectF(
            centerX - textWidth / 2 - 16f,
            textY - 24f,
            centerX + textWidth / 2 + 16f,
            textY + 8f
        );
        
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xFFFF9800); // Orange background for tap
        canvas.drawRoundRect(tooltipBg, 20f, 20f, bgPaint);
        
        // White border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(tooltipBg, 20f, 20f, borderPaint);
        
        canvas.drawText(tapText, centerX, textY, tooltipTextPaint);
        
        // Arrow pointing down
        Path arrow = new Path();
        arrow.moveTo(centerX - 10f, tooltipBg.bottom);
        arrow.lineTo(centerX, tooltipBg.bottom + 10f);
        arrow.lineTo(centerX + 10f, tooltipBg.bottom);
        arrow.close();
        canvas.drawPath(arrow, bgPaint);
        
        // ========== 4. TAP PROGRESS INDICATOR (if any taps done) ==========
        if (tapCount > 0 && requiredTaps > 0) {
            // Draw small progress dots at bottom
            float dotY = zoneRect.bottom + 20f;
            float dotSpacing = 16f;
            float startX = centerX - (requiredTaps - 1) * dotSpacing / 2f;
            
            Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            for (int i = 0; i < requiredTaps; i++) {
                float dotX = startX + i * dotSpacing;
                if (i < tapCount) {
                    dotPaint.setColor(0xFF4CAF50); // Green = completed
                } else {
                    dotPaint.setColor(0x60FFFFFF); // Gray = remaining
                }
                canvas.drawCircle(dotX, dotY, 6f, dotPaint);
            }
        }
    }
    
    /**
     * Draw animated tapping hand icon
     */
    private void drawTappingHandIcon(Canvas canvas, float centerX, float centerY, float radius) {
        // Hand moves up/down based on highlightPulse to simulate tapping
        float handOffset = -10f + 20f * highlightPulse;
        float handY = centerY + handOffset;
        
        // Draw finger pointing down
        Paint handPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handPaint.setColor(0xFFFFFFFF);
        handPaint.setStyle(Paint.Style.FILL);
        handPaint.setShadowLayer(4f, 2f, 2f, 0x60000000);
        
        // Finger shape (rounded rectangle)
        float fingerWidth = 20f;
        float fingerHeight = 40f;
        RectF finger = new RectF(
            centerX - fingerWidth / 2,
            handY - fingerHeight,
            centerX + fingerWidth / 2,
            handY
        );
        canvas.drawRoundRect(finger, 10f, 10f, handPaint);
        
        // Fingertip circle
        canvas.drawCircle(centerX, handY, fingerWidth / 2, handPaint);
        
        // Tap ripple effect when hand is "down" (pulse near 1.0)
        if (highlightPulse > 0.7f) {
            float rippleAlpha = (highlightPulse - 0.7f) / 0.3f;
            Paint ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ripplePaint.setColor(Color.argb((int)(100 * rippleAlpha), 255, 255, 255));
            ripplePaint.setStyle(Paint.Style.STROKE);
            ripplePaint.setStrokeWidth(3f);
            float rippleRadius = 30f + 20f * rippleAlpha;
            canvas.drawCircle(centerX, centerY, rippleRadius, ripplePaint);
        }
    }
    
    /**
     * Draw HOLD target indicator for hold actions (GRILL_ON_HOLD, OVEN_PREHEAT_HOLD)
     * Shows: pulsing circle + pressed hand icon + "HOLD HERE" text
     */
    private void drawHoldTargetIndicator(Canvas canvas, RectF zoneRect) {
        float centerX = zoneRect.centerX();
        float centerY = zoneRect.centerY();
        
        // ========== 1. PULSING CIRCLE TARGET (Red/Orange for hold) ==========
        float baseRadius = Math.min(zoneRect.width(), zoneRect.height()) * 0.3f;
        float pulseRadius = baseRadius + 10f * highlightPulse;
        
        // Outer glow ring - Red/Orange color for hold action
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int glowAlpha = (int) (120 + 60 * highlightPulse);
        glowPaint.setColor(Color.argb(glowAlpha, 244, 67, 54)); // Red glow
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(10f + 6f * highlightPulse);
        canvas.drawCircle(centerX, centerY, pulseRadius, glowPaint);
        
        // Inner filled circle
        Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetPaint.setColor(Color.argb(80, 255, 152, 0)); // Orange fill
        targetPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, baseRadius, targetPaint);
        
        // ========== 2. PRESSED HAND ICON (stationary finger pressing) ==========
        drawHoldingHandIcon(canvas, centerX, centerY, baseRadius);
        
        // ========== 3. "HOLD HERE" TOOLTIP ==========
        String holdText = isPouring ? "🖐️ HOLDING..." : "🖐️ HOLD HERE";
        
        Paint tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setTextSize(26f);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setColor(0xFFFFFFFF);
        tooltipTextPaint.setFakeBoldText(true);
        tooltipTextPaint.setShadowLayer(4f, 2f, 2f, 0x80000000);
        
        float textY = zoneRect.top - 25f;
        float textWidth = tooltipTextPaint.measureText(holdText);
        
        // Tooltip background - Red for hold
        RectF tooltipBg = new RectF(
            centerX - textWidth / 2 - 16f,
            textY - 24f,
            centerX + textWidth / 2 + 16f,
            textY + 8f
        );
        
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xFFF44336); // Red background for hold
        canvas.drawRoundRect(tooltipBg, 20f, 20f, bgPaint);
        
        // White border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(tooltipBg, 20f, 20f, borderPaint);
        
        canvas.drawText(holdText, centerX, textY, tooltipTextPaint);
        
        // Arrow pointing down
        Path arrow = new Path();
        arrow.moveTo(centerX - 10f, tooltipBg.bottom);
        arrow.lineTo(centerX, tooltipBg.bottom + 10f);
        arrow.lineTo(centerX + 10f, tooltipBg.bottom);
        arrow.close();
        canvas.drawPath(arrow, bgPaint);
        
        // ========== 4. HOLD PROGRESS (circular) ==========
        if (actionProgress > 0) {
            Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            progressPaint.setColor(0xFF4CAF50); // Green progress
            progressPaint.setStyle(Paint.Style.STROKE);
            progressPaint.setStrokeWidth(8f);
            progressPaint.setStrokeCap(Paint.Cap.ROUND);
            
            RectF progressRect = new RectF(
                centerX - baseRadius - 20f,
                centerY - baseRadius - 20f,
                centerX + baseRadius + 20f,
                centerY + baseRadius + 20f
            );
            canvas.drawArc(progressRect, -90f, 360f * actionProgress, false, progressPaint);
        }
    }
    
    /**
     * Draw hand icon showing finger pressing down (for hold action)
     */
    private void drawHoldingHandIcon(Canvas canvas, float centerX, float centerY, float radius) {
        // Finger pressed down - no animation, just static pressed state
        Paint handPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handPaint.setColor(0xFFFFFFFF);
        handPaint.setStyle(Paint.Style.FILL);
        handPaint.setShadowLayer(6f, 2f, 2f, 0x80000000);
        
        // Finger shape (rounded rectangle) - pressed position
        float fingerWidth = 24f;
        float fingerHeight = 35f;
        RectF finger = new RectF(
            centerX - fingerWidth / 2,
            centerY - fingerHeight / 2,
            centerX + fingerWidth / 2,
            centerY + fingerHeight / 2
        );
        canvas.drawRoundRect(finger, 12f, 12f, handPaint);
        
        // Pressure ripples expanding outward
        Paint ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setStrokeWidth(2f);
        
        for (int i = 0; i < 3; i++) {
            float phase = (highlightPulse + i * 0.33f) % 1f;
            int alpha = (int) (80 * (1f - phase));
            ripplePaint.setColor(Color.argb(alpha, 255, 255, 255));
            float rippleRadius = 20f + 40f * phase;
            canvas.drawCircle(centerX, centerY, rippleRadius, ripplePaint);
        }
    }
    
    /**
     * Draw POUR target indicator for pour action (liquid pouring)
     * Shows: pulsing circle + tilted bottle icon + "HOLD TO POUR"
     */
    private void drawPourTargetIndicator(Canvas canvas, RectF zoneRect) {
        float centerX = zoneRect.centerX();
        float centerY = zoneRect.centerY();
        
        // ========== 1. PULSING CIRCLE TARGET (Blue for pour) ==========
        float baseRadius = Math.min(zoneRect.width(), zoneRect.height()) * 0.3f;
        float pulseRadius = baseRadius + 10f * highlightPulse;
        
        // Outer glow ring - Blue color for pour action
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int glowAlpha = (int) (120 + 60 * highlightPulse);
        glowPaint.setColor(Color.argb(glowAlpha, 33, 150, 243)); // Blue glow
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(10f + 6f * highlightPulse);
        canvas.drawCircle(centerX, centerY, pulseRadius, glowPaint);
        
        // Inner filled circle
        Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetPaint.setColor(Color.argb(60, 100, 181, 246)); // Light blue fill
        targetPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, baseRadius, targetPaint);
        
        // ========== 2. POUR ICON (water drop) ==========
        drawPourIcon(canvas, centerX, centerY, baseRadius);
        
        // ========== 3. "HOLD TO POUR" TOOLTIP ==========
        String pourText = isPouring ? "💧 POURING..." : "💧 HOLD TO POUR";
        
        Paint tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setTextSize(24f);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setColor(0xFFFFFFFF);
        tooltipTextPaint.setFakeBoldText(true);
        tooltipTextPaint.setShadowLayer(4f, 2f, 2f, 0x80000000);
        
        float textY = zoneRect.top - 25f;
        float textWidth = tooltipTextPaint.measureText(pourText);
        
        // Tooltip background - Blue for pour
        RectF tooltipBg = new RectF(
            centerX - textWidth / 2 - 16f,
            textY - 24f,
            centerX + textWidth / 2 + 16f,
            textY + 8f
        );
        
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xFF2196F3); // Blue background
        canvas.drawRoundRect(tooltipBg, 20f, 20f, bgPaint);
        
        // White border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(tooltipBg, 20f, 20f, borderPaint);
        
        canvas.drawText(pourText, centerX, textY, tooltipTextPaint);
        
        // Arrow pointing down
        Path arrow = new Path();
        arrow.moveTo(centerX - 10f, tooltipBg.bottom);
        arrow.lineTo(centerX, tooltipBg.bottom + 10f);
        arrow.lineTo(centerX + 10f, tooltipBg.bottom);
        arrow.close();
        canvas.drawPath(arrow, bgPaint);
        
        // ========== 4. POUR PROGRESS BAR ==========
        if (pourProgress > 0) {
            float barWidth = zoneRect.width() * 0.8f;
            float barHeight = 12f;
            float barX = centerX - barWidth / 2;
            float barY = zoneRect.bottom + 15f;
            
            // Background bar
            Paint barBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            barBgPaint.setColor(0x40FFFFFF);
            canvas.drawRoundRect(barX, barY, barX + barWidth, barY + barHeight, 6f, 6f, barBgPaint);
            
            // Progress fill
            Paint barFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            barFillPaint.setColor(0xFF4CAF50); // Green
            canvas.drawRoundRect(barX, barY, barX + barWidth * pourProgress, barY + barHeight, 6f, 6f, barFillPaint);
        }
    }
    
    /**
     * Draw pour icon (water droplet)
     */
    private void drawPourIcon(Canvas canvas, float centerX, float centerY, float radius) {
        Paint dropPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dropPaint.setColor(0xFF64B5F6); // Light blue
        dropPaint.setStyle(Paint.Style.FILL);
        dropPaint.setShadowLayer(4f, 2f, 2f, 0x60000000);
        
        // Draw water droplet shape
        Path dropPath = new Path();
        float dropSize = radius * 0.6f;
        
        // Top point of droplet
        dropPath.moveTo(centerX, centerY - dropSize);
        
        // Curve to bottom left
        dropPath.cubicTo(
            centerX - dropSize, centerY - dropSize * 0.3f,
            centerX - dropSize * 0.8f, centerY + dropSize * 0.5f,
            centerX, centerY + dropSize * 0.7f
        );
        
        // Curve to top right
        dropPath.cubicTo(
            centerX + dropSize * 0.8f, centerY + dropSize * 0.5f,
            centerX + dropSize, centerY - dropSize * 0.3f,
            centerX, centerY - dropSize
        );
        
        dropPath.close();
        canvas.drawPath(dropPath, dropPaint);
        
        // Animated drip effect when pouring
        if (isPouring) {
            float dripY = centerY + radius + 20f + 30f * highlightPulse;
            int dripAlpha = (int) (200 * (1f - highlightPulse));
            dropPaint.setColor(Color.argb(dripAlpha, 100, 181, 246));
            canvas.drawCircle(centerX, dripY, 8f - 4f * highlightPulse, dropPaint);
        }
    }
    
    /**
     * Draw the gesture trail (for stir/shake visualization)
     */
    private void drawGestureTrail(Canvas canvas) {
        if (gestureTrail.size() < 2) return;
        
        Path trailPath = new Path();
        PointF first = gestureTrail.get(0);
        trailPath.moveTo(first.x, first.y);
        
        for (int i = 1; i < gestureTrail.size(); i++) {
            PointF point = gestureTrail.get(i);
            trailPath.lineTo(point.x, point.y);
        }
        
        // Draw trail with gradient opacity
        for (int i = 0; i < gestureTrail.size() - 1; i++) {
            PointF start = gestureTrail.get(i);
            PointF end = gestureTrail.get(i + 1);
            
            // Fade from transparent to solid
            float alpha = (float) i / gestureTrail.size();
            int color = Color.argb((int) (alpha * 180), 255, 235, 59);
            gestureTrailPaint.setColor(color);
            gestureTrailPaint.setStrokeWidth(4f + 4f * alpha);
            
            canvas.drawLine(start.x, start.y, end.x, end.y, gestureTrailPaint);
        }
    }
    
    private int getZoneColor(ZoneType zone) {
        // Return transparent colors - decorations handle the visuals
        switch (zone) {
            case FRIDGE_ZONE: return 0x00000000; // Transparent - fridge drawn in decorations
            case COUNTER_ZONE: return 0x15FFFFFF;
            case BOARD_ZONE: return 0x00000000; // Transparent - board drawn in decorations  
            case PAN_ZONE: return 0x00000000; // Transparent - stove handles this
            case POT_ZONE: return 0x00000000;
            case SERVE_ZONE: return 0x00000000; // Transparent - plate drawn in decorations
            case TOOL_ZONE: return 0x15FFFFFF;
            default: return 0x00000000;
        }
    }
    
    private String getZoneLabel(ZoneType zone) {
        switch (zone) {
            case FRIDGE_ZONE: return "🧊 Fridge";
            case COUNTER_ZONE: return "📦 Counter";
            case BOARD_ZONE: return "🔪 Cutting Board";
            case PAN_ZONE: return "🍳 Pan";
            case POT_ZONE: return "🍲 Pot";
            case SERVE_ZONE: return "🍽️ Serve";
            case TOOL_ZONE: return "🔧 Tools";
            // Additional zones
            case OVEN_ZONE: return "🔥 Oven";
            case GRILL_ZONE: return "🥩 Grill";
            case STEAMER_ZONE: return "♨️ Steamer";
            case FRYER_ZONE: return "🍟 Fryer";
            case MIXING_ZONE: return "🥣 Mixing Bowl";
            case DRAIN_ZONE: return "📥 Drain";
            case GARNISH_ZONE: return "🎨 Garnish";
            default: return zone.getDisplayName();
        }
    }
    
    private String getZoneLabelShort(ZoneType zone) {
        switch (zone) {
            case FRIDGE_ZONE: return "🧊";
            case COUNTER_ZONE: return "📦";
            case BOARD_ZONE: return "🔪";
            case PAN_ZONE: return "🍳";
            case POT_ZONE: return "🍲";
            case SERVE_ZONE: return "🍽️";
            case TOOL_ZONE: return "🔧";
            // Additional zones
            case OVEN_ZONE: return "🔥";
            case GRILL_ZONE: return "🥩";
            case STEAMER_ZONE: return "♨️";
            case FRYER_ZONE: return "🍟";
            case MIXING_ZONE: return "🥣";
            case DRAIN_ZONE: return "📥";
            case GARNISH_ZONE: return "🎨";
            default: return "";
        }
    }

    private void drawZoneLabel(Canvas canvas, ZoneType zone, RectF rect) {
        String label = getZoneLabel(zone);
        
        // Draw label background
        float textWidth = textPaint.measureText(label);
        float labelPadding = 8f;
        RectF labelRect = new RectF(
            rect.centerX() - textWidth / 2 - labelPadding,
            rect.top + 8,
            rect.centerX() + textWidth / 2 + labelPadding,
            rect.top + 48
        );
        canvas.drawRoundRect(labelRect, 8f, 8f, labelBgPaint);
        
        // Draw label text
        canvas.drawText(label, rect.centerX(), rect.top + 40, textPaint);
    }
    
    private void drawZoneLabelCompact(Canvas canvas, ZoneType zone, RectF rect) {
        // Compact labels - small icon badge in corner
        String icon = getZoneLabelShort(zone);
        if (icon.isEmpty()) return;
        
        // Position in top-left corner
        float badgeSize = 36f;
        float badgeX = rect.left + 8f;
        float badgeY = rect.top + 8f;
        
        // Badge background
        Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgePaint.setColor(0xCC000000);
        canvas.drawRoundRect(new RectF(badgeX, badgeY, badgeX + badgeSize, badgeY + badgeSize), 8f, 8f, badgePaint);
        
        // Icon
        Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconPaint.setTextSize(22f);
        iconPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(icon, badgeX + badgeSize / 2f, badgeY + badgeSize - 8f, iconPaint);
    }

    private void drawZoneItems(Canvas canvas, ZoneType zone, RectF rect) {
        List<KitchenItem> items = zoneItems.get(zone);
        if (items == null || items.isEmpty()) return;
        
        // Calculate item size based on zone type
        float itemSize = getItemSizeForZone(zone, rect);
        float centerX = rect.centerX();
        float centerY = rect.centerY();
        
        int itemCount = items.size();
        
        if (itemCount == 1) {
            // Single item: center in zone with glow effect
            KitchenItem item = items.get(0);
            float x = centerX - itemSize / 2;
            float y = centerY - itemSize / 2;
            
            drawItemWithGlow(canvas, item, x, y, itemSize);
        } else if (itemCount <= 4) {
            // 2-4 items: arrange in a nice pattern
            float offsetStep = itemSize * 0.3f;
            for (int i = 0; i < itemCount; i++) {
                KitchenItem item = items.get(i);
                float offsetX = (i % 2) * offsetStep - offsetStep / 2;
                float offsetY = (i / 2) * offsetStep - offsetStep / 2;
                
                float x = centerX - itemSize / 2 + offsetX;
                float y = centerY - itemSize / 2 + offsetY;
                
                drawItemWithGlow(canvas, item, x, y, itemSize * 0.85f);
            }
        } else {
            // Many items: show stacked with badge
            KitchenItem topItem = items.get(items.size() - 1);
            float x = centerX - itemSize / 2;
            float y = centerY - itemSize / 2;
            
            // Draw shadow stack effect
            Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            shadowPaint.setColor(0x40000000);
            for (int i = 2; i >= 0; i--) {
                canvas.drawRoundRect(
                    x + i * 3, y + i * 3,
                    x + itemSize + i * 3, y + itemSize + i * 3,
                    8f, 8f, shadowPaint);
            }
            
            drawItemWithGlow(canvas, topItem, x, y, itemSize);
            
            // Draw count badge
            drawItemCountBadge(canvas, itemCount, x + itemSize - 16, y - 8);
        }
    }
    
    /**
     * Get appropriate item size for a zone type
     */
    private float getItemSizeForZone(ZoneType zone, RectF rect) {
        float maxSize = Math.min(rect.width(), rect.height()) * 0.5f;
        switch (zone) {
            case PAN_ZONE:
            case POT_ZONE:
                return Math.min(maxSize, 80f);
            case BOARD_ZONE:
                return Math.min(maxSize, 70f);
            case SERVE_ZONE:
                return Math.min(maxSize, 60f);
            default:
                return Math.min(maxSize, 64f);
        }
    }
    
    /**
     * Draw item with subtle glow effect to make it stand out
     */
    private void drawItemWithGlow(Canvas canvas, KitchenItem item, float x, float y, float size) {
        // Apply scale animation
        float scale = Math.max(0.01f, item.scale); // Prevent zero scale
        float scaledSize = size * scale;
        float offsetX = (size - scaledSize) / 2;
        float offsetY = (size - scaledSize) / 2;
        float drawX = x + offsetX;
        float drawY = y + offsetY;
        
        if (item.drawable == null) {
            // Fallback: draw placeholder circle with item name
            Paint placeholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            placeholderPaint.setColor(0xFFFFE082);
            placeholderPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(drawX + scaledSize/2, drawY + scaledSize/2, scaledSize/2, placeholderPaint);
            
            // Draw first letter
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(0xFF795548);
            textPaint.setTextSize(scaledSize * 0.5f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            String initial = item.name != null && !item.name.isEmpty() 
                ? item.name.substring(0, 1).toUpperCase() : "?";
            canvas.drawText(initial, drawX + scaledSize/2, drawY + scaledSize/2 + scaledSize * 0.15f, textPaint);
            return;
        }
        
        // Subtle white glow behind item (pulsing slightly when animating)
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int glowAlpha = scale >= 1f ? 0x30 : (int)(0x50 * (1f - scale) + 0x30);
        glowPaint.setColor((glowAlpha << 24) | 0xFFFFFF);
        canvas.drawCircle(drawX + scaledSize/2, drawY + scaledSize/2, scaledSize/2 + 6 * scale, glowPaint);
        
        // Draw the item with animated scale
        item.drawable.setBounds(
            (int) drawX, (int) drawY,
            (int) (drawX + scaledSize), (int) (drawY + scaledSize)
        );
        item.drawable.draw(canvas);
    }
    
    /**
     * Draw count badge for stacked items
     */
    private void drawItemCountBadge(Canvas canvas, int count, float x, float y) {
        // Badge background
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xFFFF5722); // Orange
        canvas.drawCircle(x, y, 14f, bgPaint);
        
        // Badge border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setColor(0xFFFFFFFF);
        canvas.drawCircle(x, y, 14f, borderPaint);
        
        // Count text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(16f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        canvas.drawText(String.valueOf(count), x, y + 5f, textPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = gestureDetector.onTouchEvent(event);
        
        // Handle finger up - stop pouring/holding and clear gesture trail
        if (event.getAction() == MotionEvent.ACTION_UP || 
            event.getAction() == MotionEvent.ACTION_CANCEL) {
            
            // Track what was happening before stopping
            boolean wasPouring = isPouring;
            boolean wasHolding = isHoldAction(currentAction);
            ZoneType releaseZone = pouringZone != null ? pouringZone : targetActionZone;
            
            // Stop pouring if in progress (POUR action)
            stopPouring();
            
            // CRITICAL: Notify listener that hold was released
            // Only for hold actions (not POUR - POUR completion is handled by pourAnimator)
            if (wasHolding && !wasPouring && listener != null) {
                listener.onHoldReleased(releaseZone);
            }
            
            // Clear gesture trail
            gestureTrail.clear();
            showGestureTrail = false;
            
            // Check if stir/shake was in progress and notify partial completion
            if (currentAction == ActionType.SWIPE_TO_STIR && stirCount > 0) {
                // Partial stir completed
                stirPath.clear();
            }
            if (currentAction == ActionType.SHAKE_PAN && shakeCount > 0) {
                // Partial shake completed
            }
            
            invalidate();
        }
        
        return handled || super.onTouchEvent(event);
    }
    
    /**
     * Check if the action is a hold-based action (NOT including POUR which has its own flow)
     * POUR uses pourAnimator, while these use holdTimerRunnable in gameEngine
     */
    private boolean isHoldAction(ActionType action) {
        if (action == null) return false;
        // Note: POUR is handled separately by startPouring/pourAnimator
        // Note: STEAM_START is a wait action (gestureType="wait"), NOT a hold action
        return action == ActionType.OVEN_PREHEAT_HOLD 
            || action == ActionType.GRILL_ON_HOLD;
    }
    
    private boolean handleDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                // Accept all drag events
                invalidate();
                return true;
                
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;
                
            case DragEvent.ACTION_DRAG_LOCATION:
                ZoneType zone = getZoneAt(event.getX(), event.getY());
                if (zone != highlightedZone) {
                    highlightedZone = zone;
                    invalidate();
                }
                return true;
                
            case DragEvent.ACTION_DRAG_EXITED:
                return true;
                
            case DragEvent.ACTION_DROP:
                ZoneType dropZone = getZoneAt(event.getX(), event.getY());
                android.util.Log.d("KitchenView", "DROP: x=" + event.getX() + ", y=" + event.getY() + 
                    " -> dropZone=" + dropZone + ", targetActionZone=" + targetActionZone);
                Object localState = event.getLocalState();
                if (localState instanceof DragData && listener != null) {
                    DragData data = (DragData) localState;
                    android.util.Log.d("KitchenView", "DROP item: " + data.itemId + " to zone: " + dropZone);
                    if (dropZone != null) {
                        // Valid drop on a zone
                        listener.onItemDropped(data.itemId, data.fromZone, dropZone);
                    } else {
                        // Dropped outside zones - notify listener with null zone
                        listener.onItemDropped(data.itemId, data.fromZone, null);
                    }
                }
                return true;
                
            case DragEvent.ACTION_DRAG_ENDED:
                highlightedZone = null;
                invalidate();
                return true;
        }
        return false;
    }
    
    @Nullable
    private ZoneType getZoneAt(float x, float y) {
        // PRIORITY 1: If there's a targetActionZone and the point is inside it, return that
        // This is critical because some zones overlay on top of others (e.g., OVEN_ZONE over POT_ZONE)
        if (targetActionZone != null) {
            RectF targetRect = zoneRects.get(targetActionZone);
            if (targetRect != null && targetRect.contains(x, y)) {
                return targetActionZone;
            }
        }
        
        // PRIORITY 2: Check highlighted zone
        if (highlightedZone != null) {
            RectF highlightRect = zoneRects.get(highlightedZone);
            if (highlightRect != null && highlightRect.contains(x, y)) {
                return highlightedZone;
            }
        }
        
        // PRIORITY 3: Default - return first matching zone
        for (Map.Entry<ZoneType, RectF> entry : zoneRects.entrySet()) {
            if (entry.getValue().contains(x, y)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    // Public API
    
    public void setListener(KitchenEventListener listener) {
        this.listener = listener;
    }
    
    public void addItemToZone(String itemId, String itemName, Drawable drawable, ZoneType zone) {
        List<KitchenItem> items = zoneItems.get(zone);
        if (items != null) {
            // Check if item already exists in zone
            boolean exists = items.stream().anyMatch(item -> item.id.equals(itemId));
            if (!exists) {
                KitchenItem newItem = new KitchenItem(itemId, itemName, drawable);
                items.add(newItem);
                
                // Trigger pop animation
                startItemAnimation();
                
                // Haptic feedback for successful drop
                performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            }
            invalidate();
        }
    }
    
    /**
     * Start animation loop for newly added items
     */
    private void startItemAnimation() {
        // Use existing post mechanism to update animation
        postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean needsMoreAnimation = false;
                for (List<KitchenItem> items : zoneItems.values()) {
                    for (KitchenItem item : items) {
                        if (item.updateAnimation()) {
                            needsMoreAnimation = true;
                        }
                    }
                }
                invalidate();
                if (needsMoreAnimation) {
                    postDelayed(this, 16); // ~60fps
                }
            }
        }, 16);
    }
    
    public void removeItemFromZone(String itemId, ZoneType zone) {
        List<KitchenItem> items = zoneItems.get(zone);
        if (items != null) {
            items.removeIf(item -> item.id.equals(itemId));
            invalidate();
        }
    }
    
    public void clearZone(ZoneType zone) {
        List<KitchenItem> items = zoneItems.get(zone);
        if (items != null) {
            items.clear();
            invalidate();
        }
    }
    
    public void clearAllZones() {
        for (List<KitchenItem> items : zoneItems.values()) {
            items.clear();
        }
        invalidate();
    }
    
    public void highlightZone(ZoneType zone) {
        highlightedZone = zone;
        invalidate();
    }
    
    public void clearHighlight() {
        highlightedZone = null;
        invalidate();
    }
    
    public RectF getZoneRect(ZoneType zone) {
        return zoneRects.get(zone);
    }
    
    // Helper classes
    
    public static class KitchenItem {
        public final String id;
        public final String name;
        public final Drawable drawable;
        
        // Animation properties
        public float scale = 0f;  // For pop-in animation
        public float alpha = 1f;
        public long addedTime = 0; // When item was added
        
        public KitchenItem(String id, String name, Drawable drawable) {
            this.id = id;
            this.name = name;
            this.drawable = drawable;
            this.addedTime = System.currentTimeMillis();
            this.scale = 0f; // Start small for pop animation
        }
        
        /**
         * Update animation state
         * @return true if animation is still in progress
         */
        public boolean updateAnimation() {
            long elapsed = System.currentTimeMillis() - addedTime;
            float animDuration = 300f; // 300ms pop animation
            
            if (elapsed >= animDuration) {
                scale = 1f;
                return false;
            }
            
            // Spring-like bounce effect
            float t = elapsed / animDuration;
            scale = easeOutBack(t);
            return true;
        }
        
        private float easeOutBack(float t) {
            float c1 = 1.70158f;
            float c3 = c1 + 1;
            return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
        }
    }
    
    public static class DragData {
        public final String itemId;
        public final ZoneType fromZone;
        
        public DragData(String itemId, ZoneType fromZone) {
            this.itemId = itemId;
            this.fromZone = fromZone;
        }
    }
    
    // ================ GAME-STYLE KITCHEN DECORATIONS ================
    
    private void drawKitchenDecorations(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        
        Paint decorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // ========== WARM COOKING GAME BACKGROUND ==========
        
        // 1. Overall warm gradient background (cream to light wood)
        android.graphics.LinearGradient bgGradient = new android.graphics.LinearGradient(
            0, 0, 0, h,
            new int[]{0xFFFFF8E1, 0xFFFFECB3, 0xFFFFE082},
            new float[]{0f, 0.5f, 1f},
            android.graphics.Shader.TileMode.CLAMP
        );
        decorPaint.setStyle(Paint.Style.FILL);
        decorPaint.setShader(bgGradient);
        canvas.drawRect(0, 0, w, h, decorPaint);
        decorPaint.setShader(null);
        
        // 2. TOP SECTION: Wooden shelf/backsplash
        float shelfHeight = h * 0.12f;
        decorPaint.setColor(0xFF8D6E63); // Wood brown
        canvas.drawRect(0, 0, w, shelfHeight, decorPaint);
        
        // Wood grain lines
        decorPaint.setColor(0xFF6D4C41);
        decorPaint.setStrokeWidth(2f);
        decorPaint.setStyle(Paint.Style.STROKE);
        for (float y = 5f; y < shelfHeight; y += 12f) {
            canvas.drawLine(0, y, w, y, decorPaint);
        }
        
        // Shelf shadow
        decorPaint.setStyle(Paint.Style.FILL);
        android.graphics.LinearGradient shelfShadow = new android.graphics.LinearGradient(
            0, shelfHeight, 0, shelfHeight + 16f,
            0x40000000, 0x00000000,
            android.graphics.Shader.TileMode.CLAMP
        );
        decorPaint.setShader(shelfShadow);
        canvas.drawRect(0, shelfHeight, w, shelfHeight + 16f, decorPaint);
        decorPaint.setShader(null);
        
        // 3. MAIN COUNTERTOP: Large wooden cutting surface
        RectF boardRect = zoneRects.get(ZoneType.BOARD_ZONE);
        if (boardRect != null) {
            // Draw nice wooden cutting board with shadow
            drawWoodenBoard(canvas, boardRect.left - 20f, boardRect.top - 10f, 
                           boardRect.width() + 40f, boardRect.height() + 20f);
        }
        
        // 4. STOVE AREA: Draw stove with burners
        RectF panRect = zoneRects.get(ZoneType.PAN_ZONE);
        RectF potRect = zoneRects.get(ZoneType.POT_ZONE);
        
        if (panRect != null && potRect != null) {
            // Stove background
            float stoveLeft = panRect.left - 20f;
            float stoveTop = panRect.top - 20f;
            float stoveRight = potRect.right + 20f;
            float stoveBottom = Math.max(panRect.bottom, potRect.bottom) + 20f;
            
            drawStove(canvas, stoveLeft, stoveTop, stoveRight - stoveLeft, stoveBottom - stoveTop);
            
            // Draw individual burners
            drawBurnerWithGlow(canvas, panRect.centerX(), panRect.centerY() + 30f, 
                              Math.min(panRect.width(), panRect.height()) * 0.35f, true);
            drawBurnerWithGlow(canvas, potRect.centerX(), potRect.centerY() + 30f, 
                              Math.min(potRect.width(), potRect.height()) * 0.35f, true);
        }
        
        // 5. BOTTOM AREA: Kitchen counter/serving area
        RectF serveRect = zoneRects.get(ZoneType.SERVE_ZONE);
        if (serveRect != null) {
            // Plate holder/serving station
            drawServingStation(canvas, serveRect);
        }
        
        // 6. FRIDGE AREA: Draw fridge door
        RectF fridgeRect = zoneRects.get(ZoneType.FRIDGE_ZONE);
        if (fridgeRect != null) {
            drawFridgeDoor(canvas, fridgeRect);
        }
        
        // 7. Decorative frame around entire view
        decorPaint.setStyle(Paint.Style.STROKE);
        decorPaint.setColor(0xFF5D4037);
        decorPaint.setStrokeWidth(6f);
        canvas.drawRoundRect(new RectF(3f, 3f, w - 3f, h - 3f), 16f, 16f, decorPaint);
    }
    
    private void drawWoodenBoard(Canvas canvas, float x, float y, float width, float height) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rect = new RectF(x, y, x + width, y + height);
        
        // Shadow under board
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x30000000);
        canvas.drawRoundRect(new RectF(x + 8f, y + 8f, x + width + 8f, y + height + 8f), 12f, 12f, paint);
        
        // Board base (warm wood color)
        android.graphics.LinearGradient woodGradient = new android.graphics.LinearGradient(
            x, y, x + width, y,
            new int[]{0xFFD7A86E, 0xFFE8C49A, 0xFFD7A86E},
            new float[]{0f, 0.5f, 1f},
            android.graphics.Shader.TileMode.CLAMP
        );
        paint.setShader(woodGradient);
        canvas.drawRoundRect(rect, 12f, 12f, paint);
        paint.setShader(null);
        
        // Wood grain lines
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(0x30000000);
        for (float lineY = y + 10f; lineY < y + height - 10f; lineY += 8f) {
            canvas.drawLine(x + 10f, lineY, x + width - 10f, lineY, paint);
        }
        
        // Top highlight
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x40FFFFFF);
        canvas.drawRoundRect(new RectF(x + 4f, y + 4f, x + width - 4f, y + 12f), 6f, 6f, paint);
        
        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(0xFF8D6E63);
        canvas.drawRoundRect(rect, 12f, 12f, paint);
    }
    
    private void drawStove(Canvas canvas, float x, float y, float width, float height) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rect = new RectF(x, y, x + width, y + height);
        
        // Stove shadow
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x40000000);
        canvas.drawRoundRect(new RectF(x + 6f, y + 6f, x + width + 6f, y + height + 6f), 16f, 16f, paint);
        
        // Stove surface (dark metallic)
        android.graphics.LinearGradient metalGradient = new android.graphics.LinearGradient(
            x, y, x, y + height,
            new int[]{0xFF546E7A, 0xFF37474F, 0xFF263238},
            new float[]{0f, 0.5f, 1f},
            android.graphics.Shader.TileMode.CLAMP
        );
        paint.setShader(metalGradient);
        canvas.drawRoundRect(rect, 16f, 16f, paint);
        paint.setShader(null);
        
        // Stove highlight
        paint.setColor(0x30FFFFFF);
        canvas.drawRoundRect(new RectF(x + 4f, y + 4f, x + width - 4f, y + 20f), 12f, 12f, paint);
        
        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(0xFF455A64);
        canvas.drawRoundRect(rect, 16f, 16f, paint);
    }
    
    private void drawBurnerWithGlow(Canvas canvas, float cx, float cy, float radius, boolean isOn) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Glow effect (if burner is on)
        if (isOn) {
            android.graphics.RadialGradient glowGradient = new android.graphics.RadialGradient(
                cx, cy, radius * 1.5f,
                new int[]{0x60FF5722, 0x30FF8A65, 0x00FFFFFF},
                new float[]{0f, 0.5f, 1f},
                android.graphics.Shader.TileMode.CLAMP
            );
            paint.setShader(glowGradient);
            canvas.drawCircle(cx, cy, radius * 1.5f, paint);
            paint.setShader(null);
        }
        
        // Outer burner ring (dark gray metal)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF424242);
        canvas.drawCircle(cx, cy, radius, paint);
        
        // Burner coil rings
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        
        if (isOn) {
            // Glowing rings
            paint.setColor(0xFFE65100);
            canvas.drawCircle(cx, cy, radius * 0.8f, paint);
            paint.setColor(0xFFFF5722);
            canvas.drawCircle(cx, cy, radius * 0.6f, paint);
            paint.setColor(0xFFFF8A65);
            canvas.drawCircle(cx, cy, radius * 0.4f, paint);
        } else {
            // Gray rings when off
            paint.setColor(0xFF616161);
            canvas.drawCircle(cx, cy, radius * 0.8f, paint);
            canvas.drawCircle(cx, cy, radius * 0.6f, paint);
            canvas.drawCircle(cx, cy, radius * 0.4f, paint);
        }
        
        // Center
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF212121);
        canvas.drawCircle(cx, cy, radius * 0.15f, paint);
    }
    
    private void drawServingStation(Canvas canvas, RectF rect) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Serving counter shadow
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x30000000);
        canvas.drawRoundRect(new RectF(rect.left + 6f, rect.top + 6f, rect.right + 6f, rect.bottom + 6f), 16f, 16f, paint);
        
        // Marble-like surface
        android.graphics.LinearGradient marbleGradient = new android.graphics.LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            new int[]{0xFFF5F5F5, 0xFFE0E0E0, 0xFFF5F5F5},
            new float[]{0f, 0.5f, 1f},
            android.graphics.Shader.TileMode.CLAMP
        );
        paint.setShader(marbleGradient);
        canvas.drawRoundRect(rect, 16f, 16f, paint);
        paint.setShader(null);
        
        // Plate circle in center
        float plateRadius = Math.min(rect.width(), rect.height()) * 0.35f;
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(rect.centerX(), rect.centerY(), plateRadius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(0xFFE0E0E0);
        canvas.drawCircle(rect.centerX(), rect.centerY(), plateRadius - 8f, paint);
        paint.setColor(0xFFBDBDBD);
        canvas.drawCircle(rect.centerX(), rect.centerY(), plateRadius - 16f, paint);
    }
    
    private void drawFridgeDoor(Canvas canvas, RectF rect) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Fridge shadow
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x40000000);
        canvas.drawRoundRect(new RectF(rect.left + 8f, rect.top + 8f, rect.right + 8f, rect.bottom + 8f), 16f, 16f, paint);
        
        // Fridge body (light blue-gray metallic)
        android.graphics.LinearGradient fridgeGradient = new android.graphics.LinearGradient(
            rect.left, rect.top, rect.right, rect.top,
            new int[]{0xFFCFD8DC, 0xFFECEFF1, 0xFFCFD8DC},
            new float[]{0f, 0.5f, 1f},
            android.graphics.Shader.TileMode.CLAMP
        );
        paint.setShader(fridgeGradient);
        canvas.drawRoundRect(rect, 16f, 16f, paint);
        paint.setShader(null);
        
        // Door line (vertical split)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(0xFF90A4AE);
        canvas.drawLine(rect.centerX(), rect.top + 10f, rect.centerX(), rect.bottom - 10f, paint);
        
        // Handle on right door
        float handleX = rect.centerX() + 15f;
        paint.setStrokeWidth(6f);
        paint.setColor(0xFF78909C);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(handleX, rect.centerY() - 30f, handleX, rect.centerY() + 30f, paint);
        
        // Snowflake icon (fridge indicator)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF42A5F5);
        paint.setTextSize(32f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("❄️", rect.centerX() - 40f, rect.centerY(), paint);
        
        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(0xFF90A4AE);
        canvas.drawRoundRect(rect, 16f, 16f, paint);
    }
}
