package com.edu.english;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.edu.english.coloralchemy.ColorAlchemyActivity;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup profile click to open drawer
        LinearLayout profileSection = findViewById(R.id.profile_section);
        profileSection.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(android.view.Gravity.START);
            }
        });

        // Setup card click listeners
        setupCardClickListeners();
        
        // Start card animations
        animateCards();
    }

    private void setupCardClickListeners() {
        LinearLayout cardAlphabet = findViewById(R.id.card_alphabet);
        LinearLayout cardNumbers = findViewById(R.id.card_numbers);
        LinearLayout cardGames = findViewById(R.id.card_games);
        LinearLayout cardColors = findViewById(R.id.card_colors);
        LinearLayout cardShapes = findViewById(R.id.card_shapes);
        LinearLayout cardAnimals = findViewById(R.id.card_animals);

        View.OnClickListener cardClickListener = v -> {
            // Scale animation on click
            animateCardPress(v);
            
            String cardName = "";
            int id = v.getId();
            if (id == R.id.card_alphabet) {
                cardName = "Alphabet";
            } else if (id == R.id.card_numbers) {
                cardName = "Numbers";
            } else if (id == R.id.card_games) {
                // Navigate to GamesActivity
                Intent intent = new Intent(MainActivity.this, GamesActivity.class);
                startActivity(intent);
                return;
            } else if (id == R.id.card_colors) {
                // Navigate to Color Alchemy Lab
                Intent intent = new Intent(MainActivity.this, ColorAlchemyActivity.class);
                startActivity(intent);
                return;
            } else if (id == R.id.card_shapes) {
                cardName = "Shapes";
            } else if (id == R.id.card_animals) {
                cardName = "Animals";
            }
            
            Toast.makeText(this, "Let's learn " + cardName + "! 🎉", Toast.LENGTH_SHORT).show();
        };

        cardAlphabet.setOnClickListener(cardClickListener);
        cardNumbers.setOnClickListener(cardClickListener);
        cardGames.setOnClickListener(cardClickListener);
        cardColors.setOnClickListener(cardClickListener);
        cardShapes.setOnClickListener(cardClickListener);
        cardAnimals.setOnClickListener(cardClickListener);
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
            R.id.card_alphabet, R.id.card_numbers, R.id.card_games,
            R.id.card_colors, R.id.card_shapes, R.id.card_animals
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
                alphaAnim.setStartDelay(i * 100);
                translateAnim.setStartDelay(i * 100);
                alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                translateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                
                alphaAnim.start();
                translateAnim.start();
            }
        }
    }
}