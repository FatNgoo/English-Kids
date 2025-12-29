package com.edu.english.masterchef.ui.cookbook;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.Food;
import com.edu.english.masterchef.data.model.Ingredient;
import com.edu.english.masterchef.data.repository.LevelRepository;
import com.edu.english.masterchef.util.TTSManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Dialog showing dish details with ingredients
 * Click ingredient to hear TTS pronunciation
 */
public class DishDetailDialog extends Dialog {

    private Food dish;
    private TTSManager ttsManager;

    public DishDetailDialog(@NonNull Context context, Food dish) {
        super(context);
        this.dish = dish;
        this.ttsManager = TTSManager.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_dish_detail);

        // Get views
        ImageView dishImage = findViewById(R.id.dish_image);
        TextView dishName = findViewById(R.id.dish_name);
        TextView dishDescription = findViewById(R.id.dish_description);
        LinearLayout ingredientsContainer = findViewById(R.id.ingredients_container);
        MaterialButton btnClose = findViewById(R.id.btn_close);
        MaterialButton btnReadRecipe = findViewById(R.id.btn_read_recipe);

        // Set dish data
        dishName.setText(dish.getName());
        dishDescription.setText(dish.getDescription());

        // Load dish image
        int imageResId = getContext().getResources().getIdentifier(
                dish.getImageRes(), "drawable", getContext().getPackageName()
        );
        if (imageResId != 0) {
            dishImage.setImageResource(imageResId);
        }

        // Get ingredients from level config
        List<Ingredient> ingredients = LevelRepository.getInstance().getIngredientsForDish(dish.getDishId());

        // Display ingredients
        for (Ingredient ingredient : ingredients) {
            View ingredientView = getLayoutInflater().inflate(R.layout.item_ingredient_detail, ingredientsContainer, false);

            ImageView ingredientIcon = ingredientView.findViewById(R.id.ingredient_icon);
            TextView ingredientName = ingredientView.findViewById(R.id.ingredient_name);
            ImageView speakerIcon = ingredientView.findViewById(R.id.speaker_icon);

            // Set ingredient data
            ingredientName.setText(ingredient.getName());
            
            // Load ingredient icon (placeholder for now)
            ingredientIcon.setImageResource(R.drawable.ic_placeholder);

            // TTS on click
            ingredientView.setOnClickListener(v -> {
                ttsManager.speak(ingredient.getName(), "ingredient_" + ingredient.getIngredientId());
                speakerIcon.setVisibility(View.VISIBLE);
                speakerIcon.postDelayed(() -> speakerIcon.setVisibility(View.GONE), 500);
            });

            ingredientsContainer.addView(ingredientView);
        }

        // Read full recipe button
        btnReadRecipe.setOnClickListener(v -> {
            String recipeText = "This is " + dish.getName() + ". " + dish.getDescription() + 
                    ". Ingredients: " + getIngredientsText(ingredients);
            ttsManager.speak(recipeText, "recipe_full");
        });

        // Close button
        btnClose.setOnClickListener(v -> dismiss());
    }

    private String getIngredientsText(List<Ingredient> ingredients) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            text.append(ingredients.get(i).getName());
            if (i < ingredients.size() - 1) {
                text.append(", ");
            }
        }
        return text.toString();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ttsManager != null) {
            ttsManager.stop();
        }
    }
}
