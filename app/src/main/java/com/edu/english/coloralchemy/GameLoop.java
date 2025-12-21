package com.edu.english.coloralchemy;

/**
 * Game Loop Handler
 * Manages the timing and update cycle for smooth 60 FPS gameplay
 */
public class GameLoop implements Runnable {
    
    // Target 60 FPS
    private static final int TARGET_FPS = 60;
    private static final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
    private static final int MAX_FRAME_SKIP = 5;
    
    private Thread gameThread;
    private boolean isRunning;
    private boolean isPaused;
    
    private GameLoopCallback callback;
    
    // Timing variables
    private long lastUpdateTime;
    private float deltaTime;
    private int fps;
    private int frameCount;
    private long fpsTimer;
    
    /**
     * Interface for game loop callbacks
     */
    public interface GameLoopCallback {
        void onUpdate(float deltaTime);
        void onRender();
    }
    
    public GameLoop(GameLoopCallback callback) {
        this.callback = callback;
        this.isRunning = false;
        this.isPaused = false;
    }
    
    /**
     * Start the game loop
     */
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        isPaused = false;
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    /**
     * Stop the game loop
     */
    public void stop() {
        isRunning = false;
        if (gameThread != null) {
            try {
                gameThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Pause the game loop
     */
    public void pause() {
        isPaused = true;
    }
    
    /**
     * Resume the game loop
     */
    public void resume() {
        isPaused = false;
        lastUpdateTime = System.nanoTime();
    }
    
    /**
     * Check if game loop is running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Get current FPS
     */
    public int getFps() {
        return fps;
    }
    
    /**
     * Get delta time in seconds
     */
    public float getDeltaTime() {
        return deltaTime;
    }
    
    @Override
    public void run() {
        lastUpdateTime = System.nanoTime();
        fpsTimer = System.currentTimeMillis();
        frameCount = 0;
        
        while (isRunning) {
            if (isPaused) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            
            long now = System.nanoTime();
            long elapsedTime = now - lastUpdateTime;
            lastUpdateTime = now;
            
            // Calculate delta time in seconds
            deltaTime = elapsedTime / 1000000000.0f;
            
            // Cap delta time to prevent spiral of death
            if (deltaTime > 0.1f) {
                deltaTime = 0.1f;
            }
            
            // Update game logic
            if (callback != null) {
                callback.onUpdate(deltaTime);
            }
            
            // Render
            if (callback != null) {
                callback.onRender();
            }
            
            // FPS counter
            frameCount++;
            if (System.currentTimeMillis() - fpsTimer >= 1000) {
                fps = frameCount;
                frameCount = 0;
                fpsTimer = System.currentTimeMillis();
            }
            
            // Sleep to maintain target FPS
            long sleepTime = (OPTIMAL_TIME - (System.nanoTime() - now)) / 1000000;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
