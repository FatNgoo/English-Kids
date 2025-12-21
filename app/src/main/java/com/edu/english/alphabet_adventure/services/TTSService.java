package com.edu.english.alphabet_adventure.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Service for Text-to-Speech functionality in the Alphabet Adventure game.
 * Handles auto-speak every 5 seconds and manual replay.
 */
public class TTSService implements TextToSpeech.OnInitListener {

    private static final int AUTO_SPEAK_INTERVAL = 5000; // 5 seconds
    
    private TextToSpeech textToSpeech;
    private Handler handler;
    private String currentWord;
    private boolean isInitialized;
    private boolean isMuted;
    private boolean isPaused;
    private Runnable autoSpeakRunnable;

    public TTSService(Context context) {
        this.handler = new Handler(Looper.getMainLooper());
        this.isInitialized = false;
        this.isMuted = false;
        this.isPaused = false;
        this.textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && 
                result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true;
                
                // Set speech rate slower for children to hear better
                textToSpeech.setSpeechRate(0.5f);  // Very slow for clearest pronunciation
                textToSpeech.setPitch(1.1f);
            }
        }
    }

    /**
     * Set the current word and start auto-speak timer.
     * @param word The word to speak
     */
    public void setWord(String word) {
        this.currentWord = word;
        speakNow();
        startAutoSpeak();
    }

    /**
     * Speak the current word immediately.
     */
    public void speakNow() {
        if (isInitialized && currentWord != null && !isMuted && !isPaused) {
            textToSpeech.speak(currentWord, TextToSpeech.QUEUE_FLUSH, null, "word_speak");
        }
    }

    /**
     * Speak a specific text immediately.
     * @param text Text to speak
     */
    public void speak(String text) {
        if (isInitialized && !isMuted) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "custom_speak");
        }
    }

    /**
     * Start the auto-speak timer (speaks every 5 seconds).
     */
    public void startAutoSpeak() {
        stopAutoSpeak();
        
        autoSpeakRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused && !isMuted && currentWord != null) {
                    speakNow();
                    handler.postDelayed(this, AUTO_SPEAK_INTERVAL);
                }
            }
        };
        
        handler.postDelayed(autoSpeakRunnable, AUTO_SPEAK_INTERVAL);
    }

    /**
     * Stop the auto-speak timer.
     */
    public void stopAutoSpeak() {
        if (autoSpeakRunnable != null) {
            handler.removeCallbacks(autoSpeakRunnable);
            autoSpeakRunnable = null;
        }
    }

    /**
     * Pause TTS and auto-speak timer.
     */
    public void pause() {
        isPaused = true;
        stopAutoSpeak();
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    /**
     * Resume TTS and auto-speak timer.
     */
    public void resume() {
        isPaused = false;
        if (currentWord != null) {
            startAutoSpeak();
        }
    }

    /**
     * Set mute state.
     * @param muted true to mute, false to unmute
     */
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (muted && textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    /**
     * Toggle mute state.
     * @return new mute state
     */
    public boolean toggleMute() {
        isMuted = !isMuted;
        if (isMuted && textToSpeech != null) {
            textToSpeech.stop();
        }
        return isMuted;
    }

    public boolean isMuted() {
        return isMuted;
    }

    /**
     * Release TTS resources. Must be called when activity is destroyed.
     */
    public void shutdown() {
        stopAutoSpeak();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }
}
