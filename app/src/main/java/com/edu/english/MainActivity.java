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

        // Setup RecyclerViews
        setupLessonsRecyclerView();
        setupGamesRecyclerView();
    }

    private void setupLessonsRecyclerView() {
        lessonsRecycler = findViewById(R.id.lessons_recycler);
        
        // Use horizontal LinearLayoutManager for scrolling lessons
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        lessonsRecycler.setLayoutManager(horizontalLayoutManager);
        
        // Create lessons data with colors
        List<LessonItem> lessons = new ArrayList<>();
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
        games.add(new GameItem("Detective", 0xFF9C27B0, "detective"));
        games.add(new GameItem("Pet Hospital", 0xFF00BCD4, "pet_hospital"));
        
        // Create adapter with click listener
        gamesAdapter = new GamesAdapter(games, game -> {
            handleGameClick(game);
        });
        
        gamesRecycler.setAdapter(gamesAdapter);
    }

    private void handleLessonClick(LessonItem lesson) {
        switch (lesson.getLessonType()) {
            case "numbers":
                Toast.makeText(this, "Let's learn Numbers! 🎉", Toast.LENGTH_SHORT).show();
                break;
            case "colors":
                Toast.makeText(this, "Let's learn Colors! 🎉", Toast.LENGTH_SHORT).show();
                break;
            case "shapes":
                Toast.makeText(this, "Let's learn Shapes! 🎉", Toast.LENGTH_SHORT).show();
                break;
            case "animals":
                Toast.makeText(this, "Let's learn Animals! 🎉", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void handleGameClick(GameItem game) {
        if (game.getGameType().equals("word_race")) {
            Intent wordRaceIntent = new Intent(MainActivity.this, MascotSelectActivity.class);
            startActivity(wordRaceIntent);
        } else {
            String message = "Let's play " + game.getTitle() + "! 🎮";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}