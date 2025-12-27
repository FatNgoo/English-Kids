package com.edu.english.magicmelody.ui.notebook;

import android.content.ClipData;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.gridlayout.widget.GridLayout;

import com.edu.english.R;
import com.edu.english.magicmelody.util.AnimationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 🎯 Word Puzzle Mini-Game
 * Bé kéo thả các chữ cái vào ô để ghép thành từ khóa của bức tranh
 */
public class WordPuzzleActivity extends AppCompatActivity {

    private GridLayout puzzleGrid, letterBank;
    private LinearLayout answerSlotsContainer;
    private TextView worldTitle;
    private TextToSpeech tts;
    private int systemBarBottomInset = 0;
    
    private String keyword;
    private String worldId;
    private int level;
    private List<String> awakenedWords;
    private List<TextView> answerSlots;
    private List<Character> shuffledLetters;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_word_puzzle);
        
        setupSystemBars();
        getIntentData();
        initViews();
        initTTS();
        buildPuzzlePreview();
        buildAnswerSlots();
        buildLetterBank();
    }
    
    private void setupSystemBars() {
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                // Save bottom inset so UI can add extra padding to avoid navigation bar overlap
                systemBarBottomInset = systemBars.bottom;
                return insets;
            });
        }
    }
    
    private void getIntentData() {
        worldId = getIntent().getStringExtra("worldId");
        level = getIntent().getIntExtra("level", 1);
        String worldName = getIntent().getStringExtra("worldName");
        keyword = getIntent().getStringExtra("keyword");
        awakenedWords = getIntent().getStringArrayListExtra("awakenedWords");

        // Defensive defaults: ensure keyword and awakenedWords are not null
        if (keyword == null || keyword.trim().isEmpty()) {
            // try to derive from worldName or worldId
            if (worldName != null && !worldName.trim().isEmpty()) {
                // take first word of worldName as fallback keyword
                String[] parts = worldName.trim().split(" ");
                keyword = parts[0].toUpperCase();
            } else if (worldId != null && !worldId.trim().isEmpty()) {
                keyword = worldId.replace("world_", "").toUpperCase();
            } else {
                keyword = "WORD"; // ultimate fallback
            }
        }

        if (awakenedWords == null) {
            awakenedWords = new ArrayList<>();
        }
    }
    
    private void initViews() {
        ImageButton backButton = findViewById(R.id.back_button);
        worldTitle = findViewById(R.id.world_title);
        puzzleGrid = findViewById(R.id.puzzle_grid);
        answerSlotsContainer = findViewById(R.id.answer_slots_container);
        letterBank = findViewById(R.id.letter_bank);
        
        worldTitle.setText(getIntent().getStringExtra("worldName") + " " + getWorldEmoji());
        
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                AnimationUtils.animateButtonPress(v);
                finish();
            });
        }
    }
    
    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }
    
    private void buildPuzzlePreview() {
        // Post để đảm bảo puzzleGrid đã có kích thước
        puzzleGrid.post(() -> {
            puzzleGrid.removeAllViews();
            
            int gridWidth = puzzleGrid.getWidth();
            int gridHeight = puzzleGrid.getHeight();
            
            // Nếu chưa có kích thước, dùng giá trị từ parent
            if (gridWidth == 0 || gridHeight == 0) {
                View parent = (View) puzzleGrid.getParent();
                if (parent != null) {
                    gridWidth = parent.getWidth();
                    gridHeight = parent.getHeight();
                }
            }
            
            float density = getResources().getDisplayMetrics().density;
            int margin = (int) (4 * density);
            int gridMargin = (int) (16 * density); // margin của grid trong FrameLayout
            
            // Tính available size
            int availableSize = Math.min(gridWidth, gridHeight) - gridMargin;
            int totalMargin = margin * 6; // 6 margins giữa các pieces
            int pieceSize = (availableSize - totalMargin) / 3;
            
            // Đảm bảo piece size hợp lý
            int minSize = (int) (70 * density);
            int maxSize = (int) (110 * density);
            pieceSize = Math.max(minSize, Math.min(maxSize, pieceSize));
            
            int wordIndex = 0;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    FrameLayout piece = new FrameLayout(this);
                    
                    if (row == 1 && col == 1) {
                        // Center - world icon
                        piece.setBackgroundResource(R.drawable.bg_puzzle_piece);
                        ImageView icon = new ImageView(this);
                        icon.setImageResource(getWorldIconDrawable());
                        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        int padding = (int) (14 * density);
                        icon.setPadding(padding, padding, padding, padding);
                        
                        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        );
                        icon.setLayoutParams(iconParams);
                        piece.addView(icon);
                    } else {
                        if (wordIndex < awakenedWords.size()) {
                            String word = awakenedWords.get(wordIndex);
                            piece.setBackgroundResource(R.drawable.bg_puzzle_piece);
                            
                            ImageView icon = new ImageView(this);
                            icon.setImageResource(getWordIconDrawable(word));
                            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            int padding = (int) (14 * density);
                            icon.setPadding(padding, padding, padding, padding);
                            
                            FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            );
                            icon.setLayoutParams(iconParams);
                            piece.addView(icon);
                            
                            wordIndex++;
                        }
                    }
                    
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = pieceSize;
                    params.height = pieceSize;
                    params.setMargins(margin, margin, margin, margin);
                    params.rowSpec = GridLayout.spec(row, 1f);
                    params.columnSpec = GridLayout.spec(col, 1f);
                    piece.setLayoutParams(params);
                    
                    puzzleGrid.addView(piece);
                }
            }
        });
    }
    
    private void buildAnswerSlots() {
        answerSlots = new ArrayList<>();
        float density = getResources().getDisplayMetrics().density;
        final int baseMargin = (int) (8 * density);

        int len = (keyword != null) ? keyword.length() : 0;
        if (len == 0) len = 4;

        // Create slots after layout to compute available width
        final int finalBaseMargin = baseMargin;
        final int finalLen = len;
        final float finalDensity = density;
        answerSlotsContainer.post(() -> {
            answerSlotsContainer.removeAllViews();
            int containerWidth = answerSlotsContainer.getWidth();
            if (containerWidth == 0) {
                containerWidth = getResources().getDisplayMetrics().widthPixels - (int) (48 * finalDensity);
            }
            int totalMargins = finalBaseMargin * (finalLen + 1);
            int maxSlot = (int) (72 * finalDensity);
            int minSlot = (int) (34 * finalDensity);
            int slotSize = (containerWidth - totalMargins) / finalLen;
            slotSize = Math.max(minSlot, Math.min(maxSlot, slotSize));

            for (int i = 0; i < finalLen; i++) {
                TextView slot = new TextView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(slotSize, slotSize);
                lp.setMargins(finalBaseMargin, 0, finalBaseMargin, 0);
                slot.setLayoutParams(lp);
                slot.setGravity(Gravity.CENTER);
                slot.setTextColor(getColor(android.R.color.white));
                slot.setBackgroundResource(R.drawable.bg_answer_slot_empty);
                slot.setTag("slot_" + i);

                // Adaptive text size to avoid clipping
                float textPx = slotSize * 0.40f;
                slot.setIncludeFontPadding(false);
                slot.setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx);

                slot.setOnDragListener(new AnswerSlotDragListener(i));

                answerSlotsContainer.addView(slot);
                answerSlots.add(slot);
            }
        });
    }
    
    private void buildLetterBank() {
        // Shuffle letters from keyword
        shuffledLetters = new ArrayList<>();
        for (char c : keyword.toCharArray()) {
            shuffledLetters.add(c);
        }
        Collections.shuffle(shuffledLetters);
        float density = getResources().getDisplayMetrics().density;
        final int margin = (int) (8 * density);

        // Build tiles after letterBank has size
        final int finalMargin = margin;
        final int finalLettersCount = shuffledLetters.size();
        final float finalDensity2 = density;
        letterBank.post(() -> {
            letterBank.removeAllViews();
            int bankWidth = letterBank.getWidth();
            if (bankWidth == 0) {
                bankWidth = getResources().getDisplayMetrics().widthPixels - (int) (48 * finalDensity2);
            }

            int lettersCount = finalLettersCount;
            int columns = Math.min(6, Math.max(1, lettersCount));
            int totalMargins = finalMargin * (columns + 1);
            int maxTile = (int) (72 * finalDensity2);
            int minTile = (int) (34 * finalDensity2);
            int tileSize = (bankWidth - totalMargins) / columns;
            tileSize = Math.max(minTile, Math.min(maxTile, tileSize));

            letterBank.setColumnCount(columns);

            for (int i = 0; i < shuffledLetters.size(); i++) {
                char letter = shuffledLetters.get(i);
                TextView tile = new TextView(this);
                tile.setText(String.valueOf(letter));

                // Adaptive text size
                float textPx = tileSize * 0.40f;
                tile.setIncludeFontPadding(false);
                tile.setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx);

                tile.setTextColor(getColor(android.R.color.black));
                tile.setBackgroundResource(R.drawable.bg_letter_tile);
                tile.setGravity(Gravity.CENTER);
                tile.setTag("letter_" + i);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = tileSize;
                params.height = tileSize;
                params.setMargins(finalMargin, finalMargin, finalMargin, finalMargin);
                tile.setLayoutParams(params);

                // Set drag listener
                tile.setOnLongClickListener(v -> {
                    ClipData data = ClipData.newPlainText("letter", tile.getText().toString());
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadowBuilder, v, 0);
                    v.setVisibility(View.INVISIBLE);
                    return true;
                });

                letterBank.addView(tile);
            }

            // Ensure bottom padding to account for system navigation bar and give space to scroll
            int extraBottom = (int) (16 * finalDensity2) + systemBarBottomInset;
            letterBank.setPadding(letterBank.getPaddingLeft(), letterBank.getPaddingTop(),
                    letterBank.getPaddingRight(), Math.max(letterBank.getPaddingBottom(), extraBottom));
        });
    }
    
    private class AnswerSlotDragListener implements View.OnDragListener {
        private int slotIndex;
        
        public AnswerSlotDragListener(int slotIndex) {
            this.slotIndex = slotIndex;
        }
        
        @Override
        public boolean onDrag(View v, DragEvent event) {
            TextView slot = (TextView) v;
            
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                    
                case DragEvent.ACTION_DRAG_ENTERED:
                    slot.setAlpha(0.5f);
                    return true;
                    
                case DragEvent.ACTION_DRAG_EXITED:
                    slot.setAlpha(1.0f);
                    return true;
                    
                case DragEvent.ACTION_DROP:
                    slot.setAlpha(1.0f);
                    ClipData data = event.getClipData();
                    String letter = data.getItemAt(0).getText().toString();
                    
                    // Place letter in slot
                    slot.setText(letter);
                    slot.setBackgroundResource(R.drawable.bg_answer_slot_filled);
                    
                    // Remove the dragged tile
                    View draggedView = (View) event.getLocalState();
                    ViewGroup parent = (ViewGroup) draggedView.getParent();
                    parent.removeView(draggedView);
                    
                    // Check if puzzle is complete
                    checkPuzzleComplete();
                    return true;
                    
                case DragEvent.ACTION_DRAG_ENDED:
                    View dragView = (View) event.getLocalState();
                    if (!event.getResult()) {
                        dragView.setVisibility(View.VISIBLE);
                    }
                    return true;
                    
                default:
                    return false;
            }
        }
    }
    
    private void checkPuzzleComplete() {
        StringBuilder answer = new StringBuilder();
        for (TextView slot : answerSlots) {
            CharSequence text = slot.getText();
            if (text == null || text.length() == 0) {
                return; // Not complete yet
            }
            answer.append(text);
        }
        
        if (answer.toString().equals(keyword)) {
            // SUCCESS!
            onPuzzleSuccess();
        }
    }
    
    private void onPuzzleSuccess() {
        Toast.makeText(this, "🎉 Perfect! You got it!", Toast.LENGTH_LONG).show();
        
        // Animate slots
        for (TextView slot : answerSlots) {
            slot.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200)
                .withEndAction(() -> slot.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start())
                .start();
        }
        
        // Speak the keyword
        if (tts != null) {
            tts.speak(keyword, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        
        // Finish after 2 seconds
        answerSlotsContainer.postDelayed(this::finish, 2000);
    }
    
    private String getWorldEmoji() {
        switch (worldId) {
            case "world_forest": return "🌲";
            case "world_ocean": return "🌊";
            case "world_mountain": return "⛰️";
            case "world_desert": return "🏜️";
            case "world_sky": return "☁️";
            default: return "🏆";
        }
    }
    
    private int getWorldIconDrawable() {
        switch (worldId) {
            case "world_forest": return R.drawable.ic_forest_tree;
            case "world_ocean": return R.drawable.ic_ocean_whale;
            case "world_mountain": return R.drawable.ic_mountain_peak;
            case "world_desert": return R.drawable.ic_desert_pyramid;
            case "world_sky": return R.drawable.ic_sky_rocket;
            default: return R.drawable.ic_forest_tree;
        }
    }
    
    private int getWordIconDrawable(String word) {
        switch (word) {
            case "Tree": return R.drawable.ic_forest_tree;
            case "Flower": return R.drawable.ic_forest_flower;
            case "Bird": return R.drawable.ic_forest_bird;
            case "Sun": return R.drawable.ic_forest_sun;
            case "Cloud": return R.drawable.ic_forest_cloud;
            case "Butterfly": return R.drawable.ic_forest_butterfly;
            case "Mushroom": return R.drawable.ic_forest_mushroom;
            case "Grass": return R.drawable.ic_forest_grass;
            case "Fish": return R.drawable.ic_ocean_fish;
            case "Whale": return R.drawable.ic_ocean_whale;
            case "Jellyfish": return R.drawable.ic_ocean_jellyfish;
            case "Coral": return R.drawable.ic_ocean_coral;
            case "Seaweed": return R.drawable.ic_ocean_seaweed;
            case "Shell": return R.drawable.ic_ocean_shell;
            case "Crab": return R.drawable.ic_ocean_crab;
            case "Starfish": return R.drawable.ic_ocean_starfish;
            case "Goat": return R.drawable.ic_mountain_goat;
            case "Eagle": return R.drawable.ic_mountain_eagle;
            case "Snow": return R.drawable.ic_mountain_snow;
            case "Rock": return R.drawable.ic_mountain_rock;
            case "Pine": return R.drawable.ic_mountain_pine;
            case "Cabin": return R.drawable.ic_mountain_cabin;
            case "Peak": return R.drawable.ic_mountain_peak;
            case "River": return R.drawable.ic_mountain_river;
            case "Camel": return R.drawable.ic_desert_camel;
            case "Cactus": return R.drawable.ic_desert_cactus;
            case "Scorpion": return R.drawable.ic_desert_scorpion;
            case "Lizard": return R.drawable.ic_desert_lizard;
            case "Oasis": return R.drawable.ic_desert_oasis;
            case "Pyramid": return R.drawable.ic_desert_pyramid;
            case "Sand": return R.drawable.ic_desert_sand;
            case "Plane": return R.drawable.ic_sky_plane;
            case "Balloon": return R.drawable.ic_sky_balloon;
            case "Rainbow": return R.drawable.ic_sky_rainbow;
            case "Star": return R.drawable.ic_sky_star;
            case "Moon": return R.drawable.ic_sky_moon;
            case "Rocket": return R.drawable.ic_sky_rocket;
            case "Kite": return R.drawable.ic_sky_kite;
            case "UFO": return R.drawable.ic_sky_ufo;
            default: return R.drawable.ic_forest_tree;
        }
    }
    
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
