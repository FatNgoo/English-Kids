package com.edu.english.magicmelody.ui.notebook;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 🧩 Adapter for displaying collected puzzle pictures
 */
public class PuzzlePictureAdapter extends RecyclerView.Adapter<PuzzlePictureAdapter.ViewHolder> {

    private List<PuzzlePicture> pictures;
    private Context context;

    public PuzzlePictureAdapter(Context context, List<PuzzlePicture> pictures) {
        this.context = context;
        this.pictures = pictures;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_puzzle_picture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PuzzlePicture picture = pictures.get(position);
        
        holder.worldEmoji.setText(picture.getWorldEmoji());
        holder.worldName.setText(picture.getWorldName());
        holder.puzzleProgress.setText("🧩 " + picture.getAwakenedCount() + "/" + picture.getTotalPieces() + " completed");
        
        // Build mini puzzle preview (3x3 grid)
        buildPuzzlePreview(holder.puzzleGrid, picture);
        
        // Click to open word puzzle game
        holder.itemView.setOnClickListener(v -> {
            // Use view context (safe if adapter has app context)
            android.content.Context startCtx = v.getContext();
            android.content.Intent intent = new android.content.Intent(startCtx, WordPuzzleActivity.class);
            intent.putExtra("worldId", picture.getWorldId());
            intent.putExtra("level", picture.getLevel());
            intent.putExtra("worldName", picture.getWorldName());
            intent.putExtra("keyword", picture.getKeyword());
            // picture.getAwakenedWords() may return Arrays$ArrayList (not java.util.ArrayList)
            List<String> awakened = picture.getAwakenedWords();
            if (awakened == null) {
                intent.putStringArrayListExtra("awakenedWords", new ArrayList<>());
            } else if (awakened instanceof ArrayList) {
                intent.putStringArrayListExtra("awakenedWords", (ArrayList<String>) awakened);
            } else {
                // Make a safe copy
                intent.putStringArrayListExtra("awakenedWords", new ArrayList<>(awakened));
            }
            if (!(startCtx instanceof android.app.Activity)) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            try {
                startCtx.startActivity(intent);
            } catch (Exception e) {
                // log and show a short message instead of crashing
                Log.e("PuzzlePictureAdapter", "Failed to start WordPuzzleActivity", e);
                Toast.makeText(startCtx, "Unable to open notebook item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    private void buildPuzzlePreview(GridLayout grid, PuzzlePicture picture) {
        grid.removeAllViews();
        
        // Post để đảm bảo grid đã có kích thước
        grid.post(() -> {
            grid.removeAllViews();
            
            int gridWidth = grid.getWidth();
            int gridHeight = grid.getHeight();
            
            // Nếu chưa có kích thước, dùng giá trị mặc định
            if (gridWidth == 0 || gridHeight == 0) {
                float density = context.getResources().getDisplayMetrics().density;
                gridWidth = gridHeight = (int) (220 * density);
            }
            
            float density = context.getResources().getDisplayMetrics().density;
            int margin = (int) (4 * density);
            int totalMargin = margin * 6; // 3x3 grid có 6 margins giữa các pieces
            int availableSize = Math.min(gridWidth, gridHeight) - totalMargin - (int)(24 * density); // trừ margin của grid
            int pieceSize = availableSize / 3;
            
            List<String> words = picture.getAwakenedWords();
            int wordIndex = 0;
            
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    FrameLayout piece = new FrameLayout(context);
                    
                    if (row == 1 && col == 1) {
                        // Center - world icon
                        piece.setBackgroundResource(R.drawable.bg_puzzle_piece);
                        ImageView icon = new ImageView(context);
                        icon.setImageResource(getWorldIconDrawable(picture.getWorldId()));
                        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        int padding = (int) (10 * density);
                        icon.setPadding(padding, padding, padding, padding);
                        
                        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        );
                        icon.setLayoutParams(iconParams);
                        piece.addView(icon);
                    } else {
                        if (wordIndex < words.size()) {
                            String word = words.get(wordIndex);
                            piece.setBackgroundResource(R.drawable.bg_puzzle_piece);
                            
                            ImageView icon = new ImageView(context);
                            icon.setImageResource(getWordIconDrawable(word));
                            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            int padding = (int) (10 * density);
                            icon.setPadding(padding, padding, padding, padding);
                            
                            FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            );
                            icon.setLayoutParams(iconParams);
                            piece.addView(icon);
                            
                            wordIndex++;
                        } else {
                            piece.setBackgroundResource(R.drawable.bg_puzzle_piece_dark);
                        }
                    }
                    
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = pieceSize;
                    params.height = pieceSize;
                    params.setMargins(margin, margin, margin, margin);
                    params.rowSpec = GridLayout.spec(row, 1f);
                    params.columnSpec = GridLayout.spec(col, 1f);
                    piece.setLayoutParams(params);
                    
                    grid.addView(piece);
                }
            }
        });
    }

