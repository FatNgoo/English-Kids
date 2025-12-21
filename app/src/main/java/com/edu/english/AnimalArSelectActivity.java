package com.edu.english;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.adapter.AnimalArAdapter;
import com.edu.english.data.AnimalArRepository;
import com.edu.english.model.AnimalArItem;
import com.edu.english.util.ArCoreHelper;

import java.util.List;

/**
 * Activity for selecting an animal to view in AR
 * Displays a 2-column grid of 6 animals
 */
public class AnimalArSelectActivity extends AppCompatActivity implements AnimalArAdapter.OnAnimalClickListener {

    public static final String EXTRA_ANIMAL_ID = "animal_id";
    
    private RecyclerView recyclerAnimals;
    private TextView txtArStatus;
    private boolean isArSupported = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_animal_ar_select);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize views
        initViews();
        
        // Check AR support
        checkArSupport();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Animate items
        animateItems();
    }
    
    private void initViews() {
        recyclerAnimals = findViewById(R.id.recycler_animals);
        txtArStatus = findViewById(R.id.txt_ar_status);
        
        // Back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }
    
    private void checkArSupport() {
        // Check if AR is supported on this device
        isArSupported = ArCoreHelper.isArSupported(this);
        
        if (isArSupported) {
            txtArStatus.setText("AR Ready ✓");
            txtArStatus.setBackgroundResource(R.drawable.bg_card_green);
        } else {
            txtArStatus.setText("2D Mode");
            txtArStatus.setBackgroundResource(R.drawable.bg_card_orange);
        }
    }
    
    private void setupRecyclerView() {
        // Get animals from repository
        List<AnimalArItem> animals = AnimalArRepository.getInstance().getAllAnimals();
        
        // Setup GridLayoutManager with 2 columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerAnimals.setLayoutManager(layoutManager);
        
        // Setup adapter
        AnimalArAdapter adapter = new AnimalArAdapter(animals, this);
        recyclerAnimals.setAdapter(adapter);
    }
    
    @Override
    public void onAnimalClick(AnimalArItem animal) {
        // Navigate to AR Viewer with animal ID
        Intent intent = new Intent(this, AnimalArViewerActivity.class);
        intent.putExtra(EXTRA_ANIMAL_ID, animal.getId());
        startActivity(intent);
    }
    
    private void animateItems() {
        recyclerAnimals.setAlpha(0f);
        recyclerAnimals.setTranslationY(50f);
        
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(recyclerAnimals, "alpha", 0f, 1f);
        ObjectAnimator translateAnim = ObjectAnimator.ofFloat(recyclerAnimals, "translationY", 50f, 0f);
        
        alphaAnim.setDuration(500);
        translateAnim.setDuration(500);
        alphaAnim.setStartDelay(200);
        translateAnim.setStartDelay(200);
        alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        
        alphaAnim.start();
        translateAnim.start();
    }
}
