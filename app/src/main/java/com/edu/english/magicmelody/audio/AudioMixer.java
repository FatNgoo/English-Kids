package com.edu.english.magicmelody.audio;

import android.content.Context;

/**
 * 🎛️ Audio Mixer
 * 
 * Central audio management:
 * - Volume control for all audio sources
 * - Audio ducking
 * - Channel management
 */
public class AudioMixer {
    
    private static final String TAG = "AudioMixer";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 CHANNELS
    // ═══════════════════════════════════════════════════════════════
    
    public enum AudioChannel {
        MASTER(1.0f),
        MUSIC(0.7f),
        SFX(1.0f),
        NOTES(0.9f),
        VOICE(1.0f),
        UI(0.8f);
        
        private final float defaultVolume;
        
        AudioChannel(float defaultVolume) {
            this.defaultVolume = defaultVolume;
        }
        
        public float getDefaultVolume() { return defaultVolume; }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 STATE
    // ═══════════════════════════════════════════════════════════════
    
    private Context context;
    
    // Volume levels (0.0 - 1.0)
    private float masterVolume = 1.0f;
    private float musicVolume = 0.7f;
    private float sfxVolume = 1.0f;
    private float notesVolume = 0.9f;
    private float voiceVolume = 1.0f;
    private float uiVolume = 0.8f;
    
    // Mute states
    private boolean isMasterMuted = false;
    private boolean isMusicMuted = false;
    private boolean isSfxMuted = false;
    
    // Audio components
    private SoundPoolManager soundPoolManager;
    private MusicPlayer musicPlayer;
    private NoteAudioPlayer noteAudioPlayer;
    
    // Ducking
    private boolean isDucking = false;
    private float duckingLevel = 0.3f;
    private float preDuckMusicVolume;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ SINGLETON
    // ═══════════════════════════════════════════════════════════════
    
    private static AudioMixer instance;
    
    public static synchronized AudioMixer getInstance() {
        if (instance == null) {
            instance = new AudioMixer();
        }
        return instance;
    }
    