    private int getWorldIconDrawable(String worldId) {
        switch (worldId) {
            case "world_forest": return R.drawable.ic_forest_tree;
            case "world_ocean": return R.drawable.ic_ocean_whale;
            case "world_mountain": return R.drawable.ic_mountain_peak;
            case "world_desert": return R.drawable.ic_desert_pyramid;
            case "world_sky": return R.drawable.ic_sky_rocket;
            default: return R.drawable.ic_forest_tree;
        }
    }

    private int getWordIconDrawable(String word) {
        switch (word) {
            case "Tree": return R.drawable.ic_forest_tree;
            case "Flower": return R.drawable.ic_forest_flower;
            case "Bird": return R.drawable.ic_forest_bird;
            case "Sun": return R.drawable.ic_forest_sun;
            case "Cloud": return R.drawable.ic_forest_cloud;
            case "Butterfly": return R.drawable.ic_forest_butterfly;
            case "Mushroom": return R.drawable.ic_forest_mushroom;
            case "Grass": return R.drawable.ic_forest_grass;
            case "Fish": return R.drawable.ic_ocean_fish;
            case "Whale": return R.drawable.ic_ocean_whale;
            case "Jellyfish": return R.drawable.ic_ocean_jellyfish;
            case "Coral": return R.drawable.ic_ocean_coral;
            case "Seaweed": return R.drawable.ic_ocean_seaweed;
            case "Shell": return R.drawable.ic_ocean_shell;
            case "Crab": return R.drawable.ic_ocean_crab;
            case "Starfish": return R.drawable.ic_ocean_starfish;
            case "Goat": return R.drawable.ic_mountain_goat;
            case "Eagle": return R.drawable.ic_mountain_eagle;
            case "Snow": return R.drawable.ic_mountain_snow;
            case "Rock": return R.drawable.ic_mountain_rock;
            case "Pine": return R.drawable.ic_mountain_pine;
            case "Cabin": return R.drawable.ic_mountain_cabin;
            case "Peak": return R.drawable.ic_mountain_peak;
            case "River": return R.drawable.ic_mountain_river;
            case "Camel": return R.drawable.ic_desert_camel;
            case "Cactus": return R.drawable.ic_desert_cactus;
            case "Scorpion": return R.drawable.ic_desert_scorpion;
            case "Lizard": return R.drawable.ic_desert_lizard;
            case "Oasis": return R.drawable.ic_desert_oasis;
            case "Pyramid": return R.drawable.ic_desert_pyramid;
            case "Sand": return R.drawable.ic_desert_sand;
            case "Plane": return R.drawable.ic_sky_plane;
            case "Balloon": return R.drawable.ic_sky_balloon;
            case "Rainbow": return R.drawable.ic_sky_rainbow;
            case "Star": return R.drawable.ic_sky_star;
            case "Moon": return R.drawable.ic_sky_moon;
            case "Rocket": return R.drawable.ic_sky_rocket;
            case "Kite": return R.drawable.ic_sky_kite;
            case "UFO": return R.drawable.ic_sky_ufo;
            default: return R.drawable.ic_forest_tree;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        GridLayout puzzleGrid;
        TextView worldEmoji, worldName, puzzleProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            puzzleGrid = itemView.findViewById(R.id.puzzle_preview_grid);
            worldEmoji = itemView.findViewById(R.id.world_emoji);
            worldName = itemView.findViewById(R.id.world_name);
            puzzleProgress = itemView.findViewById(R.id.puzzle_progress);
        }
    }
}
