package com.edu.english.masterchef.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.edu.english.R;

/**
 * Custom View that displays chef avatar with speech bubble.
 * Supports text animation and bounce effects.
 */
public class ChefBubbleView extends View {

    private Paint bubblePaint;
    private Paint bubbleBorderPaint;
    private Paint textPaint;
    private Paint chefCirclePaint;
    
    private Drawable chefDrawable;
    private String message = "";
    private String displayedText = "";
    
    private Path bubblePath;
    private RectF bubbleRect;
    private RectF chefRect;
    
    private Handler handler;
    private int textAnimIndex = 0;
    private boolean isAnimatingText = false;
    private static final int TEXT_ANIM_DELAY = 30; // ms per character
    
    private OnBubbleClickListener clickListener;
    
    public interface OnBubbleClickListener {
        void onBubbleClicked();
    }
    
    public ChefBubbleView(Context context) {
        super(context);
        init();
    }
    
    public ChefBubbleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ChefBubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        handler = new Handler(Looper.getMainLooper());
        
        // Bubble background paint
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setColor(Color.WHITE);
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setShadowLayer(8f, 2f, 2f, 0x33000000);
        setLayerType(LAYER_TYPE_SOFTWARE, bubblePaint);
        
        // Bubble border paint
        bubbleBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubbleBorderPaint.setColor(0xFFFF9800);
        bubbleBorderPaint.setStyle(Paint.Style.STROKE);
        bubbleBorderPaint.setStrokeWidth(4f);
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF333333);
        textPaint.setTextSize(42f);
        
        // Chef circle background
        chefCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chefCirclePaint.setColor(0xFFFFE0B2);
        chefCirclePaint.setStyle(Paint.Style.FILL);
        
        // Load chef drawable
        chefDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_chef);
        
        bubblePath = new Path();
        bubbleRect = new RectF();
        chefRect = new RectF();
        
        setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onBubbleClicked();
            }
        });
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeLayout(w, h);
    }
    
    private void computeLayout(int width, int height) {
        float padding = 16f;
        float chefSize = Math.min(height - 2 * padding, 80f);
        float triangleSize = 20f;
        
        // Chef avatar on the left
        chefRect.set(
            padding,
            (height - chefSize) / 2f,
            padding + chefSize,
            (height + chefSize) / 2f
        );
        
        // Bubble on the right (with triangle pointing to chef)
        float bubbleLeft = chefRect.right + triangleSize + 8f;
        bubbleRect.set(
            bubbleLeft,
            padding,
            width - padding,
            height - padding
        );
        
        // Create bubble path with pointer
        bubblePath.reset();
        float cornerRadius = 20f;
        
        // Main rectangle
        bubblePath.addRoundRect(bubbleRect, cornerRadius, cornerRadius, Path.Direction.CW);
        
        // Triangle pointer
        float triCenterY = bubbleRect.centerY();
        bubblePath.moveTo(bubbleLeft, triCenterY - triangleSize / 2);
        bubblePath.lineTo(bubbleLeft - triangleSize, triCenterY);
        bubblePath.lineTo(bubbleLeft, triCenterY + triangleSize / 2);
        bubblePath.close();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw chef circle background
        float centerX = chefRect.centerX();
        float centerY = chefRect.centerY();
        float radius = chefRect.width() / 2f;
        canvas.drawCircle(centerX, centerY, radius + 4f, chefCirclePaint);
        
        // Draw chef avatar
        if (chefDrawable != null) {
            chefDrawable.setBounds(
                (int) chefRect.left,
                (int) chefRect.top,
                (int) chefRect.right,
                (int) chefRect.bottom
            );
            chefDrawable.draw(canvas);
        }
        
        // Draw bubble
        canvas.drawPath(bubblePath, bubblePaint);
        canvas.drawPath(bubblePath, bubbleBorderPaint);
        
        // Draw text (word-wrapped)
        drawWrappedText(canvas);
    }
    
    private void drawWrappedText(Canvas canvas) {
        if (displayedText == null || displayedText.isEmpty()) return;
        
        float maxWidth = bubbleRect.width() - 32f;
        float x = bubbleRect.left + 16f;
        float y = bubbleRect.top + 40f;
        float lineHeight = textPaint.getTextSize() * 1.3f;
        
        String[] words = displayedText.split(" ");
        StringBuilder line = new StringBuilder();
        
        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float testWidth = textPaint.measureText(testLine);
            
            if (testWidth > maxWidth && line.length() > 0) {
                canvas.drawText(line.toString(), x, y, textPaint);
                y += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        
        // Draw last line
        if (line.length() > 0) {
            canvas.drawText(line.toString(), x, y, textPaint);
        }
    }
    
    /**
     * Set message and animate text appearing character by character
     */
    public void setMessageAnimated(String message) {
        this.message = message;
        this.displayedText = "";
        this.textAnimIndex = 0;
        this.isAnimatingText = true;
        
        // Bounce animation
        ScaleAnimation bounce = new ScaleAnimation(
            0.9f, 1f, 0.9f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        bounce.setDuration(200);
        bounce.setInterpolator(new AccelerateDecelerateInterpolator());
        startAnimation(bounce);
        
        // Start text animation
        animateNextCharacter();
    }
    
    private void animateNextCharacter() {
        if (textAnimIndex < message.length()) {
            textAnimIndex++;
            displayedText = message.substring(0, textAnimIndex);
            invalidate();
            handler.postDelayed(this::animateNextCharacter, TEXT_ANIM_DELAY);
        } else {
            isAnimatingText = false;
        }
    }
    
    /**
     * Set message immediately without animation
     */
    public void setMessage(String message) {
        this.message = message;
        this.displayedText = message;
        this.isAnimatingText = false;
        invalidate();
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean isAnimating() {
        return isAnimatingText;
    }
    
    public void setOnBubbleClickListener(OnBubbleClickListener listener) {
        this.clickListener = listener;
    }
    
    /**
     * Set chef avatar drawable by resource ID
     */
    public void setChefAvatar(int drawableResId) {
        chefDrawable = ContextCompat.getDrawable(getContext(), drawableResId);
        invalidate();
    }
    
    /**
     * Set chef avatar drawable directly
     */
    public void setChefAvatar(Drawable drawable) {
        chefDrawable = drawable;
        invalidate();
    }
    
    /**
     * Set chef avatar by drawable name
     */
    public void setChefAvatarByName(String drawableName) {
        int resId = getContext().getResources().getIdentifier(
            drawableName, "drawable", getContext().getPackageName()
        );
        if (resId != 0) {
            setChefAvatar(resId);
        }
    }

    public void skipAnimation() {
        if (isAnimatingText) {
            handler.removeCallbacksAndMessages(null);
            displayedText = message;
            isAnimatingText = false;
            invalidate();
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }
}
