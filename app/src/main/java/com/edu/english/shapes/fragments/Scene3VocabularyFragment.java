package com.edu.english.shapes.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.edu.english.R;
import com.edu.english.shapes.data.ShapeDataProvider;
import com.edu.english.shapes.models.Shape;
import com.edu.english.shapes.models.ShapeObject;
import com.edu.english.shapes.ShapeTTSManager;

import java.util.List;

/**
 * Scene 3: Vocabulary Flashcards
 * Shows one vocabulary card at a time - synchronized with TTS
 * Flow: Show card → Speak word → Wait → Flip to next
 */
public class Scene3VocabularyFragment extends Fragment {

    private static final String TAG = "Scene3Vocab";
    private static final String ARG_SHAPE_INDEX = "shape_index";

    private int shapeIndex;
    private Shape shape;
    private List<ShapeObject> objects;

    // Views
    private CardView flashcard;
    private ImageView objectImage;
    private TextView objectName;
    private TextView tvTitle;
    private TextView instructionText;
    private View glowView;
    private LinearLayout progressDots;

    private ShapeTTSManager ttsManager;
    private Handler handler;
    private int currentIndex = 0;
    private ValueAnimator glowAnimator;

    public static Scene3VocabularyFragment newInstance(int shapeIndex) {
        Scene3VocabularyFragment fragment = new Scene3VocabularyFragment();
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
        return inflater.inflate(R.layout.fragment_scene3_vocabulary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ttsManager = ShapeTTSManager.getInstance(requireContext());
        initViews(view);
        setupProgressDots();
        
        // Start sequence after short delay
        handler.postDelayed(this::startVocabularySequence, 600);
    }

    private void initViews(View view) {
        flashcard = view.findViewById(R.id.flashcard);
        objectImage = view.findViewById(R.id.objectImage);
        objectName = view.findViewById(R.id.objectName);
        tvTitle = view.findViewById(R.id.shape_name_text);
        instructionText = view.findViewById(R.id.instructionText);
        glowView = view.findViewById(R.id.glowView);
        progressDots = view.findViewById(R.id.progressDots);

        // Initially hide card
        flashcard.setAlpha(0f);
        flashcard.setScaleX(0.8f);
        flashcard.setScaleY(0.8f);

        // Click to replay current word
        flashcard.setOnClickListener(v -> replayCurrentWord());
    }

    private void setupProgressDots() {
        if (objects == null) return;

        progressDots.removeAllViews();
        int dotSize = dpToPx(10);
        int dotMargin = dpToPx(6);

        for (int i = 0; i < objects.size(); i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(dotMargin, 0, dotMargin, 0);
            dot.setLayoutParams(params);
            
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(i == 0 ? Color.WHITE : Color.parseColor("#66FFFFFF"));
            dot.setBackground(drawable);
            
            progressDots.addView(dot);
        }
    }

    private void updateProgressDots() {
        for (int i = 0; i < progressDots.getChildCount(); i++) {
            View dot = progressDots.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) dot.getBackground();
            drawable.setColor(i == currentIndex ? Color.WHITE : Color.parseColor("#66FFFFFF"));
        }
    }

    private void updateTitle() {
        if (shape != null && objects != null) {
            tvTitle.setText(shape.getName().toUpperCase() + " (" + (currentIndex + 1) + "/" + objects.size() + ")");
        }
    }

    // ============ MAIN SEQUENCE ============

    private void startVocabularySequence() {
        if (!isAdded() || objects == null || objects.isEmpty()) {
            goToNextScene();
            return;
        }

        currentIndex = 0;
        showCardAndSpeak();
    }