    private AudioMixer() {
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ INITIALIZATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Initialize all audio components
     */
    public void initialize(Context context) {
        this.context = context.getApplicationContext();
        
        // Get/create audio components
        soundPoolManager = SoundPoolManager.getInstance();
        soundPoolManager.initialize(context);
        
        musicPlayer = MusicPlayer.getInstance();
        musicPlayer.initialize(context);
        
        noteAudioPlayer = new NoteAudioPlayer(context);
        
        // Apply initial volumes
        applyAllVolumes();
    }
    
    /**
     * Load audio assets for gameplay
     */
    public void loadGameplayAudio() {
        soundPoolManager.loadSoundsForGameplay();
    }
    
    /**
     * Load all audio assets
     */
    public void loadAllAudio() {
        soundPoolManager.loadAllSounds();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎚️ VOLUME CONTROL
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Set volume for a channel
     */
    public void setVolume(AudioChannel channel, float volume) {
        float clampedVolume = Math.max(0f, Math.min(1f, volume));
        
        switch (channel) {
            case MASTER:
                masterVolume = clampedVolume;
                applyAllVolumes();
                break;
            case MUSIC:
                musicVolume = clampedVolume;
                applyMusicVolume();
                break;
            case SFX:
                sfxVolume = clampedVolume;
                applySfxVolume();
                break;
            case NOTES:
                notesVolume = clampedVolume;
                applyNotesVolume();
                break;
            case VOICE:
                voiceVolume = clampedVolume;
                break;
            case UI:
                uiVolume = clampedVolume;
                break;
        }
    }
    
    /**
     * Get volume for a channel
     */
    public float getVolume(AudioChannel channel) {
        switch (channel) {
            case MASTER: return masterVolume;
            case MUSIC: return musicVolume;
            case SFX: return sfxVolume;
            case NOTES: return notesVolume;
            case VOICE: return voiceVolume;
            case UI: return uiVolume;
            default: return 1.0f;
        }
    }
    
    /**
     * Get effective volume (with master applied)
     */
    public float getEffectiveVolume(AudioChannel channel) {
        if (isMasterMuted) return 0f;
        
        float channelVolume = getVolume(channel);
        return masterVolume * channelVolume;
    }
    
    private void applyAllVolumes() {
        applyMusicVolume();
        applySfxVolume();
        applyNotesVolume();
    }
    
    private void applyMusicVolume() {
        if (musicPlayer != null) {
            musicPlayer.setMasterVolume(masterVolume);
            musicPlayer.setMusicVolume(isDucking ? musicVolume * duckingLevel : musicVolume);
            musicPlayer.setMuted(isMasterMuted || isMusicMuted);
        }
    }
    
    private void applySfxVolume() {
        if (soundPoolManager != null) {
            soundPoolManager.setMasterVolume(masterVolume);
            soundPoolManager.setSfxVolume(sfxVolume);
            soundPoolManager.setMuted(isMasterMuted || isSfxMuted);
        }
    }
    
    private void applyNotesVolume() {
        if (noteAudioPlayer != null) {
            noteAudioPlayer.setVolume(masterVolume * notesVolume);
            noteAudioPlayer.setMuted(isMasterMuted);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔇 MUTE CONTROL
    // ═══════════════════════════════════════════════════════════════
    
    public void setMasterMuted(boolean muted) {
        this.isMasterMuted = muted;
        applyAllVolumes();
    }
    
    public void setMusicMuted(boolean muted) {
        this.isMusicMuted = muted;
        applyMusicVolume();
    }
    
    public void setSfxMuted(boolean muted) {
        this.isSfxMuted = muted;
        applySfxVolume();
    }
    
    public boolean isMasterMuted() { return isMasterMuted; }
    public boolean isMusicMuted() { return isMusicMuted; }
    public boolean isSfxMuted() { return isSfxMuted; }
    
    /**
     * Toggle master mute
     */
    public boolean toggleMasterMute() {
        setMasterMuted(!isMasterMuted);
        return isMasterMuted;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🦆 AUDIO DUCKING
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Start ducking (lower music for voice/important sounds)
     */
    public void startDucking() {
        if (isDucking) return;
        
        isDucking = true;
        preDuckMusicVolume = musicVolume;
        
        if (musicPlayer != null) {
            musicPlayer.fadeVolume(musicVolume * duckingLevel, 300);
        }
    }
    
    /**
     * Stop ducking (restore music volume)
     */
    public void stopDucking() {
        if (!isDucking) return;
        
        isDucking = false;
        
        if (musicPlayer != null) {
            musicPlayer.fadeVolume(preDuckMusicVolume, 500);
        }
    }
    
    /**
     * Set ducking level (0.0 - 1.0)
     */
    public void setDuckingLevel(float level) {
        this.duckingLevel = Math.max(0f, Math.min(1f, level));
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎵 QUICK ACCESS METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Play a sound effect
     */
    public void playSfx(SoundPoolManager.SoundType type) {
        if (soundPoolManager != null) {
            soundPoolManager.play(type);
        }
    }
    
    /**
     * Play a sound effect with volume
     */
    public void playSfx(SoundPoolManager.SoundType type, float volume) {
        if (soundPoolManager != null) {
            soundPoolManager.play(type, volume);
        }
    }
    
    /**
     * Play a musical note
     */
    public void playNote(NoteAudioPlayer.Note note) {
        if (noteAudioPlayer != null) {
            noteAudioPlayer.play(note);
        }
    }
    
    /**
     * Play note for lane
     */
    public void playNoteForLane(int laneIndex, int totalLanes) {
        if (noteAudioPlayer != null) {
            noteAudioPlayer.playForLane(laneIndex, totalLanes);
        }
    }
    
    /**
     * Play hit sound
     */
    public void playHitSound(String hitType) {
        if (soundPoolManager != null) {
            soundPoolManager.playHitSound(hitType);
        }
    }
    
    /**
     * Play combo sound
     */
    public void playComboSound(int combo) {
        if (soundPoolManager != null) {
            soundPoolManager.playComboSound(combo);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎼 MUSIC CONTROL
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Play background music
     */
    public void playMusic(String assetPath) {
        if (musicPlayer != null) {
            musicPlayer.playFromAssets(assetPath);
        }
    }
    
    /**
     * Pause music
     */
    public void pauseMusic() {
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }
    
    /**
     * Resume music
     */
    public void resumeMusic() {
        if (musicPlayer != null) {
            musicPlayer.resume();
        }
    }
    
    /**
     * Stop music
     */
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
    
    /**
     * Fade out music
     */
    public void fadeOutMusic(long durationMs) {
        if (musicPlayer != null) {
            musicPlayer.fadeOutAndPause(durationMs);
        }
    }
    
    /**
     * Fade in music
     */
    public void fadeInMusic(long durationMs) {
        if (musicPlayer != null) {
            musicPlayer.fadeInAndResume(durationMs);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📱 LIFECYCLE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Pause all audio (app going to background)
     */
    public void pauseAll() {
        if (soundPoolManager != null) {
            soundPoolManager.pauseAll();
        }
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }
    
    /**
     * Resume all audio (app coming to foreground)
     */
    public void resumeAll() {
        if (soundPoolManager != null) {
            soundPoolManager.resumeAll();
        }
        if (musicPlayer != null) {
            musicPlayer.resume();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📤 GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public SoundPoolManager getSoundPoolManager() {
        return soundPoolManager;
    }
    
    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }
    
    public NoteAudioPlayer getNoteAudioPlayer() {
        return noteAudioPlayer;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 💾 SETTINGS PERSISTENCE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Save audio settings to preferences
     */
    public void saveSettings(android.content.SharedPreferences prefs) {
        prefs.edit()
            .putFloat("audio_master", masterVolume)
            .putFloat("audio_music", musicVolume)
            .putFloat("audio_sfx", sfxVolume)
            .putFloat("audio_notes", notesVolume)
            .putBoolean("audio_master_muted", isMasterMuted)
            .putBoolean("audio_music_muted", isMusicMuted)
            .putBoolean("audio_sfx_muted", isSfxMuted)
            .apply();
    }
    
    /**
     * Load audio settings from preferences
     */
    public void loadSettings(android.content.SharedPreferences prefs) {
        masterVolume = prefs.getFloat("audio_master", 1.0f);
        musicVolume = prefs.getFloat("audio_music", 0.7f);
        sfxVolume = prefs.getFloat("audio_sfx", 1.0f);
        notesVolume = prefs.getFloat("audio_notes", 0.9f);
        isMasterMuted = prefs.getBoolean("audio_master_muted", false);
        isMusicMuted = prefs.getBoolean("audio_music_muted", false);
        isSfxMuted = prefs.getBoolean("audio_sfx_muted", false);
        
        applyAllVolumes();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    public void release() {
        if (soundPoolManager != null) {
            soundPoolManager.release();
        }
        if (musicPlayer != null) {
            musicPlayer.release();
        }
        if (noteAudioPlayer != null) {
            noteAudioPlayer.release();
        }
    }
}
