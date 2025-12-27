package com.edu.english.storybook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.edu.english.R;
import com.edu.english.storybook.model.StoryCategory;

/**
 * Category selection screen for Magic Storybook.
 * Displays 4 story categories in a 2x2 grid.
 */
public class MagicStorybookCategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storybook_category);

        setupTopBar();
        setupCategoryCards();
    }

    private void setupTopBar() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnLibrary = findViewById(R.id.btn_library);

        btnBack.setOnClickListener(v -> finish());

        btnLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(this, StoryLibraryActivity.class);
            startActivity(intent);
        });
    }

    private void setupCategoryCards() {
        CardView cardNovel = findViewById(R.id.card_novel);
        CardView cardFairyTales = findViewById(R.id.card_fairy_tales);
        CardView cardWorld = findViewById(R.id.card_world);
        CardView cardHistory = findViewById(R.id.card_history);

        cardNovel.setOnClickListener(v -> openSetupSheet(StoryCategory.NOVEL));
        cardFairyTales.setOnClickListener(v -> openSetupSheet(StoryCategory.FAIRY_TALES));
        cardWorld.setOnClickListener(v -> openSetupSheet(StoryCategory.SEE_THE_WORLD));
        cardHistory.setOnClickListener(v -> openSetupSheet(StoryCategory.HISTORY));

        // Add touch animations
        setupCardAnimation(cardNovel);
        setupCardAnimation(cardFairyTales);
        setupCardAnimation(cardWorld);
        setupCardAnimation(cardHistory);
    }

    private void setupCardAnimation(CardView card) {
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    card.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    card.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    private void openSetupSheet(StoryCategory category) {
        StorySetupBottomSheet bottomSheet = StorySetupBottomSheet.newInstance(category);
        bottomSheet.show(getSupportFragmentManager(), "StorySetupBottomSheet");
    }
}
