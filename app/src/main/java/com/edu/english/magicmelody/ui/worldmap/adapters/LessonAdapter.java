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
        private final TextView lessonTitle;
        private final ImageView lockIcon;
        private final ImageView checkIcon;
        
        
        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardContainer = itemView.findViewById(R.id.cardContainer);
            lessonTitle = itemView.findViewById(R.id.lessonTitle);
            lockIcon = itemView.findViewById(R.id.lockIcon);
            checkIcon = itemView.findViewById(R.id.checkIcon);
            
        }
        
        public void bind(WorldMapViewModel.LessonDisplayItem lesson) {
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
            
            // (stars and difficulty indicator removed from layout)
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
