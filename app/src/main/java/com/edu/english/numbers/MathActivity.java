package com.edu.english.numbers;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Fun Math Activity
 * Simple addition and subtraction for kids
 */
public class MathActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int TOTAL_QUESTIONS = 10;
    private static final String[] EMOJIS = {"🍎", "🌟", "🎈", "🍪", "🍬", "🐱", "🐶", "🦋"};

    private enum Operation { ADD, SUBTRACT, MIX }
    private Operation currentOperation = Operation.ADD;

    private TextToSpeech textToSpeech;
    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());

    private int score = 0;
    private int currentQuestion = 1;
    private int num1, num2, correctAnswer;
    private String currentEmoji;

    // Views
    private TextView txtScore, txtNum1, txtNum2, txtOperator, txtVisualHelper;
    private TextView txtFeedback, txtProgress;
    private TextView tabAdd, tabSubtract, tabMix;
    private GridLayout answersGrid;
    private LinearLayout feedbackContainer, btnNextQuestion;
    private CardView cardQuestion;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_math);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textToSpeech = new TextToSpeech(this, this);
        currentEmoji = EMOJIS[random.nextInt(EMOJIS.length)];

        setupViews();
        generateQuestion();
    }

    private void setupViews() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Score
        txtScore = findViewById(R.id.txt_score);

        // Question display
        txtNum1 = findViewById(R.id.txt_num1);
        txtNum2 = findViewById(R.id.txt_num2);
        txtOperator = findViewById(R.id.txt_operator);
        txtVisualHelper = findViewById(R.id.txt_visual_helper);
        cardQuestion = findViewById(R.id.card_question);

        // Answers
        answersGrid = findViewById(R.id.answers_grid);

        // Feedback
        feedbackContainer = findViewById(R.id.feedback_container);
        txtFeedback = findViewById(R.id.txt_feedback);
        btnNextQuestion = findViewById(R.id.btn_next_question);
        btnNextQuestion.setOnClickListener(v -> nextQuestion());

        // Progress
        txtProgress = findViewById(R.id.txt_progress);
        progressBar = findViewById(R.id.progress_bar);

        // Operation tabs
        tabAdd = findViewById(R.id.tab_add);
        tabSubtract = findViewById(R.id.tab_subtract);
        tabMix = findViewById(R.id.tab_mix);

        tabAdd.setOnClickListener(v -> selectOperation(Operation.ADD));
        tabSubtract.setOnClickListener(v -> selectOperation(Operation.SUBTRACT));
        tabMix.setOnClickListener(v -> selectOperation(Operation.MIX));
    }

    private void selectOperation(Operation operation) {
        currentOperation = operation;

        // Update tab styles
        tabAdd.setBackgroundResource(operation == Operation.ADD ? R.drawable.bg_tab_selector : 0);
        tabSubtract.setBackgroundResource(operation == Operation.SUBTRACT ? R.drawable.bg_tab_selector : 0);
        tabMix.setBackgroundResource(operation == Operation.MIX ? R.drawable.bg_tab_selector : 0);

        // Reset and start new game
        score = 0;
        currentQuestion = 1;
        txtScore.setText("0");
        currentEmoji = EMOJIS[random.nextInt(EMOJIS.length)];
        generateQuestion();
    }

    private void generateQuestion() {
        feedbackContainer.setVisibility(View.GONE);

        // Determine operation
        boolean isAddition;
        if (currentOperation == Operation.MIX) {
            isAddition = random.nextBoolean();
        } else {
            isAddition = (currentOperation == Operation.ADD);
        }

        // Generate numbers (1-10 range for kids)
        if (isAddition) {
            num1 = random.nextInt(9) + 1; // 1-9
            num2 = random.nextInt(10 - num1) + 1; // ensure sum <= 10 for beginners
            correctAnswer = num1 + num2;
            txtOperator.setText("+");
        } else {
            num1 = random.nextInt(9) + 2; // 2-10
            num2 = random.nextInt(num1 - 1) + 1; // ensure positive result
            correctAnswer = num1 - num2;
            txtOperator.setText("−");
        }

        txtNum1.setText(String.valueOf(num1));
        txtNum2.setText(String.valueOf(num2));

        // Visual helper
        StringBuilder visual = new StringBuilder();
        for (int i = 0; i < num1; i++) visual.append(currentEmoji);
        visual.append(isAddition ? " + " : " − ");
        for (int i = 0; i < num2; i++) visual.append(currentEmoji);
        txtVisualHelper.setText(visual.toString());

        // Update progress
        txtProgress.setText("Question " + currentQuestion + " of " + TOTAL_QUESTIONS);
        progressBar.setProgress(currentQuestion);

        // Generate answer options
        generateAnswerOptions();

        // Animate card
        animateCard();
    }

    private void generateAnswerOptions() {
        answersGrid.removeAllViews();

        // Create list of answers
        List<Integer> answers = new ArrayList<>();
        answers.add(correctAnswer);

        // Add wrong answers
        while (answers.size() < 4) {
            int wrongAnswer = correctAnswer + random.nextInt(5) - 2; // ±2 range
            if (wrongAnswer >= 0 && wrongAnswer <= 20 && !answers.contains(wrongAnswer)) {
                answers.add(wrongAnswer);
            }
        }

        // Shuffle answers
        Collections.shuffle(answers);

        // Create buttons
        int[] colors = {0xFF6C63FF, 0xFFFF6584, 0xFF4CAF50, 0xFFFF9800};
        int size = (int) (80 * getResources().getDisplayMetrics().density);
        int margin = (int) (8 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < 4; i++) {
            final int answer = answers.get(i);

            TextView btn = new TextView(this);
            btn.setText(String.valueOf(answer));
            btn.setTextSize(28);
            btn.setTextColor(getResources().getColor(R.color.white, null));
            btn.setTypeface(null, android.graphics.Typeface.BOLD);
            btn.setGravity(Gravity.CENTER);
            btn.setBackgroundResource(R.drawable.bg_answer_button);
            btn.getBackground().setTint(colors[i]);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = size;
            params.columnSpec = GridLayout.spec(i % 2, 1f);
            params.rowSpec = GridLayout.spec(i / 2);
            params.setMargins(margin, margin, margin, margin);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> checkAnswer(answer, btn));
            answersGrid.addView(btn);
        }
    }

    private void checkAnswer(int answer, View button) {
        // Disable all buttons
        for (int i = 0; i < answersGrid.getChildCount(); i++) {
            answersGrid.getChildAt(i).setEnabled(false);
        }

        if (answer == correctAnswer) {
            // Correct!
            score += 10;
            txtScore.setText(String.valueOf(score));
            txtFeedback.setText("🎉 Correct!");
            txtFeedback.setTextColor(getResources().getColor(R.color.card_green, null));

            // Animate correct button
            button.getBackground().setTint(0xFF4CAF50);
            animateCorrect(button);

            // Speak
            if (textToSpeech != null) {
                textToSpeech.speak("Correct! " + correctAnswer, TextToSpeech.QUEUE_FLUSH, null, "correct");
            }
        } else {
            // Wrong
            txtFeedback.setText("❌ The answer is " + correctAnswer);
            txtFeedback.setTextColor(getResources().getColor(R.color.card_pink, null));

            // Shake wrong button
            button.getBackground().setTint(0xFFFF5252);
            animateWrong(button);

            // Highlight correct answer
            for (int i = 0; i < answersGrid.getChildCount(); i++) {
                TextView child = (TextView) answersGrid.getChildAt(i);
                if (Integer.parseInt(child.getText().toString()) == correctAnswer) {
                    child.getBackground().setTint(0xFF4CAF50);
                }
            }

            // Speak
            if (textToSpeech != null) {
                textToSpeech.speak("The answer is " + correctAnswer, TextToSpeech.QUEUE_FLUSH, null, "wrong");
            }
        }

        feedbackContainer.setVisibility(View.VISIBLE);
    }

    private void nextQuestion() {
        currentQuestion++;
        currentEmoji = EMOJIS[random.nextInt(EMOJIS.length)];

        if (currentQuestion > TOTAL_QUESTIONS) {
            // Game over
            showGameOver();
        } else {
            generateQuestion();
        }
    }

    private void showGameOver() {
        feedbackContainer.setVisibility(View.VISIBLE);

        String message;
        if (score >= 80) {
            message = "🏆 Amazing! You scored " + score + " points!";
        } else if (score >= 50) {
            message = "⭐ Great job! You scored " + score + " points!";
        } else {
            message = "👍 Good try! You scored " + score + " points!";
        }

        txtFeedback.setText(message);
        txtFeedback.setTextColor(getResources().getColor(R.color.badge_gold, null));

        // Change button text to "Play Again"
        TextView nextText = (TextView) btnNextQuestion.getChildAt(0);
        nextText.setText("Play Again 🔄");

        btnNextQuestion.setOnClickListener(v -> {
            score = 0;
            currentQuestion = 1;
            txtScore.setText("0");
            nextText.setText("Next →");
            btnNextQuestion.setOnClickListener(v2 -> nextQuestion());
            generateQuestion();
        });

        if (textToSpeech != null) {
            textToSpeech.speak("Great job! You scored " + score + " points!", TextToSpeech.QUEUE_FLUSH, null, "gameover");
        }
    }

    private void animateCard() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardQuestion, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardQuestion, "scaleY", 0.9f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardQuestion, "alpha", 0.5f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(300);
        set.start();
    }

    private void animateCorrect(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.start();
    }

    private void animateWrong(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, 15, -15, 10, -10, 5, -5, 0);
        shake.setDuration(400);
        shake.start();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
            textToSpeech.setSpeechRate(0.9f);
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
