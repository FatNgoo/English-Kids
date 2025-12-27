package com.edu.english.magicmelody.ui.notebook;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edu.english.R;
import com.edu.english.magicmelody.util.AnimationUtils;

/**
 * 📚 Magic Notebook Activity
 * 
 * Purpose: Collection screen for learned notes/characters
 * - 3D pop-up character interactions
 * - Replay learning content
 * - Track collected notes and achievements
 * 
 * TODO: Full implementation in next task
 */
public class MagicNotebookActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_magic_notebook);
        
        setupSystemBars();
        initViews();
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
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                AnimationUtils.animateButtonPress(v);
                finish();
            });
        }
    }
}
