package com.edu.english.masterchef.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.edu.english.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager for sound effects using SoundPool
 * Preloads all sounds for fast playback
 * NOTE: Sound effects temporarily disabled until proper sound files are added
 */
public class SoundPoolManager {
    
    private static SoundPoolManager instance;
    private SoundPool soundPool;
    private Map<String, Integer> soundMap;
    private boolean isLoaded = false;
    private boolean soundEnabled = false; // TEMPORARILY DISABLED
    private float volume = 0.0f; // MUTED

    // Sound IDs
    public static final String SOUND_CORRECT = "correct";
    public static final String SOUND_WRONG = "wrong";
    public static final String SOUND_CHOP = "chop";
    public static final String SOUND_POUR = "pour";
    public static final String SOUND_SIZZLE = "sizzle";
    public static final String SOUND_CLICK = "click";
    public static final String SOUND_SUCCESS = "success";
    public static final String SOUND_LOCKED = "locked";

    private SoundPoolManager(Context context) {
        // Create SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

        soundPool = new SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build();

        soundMap = new HashMap<>();

        // Load sounds (using existing sounds from the app, or placeholders)
        // For now, we'll use simple system sounds as placeholders
        // In production, you'd load custom sound files from res/raw/
        
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                isLoaded = true;
            }
        });

        // Load placeholder sounds
        // TODO: Replace with actual sound files when available
        loadSounds(context);
    }

    private void loadSounds(Context context) {
        // Load actual sound files from res/raw/
        // Note: These files need to be added to res/raw/ folder
        // For now, using bg_music_gameplay as a placeholder for all sounds
        
        try {
            // When proper sound files are added, uncomment these:
            // soundMap.put(SOUND_CORRECT, soundPool.load(context, R.raw.sound_correct, 1));
            // soundMap.put(SOUND_WRONG, soundPool.load(context, R.raw.sound_wrong, 1));
            // soundMap.put(SOUND_CHOP, soundPool.load(context, R.raw.sound_chop, 1));
            // soundMap.put(SOUND_POUR, soundPool.load(context, R.raw.sound_pour, 1));
            // soundMap.put(SOUND_SIZZLE, soundPool.load(context, R.raw.sound_sizzle, 1));
            // soundMap.put(SOUND_CLICK, soundPool.load(context, R.raw.sound_click, 1));
            // soundMap.put(SOUND_SUCCESS, soundPool.load(context, R.raw.sound_success, 1));
            // soundMap.put(SOUND_LOCKED, soundPool.load(context, R.raw.sound_locked, 1));
            
            // Using existing music file as placeholder
            int placeholderSound = soundPool.load(context, R.raw.bg_music_gameplay, 1);
            soundMap.put(SOUND_CORRECT, placeholderSound);
            soundMap.put(SOUND_WRONG, placeholderSound);
            soundMap.put(SOUND_CHOP, placeholderSound);
            soundMap.put(SOUND_POUR, placeholderSound);
            soundMap.put(SOUND_SIZZLE, placeholderSound);
            soundMap.put(SOUND_CLICK, placeholderSound);
            soundMap.put(SOUND_SUCCESS, placeholderSound);
            soundMap.put(SOUND_LOCKED, placeholderSound);
            
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: mark as loaded anyway
            isLoaded = true;
        }
    }

    public static synchronized SoundPoolManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundPoolManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Play a sound effect
     * NOTE: Temporarily disabled - sounds muted
     */
    public void play(String soundId) {
        if (!soundEnabled || !isLoaded || soundPool == null) {
            return; // Sound disabled
        }

        Integer soundResId = soundMap.get(soundId);
        if (soundResId != null) {
            soundPool.play(soundResId, volume, volume, 1, 0, 1.0f);
        }
    }

    /**
     * Play a sound with custom volume
     * NOTE: Temporarily disabled - sounds muted
     */
    public void play(String soundId, float customVolume) {
        if (!soundEnabled || !isLoaded || soundPool == null) {
            return; // Sound disabled
        }

        Integer soundResId = soundMap.get(soundId);
        if (soundResId != null) {
            soundPool.play(soundResId, customVolume, customVolume, 1, 0, 1.0f);
        }
    }

    /**
     * Set master volume
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    /**
     * Release SoundPool resources
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        soundMap.clear();
        isLoaded = false;
    }

    public boolean isLoaded() {
        return isLoaded;
    }
}
