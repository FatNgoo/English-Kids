package com.edu.english.shapes.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.edu.english.R;
import com.edu.english.shapes.data.ShapeDataProvider;
import com.edu.english.shapes.models.Shape;
import com.edu.english.shapes.ShapeTTSManager;
import com.edu.english.shapes.views.ShapeCharacterView;

/**
 * Scene 2: Shape Character Intro
 * A cute animated shape character appears and introduces itself
 */
public class Scene2CharacterFragment extends Fragment {

    private static final String ARG_SHAPE_INDEX = "shape_index";

    private int shapeIndex;
    private Shape shape;
    private ShapeCharacterView characterView;
    private TextView tvHello;
    private TextView tvIntro;
    private Button btnContinue;
    private View speechBubble;

    private ShapeTTSManager ttsManager;
    private Handler handler;
    private boolean hasPlayedIntro = false;

    public static Scene2CharacterFragment newInstance(int shapeIndex) {
        Scene2CharacterFragment fragment = new Scene2CharacterFragment();
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
        return inflater.inflate(R.layout.fragment_scene2_character, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ttsManager = ShapeTTSManager.getInstance(requireContext());

        initViews(view);
        setupCharacter();
    }

    private void initViews(View view) {
        characterView = view.findViewById(R.id.characterView);
        tvHello = view.findViewById(R.id.tvHello);
        tvIntro = view.findViewById(R.id.tvIntro);
        btnContinue = view.findViewById(R.id.btnContinue);
        speechBubble = view.findViewById(R.id.speechBubble);

        // Initially hide elements
        speechBubble.setVisibility(View.INVISIBLE);
        btnContinue.setVisibility(View.GONE);

        // Setup click listeners
        characterView.setOnClickListener(v -> {
            characterView.playWaveAnimation();
            playIntroVoice();
        });

        btnContinue.setOnClickListener(v -> {
            goToNextScene();
        });
    }

    private void setupCharacter() {
        if (shape != null) {
            characterView.setShapeType(shape.getType());
            tvIntro.setText("I'm a " + shape.getName() + "!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hasPlayedIntro) {
            playEntranceSequence();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // Reset when user navigates back to this scene
        if (isVisibleToUser && hasPlayedIntro) {
            // User came back - allow replay on tap
        }
    }

    private void playEntranceSequence() {
        hasPlayedIntro = true;

        // Character entrance animation
        characterView.playEntranceAnimation();

        // Show speech bubble after character appears
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            
            showSpeechBubble();
            
            // Delay TTS a bit more to ensure it's initialized
            handler.postDelayed(() -> playFullIntro(), 500);
        }, 800);
    }

    private void showSpeechBubble() {
        speechBubble.setVisibility(View.VISIBLE);
        speechBubble.setScaleX(0f);
        speechBubble.setScaleY(0f);
        speechBubble.setAlpha(0f);

        speechBubble.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();
    }

    private void playFullIntro() {
        if (!isAdded()) return;
        
        // Show text first
        tvHello.setText("Hi! 👋");
        tvIntro.setText("I'm a " + shape.getName() + "!");
        tvIntro.setVisibility(View.VISIBLE);
        
        // Character wave
        characterView.playWaveAnimation();
        
        // Animate texts
        tvHello.setAlpha(0f);
        tvHello.animate().alpha(1f).setDuration(300).start();
        
        tvIntro.setScaleX(0.8f);
        tvIntro.setScaleY(0.8f);
        tvIntro.setAlpha(0f);
        tvIntro.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setStartDelay(200)
                .start();
        
        // Stop any previous TTS and speak "Hi! I'm a Square!" cleanly
        String fullIntro = "Hi! I'm a " + shape.getName() + "!";
        ttsManager.stopAndSpeak(fullIntro, new ShapeTTSManager.OnSpeechCompleteListener() {
            @Override
            public void onSpeechComplete() {
                if (!isAdded()) return;
                
                // Character jumps
                characterView.playJumpAnimation();
                
                // Auto-advance to Scene 3 after animation
                handler.postDelayed(() -> {
                    if (isAdded()) {
                        goToNextScene();
                    }
                }, 1000);
            }
            
            @Override
            public void onSpeechError(String error) {
                if (!isAdded()) return;
                
                // Still advance on error
                handler.postDelayed(() -> {
                    if (isAdded()) {
                        goToNextScene();
                    }
                }, 1500);
            }
        });
    }

    private void playHelloVoice() {
        ttsManager.speak("Hi!", new ShapeTTSManager.OnSpeechCompleteListener() {
            @Override
            public void onSpeechComplete() {
                if (!isAdded()) return;
                
                // After "Hi!", show the intro
                handler.postDelayed(() -> {
                    if (!isAdded()) return;
                    
                    tvHello.setText("Hi! 👋");
                    characterView.playWaveAnimation();
                    
                    // Then play shape name
                    handler.postDelayed(() -> playIntroVoice(), 500);
                }, 300);
            }
            
            @Override
            public void onSpeechError(String error) {
                if (!isAdded()) return;
                handler.postDelayed(() -> playIntroVoice(), 500);
            }
        });
    }

    private void playIntroVoice() {
        // Show intro text
        tvHello.setText("Hi! 👋");
        tvIntro.setText("I'm a " + shape.getName() + "!");
        tvIntro.setVisibility(View.VISIBLE);

        // Animate intro text
        tvIntro.setScaleX(0.8f);
        tvIntro.setScaleY(0.8f);
        tvIntro.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();

        // Play voice using TTS
        ttsManager.speakShapeIntroduction(shape.getType(), new ShapeTTSManager.OnSpeechCompleteListener() {
            @Override
            public void onSpeechComplete() {
                if (!isAdded()) return;
                
                // Character jumps after intro
                characterView.playJumpAnimation();

                // Auto-advance to Scene 3 after animation
                handler.postDelayed(() -> {
                    if (isAdded()) {
                        goToNextScene();
                    }
                }, 1000);
            }
            
            @Override
            public void onSpeechError(String error) {
                if (!isAdded()) return;
                
                // Auto-advance even on error
                handler.postDelayed(() -> {
                    if (isAdded()) {
                        goToNextScene();
                    }
                }, 1000);
            }
        });
    }

    private void showContinueButton() {
        if (!isAdded()) return;
        
        btnContinue.setVisibility(View.VISIBLE);
        btnContinue.setAlpha(0f);
        btnContinue.setTranslationY(50f);
        
        btnContinue.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .start();
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
        characterView.stopAnimations();
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
