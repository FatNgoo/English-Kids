package com.edu.english.shapes.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.edu.english.R;
import com.edu.english.shapes.ShapesActivity;
import com.edu.english.shapes.data.ShapeDataProvider;
import com.edu.english.shapes.models.Shape;
import com.edu.english.shapes.models.ShapeObject;
import com.edu.english.shapes.ShapeTTSManager;

import java.util.List;

/**
 * Scene 4: Summary / Review
 * Auto-review loop showing all 6 objects with highlighting and audio
 */
public class Scene4SummaryFragment extends Fragment {

    private static final String ARG_SHAPE_INDEX = "shape_index";

    private int shapeIndex;
    private Shape shape;
    private List<ShapeObject> objects;

    private TextView tvTitle;
    private TextView tvSubtitle;
    private CardView[] objectCards;
    private ImageView[] objectImages;
    private TextView[] objectNames;
    private View[] cardBorders;

    private Button btnReplay;
    private Button btnNextShape;
    private View celebrationOverlay;

    private ShapeTTSManager ttsManager;
    private Handler handler;
    private int currentReviewIndex = 0;
    private boolean isReviewing = false;
    private ValueAnimator borderAnimator;

    public static Scene4SummaryFragment newInstance(int shapeIndex) {
        Scene4SummaryFragment fragment = new Scene4SummaryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SHAPE_INDEX, shapeIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shapeIndex = getArguments().getInt(ARG_SHAPE_INDEX, 0);
        }
        shape = ShapeDataProvider.getInstance().getShapeByIndex(shapeIndex);
        if (shape != null) {
            objects = shape.getObjects();
        }
        handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scene4_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ttsManager = ShapeTTSManager.getInstance(requireContext());

        initViews(view);
        setupObjectCards();
        startReviewSequence();
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        btnReplay = view.findViewById(R.id.btnReplay);
        btnNextShape = view.findViewById(R.id.btnNextShape);
        celebrationOverlay = view.findViewById(R.id.celebrationOverlay);

        objectCards = new CardView[6];
        objectImages = new ImageView[6];
        objectNames = new TextView[6];
        cardBorders = new View[6];

        objectCards[0] = view.findViewById(R.id.summaryCard1);
        objectCards[1] = view.findViewById(R.id.summaryCard2);
        objectCards[2] = view.findViewById(R.id.summaryCard3);
        objectCards[3] = view.findViewById(R.id.summaryCard4);
        objectCards[4] = view.findViewById(R.id.summaryCard5);
        objectCards[5] = view.findViewById(R.id.summaryCard6);

        objectImages[0] = view.findViewById(R.id.summaryImg1);
        objectImages[1] = view.findViewById(R.id.summaryImg2);
        objectImages[2] = view.findViewById(R.id.summaryImg3);
        objectImages[3] = view.findViewById(R.id.summaryImg4);
        objectImages[4] = view.findViewById(R.id.summaryImg5);
        objectImages[5] = view.findViewById(R.id.summaryImg6);

        objectNames[0] = view.findViewById(R.id.summaryName1);
        objectNames[1] = view.findViewById(R.id.summaryName2);
        objectNames[2] = view.findViewById(R.id.summaryName3);
        objectNames[3] = view.findViewById(R.id.summaryName4);
        objectNames[4] = view.findViewById(R.id.summaryName5);
        objectNames[5] = view.findViewById(R.id.summaryName6);

        cardBorders[0] = view.findViewById(R.id.border1);
        cardBorders[1] = view.findViewById(R.id.border2);
        cardBorders[2] = view.findViewById(R.id.border3);
        cardBorders[3] = view.findViewById(R.id.border4);
        cardBorders[4] = view.findViewById(R.id.border5);
        cardBorders[5] = view.findViewById(R.id.border6);

        if (shape != null) {
            tvTitle.setText(shape.getName() + " Review ⭐");
            tvSubtitle.setText("Tap any object to hear it again!");
        }

        btnReplay.setVisibility(View.GONE);
        btnNextShape.setVisibility(View.GONE);
        celebrationOverlay.setVisibility(View.GONE);

        btnReplay.setOnClickListener(v -> {
            restartReview();
        });

