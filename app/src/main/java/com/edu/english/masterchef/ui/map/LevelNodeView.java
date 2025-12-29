package com.edu.english.masterchef.ui.map;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.Food;
import com.edu.english.masterchef.data.model.LevelConfig;
import com.edu.english.masterchef.data.repository.LevelRepository;

/**
 * Custom view for a level node on the map
 * Shows level number, dish image, lock status, and stars
 * Child-friendly cooking theme design
 */
public class LevelNodeView extends LinearLayout {

    private ImageView dishImage;
    private ImageView lockIcon;
    private FrameLayout lockOverlay;
    private TextView levelNumber;
    private LinearLayout starsContainer;
    private ImageView star1, star2, star3;
    private androidx.cardview.widget.CardView cardContainer;

    private LevelConfig level;
    private boolean isUnlocked;
    private int stars;

    public LevelNodeView(Context context) {
        super(context);
        init(context);
    }

    public LevelNodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_level_node, this, true);

        dishImage = findViewById(R.id.dish_image);
        lockIcon = findViewById(R.id.lock_icon);
        lockOverlay = findViewById(R.id.lock_overlay);
        levelNumber = findViewById(R.id.level_number);
        starsContainer = findViewById(R.id.stars_container);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        cardContainer = findViewById(R.id.card_container);
    }

    /**
     * Set level data and update UI
     */
    public void setLevel(LevelConfig levelConfig, boolean unlocked, int earnedStars) {
        this.level = levelConfig;
        this.isUnlocked = unlocked;
        this.stars = earnedStars;

        // Set level number with emoji
        levelNumber.setText("🍳 Level " + levelConfig.getLevelId());

        // Get food data
        Food food = LevelRepository.getInstance().getFood(levelConfig.getDishId());
        
        if (food != null) {
            // Try to set dish image - use spaghetti icon for level 1
            String imageName = food.getImageRes();
            if (imageName != null && !imageName.isEmpty()) {
                int imageResId = getContext().getResources().getIdentifier(
                    imageName, "drawable", getContext().getPackageName()
                );
                if (imageResId != 0) {
                    dishImage.setImageResource(imageResId);
                } else {
                    // Use level-specific fallback
                    if (levelConfig.getLevelId() == 1) {
                        dishImage.setImageResource(R.drawable.ic_dish_spaghetti);
                    } else {
                        dishImage.setImageResource(R.drawable.ic_food_placeholder);
                    }
                }
            } else {
                // Use level-specific fallback
                if (levelConfig.getLevelId() == 1) {
                    dishImage.setImageResource(R.drawable.ic_dish_spaghetti);
                } else {
                    dishImage.setImageResource(R.drawable.ic_food_placeholder);
                }
            }
        } else {
            // Fallback - use spaghetti for level 1
            if (levelConfig.getLevelId() == 1) {
                dishImage.setImageResource(R.drawable.ic_dish_spaghetti);
            } else {
                dishImage.setImageResource(R.drawable.ic_food_placeholder);
            }
        }

        // Show/hide lock overlay
        if (unlocked) {
            if (lockOverlay != null) {
                lockOverlay.setVisibility(GONE);
            }
            lockIcon.setVisibility(GONE);
            dishImage.setAlpha(1.0f);
            
            // Show stars
            updateStars(earnedStars);
            
            // Pulse animation for unlocked levels
            startPulseAnimation();
        } else {
            if (lockOverlay != null) {
                lockOverlay.setVisibility(VISIBLE);
            }
            lockIcon.setVisibility(VISIBLE);
            dishImage.setAlpha(0.5f);
            starsContainer.setVisibility(GONE);
        }
    }

    /**
     * Update star display
     */
    private void updateStars(int count) {
        if (count == 0) {
            starsContainer.setVisibility(GONE);
            return;
        }

        starsContainer.setVisibility(VISIBLE);
        star1.setImageResource(count >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        star2.setImageResource(count >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        star3.setImageResource(count >= 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
    }

    /**
     * Play pulse animation for unlocked level
     */
    private void startPulseAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dishImage, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dishImage, "scaleY", 1f, 1.05f, 1f);
        scaleX.setDuration(1500);
        scaleY.setDuration(1500);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.start();
        scaleY.start();
    }

    /**
     * Play locked animation (shake)
     */
    public void playLockedAnimation() {
        ObjectAnimator shake = ObjectAnimator.ofFloat(lockIcon, "translationX", 
            0f, -10f, 10f, -10f, 10f, 0f);
        shake.setDuration(400);
        shake.start();

        // Also shake the whole view
        ObjectAnimator viewShake = ObjectAnimator.ofFloat(this, "rotation", 
            0f, -5f, 5f, -5f, 5f, 0f);
        viewShake.setDuration(400);
        viewShake.start();
    }

    public LevelConfig getLevel() {
        return level;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }
}
