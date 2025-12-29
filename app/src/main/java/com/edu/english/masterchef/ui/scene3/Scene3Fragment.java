package com.edu.english.masterchef.ui.scene3;

import android.content.ClipData;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.CookingStep;
import com.edu.english.masterchef.data.model.Food;
import com.edu.english.masterchef.data.model.LevelConfig;
import com.edu.english.masterchef.data.model.PlayerAction;
import com.edu.english.masterchef.ui.game.GameViewModel;
import com.edu.english.masterchef.ui.game.MasterChefGameActivity;
import com.edu.english.masterchef.util.AnimationHelper;
import com.edu.english.masterchef.util.SoundPoolManager;
import com.edu.english.masterchef.util.TTSManager;

/**
 * Scene 3: Kitchen Cooking
 * Kid-friendly cooking scene with drag-drop ingredients,
 * chef instructions with keyword highlighting, and fun animations
 */
public class Scene3Fragment extends Fragment {

    private Scene3ViewModel viewModel;
    private GameViewModel gameViewModel;
    private TTSManager ttsManager;
    private SoundPoolManager soundManager;
    private Handler handler = new Handler(Looper.getMainLooper());

    // Instruction panel (Order Ticket)
    private CardView cardOrderTicket;
    private TextView tvStepNumber, tvInstruction, tvInstructionVn;
    private ImageView btnSpeaker, ivChef, ivCustomer;
    private ProgressBar progressSteps;

    // Kitchen elements
    private ImageView ivStove, ivCookingVessel, ivCuttingBoard, ivServingPlate;
    private ImageView ivDropZoneIcon, ivFire, ivSteam, ivKnife, ivSpoon;
    private View cuttingBoardArea;
    private TextView tvChefSpeech;

    // Cooking area
    private CardView dropZone;
    private TextView tvZoneLabel, tvSuccessCheck;
    private LinearLayout ingredientPalette;

    // Overlays
    private View timerOverlay, completionOverlay;
    private TextView tvTimerText, tvDishName;
    private ProgressBar progressTimer;
    private ImageView ivCompletedDish;
    
    // Completion overlay elements
    private TextView tvCompletionStars, tvCompletionScore, tvCompletionPerfect, tvCompletionTime, tvUnlockMessage;
    private View btnCompletionContinue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scene3_cooking_v5, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        viewModel = new ViewModelProvider(this).get(Scene3ViewModel.class);
        gameViewModel = ((MasterChefGameActivity) requireActivity()).getGameViewModel();

        // Initialize managers
        ttsManager = TTSManager.getInstance(requireContext());
        soundManager = SoundPoolManager.getInstance(requireContext());

        // Find views
        findViews(view);
        setupListeners();
        observeViewModel();

