package com.edu.english.coloralchemy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.edu.english.R;

import java.util.List;

/**
 * Color Alchemy Lab Activity
 * Main activity for the color mixing game
 */
public class ColorAlchemyActivity extends AppCompatActivity implements GameSurfaceView.OnGameEventListener {
    
    private GameSurfaceView gameSurfaceView;
    private FrameLayout rootLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set fullscreen immersive mode
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        
        // Create root layout
        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.parseColor("#E8F5E9"));
        
        // Create and add game surface view
        gameSurfaceView = new GameSurfaceView(this);
        gameSurfaceView.setGameEventListener(this);
        
        rootLayout.addView(gameSurfaceView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        setContentView(rootLayout);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (gameSurfaceView != null) {
            gameSurfaceView.resume();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (gameSurfaceView != null) {
            gameSurfaceView.pause();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameSurfaceView != null) {
            gameSurfaceView.release();
        }
    }
    
    private void onBackPressedAction() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Leave Alchemy Lab?")
            .setMessage("Your current mix will be lost. Continue?")
            .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .setNegativeButton("Stay", null)
            .show();
    }
    
    // ==================== Game Event Callbacks ====================
    
    @Override
    public void onBackPressed() {
        onBackPressedAction();
    }
    
    @Override
    public void onCollectionPressed() {
        showCollectionDialog();
    }
    
    @Override
    public void onColorCreated(String colorName, int color) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Could show a celebration animation or toast
                Toast.makeText(ColorAlchemyActivity.this, 
                    "🎨 You created " + colorName + "!", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onShadeCreated(String shadeName, int color) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ColorAlchemyActivity.this, 
                    "✨ New shade: " + shadeName + "!", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Show color collection dialog
     */
    private void showCollectionDialog() {
        CollectionManager collectionManager = gameSurfaceView.getCollectionManager();
        List<CollectionManager.CollectedColor> colors = collectionManager.getCollectedColors();
        
        // Create dialog content
        ScrollView scrollView = new ScrollView(this);
        scrollView.setPadding(32, 32, 32, 32);
        
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        
        // Title with stats
        TextView statsText = new TextView(this);
        statsText.setText("Collected: " + colors.size() + "/" + collectionManager.getTotalPossibleColors() + 
            "\nTotal Mixes: " + collectionManager.getTotalMixes());
        statsText.setTextSize(16);
        statsText.setGravity(Gravity.CENTER);
        statsText.setPadding(0, 0, 0, 24);
        contentLayout.addView(statsText);
        
        // Progress bar
        LinearLayout progressContainer = new LinearLayout(this);
        progressContainer.setOrientation(LinearLayout.HORIZONTAL);
        progressContainer.setPadding(0, 0, 0, 32);
        
        View progressFill = new View(this);
        GradientDrawable progressBg = new GradientDrawable();
        progressBg.setColor(Color.parseColor("#3498DB"));
        progressBg.setCornerRadius(8);
        progressFill.setBackground(progressBg);
        
        int progressWidth = (int) (250 * collectionManager.getProgress());
        progressContainer.addView(progressFill, new LinearLayout.LayoutParams(
            Math.max(progressWidth, 10), 16));
        
        contentLayout.addView(progressContainer);
        
        // Color grid
        if (colors.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("🧪 Start mixing colors to build your collection!");
            emptyText.setTextSize(14);
            emptyText.setGravity(Gravity.CENTER);
            contentLayout.addView(emptyText);
        } else {
            GridLayout gridLayout = new GridLayout(this);
            gridLayout.setColumnCount(3);
            gridLayout.setUseDefaultMargins(true);
            
            for (CollectionManager.CollectedColor cc : colors) {
                LinearLayout colorItem = createColorItem(cc);
                gridLayout.addView(colorItem);
            }
            
            contentLayout.addView(gridLayout);
        }
        
        scrollView.addView(contentLayout);
        
        // Mark all as seen
        collectionManager.markAllAsSeen();
        
        // Show dialog
        new AlertDialog.Builder(this)
            .setTitle("📚 Color Collection")
            .setView(scrollView)
            .setPositiveButton("Close", null)
            .show();
    }
    
    /**
     * Create a color item view for collection grid
     */
    private LinearLayout createColorItem(CollectionManager.CollectedColor cc) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(16, 16, 16, 16);
        
        // Color circle
        View colorView = new View(this);
        GradientDrawable colorDrawable = new GradientDrawable();
        colorDrawable.setShape(GradientDrawable.OVAL);
        colorDrawable.setColor(cc.color);
        colorDrawable.setStroke(3, Color.WHITE);
        colorView.setBackground(colorDrawable);
        
        LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(60, 60);
        colorParams.setMargins(0, 0, 0, 8);
        item.addView(colorView, colorParams);
        
        // Color name
        TextView nameText = new TextView(this);
        nameText.setText(cc.getDisplayName());
        nameText.setTextSize(11);
        nameText.setGravity(Gravity.CENTER);
        nameText.setMaxWidth(80);
        item.addView(nameText);
        
        // New badge
        if (cc.isNew) {
            TextView newBadge = new TextView(this);
            newBadge.setText("NEW");
            newBadge.setTextSize(8);
            newBadge.setTextColor(Color.WHITE);
            newBadge.setBackgroundColor(Color.parseColor("#E74C3C"));
            newBadge.setPadding(6, 2, 6, 2);
            item.addView(newBadge);
        }
        
        return item;
    }
}