    /**
     * Step 1: Show card with animation, then speak when animation ends
     */
    private void showCardAndSpeak() {
        if (!isAdded() || currentIndex >= objects.size()) {
            finishAndGoToNextScene();
            return;
        }

        Log.d(TAG, "Showing card " + currentIndex + ": " + objects.get(currentIndex).getName());

        ShapeObject obj = objects.get(currentIndex);

        // Update content FIRST
        objectImage.setImageResource(obj.getImageResId());
        objectName.setText(obj.getName());
        updateTitle();
        updateProgressDots();
        instructionText.setText("🔊 " + obj.getName());

        // Animate card entrance
        flashcard.setTranslationX(250f);
        flashcard.setRotationY(-20f);
        flashcard.setAlpha(0f);
        flashcard.setScaleX(0.85f);
        flashcard.setScaleY(0.85f);

        AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator translateX = ObjectAnimator.ofFloat(flashcard, "translationX", 250f, 0f);
        ObjectAnimator rotateY = ObjectAnimator.ofFloat(flashcard, "rotationY", -20f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(flashcard, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(flashcard, "scaleX", 0.85f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(flashcard, "scaleY", 0.85f, 1f);

        animSet.playTogether(translateX, rotateY, alpha, scaleX, scaleY);
        animSet.setDuration(450);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Card is now visible - wait a moment then speak
                handler.postDelayed(() -> speakWord(obj.getName()), 300);
            }
        });
        animSet.start();
    }

    /**
     * Step 2: Speak the word, when done wait then move to next
     */
    private void speakWord(String word) {
        if (!isAdded()) return;

        Log.d(TAG, "Speaking: " + word);

        // Show glow while speaking
        startGlowAnimation();
        pulseCard();

        ttsManager.stopAndSpeak(word, new ShapeTTSManager.OnSpeechCompleteListener() {
            @Override
            public void onSpeechComplete() {
                Log.d(TAG, "TTS complete for: " + word);
                onSpeakComplete();
            }

            @Override
            public void onSpeechError(String error) {
                Log.w(TAG, "TTS error for: " + word + " - " + error);
                onSpeakComplete();
            }
        });

        // Fallback timeout in case TTS callback never fires
        handler.postDelayed(() -> {
            if (isAdded() && glowAnimator != null && glowAnimator.isRunning()) {
                Log.w(TAG, "TTS timeout fallback triggered");
                onSpeakComplete();
            }
        }, 2000);
    }

    /**
     * Step 3: After speaking, wait then flip or finish
     */
    private void onSpeakComplete() {
        if (!isAdded()) return;

        stopGlowAnimation();
        instructionText.setText("Tap to hear again");

        // Wait for child to absorb, then proceed
        handler.postDelayed(() -> {
            if (!isAdded()) return;

            currentIndex++;
            if (currentIndex >= objects.size()) {
                finishAndGoToNextScene();
            } else {
                flipToNextCard();
            }
        }, 1500); // 1.5 second pause after word
    }

