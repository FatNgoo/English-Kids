package com.edu.english.masterchef.ui.scene2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.Ingredient;
import com.edu.english.masterchef.data.model.LevelConfig;
import com.edu.english.masterchef.data.model.SceneScript;
import com.edu.english.masterchef.data.repository.LevelRepository;
import com.edu.english.masterchef.ui.game.GameViewModel;
import com.edu.english.masterchef.ui.game.MasterChefGameActivity;
import com.edu.english.masterchef.util.AnimationHelper;
import com.edu.english.masterchef.util.SoundPoolManager;
import com.edu.english.masterchef.util.TTSManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Scene 2: Supermarket Shopping
 * Kid-friendly supermarket scene where chef asks for ingredients
 * and child selects the correct ones from the shelves
 */
public class Scene2Fragment extends Fragment {

    private Scene2ViewModel viewModel;
    private GameViewModel gameViewModel;
    private SoundPoolManager soundManager;
    private TTSManager ttsManager;
    private Handler handler = new Handler(Looper.getMainLooper());

    // Chef request panel
    private CardView cardChefRequest;
    private TextView tvIngredientRequest, tvIngredientVietnamese;
    private ImageView btnSpeaker;

    // Progress
    private ProgressBar progressIngredients;
    private TextView tvProgressText;

    // Ingredients grid
    private RecyclerView rvIngredients;
    private IngredientAdapter adapter;

    // Cart section
    private TextView tvCartCount, tvCartBadge;
    private ImageView ivCartIcon;
    private View btnClear, btnCheckout;

    // Feedback overlay
    private View overlayFeedback;
    private TextView tvFeedbackEmoji, tvFeedbackText;

    // Current ingredient being requested
    private int currentRequestIndex = 0;
    private List<String> requiredIngredientIds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scene2_shopping, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        viewModel = new ViewModelProvider(this).get(Scene2ViewModel.class);
        gameViewModel = ((MasterChefGameActivity) requireActivity()).getGameViewModel();

        // Initialize managers
        soundManager = SoundPoolManager.getInstance(requireContext());
        ttsManager = TTSManager.getInstance(requireContext());

