package com.edu.english.coloralchemy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        rootLayout.setBackgroundColor(Color.parseColor("#0D1259")); // Dark blue like home
        
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
        // Collection is now handled by ColorsActivity, just go back
        finish();
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

}
