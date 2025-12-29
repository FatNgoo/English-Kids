package com.edu.english.masterchef.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages game state transitions for Master Chef
 * Ensures valid state flow: MAP → ORDERING → SHOPPING → COOKING → COMPLETED
 */
public class GameStateMachine {
    
    public enum GameState {
        IDLE,           // Initial state
        MAP_LOADED,     // Map screen loaded
        LEVEL_SELECTED, // Level selected, showing intro
        ORDERING,       // Scene 1: Restaurant ordering
        SHOPPING,       // Scene 2: Supermarket shopping
        COOKING,        // Scene 3: Kitchen cooking
        COMPLETED       // Level completed, showing results
    }

    private GameState currentState = GameState.IDLE;
    private List<StateChangeListener> listeners = new ArrayList<>();

    public interface StateChangeListener {
        void onStateChanged(GameState oldState, GameState newState);
    }

    public GameStateMachine() {
    }

    /**
     * Transition to a new state
     * @param newState Target state
     * @return true if transition was successful
     */
    public boolean transitionTo(GameState newState) {
        if (isValidTransition(currentState, newState)) {
            GameState oldState = currentState;
            currentState = newState;
            notifyListeners(oldState, newState);
            return true;
        }
        return false;
    }

    /**
     * Check if a transition is valid
     */
    private boolean isValidTransition(GameState from, GameState to) {
        // Define valid transitions
        switch (from) {
            case IDLE:
                return to == GameState.MAP_LOADED;
                
            case MAP_LOADED:
                // Allow direct transition to ORDERING for simplified flow
                return to == GameState.LEVEL_SELECTED || to == GameState.ORDERING || to == GameState.IDLE;
                
            case LEVEL_SELECTED:
                return to == GameState.ORDERING || to == GameState.MAP_LOADED;
                
            case ORDERING:
                return to == GameState.SHOPPING || to == GameState.MAP_LOADED;
                
            case SHOPPING:
                return to == GameState.COOKING || to == GameState.ORDERING;
                
            case COOKING:
                return to == GameState.COMPLETED || to == GameState.SHOPPING;
                
            case COMPLETED:
                return to == GameState.MAP_LOADED || to == GameState.IDLE;
                
            default:
                return false;
        }
    }

    /**
     * Get current state
     */
    public GameState getCurrentState() {
        return currentState;
    }

    /**
     * Reset to initial state
     */
    public void reset() {
        transitionTo(GameState.IDLE);
    }

    /**
     * Add state change listener
     */
    public void addStateChangeListener(StateChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Set single state change listener (convenience method for lambdas)
     */
    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.singleListener = listener;
    }

    public interface OnStateChangeListener {
        void onStateChange(GameState newState);
    }

    private OnStateChangeListener singleListener;

    /**
     * Remove state change listener
     */
    public void removeStateChangeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of state change
     */
    private void notifyListeners(GameState oldState, GameState newState) {
        for (StateChangeListener listener : listeners) {
            listener.onStateChanged(oldState, newState);
        }
        if (singleListener != null) {
            singleListener.onStateChange(newState);
        }
    }

    /**
     * Check if currently in gameplay (not map/menu)
     */
    public boolean isInGameplay() {
        return currentState == GameState.ORDERING ||
               currentState == GameState.SHOPPING ||
               currentState == GameState.COOKING;
    }
}
