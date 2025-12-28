package com.edu.english.magicmelody.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 🔊 Sound Pool Manager
 * 
 * Manages short sound effects using SoundPool:
 * - Note hit sounds
 * - UI feedback sounds
 * - Game event sounds
 */
public class SoundPoolManager {
    
    private static final String TAG = "SoundPoolManager";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 SOUND TYPES
    // ═══════════════════════════════════════════════════════════════
    
    public enum SoundType {
        // Hit sounds
        HIT_PERFECT("hit_perfect.ogg"),
        HIT_GOOD("hit_good.ogg"),
        HIT_OK("hit_ok.ogg"),
        HIT_MISS("hit_miss.ogg"),
        
        // Note sounds (musical)
        NOTE_DO("note_do.ogg"),
        NOTE_RE("note_re.ogg"),
        NOTE_MI("note_mi.ogg"),
        NOTE_FA("note_fa.ogg"),
        NOTE_SOL("note_sol.ogg"),
        NOTE_LA("note_la.ogg"),
        NOTE_SI("note_si.ogg"),
        
        // UI sounds
        UI_TAP("ui_tap.ogg"),
        UI_BACK("ui_back.ogg"),
        UI_SUCCESS("ui_success.ogg"),
        UI_ERROR("ui_error.ogg"),
        UI_POPUP("ui_popup.ogg"),
        
        // Game events
        COMBO_5("combo_5.ogg"),
        COMBO_10("combo_10.ogg"),
        COMBO_20("combo_20.ogg"),
        STAR_EARNED("star_earned.ogg"),
        LEVEL_UP("level_up.ogg"),
        ACHIEVEMENT("achievement.ogg"),
        
        // Boss battle
        BOSS_ATTACK("boss_attack.ogg"),
        BOSS_HIT("boss_hit.ogg"),
        BOSS_DEFEAT("boss_defeat.ogg"),
        PLAYER_DAMAGE("player_damage.ogg"),
        
        // Collection
        WORD_COLLECTED("word_collected.ogg"),
        LEGENDARY_COLLECTED("legendary_collected.ogg"),
        
        // Countdown
        COUNTDOWN_TICK("countdown_tick.ogg"),
        COUNTDOWN_GO("countdown_go.ogg");
        
        private final String fileName;
        
        SoundType(String fileName) {
            this.fileName = fileName;
        }
        
        public String getFileName() { return fileName; }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 STATE
    // ═══════════════════════════════════════════════════════════════
    
    private Context context;
    private SoundPool soundPool;
    private final Map<SoundType, Integer> soundIds = new HashMap<>();
    private final Map<SoundType, Boolean> loadedSounds = new HashMap<>();
    
    private boolean isInitialized = false;
    private int loadedCount = 0;
    private int totalSounds = 0;
    
    // Volume settings
    private float masterVolume = 1.0f;
    private float sfxVolume = 1.0f;
    private boolean isMuted = false;
    
    // Sound folder
    private static final String SOUND_FOLDER = "magicmelody/sounds/";
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface SoundPoolListener {
        void onLoadProgress(int loaded, int total);
        void onLoadComplete();
        void onLoadError(String error);
    }
    
    private SoundPoolListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ SINGLETON
    // ═══════════════════════════════════════════════════════════════
    
    private static SoundPoolManager instance;
    
    public static synchronized SoundPoolManager getInstance() {
        if (instance == null) {
            instance = new SoundPoolManager();
        }
        return instance;
    }
    
    private SoundPoolManager() {
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ INITIALIZATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(SoundPoolListener listener) {
        this.listener = listener;
    }
    
    /**
     * Initialize SoundPool
     */
    public void initialize(Context context) {
        this.context = context.getApplicationContext();
        
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        
        soundPool = new SoundPool.Builder()
            .setMaxStreams(10)  // Max concurrent sounds
            .setAudioAttributes(attributes)
            .build();
        
        // Set load complete listener
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            loadedCount++;
            
            if (listener != null) {
                listener.onLoadProgress(loadedCount, totalSounds);
            }
            
            if (loadedCount >= totalSounds) {
                isInitialized = true;
                if (listener != null) {
                    listener.onLoadComplete();
                }
            }
        });
    }
    
    /**
     * Load all sounds
     */
    public void loadAllSounds() {
        totalSounds = SoundType.values().length;
        loadedCount = 0;
        
        for (SoundType type : SoundType.values()) {
            loadSound(type);
        }
    }
    
    /**
     * Load specific sounds for a category
     */
    public void loadSoundsForGameplay() {
        SoundType[] gameplaySounds = {
            SoundType.HIT_PERFECT, SoundType.HIT_GOOD, 
            SoundType.HIT_OK, SoundType.HIT_MISS,
            SoundType.NOTE_DO, SoundType.NOTE_RE, SoundType.NOTE_MI,
            SoundType.NOTE_FA, SoundType.NOTE_SOL, SoundType.NOTE_LA, SoundType.NOTE_SI,
            SoundType.COMBO_5, SoundType.COMBO_10, SoundType.COMBO_20,
            SoundType.STAR_EARNED, SoundType.COUNTDOWN_TICK, SoundType.COUNTDOWN_GO
        };
        
        totalSounds = gameplaySounds.length;
        loadedCount = 0;
        
        for (SoundType type : gameplaySounds) {
            loadSound(type);
        }
    }
    
