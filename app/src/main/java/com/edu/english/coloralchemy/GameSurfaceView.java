package com.edu.english.coloralchemy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Game Surface View
 * Main game view with SurfaceView rendering and game loop
 */
public class GameSurfaceView extends SurfaceView implements 
        SurfaceHolder.Callback, 
        GameLoop.GameLoopCallback,
        SensorManagerHandler.OnSensorUpdateListener,
        ShadeController.OnShadeChangeListener,
        CollectionManager.OnCollectionUpdateListener {
    
    // Core components
    private GameLoop gameLoop;
    private GameRenderer renderer;
    private SensorManagerHandler sensorHandler;
    private SoundManager soundManager;
    private CollectionManager collectionManager;
    
    // Game objects
    private TestTube[] testTubes;
    private Beaker beaker;
    private ShadeController shadeController;
    
    // State
    private TestTube draggedTube;
    private boolean isInitialized;
    private boolean isPaused;
    
    // Result display
    private String currentResultName;
    private int currentResultColor;
    private float shadeValue;
    private int shadedColor;
    
    // UI Buttons
    private float resetButtonX, resetButtonY;
    private float resetButtonWidth = 150, resetButtonHeight = 50;
    private boolean resetButtonPressed;
    
    private float collectionButtonX, collectionButtonY;
    private float collectionButtonSize = 30;
    
    private float backButtonX, backButtonY;
    private float backButtonSize = 25;
    
    // Callbacks
    private OnGameEventListener gameEventListener;
    
    /**
     * Game event listener interface
     */
    public interface OnGameEventListener {
        void onBackPressed();
        void onCollectionPressed();
        void onColorCreated(String colorName, int color);
        void onShadeCreated(String shadeName, int color);
    }
    
    public GameSurfaceView(Context context) {
        super(context);
        init(context);
    }
    
    private void init(Context context) {
        // Setup surface
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(false);
        
        // Initialize components
        gameLoop = new GameLoop(this);
        renderer = new GameRenderer();
        sensorHandler = new SensorManagerHandler(context);
        soundManager = new SoundManager(context);
        collectionManager = new CollectionManager(context);
        
        // Set listeners
        sensorHandler.setListener(this);
        collectionManager.setListener(this);
        
        isInitialized = false;
        isPaused = false;
        draggedTube = null;
        resetButtonPressed = false;
        shadeValue = 0;
    }
    
    /**
     * Initialize game objects (called when surface is ready)
     */
    private void initializeGameObjects(int width, int height) {
        renderer.setScreenSize(width, height);
        
        // Calculate positions based on screen size
        float centerX = width / 2f;
        
        // Get all available tube colors (9 colors: 3 primary + 6 additional)
        int[] tubeColors = ColorMixer.getAllTubeColors();
        int totalTubes = tubeColors.length;
        
        // Calculate tube size - BIGGER and more prominent
        float tubeWidth = Math.min(width / 5f, 110); // Much larger tubes
        float tubeHeight = tubeWidth * 2.5f; // Maintain aspect ratio
        
        // Layout: 2 rows of tubes at the top
        int tubesPerRow = (totalTubes + 1) / 2; // 5 tubes per row for 9 tubes
        float tubeSpacing = (width - tubeWidth) / (tubesPerRow + 0.3f);
        
        // Position rows - Give more space
        float row1Y = height * 0.16f; // First row of tubes
        float row2Y = row1Y + tubeHeight * 0.55f; // Second row, slightly lower
        
        testTubes = new TestTube[totalTubes];
        
        // Create tubes in two rows
        for (int i = 0; i < totalTubes; i++) {
            int row = i / tubesPerRow;
            int col = i % tubesPerRow;
            
            float tubeY = (row == 0) ? row1Y : row2Y;
            
            // Calculate X position - center the rows
            int tubesInThisRow = (row == 0) ? Math.min(tubesPerRow, totalTubes) : 
                                              (totalTubes - tubesPerRow);
            float rowWidth = (tubesInThisRow - 1) * tubeSpacing;
            float startX = centerX - rowWidth / 2;
            float tubeX = startX + col * tubeSpacing;
            
            testTubes[i] = new TestTube(
                tubeX,
                tubeY,
                tubeWidth, tubeHeight,
                tubeColors[i]
            );
            
            // Set original positions for return animation
            testTubes[i].setOriginalPosition(tubeX, tubeY);
        }
        
        // Beaker (center, MUCH LARGER for better visibility)
        float beakerWidth = Math.min(width * 0.5f, 220); // Much larger beaker
        float beakerHeight = beakerWidth * 1.4f;
        beaker = new Beaker(
            centerX,
            height * 0.60f,
            beakerWidth, beakerHeight
        );
        
        // Shade controller (below beaker) - wider and positioned lower
        shadeController = new ShadeController(
            centerX,
            height * 0.84f,
            width * 0.85f
        );
        shadeController.setListener(this);
        
        // UI button positions - MUCH BIGGER buttons
        resetButtonX = centerX;
        resetButtonY = height - 55;
        resetButtonWidth = 260;  // Much wider
        resetButtonHeight = 70;  // Taller
        
        backButtonX = 60;
        backButtonY = 60;
        backButtonSize = 40;
        
        isInitialized = true;
    }
    
    // ==================== Surface Callbacks ====================
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Surface ready
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initializeGameObjects(width, height);
        
        if (!gameLoop.isRunning()) {
            gameLoop.start();
            sensorHandler.start();
        }
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        gameLoop.stop();
        sensorHandler.stop();
    }
    
    // ==================== Game Loop Callbacks ====================
    
    @Override
    public void onUpdate(float deltaTime) {
        if (!isInitialized || isPaused) return;
        
        // Update renderer
        renderer.update(deltaTime);
        
        // Update game objects
        for (TestTube tube : testTubes) {
            tube.update(deltaTime);
        }
        
        beaker.update(deltaTime);
        shadeController.update(deltaTime);
        
        // Check for pouring
        checkPouring();
    }
    
    @Override
    public void onRender() {
        if (!isInitialized) return;
        
        Canvas canvas = null;
        try {
            canvas = getHolder().lockCanvas();
            if (canvas != null) {
                synchronized (getHolder()) {
                    drawGame(canvas);
                }
            }
        } finally {
            if (canvas != null) {
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }
    
    /**
     * Draw all game elements
     */
    private void drawGame(Canvas canvas) {
        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        
        // Draw background
        renderer.drawBackground(canvas);
        
        // Draw title
        renderer.drawTitle(canvas);
        
        // Draw table
        renderer.drawTable(canvas);
        
        // Draw beaker (behind tubes when not dragging over it)
        beaker.draw(canvas);
        
        // Draw test tubes
        for (TestTube tube : testTubes) {
            if (tube != draggedTube) {
                tube.draw(canvas);
            }
        }
        
        // Draw dragged tube on top
        if (draggedTube != null) {
            draggedTube.draw(canvas);
        }
        
        // Draw shade slider if visible
        if (shadeController.isVisible()) {
            renderer.drawShadeSlider(canvas, shadeController, currentResultColor);
        }
        
        // Draw result text
        if (beaker.isResultReady() && currentResultName != null) {
            String displayName = shadeController.isVisible() ? 
                ColorMixer.getShadeName(beaker.getResultColorName(), shadeValue) :
                currentResultName;
            int displayColor = shadeController.isVisible() ? shadedColor : currentResultColor;
            
            renderer.drawResultText(canvas, displayName, displayColor, 
                renderer.getScreenHeight() * 0.15f);
        }
        
        // Draw hint
        if (beaker.getCurrentState() == Beaker.State.EMPTY) {
            renderer.drawHint(canvas, "Drag a color tube to the beaker!", 
                renderer.getScreenHeight() * 0.42f);
        } else if (beaker.getCurrentState() == Beaker.State.HAS_ONE_COLOR) {
            renderer.drawHint(canvas, "Add another color to mix!", 
                renderer.getScreenHeight() * 0.42f);
        } else if (beaker.getCurrentState() == Beaker.State.HAS_TWO_COLORS) {
            renderer.drawHint(canvas, "Shake to mix, or add a 3rd color!", 
                renderer.getScreenHeight() * 0.42f);
        }
        
        // Draw reset button (only when result is ready)
        if (beaker.isResultReady()) {
            renderer.drawResetButton(canvas, resetButtonX, resetButtonY, 
                resetButtonWidth, resetButtonHeight, resetButtonPressed);
        }
        
        // Draw back button only (Collection is now in ColorsActivity)
        renderer.drawBackButton(canvas, backButtonX, backButtonY, backButtonSize);
    }
    
    // ==================== Touch Handling ====================
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isInitialized) return false;
        
        float touchX = event.getX();
        float touchY = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return handleTouchDown(touchX, touchY);
                
            case MotionEvent.ACTION_MOVE:
                return handleTouchMove(touchX, touchY);
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return handleTouchUp(touchX, touchY);
        }
        
        return true;
    }
    
    private boolean handleTouchDown(float touchX, float touchY) {
        // Check back button
        if (isPointInCircle(touchX, touchY, backButtonX, backButtonY, backButtonSize + 10)) {
            if (gameEventListener != null) {
                gameEventListener.onBackPressed();
            }
            return true;
        }
        
        // Check reset button
        if (beaker.isResultReady() && isPointInRect(touchX, touchY, 
            resetButtonX - resetButtonWidth / 2, resetButtonY - resetButtonHeight / 2,
            resetButtonWidth, resetButtonHeight)) {
            resetButtonPressed = true;
            return true;
        }
        
        // Check shade slider
        if (shadeController.onTouchDown(touchX, touchY)) {
            return true;
        }
        
        // Check test tubes (can only drag if beaker can accept more colors - up to 3)
        if (beaker.canAddMoreColors()) {
            for (TestTube tube : testTubes) {
                if (tube.contains(touchX, touchY) && !tube.isEmpty() && tube.isEnabled()) {
                    draggedTube = tube;
                    tube.startDrag(touchX, touchY);
                    soundManager.playSFX(SoundManager.SFX_GLASS_CLINK);
                    return true;
                }
            }
        }
        
        return true;
    }
    
    private boolean handleTouchMove(float touchX, float touchY) {
        // Handle shade slider drag
        if (shadeController.isDragging()) {
            shadeController.onTouchMove(touchX, touchY);
            return true;
        }
        
        // Handle tube drag
        if (draggedTube != null) {
            draggedTube.drag(touchX, touchY);
            
            // Check if over beaker
            if (beaker.contains(touchX, touchY) && !draggedTube.isPouring()) {
                // Start pouring
                float[] pourTarget = beaker.getPourTargetPosition();
                draggedTube.startPouring(pourTarget[0], pourTarget[1]);
                soundManager.playSFX(SoundManager.SFX_LIQUID_POUR);
            }
            
            return true;
        }
        
        return true;
    }
    
    private boolean handleTouchUp(float touchX, float touchY) {
        // Handle reset button
        if (resetButtonPressed) {
            resetButtonPressed = false;
            if (isPointInRect(touchX, touchY, 
                resetButtonX - resetButtonWidth / 2, resetButtonY - resetButtonHeight / 2,
                resetButtonWidth, resetButtonHeight)) {
                resetGame();
            }
            return true;
        }
        
        // Handle shade slider
        if (shadeController.isDragging()) {
            shadeController.onTouchUp();
            return true;
        }
        
        // Handle tube release
        if (draggedTube != null) {
            // Check if was pouring into beaker
            if (draggedTube.isPouring() && beaker.contains(touchX, touchY)) {
                // Add color to beaker
                int tubeColor = draggedTube.getColor();
                beaker.addColor(tubeColor);
                
                soundManager.playSFX(SoundManager.SFX_DROP);
                
                // Check if ready to mix (with 2 or 3 colors)
                if (beaker.canMix()) {
                    soundManager.playSFX(SoundManager.SFX_BUBBLE);
                }
            }
            
            // Stop pouring and return tube
            draggedTube.stopPouring();
            draggedTube.returnToOrigin();
            draggedTube.endDrag();
            draggedTube = null;
        }
        
        return true;
    }
    
    // ==================== Sensor Callbacks ====================
    
    @Override
    public void onAccelerometerUpdate(float x, float y, float z) {
        if (!isInitialized) return;
        
        // Update all game objects with accelerometer
        for (TestTube tube : testTubes) {
            tube.updateAccelerometer(x, y, z);
        }
        beaker.updateAccelerometer(x, y, z);
    }
    
    @Override
    public void onShakeDetected(float intensity) {
        if (!isInitialized) return;
        
        // Apply shake to beaker
        beaker.applyShake(intensity);
        
        // If ready to mix, start mixing
        if (beaker.getCurrentState() == Beaker.State.READY_TO_MIX) {
            soundManager.playSFX(SoundManager.SFX_SHAKE);
        } else if (beaker.getCurrentState() == Beaker.State.MIXING) {
            soundManager.playSFX(SoundManager.SFX_BUBBLE, 0.8f + intensity * 0.4f);
        }
        
        // Check if mixing completed
        if (beaker.isResultReady() && currentResultName == null) {
            onMixingComplete();
        }
    }
    
    /**
     * Called when mixing is complete
     */
    private void onMixingComplete() {
        currentResultName = beaker.getResultColorName();
        currentResultColor = beaker.getResultColor();
        shadedColor = currentResultColor;
        
        // Show result animation
        renderer.showResult();
        
        // Play success sound and speak
        soundManager.playSFX(SoundManager.SFX_SUCCESS);
        soundManager.playSFX(SoundManager.SFX_SPARKLE);
        soundManager.speakDelayed(beaker.getResultSentence(), 500);
        
        // Add to collection
        collectionManager.addColorFromMix(currentResultColor, currentResultName);
        
        // Show shade controller
        shadeController.show(currentResultName);
        
        // Notify listener
        if (gameEventListener != null) {
            gameEventListener.onColorCreated(currentResultName, currentResultColor);
        }
    }
    
    // ==================== Shade Controller Callbacks ====================
    
    @Override
    public void onShadeChanged(float shadeVal, String shadeName) {
        this.shadeValue = shadeVal;
        this.shadedColor = ColorMixer.applyShade(currentResultColor, shadeVal);
        beaker.getLiquid().setColor(shadedColor);
    }
    
    @Override
    public void onShadeChangeStart() {
        soundManager.playSFX(SoundManager.SFX_SLIDER);
    }
    
    @Override
    public void onShadeChangeEnd() {
        // Save shade to collection if significantly different
        if (Math.abs(shadeValue) > 0.3f) {
            String shadeName = ColorMixer.getShadeName(currentResultName, shadeValue);
            boolean isNew = collectionManager.addColorShade(shadedColor, currentResultName, shadeValue);
            
            if (isNew) {
                soundManager.playSFX(SoundManager.SFX_SUCCESS);
                soundManager.speakDelayed(shadeName, 300);
                
                if (gameEventListener != null) {
                    gameEventListener.onShadeCreated(shadeName, shadedColor);
                }
            }
        }
    }
    
    // ==================== Collection Callbacks ====================
    
    @Override
    public void onColorCollected(CollectionManager.CollectedColor color) {
        // Could show a toast or notification
    }
    
    @Override
    public void onAchievementUnlocked(String achievement) {
        // Could show an achievement popup
    }
    
    // ==================== Game Control ====================
    
    /**
     * Check if tube is pouring into beaker
     */
    private void checkPouring() {
        for (TestTube tube : testTubes) {
            if (tube.isPouring() && tube.isEmpty()) {
                tube.stopPouring();
            }
        }
    }
    
    /**
     * Reset game for new mix
     */
    public void resetGame() {
        // Reset beaker
        beaker.reset();
        
        // Refill test tubes
        for (TestTube tube : testTubes) {
            tube.refill();
            tube.setEnabled(true);
        }
        
        // Hide shade controller
        shadeController.hide();
        shadeController.reset();
        
        // Reset result display
        renderer.hideResult();
        currentResultName = null;
        currentResultColor = Color.TRANSPARENT;
        shadeValue = 0;
        
        // Play sound
        soundManager.playSFX(SoundManager.SFX_TAP);
    }
    
    /**
     * Pause game
     */
    public void pause() {
        isPaused = true;
        gameLoop.pause();
        sensorHandler.stop();
        soundManager.pause();
    }
    
    /**
     * Resume game
     */
    public void resume() {
        isPaused = false;
        gameLoop.resume();
        sensorHandler.start();
        soundManager.resume();
    }
    
    /**
     * Release all resources
     */
    public void release() {
        gameLoop.stop();
        sensorHandler.stop();
        soundManager.release();
    }
    
    // ==================== Utility Methods ====================
    
    private boolean isPointInCircle(float px, float py, float cx, float cy, float radius) {
        float dx = px - cx;
        float dy = py - cy;
        return dx * dx + dy * dy <= radius * radius;
    }
    
    private boolean isPointInRect(float px, float py, float left, float top, float width, float height) {
        return px >= left && px <= left + width && py >= top && py <= top + height;
    }
    
    // ==================== Setters ====================
    
    public void setGameEventListener(OnGameEventListener listener) {
        this.gameEventListener = listener;
    }
    
    public CollectionManager getCollectionManager() {
        return collectionManager;
    }
    
    public SoundManager getSoundManager() {
        return soundManager;
    }
}
