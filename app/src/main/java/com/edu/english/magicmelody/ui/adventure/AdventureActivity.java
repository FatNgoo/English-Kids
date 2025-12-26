package com.edu.english.magicmelody.ui.adventure;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.edu.english.magicmelody.ui.gameplay.GameplayActivity;
import com.edu.english.magicmelody.ui.concert.ConcertBossActivity;
import com.edu.english.magicmelody.ui.notebook.NotebookActivity;
import com.edu.english.databinding.ActivityAdventureBinding;

import java.util.ArrayList;

/**
 * AdventureActivity - World Map screen
 * Shows all levels in a vertical scrolling zigzag layout
 * Players can select unlocked levels to play
 */
public class AdventureActivity extends AppCompatActivity implements LevelAdapter.OnLevelClickListener {
    
    public static final String EXTRA_LESSON_ID = "lesson_id";
    
    private ActivityAdventureBinding binding;
    private AdventureViewModel viewModel;
    private LevelAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AdventureActivity", "onCreate started");
        try {
            binding = ActivityAdventureBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            Log.d("AdventureActivity", "Layout inflated");
            
            setupViewModel();
            Log.d("AdventureActivity", "ViewModel setup done");
            setupRecyclerView();
            Log.d("AdventureActivity", "RecyclerView setup done");
            setupButtons();
            Log.d("AdventureActivity", "Buttons setup done");
            observeData();
            Log.d("AdventureActivity", "Data observation done");
        } catch (Exception e) {
            Log.e("AdventureActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AdventureViewModel.class);
    }
    
    private void setupRecyclerView() {
        adapter = new LevelAdapter(new ArrayList<>(), this);
        binding.recyclerViewLevels.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewLevels.setAdapter(adapter);
    }
    
    private void setupButtons() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Notebook button
        binding.btnNotebook.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotebookActivity.class);
            startActivity(intent);
        });
        
        // Concert Boss button
        binding.btnConcert.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConcertBossActivity.class);
            startActivity(intent);
        });
    }
    
    private void observeData() {
        // Observe levels
        viewModel.getLevels().observe(this, levels -> {
            if (levels != null) {
                adapter.updateLevels(levels);
                binding.loadingProgress.setVisibility(View.GONE);
            }
        });
        
        // Observe user profile
        viewModel.getUserProfile().observe(this, profile -> {
            if (profile != null && binding != null) {
                binding.txtWelcome.setText("Xin chào " + profile.getDisplayName() + "!");
            }
        });
        
        // Observe total stars
        LiveData<Integer> totalStarsLiveData = viewModel.getTotalStars();
        if (totalStarsLiveData != null) {
            totalStarsLiveData.observe(this, stars -> {
                if (binding != null) {
                    binding.txtTotalStars.setText("⭐ " + (stars != null ? stars : 0));
                }
            });
        }
    }
    
    @Override
    public void onLevelClick(AdventureViewModel.LevelItem level) {
        if (level.isUnlocked()) {
            // Start gameplay
            Intent intent = new Intent(this, GameplayActivity.class);
            intent.putExtra(EXTRA_LESSON_ID, level.getLessonId());
            startActivity(intent);
        } else {
            // Show locked message
            Toast.makeText(this, "Hoàn thành các màn trước để mở khóa!", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh levels when returning from gameplay
        viewModel.refreshLevels();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
