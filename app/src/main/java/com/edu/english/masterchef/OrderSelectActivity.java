package com.edu.english.masterchef;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;
import com.edu.english.masterchef.data.ChefCharacter;
import com.edu.english.masterchef.data.CustomerOrder;
import com.edu.english.masterchef.data.MasterChefRepository;
import com.edu.english.masterchef.data.Recipe;
import com.edu.english.masterchef.services.ChefTTSService;
import com.edu.english.masterchef.views.ChefBubbleView;
import com.edu.english.masterchef.views.OrderCardView;

/**
 * Activity that shows a customer order before starting the cooking game.
 * Flow: Customer arrives -> Shows order -> User taps "Start Cooking" -> Goes to MasterChefActivity
 */
public class OrderSelectActivity extends AppCompatActivity {

    private static final String TAG = "OrderSelectActivity";
    
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_CHEF_ID = "chef_id";

    // Views
    private ChefBubbleView chefBubbleView;
    private OrderCardView orderCardView;
    private ImageView customerAvatar;
    private ImageView imgChefFull;
    private TextView textChefSpeech;
    private FrameLayout customerAvatarContainer;
    private FrameLayout customerContainer;
    private TextView textCustomerName;
    private TextView textRecipeInfo;
    private TextView textCustomerSpeech;
    private LinearLayout chefSelectContainer;
    private CardView orderCard;
    private View btnStartCooking;
    private View btnNewOrder;
    private ImageButton btnBack;

    // Data
    private MasterChefRepository repository;
    private ChefTTSService ttsService;
    private CustomerOrder currentOrder;
    private ChefCharacter currentChef;
    private Recipe currentRecipe;
    private Handler handler;

    private final ChefTTSService.TTSListener ttsListener = new ChefTTSService.TTSListener() {
        @Override public void onSpeakStart(String text) {}
        @Override public void onSpeakDone(String text) {}
        @Override public void onTTSReady() {
            // Greet when TTS is ready
            if (currentChef != null) {
                ttsService.speak(currentChef.getGreetingEn());
            }
        }
        @Override public void onTTSError(String error) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_order_select);

        handler = new Handler(Looper.getMainLooper());
        repository = MasterChefRepository.getInstance();
        
