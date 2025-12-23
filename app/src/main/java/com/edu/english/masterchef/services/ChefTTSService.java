package com.edu.english.masterchef.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

/**
 * Text-to-Speech service for Chef instructions in Master Chef game.
 */
public class ChefTTSService implements TextToSpeech.OnInitListener {
    
    public interface TTSListener {
        void onSpeakStart(String text);
        void onSpeakDone(String text);
        void onTTSReady();
        void onTTSError(String error);
    }
    
    private TextToSpeech textToSpeech;
    private TTSListener listener;
    private Handler handler;
    private boolean isInitialized;
    private boolean isMuted;
    private String currentText;
    
    public ChefTTSService(Context context) {
        this.handler = new Handler(Looper.getMainLooper());
        this.isInitialized = false;
        this.isMuted = false;
        this.textToSpeech = new TextToSpeech(context.getApplicationContext(), this);
    }
    
    public void setListener(TTSListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            
            if (result == TextToSpeech.LANG_MISSING_DATA || 
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                notifyError("English language not supported");
                return;
            }
            
            // Configure for child-friendly speech
            textToSpeech.setSpeechRate(0.85f);  // Slightly slower
            textToSpeech.setPitch(1.1f);        // Slightly higher pitch
            
            // Set utterance listener
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    handler.post(() -> {
                        if (listener != null && currentText != null) {
                            listener.onSpeakStart(currentText);
                        }
                    });
                }
                
                @Override
                public void onDone(String utteranceId) {
                    handler.post(() -> {
                        if (listener != null && currentText != null) {
                            listener.onSpeakDone(currentText);
                        }
                    });
                }
                
                @Override
                public void onError(String utteranceId) {
                    handler.post(() -> notifyError("Speech error"));
                }
            });
            
            isInitialized = true;
            handler.post(() -> {
                if (listener != null) {
                    listener.onTTSReady();
                }
            });
        } else {
            notifyError("TTS initialization failed");
        }
    }
    
    /**
     * Speak the given text as the chef.
     */
    public void speak(String text) {
        if (!isInitialized || isMuted || text == null || text.isEmpty()) {
            return;
        }
        
        currentText = text;
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "chef_" + System.currentTimeMillis());
    }
    
    /**
     * Speak instruction text (the main chef dialogue).
     */
    public void speakInstruction(String text) {
        speak(text);
    }
    
    /**
     * Speak success/praise text.
     */
    public void speakSuccess(String text) {
        speak(text);
    }
    
    /**
     * Speak a phrase for the user to repeat (pronunciation guide).
     */
    public void speakPhraseForRepeat(String phrase) {
        if (!isInitialized || isMuted) return;
        
        currentText = phrase;
        // Speak slower for pronunciation practice
        float originalRate = 0.85f;
        textToSpeech.setSpeechRate(0.7f);
        textToSpeech.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "phrase_" + System.currentTimeMillis());
        
        // Reset rate after speaking
        handler.postDelayed(() -> textToSpeech.setSpeechRate(originalRate), 2000);
    }
    
    /**
     * Speak encouragement after wrong attempt.
     */
    public void speakEncouragement() {
        speak("Let's try again! Listen carefully.");
    }
    
    /**
     * Stop any current speech.
     */
    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }
    
    /**
     * Set mute state.
     */
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (muted) {
            stop();
        }
    }
    
    /**
     * Toggle mute state.
     * @return new mute state
     */
    public boolean toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            stop();
        }
        return isMuted;
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    /**
     * Configure voice parameters based on selected chef character.
     * @param speechRate The speech rate (0.5 - 2.0, default 1.0)
     * @param pitch The pitch (0.5 - 2.0, default 1.0)
     */
    public void setVoiceConfig(float speechRate, float pitch) {
        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(speechRate);
            textToSpeech.setPitch(pitch);
        }
    }
    
    public boolean isReady() {
        return isInitialized;
    }
    
    /**
     * Release resources. Call in Activity's onDestroy.
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        isInitialized = false;
    }
    
    private void notifyError(String error) {
        if (listener != null) {
            listener.onTTSError(error);
        }
    }
}