    /**
     * Step 4: Flip animation to next card
     */
    private void flipToNextCard() {
        if (!isAdded()) return;

        Log.d(TAG, "Flipping to card " + currentIndex);

        ShapeObject nextObj = objects.get(currentIndex);

        // Flip out animation (card leaves to left)
        AnimatorSet outSet = new AnimatorSet();
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(flashcard, "rotationY", 0f, 90f);
        ObjectAnimator translateOut = ObjectAnimator.ofFloat(flashcard, "translationX", 0f, -80f);
        ObjectAnimator scaleOut = ObjectAnimator.ofFloat(flashcard, "scaleX", 1f, 0.9f);
        
        outSet.playTogether(flipOut, translateOut, scaleOut);
        outSet.setDuration(250);
        outSet.setInterpolator(new AccelerateInterpolator());
        
        outSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Change content at midpoint (card is edge-on, not visible)
                objectImage.setImageResource(nextObj.getImageResId());
                objectName.setText(nextObj.getName());
                updateTitle();
                updateProgressDots();
                instructionText.setText("🔊 " + nextObj.getName());

                // Flip in animation (card enters from right)
                flashcard.setRotationY(-90f);
                flashcard.setTranslationX(80f);

                AnimatorSet inSet = new AnimatorSet();
                ObjectAnimator flipIn = ObjectAnimator.ofFloat(flashcard, "rotationY", -90f, 0f);
                ObjectAnimator translateIn = ObjectAnimator.ofFloat(flashcard, "translationX", 80f, 0f);
                ObjectAnimator scaleIn = ObjectAnimator.ofFloat(flashcard, "scaleX", 0.9f, 1f);
                
                inSet.playTogether(flipIn, translateIn, scaleIn);
                inSet.setDuration(250);
                inSet.setInterpolator(new DecelerateInterpolator());
                
                inSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Card is now visible - wait a moment then speak
                        handler.postDelayed(() -> speakWord(nextObj.getName()), 300);
                    }
                });
                inSet.start();
            }
        });
        outSet.start();
    }

    /**
     * Step 5: All words done, show message and go to next scene
     */
    private void finishAndGoToNextScene() {
        if (!isAdded()) return;

        Log.d(TAG, "All words complete, going to Scene 4");

        instructionText.setText("Great job! 🎉");

        // Animate card exit
        handler.postDelayed(() -> {
            if (!isAdded()) return;

            AnimatorSet exitSet = new AnimatorSet();
            ObjectAnimator translateX = ObjectAnimator.ofFloat(flashcard, "translationX", 0f, -300f);
            ObjectAnimator rotateY = ObjectAnimator.ofFloat(flashcard, "rotationY", 0f, 30f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(flashcard, "alpha", 1f, 0f);
            ObjectAnimator scale = ObjectAnimator.ofFloat(flashcard, "scaleX", 1f, 0.8f);

            exitSet.playTogether(translateX, rotateY, alpha, scale);
            exitSet.setDuration(350);
            exitSet.setInterpolator(new AccelerateInterpolator());
            exitSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    goToNextScene();
                }
            });
            exitSet.start();
        }, 2500); // Wait 2.5 seconds showing "Great job!" before exit
    }

    // ============ HELPER METHODS ============

    private void replayCurrentWord() {
        if (!isAdded() || objects == null || currentIndex >= objects.size()) return;
        
        // Only replay, don't advance
        String word = objects.get(Math.min(currentIndex, objects.size() - 1)).getName();
        startGlowAnimation();
        pulseCard();
        
        ttsManager.stopAndSpeak(word, new ShapeTTSManager.OnSpeechCompleteListener() {
            @Override
            public void onSpeechComplete() {
                stopGlowAnimation();
            }

            @Override
            public void onSpeechError(String error) {
                stopGlowAnimation();
            }
        });
    }

    private void pulseCard() {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(flashcard, "scaleX", 1f, 1.03f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(flashcard, "scaleY", 1f, 1.03f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(flashcard, "scaleX", 1.03f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(flashcard, "scaleY", 1.03f, 1f);

        AnimatorSet pulseUp = new AnimatorSet();
        pulseUp.playTogether(scaleUpX, scaleUpY);
        pulseUp.setDuration(120);

        AnimatorSet pulseDown = new AnimatorSet();
        pulseDown.playTogether(scaleDownX, scaleDownY);
        pulseDown.setDuration(120);

        AnimatorSet pulse = new AnimatorSet();
        pulse.playSequentially(pulseUp, pulseDown);
        pulse.start();
    }

    private void startGlowAnimation() {
        if (glowView == null) return;
        
        glowView.setVisibility(View.VISIBLE);
        glowView.setAlpha(0f);

        if (glowAnimator != null) {
            glowAnimator.cancel();
        }

        glowAnimator = ValueAnimator.ofFloat(0.3f, 0.7f);
        glowAnimator.setDuration(350);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.addUpdateListener(anim -> {
            if (glowView != null) {
                glowView.setAlpha((float) anim.getAnimatedValue());
            }
        });
        glowAnimator.start();
    }

    private void stopGlowAnimation() {
        if (glowAnimator != null) {
            glowAnimator.cancel();
            glowAnimator = null;
        }
        if (glowView != null) {
            glowView.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> glowView.setVisibility(View.INVISIBLE))
                    .start();
        }
    }

    private void goToNextScene() {
        if (!isAdded()) return;
        
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ShapeLearningFragment) {
            ((ShapeLearningFragment) parentFragment).goToNextScene();
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ttsManager != null) {
            ttsManager.stop();
        }
        handler.removeCallbacksAndMessages(null);
        stopGlowAnimation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        if (glowAnimator != null) {
            glowAnimator.cancel();
        }
    }
}