        btnNextShape.setOnClickListener(v -> {
            goToNextShape();
        });
    }

    private void setupObjectCards() {
        if (objects == null || objects.isEmpty()) return;

        for (int i = 0; i < objectCards.length; i++) {
            if (i < objects.size()) {
                final int index = i;
                ShapeObject obj = objects.get(i);

                objectImages[i].setImageResource(obj.getImageResId());
                objectNames[i].setText(obj.getName());
                cardBorders[i].setVisibility(View.INVISIBLE);

                // Setup click listener
                objectCards[i].setOnClickListener(v -> {
                    if (!isReviewing) {
                        playObjectReview(index);
                    }
                });
            } else {
                objectCards[i].setVisibility(View.GONE);
            }
        }
    }

    private void startReviewSequence() {
        isReviewing = true;
        currentReviewIndex = 0;

        // Stop any previous TTS before starting review
        if (ttsManager != null) {
            ttsManager.stop();
        }

        // Show all cards first with staggered entrance
        for (int i = 0; i < Math.min(objects.size(), objectCards.length); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                animateCardEntrance(index);
            }, i * 100L);
        }

        // Start auto review after all cards are shown
        handler.postDelayed(this::reviewNextObject, objects.size() * 100L + 500);
    }

    private void animateCardEntrance(int index) {
        if (!isAdded() || index >= objectCards.length) return;

        CardView card = objectCards[index];
        card.setAlpha(0f);
        card.setScaleX(0.8f);
        card.setScaleY(0.8f);

        card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void reviewNextObject() {
        if (!isAdded() || currentReviewIndex >= objects.size()) {
            finishReview();
            return;
        }

        playObjectReview(currentReviewIndex);
    }

    private void playObjectReview(int index) {
        if (!isAdded() || index >= objects.size()) return;

        ShapeObject obj = objects.get(index);

        // Highlight with blinking border
        startBorderBlink(index);

        // Highlight card
        animateCardHighlight(index, true);

        // Play object name and shape name using TTS
        ttsManager.speakVocabularyItem(obj.getName(), shape.getType(),
            new ShapeTTSManager.OnSpeechCompleteListener() {
                @Override
                public void onSpeechComplete() {
                    if (!isAdded()) return;

                    // Stop highlight
                    stopBorderBlink(index);
                    animateCardHighlight(index, false);

                    if (isReviewing) {
                        // Move to next
                        currentReviewIndex++;
                        handler.postDelayed(() -> reviewNextObject(), 300);
                    }
                }

                @Override
                public void onSpeechError(String error) {
                    if (!isAdded()) return;
                    stopBorderBlink(index);
                    animateCardHighlight(index, false);

                    if (isReviewing) {
                        currentReviewIndex++;
                        handler.postDelayed(() -> reviewNextObject(), 300);
                    }
                }
            });
    }

    private void startBorderBlink(int index) {
        if (index >= cardBorders.length) return;

        View border = cardBorders[index];
        border.setVisibility(View.VISIBLE);

        borderAnimator = ValueAnimator.ofFloat(0.3f, 1f);
        borderAnimator.setDuration(400);
        borderAnimator.setRepeatCount(ValueAnimator.INFINITE);
        borderAnimator.setRepeatMode(ValueAnimator.REVERSE);
        borderAnimator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            border.setAlpha(alpha);
        });
        borderAnimator.start();
    }

    private void stopBorderBlink(int index) {
        if (index >= cardBorders.length) return;

        if (borderAnimator != null) {
            borderAnimator.cancel();
        }

        View border = cardBorders[index];
        border.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> border.setVisibility(View.INVISIBLE))
                .start();
    }

    private void animateCardHighlight(int index, boolean highlight) {
        if (index >= objectCards.length) return;

        CardView card = objectCards[index];
        float scale = highlight ? 1.1f : 1f;
        float elevation = highlight ? 16f : 4f;

        card.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(200)
                .start();

        card.setCardElevation(elevation);
    }

    private void finishReview() {
        isReviewing = false;

        // Show celebration
        showCelebration();

        // Show buttons
        handler.postDelayed(this::showButtons, 1000);
    }

    private void showCelebration() {
        if (!isAdded()) return;

        celebrationOverlay.setVisibility(View.VISIBLE);
        celebrationOverlay.setAlpha(0f);
        celebrationOverlay.animate()
                .alpha(1f)
                .setDuration(500)
                .start();

        // Speak congratulation
        ttsManager.speakEncouragement(null);

        tvTitle.setText("Amazing! 🎉");
        tvSubtitle.setText("You learned all " + shape.getName() + " objects!");

        // Animate title
        ObjectAnimator bounce = ObjectAnimator.ofFloat(tvTitle, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator bounceY = ObjectAnimator.ofFloat(tvTitle, "scaleY", 1f, 1.2f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(bounce, bounceY);
        set.setDuration(500);
        set.start();
    }

    private void showButtons() {
        if (!isAdded()) return;

        btnReplay.setVisibility(View.VISIBLE);
        btnReplay.setAlpha(0f);
        btnReplay.animate().alpha(1f).setDuration(300).start();

        // Only show next button if there are more shapes
        if (shapeIndex < ShapeDataProvider.getInstance().getShapeCount() - 1) {
            btnNextShape.setVisibility(View.VISIBLE);
            btnNextShape.setAlpha(0f);
            btnNextShape.animate().alpha(1f).setStartDelay(100).setDuration(300).start();
        }
    }

    private void restartReview() {
        celebrationOverlay.setVisibility(View.GONE);
        btnReplay.setVisibility(View.GONE);
        btnNextShape.setVisibility(View.GONE);

        if (shape != null) {
            tvTitle.setText(shape.getName() + " Review ⭐");
            tvSubtitle.setText("Tap any object to hear it again!");
        }

        currentReviewIndex = 0;
        isReviewing = true;
        reviewNextObject();
    }

    private void goToNextShape() {
        if (getActivity() instanceof ShapesActivity) {
            ((ShapesActivity) getActivity()).goToNextShape();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop TTS when leaving this scene
        if (ttsManager != null) {
            ttsManager.stop();
        }
        handler.removeCallbacksAndMessages(null);
        isReviewing = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        if (borderAnimator != null) {
            borderAnimator.cancel();
        }
        isReviewing = false;
    }
}
