package com.edu.english.magicmelody.core.game;

import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;

/**
 * GameLoop - Main game loop using Choreographer for smooth 60fps
 * Handles timing and update callbacks without allocations
 */
public class GameLoop implements Choreographer.FrameCallback {
    
    private static final long TARGET_FRAME_TIME = 16; // ~60 FPS (16.67ms)
    
    private final Choreographer choreographer;
    private final Handler handler;
    private boolean isRunning = false;
    private boolean isPaused = false;
    
    private long startTime;
    private long lastFrameTime;
    private long gameTime;        // Time elapsed since start
    private long pauseStartTime;
    private long totalPauseTime;
    
    private int frameCount;
    private float fps;
    private long fpsLastUpdate;
    
    // Callbacks
    public interface GameLoopListener {
        void onUpdate(long gameTimeMs, float deltaTime);
        void onRender();
    }
    
    private GameLoopListener listener;
    
    // Fallback handler-based loop for older devices
    private final Runnable fallbackRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning || isPaused) return;
            
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f;
            lastFrameTime = currentTime;
            
            gameTime = (currentTime - startTime - (totalPauseTime * 1_000_000)) / 1_000_000;
            
            if (listener != null) {
                listener.onUpdate(gameTime, deltaTime);
                listener.onRender();
            }
            
            updateFps();
            
            handler.postDelayed(this, TARGET_FRAME_TIME);
        }
    };
    
    public GameLoop() {
        this.choreographer = Choreographer.getInstance();
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Start the game loop
     */
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        isPaused = false;
        startTime = System.nanoTime();
        lastFrameTime = startTime;
        gameTime = 0;
        totalPauseTime = 0;
        frameCount = 0;
        fps = 0;
        fpsLastUpdate = startTime;
        
        // Use Choreographer for smooth vsync-aligned frames
        choreographer.postFrameCallback(this);
    }
    
    /**
     * Stop the game loop
     */
    public void stop() {
        isRunning = false;
        isPaused = false;
        choreographer.removeFrameCallback(this);
        handler.removeCallbacks(fallbackRunnable);
    }
    
    /**
     * Pause the game loop
     */
    public void pause() {
        if (!isRunning || isPaused) return;
        isPaused = true;
        pauseStartTime = System.nanoTime();
    }
    
    /**
     * Resume the game loop
     */
    public void resume() {
        if (!isRunning || !isPaused) return;
        isPaused = false;
        totalPauseTime += (System.nanoTime() - pauseStartTime) / 1_000_000;
        lastFrameTime = System.nanoTime();
        choreographer.postFrameCallback(this);
    }
    
    @Override
    public void doFrame(long frameTimeNanos) {
        if (!isRunning) return;
        
        if (isPaused) {
            // Still schedule next frame to detect resume
            choreographer.postFrameCallback(this);
            return;
        }
        
        // Calculate delta time in seconds
        float deltaTime = (frameTimeNanos - lastFrameTime) / 1_000_000_000f;
        lastFrameTime = frameTimeNanos;
        
        // Cap delta time to prevent huge jumps
        if (deltaTime > 0.1f) {
            deltaTime = 0.016f;
        }
        
        // Calculate game time (excluding pause time)
        gameTime = (frameTimeNanos - startTime - (totalPauseTime * 1_000_000)) / 1_000_000;
        
        // Update game logic
        if (listener != null) {
            listener.onUpdate(gameTime, deltaTime);
            listener.onRender();
        }
        
        updateFps();
        
        // Schedule next frame
        choreographer.postFrameCallback(this);
    }
    
    private void updateFps() {
        frameCount++;
        long now = System.nanoTime();
        long elapsed = now - fpsLastUpdate;
        
        if (elapsed >= 1_000_000_000) { // 1 second
            fps = frameCount * 1_000_000_000f / elapsed;
            frameCount = 0;
            fpsLastUpdate = now;
        }
    }
    
    /**
     * Get current game time in milliseconds
     */
    public long getGameTime() {
        return gameTime;
    }
    
    /**
     * Get current FPS
     */
    public float getFps() {
        return fps;
    }
    
    /**
     * Check if running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Check if paused
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Set game loop listener
     */
    public void setListener(GameLoopListener listener) {
        this.listener = listener;
    }
    
    /**
     * Get start time
     */
    public long getStartTime() {
        return startTime / 1_000_000; // Convert to milliseconds
    }
}
