package com.edu.english.magicmelody.gameplay.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎮 Game State Manager
 * 
 * Manages the overall game state machine:
 * - State transitions
 * - Lifecycle events
 * - Game flow control
 */
public class GameStateManager {
    
    private static final String TAG = "GameStateManager";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 GAME STATES
    // ═══════════════════════════════════════════════════════════════
    
    public enum GameState {
        IDLE,           // Not started
        LOADING,        // Loading assets
        READY,          // Ready to start
        COUNTDOWN,      // Countdown before gameplay
        PLAYING,        // Active gameplay
        PAUSED,         // Game paused
        FINISHING,      // Post-gameplay (last notes)
        RESULTS,        // Showing results
        COMPLETED,      // Game completed
        FAILED,         // Game failed (optional)
        ERROR           // Error state
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 STATE VARIABLES
    // ═══════════════════════════════════════════════════════════════
    
    private GameState currentState = GameState.IDLE;
    private GameState previousState = GameState.IDLE;
    private long stateStartTimeMs = 0;
    private long totalPlayTimeMs = 0;
    private long pauseStartTimeMs = 0;
    
    // Countdown configuration
    private int countdownSeconds = 3;
    private int currentCountdown = 0;
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENERS
    // ═══════════════════════════════════════════════════════════════
    
    private final List<GameStateListener> listeners = new ArrayList<>();
    