        // Find views
        findViews(view);
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        // Initialize scene with current level's shopping data
        LevelConfig level = gameViewModel.getCurrentLevel().getValue();
        if (level != null) {
            SceneScript shoppingScene = level.getShoppingScene();
            requiredIngredientIds = shoppingScene.getRequiredIngredientIds();
            
            List<Ingredient> allIngredients = loadIngredients(
                requiredIngredientIds,
                shoppingScene.getDistractorIngredientIds()
            );
            viewModel.initializeScene(allIngredients, requiredIngredientIds);
            
            // Show first ingredient request
            showNextIngredientRequest();
        }
    }

    private void findViews(View view) {
        // Chef request panel
        cardChefRequest = view.findViewById(R.id.card_chef_request);
        tvIngredientRequest = view.findViewById(R.id.tv_ingredient_request);
        tvIngredientVietnamese = view.findViewById(R.id.tv_ingredient_vietnamese);
        btnSpeaker = view.findViewById(R.id.btn_speaker);

        // Progress
        progressIngredients = view.findViewById(R.id.progress_ingredients);
        tvProgressText = view.findViewById(R.id.tv_progress_text);

        // Grid
        rvIngredients = view.findViewById(R.id.rv_ingredients);

        // Cart
        tvCartCount = view.findViewById(R.id.tv_cart_count);
        tvCartBadge = view.findViewById(R.id.tv_cart_badge);
        ivCartIcon = view.findViewById(R.id.iv_cart_icon);
        btnClear = view.findViewById(R.id.btn_clear);
        btnCheckout = view.findViewById(R.id.btn_checkout);

        // Feedback
        overlayFeedback = view.findViewById(R.id.overlay_feedback);
        tvFeedbackEmoji = view.findViewById(R.id.tv_feedback_emoji);
        tvFeedbackText = view.findViewById(R.id.tv_feedback_text);
    }

    private void setupRecyclerView() {
        rvIngredients.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        adapter = new IngredientAdapter();
        adapter.setOnItemClickListener(this::onIngredientClicked);
        rvIngredients.setAdapter(adapter);
        
        // Set difficulty from level config
        LevelConfig level = gameViewModel.getCurrentLevel().getValue();
        if (level != null && level.getDifficulty() != null) {
            adapter.setDifficulty(level.getDifficulty());
        }
    }

    private void setupListeners() {
        btnClear.setOnClickListener(v -> {
            soundManager.play(SoundPoolManager.SOUND_CLICK);
            viewModel.clearCart();
            currentRequestIndex = 0;
            showNextIngredientRequest();
        });

        btnCheckout.setOnClickListener(v -> {
            if (viewModel.checkoutCart()) {
                soundManager.play(SoundPoolManager.SOUND_SUCCESS);
                showFeedback(true, "Great job! All ingredients collected! 🎉");
                handler.postDelayed(() -> {
                    gameViewModel.completeShoppingScene(true);
                }, 1500);
            } else {
                soundManager.play(SoundPoolManager.SOUND_WRONG);
                AnimationHelper.shake(btnCheckout);
                showFeedback(false, "Oops! Check your cart again!");
            }
        });

        btnSpeaker.setOnClickListener(v -> {
            soundManager.play(SoundPoolManager.SOUND_CLICK);
            AnimationHelper.bounce(btnSpeaker);
            speakCurrentRequest();
        });
    }

    /**
     * Handle ingredient click
     */
    private void onIngredientClicked(Ingredient ingredient) {
        soundManager.play(SoundPoolManager.SOUND_CLICK);
        
        String ingredientId = ingredient.getIngredientId();
        boolean isRequired = requiredIngredientIds.contains(ingredientId);
        List<String> selected = viewModel.getSelectedIngredientIds().getValue();
        boolean alreadySelected = selected != null && selected.contains(ingredientId);

        if (alreadySelected) {
            // Deselect
            viewModel.toggleIngredient(ingredientId);
            return;
        }

        // Check if correct ingredient
        if (isRequired) {
            // Correct!
            viewModel.toggleIngredient(ingredientId);
            soundManager.play(SoundPoolManager.SOUND_SUCCESS);
            showFeedback(true, "Correct! " + ingredient.getName() + " ✓");
            
            // Move to next ingredient request
            handler.postDelayed(() -> {
                hideFeedback();
                currentRequestIndex++;
                showNextIngredientRequest();
            }, 1000);
        } else {
            // Wrong ingredient
            soundManager.play(SoundPoolManager.SOUND_WRONG);
            showFeedback(false, "Oops! That's " + ingredient.getName() + ".\nTry again!");
            
            // Animate the wrong item
            View itemView = findViewForIngredient(ingredientId);
            if (itemView != null) {
                AnimationHelper.shake(itemView);
            }
            
            handler.postDelayed(this::hideFeedback, 1500);
        }
    }

    /**
     * Find view for ingredient in RecyclerView
     */
    private View findViewForIngredient(String ingredientId) {
        List<Ingredient> ingredients = viewModel.getAllIngredients().getValue();
        if (ingredients == null) return null;
        
        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i).getIngredientId().equals(ingredientId)) {
                return rvIngredients.getLayoutManager().findViewByPosition(i);
            }
        }
        return null;
    }

    /**
     * Show next ingredient request from chef
     */
    private void showNextIngredientRequest() {
        if (requiredIngredientIds == null || currentRequestIndex >= requiredIngredientIds.size()) {
            // All ingredients requested - show complete message
            tvIngredientRequest.setText("All ingredients found! Tap 'Go Cook!' 🎉");
            tvIngredientVietnamese.setText("(Đã tìm đủ nguyên liệu! Nhấn 'Go Cook!')");
            return;
        }

        String ingredientId = requiredIngredientIds.get(currentRequestIndex);
        Ingredient ingredient = LevelRepository.getInstance().getIngredient(ingredientId);
        
        if (ingredient != null) {
            String request = "Please find the " + ingredient.getName() + "!";
            String vietnamese = "(Hãy tìm " + ingredient.getNameVietnamese() + "!)";
            
            tvIngredientRequest.setText(request);
            tvIngredientVietnamese.setText(vietnamese);
            
            // Animate the request card
            AnimationHelper.bounce(cardChefRequest);
            
            // Auto-speak
            ttsManager.speak("Please find the " + ingredient.getName(), "ingredient_request");
        }
    }

    /**
     * Speak current ingredient request
     */
    private void speakCurrentRequest() {
        if (requiredIngredientIds == null || currentRequestIndex >= requiredIngredientIds.size()) {
            ttsManager.speak("All ingredients found! Let's go cook!", "complete_message");
            return;
        }

        String ingredientId = requiredIngredientIds.get(currentRequestIndex);
        Ingredient ingredient = LevelRepository.getInstance().getIngredient(ingredientId);
        
        if (ingredient != null) {
            ttsManager.speak("Please find the " + ingredient.getName(), "ingredient_request");
        }
    }

    /**
     * Show feedback overlay
     */
    private void showFeedback(boolean isCorrect, String message) {
        if (overlayFeedback == null) return;
        
        tvFeedbackEmoji.setText(isCorrect ? "✅" : "❌");
        tvFeedbackText.setText(message);
        tvFeedbackText.setTextColor(isCorrect ? 
            getResources().getColor(android.R.color.holo_green_dark) : 
            getResources().getColor(android.R.color.holo_red_dark));
        
        overlayFeedback.setVisibility(View.VISIBLE);
        overlayFeedback.setAlpha(0f);
        overlayFeedback.animate()
            .alpha(1f)
            .setDuration(200)
            .start();
    }

    /**
     * Hide feedback overlay
     */
    private void hideFeedback() {
        if (overlayFeedback == null) return;
        
        overlayFeedback.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction(() -> overlayFeedback.setVisibility(View.GONE))
            .start();
    }

    private void observeViewModel() {
        // Observe ingredients list
        viewModel.getAvailableIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            adapter.setIngredients(ingredients);
        });

        // Observe selected ingredients
        viewModel.getSelectedIngredients().observe(getViewLifecycleOwner(), selected -> {
            adapter.setSelectedIds(selected);
            int count = selected != null ? selected.size() : 0;
            int total = requiredIngredientIds != null ? requiredIngredientIds.size() : 0;
            
            tvCartCount.setText(count + " / " + total + " ingredients collected");
            
            // Update progress bar
            if (progressIngredients != null && total > 0) {
                int progress = (count * 100) / total;
                progressIngredients.setProgress(progress);
            }
            if (tvProgressText != null) {
                tvProgressText.setText(count + "/" + total);
            }
            
            // Update cart badge
            updateCartBadge(count);
        });

        // Observe cart validity
        viewModel.isCartValid().observe(getViewLifecycleOwner(), isValid -> {
            btnCheckout.setEnabled(isValid);
            if (isValid) {
                AnimationHelper.bounce(btnCheckout);
            }
        });
    }

    /**
     * Load ingredients from repository
     */
    private List<Ingredient> loadIngredients(List<String> requiredIds, List<String> distractorIds) {
        List<Ingredient> allIngredients = new ArrayList<>();
        LevelRepository repository = LevelRepository.getInstance();

        // Add required ingredients
        for (String id : requiredIds) {
            Ingredient ingredient = repository.getIngredient(id);
            if (ingredient != null) {
                allIngredients.add(ingredient);
            }
        }

        // Add distractor ingredients
        for (String id : distractorIds) {
            Ingredient ingredient = repository.getIngredient(id);
            if (ingredient != null) {
                allIngredients.add(ingredient);
            }
        }

        // Shuffle for randomness
        java.util.Collections.shuffle(allIngredients);

        return allIngredients;
    }

    /**
     * Show hint - highlight the current requested ingredient
     */
    public void showHint() {
        if (requiredIngredientIds == null || currentRequestIndex >= requiredIngredientIds.size()) return;
        
        String targetId = requiredIngredientIds.get(currentRequestIndex);
        View itemView = findViewForIngredient(targetId);
        
        if (itemView != null) {
            AnimationHelper.pulse(itemView);
            soundManager.play(SoundPoolManager.SOUND_CLICK);
        }
        
        // Also speak the ingredient name
        speakCurrentRequest();
    }

    /**
     * Update cart badge with animation
     */
    private void updateCartBadge(int count) {
        if (tvCartBadge == null) return;
        
        if (count > 0) {
            tvCartBadge.setText(String.valueOf(count));
            tvCartBadge.setVisibility(View.VISIBLE);
            
            // Bounce animation
            if (ivCartIcon != null) {
                AnimationHelper.bounce(ivCartIcon);
            }
            AnimationHelper.pulse(tvCartBadge);
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        ttsManager.stop();
    }
}