        initViews();
        setupWindowInsets();
        initTTS();
        loadChefAndOrder();
    }
    
    /**
     * Setup window insets to handle notch/cutout and navigation bar properly
     */
    private void setupWindowInsets() {
        View rootView = findViewById(android.R.id.content);
        LinearLayout topBar = findViewById(R.id.top_bar);
        View buttonContainer = findViewById(R.id.button_container);
        
        if (topBar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars() | 
                                                        WindowInsetsCompat.Type.displayCutout());
                // Add padding to top bar to avoid notch/cutout
                v.setPadding(
                    v.getPaddingLeft(),
                    insets.top + 12, // 12dp original padding
                    v.getPaddingRight(),
                    v.getPaddingBottom()
                );
                return windowInsets;
            });
        }
        
        if (buttonContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(buttonContainer, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
                // Add padding to bottom buttons to avoid navigation bar
                v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    insets.bottom + 12 // 12dp original padding
                );
                return windowInsets;
            });
        }
    }

    private void initViews() {
        chefBubbleView = findViewById(R.id.chef_bubble_view);
        orderCardView = findViewById(R.id.order_card_view);
        customerAvatar = findViewById(R.id.customer_avatar);
        imgChefFull = findViewById(R.id.img_chef_full);
        textChefSpeech = findViewById(R.id.text_chef_speech);
        customerAvatarContainer = findViewById(R.id.customer_avatar_container);
        customerContainer = findViewById(R.id.customer_container);
        textCustomerName = findViewById(R.id.text_customer_name);
        textRecipeInfo = findViewById(R.id.text_recipe_info);
        textCustomerSpeech = findViewById(R.id.text_customer_speech);
        chefSelectContainer = findViewById(R.id.chef_select_container);
        orderCard = findViewById(R.id.order_card);
        btnStartCooking = findViewById(R.id.btn_start_cooking);
        btnNewOrder = findViewById(R.id.btn_new_order);
        btnBack = findViewById(R.id.btn_back);

        btnStartCooking.setOnClickListener(v -> startCooking());
        btnNewOrder.setOnClickListener(v -> loadNewOrder());
        btnBack.setOnClickListener(v -> finish());
        
        // Setup proper back handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Normal back - just finish and go to previous screen
                finish();
            }
        });

        // Setup chef selection
        setupChefSelection();
        
        // Debug: Log view initialization
        Log.d(TAG, "Views initialized - orderCard: " + (orderCard != null) + 
              ", customerAvatar: " + (customerAvatar != null) +
              ", textCustomerSpeech: " + (textCustomerSpeech != null));
    }

    private void initTTS() {
        ttsService = new ChefTTSService(this);
        ttsService.setListener(ttsListener);
    }

    private void setupChefSelection() {
        chefSelectContainer.removeAllViews();
        
        for (ChefCharacter chef : repository.getUnlockedChefs()) {
            ImageView chefIcon = new ImageView(this);
            int resId = getResources().getIdentifier(
                chef.getAvatarDrawable(), "drawable", getPackageName()
            );
            if (resId != 0) {
                chefIcon.setImageResource(resId);
            }
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (64 * getResources().getDisplayMetrics().density),
                (int) (64 * getResources().getDisplayMetrics().density)
            );
            params.setMargins(8, 0, 8, 0);
            chefIcon.setLayoutParams(params);
            chefIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            chefIcon.setBackgroundResource(R.drawable.bg_circle_button);
            chefIcon.setPadding(8, 8, 8, 8);
            
            final ChefCharacter selectedChef = chef;
            chefIcon.setOnClickListener(v -> {
                selectChef(selectedChef);
                highlightSelectedChef(v);
            });
            
            chefSelectContainer.addView(chefIcon);
        }
    }

    private void highlightSelectedChef(View selected) {
        for (int i = 0; i < chefSelectContainer.getChildCount(); i++) {
            View child = chefSelectContainer.getChildAt(i);
            child.setAlpha(child == selected ? 1.0f : 0.5f);
            child.setScaleX(child == selected ? 1.1f : 1.0f);
            child.setScaleY(child == selected ? 1.1f : 1.0f);
        }
    }

    private void selectChef(ChefCharacter chef) {
        currentChef = chef;
        
        // Update chef full body image based on selection
        String chefId = chef.getId();
        int fullBodyRes = 0;
        if (chefId != null) {
            if (chefId.contains("bunny") || chefId.contains("rabbit")) {
                fullBodyRes = R.drawable.ic_chef_bunny_full;
            } else if (chefId.contains("panda")) {
                fullBodyRes = R.drawable.ic_chef_panda_full;
            } else {
                // Try to find full body drawable
                fullBodyRes = getResources().getIdentifier(
                    chef.getAvatarDrawable() + "_full", "drawable", getPackageName()
                );
            }
        }
        
        if (fullBodyRes != 0 && imgChefFull != null) {
            imgChefFull.setImageResource(fullBodyRes);
            // Animate chef entrance
            imgChefFull.setAlpha(0f);
            imgChefFull.animate().alpha(1f).setDuration(300).start();
        }
        
        // Update chef speech bubble
        if (textChefSpeech != null) {
            textChefSpeech.setText(chef.getGreetingEn());
        }
        
        // Update chef bubble (hidden but for compatibility)
        int avatarRes = getResources().getIdentifier(
            chef.getAvatarDrawable(), "drawable", getPackageName()
        );
        if (avatarRes != 0 && chefBubbleView != null) {
            chefBubbleView.setChefAvatar(avatarRes);
        }
        
        // Update TTS settings
        ttsService.setVoiceConfig(chef.getSpeechRate(), chef.getPitch());
        
        // Say greeting
        if (chefBubbleView != null) {
            chefBubbleView.setMessageAnimated(chef.getGreetingEn());
        }
        ttsService.speak(chef.getGreetingEn());
    }

    private void loadChefAndOrder() {
        Log.d(TAG, "loadChefAndOrder called");
        
        // Load default chef
        currentChef = repository.getDefaultChef();
        if (currentChef != null) {
            Log.d(TAG, "Default chef loaded: " + currentChef.getDisplayName());
            selectChef(currentChef);
            // Highlight first chef
            if (chefSelectContainer.getChildCount() > 0) {
                highlightSelectedChef(chefSelectContainer.getChildAt(0));
            }
        } else {
            Log.e(TAG, "No default chef found!");
        }

        // Check if specific recipe was requested
        String recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        if (recipeId != null && !recipeId.isEmpty()) {
            Log.d(TAG, "Specific recipe requested: " + recipeId);
            currentOrder = repository.getOrderForRecipe(recipeId);
        }
        
        if (currentOrder == null) {
            // Get random order
            Log.d(TAG, "Getting random order...");
            currentOrder = repository.getRandomOrder();
        }
        
        if (currentOrder != null) {
            Log.d(TAG, "Order loaded: " + currentOrder.getOrderId() + " for " + currentOrder.getCustomerName());
        } else {
            Log.e(TAG, "Failed to get any order from repository!");
        }

        showOrder();
    }

    private void loadNewOrder() {
        Log.d(TAG, "loadNewOrder called - getting new random order");
        currentOrder = repository.getRandomOrder();
        
        // Animate customer exit then enter (use customerContainer for full body)
        View animTarget = customerContainer != null ? customerContainer : customerAvatarContainer;
        if (animTarget != null) {
            Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    showOrder();
                }
            });
            animTarget.startAnimation(slideOut);
        } else {
            showOrder();
        }
        
        // Chef comments on new order
        if (currentChef != null) {
            String comment = "New customer! Let's see what they want!";
            if (textChefSpeech != null) {
                textChefSpeech.setText(comment);
            }
            if (chefBubbleView != null) {
                chefBubbleView.setMessageAnimated(comment);
            }
            ttsService.speak(comment);
        }
    }

    private void showOrder() {
        if (currentOrder == null) {
            Log.w(TAG, "showOrder called but currentOrder is null!");
            textCustomerName.setText("No customer yet");
            textRecipeInfo.setText("Tap 'New Customer' to get a customer");
            if (textCustomerSpeech != null) {
                textCustomerSpeech.setText("Waiting for customer...");
            }
            return;
        }
        
        Log.d(TAG, "Showing order: " + currentOrder.getOrderId() + 
              " for customer: " + currentOrder.getCustomerName());

        // Load customer full body avatar
        String avatarDrawable = currentOrder.getCustomerAvatarDrawable();
        int customerFullRes = getResources().getIdentifier(
            avatarDrawable + "_full", "drawable", getPackageName()
        );
        
        // Fallback to regular avatar if full body not found
        if (customerFullRes == 0) {
            customerFullRes = getResources().getIdentifier(
                avatarDrawable, "drawable", getPackageName()
            );
        }
        
        if (customerFullRes != 0) {
            customerAvatar.setImageResource(customerFullRes);
        } else {
            Log.w(TAG, "Customer avatar not found: " + avatarDrawable);
            customerAvatar.setImageResource(R.drawable.ic_customer_rabbit_full);
        }
        
        // Animate customer entrance
        View animTarget = customerContainer != null ? customerContainer : customerAvatarContainer;
        if (animTarget != null) {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
            animTarget.startAnimation(slideIn);
        }

        textCustomerName.setText(currentOrder.getCustomerName());
        
        // Set customer speech bubble text
        if (textCustomerSpeech != null) {
            textCustomerSpeech.setText(currentOrder.getIntroTextEn());
        }

        // Load recipe info
        currentRecipe = repository.getRecipe(currentOrder.getRecipeId());
        if (currentRecipe != null) {
            textRecipeInfo.setText("Order: " + currentRecipe.getNameEn());
            Log.d(TAG, "Recipe loaded: " + currentRecipe.getNameEn() + 
                  " with " + currentRecipe.getAllIngredientIds().size() + " ingredients");
        } else {
            Log.e(TAG, "Recipe not found for order: " + currentOrder.getRecipeId());
            textRecipeInfo.setText("Recipe not found");
        }

        // Show order with animation
        if (orderCardView != null) {
            orderCardView.setOrderAnimated(currentOrder);
        }
        
        // Animate the whole order card (hidden for compatibility)
        if (orderCard != null && orderCard.getVisibility() == View.VISIBLE) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            fadeIn.setDuration(500);
            orderCard.startAnimation(fadeIn);
        }
        
        // TTS reads the order
        handler.postDelayed(() -> {
            ttsService.speak(currentOrder.getIntroTextEn());
        }, 500);

        // Chef responds after order
        handler.postDelayed(() -> {
            if (currentChef != null) {
                String response = "Alright! Let's cook " + (currentRecipe != null ? currentRecipe.getNameEn() : "this dish") + "!";
                if (textChefSpeech != null) {
                    textChefSpeech.setText(response);
                }
                if (chefBubbleView != null) {
                    chefBubbleView.setMessageAnimated(response);
                }
            }
        }, 3000);
    }

    private void startCooking() {
        if (currentOrder == null || currentRecipe == null) return;

        Intent intent = new Intent(this, MasterChefActivity.class);
        intent.putExtra(RecipeSelectActivity.EXTRA_RECIPE_ID, currentRecipe.getId());
        intent.putExtra(EXTRA_ORDER_ID, currentOrder.getOrderId());
        if (currentChef != null) {
            intent.putExtra(EXTRA_CHEF_ID, currentChef.getId());
        }
        startActivity(intent);
        // Smooth slide transition to kitchen
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsService != null) {
            ttsService.shutdown();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