    public interface GameStateListener {
        void onStateChanged(GameState oldState, GameState newState);
        void onCountdownTick(int secondsRemaining);
        void onGameStarted();
        void onGamePaused();
        void onGameResumed();
        void onGameEnded(boolean completed);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public GameStateManager() {
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setCountdownSeconds(int seconds) {
        this.countdownSeconds = seconds;
    }
    
    public void addListener(GameStateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(GameStateListener listener) {
        listeners.remove(listener);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 STATE TRANSITIONS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Transition to a new state
     */
    private void transitionTo(GameState newState) {
        if (currentState == newState) return;
        
        // Validate transition
        if (!isValidTransition(currentState, newState)) {
            android.util.Log.w(TAG, "Invalid transition: " + currentState + " -> " + newState);
            return;
        }
        
        previousState = currentState;
        currentState = newState;
        stateStartTimeMs = System.currentTimeMillis();
        
        // Notify listeners
        notifyStateChanged(previousState, newState);
    }
    
    /**
     * Check if transition is valid
     */
    private boolean isValidTransition(GameState from, GameState to) {
        switch (from) {
            case IDLE:
                return to == GameState.LOADING || to == GameState.READY;
            case LOADING:
                return to == GameState.READY || to == GameState.ERROR;
            case READY:
                return to == GameState.COUNTDOWN || to == GameState.PLAYING;
            case COUNTDOWN:
                return to == GameState.PLAYING || to == GameState.PAUSED;
            case PLAYING:
                return to == GameState.PAUSED || to == GameState.FINISHING || 
                       to == GameState.RESULTS || to == GameState.FAILED;
            case PAUSED:
                return to == GameState.PLAYING || to == GameState.COUNTDOWN || 
                       to == GameState.RESULTS || to == GameState.IDLE;
            case FINISHING:
                return to == GameState.RESULTS;
            case RESULTS:
                return to == GameState.IDLE || to == GameState.READY || 
                       to == GameState.COMPLETED;
            case COMPLETED:
            case FAILED:
                return to == GameState.IDLE || to == GameState.READY;
            case ERROR:
                return to == GameState.IDLE;
            default:
                return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 STATE ACTIONS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Start loading phase
     */
    public void startLoading() {
        transitionTo(GameState.LOADING);
    }
    
    /**
     * Mark loading as complete
     */
    public void onLoadingComplete() {
        transitionTo(GameState.READY);
    }
    
    /**
     * Mark loading as failed
     */
    public void onLoadingFailed(String error) {
        transitionTo(GameState.ERROR);
    }
    
    /**
     * Start countdown
     */
    public void startCountdown() {
        currentCountdown = countdownSeconds;
        transitionTo(GameState.COUNTDOWN);
        notifyCountdown(currentCountdown);
    }
    
    /**
     * Tick countdown (call every second)
     */
    public void tickCountdown() {
        if (currentState != GameState.COUNTDOWN) return;
        
        currentCountdown--;
        
        if (currentCountdown > 0) {
            notifyCountdown(currentCountdown);
        } else {
            // Countdown finished - start playing
            startPlaying();
        }
    }
    
    /**
     * Start gameplay immediately
     */
    public void startPlaying() {
        transitionTo(GameState.PLAYING);
        notifyGameStarted();
    }
    
    /**
     * Pause the game
     */
    public void pause() {
        if (currentState == GameState.PLAYING || currentState == GameState.COUNTDOWN) {
            pauseStartTimeMs = System.currentTimeMillis();
            transitionTo(GameState.PAUSED);
            notifyGamePaused();
        }
    }
    
    /**
     * Resume the game
     */
    public void resume() {
        if (currentState == GameState.PAUSED) {
            // Calculate pause duration
            long pauseDuration = System.currentTimeMillis() - pauseStartTimeMs;
            
            // Resume to previous state
            if (previousState == GameState.COUNTDOWN) {
                transitionTo(GameState.COUNTDOWN);
            } else {
                transitionTo(GameState.PLAYING);
            }
            
            notifyGameResumed();
        }
    }
    
    /**
     * Start finishing phase (all notes spawned)
     */
    public void startFinishing() {
        if (currentState == GameState.PLAYING) {
            transitionTo(GameState.FINISHING);
        }
    }
    
    /**
     * Show results
     */
    public void showResults() {
        if (currentState == GameState.PLAYING || currentState == GameState.FINISHING) {
            totalPlayTimeMs = System.currentTimeMillis() - stateStartTimeMs;
            transitionTo(GameState.RESULTS);
            notifyGameEnded(true);
        }
    }
    
    /**
     * Mark game as complete
     */
    public void complete() {
        transitionTo(GameState.COMPLETED);
    }
    
    /**
     * Mark game as failed
     */
    public void fail() {
        transitionTo(GameState.FAILED);
        notifyGameEnded(false);
    }
    
    /**
     * Quit/abort game
     */
    public void quit() {
        transitionTo(GameState.IDLE);
        notifyGameEnded(false);
    }
    
    /**
     * Reset to initial state
     */
    public void reset() {
        currentState = GameState.IDLE;
        previousState = GameState.IDLE;
        stateStartTimeMs = 0;
        totalPlayTimeMs = 0;
        pauseStartTimeMs = 0;
        currentCountdown = 0;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE QUERIES
    // ═══════════════════════════════════════════════════════════════
    
    public GameState getCurrentState() {
        return currentState;
    }
    
    public GameState getPreviousState() {
        return previousState;
    }
    
    public boolean isIdle() {
        return currentState == GameState.IDLE;
    }
    
    public boolean isLoading() {
        return currentState == GameState.LOADING;
    }
    
    public boolean isReady() {
        return currentState == GameState.READY;
    }
    
    public boolean isCountingDown() {
        return currentState == GameState.COUNTDOWN;
    }
    
    public boolean isPlaying() {
        return currentState == GameState.PLAYING;
    }
    
    public boolean isPaused() {
        return currentState == GameState.PAUSED;
    }
    
    public boolean isFinishing() {
        return currentState == GameState.FINISHING;
    }
    
    public boolean isShowingResults() {
        return currentState == GameState.RESULTS;
    }
    
    public boolean isGameActive() {
        return currentState == GameState.PLAYING || 
               currentState == GameState.COUNTDOWN ||
               currentState == GameState.FINISHING;
    }
    
    public boolean isGameOver() {
        return currentState == GameState.RESULTS ||
               currentState == GameState.COMPLETED ||
               currentState == GameState.FAILED;
    }
    
    public long getTimeInCurrentState() {
        return System.currentTimeMillis() - stateStartTimeMs;
    }
    
    public long getTotalPlayTimeMs() {
        if (isPlaying()) {
            return System.currentTimeMillis() - stateStartTimeMs;
        }
        return totalPlayTimeMs;
    }
    
    public int getCurrentCountdown() {
        return currentCountdown;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════
    
    private void notifyStateChanged(GameState oldState, GameState newState) {
        for (GameStateListener listener : listeners) {
            listener.onStateChanged(oldState, newState);
        }
    }
    
    private void notifyCountdown(int seconds) {
        for (GameStateListener listener : listeners) {
            listener.onCountdownTick(seconds);
        }
    }
    
    private void notifyGameStarted() {
        for (GameStateListener listener : listeners) {
            listener.onGameStarted();
        }
    }
    
    private void notifyGamePaused() {
        for (GameStateListener listener : listeners) {
            listener.onGamePaused();
        }
    }
    
    private void notifyGameResumed() {
        for (GameStateListener listener : listeners) {
            listener.onGameResumed();
        }
    }
    
    private void notifyGameEnded(boolean completed) {
        for (GameStateListener listener : listeners) {
            listener.onGameEnded(completed);
        }
    }
}
