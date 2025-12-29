package com.edu.english.masterchef.ui.cookbook;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.edu.english.R;
import com.edu.english.masterchef.data.model.Food;
import com.edu.english.masterchef.data.model.Ingredient;
import com.edu.english.masterchef.data.repository.LevelRepository;
import com.edu.english.masterchef.data.repository.ProgressRepository;
import com.edu.english.masterchef.util.AnimationHelper;
import com.edu.english.masterchef.util.SoundPoolManager;
import com.edu.english.masterchef.util.TTSManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Cookbook Activity - Shows all unlocked dishes with ViewPager2
 * Child-friendly UI with swipeable recipe cards and TTS support
 */
public class CookbookActivity extends AppCompatActivity implements CookbookAdapter.OnDishInteractionListener {

    private ViewPager2 viewPagerDishes;
    private LinearLayout pageIndicator;
    private LinearLayout emptyState;
    private MaterialButton btnPrevious;
    private MaterialButton btnNext;
    private MaterialButton btnGridView;
    private MaterialButton btnPlayNow;
    private TextView tvRecipeCount;

    private CookbookAdapter adapter;
    private TTSManager ttsManager;
    private SoundPoolManager soundManager;
    private List<Food> unlockedDishes;

    // Indicator dots
    private ImageView[] indicatorDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookbook);

        // Handle notch/cutout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize managers
        ttsManager = TTSManager.getInstance(this);
        soundManager = SoundPoolManager.getInstance(this);

        // Find views
        findViews();

        // Setup back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            soundManager.play(SoundPoolManager.SOUND_CLICK);
            finish();
        });

        // Load unlocked dishes
        loadUnlockedDishes();

        // Setup ViewPager2
        setupViewPager();

        // Setup navigation buttons
        setupNavigationButtons();

        // Update UI based on dishes count
        updateUI();
    }

    private void findViews() {
        viewPagerDishes = findViewById(R.id.viewpager_dishes);
        pageIndicator = findViewById(R.id.page_indicator);
        emptyState = findViewById(R.id.empty_state);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnGridView = findViewById(R.id.btn_grid_view);
        btnPlayNow = findViewById(R.id.btn_play_now);
        tvRecipeCount = findViewById(R.id.tv_recipe_count);
    }

    private void loadUnlockedDishes() {
        unlockedDishes = new ArrayList<>();
        ProgressRepository progressRepo = ProgressRepository.getInstance(this);
        
        // Get all completed dishes
        List<String> completedDishIds = progressRepo.getCompletedDishes();
        
        // For each completed dish, get Food data
        for (String dishId : completedDishIds) {
            Food food = LevelRepository.getInstance().getFood(dishId);
            if (food != null) {
                unlockedDishes.add(food);
            }
        }
    }

    private void setupViewPager() {
        adapter = new CookbookAdapter(unlockedDishes, this);
        viewPagerDishes.setAdapter(adapter);
        
        // Set page transformer for card effect
        viewPagerDishes.setOffscreenPageLimit(3);
        viewPagerDishes.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            page.setScaleY(1 - absPos * 0.15f);
            page.setAlpha(1 - absPos * 0.3f);
        });

        // Page change callback
        viewPagerDishes.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                updateNavigationButtons(position);
                
                // Speak dish name when page changes
                if (position < unlockedDishes.size()) {
                    Food currentDish = unlockedDishes.get(position);
                    ttsManager.speak(currentDish.getName(), "dish_page_" + position);
                }
            }
        });
    }

    private void setupNavigationButtons() {
        btnPrevious.setOnClickListener(v -> {
            soundManager.play(SoundPoolManager.SOUND_CLICK);
            AnimationHelper.bounce(btnPrevious);
            int current = viewPagerDishes.getCurrentItem();
            if (current > 0) {
                viewPagerDishes.setCurrentItem(current - 1, true);
            }
        });

        btnNext.setOnClickListener(v -> {
            soundManager.play(SoundPoolManager.SOUND_CLICK);
            AnimationHelper.bounce(btnNext);
            int current = viewPagerDishes.getCurrentItem();
            if (current < unlockedDishes.size() - 1) {
                viewPagerDishes.setCurrentItem(current + 1, true);
            }
        });

        btnGridView.setOnClickListener(v -> {
            soundManager.play(SoundPoolManager.SOUND_CLICK);
            AnimationHelper.pulse(btnGridView);
            // TODO: Toggle between ViewPager and Grid view
            ttsManager.speak("Grid view coming soon!", "grid_view");
        });

        if (btnPlayNow != null) {
            btnPlayNow.setOnClickListener(v -> {
                soundManager.play(SoundPoolManager.SOUND_CLICK);
                finish(); // Go back to play
            });
        }
    }

    private void updateUI() {
        if (unlockedDishes.isEmpty()) {
            // Show empty state
            viewPagerDishes.setVisibility(View.GONE);
            pageIndicator.setVisibility(View.GONE);
            btnPrevious.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
            btnGridView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            tvRecipeCount.setText("0 recipes mastered");
            
            // Speak empty state message
            ttsManager.speak("No recipes yet! Complete levels to unlock dishes", "empty_state");
        } else {
            // Show dishes
            viewPagerDishes.setVisibility(View.VISIBLE);
            pageIndicator.setVisibility(View.VISIBLE);
            btnPrevious.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);
            btnGridView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            
            // Update count
            String countText = unlockedDishes.size() + " recipe" + (unlockedDishes.size() > 1 ? "s" : "") + " mastered! 🏆";
            tvRecipeCount.setText(countText);
            
            // Create indicator dots
            createIndicatorDots();
            updateIndicators(0);
            updateNavigationButtons(0);
            
            // Speak first dish name
            ttsManager.speak("Welcome to your cookbook! You have " + unlockedDishes.size() + " recipes. " + 
                    unlockedDishes.get(0).getName(), "welcome");
        }
    }

    private void createIndicatorDots() {
        pageIndicator.removeAllViews();
        indicatorDots = new ImageView[unlockedDishes.size()];

        for (int i = 0; i < unlockedDishes.size(); i++) {
            indicatorDots[i] = new ImageView(this);
            indicatorDots[i].setImageResource(R.drawable.indicator_dot_inactive);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    24, 24
            );
            params.setMargins(8, 0, 8, 0);
            indicatorDots[i].setLayoutParams(params);
            
            pageIndicator.addView(indicatorDots[i]);
        }
    }

    private void updateIndicators(int position) {
        if (indicatorDots == null) return;
        
        for (int i = 0; i < indicatorDots.length; i++) {
            if (i == position) {
                indicatorDots[i].setImageResource(R.drawable.indicator_dot_active);
                AnimationHelper.pulse(indicatorDots[i]);
            } else {
                indicatorDots[i].setImageResource(R.drawable.indicator_dot_inactive);
            }
        }
    }

    private void updateNavigationButtons(int position) {
        btnPrevious.setEnabled(position > 0);
        btnPrevious.setAlpha(position > 0 ? 1.0f : 0.5f);
        
        btnNext.setEnabled(position < unlockedDishes.size() - 1);
        btnNext.setAlpha(position < unlockedDishes.size() - 1 ? 1.0f : 0.5f);
    }

    // CookbookAdapter.OnDishInteractionListener implementations
    
    @Override
    public void onDishClick(Food dish) {
        soundManager.play(SoundPoolManager.SOUND_CLICK);
        // Show dish detail dialog
        DishDetailDialog dialog = new DishDetailDialog(this, dish);
        dialog.show();
    }

    @Override
    public void onSpeakName(Food dish) {
        soundManager.play(SoundPoolManager.SOUND_CLICK);
        ttsManager.speak(dish.getName(), "dish_name_" + dish.getDishId());
    }

    @Override
    public void onReadRecipe(Food dish) {
        soundManager.play(SoundPoolManager.SOUND_CLICK);
        
        // Build recipe text
        StringBuilder recipeText = new StringBuilder();
        recipeText.append("This is ").append(dish.getName()).append(". ");
        
        if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
            recipeText.append(dish.getDescription()).append(". ");
        }
        
        // Get ingredients
        List<Ingredient> ingredients = LevelRepository.getInstance().getIngredientsForDish(dish.getDishId());
        if (!ingredients.isEmpty()) {
            recipeText.append("You need: ");
            for (int i = 0; i < ingredients.size(); i++) {
                recipeText.append(ingredients.get(i).getName());
                if (i < ingredients.size() - 1) {
                    recipeText.append(", ");
                }
            }
            recipeText.append(".");
        }
        
        ttsManager.speak(recipeText.toString(), "recipe_" + dish.getDishId());
    }

    @Override
    public void onViewDetails(Food dish) {
        soundManager.play(SoundPoolManager.SOUND_CLICK);
        // Show dish detail dialog
        DishDetailDialog dialog = new DishDetailDialog(this, dish);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsManager != null) {
            ttsManager.stop();
        }
    }

    @Override
    public void onBackPressed() {
        soundManager.play(SoundPoolManager.SOUND_CLICK);
        finish();
    }
}
