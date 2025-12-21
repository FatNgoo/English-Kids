package com.edu.english.alphabet_pop_lab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * AlphabetGameView - Main SurfaceView for Alphabet Pop Lab game
 * Implements game loop with 60 FPS target
 */
public class AlphabetGameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    
    // Game thread
    private Thread gameThread;
    private boolean isRunning = false;
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME = 1000 / TARGET_FPS;
    
    // Screen dimensions
    private int screenWidth, screenHeight;
    
    // Game components
    private BubbleManager bubbleManager;
    private ParticleSystem particleSystem;
    private LetterAnimator letterAnimator;
    private FlashcardCarousel flashcardCarousel;
    private GameAudioManager audioManager;
    private ImageDisplay imageDisplay;
    
    // Game state
    public enum GameState {
        BUBBLES,        // Step 1: Floating bubbles
        POPPING,        // Step 2: Bubble just popped, showing particle effect
        LETTER_ZOOM,    // Step 3: Letter zooming to center
        FLASHCARDS      // Step 4: Showing flashcard carousel
    }
    
    private GameState currentState = GameState.BUBBLES;
    
    // Currently selected bubble/letter
    private Bubble selectedBubble = null;
    
    // Background gradient
    private Paint backgroundPaint;
    private LinearGradient backgroundGradient;
    
    // Dim overlay
    private Paint dimPaint;
    private float dimAlpha = 0f;
    private float targetDimAlpha = 0f;
    
    // Touch
    private float touchX, touchY;
    
    // Callback for activity
    private GameCallback gameCallback;
    
    public interface GameCallback {
        void onBackToMenu();
    }
    
    public AlphabetGameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        
        // Initialize audio manager
        audioManager = new GameAudioManager(context);
        
        // Initialize paints
        initPaints();
    }
    
    private void initPaints() {
        backgroundPaint = new Paint();
        
        dimPaint = new Paint();
        dimPaint.setStyle(Paint.Style.FILL);
    }
    
    private void initBackground() {
        // Create gradient background (pastel sky)
        backgroundGradient = new LinearGradient(
            0, 0, 0, screenHeight,
            new int[] {
                0xFF87CEEB,  // Sky blue top
                0xFFB0E0E6,  // Powder blue
                0xFFE0FFFF   // Light cyan bottom
            },
            new float[] {0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(backgroundGradient);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenWidth = getWidth();
        screenHeight = getHeight();
        
        initBackground();
        initGameComponents();
        
        // Start game loop
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        
        initBackground();
        
        if (bubbleManager != null) {
            bubbleManager.updateScreenSize(width, height);
        }
        if (letterAnimator != null) {
            letterAnimator.updateScreenSize(width, height);
        }
        if (flashcardCarousel != null) {
            flashcardCarousel.updateScreenSize(width, height);
        }
        if (imageDisplay != null) {
            imageDisplay.updateScreenSize(width, height);
        }
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        try {
            if (gameThread != null) {
                gameThread.join(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void initGameComponents() {
        // Initialize bubble manager with all 26 letters
        bubbleManager = new BubbleManager(screenWidth, screenHeight);
        bubbleManager.initBubbles(26);
        
        // Initialize particle system
        particleSystem = new ParticleSystem();
        
        // Initialize letter animator
        letterAnimator = new LetterAnimator(screenWidth, screenHeight);
        
        // Initialize flashcard carousel
        flashcardCarousel = new FlashcardCarousel(screenWidth, screenHeight);
        flashcardCarousel.setOnCardClickListener(card -> {
            if (audioManager != null) {
                audioManager.speakWord(card.word);
            }
            // Update image display with current card
            if (imageDisplay != null) {
                imageDisplay.setWord(card.word, card.letter, card.letterColor);
            }
        });
        
        // Initialize image display
        imageDisplay = new ImageDisplay(screenWidth, screenHeight);
    }
    
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        
        while (isRunning) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastTime) / 1_000_000_000f;
            lastTime = currentTime;
            
            // Cap delta time to prevent physics issues
            deltaTime = Math.min(deltaTime, 0.05f);
            
            update(deltaTime);
            render();
            
            // Frame rate control
            long frameTime = (System.nanoTime() - currentTime) / 1_000_000;
            if (frameTime < FRAME_TIME) {
                try {
                    Thread.sleep(FRAME_TIME - frameTime);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }
    }
    
    private void update(float deltaTime) {
        // Update dim overlay
        if (dimAlpha < targetDimAlpha) {
            dimAlpha = Math.min(targetDimAlpha, dimAlpha + deltaTime * 2f);
        } else if (dimAlpha > targetDimAlpha) {
            dimAlpha = Math.max(targetDimAlpha, dimAlpha - deltaTime * 2f);
        }
        
        // Update based on state
        switch (currentState) {
            case BUBBLES:
                bubbleManager.update(deltaTime);
                break;
                
            case POPPING:
                particleSystem.update(deltaTime);
                letterAnimator.update(deltaTime);
                
                // Wait for particle effect to mostly finish
                if (!particleSystem.hasActiveParticles() || 
                    letterAnimator.getCurrentState() == LetterAnimator.State.HOLD) {
                    // Transition to letter zoom state
                    currentState = GameState.LETTER_ZOOM;
                }
                break;
                
            case LETTER_ZOOM:
                letterAnimator.update(deltaTime);
                particleSystem.update(deltaTime);
                
                if (letterAnimator.isInCorner()) {
                    // Show flashcards
                    currentState = GameState.FLASHCARDS;
                    if (selectedBubble != null) {
                        flashcardCarousel.setLetter(
                            letterAnimator.getCurrentLetter(),
                            letterAnimator.getCurrentColor()
                        );
                        flashcardCarousel.show();
                        
                        // Show image display with first card
                        FlashcardCarousel.Flashcard firstCard = flashcardCarousel.getCurrentCard();
                        if (firstCard != null && imageDisplay != null) {
                            imageDisplay.setWord(firstCard.word, firstCard.letter, firstCard.letterColor);
                            imageDisplay.show();
                        }
                    }
                }
                break;
                
            case FLASHCARDS:
                letterAnimator.update(deltaTime);
                flashcardCarousel.update(deltaTime);
                particleSystem.update(deltaTime);
                
                // Update image display based on current flashcard
                if (imageDisplay != null) {
                    imageDisplay.update(deltaTime);
                    FlashcardCarousel.Flashcard currentCard = flashcardCarousel.getCurrentCard();
                    if (currentCard != null) {
                        imageDisplay.setWord(currentCard.word, currentCard.letter, currentCard.letterColor);
                    }
                }
                break;
        }
    }
    
    private void render() {
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
    
    private void drawGame(Canvas canvas) {
        // Draw gradient background
        canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);
        
        // Draw bubbles (except in flashcard state)
        if (currentState != GameState.FLASHCARDS) {
            bubbleManager.draw(canvas);
        }
        
        // Draw dim overlay
        if (dimAlpha > 0) {
            dimPaint.setColor(Color.argb((int) (dimAlpha * 150), 0, 0, 0));
            canvas.drawRect(0, 0, screenWidth, screenHeight, dimPaint);
        }
        
        // Draw particles
        particleSystem.draw(canvas);
        
        // Draw animated letter
        letterAnimator.draw(canvas);
        
        // Draw image display
        if (imageDisplay != null) {
            imageDisplay.draw(canvas);
        }
        
        // Draw flashcard carousel
        flashcardCarousel.draw(canvas);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(touchX, touchY);
                break;
                
            case MotionEvent.ACTION_MOVE:
                handleTouchMove(touchX, touchY);
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleTouchUp(touchX, touchY);
                break;
        }
        
        return true;
    }
    
    private void handleTouchDown(float x, float y) {
        switch (currentState) {
            case BUBBLES:
                // Check if bubble tapped
                Bubble bubble = bubbleManager.getBubbleAt(x, y);
                if (bubble != null) {
                    popBubble(bubble);
                }
                break;
                
            case FLASHCARDS:
                if (!flashcardCarousel.onTouchDown(x, y)) {
                    // Tap outside flashcards - go back to bubbles
                    goBackToBubbles();
                }
                break;
        }
    }
    
    private void handleTouchMove(float x, float y) {
        if (currentState == GameState.FLASHCARDS) {
            flashcardCarousel.onTouchMove(x, y);
        }
    }
    
    private void handleTouchUp(float x, float y) {
        if (currentState == GameState.FLASHCARDS) {
            flashcardCarousel.onTouchUp(x, y);
        }
    }
    
    /**
     * Pop a bubble and start the animation sequence
     */
    private void popBubble(Bubble bubble) {
        selectedBubble = bubble;
        
        // Create particle burst
        particleSystem.createBurst(bubble.x, bubble.y, bubble.baseColor, bubble.radius);
        
        // Pop the bubble
        bubble.pop();
        
        // Play pop sound and speak letter
        if (audioManager != null) {
            audioManager.playPopSound();
            audioManager.speakLetter(bubble.letter);
        }
        
        // Start letter animation
        letterAnimator.startZoomToCenter(bubble.letter, bubble.baseColor, bubble.x, bubble.y);
        
        // Fade other bubbles
        bubbleManager.fadeOutExcept(bubble, 0.3f);
        
        // Start dim effect
        targetDimAlpha = 0.3f;
        
        // Change state
        currentState = GameState.POPPING;
    }
    
    /**
     * Go back to bubbles view
     */
    private void goBackToBubbles() {
        // Hide flashcards immediately
        flashcardCarousel.hide();
        
        // Hide image display
        if (imageDisplay != null) {
            imageDisplay.hide();
        }
        
        // Fade out letter
        letterAnimator.reset();
        
        // Remove dim
        targetDimAlpha = 0f;
        dimAlpha = 0f;
        
        // Restore bubble alphas
        bubbleManager.restoreAlphas();
        
        // Reset selected bubble
        if (selectedBubble != null) {
            bubbleManager.resetBubble(selectedBubble.letter);
            selectedBubble = null;
        }
        
        // Change state immediately
        currentState = GameState.BUBBLES;
    }
    
    /**
     * Set game callback
     */
    public void setGameCallback(GameCallback callback) {
        this.gameCallback = callback;
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        isRunning = false;
        if (audioManager != null) {
            audioManager.release();
        }
    }
    
    /**
     * Toggle mute
     */
    public void toggleMute() {
        if (audioManager != null) {
            audioManager.setMuted(!audioManager.isMuted());
        }
    }
    
    /**
     * Check if muted
     */
    public boolean isMuted() {
        return audioManager != null && audioManager.isMuted();
    }
}
