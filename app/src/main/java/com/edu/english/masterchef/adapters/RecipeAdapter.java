package com.edu.english.masterchef.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.masterchef.data.Recipe;

import java.util.List;

/**
 * RecyclerView Adapter for displaying recipe list in recipe selection screen.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final List<Recipe> recipes;
    private final Context context;
    private final OnRecipeClickListener listener;
    
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }
    
    public RecipeAdapter(Context context, List<Recipe> recipes, OnRecipeClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }
    
    @Override
    public int getItemCount() {
        return recipes.size();
    }
    
    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgRecipe;
        private final TextView textRecipeName;
        private final TextView textRecipeNameVi;
        private final TextView textDifficulty;
        private final TextView textStepsCount;
        private final View btnPlayRecipe;
        
        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecipe = itemView.findViewById(R.id.img_recipe);
            textRecipeName = itemView.findViewById(R.id.text_recipe_name);
            textRecipeNameVi = itemView.findViewById(R.id.text_recipe_name_vi);
            textDifficulty = itemView.findViewById(R.id.text_difficulty);
            textStepsCount = itemView.findViewById(R.id.text_steps_count);
            btnPlayRecipe = itemView.findViewById(R.id.btn_play_recipe);
        }
        
        void bind(Recipe recipe) {
            textRecipeName.setText(recipe.getNameEn());
            textRecipeNameVi.setText(recipe.getNameVi());
            textStepsCount.setText(recipe.getSteps().size() + " steps");
            
            // Set difficulty with appropriate color
            int difficulty = recipe.getDifficulty();
            String difficultyText;
            int difficultyColor;
            
            if (difficulty <= 1) {
                difficultyText = "Easy";
                difficultyColor = 0xFF4CAF50; // Green
            } else if (difficulty == 2) {
                difficultyText = "Medium";
                difficultyColor = 0xFFFF9800; // Orange
            } else {
                difficultyText = "Hard";
                difficultyColor = 0xFFF44336; // Red
            }
            
            textDifficulty.setText(difficultyText);
            textDifficulty.getBackground().setTint(difficultyColor);
            
            // Set recipe thumbnail
            try {
                int drawableId = context.getResources().getIdentifier(
                    recipe.getThumbnailDrawable(),
                    "drawable",
                    context.getPackageName()
                );
                if (drawableId != 0) {
                    imgRecipe.setImageDrawable(ContextCompat.getDrawable(context, drawableId));
                } else {
                    imgRecipe.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_tool_pot));
                }
            } catch (Exception e) {
                imgRecipe.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_tool_pot));
            }
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });
            
            btnPlayRecipe.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });
        }
    }
}
