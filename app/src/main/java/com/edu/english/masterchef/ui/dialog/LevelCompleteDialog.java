package com.edu.english.masterchef.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.edu.english.R;
import com.edu.english.masterchef.util.AnimationHelper;

/**
 * Dialog shown when a level is completed
 * Shows score, stars, stats, and options to replay or continue
 */
public class LevelCompleteDialog extends Dialog {

    private int score;
    private int stars;
    private int timeSeconds;
    private int errors;
    private int perfectSteps;
    private int totalSteps;
    private boolean nextLevelUnlocked;
    
    private OnActionListener listener;

    private ImageView star1, star2, star3;
    private TextView tvScore, tvMessage, tvTime, tvErrors, tvPerfectSteps, tvUnlockMessage;
    private Button btnReplay, btnContinue;

    public interface OnActionListener {
        void onReplay();
        void onContinue();
    }

    public LevelCompleteDialog(@NonNull Context context, int score, int stars, 
                                int timeSeconds, int errors, int perfectSteps, int totalSteps,
                                boolean nextLevelUnlocked) {
        super(context);
        this.score = score;
        this.stars = stars;
        this.timeSeconds = timeSeconds;
        this.errors = errors;
        this.perfectSteps = perfectSteps;
        this.totalSteps = totalSteps;
        this.nextLevelUnlocked = nextLevelUnlocked;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_level_complete_v2);
        setCancelable(false);
        
        // Make dialog background transparent to show rounded corners
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        findViews();
        setupViews();
        setupListeners();
        animateStars();
    }

    private void findViews() {
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        tvScore = findViewById(R.id.tv_score);
        tvMessage = findViewById(R.id.tv_message);
        tvTime = findViewById(R.id.tv_time);
        tvErrors = findViewById(R.id.tv_errors);
        tvPerfectSteps = findViewById(R.id.tv_perfect_steps);
        tvUnlockMessage = findViewById(R.id.tv_unlock_message);
        btnReplay = findViewById(R.id.btn_replay);
        btnContinue = findViewById(R.id.btn_continue);
    }

    private void setupViews() {
        // Score
        tvScore.setText("Score: " + score);

        // Message based on stars - Child-friendly messages
        String message;
        switch (stars) {
            case 3:
                message = "🎉 Amazing! You're a Master Chef! 🎉";
                break;
            case 2:
                message = "🌟 Great cooking! Keep it up! 🌟";
                break;
            case 1:
                message = "👍 Good job! You can do better! 👍";
                break;
            default:
                message = "💪 Nice try! Practice makes perfect! 💪";
                break;
        }
        tvMessage.setText(message);

        // Stats - formatted nicely
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        tvTime.setText(String.format("%dm %ds", minutes, seconds));
        tvErrors.setText(String.valueOf(errors));
        tvPerfectSteps.setText(String.format("%d/%d", perfectSteps, totalSteps));

        // Unlock message
        if (nextLevelUnlocked) {
            tvUnlockMessage.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnReplay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReplay();
            }
            dismiss();
        });

        btnContinue.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContinue();
            }
            dismiss();
        });
    }

    /**
     * Animate stars appearing one by one
     */
    private void animateStars() {
        Handler handler = new Handler();
        
        // Initially hide all stars
        star1.setVisibility(View.INVISIBLE);
        star2.setVisibility(View.INVISIBLE);
        star3.setVisibility(View.INVISIBLE);

        // Animate stars based on count
        if (stars >= 1) {
            handler.postDelayed(() -> {
                star1.setImageResource(R.drawable.ic_star_filled);
                star1.setVisibility(View.VISIBLE);
                AnimationHelper.pop(star1);
            }, 300);
        }

        if (stars >= 2) {
            handler.postDelayed(() -> {
                star2.setImageResource(R.drawable.ic_star_filled);
                star2.setVisibility(View.VISIBLE);
                AnimationHelper.pop(star2);
            }, 600);
        }

        if (stars >= 3) {
            handler.postDelayed(() -> {
                star3.setImageResource(R.drawable.ic_star_filled);
                star3.setVisibility(View.VISIBLE);
                AnimationHelper.pop(star3);
            }, 900);
        }
    }

    public void setOnActionListener(OnActionListener listener) {
        this.listener = listener;
    }
}