        // Initialize scene with current level's cooking steps
        LevelConfig level = gameViewModel.getCurrentLevel().getValue();
        if (level != null && level.getCookingScene() != null) {
            viewModel.initializeScene(level.getCookingScene());
        }
    }

    private void findViews(View view) {
        // Order ticket / Instruction panel
        cardOrderTicket = view.findViewById(R.id.card_order_ticket);
        tvStepNumber = view.findViewById(R.id.tv_step_number);
        tvInstruction = view.findViewById(R.id.tv_instruction);
        tvInstructionVn = view.findViewById(R.id.tv_instruction_vn);
        btnSpeaker = view.findViewById(R.id.btn_speaker);
        progressSteps = view.findViewById(R.id.progress_steps);

        // Characters
        ivChef = view.findViewById(R.id.iv_chef);
        ivCustomer = view.findViewById(R.id.iv_customer);
        tvChefSpeech = view.findViewById(R.id.tv_chef_speech);

        // Kitchen elements
        ivStove = view.findViewById(R.id.iv_stove);
        ivCookingVessel = view.findViewById(R.id.iv_cooking_vessel);
        ivCuttingBoard = view.findViewById(R.id.iv_cutting_board);
        ivServingPlate = view.findViewById(R.id.iv_serving_plate);
        cuttingBoardArea = view.findViewById(R.id.cutting_board_area);
        ivDropZoneIcon = view.findViewById(R.id.iv_drop_zone_icon);
        ivFire = view.findViewById(R.id.iv_fire);
        ivSteam = view.findViewById(R.id.iv_steam);
        ivKnife = view.findViewById(R.id.iv_knife);
        ivSpoon = view.findViewById(R.id.iv_spoon);

        // Cooking area
        dropZone = view.findViewById(R.id.drop_zone);
        tvZoneLabel = view.findViewById(R.id.tv_zone_label);
        tvSuccessCheck = view.findViewById(R.id.tv_success_check);
        ingredientPalette = view.findViewById(R.id.ingredient_container);

        // Overlays
        timerOverlay = view.findViewById(R.id.timer_overlay);
        completionOverlay = view.findViewById(R.id.completion_overlay);
        tvTimerText = view.findViewById(R.id.tv_timer_text);
        tvDishName = view.findViewById(R.id.tv_dish_name);
        progressTimer = view.findViewById(R.id.progress_timer);
        ivCompletedDish = view.findViewById(R.id.iv_completed_dish);
        
        // Completion overlay elements
        tvCompletionStars = view.findViewById(R.id.tv_completion_stars);
        tvCompletionScore = view.findViewById(R.id.tv_completion_score);
        tvCompletionPerfect = view.findViewById(R.id.tv_completion_perfect);
        tvCompletionTime = view.findViewById(R.id.tv_completion_time);
        tvUnlockMessage = view.findViewById(R.id.tv_unlock_message);
        btnCompletionContinue = view.findViewById(R.id.btn_completion_continue);
    }

    private void setupListeners() {
        if (btnSpeaker != null) {
            btnSpeaker.setOnClickListener(v -> {
                soundManager.play(SoundPoolManager.SOUND_CLICK);
                AnimationHelper.bounce(btnSpeaker);
                CookingStep step = viewModel.getCurrentStep().getValue();
                if (step != null) {
                    ttsManager.speak(step.getInstruction(), "step_instruction");
                }
            });
        }

        // Setup drop zone
        setupDropZone();
    }

    private void observeViewModel() {
        // Observe current step
        viewModel.getCurrentStep().observe(getViewLifecycleOwner(), this::displayStep);

        // Observe step index
        viewModel.getCurrentStepIndex().observe(getViewLifecycleOwner(), index -> {
            if (index == null) return;
            int stepNumber = index + 1;
            int totalSteps = viewModel.getTotalSteps();
            if (tvStepNumber != null) {
                tvStepNumber.setText("Step " + stepNumber + " / " + totalSteps);
            }
            
            // Update progress bar
            if (progressSteps != null && totalSteps > 0) {
                int progress = (stepNumber * 100) / totalSteps;
                progressSteps.setProgress(progress);
            }
        });

        // Observe step completion
        viewModel.isStepComplete().observe(getViewLifecycleOwner(), isComplete -> {
            if (isComplete && viewModel.isAllStepsComplete()) {
                showCompletion();
            }
        });
    }

    /**
     * Display a cooking step with keyword highlighting
     */
    private void displayStep(CookingStep step) {
        if (step == null) return;

        // Update instruction text with keyword highlighting
        String instruction = step.getInstruction();
        String targetItem = step.getTargetItemId();
        
        // Highlight keyword in RED for emphasis
        SpannableString spannableInstruction = highlightKeyword(instruction, targetItem);
        if (tvInstruction != null) {
            tvInstruction.setText(spannableInstruction);
        }
        
        // Animate order ticket card
        if (cardOrderTicket != null) {
            AnimationHelper.bounce(cardOrderTicket);
        }
        
        // Chef wave animation
        if (ivChef != null) {
            AnimationHelper.pulse(ivChef);
            showChefReaction("👨‍🍳");
        }
        
        // Customer anticipation animation
        if (ivCustomer != null) {
            AnimationHelper.pulse(ivCustomer);
        }
        
        // Update cooking vessel based on step
        updateCookingVessel(step);

        // Speak instruction
        ttsManager.speak(instruction, "step_" + viewModel.getCurrentStepNumber());

        // Setup UI based on step type
        switch (step.getStepType()) {
            case "DragDropToZone":
            case "DragDrop":
                setupDragDropStep(step);
                break;
            case "TapOnItem":
            case "TapItem":
                setupTapItemStep(step);
                break;
            case "TapOnZone":
            case "TapZone":
                setupTapZoneStep(step);
                break;
            case "HoldToPour":
            case "Hold":
                setupHoldStep(step);
                break;
            case "StirGesture":
            case "Stir":
                setupStirStep(step);
                break;
            case "TimerWait":
                setupTimerStep(step);
                break;
            default:
                setupSimpleStep(step);
                break;
        }
    }

    /**
     * Highlight keyword in instruction text (RED color for emphasis)
     */
    private SpannableString highlightKeyword(String text, String keyword) {
        SpannableString spannable = new SpannableString(text);
        
        if (keyword == null || keyword.isEmpty()) return spannable;
        
        // Convert keyword to readable form (e.g., "olive_oil" -> "olive oil")
        String readableKeyword = keyword.replace("_", " ");
        
        int startIndex = text.toLowerCase().indexOf(readableKeyword.toLowerCase());
        if (startIndex >= 0) {
            int endIndex = startIndex + readableKeyword.length();
            // RED color for keyword
            spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#FF5722")),
                startIndex, endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            // Bold the keyword
            spannable.setSpan(
                new StyleSpan(android.graphics.Typeface.BOLD),
                startIndex, endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        
        return spannable;
    }

    /**
     * Show chef reaction with emoji
     */
    private void showChefReaction(String emoji) {
        if (tvChefSpeech != null) {
            tvChefSpeech.setText(emoji);
            tvChefSpeech.setVisibility(View.VISIBLE);
            tvChefSpeech.setScaleX(0f);
            tvChefSpeech.setScaleY(0f);
            tvChefSpeech.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .withEndAction(() -> {
                    handler.postDelayed(() -> {
                        if (tvChefSpeech != null) {
                            tvChefSpeech.setVisibility(View.GONE);
                        }
                    }, 1500);
                })
                .start();
        }
    }

    /**
     * Update cooking vessel based on step type
     */
    private void updateCookingVessel(CookingStep step) {
        if (ivCookingVessel == null) return;
        
        String stepType = step.getStepType();
        String zone = step.getTargetZoneId();
        String instruction = step.getInstruction() != null ? step.getInstruction().toLowerCase() : "";
        
        // Determine if this is a cutting/chopping step
        boolean isCuttingStep = (zone != null && zone.contains("cut")) 
            || "cut".equalsIgnoreCase(stepType)
            || instruction.contains("chop") 
            || instruction.contains("cut")
            || instruction.contains("slice");
        
        // Determine if this is a cooking/frying/boiling step
        boolean isCookingStep = (zone != null && (zone.contains("pan") || zone.contains("pot") || zone.contains("stove")))
            || instruction.contains("fry")
            || instruction.contains("cook")
            || instruction.contains("boil")
            || instruction.contains("stir")
            || instruction.contains("sauté")
            || instruction.contains("heat");
        
        // Show fire and steam based on cooking action
        if (ivFire != null) {
            ivFire.setVisibility(isCookingStep ? View.VISIBLE : View.INVISIBLE);
            if (isCookingStep) {
                // Add flickering animation to fire
                ivFire.animate()
                    .scaleX(1.1f).scaleY(1.1f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        if (ivFire != null) {
                            ivFire.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
                        }
                    })
                    .start();
            }
        }
        
        if (ivSteam != null) {
            ivSteam.setVisibility(isCookingStep ? View.VISIBLE : View.GONE);
            if (isCookingStep) {
                // Add floating animation to steam
                ivSteam.animate()
                    .translationYBy(-10f)
                    .alpha(0.8f)
                    .setDuration(500)
                    .withEndAction(() -> {
                        if (ivSteam != null) {
                            ivSteam.animate().translationYBy(10f).alpha(0.5f).setDuration(500).start();
                        }
                    })
                    .start();
            }
        }
        
        // Highlight cutting board area for cutting steps
        if (cuttingBoardArea != null) {
            cuttingBoardArea.setAlpha(isCuttingStep ? 1.0f : 0.7f);
        }
        
        // Update cooking vessel icon based on zone
        if (zone != null) {
            if (zone.contains("pot") || zone.contains("boil")) {
                ivCookingVessel.setImageResource(R.drawable.ic_cooking_pot);
            } else {
                ivCookingVessel.setImageResource(R.drawable.ic_frying_pan);
            }
        }
    }

    /**
     * Update drop zone icon based on zone type
     */
    private void updateDropZoneIcon(String zoneId) {
        if (ivDropZoneIcon == null) return;
        
        if (zoneId == null) {
            ivDropZoneIcon.setImageResource(R.drawable.ic_frying_pan);
            return;
        }
        
        if (zoneId.contains("cutting") || zoneId.contains("cut")) {
            ivDropZoneIcon.setImageResource(R.drawable.ic_cutting_board);
        } else if (zoneId.contains("pot") || zoneId.contains("boil")) {
            ivDropZoneIcon.setImageResource(R.drawable.ic_cooking_pot);
        } else if (zoneId.contains("plate") || zoneId.contains("serve")) {
            ivDropZoneIcon.setImageResource(R.drawable.ic_serving_plate);
        } else {
            ivDropZoneIcon.setImageResource(R.drawable.ic_frying_pan);
        }
    }

    /**
     * Setup drag-drop step
     */
    private void setupDragDropStep(CookingStep step) {
        // Update drop zone
        String targetItem = step.getTargetItemId();
        String zone = step.getTargetZoneId();
        
        if (tvZoneLabel != null) {
            tvZoneLabel.setText("Drop " + formatItemName(targetItem) + " here");
        }
        updateDropZoneIcon(zone);
        if (dropZone != null) {
            dropZone.setVisibility(View.VISIBLE);
            // Reset drop zone color
            dropZone.setCardBackgroundColor(Color.parseColor("#40FFFFFF"));
        }

        // Create draggable item
        if (ingredientPalette != null) {
            ingredientPalette.removeAllViews();
            View draggableItem = createDraggableItem(targetItem);
            ingredientPalette.addView(draggableItem);
            
            // Animate item entrance
            draggableItem.setScaleX(0f);
            draggableItem.setScaleY(0f);
            draggableItem.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.OvershootInterpolator(2f))
                .start();
        }
    }

    /**
     * Get emoji for cooking zone
     */
    private String getZoneEmoji(String zoneId) {
        if (zoneId == null) return "🍳";
        
        if (zoneId.contains("cutting") || zoneId.contains("cut")) return "🔪";
        if (zoneId.contains("stove") || zoneId.contains("pan")) return "🍳";
        if (zoneId.contains("plate")) return "🍽️";
        if (zoneId.contains("counter")) return "📦";
        if (zoneId.contains("bowl")) return "🥣";
        if (zoneId.contains("pot")) return "🥘";
        
        return "🍳";
    }

    /**
     * Format item name for display
     */
    private String formatItemName(String itemId) {
        if (itemId == null) return "";
        return itemId.replace("_", " ");
    }

    /**
     * Create a draggable ingredient item
     */
    private View createDraggableItem(String itemId) {
        CardView cardView = new CardView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(12, 8, 12, 8);
        cardView.setLayoutParams(params);
        cardView.setRadius(20);
        cardView.setCardElevation(8);
        cardView.setCardBackgroundColor(Color.WHITE);
        cardView.setMinimumWidth(170);

        LinearLayout container = new LinearLayout(requireContext());
        container.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER);
        container.setPadding(16, 12, 16, 16);

        // Emoji/Icon
        TextView emojiView = new TextView(requireContext());
        emojiView.setText(getIngredientEmoji(itemId));
        emojiView.setTextSize(32);
        emojiView.setGravity(android.view.Gravity.CENTER);
        
        // Name label
        TextView nameView = new TextView(requireContext());
        nameView.setText(formatItemName(itemId));
        nameView.setTextSize(13);
        nameView.setTextColor(Color.parseColor("#333333"));
        nameView.setGravity(android.view.Gravity.CENTER);
        nameView.setMaxLines(2);
        nameView.setPadding(0, 6, 0, 0);

        container.addView(emojiView);
        container.addView(nameView);
        cardView.addView(container);

        // Make it draggable
        cardView.setTag(itemId);
        cardView.setOnLongClickListener(v -> {
            soundManager.play(SoundPoolManager.SOUND_CLICK);
            ClipData data = ClipData.newPlainText("itemId", itemId);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadowBuilder, v, 0);
            return true;
        });
        
        // Also tap to move (easier for kids)
        cardView.setOnClickListener(v -> {
            soundManager.play(SoundPoolManager.SOUND_CLICK);
            AnimationHelper.bounce(cardView);
            // Auto-complete drag for tap
            handler.postDelayed(() -> {
                autoCompleteDragDrop(itemId);
            }, 300);
        });

        return cardView;
    }

    /**
     * Get emoji for ingredient
     */
    private String getIngredientEmoji(String itemId) {
        if (itemId == null) return "🥘";
        
        if (itemId.contains("tomato")) return "🍅";
        if (itemId.contains("onion")) return "🧅";
        if (itemId.contains("garlic")) return "🧄";
        if (itemId.contains("beef") || itemId.contains("meat")) return "🥩";
        if (itemId.contains("pasta") || itemId.contains("spaghetti")) return "🍝";
        if (itemId.contains("oil") || itemId.contains("olive")) return "🫒";
        if (itemId.contains("salt")) return "🧂";
        if (itemId.contains("basil")) return "🌿";
        if (itemId.contains("cheese")) return "🧀";
        if (itemId.contains("sauce")) return "🥫";
        if (itemId.contains("egg")) return "🥚";
        if (itemId.contains("pepper")) return "🌶️";
        if (itemId.contains("chicken")) return "🍗";
        if (itemId.contains("rice")) return "🍚";
        if (itemId.contains("fish") || itemId.contains("salmon")) return "🐟";
        if (itemId.contains("shrimp")) return "🦐";
        if (itemId.contains("potato")) return "🥔";
        if (itemId.contains("carrot")) return "🥕";
        if (itemId.contains("lettuce") || itemId.contains("salad")) return "🥬";
        if (itemId.contains("mushroom")) return "🍄";
        if (itemId.contains("butter")) return "🧈";
        if (itemId.contains("milk")) return "🥛";
        if (itemId.contains("flour")) return "🌾";
        if (itemId.contains("sugar")) return "🍬";
        
        return "🥘";
    }

    /**
     * Get food image resource by food ID
     */
    private int getFoodImageResource(String foodId) {
        if (foodId == null) return R.drawable.ic_placeholder;
        
        String id = foodId.toLowerCase();
        if (id.contains("spaghetti") || id.contains("bolognese") || id.contains("pasta")) {
            return R.drawable.food_spaghetti;
        }
        if (id.contains("pizza")) {
            return R.drawable.food_pizza;
        }
        if (id.contains("burger") || id.contains("hamburger")) {
            return R.drawable.food_burger;
        }
        if (id.contains("salad")) {
            return R.drawable.food_salad;
        }
        if (id.contains("soup")) {
            return R.drawable.food_soup;
        }
        if (id.contains("sandwich")) {
            return R.drawable.food_sandwich;
        }
        if (id.contains("pancake")) {
            return R.drawable.food_pancake;
        }
        if (id.contains("cake")) {
            return R.drawable.food_cake;
        }
        if (id.contains("cookie")) {
            return R.drawable.food_cookie;
        }
        if (id.contains("ice_cream") || id.contains("icecream")) {
            return R.drawable.food_ice_cream;
        }
        
        return R.drawable.ic_placeholder;
    }

    /**
     * Auto-complete drag-drop action (tap shortcut for kids)
     */
    private void autoCompleteDragDrop(String itemId) {
        CookingStep currentStep = viewModel.getCurrentStep().getValue();
        if (currentStep == null) return;
        
        PlayerAction action = new PlayerAction();
        action.setActionType("DragDrop");
        action.setItemId(itemId);
        action.setZoneId(currentStep.getTargetZoneId());

        boolean isValid = gameViewModel.validateCookingStep(action);
        
        if (isValid) {
            // Visual feedback for success
            soundManager.play(SoundPoolManager.SOUND_SUCCESS);
            showStepSuccess();
            
            // Move to next step after animation
            handler.postDelayed(() -> viewModel.completeCurrentStep(), 1000);
        } else {
            // Visual feedback for error
            soundManager.play(SoundPoolManager.SOUND_WRONG);
            showStepError();
        }
    }

    /**
     * Show toast message
     */
    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show step error animation
     */
    private void showStepError() {
        if (dropZone != null) {
            // Shake animation for error
            dropZone.animate()
                .translationX(-10)
                .setDuration(50)
                .withEndAction(() -> dropZone.animate()
                    .translationX(10)
                    .setDuration(50)
                    .withEndAction(() -> dropZone.animate()
                        .translationX(-5)
                        .setDuration(50)
                        .withEndAction(() -> dropZone.animate()
                            .translationX(0)
                            .setDuration(50)
                            .start())
                        .start())
                    .start())
                .start();
            
            // Flash red
            dropZone.setCardBackgroundColor(Color.parseColor("#FFCDD2"));
            handler.postDelayed(() -> {
                if (dropZone != null) {
                    dropZone.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
                }
            }, 500);
        }
    }

    /**
     * Setup drop zone for drag-drop
     */
    private void setupDropZone() {
        if (dropZone == null) return;
        
        dropZone.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    dropZone.setCardBackgroundColor(Color.parseColor("#C8E6C9"));
                    AnimationHelper.bounce(dropZone);
                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    dropZone.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
                    break;

                case DragEvent.ACTION_DROP:
                    // Get dropped item ID
                    ClipData data = event.getClipData();
                    String droppedItemId = data.getItemAt(0).getText().toString();
                    
                    // Validate with GameViewModel
                    CookingStep currentStep = viewModel.getCurrentStep().getValue();
                    if (currentStep != null) {
                        PlayerAction action = new PlayerAction();
                        action.setActionType("DragDrop");
                        action.setItemId(droppedItemId);
                        action.setZoneId(currentStep.getTargetZoneId());

                        boolean isValid = gameViewModel.validateCookingStep(action);
                        
                        if (isValid) {
                            // Visual feedback for success
                            soundManager.play(SoundPoolManager.SOUND_SUCCESS);
                            showStepSuccess();
                            
                            // Move to next step after animation
                            handler.postDelayed(() -> viewModel.completeCurrentStep(), 1000);
                        } else {
                            // Visual feedback for error
                            soundManager.play(SoundPoolManager.SOUND_WRONG);
                            showStepError();
                        }
                    }
                    break;

                case DragEvent.ACTION_DRAG_ENDED:
                    dropZone.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
                    break;
            }
            return true;
        });
    }

    /**
     * Show step success animation
     */
    private void showStepSuccess() {
        if (tvSuccessCheck != null) {
            tvSuccessCheck.setVisibility(View.VISIBLE);
            tvSuccessCheck.setScaleX(0f);
            tvSuccessCheck.setScaleY(0f);
            tvSuccessCheck.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .withEndAction(() -> {
                    tvSuccessCheck.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            handler.postDelayed(() -> {
                                tvSuccessCheck.setVisibility(View.GONE);
                            }, 500);
                        })
                        .start();
                })
                .start();
        }
        
        // Update zone label
        tvZoneLabel.setText("✓ Done!");
        dropZone.setCardBackgroundColor(Color.parseColor("#C8E6C9"));
    }

    /**
     * Setup tap item step
     */
    private void setupTapItemStep(CookingStep step) {
        // For tap steps, create a tappable item
        if (ingredientPalette == null) return;
        
        ingredientPalette.removeAllViews();
        
        CardView tappableItem = new CardView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(12, 8, 12, 8);
        tappableItem.setLayoutParams(params);
        tappableItem.setRadius(20);
        tappableItem.setCardElevation(8);
        tappableItem.setCardBackgroundColor(Color.WHITE);
        tappableItem.setMinimumWidth(170);

        LinearLayout container = new LinearLayout(requireContext());
        container.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER);
        container.setPadding(16, 12, 16, 16);

        TextView emojiView = new TextView(requireContext());
        emojiView.setText(getIngredientEmoji(step.getTargetItemId()));
        emojiView.setTextSize(32);
        emojiView.setGravity(android.view.Gravity.CENTER);

        TextView tapLabel = new TextView(requireContext());
        tapLabel.setText("TAP ME!");
        tapLabel.setTextSize(13);
        tapLabel.setTextColor(Color.parseColor("#FF6B35"));
        tapLabel.setGravity(android.view.Gravity.CENTER);
        tapLabel.setPadding(0, 6, 0, 0);

        container.addView(emojiView);
        container.addView(tapLabel);
        tappableItem.addView(container);

        tappableItem.setOnClickListener(v -> {
            AnimationHelper.bounce(tappableItem);
            
            PlayerAction action = new PlayerAction();
            action.setActionType("Tap");
            action.setItemId(step.getTargetItemId());
            action.setTapCount(step.getRequiredCount()); // Meet required count

            boolean isValid = gameViewModel.validateCookingStep(action);
            if (isValid) {
                soundManager.play(SoundPoolManager.SOUND_SUCCESS);
                showStepSuccess();
                handler.postDelayed(() -> viewModel.completeCurrentStep(), 800);
            } else {
                soundManager.play(SoundPoolManager.SOUND_WRONG);
                showStepError();
            }
        });

        ingredientPalette.addView(tappableItem);
        if (dropZone != null) {
            dropZone.setVisibility(View.GONE);
        }
        
        // Pulse animation to attract attention
        AnimationHelper.pulse(tappableItem);
    }

    /**
     * Setup tap zone step
     */
    private void setupTapZoneStep(CookingStep step) {
        if (dropZone == null) return;
        
        dropZone.setVisibility(View.VISIBLE);
        if (tvZoneLabel != null) {
            tvZoneLabel.setText("Tap here! 👆");
        }
        updateDropZoneIcon(step.getTargetZoneId());
        dropZone.setCardBackgroundColor(Color.parseColor("#40FFF3E0"));
        
        // Pulse to attract attention
        AnimationHelper.pulse(dropZone);
        
        dropZone.setOnClickListener(v -> {
            AnimationHelper.bounce(dropZone);
            
            PlayerAction action = new PlayerAction();
            action.setActionType("TapZone");
            action.setZoneId(step.getTargetZoneId());
            action.setTapCount(step.getRequiredCount()); // Meet required count

            boolean isValid = gameViewModel.validateCookingStep(action);
            if (isValid) {
                soundManager.play(SoundPoolManager.SOUND_SUCCESS);
                showStepSuccess();
                handler.postDelayed(() -> viewModel.completeCurrentStep(), 800);
            } else {
                soundManager.play(SoundPoolManager.SOUND_WRONG);
                showStepError();
            }
        });

        if (ingredientPalette != null) {
            ingredientPalette.removeAllViews();
        }
    }

    /**
     * Setup hold step - user holds on dropZone to pour
     */
    private void setupHoldStep(CookingStep step) {
        if (dropZone == null) return;
        
        dropZone.setVisibility(View.VISIBLE);
        if (tvZoneLabel != null) {
            tvZoneLabel.setText("Hold to pour! 🫗");
        }
        updateDropZoneIcon(step.getTargetZoneId());
        dropZone.setCardBackgroundColor(Color.parseColor("#40E1F5FE"));
        
        AnimationHelper.pulse(dropZone);
        
        final int requiredHoldMs = step.getRequiredHoldMs() > 0 ? step.getRequiredHoldMs() : 2000;
        final long[] holdStartTime = {0};
        final boolean[] isHolding = {false};
        final Runnable[] holdCompleteRunnable = {null};
        
        // Touch listener for hold detection
        dropZone.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    // Start holding
                    holdStartTime[0] = System.currentTimeMillis();
                    isHolding[0] = true;
                    
                    // Visual feedback
                    dropZone.setCardBackgroundColor(Color.parseColor("#80BBDEFB"));
                    AnimationHelper.pulse(dropZone);
                    soundManager.play(SoundPoolManager.SOUND_CLICK);
                    
                    // Schedule completion check
                    holdCompleteRunnable[0] = () -> {
                        if (isHolding[0]) {
                            long holdDuration = System.currentTimeMillis() - holdStartTime[0];
                            if (holdDuration >= requiredHoldMs) {
                                // Hold completed!
                                PlayerAction action = new PlayerAction();
                                action.setType(PlayerAction.ActionType.HOLD_COMPLETE);
                                action.setItemId(step.getItemId());
                                action.setTargetZone(step.getTargetZoneId());
                                action.setHoldDuration((int) holdDuration);
                                
                                boolean isValid = gameViewModel.validateCookingStep(action);
                                if (isValid) {
                                    soundManager.play(SoundPoolManager.SOUND_SUCCESS);
                                    showStepSuccess();
                                    dropZone.setOnTouchListener(null);
                                    handler.postDelayed(() -> viewModel.completeCurrentStep(), 800);
                                }
                            }
                        }
                    };
                    handler.postDelayed(holdCompleteRunnable[0], requiredHoldMs);
                    return true;
                    
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    // Released
                    isHolding[0] = false;
                    if (holdCompleteRunnable[0] != null) {
                        handler.removeCallbacks(holdCompleteRunnable[0]);
                    }
                    
                    long holdDuration = System.currentTimeMillis() - holdStartTime[0];
                    dropZone.setCardBackgroundColor(Color.parseColor("#40E1F5FE"));
                    
                    if (holdDuration < requiredHoldMs) {
                        // Not held long enough - show progress
                        int percent = (int) ((holdDuration * 100) / requiredHoldMs);
                        showToast("Hold longer! " + percent + "%");
                    }
                    return true;
            }
            return false;
        });
        
        // Disable click listener (using touch instead)
        dropZone.setOnClickListener(null);

        if (ingredientPalette != null) {
            ingredientPalette.removeAllViews();
        }
    }

    /**
     * Setup stir step
     */
    private void setupStirStep(CookingStep step) {
        if (dropZone == null) return;
        
        dropZone.setVisibility(View.VISIBLE);
        if (tvZoneLabel != null) {
            tvZoneLabel.setText("Stir here! 🥄");
        }
        updateDropZoneIcon(step.getTargetZoneId());
        dropZone.setCardBackgroundColor(Color.parseColor("#40FFF8E1"));
        
        AnimationHelper.pulse(dropZone);
        
        dropZone.setOnClickListener(v -> {
            // Rotate animation to simulate stirring
            dropZone.animate()
                .rotationBy(360f)
                .setDuration(500)
                .withEndAction(() -> {
                    PlayerAction action = new PlayerAction();
                    action.setActionType("Stir");
                    action.setZoneId(step.getTargetZoneId());
                    action.setGestureProgress(1.0f); // Full progress on tap

                    boolean isValid = gameViewModel.validateCookingStep(action);
                    if (isValid) {
                        soundManager.play(SoundPoolManager.SOUND_SUCCESS);
                        showStepSuccess();
                        handler.postDelayed(() -> viewModel.completeCurrentStep(), 800);
                    } else {
                        soundManager.play(SoundPoolManager.SOUND_WRONG);
                        showStepError();
                    }
                })
                .start();
        });

        if (ingredientPalette != null) {
            ingredientPalette.removeAllViews();
        }
    }

    /**
     * Setup timer step
     */
    private void setupTimerStep(CookingStep step) {
        // Show timer overlay
        if (timerOverlay != null) {
            timerOverlay.setVisibility(View.VISIBLE);
            tvTimerText.setText("Cooking...");
            progressTimer.setProgress(0);
            
            // Animate progress - use requiredHoldMs for timer duration
            int durationMs = step.getRequiredHoldMs() > 0 ? step.getRequiredHoldMs() : 3000;
            int steps = 20;
            int interval = durationMs / steps;
            
            for (int i = 1; i <= steps; i++) {
                final int progress = (i * 100) / steps;
                handler.postDelayed(() -> {
                    progressTimer.setProgress(progress);
                }, i * interval);
            }
            
            // Complete after timer
            handler.postDelayed(() -> {
                timerOverlay.setVisibility(View.GONE);
                
                PlayerAction action = new PlayerAction();
                action.setActionType("TimerComplete");

                boolean isValid = gameViewModel.validateCookingStep(action);
                if (isValid) {
                    soundManager.play(SoundPoolManager.SOUND_SUCCESS);
                    viewModel.completeCurrentStep();
                }
            }, durationMs);
        } else {
            // Fallback to simple step
            setupSimpleStep(step);
        }
    }

    /**
     * Setup simple continue button step
     */
    private void setupSimpleStep(CookingStep step) {
        if (dropZone == null) return;
        
        dropZone.setVisibility(View.VISIBLE);
        if (tvZoneLabel != null) {
            tvZoneLabel.setText("Tap to Continue ▶️");
        }
        if (ivDropZoneIcon != null) {
            ivDropZoneIcon.setImageResource(R.drawable.ic_frying_pan);
        }
        dropZone.setCardBackgroundColor(Color.parseColor("#40E8F5E9"));
        
        dropZone.setOnClickListener(v -> {
            soundManager.play(SoundPoolManager.SOUND_CLICK);
            AnimationHelper.bounce(dropZone);
            
            PlayerAction action = new PlayerAction();
            action.setActionType("Continue");

            boolean isValid = gameViewModel.validateCookingStep(action);
            if (isValid) {
                handler.postDelayed(() -> viewModel.completeCurrentStep(), 300);
            }
        });

        if (ingredientPalette != null) {
            ingredientPalette.removeAllViews();
        }
    }

    /**
     * Show completion celebration
     */
    private void showCompletion() {
        if (completionOverlay != null) {
            LevelConfig level = gameViewModel.getCurrentLevel().getValue();
            if (level != null && level.getFood() != null) {
                // Set dish name
                if (tvDishName != null) {
                    tvDishName.setText(level.getFood().getName());
                }
                // Set completed dish image
                if (ivCompletedDish != null) {
                    int imageResId = getFoodImageResource(level.getFood().getDishId());
                    ivCompletedDish.setImageResource(imageResId);
                }
            }
            
            // Get score and stats
            Integer scoreValue = gameViewModel.getCurrentScore().getValue();
            int score = scoreValue != null ? scoreValue : 0;
            int stars = gameViewModel.getStarsForScore(score);
            int perfectSteps = gameViewModel.getPerfectSteps();
            int totalSteps = gameViewModel.getTotalSteps();
            int timeSeconds = gameViewModel.getElapsedTimeSeconds();
            
            // Update stars display
            if (tvCompletionStars != null) {
                String starsText;
                switch (stars) {
                    case 3: starsText = "⭐ ⭐ ⭐"; break;
                    case 2: starsText = "⭐ ⭐ ☆"; break;
                    case 1: starsText = "⭐ ☆ ☆"; break;
                    default: starsText = "☆ ☆ ☆"; break;
                }
                tvCompletionStars.setText(starsText);
            }
            
            // Update score
            if (tvCompletionScore != null) {
                tvCompletionScore.setText("Score: " + score);
            }
            
            // Update perfect steps
            if (tvCompletionPerfect != null) {
                tvCompletionPerfect.setText("✓ " + perfectSteps + "/" + totalSteps);
            }
            
            // Update time
            if (tvCompletionTime != null) {
                int minutes = timeSeconds / 60;
                int seconds = timeSeconds % 60;
                tvCompletionTime.setText(String.format("⏱ %d:%02d", minutes, seconds));
            }
            
            // Show unlock message if earned at least 1 star
            if (tvUnlockMessage != null) {
                tvUnlockMessage.setVisibility(stars >= 1 ? View.VISIBLE : View.GONE);
            }
            
            // Setup continue button
            if (btnCompletionContinue != null) {
                btnCompletionContinue.setOnClickListener(v -> {
                    soundManager.play(SoundPoolManager.SOUND_CLICK);
                    gameViewModel.completeCookingScene();
                });
            }
            
            completionOverlay.setVisibility(View.VISIBLE);
            completionOverlay.setAlpha(0f);
            completionOverlay.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
            
            soundManager.play(SoundPoolManager.SOUND_SUCCESS);
            ttsManager.speak("Congratulations! You completed the dish!", "completion");
        } else {
            // Fallback: directly complete
            gameViewModel.completeCookingScene();
        }
    }

    /**
     * Show hint for current cooking step
     */
    public void showHint() {
        CookingStep step = viewModel.getCurrentStep().getValue();
        if (step == null) return;

        soundManager.play(SoundPoolManager.SOUND_CLICK);
        
        // Pulse the speaker button
        if (btnSpeaker != null) {
            AnimationHelper.pulse(btnSpeaker);
        }

        // Pulse relevant UI element based on step type
        String stepType = step.getStepType();
        if ("DragDropToZone".equals(stepType) || "DragDrop".equals(stepType)) {
            // Pulse both ingredient and drop zone
            if (ingredientPalette != null && ingredientPalette.getChildCount() > 0) {
                AnimationHelper.pulse(ingredientPalette.getChildAt(0));
            }
            if (dropZone != null) {
                AnimationHelper.pulse(dropZone);
            }
        } else if (dropZone != null) {
            AnimationHelper.pulse(dropZone);
        }

        // Speak the instruction
        ttsManager.speak(step.getInstruction(), "hint_instruction");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        ttsManager.stop();
    }
}
