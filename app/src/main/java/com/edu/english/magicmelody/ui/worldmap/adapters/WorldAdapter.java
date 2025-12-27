package com.edu.english.magicmelody.ui.worldmap.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.magicmelody.ui.worldmap.WorldMapViewModel;

/**
 * 🌍 World Adapter
 * 
 * RecyclerView adapter for displaying worlds on the map
 * - Grid layout for 3D-style world map
 * - Unlock/lock states
 * - Evolution progress indicators
 */
public class WorldAdapter extends ListAdapter<WorldMapViewModel.WorldDisplayItem, WorldAdapter.WorldViewHolder> {
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 CALLBACK
    // ═══════════════════════════════════════════════════════════════
    
    public interface OnWorldClickListener {
        void onWorldClick(WorldMapViewModel.WorldDisplayItem world);
        void onWorldLongClick(WorldMapViewModel.WorldDisplayItem world);
    }
    
    private OnWorldClickListener listener;
    private int selectedPosition = -1;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public WorldAdapter() {
        super(DIFF_CALLBACK);
    }
    
    private static final DiffUtil.ItemCallback<WorldMapViewModel.WorldDisplayItem> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<WorldMapViewModel.WorldDisplayItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull WorldMapViewModel.WorldDisplayItem oldItem, 
                                          @NonNull WorldMapViewModel.WorldDisplayItem newItem) {
                return oldItem.getWorldId().equals(newItem.getWorldId());
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull WorldMapViewModel.WorldDisplayItem oldItem, 
                                              @NonNull WorldMapViewModel.WorldDisplayItem newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                       oldItem.isUnlocked() == newItem.isUnlocked() &&
                       oldItem.getEvolutionStage() == newItem.getEvolutionStage() &&
                       oldItem.getCompletedLessons() == newItem.getCompletedLessons();
            }
        };
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 ADAPTER METHODS
    // ═══════════════════════════════════════════════════════════════
    
    @NonNull
    @Override
    public WorldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_world, parent, false);
        return new WorldViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WorldViewHolder holder, int position) {
        WorldMapViewModel.WorldDisplayItem world = getItem(position);
        holder.bind(world, position == selectedPosition);
        
        holder.itemView.setOnClickListener(v -> {
            int prevSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            if (prevSelected != -1) {
                notifyItemChanged(prevSelected);
            }
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onWorldClick(world);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onWorldLongClick(world);
            }
            return true;
        });
    }
    
    public void setOnWorldClickListener(OnWorldClickListener listener) {
        this.listener = listener;
    }
    
    public void setSelectedWorld(String worldId) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).getWorldId().equals(worldId)) {
                int prevSelected = selectedPosition;
                selectedPosition = i;
                
                if (prevSelected != -1) {
                    notifyItemChanged(prevSelected);
                }
                notifyItemChanged(selectedPosition);
                break;
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 VIEW HOLDER
    // ═══════════════════════════════════════════════════════════════
    
    static class WorldViewHolder extends RecyclerView.ViewHolder {
        
        private final View cardContainer;
        private final ImageView worldIcon;
        private final ImageView lockIcon;
        private final TextView worldName;
        private final TextView worldDescription;
        private final ProgressBar evolutionProgress;
        private final TextView progressText;
        private final View selectionGlow;
        private final View evolutionStars;
        
        public WorldViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardContainer = itemView.findViewById(R.id.cardContainer);
            worldIcon = itemView.findViewById(R.id.worldIcon);
            lockIcon = itemView.findViewById(R.id.lockIcon);
            worldName = itemView.findViewById(R.id.worldName);
            worldDescription = itemView.findViewById(R.id.worldDescription);
            evolutionProgress = itemView.findViewById(R.id.evolutionProgress);
            progressText = itemView.findViewById(R.id.progressText);
            selectionGlow = itemView.findViewById(R.id.selectionGlow);
            evolutionStars = itemView.findViewById(R.id.evolutionStars);
        }
        
        public void bind(WorldMapViewModel.WorldDisplayItem world, boolean isSelected) {
            // Set world name
            if (worldName != null) {
                worldName.setText(world.getName());
            }
            
            // Set description
            if (worldDescription != null) {
                worldDescription.setText(world.getDescription());
            }
            
            // Handle lock state
            boolean isUnlocked = world.isUnlocked();
            
            if (lockIcon != null) {
                lockIcon.setVisibility(isUnlocked ? View.GONE : View.VISIBLE);
            }
            
            if (worldIcon != null) {
                worldIcon.setAlpha(isUnlocked ? 1f : 0.5f);
                // Set world icon based on worldId
                setWorldIcon(worldIcon, world.getWorldId(), world.getEvolutionStage());
            }
            
            // Set evolution progress
            if (evolutionProgress != null) {
                evolutionProgress.setMax(world.getTotalLessons());
                evolutionProgress.setProgress(world.getCompletedLessons());
            }
            
            if (progressText != null) {
                String progress = world.getCompletedLessons() + "/" + world.getTotalLessons();
                progressText.setText(progress);
            }
            
            // Handle selection state
            if (selectionGlow != null) {
                selectionGlow.setVisibility(isSelected ? View.VISIBLE : View.GONE);
                
                if (isSelected) {
                    playSelectionAnimation();
                }
            }
            
            // Set card background based on evolution
            if (cardContainer != null) {
                setEvolutionBackground(cardContainer, world.getEvolutionStage());
            }
        }
        
        private void setWorldIcon(ImageView imageView, String worldId, int evolutionStage) {
            // Set icon based on world and evolution stage
            // In production, load from drawable resources
            int iconRes = R.drawable.ic_world_default;
            
            // Example: ic_world_forest_0, ic_world_forest_1, etc.
            String iconName = "ic_world_" + worldId.replace("world_", "") + "_" + evolutionStage;
            
            // Try to get drawable by name
            int resId = imageView.getContext().getResources()
                .getIdentifier(iconName, "drawable", imageView.getContext().getPackageName());
            
            if (resId != 0) {
                imageView.setImageResource(resId);
            } else {
                imageView.setImageResource(iconRes);
            }
        }
        
        private void setEvolutionBackground(View container, int evolutionStage) {
            // Set background gradient based on evolution
            int bgRes;
            
            switch (evolutionStage) {
                case 0:
                    bgRes = R.drawable.bg_card_world_gray;
                    break;
                case 1:
                    bgRes = R.drawable.bg_card_world_tinted;
                    break;
                case 2:
                    bgRes = R.drawable.bg_card_world_colorful;
                    break;
                case 3:
                    bgRes = R.drawable.bg_card_world_vibrant;
                    break;
                default:
                    bgRes = R.drawable.bg_card_world_gray;
            }
            
            // Check if drawable exists
            try {
                container.setBackgroundResource(bgRes);
            } catch (Exception e) {
                // Fallback
                container.setBackgroundResource(R.drawable.bg_card_glow);
            }
        }
        
        private void playSelectionAnimation() {
            if (selectionGlow == null) return;
            
            AnimatorSet pulseSet = new AnimatorSet();
            
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(selectionGlow, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(selectionGlow, "scaleY", 1f, 1.1f, 1f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(selectionGlow, "alpha", 0.5f, 1f, 0.5f);
            
            pulseSet.playTogether(scaleX, scaleY, alpha);
            pulseSet.setDuration(1000);
            pulseSet.start();
        }
    }
}
