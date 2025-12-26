package com.edu.english.magicmelody.core.audio;

import android.os.Handler;
import android.os.Looper;

/**
 * Metronome - Provides timing ticks for rhythm gameplay
 * Can be used for practice mode or as timing reference
 */
public class Metronome {
    
    private static final int DEFAULT_BPM = 90;
    
    private final ToneEngine toneEngine;
    private final Handler handler;
    private int bpm;
    private long intervalMs;
    private boolean isRunning = false;
    private int tickCount = 0;
    
    // Callbacks
    public interface OnTickListener {
        void onTick(int tickNumber, long timestamp);
    }
    
    private OnTickListener tickListener;
    private boolean playSound = false;
    
    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;
            
            tickCount++;
            long timestamp = System.currentTimeMillis();
            
            // Play tick sound if enabled
            if (playSound) {
                // High tick on beat 1, low on others
                double freq = (tickCount % 4 == 1) ? 880.0 : 440.0;
                toneEngine.playFrequency(freq, 30);
            }
            
            // Notify listener
            if (tickListener != null) {
                tickListener.onTick(tickCount, timestamp);
            }
            
            // Schedule next tick
            handler.postDelayed(this, intervalMs);
        }
    };
    
    public Metronome() {
        this(DEFAULT_BPM);
    }
    
    public Metronome(int bpm) {
        this.toneEngine = new ToneEngine();
        this.handler = new Handler(Looper.getMainLooper());
        setBpm(bpm);
    }
    
    /**
     * Set BPM (beats per minute)
     */
    public void setBpm(int bpm) {
        this.bpm = Math.max(30, Math.min(240, bpm));
        this.intervalMs = 60000 / this.bpm;
    }
    
    /**
     * Get current BPM
     */
    public int getBpm() {
        return bpm;
    }
    
    /**
     * Get interval between ticks in milliseconds
     */
    public long getIntervalMs() {
        return intervalMs;
    }
    
    /**
     * Start the metronome
     */
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        tickCount = 0;
        handler.post(tickRunnable);
    }
    
    /**
     * Stop the metronome
     */
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(tickRunnable);
        toneEngine.stop();
    }
    
    /**
     * Check if running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Get current tick count
     */
    public int getTickCount() {
        return tickCount;
    }
    
    /**
     * Enable/disable tick sound
     */
    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }
    
    /**
     * Set tick listener
     */
    public void setOnTickListener(OnTickListener listener) {
        this.tickListener = listener;
    }
    
    /**
     * Calculate time for a specific beat number
     * @param beatNumber Beat number (1-based)
     * @param startTime Start time of the song
     * @return Expected time for that beat
     */
    public long getTimeForBeat(int beatNumber, long startTime) {
        return startTime + ((beatNumber - 1) * intervalMs);
    }
    
    /**
     * Get current beat number based on elapsed time
     * @param startTime Start time of the song
     * @return Current beat number
     */
    public int getCurrentBeat(long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        return (int) (elapsed / intervalMs) + 1;
    }
    
    /**
     * Release resources
     */
    public void release() {
        stop();
        toneEngine.release();
    }
}
