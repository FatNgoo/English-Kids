package com.edu.english.masterchef.ui.scene1;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.DialogLine;
import com.edu.english.masterchef.data.model.Food;
import com.edu.english.masterchef.data.model.LevelConfig;
import com.edu.english.masterchef.ui.game.GameViewModel;
import com.edu.english.masterchef.ui.game.MasterChefGameActivity;
import com.edu.english.masterchef.util.AnimationHelper;
import com.edu.english.masterchef.util.SoundPoolManager;
import com.edu.english.masterchef.util.TTSManager;

/**
 * Scene 1: Restaurant Ordering
 * Kid-friendly restaurant scene with speech bubbles, food preview,
 * and interactive dialog with TTS karaoke
 */
public class Scene1Fragment extends Fragment {

    private Scene1ViewModel viewModel;
    private GameViewModel gameViewModel;
    private TTSManager ttsManager;
    private SoundPoolManager soundManager;

    // Characters
    private ImageView ivChef, ivCustomer, btnSpeaker;
    private ImageView ivSpeakerAvatar;
    
    // Speech bubbles
    private CardView bubbleChef, bubbleCustomer;
    private TextView tvChefSpeech, tvCustomerSpeech;
    
    // Food preview
    private CardView cardMenu;
    private ImageView ivFoodPreview;
    private TextView tvFoodName;
    
    // Dialog panel
    private CardView cardDialog;
    private TextView tvSpeaker, tvDialog, tvTranslation;
    private View btnContinue, btnSkip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scene1_ordering_v3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        viewModel = new ViewModelProvider(this).get(Scene1ViewModel.class);
        gameViewModel = ((MasterChefGameActivity) requireActivity()).getGameViewModel();

        // Initialize managers
        ttsManager = TTSManager.getInstance(requireContext());
        soundManager = SoundPoolManager.getInstance(requireContext());

        // Find views
        findViews(view);
        setupListeners();
        observeViewModel();

