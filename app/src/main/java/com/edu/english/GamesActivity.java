package com.edu.english;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.magicmelody.ui.splash.MagicMelodySplashActivity;

public class GamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_games);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Setup game card click listeners
        setupGameCardClickListeners();
        
        // Start card animations
        animateCards();
    }

    private void setupGameCardClickListeners() {
        LinearLayout cardMasterChef = findViewById(R.id.card_master_chef);
        LinearLayout cardPetHospital = findViewById(R.id.card_pet_hospital);
        LinearLayout cardMagicMelody = findViewById(R.id.card_magic_melody);

        View.OnClickListener gameClickListener = v -> {
            // Scale animation on click
            animateCardPress(v);
            
            String gameName = "";
            int id = v.getId();
            if (id == R.id.card_master_chef) {
                gameName = "Master Chef";
                Toast.makeText(this, "Let's play " + gameName + "! 🎮", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.card_pet_hospital) {
                gameName = "Pet Hospital";
                Toast.makeText(this, "Let's play " + gameName + "! 🎮", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.card_magic_melody) {
                // Launch Magic Melody Game!
                launchMagicMelody();
                return;
            }
        };

        cardMasterChef.setOnClickListener(gameClickListener);
        cardPetHospital.setOnClickListener(gameClickListener);
        if (cardMagicMelody != null) {
            cardMagicMelody.setOnClickListener(gameClickListener);
        }
    }
    
    private void launchMagicMelody() {
        Intent intent = new Intent(this, MagicMelodySplashActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void animateCardPress(View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);
        
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);
        scaleUpX.setStartDelay(100);
        scaleUpY.setStartDelay(100);
        
        scaleDownX.start();
        scaleDownY.start();
        scaleUpX.start();
        scaleUpY.start();
    }

    private void animateCards() {
        int[] cardIds = {
            R.id.card_master_chef,
            R.id.card_pet_hospital,
            R.id.card_magic_melody
        };

        for (int i = 0; i < cardIds.length; i++) {
            View card = findViewById(cardIds[i]);
            if (card != null) {
                card.setAlpha(0f);
                card.setTranslationY(50f);
                
                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);
                ObjectAnimator translateAnim = ObjectAnimator.ofFloat(card, "translationY", 50f, 0f);
                
                alphaAnim.setDuration(400);
                translateAnim.setDuration(400);
                alphaAnim.setStartDelay(i * 150);
                translateAnim.setStartDelay(i * 150);
                alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                translateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                
                alphaAnim.start();
                translateAnim.start();
            }
        }
    }
}
