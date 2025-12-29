package com.edu.english.masterchef.ui.map;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.Food;
import com.edu.english.masterchef.data.model.LevelConfig;
import com.edu.english.masterchef.data.repository.LevelRepository;
import com.google.android.material.button.MaterialButton;

/**
 * Dialog showing level introduction before starting
 * Shows dish image, name, and preview of ingredients
 */
public class LevelIntroDialog extends Dialog {

    private LevelConfig level;
    private OnStartListener onStartListener;

    public interface OnStartListener {
        void onStart();
    }

    public LevelIntroDialog(@NonNull Context context, LevelConfig level) {
        super(context);
        this.level = level;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_level_intro);
        
        // Make dialog background transparent for rounded corners
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Get views
        ImageView dishImage = findViewById(R.id.dish_image);
        TextView dishName = findViewById(R.id.dish_name);
        TextView dishDescription = findViewById(R.id.dish_description);
        TextView difficultyText = findViewById(R.id.difficulty_text);
        MaterialButton btnStart = findViewById(R.id.btn_start);
        MaterialButton btnPreviewWords = findViewById(R.id.btn_preview_words);
        MaterialButton btnCancel = findViewById(R.id.btn_cancel);

        // Get food data
        Food food = LevelRepository.getInstance().getFood(level.getDishId());
        
        if (food != null) {
            // Set dish image - try to load, fallback to spaghetti for level 1
            String imageName = food.getImageRes();
            boolean imageSet = false;
            
            if (imageName != null && !imageName.isEmpty()) {
                int imageResId = getContext().getResources().getIdentifier(
                    imageName, "drawable", getContext().getPackageName()
                );
                if (imageResId != 0) {
                    dishImage.setImageResource(imageResId);
                    imageSet = true;
                }
            }
            
            if (!imageSet) {
                // Use level-specific fallback
                if (level.getLevelId() == 1) {
                    dishImage.setImageResource(R.drawable.ic_dish_spaghetti);
                } else {
                    dishImage.setImageResource(R.drawable.ic_food_placeholder);
                }
            }

            // Set dish name with emoji
            String foodEmoji = getFoodEmoji(food.getName());
            dishName.setText(foodEmoji + " " + food.getName());
            
            // Set description
            dishDescription.setText(food.getDescription());
        } else {
            // Fallback for level 1
            if (level.getLevelId() == 1) {
                dishImage.setImageResource(R.drawable.ic_dish_spaghetti);
                dishName.setText("🍝 Spaghetti Bolognese");
                dishDescription.setText("Classic Italian pasta with rich meat sauce");
            }
        }

        // Set difficulty with emoji
        String difficulty = level.getDifficulty();
        String difficultyEmoji = getDifficultyEmoji(difficulty);
        String formattedDifficulty = difficulty.substring(0, 1).toUpperCase() + difficulty.substring(1);
        difficultyText.setText(difficultyEmoji + " Difficulty: " + formattedDifficulty);

        // Set difficulty color
        int difficultyColor;
        switch (difficulty) {
            case "easy":
                difficultyColor = getContext().getColor(android.R.color.holo_green_dark);
                break;
            case "normal":
                difficultyColor = getContext().getColor(android.R.color.holo_orange_dark);
                break;
            case "hard":
                difficultyColor = getContext().getColor(android.R.color.holo_red_dark);
                break;
            default:
                difficultyColor = getContext().getColor(android.R.color.darker_gray);
        }
        difficultyText.setTextColor(difficultyColor);

        // Start button
        btnStart.setOnClickListener(v -> {
            if (onStartListener != null) {
                onStartListener.onStart();
            }
        });

        // Preview words button
        btnPreviewWords.setOnClickListener(v -> {
            // Show vocabulary preview dialog
            List<com.edu.english.masterchef.data.model.Ingredient> ingredients = 
                    LevelRepository.getInstance().getIngredientsForDish(level.getDishId());
            
            VocabularyPreviewDialog vocabDialog = new VocabularyPreviewDialog(getContext(), ingredients);
            vocabDialog.setOnStartListener(() -> {
                if (onStartListener != null) {
                    onStartListener.onStart();
                    dismiss();
                }
            });
            vocabDialog.show();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dismiss());
    }
    
    /**
     * Get appropriate emoji for food name
     */
    private String getFoodEmoji(String foodName) {
        if (foodName == null) return "🍽️";
        String lowerName = foodName.toLowerCase();
        
        if (lowerName.contains("spaghetti") || lowerName.contains("pasta")) return "🍝";
        if (lowerName.contains("pizza")) return "🍕";
        if (lowerName.contains("burger")) return "🍔";
        if (lowerName.contains("salad")) return "🥗";
        if (lowerName.contains("soup")) return "🍲";
        if (lowerName.contains("sandwich")) return "🥪";
        if (lowerName.contains("cake")) return "🎂";
        if (lowerName.contains("cookie")) return "🍪";
        if (lowerName.contains("rice")) return "🍚";
        if (lowerName.contains("chicken")) return "🍗";
        if (lowerName.contains("fish")) return "🐟";
        if (lowerName.contains("egg")) return "🥚";
        if (lowerName.contains("bread")) return "🍞";
        
        return "🍽️";
    }
    
    /**
     * Get emoji for difficulty level
     */
    private String getDifficultyEmoji(String difficulty) {
        if (difficulty == null) return "⭐";
        switch (difficulty.toLowerCase()) {
            case "easy": return "⭐";
            case "normal": return "⭐⭐";
            case "hard": return "⭐⭐⭐";
            default: return "⭐";
        }
    }

    public void setOnStartListener(OnStartListener listener) {
        this.onStartListener = listener;
    }
}
