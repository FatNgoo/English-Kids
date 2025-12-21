package com.edu.english.alphabet_adventure.engine;

import android.os.Handler;
import android.os.Looper;

import com.edu.english.alphabet_adventure.models.GameState;
import com.edu.english.alphabet_adventure.models.LetterToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Game engine that handles the game loop, token spawning, movement, and collision detection.
 */
public class GameEngine {

    public interface GameEventListener {
        void onTokensUpdated(List<LetterToken> tokens);
        void onCorrectLetter(char letter);
        void onWrongLetter(char letter, char expected);
        void onWordComplete();
        void onGameOver();
        void onLivesChanged(int lives);
    }

    private static final int TICK_INTERVAL = 16; // ~60 FPS
    private static final float TOKEN_SPEED = 8.5f; // Pixels per tick (70% faster)
    private static final float SPAWN_X = 1200f; // Start position (off screen right)
    private static final float COLLISION_X = 150f; // Mascot position
    private static final float COLLISION_THRESHOLD = 60f;
    private static final int LANE_COUNT = 5;
    private static final int SPAWN_DELAY = 500; // ms between waves

    private GameState gameState;
    private GameEventListener listener;
    private Handler handler;
    private Random random;
    private boolean isRunning;
    private float screenWidth;

    private Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            if (isRunning && gameState.isPlaying()) {
                tick();
                handler.postDelayed(this, TICK_INTERVAL);
            }
        }
    };

    public GameEngine(GameState gameState, GameEventListener listener) {
        this.gameState = gameState;
        this.listener = listener;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.isRunning = false;
        this.screenWidth = SPAWN_X;
    }

    public void setScreenWidth(float width) {
        this.screenWidth = width;
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            spawnWave();
            handler.post(gameLoop);
        }
    }

    public void pause() {
        isRunning = false;
        gameState.setStatus(GameState.Status.PAUSED);
    }

    public void resume() {
        if (gameState.getStatus() == GameState.Status.PAUSED) {
            gameState.setStatus(GameState.Status.PLAYING);
            isRunning = true;
            handler.post(gameLoop);
        }
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacks(gameLoop);
    }

    private void tick() {
        if (!gameState.isPlaying()) return;

        List<LetterToken> tokens = gameState.getTokens();
        boolean needsNewWave = true;
        LetterToken collidedToken = null;

        // Update token positions and check for collisions
        Iterator<LetterToken> iterator = tokens.iterator();
        while (iterator.hasNext()) {
            LetterToken token = iterator.next();
            if (!token.isActive()) continue;

            token.moveLeft(TOKEN_SPEED);
            needsNewWave = false;

            // Check collision with mascot
            if (token.getPositionX() <= COLLISION_X + COLLISION_THRESHOLD &&
                token.getPositionX() >= COLLISION_X - COLLISION_THRESHOLD &&
                token.getLane() == gameState.getCurrentLane()) {
                
                collidedToken = token;
                token.setActive(false);
            }

            // Remove tokens that went off screen
            if (token.getPositionX() < -100) {
                iterator.remove();
            }
        }

        // Handle collision
        if (collidedToken != null) {
            handleCollision(collidedToken);
        }

        // Spawn new wave if needed
        if (needsNewWave && gameState.isPlaying()) {
            handler.postDelayed(this::spawnWave, SPAWN_DELAY);
        }

        // Notify listener
        if (listener != null) {
            listener.onTokensUpdated(new ArrayList<>(tokens));
        }
    }

    private void handleCollision(LetterToken token) {
        char targetLetter = gameState.getTargetLetter();
        
        if (token.getLetter() == targetLetter) {
            // Correct letter!
            gameState.incrementIndex();
            gameState.addScore(10);
            
            if (listener != null) {
                listener.onCorrectLetter(token.getLetter());
            }

            // Check if word is complete
            if (gameState.isWordComplete()) {
                gameState.setStatus(GameState.Status.WIN);
                stop();
                if (listener != null) {
                    listener.onWordComplete();
                }
            } else {
                // Clear remaining tokens and spawn new wave
                gameState.clearTokens();
                handler.postDelayed(this::spawnWave, SPAWN_DELAY);
            }
        } else {
            // Wrong letter!
            gameState.decrementLives();
            
            if (listener != null) {
                listener.onWrongLetter(token.getLetter(), targetLetter);
                listener.onLivesChanged(gameState.getLives());
            }

            if (gameState.getLives() <= 0) {
                gameState.setStatus(GameState.Status.LOSE);
                stop();
                if (listener != null) {
                    listener.onGameOver();
                }
            } else {
                // Still have lives - clear tokens and spawn new wave
                gameState.clearTokens();
                handler.postDelayed(this::spawnWave, SPAWN_DELAY + 300); // Extra delay after wrong
            }
        }
    }

    /**
     * Spawn a wave of 5 letter tokens, one for each lane.
     * Ensures exactly one correct letter is included.
     */
    public void spawnWave() {
        if (!gameState.isPlaying() || gameState.getCurrentWord() == null) return;

        gameState.clearTokens();
        
        char targetLetter = gameState.getTargetLetter();
        int correctLane = random.nextInt(LANE_COUNT);

        // Generate distractor letters
        Set<Character> usedLetters = new HashSet<>();
        usedLetters.add(targetLetter);

        List<Character> distractors = new ArrayList<>();
        while (distractors.size() < LANE_COUNT - 1) {
            char c = (char) ('A' + random.nextInt(26));
            if (!usedLetters.contains(c)) {
                usedLetters.add(c);
                distractors.add(c);
            }
        }
        Collections.shuffle(distractors);

        // Create tokens for each lane
        int distractorIndex = 0;
        float startX = screenWidth > 0 ? screenWidth + 100 : SPAWN_X;
        
        for (int lane = 0; lane < LANE_COUNT; lane++) {
            char letter;
            boolean isCorrect;
            
            if (lane == correctLane) {
                letter = targetLetter;
                isCorrect = true;
            } else {
                letter = distractors.get(distractorIndex++);
                isCorrect = false;
            }
            
            LetterToken token = new LetterToken(letter, lane, startX, isCorrect);
            gameState.addToken(token);
        }

        if (listener != null) {
            listener.onTokensUpdated(new ArrayList<>(gameState.getTokens()));
        }
    }

    public void moveUp() {
        gameState.moveUp();
    }

    public void moveDown() {
        gameState.moveDown();
    }

    public int getCurrentLane() {
        return gameState.getCurrentLane();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
