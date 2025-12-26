package com.edu.english.magicmelody.ui.notebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.magicmelody.data.model.CollectionEntity;

import java.util.List;

/**
 * Adapter for NotebookActivity collection grid
 */
public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {
    
    private List<CollectionEntity> items;
    private final OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onItemClick(CollectionEntity item);
    }
    
    public CollectionAdapter(List<CollectionEntity> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }
    
    public void updateItems(List<CollectionEntity> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionEntity item = items.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        
        private final CardView cardItem;
        private final TextView txtVocab;
        private final TextView txtVietnamese;
        private final ImageView imgTheme;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardItem);
            txtVocab = itemView.findViewById(R.id.txtVocab);
            txtVietnamese = itemView.findViewById(R.id.txtVietnamese);
            imgTheme = itemView.findViewById(R.id.imgTheme);
        }
        
        void bind(CollectionEntity item) {
            txtVocab.setText(item.getVocab());
            txtVietnamese.setText(item.getVocabVietnamese());
            
            // Set color based on theme
            int color = getThemeColor(item.getTheme());
            cardItem.setCardBackgroundColor(color);
            
            // Click listener
            cardItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
                
                // Bounce animation
                cardItem.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            cardItem.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();
                        })
                        .start();
            });
        }
        
        private int getThemeColor(String theme) {
            if (theme == null) return 0xFF4CAF50;
            
            switch (theme) {
                case "FANTASY":
                    return 0xFF9C27B0;
                case "SCIFI":
                    return 0xFF2196F3;
                case "NATURE":
                default:
                    return 0xFF4CAF50;
            }
        }
    }
}
