package com.edu.english.alphabet_pop_lab;

import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * BubbleManager - Manages all bubbles in the game
 * Handles creation, updates, collisions, and rendering
 */
public class BubbleManager {
    
    private List<Bubble> bubbles;
    private int screenWidth, screenHeight;
    private Random random;
    
    // Bubble colors (pastel palette - kid friendly)
    private static final int[] BUBBLE_COLORS = {
        0xFFFF6B6B,  // Coral Red
        0xFF4ECDC4,  // Teal
        0xFFFFE66D,  // Yellow
        0xFF95E1D3,  // Mint
        0xFFF38181,  // Pink
        0xFFAA96DA,  // Purple
        0xFF6C5CE7,  // Indigo
        0xFFFD79A8,  // Rose
        0xFF00B894,  // Green
        0xFF74B9FF,  // Sky Blue
        0xFFFFAB76,  // Orange
        0xFFA29BFE,  // Lavender
    };
    
    // Letters to display
    private static final char[] ALPHABET = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z'
    };
    
    public BubbleManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.bubbles = new ArrayList<>();
        this.random = new Random();
    }
    
    /**
     * Initialize bubbles for all letters
     */
    public void initBubbles(int count) {
        bubbles.clear();
        
        // Calculate grid positions to avoid initial overlap
        // For 26 letters, use 5 columns (5x6 grid = 30 cells, fits 26)
        int cols = 5;
        int rows = (int) Math.ceil((float) count / cols);
        float cellWidth = screenWidth / (float) cols;
        float cellHeight = (screenHeight * 0.85f) / rows;
        
        // Smaller base radius for 26 bubbles
        float baseRadius = Math.min(cellWidth, cellHeight) * 0.32f;
        baseRadius = Math.max(40, Math.min(baseRadius, 65));
        
        for (int i = 0; i < Math.min(count, ALPHABET.length); i++) {
            char letter = ALPHABET[i];
            int color = BUBBLE_COLORS[i % BUBBLE_COLORS.length];
            
            // Calculate grid position with some randomness
            int col = i % cols;
            int row = i / cols;
            
            float x = cellWidth * (col + 0.5f) + (random.nextFloat() - 0.5f) * cellWidth * 0.25f;
            float y = cellHeight * (row + 0.5f) + screenHeight * 0.05f + (random.nextFloat() - 0.5f) * cellHeight * 0.25f;
            
            // Ensure within bounds
            x = Math.max(baseRadius + 5, Math.min(screenWidth - baseRadius - 5, x));
            y = Math.max(baseRadius + 5, Math.min(screenHeight - baseRadius - 5, y));
            
            // Add slight size variation
            float radius = baseRadius * (0.85f + random.nextFloat() * 0.3f);
            
            Bubble bubble = new Bubble(letter, x, y, radius, color, screenWidth, screenHeight);
            bubbles.add(bubble);
        }
    }
    
    /**
     * Update all bubbles
     */
    public void update(float deltaTime) {
        // Update each bubble
        for (Bubble bubble : bubbles) {
            bubble.update(deltaTime);
        }
        
        // Handle collisions between bubbles
        for (int i = 0; i < bubbles.size(); i++) {
            for (int j = i + 1; j < bubbles.size(); j++) {
                bubbles.get(i).handleCollision(bubbles.get(j));
            }
        }
    }
    
    /**
     * Draw all bubbles
     */
    public void draw(Canvas canvas) {
        for (Bubble bubble : bubbles) {
            bubble.draw(canvas);
        }
    }
    
    /**
     * Find bubble at touch position
     */
    public Bubble getBubbleAt(float x, float y) {
        // Check in reverse order (top bubbles first)
        for (int i = bubbles.size() - 1; i >= 0; i--) {
            Bubble bubble = bubbles.get(i);
            if (!bubble.isPopped && bubble.contains(x, y)) {
                return bubble;
            }
        }
        return null;
    }
    
    /**
     * Pop a specific bubble
     */
    public void popBubble(Bubble bubble) {
        if (bubble != null && !bubble.isPopped) {
            bubble.pop();
        }
    }
    
    /**
     * Reset a popped bubble (when going back to bubble view)
     */
    public void resetBubble(char letter) {
        for (Bubble bubble : bubbles) {
            if (bubble.letter == letter) {
                bubble.isPopped = false;
                bubble.alpha = 1.0f;
                bubble.scale = 1.0f;
                
                // Reposition randomly
                bubble.x = random.nextFloat() * (screenWidth - bubble.baseRadius * 2) + bubble.baseRadius;
                bubble.y = random.nextFloat() * (screenHeight * 0.6f - bubble.baseRadius * 2) + bubble.baseRadius + screenHeight * 0.1f;
                
                // Reset velocity
                bubble.vx = (float) (Math.random() * 60 - 30);
                bubble.vy = (float) (Math.random() * 60 - 30);
                break;
            }
        }
    }
    
    /**
     * Fade out all bubbles except selected one
     */
    public void fadeOutExcept(Bubble selected, float targetAlpha) {
        for (Bubble bubble : bubbles) {
            if (bubble != selected && !bubble.isPopped) {
                bubble.alpha = targetAlpha;
            }
        }
    }
    
    /**
     * Restore all bubble alphas
     */
    public void restoreAlphas() {
        for (Bubble bubble : bubbles) {
            if (!bubble.isPopped) {
                bubble.alpha = 1.0f;
            }
        }
    }
    
    /**
     * Get all bubbles
     */
    public List<Bubble> getBubbles() {
        return bubbles;
    }
    
    /**
     * Get count of non-popped bubbles
     */
    public int getActiveBubbleCount() {
        int count = 0;
        for (Bubble bubble : bubbles) {
            if (!bubble.isPopped) count++;
        }
        return count;
    }
    
    /**
     * Update screen dimensions (for orientation change)
     */
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        
        // Update all bubbles' screen bounds
        for (Bubble bubble : bubbles) {
            bubble.x = Math.max(bubble.radius, Math.min(width - bubble.radius, bubble.x));
            bubble.y = Math.max(bubble.radius, Math.min(height - bubble.radius, bubble.y));
        }
    }
}