    /**
     * Load a single sound
     */
    private void loadSound(SoundType type) {
        try {
            String path = SOUND_FOLDER + type.getFileName();
            AssetFileDescriptor afd = context.getAssets().openFd(path);
            int soundId = soundPool.load(afd, 1);
            soundIds.put(type, soundId);
            loadedSounds.put(type, false);
            afd.close();
        } catch (IOException e) {
            Log.w(TAG, "Sound file not found: " + type.getFileName() + ", using placeholder");
            // Create a placeholder - in production, ensure all files exist
            loadedSounds.put(type, true);
            loadedCount++;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔊 PLAYBACK
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Play a sound
     */
    public int play(SoundType type) {
        return play(type, 1.0f, 1.0f, 0, 0, 1.0f);
    }
    
    /**
     * Play a sound with volume
     */
    public int play(SoundType type, float volume) {
        return play(type, volume, volume, 0, 0, 1.0f);
    }
    
    /**
     * Play a sound with pitch
     */
    public int playWithPitch(SoundType type, float pitch) {
        return play(type, 1.0f, 1.0f, 0, 0, pitch);
    }
    
    /**
     * Play a sound with full control
     */
    public int play(SoundType type, float leftVolume, float rightVolume, 
                    int priority, int loop, float rate) {
        if (soundPool == null || isMuted) return 0;
        
        Integer soundId = soundIds.get(type);
        if (soundId == null) return 0;
        
        float finalVolume = masterVolume * sfxVolume;
        float left = leftVolume * finalVolume;
        float right = rightVolume * finalVolume;
        
        // Clamp rate (pitch) between 0.5 and 2.0
        float clampedRate = Math.max(0.5f, Math.min(2.0f, rate));
        
        return soundPool.play(soundId, left, right, priority, loop, clampedRate);
    }
    
    /**
     * Play hit sound based on timing
     */
    public void playHitSound(String hitType) {
        switch (hitType.toUpperCase()) {
            case "PERFECT":
                play(SoundType.HIT_PERFECT);
                break;
            case "GOOD":
                play(SoundType.HIT_GOOD);
                break;
            case "OK":
                play(SoundType.HIT_OK);
                break;
            case "MISS":
                play(SoundType.HIT_MISS, 0.7f);
                break;
        }
    }
    
    /**
     * Play combo milestone sound
     */
    public void playComboSound(int combo) {
        if (combo == 5) {
            play(SoundType.COMBO_5);
        } else if (combo == 10) {
            play(SoundType.COMBO_10);
        } else if (combo >= 20 && combo % 10 == 0) {
            play(SoundType.COMBO_20);
        }
    }
    
    /**
     * Play musical note
     */
    public void playNote(int noteIndex, float pitch) {
        SoundType[] notes = {
            SoundType.NOTE_DO, SoundType.NOTE_RE, SoundType.NOTE_MI,
            SoundType.NOTE_FA, SoundType.NOTE_SOL, SoundType.NOTE_LA, SoundType.NOTE_SI
        };
        
        int index = noteIndex % notes.length;
        playWithPitch(notes[index], pitch);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎚️ VOLUME CONTROL
    // ═══════════════════════════════════════════════════════════════
    
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    public float getMasterVolume() {
        return masterVolume;
    }
    
    public float getSfxVolume() {
        return sfxVolume;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⏹️ STREAM CONTROL
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Stop a specific stream
     */
    public void stop(int streamId) {
        if (soundPool != null && streamId != 0) {
            soundPool.stop(streamId);
        }
    }
    
    /**
     * Pause a stream
     */
    public void pause(int streamId) {
        if (soundPool != null && streamId != 0) {
            soundPool.pause(streamId);
        }
    }
    
    /**
     * Resume a stream
     */
    public void resume(int streamId) {
        if (soundPool != null && streamId != 0) {
            soundPool.resume(streamId);
        }
    }
    
    /**
     * Pause all streams
     */
    public void pauseAll() {
        if (soundPool != null) {
            soundPool.autoPause();
        }
    }
    
    /**
     * Resume all streams
     */
    public void resumeAll() {
        if (soundPool != null) {
            soundPool.autoResume();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE
    // ═══════════════════════════════════════════════════════════════
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public float getLoadProgress() {
        if (totalSounds == 0) return 0f;
        return (float) loadedCount / totalSounds;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Release resources
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        
        soundIds.clear();
        loadedSounds.clear();
        isInitialized = false;
        listener = null;
    }
    
    /**
     * Unload a specific sound
     */
    public void unload(SoundType type) {
        Integer soundId = soundIds.get(type);
        if (soundId != null && soundPool != null) {
            soundPool.unload(soundId);
            soundIds.remove(type);
            loadedSounds.remove(type);
        }
    }

    /**
     * Unload unused sounds to free memory
     * Keeps essential UI and hit sounds loaded
     */
    public void unloadUnusedSounds() {
        if (soundPool == null) return;
        
        // Keep essential sounds, unload others
        for (SoundType type : SoundType.values()) {
            // Keep essential sounds
            if (type == SoundType.UI_TAP || 
                type == SoundType.HIT_PERFECT ||
                type == SoundType.HIT_GOOD ||
                type == SoundType.HIT_OK ||
                type == SoundType.HIT_MISS) {
                continue;
            }
            
            // Unload non-essential sounds
            Integer soundId = soundIds.get(type);
            if (soundId != null) {
                soundPool.unload(soundId);
                soundIds.remove(type);
                loadedSounds.remove(type);
            }
        }
        
        Log.d(TAG, "🧹 Unloaded unused sounds to free memory");
    }
}
