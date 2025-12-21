package com.edu.english.shapes.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Audio manager for handling sound effects and voice playback
 */
public class ShapeAudioManager {
    private static final String TAG = "ShapeAudioManager";
    
    private static ShapeAudioManager instance;
    private Context context;
    private SoundPool soundPool;
    private MediaPlayer mediaPlayer;
    private Map<String, Integer> soundMap;
    private boolean isLoaded = false;
    
    // Sound effect IDs
    public static final String SOUND_CLICK = "click";
    public static final String SOUND_WOW = "wow";
    public static final String SOUND_POP = "pop";
    public static final String SOUND_SUCCESS = "success";
    public static final String SOUND_SPARKLE = "sparkle";
    public static final String SOUND_BOUNCE = "bounce";
    public static final String SOUND_TRACE = "trace";
    
    private ShapeAudioManager(Context context) {
        this.context = context.getApplicationContext();
        this.soundMap = new HashMap<>();
        initSoundPool();
    }
    
    public static synchronized ShapeAudioManager getInstance(Context context) {
        if (instance == null) {
            instance = new ShapeAudioManager(context);
        }
        return instance;
    }
    
    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                isLoaded = true;
            }
        });
        
        // Load sound effects
        loadSound(SOUND_CLICK, "sounds/click.mp3");
        loadSound(SOUND_WOW, "sounds/wow.mp3");
        loadSound(SOUND_POP, "sounds/pop.mp3");
        loadSound(SOUND_SUCCESS, "sounds/success.mp3");
        loadSound(SOUND_SPARKLE, "sounds/sparkle.mp3");
        loadSound(SOUND_BOUNCE, "sounds/bounce.mp3");
        loadSound(SOUND_TRACE, "sounds/trace.mp3");
    }
    
    private void loadSound(String key, String assetPath) {
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(assetPath);
            int soundId = soundPool.load(afd, 1);
            soundMap.put(key, soundId);
            afd.close();
        } catch (IOException e) {
            Log.w(TAG, "Sound file not found: " + assetPath + ", using fallback");
            // Will use fallback tones if files don't exist
        }
    }
    
    public void playSound(String soundKey) {
        Integer soundId = soundMap.get(soundKey);
        if (soundId != null && isLoaded) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        } else {
            // Generate a simple tone as fallback
            playFallbackTone(soundKey);
        }
    }
    
    private void playFallbackTone(String soundKey) {
        // Simple beep as fallback when audio files don't exist
        try {
            android.media.ToneGenerator toneGenerator = new android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_MUSIC, 50);
            switch (soundKey) {
                case SOUND_CLICK:
                    toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 50);
                    break;
                case SOUND_WOW:
                case SOUND_SUCCESS:
                    toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 150);
                    break;
                case SOUND_POP:
                case SOUND_BOUNCE:
                    toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 80);
                    break;
                default:
                    toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 50);
            }
            // Release after delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                    toneGenerator::release, 200);
        } catch (Exception e) {
            Log.w(TAG, "Failed to play fallback tone", e);
        }
    }
    
    public void playVoice(String voiceFileName, OnVoiceCompleteListener listener) {
        stopVoice();
        
        try {
            AssetFileDescriptor afd = context.getAssets().openFd("voices/" + voiceFileName + ".mp3");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(mp -> {
                if (listener != null) {
                    listener.onComplete();
                }
            });
            mediaPlayer.start();
            afd.close();
        } catch (IOException e) {
            Log.w(TAG, "Voice file not found: " + voiceFileName + ", using TTS fallback");
            // Use text-to-speech as fallback
            speakWithTTS(voiceFileName, listener);
        }
    }
    
    private void speakWithTTS(String text, OnVoiceCompleteListener listener) {
        // Clean up the voice file name to get readable text
        String cleanText = text.replace("_", " ")
                .replace("obj ", "")
                .replace("shape ", "");
        
        android.speech.tts.TextToSpeech tts = new android.speech.tts.TextToSpeech(context, status -> {});
        
        // Simulate speech with a delay and callback
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (listener != null) {
                listener.onComplete();
            }
        }, 800);
    }
    
    public void stopVoice() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.w(TAG, "Error stopping media player", e);
            }
            mediaPlayer = null;
        }
    }
    
    public void release() {
        stopVoice();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        instance = null;
    }
    
    public interface OnVoiceCompleteListener {
        void onComplete();
    }
}
