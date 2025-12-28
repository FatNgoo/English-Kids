package com.edu.english.magicmelody.ui.worldmap.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.magicmelody.ui.worldmap.WorldMapViewModel;

/**
 * 📚 Lesson Adapter
 * 
 * RecyclerView adapter for displaying lessons within a world
 * - Horizontal scrolling list
 * - Star rating display
 * - Lock/unlock states
 */
public class LessonAdapter extends ListAdapter<WorldMapViewModel.LessonDisplayItem, LessonAdapter.LessonViewHolder> {
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 CALLBACK
    // ═══════════════════════════════════════════════════════════════
    
    public interface OnLessonClickListener {
        void onLessonClick(WorldMapViewModel.LessonDisplayItem lesson);
    }
    
    private OnLessonClickListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public LessonAdapter() {
        super(DIFF_CALLBACK);
    }
    
    private static final DiffUtil.ItemCallback<WorldMapViewModel.LessonDisplayItem> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<WorldMapViewModel.LessonDisplayItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull WorldMapViewModel.LessonDisplayItem oldItem, 
                                          @NonNull WorldMapViewModel.LessonDisplayItem newItem) {
                return oldItem.getLessonId().equals(newItem.getLessonId());
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull WorldMapViewModel.LessonDisplayItem oldItem, 
                                              @NonNull WorldMapViewModel.LessonDisplayItem newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                       oldItem.isUnlocked() == newItem.isUnlocked() &&
                       oldItem.isCompleted() == newItem.isCompleted() &&
                       oldItem.getStars() == newItem.getStars();
            }
        };
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 ADAPTER METHODS
    // ═══════════════════════════════════════════════════════════════
    
    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        WorldMapViewModel.LessonDisplayItem lesson = getItem(position);
        holder.bind(lesson);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Play bounce animation
                holder.playClickAnimation();
                listener.onLessonClick(lesson);
            }
        });
    }
    
    public void setOnLessonClickListener(OnLessonClickListener listener) {
        this.listener = listener;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 VIEW HOLDER
    // ═══════════════════════════════════════════════════════════════
    
    static class LessonViewHolder extends RecyclerView.ViewHolder {
        
        private final View cardContainer;
        private final TextView lessonNumber;
        private final TextView lessonTitle;
        private final ImageView lockIcon;
        private final ImageView checkIcon;
        private final View star1, star2, star3;
        private final View difficultyIndicator;
        
        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardContainer = itemView.findViewById(R.id.cardContainer);
            lessonNumber = itemView.findViewById(R.id.lessonNumber);
            lessonTitle = itemView.findViewById(R.id.lessonTitle);
            lockIcon = itemView.findViewById(R.id.lockIcon);
            checkIcon = itemView.findViewById(R.id.checkIcon);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            difficultyIndicator = itemView.findViewById(R.id.difficultyIndicator);
        }
        
        public void bind(WorldMapViewModel.LessonDisplayItem lesson) {
            // Set lesson number
            if (lessonNumber != null) {
                lessonNumber.setText(String.valueOf(lesson.getLessonNumber()));
            }
            
            // Set lesson title
            if (lessonTitle != null) {
                lessonTitle.setText(lesson.getTitle());
            }
            
            // Handle lock state
            boolean isUnlocked = lesson.isUnlocked();
            boolean isCompleted = lesson.isCompleted();
            
            if (lockIcon != null) {
                lockIcon.setVisibility(isUnlocked ? View.GONE : View.VISIBLE);
            }
            
            if (checkIcon != null) {
                checkIcon.setVisibility(isCompleted ? View.VISIBLE : View.GONE);
            }
            
            // Set card opacity based on lock state
            if (cardContainer != null) {
                cardContainer.setAlpha(isUnlocked ? 1f : 0.5f);
                
                // Set background based on completion
                if (isCompleted) {
                    cardContainer.setBackgroundResource(R.drawable.bg_lesson_completed);
                } else if (isUnlocked) {
                    cardContainer.setBackgroundResource(R.drawable.bg_lesson_available);
                } else {
                    cardContainer.setBackgroundResource(R.drawable.bg_lesson_locked);
                }
            }
            
            // Set stars
            int stars = lesson.getStars();
            setStarState(star1, stars >= 1);
            setStarState(star2, stars >= 2);
            setStarState(star3, stars >= 3);
            
            // Set difficulty indicator color
            if (difficultyIndicator != null) {
                setDifficultyColor(difficultyIndicator, lesson.getDifficulty());
            }
        }
        
        private void setStarState(View star, boolean filled) {
            if (star == null) return;
            
            star.setAlpha(filled ? 1f : 0.3f);
            star.setScaleX(filled ? 1f : 0.8f);
            star.setScaleY(filled ? 1f : 0.8f);
        }
        
        private void setDifficultyColor(View indicator, int difficulty) {
            int color;
            
            switch (difficulty) {
                case 1:
                    color = 0xFF4CAF50; // Green - Easy
                    break;
                case 2:
                    color = 0xFFFFEB3B; // Yellow - Medium
                    break;
                case 3:
                    color = 0xFFFF9800; // Orange - Hard
                    break;
                case 4:
                    color = 0xFFFF5722; // Red-Orange - Very Hard
                    break;
                case 5:
                    color = 0xFFE91E63; // Pink - Expert
                    break;
                default:
                    color = 0xFF9E9E9E; // Gray
            }
            
            indicator.setBackgroundColor(color);
        }
        
        public void playClickAnimation() {
            AnimatorSet bounceSet = new AnimatorSet();
            
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", 1f, 0.95f, 1.05f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", 1f, 0.95f, 1.05f, 1f);
            
            bounceSet.playTogether(scaleX, scaleY);
            bounceSet.setDuration(200);
            bounceSet.start();
        }
    }
}
