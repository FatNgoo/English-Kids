package com.edu.english.magicmelody.ui.notebook.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.magicmelody.data.entity.CollectedNote;

/**
 * 🎵 Note Collection Adapter
 * 
 * RecyclerView adapter for Magic Notebook collection
 * - Grid layout with note cards
 * - Play button overlay
 * - Favorite toggle
 * - Mastery indicator
 */
public class NoteCollectionAdapter extends ListAdapter<CollectedNote, NoteCollectionAdapter.NoteViewHolder> {
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 CALLBACK
    // ═══════════════════════════════════════════════════════════════
    
    public interface OnNoteActionListener {
        void onNoteClick(CollectedNote note);
        void onPlayClick(CollectedNote note);
        void onFavoriteClick(CollectedNote note);
    }
    
    private OnNoteActionListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public NoteCollectionAdapter() {
        super(DIFF_CALLBACK);
    }
    
    private static final DiffUtil.ItemCallback<CollectedNote> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<CollectedNote>() {
            @Override
            public boolean areItemsTheSame(@NonNull CollectedNote oldItem, 
                                          @NonNull CollectedNote newItem) {
                return oldItem.getNoteId() == newItem.getNoteId();
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull CollectedNote oldItem, 
                                              @NonNull CollectedNote newItem) {
                return oldItem.getNoteName().equals(newItem.getNoteName()) &&
                       oldItem.isFavorite() == newItem.isFavorite() &&
                       oldItem.isMastered() == newItem.isMastered() &&
                       oldItem.getPlayCount() == newItem.getPlayCount();
            }
        };
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 ADAPTER METHODS
    // ═══════════════════════════════════════════════════════════════
    
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_note_collection, parent, false);
        return new NoteViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        CollectedNote note = getItem(position);
        holder.bind(note);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note);
            }
        });
    }
    
    public void setOnNoteActionListener(OnNoteActionListener listener) {
        this.listener = listener;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 VIEW HOLDER
    // ═══════════════════════════════════════════════════════════════
    
    class NoteViewHolder extends RecyclerView.ViewHolder {
        
        private final View cardContainer;
        private final ImageView noteIcon;
        private final TextView noteName;
        private final TextView noteWord;
        private final ImageButton playButton;
        private final ImageButton favoriteButton;
        private final View masteryIndicator;
        private final TextView playCountText;
        private final View noteTypeIndicator;
        private final View legendaryGlow;
        
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardContainer = itemView.findViewById(R.id.cardContainer);
            noteIcon = itemView.findViewById(R.id.noteIcon);
            noteName = itemView.findViewById(R.id.noteName);
            noteWord = itemView.findViewById(R.id.noteWord);
            playButton = itemView.findViewById(R.id.playButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            masteryIndicator = itemView.findViewById(R.id.masteryIndicator);
            playCountText = itemView.findViewById(R.id.playCountText);
            noteTypeIndicator = itemView.findViewById(R.id.noteTypeIndicator);
            legendaryGlow = itemView.findViewById(R.id.legendaryGlow);
        }
        
        public void bind(CollectedNote note) {
            // Set note name
            if (noteName != null) {
                noteName.setText(note.getNoteName());
            }
            
            // Set word
            if (noteWord != null) {
                noteWord.setText(note.getWord());
            }
            
            // Set play count
            if (playCountText != null) {
                String playText = "♪ " + note.getPlayCount();
                playCountText.setText(playText);
            }
            
            // Set note icon based on type
            if (noteIcon != null) {
                setNoteIcon(noteIcon, note.getNoteType());
            }
            
            // Set favorite state
            if (favoriteButton != null) {
                favoriteButton.setImageResource(
                    note.isFavorite() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline
                );
                
                favoriteButton.setOnClickListener(v -> {
                    if (listener != null) {
                        playFavoriteAnimation();
                        listener.onFavoriteClick(note);
                    }
                });
            }
            
            // Set play button
            if (playButton != null) {
                playButton.setOnClickListener(v -> {
                    if (listener != null) {
                        playPlayAnimation();
                        listener.onPlayClick(note);
                    }
                });
            }
            
            // Set mastery indicator
            if (masteryIndicator != null) {
                masteryIndicator.setVisibility(note.isMastered() ? View.VISIBLE : View.GONE);
            }
            
            // Set type indicator color
            if (noteTypeIndicator != null) {
                setTypeIndicatorColor(noteTypeIndicator, note.getNoteType());
            }
            
            // Set legendary glow
            if (legendaryGlow != null) {
                boolean isLegendary = "legendary".equals(note.getNoteType());
                legendaryGlow.setVisibility(isLegendary ? View.VISIBLE : View.GONE);
                
                if (isLegendary) {
                    playLegendaryGlowAnimation();
                }
            }
            
            // Set card background based on type
            if (cardContainer != null) {
                setCardBackground(cardContainer, note.getNoteType());
            }
        }
        
        private void setNoteIcon(ImageView imageView, String noteType) {
            int iconRes;
            
            switch (noteType) {
                case "special":
                    iconRes = R.drawable.ic_note_special;
                    break;
                case "legendary":
                    iconRes = R.drawable.ic_note_legendary;
                    break;
                case "basic":
                default:
                    iconRes = R.drawable.ic_note_basic;
            }
            
            // Check if drawable exists
            try {
                imageView.setImageResource(iconRes);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_music_note);
            }
        }
        
        private void setTypeIndicatorColor(View indicator, String noteType) {
            int color;
            
            switch (noteType) {
                case "special":
                    color = 0xFF00BCD4; // Cyan
                    break;
                case "legendary":
                    color = 0xFFFFD700; // Gold
                    break;
                case "basic":
                default:
                    color = 0xFF4CAF50; // Green
            }
            
            indicator.setBackgroundColor(color);
        }
        
        private void setCardBackground(View container, String noteType) {
            int bgRes;
            
            switch (noteType) {
                case "special":
                    bgRes = R.drawable.bg_note_card_special;
                    break;
                case "legendary":
                    bgRes = R.drawable.bg_note_card_legendary;
                    break;
                case "basic":
                default:
                    bgRes = R.drawable.bg_note_card_basic;
            }
            
            // Check if drawable exists
            try {
                container.setBackgroundResource(bgRes);
            } catch (Exception e) {
                container.setBackgroundResource(R.drawable.bg_card_glow);
            }
        }
        
        private void playFavoriteAnimation() {
            if (favoriteButton == null) return;
            
            AnimatorSet bounceSet = new AnimatorSet();
            
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(favoriteButton, "scaleX", 1f, 1.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(favoriteButton, "scaleY", 1f, 1.3f, 1f);
            
            bounceSet.playTogether(scaleX, scaleY);
            bounceSet.setDuration(200);
            bounceSet.start();
        }
        
        private void playPlayAnimation() {
            if (playButton == null) return;
            
            AnimatorSet bounceSet = new AnimatorSet();
            
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(playButton, "scaleX", 1f, 0.9f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(playButton, "scaleY", 1f, 0.9f, 1.1f, 1f);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(playButton, "rotation", 0f, 10f, -10f, 0f);
            
            bounceSet.playTogether(scaleX, scaleY, rotation);
            bounceSet.setDuration(300);
            bounceSet.start();
        }
        
        private void playLegendaryGlowAnimation() {
            if (legendaryGlow == null) return;
            
            ObjectAnimator pulse = ObjectAnimator.ofFloat(legendaryGlow, "alpha", 0.3f, 0.8f, 0.3f);
            pulse.setDuration(2000);
            pulse.setRepeatCount(ObjectAnimator.INFINITE);
            pulse.start();
        }
    }
}
