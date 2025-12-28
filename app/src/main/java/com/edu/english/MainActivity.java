package com.edu.english;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.alphabet_adventure.screens.MascotSelectActivity;
import com.edu.english.alphabet_pop_lab.AlphabetPopLabActivity;
import com.edu.english.coloralchemy.ColorsActivity;
import com.edu.english.magicmelody.ui.splash.MagicMelodySplashActivity;
import com.edu.english.numbers.NumbersLessonActivity;
import com.edu.english.storybook.MagicStorybookCategoryActivity;
import com.edu.english.shapes.ShapesActivity;
import com.edu.english.numbers.NumbersSelectActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView lessonsRecycler;
    private RecyclerView gamesRecycler;
    private LessonsAdapter lessonsAdapter;
    private GamesAdapter gamesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Make window visible immediately to prevent black screen
        getWindow().setBackgroundDrawableResource(R.color.deep_blue);
        
        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup profile click to open drawer
        LinearLayout profileSection = findViewById(R.id.profile_section);
        profileSection.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(android.view.Gravity.START);
            }
        });

        // Setup Settings button
        setupSettingsButton();

        // Setup RecyclerViews
        setupLessonsRecyclerView();
        setupGamesRecyclerView();
    }

    private void setupSettingsButton() {
        LinearLayout btnSettings = findViewById(R.id.btn_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                // Close drawer first
                if (drawerLayout != null) {
                    drawerLayout.closeDrawers();
                }
                
                // Show settings dialog
                SettingsDialog settingsDialog = new SettingsDialog(this);
                settingsDialog.setOnSettingsChangedListener(new SettingsDialog.OnSettingsChangedListener() {
                    @Override
                    public void onSoundToggled(boolean enabled) {
                        // Handle sound toggle
                    }

                    @Override
                    public void onMusicToggled(boolean enabled) {
                        // Handle music toggle
                    }

                    @Override
                    public void onVolumeChanged(int volume) {
                        // Handle volume change
                    }

                    @Override
                    public void onBrightnessChanged(int brightness) {
                        // Apply brightness to activity window
                        android.view.WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                        layoutParams.screenBrightness = brightness / 100f;
                        getWindow().setAttributes(layoutParams);
                    }
                });
                settingsDialog.show();
            });
        }
    }

    private void setupLessonsRecyclerView() {
        lessonsRecycler = findViewById(R.id.lessons_recycler);
        
        // Use horizontal LinearLayoutManager for scrolling lessons
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        lessonsRecycler.setLayoutManager(horizontalLayoutManager);
        
        // Create lessons data with colors
        List<LessonItem> lessons = new ArrayList<>();
        lessons.add(new LessonItem("Alphabet", 0xFF6C63FF, "alphabet"));
        lessons.add(new LessonItem("Numbers", 0xFFFF6584, "numbers"));
        lessons.add(new LessonItem("Colors", 0xFF4CAF50, "colors"));
        lessons.add(new LessonItem("Shapes", 0xFFFF9800, "shapes"));
        lessons.add(new LessonItem("Animals", 0xFF2196F3, "animals"));
        
        // Create adapter with click listener
        lessonsAdapter = new LessonsAdapter(lessons, lesson -> {
            handleLessonClick(lesson);
        });
        
        lessonsRecycler.setAdapter(lessonsAdapter);
    }

    private void setupGamesRecyclerView() {
        gamesRecycler = findViewById(R.id.games_recycler);
        
        // Use LinearLayoutManager for full-width items
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        gamesRecycler.setLayoutManager(linearLayoutManager);
        
        // Create games data with colors
        List<GameItem> games = new ArrayList<>();
        games.add(new GameItem("Word Race", 0xFF6C63FF, "word_race"));
        games.add(new GameItem("Master Chef", 0xFFE91E63, "master_chef"));
        games.add(new GameItem("Magic Melody", 0xFF00BCD4, "magic_melody"));
        // Magic Storybook - right after Magic Melody
        games.add(new GameItem("Magic Storybook", 0xFF7C4DFF, "magic_storybook"));
        
        // Create adapter with click listener
        gamesAdapter = new GamesAdapter(games, game -> {
            handleGameClick(game);
        });
        
        gamesRecycler.setAdapter(gamesAdapter);
    }

    private void handleLessonClick(LessonItem lesson) {
        switch (lesson.getLessonType()) {
            case "alphabet":
                Intent alphabetIntent = new Intent(MainActivity.this, AlphabetPopLabActivity.class);
                startActivity(alphabetIntent);
                break;
            case "numbers":
                // Navigate to Numbers Selection screen with 3 options
                Intent numbersIntent = new Intent(MainActivity.this, NumbersSelectActivity.class);
                startActivity(numbersIntent);
                break;
            case "colors":
                Intent colorsIntent = new Intent(MainActivity.this, ColorsActivity.class);
                startActivity(colorsIntent);
                break;
            case "shapes":
                Intent shapesIntent = new Intent(MainActivity.this, ShapesActivity.class);
                startActivity(shapesIntent);
                break;
            case "animals":
                Intent animalsIntent = new Intent(MainActivity.this, AnimalArSelectActivity.class);
                startActivity(animalsIntent);
                break;
        }
    }

    private void handleGameClick(GameItem game) {
        if (game.getGameType().equals("word_race")) {
            Intent wordRaceIntent = new Intent(MainActivity.this, MascotSelectActivity.class);
            startActivity(wordRaceIntent);
        } else if (game.getGameType().equals("magic_storybook")) {
            Intent storybookIntent = new Intent(MainActivity.this, MagicStorybookCategoryActivity.class);
            startActivity(storybookIntent);
        } else if (game.getGameType().equals("magic_melody")) {
            // Launch Magic Melody Game!
            Intent magicMelodyIntent = new Intent(MainActivity.this, MagicMelodySplashActivity.class);
            startActivity(magicMelodyIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            String message = "Let's play " + game.getTitle() + "! 🎮";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}