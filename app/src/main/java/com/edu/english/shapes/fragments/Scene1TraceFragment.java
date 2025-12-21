package com.edu.english.shapes.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.edu.english.R;
import com.edu.english.shapes.data.ShapeDataProvider;
import com.edu.english.shapes.models.Shape;
import com.edu.english.shapes.ShapeTTSManager;
import com.edu.english.shapes.views.ShapeTraceView;

/**
 * Scene 1: Trace the Shape
 * Kids trace the shape with their finger on a glowing dotted path
 */
public class Scene1TraceFragment extends Fragment implements ShapeTraceView.OnTraceCompleteListener {

    private static final String ARG_SHAPE_INDEX = "shape_index";

    private int shapeIndex;
    private Shape shape;
    private ShapeTraceView traceView;
    private TextView tvInstruction;
    private TextView tvProgress;
    private Button btnContinue;
    private Button btnRetry;
    private View successOverlay;

    private ShapeTTSManager ttsManager;
    private Handler handler;

    public static Scene1TraceFragment newInstance(int shapeIndex) {
        Scene1TraceFragment fragment = new Scene1TraceFragment();
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
        handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scene1_trace, container, false);
    }

    private boolean hasPlayedInstruction = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ttsManager = ShapeTTSManager.getInstance(requireContext());

        initViews(view);
        setupTraceView();
        showInstruction();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hasPlayedInstruction) {
            hasPlayedInstruction = true;
            // Stop any previous TTS and play instruction
            handler.postDelayed(() -> {
                if (isAdded() && ttsManager != null) {
                    ttsManager.stopAndSpeak("Trace the " + shape.getName() + " with your finger!", null);
                }
            }, 800);
        }
    }

    private void initViews(View view) {
        traceView = view.findViewById(R.id.traceView);
        tvInstruction = view.findViewById(R.id.tvInstruction);
        tvProgress = view.findViewById(R.id.tvProgress);
        btnContinue = view.findViewById(R.id.btnContinue);
        btnRetry = view.findViewById(R.id.btnRetry);
        successOverlay = view.findViewById(R.id.successOverlay);

        btnContinue.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        successOverlay.setVisibility(View.GONE);

        btnContinue.setOnClickListener(v -> {
            goToNextScene();
        });

        btnRetry.setOnClickListener(v -> {
            resetTrace();
        });
    }

    private void setupTraceView() {
        if (shape != null) {
            traceView.setShapeType(shape.getType());
            traceView.setOnTraceCompleteListener(this);
        }
    }

    private void showInstruction() {
        tvInstruction.setText("Trace the " + shape.getName() + "!");
        tvProgress.setText("Follow the dotted line ✨");

        // Animate instruction
        ObjectAnimator pulse = ObjectAnimator.ofFloat(tvInstruction, "scaleX", 1f, 1.05f, 1f);
        pulse.setDuration(1000);
        pulse.setRepeatCount(2);
        pulse.start();
    }

    @Override
    public void onCheckpointReached(int checkpoint) {
        tvProgress.setText("Great! " + checkpoint + "/4 ⭐");

        // Animate progress text
        ObjectAnimator bounce = ObjectAnimator.ofFloat(tvProgress, "scaleY", 1f, 1.2f, 1f);
        bounce.setDuration(300);
        bounce.start();

        if (checkpoint == 3) {
            // Special moment
            tvProgress.setText("Oh wow! Keep going! 🌟");
        }
    }

    @Override
    public void onTraceComplete() {
        showSuccessState();
    }

    private void showSuccessState() {
        tvInstruction.setText("Amazing! 🎉");
        tvProgress.setText("You traced the " + shape.getName() + "!");

        // Show success overlay with animation
        successOverlay.setVisibility(View.VISIBLE);
        successOverlay.setAlpha(0f);
        successOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        // Play shape pronunciation using TTS
        ttsManager.speakShapeName(shape.getType(), new ShapeTTSManager.OnSpeechCompleteListener() {
            @Override
            public void onSpeechComplete() {
                if (!isAdded()) return;
                
                // Auto-advance to Scene 2 after a short delay
                handler.postDelayed(() -> {
                    if (isAdded()) {
                        goToNextScene();
                    }
                }, 800);
            }

            @Override
            public void onSpeechError(String error) {
                if (!isAdded()) return;
                
                // Still advance even if TTS fails
                handler.postDelayed(() -> {
                    if (isAdded()) {
                        goToNextScene();
                    }
                }, 1000);
            }
        });
    }

    private void animateButtonEntrance(View button, long delay) {
        button.setScaleX(0f);
        button.setScaleY(0f);
        button.setAlpha(0f);

        AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 0f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);

        animSet.playTogether(scaleX, scaleY, alpha);
        animSet.setDuration(400);
        animSet.setStartDelay(delay);
        animSet.setInterpolator(new OvershootInterpolator());
        animSet.start();
    }

    private void resetTrace() {
        traceView.reset();
        successOverlay.setVisibility(View.GONE);
        btnContinue.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        showInstruction();
    }

    private void goToNextScene() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ShapeLearningFragment) {
            ((ShapeLearningFragment) parentFragment).goToNextScene();
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
