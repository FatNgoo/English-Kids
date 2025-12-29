package com.edu.english.masterchef.ui.cookbook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.Food;
import com.edu.english.masterchef.util.AnimationHelper;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Adapter for displaying unlocked recipes in cookbook with ViewPager2
 * Child-friendly UI with TTS support and large touch targets
 */
public class CookbookAdapter extends RecyclerView.Adapter<CookbookAdapter.RecipeViewHolder> {

    private List<Food> dishes;
    private OnDishInteractionListener listener;

    public interface OnDishInteractionListener {
        void onDishClick(Food dish);
        void onSpeakName(Food dish);
        void onReadRecipe(Food dish);
        void onViewDetails(Food dish);
    }

    public CookbookAdapter(List<Food> dishes, OnDishInteractionListener listener) {
        this.dishes = dishes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cookbook_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Food dish = dishes.get(position);
        holder.bind(dish, listener);
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    public void updateDishes(List<Food> newDishes) {
        this.dishes = newDishes;
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView dishImage;
        TextView dishName;
        TextView dishNameVn;
        TextView dishDescription;
        ImageButton btnSpeakName;
        MaterialButton btnViewDetails;
        MaterialButton btnReadRecipe;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            dishImage = itemView.findViewById(R.id.dish_image);
            dishName = itemView.findViewById(R.id.dish_name);
            dishNameVn = itemView.findViewById(R.id.dish_name_vn);
            dishDescription = itemView.findViewById(R.id.dish_description);
            btnSpeakName = itemView.findViewById(R.id.btn_speak_name);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnReadRecipe = itemView.findViewById(R.id.btn_read_recipe);
        }

        public void bind(Food dish, OnDishInteractionListener listener) {
            // Set dish name
            dishName.setText(dish.getName());

            // Set Vietnamese name if available
            if (dish.getNameVietnamese() != null && !dish.getNameVietnamese().isEmpty()) {
                dishNameVn.setText(dish.getNameVietnamese());
                dishNameVn.setVisibility(View.VISIBLE);
            } else {
                dishNameVn.setVisibility(View.GONE);
            }

            // Set description
            if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                dishDescription.setText(dish.getDescription());
                dishDescription.setVisibility(View.VISIBLE);
            } else {
                dishDescription.setVisibility(View.GONE);
            }

            // Load dish image
            int imageResId = itemView.getContext().getResources().getIdentifier(
                    dish.getImageRes(), "drawable", itemView.getContext().getPackageName()
            );
            if (imageResId != 0) {
                dishImage.setImageResource(imageResId);
            } else {
                dishImage.setImageResource(R.drawable.ic_placeholder);
            }

            // Animate card on bind
            AnimationHelper.fadeIn(cardView);

            // TTS speak name button
            btnSpeakName.setOnClickListener(v -> {
                AnimationHelper.bounce(btnSpeakName);
                if (listener != null) {
                    listener.onSpeakName(dish);
                }
            });

            // View details button
            btnViewDetails.setOnClickListener(v -> {
                AnimationHelper.pulse(btnViewDetails);
                if (listener != null) {
                    listener.onViewDetails(dish);
                }
            });

            // Read recipe button
            btnReadRecipe.setOnClickListener(v -> {
                AnimationHelper.pulse(btnReadRecipe);
                if (listener != null) {
                    listener.onReadRecipe(dish);
                }
            });

            // Card click
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDishClick(dish);
                }
            });
        }
    }
}
