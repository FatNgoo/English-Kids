package com.edu.english.magicmelody.core.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * SoundEngine - Main audio engine for Magic Melody
 * Uses SoundPool for low-latency playback with ToneEngine fallback
 */
public class SoundEngine {
    
    private static final String TAG = "SoundEngine";
    private static final int MAX_STREAMS = 10;
    
    // Note names for reference
    public static final String[] NOTE_NAMES = {"C", "D", "E", "F", "G", "A", "B"};
    
    private final Context context;
    private SoundPool soundPool;
    private ToneEngine toneEngine;
    private final Map<Integer, Integer> soundIds; // noteIndex -> soundId
    private boolean useFallback = true; // Start with fallback, try to load samples
    private float volume = 1.0f;
    private boolean isInitialized = false;
    
    // Callbacks
    public interface OnLoadCompleteListener {
        void onLoadComplete(boolean success);
    }
    
    public SoundEngine(Context context) {
        this.context = context;
        this.soundIds = new HashMap<>();
        this.toneEngine = new ToneEngine();
        initSoundPool();
    }
    
    private void initSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        
        soundPool = new SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(attributes)
            .build();
        
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                Log.d(TAG, "Sound loaded: " + sampleId);
            } else {
                Log.w(TAG, "Failed to load sound: " + sampleId);
            }
        });
        
        // Try to load piano samples from assets
        loadPianoSamples();
    }
    
    /**
     * Load piano samples from assets
     * Falls back to ToneEngine if samples not found
     */
    private void loadPianoSamples() {
        try {
            // Try to load piano samples from assets/sounds/piano/
            String[] assetFiles = context.getAssets().list("sounds/piano");
            
            if (assetFiles != null && assetFiles.length >= 7) {
                // Load each note sample
                for (int i = 0; i < 7; i++) {
                    String filename = "sounds/piano/note_" + i + ".ogg";
                    try {
                        int soundId = soundPool.load(
                            context.getAssets().openFd(filename), 1);
                        soundIds.put(i, soundId);
                        useFallback = false;
                    } catch (Exception e) {
                        Log.w(TAG, "Could not load " + filename + ", using fallback");
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "No piano samples found, using ToneEngine fallback");
        }
        
        isInitialized = true;
        
        if (useFallback) {
            Log.d(TAG, "Using ToneEngine fallback for all notes");
        }
    }
    
    /**
     * Play a piano note
     * @param noteIndex 0-6 (C-B)
     */
    public void playNote(int noteIndex) {
        playNote(noteIndex, 300); // Default 300ms
    }
    
    /**
     * Play a piano note with specified duration
     * @param noteIndex 0-6 (C-B)
     * @param durationMs Duration in milliseconds (for fallback)
     */
    public void playNote(int noteIndex, int durationMs) {
        if (noteIndex < 0 || noteIndex > 6) return;
        
        if (!useFallback && soundIds.containsKey(noteIndex)) {
            // Use SoundPool
            Integer soundId = soundIds.get(noteIndex);
            if (soundId != null) {
                soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
            }
        } else {
            // Use ToneEngine fallback
            toneEngine.playNote(noteIndex, durationMs);
        }
    }
    
    /**
     * Play a short melody
     * @param noteIndices Array of note indices
     * @param noteDurationMs Duration for each note
     * @param callback Optional callback when finished
     */
    public void playMelody(int[] noteIndices, int noteDurationMs, Runnable callback) {
        toneEngine.playMelody(noteIndices, noteDurationMs, callback);
    }
    
    /**
     * Play feedback sound for correct/wrong
     */
    public void playCorrectSound() {
        // Play ascending notes C-E-G
        int[] melody = {0, 2, 4};
        playMelody(melody, 150, null);
    }
    
    public void playWrongSound() {
        // Play a discordant sound
        toneEngine.playFrequency(200, 200);
    }
    
    public void playPerfectSound() {
        // Play full chord arpeggio
        int[] melody = {0, 2, 4, 6};
        playMelody(melody, 100, null);
    }
    
    /**
     * Set volume (0.0 - 1.0)
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }
    
    /**
     * Get current volume
     */
    public float getVolume() {
        return volume;
    }
    
    /**
     * Check if using fallback (ToneEngine)
     */
    public boolean isUsingFallback() {
        return useFallback;
    }
    
    /**
     * Check if initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * Stop all sounds
     */
    public void stopAll() {
        if (soundPool != null) {
            soundPool.autoPause();
        }
        if (toneEngine != null) {
            toneEngine.stop();
        }
    }
    
    /**
     * Resume playback
     */
    public void resume() {
        if (soundPool != null) {
            soundPool.autoResume();
        }
    }
    
    /**
     * Release all resources
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (toneEngine != null) {
            toneEngine.release();
            toneEngine = null;
        }
        soundIds.clear();
    }
    
    /**
     * Get note name from index
     */
    public static String getNoteName(int noteIndex) {
        if (noteIndex >= 0 && noteIndex < NOTE_NAMES.length) {
            return NOTE_NAMES[noteIndex];
        }
        return "?";
    }
}
