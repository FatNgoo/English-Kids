package com.edu.english.masterchef;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;
import com.edu.english.masterchef.data.ActionType;
import com.edu.english.masterchef.data.Ingredient;
import com.edu.english.masterchef.data.MasterChefRepository;
import com.edu.english.masterchef.data.Recipe;
import com.edu.english.masterchef.data.RecipeStep;
import com.edu.english.masterchef.data.StepState;
import com.edu.english.masterchef.data.Tool;
import com.edu.english.masterchef.data.ZoneType;
import com.edu.english.masterchef.engine.KitchenGameEngine;
import com.edu.english.masterchef.services.ChefTTSService;
import com.edu.english.masterchef.services.SpeechRecognitionService;
import com.edu.english.masterchef.views.DraggableItemView;
import com.edu.english.masterchef.views.KitchenView;

import java.util.List;
import java.util.Locale;

/**
 * Main game activity for Master Chef cooking game.
 */
public class MasterChefActivity extends AppCompatActivity implements 
        KitchenGameEngine.GameEventListener,
        KitchenView.KitchenEventListener {

    // Views
    private KitchenView kitchenView;
    private TextView textRecipeName;
    private TextView textStepIndicator;
    private TextView textChefMessage;
    private ImageButton btnBack;
    private ImageButton btnHint;
    private ImageButton btnReplayTts;
    private LinearLayout ingredientTray;
    private FrameLayout actionProgressContainer;
    private ProgressBar progressAction;
    private TextView textActionHint;
    
    // Gesture hint overlay
    private com.edu.english.masterchef.views.GestureHintOverlay gestureHintOverlay;
    
    // Speaking overlay
    private FrameLayout speakingOverlay;
    private TextView textSpeakingPhrase;
    private TextView textSpeakingStatus;
    private ImageButton btnMic;
    private View btnHearAgain;
    private View btnSkipSpeaking;
    
    // Complete overlay
    private FrameLayout recipeCompleteOverlay;
    private TextView textFinalScore;
    private TextView textSpeakingStats;
    private View btnPlayAgain;
    private View btnChooseRecipe;
    private View btnExitGame;
    
    // Success message
    private TextView textSuccessMessage;
    
    // Game components
    private KitchenGameEngine gameEngine;
    private ChefTTSService ttsService;
    private SpeechRecognitionService speechService;
    private MasterChefRepository repository;
    
    // Current state
    private Recipe currentRecipe;
    private Handler handler;
    private boolean isSpeakingCheckActive = false;
    private int speakingCorrectCount = 0;
    private int speakingTotalCount = 0;
    private String currentSpeakingPhrase = "";
    
    // Permission launcher
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    // TTS Listener
    private final ChefTTSService.TTSListener ttsListener = new ChefTTSService.TTSListener() {
        @Override
        public void onSpeakStart(String text) {
            btnReplayTts.setEnabled(false);
        }
        
        @Override
        public void onSpeakDone(String text) {
            btnReplayTts.setEnabled(true);
        }
        
        @Override
        public void onTTSReady() {}
        
        @Override
        public void onTTSError(String error) {
            Toast.makeText(MasterChefActivity.this, "TTS: " + error, Toast.LENGTH_SHORT).show();
        }
    };
    
    // Speech Listener
    private final SpeechRecognitionService.SpeechListener speechListener = new SpeechRecognitionService.SpeechListener() {
        @Override
        public void onReadyForSpeech() {
            textSpeakingStatus.setText("Listening...");
        }
        
        @Override
        public void onListening() {
            textSpeakingStatus.setText("🎤 Listening...");
        }
        
        @Override
        public void onSpeechResult(String spokenText, boolean matchesExpected) {
            if (matchesExpected) {
                textSpeakingStatus.setText("✓ Perfect! \"" + spokenText + "\"");
                speakingCorrectCount++;
                handler.postDelayed(() -> closeSpeakingOverlay(), 1500);
            } else {
                textSpeakingStatus.setText("Try again! You said: \"" + spokenText + "\"");
            }
            btnMic.setEnabled(true);
        }
        
        @Override
        public void onPartialResult(String partialText) {
            textSpeakingStatus.setText("Hearing: " + partialText + "...");
        }
        
        @Override
        public void onError(String errorMessage, boolean canRetry) {
            textSpeakingStatus.setText("Error: " + errorMessage);
            btnMic.setEnabled(true);
        }
        
        @Override
        public void onSpeechNotAvailable() {
            textSpeakingStatus.setText("Speech not available");
            btnMic.setEnabled(false);
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        android.util.Log.d("MasterChef", "onCreate - START");
        setContentView(R.layout.activity_master_chef);
        android.util.Log.d("MasterChef", "onCreate - setContentView done");
        
        handler = new Handler(Looper.getMainLooper());
        repository = MasterChefRepository.getInstance();
        android.util.Log.d("MasterChef", "onCreate - repository initialized");
        
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted && speechService != null) {
                    speechService.initialize();
                    startSpeechRecognition();
                } else {
                    textSpeakingStatus.setText("Microphone permission required");
                }
            }
        );
        
        android.util.Log.d("MasterChef", "onCreate - calling initViews");
        initViews();
        setupWindowInsets();
        android.util.Log.d("MasterChef", "onCreate - calling initServices");
        initServices();
        android.util.Log.d("MasterChef", "onCreate - calling loadRecipe");
        loadRecipe();
        android.util.Log.d("MasterChef", "onCreate - COMPLETE");
    }
    
    /**
     * Setup window insets to handle notch/cutout and navigation bar properly
     */
    private void setupWindowInsets() {
        LinearLayout hudTop = findViewById(R.id.hud_top);
        View bottomDock = findViewById(R.id.ingredient_tray_scroll);
        
        if (hudTop != null) {
            ViewCompat.setOnApplyWindowInsetsListener(hudTop, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars() | 
                                                        WindowInsetsCompat.Type.displayCutout());
                // Add padding to top HUD to avoid notch/cutout
                v.setPadding(
                    (int) (12 * getResources().getDisplayMetrics().density), // 12dp horizontal
                    insets.top + (int) (10 * getResources().getDisplayMetrics().density), // status bar + 10dp
                    (int) (12 * getResources().getDisplayMetrics().density),
                    (int) (10 * getResources().getDisplayMetrics().density) // 10dp bottom
                );
                return windowInsets;
            });
        }
        
        // Handle navigation bar for bottom content
        View parentLayout = bottomDock != null ? (View) bottomDock.getParent() : null;
        if (parentLayout != null && parentLayout.getParent() instanceof LinearLayout) {
            LinearLayout bottomContainer = (LinearLayout) parentLayout.getParent();
            ViewCompat.setOnApplyWindowInsetsListener(bottomContainer, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
                v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    insets.bottom
                );
                return windowInsets;
            });
        }
    }
    
    private void initViews() {
        android.util.Log.d("MasterChef", "initViews - START");
        kitchenView = findViewById(R.id.kitchen_view);
        android.util.Log.d("MasterChef", "initViews - kitchenView=" + (kitchenView != null));
        textRecipeName = findViewById(R.id.text_recipe_name);
        textStepIndicator = findViewById(R.id.text_step_indicator);
        textChefMessage = findViewById(R.id.text_chef_message);
        btnBack = findViewById(R.id.btn_back);
        btnHint = findViewById(R.id.btn_hint);
        btnReplayTts = findViewById(R.id.btn_replay_tts);
        ingredientTray = findViewById(R.id.ingredient_tray);
        actionProgressContainer = findViewById(R.id.action_progress_container);
        progressAction = findViewById(R.id.progress_action);
        textActionHint = findViewById(R.id.text_action_hint);
        
        speakingOverlay = findViewById(R.id.speaking_overlay);
        textSpeakingPhrase = findViewById(R.id.text_speaking_phrase);
        textSpeakingStatus = findViewById(R.id.text_speaking_status);
        btnMic = findViewById(R.id.btn_mic);
        btnHearAgain = findViewById(R.id.btn_hear_again);
        btnSkipSpeaking = findViewById(R.id.btn_skip_speaking);
        
        recipeCompleteOverlay = findViewById(R.id.recipe_complete_overlay);
        textFinalScore = findViewById(R.id.text_final_score);
        textSpeakingStats = findViewById(R.id.text_speaking_stats);
        btnPlayAgain = findViewById(R.id.btn_play_again);
        btnChooseRecipe = findViewById(R.id.btn_choose_recipe);
        btnExitGame = findViewById(R.id.btn_exit_game);
        
        textSuccessMessage = findViewById(R.id.text_success_message);
        
        // Gesture hint overlay
        gestureHintOverlay = findViewById(R.id.gesture_hint_overlay);
        
        kitchenView.setListener(this);
        
        btnBack.setOnClickListener(v -> showExitConfirmDialog());
        btnHint.setOnClickListener(v -> showGestureHint()); // Show gesture hint instead of just audio
        
        // Setup proper back handling with dispatcher
        setupBackHandler();
        btnReplayTts.setOnClickListener(v -> replayChefMessage());
        
        btnMic.setOnClickListener(v -> onMicButtonClick());
        btnHearAgain.setOnClickListener(v -> speakCurrentPhrase());
        btnSkipSpeaking.setOnClickListener(v -> skipSpeakingCheck());
        
        btnPlayAgain.setOnClickListener(v -> restartRecipe());
        btnChooseRecipe.setOnClickListener(v -> navigateBackToDining()); // Go back to dining for new order
        btnExitGame.setOnClickListener(v -> {
            // Clean up and go to game selection
            if (gameEngine != null) gameEngine.cleanup();
            finish();
        });
        
        // Click on gesture hint overlay to dismiss
        if (gestureHintOverlay != null) {
            gestureHintOverlay.setOnClickListener(v -> gestureHintOverlay.hideHint());
        }
    }
    
    private void initServices() {
        ttsService = new ChefTTSService(this);
        ttsService.setListener(ttsListener);
        
        speechService = new SpeechRecognitionService(this);
        speechService.setListener(speechListener);
    }
    
    private void loadRecipe() {
        String recipeId = getIntent().getStringExtra(RecipeSelectActivity.EXTRA_RECIPE_ID);
        
        android.util.Log.d("MasterChef", "loadRecipe - recipeId from intent: " + recipeId);
        
        if (recipeId == null || recipeId.isEmpty()) {
            android.util.Log.w("MasterChef", "No recipe ID in intent, getting first recipe");
            List<Recipe> recipes = repository.getAllRecipes();
            android.util.Log.d("MasterChef", "Available recipes count: " + recipes.size());
            if (!recipes.isEmpty()) {
                currentRecipe = recipes.get(0);
                recipeId = currentRecipe.getId();
            }
        } else {
            currentRecipe = repository.getRecipe(recipeId);
        }
        
        if (currentRecipe == null) {
            android.util.Log.e("MasterChef", "Recipe not found! recipeId=" + recipeId);
            Toast.makeText(this, "Recipe not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        android.util.Log.d("MasterChef", "Recipe loaded: " + currentRecipe.getNameEn() + 
            " Steps: " + currentRecipe.getTotalSteps() +
            " Ingredients: " + (currentRecipe.getAllIngredientIds() != null ? currentRecipe.getAllIngredientIds().size() : 0) +
            " Tools: " + (currentRecipe.getAllToolIds() != null ? currentRecipe.getAllToolIds().size() : 0));
        
        gameEngine = new KitchenGameEngine();
        gameEngine.setListener(this);
        
        textRecipeName.setText(currentRecipe.getNameEn());
        populateIngredientTray();
        gameEngine.startRecipe(recipeId);
    }
    
    private void populateIngredientTray() {
        ingredientTray.removeAllViews();
        
        // If we have a current step with specific inventory, use that
        // Otherwise fall back to showing all recipe items (for initial load)
        RecipeStep firstStep = currentRecipe.getStep(0);
        if (firstStep != null && firstStep.hasInventoryForStep()) {
            updateIngredientTrayForStep(firstStep);
            return;
        }
        
        // Legacy fallback: show all items (for recipes not yet updated)
        List<String> ingredientIds = currentRecipe.getAllIngredientIds();
        List<String> toolIds = currentRecipe.getAllToolIds();
        
        // DEBUG: Log what we're loading
        android.util.Log.d("MasterChef", "populateIngredientTray - Recipe: " + currentRecipe.getNameEn());
        android.util.Log.d("MasterChef", "Ingredient IDs: " + (ingredientIds != null ? ingredientIds.toString() : "null"));
        android.util.Log.d("MasterChef", "Tool IDs: " + (toolIds != null ? toolIds.toString() : "null"));
        
        int itemCount = 0;
        
        if (ingredientIds != null) {
            for (String id : ingredientIds) {
                Ingredient ingredient = repository.getIngredient(id);
                if (ingredient != null) {
                    addTrayItem(ingredient.getId(), ingredient.getNameEn(), ingredient.getDrawableName(), 1);
                    itemCount++;
                } else {
                    android.util.Log.w("MasterChef", "Ingredient not found: " + id);
                }
            }
        }
        
        if (toolIds != null) {
            for (String id : toolIds) {
                // Skip UI control tools (knobs, sliders) - these are not drag-drop items
                if (isUIControlTool(id)) {
                    android.util.Log.d("MasterChef", "Skipping UI control tool: " + id);
                    continue;
                }
                
                Tool tool = repository.getTool(id);
                if (tool != null) {
                    addTrayItem(tool.getId(), tool.getNameEn(), tool.getDrawableName(), 1);
                    itemCount++;
                } else {
                    android.util.Log.w("MasterChef", "Tool not found: " + id);
                }
            }
        }
        
        android.util.Log.d("MasterChef", "Total items added to tray: " + itemCount);
        
        // DEBUG: If no items, show a debug message
        if (itemCount == 0) {
            android.util.Log.e("MasterChef", "NO ITEMS IN TRAY! Recipe may have empty ingredient/tool lists");
            TextView debugText = new TextView(this);
            debugText.setText("⚠️ No items available (debug)");
            debugText.setTextColor(0xFFFF5722);
            debugText.setPadding(16, 16, 16, 16);
            ingredientTray.addView(debugText);
        }
    }
    
    /**
     * Check if a tool ID is a UI control (not a drag-drop item)
     * UI controls like temperature knobs, timers should be rendered as UI elements, not inventory items
     */
    private boolean isUIControlTool(String toolId) {
        return toolId != null && (
            toolId.contains("knob") || 
            toolId.contains("timer") || 
            toolId.contains("slider") ||
            toolId.contains("button")
        );
    }
    
    /**
     * Update the ingredient tray to show only items needed for the current step.
     * This provides a cleaner, less cluttered experience.
     */
    private void updateIngredientTrayForStep(RecipeStep step) {
        if (step == null) return;
        
        ingredientTray.removeAllViews();
        
        List<String> inventoryItems = step.getInventoryForStep();
        
        // If step has no specific inventory, check requiredItems + requiredTool
        if (inventoryItems == null || inventoryItems.isEmpty()) {
            inventoryItems = new java.util.ArrayList<>();
            
            // Add required items
            if (step.getRequiredItems() != null) {
                inventoryItems.addAll(step.getRequiredItems());
            }
            
            // Add required tool (if not a UI control)
            String tool = step.getRequiredTool();
            if (tool != null && !isUIControlTool(tool)) {
                inventoryItems.add(tool);
            }
        }
        
        android.util.Log.d("MasterChef", "updateIngredientTrayForStep - Step " + step.getStepNumber() + 
            " items: " + inventoryItems);
        
        int itemCount = 0;
        
        for (String itemSpec : inventoryItems) {
            // Parse item spec: "itemId" or "itemId:count"
            String itemId = itemSpec;
            int count = 1;
            
            if (itemSpec.contains(":")) {
                String[] parts = itemSpec.split(":");
                itemId = parts[0];
                try {
                    count = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    count = 1;
                }
            }
            
            // Skip UI control tools
            if (isUIControlTool(itemId)) {
                continue;
            }
            
            // Try to find as ingredient first
            Ingredient ingredient = repository.getIngredient(itemId);
            if (ingredient != null) {
                addTrayItem(ingredient.getId(), ingredient.getNameEn(), ingredient.getDrawableName(), count);
                itemCount++;
                continue;
            }
            
            // Try to find as tool
            Tool tool = repository.getTool(itemId);
            if (tool != null) {
                addTrayItem(tool.getId(), tool.getNameEn(), tool.getDrawableName(), count);
                itemCount++;
                continue;
            }
            
            android.util.Log.w("MasterChef", "Item not found: " + itemId);
        }
        
        // If no items for this step, show a hint
        if (itemCount == 0) {
            // This step might be gesture-only (no items needed)
            android.util.Log.d("MasterChef", "Step " + step.getStepNumber() + " has no inventory items (gesture-only step)");
        }
    }
    
    private void addTrayItem(String id, String name, String drawableName, int count) {
        DraggableItemView itemView = new DraggableItemView(this);
        
        try {
            int drawableId = getResources().getIdentifier(drawableName, "drawable", getPackageName());
            if (drawableId != 0) {
                itemView.setItem(id, name, drawableId);
            } else {
                // Fallback: set item with default drawable
                itemView.setItem(id, name, android.R.drawable.ic_menu_gallery);
            }
        } catch (Exception e) {
            // Fallback: set item even if drawable fails
            itemView.setItem(id, name, android.R.drawable.ic_menu_gallery);
        }
        
        // Show stack count if more than 1
        if (count > 1) {
            itemView.setStackCount(count);
        }
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            (int) (80 * getResources().getDisplayMetrics().density),
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);
        itemView.setLayoutParams(params);
        ingredientTray.addView(itemView);
    }
    
    // ========== GameEventListener ==========
    
    @Override
    public void onStepChanged(RecipeStep step, int stepIndex, int totalSteps) {
        textStepIndicator.setText(String.format(Locale.US, "Step %d/%d", stepIndex + 1, totalSteps));
        if (step.getTargetZone() != null) {
            kitchenView.highlightZone(step.getTargetZone());
        }
        updateActionUI(step);
        
        // Update ingredient tray to show only items needed for this step
        updateIngredientTrayForStep(step);
    }
    
    @Override
    public void onStepStateChanged(StepState state) {
        switch (state) {
            case WAITING_ITEM:
                actionProgressContainer.setVisibility(View.GONE);
                break;
            case READY_ACTION:
            case ACTION_IN_PROGRESS:
                actionProgressContainer.setVisibility(View.VISIBLE);
                break;
            case STEP_DONE:
                actionProgressContainer.setVisibility(View.GONE);
                kitchenView.clearHighlight();
                break;
        }
    }
    
    @Override
    public void onActionProgress(ActionType action, float progress) {
        progressAction.setProgress((int) (progress * 100));
    }
    
    @Override
    public void onActionComplete(ActionType action) {
        actionProgressContainer.setVisibility(View.GONE);
    }
    
    @Override
    public void onItemPlaced(String itemId, ZoneType zone) {}
    
    @Override
    public void onStepSuccess(String successText) {
        showSuccessMessage(successText);
    }
    
    @Override
    public void onSpeakingRequired(String phrase) {
        showSpeakingPractice(phrase);
    }
    
    @Override
    public void onSpeakingResult(boolean correct, String phrase) {}
    
    @Override
    public void onRecipeComplete(int score) {
        showRecipeComplete(score);
    }
    
    @Override
    public void onChefSpeak(String text) {
        textChefMessage.setText(text);
        ttsService.speak(text);
    }
    
    @Override
    public void onHintRequested(String hint) {
        Toast.makeText(this, hint, Toast.LENGTH_LONG).show();
        ttsService.speak(hint);
    }
    
    @Override
    public void onCookingTimer(long remaining) {
        float progress = 1f - (remaining / 5000f);
        progressAction.setProgress((int) (progress * 100));
    }
    
    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    // ========== KitchenView.KitchenEventListener ==========
    
    @Override
    public void onItemDropped(String itemId, ZoneType fromZone, ZoneType toZone) {
        // Always reset draggable items first
        resetDraggableItems();
        
        if (toZone == null) {
            // Dropped outside any zone - animate error shake
            showDropError("Drop on a zone!");
            animateFailedDrop();
            return;
        }
        
        if (gameEngine != null) {
            boolean success = gameEngine.onDropItem(itemId, toZone);
            if (!success) {
                showDropError("Try a different zone!");
                animateFailedDrop();
            } else {
                // Success! Give visual feedback
                showDropSuccess(toZone);
                
                // *** PERSIST ITEM IN ZONE ***
                // Find the drawable for this item and add it to KitchenView
                addItemToKitchenZone(itemId, toZone);
            }
        }
    }
    
    /**
     * Add dropped item to KitchenView zone for persistent rendering
     */
    private void addItemToKitchenZone(String itemId, ZoneType zone) {
        if (kitchenView == null) return;
        
        // Try to find drawable from ingredientTray
        android.graphics.drawable.Drawable drawable = findItemDrawable(itemId);
        
        if (drawable != null) {
            // Clone drawable to avoid state sharing
            android.graphics.drawable.Drawable drawableClone = drawable.getConstantState() != null 
                ? drawable.getConstantState().newDrawable().mutate() 
                : drawable;
            kitchenView.addItemToZone(itemId, itemId, drawableClone, zone);
        } else {
            // Fallback: add with null drawable (will show placeholder)
            kitchenView.addItemToZone(itemId, itemId, null, zone);
        }
    }
    
    /**
     * Find drawable for an item by its ID
     */
    private android.graphics.drawable.Drawable findItemDrawable(String itemId) {
        // Check tray items first (most reliable)
        for (int i = 0; i < ingredientTray.getChildCount(); i++) {
            View child = ingredientTray.getChildAt(i);
            if (child instanceof DraggableItemView) {
                DraggableItemView draggable = (DraggableItemView) child;
                if (itemId.equals(draggable.getItemId())) {
                    return draggable.getItemDrawable();
                }
            }
        }
        
        // Fallback: try to get from repository using drawableName
        try {
            Ingredient ingredient = repository.getIngredient(itemId);
            if (ingredient != null && ingredient.getDrawableName() != null) {
                int resId = getResources().getIdentifier(
                    ingredient.getDrawableName(), "drawable", getPackageName());
                if (resId != 0) {
                    return ContextCompat.getDrawable(this, resId);
                }
            }
            Tool tool = repository.getTool(itemId);
            if (tool != null && tool.getDrawableName() != null) {
                int resId = getResources().getIdentifier(
                    tool.getDrawableName(), "drawable", getPackageName());
                if (resId != 0) {
                    return ContextCompat.getDrawable(this, resId);
                }
            }
        } catch (Exception e) {
            android.util.Log.w("MasterChef", "Could not find drawable for: " + itemId);
        }
        
        return null;
    }
    
    private void resetDraggableItems() {
        for (int i = 0; i < ingredientTray.getChildCount(); i++) {
            View child = ingredientTray.getChildAt(i);
            if (child instanceof DraggableItemView) {
                ((DraggableItemView) child).onDragEnded();
            }
        }
    }
    
    private void animateFailedDrop() {
        // Shake the tray briefly to indicate failed drop
        if (ingredientTray != null) {
            ingredientTray.animate()
                .translationX(10f)
                .setDuration(50)
                .withEndAction(() -> {
                    ingredientTray.animate()
                        .translationX(-10f)
                        .setDuration(50)
                        .withEndAction(() -> {
                            ingredientTray.animate()
                                .translationX(0f)
                                .setDuration(50)
                                .start();
                        })
                        .start();
                })
                .start();
        }
    }
    
    private void showDropSuccess(ZoneType zone) {
        // Quick flash effect on kitchen view
        if (kitchenView != null) {
            kitchenView.highlightZone(zone);
            kitchenView.postDelayed(() -> kitchenView.clearHighlight(), 300);
        }
    }
    
    private void showDropError(String message) {
        Toast.makeText(this, "❌ " + message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onZoneTapped(ZoneType zone) {
        if (gameEngine != null) {
            gameEngine.onTap();
        }
    }
    
    @Override
    public void onZoneSwiped(ZoneType zone, float velocityX, float velocityY) {
        if (gameEngine != null) {
            gameEngine.onSwipe();
        }
    }
    
    @Override
    public void onZoneLongPressed(ZoneType zone) {
        android.util.Log.d("MasterChef", "onZoneLongPressed: zone=" + zone);
        // Long press handling depends on action type
        if (gameEngine != null) {
            RecipeStep step = gameEngine.getState().getCurrentStep();
            if (step != null) {
                ActionType action = step.getActionType();
                android.util.Log.d("MasterChef", "onZoneLongPressed: action=" + action + 
                    ", isHoldAction=" + (action != null ? action.isHoldAction() : "null"));
                // Only call onHoldStart for actual hold actions
                // POUR is handled by KitchenView's pourAnimator
                if (action != null && action.isHoldAction()) {
                    android.util.Log.d("MasterChef", "onZoneLongPressed: Calling gameEngine.onHoldStart()");
                    gameEngine.onHoldStart();
                }
                // POUR action doesn't need onHoldStart - it uses pourAnimator
                // and notifies via onPourProgress/onPourComplete
            } else {
                android.util.Log.d("MasterChef", "onZoneLongPressed: No current step!");
            }
        } else {
            android.util.Log.d("MasterChef", "onZoneLongPressed: gameEngine is null!");
        }
    }
    
    // ========== ENHANCED GESTURE CALLBACKS ==========
    
    @Override
    public void onStirProgress(ZoneType zone, int stirCount, float progress) {
        // Update progress bar
        progressAction.setProgress((int) (progress * 100));
        textActionHint.setText("Stirring... " + (int)(progress * 100) + "%");
        
        // Notify game engine
        if (gameEngine != null) {
            gameEngine.onSwipe(); // Each stir counts as a swipe
        }
    }
    
    @Override
    public void onStirComplete(ZoneType zone, int totalStirs) {
        textActionHint.setText("Perfect stirring! ✨");
    }
    
    @Override
    public void onShakeProgress(ZoneType zone, int shakeCount, float progress) {
        progressAction.setProgress((int) (progress * 100));
        textActionHint.setText("Shaking... " + shakeCount + " shakes!");
        
        if (gameEngine != null) {
            gameEngine.onSwipe(); // Each shake counts as action
        }
    }
    
    @Override
    public void onShakeComplete(ZoneType zone, int totalShakes) {
        textActionHint.setText("Great shaking! 🍳");
    }
    
    @Override
    public void onPourProgress(ZoneType zone, float progress) {
        progressAction.setProgress((int) (progress * 100));
        textActionHint.setText("Pouring... " + (int)(progress * 100) + "%");
    }
    
    @Override
    public void onPourComplete(ZoneType zone, long duration) {
        textActionHint.setText("Perfect pour! 💧");
        if (gameEngine != null) {
            gameEngine.onPourComplete();
        }
    }
    
    @Override
    public void onTapProgress(ZoneType zone, int tapCount, float progress) {
        progressAction.setProgress((int) (progress * 100));
        textActionHint.setText("Cutting... " + tapCount + " taps!");
    }
    
    @Override
    public void onTapComplete(ZoneType zone, int totalTaps) {
        textActionHint.setText("Nicely chopped! 🔪");
    }
    
    @Override
    public void onHoldReleased(ZoneType zone) {
        // CRITICAL: Stop hold timer in game engine when finger is released
        // This prevents black screen caused by runaway timers
        if (gameEngine != null) {
            gameEngine.onHoldEnd();
        }
    }
    
    @Override
    public void onItemPlacedInZone(String itemId, ZoneType zone, android.graphics.drawable.Drawable drawable) {
        // Item was placed in zone - already handled by KitchenView
        // Game engine will track this through onItemDropped
    }
    
    // ========== UI Helpers ==========
    
    private void updateActionUI(RecipeStep step) {
        ActionType action = step.getActionType();
        if (action == null) return;
        
        String hint;
        int requiredCount = 0;
        long duration = 0;
        
        switch (action) {
            case TAP_TO_CUT:
                requiredCount = step.getTapCount() > 0 ? step.getTapCount() : 5;
                hint = "Tap " + requiredCount + " times to cut! 🔪";
                break;
            case TAP_TO_POUND:
                requiredCount = step.getTapCount() > 0 ? step.getTapCount() : 3;
                hint = "Tap " + requiredCount + " times to pound! 🥊";
                break;
            case SWIPE_TO_STIR:
                requiredCount = step.getStirCount() > 0 ? step.getStirCount() : 5;
                hint = "Stir in circles " + requiredCount + " times! 🥄";
                break;
            case SHAKE_PAN:
            case SHAKE_BASKET:
                requiredCount = step.getShakeCount() > 0 ? step.getShakeCount() : 4;
                hint = "Shake left-right " + requiredCount + " times! 🍳";
                break;
            case WAIT_COOK:
                duration = step.getCookTime() > 0 ? step.getCookTime() : 3000;
                hint = "Wait for cooking... ⏳";
                break;
            case POUR:
                duration = step.getPourTime() > 0 ? step.getPourTime() : 2000;
                hint = "Hold to pour! 💧";
                break;
            case SEASON_SHAKE:
                requiredCount = 3;
                hint = "Shake to season! 🧂";
                break;
            // ========== HOLD ACTIONS ==========
            case OVEN_PREHEAT_HOLD:
                duration = step.getHoldMs() > 0 ? step.getHoldMs() : 1500;
                hint = "🖐️ Hold the button to preheat oven! 🔥";
                break;
            case GRILL_ON_HOLD:
                duration = step.getHoldMs() > 0 ? step.getHoldMs() : 1500;
                hint = "🖐️ Hold to turn on the grill! 🥩";
                break;
            // ========== WAIT ACTIONS ==========
            case STEAM_START:
                duration = step.getWarmUpMs() > 0 ? step.getWarmUpMs() : 3000;
                hint = "Wait for steam... ♨️";
                break;
            case WAIT_BOIL:
            case WAIT_OIL_HEAT:
            case WAIT_SEAR:
                duration = step.getCookTime() > 0 ? step.getCookTime() : 3000;
                hint = "Wait for heating... ⏳";
                break;
            // ========== ROTARY ACTIONS ==========
            case ROTATE_KNOB_TEMP:
                hint = "Turn the knob to set temperature! 🌡️";
                break;
            case ROTATE_KNOB_TIMER:
                hint = "Turn the knob to set timer! ⏱️";
                break;
            // ========== SWIPE ACTIONS ==========
            case GRILL_FLIP_SWIPE:
                requiredCount = 1;
                hint = "Swipe to flip! 🍖";
                break;
            case WIPE_FOG_SWIPE:
                requiredCount = step.getSwipeCount() > 0 ? step.getSwipeCount() : 6;
                hint = "Swipe to wipe fog! " + requiredCount + " times! 💨";
                break;
            case SKIM_FOAM_SWIPE:
                requiredCount = 3;
                hint = "Swipe to skim foam! 🥄";
                break;
            // ========== OTHER ACTIONS ==========
            case TIMING_STOP:
                hint = "Tap at the right moment! ⏰";
                break;
            case WHISK_CIRCLES:
                requiredCount = 5;
                hint = "Whisk in circles! 🥚";
                break;
            case KNEAD_DRAG:
                requiredCount = 5;
                hint = "Drag to knead! 🍞";
                break;
            case TAP_TO_CRACK:
                requiredCount = 2;
                hint = "Tap to crack! 🥚";
                break;
            case GARNISH_DRAG_SNAP:
                hint = "Place items on the plate! 🍽️";
                break;
            case TAP_TO_DRIZZLE:
                requiredCount = 3;
                hint = "Tap to drizzle! 🍯";
                break;
            case OIL_LEVEL_SLIDER:
            case HEAT_LEVEL_SLIDER:
                hint = "Slide to adjust! 📊";
                break;
            case SERVE:
                hint = "Serve the dish! 🍽️";
                break;
            case DRAG_TO_ZONE:
                hint = "Drag to the right zone! 👆";
                break;
            default:
                hint = "Follow the instructions";
        }
        textActionHint.setText(hint);
        
        // Configure KitchenView for this action
        if (kitchenView != null) {
            kitchenView.setCurrentAction(action, step.getTargetZone(), requiredCount, duration);
        }
    }
    
    private void showSuccessMessage(String message) {
        textSuccessMessage.setText(message != null ? message : "Great job! ⭐");
        textSuccessMessage.setVisibility(View.VISIBLE);
        
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        textSuccessMessage.startAnimation(fadeIn);
        
        handler.postDelayed(() -> {
            Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
            textSuccessMessage.startAnimation(fadeOut);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    textSuccessMessage.setVisibility(View.GONE);
                }
                @Override public void onAnimationRepeat(Animation animation) {}
            });
        }, 1200);
    }
    
    private void showSpeakingPractice(String phrase) {
        isSpeakingCheckActive = true;
        speakingTotalCount++;
        currentSpeakingPhrase = phrase;
        
        textSpeakingPhrase.setText(phrase);
        textSpeakingStatus.setText("Tap the microphone and speak!");
        speakingOverlay.setVisibility(View.VISIBLE);
        speakCurrentPhrase();
    }
    
    private void speakCurrentPhrase() {
        ttsService.speakPhraseForRepeat(currentSpeakingPhrase);
    }
    
    private void onMicButtonClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                == PackageManager.PERMISSION_GRANTED) {
            if (!speechService.isAvailable()) {
                speechService.initialize();
            }
            startSpeechRecognition();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }
    
    private void startSpeechRecognition() {
        btnMic.setEnabled(false);
        textSpeakingStatus.setText("Listening...");
        speechService.startListening(currentSpeakingPhrase);
    }
    
    private void skipSpeakingCheck() {
        closeSpeakingOverlay();
    }
    
    private void closeSpeakingOverlay() {
        isSpeakingCheckActive = false;
        speakingOverlay.setVisibility(View.GONE);
        if (gameEngine != null) {
            gameEngine.onSpeakingCheckDone(true);
        }
    }
    
    private void showRecipeComplete(int score) {
        textFinalScore.setText("Score: " + score + " ⭐");
        textSpeakingStats.setText("Speaking: " + speakingCorrectCount + "/" + speakingTotalCount + " correct");
        recipeCompleteOverlay.setVisibility(View.VISIBLE);
    }
    
    private void restartRecipe() {
        recipeCompleteOverlay.setVisibility(View.GONE);
        speakingCorrectCount = 0;
        speakingTotalCount = 0;
        kitchenView.clearAllZones();
        
        gameEngine = new KitchenGameEngine();
        gameEngine.setListener(this);
        gameEngine.startRecipe(currentRecipe.getId());
    }
    
    private void requestHint() {
        if (gameEngine != null) {
            gameEngine.requestHint();
        }
    }
    
    /**
     * Show animated gesture hint overlay for current action
     */
    private void showGestureHint() {
        if (gestureHintOverlay == null || gameEngine == null) {
            // Fallback to audio hint
            requestHint();
            return;
        }
        
        RecipeStep currentStep = gameEngine.getState().getCurrentStep();
        if (currentStep == null || currentStep.getActionType() == null) {
            requestHint();
            return;
        }
        
        // Show visual gesture hint
        gestureHintOverlay.showHint(currentStep.getActionType());
        
        // Also play audio hint
        requestHint();
        
        // Auto-hide after 3 seconds
        handler.postDelayed(() -> {
            if (gestureHintOverlay != null) {
                gestureHintOverlay.hideHint();
            }
        }, 3000);
    }
    
    private void replayChefMessage() {
        String message = textChefMessage.getText().toString();
        if (!message.isEmpty()) {
            ttsService.speak(message);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // CRITICAL: Stop hold timers when app goes to background
        // This prevents black screen caused by timers running while paused
        if (gameEngine != null) {
            gameEngine.onHoldEnd(); // Stop any ongoing hold
            gameEngine.pauseGame(); // Pause game state
        }
        // Stop pouring in KitchenView
        if (kitchenView != null) {
            kitchenView.stopPouring();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Resume game when returning from background
        if (gameEngine != null && currentRecipe != null) {
            gameEngine.resumeGame();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup all timers
        if (gameEngine != null) {
            gameEngine.onHoldEnd();
        }
        if (ttsService != null) ttsService.shutdown();
        if (speechService != null) speechService.destroy();
        handler.removeCallbacksAndMessages(null);
    }
    
    /**
     * Setup proper back handling using OnBackPressedDispatcher (not deprecated)
     */
    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSpeakingCheckActive) {
                    skipSpeakingCheck();
                } else if (recipeCompleteOverlay.getVisibility() == View.VISIBLE) {
                    navigateBackToDining();
                } else {
                    // Show confirm dialog when cooking
                    showExitConfirmDialog();
                }
            }
        });
    }
    
    /**
     * Show confirmation dialog before exiting kitchen
     */
    private void showExitConfirmDialog() {
        // Stop any playing TTS
        if (ttsService != null) {
            ttsService.stop();
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Exit Kitchen?")
            .setMessage("Are you sure you want to leave? Your cooking progress will be lost.")
            .setPositiveButton("Yes, Exit", (dialog, which) -> {
                navigateBackToDining();
            })
            .setNegativeButton("Keep Cooking", (dialog, which) -> {
                dialog.dismiss();
            })
            .setCancelable(true)
            .show();
    }
    
    /**
     * Navigate back to dining room (OrderSelectActivity) with result
     */
    private void navigateBackToDining() {
        // Clean up resources
        if (gameEngine != null) {
            gameEngine.cleanup();
        }
        
        // Navigate back to dining room for new order
        Intent intent = new Intent(this, OrderSelectActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // Smooth slide transition back to dining room
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}
