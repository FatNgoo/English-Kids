package com.edu.english.coloralchemy;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.edu.english.R;

import java.util.List;
import java.util.Locale;

/**
 * Colors Activity
 * Main landing page for Colors section
 * Shows unlocked colors and provides access to the Color Lab
 */
public class ColorsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private GridLayout colorsGrid;
    private FrameLayout colorDetailOverlay;
    private View colorCircle;
    private TextView tvColorName;
    private ImageView btnSpeakColor;
    private FrameLayout btnBack;
    private CardView cardLab;
    private TextView tvProgress;

    private CollectionManager collectionManager;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;

    // All possible colors to display - synced with Color Lab
    // Primary colors are always unlocked (available as tubes)
    // Tube colors (Pink, Cyan, White, Black, Lime, Magenta) are also unlocked as tubes
    // Secondary/Tertiary colors are unlocked by mixing
    private static final ColorInfo[] ALL_COLORS = {
            // Primary Colors (always available as tubes)
            new ColorInfo("Red", ColorMixer.COLOR_RED, true),
            new ColorInfo("Blue", ColorMixer.COLOR_BLUE, true),
            new ColorInfo("Yellow", ColorMixer.COLOR_YELLOW, true),

            // Secondary Colors (created by mixing primaries)
            new ColorInfo("Purple", ColorMixer.COLOR_PURPLE, false),
            new ColorInfo("Orange", ColorMixer.COLOR_ORANGE, false),
            new ColorInfo("Green", ColorMixer.COLOR_GREEN, false),

            // Additional Tube Colors (available as tubes in lab)
            new ColorInfo("Pink", ColorMixer.COLOR_PINK, true),
            new ColorInfo("Cyan", ColorMixer.COLOR_CYAN, true),
            new ColorInfo("White", ColorMixer.COLOR_WHITE, true),
            new ColorInfo("Black", ColorMixer.COLOR_BLACK, true),
            new ColorInfo("Lime", ColorMixer.COLOR_LIME, true),
            new ColorInfo("Magenta", ColorMixer.COLOR_MAGENTA, true),

            // Tertiary Colors (created by mixing)
            new ColorInfo("Brown", ColorMixer.COLOR_BROWN, false),
            new ColorInfo("Gray", ColorMixer.COLOR_GRAY, false),
            new ColorInfo("Peach", ColorMixer.COLOR_PEACH, false),
            new ColorInfo("Lavender", ColorMixer.COLOR_LAVENDER, false),
            new ColorInfo("Teal", ColorMixer.COLOR_TEAL, false),
            new ColorInfo("Coral", ColorMixer.COLOR_CORAL, false),
            new ColorInfo("Olive", ColorMixer.COLOR_OLIVE, false),
            new ColorInfo("Maroon", ColorMixer.COLOR_MAROON, false),
            new ColorInfo("Navy", ColorMixer.COLOR_NAVY, false),
            new ColorInfo("Turquoise", ColorMixer.COLOR_TURQUOISE, false),
    };

    private static class ColorInfo {
        String name;
        int color;
        boolean alwaysUnlocked;

        ColorInfo(String name, int color, boolean alwaysUnlocked) {
            this.name = name;
            this.color = color;
            this.alwaysUnlocked = alwaysUnlocked;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colors);

        // Initialize views
        colorsGrid = findViewById(R.id.colors_grid);
        colorDetailOverlay = findViewById(R.id.color_detail_overlay);
        colorCircle = findViewById(R.id.color_circle);
        tvColorName = findViewById(R.id.tv_color_name);
        btnSpeakColor = findViewById(R.id.btn_speak_color);
        btnBack = findViewById(R.id.btn_back);
        cardLab = findViewById(R.id.card_lab);
        tvProgress = findViewById(R.id.tv_progress);

        // Initialize collection manager
        collectionManager = new CollectionManager(this);

        // Initialize TTS
        textToSpeech = new TextToSpeech(this, this);

        // Setup UI
        setupBackButton();
        setupLabCard();
        setupColorDetailOverlay();
        populateColorsGrid();
        updateProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload collection data from storage in case user created new colors in lab
        collectionManager.reload();
        // Refresh colors display
        populateColorsGrid();
        updateProgress();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED;
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void setupLabCard() {
        cardLab.setOnClickListener(v -> {
            Intent intent = new Intent(ColorsActivity.this, ColorAlchemyActivity.class);
            startActivity(intent);
        });
    }

    private void setupColorDetailOverlay() {
        // Close overlay when tapping outside
        colorDetailOverlay.setOnClickListener(v -> hideColorDetail());

        // Speak color when tapping the circle or speaker button
        View.OnClickListener speakListener = v -> {
            String colorName = tvColorName.getText().toString();
            speakColor(colorName);
        };

        findViewById(R.id.color_circle_container).setOnClickListener(speakListener);
        btnSpeakColor.setOnClickListener(speakListener);
    }

    private void populateColorsGrid() {
        colorsGrid.removeAllViews();

        // Get unlocked colors from collection
        List<CollectionManager.CollectedColor> collectedColors = collectionManager.getCollectedColors();

        int columnCount = 5; // 5 columns for more colors
        int totalColors = ALL_COLORS.length;
        int rowCount = (int) Math.ceil((double) totalColors / columnCount);

        // Calculate item size based on screen width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (32 * getResources().getDisplayMetrics().density); // 16dp * 2
        int margins = (int) (8 * getResources().getDisplayMetrics().density * (columnCount + 1));
        int itemSize = (screenWidth - padding - margins) / columnCount;

        for (int i = 0; i < totalColors; i++) {
            ColorInfo colorInfo = ALL_COLORS[i];
            boolean isUnlocked = colorInfo.alwaysUnlocked || isColorCollected(colorInfo.name, collectedColors);

            View colorItem = createColorItem(colorInfo, isUnlocked, itemSize);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = itemSize;
            params.height = itemSize + (int) (32 * getResources().getDisplayMetrics().density); // Extra space for name
            params.setMargins(8, 8, 8, 8);
            params.columnSpec = GridLayout.spec(i % columnCount);
            params.rowSpec = GridLayout.spec(i / columnCount);

            colorsGrid.addView(colorItem, params);
        }
    }

    private boolean isColorCollected(String colorName, List<CollectionManager.CollectedColor> collectedColors) {
        for (CollectionManager.CollectedColor cc : collectedColors) {
            if (cc.name.equals(colorName)) {
                return true;
            }
        }
        return false;
    }

    private View createColorItem(ColorInfo colorInfo, boolean isUnlocked, int size) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);

        // Color circle
        View colorView = new View(this);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);

        if (isUnlocked) {
            drawable.setColor(colorInfo.color);
            drawable.setStroke((int) (4 * getResources().getDisplayMetrics().density), Color.WHITE);
        } else {
            // Locked - show gray
            drawable.setColor(Color.parseColor("#4D4D4D"));
            drawable.setStroke((int) (4 * getResources().getDisplayMetrics().density), Color.parseColor("#666666"));
        }

        colorView.setBackground(drawable);

        LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(size - 16, size - 16);
        colorParams.gravity = Gravity.CENTER;
        container.addView(colorView, colorParams);

        // Color name
        TextView nameText = new TextView(this);
        nameText.setGravity(Gravity.CENTER);
        nameText.setTextSize(12);
        nameText.setMaxLines(1);

        if (isUnlocked) {
            nameText.setText(colorInfo.name);
            nameText.setTextColor(Color.WHITE);
        } else {
            nameText.setText("???");
            nameText.setTextColor(Color.parseColor("#888888"));
        }

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        container.addView(nameText, textParams);

        // Click listener
        if (isUnlocked) {
            container.setClickable(true);
            container.setFocusable(true);
            container.setOnClickListener(v -> showColorDetail(colorInfo.name, colorInfo.color));

            // Add ripple effect
            container.setBackgroundResource(android.R.color.transparent);
        }

        return container;
    }

    private void showColorDetail(String colorName, int color) {
        // Set color
        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);
        circleDrawable.setColor(color);
        circleDrawable.setStroke((int) (8 * getResources().getDisplayMetrics().density), Color.WHITE);
        colorCircle.setBackground(circleDrawable);

        // Set name
        tvColorName.setText(colorName);

        // Reset animation state
        colorCircle.setScaleX(0f);
        colorCircle.setScaleY(0f);
        colorCircle.setAlpha(0f);
        tvColorName.setTranslationY(50f);
        tvColorName.setAlpha(0f);
        colorDetailOverlay.setAlpha(0f);

        // Show overlay
        colorDetailOverlay.setVisibility(View.VISIBLE);

        // Animate in
        ObjectAnimator overlayFade = ObjectAnimator.ofFloat(colorDetailOverlay, "alpha", 0f, 1f);
        overlayFade.setDuration(200);

        ObjectAnimator circleScaleX = ObjectAnimator.ofFloat(colorCircle, "scaleX", 0f, 1f);
        ObjectAnimator circleScaleY = ObjectAnimator.ofFloat(colorCircle, "scaleY", 0f, 1f);
        ObjectAnimator circleAlpha = ObjectAnimator.ofFloat(colorCircle, "alpha", 0f, 1f);

        AnimatorSet circleAnim = new AnimatorSet();
        circleAnim.playTogether(circleScaleX, circleScaleY, circleAlpha);
        circleAnim.setDuration(400);
        circleAnim.setInterpolator(new OvershootInterpolator(1.2f));
        circleAnim.setStartDelay(100);

        ObjectAnimator nameSlide = ObjectAnimator.ofFloat(tvColorName, "translationY", 50f, 0f);
        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(tvColorName, "alpha", 0f, 1f);

        AnimatorSet nameAnim = new AnimatorSet();
        nameAnim.playTogether(nameSlide, nameAlpha);
        nameAnim.setDuration(300);
        nameAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        nameAnim.setStartDelay(250);

        AnimatorSet fullAnim = new AnimatorSet();
        fullAnim.playTogether(overlayFade, circleAnim, nameAnim);
        fullAnim.start();

        // Auto-speak after animation
        colorCircle.postDelayed(() -> speakColor(colorName), 500);
    }

    private void hideColorDetail() {
        // Animate out
        ObjectAnimator overlayFade = ObjectAnimator.ofFloat(colorDetailOverlay, "alpha", 1f, 0f);
        overlayFade.setDuration(200);
        overlayFade.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator circleScale = ObjectAnimator.ofFloat(colorCircle, "scaleX", 1f, 0.5f);
        ObjectAnimator circleScaleY = ObjectAnimator.ofFloat(colorCircle, "scaleY", 1f, 0.5f);

        AnimatorSet hideAnim = new AnimatorSet();
        hideAnim.playTogether(overlayFade, circleScale, circleScaleY);
        hideAnim.setDuration(200);

        hideAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                colorDetailOverlay.setVisibility(View.GONE);
            }
        });

        hideAnim.start();
    }

    private void speakColor(String colorName) {
        if (ttsReady && textToSpeech != null) {
            textToSpeech.speak(colorName, TextToSpeech.QUEUE_FLUSH, null, "color_speak");

            // Pulse animation on speak
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(colorCircle, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(colorCircle, "scaleY", 1f, 1.1f, 1f);

            AnimatorSet pulseAnim = new AnimatorSet();
            pulseAnim.playTogether(scaleX, scaleY);
            pulseAnim.setDuration(300);
            pulseAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            pulseAnim.start();
        }
    }

    private void updateProgress() {
        List<CollectionManager.CollectedColor> collectedColors = collectionManager.getCollectedColors();
        
        // Count always unlocked colors
        int unlocked = 0;
        for (ColorInfo colorInfo : ALL_COLORS) {
            if (colorInfo.alwaysUnlocked) {
                unlocked++;
            }
        }

        // Count collected colors (not shades)
        for (CollectionManager.CollectedColor cc : collectedColors) {
            for (ColorInfo colorInfo : ALL_COLORS) {
                if (!colorInfo.alwaysUnlocked && cc.name.equals(colorInfo.name)) {
                    unlocked++;
                    break;
                }
            }
        }

        tvProgress.setText(unlocked + "/" + ALL_COLORS.length);
    }
}
