package com.edu.english.magicmelody.ui.notebook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.edu.english.R;
import com.edu.english.magicmelody.util.Constants;
import com.edu.english.magicmelody.util.AnimationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 📚 Magic Notebook Activity
 * 
 * Purpose: Collection screen for puzzle pictures from completed levels
 * - Display all collected puzzle pictures
 * - Click to play word puzzle mini-game
 */
public class MagicNotebookActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "MagicMelodyPrefs";
    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private PuzzlePictureAdapter adapter;
    private List<PuzzlePicture> pictures;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_magic_notebook);
        
        setupSystemBars();
        initViews();
        loadPictures();
    }
    
    private void setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void initViews() {
        ImageButton backButton = findViewById(R.id.back_button);
        recyclerView = findViewById(R.id.pictures_recycler_view);
        // Ensure a LayoutManager is set to avoid RecyclerView crashes
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        emptyState = findViewById(R.id.empty_state);
        
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                AnimationUtils.animateButtonPress(v);
                finish();
            });
        }
    }
    
    private void loadPictures() {
        pictures = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Load all saved pictures from all worlds and levels
        String[] worldIds = {Constants.WORLD_FOREST, Constants.WORLD_OCEAN, Constants.WORLD_MOUNTAIN, 
                            Constants.WORLD_DESERT, Constants.WORLD_SKY};
        
        for (String worldId : worldIds) {
            for (int level = 1; level <= 3; level++) {
                String pictureKey = "picture_" + worldId + "_level_" + level;
                boolean hasPicture = prefs.getBoolean(pictureKey, false);
                
                if (hasPicture) {
                    String awakenedKey = "picture_awakened_" + worldId + "_level_" + level;
                    String totalKey = "picture_total_" + worldId + "_level_" + level;
                    String wordsKey = "picture_words_" + worldId + "_level_" + level;
                    
                    int awakened = prefs.getInt(awakenedKey, 0);
                    int total = prefs.getInt(totalKey, 8);
                    String wordsStr = prefs.getString(wordsKey, "");
                    
                    List<String> words = new ArrayList<>();
                    if (!wordsStr.isEmpty()) {
                        words = Arrays.asList(wordsStr.split(","));
                    }
                    
                    String worldName = getWorldName(worldId);
                    String worldEmoji = getWorldEmoji(worldId);
                    String keyword = getWorldKeyword(worldId);
                    
                    PuzzlePicture picture = new PuzzlePicture(worldId, level, worldName, worldEmoji, 
                                                             awakened, total, words, keyword);
                    pictures.add(picture);
                }
            }
        }
        
        // Show empty state or pictures
        if (pictures.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new PuzzlePictureAdapter(this, pictures);
            recyclerView.setAdapter(adapter);
        }
    }
    
    private String getWorldName(String worldId) {
        switch (worldId) {
            case Constants.WORLD_FOREST: return "Forest Kingdom";
            case Constants.WORLD_OCEAN: return "Ocean World";
            case Constants.WORLD_MOUNTAIN: return "Mountain Heights";
            case Constants.WORLD_DESERT: return "Desert Lands";
            case Constants.WORLD_SKY: return "Sky Realm";
            default: return "Unknown World";
        }
    }
    
    private String getWorldEmoji(String worldId) {
        switch (worldId) {
            case Constants.WORLD_FOREST: return "🌲";
            case Constants.WORLD_OCEAN: return "🌊";
            case Constants.WORLD_MOUNTAIN: return "⛰️";
            case Constants.WORLD_DESERT: return "🏜️";
            case Constants.WORLD_SKY: return "☁️";
            default: return "🏆";
        }
    }
    
    private String getWorldKeyword(String worldId) {
        switch (worldId) {
            case Constants.WORLD_FOREST: return "FOREST";
            case Constants.WORLD_OCEAN: return "OCEAN";
            case Constants.WORLD_MOUNTAIN: return "MOUNTAIN";
            case Constants.WORLD_DESERT: return "DESERT";
            case Constants.WORLD_SKY: return "SKY";
            default: return "WORLD";
        }
    }
}
