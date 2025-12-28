package com.edu.english.magicmelody.gameplay.engine;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎵 Rhythm Engine
 * 
 * Core rhythm game engine handling:
 * - Beat timing & BPM synchronization
 * - Game loop at 60fps
 * - Audio synchronization
 * - Time management
 */
public class RhythmEngine {
    
    private static final String TAG = "RhythmEngine";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 TIMING CONSTANTS
    // ═══════════════════════════════════════════════════════════════
    
    private static final long FRAME_TIME_MS = 16; // ~60fps
    private static final long FRAME_TIME_NS = 16_666_666; // 16.67ms in nanoseconds
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 ENGINE STATE
    // ═══════════════════════════════════════════════════════════════
    
    public enum EngineState {
        IDLE,
        READY,
        RUNNING,
        PAUSED,
        STOPPED
    }
    
    private EngineState state = EngineState.IDLE;
    
    // ═══════════════════════════════════════════════════════════════
    // ⏱️ TIMING VARIABLES
    // ═══════════════════════════════════════════════════════════════
    
    private float bpm = 120f;
    private float beatDurationMs;
    private long songStartTimeMs;
    private long pauseTimeMs;
    private long totalPausedTimeMs;
    private long lastFrameTimeNs;
    
    // Current timing
    private float currentSongTimeMs;
    private float currentBeat;
    private int currentBeatNumber;
    private float beatProgress; // 0-1 within current beat
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 GAME LOOP
    // ═══════════════════════════════════════════════════════════════
    
    private Handler gameLoopHandler;
    private Runnable gameLoopRunnable;
    private boolean isLoopRunning = false;
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENERS
    // ═══════════════════════════════════════════════════════════════
    
    private final List<RhythmEngineListener> listeners = new ArrayList<>();
    private final List<BeatListener> beatListeners = new ArrayList<>();
    
    public interface RhythmEngineListener {
        void onEngineUpdate(float deltaTimeMs, float songTimeMs);
        void onBeatTick(int beatNumber, float beatProgress);
        void onStateChanged(EngineState newState);
    }
    
    public interface BeatListener {
        void onBeat(int beatNumber);
        void onHalfBeat(int beatNumber);
        void onQuarterBeat(int beatNumber, int quarter);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public RhythmEngine() {
        this(120f);
    }
    
    public RhythmEngine(float bpm) {
        this.bpm = bpm;
        this.beatDurationMs = 60000f / bpm;
        this.gameLoopHandler = new Handler(Looper.getMainLooper());
        
        initGameLoop();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 GAME LOOP SETUP
    // ═══════════════════════════════════════════════════════════════
    
    private void initGameLoop() {
        gameLoopRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isLoopRunning || state != EngineState.RUNNING) {
                    return;
                }
                
                // Calculate delta time
                long currentTimeNs = System.nanoTime();
                float deltaTimeMs = (currentTimeNs - lastFrameTimeNs) / 1_000_000f;
                lastFrameTimeNs = currentTimeNs;
                
                // Clamp delta time to prevent huge jumps
                deltaTimeMs = Math.min(deltaTimeMs, 50f);
                
                // Update timing
                updateTiming();
                
                // Notify listeners
                notifyUpdate(deltaTimeMs);
                
                // Check for beat events
                checkBeatEvents();
                
                // Schedule next frame
                long nextFrameDelay = Math.max(1, FRAME_TIME_MS - (long)(deltaTimeMs - FRAME_TIME_MS));
                gameLoopHandler.postDelayed(this, nextFrameDelay);
            }
        };
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⏱️ TIMING UPDATES
    // ═══════════════════════════════════════════════════════════════
    
    private void updateTiming() {
        long currentRealTimeMs = SystemClock.elapsedRealtime();
        currentSongTimeMs = currentRealTimeMs - songStartTimeMs - totalPausedTimeMs;
        
        // Calculate beat information
        currentBeat = currentSongTimeMs / beatDurationMs;
        currentBeatNumber = (int) Math.floor(currentBeat);
        beatProgress = currentBeat - currentBeatNumber;
    }
    
    private int lastNotifiedBeat = -1;
    private int lastNotifiedHalfBeat = -1;
    private int lastNotifiedQuarter = -1;
    
