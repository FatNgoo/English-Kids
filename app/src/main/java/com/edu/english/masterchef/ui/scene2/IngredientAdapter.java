package com.edu.english.masterchef.ui.scene2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.Ingredient;
import com.edu.english.masterchef.util.AnimationHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for ingredient grid in Scene 2
 */
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private List<Ingredient> ingredients = new ArrayList<>();
    private Set<String> selectedIds = new HashSet<>();
    private OnItemClickListener listener;
    private String difficulty = "normal"; // easy, normal, hard

    public interface OnItemClickListener {
        void onItemClick(Ingredient ingredient);
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.bind(ingredient, selectedIds.contains(ingredient.getIngredientId()));
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedIds(Set<String> selectedIds) {
        this.selectedIds = selectedIds != null ? selectedIds : new HashSet<>();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
        notifyDataSetChanged();
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIngredient, ivCheckmark;
        private TextView tvIngredientName;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIngredient = itemView.findViewById(R.id.iv_ingredient);
            ivCheckmark = itemView.findViewById(R.id.iv_checkmark);
            tvIngredientName = itemView.findViewById(R.id.tv_ingredient_name);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(ingredients.get(position));
                    AnimationHelper.bounce(itemView);
                }
            });
        }

        public void bind(Ingredient ingredient, boolean isSelected) {
            tvIngredientName.setText(ingredient.getName());
            
            // Set ingredient image based on difficulty
            // Easy: Always show images
            // Normal: Show images
            // Hard: No images (text only)
            if ("hard".equals(difficulty)) {
                ivIngredient.setVisibility(View.GONE);
            } else {
                ivIngredient.setVisibility(View.VISIBLE);
                // Try to get custom icon based on ingredient name
                int iconRes = getIngredientIcon(ingredient.getName());
                if (iconRes != 0) {
                    ivIngredient.setImageResource(iconRes);
                } else {
                    // Fallback to default image resource
                    ivIngredient.setImageResource(ingredient.getImageRes());
                }
            }

            // Show/hide checkmark and update background
            if (isSelected) {
                ivCheckmark.setVisibility(View.VISIBLE);
                itemView.setSelected(true);
            } else {
                ivCheckmark.setVisibility(View.GONE);
                itemView.setSelected(false);
            }
        }
        
        /**
         * Get appropriate icon resource for ingredient name
         */
        private int getIngredientIcon(String name) {
            if (name == null) return 0;
            String lowerName = name.toLowerCase();
            
            if (lowerName.contains("tomato")) return R.drawable.ic_ingredient_tomato;
            if (lowerName.contains("onion")) return R.drawable.ic_ingredient_onion;
            if (lowerName.contains("garlic")) return R.drawable.ic_ingredient_garlic;
            if (lowerName.contains("carrot")) return R.drawable.ic_ingredient_carrot;
            if (lowerName.contains("beef") || lowerName.contains("meat") || lowerName.contains("ground")) 
                return R.drawable.ic_ingredient_beef;
            if (lowerName.contains("pasta") || lowerName.contains("spaghetti") || lowerName.contains("noodle")) 
                return R.drawable.ic_ingredient_pasta;
            if (lowerName.contains("oil") || lowerName.contains("olive")) 
                return R.drawable.ic_ingredient_oil;
            
            return 0; // Use default
        }
    }
}