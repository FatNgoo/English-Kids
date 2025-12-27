package com.edu.english.storybook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edu.english.R;
import com.edu.english.storybook.model.QuizQuestion;
import com.edu.english.storybook.model.Story;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.List;

/**
 * Bottom sheet for the story quiz.
 */
public class QuizBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STORY_JSON = "story_json";

    private Story story;
    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean hasAnswered = false;

    private TextView tvProgress;
    private TextView tvScore;
    private LinearProgressIndicator progressBar;
    private TextView tvQuestion;
    private RadioGroup radioGroupChoices;
    private MaterialRadioButton choiceA, choiceB, choiceC, choiceD;
    private TextView tvFeedback;
    private MaterialButton btnSubmit;
    private LinearLayout completeState;
    private TextView tvResultTitle;
    private TextView tvFinalScore;
    private MaterialButton btnClose;

    public static QuizBottomSheet newInstance(String storyJson) {
        QuizBottomSheet fragment = new QuizBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_STORY_JSON, storyJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            story = Story.fromJson(getArguments().getString(ARG_STORY_JSON));
            if (story != null) {
                questions = story.getQuizQuestions();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (story == null || questions == null || questions.isEmpty()) {
            dismiss();
            return;
        }

        bindViews(view);
        setupListeners();
        displayQuestion(0);
    }

    private void bindViews(View view) {
        tvProgress = view.findViewById(R.id.tv_progress);
        tvScore = view.findViewById(R.id.tv_score);
        progressBar = view.findViewById(R.id.progress_bar);
        tvQuestion = view.findViewById(R.id.tv_question);
        radioGroupChoices = view.findViewById(R.id.radio_group_choices);
        choiceA = view.findViewById(R.id.choice_a);
        choiceB = view.findViewById(R.id.choice_b);
        choiceC = view.findViewById(R.id.choice_c);
        choiceD = view.findViewById(R.id.choice_d);
        tvFeedback = view.findViewById(R.id.tv_feedback);
        btnSubmit = view.findViewById(R.id.btn_submit);
        completeState = view.findViewById(R.id.complete_state);
        tvResultTitle = view.findViewById(R.id.tv_result_title);
        tvFinalScore = view.findViewById(R.id.tv_final_score);
        btnClose = view.findViewById(R.id.btn_close);
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> {
            if (hasAnswered) {
                nextQuestion();
            } else {
                submitAnswer();
            }
        });

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void displayQuestion(int index) {
        if (index >= questions.size()) {
            showResults();
            return;
        }

        currentQuestionIndex = index;
        QuizQuestion question = questions.get(index);
        hasAnswered = false;

        // Update progress
        tvProgress.setText("Question " + (index + 1) + " of " + questions.size());
        tvScore.setText("Score: " + score + "/" + questions.size());
        progressBar.setProgress((index * 100) / questions.size());

        // Set question and choices
        tvQuestion.setText(question.getQuestion());
        
        List<String> choices = question.getChoices();
        choiceA.setText(choices.size() > 0 ? choices.get(0) : "");
        choiceB.setText(choices.size() > 1 ? choices.get(1) : "");
        choiceC.setText(choices.size() > 2 ? choices.get(2) : "");
        choiceD.setText(choices.size() > 3 ? choices.get(3) : "");

        // Reset state
        radioGroupChoices.clearCheck();
        tvFeedback.setVisibility(View.GONE);
        btnSubmit.setText("Submit Answer");
        enableChoices(true);
    }

    private void submitAnswer() {
        int selectedId = radioGroupChoices.getCheckedRadioButtonId();
        if (selectedId == -1) {
            tvFeedback.setText("Please select an answer");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            tvFeedback.setVisibility(View.VISIBLE);
            return;
        }

        hasAnswered = true;
        enableChoices(false);

        QuizQuestion question = questions.get(currentQuestionIndex);
        int selectedIndex = getSelectedIndex(selectedId);
        boolean isCorrect = selectedIndex == question.getCorrectIndex();

        if (isCorrect) {
            score++;
            tvFeedback.setText("✓ Correct!");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            String correctAnswer = question.getChoices().get(question.getCorrectIndex());
            tvFeedback.setText("✗ The correct answer is: " + correctAnswer);
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        tvFeedback.setVisibility(View.VISIBLE);
        tvScore.setText("Score: " + score + "/" + questions.size());
        btnSubmit.setText(currentQuestionIndex < questions.size() - 1 ? "Next Question" : "See Results");
    }

    private int getSelectedIndex(int selectedId) {
        if (selectedId == R.id.choice_a) return 0;
        if (selectedId == R.id.choice_b) return 1;
        if (selectedId == R.id.choice_c) return 2;
        if (selectedId == R.id.choice_d) return 3;
        return -1;
    }

    private void enableChoices(boolean enabled) {
        choiceA.setEnabled(enabled);
        choiceB.setEnabled(enabled);
        choiceC.setEnabled(enabled);
        choiceD.setEnabled(enabled);
    }

    private void nextQuestion() {
        displayQuestion(currentQuestionIndex + 1);
    }

    private void showResults() {
        // Hide question UI
        tvProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        tvQuestion.setVisibility(View.GONE);
        radioGroupChoices.setVisibility(View.GONE);
        tvFeedback.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);

        // Show results
        completeState.setVisibility(View.VISIBLE);

        double percentage = (double) score / questions.size() * 100;
        if (percentage >= 80) {
            tvResultTitle.setText("Amazing! 🌟");
            tvResultTitle.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (percentage >= 60) {
            tvResultTitle.setText("Good Job! 👍");
            tvResultTitle.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            tvResultTitle.setText("Keep Learning! 📚");
            tvResultTitle.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }

        tvFinalScore.setText("You scored " + score + " out of " + questions.size() + "!");
    }
}