    private void checkBeatEvents() {
        // Full beat
        if (currentBeatNumber > lastNotifiedBeat) {
            lastNotifiedBeat = currentBeatNumber;
            notifyBeat(currentBeatNumber);
        }
        
        // Half beat (0.5)
        int halfBeat = (int)(currentBeat * 2);
        if (halfBeat > lastNotifiedHalfBeat) {
            lastNotifiedHalfBeat = halfBeat;
            if (halfBeat % 2 == 1) { // Only notify on the half
                notifyHalfBeat(currentBeatNumber);
            }
        }
        
        // Quarter beat (0.25, 0.5, 0.75)
        int quarterBeat = (int)(currentBeat * 4);
        if (quarterBeat > lastNotifiedQuarter) {
            lastNotifiedQuarter = quarterBeat;
            notifyQuarterBeat(currentBeatNumber, quarterBeat % 4);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎮 ENGINE CONTROLS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Prepare the engine with given BPM
     */
    public void prepare(float bpm) {
        this.bpm = bpm;
        this.beatDurationMs = 60000f / bpm;
        this.state = EngineState.READY;
        
        resetTimingState();
        notifyStateChanged();
    }
    
    /**
     * Start the rhythm engine
     */
    public void start() {
        if (state != EngineState.READY && state != EngineState.PAUSED) {
            prepare(bpm);
        }
        
        if (state == EngineState.PAUSED) {
            // Resume from pause
            long pauseDuration = SystemClock.elapsedRealtime() - pauseTimeMs;
            totalPausedTimeMs += pauseDuration;
        } else {
            // Fresh start
            songStartTimeMs = SystemClock.elapsedRealtime();
            totalPausedTimeMs = 0;
            resetTimingState();
        }
        
        lastFrameTimeNs = System.nanoTime();
        state = EngineState.RUNNING;
        isLoopRunning = true;
        
        notifyStateChanged();
        
        // Start the game loop
        gameLoopHandler.post(gameLoopRunnable);
    }
    
    /**
     * Pause the engine
     */
    public void pause() {
        if (state != EngineState.RUNNING) return;
        
        pauseTimeMs = SystemClock.elapsedRealtime();
        state = EngineState.PAUSED;
        isLoopRunning = false;
        
        gameLoopHandler.removeCallbacks(gameLoopRunnable);
        notifyStateChanged();
    }
    
    /**
     * Resume from pause
     */
    public void resume() {
        if (state != EngineState.PAUSED) return;
        start();
    }
    
    /**
     * Stop the engine completely
     */
    public void stop() {
        state = EngineState.STOPPED;
        isLoopRunning = false;
        
        gameLoopHandler.removeCallbacks(gameLoopRunnable);
        notifyStateChanged();
    }
    
    /**
     * Reset the engine to initial state
     */
    public void reset() {
        stop();
        resetTimingState();
        state = EngineState.IDLE;
        notifyStateChanged();
    }
    
    private void resetTimingState() {
        lastNotifiedBeat = -1;
        lastNotifiedHalfBeat = -1;
        lastNotifiedQuarter = -1;
        currentSongTimeMs = 0;
        currentBeat = 0;
        currentBeatNumber = 0;
        beatProgress = 0;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 TIMING UTILITIES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Convert beat number to milliseconds
     */
    public float beatToMs(float beat) {
        return beat * beatDurationMs;
    }
    
    /**
     * Convert milliseconds to beat number
     */
    public float msToBeat(float ms) {
        return ms / beatDurationMs;
    }
    
    /**
     * Get time until next beat in milliseconds
     */
    public float getTimeUntilNextBeat() {
        return (1f - beatProgress) * beatDurationMs;
    }
    
    /**
     * Get time since last beat in milliseconds
     */
    public float getTimeSinceLastBeat() {
        return beatProgress * beatDurationMs;
    }
    
    /**
     * Check if we're currently on a beat (within tolerance)
     */
    public boolean isOnBeat(float toleranceMs) {
        float timeSinceBeat = getTimeSinceLastBeat();
        float timeUntilBeat = getTimeUntilNextBeat();
        
        return timeSinceBeat < toleranceMs || timeUntilBeat < toleranceMs;
    }
    
    /**
     * Sync to external audio time
     */
    public void syncToAudioTime(float audioTimeMs) {
        float drift = currentSongTimeMs - audioTimeMs;
        
        // If drift is significant, adjust
        if (Math.abs(drift) > 50) { // 50ms threshold
            totalPausedTimeMs -= (long) drift;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public EngineState getState() {
        return state;
    }
    
    public boolean isRunning() {
        return state == EngineState.RUNNING;
    }
    
    public boolean isPaused() {
        return state == EngineState.PAUSED;
    }
    
    public float getBpm() {
        return bpm;
    }
    
    public float getBeatDurationMs() {
        return beatDurationMs;
    }
    
    public float getCurrentSongTimeMs() {
        return currentSongTimeMs;
    }
    
    public float getCurrentBeat() {
        return currentBeat;
    }
    
    public int getCurrentBeatNumber() {
        return currentBeatNumber;
    }
    
    public float getBeatProgress() {
        return beatProgress;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════
    
    public void addListener(RhythmEngineListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(RhythmEngineListener listener) {
        listeners.remove(listener);
    }
    
    public void addBeatListener(BeatListener listener) {
        if (!beatListeners.contains(listener)) {
            beatListeners.add(listener);
        }
    }
    
    public void removeBeatListener(BeatListener listener) {
        beatListeners.remove(listener);
    }
    
    private void notifyUpdate(float deltaTimeMs) {
        for (RhythmEngineListener listener : listeners) {
            listener.onEngineUpdate(deltaTimeMs, currentSongTimeMs);
            listener.onBeatTick(currentBeatNumber, beatProgress);
        }
    }
    
    private void notifyBeat(int beatNumber) {
        for (BeatListener listener : beatListeners) {
            listener.onBeat(beatNumber);
        }
    }
    
    private void notifyHalfBeat(int beatNumber) {
        for (BeatListener listener : beatListeners) {
            listener.onHalfBeat(beatNumber);
        }
    }
    
    private void notifyQuarterBeat(int beatNumber, int quarter) {
        for (BeatListener listener : beatListeners) {
            listener.onQuarterBeat(beatNumber, quarter);
        }
    }
    
    private void notifyStateChanged() {
        for (RhythmEngineListener listener : listeners) {
            listener.onStateChanged(state);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🧹 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    public void release() {
        stop();
        listeners.clear();
        beatListeners.clear();
        gameLoopHandler.removeCallbacksAndMessages(null);
    }
}
