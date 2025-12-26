package com.edu.english.alphabet_adventure.screens;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;
import com.edu.english.alphabet_adventure.data.GameData;
import com.edu.english.alphabet_adventure.models.Mascot;
import com.edu.english.alphabet_adventure.services.GamePreferences;

import java.util.List;

/**
 * Screen for selecting a mascot before starting the game.
 */
public class MascotSelectActivity extends AppCompatActivity {

    private GamePreferences preferences;
    private int selectedMascotId = -1;
    private CardView selectedCard = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mascot_select);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preferences = new GamePreferences(this);
        
        setupBackButton();
        setupMascotCards();
        setupStartButton();
        animateCards();
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupMascotCards() {
        List<Mascot> mascots = GameData.getMascots();
        int[] cardIds = {R.id.card_mascot_1, R.id.card_mascot_2, R.id.card_mascot_3, R.id.card_mascot_4};
        int[] emojiIds = {R.id.emoji_mascot_1, R.id.emoji_mascot_2, R.id.emoji_mascot_3, R.id.emoji_mascot_4};
        int[] nameIds = {R.id.name_mascot_1, R.id.name_mascot_2, R.id.name_mascot_3, R.id.name_mascot_4};

        // Pre-select saved mascot
        int savedMascotId = preferences.getSelectedMascot();

        for (int i = 0; i < mascots.size() && i < cardIds.length; i++) {
            Mascot mascot = mascots.get(i);
            CardView card = findViewById(cardIds[i]);
            TextView emojiView = findViewById(emojiIds[i]);
            TextView nameView = findViewById(nameIds[i]);

            emojiView.setText(mascot.getEmoji());
            nameView.setText(mascot.getName());

            final int mascotId = mascot.getId();
            
            // Pre-select if it's the saved mascot
            if (mascotId == savedMascotId) {
                selectCard(card, mascotId);
            }

            card.setOnClickListener(v -> {
                animateCardPress(card);
                selectCard(card, mascotId);
            });
        }
    }

    private void selectCard(CardView card, int mascotId) {
        // Deselect previous
        if (selectedCard != null) {
            selectedCard.setCardElevation(4f);
            selectedCard.setScaleX(1f);
            selectedCard.setScaleY(1f);
            View checkmark = getCheckmarkForCard(selectedCard);
            if (checkmark != null) checkmark.setVisibility(View.GONE);
        }

        // Select new
        selectedCard = card;
        selectedMascotId = mascotId;
        
        card.setCardElevation(16f);
        card.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(200)
            .setInterpolator(new OvershootInterpolator())
            .start();

        View checkmark = getCheckmarkForCard(card);
        if (checkmark != null) {
            checkmark.setVisibility(View.VISIBLE);
            checkmark.setAlpha(0f);
            checkmark.animate().alpha(1f).setDuration(200).start();
        }

        // Enable start button
        findViewById(R.id.btn_start).setEnabled(true);
        findViewById(R.id.btn_start).setAlpha(1f);
    }
    
    private View getCheckmarkForCard(CardView card) {
        int cardId = card.getId();
        if (cardId == R.id.card_mascot_1) return findViewById(R.id.checkmark_1);
        if (cardId == R.id.card_mascot_2) return findViewById(R.id.checkmark_2);
        if (cardId == R.id.card_mascot_3) return findViewById(R.id.checkmark_3);
        if (cardId == R.id.card_mascot_4) return findViewById(R.id.checkmark_4);
        return null;
    }

    private void setupStartButton() {
        LinearLayout startButton = findViewById(R.id.btn_start);
        startButton.setEnabled(false);
        startButton.setAlpha(0.5f);

        startButton.setOnClickListener(v -> {
            if (selectedMascotId > 0) {
                preferences.setSelectedMascot(selectedMascotId);
                
                Intent intent = new Intent(this, AlphabetGameActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void animateCardPress(View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1.05f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1.05f);
        scaleUpX.setDuration(150);
        scaleUpY.setDuration(150);
        scaleUpX.setStartDelay(100);
        scaleUpY.setStartDelay(100);

        scaleDownX.start();
        scaleDownY.start();
        scaleUpX.start();
        scaleUpY.start();
    }

    private void animateCards() {
        int[] cardIds = {R.id.card_mascot_1, R.id.card_mascot_2, R.id.card_mascot_3, R.id.card_mascot_4};

        for (int i = 0; i < cardIds.length; i++) {
            View card = findViewById(cardIds[i]);
            if (card != null) {
                card.setAlpha(0f);
                card.setTranslationY(100f);

                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);
                ObjectAnimator translateAnim = ObjectAnimator.ofFloat(card, "translationY", 100f, 0f);

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
