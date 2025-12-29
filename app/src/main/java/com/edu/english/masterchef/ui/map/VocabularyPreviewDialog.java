package com.edu.english.masterchef.ui.map;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.Ingredient;
import com.edu.english.masterchef.util.TTSManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Dialog showing vocabulary preview before starting level
 * Kids can hear pronunciation of each ingredient
 */
public class VocabularyPreviewDialog extends Dialog {

    private List<Ingredient> ingredients;
    private TTSManager ttsManager;
    private OnStartListener onStartListener;

    public interface OnStartListener {
        void onStart();
    }

    public VocabularyPreviewDialog(@NonNull Context context, List<Ingredient> ingredients) {
        super(context);
        this.ingredients = ingredients;
        this.ttsManager = TTSManager.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_vocabulary_preview);

        // Get views
        TextView titleText = findViewById(R.id.title_text);
        LinearLayout vocabularyContainer = findViewById(R.id.vocabulary_container);
        MaterialButton btnListenAll = findViewById(R.id.btn_listen_all);
        MaterialButton btnStart = findViewById(R.id.btn_start);
        MaterialButton btnCancel = findViewById(R.id.btn_cancel);

        titleText.setText("Vocabulary for this level");

        // Add each ingredient as a clickable item
        for (Ingredient ingredient : ingredients) {
            View vocabView = getLayoutInflater().inflate(R.layout.item_vocabulary_preview, vocabularyContainer, false);

            TextView vocabName = vocabView.findViewById(R.id.vocab_name);
            View speakerButton = vocabView.findViewById(R.id.speaker_button);

            vocabName.setText(ingredient.getName());

            // Click to hear pronunciation
            vocabView.setOnClickListener(v -> {
                ttsManager.speak(ingredient.getName(), "vocab_" + ingredient.getIngredientId());
                speakerButton.setScaleX(1.2f);
                speakerButton.setScaleY(1.2f);
                speakerButton.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200)
                        .start();
            });

            vocabularyContainer.addView(vocabView);
        }

        // Listen all button - read all ingredients in sequence
        btnListenAll.setOnClickListener(v -> {
            readAllIngredients();
        });

        // Start button
        btnStart.setOnClickListener(v -> {
            if (onStartListener != null) {
                onStartListener.onStart();
            }
            dismiss();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void readAllIngredients() {
        StringBuilder allText = new StringBuilder("The ingredients are: ");
        for (int i = 0; i < ingredients.size(); i++) {
            allText.append(ingredients.get(i).getName());
            if (i < ingredients.size() - 1) {
                allText.append(", ");
            }
        }
        ttsManager.speak(allText.toString(), "all_ingredients");
    }

    public void setOnStartListener(OnStartListener listener) {
        this.onStartListener = listener;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ttsManager != null) {
            ttsManager.stop();
        }
    }
}
