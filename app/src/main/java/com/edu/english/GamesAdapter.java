package com.edu.english;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GameViewHolder> {

    private List<GameItem> games;
    private OnGameClickListener listener;

    public interface OnGameClickListener {
        void onGameClick(GameItem game);
    }

    public GamesAdapter(List<GameItem> games, OnGameClickListener listener) {
        this.games = games;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameItem game = games.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    class GameViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout container;
        private TextView titleText;
        private TextView subtitleText;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.game_container);
            titleText = itemView.findViewById(R.id.game_title);
            subtitleText = itemView.findViewById(R.id.game_subtitle);
        }

        public void bind(GameItem game) {
            titleText.setText(game.getTitle());
            
            // Handle subtitle
            if (subtitleText != null) {
                if (game.hasSubtitle()) {
                    subtitleText.setText(game.getSubtitle());
                    subtitleText.setVisibility(View.VISIBLE);
                } else {
                    subtitleText.setVisibility(View.GONE);
                }
            }
            
            // Use background images for each game
            switch (game.getTitle()) {
                case "Word Race":
                    container.setBackgroundResource(R.drawable.bg_wordrace);
                    break;
                case "Master Chef":
                    container.setBackgroundResource(R.drawable.bg_masterchef);
                    break;
                case "Detective":
                    container.setBackgroundResource(R.drawable.bg_detective);
                    break;
                case "Magic Melody":
                    container.setBackgroundResource(R.drawable.bg_magicmelody);
                    break;
                case "Magic Storybook":
                    container.setBackgroundResource(R.drawable.bg_magic_storybook);
                    break;
                default:
                    container.setBackgroundColor(game.getBackgroundColor());
                    break;
            }

            // Set click listener with animation
            itemView.setOnClickListener(v -> {
                animateClick(v);
                if (listener != null) {
                    listener.onGameClick(game);
                }
            });
        }

        private void animateClick(View view) {
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.98f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.98f);
            scaleDownX.setDuration(100);
            scaleDownY.setDuration(100);

            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.98f, 1f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.98f, 1f);
            scaleUpX.setDuration(100);
            scaleUpY.setDuration(100);
            scaleUpX.setStartDelay(100);
            scaleUpY.setStartDelay(100);

            scaleDownX.start();
            scaleDownY.start();
            scaleUpX.start();
            scaleUpY.start();
        }
    }
}

