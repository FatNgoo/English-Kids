package com.edu.english.coloralchemy;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Sound Manager
 * Handles all audio: SFX, voice, and background music
 */
public class SoundManager implements TextToSpeech.OnInitListener {
    
    private Context context;
    
    // SoundPool for short sound effects
    private SoundPool soundPool;
    private Map<String, Integer> soundIds;
    private Map<String, Integer> streamIds;
    
    // MediaPlayer for background music
    private MediaPlayer musicPlayer;
    private float musicVolume = 0.3f;
    
    // Text-to-Speech for color names
    private TextToSpeech tts;
    private boolean ttsReady;
    
    // Volume levels
    private float sfxVolume = 1.0f;
    private float voiceVolume = 1.0f;
    private boolean isMuted = false;
    
    // Sound effect keys
    public static final String SFX_GLASS_CLINK = "glass_clink";
    public static final String SFX_LIQUID_POUR = "liquid_pour";
    public static final String SFX_BUBBLE = "bubble";
    public static final String SFX_SHAKE = "shake";
    public static final String SFX_SUCCESS = "success";
    public static final String SFX_SPARKLE = "sparkle";
    public static final String SFX_SLIDER = "slider";
    public static final String SFX_TAP = "tap";
    public static final String SFX_DROP = "drop";
    
    public SoundManager(Context context) {
        this.context = context;
        this.soundIds = new HashMap<>();
        this.streamIds = new HashMap<>();
        this.ttsReady = false;
        
        initSoundPool();
        initTTS();
    }
    
    /**
     * Initialize SoundPool for sound effects
     */
    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        
        soundPool = new SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build();
        
