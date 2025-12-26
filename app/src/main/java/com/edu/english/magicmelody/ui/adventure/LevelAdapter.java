package com.edu.english.magicmelody.ui.adventure;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;

import java.util.List;

/**
 * LevelAdapter - RecyclerView adapter for Magic Melody world map
 * Displays levels with musical theme in alternating pattern
 */
public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {
    
    private List<AdventureViewModel.LevelItem> levels;
    private final OnLevelClickListener listener;
    
    public interface OnLevelClickListener {
        void onLevelClick(AdventureViewModel.LevelItem level);
    }
    
    public LevelAdapter(List<AdventureViewModel.LevelItem> levels, OnLevelClickListener listener) {
        this.levels = levels;
        this.listener = listener;
    }
    
    public void updateLevels(List<AdventureViewModel.LevelItem> newLevels) {
        this.levels = newLevels;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_level_node, parent, false);
        return new LevelViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        AdventureViewModel.LevelItem level = levels.get(position);
        holder.bind(level, position);
    }
    
    @Override
    public int getItemCount() {
        return levels != null ? levels.size() : 0;
    }
    
    class LevelViewHolder extends RecyclerView.ViewHolder {
        
        private final CardView cardLevel;
        private final LinearLayout cardContent;
        private final TextView txtLevelNumber;
        private final TextView txtLevelTitle;
        private final ImageView imgLock;
        private final TextView txtStars;
        private final View pathLine;
        private final ImageButton btnPlay;
        
        LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            cardLevel = itemView.findViewById(R.id.cardLevel);
            cardContent = itemView.findViewById(R.id.cardContent);
            txtLevelNumber = itemView.findViewById(R.id.txtLevelNumber);
            txtLevelTitle = itemView.findViewById(R.id.txtLevelTitle);
            imgLock = itemView.findViewById(R.id.imgLock);
            txtStars = itemView.findViewById(R.id.txtStars);
            pathLine = itemView.findViewById(R.id.pathLine);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }
        
        void bind(AdventureViewModel.LevelItem level, int position) {
            // Set level number
            txtLevelNumber.setText(String.valueOf(level.getLevelNumber()));
            
            // Set title with music emoji
            String title = level.getTitleVietnamese() != null ? 
                    level.getTitleVietnamese() : level.getTitle();
            txtLevelTitle.setText("🎵 " + title);
            
            // Alternating zigzag layout for visual interest
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cardLevel.getLayoutParams();
            float density = itemView.getContext().getResources().getDisplayMetrics().density;
            int baseMargin = (int) (20 * density);
            int offsetMargin = (int) (40 * density);
            
            if (position % 2 == 0) {
                params.leftMargin = baseMargin;
                params.rightMargin = offsetMargin;
            } else {
                params.leftMargin = offsetMargin;
                params.rightMargin = baseMargin;
            }
            cardLevel.setLayoutParams(params);
            
            // Lock/unlock state with theme-appropriate styling
            if (level.isUnlocked()) {
                // Unlocked state - vibrant and inviting
                imgLock.setVisibility(View.GONE);
                cardLevel.setAlpha(1.0f);
                txtLevelNumber.setVisibility(View.VISIBLE);
                btnPlay.setVisibility(View.VISIBLE);
                btnPlay.setEnabled(true);
                
                // Set background for unlocked
                if (cardContent != null) {
                    cardContent.setBackgroundResource(R.drawable.bg_level_card_unlocked);
                }
                
                // Show stars earned
                String stars = getStarsString(level.getStars());
                txtStars.setText(stars);
                txtStars.setVisibility(View.VISIBLE);
                
                // Text colors for unlocked
                txtLevelTitle.setTextColor(0xFF4A148C); // Deep purple
                txtLevelNumber.setTextColor(0xFF5D4037); // Brown
            } else {
                // Locked state - subtle but still visible
                imgLock.setVisibility(View.VISIBLE);
                cardLevel.setAlpha(0.7f);
                txtLevelNumber.setVisibility(View.GONE);
                btnPlay.setVisibility(View.GONE);
                txtStars.setVisibility(View.GONE);
                
                // Set background for locked
                if (cardContent != null) {
                    cardContent.setBackgroundResource(R.drawable.bg_level_card_locked);
                }
                
                // Text colors for locked
                txtLevelTitle.setTextColor(0xAAFFFFFF); // Semi-transparent white
            }
            
            // Path line visibility (connects to next level)
            if (position < levels.size() - 1) {
                pathLine.setVisibility(View.VISIBLE);
            } else {
                pathLine.setVisibility(View.GONE);
            }
            
            // Click listener on entire card
            cardLevel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLevelClick(level);
                }
            });
            
            // Play button click
            if (btnPlay != null) {
                btnPlay.setOnClickListener(v -> {
                    if (listener != null && level.isUnlocked()) {
                        listener.onLevelClick(level);
                    }
                });
            }
        }
        
        private String getStarsString(int stars) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                if (i < stars) {
                    sb.append("⭐");
                } else {
                    sb.append("☆");
                }
            }
            return sb.toString();
        }
    }
}
