package com.edu.english.shapes;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.edu.english.R;
import com.edu.english.shapes.adapters.ShapesPagerAdapter;
import com.edu.english.shapes.data.ShapeDataProvider;
import com.edu.english.shapes.models.Shape;
import com.edu.english.shapes.utils.ShapeAudioManager;

/**
 * Main activity for the Shapes Learning Game
 * Contains ViewPager2 for navigating between different shapes
 */
public class ShapesActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ShapesPagerAdapter pagerAdapter;
    private ImageButton btnBack;
    private TextView tvShapeName;
    private TextView tvProgress;

    private ShapeDataProvider dataProvider;
    private ShapeAudioManager audioManager;
    private int currentShapeIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_shapes);

        // Hide system bars
        hideSystemBars();

        initData();
        initViews();
        setupViewPager();
        setupClickListeners();
        updateUI();
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void initData() {
        dataProvider = ShapeDataProvider.getInstance();
        audioManager = ShapeAudioManager.getInstance(this);
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        btnBack = findViewById(R.id.btnBack);
        tvShapeName = findViewById(R.id.tvShapeName);
        tvProgress = findViewById(R.id.tvProgress);
    }

    private void setupViewPager() {
        pagerAdapter = new ShapesPagerAdapter(this, dataProvider.getAllShapes());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        
        // Disable swipe - only allow navigation through completion
        viewPager.setUserInputEnabled(false);

        // Add page transformer for smooth transitions
        viewPager.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            page.setAlpha(1f - absPos * 0.5f);
            page.setScaleY(1f - absPos * 0.1f);
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentShapeIndex = position;
                updateUI();
                audioManager.playSound(ShapeAudioManager.SOUND_POP);
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            audioManager.playSound(ShapeAudioManager.SOUND_CLICK);
            finish();
        });
    }

    private void updateUI() {
        Shape currentShape = dataProvider.getShapeByIndex(currentShapeIndex);
        if (currentShape != null) {
            tvShapeName.setText(currentShape.getName());
        }

        tvProgress.setText(String.format("%d / %d", currentShapeIndex + 1, dataProvider.getShapeCount()));
    }

    public void goToNextShape() {
        if (currentShapeIndex < dataProvider.getShapeCount() - 1) {
            viewPager.setCurrentItem(currentShapeIndex + 1, true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioManager != null) {
            audioManager.release();
        }
    }

    @Override
    public void onBackPressed() {
        audioManager.playSound(ShapeAudioManager.SOUND_CLICK);
        super.onBackPressed();
    }
}
