package com.edu.english.masterchef.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.edu.english.R;
import com.edu.english.masterchef.data.CustomerOrder;

/**
 * Custom View that displays a customer order card.
 * Shows customer avatar, name, and their order intro text.
 * Supports slide-in animation for new orders.
 */
public class OrderCardView extends View {

    private Paint cardPaint;
    private Paint cardBorderPaint;
    private Paint titlePaint;
    private Paint textPaint;
    private Paint starPaint;
    
    private Drawable customerDrawable;
    private String customerName = "";
    private String orderText = "";
    private String displayedText = "";
    private int difficulty = 1;
    private int bonusStars = 1;
    
    private RectF cardRect;
    private RectF avatarRect;
    
    private Handler handler;
    private int textAnimIndex = 0;
    private boolean isAnimatingText = false;
    private static final int TEXT_ANIM_DELAY = 25; // ms per character
    
    private OnOrderClickListener clickListener;
    private OnOrderCompleteListener completeListener;
    
    public interface OnOrderClickListener {
        void onOrderClicked();
    }
    
    public interface OnOrderCompleteListener {
        void onTextAnimationComplete();
    }
    
    public OrderCardView(Context context) {
        super(context);
        init();
    }
    
    public OrderCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public OrderCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        handler = new Handler(Looper.getMainLooper());
        
        // Card background paint
        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setColor(0xFFFFF8E1); // Light cream
        cardPaint.setStyle(Paint.Style.FILL);
        cardPaint.setShadowLayer(10f, 3f, 3f, 0x33000000);
        setLayerType(LAYER_TYPE_SOFTWARE, cardPaint);
        
        // Card border paint
        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBorderPaint.setColor(0xFFFFCC80); // Orange border
        cardBorderPaint.setStyle(Paint.Style.STROKE);
        cardBorderPaint.setStrokeWidth(3f);
        