        // Initialize scene with current level
        LevelConfig level = gameViewModel.getCurrentLevel().getValue();
        if (level != null) {
            setupFoodPreview(level.getFood());
            viewModel.initializeScene(level.getOrderingScene());
        }
    }

    private void findViews(View view) {
        // Characters
        ivChef = view.findViewById(R.id.iv_chef);
        ivCustomer = view.findViewById(R.id.iv_customer);
        
        // Speech bubbles
        bubbleChef = view.findViewById(R.id.bubble_chef);
        bubbleCustomer = view.findViewById(R.id.bubble_customer);
        tvChefSpeech = view.findViewById(R.id.tv_chef_speech);
        tvCustomerSpeech = view.findViewById(R.id.tv_customer_speech);
        
        // Food preview
        cardMenu = view.findViewById(R.id.card_menu);
        ivFoodPreview = view.findViewById(R.id.iv_food_preview);
        tvFoodName = view.findViewById(R.id.tv_food_name);
        
        // Dialog panel
        cardDialog = view.findViewById(R.id.card_dialog);
        tvSpeaker = view.findViewById(R.id.tv_speaker);
        tvDialog = view.findViewById(R.id.tv_dialog);
        tvTranslation = view.findViewById(R.id.tv_translation);
        btnContinue = view.findViewById(R.id.btn_continue);
        btnSpeaker = view.findViewById(R.id.btn_speaker);
        ivSpeakerAvatar = view.findViewById(R.id.iv_speaker_avatar);
        btnSkip = view.findViewById(R.id.btn_skip);
    }

    /**
     * Setup food preview card
     */
    private void setupFoodPreview(Food food) {
        if (food == null || cardMenu == null) return;
        
        if (tvFoodName != null) {
            tvFoodName.setText(food.getName());
        }
        // Food preview animation
        cardMenu.setScaleX(0f);
        cardMenu.setScaleY(0f);
        cardMenu.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(300)
            .setInterpolator(new android.view.animation.OvershootInterpolator(2f))
            .start();
    }

    private void setupListeners() {
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                soundManager.play(SoundPoolManager.SOUND_CLICK);
                if (viewModel.hasMoreDialogs()) {
                    viewModel.nextDialog();
                } else {
                    completeScene();
                }
            });
        }

        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> {
                soundManager.play(SoundPoolManager.SOUND_CLICK);
                ttsManager.stop();
                completeScene();
            });
        }

        if (btnSpeaker != null) {
            btnSpeaker.setOnClickListener(v -> {
                soundManager.play(SoundPoolManager.SOUND_CLICK);
                AnimationHelper.bounce(btnSpeaker);
                DialogLine dialog = viewModel.getCurrentDialog().getValue();
                if (dialog != null) {
                    speakCurrentDialog(dialog);
                }
            });
        }
    }

    private void observeViewModel() {
        // Observe current dialog
        viewModel.getCurrentDialog().observe(getViewLifecycleOwner(), this::displayDialog);

        // Observe completion
        viewModel.isComplete().observe(getViewLifecycleOwner(), isComplete -> {
            if (isComplete) {
                completeScene();
            }
        });
        
        // Initial character entrance animations
        View rootView = getView();
        if (rootView != null) {
            rootView.post(() -> {
                animateCharacterEntrance();
            });
        }
    }

    /**
     * Display a dialog line with speech bubble effects
     */
    private void displayDialog(DialogLine dialog) {
        if (dialog == null) return;

        // Hide all speech bubbles first
        hideSpeechBubbles();

        // Update speaker info
        boolean isChef = dialog.getSpeaker().equalsIgnoreCase("npc") || 
                         dialog.getSpeaker().equalsIgnoreCase("chef");
        
        if (isChef) {
            if (tvSpeaker != null) {
                tvSpeaker.setText("👨‍🍳 Chef says:");
                tvSpeaker.setTextColor(Color.parseColor("#FF6B35"));
            }
            if (ivSpeakerAvatar != null) {
                ivSpeakerAvatar.setImageResource(R.drawable.ic_chef_avatar);
            }
            showChefBubble(dialog.getText());
        } else {
            if (tvSpeaker != null) {
                tvSpeaker.setText("👧 You say:");
                tvSpeaker.setTextColor(Color.parseColor("#1565C0"));
            }
            if (ivSpeakerAvatar != null) {
                ivSpeakerAvatar.setImageResource(R.drawable.ic_kid_character);
            }
            showCustomerBubble(dialog.getText());
        }

        // Update dialog text
        if (tvDialog != null) tvDialog.setText(dialog.getText());
        if (tvTranslation != null) tvTranslation.setText("(" + dialog.getTranslation() + ")");

        // Animate dialog card
        if (cardDialog != null) {
            cardDialog.setAlpha(0f);
            cardDialog.setTranslationY(100f);
            cardDialog.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
        }

        // Highlight active character
        highlightSpeaker(dialog.getSpeaker());

        // Auto-speak with karaoke
        speakCurrentDialog(dialog);

        // Hide continue button until speech done
        if (btnContinue != null) btnContinue.setVisibility(View.INVISIBLE);
        ttsManager.setOnTTSListener(new TTSManager.OnTTSListener() {
            @Override
            public void onTTSInitialized(boolean success) {}

            @Override
            public void onSpeakStart(String utteranceId) {}

            @Override
            public void onSpeakDone(String utteranceId) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (btnContinue != null) {
                            btnContinue.setVisibility(View.VISIBLE);
                            AnimationHelper.bounce(btnContinue);
                        }
                        soundManager.play(SoundPoolManager.SOUND_SUCCESS);
                    });
                }
            }

            @Override
            public void onSpeakError(String utteranceId) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (btnContinue != null) btnContinue.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    /**
     * Show chef speech bubble with animation
     */
    private void showChefBubble(String text) {
        if (bubbleChef == null || tvChefSpeech == null) return;
        
        tvChefSpeech.setText(text);
        bubbleChef.setVisibility(View.VISIBLE);
        bubbleChef.setScaleX(0f);
        bubbleChef.setScaleY(0f);
        bubbleChef.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.5f))
            .start();
    }

    /**
     * Show customer speech bubble with animation
     */
    private void showCustomerBubble(String text) {
        if (bubbleCustomer == null || tvCustomerSpeech == null) return;
        
        tvCustomerSpeech.setText(text);
        bubbleCustomer.setVisibility(View.VISIBLE);
        bubbleCustomer.setScaleX(0f);
        bubbleCustomer.setScaleY(0f);
        bubbleCustomer.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.5f))
            .start();
    }

    /**
     * Hide all speech bubbles
     */
    private void hideSpeechBubbles() {
        if (bubbleChef != null) {
            bubbleChef.setVisibility(View.GONE);
        }
        if (bubbleCustomer != null) {
            bubbleCustomer.setVisibility(View.GONE);
        }
    }

    /**
     * Highlight the current speaker with animation
     */
    private void highlightSpeaker(String speaker) {
        if (ivChef == null || ivCustomer == null) return;
        
        boolean isChef = speaker.equalsIgnoreCase("npc") || 
                         speaker.equalsIgnoreCase("chef");
        
        if (!isChef) {
            // Customer/Player speaking
            AnimationHelper.bounce(ivCustomer);
            ivCustomer.setAlpha(1.0f);
            ivChef.setAlpha(0.6f);
            ivCustomer.setScaleX(1.1f);
            ivCustomer.setScaleY(1.1f);
            ivChef.setScaleX(1.0f);
            ivChef.setScaleY(1.0f);
        } else {
            // Chef/NPC speaking
            AnimationHelper.bounce(ivChef);
            ivChef.setAlpha(1.0f);
            ivCustomer.setAlpha(0.6f);
            ivChef.setScaleX(1.1f);
            ivChef.setScaleY(1.1f);
            ivCustomer.setScaleX(1.0f);
            ivCustomer.setScaleY(1.0f);
        }
    }

    /**
     * Speak current dialog with karaoke highlighting
     * Double read: slow → normal for better learning
     */
    private void speakCurrentDialog(DialogLine dialog) {
        if (dialog == null || tvDialog == null) return;

        // Enable double read for learning
        ttsManager.speakWithKaraoke(
            dialog.getText(),
            tvDialog,
            Color.parseColor("#FF6B35"), // Highlight color (orange)
            Color.parseColor("#333333"), // Normal color (dark gray)
            true                         // Double read: slow → normal
        );
    }

    /**
     * Complete this scene and move to shopping (Scene 2)
     */
    private void completeScene() {
        ttsManager.stop();
        
        // Success animation before transition
        soundManager.play(SoundPoolManager.SOUND_SUCCESS);
        
        // Animate characters waving goodbye
        if (ivChef != null) {
            ivChef.animate()
                .rotationBy(15f)
                .setDuration(200)
                .withEndAction(() -> {
                    ivChef.animate().rotationBy(-15f).setDuration(200).start();
                })
                .start();
        }
        
        // Transition after short delay
        View viewToPost = cardDialog != null ? cardDialog : getView();
        if (viewToPost != null) {
            viewToPost.postDelayed(() -> {
                gameViewModel.completeOrderingScene();
            }, 500);
        } else {
            gameViewModel.completeOrderingScene();
        }
    }

    /**
     * Show hint for current dialog
     */
    public void showHint() {
        DialogLine dialog = viewModel.getCurrentDialog().getValue();
        if (dialog != null && btnSpeaker != null) {
            // Pulse the speaker button
            AnimationHelper.pulse(btnSpeaker);
            
            // Automatically play the audio
            speakCurrentDialog(dialog);
        }
    }

    /**
     * Animate character entrance with walk-in effect
     */
    private void animateCharacterEntrance() {
        if (ivChef == null || ivCustomer == null) return;
        
        // Chef walks in from left
        ivChef.setTranslationX(-500f);
        ivChef.setAlpha(0f);
        ivChef.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .start();

        // Customer walks in from right
        ivCustomer.setTranslationX(500f);
        ivCustomer.setAlpha(0f);
        ivCustomer.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(200)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .withEndAction(() -> {
                // NPC bow gesture after entrance
                animateNPCBow();
            })
            .start();
    }

    /**
     * Animate NPC bow gesture
     */
    private void animateNPCBow() {
        if (ivCustomer == null) return;
        
        ivCustomer.animate()
            .rotationBy(15f)
            .setDuration(300)
            .withEndAction(() -> {
                ivCustomer.animate()
                    .rotationBy(-15f)
                    .setDuration(300)
                    .start();
            })
            .start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ttsManager.stop();
    }
}
