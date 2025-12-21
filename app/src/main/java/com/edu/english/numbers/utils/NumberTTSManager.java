package com.edu.english.numbers.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;

/**
 * TTS Manager for Numbers lesson
 * Handles text-to-speech for number pronunciation
 */
public class NumberTTSManager {
    
    private static final String TAG = "NumberTTSManager";
    
    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;
    private OnTTSListener listener;
    private Context context;
    
    public interface OnTTSListener {
        void onTTSReady();
        void onSpeechStart();
        void onSpeechDone();
        void onSpeechError(String error);
    }
    
    public NumberTTSManager(Context context, OnTTSListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        initTTS();
    }
    
    private void initTTS() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "US English not available, trying UK English");
                    result = textToSpeech.setLanguage(Locale.UK);
                }
                
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "English TTS not supported");
                    if (listener != null) {
                        listener.onSpeechError("English TTS not supported on this device");
                    }
                    return;
                }
                
                // Configure for clear pronunciation
                textToSpeech.setSpeechRate(0.8f);  // Slower for kids
                textToSpeech.setPitch(1.1f);       // Slightly higher, friendlier
                
                // Set utterance listener
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "TTS started: " + utteranceId);
                        if (listener != null) {
                            listener.onSpeechStart();
                        }
                    }
                    
                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(TAG, "TTS done: " + utteranceId);
                        if (listener != null) {
                            listener.onSpeechDone();
                        }
                    }
                    
                    @Override
                    public void onError(String utteranceId) {
                        Log.e(TAG, "TTS error: " + utteranceId);
                        if (listener != null) {
                            listener.onSpeechError("TTS playback error");
                        }
                    }
                });
                
                isInitialized = true;
                Log.d(TAG, "TTS initialized successfully");
                
                if (listener != null) {
                    listener.onTTSReady();
                }
                
            } else {
                Log.e(TAG, "TTS initialization failed with status: " + status);
                if (listener != null) {
                    listener.onSpeechError("TTS initialization failed");
                }
            }
        });
    }
    
    /**
     * Speak a number word (e.g., "five")
     */
    public void speakNumber(String numberWord) {
        speak(numberWord);
    }
    
    /**
     * Speak encouragement message
     */
    public void speakEncouragement(String message) {
        speak(message);
    }
    
    /**
     * Speak the question
     */
    public void speakQuestion(String question) {
        speak(question);
    }
    
    /**
     * Speak generic text
     */
    public void speak(String text) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized yet");
            return;
        }
        
        if (textToSpeech == null) {
            Log.e(TAG, "TTS is null");
            return;
        }
        
        // Stop any current speech
        stop();
        
        String utteranceId = UUID.randomUUID().toString();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
    
    /**
     * Stop current speech
     */
    public void stop() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
    }
    
    /**
     * Check if TTS is currently speaking
     */
    public boolean isSpeaking() {
        return textToSpeech != null && textToSpeech.isSpeaking();
    }
    
    /**
     * Check if TTS is initialized and ready
     */
    public boolean isReady() {
        return isInitialized && textToSpeech != null;
    }
    
    /**
     * Release TTS resources - MUST call in onDestroy
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        isInitialized = false;
        Log.d(TAG, "TTS shutdown complete");
    }
    
    /**
     * Set a new listener
     */
    public void setListener(OnTTSListener listener) {
        this.listener = listener;
    }
}