        // Title paint
        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(0xFF5D4037); // Brown
        titlePaint.setTextSize(36f);
        titlePaint.setFakeBoldText(true);
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF333333);
        textPaint.setTextSize(28f);
        
        // Star paint
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(0xFFFFD700); // Gold
        starPaint.setTextSize(24f);
        
        // Load default customer drawable
        customerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_customer);
        
        cardRect = new RectF();
        avatarRect = new RectF();
        
        setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onOrderClicked();
            }
        });
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeLayout(w, h);
    }
    
    private void computeLayout(int width, int height) {
        float padding = 12f;
        float avatarSize = Math.min(height - 2 * padding, 70f);
        
        // Card rect
        cardRect.set(padding, padding, width - padding, height - padding);
        
        // Customer avatar on top-left
        avatarRect.set(
            padding * 2,
            padding * 2,
            padding * 2 + avatarSize,
            padding * 2 + avatarSize
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw card background
        float cornerRadius = 16f;
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardPaint);
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardBorderPaint);
        
        // Draw customer avatar
        if (customerDrawable != null) {
            customerDrawable.setBounds(
                (int) avatarRect.left,
                (int) avatarRect.top,
                (int) avatarRect.right,
                (int) avatarRect.bottom
            );
            customerDrawable.draw(canvas);
        }
        
        // Draw customer name
        float textX = avatarRect.right + 16f;
        float nameY = avatarRect.top + titlePaint.getTextSize();
        canvas.drawText(customerName, textX, nameY, titlePaint);
        
        // Draw stars for difficulty
        float starX = textX;
        float starY = nameY + 24f;
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < difficulty; i++) {
            stars.append("★");
        }
        canvas.drawText(stars.toString(), starX, starY, starPaint);
        
        // Draw order text (word-wrapped)
        drawWrappedText(canvas, textX, avatarRect.bottom + 20f);
    }
    
    private void drawWrappedText(Canvas canvas, float startX, float startY) {
        if (displayedText == null || displayedText.isEmpty()) return;
        
        float maxWidth = cardRect.right - startX - 16f;
        float y = startY;
        float lineHeight = textPaint.getTextSize() * 1.4f;
        
        String[] words = displayedText.split(" ");
        StringBuilder line = new StringBuilder();
        
        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float testWidth = textPaint.measureText(testLine);
            
            if (testWidth > maxWidth && line.length() > 0) {
                canvas.drawText(line.toString(), startX, y, textPaint);
                y += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        
        // Draw last line
        if (line.length() > 0) {
            canvas.drawText(line.toString(), startX, y, textPaint);
        }
    }
    
    /**
     * Set order with slide-in animation and text animation
     */
    public void setOrderAnimated(CustomerOrder order) {
        // Slide-in animation
        TranslateAnimation slideIn = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        );
        slideIn.setDuration(400);
        slideIn.setFillAfter(true);
        
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // Start text animation after slide-in
                animateText();
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        applyOrder(order);
        startAnimation(slideIn);
    }
    
    /**
     * Set order without animation
     */
    public void setOrder(CustomerOrder order) {
        applyOrder(order);
        this.displayedText = this.orderText;
        invalidate();
    }
    
    private void applyOrder(CustomerOrder order) {
        this.customerName = order.getCustomerName();
        this.orderText = order.getIntroTextEn();
        this.difficulty = order.getDifficulty();
        this.bonusStars = order.getBonusStars();
        this.displayedText = "";
        this.textAnimIndex = 0;
        
        // Load customer avatar
        String avatarName = order.getCustomerAvatarDrawable();
        if (avatarName != null && !avatarName.isEmpty()) {
            int resId = getContext().getResources().getIdentifier(
                avatarName, "drawable", getContext().getPackageName()
            );
            if (resId != 0) {
                customerDrawable = ContextCompat.getDrawable(getContext(), resId);
            }
        }
        
        invalidate();
    }
    
    private void animateText() {
        isAnimatingText = true;
        animateNextCharacter();
    }
    
    private void animateNextCharacter() {
        if (textAnimIndex < orderText.length()) {
            textAnimIndex++;
            displayedText = orderText.substring(0, textAnimIndex);
            invalidate();
            handler.postDelayed(this::animateNextCharacter, TEXT_ANIM_DELAY);
        } else {
            isAnimatingText = false;
            if (completeListener != null) {
                completeListener.onTextAnimationComplete();
            }
        }
    }
    
    public void setCustomerName(String name) {
        this.customerName = name;
        invalidate();
    }
    
    public void setOrderText(String text) {
        this.orderText = text;
        this.displayedText = text;
        invalidate();
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(3, difficulty));
        invalidate();
    }
    
    public void setBonusStars(int stars) {
        this.bonusStars = stars;
    }
    
    public int getBonusStars() {
        return bonusStars;
    }
    
    public void setCustomerAvatar(int drawableResId) {
        customerDrawable = ContextCompat.getDrawable(getContext(), drawableResId);
        invalidate();
    }
    
    public void setCustomerAvatar(String drawableName) {
        int resId = getContext().getResources().getIdentifier(
            drawableName, "drawable", getContext().getPackageName()
        );
        if (resId != 0) {
            customerDrawable = ContextCompat.getDrawable(getContext(), resId);
            invalidate();
        }
    }
    
    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.clickListener = listener;
    }
    
    public void setOnOrderCompleteListener(OnOrderCompleteListener listener) {
        this.completeListener = listener;
    }
    
    public void skipAnimation() {
        if (isAnimatingText) {
            handler.removeCallbacksAndMessages(null);
            displayedText = orderText;
            isAnimatingText = false;
            invalidate();
        }
    }
    
    public boolean isAnimating() {
        return isAnimatingText;
    }
    
    /**
     * Show thank you message after order completed
     */
    public void showThankYou(String thankYouText) {
        this.orderText = thankYouText;
        this.displayedText = "";
        this.textAnimIndex = 0;
        animateText();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }
}
