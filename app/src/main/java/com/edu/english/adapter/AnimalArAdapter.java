package com.edu.english.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.model.AnimalArItem;

import java.util.List;

/**
 * RecyclerView Adapter for Animal AR selection grid
 */
public class AnimalArAdapter extends RecyclerView.Adapter<AnimalArAdapter.AnimalViewHolder> {
    
    private List<AnimalArItem> animals;
    private OnAnimalClickListener listener;
    
    public interface OnAnimalClickListener {
        void onAnimalClick(AnimalArItem animal);
    }
    
    public AnimalArAdapter(List<AnimalArItem> animals, OnAnimalClickListener listener) {
        this.animals = animals;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_animal_ar, parent, false);
        return new AnimalViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        AnimalArItem animal = animals.get(position);
        holder.bind(animal);
    }
    
    @Override
    public int getItemCount() {
        return animals != null ? animals.size() : 0;
    }
    
    class AnimalViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView imgAnimal;
        private TextView txtNameEn;
        private View cardContainer;
        
        AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAnimal = itemView.findViewById(R.id.img_animal);
            txtNameEn = itemView.findViewById(R.id.txt_name_en);
            cardContainer = itemView.findViewById(R.id.card_container);
        }
        
        void bind(AnimalArItem animal) {
            // Set image
            imgAnimal.setImageResource(animal.getThumbResId());
            
            // Set name
            txtNameEn.setText(animal.getNameEn());
            
            // Click listener with animation
            cardContainer.setOnClickListener(v -> {
                // Scale animation
                v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                if (listener != null) {
                                    listener.onAnimalClick(animal);
                                }
                            })
                            .start();
                    })
                    .start();
            });
        }
    }
}