        // Load sound effects
        // Note: You'll need to add actual sound files to res/raw/
        // For now, we'll create placeholders and generate tones programmatically
        loadSounds();
    }
    
    /**
     * Load all sound effects
     */
    private void loadSounds() {
        // Try to load sounds from resources if they exist
        // These would be .wav or .ogg files in res/raw/
        try {
            int resId;
            
            resId = context.getResources().getIdentifier("glass_clink", "raw", context.getPackageName());
            if (resId != 0) soundIds.put(SFX_GLASS_CLINK, soundPool.load(context, resId, 1));
            
            resId = context.getResources().getIdentifier("liquid_pour", "raw", context.getPackageName());
            if (resId != 0) soundIds.put(SFX_LIQUID_POUR, soundPool.load(context, resId, 1));
            
            resId = context.getResources().getIdentifier("bubble", "raw", context.getPackageName());
            if (resId != 0) soundIds.put(SFX_BUBBLE, soundPool.load(context, resId, 1));
            
            resId = context.getResources().getIdentifier("shake", "raw", context.getPackageName());
            if (resId != 0) soundIds.put(SFX_SHAKE, soundPool.load(context, resId, 1));
            
            resId = context.getResources().getIdentifier("success", "raw", context.getPackageName());
            if (resId != 0) soundIds.put(SFX_SUCCESS, soundPool.load(context, resId, 1));
            
            resId = context.getResources().getIdentifier("sparkle", "raw", context.getPackageName());
            if (resId != 0) soundIds.put(SFX_SPARKLE, soundPool.load(context, resId, 1));
            
            resId = context.getResources().getIdentifier("slider", "raw", context.getPackageName());
            if (resId != 0) soundIds.put(SFX_SLIDER, soundPool.load(context, resId, 1));
            
            resId = context.getResources().getIdentifier("tap", "raw", context.getPackageName());
            if (resId != 0) soundIds.put(SFX_TAP, soundPool.load(context, resId, 1));
            
            resId = context.getResources().getIdentifier("drop", "raw", context.getPackageName());
            if (resId != 0) soundIds.put(SFX_DROP, soundPool.load(context, resId, 1));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize Text-to-Speech
     */
    private void initTTS() {
        tts = new TextToSpeech(context, this);
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            ttsReady = (result != TextToSpeech.LANG_MISSING_DATA && 
                       result != TextToSpeech.LANG_NOT_SUPPORTED);
            
            if (ttsReady) {
                // Set speech rate for kid-friendly pace
                tts.setSpeechRate(0.85f);
                tts.setPitch(1.1f); // Slightly higher pitch for friendly tone
            }
        }
    }
    
    /**
     * Play a sound effect
     */
    public void playSFX(String soundKey) {
        if (isMuted) return;
        
        Integer soundId = soundIds.get(soundKey);
        if (soundId != null) {
            int streamId = soundPool.play(soundId, sfxVolume, sfxVolume, 1, 0, 1.0f);
            streamIds.put(soundKey, streamId);
        }
    }
    
    /**
     * Play a sound effect with pitch variation
     */
    public void playSFX(String soundKey, float pitch) {
        if (isMuted) return;
        
        Integer soundId = soundIds.get(soundKey);
        if (soundId != null) {
            float clampedPitch = EasingFunctions.clamp(pitch, 0.5f, 2.0f);
            int streamId = soundPool.play(soundId, sfxVolume, sfxVolume, 1, 0, clampedPitch);
            streamIds.put(soundKey, streamId);
        }
    }
    
    /**
     * Play looping sound
     */
    public void playLoopingSFX(String soundKey) {
        if (isMuted) return;
        
        Integer soundId = soundIds.get(soundKey);
        if (soundId != null) {
            int streamId = soundPool.play(soundId, sfxVolume, sfxVolume, 1, -1, 1.0f);
            streamIds.put(soundKey, streamId);
        }
    }
    
    /**
     * Stop a specific sound
     */
    public void stopSFX(String soundKey) {
        Integer streamId = streamIds.get(soundKey);
        if (streamId != null) {
            soundPool.stop(streamId);
            streamIds.remove(soundKey);
        }
    }
    
    /**
     * Stop all sounds
     */
    public void stopAllSFX() {
        for (Integer streamId : streamIds.values()) {
            soundPool.stop(streamId);
        }
        streamIds.clear();
    }
    
    /**
     * Speak text using TTS
     */
    public void speak(String text) {
        if (isMuted || !ttsReady) return;
        
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "color_speak");
    }
    
    /**
     * Speak text after a delay
     */
    public void speakDelayed(final String text, long delayMs) {
        if (isMuted || !ttsReady) return;
        
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                speak(text);
            }
        }, delayMs);
    }
    
    /**
     * Speak color name
     */
    public void speakColorName(String colorName) {
        speak(colorName);
    }
    
    /**
     * Speak mixing result sentence
     */
    public void speakMixResult(String sentence) {
        speak(sentence);
    }
    
    /**
     * Start background music
     */
    public void startBackgroundMusic(int resourceId) {
        stopBackgroundMusic();
        
        try {
            musicPlayer = MediaPlayer.create(context, resourceId);
            if (musicPlayer != null) {
                musicPlayer.setLooping(true);
                musicPlayer.setVolume(musicVolume, musicVolume);
                musicPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (musicPlayer != null) {
            try {
                musicPlayer.stop();
                musicPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            musicPlayer = null;
        }
    }
    
    /**
     * Pause background music
     */
    public void pauseBackgroundMusic() {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.pause();
        }
    }
    
    /**
     * Resume background music
     */
    public void resumeBackgroundMusic() {
        if (musicPlayer != null && !musicPlayer.isPlaying()) {
            musicPlayer.start();
        }
    }
    
    /**
     * Set SFX volume
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = EasingFunctions.clamp(volume, 0, 1);
    }
    
    /**
     * Set music volume
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = EasingFunctions.clamp(volume, 0, 1);
        if (musicPlayer != null) {
            musicPlayer.setVolume(musicVolume, musicVolume);
        }
    }
    
    /**
     * Set voice volume
     */
    public void setVoiceVolume(float volume) {
        this.voiceVolume = EasingFunctions.clamp(volume, 0, 1);
    }
    
    /**
     * Mute all sounds
     */
    public void mute() {
        isMuted = true;
        stopAllSFX();
        pauseBackgroundMusic();
        tts.stop();
    }
    
    /**
     * Unmute all sounds
     */
    public void unmute() {
        isMuted = false;
        resumeBackgroundMusic();
    }
    
    /**
     * Toggle mute state
     */
    public void toggleMute() {
        if (isMuted) {
            unmute();
        } else {
            mute();
        }
    }
    
    /**
     * Check if muted
     */
    public boolean isMuted() {
        return isMuted;
    }
    
    /**
     * Release all resources
     */
    public void release() {
        stopAllSFX();
        stopBackgroundMusic();
        
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
    
    /**
     * Pause all audio
     */
    public void pause() {
        soundPool.autoPause();
        pauseBackgroundMusic();
    }
    
    /**
     * Resume all audio
     */
    public void resume() {
        soundPool.autoResume();
        if (!isMuted) {
            resumeBackgroundMusic();
        }
    }
}
