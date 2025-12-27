package com.edu.english.numbers;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;

import java.util.Locale;

/**
 * Number Learning Activity
 * Displays numbers 1-20 with English pronunciation
 */
public class NumberLearnActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String[] NUMBER_WORDS = {
            "one", "two", "three", "four", "five",
            "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen",
            "sixteen", "seventeen", "eighteen", "nineteen", "twenty"
    };

    private static final int[] CARD_COLORS = {
            0xFFFF6B6B, 0xFFFF8E53, 0xFFFFD93D, 0xFF6BCB77, 0xFF4D96FF,
            0xFFAE81FF, 0xFFFF6B9D, 0xFF00D9FF, 0xFFFF6584, 0xFF6C63FF,
            0xFFFF7F50, 0xFF20B2AA, 0xFFDA70D6, 0xFF32CD32, 0xFF1E90FF,
            0xFFFF4500, 0xFF9370DB, 0xFF3CB371, 0xFFFF69B4, 0xFF6A5ACD
    };

    private TextToSpeech textToSpeech;
    private int currentNumber = 1;

    private TextView txtBigNumber;
    private TextView txtEnglishWord;
    private TextView txtProgress;
    private LinearLayout btnSpeak;
    private View mainCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_number_learn);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize TTS
        textToSpeech = new TextToSpeech(this, this);

        setupViews();
        setupNumbersGrid();
        updateDisplay();
    }

    private void setupViews() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Main display
        txtBigNumber = findViewById(R.id.txt_big_number);
        txtEnglishWord = findViewById(R.id.txt_english_word);
        txtProgress = findViewById(R.id.txt_progress);
        btnSpeak = findViewById(R.id.btn_speak);

        // Find the card's parent LinearLayout
        mainCard = ((View) txtBigNumber.getParent());

        // Navigation
        ImageButton btnPrev = findViewById(R.id.btn_prev);
        ImageButton btnNext = findViewById(R.id.btn_next);

        btnPrev.setOnClickListener(v -> {
            if (currentNumber > 1) {
                currentNumber--;
                updateDisplay();
                animateCard();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentNumber < 20) {
                currentNumber++;
                updateDisplay();
                animateCard();
            }
        });

        // Speak button
        btnSpeak.setOnClickListener(v -> speakNumber(currentNumber));
    }

    private void setupNumbersGrid() {
        GridLayout grid = findViewById(R.id.numbers_grid);

        for (int i = 1; i <= 20; i++) {
            final int number = i;

            TextView numberBtn = new TextView(this);
            numberBtn.setText(String.valueOf(i));
            numberBtn.setTextSize(18);
            numberBtn.setTextColor(getResources().getColor(R.color.white, null));
            numberBtn.setTypeface(null, android.graphics.Typeface.BOLD);
            numberBtn.setGravity(Gravity.CENTER);
            numberBtn.setBackgroundResource(R.drawable.bg_number_grid_item);

            // Calculate size based on screen
            int size = (int) (56 * getResources().getDisplayMetrics().density);
            int margin = (int) (4 * getResources().getDisplayMetrics().density);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            numberBtn.setLayoutParams(params);

            // Set background color tint
            numberBtn.getBackground().setTint(CARD_COLORS[(i - 1) % CARD_COLORS.length]);

            numberBtn.setOnClickListener(v -> {
                currentNumber = number;
                updateDisplay();
                animateCard();
                speakNumber(number);
            });

            grid.addView(numberBtn);
        }
    }

    private void updateDisplay() {
        txtBigNumber.setText(String.valueOf(currentNumber));
        txtEnglishWord.setText(NUMBER_WORDS[currentNumber - 1].toUpperCase());
        txtProgress.setText(currentNumber + " / 20");

        // Update card color
        mainCard.getBackground().setTint(CARD_COLORS[(currentNumber - 1) % CARD_COLORS.length]);
    }

    private void animateCard() {
        // Scale bounce animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mainCard, "scaleX", 0.9f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mainCard, "scaleY", 0.9f, 1.1f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    private void speakNumber(int number) {
        if (textToSpeech != null) {
            String word = NUMBER_WORDS[number - 1];
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "number_" + number);

            // Animate speak button
            ObjectAnimator pulse = ObjectAnimator.ofFloat(btnSpeak, "scaleX", 1f, 1.2f, 1f);
            ObjectAnimator pulseY = ObjectAnimator.ofFloat(btnSpeak, "scaleY", 1f, 1.2f, 1f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(pulse, pulseY);
            set.setDuration(200);
            set.start();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "English TTS not supported", Toast.LENGTH_SHORT).show();
            } else {
                // Set speech rate for clearer pronunciation
                textToSpeech.setSpeechRate(0.8f);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
