package com.edu.english.numbers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;

/**
 * Numbers Selection Activity
 * Allows user to choose between:
 * 1. Number - Learn numbers in English
 * 2. Math - Fun math games
 * 3. Speech - Practice speaking (existing NumbersLessonActivity)
 */
public class NumbersSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_numbers_select);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupViews();
    }

    private void setupViews() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Card 1: Learn Numbers
        CardView cardLearnNumbers = findViewById(R.id.card_learn_numbers);
        cardLearnNumbers.setOnClickListener(v -> {
            Intent intent = new Intent(this, NumberLearnActivity.class);
            startActivity(intent);
        });

        // Card 2: Math
        CardView cardMath = findViewById(R.id.card_math);
        cardMath.setOnClickListener(v -> {
            Intent intent = new Intent(this, MathActivity.class);
            startActivity(intent);
        });

        // Card 3: Speech Practice (existing functionality)
        CardView cardSpeech = findViewById(R.id.card_speech);
        cardSpeech.setOnClickListener(v -> {
            Intent intent = new Intent(this, NumbersLessonActivity.class);
            startActivity(intent);
        });
    }
}
